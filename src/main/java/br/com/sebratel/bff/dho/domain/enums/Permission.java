package br.com.sebratel.bff.dho.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    // Recruitment Process - Gestor
    RQ01("Criar uma nova vaga"),
    RQ02("Ver o andamento das vagas"),
    RQ03("Ver detalhes do processo de admissão"),
    RQ04("Fluxo de Decisão Final (Aprovar/Recusar candidatos)"),
    RQ21("Ver o perfil do usuário"),
    RQ22("Limite de contratações por vaga"),

    // Recruitment Process - Gerente R&S (DHO)
    RQ05("Ver todas as vagas existentes"),
    RQ06("Aprovar ou Recusar vaga inicial"),
    RQ07("Vincular/Trocar recrutadora responsável"),
    RQ08("Finalizar vaga a qualquer momento"),
    RQ09("Histórico de todos os processos finalizados / Dashboard de Indicadores"),

    // Recruitment Process - Recrutadora
    RQ10("Listar processos vinculados"),
    RQ11("Visualização de candidatos vinculados à vaga"),
    RQ12("Pipeline de recrutamento"),
    RQ13("Log de Eventos"),
    RQ14("Decisão em entrevista"),
    RQ15("Acompanhamento de processos pós-aprovação"),
    RQ16("Upload de documentos em massa"),
    RQ17("Finalizar contratação ou marcar recusa"),
    RQ23("Dashboard de Indicadores de Recrutamento (Vagas abertas, contratações no mês, aprovações pendentes e tempo médio de contratação)"),

    // Suggestions
    RQ18("Criar uma nova sugestão"),
    RQ19("Listagem de sugestões"),
    RQ20("Sistema de votação"),
    SUGGESTIONS("Acesso geral ao sistema de sugestões"),

    // Authentication & Identity
    RQ24("Acessar dados do usuário autenticado (/auth/me)"),
    RQ25("Busca de usuário por email"),
    RQ26("Retorno de role e permissões no /auth/me"),
    RQ27("Auto-provisionamento de usuário"),
    RQ28("Atribuição de role default"),
    DEFAULT("Permissão padrão");

    private final String description;
}
