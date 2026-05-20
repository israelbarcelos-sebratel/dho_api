package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.*;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.*;
import br.com.sebratel.bff.dho.mapper.RecruitmentProcessMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecruitmentProcessServiceTest {

    @Mock private RecruitmentProcessRepository recruitmentProcessRepository;
    @Mock private DhoProcessStatusRepository processStatusRepository;
    @Mock private DhoProcessStageRepository processStageRepository;
    @Mock private PeopleRepository peopleRepository;
    @Mock private RecruitmentProcessLogRepository logRepository;
    @Mock private OpportunityRepository opportunityRepository;
    @Mock private RecruitmentProcessMapper recruitmentProcessMapper;
    @Mock private RecruitmentProcessWorkflowService workflowService;
    @Mock private RecruitmentProcessQueryService queryService;

    @InjectMocks
    private RecruitmentProcessService recruitmentProcessService;

    private RecruitmentProcess process;
    private People candidate;

    @BeforeEach
    void setUp() {
        candidate = new People();
        candidate.setId(1);
        process = new RecruitmentProcess();
        process.setId(1);
        process.setCandidate(candidate);
    }

    @Test
    void create_ShouldWork() {
        RecruitmentProcessRequestDTO dto = new RecruitmentProcessRequestDTO(1, 1);
        Opportunity opp = new Opportunity();
        DhoOpportunityStatus status = new DhoOpportunityStatus();
        status.setName("Aprovada");
        opp.setOpportunityStatus(status);

        when(peopleRepository.findById(1)).thenReturn(Optional.of(candidate));
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opp));
        when(processStatusRepository.findByName("Em andamento")).thenReturn(Optional.of(new DhoProcessStatus()));
        when(processStageRepository.findByName("Banco de Talentos")).thenReturn(Optional.of(new DhoProcessStage()));
        when(recruitmentProcessRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recruitmentProcessMapper.mapToResponseDTO(any())).thenReturn(RecruitmentProcessResponseDTO.builder().build());

        assertNotNull(recruitmentProcessService.create(dto));
        verify(recruitmentProcessMapper).mapToResponseDTO(any());
    }

    @Test
    void hire_ShouldWork() {
        recruitmentProcessService.hire(1);
        verify(workflowService).hire(1);
    }

    @Test
    void refuse_ShouldWork() {
        InterviewDecisionDTO dto = new InterviewDecisionDTO("A".repeat(200));
        recruitmentProcessService.refuse(1, dto);
        verify(workflowService).refuse(1, dto);
    }

    @Test
    void getIndicators_ShouldWork() {
        when(queryService.getIndicators()).thenReturn(new RecruitmentIndicatorsDTO(0L, 0L, 0L, 0L, 0L, 0L, null));
        assertNotNull(recruitmentProcessService.getIndicators());
        verify(queryService).getIndicators();
    }

    @Test
    void managerDecision_ShouldApproveAndMoveStage() {
        ManagerDecisionDTO dto = new ManagerDecisionDTO(true, "A".repeat(200));
        recruitmentProcessService.managerDecision(1, dto);
        verify(workflowService).managerDecision(1, dto);
    }

    @Test
    void managerDecision_ShouldReprove() {
        ManagerDecisionDTO dto = new ManagerDecisionDTO(false, "A".repeat(200));
        recruitmentProcessService.managerDecision(1, dto);
        verify(workflowService).managerDecision(1, dto);
    }
}
