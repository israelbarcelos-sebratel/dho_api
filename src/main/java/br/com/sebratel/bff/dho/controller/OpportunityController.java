package br.com.sebratel.bff.dho.controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityAssignRecruiterDTO;


import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;



import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import br.com.sebratel.bff.dho.dto.OpportunityPipelineResponseDTO;

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
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
@Tag(name = "Oportunidades", description = "Endpoints para gestão de oportunidades de emprego")
@Slf4j
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_all_requests.name()) or hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Listar todas as oportunidades", description = "Retorna uma lista com todas as oportunidades cadastradas.")
    public ResponseEntity<List<OpportunityResponseDTO>> getAllOpportunities() {
        return ResponseEntity.ok(opportunityService.findAll());
    }
    @GetMapping("/approved")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_all_requests.name()) or hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Listar oportunidades aprovadas", description = "Retorna uma lista com todas as oportunidades que possuem status 'Aprovada' e estão vinculadas ao recrutador autenticado.")
    public ResponseEntity<List<OpportunityResponseDTO>> getApprovedOpportunities(Authentication authentication) {
        return ResponseEntity.ok(opportunityService.findApprovedOpportunities(authentication));
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
    public ResponseEntity<OpportunityResponseDTO> approveOpportunity(@PathVariable Integer id, @RequestBody @Valid OpportunityApprovalDTO dto) {
        return ResponseEntity.ok(opportunityService.approve(id, dto));
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_contract_process.name())")
    @Operation(summary = "Recusar oportunidade", description = "Altera o status da oportunidade para recusada.")
    public ResponseEntity<OpportunityResponseDTO> refuseOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.refuse(id, approvalDTO));
    }

    @PostMapping("/{id}/reprove")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_contract_process.name())")
    @Operation(summary = "Reprovar oportunidade", description = "Altera o status da oportunidade para recusada.")
    public ResponseEntity<OpportunityResponseDTO> reproveOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        log.info("[OpportunityController] reproveOpportunity - Request received to reprove opportunity ID: {}", id);
        OpportunityResponseDTO response = opportunityService.refuse(id, approvalDTO);
        log.info("[OpportunityController] reproveOpportunity - Opportunity ID: {} successfully reproved", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).final_decision.name())")
    @Operation(summary = "Finalizar oportunidade", description = "Marca a oportunidade como finalizada.")
    public ResponseEntity<OpportunityResponseDTO> finalizeOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.finalize(id, approvalDTO));
    }

    @PostMapping("/{id}/assign-recruiter")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).assign_recruiter.name())")
    @Operation(summary = "Atribuir recrutador", description = "Vincular ou trocar a recrutadora responsável por uma oportunidade.")
    public ResponseEntity<OpportunityResponseDTO> assignRecruiter(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityAssignRecruiterDTO dto) {
        log.info("[CONTROLLER ENTRY] POST /opportunities/{}/assign-recruiter - recruiterId: {}", id, dto.recruiterId());
        OpportunityResponseDTO response = opportunityService.assignRecruiter(id, dto);
        log.info("[CONTROLLER EXIT] Recrutador atribuído com sucesso à oportunidade {}", id);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_pipeline.name())")
    @Operation(summary = "Listar candidatos de uma oportunidade", description = "Retorna os candidatos associados a uma oportunidade específica.")
    public ResponseEntity<List<CandidateResponseDTO>> getCandidatesByOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.findCandidatesByOpportunityId(id));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_pipeline.name())")
    @Operation(summary = "Listar logs de uma oportunidade", description = "Retorna o histórico completo de eventos de uma oportunidade e seus candidatos.")
    public ResponseEntity<List<RecruitmentProcessLogDTO>> getLogs(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.getLogs(id));
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_pipeline.name())")
    @Operation(summary = "Obter pipeline de todas as oportunidades do recrutador", description = "Retorna uma lista de oportunidades com seus respectivos processos e candidatos, filtrando por aquelas onde o usuário autenticado é o recrutador responsável.")
    public ResponseEntity<List<OpportunityPipelineResponseDTO>> getMyOpportunitiesPipeline(Authentication authentication) {
        return ResponseEntity.ok(opportunityService.getOpportunitiesPipelineForRecruiter(authentication));
    }

    @GetMapping("/{id}/pipeline")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_pipeline.name())")
    @Operation(summary = "Obter pipeline da oportunidade", description = "Retorna a estrutura da oportunidade com seus processos e candidatos para visualização no pipeline.")
    public ResponseEntity<OpportunityPipelineResponseDTO> getOpportunityPipeline(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.getOpportunityPipeline(id));
    }
}
