# Documentação do Banco de Dados - DHO_Application

Este documento descreve a estrutura de tabelas e os relacionamentos do banco de dados `DHO_Application` (MariaDB), conforme implementado nas entidades do sistema.

## 1. Tabelas Principais

### 1.1. `people`
Centraliza as informações de todos os indivíduos no sistema (candidatos, colaboradores, gestores e recrutadores).

| Campo | Tipo (JPA/SQL) | Descrição |
|-------|----------------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `profile_image` | String | Caminho ou link da imagem de perfil. |
| `registration_number` | Integer | Número de matrícula/registro. |
| `name` | String | Nome completo. |
 | `email` | String | Email do usuário. |
| `phone_number` | String | Telefone de contato. |
| `cpf` | String | Cadastro de Pessoa Física. |
| `rg` | String | Registro Geral. |
| `date_birth` | LocalDateTime | Data de nascimento. |
| `sex` | String | Gênero/Sexo. |
| `replacement` | Boolean | Indica se é uma substituição. |
| `recruitment_data` | LocalDateTime | Data de coleta de dados de recrutamento. |
| `admission_date` | LocalDateTime | Data de admissão. |
| `observations` | String | Observações gerais. |
| `professional_references`| String | Referências profissionais. |
| `collaborator_knowledge` | String | Conhecimentos do colaborador. |
| `labor_lawsuit` | String | Informações sobre processos trabalhistas. |
| `criminal_background` | Boolean | Status de antecedentes criminais. |
| `external_link` | String | Link externo (ex: LinkedIn). |
| `mindsight_link` | String | Link para avaliação Mindsight. |
| `cis_link` | String | Link para avaliação CIS. |
| `id_resignation_motivation`| Integer (FK) | Referência a `resignation_motivation`. |
| `id_resignation_type` | Integer (FK) | Referência a `resignation_type`. |
| `id_education` | Integer (FK) | Referência a `education`. |
| `id_situation` | Integer (FK) | Referência a `situation`. |
| `id_recruitment_source` | Integer (FK) | Referência a `recruitment_source`. |

### 1.2. `roles`
Define os papéis de acesso no sistema (ex: ADMIN, GESTOR, COLABORADOR).

| Campo | Tipo | Descrição |
|-------|------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `name` | String | Nome do papel. |
| `description` | String | Descrição das responsabilidades. |

### 1.3. `permissions`
Define permissões granulares para funcionalidades do sistema.

| Campo | Tipo | Descrição |
|-------|------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `name` | String | Nome da permissão (ex: sugerir_ideia). |
| `description` | String | O que a permissão permite fazer. |

### 1.4. `people_roles` (Tabela de Ligação)
Relaciona pessoas a papéis (roles) - Muitos-para-Muitos.

| Campo | Tipo | Descrição |
|-------|------|-------------|
| `people_id` | Integer (PK, FK)| Link com `people`. |
| `role_id` | Integer (PK, FK)| Link com `roles`. |

### 1.5. `role_permissions` (Tabela de Ligação)
Relaciona papéis a permissões.

| Campo | Tipo | Descrição |
|-------|------|-------------|
| `role_id` | Integer (PK, FK)| Link com `roles`. |
| `permission_id` | Integer (PK, FK)| Link com `permissions`. |


### 1.6. `role_requests`
Gerencia solicitações de papéis feitas pelos usuários.

| Campo | Tipo | Descrição |
|-------|------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `people_id` | Integer (FK) | Usuário que solicitou. |
| `role_id` | Integer (FK) | Papel solicitado. |
| `status` | String | Status (PENDING, APPROVED, REJECTED). |
| `request_date` | LocalDateTime| Data da solicitação. |
| `resolution_date`| LocalDateTime| Data de aprovação/rejeição. |


### 1.7. `opportunities`
Gerencia as vagas e o ciclo de vida da abertura de oportunidades.

| Campo | Tipo (JPA/SQL) | Descrição |
|-------|----------------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `open_opportunity_date` | LocalDateTime | Data de abertura da vaga. |
| `people_id` | Integer (FK) | Candidato (link com `people`). |
| `position_id` | Integer (FK) | Cargo (link com `position`). |
| `team_id` | Integer (FK) | Equipe (link com `team`). |
| `departament_id` | Integer (FK) | Departamento (link com `departament`). |
| `opportunity_motive_id` | Integer (FK) | Motivo da abertura (link com `opportunity_motive`). |
| `replaced_person_id` | Integer (FK) | Pessoa substituída (link com `people`). |
| `base_origin_id` | Integer (FK) | Origem da base (link com `base_origin`). |
| `opportunity_status_id` | Integer (FK) | Status da oportunidade (link com `opportunity_status`). |
| `process_stage_id` | Integer (FK) | Estágio atual do processo (link com `process_stage`). |
| `process_status_id` | Integer (FK) | Status atual do processo (link com `process_status`). |
| `deadline_sla_days` | Integer | Prazo de SLA em dias. |
| `accept_date` | LocalDateTime | Data de aceite da proposta. |
| `responsible_recruiter_id`| Integer (FK) | Recrutador responsável (link com `people`). |
| `observations` | String | Observações da oportunidade. |
| `refusal_justification` | String (1000) | Justificativa em caso de recusa. |

### 1.8. `recruitment_process`
Acompanha a jornada específica de um candidato dentro de uma oportunidade.

| Campo | Tipo (JPA/SQL) | Descrição |
|-------|----------------|-------------|
| `id` | Integer (PK) | Identificador único. |
| `people_id` | Integer (FK) | Referência ao candidato (`people`). |
| `opportunity_id` | Integer (FK) | Referência à oportunidade (`opportunities`). |
| `process_status_id` | Integer (FK) | Referência ao status do processo. |
| `process_stage_id` | Integer (FK) | Referência ao estágio do processo. |
| `recruitment_source_id` | Integer (FK) | Referência à fonte de recrutamento. |
| `situation_id` | Integer (FK) | Referência à situação atual. |
| `interview_report` | String (1000) | Relatório/parecer da entrevista. |

### 1.9. `suggestions` & `suggestions_votes`
Sistema de feedback e sugestões.

**Tabela `suggestions`**:
- `id` (Long, PK)
- `title` (String)
- `description` (String)
- `email` (String)

**Tabela `suggestions_votes`**:
- `id` (Long, PK)
- `email` (String)
- `vote` (String)
- `suggestion_id` (Long, FK): Referência à tabela `suggestions`.

---

## 2. Tabelas Auxiliares (Domínios)

A maioria das tabelas auxiliares segue o padrão de colunas: `id`, `[nome_da_tabela]_name` e `[nome_da_tabela]_description`.

| Tabela | Descrição |
|--------|-----------|
| `position` | Cargos cadastrados no sistema. |
| `team` | Equipes (Contém `manager_id` referenciando `people`). |
| `departament` | Departamentos da empresa. |
| `opportunity_status`| Status possíveis para uma vaga (Aberta, Fechada, etc). |
| `process_stage` | Estágios do processo (Triagem, Entrevista, Proposta, etc). |
| `process_status` | Status do candidato no processo (Aprovado, Reprovado, etc). |
| `education` | Níveis de escolaridade. |
| `situation` | Situações cadastrais (Ativo, Desligado, Candidato). |
| `recruitment_source`| Origens do recrutamento (Indicação, LinkedIn, etc). |
| `base_origin` | Origem da base de dados. |
| `opportunity_motive`| Motivos para abertura de vaga (Aumento de quadro, Substituição). |
| `resignation_type` | Tipos de desligamento. |
| `resignation_motivation`| Motivos de pedidos de demissão. |

---

## 3. Principais Relacionamentos

1.  **Múltiplos Papéis de `people`**:
    *   Uma pessoa pode ser um **Candidato** em `recruitment_process`.
    *   Uma pessoa pode ser o **Recrutador Responsável** em `opportunities`.
    *   Uma pessoa pode ser a **Pessoa Substituída** em `opportunities`.
    *   Uma pessoa pode ser o **Gestor** em `team`.

2.  **Ciclo da Oportunidade**:
    *   `recruitment_process` é a tabela de ligação (N:N) conceitual entre `people` e `opportunities`, enriquecida com dados do progresso do candidato.

3.  **Padronização de FKs**:
    *   Observa-se o uso de prefixos `id_` em algumas tabelas (ex: `id_education`) e sufixos `_id` em outras (ex: `people_id`), mantendo a compatibilidade com o esquema legado.

