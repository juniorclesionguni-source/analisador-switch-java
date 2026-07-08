import erros.ColetorErros;
import lexico.Lexer;
import lexico.Token;
import relatorio.Relatorio;
import semantico.AnalisadorSemantico;
import sintatico.Parser;
import sintatico.Programa;
import tabelas.TabelaLexemas;
import tabelas.TabelaSimbolos;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Programa principal (módulo "main" da Secção 9).
 *
 * O quê: orquestra as três fases (léxica -> sintáctica -> semântica) e manda
 *        imprimir as quatro saídas obrigatórias.
 * Uso:   java -cp out Main <ficheiro-fonte.txt>
 */
public class Main {

    public static void main(String[] args) {
        // Garante acentos correctos do português na saída.
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (Exception ignored) {
            // Se a plataforma não suportar, continua na codificação por omissão.
        }

        if (args.length < 1) {
            System.out.println("Uso: java -cp out Main <ficheiro-fonte.txt>");
            System.out.println("Exemplo: java -cp out Main testes/t1_valido_simples.txt");
            return;
        }

        String caminho = args[0];
        String fonte;
        try {
            fonte = new String(Files.readAllBytes(Paths.get(caminho)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Erro: não foi possível ler o ficheiro '" + caminho + "': " + e.getMessage());
            return;
        }

        // Colector central de erros, partilhado por todas as fases.
        ColetorErros erros = new ColetorErros();

        // Fase 1 — Análise léxica.
        Lexer lexer = new Lexer(fonte, erros);
        List<Token> tokens = lexer.analisar();

        // Fase 2 — Análise sintáctica (constrói a AST).
        Parser parser = new Parser(tokens, erros);
        Programa programa = parser.analisar();

        // Fase 3 — Análise semântica (preenche a tabela de símbolos e valida tipos).
        TabelaSimbolos tabelaSimbolos = new TabelaSimbolos();
        AnalisadorSemantico semantico = new AnalisadorSemantico(tabelaSimbolos, erros);
        semantico.analisar(programa);

        // Saída — as quatro tabelas.
        TabelaLexemas tabelaLexemas = new TabelaLexemas(tokens);
        Relatorio relatorio = new Relatorio();
        relatorio.imprimirTudo(fonte, tabelaLexemas.tokens(), tabelaSimbolos.todos(), erros.lista());
    }
}
