package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.repository.DhoBaseOriginRepository;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;

import br.com.sebratel.bff.dho.domain.repository.*;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.sebratel.bff.dho.domain.enums.Permission;
import br.com.sebratel.bff.dho.dto.UserResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import br.com.sebratel.bff.dho.dto.RequisitionSearchDTO;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final DhoOpportunityStatusRepository statusRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final PeopleRepository peopleRepository;
    private final DhoBaseOriginRepository baseOriginRepository;
    private final DhoPositionRepository positionRepository;
    private final DhoTeamRepository teamRepository;
    private final DhoDepartmentRepository departmentRepository;
    private final RecruitmentProcessLogRepository logRepository;

    private final DhoOpportunityMotiveRepository opportunityMotiveRepository;

    public List<CandidateResponseDTO> findCandidatesForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        if (!"Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A requisição deve estar aprovada para ver os candidatos");
        }

        boolean isAdmin = hasPermission(authentication, Permission.view_all_requests);
        boolean isRequester = opportunity.getRequester() != null && opportunity.getRequester().getEmail().equals(authentication.getName());

        if (!isAdmin && !isRequester) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta requisição");
        }

        return findCandidatesByOpportunityId(id);
    }

    public List<CandidateResponseDTO> findCandidatesByOpportunityId(Integer id) {
        return recruitmentProcessRepository.findByOpportunityId(id).stream()
                .map(rp -> CandidateResponseDTO.builder()
                        .id(rp.getCandidate().getId())
                        .name(rp.getCandidate().getName())
                        .processStage(mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatus(mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                        .build())
                .collect(Collectors.toList());
    }

    public List<OpportunityResponseDTO> findAll() {
        return opportunityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    public List<OpportunityResponseDTO> findApprovedOpportunities() {
        return opportunityRepository.findByOpportunityStatusName("Aprovada").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public OpportunityResponseDTO findById(Integer id) {
        return opportunityRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
    }

    @Transactional
    public OpportunityResponseDTO create(OpportunityRequestDTO dto, Authentication authentication) {
        DhoOpportunityStatus pendingStatus = statusRepository.findByName("Pendente")
                .orElseThrow(() -> new RuntimeException("Status 'Pendente' não encontrado"));

        People requester = Optional.ofNullable(authentication)
                .map(Authentication::getName)
                .flatMap(peopleRepository::findByEmail)
                .orElse(null);

        DhoBaseOrigin baseOrigin = Optional.ofNullable(dto.baseOriginId())
                .flatMap(baseOriginRepository::findById)
                .or(() -> baseOriginRepository.findByName("Porto Alegre"))
                .orElseThrow(() -> new RuntimeException("Base de origem padrão não encontrada"));

        Opportunity opportunity = Opportunity.builder()
                .openOpportunityDate(Optional.ofNullable(dto.openOpportunityDate()).orElse(LocalDateTime.now()))
                .requester(requester)
                .candidate(findOptionalEntity(dto.candidateId(), peopleRepository))
                .position(findOptionalEntity(dto.positionId(), positionRepository))
                .team(findOptionalEntity(dto.teamId(), teamRepository))
                .department(findOptionalEntity(dto.departmentId(), departmentRepository))
                .opportunityMotive(findOptionalEntity(dto.opportunityMotiveId(), opportunityMotiveRepository))
                .replacedPerson(findOptionalEntity(dto.replacedPersonId(), peopleRepository))
                .responsibleRecruiter(findOptionalEntity(dto.responsibleRecruiterId(), peopleRepository))
                .baseOrigin(baseOrigin)
                .opportunityStatus(pendingStatus)
                .deadlineSlaDays(dto.deadlineSlaDays())
                .acceptDate(dto.acceptDate())
                .observations(dto.observations())
                .workSchedule(dto.workSchedule())
                .hardSkills(dto.hardSkills())
                .softSkills(dto.softSkills())
                .build();

        return convertToDTO(opportunityRepository.save(opportunity));
    }

    private <T, ID> T findOptionalEntity(ID id, org.springframework.data.repository.CrudRepository<T, ID> repository) {
        return Optional.ofNullable(id)
                .flatMap(repository::findById)
                .orElse(null);
    }

    @Transactional
    public OpportunityResponseDTO approve(Integer id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if ("Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta oportunidade já está aprovada");
        }

        DhoOpportunityStatus approvedStatus = statusRepository.findByName("Aprovada")
                .orElseThrow(() -> new RuntimeException("Status 'Aprovada' não encontrado"));

        opportunity.setOpportunityStatus(approvedStatus);
        opportunity.setDeadlineSlaDays(30);
        
        return convertToDTO(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityResponseDTO refuse(Integer id, OpportunityApprovalDTO dto) {
        if (dto.justification() == null || dto.justification().length() < 200) {
            throw new RuntimeException("A justificativa de recusa deve ter no mínimo 200 caracteres");
        }

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus refusedStatus = statusRepository.findByName("Recusada")
                .orElseThrow(() -> new RuntimeException("Status 'Recusada' não encontrado"));

        opportunity.setOpportunityStatus(refusedStatus);
        opportunity.setRefusalJustification(dto.justification());
        return convertToDTO(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityResponseDTO finalize(Integer id, OpportunityApprovalDTO dto) {
        if (dto.justification() == null || dto.justification().length() < 200) {
            throw new RuntimeException("A justificativa de finalização deve ter no mínimo 200 caracteres");
        }

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus finalizedStatus = statusRepository.findByName("Finalizada")
                .orElseThrow(() -> new RuntimeException("Status 'Finalizada' não encontrado"));

        opportunity.setOpportunityStatus(finalizedStatus);
        opportunity.setFinalizationJustification(dto.justification());
        return convertToDTO(opportunityRepository.save(opportunity));
    }

    private OpportunityResponseDTO convertToDTO(Opportunity opportunity) {
        String statusName = mapName(opportunity.getOpportunityStatus(), DhoOpportunityStatus::getName);
        String uiStatus = "Pendente".equals(statusName) ? "Enviado para aprovação" : statusName;
        
        String statusVariant = "warning";
        if ("Aprovada".equals(statusName)) statusVariant = "success";
        if ("Recusada".equals(statusName)) statusVariant = "danger";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = opportunity.getOpenOpportunityDate() != null 
            ? opportunity.getOpenOpportunityDate().format(formatter) 
            : "";

        UserResponseDTO requesterDTO = null;
        if (opportunity.getRequester() != null) {
            requesterDTO = UserResponseDTO.builder()
                    .id(opportunity.getRequester().getId())
                    .name(opportunity.getRequester().getName())
                    .email(opportunity.getRequester().getEmail())
                    .build();
        }

        return OpportunityResponseDTO.builder()
                .id(opportunity.getId())
                .openOpportunityDate(opportunity.getOpenOpportunityDate())
                .positionName(mapName(opportunity.getPosition(), DhoPosition::getName))
                .teamName(mapName(opportunity.getTeam(), DhoTeam::getName))
                .departmentName(mapName(opportunity.getDepartment(), DhoDepartment::getName))
                .opportunityMotiveName(mapName(opportunity.getOpportunityMotive(), DhoOpportunityMotive::getName))
                .baseOriginName(mapName(opportunity.getBaseOrigin(), DhoBaseOrigin::getName))
                .opportunityStatusName(statusName)
                .processStageName(null)
                .processStatusName(null)
                .deadlineSlaDays(opportunity.getDeadlineSlaDays())
                .acceptDate(opportunity.getAcceptDate())
                .responsibleRecruiterName(mapName(opportunity.getResponsibleRecruiter(), People::getName))
                .observations(opportunity.getObservations())
                .refusalJustification(opportunity.getRefusalJustification())
                .finalizationJustification(opportunity.getFinalizationJustification())
                .workSchedule(opportunity.getWorkSchedule())
                .hardSkills(opportunity.getHardSkills())
                .softSkills(opportunity.getSoftSkills())
                .title(mapName(opportunity.getPosition(), DhoPosition::getName))
                .type(opportunity.getWorkSchedule())
                .team(mapName(opportunity.getTeam(), DhoTeam::getName))
                .status(uiStatus)
                .date(formattedDate)
                .statusVariant(statusVariant)
                .requester(requesterDTO)
                .build();
    }

    public List<OpportunityResponseDTO> findAllForUser(Authentication authentication, RequisitionSearchDTO searchDTO) {
        boolean canViewAll = hasPermission(authentication, Permission.view_all_requests);
        boolean wantViewAll = searchDTO != null && Boolean.TRUE.equals(searchDTO.getShowAllRequisitions());

        if (canViewAll && wantViewAll) {
            return findAll();
        }
        String email = authentication != null ? authentication.getName() : "";
        return opportunityRepository.findByRequesterEmail(email).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OpportunityResponseDTO findByIdForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (authentication == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        if (!hasPermission(authentication, Permission.view_all_requests)) {
            if (opportunity.getRequester() == null || !opportunity.getRequester().getEmail().equals(authentication.getName())) {
                throw new RuntimeException("Acesso negado a esta requisição");
            }
        }

        return convertToDTO(opportunity);
    }

    private boolean hasPermission(Authentication authentication, Permission permission) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(permission.name()) || a.equals("ROLE_ADMIN"));
    }

    private <T, R> R mapName(T entity, Function<T, R> mapper) {
        return Optional.ofNullable(entity).map(mapper).orElse(null);
    }

    public List<RecruitmentProcessLogDTO> getLogs(Integer id) {
        return logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(id).stream()
                .map(log -> RecruitmentProcessLogDTO.builder()
                        .id(log.getId())
                        .actionName(log.getActionName())
                        .startTime(log.getStartTime())
                        .endTime(log.getEndTime())
                        .durationMs(log.getDurationMs())
                        .status(log.getStatus())
                        .errorMessage(log.getErrorMessage())
                        .candidateName(log.getRecruitmentProcess() != null && log.getRecruitmentProcess().getCandidate() != null 
                                ? log.getRecruitmentProcess().getCandidate().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
