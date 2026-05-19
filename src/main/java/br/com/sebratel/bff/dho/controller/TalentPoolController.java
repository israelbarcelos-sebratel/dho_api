package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.TalentPoolRequestDTO;
import br.com.sebratel.bff.dho.dto.TalentPoolResponseDTO;
import br.com.sebratel.bff.dho.dto.TalentPoolSelectResponseDTO;
import br.com.sebratel.bff.dho.service.talentpool.TalentPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/talent-pool")
@RequiredArgsConstructor
@Tag(name = "Banco de Talentos", description = "Endpoints para gestão do banco de talentos")
public class TalentPoolController {

    private final TalentPoolService talentPoolService;

    @GetMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @Operation(summary = "Listar banco de talentos", description = "Retorna todas as pessoas cadastradas no banco de talentos.")
    public List<TalentPoolResponseDTO> getAll() {
        return talentPoolService.findAll();
    }

    @GetMapping("/select")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @Operation(summary = "Listar pessoas do banco de talentos para seleção", description = "Retorna uma lista simplificada com ID da pessoa e nome para preenchimento de dropdowns.")
    public List<TalentPoolSelectResponseDTO> getAllForSelect() {
        return talentPoolService.findAllForSelect();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @Operation(summary = "Obter registro do banco de talentos por ID")
    public TalentPoolResponseDTO getById(@PathVariable Integer id) {
        return talentPoolService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar pessoa ao banco de talentos")
    public TalentPoolResponseDTO create(@RequestBody @Valid TalentPoolRequestDTO dto) {
        return talentPoolService.addToPool(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @Operation(summary = "Atualizar registro do banco de talentos")
    public TalentPoolResponseDTO update(@PathVariable Integer id, @RequestBody @Valid TalentPoolRequestDTO dto) {
        return talentPoolService.updatePoolEntry(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover pessoa do banco de talentos")
    public void delete(@PathVariable Integer id) {
        talentPoolService.removeFromPool(id);
    }

    @GetMapping("/{id}/processes")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_talent_pool.name())")
    @Operation(summary = "Listar processos de um candidato do banco de talentos", description = "Retorna todos os processos de recrutamento vinculados ao candidato do banco de talentos.")
    public List<RecruitmentProcessResponseDTO> getProcesses(@PathVariable Integer id) {
        return talentPoolService.findProcessesByTalentPoolId(id);
    }
}
