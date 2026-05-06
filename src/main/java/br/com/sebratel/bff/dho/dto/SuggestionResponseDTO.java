package br.com.sebratel.bff.dho.dto;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.entity.SuggestionVote;

import java.util.List;

public record SuggestionResponseDTO(
    Long id,
    String title,
    String description,
    String email,
    Integer totalVotes
) {
    public static SuggestionResponseDTO fromEntity(Suggestion entity) {
        int sumVotes = 0;
        if (entity.getVotes() != null) {
            sumVotes = entity.getVotes().stream()
                    .mapToInt(SuggestionVote::getVote)
                    .sum();
        }

        return new SuggestionResponseDTO(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEmail(),
            sumVotes
        );
    }
}
