package br.com.sebratel.bff.dho.service.impl;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRoleRequest;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRequestRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.RoleRequestCreateDTO;
import br.com.sebratel.bff.dho.dto.RoleRequestResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DhoRoleRequestServiceImpl implements DhoRoleRequestService {

    private static final Logger log = LoggerFactory.getLogger(DhoRoleRequestServiceImpl.class);

    private final DhoRoleRequestRepository roleRequestRepository;
    private final PeopleRepository peopleRepository;
    private final DhoRoleRepository roleRepository;

    @Override
    @Transactional
    public RoleRequestResponseDTO createRequest(RoleRequestCreateDTO request, Authentication authentication) {
        String email = authentication.getName();
        log.info("[DhoRoleRequestService] [createRequest] - Action: Iniciando solicitação de papel para usuário: {}", email);

        People person = peopleRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[DhoRoleRequestService] [createRequest] - Error: Usuário não encontrado com email: {}", email);
                    return new RuntimeException("Usuário não encontrado");
                });

        // RN: Um usuário pode ter apenas uma role
        if (person.getRoles() != null && !person.getRoles().isEmpty()) {
            log.warn("[DhoRoleRequestService] [createRequest] - Warning: Usuário {} já possui um papel ativo. Não é permitido solicitar outro.", email);
            throw new RuntimeException("Você já possui um papel atribuído. Caso deseje alterá-lo, entre em contato com o administrador.");
        }

        // RN: Verificar se já existe uma solicitação pendente
        boolean hasPendingRequest = roleRequestRepository.existsByPersonAndStatus(person, "PENDING");
        if (hasPendingRequest) {
            log.warn("[DhoRoleRequestService] [createRequest] - Warning: Usuário {} já possui uma solicitação pendente.", email);
            throw new RuntimeException("Você já possui uma solicitação de papel pendente de análise.");
        }

        DhoRole role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> {
                    log.error("[DhoRoleRequestService] [createRequest] - Error: Papel ID {} não encontrado", request.getRoleId());
                    return new RuntimeException("Papel não encontrado");
                });

        DhoRoleRequest roleRequest = DhoRoleRequest.builder()
                .person(person)
                .role(role)
                .status("PENDING")
                .requestDate(LocalDateTime.now())
                .build();

        DhoRoleRequest savedRequest = roleRequestRepository.save(roleRequest);
        log.info("[DhoRoleRequestService] [createRequest] - Action: Solicitação criada com sucesso. ID: {} | Papel: {}", savedRequest.getId(), role.getName());

        return mapToResponse(savedRequest);
    }

    @Override
    public List<RoleRequestResponseDTO> findAll() {
        log.info("[DhoRoleRequestService] [findAll] - Action: Buscando todas as solicitações pendentes");
        List<RoleRequestResponseDTO> requests = roleRequestRepository.findByStatus("PENDING").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.info("[DhoRoleRequestService] [findAll] - Action: Retornando {} solicitações pendentes", requests.size());
        return requests;
    }

    @Override
    @Transactional
    public RoleRequestResponseDTO approveRequest(Integer requestId) {
        log.info("[DhoRoleRequestService] [approveRequest] - Action: Aprovando solicitação ID: {}", requestId);

        DhoRoleRequest roleRequest = roleRequestRepository.findByIdWithPersonAndRoles(requestId)
                .orElseThrow(() -> {
                    log.error("[DhoRoleRequestService] [approveRequest] - Error: Solicitação ID {} não encontrada", requestId);
                    return new RuntimeException("Solicitação não encontrada");
                });

        if (!"PENDING".equals(roleRequest.getStatus())) {
            log.warn("[DhoRoleRequestService] [approveRequest] - Warning: Solicitação ID {} já processada com status: {}", requestId, roleRequest.getStatus());
            throw new RuntimeException("Solicitação já processada");
        }

        roleRequest.setStatus("APPROVED");
        roleRequest.setResolutionDate(LocalDateTime.now());

        People person = roleRequest.getPerson();
        DhoRole requestedRole = roleRequest.getRole();

        log.info("[DhoRoleRequestService] [approveRequest] - Action: Atribuindo papel '{}' para o usuário '{}'", requestedRole.getName(), person.getEmail());

        // RN: Garantir apenas UMA role (limpa as atuais e adiciona a nova)
        if (person.getRoles() == null) {
            person.setRoles(new HashSet<>());
        } else {
            person.getRoles().clear();
        }
        person.getRoles().add(requestedRole);
        peopleRepository.save(person);

        RoleRequestResponseDTO response = mapToResponse(roleRequest);
        roleRequestRepository.delete(roleRequest);
        
        log.info("[DhoRoleRequestService] [approveRequest] - Action: Solicitação ID {} aprovada e removida com sucesso", requestId);
        return response;
    }

    @Override
    @Transactional
    public RoleRequestResponseDTO rejectRequest(Integer requestId) {
        log.info("[DhoRoleRequestService] [rejectRequest] - Action: Rejeitando solicitação ID: {}", requestId);

        DhoRoleRequest roleRequest = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("[DhoRoleRequestService] [rejectRequest] - Error: Solicitação ID {} não encontrada", requestId);
                    return new RuntimeException("Solicitação não encontrada");
                });

        if (!"PENDING".equals(roleRequest.getStatus())) {
            log.warn("[DhoRoleRequestService] [rejectRequest] - Warning: Solicitação ID {} já processada com status: {}", requestId, roleRequest.getStatus());
            throw new RuntimeException("Solicitação já processada");
        }

        roleRequest.setStatus("REJECTED");
        roleRequest.setResolutionDate(LocalDateTime.now());

        RoleRequestResponseDTO response = mapToResponse(roleRequest);
        roleRequestRepository.delete(roleRequest);
        
        log.info("[DhoRoleRequestService] [rejectRequest] - Action: Solicitação ID {} rejeitada e removida com sucesso", requestId);
        return response;
    }

    private RoleRequestResponseDTO mapToResponse(DhoRoleRequest roleRequest) {
        return RoleRequestResponseDTO.builder()
                .id(roleRequest.getId())
                .personId(roleRequest.getPerson().getId())
                .personName(roleRequest.getPerson().getName())
                .personEmail(roleRequest.getPerson().getEmail())
                .roleId(roleRequest.getRole().getId())
                .roleName(roleRequest.getRole().getName())
                .status(roleRequest.getStatus())
                .requestDate(roleRequest.getRequestDate())
                .resolutionDate(roleRequest.getResolutionDate())
                .build();
    }
}
