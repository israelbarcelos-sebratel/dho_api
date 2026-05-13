import csv
import os
import sys

def calculate_coverage(csv_file):
    if not os.path.exists(csv_file):
        print(f"Erro: Arquivo {csv_file} não encontrado. Execute './mvnw test' primeiro.")
        return

    missed_instructions = 0
    covered_instructions = 0
    missed_branches = 0
    covered_branches = 0

    try:
        with open(csv_file, mode='r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                missed_instructions += int(row['INSTRUCTION_MISSED'])
                covered_instructions += int(row['INSTRUCTION_COVERED'])
                missed_branches += int(row['BRANCH_MISSED'])
                covered_branches += int(row['BRANCH_COVERED'])
    except Exception as e:
        print(f"Erro ao ler CSV: {e}")
        return

    total_instructions = missed_instructions + covered_instructions
    total_branches = missed_branches + covered_branches

    instr_percentage = (covered_instructions / total_instructions * 100) if total_instructions > 0 else 0
    branch_percentage = (covered_branches / total_branches * 100) if total_branches > 0 else 0

    print("\n" + "="*50)
    print("      RESUMO DE COBERTURA DE CÓDIGO (JACOCO)")
    print("="*50)
    print(f"Instruções: {instr_percentage:>6.2f}% ({covered_instructions}/{total_instructions})")
    print(f"Branches:    {branch_percentage:>6.2f}% ({covered_branches}/{total_branches})")
    print("-" * 50)
    print(f"Relatório completo: target/site/jacoco/index.html")
    print("="*50 + "\n")

if __name__ == "__main__":
    path = "target/site/jacoco/jacoco.csv"
    calculate_coverage(path)
