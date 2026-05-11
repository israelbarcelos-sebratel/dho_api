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

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Oportunidades", description = "Endpoints para gestão de oportunidades de emprego")
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_all_requests.name()) or hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Listar todas as oportunidades", description = "Retorna uma lista com todas as oportunidades cadastradas.")
    public ResponseEntity<List<OpportunityResponseDTO>> getAllOpportunities() {
        return ResponseEntity.ok(opportunityService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name()) or hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_contract_process.name())")
    @Operation(summary = "Criar nova oportunidade", description = "Cadastra uma nova oportunidade no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Oportunidade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<OpportunityResponseDTO> createOpportunity(@RequestBody @Valid OpportunityRequestDTO opportunityRequestDTO, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(opportunityRequestDTO, authentication));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_contract_process.name())")
    @Operation(summary = "Aprovar oportunidade", description = "Altera o status da oportunidade para aprovada.")
    public ResponseEntity<OpportunityResponseDTO> approveOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.approve(id));
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_contract_process.name())")
    @Operation(summary = "Recusar oportunidade", description = "Altera o status da oportunidade para recusada.")
    public ResponseEntity<OpportunityResponseDTO> refuseOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.refuse(id, approvalDTO));
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).final_decision.name())")
    @Operation(summary = "Finalizar oportunidade", description = "Marca a oportunidade como finalizada.")
    public ResponseEntity<OpportunityResponseDTO> finalizeOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.finalize(id, approvalDTO));
    }

    @GetMapping("/{id}/candidates")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_pipeline.name())")
    @Operation(summary = "Listar candidatos de uma oportunidade", description = "Retorna os candidatos associados a uma oportunidade específica.")
    public ResponseEntity<List<CandidateResponseDTO>> getCandidatesByOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.findCandidatesByOpportunityId(id));
    }

}
