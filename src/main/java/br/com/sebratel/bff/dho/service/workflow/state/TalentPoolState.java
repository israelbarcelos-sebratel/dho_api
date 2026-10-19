package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("Banco de TalentosState")
public class TalentPoolState extends AbstractProcessState {
    @Override
    public void moveToScreening(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Banco de Talentos";
    }
}
