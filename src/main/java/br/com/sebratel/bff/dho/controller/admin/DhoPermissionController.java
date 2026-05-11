package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.PermissionRequestDTO;
import br.com.sebratel.bff.dho.dto.PermissionResponseDTO;
import br.com.sebratel.bff.dho.service.DhoPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Admin - Permissões", description = "Gerenciamento de permissões do sistema")
@PreAuthorize("hasRole('ADMIN')")
public class DhoPermissionController {

    private final DhoPermissionService permissionService;

    @GetMapping
    @Operation(summary = "Listar todas as permissões")
    public ResponseEntity<List<PermissionResponseDTO>> getAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar uma nova permissão")
    public ResponseEntity<PermissionResponseDTO> create(@RequestBody PermissionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }
}
