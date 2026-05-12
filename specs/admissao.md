# Feature Name: [Admissão]

## 1. Contexto e Objetivo
O gestor deve poder iniciar o pedido de uma nova vaga para o DHO, o DHO deve validar e iniciar o processo de recrutamento e seleção.

---

## 2. Requisitos de Negócio & Regras (Backlog)
### Gestor
- [ ] **RQ01:** Criar uma nova vaga.
- [x] **RQ02:** Ver o andamento das vagas.
- [ ] **RQ03:** Ver detalhes do processo de admissão.
- [ ] **RQ04:** Fluxo de Decisão Final (Aprovar/Recusar candidatos).
- [ ] **RQ22:** Limite de contratações por vaga.
- [ ] **RQ21:** Ver o perfil do usuário.

### Gerente R&S (DHO)
- [x] **RQ05:** Ver todas as vagas existentes.
- [ ] **RQ06:** Aprovar ou Recusar vaga inicial.
- [ ] **RQ07:** Vincular/Trocar recrutadora responsável.
- [ ] **RQ08:** Finalizar vaga a qualquer momento.
- [ ] **RQ09:** Histórico de todos os processos finalizados.

### Recrutadora
- [ ] **RQ10:** Listar processos vinculados.
- [ ] **RQ11:** Visualização de candidatos vinculados à vaga.
- [ ] **RQ12:** Pipeline de recrutamento.
- [ ] **RQ13:** Log de Eventos.
- [ ] **RQ14:** Decisão em entrevista.
- [ ] **RQ15:** Acompanhamento de processos pós-aprovação.
- [ ] **RQ16:** Upload de documentos em massa.
- [x] **RQ17:** Finalizar contratação ou marcar recusa.
- [ ] **RQ23:** Dashboard de Indicadores de Recrutamento (Vagas abertas, contratações no mês, aprovações pendentes e tempo médio de contratação).


## 3. Especificações Técnicas

### 3.1. Filtro de Requisições
O endpoint de listagem de requisições (`POST /requisitions`) permite filtrar a visibilidade dos dados:

- **Campo**: `showAllRequisitions` (Boolean)
- **Comportamento**:
    - Se `false` (padrão): Retorna apenas as requisições criadas pelo usuário autenticado.
    - Se `true`: Retorna todas as requisições do sistema, **desde que** o usuário possua a permissão `view_all_requests` ou role `ADMIN`.
    - Se o usuário não possuir a permissão necessária e enviar `true`, o sistema retornará apenas as suas próprias requisições como medida de segurança.

### 3.2. Candidatos da Requisição
O endpoint `GET /requisitions/{id}/candidates` permite que o gestor visualize os candidatos vinculados a uma de suas vagas aprovadas.

- **Regras de Acesso**:
    - O usuário deve ser o solicitante (`requester`) da vaga OU possuir permissão `view_all_requests`/`ROLE_ADMIN`.
    - A vaga deve estar com o status "Aprovada".
- **Retorno**: Lista de `CandidateResponseDTO`.
