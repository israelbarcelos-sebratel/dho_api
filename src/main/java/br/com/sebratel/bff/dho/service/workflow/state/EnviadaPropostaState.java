package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("Enviada PropostaState")
public class EnviadaPropostaState extends AbstractProcessState {
    @Override
    public void moveToAwaitingDocuments(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public void candidateDecision(RecruitmentProcess process, boolean accepted) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Enviada Proposta";
    }
}
