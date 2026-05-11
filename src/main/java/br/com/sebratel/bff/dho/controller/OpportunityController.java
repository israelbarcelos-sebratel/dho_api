package br.com.sebratel.bff.dho.controller;
import org.springframework.security.access.prepost.PreAuthorize;

import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;

import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;


import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Oportunidades", description = "Endpoints para gestão de oportunidades de emprego")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    @PreAuthorize("hasAuthority('RQ05') or hasAuthority('RQ02')")
    @Operation(summary = "Listar todas as oportunidades", description = "Retorna uma lista com todas as oportunidades cadastradas.")
    public ResponseEntity<List<OpportunityResponseDTO>> getAllOpportunities() {
        return ResponseEntity.ok(opportunityService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RQ01') or hasAuthority('RQ06')")
    @Operation(summary = "Criar nova oportunidade", description = "Cadastra uma nova oportunidade no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Oportunidade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<OpportunityResponseDTO> createOpportunity(@RequestBody @Valid OpportunityRequestDTO opportunityRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(opportunityRequestDTO));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('RQ06')")
    @Operation(summary = "Aprovar oportunidade", description = "Altera o status da oportunidade para aprovada.")
    public ResponseEntity<OpportunityResponseDTO> approveOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.approve(id));
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority('RQ06')")
    @Operation(summary = "Recusar oportunidade", description = "Altera o status da oportunidade para recusada.")
    public ResponseEntity<OpportunityResponseDTO> refuseOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.refuse(id, approvalDTO));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority('RQ08')")
    @Operation(summary = "Finalizar oportunidade", description = "Marca a oportunidade como finalizada.")
    public ResponseEntity<OpportunityResponseDTO> finalizeOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.finalize(id, approvalDTO));
    }

    @GetMapping("/{id}/candidates")
    @PreAuthorize("hasAuthority('RQ11')")
    @Operation(summary = "Listar candidatos de uma oportunidade", description = "Retorna os candidatos associados a uma oportunidade específica.")
    public ResponseEntity<List<CandidateResponseDTO>> getCandidatesByOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.findCandidatesByOpportunityId(id));
    }

}
