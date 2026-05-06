package br.com.sebratel.bff.dho.dto;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;
import br.com.sebratel.bff.dho.domain.entity.SuggestionVote;

import java.util.List;

public record SuggestionResponseDTO(
    Long id,
    String title,
    String description,
    String email,
    Long positiveVotes,
    Long negativeVotes
) {
    public static SuggestionResponseDTO fromEntity(Suggestion entity) {
        long positiveVotes = 0;
        long negativeVotes = 0;
        
        if (entity.getVotes() != null) {
            positiveVotes = entity.getVotes().stream()
                    .filter(v -> "POSITIVO".equals(v.getVote()))
                    .count();
            
            negativeVotes = entity.getVotes().stream()
                    .filter(v -> "NEGATIVO".equals(v.getVote()))
                    .count();
        }

        return new SuggestionResponseDTO(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getEmail(),
            positiveVotes,
            negativeVotes
        );
    }
}
