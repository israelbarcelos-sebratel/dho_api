package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("OnboardingState")
public class OnboardingState extends AbstractProcessState {
    @Override
    public void candidateDecision(RecruitmentProcess process, boolean accepted) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Onboarding";
    }
}
