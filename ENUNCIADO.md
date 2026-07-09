# Enunciado — Trabalho 2 de Compiladores (Grupo 4)

## Regras gerais (todos os temas)

I. Cada item é um tema cujo trabalho deve ser feito por grupos de quatro
estudantes, grupos previamente formados a partir do trabalho 1. O trabalho 2
deve ser entregue e defendido. O trabalho deve ser feito no período de 15 dias.

II. Todos os trabalhos devem implementar as fases de análise de um compilador:
Análise **Léxica, Sintáctica e Semântica**.

III. Todos os trabalhos requerem o uso de identificadores da categoria
**variável ou constante**; logo, um analisador deverá fazer o reconhecimento
deles e seguir-se a análise da estrutura sintáctica prevista no trabalho.

IV. Todos os trabalhos devem visualizar o **código fonte analisado**, a
**tabela de lexemas** e a **tabela de símbolos**. Apresentar a **lista dos
erros** identificados na fase de análise.

## Tema 4 — Estrutura de selecção múltipla SWITCH da linguagem Java

Considere as diversas formas de definição da estrutura, **em cadeia ou
aninhados**. Cada caso na estrutura deve aceitar instruções do tipo:
**estrutura switch**, **instruções simples de atribuição** e a palavra
reservada **break**. Os identificadores usados nas instruções de atribuição e
o selector devem ser de tipo **int ou string**.

---

## Conformidade da implementação

| Requisito do enunciado | Onde está |
|---|---|
| Análise léxica | `src/lexico/Lexer.java` |
| Análise sintáctica | `src/sintatico/Parser.java` (descendente recursivo) |
| Análise semântica | `src/semantico/AnalisadorSemantico.java` |
| Variável e constante (`final`) | `src/tabelas/Categoria.java` + declarações |
| Código fonte analisado | aba "Codigo numerado" (UI) / secção (a) (consola) |
| Tabela de lexemas | aba "Lexemas" / secção (b) |
| Tabela de símbolos | aba "Simbolos" / secção (c) |
| Lista de erros | aba "Erros" / secção (d) |
| Switch em cadeia | `LISTA_SWITCH` — switches em sequência (teste T3) |
| Switch aninhado | `INSTRUCAO → SWITCH` — sem limite de profundidade (teste T2) |
| Case aceita: switch, atribuição, break | `Parser.parseListaInstr` |
| Selector e atribuições de tipo int ou string | verificações 2–4 do semântico |
| Extra: árvore sintáctica (AST) desenhada | aba "Arvore (AST)" |
