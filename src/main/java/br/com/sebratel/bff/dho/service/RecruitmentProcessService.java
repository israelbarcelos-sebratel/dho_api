package br.com.sebratel.bff.dho.service;

import lombok.extern.slf4j.Slf4j;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.dto.RecruitmentIndicatorsDTO;
import java.time.Duration;
import java.util.Map;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStageRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessRequestDTO;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStageDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStatusDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessStageResponseDTO;
import java.util.ArrayList;



import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentProcessService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final DhoProcessStageRepository processStageRepository;
    private final PeopleRepository peopleRepository;
    private final DhoRoleRepository roleRepository;
    private final RecruitmentProcessLogRepository logRepository;
    private final OpportunityRepository opportunityRepository;

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

        RecruitmentProcess saved = recruitmentProcessRepository.save(process);

        return RecruitmentProcessResponseDTO.builder()
                .id(saved.getId())
                .candidateName(candidate.getName())
                .positionName(opportunity.getPosition() != null ? opportunity.getPosition().getName() : null)
                .processStatusName(initialStatus.getName())
                .processStageName(initialStage.getName())
                .opportunityId(opportunity.getId())
                .build();
    }

    @Transactional
    public void approve(Integer id) {
        updateStatus(id, "Aprovado", null);
    }

    @Transactional
    public void refuse(Integer id, InterviewDecisionDTO dto) {
        updateStatus(id, "Recusado", dto.reason());
    }

    @Transactional
    public void withdraw(Integer id) {
        updateStatus(id, "Desistência", null);
    }

    @Transactional
    public void hire(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));
        if (process.getOpportunity() != null && !"Aprovada".equals(process.getOpportunity().getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível efetivar a contratação de um processo cuja oportunidade não está aprovada");
        }



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
    public void moveToTechnicalTest(Integer id, InterviewDecisionDTO dto) {
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
    }

    @Transactional
    public void managerDecision(Integer id, br.com.sebratel.bff.dho.dto.ManagerDecisionDTO dto) {
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
    public void candidateDecision(Integer id, br.com.sebratel.bff.dho.dto.CandidateDecisionDTO dto) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo não encontrado"));
        if (!"Enviada Proposta".equals(process.getProcessStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decisão do candidato só pode ser tomada após envio da proposta");
        }
        String statusName = dto.isAccepted() ? "Finalizado" : "Recusada pelo candidato";
        updateStatus(id, statusName, dto.getReason());
    }

    private void updateStage(Integer id, String stageName) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));
        if (process.getOpportunity() != null && !"Aprovada".equals(process.getOpportunity().getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível alterar a etapa de um processo cuja oportunidade não está aprovada");
        }



        DhoProcessStage stage = processStageRepository.findByName(stageName)
                .orElseThrow(() -> new RuntimeException("Estágio '" + stageName + "' não encontrado"));

        process.setProcessStage(stage);
        recruitmentProcessRepository.save(process);
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

    private void updateStatus(Integer id, String statusName, String report) {
        log.info("Iniciando atualização de status para '{}' no processo ID: {}", statusName, id);
        
        log.info("Buscando processo ID: {} para atualizar status", id);
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));
        log.info("Processo encontrado para atualização de status");

        if (process.getOpportunity() != null && !"Aprovada".equals(process.getOpportunity().getOpportunityStatus().getName())) {
            log.warn("Falha ao atualizar status: Oportunidade não aprovada para o processo ID: {}", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível alterar o status de um processo cuja oportunidade não está aprovada");
        }

        log.info("Buscando status pelo nome: '{}'", statusName);
        DhoProcessStatus status = processStatusRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' não encontrado"));
        log.info("Status '{}' encontrado", statusName);

        process.setProcessStatus(status);
        process.setInterviewReport(report);
        
        log.info("Salvando alteração de status no banco para o processo ID: {}", id);
        recruitmentProcessRepository.save(process);
        log.info("Status do processo ID: {} atualizado com sucesso para '{}'", id, statusName);
    }

    public List<RecruitmentProcessHistoryDTO> getFinalizedProcesses() {
        List<String> finalizedStatuses = List.of("Contratado", "Recusado", "Desistência");
        return recruitmentProcessRepository.findByProcessStatusNameIn(finalizedStatuses).stream()
                .map(process -> RecruitmentProcessHistoryDTO.builder()
                        .id(process.getId())
                        .candidateName(Optional.ofNullable(process.getCandidate()).map(People::getName).orElse(null))
                        .positionName(Optional.ofNullable(process.getOpportunity())
                                .map(opp -> Optional.ofNullable(opp.getPosition()).map(pos -> pos.getName()).orElse(null))
                                .orElse(null))
                        .statusName(Optional.ofNullable(process.getProcessStatus()).map(status -> status.getName()).orElse(null))
                        .admissionDate(Optional.ofNullable(process.getCandidate()).map(People::getAdmissionDate).orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

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

                .build();
    }

    public List<RecruitmentProcessResponseDTO> getProcessesByRecruiterEmail(String email) {
        People recruiter = peopleRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado com o e-mail: " + email));

        return recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(recruiter.getId()).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RecruitmentProcessResponseDTO> getProcessesByRecruiter(Integer recruiterId) {
        People recruiter = peopleRepository.findById(recruiterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado com o ID: " + recruiterId));

        return recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(recruiterId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    public List<RecruitmentProcessLogDTO> getLogs(Integer id) {
        return logRepository.findByRecruitmentProcessIdOrderByStartTimeDesc(id).stream()
                .map(log -> RecruitmentProcessLogDTO.builder()
                        .id(log.getId())
                        .actionName(log.getActionName())
                        .startTime(log.getStartTime())
                        .endTime(log.getEndTime())
                        .durationMs(log.getDurationMs())
                        .status(log.getStatus())
                        .errorMessage(log.getErrorMessage())
                        .candidateName(log.getRecruitmentProcess() != null && log.getRecruitmentProcess().getCandidate() != null 
                                ? log.getRecruitmentProcess().getCandidate().getName() : null)
                        .executedBy(log.getExecutedBy())
                        .build())
                .collect(Collectors.toList());
    }

    public RecruitmentIndicatorsDTO getIndicators() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);
        LocalDateTime lastYear = now.minusYears(1);

        long openVacancies = opportunityRepository.countByOpportunityStatusNameNotIn(List.of("Finalizada", "Cancelada"));

        long approvedHiresLastMonth = logRepository.findByActionNameAndStatusAndStartTimeAfter("HIRE", "SUCCESS", lastMonth).size();

        long pendingApprovals = recruitmentProcessRepository.countByProcessStatusName("Aguardando aprovação");

        List<RecruitmentProcessLog> hireLogs = logRepository.findByActionNameAndStatusAndStartTimeAfter("HIRE", "SUCCESS", lastYear);
        List<RecruitmentProcessLog> approveLogs = logRepository.findByActionNameAndStatusAndStartTimeAfter("APPROVE", "SUCCESS", lastYear);

        Map<Integer, LocalDateTime> approvalTimes = approveLogs.stream()
                .filter(log -> log.getRecruitmentProcess() != null && log.getRecruitmentProcess().getCandidate() != null)
                .collect(Collectors.toMap(
                        log -> log.getRecruitmentProcess().getCandidate().getId(),
                        RecruitmentProcessLog::getStartTime,
                        (existing, replacement) -> existing
                ));

        double totalDays = 0;
        int hiredCount = 0;

        for (RecruitmentProcessLog hireLog : hireLogs) {
            if (hireLog.getRecruitmentProcess() != null && hireLog.getRecruitmentProcess().getCandidate() != null) {
                Integer candidateId = hireLog.getRecruitmentProcess().getCandidate().getId();
                if (approvalTimes.containsKey(candidateId)) {
                    long days = Duration.between(approvalTimes.get(candidateId), hireLog.getStartTime()).toDays();
                    totalDays += days;
                    hiredCount++;
                }
            }
        }

        Double averageHiringTime = hiredCount > 0 ? totalDays / hiredCount : 0.0;

        return RecruitmentIndicatorsDTO.builder()
                .openVacancies(openVacancies)
                .approvedHiresLastMonth(approvedHiresLastMonth)
                .pendingApprovals(pendingApprovals)
                .averageHiringTimeLastYear(averageHiringTime)
                .build();
    }

    public List<RecruitmentProcessStageResponseDTO> getProcessStages(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));

        List<String> orderedStages = List.of(
                "Banco de Talentos",
                "Triagem",
                "Entrevista",
                "Teste Técnico",
                "Decisão Final",
                "Aprovado"
        );

        String currentStageName = process.getProcessStage().getName();
        String currentStatusName = process.getProcessStatus().getName();
        
        int currentStageIndex = orderedStages.indexOf(currentStageName);
        List<RecruitmentProcessStageResponseDTO> stagesResponse = new ArrayList<>();

        for (int i = 0; i < orderedStages.size(); i++) {
            String stageName = orderedStages.get(i);
            String stageStatus;

            if ("Finalizado".equals(currentStatusName) || "Contratado".equals(currentStatusName)) {
                stageStatus = "CONCLUIDO";
            } else if ("Desistência".equals(currentStatusName)) {
                if (i < currentStageIndex) {
                    stageStatus = "CONCLUIDO";
                } else if (i == currentStageIndex) {
                    stageStatus = "DESISTENCIA";
                } else {
                    stageStatus = "PENDENTE";
                }
            } else if (currentStatusName.toLowerCase().contains("recusado") || "Reprovado".equals(currentStatusName)) {
                if (i < currentStageIndex) {
                    stageStatus = "CONCLUIDO";
                } else if (i == currentStageIndex) {
                    stageStatus = "RECUSADO";
                } else {
                    stageStatus = "PENDENTE";
                }
            } else {
                if (i < currentStageIndex) {
                    stageStatus = "CONCLUIDO";
                } else if (i == currentStageIndex) {
                    stageStatus = "EM_ANDAMENTO";
                } else {
                    stageStatus = "PENDENTE";
                }
            }

            stagesResponse.add(new RecruitmentProcessStageResponseDTO(stageName, stageStatus));
        }

        return stagesResponse;
    }
}
