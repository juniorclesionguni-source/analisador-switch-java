package sintatico;

import lexico.ClasseToken;

/**
 * ATRIBUICAO -> IDENT '=' VALOR ';'
 *
 * O quê: guarda o identificador de destino e o valor atribuído.
 * Porquê: a classe do valor (LITERAL_INT, LITERAL_STRING ou IDENTIFICADOR)
 *         é necessária para a verificação semântica de compatibilidade de
 *         tipos (Secção 6.3, ponto 4).
 */
public class Atribuicao extends Instrucao {

    public final String destino;        // identificador do lado esquerdo
    public final String valorLexema;    // texto do valor (literal ou ident)
    public final ClasseToken valorClasse; // classe léxica do valor

    public Atribuicao(String destino, String valorLexema, ClasseToken valorClasse, int linha) {
        super(linha);
        this.destino = destino;
        this.valorLexema = valorLexema;
        this.valorClasse = valorClasse;
    }
}
