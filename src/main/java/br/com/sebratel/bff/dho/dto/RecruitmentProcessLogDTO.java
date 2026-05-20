package br.com.sebratel.bff.dho.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record RecruitmentProcessLogDTO(
    Integer id,
    String actionName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationMs,
    String status,
    String errorMessage,
    String candidateName,
    String executedBy
) {}
