package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class AbstractProcessState implements ProcessState {

    protected void throwInvalidTransition(String targetAction) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            String.format("Ação '%s' não permitida no estágio atual: %s", targetAction, getStageName()));
    }

    @Override public void moveToScreening(RecruitmentProcess process) { throwInvalidTransition("Mover para Triagem"); }
    @Override public void moveToInterview(RecruitmentProcess process) { throwInvalidTransition("Mover para Entrevista"); }
    @Override public void moveToTechnicalTest(RecruitmentProcess process, String reason) { throwInvalidTransition("Mover para Teste Técnico"); }
    @Override public void moveToFinalDecision(RecruitmentProcess process) { throwInvalidTransition("Mover para Decisão Final"); }
    @Override public void managerDecision(RecruitmentProcess process, boolean approved) { throwInvalidTransition("Decisão do Gestor"); }
    @Override public void sendProposal(RecruitmentProcess process) { throwInvalidTransition("Enviar Proposta"); }
    @Override public void moveToAwaitingDocuments(RecruitmentProcess process) { throwInvalidTransition("Mover para Aguardando Documentos"); }
    @Override public void moveToOnboarding(RecruitmentProcess process) { throwInvalidTransition("Mover para Onboarding"); }
    @Override public void candidateDecision(RecruitmentProcess process, boolean accepted) { throwInvalidTransition("Decisão do Candidato"); }
}
