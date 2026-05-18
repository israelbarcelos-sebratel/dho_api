package br.com.sebratel.bff.dho.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityResponseDTO {
    private Integer id;
    private LocalDateTime openOpportunityDate;
    private String positionName;
    private String teamName;
    private String departmentName;
    private String opportunityMotiveName;
    private String baseOriginName;
    private String opportunityStatusName;
    private String processStageName;
    private String processStatusName;
    private Integer deadlineSlaDays;
    private LocalDateTime acceptDate;
    private String responsibleRecruiterName;
    private String observations;
    private String refusalJustification;
    private String finalizationJustification;
    private String workSchedule;
    private String hardSkills;
    private String softSkills;

    // Frontend compatibility fields
    private String title;
    private String type;
    private String team;
    private String status;
    private String date;
    private String statusVariant;
    private UserResponseDTO requester;
    private java.util.List<CandidateResponseDTO> candidates;
    private java.util.List<RecruitmentProcessResponseDTO> recruitmentProcesses;
}
