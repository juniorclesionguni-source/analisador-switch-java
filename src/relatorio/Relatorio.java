package relatorio;

import erros.Erro;
import lexico.Token;
import tabelas.Simbolo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public void imprimirTudo(String fonte, List<Token> tokens,
                             Collection<Simbolo> simbolos, List<Erro> erros) {
        imprimirCabecalho();
        imprimirCodigoFonte(fonte);
        imprimirTabelaLexemas(tokens);
        imprimirTabelaSimbolos(simbolos);
        imprimirErros(erros);
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
