-- Query para sincronizar os nomes das permissões com o novo Enum Permission
-- Execute este script no banco de dados DHO_Application para garantir a integridade do mapeamento JPA

-- 1. Padronizar permissões de requisitos (RQxx) para caixa alta
UPDATE permissions SET name = UPPER(name) WHERE name REGEXP '^RQ[0-9]{2}';

-- 2. Padronizar a permissão de sugestões
UPDATE permissions SET name = 'SUGGESTIONS' WHERE name = 'suggestions';

-- 3. Padronizar a permissão padrão
UPDATE permissions SET name = 'DEFAULT' WHERE name = 'default' OR name = 'Default';

-- 4. Verificar se existem permissões que não estão no Enum (opcional)
-- SELECT * FROM permissions WHERE name NOT IN (
--     'RQ01', 'RQ02', 'RQ03', 'RQ04', 'RQ05', 'RQ06', 'RQ07', 'RQ08', 'RQ09', 'RQ10', 
--     'RQ11', 'RQ12', 'RQ13', 'RQ14', 'RQ15', 'RQ16', 'RQ17', 'RQ18', 'RQ19', 'RQ20', 
--     'RQ21', 'RQ22', 'RQ23', 'RQ24', 'RQ25', 'RQ26', 'RQ27', 'RQ28', 'SUGGESTIONS', 'DEFAULT'
-- );
