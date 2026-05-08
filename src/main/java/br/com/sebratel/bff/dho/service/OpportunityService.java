package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OpportunityResponseDTO findById(Integer id) {
        return opportunityRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
    }



    @Transactional
    public OpportunityResponseDTO create(OpportunityRequestDTO dto) {
        Opportunity opportunity = Opportunity.builder()
                .openOpportunityDate(dto.openOpportunityDate())
                .candidate(dto.candidateId() != null ? People.builder().id(dto.candidateId()).build() : null)
                .position(dto.positionId() != null ? DhoPosition.builder().id(dto.positionId()).build() : null)
                .team(dto.teamId() != null ? DhoTeam.builder().id(dto.teamId()).build() : null)
                .department(dto.departmentId() != null ? DhoDepartment.builder().id(dto.departmentId()).build() : null)
                .opportunityMotive(dto.opportunityMotiveId() != null ? DhoOpportunityMotive.builder().id(dto.opportunityMotiveId()).build() : null)
                .replacedPerson(dto.replacedPersonId() != null ? People.builder().id(dto.replacedPersonId()).build() : null)
                .baseOrigin(dto.baseOriginId() != null ? DhoBaseOrigin.builder().id(dto.baseOriginId()).build() : null)
                .opportunityStatus(dto.opportunityStatusId() != null ? DhoOpportunityStatus.builder().id(dto.opportunityStatusId()).build() : null)
                .processStage(dto.processStageId() != null ? DhoProcessStage.builder().id(dto.processStageId()).build() : null)
                .processStatus(dto.processStatusId() != null ? DhoProcessStatus.builder().id(dto.processStatusId()).build() : null)
                .deadlineSlaDays(dto.deadlineSlaDays())
                .acceptDate(dto.acceptDate())
                .responsibleRecruiter(dto.responsibleRecruiterId() != null ? People.builder().id(dto.responsibleRecruiterId()).build() : null)
                .observations(dto.observations())
                .build();

        Opportunity saved = opportunityRepository.save(opportunity);
        return findById(saved.getId());
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
