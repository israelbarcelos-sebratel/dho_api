# Especificação de Funcionalidade: Fluxo de Recrutamento Completo

Esta especificação descreve o fluxo de recrutamento de um candidato, desde a criação da oportunidade até a finalização do processo após a aceitação da proposta.

## Estados do Processo

### Estágios (`DhoProcessStage`)
- **Banco de Talentos**: Estágio inicial quando o candidato é vinculado a uma oportunidade aprovada.
- **Triagem**: Quando o recrutador decide iniciar a avaliação do candidato.
- **Entrevista**: Quando o candidato avança para a fase de entrevistas.
- **Teste Técnico**: Quando o candidato avança para a fase de avaliação técnica.
- **Decisão Final**: Quando o candidato está apto para a decisão final do gestor.

### Status (`DhoProcessStatus`)
- **Em andamento**: Status padrão durante as fases de avaliação.
- **Aguardando aprovação**: Definido quando o candidato é movido para a etapa de Decisão Final.
- **Aprovado pelo Gestor**: Definido após a aprovação do gestor.
- **Recusado pelo gestor**: Definido caso o gestor não aprove o candidato.
- **Enviada Proposta**: Definido após a recrutadora realizar a proposta formal.
- **Finalizado**: Definido após o candidato aceitar a proposta.
- **Recusada pelo candidato**: Definido caso o candidato decline a proposta.

## Atores
- **Recrutadora**: Responsável por criar a oportunidade, vincular candidatos, mover entre estágios e fazer a proposta.
- **Gestor**: Responsável por aprovar ou recusar o candidato após as avaliações técnicas/entrevistas.
- **Candidato**: Responsável por aceitar ou recusar a proposta.

## Fluxo Principal (Happy Path)
1. **Criação da Oportunidade**: A recrutadora ou gestor cria uma nova vaga através do endpoint `POST /api/opportunities`.
2. **Aprovação da Oportunidade**: A oportunidade deve ser aprovada (`POST /api/opportunities/{id}/approve`) antes de qualquer vínculo.
3. **Vínculo**: O candidato é atrelado à oportunidade aprovada através de `POST /api/recruitment-processes`. O sistema define automaticamente o estágio como **Banco de Talentos**.
4. **Avanço para Triagem**: A recrutadora move o candidato do Banco de Talentos para **Triagem** (`POST /api/recruitment-processes/{id}/move-to-screening`).
5. **Avanço para Entrevista**: A recrutadora move o candidato para o estágio de **Entrevista** (`POST /api/recruitment-processes/{id}/move-to-interview`).
6. **Avanço para Teste Técnico**: A recrutadora move o candidato para o estágio de **Teste Técnico** (`POST /api/recruitment-processes/{id}/move-to-technical-test`).
    - **Nota**: Este endpoint requer um corpo JSON: `{"reason": "Parecer com no mínimo 10 caracteres"}`.
7. **Avanço para Decisão Final**: A recrutadora move o candidato para o estágio de **Decisão Final** (`POST /api/recruitment-processes/{id}/move-to-final-decision`).
8. **Decisão do Gestor**: O gestor revisa o processo e aprova o candidato (`POST /api/recruitment-processes/{id}/manager-decision`). O status muda para **Aprovado pelo Gestor**.
9. **Proposta**: A recrutadora envia a proposta (`POST /api/recruitment-processes/{id}/proposal`). O status muda para **Enviada Proposta**.
10. **Decisão do Candidato**: O candidato aceita a proposta (`POST /api/recruitment-processes/{id}/candidate-decision`). O status muda para **Finalizado**.

## Exemplos de Uso (CURL)

### Mover para Teste Técnico
```bash
curl -X POST http://localhost:8090/api/recruitment-processes/{id}/move-to-technical-test \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {TOKEN}" \
-d '{"reason": "Parecer técnico com mais de dez caracteres"}'
```

### Mover para Decisão Final
```bash
curl -X POST http://localhost:8090/api/recruitment-processes/{id}/move-to-final-decision \
-H "Authorization: Bearer {TOKEN}"
```

## Regras de Negócio
- **Vínculo de Candidato**: Só é permitido vincular um candidato se a oportunidade estiver com status **Aprovada**.
- **Deadline SLA**: Ao aprovar uma oportunidade, o SLA é definido automaticamente para 30 dias.
- **Base de Origem**: Se não informada, a base padrão é "Porto Alegre".
- **Movimentação**: 
    - Toda ação em um processo (etapa ou status) exige que a oportunidade vinculada esteja **Aprovada**.
    - Se o gestor recusar, o status deve ser **Recusado pelo gestor** e o processo é interrompido.
    - Se o candidato recusar a proposta, o status deve refletir a recusa e o processo é interrompido.
- **Robustez de Comparação**:
    - Todas as validações de estágio e status no backend são realizadas de forma **insensível a maiúsculas/minúsculas** (case-insensitive) e ignorando espaços extras (trim). Isso garante que variações no banco de dados (ex: "Entrevista", "ENTREVISTA" ou "Entrevista ") não interrompam o fluxo do sistema.


## Regras de Segurança e Resistência
- **Restrição de Papéis**:
    - Apenas usuários com permissão `approve_candidate` podem mover candidatos entre estágios (`Triagem`, `Entrevista`, `Teste Técnico`).
    - Apenas usuários com permissão `reject_candidate` podem registrar a decisão do gestor.
    - Apenas usuários com permissão `initiate_contract_process` podem enviar propostas e finalizar o processo.
- **Validação de Estado**:
    - Não é permitido pular estágios obrigatórios.
    - O processo deve estar no status correto para aceitar uma decisão do candidato.
