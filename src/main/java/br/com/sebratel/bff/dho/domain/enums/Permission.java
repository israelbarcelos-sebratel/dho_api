package br.com.sebratel.bff.dho.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Permission {
    suggestions("Usuário pode ler, criar e votar em sugestões"),
    DEFAULT("Permissão padrão"),
    initiate_contract_process("Usuário pode ler, criar e votar em sugestões"),
    approve_contract_proccess("Erro de digitação no banco (proccess)"),
    approve_contract_process("Permite aprovar ou reprovar requisições de pessoal pendentes"),
    add_candidate("Usuário pode ler, criar e votar em sugestões"),
    approve_candidate("Usuário pode ler, criar e votar em sugestões"),
    withdraw_candidate("Usuário pode ler, criar e votar em sugestões"),
    view_indicators("Permite o acesso à página de Indicadores e Dashboards de desempenho"),
    view_all_requests("Permite visualizar todas as requisições do sistema, não apenas as próprias"),
    assign_recruiter("Permite atribuir uma recrutadora responsável por uma vaga específica"),
    view_job_tracking("Permite o acesso às páginas de Acompanhamento de Vagas e Detalhes da Admissão"),
    view_pipeline("Permite o acesso à visualização do pipeline/funil de candidatos"),
    reject_candidate("Permite reprovar candidatos em qualquer etapa do processo seletivo"),
    final_decision("Permite o acesso à página de Decisão Final e aprovação final de contratação"),
    manage_settings("Permite o acesso e alteração das configurações globais do sistema"),
    list_linked_processes("Listar processos vinculados (RQ10)"),
    event_log("Log de Eventos (RQ13)");

    private final String description;

    @JsonValue
    public String getName() {
        return name();
    }

    @JsonCreator
    public static Permission fromString(String value) {
        return Stream.of(Permission.values())
                .filter(p -> p.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + value));
    }
}
