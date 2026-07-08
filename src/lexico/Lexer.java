package lexico;

import erros.ColetorErros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analisador Léxico (Secção 6.1) — escrito À MÃO (sem geradores).
 *
 * O quê:   percorre o código-fonte carácter a carácter e produz a lista de
 *          tokens (lexema, classe, linha, coluna).
 * Entrada: o texto completo do ficheiro-fonte.
 * Saída:   uma List<Token> terminada por um token EOF.
 *
 * Ignora espaços, tabs, quebras de linha e comentários (// e /* *​/), mas
 * conta sempre as linhas para que os erros tenham a localização correcta.
 * Erros léxicos (carácter inválido, string não terminada) são registados no
 * ColetorErros e o lexer CONTINUA (recuperação).
 */
public class Lexer {

    /** Palavras reservadas da linguagem-fonte (Secção 5). */
    private static final Set<String> PALAVRAS_RESERVADAS = new HashSet<>(Arrays.asList(
            "switch", "case", "default", "break", "int", "String", "string", "final"
    ));

    private final String fonte;
    private final ColetorErros erros;

    private int pos = 0;     // índice actual no texto
    private int linha = 1;   // linha actual (começa em 1)
    private int coluna = 1;  // coluna actual (começa em 1)

    public Lexer(String fonte, ColetorErros erros) {
        this.fonte = fonte;
        this.erros = erros;
    }

    /** Executa a análise léxica completa e devolve a lista de tokens. */
    public List<Token> analisar() {
        List<Token> tokens = new ArrayList<>();

        while (!fim()) {
            char c = atual();

            // 1) Espaços em branco e quebras de linha — ignorar (mas contar linhas).
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                avancar();
                continue;
            }

            // 2) Comentários — ignorar.
            if (c == '/' && proximo() == '/') { comentarioLinha(); continue; }
            if (c == '/' && proximo() == '*') { comentarioBloco(); continue; }

            // A partir daqui começa um token: guardar a posição inicial.
            int linhaIni = linha;
            int colIni = coluna;

            // 3) Identificadores e palavras reservadas (começam por letra).
            if (ehLetra(c)) {
                String lexema = lerIdentificador();
                ClasseToken classe = PALAVRAS_RESERVADAS.contains(lexema)
                        ? ClasseToken.PALAVRA_RESERVADA
                        : ClasseToken.IDENTIFICADOR;
                tokens.add(new Token(classe, lexema, linhaIni, colIni));
                continue;
            }

            // 4) Literais inteiros (com sinal negativo opcional).
            if (ehDigito(c)) {
                tokens.add(new Token(ClasseToken.LITERAL_INT, lerInteiro(), linhaIni, colIni));
                continue;
            }
            if (c == '-' && ehDigito(proximo())) {
                avancar(); // consome o '-'
                tokens.add(new Token(ClasseToken.LITERAL_INT, "-" + lerInteiro(), linhaIni, colIni));
                continue;
            }

            // 5) Literais string.
            if (c == '"') {
                String lexema = lerString(linhaIni);
                if (lexema != null) {
                    tokens.add(new Token(ClasseToken.LITERAL_STRING, lexema, linhaIni, colIni));
                }
                continue;
            }

            // 6) Operadores, delimitadores e pontuação (símbolos de 1 carácter).
            switch (c) {
                case '=':
                    tokens.add(new Token(ClasseToken.OP_ATRIBUICAO, "=", linhaIni, colIni));
                    avancar();
                    break;
                case '(': case ')': case '{': case '}':
                    tokens.add(new Token(ClasseToken.DELIMITADOR, String.valueOf(c), linhaIni, colIni));
                    avancar();
                    break;
                case ':': case ';':
                    tokens.add(new Token(ClasseToken.PONTUACAO, String.valueOf(c), linhaIni, colIni));
                    avancar();
                    break;
                default:
                    // 7) Qualquer outro carácter é um erro léxico.
                    erros.adicionarLexico("Carácter inválido '" + c + "'", linhaIni);
                    avancar(); // recupera: salta o carácter e continua
            }
        }

        // Sentinela de fim de ficheiro — simplifica a vida ao parser.
        tokens.add(new Token(ClasseToken.EOF, "<fim>", linha, coluna));
        return tokens;
    }

    // ----------------------------------------------------------------------
    // Reconhecedores específicos
    // ----------------------------------------------------------------------

    /** IDENT -> letra ( letra | dígito )* */
    private String lerIdentificador() {
        StringBuilder sb = new StringBuilder();
        while (!fim() && (ehLetra(atual()) || ehDigito(atual()))) {
            sb.append(atual());
            avancar();
        }
        return sb.toString();
    }

    /** Lê uma sequência de dígitos (a parte sem sinal de um LIT_INT). */
    private String lerInteiro() {
        StringBuilder sb = new StringBuilder();
        while (!fim() && ehDigito(atual())) {
            sb.append(atual());
            avancar();
        }
        return sb.toString();
    }

    /**
     * LIT_STRING -> '"' (qualquer carácter excepto '"')* '"'
     * Devolve o lexema COM aspas (para a tabela de lexemas) ou null se houver
     * erro (string não terminada antes do fim da linha/ficheiro).
     */
    private String lerString(int linhaIni) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        avancar(); // consome as aspas de abertura

        while (!fim() && atual() != '"' && atual() != '\n') {
            sb.append(atual());
            avancar();
        }

        if (fim() || atual() == '\n') {
            erros.adicionarLexico("String não terminada (falta fechar com aspas)", linhaIni);
            return null;
        }

        sb.append('"');
        avancar(); // consome as aspas de fecho
        return sb.toString();
    }

    /** Comentário de linha: // ... até ao fim da linha. */
    private void comentarioLinha() {
        while (!fim() && atual() != '\n') {
            avancar();
        }
    }

    /** Comentário de bloco: /* ... *​/ (pode ocupar várias linhas). */
    private void comentarioBloco() {
        int linhaIni = linha;
        avancar(); // '/'
        avancar(); // '*'
        while (!fim() && !(atual() == '*' && proximo() == '/')) {
            avancar();
        }
        if (fim()) {
            erros.adicionarLexico("Comentário de bloco não terminado", linhaIni);
            return;
        }
        avancar(); // '*'
        avancar(); // '/'
    }

    // ----------------------------------------------------------------------
    // Utilitários de baixo nível
    // ----------------------------------------------------------------------

    private boolean fim() {
        return pos >= fonte.length();
    }

    private char atual() {
        return fonte.charAt(pos);
    }

    private char proximo() {
        return (pos + 1 < fonte.length()) ? fonte.charAt(pos + 1) : '\0';
    }

    /** Avança um carácter, actualizando a linha e a coluna. */
    private void avancar() {
        if (fim()) {
            return;
        }
        if (fonte.charAt(pos) == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }
        pos++;
    }

    private boolean ehLetra(char c) {
        return Character.isLetter(c);
    }

    private boolean ehDigito(char c) {
        return c >= '0' && c <= '9';
    }
}
