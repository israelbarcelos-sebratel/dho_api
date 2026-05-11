package br.com.sebratel.bff.dho.dto;

import lombok.Builder;

@Builder
public record RecruitmentIndicatorsDTO(
    long openVacancies,
    long approvedHiresLastMonth,
    long pendingApprovals,
    Double averageHiringTimeLastYear
) {}
