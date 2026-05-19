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
    private final OpportunityRepository opportunityRepository;

    @Transactional
    public void approve(Integer id, OpportunityApprovalDTO dto) {
        updateStatus(id, "Aprovado", null);
    }

    @Transactional
    public void refuse(Integer id, InterviewDecisionDTO dto) {
        updateStatus(id, "Reprovado", dto.reason());
    }

    @Transactional
    public void withdraw(Integer id) {
        updateStatus(id, "Desistência", null);
    }

    @Transactional
    public void hire(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));
        
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
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Triagem".equals(process.getProcessStage().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidato deve estar em Triagem para ir para Entrevista");
        }
        updateStage(id, "Entrevista");
    }

    @Transactional
    public void moveToTechnicalTest(Integer id, TechnicalTestRequestDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Entrevista".equals(process.getProcessStage().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidato deve estar em Entrevista para ir para Teste Técnico");
        }
        process.setRecruiterReport(dto.reason());
        updateStage(id, "Teste Técnico");
    }

    @Transactional
    public void moveToScreening(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));

        if (!"Banco de Talentos".equals(process.getProcessStage().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidato deve estar no Banco de Talentos para ir para Triagem");
        }
        updateStage(id, "Triagem");
    }

    @Transactional
    public void moveToFinalDecision(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        if (!"Teste Técnico".equals(process.getProcessStage().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidato deve estar em Teste Técnico para ir para Decisão Final");
        }
        updateStage(id, "Decisão Final");
        updateStatus(id, "Aguardando aprovação", null);
    }

    @Transactional
    public void managerDecision(Integer id, ManagerDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Decisão Final".equals(process.getProcessStage().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisão do gestor só pode ser tomada no estágio de Decisão Final");
        }
        
        if (dto.isApproved()) {
            updateStatus(id, "Aprovado", dto.getReason());
            updateStage(id, "Aprovado");
        } else {
            updateStatus(id, "Reprovado", dto.getReason());
        }
    }

    @Transactional
    public void sendProposal(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Aprovado".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proposta só pode ser enviada após aprovação do gestor");
        }
        updateStatus(id, "Enviada Proposta", null);
    }

    @Transactional
    public void candidateDecision(Integer id, CandidateDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Enviada Proposta".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisão do candidato só pode ser tomada após envio da proposta");
        }
        String statusName = dto.isAccepted() ? "Finalizado" : "Recusada pelo candidato";
        updateStatus(id, statusName, dto.getReason());
    }

    @Transactional
    public void updateStage(Integer id, RecruitmentProcessStageDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));

        DhoProcessStage stage = processStageRepository.findByName(dto.getStageName())
                .orElseThrow(() -> new RuntimeException("Estágio '" + dto.getStageName() + "' não encontrado"));

        process.setProcessStage(stage);
        recruitmentProcessRepository.save(process);
    }

    @Transactional
    public void updateStatus(Integer id, RecruitmentProcessStatusDTO dto) {
        updateStatus(id, dto.getStatusName(), null);
    }

    private void updateStage(Integer id, String stageName) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));
        
        validateOpportunityApproved(process);

        DhoProcessStage stage = processStageRepository.findByName(stageName)
                .orElseThrow(() -> new RuntimeException("Estágio '" + stageName + "' não encontrado"));

        process.setProcessStage(stage);
        recruitmentProcessRepository.save(process);
    }

    private void updateStatus(Integer id, String statusName, String report) {
        log.info("Iniciando atualização de status para '{}\' no processo ID: {}", statusName, id);
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));

        validateOpportunityApproved(process);

        DhoProcessStatus status = processStatusRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' não encontrado"));

        process.setProcessStatus(status);
        process.setInterviewReport(report);
        recruitmentProcessRepository.save(process);
        log.info("Status do processo ID: {} atualizado com sucesso para '{}\'", id, statusName);
    }

    private void validateOpportunityApproved(RecruitmentProcess process) {
        if (process.getOpportunity() != null && !"Aprovada".equals(process.getOpportunity().getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível realizar esta ação em um processo cuja oportunidade não está aprovada");
        }
    }
}
