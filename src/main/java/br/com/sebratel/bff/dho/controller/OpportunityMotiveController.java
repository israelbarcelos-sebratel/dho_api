package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityMotive;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityMotiveRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opportunity-motives")
@RequiredArgsConstructor
@Tag(name = "Motivos de Oportunidade", description = "Endpoints para consulta de motivos de abertura de vagas")
public class OpportunityMotiveController {

    private final DhoOpportunityMotiveRepository opportunityMotiveRepository;

    @GetMapping
    @Operation(summary = "Listar todos os motivos de oportunidade", description = "Retorna uma lista com todos os motivos cadastrados no sistema.")
    public ResponseEntity<List<DhoOpportunityMotive>> getAllOpportunityMotives() {
        return ResponseEntity.ok(opportunityMotiveRepository.findAll());
    }
}
