package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.OpportunityPipelineResponseDTO;
import br.com.sebratel.bff.dho.mapper.OpportunityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OpportunityPipelineServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private RecruitmentProcessRepository recruitmentProcessRepository;

    @Mock
    private OpportunityMapper opportunityMapper;

    @InjectMocks
    private OpportunityPipelineService opportunityPipelineService;

    private Opportunity opportunity;
    private People candidate1;
    private People candidate2;
    private RecruitmentProcess process1;
    private RecruitmentProcess process2;

    @BeforeEach
    void setUp() {
        opportunity = Opportunity.builder().id(1).build();
        candidate1 = People.builder().id(1).name("Candidate 1").build();
        candidate2 = People.builder().id(2).name("Candidate 2").build();

        DhoProcessStatus statusAprovado = new DhoProcessStatus();
        statusAprovado.setName("Aprovado");

        DhoProcessStatus statusReprovado = new DhoProcessStatus();
        statusReprovado.setName("Reprovado");

        process1 = RecruitmentProcess.builder()
                .id(10)
                .opportunity(opportunity)
                .candidate(candidate1)
                .processStatus(statusAprovado)
                .build();

        process2 = RecruitmentProcess.builder()
                .id(11)
                .opportunity(opportunity)
                .candidate(candidate2)
                .processStatus(statusReprovado)
                .build();
    }

    @Test
    void shouldReturnAllCandidatesIncludingReprovedOnes() {
        // Given
        Integer oppId = 1;
        when(opportunityRepository.findById(oppId)).thenReturn(Optional.of(opportunity));
        when(recruitmentProcessRepository.findByOpportunityId(oppId)).thenReturn(List.of(process1, process2));
        
        when(opportunityMapper.mapName(any(), any())).thenAnswer(invocation -> {
            Object entity = invocation.getArgument(0);
            if (entity instanceof DhoProcessStatus) {
                return ((DhoProcessStatus) entity).getName();
            }
            return null;
        });

        // When
        OpportunityPipelineResponseDTO response = opportunityPipelineService.getOpportunityPipeline(oppId);

        // Then
        assertEquals(2, response.getOpportunity().getProcesses().size(), "Should return both candidates");
    }
}
