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
public class RecruitmentProcessResponseDTO {
    private Integer id;
    private String candidateName;
    private String positionName;
    private String processStatusName;
    private String processStageName;
    private Integer opportunityId;
    private String recruiterReport;
    private String interviewReport;
    private java.time.LocalDateTime opportunityDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

}
