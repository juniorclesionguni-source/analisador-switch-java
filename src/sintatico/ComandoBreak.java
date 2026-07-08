package sintatico;

/**
 * BREAK -> 'break' ';'
 *
 * O quê: instrução de quebra; não tem dados associados além da linha.
 * Porquê: existe como nó próprio para que a lista de instruções de um 'case'
 *         seja homogénea (tudo é Instrucao) e para registar a sua posição.
 */
public class ComandoBreak extends Instrucao {

    public ComandoBreak(int linha) {
        super(linha);
    }
}
