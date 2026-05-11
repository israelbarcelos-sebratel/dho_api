package br.com.sebratel.bff.dho.service.impl;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.dto.RoleResponseDTO;
import br.com.sebratel.bff.dho.dto.UserRolesResponseDTO;
import br.com.sebratel.bff.dho.service.AdminPeopleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPeopleServiceImpl implements AdminPeopleService {

    private final PeopleRepository peopleRepository;
    private final DhoRoleRepository roleRepository;

    @Override
    public List<UserRolesResponseDTO> findAll() {
        return peopleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserRolesResponseDTO assignRoles(Integer peopleId, Set<Integer> roleIds) {
        People person = peopleRepository.findById(peopleId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<DhoRole> roles = roleRepository.findAllById(roleIds);
        person.setRoles(new HashSet<>(roles));

        return mapToResponse(peopleRepository.save(person));
    }

    private UserRolesResponseDTO mapToResponse(People person) {
        Set<RoleResponseDTO> roleDTOs = null;
        if (person.getRoles() != null) {
            roleDTOs = person.getRoles().stream()
                    .map(role -> RoleResponseDTO.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .description(role.getDescription())
                            .permissions(role.getPermissions() != null ? role.getPermissions().stream()
                                    .map(p -> PermissionResponseDTO.builder()
                                            .id(p.getId())
                                            .name(p.getName())
                                            .description(p.getDescription())
                                            .build())
                                    .collect(Collectors.toSet()) : null)
                            .build())
                    .collect(Collectors.toSet());
        }

        return UserRolesResponseDTO.builder()
                .id(person.getId())
                .name(person.getName())
                .email(person.getEmail())
                .roles(roleDTOs)
                .build();
    }
}
