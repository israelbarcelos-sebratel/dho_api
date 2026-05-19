package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import br.com.sebratel.bff.dho.domain.repository.DhoPositionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
@Tag(name = "Cargos", description = "Endpoints para consulta de cargos (positions) disponíveis")
public class PositionController {

    private final DhoPositionRepository positionRepository;

    @GetMapping
    @Operation(summary = "Listar todos os cargos", description = "Retorna uma lista com todos os cargos cadastrados no sistema.")
    public ResponseEntity<List<DhoPosition>> getAllPositions() {
        return ResponseEntity.ok(positionRepository.findAll());
    }
}
