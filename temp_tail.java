    @GetMapping("/{id}/stages")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Recuperar estágios do processo", description = "Retorna todos os estágios do processo de seleção com seus respectivos status para um candidato específico.")
    public ResponseEntity<List<RecruitmentProcessStageResponseDTO>> getProcessStages(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getProcessStages(id));
    }

    @PostMapping(value = "/{id}/additional-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).add_candidate.name())")
    @Operation(summary = "Adicionar documentos e comentários", description = "Permite adicionar informações complementares ao processo de recrutamento.")
    @GetMapping("/{id}/additional-info")
    @PreAuthorize("hasAuthority(T(br.com.sebratel.bff.dho.domain.enums.Permission).view_job_tracking.name())")
    @Operation(summary = "Obter informações adicionais do processo", description = "Retorna a lista de comentários e documentos anexados ao processo.")
    public ResponseEntity<List<br.com.sebratel.bff.dho.dto.RecruitmentProcessItemResponseDTO>> getAdditionalInfo(@PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id) {
        return ResponseEntity.ok(recruitmentProcessService.getAdditionalInfo(id));
    }

    public ResponseEntity<Void> addAdditionalInfo(
            @PathVariable @Parameter(description = "ID do processo de recrutamento") Integer id,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws IOException {
        recruitmentProcessService.addAdditionalInfo(id, comment, files);
        return ResponseEntity.ok().build();
    }
}
