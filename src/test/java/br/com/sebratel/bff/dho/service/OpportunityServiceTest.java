package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoBaseOrigin;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.*;
import br.com.sebratel.bff.dho.mapper.OpportunityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpportunityServiceTest {

    @Mock private OpportunityRepository opportunityRepository;
    @Mock private DhoOpportunityStatusRepository statusRepository;
    @Mock private RecruitmentProcessRepository recruitmentProcessRepository;
    @Mock private PeopleRepository peopleRepository;
    @Mock private DhoBaseOriginRepository baseOriginRepository;
    @Mock private RecruitmentProcessLogRepository logRepository;
    @Mock private DhoPositionRepository positionRepository;
    @Mock private DhoTeamRepository teamRepository;
    @Mock private DhoDepartmentRepository departmentRepository;
    @Mock private DhoOpportunityMotiveRepository opportunityMotiveRepository;

    @Mock private OpportunityMapper opportunityMapper;
    @Mock private OpportunityWorkflowService workflowService;
    @Mock private OpportunityPipelineService pipelineService;
    @Mock private OpportunityCandidateService candidateService;

    @InjectMocks
    private OpportunityService opportunityService;

    @Test
    void shouldApproveOpportunity() {
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), "A".repeat(200));
        when(workflowService.approve(eq(1), any(OpportunityApprovalDTO.class))).thenReturn(OpportunityResponseDTO.builder().opportunityStatusName("Aprovada").build());
        
        OpportunityResponseDTO result = opportunityService.approve(1, dto);
        
        assertNotNull(result);
        assertEquals("Aprovada", result.getOpportunityStatusName());
        verify(workflowService).approve(eq(1), any(OpportunityApprovalDTO.class));
    }

    @Test
    void approve_ShouldThrowException_WhenAlreadyApproved() {
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), "A".repeat(200));
        when(workflowService.approve(eq(1), any(OpportunityApprovalDTO.class))).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST));
        
        assertThrows(ResponseStatusException.class, () -> opportunityService.approve(1, dto));
    }

    @Test
    void shouldRefuseOpportunityWithValidJustification() {
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(LocalDateTime.now().plusDays(1), "A".repeat(200));
        when(workflowService.refuse(eq(1), any(OpportunityApprovalDTO.class))).thenReturn(OpportunityResponseDTO.builder().opportunityStatusName("Recusada").build());
        
        OpportunityResponseDTO result = opportunityService.refuse(1, dto);
        
        assertNotNull(result);
        assertEquals("Recusada", result.getOpportunityStatusName());
    }

    @Test
    void create_ShouldWork() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, null, null, null, null, null, null, null, null
        );
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.of(new DhoOpportunityStatus()));
        when(baseOriginRepository.findByName("Porto Alegre")).thenReturn(Optional.of(new DhoBaseOrigin()));
        when(opportunityRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(opportunityMapper.convertToDTO(any(), eq(false))).thenReturn(new OpportunityResponseDTO());
        
        assertNotNull(opportunityService.create(dto, null));
    }

    @Test
    void findCandidatesForUser_ShouldAllowAdmin() {
        Authentication auth = mock(Authentication.class);
        when(candidateService.findCandidatesForUser(1, auth)).thenReturn(Collections.emptyList());
        
        assertNotNull(opportunityService.findCandidatesForUser(1, auth));
    }

    @Test
    void findByIdForUser_ShouldReturnOpportunity_WhenAdmin() {
        Authentication auth = mock(Authentication.class);
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(opportunityMapper.hasPermission(any(), any())).thenReturn(true);
        when(opportunityMapper.convertToDTO(any(), eq(true))).thenReturn(new OpportunityResponseDTO());
        
        assertNotNull(opportunityService.findByIdForUser(1, auth));
    }

    @Test
    void assignRecruiter_ShouldWork() {
        OpportunityAssignRecruiterDTO dto = new OpportunityAssignRecruiterDTO(10);
        when(workflowService.assignRecruiter(eq(1), any())).thenReturn(OpportunityResponseDTO.builder().responsibleRecruiterName("Recruiter Name").build());

        OpportunityResponseDTO result = opportunityService.assignRecruiter(1, dto);

        assertNotNull(result);
        assertEquals("Recruiter Name", result.getResponsibleRecruiterName());
    }
}
