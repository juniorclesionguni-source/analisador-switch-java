# DEFESA — Analisador da Estrutura SWITCH de Java (Grupo 4)

Documento de apoio à defesa. Explica cada fase, justifica a gramática e reúne
perguntas/respostas prováveis do docente.

---

## 1. As três fases (o quê / entrada / saída)

### 1.1 Análise Léxica — `lexico/Lexer.java`
- **O que faz:** lê o código-fonte carácter a carácter e agrupa-o em *tokens*
  (lexema + classe + linha + coluna). Ignora espaços e comentários (`//` e
  `/* */`), mas conta sempre as linhas.
- **Entrada:** o texto do ficheiro `.txt`.
- **Saída:** uma lista de `Token` terminada por um token `EOF`. Erros léxicos
  (carácter inválido, string não terminada) vão para o colector e a análise
  continua.

### 1.2 Análise Sintáctica — `sintatico/Parser.java`
- **O que faz:** valida a ordem dos tokens segundo a gramática (Secção 4) e
  constrói a **árvore sintáctica (AST)**. É um parser **descendente recursivo**:
  cada não-terminal tem o seu método (`parseSwitch`, `parseCaso`, …).
- **Entrada:** a lista de tokens do lexer.
- **Saída:** um objecto `Programa` (a AST). Em caso de erro, regista-o e
  **recupera** (sincroniza num ponto seguro) para continuar.

### 1.3 Análise Semântica — `semantico/AnalisadorSemantico.java`
- **O que faz:** percorre a AST e, com a tabela de símbolos, valida o
  *significado*: declarações, tipos, selector↔case, atribuições e constantes.
- **Entrada:** a AST e uma tabela de símbolos vazia.
- **Saída:** a tabela de símbolos preenchida e os erros semânticos no colector.

---

## 2. Justificação da gramática (cadeia e aninhamento)

A gramática (Secção 4 do enunciado) está implementada quase 1-para-1 em
métodos do parser. Dois pontos são a chave do enunciado:

- **Cadeia de switches** — vem da regra
  `LISTA_SWITCH → SWITCH LISTA_SWITCH | SWITCH` (um ou mais). No código é o
  ciclo `while (verificaLexema("switch"))` em `parseListaSwitch`: lê switches
  em sequência ao nível do programa.

- **Aninhamento** — vem de `INSTRUCAO → SWITCH | ATRIBUICAO | BREAK`. Como um
  `SWITCH` é uma instrução válida dentro de um `case`, um switch pode conter
  outro switch **sem limite de profundidade**. No código isto traduz-se em
  `ComandoSwitch extends Instrucao` e na chamada recursiva a `parseSwitch()`
  dentro de `parseListaInstr()`. A mesma recursão acontece na fase semântica
  (`verificarSwitch` → `verificarInstrucao` → `verificarSwitch`).

A forma **simples** é apenas o caso base: um switch com `case`s e um `default`
opcional.

---

## 3. Tratamento e recuperação de erros

- **Colector único** (`erros/ColetorErros.java`): as três fases escrevem na
  mesma lista. Nada aborta no primeiro erro.
- **Recuperação sintáctica** (`Parser.sincronizar`): ao faltar um token, o
  parser regista *“esperado X mas encontrado Y”* (com a linha) e **salta** até
  um ponto seguro — `;`, `case`, `default`, `}` ou `switch`. Assim evita uma
  cascata de erros e consegue analisar o resto do ficheiro.
- **Ordenação:** o relatório mostra os erros ordenados por linha, mantendo (na
  mesma linha) a ordem léxica → sintáctica → semântica.

Exemplo real (teste T4): falta o `;` depois de `break` (linha 7). O parser
reporta o erro mas continua, e ainda detecta o erro semântico do `case 2`
seguinte (`dia = "ola"`, linha 9).

---

## 4. Perguntas e respostas prováveis

**P0. Que tipo de gramática é esta?**
É uma **gramática livre de contexto** (GLC, Tipo 2 na hierarquia de Chomsky):
todas as produções têm um único não-terminal à esquerda (`A → α`). É o tipo
usado para a sintaxe de linguagens de programação porque expressa estruturas
recursivas/aninhadas (switch dentro de switch), o que uma gramática regular
(Tipo 3) não consegue — os tokens em si é que são regulares, e por isso ficam
a cargo do lexer. A notação é **BNF** (com o `[ ]` opcional emprestado do
EBNF). Além disso a gramática é **LL(1)**: lendo a entrada da esquerda para a
direita, basta **1 token de lookahead** para escolher a produção (se o token é
`switch`, `final`, `case` ou `break`, cada um determina uma regra diferente),
sem ambiguidade nem backtracking — e é exactamente essa propriedade que
permite implementá-la como parser descendente recursivo.

**P1. Porque é um analisador descendente recursivo e não tabular (LR)?**
Porque a gramática é pequena, LL(1) e fácil de mapear: cada não-terminal vira
um método e a recursão da linguagem (switch dentro de switch) vira recursão de
funções. É mais legível e directamente defensável linha a linha, sem tabelas
geradas por ferramentas (que o enunciado proíbe).

**P2. Como distingues variável de constante na tabela de símbolos?**
Pela palavra reservada `final` no início da declaração. O parser marca a
`Categoria` como `CONSTANTE` (senão `VARIAVEL`) em `parseDeclaracao`, e essa
categoria fica guardada no `Simbolo`. A atribuição a um símbolo `CONSTANTE`
gera erro semântico.

**P3. Como detectas a incompatibilidade selector ↔ case?**
Procuro o tipo do selector na tabela de símbolos. Se for `int`, todos os
rótulos de `case` têm de ter classe léxica `LITERAL_INT`; se for `string`,
têm de ser `LITERAL_STRING`. A verificação está em `verificarSwitch`,
comparando `tipoSelector` com `c.rotuloClasse`.

**P4. Onde e como recuperas de um erro sintáctico?**
Nos métodos `exigirLexema`/`exigirClasse` (registo do erro) e em
`sincronizar()`/`sincronizarFimDeSwitch()` (salto até um ponto seguro). É a
estratégia de *panic-mode recovery* com tokens de sincronização.

**P5. Porque separas o parser (sintáctico) do analisador semântico?**
Para ter fases bem definidas e módulos independentes: o parser só verifica a
*estrutura* e produz a AST; o semântico só verifica o *significado*. Posso
alterar regras semânticas sem mexer no parser, e vice-versa.

**P6. Como contas as linhas se ignoras espaços e comentários?**
O lexer ignora o *conteúdo* de espaços e comentários, mas o método `avancar()`
incrementa a linha sempre que consome um `\n` — mesmo dentro de comentários.
Por isso os números de linha dos erros ficam correctos.

**P7. O que muda no código para aceitar um novo tipo (ex.: `double`)?**
Ver a secção 5 abaixo — é a pergunta mais provável sobre extensibilidade.

**P8. Porque é que o `default` é opcional e só pode aparecer uma vez?**
Porque a gramática o coloca como `[ DEFAULT ]` (opcional) depois de
`LISTA_CASO`. No parser é um único `if (verificaLexema("default"))` após o
ciclo dos `case`, logo aceita zero ou um `default`.

---

## 5. Onde alterar para cada mudança comum

| Mudança pretendida | Ficheiro(s) / função(ões) a alterar |
|---|---|
| **Aceitar um novo tipo `double`** | `lexico/Lexer.java` (juntar `"double"` a `PALAVRAS_RESERVADAS` e ler literais decimais), `lexico/ClasseToken.java` (juntar `LITERAL_DOUBLE`), `sintatico/Parser.java` (`parseTipo` aceitar `"double"`), `semantico/AnalisadorSemantico.java` (`tipoDoValor` mapear o novo literal; o selector continua restrito a int/string pela verificação em `verificarSwitch`) |
| **Nova palavra reservada** | `lexico/Lexer.java` → conjunto `PALAVRAS_RESERVADAS` |
| **Novo símbolo/operador** | `lexico/Lexer.java` → bloco `switch (c)` final |
| **Nova instrução dentro do `case`** | `sintatico/Parser.java` → `parseListaInstr`/`parseInstrucao` + novo nó em `sintatico/` + tratamento em `AnalisadorSemantico.verificarInstrucao` |
| **Nova regra semântica** | `semantico/AnalisadorSemantico.java` |
| **Nova verificação de erro / mensagem** | método relevante + `erros/ColetorErros.java` |
| **Formato das tabelas de saída** | `relatorio/Relatorio.java` |
| **Detectar `case` duplicado** | já implementado em `verificarSwitch` (conjunto `rotulosVistos`) |

---

## 6. Resumo dos erros detectados

| Fase | Erros |
|---|---|
| Léxica | carácter inválido; string não terminada; comentário de bloco não terminado |
| Sintáctica | falta de `(` `)` `{` `}` `:` `;`; `case`/`break` mal formado; token inesperado; falta de `switch`/`case` |
| Semântica | identificador não declarado; tipo de selector inválido; selector↔case incompatível; tipo incompatível na atribuição/inicialização; atribuição a constante; declaração duplicada; `case` duplicado |
