package br.edu.pucgoias.brasilang.model.error;

import br.edu.pucgoias.brasilang.model.lexico.Token;

/**
 * Exceção com mensagens mais didáticas para erros léxicos ou sintáticos.
 */
public class BrasilangException extends RuntimeException {

    public BrasilangException(String message) {
        super(message);
    }

    public static BrasilangException lexical(String message, int line, int col) {
        return new BrasilangException("Erro léxico em " + line + ":" + col + " - " + message);
    }

    public static BrasilangException syntactic(String message, Token found) {
        String where = found == null ? "final do arquivo" : (found.line + ":" + found.col);
        String lexeme = found == null ? "<EOF>" : "'" + found.lexeme + "'";
        return new BrasilangException("Erro de sintaxe em " + where + " perto de " + lexeme + " - " + message);
    }
}
