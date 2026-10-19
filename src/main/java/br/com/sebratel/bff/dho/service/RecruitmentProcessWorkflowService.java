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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

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
        try {
            return (ProcessState) applicationContext.getBean(stageName + "State");
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("Bean não encontrado para o estágio '{}State', tentando busca robusta por nome do estágio", stageName);
            return applicationContext.getBeansOfType(ProcessState.class).values().stream()
                    .filter(state -> stageName.equalsIgnoreCase(state.getStageName()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Nenhum bean de estado configurado para o estágio: " + stageName));
        }
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

        String stage = process.getProcessStage().getName();
        String status = process.getProcessStatus().getName();
        java.util.List<String> restrictedStates = java.util.List.of(
                "Aprovado", "Aguardando aprovação", "Finalizado",
                "Aguardando documentos", "Onboarding", "Enviada Proposta"
        );

        if (restrictedStates.contains(stage) || restrictedStates.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A partir da etapa de 'Aguardando aprovação', o candidato não pode mais ser reprovado, apenas marcado como desistente.");
        }

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

        DhoProcessStatus status = processStatusRepository.findByNameIgnoreCase("Contratado")
                .orElseThrow(() -> new RuntimeException("Status 'Contratado' não encontrado"));

        DhoProcessStage stage = processStageRepository.findByNameIgnoreCase("Finalizado")
                .orElseThrow(() -> new RuntimeException("Estágio 'Finalizado' não encontrado"));

        process.setProcessStatus(status);
        process.setProcessStage(stage);
        
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
        updateStage(process, "Aguardando aprovação");
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
        if (!"Aguardando aprovação".equals(currentStage) && !"Aprovado".equals(currentStage)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelamento de decisão só é permitido para processos em Aguardando aprovação ou Aprovado");
        }

        updateStage(process, "Aguardando aprovação");
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
        updateStage(process, "Enviada Proposta");
    }

    @Transactional
    public void moveToAwaitingDocuments(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        
        if (!"Enviada Proposta".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movimentação para aguardando documentos só pode ser feita após o envio da proposta");
        }

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
        
        if (!"Enviada Proposta".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisão do candidato só pode ser tomada após envio da proposta");
        }

        if (dto.isAccepted()) {
            getCurrentState(process).candidateDecision(process, true);
            updateStatus(process, "Em andamento", dto.getReason());
            updateStage(process, "Aguardando documentos");
        } else {
            updateStatus(process, "Recusada pelo candidato", dto.getReason());
        }
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

        DhoProcessStage stage = processStageRepository.findByNameIgnoreCase(stageName)
                .orElseThrow(() -> new RuntimeException("Estágio '" + stageName + "' não encontrado"));

        if ("Triagem".equalsIgnoreCase(stageName) && process.getStartDate() == null) {
            process.setStartDate(LocalDateTime.now());
            log.info("Data de início definida para o processo ID: {}", process.getId());
        }

        process.setProcessStage(stage);
        recruitmentProcessRepository.save(process);
    }

    private void updateStatus(RecruitmentProcess process, String statusName, String report) {
        log.info("Iniciando atualização de status para '{}' no processo ID: {}", statusName, process.getId());
        
        validateOpportunityApproved(process);

        DhoProcessStatus status = processStatusRepository.findByNameIgnoreCase(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' não encontrado"));

        if (("Reprovado".equalsIgnoreCase(statusName) || "Desistência".equalsIgnoreCase(statusName)) && process.getEndDate() == null) {
            process.setEndDate(LocalDateTime.now());
            log.info("Data de fim definida para o processo ID: {} devido ao status: {}", process.getId(), statusName);
        }

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
