package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
