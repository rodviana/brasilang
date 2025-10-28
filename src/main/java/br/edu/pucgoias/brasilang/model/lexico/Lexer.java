package br.edu.pucgoias.brasilang.model.lexico;

public final class Lexer {
    private final String src;

    private int index = 0;
    private int line = 1;
    private int col = 1;

    public Lexer(String src) {
        this.src = src;
    }

    public String getSource() {
        return src;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    @Override
    public String toString() {
        return String.format("Lexer{src='%s', index=%d, line=%d, col=%d}", src, index, line, col);
    }
}
