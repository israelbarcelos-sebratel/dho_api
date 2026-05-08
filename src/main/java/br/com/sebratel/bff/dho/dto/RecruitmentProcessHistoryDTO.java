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
public class RecruitmentProcessHistoryDTO {
    private Integer id;
    private String candidateName;
    private String positionName;
    private String statusName;
    private LocalDateTime admissionDate;
}
