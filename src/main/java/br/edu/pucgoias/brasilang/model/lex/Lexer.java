package br.edu.pucgoias.brasilang.model.lex;

/** Lexer is a pure state holder; processing happens in LexerService. */
public final class Lexer {
    private final String src;
    private final KeywordResolver resolver;

    private int index = 0;
    private int line  = 1;
    private int col   = 1;

    public Lexer(String src) {
        this.src = src;
        this.resolver = new KeywordResolver();
    }


    // --- accessors for service ---
    public String getSource() { return src; }
    public KeywordResolver getResolver() { return resolver; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    public int getLine() { return line; }
    public void setLine(int line) { this.line = line; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }
}
