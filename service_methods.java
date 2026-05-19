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
