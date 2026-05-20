package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoBaseOrigin;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoOpportunityStatus;
import br.com.sebratel.bff.dho.domain.enums.Permission;
import br.com.sebratel.bff.dho.domain.repository.*;
import br.com.sebratel.bff.dho.dto.*;
import br.com.sebratel.bff.dho.mapper.OpportunityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final DhoOpportunityStatusRepository statusRepository;
    private final PeopleRepository peopleRepository;
    private final DhoBaseOriginRepository baseOriginRepository;
    private final DhoPositionRepository positionRepository;
    private final DhoTeamRepository teamRepository;
    private final DhoDepartmentRepository departmentRepository;
    private final RecruitmentProcessLogRepository logRepository;
    private final DhoOpportunityMotiveRepository opportunityMotiveRepository;

    private final OpportunityMapper opportunityMapper;
    private final OpportunityWorkflowService workflowService;
    private final OpportunityPipelineService pipelineService;
    private final OpportunityCandidateService candidateService;

    public List<CandidateResponseDTO> findCandidatesForUser(Integer id, Authentication authentication) {
        return candidateService.findCandidatesForUser(id, authentication);
    }

    public List<CandidateResponseDTO> findCandidatesByOpportunityId(Integer id) {
        return candidateService.findCandidatesByOpportunityId(id);
    }

    public List<OpportunityResponseDTO> findAll() {
        return opportunityRepository.findAll().stream()
                .map(opp -> opportunityMapper.convertToDTO(opp, false))
                .collect(Collectors.toList());
    }

    public List<OpportunityResponseDTO> findApprovedOpportunities(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";
        return opportunityRepository.findByOpportunityStatusNameAndResponsibleRecruiterEmail("Aprovada", email).stream()
                .map(opp -> opportunityMapper.convertToDTO(opp, false))
                .collect(Collectors.toList());
    }

    public OpportunityResponseDTO findById(Integer id) {
        return opportunityRepository.findById(id)
                .map(opp -> opportunityMapper.convertToDTO(opp, false))
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
    }

    @Transactional
    public OpportunityResponseDTO create(OpportunityRequestDTO dto, Authentication authentication) {
        DhoOpportunityStatus pendingStatus = statusRepository.findByNameIgnoreCase("Pendente")
                .orElseThrow(() -> new RuntimeException("Status 'Pendente' não encontrado"));

        People requester = Optional.ofNullable(authentication)
                .map(Authentication::getName)
                .flatMap(peopleRepository::findByEmail)
                .orElse(null);

        DhoBaseOrigin baseOrigin = Optional.ofNullable(dto.baseOriginId())
                .flatMap(baseOriginRepository::findById)
                .or(() -> baseOriginRepository.findByName("Porto Alegre"))
                .orElseThrow(() -> new RuntimeException("Base de origem padrão não encontrada"));

        Opportunity opportunity = Opportunity.builder()
                .openOpportunityDate(Optional.ofNullable(dto.openOpportunityDate()).orElse(LocalDateTime.now()))
                .requester(requester)
                .candidate(findOptionalEntity(dto.candidateId(), peopleRepository))
                .position(findOptionalEntity(dto.positionId(), positionRepository))
                .team(findOptionalEntity(dto.teamId(), teamRepository))
                .department(findOptionalEntity(dto.departmentId(), departmentRepository))
                .opportunityMotive(findOptionalEntity(dto.opportunityMotiveId(), opportunityMotiveRepository))
                .replacedPerson(findOptionalEntity(dto.replacedPersonId(), peopleRepository))
                .responsibleRecruiter(findOptionalEntity(dto.responsibleRecruiterId(), peopleRepository))
                .baseOrigin(baseOrigin)
                .opportunityStatus(pendingStatus)
                .deadlineSlaDays(dto.deadlineSlaDays())
                .acceptDate(dto.acceptDate())
                .observations(dto.observations())
                .workSchedule(dto.workSchedule())
                .hardSkills(dto.hardSkills())
                .softSkills(dto.softSkills())
                .build();

        return opportunityMapper.convertToDTO(opportunityRepository.save(opportunity), false);
    }

    private <T, ID> T findOptionalEntity(ID id, org.springframework.data.repository.CrudRepository<T, ID> repository) {
        return Optional.ofNullable(id)
                .flatMap(repository::findById)
                .orElse(null);
    }

    @Transactional
    public OpportunityResponseDTO approve(Integer id, OpportunityApprovalDTO dto) {
        return workflowService.approve(id, dto);
    }

    @Transactional
    public OpportunityResponseDTO refuse(Integer id, OpportunityApprovalDTO dto) {
        return workflowService.refuse(id, dto);
    }

    @Transactional
    public OpportunityResponseDTO finalize(Integer id, OpportunityApprovalDTO dto) {
        return workflowService.finalize(id, dto);
    }

    @Transactional
    public OpportunityResponseDTO assignRecruiter(Integer id, OpportunityAssignRecruiterDTO dto) {
        return workflowService.assignRecruiter(id, dto);
    }

    public List<OpportunityResponseDTO> findAllForUser(Authentication authentication, RequisitionSearchDTO searchDTO) {
        boolean canViewAll = opportunityMapper.hasPermission(authentication, Permission.view_all_requests);
        boolean wantViewAll = searchDTO != null && Boolean.TRUE.equals(searchDTO.getShowAllRequisitions());

        if (canViewAll && wantViewAll) {
            return opportunityRepository.findAll().stream()
                .map(opp -> opportunityMapper.convertToDTO(opp, true))
                .collect(Collectors.toList());
        }
        String email = authentication != null ? authentication.getName() : "";
        return opportunityRepository.findByRequesterEmail(email).stream()
                .map(opp -> opportunityMapper.convertToDTO(opp, true))
                .collect(Collectors.toList());
    }

    public OpportunityResponseDTO findByIdForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (authentication == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        if (!opportunityMapper.hasPermission(authentication, Permission.view_all_requests)) {
            if (opportunity.getRequester() == null || !opportunity.getRequester().getEmail().equals(authentication.getName())) {
                throw new RuntimeException("Acesso negado a esta requisição");
            }
        }

        return opportunityMapper.convertToDTO(opportunity, true);
    }

    public List<RecruitmentProcessLogDTO> getLogs(Integer id) {
        return logRepository.findByRecruitmentProcessOpportunityIdOrderByStartTimeDesc(id).stream()
                .map(log -> RecruitmentProcessLogDTO.builder()
                        .id(log.getId())
                        .actionName(log.getActionName())
                        .startTime(log.getStartTime())
                        .endTime(log.getEndTime())
                        .durationMs(log.getDurationMs())
                        .status(log.getStatus())
                        .errorMessage(log.getErrorMessage())
                        .candidateName(log.getRecruitmentProcess() != null && log.getRecruitmentProcess().getCandidate() != null 
                                ? log.getRecruitmentProcess().getCandidate().getName() : null)
                        .executedBy(log.getExecutedBy())
                        .build())
                .collect(Collectors.toList());
    }

    public List<OpportunityPipelineResponseDTO> getOpportunitiesPipelineForRecruiter(Authentication authentication) {
        return pipelineService.getOpportunitiesPipelineForRecruiter(authentication);
    }

    public OpportunityPipelineResponseDTO getOpportunityPipeline(Integer id) {
        return pipelineService.getOpportunityPipeline(id);
    }
}
