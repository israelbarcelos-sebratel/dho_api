package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessHistoryDTO;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecruitmentProcessService {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final DhoProcessStatusRepository processStatusRepository;
    private final PeopleRepository peopleRepository;

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
                        .candidateName(process.getCandidate() != null ? process.getCandidate().getName() : null)
                        .positionName(process.getOpportunity() != null && process.getOpportunity().getPosition() != null ? process.getOpportunity().getPosition().getName() : null)
                        .statusName(process.getProcessStatus() != null ? process.getProcessStatus().getName() : null)
                        .admissionDate(process.getCandidate() != null ? process.getCandidate().getAdmissionDate() : null)
                        .build())
                .collect(Collectors.toList());
    }

}
