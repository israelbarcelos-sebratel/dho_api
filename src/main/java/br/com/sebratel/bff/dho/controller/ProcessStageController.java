package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/process-stages")
@RequiredArgsConstructor
@Tag(name = "Estágios de Processo", description = "Endpoints para consulta de estágios possíveis de um processo de recrutamento")
public class ProcessStageController {

    private final DhoProcessStageRepository processStageRepository;

    @GetMapping
    @Operation(summary = "Listar todos os estágios de processo", description = "Retorna uma lista com todos os estágios de processo de recrutamento cadastrados no banco de dados.")
    public ResponseEntity<List<DhoProcessStage>> getAll() {
        return ResponseEntity.ok(processStageRepository.findAll());
    }
}
