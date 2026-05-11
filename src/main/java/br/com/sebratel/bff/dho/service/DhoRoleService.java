package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.dto.RoleRequestDTO;
import br.com.sebratel.bff.dho.dto.RoleResponseDTO;

import java.util.List;
import java.util.Set;

public interface DhoRoleService {
    List<RoleResponseDTO> findAll();
    RoleResponseDTO create(RoleRequestDTO request);
    RoleResponseDTO assignPermissions(Integer roleId, Set<Integer> permissionIds);
}
