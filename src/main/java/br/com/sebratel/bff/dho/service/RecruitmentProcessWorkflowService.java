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
import br.com.sebratel.bff.dho.service.workflow.state.ProcessState;
import org.springframework.context.ApplicationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentProcessWorkflowService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final DhoProcessStageRepository processStageRepository;
    private final PeopleRepository peopleRepository;
    private final ApplicationContext applicationContext;

    private ProcessState getCurrentState(RecruitmentProcess process) {
        String stageName = process.getProcessStage().getName();
        return (ProcessState) applicationContext.getBean(stageName + "State");
    }

    @Transactional
    public void approve(Integer id, OpportunityApprovalDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        updateStatus(process, "Aprovado", null);
    }

    @Transactional
    public void refuse(Integer id, InterviewDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        updateStatus(process, "Reprovado", dto.reason());
    }

    @Transactional
    public void withdraw(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        updateStatus(process, "Desistência", null);
    }

    @Transactional
    public void hire(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo de recrutamento não encontrado"));
        
        validateOpportunityApproved(process);

        DhoProcessStatus status = processStatusRepository.findByName("Contratado")
                .orElseThrow(() -> new RuntimeException("Status 'Contratado' não encontrado"));

        process.setProcessStatus(status);
        
        People candidate = process.getCandidate();
        if (candidate != null) {
            candidate.setAdmissionDate(LocalDateTime.now());
            peopleRepository.save(candidate);
        }

        recruitmentProcessRepository.save(process);
    }

    @Transactional
    public void moveToInterview(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        getCurrentState(process).moveToInterview(process);
        updateStage(process, "Entrevista");
    }

    @Transactional
    public void moveToTechnicalTest(Integer id, TechnicalTestRequestDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        getCurrentState(process).moveToTechnicalTest(process, dto.reason());
        process.setRecruiterReport(dto.reason());
        updateStage(process, "Teste Técnico");
    }

    @Transactional
    public void moveToScreening(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        getCurrentState(process).moveToScreening(process);
        updateStage(process, "Triagem");
    }

    @Transactional
    public void moveToFinalDecision(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        getCurrentState(process).moveToFinalDecision(process);
        updateStage(process, "Decisão Final");
        updateStatus(process, "Aguardando aprovação", null);
    }

    @Transactional
    public void managerDecision(Integer id, ManagerDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        getCurrentState(process).managerDecision(process, dto.isApproved());
        
        if (dto.isApproved()) {
            updateStatus(process, "Aprovado", dto.getReason());
            updateStage(process, "Aprovado");
        } else {
            updateStatus(process, "Reprovado", dto.getReason());
        }
    }

    @Transactional
    public void cancelManagerDecision(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        String currentStage = process.getProcessStage().getName();
        if (!"Decisão Final".equals(currentStage) && !"Aprovado".equals(currentStage)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelamento de decisão só é permitido para processos em Decisão Final ou Aprovado");
        }

        updateStage(process, "Decisão Final");
        updateStatus(process, "Aguardando aprovação", null);
    }


    @Transactional
    public void sendProposal(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        if (!"Aprovado".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposta só pode ser enviada após aprovação do gestor");
        }
        
        getCurrentState(process).sendProposal(process);
        updateStatus(process, "Enviada Proposta", null);
    }

    @Transactional
    public void moveToAwaitingDocuments(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        getCurrentState(process).moveToAwaitingDocuments(process);
        updateStage(process, "Aguardando documentos");
    }

    @Transactional
    public void moveToOnboarding(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        getCurrentState(process).moveToOnboarding(process);
        updateStage(process, "Onboarding");
    }

    @Transactional
    public void candidateDecision(Integer id, CandidateDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        if (dto.isAccepted()) {
            getCurrentState(process).candidateDecision(process, true);
        }

        if (!"Enviada Proposta".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisão do candidato só pode ser tomada após envio da proposta");
        }
        
        String statusName = dto.isAccepted() ? "Finalizado" : "Recusada pelo candidato";
        updateStatus(process, statusName, dto.getReason());
    }

    @Transactional
    public void updateStage(Integer id, RecruitmentProcessStageDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo de recrutamento não encontrado"));

        updateStage(process, dto.getStageName());
    }

    @Transactional
    public void updateStatus(Integer id, RecruitmentProcessStatusDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo de recrutamento não encontrado"));
        updateStatus(process, dto.getStatusName(), null);
    }

    private void updateStage(RecruitmentProcess process, String stageName) {
        validateOpportunityApproved(process);

        DhoProcessStage stage = processStageRepository.findByName(stageName)
                .orElseThrow(() -> new RuntimeException("Estágio '" + stageName + "' não encontrado"));

        process.setProcessStage(stage);
        recruitmentProcessRepository.save(process);
    }

    private void updateStatus(RecruitmentProcess process, String statusName, String report) {
        log.info("Iniciando atualização de status para '{}' no processo ID: {}", statusName, process.getId());
        
        validateOpportunityApproved(process);

        DhoProcessStatus status = processStatusRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' não encontrado"));

        process.setProcessStatus(status);
        process.setInterviewReport(report);
        recruitmentProcessRepository.save(process);
        log.info("Status do processo ID: {} atualizado com sucesso para '{}'", process.getId(), statusName);
    }

    private void validateOpportunityApproved(RecruitmentProcess process) {
        if (process.getOpportunity() != null && !"Aprovada".equals(process.getOpportunity().getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível realizar esta ação em um processo cuja oportunidade não está aprovada");
        }
    }
}
