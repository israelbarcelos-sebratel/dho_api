package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoRole;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoRoleRepository;
import br.com.sebratel.bff.dho.dto.InterviewDecisionDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecruitmentProcessServiceTest {

    @Mock
    private RecruitmentProcessRepository recruitmentProcessRepository;

    @Mock
    private DhoProcessStatusRepository processStatusRepository;

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private DhoRoleRepository roleRepository;

    @Mock
    private RecruitmentProcessLogRepository logRepository;

    @InjectMocks
    private RecruitmentProcessService recruitmentProcessService;

    private RecruitmentProcess process;
    private DhoProcessStatus status;
    private People candidate;

    @BeforeEach
    void setUp() {
        candidate = new People();
        candidate.setId(1);
        candidate.setName("John Doe");

        process = new RecruitmentProcess();
        process.setId(1);
        process.setCandidate(candidate);

        status = new DhoProcessStatus();
        status.setName("Aprovado");
    }

    @Test
    void hire_ShouldUpdateStatusAndAdmissionDate() {
        DhoProcessStatus hiredStatus = new DhoProcessStatus();
        hiredStatus.setName("Contratado");

        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Contratado")).thenReturn(Optional.of(hiredStatus));

        recruitmentProcessService.hire(1);

        assertEquals("Contratado", process.getProcessStatus().getName());
        assertNotNull(candidate.getAdmissionDate());
        verify(peopleRepository).save(candidate);
        verify(recruitmentProcessRepository).save(process);
    }

    @Test
    void refuse_ShouldThrowException_WhenReportIsTooShort() {
        InterviewDecisionDTO dto = new InterviewDecisionDTO("Short report");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            recruitmentProcessService.refuse(1, dto);
        });

        assertEquals("O relato detalhado de recusa deve ter no mínimo 200 caracteres", exception.getMessage());
        verify(recruitmentProcessRepository, never()).save(any());
    }

    @Test
    void refuse_ShouldUpdateStatus_WhenReportIsValid() {
        String longReport = "A".repeat(200);
        InterviewDecisionDTO dto = new InterviewDecisionDTO(longReport);
        
        DhoProcessStatus refusedStatus = new DhoProcessStatus();
        refusedStatus.setName("Recusado");

        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Recusado")).thenReturn(Optional.of(refusedStatus));

        recruitmentProcessService.refuse(1, dto);

        assertEquals("Recusado", process.getProcessStatus().getName());
        assertEquals(longReport, process.getInterviewReport());
        verify(recruitmentProcessRepository).save(process);
    }

    @Test
    void getProcessesByRecruiter_ShouldReturnProcesses() {
        People recruiter = new People();
        recruiter.setId(1);
        DhoRole role = new DhoRole();
        role.setName("Recrutador");
        recruiter.setRoles(new HashSet<>(Collections.singletonList(role)));

        Opportunity opportunity = new Opportunity();
        opportunity.setId(10);
        DhoPosition position = new DhoPosition();
        position.setName("Developer");
        opportunity.setPosition(position);
        
        process.setOpportunity(opportunity);
        process.setProcessStatus(status);

        when(peopleRepository.findById(1)).thenReturn(Optional.of(recruiter));
        when(recruitmentProcessRepository.findByOpportunityResponsibleRecruiterId(1))
                .thenReturn(List.of(process));

        List<RecruitmentProcessResponseDTO> result = recruitmentProcessService.getProcessesByRecruiter(1);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getCandidateName());
        assertEquals("Developer", result.get(0).getPositionName());
        assertEquals(10, result.get(0).getOpportunityId());
    }

    @Test
    void getLogs_ShouldReturnLogs() {
        RecruitmentProcessLog log = new RecruitmentProcessLog();
        log.setId(1);
        log.setActionName("APPROVE");

        when(logRepository.findByRecruitmentProcessId(1)).thenReturn(List.of(log));

        List<RecruitmentProcessLogDTO> result = recruitmentProcessService.getLogs(1);

        assertEquals(1, result.size());
        assertEquals("APPROVE", result.get(0).actionName());
    }
}
