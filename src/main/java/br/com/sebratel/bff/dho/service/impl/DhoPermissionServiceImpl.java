package br.com.sebratel.bff.dho.service.impl;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPermission;
import br.com.sebratel.bff.dho.domain.repository.DhoPermissionRepository;
import br.com.sebratel.bff.dho.dto.PermissionRequestDTO;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.service.DhoPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DhoPermissionServiceImpl implements DhoPermissionService {

    private final DhoPermissionRepository permissionRepository;

    @Override
    public List<PermissionResponseDTO> findAll() {
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponseDTO create(PermissionRequestDTO request) {
        DhoPermission permission = DhoPermission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToResponse(permissionRepository.save(permission));
    }

    private PermissionResponseDTO mapToResponse(DhoPermission permission) {
        return PermissionResponseDTO.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
