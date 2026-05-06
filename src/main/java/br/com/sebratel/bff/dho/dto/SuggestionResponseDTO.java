package br.com.sebratel.bff.dho.dto;

import br.com.sebratel.bff.dho.domain.entity.Suggestion;

public record SuggestionResponseDTO(
    Long id,
    String title,
    String description
) {
    public static SuggestionResponseDTO fromEntity(Suggestion entity) {
        return new SuggestionResponseDTO(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription()
        );
    }
}
