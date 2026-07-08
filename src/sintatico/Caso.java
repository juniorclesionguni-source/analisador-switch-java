package sintatico;

import lexico.ClasseToken;

import java.util.List;

/**
 * CASO -> 'case' CONSTANTE ':' LISTA_INSTR
 * DEFAULT -> 'default' ':' LISTA_INSTR
 *
 * O quê: representa tanto um 'case' como o 'default' (distinguidos por
 *        {@code ehDefault}).
 * Porquê: o 'default' partilha a mesma estrutura de um 'case' (uma lista de
 *         instruções), apenas sem rótulo constante.
 */
public class Caso {

    public final boolean ehDefault;
    public final String rotuloLexema;       // null se for 'default'
    public final ClasseToken rotuloClasse;  // null se for 'default'
    public final int linha;
    public final List<Instrucao> instrucoes;

    public Caso(boolean ehDefault, String rotuloLexema, ClasseToken rotuloClasse,
                int linha, List<Instrucao> instrucoes) {
        this.ehDefault = ehDefault;
        this.rotuloLexema = rotuloLexema;
        this.rotuloClasse = rotuloClasse;
        this.linha = linha;
        this.instrucoes = instrucoes;
    }
}
