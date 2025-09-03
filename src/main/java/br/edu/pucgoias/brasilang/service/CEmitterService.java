package br.edu.pucgoias.brasilang.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.lex.Token;
import br.edu.pucgoias.brasilang.model.lex.EnumTokenType;

@Service
public final class CEmitterService {

    /** Entrada única: lista de tokens do programa inteiro. Saída: código-fonte C. */
    public String tokensParaC(List<Token> tokenList) {
        int[] pos = {0};

        StringBuilder headers = new StringBuilder()
                .append("#include <stdio.h>\n")
                .append("#include <math.h>\n\n");

        StringBuilder globals = new StringBuilder();
        StringBuilder main = new StringBuilder();
        boolean entrouMain = false;

        while (!check(tokenList, pos, EnumTokenType.EOF)) {
            if (match(tokenList, pos, EnumTokenType.SEMI)) continue; // ignora ';' solto

            // Enquanto não entrou no main, aceite apenas decls globais com tipo
            if (!entrouMain && isTypeToken(look(tokenList, pos).type)) {
                globals.append(emitVarDecl(tokenList, pos, 0));
                continue;
            }

            // A partir do primeiro statement não-tipado, abrimos a main
            entrouMain = true;
            main.append(emitStatement(tokenList, pos, 1));
        }

        if (!entrouMain) {
            // se não houve nenhum statement, ainda assim gere um main vazio
            main.append(indent(1)).append("return 0;\n");
        }

        StringBuilder out = new StringBuilder();
        out.append(headers);
        out.append(globals);
        out.append("int main(void) {\n");
        out.append(main);
        out.append("}\n");
        return out.toString();
    }

    // ---------------- Statements ----------------

    private String emitStatement(List<Token> toks, int[] pos, int ind) {
        Token t = look(toks, pos);

        // if/while/for/return/bloco
        switch (t.type) {
            case SE:        return emitIf(toks, pos, ind);
            case ENQUANTO:  return emitWhile(toks, pos, ind);
            case PARA:      return emitFor(toks, pos, ind);
            case RETORNE:   return emitReturn(toks, pos, ind);
            case LBRACE:    return emitBlock(toks, pos, ind);
            default: break;
        }

        // declaração local: tipo ID (= expr)?;
        if (isTypeToken(t.type)) {
            return emitVarDecl(toks, pos, ind);
        }

        // caso especial: imprima(...)
        if (t.type == EnumTokenType.ID && "imprima".equals(t.lexeme) && peekIs(toks, pos, 1, EnumTokenType.LPAR)) {
            consume(toks, pos); // 'imprima'
            expect(toks, pos, EnumTokenType.LPAR, "'('");
            String arg = parseExprUntilToString(toks, pos, EnumTokenType.RPAR, false); // não consome ')'
            expect(toks, pos, EnumTokenType.RPAR, "')'");
            expect(toks, pos, EnumTokenType.SEMI, "';'");
            return indent(ind) + "printf(\"%d\\n\", (int)(" + arg + "));\n";
        }

        // atribuição: ID = expr;
        if (t.type == EnumTokenType.ID && peekIs(toks, pos, 1, EnumTokenType.ASSIGN)) {
            String name = consume(toks, pos).lexeme;
            expect(toks, pos, EnumTokenType.ASSIGN, "'='");
            String rhs = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true); // consome ';'
            return indent(ind) + name + " = " + rhs + ";\n";
        }

        // expressão terminada em ';'
        String e = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
        return indent(ind) + e + ";\n";
    }

    private String emitIf(List<Token> toks, int[] pos, int ind) {
        expect(toks, pos, EnumTokenType.SE, "'se'");
        expect(toks, pos, EnumTokenType.LPAR, "'('");
        String cond = parseExprUntilToString(toks, pos, EnumTokenType.RPAR, false);
        expect(toks, pos, EnumTokenType.RPAR, "')'");
        String thenB = emitBlock(toks, pos, ind);
        StringBuilder sb = new StringBuilder();
        sb.append(indent(ind)).append("if (").append(cond).append(") ").append(thenB);
        if (match(toks, pos, EnumTokenType.SENAO)) {
            String elseB = emitBlock(toks, pos, ind);
            sb.append(indent(ind)).append("else ").append(elseB);
        }
        return sb.toString();
    }

    private String emitWhile(List<Token> toks, int[] pos, int ind) {
        expect(toks, pos, EnumTokenType.ENQUANTO, "'enquanto'");
        expect(toks, pos, EnumTokenType.LPAR, "'('");
        String cond = parseExprUntilToString(toks, pos, EnumTokenType.RPAR, false);
        expect(toks, pos, EnumTokenType.RPAR, "')'");
        String body = emitBlock(toks, pos, ind);
        return indent(ind) + "while (" + cond + ") " + body;
    }

    private String emitFor(List<Token> toks, int[] pos, int ind) {
        expect(toks, pos, EnumTokenType.PARA, "'para'");
        expect(toks, pos, EnumTokenType.LPAR, "'('");

        // init
        String init;
        if (isTypeToken(look(toks, pos).type)) {
            init = stripSemicolon(emitVarDeclHeaderOnly(toks, pos)); // consome até ';'
        } else if (check(toks, pos, EnumTokenType.SEMI)) {
            consume(toks, pos);
            init = "";
        } else if (look(toks, pos).type == EnumTokenType.ID && peekIs(toks, pos, 1, EnumTokenType.ASSIGN)) {
            String name = consume(toks, pos).lexeme;
            expect(toks, pos, EnumTokenType.ASSIGN, "'='");
            String rhs = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
            init = name + " = " + rhs;
        } else {
            init = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
        }

        // cond
        String cond;
        if (check(toks, pos, EnumTokenType.SEMI)) {
            consume(toks, pos);
            cond = "1";
        } else {
            cond = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
        }

        String update;
        if (check(toks, pos, EnumTokenType.RPAR)) {
            consume(toks, pos);             // não há update -> consome ')'
            update = "";
        } else {
            update = parseExprUntilToString(toks, pos, EnumTokenType.RPAR, false); // NÃO consome ')'
            expect(toks, pos, EnumTokenType.RPAR, "')'"); // consome ')'
        }
   
        String body = emitBlock(toks, pos, ind);

        return indent(ind) + "for (" + init + "; " + cond + "; " + update + ") " + body;
    }

    private String emitReturn(List<Token> toks, int[] pos, int ind) {
        expect(toks, pos, EnumTokenType.RETORNE, "'retorne'");
        if (match(toks, pos, EnumTokenType.SEMI)) {
            return indent(ind) + "return;\n";
        }
        String v = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
        return indent(ind) + "return " + v + ";\n";
    }

    private String emitBlock(List<Token> toks, int[] pos, int ind) {
        expect(toks, pos, EnumTokenType.LBRACE, "'{'");
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        while (!check(toks, pos, EnumTokenType.RBRACE)) {
            sb.append(emitStatement(toks, pos, ind + 1));
        }
        expect(toks, pos, EnumTokenType.RBRACE, "'}'");
        sb.append(indent(ind)).append("}\n");
        return sb.toString();
    }

    // ---------------- Declarações ----------------

    private String emitVarDecl(List<Token> toks, int[] pos, int ind) {
        String cType = mapCType(consume(toks, pos));        // INT/FLOAT/DOUBLE
        String name  = expect(toks, pos, EnumTokenType.ID, "identificador").lexeme;
        String init = "";
        if (match(toks, pos, EnumTokenType.ASSIGN)) {
            String rhs = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
            init = " = " + rhs;
        } else {
            expect(toks, pos, EnumTokenType.SEMI, "';'");
        }
        return indent(ind) + cType + " " + name + init + ";\n";
    }

    /** Para cabeçalho do for: lê tipo/ID/(=expr)? até ';' e retorna sem newline. */
    private String emitVarDeclHeaderOnly(List<Token> toks, int[] pos) {
        String cType = mapCType(consume(toks, pos));
        String name  = expect(toks, pos, EnumTokenType.ID, "identificador").lexeme;
        String init = "";
        if (match(toks, pos, EnumTokenType.ASSIGN)) {
            String rhs = parseExprUntilToString(toks, pos, EnumTokenType.SEMI, true);
            init = " = " + rhs;
        } else {
            expect(toks, pos, EnumTokenType.SEMI, "';'");
        }
        return cType + " " + name + init + ";";
    }

    // ---------------- Expressões ----------------
    // Coleta tokens até 'terminator' em nível de parênteses 0 e converte o "slice" em String C.
    private String parseExprUntilToString(List<Token> toks, int[] pos, EnumTokenType terminator, boolean consumeTerminator) {
        List<Token> slice = new ArrayList<>();
        int i = pos[0];
        int par = 0;
        for (; i < toks.size(); i++) {
            Token tk = toks.get(i);
            if (tk.type == EnumTokenType.LPAR) par++;
            else if (tk.type == EnumTokenType.RPAR) par--;
            if (par == 0 && tk.type == terminator) break;
            if (!(par == 0 && tk.type == terminator)) slice.add(tk);
        }
        if (i >= toks.size()) {
            throw new RuntimeException("Esperado terminador " + terminator + " antes do EOF.\n" 
                + dumpAround(toks, pos, 6));
        }
        pos[0] = i;
        if (consumeTerminator) expect(toks, pos, terminator, "'" + terminator + "'");

        int[] p2 = {0};
        return parseEqualityToString(slice, p2);
    }
    
    private static String dumpAround(List<Token> toks, int[] pos, int radius) {
        StringBuilder sb = new StringBuilder();
        int from = Math.max(0, pos[0] - radius);
        int to   = Math.min(toks.size(), pos[0] + radius);
        sb.append("pos=").append(pos[0]).append(" | window [").append(from).append(',').append(to).append(")\n");
        for (int i = from; i < to; i++) {
            Token tk = toks.get(i);
            sb.append(i == pos[0] ? " -> " : "    ");
            sb.append(i).append(": ").append(tk.type).append("('").append(tk.lexeme)
              .append("')@").append(tk.line).append(':').append(tk.col).append('\n');
        }
        return sb.toString();
    }


    private String parseEqualityToString(List<Token> s, int[] p) {
        String left = parseRelationToString(s, p);
        while (check(s, p, EnumTokenType.EQ) || check(s, p, EnumTokenType.NEQ)) {
            String op = consume(s, p).lexeme;
            String right = parseRelationToString(s, p);
            left = "(" + left + " " + op + " " + right + ")";
        }
        return left;
    }

    private String parseRelationToString(List<Token> s, int[] p) {
        String left = parseAddToString(s, p);
        while (check(s, p, EnumTokenType.LT) || check(s, p, EnumTokenType.LE) ||
               check(s, p, EnumTokenType.GT) || check(s, p, EnumTokenType.GE)) {
            String op = consume(s, p).lexeme;
            String right = parseAddToString(s, p);
            left = "(" + left + " " + op + " " + right + ")";
        }
        return left;
    }

    private String parseAddToString(List<Token> s, int[] p) {
        String left = parseMulToString(s, p);
        while (check(s, p, EnumTokenType.PLUS) || check(s, p, EnumTokenType.MINUS)) {
            String op = consume(s, p).lexeme;
            String right = parseMulToString(s, p);
            left = "(" + left + " " + op + " " + right + ")";
        }
        return left;
    }

    private String parseMulToString(List<Token> s, int[] p) {
        String left = parseUnaryToString(s, p);
        while (check(s, p, EnumTokenType.STAR) || check(s, p, EnumTokenType.SLASH)) {
            String op = consume(s, p).lexeme;
            String right = parseUnaryToString(s, p);
            left = "(" + left + " " + op + " " + right + ")";
        }
        return left;
    }

    private String parseUnaryToString(List<Token> s, int[] p) {
        if (check(s, p, EnumTokenType.MINUS)) {
            String op = consume(s, p).lexeme;
            String right = parseUnaryToString(s, p);
            return "(" + op + right + ")";
        }
        return parsePrimaryToString(s, p);
    }

    private String parsePrimaryToString(List<Token> s, int[] p) {
        Token t = look(s, p);
        switch (t.type) {
            case INTLIT:    consume(s, p); return t.lexeme;
            case FLOATLIT:  consume(s, p); return t.lexeme;
            case STRINGLIT: consume(s, p); return "\"" + escape(t.lexeme) + "\"";
            case ID: {
                consume(s, p);
                if (match(s, p, EnumTokenType.LPAR)) {
                    // chamada genérica: nome(args...)
                    StringBuilder args = new StringBuilder();
                    if (!check(s, p, EnumTokenType.RPAR)) {
                        do {
                            if (args.length() > 0) args.append(", ");
                            args.append(parseEqualityToString(s, p));
                        } while (match(s, p, EnumTokenType.COMMA));
                    }
                    expect(s, p, EnumTokenType.RPAR, "')'");
                    return t.lexeme + "(" + args + ")";
                }
                return t.lexeme;
            }
            case LPAR: {
                consume(s, p);
                String inside = parseEqualityToString(s, p);
                expect(s, p, EnumTokenType.RPAR, "')'");
                return "(" + inside + ")";
            }
            default:
                throw new RuntimeException("Expressão esperada em " + t.line + ":" + t.col + " (encontrado " + t.type + ")");
        }
    }

    // ---------------- Utils ----------------

    private static boolean isTypeToken(EnumTokenType t) {
        return t == EnumTokenType.INT || t == EnumTokenType.FLOAT || t == EnumTokenType.DOUBLE;
    }

    private static String mapCType(Token tType) {
        return switch (tType.type) {
            case INT -> "int";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            default -> throw new IllegalArgumentException("Tipo não suportado: " + tType.type);
        };
    }

    private static String indent(int n) { return "    ".repeat(Math.max(0, n)); }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String stripSemicolon(String s) {
        int i = s.length() - 1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) i--;
        return (i >= 0 && s.charAt(i) == ';') ? s.substring(0, i) : s;
    }

    // navegação sobre tokens (mesmo padrão do seu SintaxeService)
    private static Token look(List<Token> toks, int[] pos) {
        int i = pos[0];
        return i < toks.size() ? toks.get(i) : toks.get(toks.size() - 1);
    }
    private static boolean check(List<Token> toks, int[] pos, EnumTokenType tt) {
        return look(toks, pos).type == tt;
    }
    private static boolean match(List<Token> toks, int[] pos, EnumTokenType tt) {
        if (check(toks, pos, tt)) { pos[0]++; return true; }
        return false;
    }
    private static boolean peekIs(List<Token> toks, int[] pos, int ahead, EnumTokenType tt) {
        int i = pos[0] + ahead;
        return i < toks.size() && toks.get(i).type == tt;
    }
    private static Token consume(List<Token> toks, int[] pos) {
        return toks.get(pos[0]++);
    }
    private static Token expect(List<Token> toks, int[] pos, EnumTokenType tt, String what) {
        Token t = look(toks, pos);
        if (t.type != tt) throw new RuntimeException("Esperado " + what + " (" + tt + ") em " + t.line + ":" + t.col + ", encontrado " + t.type);
        return consume(toks, pos);
    }
}
