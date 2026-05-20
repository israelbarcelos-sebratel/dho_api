package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("AprovadoState")
public class ApprovedState extends AbstractProcessState {
    @Override
    public void sendProposal(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Aprovado";
    }
}
