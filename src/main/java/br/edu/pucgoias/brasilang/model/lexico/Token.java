package br.edu.pucgoias.brasilang.model.lexico;

public class Token {
    public final EnumTokenType type;
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(EnumTokenType type, String lexeme, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    @Override public String toString() {
        return type + "('" + lexeme + "')@" + line + ":" + col;
    }
}
