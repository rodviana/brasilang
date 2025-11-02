package br.edu.pucgoias.brasilang.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.lexico.Lexer;
import br.edu.pucgoias.brasilang.model.lexico.Token;

@Service
public final class LexerService {

    public List<Token> buildTokenList(Lexer lexer) {
        List<Token> tokenList = new ArrayList<>();
        Token currentToken;
        do {
            currentToken = next(lexer);
            tokenList.add(currentToken);
        } while (currentToken.type != EnumTokenType.EOF);
        return tokenList;
    }

    public Token next(Lexer l) {
        skipWhitespaceAndComments(l);

        int startLine = l.getLine();
        int startCol = l.getCol();

        if (isEndOfSource(l))
            return new Token(EnumTokenType.EOF, "", startLine, startCol);

        Token t;

        t = tryReadDoubleOperator(l, startLine, startCol);
        if (t != null)
            return t;

        t = tryReadSingleSymbolOrSimpleOperator(l, startLine, startCol);
        if (t != null)
            return t;

        t = tryReadStringLiteral(l, startLine, startCol);
        if (t != null)
            return t;

        t = tryReadCharLiteral(l, startLine, startCol);
        if (t != null)
            return t;

        t = tryReadNumber(l, startLine, startCol);
        if (t != null)
            return t;

        t = tryReadIdentifierOrKeyword(l, startLine, startCol);
        if (t != null)
            return t;

        char bad = preVisualizeNextCharacter(l, 0);

        throw error("Invalid character: '" + bad + "'", startLine, startCol);
    }

    // ===== core stream helpers =====
    private boolean isEndOfSource(Lexer l) {
        return l.getIndex() >= l.getSource().length();
    }

    private char preVisualizeNextCharacter(Lexer l, int ahead) {
        int pos = l.getIndex() + ahead;
        return pos < l.getSource().length() ? l.getSource().charAt(pos) : '\0';
    }

    private char consumeAndGetCurrentCharacter(Lexer l) {
        char c = preVisualizeNextCharacter(l, 0);
        l.setIndex(l.getIndex() + 1);
        if (c == '\n') {
            l.setLine(l.getLine() + 1);
            l.setCol(1);
        } else {
            l.setCol(l.getCol() + 1);
        }
        return c;
    }

    private void consumeCharacters(Lexer l, int count) {
        for (int k = 0; k < count && !isEndOfSource(l); k++)
            consumeAndGetCurrentCharacter(l);
    }

    // ===== whitespace & comments =====
    private boolean nextIsCRLF(Lexer l) {
        return !isEndOfSource(l)
                && preVisualizeNextCharacter(l, 0) == '\r'
                && preVisualizeNextCharacter(l, 1) == '\n';
    }

    private boolean startsWith(Lexer l, String s) {
        if (isEndOfSource(l))
            return false;
        for (int k = 0; k < s.length(); k++) {
            if (preVisualizeNextCharacter(l, k) != s.charAt(k))
                return false;
        }
        return true;
    }

    private boolean skipSpaces(Lexer l) {
        boolean consumed = false;
        while (!isEndOfSource(l) && Character.isWhitespace(preVisualizeNextCharacter(l, 0))) {
            if (nextIsCRLF(l))
                consumeCharacters(l, 1); // eat only '\r'
            consumeCharacters(l, 1);
            consumed = true;
        }
        return consumed;
    }

    private boolean skipLineCommentIfPresent(Lexer l) {
        if (startsWith(l, "//")) {
            consumeCharacters(l, 2);
            while (!isEndOfSource(l) && preVisualizeNextCharacter(l, 0) != '\n')
                consumeCharacters(l, 1);
            return true;
        }
        return false;
    }

    private boolean skipBlockCommentIfPresent(Lexer l) {
        if (startsWith(l, "/*")) {
            consumeCharacters(l, 2);
            while (!isEndOfSource(l)
                    && !(preVisualizeNextCharacter(l, 0) == '*' && preVisualizeNextCharacter(l, 1) == '/')) {
                consumeCharacters(l, 1);
            }
            if (!isEndOfSource(l))
                consumeCharacters(l, 2); // "*/"
            return true;
        }
        return false;
    }

    private void skipWhitespaceAndComments(Lexer l) {
        boolean consumed;
        do {
            consumed = false;
            consumed |= skipSpaces(l);
            consumed |= skipLineCommentIfPresent(l);
            consumed |= skipBlockCommentIfPresent(l);
        } while (consumed);
    }

    // ===== scanners =====
    private Token tryReadDoubleOperator(Lexer l, int startLine, int startCol) {
        char c0 = preVisualizeNextCharacter(l, 0), c1 = preVisualizeNextCharacter(l, 1);
        if (c0 == '=' && c1 == '=') {
            consumeCharacters(l, 2);
            return t(EnumTokenType.EQ, "==", startLine, startCol);
        }
        if (c0 == '!' && c1 == '=') {
            consumeCharacters(l, 2);
            return t(EnumTokenType.NEQ, "!=", startLine, startCol);
        }
        if (c0 == '<' && c1 == '=') {
            consumeCharacters(l, 2);
            return t(EnumTokenType.LE, "<=", startLine, startCol);
        }
        if (c0 == '>' && c1 == '=') {
            consumeCharacters(l, 2);
            return t(EnumTokenType.GE, ">=", startLine, startCol);
        }
        return null;
    }

    private Token tryReadSingleSymbolOrSimpleOperator(Lexer l, int startLine, int startCol) {
        char c0 = preVisualizeNextCharacter(l, 0);
        switch (c0) {
            case '(':
                consumeCharacters(l, 1);
                return t(EnumTokenType.LPAR, "(", startLine, startCol);
            case ')':
                consumeCharacters(l, 1);
                return t(EnumTokenType.RPAR, ")", startLine, startCol);
            case '{':
                consumeCharacters(l, 1);
                return t(EnumTokenType.LBRACE, "{", startLine, startCol);
            case '}':
                consumeCharacters(l, 1);
                return t(EnumTokenType.RBRACE, "}", startLine, startCol);
            case '[':
                consumeCharacters(l, 1);
                return t(EnumTokenType.LBRACK, "[", startLine, startCol);
            case ']':
                consumeCharacters(l, 1);
                return t(EnumTokenType.RBRACK, "]", startLine, startCol);
            case ':':
                consumeCharacters(l, 1);
                return t(EnumTokenType.COLON, ":", startLine, startCol);
            case ';':
                consumeCharacters(l, 1);
                return t(EnumTokenType.SEMI, ";", startLine, startCol);
            case ',':
                consumeCharacters(l, 1);
                return t(EnumTokenType.COMMA, ",", startLine, startCol);
            case '+':
                consumeCharacters(l, 1);
                return t(EnumTokenType.PLUS, "+", startLine, startCol);
            case '-':
                consumeCharacters(l, 1);
                return t(EnumTokenType.MINUS, "-", startLine, startCol);
            case '*':
                consumeCharacters(l, 1);
                return t(EnumTokenType.STAR, "*", startLine, startCol);
            case '/':
                consumeCharacters(l, 1);
                return t(EnumTokenType.SLASH, "/", startLine, startCol);
            case '=':
                consumeCharacters(l, 1);
                return t(EnumTokenType.ASSIGN, "=", startLine, startCol);
            case '<':
                consumeCharacters(l, 1);
                return t(EnumTokenType.LT, "<", startLine, startCol);
            case '>':
                consumeCharacters(l, 1);
                return t(EnumTokenType.GT, ">", startLine, startCol);
            default:
                return null;
        }
    }

    private Token tryReadNumber(Lexer l, int startLine, int startCol) {
        char c0 = preVisualizeNextCharacter(l, 0);
        char c1 = preVisualizeNextCharacter(l, 1);
        if (!(Character.isDigit(c0) || (c0 == '.' && Character.isDigit(c1))))
            return null;

        StringBuilder sb = new StringBuilder();
        boolean hasDot = false;

        while (true) {
            char look = preVisualizeNextCharacter(l, 0);
            if (Character.isDigit(look)) {
                sb.append(consumeAndGetCurrentCharacter(l));
                continue;
            }
            if (look == '.' && !hasDot && Character.isDigit(preVisualizeNextCharacter(l, 1))) {
                hasDot = true;
                sb.append(consumeAndGetCurrentCharacter(l));
                continue;
            }
            break;
        }
        String lex = sb.toString();
        return t(hasDot ? EnumTokenType.FLOATLIT : EnumTokenType.INTLIT, lex, startLine, startCol);
    }

    private Token tryReadIdentifierOrKeyword(Lexer l, int startLine, int startCol) {
        char c0 = preVisualizeNextCharacter(l, 0);

        if (!(Character.isLetter(c0) || c0 == '_'))
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(consumeAndGetCurrentCharacter(l));

        while (Character.isLetterOrDigit(preVisualizeNextCharacter(l, 0)) || preVisualizeNextCharacter(l, 0) == '_') {
            sb.append(consumeAndGetCurrentCharacter(l));
        }

        String lex = sb.toString();
        EnumTokenType kw = EnumTokenType.resolve(lex);

        return new Token(kw != null ? kw : EnumTokenType.ID, lex, startLine, startCol);
    }

    private Token tryReadStringLiteral(Lexer l, int startLine, int startCol) {
        if (preVisualizeNextCharacter(l, 0) != '"')
            return null;

        consumeCharacters(l, 1); // consume opening "
        StringBuilder sb = new StringBuilder();

        while (!isEndOfSource(l)) {
            char ch = preVisualizeNextCharacter(l, 0);

            if (ch == '"') { // closing quote
                consumeCharacters(l, 1);
                return t(EnumTokenType.STRINGLIT, sb.toString(), startLine, startCol);
            }

            if (ch == '\\') { // escape sequence
                consumeCharacters(l, 1);
                if (isEndOfSource(l))
                    break;
                char esc = preVisualizeNextCharacter(l, 0);
                consumeCharacters(l, 1);
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(esc); // keep unknown escapes as-is
                }
                continue;
            }

            // normal char
            sb.append(consumeAndGetCurrentCharacter(l));
        }

        throw error("Unterminated string literal", startLine, startCol);
    }

    private Token tryReadCharLiteral(Lexer l, int startLine, int startCol) {
        if (preVisualizeNextCharacter(l, 0) != '\'')
            return null;

        consumeCharacters(l, 1); // consume opening '
        if (isEndOfSource(l))
            throw error("Unterminated character literal", startLine, startCol);

        char ch = preVisualizeNextCharacter(l, 0);
        String literal;

        if (ch == '\\') { // escape sequence
            consumeCharacters(l, 1);
            if (isEndOfSource(l))
                throw error("Unterminated character literal", startLine, startCol);
            char esc = preVisualizeNextCharacter(l, 0);
            consumeCharacters(l, 1);
            literal = "\\" + esc; // Keep it as a string for C translation
        } else {
            consumeCharacters(l, 1);
            literal = String.valueOf(ch);
        }

        if (isEndOfSource(l) || preVisualizeNextCharacter(l, 0) != '\'') {
            throw error("Unterminated character literal, expected a closing '", startLine, startCol);
        }

        consumeCharacters(l, 1); // consume closing '

        return t(EnumTokenType.CHARLIT, literal, startLine, startCol);
    }

    private static Token t(EnumTokenType tt, String lex, int l, int c) {
        return new Token(tt, lex, l, c);
    }

    private RuntimeException error(String msg, int l, int c) {
        return new RuntimeException("Lexical error at " + l + ":" + c + " - " + msg);
    }
}
