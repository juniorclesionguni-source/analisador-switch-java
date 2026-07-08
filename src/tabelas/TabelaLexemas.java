package tabelas;

import lexico.ClasseToken;
import lexico.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabela de Lexemas (módulo da Secção 9).
 *
 * O quê: regista os tokens reconhecidos pelo Lexer para apresentação (Secção 7-b).
 * Porquê: separa a recolha de tokens "para mostrar" do token sentinela EOF,
 *         que é apenas um auxiliar interno do parser e não deve aparecer na
 *         tabela final.
 */
public class TabelaLexemas {

    private final List<Token> tokens = new ArrayList<>();

    public TabelaLexemas(List<Token> todosOsTokens) {
        for (Token t : todosOsTokens) {
            if (t.classe != ClasseToken.EOF) {
                tokens.add(t);
            }
        }
    }

    public List<Token> tokens() {
        return tokens;
    }
}
