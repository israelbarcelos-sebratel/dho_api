package br.com.sebratel.bff.dho.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RecruitmentProcessItemResponseDTO(
    Integer id,
    String comment,
    LocalDateTime createdAt,
    String createdBy,
    List<DocumentResponseDTO> documents
) {}
