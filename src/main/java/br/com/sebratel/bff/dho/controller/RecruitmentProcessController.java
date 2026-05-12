package br.com.sebratel.bff.dho.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentIndicatorsDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessRequestDTO;
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
