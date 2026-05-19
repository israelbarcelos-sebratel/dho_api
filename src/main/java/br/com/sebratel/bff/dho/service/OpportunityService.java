package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.repository.DhoBaseOriginRepository;
import lombok.extern.slf4j.Slf4j;

import br.com.sebratel.bff.dho.dto.RecruitmentProcessLogDTO;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcessLog;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessLogRepository;

import br.com.sebratel.bff.dho.domain.repository.*;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;

import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.DhoOpportunityStatusRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.domain.repository.PeopleRepository;
import br.com.sebratel.bff.dho.dto.OpportunityAssignRecruiterDTO;
import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.dto.OpportunityApprovalDTO;
import br.com.sebratel.bff.dho.dto.OpportunityRequestDTO;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;


import br.com.sebratel.bff.dho.domain.enums.Permission;
import br.com.sebratel.bff.dho.dto.UserResponseDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import br.com.sebratel.bff.dho.dto.RequisitionSearchDTO;

import br.com.sebratel.bff.dho.dto.OpportunityPipelineResponseDTO;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final DhoOpportunityStatusRepository statusRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final PeopleRepository peopleRepository;
    private final DhoBaseOriginRepository baseOriginRepository;
    private final DhoPositionRepository positionRepository;
    private final DhoTeamRepository teamRepository;
    private final DhoDepartmentRepository departmentRepository;
    private final RecruitmentProcessLogRepository logRepository;

    private final DhoOpportunityMotiveRepository opportunityMotiveRepository;
    private final RecruitmentProcessService recruitmentProcessService;

    public List<CandidateResponseDTO> findCandidatesForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        if (!"Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A requisição deve estar aprovada para ver os candidatos");
        }

        boolean isAdmin = hasPermission(authentication, Permission.view_all_requests);
        boolean isRequester = opportunity.getRequester() != null && opportunity.getRequester().getEmail().equals(authentication.getName());

        if (!isAdmin && !isRequester) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado a esta requisição");
        }

        return findCandidatesByOpportunityId(id);
    }

    public List<CandidateResponseDTO> findCandidatesByOpportunityId(Integer id) {
        return recruitmentProcessRepository.findByOpportunityId(id).stream()
                .map(rp -> CandidateResponseDTO.builder()
                        .id(rp.getCandidate().getId())
                        .name(rp.getCandidate().getName())
                        .processStage(mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatus(mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                        .build())
                .collect(Collectors.toList());
    }

    public List<OpportunityResponseDTO> findAll() {
        return opportunityRepository.findAll().stream()
                .map(opp -> convertToDTO(opp, false))
                .collect(Collectors.toList());
    }
    public List<OpportunityResponseDTO> findApprovedOpportunities(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";
        return opportunityRepository.findByOpportunityStatusNameAndResponsibleRecruiterEmail("Aprovada", email).stream()
                .map(opp -> convertToDTO(opp, false))
                .collect(Collectors.toList());
    }


    public OpportunityResponseDTO findById(Integer id) {
        return opportunityRepository.findById(id)
                .map(opp -> convertToDTO(opp, false))
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));
    }

    @Transactional
    public OpportunityResponseDTO create(OpportunityRequestDTO dto, Authentication authentication) {
        DhoOpportunityStatus pendingStatus = statusRepository.findByName("Pendente")
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

        return convertToDTO(opportunityRepository.save(opportunity), false);
    }

    private <T, ID> T findOptionalEntity(ID id, org.springframework.data.repository.CrudRepository<T, ID> repository) {
        return Optional.ofNullable(id)
                .flatMap(repository::findById)
                .orElse(null);
    }

    @Transactional
    public OpportunityResponseDTO approve(Integer id, OpportunityApprovalDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if ("Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta oportunidade já está aprovada");
        }

        DhoOpportunityStatus approvedStatus = statusRepository.findByName("Aprovada")
                .orElseThrow(() -> new RuntimeException("Status 'Aprovada' não encontrado"));

        opportunity.setOpportunityStatus(approvedStatus);
        opportunity.setDeadlineSlaDays(30);
        opportunity.setOpenOpportunityDate(dto.opportunityDate());
        
        return convertToDTO(opportunityRepository.save(opportunity), false);
    }

    @Transactional
    public OpportunityResponseDTO refuse(Integer id, OpportunityApprovalDTO dto) {
        log.info("[OpportunityService] refuse - Starting reproval process for opportunity ID: {}", id);
        
        log.debug("[OpportunityService] refuse - Fetching opportunity from database for ID: {}", id);
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[OpportunityService] refuse - Opportunity not found with ID: {}", id);
                    return new RuntimeException("Oportunidade não encontrada");
                });
        log.debug("[OpportunityService] refuse - Opportunity found: {}", opportunity.getPosition() != null ? opportunity.getPosition().getName() : "Unknown");

        log.debug("[OpportunityService] refuse - Fetching 'Recusada' status from database");
        DhoOpportunityStatus refusedStatus = statusRepository.findByName("Recusada")
                .orElseThrow(() -> {
                    log.error("[OpportunityService] refuse - Status 'Recusada' not found in database");
                    return new RuntimeException("Status 'Recusada' não encontrado");
                });

        opportunity.setOpportunityStatus(refusedStatus);
        opportunity.setRefusalJustification(dto.reason());
        
        log.info("[OpportunityService] refuse - Saving updated opportunity ID: {} with status 'Recusada'", id);
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        
        log.info("[OpportunityService] refuse - Opportunity ID: {} successfully reproved and saved", id);
        return convertToDTO(savedOpportunity, false);
    }

    @Transactional
    public OpportunityResponseDTO finalize(Integer id, OpportunityApprovalDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        DhoOpportunityStatus finalizedStatus = statusRepository.findByName("Finalizada")
                .orElseThrow(() -> new RuntimeException("Status 'Finalizada' não encontrado"));

        opportunity.setOpportunityStatus(finalizedStatus);
        opportunity.setFinalizationJustification(dto.reason());
        return convertToDTO(opportunityRepository.save(opportunity), false);
    }

    @Transactional
    public OpportunityResponseDTO assignRecruiter(Integer id, OpportunityAssignRecruiterDTO dto) {
        log.info("[SERVICE ENTRY] assignRecruiter - id: {}, recruiterId: {}", id, dto.recruiterId());
        
        log.info("[REPO CALL] opportunityRepository.findById - id: {}", id);
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[REPO RESULT] Oportunidade não encontrada com ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada");
                });
        log.info("[REPO RESULT] Oportunidade encontrada: {}", opportunity.getId());

        log.info("[REPO CALL] peopleRepository.findById - id: {}", dto.recruiterId());
        People recruiter = peopleRepository.findById(dto.recruiterId())
                .orElseThrow(() -> {
                    log.error("[REPO RESULT] Recrutador não encontrado com ID: {}", dto.recruiterId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado");
                });
        log.info("[REPO RESULT] Recrutador encontrado: {}", recruiter.getName());

        log.info("[ACTION] Atribuindo recrutador {} à oportunidade {}", recruiter.getName(), opportunity.getId());
        opportunity.setResponsibleRecruiter(recruiter);
        
        log.info("[REPO CALL] opportunityRepository.save");
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);
        log.info("[REPO RESULT] Oportunidade salva com sucesso");
        
        OpportunityResponseDTO response = convertToDTO(savedOpportunity, false);
        log.info("[SERVICE EXIT] assignRecruiter finalizado para oportunidade {}", id);
        return response;
    }

    private OpportunityResponseDTO convertToDTO(Opportunity opportunity, boolean includeCandidates) {
        String statusName = mapName(opportunity.getOpportunityStatus(), DhoOpportunityStatus::getName);
        String uiStatus = "Pendente".equals(statusName) ? "Enviado para aprovação" : statusName;
        
        String statusVariant = "warning";
        if ("Aprovada".equals(statusName)) statusVariant = "success";
        if ("Recusada".equals(statusName)) statusVariant = "danger";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = opportunity.getOpenOpportunityDate() != null 
            ? opportunity.getOpenOpportunityDate().format(formatter) 
            : "";

        UserResponseDTO requesterDTO = null;
        if (opportunity.getRequester() != null) {
            List<String> roles = Collections.emptyList();
            if (opportunity.getRequester().getRoles() != null) {
                roles = opportunity.getRequester().getRoles().stream()
                        .map(DhoRole::getName)
                        .collect(Collectors.toList());
            }

            requesterDTO = UserResponseDTO.builder()
                    .id(opportunity.getRequester().getId())
                    .name(opportunity.getRequester().getName())
                    .email(opportunity.getRequester().getEmail())
                    .roles(roles)
                    .build();
        }

        List<RecruitmentProcessResponseDTO> recruitmentProcesses = includeCandidates
            ? recruitmentProcessRepository.findByOpportunityId(opportunity.getId()).stream()
                .map(recruitmentProcessService::mapToResponseDTO)
                .collect(Collectors.toList())
            : null;

        return OpportunityResponseDTO.builder()
                .id(opportunity.getId())
                .openOpportunityDate(opportunity.getOpenOpportunityDate())
                .positionName(mapName(opportunity.getPosition(), DhoPosition::getName))
                .teamName(mapName(opportunity.getTeam(), DhoTeam::getName))
                .departmentName(mapName(opportunity.getDepartment(), DhoDepartment::getName))
                .opportunityMotiveName(mapName(opportunity.getOpportunityMotive(), DhoOpportunityMotive::getName))
                .baseOriginName(mapName(opportunity.getBaseOrigin(), DhoBaseOrigin::getName))
                .opportunityStatusName(statusName)
                .processStageName(null)
                .processStatusName(null)
                .deadlineSlaDays(opportunity.getDeadlineSlaDays())
                .acceptDate(opportunity.getAcceptDate())
                .responsibleRecruiterName(mapName(opportunity.getResponsibleRecruiter(), People::getName))
                .observations(opportunity.getObservations())
                .refusalJustification(opportunity.getRefusalJustification())
                .finalizationJustification(opportunity.getFinalizationJustification())
                .workSchedule(opportunity.getWorkSchedule())
                .hardSkills(opportunity.getHardSkills())
                .softSkills(opportunity.getSoftSkills())
                .title(mapName(opportunity.getPosition(), DhoPosition::getName))
                .type(opportunity.getWorkSchedule())
                .team(mapName(opportunity.getTeam(), DhoTeam::getName))
                .status(uiStatus)
                .date(formattedDate)
                .statusVariant(statusVariant)
                .requester(requesterDTO)
                .recruitmentProcesses(recruitmentProcesses)
                .build();
    }

    public List<OpportunityResponseDTO> findAllForUser(Authentication authentication, RequisitionSearchDTO searchDTO) {
        boolean canViewAll = hasPermission(authentication, Permission.view_all_requests);
        boolean wantViewAll = searchDTO != null && Boolean.TRUE.equals(searchDTO.getShowAllRequisitions());

        if (canViewAll && wantViewAll) {
            return opportunityRepository.findAll().stream()
                .map(opp -> convertToDTO(opp, true))
                .collect(Collectors.toList());
        }
        String email = authentication != null ? authentication.getName() : "";
        return opportunityRepository.findByRequesterEmail(email).stream()
                .map(opp -> convertToDTO(opp, true))
                .collect(Collectors.toList());
    }

    public OpportunityResponseDTO findByIdForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidade não encontrada"));

        if (authentication == null) {
            throw new RuntimeException("Usuário não autenticado");
        }

        if (!hasPermission(authentication, Permission.view_all_requests)) {
            if (opportunity.getRequester() == null || !opportunity.getRequester().getEmail().equals(authentication.getName())) {
                throw new RuntimeException("Acesso negado a esta requisição");
            }
        }

        return convertToDTO(opportunity, true);
    }

    private boolean hasPermission(Authentication authentication, Permission permission) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(permission.name()) || a.equals("ROLE_ADMIN"));
    }

    private <T, R> R mapName(T entity, Function<T, R> mapper) {
        return Optional.ofNullable(entity).map(mapper).orElse(null);
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
        String email = authentication.getName();
        People recruiter = peopleRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recrutador não encontrado"));

        List<Opportunity> opportunities = opportunityRepository.findByOpportunityStatusNameAndResponsibleRecruiterEmail("Aprovada", email);

        return opportunities.stream()
                .map(opp -> {
                    List<RecruitmentProcess> processes = recruitmentProcessRepository.findByOpportunityId(opp.getId());
                    
                    List<OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO> processDTOs = processes.stream()
                            .map(rp -> OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO.builder()
                                    .id(rp.getId())
                                    .processStageName(mapName(rp.getProcessStage(), DhoProcessStage::getName))
                                    .processStatusName(mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                                    .candidate(OpportunityPipelineResponseDTO.CandidatePipelineDTO.builder()
                                            .id(rp.getCandidate().getId())
                                            .name(rp.getCandidate().getName())
                                            .email(rp.getCandidate().getEmail())
                                            .phoneNumber(rp.getCandidate().getPhoneNumber())
                                            .profileImage(rp.getCandidate().getProfileImage())
                                            .build())
                                    .build())
                            .collect(Collectors.toList());

                    OpportunityPipelineResponseDTO.OpportunityPipelineDataDTO opportunityData = OpportunityPipelineResponseDTO.OpportunityPipelineDataDTO.builder()
                            .id(opp.getId())
                            .positionName(mapName(opp.getPosition(), DhoPosition::getName))
                            .teamName(mapName(opp.getTeam(), DhoTeam::getName))
                            .departmentName(mapName(opp.getDepartment(), DhoDepartment::getName))
                            .processes(processDTOs)
                            .build();

                    return OpportunityPipelineResponseDTO.builder()
                            .opportunity(opportunityData)
                            .build();
                })
                .collect(Collectors.toList());
    }


    public OpportunityPipelineResponseDTO getOpportunityPipeline(Integer id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        List<RecruitmentProcess> processes = recruitmentProcessRepository.findByOpportunityId(id);

        List<OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO> processDTOs = processes.stream()
                .map(rp -> OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO.builder()
                        .id(rp.getId())
                        .processStageName(mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatusName(mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                        .candidate(OpportunityPipelineResponseDTO.CandidatePipelineDTO.builder()
                                .id(rp.getCandidate().getId())
                                .name(rp.getCandidate().getName())
                                .email(rp.getCandidate().getEmail())
                                .phoneNumber(rp.getCandidate().getPhoneNumber())
                                .profileImage(rp.getCandidate().getProfileImage())
                                .build())
                        .build())
                .collect(Collectors.toList());

        OpportunityPipelineResponseDTO.OpportunityPipelineDataDTO opportunityData = OpportunityPipelineResponseDTO.OpportunityPipelineDataDTO.builder()
                .id(opportunity.getId())
                .positionName(mapName(opportunity.getPosition(), DhoPosition::getName))
                .teamName(mapName(opportunity.getTeam(), DhoTeam::getName))
                .departmentName(mapName(opportunity.getDepartment(), DhoDepartment::getName))
                .processes(processDTOs)
                .build();

        return OpportunityPipelineResponseDTO.builder()
                .opportunity(opportunityData)
                .build();
    }
}
