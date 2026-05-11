package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;

import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;

import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final DhoOpportunityStatusRepository statusRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;

    public List<CandidateResponseDTO> findCandidatesByOpportunityId(Integer id) {
        return recruitmentProcessRepository.findByOpportunityId(id).stream()
                .map(rp -> CandidateResponseDTO.builder()
                        .id(rp.getCandidate().getId())
                        .name(rp.getCandidate().getName())
                        .processStage(mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatus(mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                        .build())
                .collect(Collectors.toList());
    }



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
        DhoOpportunityStatus pendingStatus = statusRepository.findByName("Pendente")
                .orElseThrow(() -> new RuntimeException("Status 'Pendente' não encontrado"));

        Opportunity opportunity = Opportunity.builder()
                .openOpportunityDate(Optional.ofNullable(dto.openOpportunityDate()).orElse(LocalDateTime.now()))
                .candidate(mapReference(dto.candidateId(), id -> People.builder().id(id).build()))
                .position(mapReference(dto.positionId(), id -> DhoPosition.builder().id(id).build()))
                .team(mapReference(dto.teamId(), id -> DhoTeam.builder().id(id).build()))
                .department(mapReference(dto.departmentId(), id -> DhoDepartment.builder().id(id).build()))
                .opportunityMotive(mapReference(dto.opportunityMotiveId(), id -> DhoOpportunityMotive.builder().id(id).build()))
                .replacedPerson(mapReference(dto.replacedPersonId(), id -> People.builder().id(id).build()))
                .baseOrigin(mapReference(dto.baseOriginId(), id -> DhoBaseOrigin.builder().id(id).build()))
                .opportunityStatus(pendingStatus)
                .processStage(mapReference(dto.processStageId(), id -> DhoProcessStage.builder().id(id).build()))
                .processStatus(mapReference(dto.processStatusId(), id -> DhoProcessStatus.builder().id(id).build()))
                .deadlineSlaDays(dto.deadlineSlaDays())
                .acceptDate(dto.acceptDate())
                .responsibleRecruiter(mapReference(dto.responsibleRecruiterId(), id -> People.builder().id(id).build()))
                .observations(dto.observations())
                .workSchedule(dto.workSchedule())
                .hardSkills(dto.hardSkills())
                .softSkills(dto.softSkills())
                .build();

        Opportunity saved = opportunityRepository.save(opportunity);
        return convertToDTO(saved);
    }
    @Transactional
    public OpportunityResponseDTO approve(Integer id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus approvedStatus = statusRepository.findByName("Aprovada")
                .orElseThrow(() -> new RuntimeException("Status 'Aprovada' não encontrado"));

        opportunity.setOpportunityStatus(approvedStatus);
        return convertToDTO(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityResponseDTO refuse(Integer id, OpportunityApprovalDTO dto) {
        if (dto.justification() == null || dto.justification().length() < 200) {
            throw new RuntimeException("A justificativa de recusa deve ter no mínimo 200 caracteres");
        }

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus refusedStatus = statusRepository.findByName("Recusada")
                .orElseThrow(() -> new RuntimeException("Status 'Recusada' não encontrado"));

        opportunity.setOpportunityStatus(refusedStatus);
        opportunity.setRefusalJustification(dto.justification());
        return convertToDTO(opportunityRepository.save(opportunity));
    }

    @Transactional
    public OpportunityResponseDTO finalize(Integer id, OpportunityApprovalDTO dto) {
        if (dto.justification() == null || dto.justification().length() < 200) {
            throw new RuntimeException("A justificativa de finalização deve ter no mínimo 200 caracteres");
        }

        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus finalizedStatus = statusRepository.findByName("Finalizada")
                .orElseThrow(() -> new RuntimeException("Status 'Finalizada' não encontrado"));

        opportunity.setOpportunityStatus(finalizedStatus);
        opportunity.setFinalizationJustification(dto.justification());
        return convertToDTO(opportunityRepository.save(opportunity));
    }


    private OpportunityResponseDTO convertToDTO(Opportunity opportunity) {
        return OpportunityResponseDTO.builder()
                .id(opportunity.getId())
                .openOpportunityDate(opportunity.getOpenOpportunityDate())
                .candidateName(mapName(opportunity.getCandidate(), People::getName))
                .positionName(mapName(opportunity.getPosition(), DhoPosition::getName))
                .teamName(mapName(opportunity.getTeam(), DhoTeam::getName))
                .departmentName(mapName(opportunity.getDepartment(), DhoDepartment::getName))
                .opportunityMotiveName(mapName(opportunity.getOpportunityMotive(), DhoOpportunityMotive::getName))
                .replacedPersonName(mapName(opportunity.getReplacedPerson(), People::getName))
                .baseOriginName(mapName(opportunity.getBaseOrigin(), DhoBaseOrigin::getName))
                .opportunityStatusName(mapName(opportunity.getOpportunityStatus(), DhoOpportunityStatus::getName))
                .processStageName(mapName(opportunity.getProcessStage(), DhoProcessStage::getName))
                .processStatusName(mapName(opportunity.getProcessStatus(), DhoProcessStatus::getName))
                .deadlineSlaDays(opportunity.getDeadlineSlaDays())
                .acceptDate(opportunity.getAcceptDate())
                .responsibleRecruiterName(mapName(opportunity.getResponsibleRecruiter(), People::getName))
                .observations(opportunity.getObservations())
                .refusalJustification(opportunity.getRefusalJustification())
                .finalizationJustification(opportunity.getFinalizationJustification())
                .workSchedule(opportunity.getWorkSchedule())
                .hardSkills(opportunity.getHardSkills())
                .softSkills(opportunity.getSoftSkills())
                .build();
    }

    private <T, ID> T mapReference(ID id, Function<ID, T> builder) {
        return Optional.ofNullable(id).map(builder).orElse(null);
    }

    private <T, R> R mapName(T entity, Function<T, R> mapper) {
        return Optional.ofNullable(entity).map(mapper).orElse(null);
    }
}
