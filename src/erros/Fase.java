package erros;

/**
 * Fase do compilador em que um erro foi detectado.
 *
 * O quê: enumera as três fases clássicas do front-end.
 * Porquê: a lista de erros é única (colector central), mas cada erro precisa
 *         de indicar em que fase ocorreu, para o relatório final (Secção 7-d).
 */
public enum Fase {
    LEXICA,
    SINTATICA,
    SEMANTICA;

    /** Texto em português para apresentar na tabela de erros. */
    public String descricao() {
        switch (this) {
            case LEXICA:    return "Léxica";
            case SINTATICA: return "Sintáctica";
            default:        return "Semântica";
        }
    }
}
