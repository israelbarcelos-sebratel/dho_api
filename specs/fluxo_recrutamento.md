# Especificação de Funcionalidade: Fluxo de Recrutamento Completo

Esta especificação descreve o fluxo de recrutamento de um candidato, desde a criação da oportunidade até a finalização do processo após a aceitação da proposta.

## Estados do Processo

### Estágios (`DhoProcessStage`)
- **Triagem**: Estágio inicial quando o candidato é vinculado à oportunidade.
- **Entrevista**: Quando o candidato avança para a fase de entrevistas.
- **Teste Técnico**: Quando o candidato avança para a fase de avaliação técnica.

### Status (`DhoProcessStatus`)
- **Em andamento**: Status padrão durante as fases de avaliação.
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
1. **Criação da Oportunidade**: A recrutadora ou gestor cria uma nova vaga.
2. **Criação do Candidato**: O sistema registra um novo candidato.
3. **Vínculo**: O candidato é atrelado à oportunidade. O sistema define automaticamente o estágio como **Triagem**.
4. **Avanço para Entrevista**: A recrutadora move o candidato para o estágio de **Entrevista**.
5. **Avanço para Teste Técnico**: A recrutadora move o candidato para o estágio de **Teste Técnico**.
6. **Decisão do Gestor**: O gestor revisa o processo e aprova o candidato. O status muda para **Aprovado pelo Gestor**.
7. **Proposta**: A recrutadora envia a proposta. O status muda para **Enviada Proposta**.
8. **Decisão do Candidato**: O candidato aceita a proposta. O status muda para **Finalizado**.

## Regras de Negócio
- Se o gestor recusar, o status deve ser **Recusado pelo gestor** e o processo é interrompido.
- Se o candidato recusar a proposta, o status deve refletir a recusa e o processo é interrompido.
- A mudança de status e estágio deve gerar logs no sistema (opcional para este spec, mas desejável).


## Regras de Segurança e Resistência
- **Restrição de Papéis**:
    - Apenas usuários com permissão `approve_candidate` podem mover candidatos entre estágios (`Entrevista`, `Teste Técnico`).
    - Apenas usuários com permissão `reject_candidate` podem registrar a decisão do gestor.
    - Apenas usuários com permissão `initiate_contract_process` podem enviar propostas e finalizar o processo.
- **Validação de Estado**:
    - Não é permitido pular estágios obrigatorios (ex: Proposta sem aprovação do gestor).
    - Não é permitido retroceder estágios.
    - O processo deve estar no status correto para aceitar uma decisão do candidato.
