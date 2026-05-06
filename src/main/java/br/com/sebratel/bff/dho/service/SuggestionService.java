package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.entity.SuggestionVote;
import br.com.sebratel.bff.dho.domain.repository.SuggestionRepository;
import br.com.sebratel.bff.dho.domain.repository.SuggestionVoteRepository;
import br.com.sebratel.bff.dho.dto.SuggestionRequestDTO;
import br.com.sebratel.bff.dho.dto.SuggestionResponseDTO;
import br.com.sebratel.bff.dho.dto.VoteRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final SuggestionVoteRepository suggestionVoteRepository;

    public List<SuggestionResponseDTO> findAll() {
        return suggestionRepository.findAll()
                .stream()
                .map(SuggestionResponseDTO::fromEntity)
                .toList();
    }

    public SuggestionResponseDTO findById(Long id) {
        return suggestionRepository.findById(id)
                .map(SuggestionResponseDTO::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));
    }

    public SuggestionResponseDTO create(SuggestionRequestDTO dto) {
        Suggestion suggestion = Suggestion.builder()
                .title(dto.title())
                .description(dto.description())
                .email(dto.email())
                .build();
        
        return SuggestionResponseDTO.fromEntity(suggestionRepository.save(suggestion));
    }

    public SuggestionResponseDTO update(Long id, SuggestionRequestDTO dto) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));
        
        suggestion.setTitle(dto.title());
        suggestion.setDescription(dto.description());
        suggestion.setEmail(dto.email());
        
        return SuggestionResponseDTO.fromEntity(suggestionRepository.save(suggestion));
    }

    public void delete(Long id) {
        if (!suggestionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada");
        }
        suggestionRepository.deleteById(id);
    }

    public void vote(Long id, VoteRequestDTO dto) {
        Suggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sugestão não encontrada"));

        SuggestionVote vote = SuggestionVote.builder()
                .suggestion(suggestion)
                .email(dto.email())
                .vote(dto.vote())
                .build();

        suggestionVoteRepository.save(vote);
    }
}
