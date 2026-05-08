package br.com.sebratel.bff.dho.controller;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;


import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;

    @GetMapping
    public ResponseEntity<List<OpportunityResponseDTO>> getAllOpportunities() {
        return ResponseEntity.ok(opportunityService.findAll());
    }

    @PostMapping
    public ResponseEntity<OpportunityResponseDTO> createOpportunity(@RequestBody @Valid OpportunityRequestDTO opportunityRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(opportunityRequestDTO));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OpportunityResponseDTO> approveOpportunity(@PathVariable Integer id) {
        return ResponseEntity.ok(opportunityService.approve(id));
    }

    @PostMapping("/{id}/refuse")
    public ResponseEntity<OpportunityResponseDTO> refuseOpportunity(
            @PathVariable Integer id,
            @RequestBody @Valid OpportunityApprovalDTO approvalDTO) {
        return ResponseEntity.ok(opportunityService.refuse(id, approvalDTO));
    }
}
