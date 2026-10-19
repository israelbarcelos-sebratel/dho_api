package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("Teste TécnicoState")
public class TechnicalTestState extends AbstractProcessState {
    @Override
    public void moveToFinalDecision(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Teste Técnico";
    }
}
