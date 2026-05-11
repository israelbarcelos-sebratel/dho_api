package br.com.sebratel.bff.dho.controller.admin;

import br.com.sebratel.bff.dho.dto.RoleRequestResponseDTO;
import br.com.sebratel.bff.dho.service.DhoRoleRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles/requests")
@RequiredArgsConstructor
@Tag(name = "Admin - Solicitações de Papéis", description = "Gerenciamento de solicitações de papéis")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleRequestController {

    private final DhoRoleRequestService roleRequestService;

    @GetMapping
    @Operation(summary = "Listar todas as solicitações de papéis")
    public ResponseEntity<List<RoleRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(roleRequestService.findAll());
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Aprovar uma solicitação de papel")
    public ResponseEntity<RoleRequestResponseDTO> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(roleRequestService.approveRequest(id));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Rejeitar uma solicitação de papel")
    public ResponseEntity<RoleRequestResponseDTO> reject(@PathVariable Integer id) {
        return ResponseEntity.ok(roleRequestService.rejectRequest(id));
    }
}
