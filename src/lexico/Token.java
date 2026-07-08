package lexico;

/**
 * Unidade léxica produzida pelo Lexer.
 *
 * O quê: guarda o lexema (texto), a sua classe, e a posição (linha, coluna).
 * Porquê: a linha/coluna são necessárias para as mensagens de erro e para a
 *         tabela de lexemas (Secção 7-b).
 */
public class Token {

    public final ClasseToken classe;
    public final String lexema;
    public final int linha;
    public final int coluna;

    public Token(ClasseToken classe, String lexema, int linha, int coluna) {
        this.classe = classe;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    /** Verdadeiro se o texto do token for igual a {@code s}. */
    public boolean ehLexema(String s) {
        return lexema.equals(s);
    }

    /** Verdadeiro se o token pertencer à classe {@code c}. */
    public boolean ehClasse(ClasseToken c) {
        return classe == c;
    }

    @Override
    public String toString() {
        return "'" + lexema + "' (" + classe + ") @ " + linha + ":" + coluna;
    }
}
