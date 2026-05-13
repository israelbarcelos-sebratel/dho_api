package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

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
    @Mock private DhoPositionRepository positionRepository;
    @Mock private DhoTeamRepository teamRepository;
    @Mock private DhoDepartmentRepository departmentRepository;
    @Mock private RecruitmentProcessLogRepository logRepository;
    @Mock private DhoOpportunityMotiveRepository opportunityMotiveRepository;

    @InjectMocks
    private OpportunityService opportunityService;

    @Test
    void shouldApproveOpportunity() {
        DhoOpportunityStatus pendingStatus = new DhoOpportunityStatus();
        pendingStatus.setName("Pendente");
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setOpportunityStatus(pendingStatus);
        DhoOpportunityStatus approvedStatus = new DhoOpportunityStatus();
        approvedStatus.setName("Aprovada");
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(statusRepository.findByName("Aprovada")).thenReturn(Optional.of(approvedStatus));
        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(opportunity);
        OpportunityResponseDTO result = opportunityService.approve(1);
        assertNotNull(result);
        assertEquals("Aprovada", result.getOpportunityStatusName());
    }

    @Test
    void approve_ShouldThrowException_WhenAlreadyApproved() {
        DhoOpportunityStatus approvedStatus = new DhoOpportunityStatus();
        approvedStatus.setName("Aprovada");
        Opportunity opportunity = new Opportunity();
        opportunity.setOpportunityStatus(approvedStatus);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        assertThrows(ResponseStatusException.class, () -> opportunityService.approve(1));
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
    }

    @Test
    void create_ShouldWork() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, null, null, null, null, null, null, null, null
        );
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.of(new DhoOpportunityStatus()));
        when(baseOriginRepository.findByName("Porto Alegre")).thenReturn(Optional.of(new DhoBaseOrigin()));
        when(opportunityRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(opportunityService.create(dto, null));
    }

    @Test
    void findCandidatesForUser_ShouldAllowAdmin() {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();
        Opportunity opportunity = new Opportunity();
        DhoOpportunityStatus status = new DhoOpportunityStatus();
        status.setName("Aprovada");
        opportunity.setOpportunityStatus(status);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(recruitmentProcessRepository.findByOpportunityId(1)).thenReturn(Collections.emptyList());
        assertNotNull(opportunityService.findCandidatesForUser(1, auth));
    }


    @Test
    void findByIdForUser_ShouldReturnOpportunity_WhenAdmin() {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setOpportunityStatus(new DhoOpportunityStatus());
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        assertNotNull(opportunityService.findByIdForUser(1, auth));
    }

    @Test
    void findByIdForUser_ShouldReturnOpportunity_WhenOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("owner@test.com");
        doReturn(Collections.emptyList()).when(auth).getAuthorities();
        People requester = new People();
        requester.setEmail("owner@test.com");
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setRequester(requester);
        opportunity.setOpportunityStatus(new DhoOpportunityStatus());
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        assertNotNull(opportunityService.findByIdForUser(1, auth));
    }

    @Test
    void findByIdForUser_ShouldThrowException_WhenNotFound() {
        when(opportunityRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> opportunityService.findByIdForUser(1, null));
    }

    @Test
    void findByIdForUser_ShouldThrowException_WhenNotAdminAndNotOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("other@test.com");
        doReturn(Collections.emptyList()).when(auth).getAuthorities();
        People requester = new People();
        requester.setEmail("owner@test.com");
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setRequester(requester);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> opportunityService.findByIdForUser(1, auth));
        assertEquals("Acesso negado a esta requisição", ex.getMessage());
    }

    @Test
    void findByIdForUser_ShouldThrowException_WhenNotAdminAndRequesterIsNull() {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.emptyList()).when(auth).getAuthorities();
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setRequester(null);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> opportunityService.findByIdForUser(1, auth));
        assertEquals("Acesso negado a esta requisição", ex.getMessage());
    }

}
