package lexico;

/**
 * Classes léxicas (categorias de token) reconhecidas — Tabela da Secção 5.
 *
 * O quê: cada token produzido pelo Lexer pertence a uma destas classes.
 * Porquê: o parser decide com base na classe (e às vezes no lexema) qual a
 *         regra da gramática a aplicar.
 */
public enum ClasseToken {
    PALAVRA_RESERVADA, // switch, case, default, break, int, string, final
    IDENTIFICADOR,     // letra seguida de letras/dígitos
    LITERAL_INT,       // sequência de dígitos (com sinal opcional)
    LITERAL_STRING,    // texto entre aspas "..."
    OP_ATRIBUICAO,     // =
    DELIMITADOR,       // ( ) { }
    PONTUACAO,         // : ;

    EOF,               // sentinela de fim de ficheiro (uso interno do parser)
    INVALIDO           // resultado de um erro léxico (não usado na tabela final)
}
