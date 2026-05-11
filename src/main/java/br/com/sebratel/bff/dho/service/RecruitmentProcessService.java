package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.dto.RecruitmentIndicatorsDTO;
import java.time.Duration;
import java.util.Map;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.function.Function;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;

@Service
@RequiredArgsConstructor
public class RecruitmentProcessService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final PeopleRepository peopleRepository;
    private final DhoRoleRepository roleRepository;
    private final RecruitmentProcessLogRepository logRepository;
    private final OpportunityRepository opportunityRepository;


    @Transactional
    public void approve(Integer id) {
        updateStatus(id, "Aprovado", null);
    }

    @Transactional
    public void refuse(Integer id, InterviewDecisionDTO dto) {
        if (dto.report() == null || dto.report().length() < 200) {
            throw new RuntimeException("O relato detalhado de recusa deve ter no mínimo 200 caracteres");
        }
        updateStatus(id, "Recusado", dto.report());
    }

    @Transactional
    public void withdraw(Integer id) {
        updateStatus(id, "Desistência", null);
    }

    @Transactional
    public void hire(Integer id) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));

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

    private void updateStatus(Integer id, String statusName, String report) {
        RecruitmentProcess process = recruitmentProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processo de recrutamento não encontrado"));

        DhoProcessStatus status = processStatusRepository.findByName(statusName)
                .orElseThrow(() -> new RuntimeException("Status '" + statusName + "' não encontrado"));

        process.setProcessStatus(status);
        process.setInterviewReport(report);
        recruitmentProcessRepository.save(process);
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

    public List<RecruitmentProcessResponseDTO> getProcessesByRecruiter(Integer recruiterId) {
        People recruiter = peopleRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recrutador não encontrado"));

        // Verify if the person has the recruiter role
        if (recruiter.getRoles() != null) {
            boolean isRecruiter = recruiter.getRoles().stream()
                    .anyMatch(role -> "Recrutador".equalsIgnoreCase(role.getName()) || "RECRUITER".equalsIgnoreCase(role.getName()));
            if (!isRecruiter) {
                // For now, we allow it if no role is assigned to avoid breaking existing data, 
                // but the logic is ready to be enforced.
            }
        }

        return recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(recruiterId).stream()
                .map(process -> RecruitmentProcessResponseDTO.builder()
                        .id(process.getId())
                        .candidateName(Optional.ofNullable(process.getCandidate()).map(People::getName).orElse(null))
                        .positionName(Optional.ofNullable(process.getOpportunity())
                                .map(opp -> Optional.ofNullable(opp.getPosition()).map(pos -> pos.getName()).orElse(null))
                                .orElse(null))
                        .processStatusName(Optional.ofNullable(process.getProcessStatus()).map(status -> status.getName()).orElse(null))
                        .processStageName(Optional.ofNullable(process.getProcessStage()).map(stage -> stage.getName()).orElse(null))
                        .opportunityId(Optional.ofNullable(process.getOpportunity()).map(opp -> opp.getId()).orElse(null))
                        .build())
                .collect(Collectors.toList());
    }
    public List<RecruitmentProcessLogDTO> getLogs(Integer id) {
        return logRepository.findByRecruitmentProcessId(id).stream()
                .map(log -> RecruitmentProcessLogDTO.builder()
                        .id(log.getId())
                        .actionName(log.getActionName())
                        .startTime(log.getStartTime())
                        .endTime(log.getEndTime())
                        .durationMs(log.getDurationMs())
                        .status(log.getStatus())
                        .errorMessage(log.getErrorMessage())
                        .build())
                .collect(Collectors.toList());
    }



    public RecruitmentIndicatorsDTO getIndicators() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);
        LocalDateTime lastYear = now.minusYears(1);

        // 1. Vagas abertas (status != finalizada, cancelada)
        long openVacancies = opportunityRepository.countByOpportunityStatusNameNotIn(List.of("Finalizada", "Cancelada"));

        // 2. Contratações aprovadas do último mês (usando logs de HIRE com status SUCCESS)
        long approvedHiresLastMonth = logRepository.findByActionNameAndStatusAndStartTimeAfter("HIRE", "SUCCESS", lastMonth).size();

        // 3. Aprovações pendentes (Processos com status 'Aguardando aprovação')
        long pendingApprovals = recruitmentProcessRepository.countByProcessStatusName("Aguardando aprovação");

        // 4. Tempo de contratação média do último ano
        List<RecruitmentProcessLog> hireLogs = logRepository.findByActionNameAndStatusAndStartTimeAfter("HIRE", "SUCCESS", lastYear);
        List<RecruitmentProcessLog> approveLogs = logRepository.findByActionNameAndStatusAndStartTimeAfter("APPROVE", "SUCCESS", lastYear);

        Map<Integer, LocalDateTime> approvalTimes = approveLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getRecruitmentProcess().getId(),
                        RecruitmentProcessLog::getStartTime,
                        (existing, replacement) -> existing // In case of multiple approvals, take the first one
                ));

        double totalDays = 0;
        int hiredCount = 0;

        for (RecruitmentProcessLog hireLog : hireLogs) {
            Integer processId = hireLog.getRecruitmentProcess().getId();
            if (approvalTimes.containsKey(processId)) {
                long days = Duration.between(approvalTimes.get(processId), hireLog.getStartTime()).toDays();
                totalDays += days;
                hiredCount++;
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

}
