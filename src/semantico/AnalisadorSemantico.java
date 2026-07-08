package semantico;

import erros.ColetorErros;
import lexico.ClasseToken;
import sintatico.Atribuicao;
import sintatico.Caso;
import sintatico.ComandoSwitch;
import sintatico.Declaracao;
import sintatico.Instrucao;
import sintatico.Programa;
import tabelas.Categoria;
import tabelas.Simbolo;
import tabelas.TabelaSimbolos;

import java.util.HashSet;
import java.util.Set;

/**
 * Analisador Semântico (Secção 6.3).
 *
 * O quê:   percorre a AST e, usando a tabela de símbolos, verifica as regras
 *          de significado da linguagem.
 * Entrada: a AST (Programa) e uma tabela de símbolos vazia.
 * Saída:   a tabela de símbolos preenchida + erros semânticos no colector.
 *
 * Verificações implementadas:
 *   1. Identificador não declarado (selector, destino ou origem de atribuição).
 *   2. Tipo do selector tem de ser int ou string.
 *   3. Compatibilidade selector <-> case (int->LIT_INT, string->LIT_STRING).
 *   4. Compatibilidade de tipos na atribuição (e na inicialização).
 *   5. Atribuição a constante ('final') -> erro.
 *   6. Declaração duplicada do mesmo identificador.
 *   7. (Opcional) 'case' duplicado dentro do mesmo switch.
 */
public class AnalisadorSemantico {

    private final TabelaSimbolos tabela;
    private final ColetorErros erros;

    public AnalisadorSemantico(TabelaSimbolos tabela, ColetorErros erros) {
        this.tabela = tabela;
        this.erros = erros;
    }

    public void analisar(Programa programa) {
        // Fase 1: processar declarações -> preencher a tabela de símbolos.
        for (Declaracao d : programa.declaracoes) {
            processarDeclaracao(d);
        }
        // Fase 2: verificar os switches (e o seu aninhamento).
        for (ComandoSwitch s : programa.switches) {
            verificarSwitch(s);
        }
    }

    // ----------------------------------------------------------------------
    // Declarações
    // ----------------------------------------------------------------------
    private void processarDeclaracao(Declaracao d) {
        if (d.identificador == null) {
            return; // erro sintáctico já reportado; nada a inserir
        }

        // (4) Verifica o tipo do inicializador, se existir.
        if (d.valorClasse != null) {
            String tipoValor = tipoDoValor(d.valorLexema, d.valorClasse, d.linha);
            if (tipoValor != null && !tipoValor.equals(d.tipo)) {
                erros.adicionarSemantico(
                        "Tipo incompatível na inicialização de '" + d.identificador
                                + "': a variável é " + d.tipo + " mas o valor é " + tipoValor,
                        d.linha);
            }
        }

        // (6) Insere; se já existir, é declaração duplicada.
        Simbolo simbolo = new Simbolo(
                d.identificador, d.categoria, d.tipo,
                (d.valorLexema != null) ? d.valorLexema : "-", d.linha);
        if (!tabela.inserir(simbolo)) {
            erros.adicionarSemantico(
                    "Declaração duplicada do identificador '" + d.identificador + "'", d.linha);
        }
    }

    /**
     * Determina o tipo de um VALOR.
     * @return "int" / "string" ou null se for indeterminável (ex.: identificador
     *         não declarado — caso em que regista o respectivo erro).
     */
    private String tipoDoValor(String lexema, ClasseToken classe, int linha) {
        if (classe == ClasseToken.LITERAL_INT) {
            return "int";
        }
        if (classe == ClasseToken.LITERAL_STRING) {
            return "String";
        }
        if (classe == ClasseToken.IDENTIFICADOR) {
            Simbolo s = tabela.procurar(lexema);
            if (s == null) {
                erros.adicionarSemantico("Identificador não declarado: '" + lexema + "'", linha);
                return null;
            }
            return s.tipo;
        }
        return null;
    }

    // ----------------------------------------------------------------------
    // Switch (recursivo, para suportar aninhamento)
    // ----------------------------------------------------------------------
    private void verificarSwitch(ComandoSwitch s) {
        String tipoSelector = null;

        // (1) e (2) selector declarado e de tipo válido.
        if (s.selector != null) {
            Simbolo sel = tabela.procurar(s.selector);
            if (sel == null) {
                erros.adicionarSemantico(
                        "Identificador não declarado: selector '" + s.selector + "'",
                        s.linhaSelector);
            } else {
                tipoSelector = sel.tipo;
                if (!tipoSelector.equals("int") && !tipoSelector.equals("String")) {
                    erros.adicionarSemantico(
                            "Tipo do selector '" + s.selector + "' inválido (" + tipoSelector
                                    + "); só são permitidos int ou String",
                            s.linhaSelector);
                }
            }
        }

        // (3) e (7) percorrer os 'case'.
        Set<String> rotulosVistos = new HashSet<>();
        for (Caso c : s.casos) {
            if (c.rotuloLexema != null) {
                // (7) case duplicado
                if (!rotulosVistos.add(c.rotuloLexema)) {
                    erros.adicionarSemantico(
                            "'case' duplicado no mesmo switch: " + c.rotuloLexema, c.linha);
                }
                // (3) compatibilidade selector <-> case
                if (tipoSelector != null) {
                    if (tipoSelector.equals("int") && c.rotuloClasse != ClasseToken.LITERAL_INT) {
                        erros.adicionarSemantico(
                                "Incompatibilidade selector<->case: o selector é int mas o case "
                                        + c.rotuloLexema + " é String",
                                c.linha);
                    } else if (tipoSelector.equals("String") && c.rotuloClasse != ClasseToken.LITERAL_STRING) {
                        erros.adicionarSemantico(
                                "Incompatibilidade selector<->case: o selector é String mas o case "
                                        + c.rotuloLexema + " é int",
                                c.linha);
                    }
                }
            }
            for (Instrucao i : c.instrucoes) {
                verificarInstrucao(i);
            }
        }

        if (s.casoDefault != null) {
            for (Instrucao i : s.casoDefault.instrucoes) {
                verificarInstrucao(i);
            }
        }
    }

    private void verificarInstrucao(Instrucao i) {
        if (i instanceof ComandoSwitch) {
            verificarSwitch((ComandoSwitch) i);     // aninhamento
        } else if (i instanceof Atribuicao) {
            verificarAtribuicao((Atribuicao) i);
        }
        // ComandoBreak: nada a verificar semanticamente.
    }

    private void verificarAtribuicao(Atribuicao a) {
        String tipoDestino = null;

        // (1) destino declarado? (5) é constante?
        Simbolo destino = tabela.procurar(a.destino);
        if (destino == null) {
            erros.adicionarSemantico("Identificador não declarado: '" + a.destino + "'", a.linha);
        } else {
            tipoDestino = destino.tipo;
            if (destino.categoria == Categoria.CONSTANTE) {
                erros.adicionarSemantico(
                        "Atribuição a constante não permitida: '" + a.destino
                                + "' foi declarada com 'final'",
                        a.linha);
            }
        }

        // (4) compatibilidade de tipos.
        String tipoValor = tipoDoValor(a.valorLexema, a.valorClasse, a.linha);
        if (tipoDestino != null && tipoValor != null && !tipoDestino.equals(tipoValor)) {
            erros.adicionarSemantico(
                    "Tipo incompatível na atribuição a '" + a.destino + "': o destino é "
                            + tipoDestino + " mas o valor é " + tipoValor,
                    a.linha);
        }
    }
}
