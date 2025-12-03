package br.edu.pucgoias.brasilang.exception;

/**
 * Exceção lançada quando um erro lexical é detectado durante a análise léxica.
 */
public class LexicalException extends RuntimeException {

    private final int line;
    private final int col;

    public LexicalException(String message, int line, int col) {
        super(String.format("Erro léxico em %d:%d - %s", line, col, message));
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
