package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    public List<OpportunityResponseDTO> findAll() {
        return opportunityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OpportunityResponseDTO convertToDTO(Opportunity opportunity) {
        return OpportunityResponseDTO.builder()
                .id(opportunity.getId())
                .openOpportunityDate(opportunity.getOpenOpportunityDate())
                .candidateName(opportunity.getCandidate() != null ? opportunity.getCandidate().getName() : null)
                .positionName(opportunity.getPosition() != null ? opportunity.getPosition().getName() : null)
                .teamName(opportunity.getTeam() != null ? opportunity.getTeam().getName() : null)
                .departmentName(opportunity.getDepartment() != null ? opportunity.getDepartment().getName() : null)
                .opportunityMotiveName(opportunity.getOpportunityMotive() != null ? opportunity.getOpportunityMotive().getName() : null)
                .replacedPersonName(opportunity.getReplacedPerson() != null ? opportunity.getReplacedPerson().getName() : null)
                .baseOriginName(opportunity.getBaseOrigin() != null ? opportunity.getBaseOrigin().getName() : null)
                .opportunityStatusName(opportunity.getOpportunityStatus() != null ? opportunity.getOpportunityStatus().getName() : null)
                .processStageName(opportunity.getProcessStage() != null ? opportunity.getProcessStage().getName() : null)
                .processStatusName(opportunity.getProcessStatus() != null ? opportunity.getProcessStatus().getName() : null)
                .deadlineSlaDays(opportunity.getDeadlineSlaDays())
                .acceptDate(opportunity.getAcceptDate())
                .responsibleRecruiterName(opportunity.getResponsibleRecruiter() != null ? opportunity.getResponsibleRecruiter().getName() : null)
                .observations(opportunity.getObservations())
                .build();
    }
}
