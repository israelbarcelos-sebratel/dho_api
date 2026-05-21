package br.com.sebratel.bff.dho.service.workflow;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStageRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoProcessStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.service.RecruitmentProcessWorkflowService;
import br.com.sebratel.bff.dho.service.workflow.state.ProcessState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecruitmentProcessWorkflowServiceTest {

    @Mock private RecruitmentProcessRepository recruitmentProcessRepository;
    @Mock private DhoProcessStatusRepository processStatusRepository;
    @Mock private DhoProcessStageRepository processStageRepository;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks
    private RecruitmentProcessWorkflowService workflowService;

    private RecruitmentProcess process;

    @BeforeEach
    void setUp() {
        process = new RecruitmentProcess();
        process.setId(1);
        Opportunity opp = new Opportunity();
        DhoOpportunityStatus oppStatus = new DhoOpportunityStatus();
        oppStatus.setName("Aprovada");
        opp.setOpportunityStatus(oppStatus);
        process.setOpportunity(opp);
        
        DhoProcessStage stage = new DhoProcessStage();
        stage.setName("Banco de Talentos");
        process.setProcessStage(stage);
        
        DhoProcessStatus status = new DhoProcessStatus();
        status.setName("Em andamento");
        process.setProcessStatus(status);
    }

    @Test
    void moveToScreening_ShouldSetStartDate() {
        ProcessState stateMock = mock(ProcessState.class);
        lenient().when(stateMock.getStageName()).thenReturn("Banco de Talentos");
        lenient().when(applicationContext.getBean(anyString())).thenReturn(stateMock);
        
        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStageRepository.findByNameIgnoreCase("Triagem")).thenReturn(Optional.of(new DhoProcessStage()));
        
        workflowService.moveToScreening(1);
        
        assertNotNull(process.getStartDate(), "Start date should be set when moving to screening");
    }

    @Test
    void withdraw_ShouldSetEndDate() {
        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByNameIgnoreCase("Desistência")).thenReturn(Optional.of(new DhoProcessStatus()));
        
        workflowService.withdraw(1);
        
        assertNotNull(process.getEndDate(), "End date should be set on withdrawal");
    }

    @Test
    void refuse_ShouldSetEndDate() {
        when(recruitmentProcessRepository.findById(1)).thenReturn(Optional.of(process));
        when(processStatusRepository.findByNameIgnoreCase("Reprovado")).thenReturn(Optional.of(new DhoProcessStatus()));

        workflowService.refuse(1, new br.com.sebratel.bff.dho.dto.InterviewDecisionDTO("Reprovação por falta de fit técnico comprovado em entrevista"));
        
        assertNotNull(process.getEndDate(), "End date should be set on refusal");
    }
}
