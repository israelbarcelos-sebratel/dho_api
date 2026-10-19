package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/opportunity-statuses")
@RequiredArgsConstructor
@Tag(name = "Status de Oportunidade", description = "Endpoints para consulta de status possíveis de uma oportunidade")
public class OpportunityStatusController {

    private final DhoOpportunityStatusRepository opportunityStatusRepository;

    @GetMapping
    @Operation(summary = "Listar todos os status de oportunidade", description = "Retorna uma lista com todos os status de oportunidade cadastrados.")
    public ResponseEntity<List<DhoOpportunityStatus>> getAll() {
        return ResponseEntity.ok(opportunityStatusRepository.findAll());
    }
}
