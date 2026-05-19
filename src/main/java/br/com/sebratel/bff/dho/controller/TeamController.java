package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoTeam;
import br.com.sebratel.bff.dho.domain.repository.DhoTeamRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Times", description = "Endpoints para consulta de times")
public class TeamController {

    private final DhoTeamRepository teamRepository;

    @GetMapping
    @Operation(summary = "Listar todos os times", description = "Retorna uma lista com todos os times cadastrados no sistema.")
    public ResponseEntity<List<DhoTeam>> getAllTeams() {
        return ResponseEntity.ok(teamRepository.findAll());
    }
}
