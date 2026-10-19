package br.com.sebratel.bff.dho.service.workflow.state;

import br.com.sebratel.bff.dho.domain.entity.RecruitmentProcess;
import org.springframework.stereotype.Component;

@Component("Aguardando documentosState")
public class AwaitingDocumentsState extends AbstractProcessState {
    @Override
    public void moveToOnboarding(RecruitmentProcess process) {
        // Transição permitida
    }

    @Override
    public String getStageName() {
        return "Aguardando documentos";
    }
}
