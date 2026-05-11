package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.UserRolesResponseDTO;
import br.com.sebratel.bff.dho.service.AdminPeopleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/people")
@RequiredArgsConstructor
@Tag(name = "Admin - Usuários e Papéis", description = "Gerenciamento de atribuição de papéis a usuários")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPeopleController {

    private final AdminPeopleService adminPeopleService;

    @GetMapping
    @Operation(summary = "Listar usuários e seus papéis")
    public ResponseEntity<List<UserRolesResponseDTO>> getAll() {
        return ResponseEntity.ok(adminPeopleService.findAll());
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Atribuir papéis a um usuário")
    public ResponseEntity<UserRolesResponseDTO> assignRoles(@PathVariable Integer id, @RequestBody Set<Integer> roleIds) {
        return ResponseEntity.ok(adminPeopleService.assignRoles(id, roleIds));
    }
}
