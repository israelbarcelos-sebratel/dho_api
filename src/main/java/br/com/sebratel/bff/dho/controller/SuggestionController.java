package br.com.sebratel.bff.dho.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;


import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/suggestions")
@RequiredArgsConstructor
@Tag(name = "Sugestões", description = "Endpoints para o sistema de sugestões e votação")
public class SuggestionController {

    private static final Logger log = LoggerFactory.getLogger(SuggestionController.class);

    private final SuggestionService suggestionService;

    private void logSecurityContext(String methodName, String endpoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.info("[SuggestionController] [{}] - Action: Acessando endpoint {} | Usuário: {} | Autoridades: {}", 
                methodName, endpoint, auth.getName(), 
                auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(java.util.stream.Collectors.joining(", ")));
        } else {
            log.warn("[SuggestionController] [{}] - Action: Endpoint {} acessado sem autenticação", methodName, endpoint);
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('suggestions')")
    @Operation(summary = "Listar todas as sugestões", description = "Retorna uma lista com todas as sugestões cadastradas.")
    public List<SuggestionResponseDTO> getAll() {
        logSecurityContext("getAll", "GET /suggestions");
        log.info("[SuggestionController] [getAll] - Action: Buscando todas as sugestões");
        List<SuggestionResponseDTO> suggestions = suggestionService.findAll();
        log.info("[SuggestionController] [getAll] - Action: Retornando {} sugestões", suggestions.size());
        return suggestions;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('suggestions')")
    @Operation(summary = "Obter sugestão por ID")
    public SuggestionResponseDTO getById(@PathVariable Long id) {
        logSecurityContext("getById", "GET /suggestions/" + id);
        log.info("[SuggestionController] [getById] - Action: Buscando sugestão com ID: {}", id);
        SuggestionResponseDTO response = suggestionService.findById(id);
        log.info("[SuggestionController] [getById] - Action: Sugestão com ID: {} encontrada", id);
        return response;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('suggestions')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova sugestão")
    public SuggestionResponseDTO create(@RequestBody @Valid SuggestionRequestDTO dto) {
        logSecurityContext("create", "POST /suggestions");
        log.info("[SuggestionController] [create] - Action: Criando nova sugestão para o email: {}", dto.email());
        SuggestionResponseDTO response = suggestionService.create(dto);
        log.info("[SuggestionController] [create] - Action: Sugestão criada com sucesso. ID: {}", response.id());
        return response;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RQ18')")
    @Operation(summary = "Atualizar sugestão existente")
    public SuggestionResponseDTO update(@PathVariable Long id, @RequestBody @Valid SuggestionRequestDTO dto) {
        logSecurityContext("update", "PUT /suggestions/" + id);
        log.info("[SuggestionController] [update] - Action: Atualizando sugestão com ID: {}", id);
        SuggestionResponseDTO response = suggestionService.update(id, dto);
        log.info("[SuggestionController] [update] - Action: Sugestão com ID: {} atualizada com sucesso", id);
        return response;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RQ18')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir sugestão")
    public void delete(@PathVariable Long id) {
        logSecurityContext("delete", "DELETE /suggestions/" + id);
        log.info("[SuggestionController] [delete] - Action: Excluindo sugestão com ID: {}", id);
        suggestionService.delete(id);
        log.info("[SuggestionController] [delete] - Action: Sugestão com ID: {} excluída com sucesso", id);
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("hasAuthority('suggestions')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Votar em uma sugestão")
    public void vote(@PathVariable Long id, @RequestBody @Valid VoteRequestDTO dto) {
        logSecurityContext("vote", "POST /suggestions/" + id + "/vote");
        log.info("[SuggestionController] [vote] - Action: Registrando voto para a sugestão ID: {} | Email: {} | Voto: {}", id, dto.email(), dto.vote());
        suggestionService.vote(id, dto);
        log.info("[SuggestionController] [vote] - Action: Voto registrado com sucesso para a sugestão ID: {}", id);
    }
}
