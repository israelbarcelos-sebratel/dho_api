package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentProcessResponseDTO {
    private Integer id;
    private String candidateName;
    private String positionName;
    private String processStatusName;
    private String processStageName;
    private Integer opportunityId;
    private String recruiterReport;
    private String interviewReport;
}
