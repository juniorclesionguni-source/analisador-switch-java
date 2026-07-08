package sintatico;

import java.util.ArrayList;
import java.util.List;

/**
 * PROGRAMA -> LISTA_DECL LISTA_SWITCH
 *
 * O quê: raiz da árvore sintáctica (AST). Agrupa todas as declarações e todos
 *        os switches de topo (a "cadeia").
 * Porquê: é a estrutura que o parser devolve e que o analisador semântico
 *         percorre.
 */
public class Programa {

    public final List<Declaracao> declaracoes = new ArrayList<>();
    public final List<ComandoSwitch> switches = new ArrayList<>();
}
