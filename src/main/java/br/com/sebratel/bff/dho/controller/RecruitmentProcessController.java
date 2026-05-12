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


import java.util.List;
import br.com.sebratel.bff.dho.service.RecruitmentProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruitment-processes")
@RequiredArgsConstructor
public class RecruitmentProcessController {

    private final RecruitmentProcessService recruitmentProcessService;

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    public ResponseEntity<RecruitmentProcessResponseDTO> create(@RequestBody @Valid RecruitmentProcessRequestDTO dto) {
        return ResponseEntity.ok(recruitmentProcessService.create(dto));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        recruitmentProcessService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).reject_candidate.name())")
    public ResponseEntity<Void> refuse(@PathVariable Integer id, @RequestBody @Valid InterviewDecisionDTO dto) {
        recruitmentProcessService.refuse(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).withdraw_candidate.name())")
    public ResponseEntity<Void> withdraw(@PathVariable Integer id) {
        recruitmentProcessService.withdraw(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).final_decision.name())")
    public ResponseEntity<Void> hire(@PathVariable Integer id) {
        recruitmentProcessService.hire(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}/move-to-interview")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    public ResponseEntity<Void> moveToInterview(@PathVariable Integer id) {
        recruitmentProcessService.moveToInterview(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/move-to-technical-test")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    public ResponseEntity<Void> moveToTechnicalTest(@PathVariable Integer id) {
        recruitmentProcessService.moveToTechnicalTest(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/manager-decision")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).reject_candidate.name())")
    public ResponseEntity<Void> managerDecision(@PathVariable Integer id, @RequestBody br.com.sebratel.bff.dho.dto.ManagerDecisionDTO dto) {
        recruitmentProcessService.managerDecision(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/proposal")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    public ResponseEntity<Void> sendProposal(@PathVariable Integer id) {
        recruitmentProcessService.sendProposal(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/candidate-decision")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).initiate_contract_process.name())")
    public ResponseEntity<Void> candidateDecision(@PathVariable Integer id, @RequestBody br.com.sebratel.bff.dho.dto.CandidateDecisionDTO dto) {
        recruitmentProcessService.candidateDecision(id, dto);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    public ResponseEntity<Void> updateStage(@PathVariable Integer id, @RequestBody @Valid RecruitmentProcessStageDTO dto) {
        recruitmentProcessService.updateStage(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).approve_candidate.name())")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id, @RequestBody @Valid RecruitmentProcessStatusDTO dto) {
        recruitmentProcessService.updateStatus(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    public ResponseEntity<List<RecruitmentProcessHistoryDTO>> getHistory() {
        return ResponseEntity.ok(recruitmentProcessService.getFinalizedProcesses());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).list_linked_processes.name())")
    public ResponseEntity<List<RecruitmentProcessResponseDTO>> getMyProcesses(@RequestParam Integer recruiterId) {
        return ResponseEntity.ok(recruitmentProcessService.getProcessesByRecruiter(recruiterId));
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).event_log.name())")
    public ResponseEntity<List<RecruitmentProcessLogDTO>> getLogs(@PathVariable Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getLogs(id));
    }

    @GetMapping("/indicators")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_indicators.name())")
    public ResponseEntity<RecruitmentIndicatorsDTO> getIndicators() {
        return ResponseEntity.ok(recruitmentProcessService.getIndicators());
    }
}
