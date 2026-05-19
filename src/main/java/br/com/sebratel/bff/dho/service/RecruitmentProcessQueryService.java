package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.*;
import br.com.sebratel.bff.dho.mapper.RecruitmentProcessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentProcessQueryService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final OpportunityRepository opportunityRepository;
    private final RecruitmentProcessLogRepository logRepository;
    private final RecruitmentProcessMapper recruitmentProcessMapper;

    public List<RecruitmentProcessHistoryDTO> getFinalizedProcesses() {
        List<String> finalizedStatuses = List.of("Contratado", "Reprovado", "Desistência");
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
        long approvedHiresLastMonth = logRepository.findByActionNameInAndStatusAndStartTimeAfter(List.of("HIRE", "Contratação do candidato efetivada"), "SUCCESS", lastMonth).size();
        long pendingApprovals = recruitmentProcessRepository.countByProcessStatusName("Aguardando aprovação");

        List<RecruitmentProcessLog> hireLogs = logRepository.findByActionNameInAndStatusAndStartTimeAfter(List.of("HIRE", "Contratação do candidato efetivada"), "SUCCESS", lastYear);
        List<RecruitmentProcessLog> approveLogs = logRepository.findByActionNameInAndStatusAndStartTimeAfter(List.of("APPROVE", "Candidato aprovado"), "SUCCESS", lastYear);

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
                "Banco de Talentos", "Triagem", "Entrevista", "Teste Técnico", "Decisão Final", "Aprovado"
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
                stageStatus = (i < currentStageIndex) ? "CONCLUIDO" : (i == currentStageIndex ? "DESISTENCIA" : "PENDENTE");
            } else if (currentStatusName.toLowerCase().contains("recusado") || "Reprovado".equals(currentStatusName)) {
                stageStatus = (i < currentStageIndex) ? "CONCLUIDO" : (i == currentStageIndex ? "RECUSADO" : "PENDENTE");
            } else {
                stageStatus = (i < currentStageIndex) ? "CONCLUIDO" : (i == currentStageIndex ? "EM_ANDAMENTO" : "PENDENTE");
            }
            stagesResponse.add(new RecruitmentProcessStageResponseDTO(stageName, stageStatus));
        }
        return stagesResponse;
    }
}
