package tabelas;

/**
 * Entrada da tabela de símbolos: um identificador declarado.
 *
 * O quê: guarda nome, categoria (variável/constante), tipo (int/string),
 *        valor inicial e a linha onde foi declarado.
 * Porquê: a análise semântica consulta estes dados para validar selectores,
 *         atribuições e compatibilidade de tipos (Secção 6.3).
 */
public class Simbolo {

    public final String identificador;
    public final Categoria categoria;
    public final String tipo;   // "int" ou "string"
    public final String valor;  // valor inicial ou "-" se não inicializado
    public final int linhaDeclaracao;

    public Simbolo(String identificador, Categoria categoria, String tipo,
                   String valor, int linhaDeclaracao) {
        this.identificador = identificador;
        this.categoria = categoria;
        this.tipo = tipo;
        this.valor = valor;
        this.linhaDeclaracao = linhaDeclaracao;
    }
}
