package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("TriagemState")
public class ScreeningState extends AbstractProcessState {
    @Override
    public void moveToInterview(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Triagem";
    }
}
