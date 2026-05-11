package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.RoleResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Papéis", description = "Endpoints para consulta de papéis disponíveis")
public class RoleController {

    private final DhoRoleService roleService;

    @GetMapping
    @Operation(summary = "Listar todos os papéis disponíveis", description = "Retorna uma lista com todos os papéis cadastrados no sistema.")
    public ResponseEntity<List<RoleResponseDTO>> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }
}
