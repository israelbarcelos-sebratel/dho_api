package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.*;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.*;
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
        assertNotNull(recruitmentProcessService.create(dto));
    }

    @Test
    void hire_ShouldWork() {
        DhoProcessStatus hiredStatus = new DhoProcessStatus();
        hiredStatus.setName("Contratado");
        Opportunity opp = new Opportunity();
        DhoOpportunityStatus oppStatus = new DhoOpportunityStatus();
        oppStatus.setName("Aprovada");
        opp.setOpportunityStatus(oppStatus);
        process.setOpportunity(opp);
        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Contratado")).thenReturn(Optional.of(hiredStatus));
        recruitmentProcessService.hire(1);
        verify(recruitmentProcessRepository).save(process);
    }

    @Test
    void refuse_ShouldWork() {
        InterviewDecisionDTO dto = new InterviewDecisionDTO("A".repeat(200));
        Opportunity opp = new Opportunity();
        DhoOpportunityStatus oppStatus = new DhoOpportunityStatus();
        oppStatus.setName("Aprovada");
        opp.setOpportunityStatus(oppStatus);
        process.setOpportunity(opp);
        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Reprovado")).thenReturn(Optional.of(new DhoProcessStatus()));
        recruitmentProcessService.refuse(1, dto);
        verify(recruitmentProcessRepository).save(process);
    }

    @Test
    void getIndicators_ShouldWork() {
        when(opportunityRepository.countByOpportunityStatusNameNotIn(any())).thenReturn(5L);
        when(logRepository.findByActionNameAndStatusAndStartTimeAfter(any(), any(), any())).thenReturn(Collections.emptyList());
        when(recruitmentProcessRepository.countByProcessStatusName(any())).thenReturn(2L);
        assertNotNull(recruitmentProcessService.getIndicators());
    }

    @Test
    void managerDecision_ShouldApproveAndMoveStage() {
        ManagerDecisionDTO dto = new ManagerDecisionDTO(true, "A".repeat(200));

        DhoProcessStage stage = new DhoProcessStage();
        stage.setName("Decisão Final");
        process.setProcessStage(stage);

        Opportunity opp = new Opportunity();
        DhoOpportunityStatus oppStatus = new DhoOpportunityStatus();
        oppStatus.setName("Aprovada");
        opp.setOpportunityStatus(oppStatus);
        process.setOpportunity(opp);

        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Aprovado")).thenReturn(Optional.of(new DhoProcessStatus()));
        when(processStageRepository.findByName("Aprovado")).thenReturn(Optional.of(new DhoProcessStage()));

        recruitmentProcessService.managerDecision(1, dto);

        verify(recruitmentProcessRepository, times(2)).save(process);
    }

    @Test
    void managerDecision_ShouldReprove() {
        ManagerDecisionDTO dto = new ManagerDecisionDTO(false, "A".repeat(200));

        DhoProcessStage stage = new DhoProcessStage();
        stage.setName("Decisão Final");
        process.setProcessStage(stage);

        Opportunity opp = new Opportunity();
        DhoOpportunityStatus oppStatus = new DhoOpportunityStatus();
        oppStatus.setName("Aprovada");
        opp.setOpportunityStatus(oppStatus);
        process.setOpportunity(opp);

        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByName("Reprovado")).thenReturn(Optional.of(new DhoProcessStatus()));

        recruitmentProcessService.managerDecision(1, dto);

        verify(recruitmentProcessRepository).save(process);
    }
}
