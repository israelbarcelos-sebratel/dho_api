package br.com.sebratel.bff.dho.service.impl;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPermission;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.dto.RoleRequestDTO;
import br.com.sebratel.bff.dho.dto.RoleResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DhoRoleServiceImpl implements DhoRoleService {

    private final DhoRoleRepository roleRepository;
    private final DhoPermissionRepository permissionRepository;

    @Override
    public List<RoleResponseDTO> findAll() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO create(RoleRequestDTO request) {
        DhoRole role = DhoRole.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDTO assignPermissions(Integer roleId, Set<Integer> permissionIds) {
        DhoRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Papel não encontrado"));
        
        List<DhoPermission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(new HashSet<>(permissions));
        
        return mapToResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public RoleResponseDTO addPermission(Integer roleId, Integer permissionId) {
        DhoRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Papel não encontrado"));

        DhoPermission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }
        role.getPermissions().add(permission);

        return mapToResponse(roleRepository.save(role));
    }

    private RoleResponseDTO mapToResponse(DhoRole role) {
        Set<PermissionResponseDTO> permissionDTOs = null;
        if (role.getPermissions() != null) {
            permissionDTOs = role.getPermissions().stream()
                    .map(p -> PermissionResponseDTO.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .build())
                    .collect(Collectors.toSet());
        }

        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissionDTOs)
                .build();
    }
}
