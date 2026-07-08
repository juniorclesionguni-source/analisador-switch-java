#!/usr/bin/env bash
# Compila o projecto (Linux / macOS / Git Bash).
# Uso:  ./compilar.sh
set -e
BASE="$(cd "$(dirname "$0")" && pwd)"
find "$BASE/src" -name '*.java' > "$BASE/sources.txt"
javac -encoding UTF-8 -d "$BASE/out" @"$BASE/sources.txt"
echo "Compilacao OK. Para executar:"
echo "  java -cp \"$BASE/out\" Main testes/t1_valido_simples.txt"
