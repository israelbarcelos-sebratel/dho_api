# Especificação de Funcionalidade: Fluxo de Recrutamento Completo

Esta especificação descreve o fluxo de recrutamento de um candidato, desde a criação da oportunidade até a finalização do processo após a aceitação da proposta, utilizando o padrão de estados (State Pattern) para garantir a integridade do processo.

## Estados do Processo

### Estágios (`DhoProcessStage`)
- **Banco de Talentos**: Estágio inicial quando o candidato é vinculado a uma oportunidade aprovada.
- **Triagem**: Quando o recrutador inicia a avaliação do candidato.
- **Entrevista**: Fase de entrevistas com o candidato.
- **Teste Técnico**: Fase de avaliação técnica.
- **Decisão Final**: Fase de decisão final do gestor.
- **Aprovado**: Estágio após aprovação do gestor e antes do envio de documentos.
- **Aguardando documentos**: Fase de coleta de documentação após aceite prévio/envio de proposta.
- **Onboarding**: Fase final de integração antes da conclusão do processo.

### Status (`DhoProcessStatus`)
- **Em andamento**: Status padrão durante as fases de avaliação.
- **Aguardando aprovação**: Definido quando o candidato é movido para a etapa de Decisão Final.
- **Aprovado**: Definido após a aprovação do gestor.
- **Reprovado**: Definido caso o gestor não aprove o candidato.
- **Enviada Proposta**: Definido após a recrutadora realizar a proposta formal.
- **Finalizado**: Definido após o candidato aceitar a proposta no final do Onboarding.
- **Recusada pelo candidato**: Definido caso o candidato decline a proposta.

## Atores
- **Gestor**: Inicia a oportunidade e aprova/reprova o candidato na Decisão Final.
- **Gerente de R&S**: Aceita a proposta de vaga e vincula a uma recrutadora.
- **Recrutadora**: Responsável por vincular candidatos, mover entre estágios e conduzir o processo operacional.

## Fluxo Principal (Sequência Obrigatória)

| Passo | Ação | Endpoint | Regra de Transição (State Pattern) |
| :--- | :--- | :--- | :--- |
| 1 | **Criar Oportunidade** | `POST /api/opportunities` | Gestor inicia o processo |
| 2 | **Aceitar Proposta Vaga** | `POST /api/opportunities/{id}/approve` | Gerente de R&S aprova a vaga |
| 3 | **Vincular Recrutadora** | `POST /api/opportunities/{id}/assign-recruiter` | Gerente de R&S define a responsável |
| 4 | **Vincular Candidato** | `POST /api/recruitment-processes` | Inicia em **Banco de Talentos** |
| 5 | **Mover para Triagem** | `POST .../{id}/move-to-screening` | Origem: **Banco de Talentos** |
| 6 | **Mover para Entrevista** | `POST .../{id}/move-to-interview` | Origem: **Triagem** |
| 7 | **Mover para Teste Técnico** | `POST .../{id}/move-to-technical-test` | Origem: **Entrevista** |
| 8 | **Mover para Decisão Final** | `POST .../{id}/move-to-final-decision` | Origem: **Teste Técnico** |
| 9 | **Decisão do Gestor** | `POST .../{id}/manager-decision` | Origem: **Decisão Final** |
| 10 | **Enviar Proposta** | `POST .../{id}/proposal` | Status deve ser **Aprovado** |
| 11 | **Aguardando Documentos** | `POST .../{id}/move-to-awaiting-documents` | Status deve ser **Enviada Proposta** |
| 12 | **Onboarding** | `POST .../{id}/move-to-onboarding` | Origem: **Aguardando documentos** |
| 13 | **Finalizar Processo** | `POST .../{id}/candidate-decision` | Origem: **Onboarding** |

## Exemplos de Uso (CURL)

### Mover para Onboarding
```bash
curl -X POST http://localhost:8090/api/recruitment-processes/{id}/move-to-onboarding \
-H "Authorization: Bearer {TOKEN}"
```

### Decisão do Gestor (Aprovação)
```bash
curl -X POST http://localhost:8090/api/recruitment-processes/{id}/manager-decision \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {TOKEN}" \
-d '{
  "approved": true,
  "reason": "Candidato com excelente perfil técnico e alinhamento cultural demonstrado durante as etapas anteriores, superando as expectativas para a posição..."
}'
```

## Regras de Negócio e Segurança

- **State Pattern**: O sistema utiliza uma máquina de estados inteligente. Cada etapa conhece apenas a sua próxima etapa válida. Qualquer tentativa de "pular" processos resultará em erro `400 Bad Request`.
- **Validação de Motivos**: Endpoints como `manager-decision` e `candidate-decision` exigem um campo `reason` com no mínimo 200 caracteres.
- **Oportunidade Aprovada**: Nenhuma ação no processo de recrutamento é permitida se a oportunidade vinculada não estiver com o status **Aprovada**.
- **Permissões**:
    - `approve_candidate`: Necessária para movimentações entre estágios.
    - `reject_candidate`: Necessária para a decisão do gestor.
    - `initiate_contract_process`: Necessária para envio de propostas e finalização.
