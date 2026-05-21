package br.com.sebratel.bff.dho.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityPipelineResponseDTO {
    @JsonProperty("Oportunity")
    private OpportunityPipelineDataDTO opportunity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpportunityPipelineDataDTO {
        private Integer id;
        private String positionName;
        private String teamName;
        private String departmentName;
        private Integer numberOfVacancies;
        private LocalDateTime openOpportunityDate;

        @JsonProperty("processess")
        private List<RecruitmentProcessPipelineDTO> processes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecruitmentProcessPipelineDTO {
        private Integer id;
        private String processStageName;
        private String processStatusName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private CandidatePipelineDTO candidate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidatePipelineDTO {
        private Integer id;
        private String name;
        private String email;
        private String phoneNumber;
        private String profileImage;
    }
}
