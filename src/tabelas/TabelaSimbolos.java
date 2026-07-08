package tabelas;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabela de Símbolos (módulo da Secção 9).
 *
 * O quê: estrutura que associa cada identificador ao seu Simbolo.
 * Porquê: é a memória partilhada entre a fase de declarações e a fase de
 *         verificação semântica. Usa LinkedHashMap para preservar a ordem de
 *         declaração na impressão da tabela (Secção 7-c).
 */
public class TabelaSimbolos {

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    /**
     * Insere um símbolo.
     * @return false se o identificador já existia (declaração duplicada),
     *         mantendo a primeira declaração; true caso contrário.
     */
    public boolean inserir(Simbolo s) {
        if (simbolos.containsKey(s.identificador)) {
            return false;
        }
        simbolos.put(s.identificador, s);
        return true;
    }

    /** Procura um símbolo pelo nome; devolve null se não existir. */
    public Simbolo procurar(String identificador) {
        return simbolos.get(identificador);
    }

    public boolean existe(String identificador) {
        return simbolos.containsKey(identificador);
    }

    /** Todos os símbolos, por ordem de declaração. */
    public Collection<Simbolo> todos() {
        return simbolos.values();
    }
}
