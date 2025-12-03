package br.edu.pucgoias.brasilang.exception;

/**
 * Exceção lançada quando um erro semântico é detectado durante a análise semântica.
 */
public class SemanticException extends RuntimeException {

    private final int line;
    private final int col;

    public SemanticException(String message) {
        super("Erro semântico: " + message);
        this.line = -1;
        this.col = -1;
    }

    public SemanticException(String message, int line, int col) {
        super(String.format("Erro semântico em %d:%d - %s", line, col, message));
        this.line = line;
        this.col = col;
    }

    public int getLine() {
        return line;
    }

    public int getCol() {
        return col;
    }
}
