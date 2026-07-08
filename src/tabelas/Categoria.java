package tabelas;

/**
 * Categoria de um identificador na tabela de símbolos (Secção 3).
 *
 * O quê: distingue uma variável de uma constante.
 * Porquê: uma CONSTANTE (declarada com 'final') não pode receber atribuições —
 *         a verificação semântica usa esta informação.
 */
public enum Categoria {
    VARIAVEL,
    CONSTANTE;

    public String descricao() {
        return (this == VARIAVEL) ? "variável" : "constante";
    }
}
