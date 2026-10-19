package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoBaseOrigin;
import br.com.sebratel.bff.dho.domain.repository.DhoBaseOriginRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/base-origins")
@RequiredArgsConstructor
@Tag(name = "Bases de Origem", description = "Endpoints para listagem de bases de origem (unidades)")
public class BaseOriginController {

    private final DhoBaseOriginRepository baseOriginRepository;

    @GetMapping
    @Operation(summary = "Listar todas as bases de origem", description = "Retorna uma lista com todas as unidades/bases cadastradas.")
    public ResponseEntity<List<DhoBaseOrigin>> getAll() {
        return ResponseEntity.ok(baseOriginRepository.findAll());
    }
}
