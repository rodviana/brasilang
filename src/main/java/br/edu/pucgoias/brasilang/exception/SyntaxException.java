package br.edu.pucgoias.brasilang.exception;

import br.edu.pucgoias.brasilang.model.lexico.Token;

/**
 * Exceção lançada quando um erro sintático é detectado durante a análise sintática.
 */
public class SyntaxException extends RuntimeException {

    private final int line;
    private final int col;
    private final Token token;

    public SyntaxException(String message, Token token) {
        super(String.format("Erro sintático em %d:%d - %s (token: %s)", 
                token.line, token.col, message, token.type));
        this.line = token.line;
        this.col = token.col;
        this.token = token;
    }

    public SyntaxException(String message, int line, int col) {
        super(String.format("Erro sintático em %d:%d - %s", line, col, message));
        this.line = line;
        this.col = col;
        this.token = null;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }

    public Token getToken() {
        return token;
    }
}
