package br.com.sebratel.bff.dho.mapper;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RecruitmentProcessMapper {

    public RecruitmentProcessResponseDTO mapToResponseDTO(RecruitmentProcess process) {
        return RecruitmentProcessResponseDTO.builder()
                .id(process.getId())
                .candidateName(Optional.ofNullable(process.getCandidate()).map(People::getName).orElse(null))
                .positionName(Optional.ofNullable(process.getOpportunity())
                        .map(opp -> Optional.ofNullable(opp.getPosition()).map(pos -> pos.getName()).orElse(null))
                        .orElse(null))
                .processStatusName(Optional.ofNullable(process.getProcessStatus()).map(status -> status.getName()).orElse(null))
                .processStageName(Optional.ofNullable(process.getProcessStage()).map(stage -> stage.getName()).orElse(null))
                .opportunityId(Optional.ofNullable(process.getOpportunity()).map(opp -> opp.getId()).orElse(null))
                .recruiterReport(process.getRecruiterReport())
                .interviewReport(process.getInterviewReport())
                .opportunityDate(Optional.ofNullable(process.getOpportunity()).map(Opportunity::getOpenOpportunityDate).orElse(null))
                .build();
    }
}
