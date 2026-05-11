package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.RoleRequestCreateDTO;
import br.com.sebratel.bff.dho.dto.RoleRequestResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles/requests")
@RequiredArgsConstructor
@Tag(name = "Papéis - Solicitações", description = "Endpoints para usuários solicitarem papéis")
public class DhoRoleRequestController {

    private final DhoRoleRequestService roleRequestService;

    @PostMapping
    @Operation(summary = "Solicitar um papel")
    public ResponseEntity<RoleRequestResponseDTO> create(@RequestBody RoleRequestCreateDTO request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleRequestService.createRequest(request, authentication));
    }
}
