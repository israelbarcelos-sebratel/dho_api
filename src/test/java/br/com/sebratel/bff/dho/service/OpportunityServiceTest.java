package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private DhoOpportunityStatusRepository statusRepository;

    @InjectMocks
    private OpportunityService opportunityService;

    @Test
    void shouldApproveOpportunity() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        
        DhoOpportunityStatus approvedStatus = new DhoOpportunityStatus();
        approvedStatus.setName("Aprovada");

        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(statusRepository.findByName("Aprovada")).thenReturn(Optional.of(approvedStatus));
        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(opportunity);

        OpportunityResponseDTO result = opportunityService.approve(1);

        assertNotNull(result);
        assertEquals("Aprovada", result.getOpportunityStatusName());
        verify(opportunityRepository).save(opportunity);
    }

    @Test
    void shouldRefuseOpportunityWithValidJustification() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        
        DhoOpportunityStatus refusedStatus = new DhoOpportunityStatus();
        refusedStatus.setName("Recusada");

        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(justification);

        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(statusRepository.findByName("Recusada")).thenReturn(Optional.of(refusedStatus));
        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(opportunity);

        OpportunityResponseDTO result = opportunityService.refuse(1, dto);

        assertNotNull(result);
        assertEquals("Recusada", result.getOpportunityStatusName());
        assertEquals(justification, result.getRefusalJustification());
        verify(opportunityRepository).save(opportunity);
    }

    @Test
    void shouldThrowExceptionWhenRefusingWithShortJustification() {
        String justification = "Short justification";
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(justification);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            opportunityService.refuse(1, dto);
        });

        assertEquals("A justificativa de recusa deve ter no mínimo 200 caracteres", exception.getMessage());
        verify(opportunityRepository, never()).save(any());
    }
}
