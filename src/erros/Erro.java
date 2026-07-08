package erros;

/**
 * Representa um único erro encontrado durante a análise.
 *
 * O quê: guarda a fase, a descrição (em português) e a linha do código-fonte.
 * Porquê: o requisito (Secção 2) é não abortar no primeiro erro, mas sim
 *         acumular todos os erros numa estrutura comum e mostrá-los no fim.
 */
public class Erro {

    public final Fase fase;
    public final String descricao;
    public final int linha;

    public Erro(Fase fase, String descricao, int linha) {
        this.fase = fase;
        this.descricao = descricao;
        this.linha = linha;
    }

    @Override
    public String toString() {
        return "[" + fase.descricao() + "] linha " + linha + ": " + descricao;
    }
}
