package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.dto.PermissionRequestDTO;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;

import java.util.List;

public interface DhoPermissionService {
    List<PermissionResponseDTO> findAll();
    PermissionResponseDTO create(PermissionRequestDTO request);
}
