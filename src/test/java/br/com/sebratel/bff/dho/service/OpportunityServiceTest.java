package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoBaseOrigin;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
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
    void create_ShouldWorkWithAuthentication() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, null, null, null, null, null, null, null, null
        );
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@test.com");
        when(peopleRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new People()));
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.of(new DhoOpportunityStatus()));
        when(baseOriginRepository.findByName("Porto Alegre")).thenReturn(Optional.of(new DhoBaseOrigin()));
        when(opportunityRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(opportunityService.create(dto, auth));
    }


    @Test
    void create_ShouldWorkWithProvidedBaseOrigin() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, 1, null, null, null, null, null, null, null
        );
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.of(new DhoOpportunityStatus()));
        when(baseOriginRepository.findById(1)).thenReturn(Optional.of(new DhoBaseOrigin()));
        when(opportunityRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertNotNull(opportunityService.create(dto, null));
    }

    @Test
    void create_ShouldThrowException_WhenDefaultBaseOriginNotFound() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, null, null, null, null, null, null, null, null
        );
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.of(new DhoOpportunityStatus()));
        when(baseOriginRepository.findByName("Porto Alegre")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> opportunityService.create(dto, null));
        assertEquals("Base de origem padrão não encontrada", ex.getMessage());
    }

    @Test
    void create_ShouldThrowException_WhenStatusNotFound() {
        OpportunityRequestDTO dto = new OpportunityRequestDTO(
            null, null, 1, 1, 1, 1, "work", "hard", "soft", null, null, null, null, null, null, null, null, null
        );
        when(statusRepository.findByName("Pendente")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> opportunityService.create(dto, null));
        assertEquals("Status 'Pendente' não encontrado", ex.getMessage());
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
    void findByIdForUser_ShouldReturnOpportunity_WhenHasViewAllRequestsPermission() {
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singleton(new SimpleGrantedAuthority("view_all_requests"))).when(auth).getAuthorities();
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setOpportunityStatus(new DhoOpportunityStatus());
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        assertNotNull(opportunityService.findByIdForUser(1, auth));
    }

    @Test
    void findByIdForUser_ShouldThrowException_WhenAuthenticationIsNull() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
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

    @Test
    void findById_ShouldReturnOpportunity_WhenExists() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        opportunity.setOpportunityStatus(new DhoOpportunityStatus());
        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        OpportunityResponseDTO result = opportunityService.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(opportunityRepository).findById(1);
    }

    @Test
    void getLogs_ShouldReturnEmptyList_WhenNoLogsFound() {
        when(logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(1))
                .thenReturn(Collections.emptyList());

        List<RecruitmentProcessLogDTO> result = opportunityService.getLogs(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(logRepository).findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(1);
    }

    @Test
    void getLogs_ShouldMapLogsCorrectly_WhenAllDataIsPresent() {
        People candidate = People.builder().name("John Doe").build();
        RecruitmentProcess process = RecruitmentProcess.builder().candidate(candidate).build();
        RecruitmentProcessLog log = RecruitmentProcessLog.builder()
                .id(1)
                .actionName("TEST_ACTION")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusSeconds(1))
                .durationMs(1000L)
                .status("SUCCESS")
                .errorMessage("No error")
                .recruitmentProcess(process)
                .build();

        when(logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(1))
                .thenReturn(List.of(log));

        List<RecruitmentProcessLogDTO> result = opportunityService.getLogs(1);

        assertEquals(1, result.size());
        RecruitmentProcessLogDTO dto = result.getFirst();
        assertEquals(log.getId(), dto.id());
        assertEquals(log.getActionName(), dto.actionName());
        assertEquals(log.getStartTime(), dto.startTime());
        assertEquals(log.getEndTime(), dto.endTime());
        assertEquals(log.getDurationMs(), dto.durationMs());
        assertEquals(log.getStatus(), dto.status());
        assertEquals(log.getErrorMessage(), dto.errorMessage());
        assertEquals("John Doe", dto.candidateName());
    }

    @Test
    void getLogs_ShouldReturnNullCandidateName_WhenProcessIsNull() {
        RecruitmentProcessLog log = RecruitmentProcessLog.builder()
                .recruitmentProcess(null)
                .build();

        when(logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(1))
                .thenReturn(List.of(log));

        List<RecruitmentProcessLogDTO> result = opportunityService.getLogs(1);

        assertNull(result.getFirst().candidateName());
    }

    @Test
    void getLogs_ShouldReturnNullCandidateName_WhenCandidateIsNull() {
        RecruitmentProcess process = RecruitmentProcess.builder().candidate(null).build();
        RecruitmentProcessLog log = RecruitmentProcessLog.builder()
                .recruitmentProcess(process)
                .build();

        when(logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(1))
                .thenReturn(List.of(log));

        List<RecruitmentProcessLogDTO> result = opportunityService.getLogs(1);

        assertNull(result.getFirst().candidateName());
    }

    @Test
    void finalize_ShouldThrowException_WhenStatusNotFound() {
        Opportunity opportunity = new Opportunity();
        opportunity.setId(1);
        String justification = "A".repeat(200);
        OpportunityApprovalDTO dto = new OpportunityApprovalDTO(justification);

        when(opportunityRepository.findById(1)).thenReturn(Optional.of(opportunity));
        when(statusRepository.findByName("Finalizada")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> opportunityService.finalize(1, dto));
        assertEquals("Status 'Finalizada' não encontrado", ex.getMessage());
    }

}
