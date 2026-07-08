package erros;

import java.util.ArrayList;
import java.util.List;

/**
 * Colector central de erros (módulo "erros" da Secção 9).
 *
 * O quê: mantém UMA lista única onde todas as fases (léxica, sintáctica e
 *        semântica) registam os seus erros.
 * Porquê: permite a recuperação e continuação da análise — cada fase chama o
 *         método adequado em vez de lançar excepções e abortar.
 */
public class ColetorErros {

    private final List<Erro> erros = new ArrayList<>();

    public void adicionarLexico(String descricao, int linha) {
        erros.add(new Erro(Fase.LEXICA, descricao, linha));
    }

    public void adicionarSintatico(String descricao, int linha) {
        erros.add(new Erro(Fase.SINTATICA, descricao, linha));
    }

    public void adicionarSemantico(String descricao, int linha) {
        erros.add(new Erro(Fase.SEMANTICA, descricao, linha));
    }

    /** Lista de todos os erros, na ordem em que foram detectados. */
    public List<Erro> lista() {
        return erros;
    }

    public boolean vazio() {
        return erros.isEmpty();
    }

    public int quantidade() {
        return erros.size();
    }
}
