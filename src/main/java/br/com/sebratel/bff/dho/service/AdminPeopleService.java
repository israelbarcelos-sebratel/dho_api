package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.dto.UserRolesResponseDTO;

import java.util.List;
import java.util.Set;

public interface AdminPeopleService {
    List<UserRolesResponseDTO> findAll();
    UserRolesResponseDTO assignRoles(Integer peopleId, Set<Integer> roleIds);
}
