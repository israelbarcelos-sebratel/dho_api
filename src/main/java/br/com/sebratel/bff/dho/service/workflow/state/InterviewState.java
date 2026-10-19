package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("EntrevistaState")
public class InterviewState extends AbstractProcessState {
    @Override
    public void moveToTechnicalTest(RecruitmentProcess process, String reason) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Entrevista";
    }
}
