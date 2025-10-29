package br.edu.pucgoias.brasilang.model.lexico;

public enum EnumTokenType {
    // keywords (com aliases pt/en)
    FUNCAO("funcao"),
    RETORNE("retorne"),
    SE("se"),
    SENAO("senao"),
    ENQUANTO("enquanto"),
    REPITA("repita"),
    PARA("para"),
    IMPRIMA("imprima"),
    INT("inteiro"),
    FLOAT("flutuante"),
    DOUBLE("duplo"),
    VOID("vazio"),

    // demais tokens
    ID, INTLIT, FLOATLIT, STRINGLIT,
    LPAR, RPAR, LBRACE, RBRACE, LBRACK, RBRACK, COLON, SEMI, COMMA,
    ASSIGN, PLUS, MINUS, STAR, SLASH,
    LT, LE, GT, GE, EQ, NEQ,
    EOF;

    private final String[] lexemes; // só para keywords

    EnumTokenType(String... lexemes) {
        this.lexemes = lexemes;
    }

    private static final java.util.Map<String, EnumTokenType> BY_LEXEME = new java.util.HashMap<>();
    static {
        for (EnumTokenType t : values()) {
            if (t.lexemes != null) {
                for (String s : t.lexemes) {
                    BY_LEXEME.put(s, t);
                }
            }
        }
    }

    /**
     * Resolve o tipo do token a partir do lexema se ele for uma palavra-chave; caso
     * contrário retorna null.
     */
    public static EnumTokenType resolve(String id) {
        return BY_LEXEME.get(id);
    }

    public boolean isKeyword() {
        return lexemes != null && lexemes.length > 0;
    }
}
