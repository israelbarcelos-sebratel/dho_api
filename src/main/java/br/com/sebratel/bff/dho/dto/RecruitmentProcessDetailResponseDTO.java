package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentProcessDetailResponseDTO {
    private Integer id;
    private String positionName;
    private String processStageName;
    private String processStatusName;
    private LocalDateTime createdAt;
    private Integer opportunityId;
    private CandidateDetailDTO candidate;
}
