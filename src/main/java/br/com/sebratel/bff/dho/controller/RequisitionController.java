package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/requisitions")
@RequiredArgsConstructor
@Tag(name = "Requisições", description = "Endpoints para gestores acompanharem suas requisições")
public class RequisitionController {

    private final OpportunityService opportunityService;

    @GetMapping
    @Operation(summary = "Listar requisições do gestor", description = "Retorna as requisições criadas pelo usuário autenticado ou todas se for admin/DHO.")
    public ResponseEntity<List<OpportunityResponseDTO>> getAll(Authentication authentication) {
        return ResponseEntity.ok(opportunityService.findAllForUser(authentication));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de uma requisição", description = "Retorna os detalhes de uma requisição específica.")
    public ResponseEntity<OpportunityResponseDTO> getById(@PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(opportunityService.findByIdForUser(id, authentication));
    }
}
