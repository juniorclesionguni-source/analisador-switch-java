# Analisador da Estrutura SWITCH de Java — Compiladores (Grupo 4)

Front-end de compilador (análise **Léxica → Sintáctica → Semântica**) para um
subconjunto de Java centrado na estrutura `switch`. Tudo escrito **à mão** —
sem geradores (ANTLR, JFlex, CUP, etc.). Saída em **tabelas formatadas na
consola**, em português.

## Requisitos

- **Java 8 ou superior** (testado com OpenJDK 17).
- O `javac` e o `java` têm de estar no `PATH`.

## Estrutura de pastas (módulos — Secção 9 do enunciado)

```
analisador-switch-java/
├── src/
│   ├── Main.java                 # orquestra as 3 fases e lê o ficheiro de entrada
│   ├── lexico/                   # análise léxica
│   │   ├── ClasseToken.java      #   classes (categorias) de token
│   │   ├── Token.java            #   token: lexema, classe, linha, coluna
│   │   └── Lexer.java            #   gera a lista de tokens (carácter a carácter)
│   ├── sintatico/                # análise sintáctica (parser descendente recursivo)
│   │   ├── Parser.java           #   um método por não-terminal da gramática
│   │   ├── Programa.java         #   nós da árvore sintáctica (AST):
│   │   ├── Declaracao.java       #     declarações
│   │   ├── ComandoSwitch.java    #     switch (suporta cadeia e aninhamento)
│   │   ├── Caso.java             #     case / default
│   │   ├── Instrucao.java        #     instrução (base)
│   │   ├── Atribuicao.java       #     atribuição
│   │   └── ComandoBreak.java     #     break
│   ├── semantico/
│   │   └── AnalisadorSemantico.java  # verificações de tipos e declarações
│   ├── tabelas/
│   │   ├── Categoria.java        #   variável / constante
│   │   ├── Simbolo.java          #   entrada da tabela de símbolos
│   │   ├── TabelaSimbolos.java   #   inserir / procurar
│   │   └── TabelaLexemas.java    #   registo de tokens para apresentação
│   ├── erros/
│   │   ├── Fase.java             #   léxica / sintáctica / semântica
│   │   ├── Erro.java             #   um erro (fase, descrição, linha)
│   │   └── ColetorErros.java     #   colector central (lista única)
│   └── relatorio/
│       └── Relatorio.java        # imprime as 4 tabelas obrigatórias
├── testes/
│   ├── t1_valido_simples.txt
│   ├── t2_valido_aninhado.txt
│   ├── t3_valido_cadeia.txt
│   └── t4_com_erros.txt
├── compilar.ps1                  # script de compilação (Windows/PowerShell)
├── compilar.sh                   # script de compilação (Linux/macOS/Git Bash)
├── README.md
└── DEFESA.md                     # apoio para a defesa
```

## Como compilar

### Windows (PowerShell)
```powershell
.\compilar.ps1
```
ou manualmente:
```powershell
$srcs = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $srcs
```

### Linux / macOS / Git Bash
```bash
./compilar.sh
```
ou manualmente:
```bash
javac -encoding UTF-8 -d out $(find src -name '*.java')
```

> Nota: o `javac` não expande `*.java` por si — por isso usamos a lista de
> ficheiros (`Get-ChildItem`/`find`). Os scripts já tratam disto.

## Como executar

```bash
java -cp out Main testes/t1_valido_simples.txt
```

Substitui pelo teste pretendido (`t2_...`, `t3_...`, `t4_...`) ou pelo teu
próprio ficheiro `.txt`.

### Acentos no Windows
A aplicação já força a saída em UTF-8. Se mesmo assim a consola mostrar acentos
incorrectos, executa primeiro:
```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

## O que o programa imprime (Secção 7)

1. **(a)** o código-fonte analisado, com numeração de linhas;
2. **(b)** a tabela de lexemas (`#`, lexema, classe, linha, coluna);
3. **(c)** a tabela de símbolos (`#`, identificador, categoria, tipo, valor, linha);
4. **(d)** a lista de erros (`#`, fase, descrição, linha) — ou
   *“Análise concluída sem erros.”*

A análise **não aborta no primeiro erro**: acumula todos os erros das três
fases e mostra-os no fim, ordenados por linha.

## Linguagem-fonte reconhecida (resumo)

- Declarações de `int` e `string`, variáveis ou `final` (constantes).
- Uma ou mais estruturas `switch` (formas **simples**, **em cadeia** e
  **aninhada**).
- Dentro de cada `case`/`default`: apenas `switch` aninhado, `atribuição` ou
  `break;`.

A gramática completa (BNF) está na Secção 4 do enunciado e no `DEFESA.md`.
