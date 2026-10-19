package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;

public interface ProcessState {
    void moveToScreening(RecruitmentProcess process);
    void moveToInterview(RecruitmentProcess process);
    void moveToTechnicalTest(RecruitmentProcess process, String reason);
    void moveToFinalDecision(RecruitmentProcess process);
    void managerDecision(RecruitmentProcess process, boolean approved);
    void sendProposal(RecruitmentProcess process);
    void moveToAwaitingDocuments(RecruitmentProcess process);
    void moveToOnboarding(RecruitmentProcess process);
    void candidateDecision(RecruitmentProcess process, boolean accepted);
    
    String getStageName();
}
