package sintatico;

import lexico.ClasseToken;
import tabelas.Categoria;

/**
 * DECLARACAO -> TIPO IDENT [ '=' VALOR ]            (variável)
 *             | 'final' TIPO IDENT '=' VALOR        (constante)
 *
 * O quê: descreve uma declaração ainda não validada.
 * Porquê: o parser apenas reconhece a forma sintáctica e guarda os dados; é a
 *         fase semântica que insere o símbolo na tabela, detecta duplicados e
 *         valida o tipo do inicializador.
 */
public class Declaracao {

    public final Categoria categoria;
    public final String tipo;             // "int" | "string" | "?" (se erro)
    public final String identificador;    // null se houve erro
    public final String valorLexema;      // null se sem inicializador
    public final ClasseToken valorClasse; // null se sem inicializador
    public final int linha;

    public Declaracao(Categoria categoria, String tipo, String identificador,
                      String valorLexema, ClasseToken valorClasse, int linha) {
        this.categoria = categoria;
        this.tipo = tipo;
        this.identificador = identificador;
        this.valorLexema = valorLexema;
        this.valorClasse = valorClasse;
        this.linha = linha;
    }
}
