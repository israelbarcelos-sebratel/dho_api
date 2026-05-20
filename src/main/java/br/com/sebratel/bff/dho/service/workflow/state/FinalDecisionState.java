package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("Aguardando aprovaçãoState")
public class FinalDecisionState extends AbstractProcessState {
    @Override
    public void managerDecision(RecruitmentProcess process, boolean approved) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Aguardando aprovação";
    }
}
