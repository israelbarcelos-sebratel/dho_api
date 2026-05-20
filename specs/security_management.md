# Gerenciamento de Segurança e Perfis

Este documento descreve os endpoints e regras de negócio para o gerenciamento de roles (papéis), permissões e atribuição de perfis a usuários.

## 1. Regras de Acesso
- Todos os endpoints descritos aqui são restritos a usuários com a role `ADMIN`.
- A autenticação é baseada em JWT (Google OAuth2).
- **Múltiplos Papéis**: Um usuário (`People`) pode possuir múltiplos papéis (`Role`) simultaneamente.

## 2. Endpoints de Permissões (Permissions)

### 2.1. Listar Permissões
- **GET** `/api/admin/permissions`
- **Descrição**: Retorna todas as permissões cadastradas.

### 2.2. Criar Permissão
- **POST** `/api/admin/permissions`
- **Body**:
```json
{
  "name": "NOME_DA_PERMISSAO",
  "description": "Descrição detalhada"
}
```

## 3. Endpoints de Papéis (Roles)

### 3.1. Listar Papéis
- **GET** `/api/admin/roles`
- **Descrição**: Retorna todos os papéis e suas permissões associadas.

### 3.2. Criar Papel
- **POST** `/api/admin/roles`
- **Body**:
```json
{
  "name": "NOME_DA_ROLE",
  "description": "Descrição detalhada"
}
```

### 3.3. Associar Permissões a um Papel
- **PUT** `/api/admin/roles/{roleId}/permissions`
- **Body**:
```json
[1, 2, 3] // IDs das permissões
```

### 3.4. Adicionar uma Permissão a um Papel
- **POST** `/api/admin/roles/{roleId}/permissions/{permissionId}`
- **Descrição**: Adiciona uma única permissão específica ao papel indicado.

## 4. Endpoints de Atribuição de Usuários (People Roles)

### 4.1. Listar Usuários e seus Papéis
- **GET** `/api/admin/people`
- **Descrição**: Lista pessoas cadastradas com seus respectivos papéis.

### 4.2. Atribuir Papéis a uma Pessoa
- **PUT** `/api/admin/people/{peopleId}/roles`
- **Body**:
```json
[1, 2, 3] // IDs das roles
```
- **Regra**: Os papéis atuais da pessoa serão substituídos pela nova lista de papéis informada.


## 5. Endpoints de Solicitação de Papéis (Role Requests)

### 5.1. Solicitar um Papel
- **POST** `/api/roles/requests`
- **Body**:
```json
{
  "roleId": 1
}
```
- **Descrição**: Permite que qualquer usuário autenticado solicite um papel. O `peopleId` é extraído do token JWT.
- **Regra de Negócio**: Um usuário não pode solicitar um papel se já possuir um papel ativo ou se já houver uma solicitação pendente para ele.

### 5.2. Listar Solicitações (Admin)
- **GET** `/api/admin/roles/requests`
- **Descrição**: Retorna todas as solicitações de papéis que possuem status **PENDENTE**. Restrito a `ADMIN`.
- **Regra de Negócio**: Solicitações processadas (aprovadas ou rejeitadas) são removidas do sistema e não aparecem nesta listagem.

### 5.3. Aprovar Solicitação (Admin)
- **PUT** `/api/admin/roles/requests/{requestId}/approve`
- **Descrição**: Aprova a solicitação, substitui o papel atual do usuário pelo novo papel solicitado e **exclui o registro de solicitação** do banco de dados.

### 5.4. Rejeitar Solicitação (Admin)
- **PUT** `/api/admin/roles/requests/{requestId}/reject`
- **Descrição**: Rejeita a solicitação e **exclui o registro de solicitação** do banco de dados.


## 6. Endpoints de Consulta de Papéis (Geral)

### 6.1. Listar Papéis Disponíveis
- **GET** `/api/roles`
- **Descrição**: Retorna a lista de todos os papéis disponíveis no sistema. Acessível por qualquer usuário autenticado.

## 7. Configuração de CORS

O sistema utiliza uma configuração centralizada de CORS para permitir requisições de origens confiáveis.

### 7.1. Variáveis de Ambiente
- `ALLOWED_ORIGINS`: Lista separada por vírgulas das origens permitidas (ex: `http://localhost:5173,https://meu-dominio.com`). Se não definida, o padrão é `*` para ambientes que não sejam de produção.

### 7.2. Comportamento por Perfil
- **Desenvolvimento/Staging (não `prod`)**: Permite todas as origens (`*`) por padrão para facilitar o desenvolvimento.
- **Produção (`prod`)**: Restringe as origens para aquelas definidas na variável `ALLOWED_ORIGINS`. Requer que `allowCredentials` seja `true`.

