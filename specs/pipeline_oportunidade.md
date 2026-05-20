# Especificação Técnica: Endpoint de Pipeline de Oportunidades (Recrutador)

Este documento descreve o endpoint criado para suportar a visualização do pipeline (funil) de candidatos para todas as vagas vinculadas ao recrutador autenticado.

## 1. Informações do Endpoint

- **URL**: `/api/opportunities/pipeline`
- **Método**: `GET`
- **Autenticação**: Requer JWT válido.
- **Permissão Necessária**: `view_pipeline` ou `ROLE_ADMIN`.

## 2. Descrição
O endpoint retorna uma lista de objetos, onde cada objeto contém os detalhes de uma oportunidade (onde o usuário autenticado é o `responsible_recruiter`) e a lista de todos os processos de recrutamento (candidatos) vinculados a ela.

## 3. Estrutura de Resposta (JSON)

Retorna um Array de objetos com a estrutura solicitada:

```json
[
  {
    "opportunity": {
      "id": "Integer",
      "positionName": "String",
      "teamName": "String",
      "departmentName": "String",
      "processes": [
        {
          "id": "Integer (ID do Processo)",
          "processStageName": "String (Ex: Triagem, Entrevista)",
          "processStatusName": "String (Ex: Em andamento, Aprovado)",
          "candidate": {
            "id": "Integer (ID do Candidato/Pessoa)",
            "name": "String",
            "email": "String",
            "phoneNumber": "String",
            "profileImage": "String (URL)"
          }
        }
      ]
    }
  }
]
```

## 4. Exemplo de Uso (CURL)

Substitua `{TOKEN}` pelo seu token de acesso.

```bash
curl --location 'http://localhost:8080/api/opportunities/pipeline' \
--header 'Authorization: Bearer {TOKEN}'
```

## 5. Regras de Negócio Aplicadas
- Retorna apenas oportunidades com status **"Aprovada"**.
- Filtra automaticamente pelo e-mail do usuário autenticado (vinculado ao campo `responsible_recruiter_id`).
- Se o recrutador não tiver vagas atribuídas, retorna uma lista vazia `[]`.
