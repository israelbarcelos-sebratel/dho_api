#!/bin/bash

# Cores para o terminal
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Iniciando modo de monitoramento de coverage...${NC}"
echo -e "${YELLOW}Dica: Pressione Ctrl+C para sair.${NC}"

# Função para executar testes e mostrar coverage
run_coverage() {
    echo -e "\n${YELLOW}Detectada mudança em src/. Executando testes...${NC}\n"
    ./mvnw test -DskipTests=false
    python3 scripts/coverage_summary.py
}

# Primeira execução
run_coverage

# Loop de monitoramento manual simplificado
# Ele verifica o timestamp do diretório src recursivamente
LAST_STATE=$(find src -type f -exec stat -c '%Y' {} + | sort -n | tail -1)

while true; do
    sleep 2
    CURRENT_STATE=$(find src -type f -exec stat -c '%Y' {} + | sort -n | tail -1)
    
    if [ "$CURRENT_STATE" != "$LAST_STATE" ]; then
        run_coverage
        LAST_STATE=$CURRENT_STATE
    fi
done
