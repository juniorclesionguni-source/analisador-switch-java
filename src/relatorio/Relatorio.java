package relatorio;

import erros.Erro;
import lexico.Token;
import sintatico.Atribuicao;
import sintatico.Caso;
import sintatico.ComandoBreak;
import sintatico.ComandoSwitch;
import sintatico.Declaracao;
import sintatico.Instrucao;
import sintatico.Programa;
import tabelas.Simbolo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Módulo de saída / relatório (Secção 9).
 *
 * O quê: imprime na consola as quatro saídas obrigatórias (Secção 7):
 *        (a) código-fonte numerado, (b) tabela de lexemas, (c) tabela de
 *        símbolos, (d) lista de erros.
 * Porquê: concentra toda a formatação num único módulo, separando a
 *         apresentação da lógica de análise.
 */
public class Relatorio {

    /**
     * Apresentação FASEADA: cada fase do compilador aparece por ordem, com uma
     * pausa (ENTER) entre elas, para acompanhar o fluxo Léxica -> Sintáctica ->
     * Semântica. Se a entrada não for interactiva (pipe/redireccionamento), as
     * pausas são ignoradas e tudo é impresso de seguida.
     */
    public void imprimirTudo(String fonte, List<Token> tokens, Programa programa,
                             Collection<Simbolo> simbolos, List<Erro> erros) {
        imprimirCabecalho();
        imprimirCodigoFonte(fonte);

        pausa();
        System.out.println();
        System.out.println(">>> FASE 1 — ANÁLISE LÉXICA (código -> tokens)");
        imprimirTabelaLexemas(tokens);

        pausa();
        System.out.println();
        System.out.println(">>> FASE 2 — ANÁLISE SINTÁCTICA (tokens -> árvore sintáctica)");
        imprimirArvore(programa);

        pausa();
        System.out.println();
        System.out.println(">>> FASE 3 — ANÁLISE SEMÂNTICA (árvore + tabela de símbolos -> validação)");
        imprimirTabelaSimbolos(simbolos);
        imprimirErros(erros);
    }

    // ponytail: Scanner partilhado; System.console()==null => não interactivo, sem pausas
    private final Scanner teclado = new Scanner(System.in);

    private void pausa() {
        if (System.console() == null) {
            return;
        }
        System.out.println();
        System.out.print("--- Prima ENTER para ver a fase seguinte ---");
        teclado.nextLine();
    }

    // ----------------------------------------------------------------------
    private void imprimirCabecalho() {
        System.out.println("============================================================");
        System.out.println(" ANALISADOR DA ESTRUTURA SWITCH DE JAVA  -  Compiladores G4 ");
        System.out.println("============================================================");
    }

    // (a) Código-fonte numerado -------------------------------------------
    private void imprimirCodigoFonte(String fonte) {
        System.out.println();
        System.out.println("== (a) CÓDIGO-FONTE ANALISADO ==");
        String[] linhas = fonte.replace("\r", "").split("\n", -1);
        // Remove uma eventual linha vazia final (ficheiro terminado em \n).
        int total = linhas.length;
        if (total > 0 && linhas[total - 1].isEmpty()) {
            total--;
        }
        int largNum = String.valueOf(Math.max(total, 1)).length();
        for (int i = 0; i < total; i++) {
            String num = alinharDireita(String.valueOf(i + 1), largNum);
            System.out.println(num + " | " + linhas[i]);
        }
    }

    // (b) Tabela de lexemas ------------------------------------------------
    private void imprimirTabelaLexemas(List<Token> tokens) {
        List<String[]> linhas = new ArrayList<>();
        for (Token t : tokens) {
            linhas.add(new String[]{
                    t.lexema,
                    classificacaoLexema(t),
                    String.valueOf(t.linha)
            });
        }
        imprimirTabela("== (b) TABELA DE LEXEMAS ==",
                new String[]{"TOKEN", "CLASSIFICACAO", "LINHA"},
                linhas);
    }

    private String classificacaoLexema(Token token) {
        switch (token.classe) {
            case PALAVRA_RESERVADA:
                if ("int".equals(token.lexema)) {
                    return "Tipo inteiro";
                }
                if ("String".equals(token.lexema) || "string".equals(token.lexema)) {
                    return "Tipo objecto";
                }
                return "Palavra reservada";
            case IDENTIFICADOR:
                return "Identificador";
            case LITERAL_INT:
                return "Literal inteiro";
            case LITERAL_STRING:
                return "Literal string";
            case OP_ATRIBUICAO:
                return "Operador de atribuicao";
            case DELIMITADOR:
            case PONTUACAO:
                return "Simbolo especial";
            default:
                return token.classe.name();
        }
    }

    // Árvore sintáctica (AST) ----------------------------------------------
    // Imprime a árvore com ramos ASCII (├── └──), no estilo do comando 'tree',
    // para se ver de imediato a hierarquia: cadeia = irmãos ao nível de
    // PROGRAMA; aninhamento = um SWITCH como filho de um CASE.
    private void imprimirArvore(Programa programa) {
        System.out.println();
        System.out.println("== ÁRVORE SINTÁCTICA (AST) ==");
        System.out.println("PROGRAMA");

        List<Object> filhos = new ArrayList<>();
        filhos.addAll(programa.declaracoes);
        filhos.addAll(programa.switches);
        for (int i = 0; i < filhos.size(); i++) {
            imprimirNo(filhos.get(i), "", i == filhos.size() - 1);
        }
    }

    /** Imprime um nó e, recursivamente, os seus filhos. */
    private void imprimirNo(Object no, String prefixo, boolean ultimo) {
        String ramo = ultimo ? "└── " : "├── ";
        String prefixoFilhos = prefixo + (ultimo ? "    " : "│   ");

        if (no instanceof Declaracao) {
            Declaracao d = (Declaracao) no;
            String init = (d.valorLexema != null) ? " = " + d.valorLexema : "";
            System.out.println(prefixo + ramo + "DECLARACAO [" + d.categoria.descricao()
                    + "] " + d.tipo + " " + d.identificador + init + "   (linha " + d.linha + ")");

        } else if (no instanceof ComandoSwitch) {
            ComandoSwitch s = (ComandoSwitch) no;
            System.out.println(prefixo + ramo + "SWITCH (" + s.selector + ")   (linha " + s.linha + ")");
            List<Caso> casos = new ArrayList<>(s.casos);
            if (s.casoDefault != null) {
                casos.add(s.casoDefault);
            }
            for (int i = 0; i < casos.size(); i++) {
                imprimirNo(casos.get(i), prefixoFilhos, i == casos.size() - 1);
            }

        } else if (no instanceof Caso) {
            Caso c = (Caso) no;
            String rotulo = c.ehDefault ? "DEFAULT" : "CASE " + c.rotuloLexema;
            System.out.println(prefixo + ramo + rotulo + "   (linha " + c.linha + ")");
            for (int i = 0; i < c.instrucoes.size(); i++) {
                imprimirNo(c.instrucoes.get(i), prefixoFilhos, i == c.instrucoes.size() - 1);
            }

        } else if (no instanceof Atribuicao) {
            Atribuicao a = (Atribuicao) no;
            System.out.println(prefixo + ramo + "ATRIBUICAO " + a.destino + " = "
                    + a.valorLexema + "   (linha " + a.linha + ")");

        } else if (no instanceof ComandoBreak) {
            Instrucao b = (ComandoBreak) no;
            System.out.println(prefixo + ramo + "BREAK   (linha " + b.linha + ")");
        }
    }

    // (c) Tabela de símbolos ----------------------------------------------
    private void imprimirTabelaSimbolos(Collection<Simbolo> simbolos) {
        if (simbolos.isEmpty()) {
            System.out.println();
            System.out.println("== (c) TABELA DE SÍMBOLOS ==");
            System.out.println("(sem identificadores declarados)");
            return;
        }
        List<String[]> linhas = new ArrayList<>();
        for (Simbolo s : simbolos) {
            linhas.add(new String[]{
                    s.identificador,
                    s.categoria.descricao(),
                    s.tipo,
                    estadoMemoria(s.tipo),
                    "0",
                    "--------",
                    "--------",
                    "--------",
                    s.valor,
                    "--------",
                    "--------"
            });
        }
        imprimirTabela("== (c) TABELA DE SÍMBOLOS ==",
                new String[]{
                        "IDENTIFICADOR",
                        "CATEGORIA",
                        "TIPO",
                        "EST.MEM.",
                        "NIVEL",
                        "NR. PARAM",
                        "SEQ.PARAM",
                        "F.PASSAGEM",
                        "VALOR",
                        "DIMENSAO",
                        "REF"
                },
                linhas);
    }

    private String estadoMemoria(String tipo) {
        if ("int".equals(tipo)) {
            return "primitiva";
        }
        if ("String".equals(tipo) || "string".equals(tipo)) {
            return "objecto";
        }
        return "--------";
    }

    // (d) Lista de erros ---------------------------------------------------
    private void imprimirErros(List<Erro> erros) {
        if (erros.isEmpty()) {
            System.out.println();
            System.out.println("== (d) LISTA DE ERROS ==");
            System.out.println("Análise concluída sem erros.");
            return;
        }
        // Ordena por linha (ordenação estável: dentro da mesma linha mantém-se
        // a ordem de detecção léxica -> sintáctica -> semântica).
        List<Erro> ordenados = new ArrayList<>(erros);
        ordenados.sort((a, b) -> Integer.compare(a.linha, b.linha));

        List<String[]> linhas = new ArrayList<>();
        int i = 1;
        for (Erro e : ordenados) {
            linhas.add(new String[]{
                    String.valueOf(i++),
                    e.fase.descricao(),
                    e.descricao,
                    String.valueOf(e.linha)
            });
        }
        imprimirTabela("== (d) LISTA DE ERROS ==",
                new String[]{"#", "Fase", "Descrição", "Linha"},
                linhas);
    }

    // ----------------------------------------------------------------------
    // Impressão genérica de tabelas em ASCII
    // ----------------------------------------------------------------------
    private void imprimirTabela(String titulo, String[] cabecalho, List<String[]> linhas) {
        int n = cabecalho.length;
        int[] largura = new int[n];
        for (int i = 0; i < n; i++) {
            largura[i] = cabecalho[i].length();
        }
        for (String[] linha : linhas) {
            for (int i = 0; i < n; i++) {
                int len = (linha[i] == null) ? 0 : linha[i].length();
                if (len > largura[i]) {
                    largura[i] = len;
                }
            }
        }

        String separador = construirSeparador(largura);
        System.out.println();
        System.out.println(titulo);
        System.out.println(separador);
        System.out.println(construirLinha(cabecalho, largura));
        System.out.println(separador);
        for (String[] linha : linhas) {
            System.out.println(construirLinha(linha, largura));
        }
        System.out.println(separador);
    }

    private String construirSeparador(int[] largura) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : largura) {
            for (int i = 0; i < w + 2; i++) {
                sb.append('-');
            }
            sb.append('+');
        }
        return sb.toString();
    }

    private String construirLinha(String[] celulas, int[] largura) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < largura.length; i++) {
            String c = (celulas[i] == null) ? "" : celulas[i];
            sb.append(' ').append(c);
            for (int k = c.length(); k < largura[i]; k++) {
                sb.append(' ');
            }
            sb.append(' ').append('|');
        }
        return sb.toString();
    }

    private String alinharDireita(String s, int largura) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < largura; i++) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }
}
