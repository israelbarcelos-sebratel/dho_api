package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.dto.RoleRequestCreateDTO;
import br.com.sebratel.bff.dho.dto.RoleRequestResponseDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DhoRoleRequestService {
    RoleRequestResponseDTO createRequest(RoleRequestCreateDTO request, Authentication authentication);
    List<RoleRequestResponseDTO> findAll();
    RoleRequestResponseDTO approveRequest(Integer requestId);
    RoleRequestResponseDTO rejectRequest(Integer requestId);
}
