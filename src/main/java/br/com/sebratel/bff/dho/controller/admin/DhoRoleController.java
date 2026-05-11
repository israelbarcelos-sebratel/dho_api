package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.RoleRequestDTO;
import br.com.sebratel.bff.dho.dto.RoleResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Admin - Papéis (Roles)", description = "Gerenciamento de papéis e suas permissões")
@PreAuthorize("hasRole('ADMIN')")
public class DhoRoleController {

    private final DhoRoleService roleService;

    @GetMapping
    @Operation(summary = "Listar todos os papéis")
    public ResponseEntity<List<RoleResponseDTO>> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar um novo papel")
    public ResponseEntity<RoleResponseDTO> create(@RequestBody RoleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Atribuir permissões a um papel")
    public ResponseEntity<RoleResponseDTO> assignPermissions(@PathVariable Integer id, @RequestBody Set<Integer> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissions(id, permissionIds));
    }
}
