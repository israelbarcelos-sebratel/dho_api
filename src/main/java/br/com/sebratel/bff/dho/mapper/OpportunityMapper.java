package br.com.sebratel.bff.dho.mapper;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.People;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.*;
import br.com.sebratel.bff.dho.domain.enums.Permission;
import br.com.sebratel.bff.dho.dto.OpportunityResponseDTO;
import br.com.sebratel.bff.dho.dto.RecruitmentProcessResponseDTO;
import br.com.sebratel.bff.dho.dto.UserResponseDTO;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.service.RecruitmentProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpportunityMapper {

    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final RecruitmentProcessService recruitmentProcessService;

    public OpportunityResponseDTO convertToDTO(Opportunity opportunity, boolean includeCandidates) {
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

    public <T, R> R mapName(T entity, Function<T, R> mapper) {
        return Optional.ofNullable(entity).map(mapper).orElse(null);
    }

    public boolean hasPermission(Authentication authentication, Permission permission) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(permission.name()) || a.equals("ROLE_ADMIN"));
    }
}
