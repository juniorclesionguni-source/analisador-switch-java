package sintatico;

/**
 * Nó base de uma INSTRUCAO (Secção 4 da gramática).
 *
 * O quê: superclasse comum a Atribuicao, ComandoBreak e ComandoSwitch.
 * Porquê: dentro de um 'case' (ou 'default') só são permitidas estas três
 *         instruções; tratá-las polimorficamente simplifica o parser e o
 *         analisador semântico. Note-se que ComandoSwitch é uma Instrucao —
 *         é assim que o aninhamento de switches é representado.
 */
public abstract class Instrucao {

    public final int linha;

    protected Instrucao(int linha) {
        this.linha = linha;
    }
}
