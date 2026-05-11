package br.com.sebratel.bff.dho.controller;
import org.springframework.security.access.prepost.PreAuthorize;


import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;

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
    @PreAuthorize("hasAuthority('RQ14')")
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        recruitmentProcessService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refuse")
    @PreAuthorize("hasAuthority('RQ14')")
    public ResponseEntity<Void> refuse(@PathVariable Integer id, @RequestBody @Valid InterviewDecisionDTO dto) {
        recruitmentProcessService.refuse(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('RQ14')")
    public ResponseEntity<Void> withdraw(@PathVariable Integer id) {
        recruitmentProcessService.withdraw(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAuthority('RQ17')")
    public ResponseEntity<Void> hire(@PathVariable Integer id) {
        recruitmentProcessService.hire(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('RQ09')")
    public ResponseEntity<List<RecruitmentProcessHistoryDTO>> getHistory() {
        return ResponseEntity.ok(recruitmentProcessService.getFinalizedProcesses());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('RQ10')")
    public ResponseEntity<List<RecruitmentProcessResponseDTO>> getMyProcesses(@RequestParam Integer recruiterId) {
        return ResponseEntity.ok(recruitmentProcessService.getProcessesByRecruiter(recruiterId));
    }
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('RQ13')")
    public ResponseEntity<List<RecruitmentProcessLogDTO>> getLogs(@PathVariable Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getLogs(id));
    }


}
