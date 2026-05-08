package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
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
    public ResponseEntity<Void> approve(@PathVariable Integer id) {
        recruitmentProcessService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/refuse")
    public ResponseEntity<Void> refuse(@PathVariable Integer id, @RequestBody @Valid InterviewDecisionDTO dto) {
        recruitmentProcessService.refuse(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable Integer id) {
        recruitmentProcessService.withdraw(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/hire")
    public ResponseEntity<Void> hire(@PathVariable Integer id) {
        recruitmentProcessService.hire(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<RecruitmentProcessHistoryDTO>> getHistory() {
        return ResponseEntity.ok(recruitmentProcessService.getFinalizedProcesses());
    }

}
