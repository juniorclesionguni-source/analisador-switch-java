package sintatico;

import erros.ColetorErros;
import lexico.ClasseToken;
import lexico.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisador Sintáctico (Secção 6.2) — parser DESCENDENTE RECURSIVO, à mão.
 *
 * O quê:   valida a estrutura do programa segundo a gramática da Secção 4 e
 *          constrói a árvore sintáctica (AST) que será percorrida pela fase
 *          semântica.
 * Entrada: a lista de tokens produzida pelo Lexer.
 * Saída:   um objecto Programa (a AST).
 *
 * Cada não-terminal da gramática corresponde a um método (parseXxx). Isto
 * torna a implementação directamente legível a partir da BNF — essencial para
 * a defesa.
 *
 * RECUPERAÇÃO DE ERRO (Secção 6.2): quando um token esperado falta, regista-se
 * o erro e tenta-se continuar. O método {@link #sincronizar()} salta tokens
 * até um ponto seguro (';', 'case', 'default', '}', 'switch'), evitando uma
 * cascata de erros e permitindo analisar o resto do ficheiro.
 */
public class Parser {

    private final List<Token> tokens;
    private final ColetorErros erros;
    private int pos = 0;

    public Parser(List<Token> tokens, ColetorErros erros) {
        this.tokens = tokens;
        this.erros = erros;
    }

    // ======================================================================
    // Ponto de entrada: PROGRAMA -> LISTA_DECL LISTA_SWITCH
    // ======================================================================
    public Programa analisar() {
        Programa programa = new Programa();
        parseListaDecl(programa);
        parseListaSwitch(programa);

        if (!fim()) {
            erros.adicionarSintatico(
                    "Tokens inesperados após o fim do programa: '" + atual().lexema + "'",
                    atual().linha);
        }
        return programa;
    }

    // ======================================================================
    // LISTA_DECL -> DECLARACAO ';' LISTA_DECL | ε
    // ======================================================================
    private void parseListaDecl(Programa programa) {
        while (inicioDeDeclaracao()) {
            Declaracao d = parseDeclaracao();
            if (exigirLexema(";", "';' no fim da declaração") == null) {
                sincronizar();
            }
            if (d != null) {
                programa.declaracoes.add(d);
            }
        }
    }

    private boolean inicioDeDeclaracao() {
        return verificaLexema("int") || verificaTipoString() || verificaLexema("final");
    }

    /**
     * DECLARACAO -> TIPO IDENT [ '=' VALOR ]
     *             | 'final' TIPO IDENT '=' VALOR
     */
    private Declaracao parseDeclaracao() {
        int linha = atual().linha;

        if (verificaLexema("final")) {
            avancar(); // consome 'final'
            String tipo = parseTipo();
            Token id = exigirClasse(ClasseToken.IDENTIFICADOR, "identificador");
            String nome = (id != null) ? id.lexema : null;

            // Uma constante TEM de ser inicializada.
            String valorLexema = null;
            ClasseToken valorClasse = null;
            if (exigirLexema("=", "'=' (uma constante 'final' tem de ser inicializada)") != null) {
                Token valor = parseValor();
                if (valor != null) {
                    valorLexema = valor.lexema;
                    valorClasse = valor.classe;
                }
            }
            return new Declaracao(tabelas.Categoria.CONSTANTE, tipo, nome, valorLexema, valorClasse, linha);
        }

        // Variável: inicializador é opcional.
        String tipo = parseTipo();
        Token id = exigirClasse(ClasseToken.IDENTIFICADOR, "identificador");
        String nome = (id != null) ? id.lexema : null;

        String valorLexema = null;
        ClasseToken valorClasse = null;
        if (verificaLexema("=")) {
            avancar(); // consome '='
            Token valor = parseValor();
            if (valor != null) {
                valorLexema = valor.lexema;
                valorClasse = valor.classe;
            }
        }
        return new Declaracao(tabelas.Categoria.VARIAVEL, tipo, nome, valorLexema, valorClasse, linha);
    }

    /** TIPO -> 'int' | 'String' */
    private String parseTipo() {
        if (verificaLexema("int")) {
            return avancar().lexema;
        }
        if (verificaTipoString()) {
            avancar();
            return "String";
        }
        erros.adicionarSintatico(
                "Esperado tipo ('int' ou 'String') mas encontrado '" + atual().lexema + "'",
                atual().linha);
        return "?";
    }

    private boolean verificaTipoString() {
        return verificaLexema("String") || verificaLexema("string");
    }

    /** VALOR -> CONSTANTE | IDENT  (CONSTANTE -> LIT_INT | LIT_STRING) */
    private Token parseValor() {
        if (verificaClasse(ClasseToken.LITERAL_INT)
                || verificaClasse(ClasseToken.LITERAL_STRING)
                || verificaClasse(ClasseToken.IDENTIFICADOR)) {
            return avancar();
        }
        erros.adicionarSintatico(
                "Esperado valor (literal int, literal string ou identificador) mas encontrado '"
                        + atual().lexema + "'",
                atual().linha);
        return null;
    }

    // ======================================================================
    // LISTA_SWITCH -> SWITCH LISTA_SWITCH | SWITCH   (>= 1 -> permite cadeia)
    // ======================================================================
    private void parseListaSwitch(Programa programa) {
        boolean algum = false;
        while (verificaLexema("switch")) {
            ComandoSwitch s = parseSwitch();
            if (s != null) {
                programa.switches.add(s);
            }
            algum = true;
        }
        if (!algum) {
            erros.adicionarSintatico(
                    "Esperada pelo menos uma estrutura 'switch' mas encontrado '" + atual().lexema + "'",
                    atual().linha);
        }
    }

    /** SWITCH -> 'switch' '(' SELECTOR ')' '{' LISTA_CASO [ DEFAULT ] '}' */
    private ComandoSwitch parseSwitch() {
        int linha = atual().linha;
        exigirLexema("switch", "'switch'");        // consome 'switch'
        exigirLexema("(", "'(' após 'switch'");

        // SELECTOR -> IDENT
        Token sel = exigirClasse(ClasseToken.IDENTIFICADOR, "identificador (selector)");
        String selector = (sel != null) ? sel.lexema : null;
        int linhaSelector = (sel != null) ? sel.linha : linha;

        exigirLexema(")", "')' após o selector");
        exigirLexema("{", "'{' a abrir o corpo do switch");

        ComandoSwitch comando = new ComandoSwitch(selector, linhaSelector, linha);

        // LISTA_CASO -> CASO LISTA_CASO | CASO   (pelo menos um)
        if (!verificaLexema("case")) {
            erros.adicionarSintatico(
                    "Esperado pelo menos um 'case' dentro do switch", atual().linha);
        }
        while (verificaLexema("case")) {
            Caso c = parseCaso();
            if (c != null) {
                comando.casos.add(c);
            }
        }

        // DEFAULT opcional
        if (verificaLexema("default")) {
            comando.casoDefault = parseDefault();
        }

        if (exigirLexema("}", "'}' a fechar o switch") == null) {
            sincronizarFimDeSwitch();
        }
        return comando;
    }

    /** CASO -> 'case' CONSTANTE ':' LISTA_INSTR */
    private Caso parseCaso() {
        int linha = atual().linha;
        exigirLexema("case", "'case'"); // consome 'case'

        Token rotulo = null;
        if (verificaClasse(ClasseToken.LITERAL_INT) || verificaClasse(ClasseToken.LITERAL_STRING)) {
            rotulo = avancar();
        } else {
            erros.adicionarSintatico(
                    "Esperada constante (literal int ou string) após 'case' mas encontrado '"
                            + atual().lexema + "'",
                    atual().linha);
        }

        exigirLexema(":", "':' após o rótulo do 'case'");
        List<Instrucao> instrucoes = parseListaInstr();

        String rotuloLexema = (rotulo != null) ? rotulo.lexema : null;
        ClasseToken rotuloClasse = (rotulo != null) ? rotulo.classe : null;
        return new Caso(false, rotuloLexema, rotuloClasse, linha, instrucoes);
    }

    /** DEFAULT -> 'default' ':' LISTA_INSTR */
    private Caso parseDefault() {
        int linha = atual().linha;
        exigirLexema("default", "'default'");
        exigirLexema(":", "':' após 'default'");
        List<Instrucao> instrucoes = parseListaInstr();
        return new Caso(true, null, null, linha, instrucoes);
    }

    /**
     * LISTA_INSTR -> INSTRUCAO LISTA_INSTR | ε
     * INSTRUCAO   -> SWITCH | ATRIBUICAO | BREAK
     */
    private List<Instrucao> parseListaInstr() {
        List<Instrucao> lista = new ArrayList<>();
        while (true) {
            if (verificaLexema("switch")) {                    // aninhamento
                ComandoSwitch s = parseSwitch();
                if (s != null) {
                    lista.add(s);
                }
            } else if (verificaLexema("break")) {
                lista.add(parseBreak());
            } else if (verificaClasse(ClasseToken.IDENTIFICADOR)) {
                Instrucao a = parseAtribuicao();
                if (a != null) {
                    lista.add(a);
                }
            } else {
                // Paragens normais da lista de instruções.
                if (verificaLexema("case") || verificaLexema("default")
                        || verificaLexema("}") || fim()) {
                    break;
                }
                // Token inesperado dentro de um case: regista e recupera.
                erros.adicionarSintatico(
                        "Instrução inválida: token inesperado '" + atual().lexema + "'",
                        atual().linha);
                sincronizar();
                if (verificaLexema("case") || verificaLexema("default")
                        || verificaLexema("}") || fim()) {
                    break;
                }
            }
        }
        return lista;
    }

    /** ATRIBUICAO -> IDENT '=' VALOR ';' */
    private Atribuicao parseAtribuicao() {
        Token destino = avancar(); // IDENTIFICADOR (já confirmado pelo chamador)
        int linha = destino.linha;
        String nome = destino.lexema;

        if (!verificaLexema("=")) {
            erros.adicionarSintatico("Esperado '=' na atribuição a '" + nome + "'", linha);
            sincronizar();
            return null;
        }
        avancar(); // consome '='

        Token valor = parseValor();

        if (verificaLexema(";")) {
            avancar(); // consome ';'
        } else {
            erros.adicionarSintatico("Esperado ';' no fim da atribuição a '" + nome + "'", linha);
            // Não consome: deixa a recuperação natural reposicionar (case/}/...).
        }

        if (valor == null) {
            return null;
        }
        return new Atribuicao(nome, valor.lexema, valor.classe, linha);
    }

    /** BREAK -> 'break' ';' */
    private ComandoBreak parseBreak() {
        Token br = exigirLexema("break", "'break'"); // consome 'break'
        int linha = (br != null) ? br.linha : atual().linha;

        if (verificaLexema(";")) {
            avancar(); // consome ';'
        } else {
            // Reporta na linha do 'break' (não na do token seguinte).
            erros.adicionarSintatico("Esperado ';' após 'break'", linha);
        }
        return new ComandoBreak(linha);
    }

    // ======================================================================
    // Utilitários do parser
    // ======================================================================

    private Token atual() {
        return tokens.get(pos);
    }

    private boolean fim() {
        return atual().classe == ClasseToken.EOF;
    }

    private Token avancar() {
        Token t = atual();
        if (!fim()) {
            pos++;
        }
        return t;
    }

    private boolean verificaLexema(String lexema) {
        return atual().lexema.equals(lexema);
    }

    private boolean verificaClasse(ClasseToken classe) {
        return atual().classe == classe;
    }

    /**
     * Exige um lexema concreto: se corresponder, consome-o e devolve-o; caso
     * contrário regista um erro sintáctico (esperado vs. encontrado) e devolve
     * null SEM consumir.
     */
    private Token exigirLexema(String lexema, String descricao) {
        if (verificaLexema(lexema)) {
            return avancar();
        }
        erros.adicionarSintatico(
                "Esperado " + descricao + " mas encontrado '" + atual().lexema + "'",
                atual().linha);
        return null;
    }

    /** Como {@link #exigirLexema}, mas exige uma CLASSE de token. */
    private Token exigirClasse(ClasseToken classe, String descricao) {
        if (verificaClasse(classe)) {
            return avancar();
        }
        erros.adicionarSintatico(
                "Esperado " + descricao + " mas encontrado '" + atual().lexema + "'",
                atual().linha);
        return null;
    }

    /**
     * Recuperação genérica: avança até um ponto de sincronização. Consome o ';'
     * (fim natural de instrução) ou pára imediatamente antes de 'case',
     * 'default', '}' ou 'switch' (inícios de estruturas seguintes).
     */
    private void sincronizar() {
        while (!fim()) {
            String lx = atual().lexema;
            if (lx.equals(";")) {
                avancar();
                return;
            }
            if (lx.equals("case") || lx.equals("default") || lx.equals("}") || lx.equals("switch")) {
                return;
            }
            avancar();
        }
    }

    /** Recuperação específica do fecho de um switch: salta até '}' ou 'switch'. */
    private void sincronizarFimDeSwitch() {
        while (!fim() && !verificaLexema("}") && !verificaLexema("switch")) {
            avancar();
        }
        if (verificaLexema("}")) {
            avancar();
        }
    }
}
