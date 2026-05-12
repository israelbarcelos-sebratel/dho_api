package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;

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

import br.com.sebratel.bff.dho.dto.RequisitionSearchDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/requisitions")
@RequiredArgsConstructor
@Tag(name = "Requisições", description = "Endpoints para gestores acompanharem suas requisições")
public class RequisitionController {

    private final OpportunityService opportunityService;

    @PostMapping
    @Operation(summary = "Listar requisições do gestor", description = "Retorna as requisições criadas pelo usuário autenticado. Admins podem usar showAllRequisitions: true para ver tudo.")
    public ResponseEntity<List<OpportunityResponseDTO>> getAll(@RequestBody(required = false) RequisitionSearchDTO searchDTO, Authentication authentication) {
        if (searchDTO == null) {
            searchDTO = new RequisitionSearchDTO();
        }
        return ResponseEntity.ok(opportunityService.findAllForUser(authentication, searchDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de uma requisição", description = "Retorna os detalhes de uma requisição específica.")
    public ResponseEntity<OpportunityResponseDTO> getById(@PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(opportunityService.findByIdForUser(id, authentication));
    }

    @GetMapping("/{id}/candidates")
    @Operation(summary = "Listar candidatos de uma requisição aprovada", description = "Retorna os candidatos vinculados a uma vaga aprovada. Requer ser o solicitante ou ter permissão admin.")
    public ResponseEntity<List<CandidateResponseDTO>> getCandidates(@PathVariable Integer id, Authentication authentication) {
        return ResponseEntity.ok(opportunityService.findCandidatesForUser(id, authentication));
    }

}
