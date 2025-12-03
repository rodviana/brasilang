package br.edu.pucgoias.brasilang.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.lexico.Token;
import br.edu.pucgoias.brasilang.model.sintaxe.Sintaxe;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.ArrayAccess;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Literal;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Variable;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.BinaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.UnaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.FunctionCall;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Cast;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Assign;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.VariableDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Print;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ReturnStatement;
import br.edu.pucgoias.brasilang.exception.SyntaxException;

@Service
public class SintaxeService {

    public List<AbstractStatement> buildProgramStatementList(Sintaxe sintaxe) {
        List<AbstractStatement> program = new ArrayList<>();
        try {
            while (!this.isNextTokenEOF(sintaxe))
                program.add(this.getNextStatement(sintaxe));
        } catch (SyntaxException e) {
            throw e;
        } catch (Exception e) {
            program.forEach(statement -> System.out.println(statement.toString()));
            throw e;
        }

        return program;
    }

    private boolean isNextTokenEOF(Sintaxe sintaxe) {
        return sintaxe.previewNextToken() == null || EnumTokenType.EOF.equals(sintaxe.previewNextToken().type);
    }

    private AbstractStatement getNextStatement(Sintaxe sintaxe) {
        Token token = sintaxe.advanceToNextToken();

        switch (token.type) {
            case INT:
            case FLOAT:
            case DOUBLE:
            case BOOL:
            case CHAR:
            case STRING:
                return parseVariableDeclaration(sintaxe, token);
            case ID:
                return parseAssign(sintaxe, token);
            case SE:
                return parseConditionalStruct(sintaxe);
            case IMPRIMA:
                return parsePrint(sintaxe);
            case ENQUANTO:
            case PARA:
            case REPITA:
                return parseRepetitionStruct(sintaxe, token);
            case RETORNE:
                return parseReturnStatement(sintaxe);
            case FUNCAO:
                return parseFunctionDeclaration(sintaxe);
            default:
                throw new SyntaxException("Token inesperado", token);
        }
    }

    // Parse declaração de variável: inteiro g = 10; ou inteiro vetor[10];
    private VariableDeclaration parseVariableDeclaration(Sintaxe sintaxe, Token typeToken) {
        Token varName = sintaxe.advanceToNextToken(); // ID
        if (varName == null || varName.type != EnumTokenType.ID) {
            throw new SyntaxException("Esperado identificador após tipo", varName != null ? varName : typeToken);
        }
        List<AbstractExpression> dimensions = null;
        AbstractExpression initialization = null;

        if (sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
            dimensions = new ArrayList<>();
            while (sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                sintaxe.advanceToNextToken(); // Consome LBRACK
                dimensions.add(parseExpression(sintaxe));
                Token rbrack = sintaxe.advanceToNextToken();
                if (rbrack == null || rbrack.type != EnumTokenType.RBRACK) {
                    throw new SyntaxException("Esperado ']'", rbrack != null ? rbrack : varName);
                }
            }
        } else if (sintaxe.previewNextToken().type == EnumTokenType.ASSIGN) {
            sintaxe.advanceToNextToken(); // Consome ASSIGN
            initialization = parseExpression(sintaxe);
        }

        Token semi = sintaxe.advanceToNextToken();
        if (semi == null || semi.type != EnumTokenType.SEMI) {
            throw new SyntaxException("Esperado ';' ao final da declaração", semi != null ? semi : varName);
        }
        return new VariableDeclaration(varName.lexeme, typeToken.type, dimensions, initialization);
    }

    // Parse atribuição: x = 5; ou x[0] = 5;
    private Assign parseAssign(Sintaxe sintaxe, Token idToken) {
        AbstractExpression target;
        if (sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
            List<AbstractExpression> indices = new ArrayList<>();
            while (sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                sintaxe.advanceToNextToken(); // Consome LBRACK
                indices.add(parseExpression(sintaxe));
                Token rbrack = sintaxe.advanceToNextToken();
                if (rbrack == null || rbrack.type != EnumTokenType.RBRACK) {
                    throw new SyntaxException("Esperado ']'", rbrack != null ? rbrack : idToken);
                }
            }
            target = new ArrayAccess(idToken.lexeme, indices);
        } else {
            target = new Variable(idToken.lexeme);
        }

        Token assign = sintaxe.advanceToNextToken();
        if (assign == null || assign.type != EnumTokenType.ASSIGN) {
            throw new SyntaxException("Esperado '='", assign != null ? assign : idToken);
        }
        AbstractExpression expr = parseExpression(sintaxe);
        Token semi = sintaxe.advanceToNextToken();
        if (semi == null || semi.type != EnumTokenType.SEMI) {
            throw new SyntaxException("Esperado ';'", semi != null ? semi : idToken);
        }
        return new Assign(target, expr);
    }

    // Parse comando de impressão: imprima(expr);
    private Print parsePrint(Sintaxe sintaxe) {
        Token lpar = sintaxe.advanceToNextToken();
        if (lpar == null || lpar.type != EnumTokenType.LPAR) {
            throw new SyntaxException("Esperado '(' após 'imprima'", lpar);
        }
        AbstractExpression expr = parseExpression(sintaxe);
        Token rpar = sintaxe.advanceToNextToken();
        if (rpar == null || rpar.type != EnumTokenType.RPAR) {
            throw new SyntaxException("Esperado ')'", rpar);
        }
        Token semi = sintaxe.advanceToNextToken();
        if (semi == null || semi.type != EnumTokenType.SEMI) {
            throw new SyntaxException("Esperado ';'", semi);
        }
        return new Print(expr);
    }

    // Parse estrutura condicional: se (...) { ... } senao { ... }
    private ConditionalStruct parseConditionalStruct(Sintaxe sintaxe) {
        Token lpar = sintaxe.advanceToNextToken();
        if (lpar == null || lpar.type != EnumTokenType.LPAR) {
            throw new SyntaxException("Esperado '(' após 'se'", lpar);
        }
        AbstractExpression condition = parseExpression(sintaxe);
        Token rpar = sintaxe.advanceToNextToken();
        if (rpar == null || rpar.type != EnumTokenType.RPAR) {
            throw new SyntaxException("Esperado ')'", rpar);
        }
        Token lbrace = sintaxe.advanceToNextToken();
        if (lbrace == null || lbrace.type != EnumTokenType.LBRACE) {
            throw new SyntaxException("Esperado '{'", lbrace);
        }
        List<AbstractStatement> thenBlock = buildBlock(sintaxe);
        List<AbstractStatement> elseBlock = null;
        Token next = sintaxe.previewNextToken();
        if (next != null && next.type == EnumTokenType.SENAO) {
            sintaxe.advanceToNextToken(); // Consome SENAO
            Token elseBrace = sintaxe.advanceToNextToken();
            if (elseBrace == null || elseBrace.type != EnumTokenType.LBRACE) {
                throw new SyntaxException("Esperado '{'", elseBrace);
            }
            elseBlock = buildBlock(sintaxe);
        }
        return new ConditionalStruct(condition, thenBlock, elseBlock);
    }

    // Parse estrutura de repetição: enquanto (...) { ... } ou para (...) { ... }
    private RepetitionStruct parseRepetitionStruct(Sintaxe sintaxe, Token typeToken) {
        if (typeToken.type == EnumTokenType.REPITA) {
            Token lbrace = sintaxe.advanceToNextToken();
            if (lbrace == null || lbrace.type != EnumTokenType.LBRACE) {
                throw new SyntaxException("Esperado '{'", lbrace);
            }
            List<AbstractStatement> body = buildBlock(sintaxe);
            Token enquanto = sintaxe.advanceToNextToken();
            if (enquanto == null || enquanto.type != EnumTokenType.ENQUANTO) {
                throw new SyntaxException("Esperado 'enquanto' após bloco", enquanto);
            }
            Token lpar = sintaxe.advanceToNextToken();
            if (lpar == null || lpar.type != EnumTokenType.LPAR) {
                throw new SyntaxException("Esperado '('", lpar);
            }
            AbstractExpression condition = parseExpression(sintaxe);
            Token rpar = sintaxe.advanceToNextToken();
            if (rpar == null || rpar.type != EnumTokenType.RPAR) {
                throw new SyntaxException("Esperado ')'", rpar);
            }
            Token semi = sintaxe.advanceToNextToken();
            if (semi == null || semi.type != EnumTokenType.SEMI) {
                throw new SyntaxException("Esperado ';'", semi);
            }
            return new RepetitionStruct(typeToken.type, condition, body);
        } else {
            Token lpar = sintaxe.advanceToNextToken();
            if (lpar == null || lpar.type != EnumTokenType.LPAR) {
                throw new SyntaxException("Esperado '('", lpar);
            }
            AbstractExpression condition = parseExpression(sintaxe);
            Token rpar = sintaxe.advanceToNextToken();
            if (rpar == null || rpar.type != EnumTokenType.RPAR) {
                throw new SyntaxException("Esperado ')'", rpar);
            }
            Token lbrace = sintaxe.advanceToNextToken();
            if (lbrace == null || lbrace.type != EnumTokenType.LBRACE) {
                throw new SyntaxException("Esperado '{'", lbrace);
            }
            List<AbstractStatement> body = buildBlock(sintaxe);
            return new RepetitionStruct(typeToken.type, condition, body);
        }
    }

    // Parse bloco de statements entre { e }
    private List<AbstractStatement> buildBlock(Sintaxe sintaxe) {
        List<AbstractStatement> block = new ArrayList<>();
        while (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type != EnumTokenType.RBRACE) {
            block.add(getNextStatement(sintaxe));
        }
        Token rbrace = sintaxe.advanceToNextToken();
        if (rbrace == null || rbrace.type != EnumTokenType.RBRACE) {
            throw new SyntaxException("Esperado '}'", rbrace);
        }
        return block;
    }

    // Entrada principal para análise de expressões
    private AbstractExpression parseExpression(Sintaxe sintaxe) {
        return parseLogicalOr(sintaxe);
    }

    // igualdade (==, !=)
    private AbstractExpression parseEquality(Sintaxe sintaxe) {
        AbstractExpression expr = parseComparison(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && (next.type == EnumTokenType.EQ || next.type == EnumTokenType.NEQ)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseComparison(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // comparações (<, <=, >, >=)
    private AbstractExpression parseComparison(Sintaxe sintaxe) {
        AbstractExpression expr = parseTerm(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && (next.type == EnumTokenType.LT || next.type == EnumTokenType.LE ||
                next.type == EnumTokenType.GT || next.type == EnumTokenType.GE)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseTerm(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // lógica (&&, ||)
    // private AbstractExpression parseLogic(Sintaxe sintaxe) {
    // AbstractExpression expr = parseEquality(sintaxe);
    // Token next = sintaxe.previewNextToken();
    // while (next != null && (next.type == EnumTokenType.AND || next.type ==
    // EnumTokenType.OR)) {
    // Token operator = sintaxe.advanceToNextToken();
    // AbstractExpression right = parseEquality(sintaxe);
    // expr = new BinaryOperation(operator.lexeme, expr, right);
    // next = sintaxe.previewNextToken();
    // }
    // return expr;
    // }

    // OR lógico
    private AbstractExpression parseLogicalOr(Sintaxe sintaxe) {
        AbstractExpression expr = parseLogicalAnd(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && next.type == EnumTokenType.OR) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseLogicalAnd(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // AND lógico
    private AbstractExpression parseLogicalAnd(Sintaxe sintaxe) {
        AbstractExpression expr = parseEquality(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && next.type == EnumTokenType.AND) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseEquality(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // termos (+, -)
    private AbstractExpression parseTerm(Sintaxe sintaxe) {
        AbstractExpression expr = parseFactor(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && (next.type == EnumTokenType.PLUS || next.type == EnumTokenType.MINUS)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseFactor(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // fatores (*, /)
    private AbstractExpression parseFactor(Sintaxe sintaxe) {
        AbstractExpression expr = parseUnary(sintaxe);
        Token next = sintaxe.previewNextToken();
        while (next != null && (next.type == EnumTokenType.STAR || next.type == EnumTokenType.SLASH)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseUnary(sintaxe);
            expr = new BinaryOperation(operator.lexeme, expr, right);
            next = sintaxe.previewNextToken();
        }
        return expr;
    }

    // unários (-, +, nao, casts)
    private AbstractExpression parseUnary(Sintaxe sintaxe) {
        // Detecta cast na forma: ( tipo ) <expressao>
        List<Token> tokens = sintaxe.getTokenList();
        int pos = sintaxe.getPosition();
        if (pos + 2 < tokens.size()) {
            Token t0 = tokens.get(pos);
            Token t1 = tokens.get(pos + 1);
            Token t2 = tokens.get(pos + 2);
            boolean t1IsType = t1 != null && (t1.type == EnumTokenType.INT || t1.type == EnumTokenType.FLOAT
                    || t1.type == EnumTokenType.DOUBLE || t1.type == EnumTokenType.CHAR
                    || t1.type == EnumTokenType.STRING || t1.type == EnumTokenType.BOOL
                    || t1.type == EnumTokenType.VOID);
            if (t0 != null && t0.type == EnumTokenType.LPAR && t1IsType && t2 != null && t2.type == EnumTokenType.RPAR) {
                // consume '(' TYPE ')'
                sintaxe.advanceToNextToken(); // LPAR
                Token typeToken = sintaxe.advanceToNextToken(); // tipo
                sintaxe.advanceToNextToken(); // RPAR
                // Aplica cast ao próximo unary (mesma precedência dos unários)
                AbstractExpression right = parseUnary(sintaxe);
                return new Cast(typeToken.type, right);
            }
        }

        Token next = sintaxe.previewNextToken();
        if (next != null && (next.type == EnumTokenType.MINUS || next.type == EnumTokenType.PLUS
                || next.type == EnumTokenType.NOT)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseUnary(sintaxe);
            return new UnaryOperation(operator.lexeme, right);
        }

        return parsePrimary(sintaxe);
    }

    // primários (literais, variáveis, parênteses, chamadas de função)
    private AbstractExpression parsePrimary(Sintaxe sintaxe) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null) {
            throw new SyntaxException("Expressão incompleta", 0, 0);
        }
        switch (token.type) {
            case INTLIT:
                return new Literal(Integer.parseInt(token.lexeme));
            case FLOATLIT:
                return new Literal(Double.parseDouble(token.lexeme));
            case CHARLIT:
                if (token.lexeme.startsWith("\\")) {
                    return new Literal(token.lexeme);
                }
                return new Literal(token.lexeme.charAt(0));
            case STRINGLIT:
                return new Literal(token.lexeme);
            case TRUE:
                return new Literal(true);
            case FALSE:
                return new Literal(false);
            case ID:
                // Chamada de função: ID(...)
                if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.LPAR) {
                    sintaxe.advanceToNextToken(); // consome LPAR
                    List<AbstractExpression> args = new ArrayList<>();
                    if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type != EnumTokenType.RPAR) {
                        while (true) {
                            args.add(parseExpression(sintaxe));
                            if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.COMMA) {
                                sintaxe.advanceToNextToken(); // consome COMMA
                                continue;
                            }
                            break;
                        }
                    }
                    Token funcRpar = sintaxe.advanceToNextToken();
                    if (funcRpar == null || funcRpar.type != EnumTokenType.RPAR) {
                        throw new SyntaxException("Esperado ')'", funcRpar);
                    }
                    return new FunctionCall(token.lexeme, args);
                }

                // Pode ser uma variável simples ou um acesso a vetor
                if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                    List<AbstractExpression> indices = new ArrayList<>();
                    while (sintaxe.previewNextToken() != null
                            && sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                        sintaxe.advanceToNextToken(); // Consome LBRACK
                        indices.add(parseExpression(sintaxe));
                        Token arrRbrack = sintaxe.advanceToNextToken();
                        if (arrRbrack == null || arrRbrack.type != EnumTokenType.RBRACK) {
                            throw new SyntaxException("Esperado ']'", arrRbrack);
                        }
                    }
                    return new ArrayAccess(token.lexeme, indices);
                }
                return new Variable(token.lexeme);
            case LPAR:
                AbstractExpression expr = parseExpression(sintaxe);
                Token exprRpar = sintaxe.advanceToNextToken();
                if (exprRpar == null || exprRpar.type != EnumTokenType.RPAR) {
                    throw new SyntaxException("Esperado ')'", exprRpar);
                }
                return expr;
            default:
                throw new SyntaxException("Expressão não suportada", token);
        }
    }

    // Parse declaração de função: funcao <tipo> <nome>( [ <tipo> <nome> {, <tipo> <nome>} ] ) { ... }
    private FunctionDeclaration parseFunctionDeclaration(Sintaxe sintaxe) {
        Token returnTypeToken = sintaxe.advanceToNextToken();
        if (returnTypeToken == null) {
            throw new SyntaxException("Tipo de retorno esperado após 'funcao'", returnTypeToken);
        }
        Token nameToken = sintaxe.advanceToNextToken();
        if (nameToken == null || nameToken.type != EnumTokenType.ID) {
            throw new SyntaxException("Nome de função esperado", nameToken);
        }

        Token lpar = sintaxe.advanceToNextToken();
        if (lpar == null || lpar.type != EnumTokenType.LPAR) {
            throw new SyntaxException("Esperado '('", lpar);
        }
        List<FunctionDeclaration.Parameter> params = new ArrayList<>();
        if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type != EnumTokenType.RPAR) {
            while (true) {
                Token pType = sintaxe.advanceToNextToken();
                if (pType == null) {
                    throw new SyntaxException("Tipo de parâmetro esperado", pType);
                }
                Token pName = sintaxe.advanceToNextToken();
                if (pName == null || pName.type != EnumTokenType.ID) {
                    throw new SyntaxException("Nome de parâmetro esperado", pName);
                }
                params.add(new FunctionDeclaration.Parameter(pName.lexeme, pType.type));
                if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.COMMA) {
                    sintaxe.advanceToNextToken();
                    continue;
                }
                break;
            }
        }
        Token rpar = sintaxe.advanceToNextToken();
        if (rpar == null || rpar.type != EnumTokenType.RPAR) {
            throw new SyntaxException("Esperado ')'", rpar);
        }

        Token lbrace = sintaxe.advanceToNextToken();
        if (lbrace == null || lbrace.type != EnumTokenType.LBRACE) {
            throw new SyntaxException("Esperado '{'", lbrace);
        }
        List<AbstractStatement> body = buildBlock(sintaxe);

        return new FunctionDeclaration(returnTypeToken.type, nameToken.lexeme, params, body);
    }

    // Parse return: retorne expr;
    private ReturnStatement parseReturnStatement(Sintaxe sintaxe) {
        AbstractExpression expr = parseExpression(sintaxe);
        Token semi = sintaxe.advanceToNextToken();
        if (semi == null || semi.type != EnumTokenType.SEMI) {
            throw new SyntaxException("Esperado ';' após retorne", semi);
        }
        return new ReturnStatement(expr);
    }

}
