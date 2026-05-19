package br.com.sebratel.bff.dho.service;

import br.com.sebratel.bff.dho.domain.entity.Opportunity;
import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoDepartment;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoPosition;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStage;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoProcessStatus;
import br.com.sebratel.bff.dho.domain.entity.auxiliary.DhoTeam;
import br.com.sebratel.bff.dho.domain.repository.OpportunityRepository;
import br.com.sebratel.bff.dho.domain.repository.RecruitmentProcessRepository;
import br.com.sebratel.bff.dho.dto.OpportunityPipelineResponseDTO;
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
public class OpportunityPipelineService {

    private final OpportunityRepository opportunityRepository;
    private final RecruitmentProcessRepository recruitmentProcessRepository;
    private final OpportunityMapper opportunityMapper;

    public List<OpportunityPipelineResponseDTO> getOpportunitiesPipelineForRecruiter(Authentication authentication) {
        String email = authentication.getName();
        List<Opportunity> opportunities = opportunityRepository.findByOpportunityStatusNameAndResponsibleRecruiterEmail("Aprovada", email);

        return opportunities.stream()
                .map(this::buildPipelineResponse)
                .collect(Collectors.toList());
    }

    public OpportunityPipelineResponseDTO getOpportunityPipeline(Integer id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada"));

        return buildPipelineResponse(opportunity);
    }

    private OpportunityPipelineResponseDTO buildPipelineResponse(Opportunity opp) {
        List<RecruitmentProcess> processes = recruitmentProcessRepository.findByOpportunityId(opp.getId());
        
        List<OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO> processDTOs = processes.stream()
                .filter(rp -> {
                    String status = opportunityMapper.mapName(rp.getProcessStatus(), DhoProcessStatus::getName);
                    return status != null && !List.of("Cancelado", "Recusado", "Finalizado", "Contratado", "Reprovado", "Desistência").contains(status);
                })
                .map(rp -> OpportunityPipelineResponseDTO.RecruitmentProcessPipelineDTO.builder()
                        .id(rp.getId())
                        .processStageName(opportunityMapper.mapName(rp.getProcessStage(), DhoProcessStage::getName))
                        .processStatusName(opportunityMapper.mapName(rp.getProcessStatus(), DhoProcessStatus::getName))
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
                .positionName(opportunityMapper.mapName(opp.getPosition(), DhoPosition::getName))
                .teamName(opportunityMapper.mapName(opp.getTeam(), DhoTeam::getName))
                .departmentName(opportunityMapper.mapName(opp.getDepartment(), DhoDepartment::getName))
                .processes(processDTOs)
                .build();

        return OpportunityPipelineResponseDTO.builder()
                .opportunity(opportunityData)
                .build();
    }
}
