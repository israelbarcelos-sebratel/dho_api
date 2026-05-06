package br.com.sebratel.bff.dho.controller;

import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import br.com.sebratel.bff.dho.service.SuggestionService;
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
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public List<SuggestionResponseDTO> getAll() {
        return suggestionService.findAll();
    }

    @GetMapping("/{id}")
    public SuggestionResponseDTO getById(@PathVariable Long id) {
        return suggestionService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuggestionResponseDTO create(@RequestBody @Valid SuggestionRequestDTO dto) {
        return suggestionService.create(dto);
    }

    @PutMapping("/{id}")
    public SuggestionResponseDTO update(@PathVariable Long id, @RequestBody @Valid SuggestionRequestDTO dto) {
        return suggestionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        suggestionService.delete(id);
    }

    @PostMapping("/{id}/vote")
    @ResponseStatus(HttpStatus.CREATED)
    public void vote(@PathVariable Long id, @RequestBody @Valid VoteRequestDTO dto) {
        suggestionService.vote(id, dto);
    }
}
