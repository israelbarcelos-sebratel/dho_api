package br.com.sebratel.bff.dho.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    SUGGESTIONS("Usuário pode ler, criar e votar em sugestões"),
    DEFAULT("Permissão padrão"),
    INITIATE_CONTRACT_PROCESS("Usuário pode ler, criar e votar em sugestões"),
    APPROVE_CONTRACT_PROCESS("Permite aprovar ou reprovar requisições de pessoal pendentes"),
    ADD_CANDIDATE("Usuário pode ler, criar e votar em sugestões"),
    APPROVE_CANDIDATE("Usuário pode ler, criar e votar em sugestões"),
    WITHDRAW_CANDIDATE("Usuário pode ler, criar e votar em sugestões"),
    VIEW_INDICATORS("Permite o acesso à página de Indicadores e Dashboards de desempenho"),
    VIEW_ALL_REQUESTS("Permite visualizar todas as requisições do sistema, não apenas as próprias"),
    ASSIGN_RECRUITER("Permite atribuir uma recrutadora responsável por uma vaga específica"),
    VIEW_JOB_TRACKING("Permite o acesso às páginas de Acompanhamento de Vagas e Detalhes da Admissão"),
    VIEW_PIPELINE("Permite o acesso à visualização do pipeline/funil de candidatos"),
    REJECT_CANDIDATE("Permite reprovar candidatos em qualquer etapa do processo seletivo"),
    FINAL_DECISION("Permite o acesso à página de Decisão Final e aprovação final de contratação"),
    MANAGE_SETTINGS("Permite o acesso e alteração das configurações globais do sistema");

    private final String description;
}
