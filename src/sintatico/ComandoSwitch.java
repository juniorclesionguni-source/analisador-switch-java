package sintatico;

import java.util.ArrayList;
import java.util.List;

/**
 * SWITCH -> 'switch' '(' SELECTOR ')' '{' LISTA_CASO [ DEFAULT ] '}'
 *
 * O quê: representa uma estrutura switch completa.
 * Porquê: estende Instrucao para suportar ANINHAMENTO (um switch dentro de um
 *         case). As formas "simples", "em cadeia" e "aninhada" usam todas este
 *         mesmo nó — a cadeia é uma lista destes nós ao nível do programa, e o
 *         aninhamento é este nó dentro da lista de instruções de um Caso.
 */
public class ComandoSwitch extends Instrucao {

    public final String selector;       // identificador do selector (pode ser null se houve erro)
    public final int linhaSelector;
    public final List<Caso> casos = new ArrayList<>();
    public Caso casoDefault;            // opcional (null se não existir)

    public ComandoSwitch(String selector, int linhaSelector, int linha) {
        super(linha);
        this.selector = selector;
        this.linhaSelector = linhaSelector;
    }
}
