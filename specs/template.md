# Feature Name: [Autenticação OIDC (Google)]

## 1. Contexto e Objetivo
O objetivo desta feature é permitir que o usuário se conecte à aplicação React via OIDC (Google). A API deve expor um endpoint para identificar o usuário logado, buscar suas informações no banco de dados e realizar o auto-provisionamento caso o usuário ainda não exista.

---

## 2. Requisitos de Negócio & Regras (Backlog)

### Autenticação & Identidade
- [ ] **RQ23:** O usuário deve se conectar na aplicação React via OIDC utilizando o Google.
- [ ] **RQ24:** A API deve expor a rota `/auth/me` para retornar os dados do usuário autenticado.
- [x] **RQ25:** A busca do usuário na tabela `people` deve ser feita através do **email** enviado pelo frontend.
- [ ] **RQ26:** O retorno da rota `/auth/me` deve conter a **role** e as **permissões** do usuário.
- [x] **RQ27:** Caso o usuário não tenha registro na tabela `people`, a API deve criar um novo registro com as informações vindas do frontend.
- [ ] **RQ28:** Novos cadastros realizados via auto-provisionamento devem receber uma **role default**.

### Credenciais (Frontend Reference)
- `VITE_GOOGLE_CLIENT_ID=<REDACTED>`
- `VITE_GOOGLE_CLIENT_SECRET=<REDACTED>`

---

## 3. Notas de Implementação (Técnico)
- [x] Adicionar campo `email` na tabela `people` (conforme identificado na análise do schema atual).
- Garantir que a entidade `DhoRole` suporte a listagem de permissões ou campos associados.
- O fluxo de auto-provisionamento deve garantir a integridade dos dados obrigatórios da tabela `people`.

\n--- \n\n## 4. Regras de Desenvolvimento (Importante)\n- [ ] **Sempre** buildar a aplicação (`./mvnw clean compile`) antes de realizar qualquer commit para garantir que o código está íntegro.\n- [ ] Validar mudanças em classes de configuração de segurança com atenção redobrada à sintaxe e ordem das anotações.\n- [ ] Sempre verificar logs de erro de build (`target/classes` ou logs do Maven) antes de reportar falhas.\n