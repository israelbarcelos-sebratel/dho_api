package br.com.sebratel.bff.dho.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentIndicatorsDTO;


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

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).APPROVE_CANDIDATE.name())")
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        recruitmentProcessService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).REJECT_CANDIDATE.name())")
    public ResponseEntity<Void> refuse(@PathVariable Integer id, @RequestBody @Valid InterviewDecisionDTO dto) {
        recruitmentProcessService.refuse(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).WITHDRAW_CANDIDATE.name())")
    public ResponseEntity<Void> withdraw(@PathVariable Integer id) {
        recruitmentProcessService.withdraw(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).FINAL_DECISION.name())")
    public ResponseEntity<Void> hire(@PathVariable Integer id) {
        recruitmentProcessService.hire(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).VIEW_JOB_TRACKING.name())")
    public ResponseEntity<List<RecruitmentProcessHistoryDTO>> getHistory() {
        return ResponseEntity.ok(recruitmentProcessService.getFinalizedProcesses());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).RQ10.name())")
    public ResponseEntity<List<RecruitmentProcessResponseDTO>> getMyProcesses(@RequestParam Integer recruiterId) {
        return ResponseEntity.ok(recruitmentProcessService.getProcessesByRecruiter(recruiterId));
    }
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).RQ13.name())")
    public ResponseEntity<List<RecruitmentProcessLogDTO>> getLogs(@PathVariable Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getLogs(id));
    }



    @GetMapping("/indicators")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).VIEW_INDICATORS.name())")
    public ResponseEntity<RecruitmentIndicatorsDTO> getIndicators() {
        return ResponseEntity.ok(recruitmentProcessService.getIndicators());
    }

}
