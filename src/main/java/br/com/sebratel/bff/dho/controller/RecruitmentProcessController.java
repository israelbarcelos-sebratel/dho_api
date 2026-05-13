package br.com.sebratel.bff.dho.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentIndicatorsDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessRequestDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStageDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStatusDTO;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import br.com.sebratel.bff.dho.service.RecruitmentProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruitment-processes")
@RequiredArgsConstructor
@Tag(name = "Processos de Recrutamento", description = "Endpoints para gestão do fluxo de contratação e candidatos")
public class RecruitmentProcessController {

    private final RecruitmentProcessService recruitmentProcessService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    @Operation(summary = "Iniciar processo de recrutamento", description = "Vincula um candidato a uma oportunidade e inicia o fluxo de contratação.")
    public ResponseEntity<RecruitmentProcessResponseDTO> create(@RequestBody @Valid RecruitmentProcessRequestDTO dto) {
        return ResponseEntity.ok(recruitmentProcessService.create(dto));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Aprovar candidato na etapa atual", description = "Avança o candidato para a próxima fase do processo.")
    public ResponseEntity<Void> approve(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).reject_candidate.name())")
    @Operation(summary = "Reprovar candidato", description = "Encerra o processo para o candidato com uma justificativa.")
    public ResponseEntity<Void> refuse(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id, @RequestBody @Valid InterviewDecisionDTO dto) {
        recruitmentProcessService.refuse(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).withdraw_candidate.name())")
    @Operation(summary = "Registrar desistência", description = "Marca que o candidato desistiu do processo seletivo.")
    public ResponseEntity<Void> withdraw(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.withdraw(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).final_decision.name())")
    @Operation(summary = "Efetivar contratação", description = "Finaliza o processo com a contratação do candidato.")
    public ResponseEntity<Void> hire(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.hire(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/move-to-interview")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Mover para entrevista", description = "Altera a etapa do processo para Entrevista.")
    public ResponseEntity<Void> moveToInterview(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.moveToInterview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/move-to-technical-test")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Mover para teste técnico", description = "Altera a etapa do processo para Teste Técnico.")
    public ResponseEntity<Void> moveToTechnicalTest(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.moveToTechnicalTest(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/move-to-screening")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Mover para triagem", description = "Move o candidato do Banco de Talentos para a etapa de Triagem.")
    public ResponseEntity<Void> moveToScreening(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.moveToScreening(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/manager-decision")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).reject_candidate.name())")
    @Operation(summary = "Decisão do gestor", description = "Registra o feedback/decisão do gestor sobre o candidato.")
    public ResponseEntity<Void> managerDecision(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id, @RequestBody br.com.sebratel.bff.dho.dto.ManagerDecisionDTO dto) {
        recruitmentProcessService.managerDecision(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/proposal")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    @Operation(summary = "Enviar proposta", description = "Registra que a proposta salarial foi enviada ao candidato.")
    public ResponseEntity<Void> sendProposal(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        recruitmentProcessService.sendProposal(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/candidate-decision")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    @Operation(summary = "Decisão do candidato", description = "Registra se o candidato aceitou ou recusou a proposta.")
    public ResponseEntity<Void> candidateDecision(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id, @RequestBody br.com.sebratel.bff.dho.dto.CandidateDecisionDTO dto) {
        recruitmentProcessService.candidateDecision(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Atualizar etapa manualmente", description = "Altera a etapa atual do processo seletivo.")
    public ResponseEntity<Void> updateStage(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id, @RequestBody @Valid RecruitmentProcessStageDTO dto) {
        recruitmentProcessService.updateStage(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    @Operation(summary = "Atualizar status manualmente", description = "Altera o status (Em andamento, pausado, etc) do processo.")
    public ResponseEntity<Void> updateStatus(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id, @RequestBody @Valid RecruitmentProcessStatusDTO dto) {
        recruitmentProcessService.updateStatus(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Listar histórico de processos", description = "Retorna todos os processos de recrutamento finalizados.")
    public ResponseEntity<List<RecruitmentProcessHistoryDTO>> getHistory() {
        return ResponseEntity.ok(recruitmentProcessService.getFinalizedProcesses());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).list_linked_processes.name())")
    @Operation(summary = "Listar meus processos", description = "Retorna os processos vinculados ao recrutador autenticado.")
    public ResponseEntity<List<RecruitmentProcessResponseDTO>> getMyProcesses(Authentication authentication) {
        return ResponseEntity.ok(recruitmentProcessService.getProcessesByRecruiterEmail(authentication.getName()));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).event_log.name())")
    @Operation(summary = "Obter logs do processo", description = "Retorna o histórico de eventos e mudanças de um processo específico.")
    public ResponseEntity<List<RecruitmentProcessLogDTO>> getLogs(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getLogs(id));
    }

    @GetMapping("/indicators")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_indicators.name())")
    @Operation(summary = "Obter indicadores de recrutamento", description = "Retorna estatísticas gerais sobre os processos de recrutamento.")
    public ResponseEntity<RecruitmentIndicatorsDTO> getIndicators() {
        return ResponseEntity.ok(recruitmentProcessService.getIndicators());
    }
}
