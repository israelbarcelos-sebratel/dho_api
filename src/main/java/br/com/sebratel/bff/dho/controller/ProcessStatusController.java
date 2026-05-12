package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/process-statuses")
@RequiredArgsConstructor
@Tag(name = "Status de Processo", description = "Endpoints para consulta de status possíveis de um processo de recrutamento")
public class ProcessStatusController {

    private final DhoProcessStatusRepository processStatusRepository;

    @GetMapping
    @Operation(summary = "Listar todos os status de processo", description = "Retorna uma lista com todos os status de processo de recrutamento cadastrados.")
    public ResponseEntity<List<DhoProcessStatus>> getAll() {
        return ResponseEntity.ok(processStatusRepository.findAll());
    }
}
