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
