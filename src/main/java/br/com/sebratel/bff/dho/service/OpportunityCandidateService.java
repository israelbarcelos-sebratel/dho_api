package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.enums.Permission;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.CandidateResponseDTO;
import br.com.sebratel.bff.dho.mapper.OpportunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpportunityCandidateService {

    private final OpportunityRepository opportunityRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final OpportunityMapper opportunityMapper;

    public List<CandidateResponseDTO> findCandidatesForUser(Integer id, Authentication authentication) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        if (!"Aprovada".equals(opportunity.getOpportunityStatus().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A requisição deve estar aprovada para ver os candidatos");
        }

        boolean isAdmin = opportunityMapper.hasPermission(authentication, Permission.view_all_requests);
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
                        .processStage(opportunityMapper.mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatus(opportunityMapper.mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
                        .build())
                .collect(Collectors.toList());
    }
}
