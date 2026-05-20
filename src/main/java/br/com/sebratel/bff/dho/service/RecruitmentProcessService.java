package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStageRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.*;
import br.com.sebratel.bff.dho.mapper.RecruitmentProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentProcessService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final DhoProcessStageRepository processStageRepository;
    private final PeopleRepository peopleRepository;
    private final OpportunityRepository opportunityRepository;

    private final RecruitmentProcessMapper recruitmentProcessMapper;
    private final RecruitmentProcessWorkflowService workflowService;
    private final RecruitmentProcessQueryService queryService;

    @Transactional
    public RecruitmentProcessResponseDTO create(RecruitmentProcessRequestDTO dto) {
        People candidate = peopleRepository.findById(dto.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidato não encontrado"));

        Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId())
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
        
        if (!"Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível vincular um candidato a uma oportunidade que não esteja aprovada");
        }

        if (recruitmentProcessRepository.existsByCandidateIdAndOpportunityId(dto.getCandidateId(), dto.getOpportunityId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este candidato já está vinculado a esta oportunidade");
        }

        DhoProcessStatus initialStatus = processStatusRepository.findByName("Em andamento")
                .orElseThrow(() -> new RuntimeException("Status 'Em andamento' não encontrado no banco"));

        DhoProcessStage initialStage = processStageRepository.findByName("Banco de Talentos")
                .orElseThrow(() -> new RuntimeException("Estágio 'Banco de Talentos' não encontrado no banco"));

        RecruitmentProcess process = RecruitmentProcess.builder()
                .candidate(candidate)
                .opportunity(opportunity)
                .processStatus(initialStatus)
                .processStage(initialStage)
                .build();

        return recruitmentProcessMapper.mapToResponseDTO(recruitmentProcessRepository.save(process));
    }

    @Transactional public void approve(Integer id, OpportunityApprovalDTO dto) { workflowService.approve(id, dto); }
    @Transactional public void refuse(Integer id, InterviewDecisionDTO dto) { workflowService.refuse(id, dto); }
    @Transactional public void withdraw(Integer id) { workflowService.withdraw(id); }
    @Transactional public void hire(Integer id) { workflowService.hire(id); }
    @Transactional public void moveToInterview(Integer id) { workflowService.moveToInterview(id); }
    @Transactional public void moveToTechnicalTest(Integer id, TechnicalTestRequestDTO dto) { workflowService.moveToTechnicalTest(id, dto); }
    @Transactional public void moveToScreening(Integer id) { workflowService.moveToScreening(id); }
    @Transactional public void moveToFinalDecision(Integer id) { workflowService.moveToFinalDecision(id); }
    @Transactional public void moveToAwaitingDocuments(Integer id) { workflowService.moveToAwaitingDocuments(id); }
    @Transactional public void moveToOnboarding(Integer id) { workflowService.moveToOnboarding(id); }
    @Transactional public void managerDecision(Integer id, ManagerDecisionDTO dto) { workflowService.managerDecision(id, dto); }
    @Transactional public void sendProposal(Integer id) { workflowService.sendProposal(id); }
    @Transactional public void candidateDecision(Integer id, CandidateDecisionDTO dto) { workflowService.candidateDecision(id, dto); }
    @Transactional public void cancelManagerDecision(Integer id) { workflowService.cancelManagerDecision(id); }

    @Transactional public void updateStage(Integer id, RecruitmentProcessStageDTO dto) { workflowService.updateStage(id, dto); }
    @Transactional public void updateStatus(Integer id, RecruitmentProcessStatusDTO dto) { workflowService.updateStatus(id, dto); }

    public List<RecruitmentProcessHistoryDTO> getFinalizedProcesses() { return queryService.getFinalizedProcesses(); }
    public RecruitmentProcessResponseDTO mapToResponseDTO(RecruitmentProcess process) { return recruitmentProcessMapper.mapToResponseDTO(process); }
    
    public List<RecruitmentProcessResponseDTO> getProcessesByRecruiterEmail(String email) {
        People recruiter = peopleRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado com o e-mail: " + email));
        return recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(recruiter.getId()).stream()
                .map(this::mapToResponseDTO).toList();
    }

    public List<RecruitmentProcessResponseDTO> getProcessesByRecruiter(Integer recruiterId) {
        return recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(recruiterId).stream()
                .map(this::mapToResponseDTO).toList();
    }

    public List<RecruitmentProcessLogDTO> getLogs(Integer id) { return queryService.getLogs(id); }
    public RecruitmentIndicatorsDTO getIndicators() { return queryService.getIndicators(); }
    public List<RecruitmentProcessStageResponseDTO> getProcessStages(Integer id) { return queryService.getProcessStages(id); }
}
