package br.edu.pucgoias.brasilang.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.error.BrasilangException;
import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.lexico.Token;
import br.edu.pucgoias.brasilang.model.sintaxe.Sintaxe;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.ArrayAccess;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.BinaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.CastExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.FunctionCall;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Literal;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.UnaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Variable;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Assign;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ExpressionStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Print;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ReturnStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.VariableDeclaration;

@Service
public class SintaxeService {

    public List<AbstractStatement> buildProgramStatementList(Sintaxe sintaxe) {
        List<AbstractStatement> program = new ArrayList<>();
        while (!isNextTokenEOF(sintaxe)) {
            program.add(getNextStatement(sintaxe));
        }
        return program;
    }

    private boolean isNextTokenEOF(Sintaxe sintaxe) {
        return sintaxe.previewNextToken() == null || EnumTokenType.EOF.equals(sintaxe.previewNextToken().type);
    }

    private AbstractStatement getNextStatement(Sintaxe sintaxe) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null) {
            throw BrasilangException.syntactic("Fim inesperado do arquivo.", null);
        }

        switch (token.type) {
            case FUNCAO:
                return parseFunctionDeclaration(sintaxe);
            case RETORNE:
                return parseReturn(sintaxe);
            case INT:
            case FLOAT:
            case DOUBLE:
            case BOOL:
            case CHAR:
            case STRING:
            case VOID:
                return parseVariableOrFunction(sintaxe, token);
            case ID:
                return parseAssignOrCall(sintaxe, token);
            case SE:
                return parseConditionalStruct(sintaxe);
            case IMPRIMA:
                return parsePrint(sintaxe);
            case ENQUANTO:
            case PARA:
            case REPITA:
                return parseRepetitionStruct(sintaxe, token);
            default:
                throw BrasilangException.syntactic("Token inesperado em nível de statement: " + token.type, token);
        }
    }

    // ===== funções e retornos =====
    private AbstractStatement parseVariableOrFunction(Sintaxe sintaxe, Token typeToken) {
        Token nameToken = expect(sintaxe, EnumTokenType.ID,
                "Esperado um identificador após o tipo " + typeToken.lexeme);
        Token next = lookAhead(sintaxe, 0);
        if (next != null && next.type == EnumTokenType.LPAR) {
            return parseFunctionDeclaration(sintaxe, typeToken.type, nameToken);
        }
        return parseVariableDeclaration(sintaxe, typeToken.type, nameToken);
    }

    private FunctionDeclaration parseFunctionDeclaration(Sintaxe sintaxe) {
        Token returnType = expectType(sintaxe, "Informe o tipo de retorno da função após 'funcao'.");
        Token name = expect(sintaxe, EnumTokenType.ID, "Informe o nome da função.");
        return parseFunctionDeclaration(sintaxe, returnType.type, name);
    }

    private FunctionDeclaration parseFunctionDeclaration(Sintaxe sintaxe, EnumTokenType returnType, Token nameToken) {
        expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' para iniciar a lista de parâmetros da função.");
        List<FunctionDeclaration.Parameter> params = new ArrayList<>();
        Token preview = sintaxe.previewNextToken();
        if (preview != null && preview.type != EnumTokenType.RPAR) {
            boolean keepParsing = true;
            while (keepParsing) {
                Token paramType = expectType(sintaxe, "Tipo do parâmetro obrigatório.");
                Token paramName = expect(sintaxe, EnumTokenType.ID, "Esperado nome do parâmetro.");
                params.add(new FunctionDeclaration.Parameter(paramName.lexeme, paramType.type));
                Token commaOrRpar = sintaxe.previewNextToken();
                if (commaOrRpar != null && commaOrRpar.type == EnumTokenType.COMMA) {
                    sintaxe.advanceToNextToken(); // consome COMMA
                } else {
                    keepParsing = false;
                }
            }
        }
        expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após parâmetros da função " + nameToken.lexeme + ".");
        expect(sintaxe, EnumTokenType.LBRACE, "Esperado '{' para abrir o corpo da função.");
        List<AbstractStatement> body = buildBlock(sintaxe);
        return new FunctionDeclaration(returnType, nameToken.lexeme, params, body);
    }

    private ReturnStatement parseReturn(Sintaxe sintaxe) {
        AbstractExpression expr = parseExpression(sintaxe);
        expect(sintaxe, EnumTokenType.SEMI, "Use ';' para finalizar a instrução de retorno.");
        return new ReturnStatement(expr);
    }

    // ===== variáveis e atribuições =====
    private VariableDeclaration parseVariableDeclaration(Sintaxe sintaxe, EnumTokenType typeToken, Token nameToken) {
        if (typeToken == EnumTokenType.VOID) {
            throw BrasilangException.syntactic("Variáveis não podem ser declaradas com tipo vazio.", nameToken);
        }
        List<AbstractExpression> dimensions = new ArrayList<>();
        AbstractExpression initialization = null;

        while (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
            sintaxe.advanceToNextToken(); // LBRACK
            dimensions.add(parseExpression(sintaxe));
            expect(sintaxe, EnumTokenType.RBRACK, "Esperado ']' após a dimensão do vetor/matriz.");
        }

        if (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.ASSIGN) {
            sintaxe.advanceToNextToken(); // ASSIGN
            initialization = parseExpression(sintaxe);
        }

        expect(sintaxe, EnumTokenType.SEMI, "Esperado ';' ao final da declaração de " + nameToken.lexeme + ".");
        return new VariableDeclaration(nameToken.lexeme, typeToken, dimensions, initialization);
    }

    private AbstractStatement parseAssignOrCall(Sintaxe sintaxe, Token idToken) {
        Token next = sintaxe.previewNextToken();
        if (next != null && next.type == EnumTokenType.LPAR) {
            AbstractExpression call = parseFunctionCallAfterName(sintaxe, idToken);
            expect(sintaxe, EnumTokenType.SEMI, "Esperado ';' ao final da chamada da função.");
            return new ExpressionStatement(call);
        }
        return parseAssign(sintaxe, idToken);
    }

    private Assign parseAssign(Sintaxe sintaxe, Token idToken) {
        AbstractExpression target;
        Token preview = sintaxe.previewNextToken();
        if (preview != null && preview.type == EnumTokenType.LBRACK) {
            List<AbstractExpression> indices = new ArrayList<>();
            while (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                sintaxe.advanceToNextToken(); // Consome LBRACK
                indices.add(parseExpression(sintaxe));
                expect(sintaxe, EnumTokenType.RBRACK, "Esperado ']' para fechar o índice do vetor/matriz.");
            }
            target = new ArrayAccess(idToken.lexeme, indices);
        } else {
            target = new Variable(idToken.lexeme);
        }

        expect(sintaxe, EnumTokenType.ASSIGN, "Esperado '=' em uma atribuição.");
        AbstractExpression expr = parseExpression(sintaxe);
        expect(sintaxe, EnumTokenType.SEMI, "Esperado ';' após a atribuição.");
        return new Assign(target, expr);
    }

    // ===== comandos simples =====
    private Print parsePrint(Sintaxe sintaxe) {
        expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' após 'imprima'.");
        AbstractExpression expr = parseExpression(sintaxe);
        expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após o conteúdo a ser impresso.");
        expect(sintaxe, EnumTokenType.SEMI, "Esperado ';' ao final do comando de impressão.");
        return new Print(expr);
    }

    // ===== controle de fluxo =====
    private ConditionalStruct parseConditionalStruct(Sintaxe sintaxe) {
        expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' após 'se'.");
        AbstractExpression condition = parseExpression(sintaxe);
        expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após a condição do 'se'.");
        expect(sintaxe, EnumTokenType.LBRACE, "Esperado '{' para abrir o bloco do 'se'.");
        List<AbstractStatement> thenBlock = buildBlock(sintaxe);

        List<AbstractStatement> elseBlock = null;
        Token next = sintaxe.previewNextToken();
        if (next != null && next.type == EnumTokenType.SENAO) {
            sintaxe.advanceToNextToken(); // Consome SENAO
            expect(sintaxe, EnumTokenType.LBRACE, "Esperado '{' para abrir o bloco do 'senao'.");
            elseBlock = buildBlock(sintaxe);
        }
        return new ConditionalStruct(condition, thenBlock, elseBlock);
    }

    private RepetitionStruct parseRepetitionStruct(Sintaxe sintaxe, Token typeToken) {
        if (typeToken.type == EnumTokenType.REPITA) {
            expect(sintaxe, EnumTokenType.LBRACE, "Esperado '{' após 'repita'.");
            List<AbstractStatement> body = buildBlock(sintaxe);
            expect(sintaxe, EnumTokenType.ENQUANTO, "Esperado 'enquanto' para finalizar o bloco 'repita'.");
            expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' após 'enquanto' no 'repita'.");
            AbstractExpression condition = parseExpression(sintaxe);
            expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após condição do 'repita/enquanto'.");
            expect(sintaxe, EnumTokenType.SEMI, "Finalize o 'repita/enquanto' com ';'.");
            return new RepetitionStruct(typeToken.type, condition, body);
        }

        expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' após " + typeToken.lexeme + ".");
        AbstractExpression condition = parseExpression(sintaxe);
        expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após condição do laço.");
        expect(sintaxe, EnumTokenType.LBRACE, "Esperado '{' para abrir o corpo do laço.");
        List<AbstractStatement> body = buildBlock(sintaxe);
        return new RepetitionStruct(typeToken.type, condition, body);
    }

    private List<AbstractStatement> buildBlock(Sintaxe sintaxe) {
        List<AbstractStatement> block = new ArrayList<>();
        while (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type != EnumTokenType.RBRACE) {
            block.add(getNextStatement(sintaxe));
        }
        expect(sintaxe, EnumTokenType.RBRACE, "Bloco precisa ser fechado com '}'.");
        return block;
    }

    // ===== análise de expressões =====
    private AbstractExpression parseExpression(Sintaxe sintaxe) {
        return parseLogicalOr(sintaxe);
    }

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

    private AbstractExpression parseUnary(Sintaxe sintaxe) {
        Token next = sintaxe.previewNextToken();
        if (next != null && (next.type == EnumTokenType.MINUS || next.type == EnumTokenType.PLUS
                || next.type == EnumTokenType.NOT)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseUnary(sintaxe);
            return new UnaryOperation(operator.lexeme, right);
        }

        return parsePrimary(sintaxe);
    }

    private AbstractExpression parsePrimary(Sintaxe sintaxe) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null) {
            throw BrasilangException.syntactic("Expressão incompleta.", token);
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
                Token lookAhead = sintaxe.previewNextToken();
                if (lookAhead != null && lookAhead.type == EnumTokenType.LPAR) {
                    return parseFunctionCallAfterName(sintaxe, token);
                }
                if (lookAhead != null && lookAhead.type == EnumTokenType.LBRACK) {
                    List<AbstractExpression> indices = new ArrayList<>();
                    while (sintaxe.previewNextToken() != null
                            && sintaxe.previewNextToken().type == EnumTokenType.LBRACK) {
                        sintaxe.advanceToNextToken(); // Consome LBRACK
                        indices.add(parseExpression(sintaxe));
                        expect(sintaxe, EnumTokenType.RBRACK, "Esperado ']' após índice de vetor/matriz.");
                    }
                    return new ArrayAccess(token.lexeme, indices);
                }
                return new Variable(token.lexeme);
            case LPAR:
                Token maybeType = sintaxe.previewNextToken();
                if (maybeType != null && isTypeToken(maybeType.type)) {
                    sintaxe.advanceToNextToken(); // consome tipo
                    EnumTokenType castType = maybeType.type;
                    expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após tipo no cast.");
                    AbstractExpression castExpr = parseUnary(sintaxe);
                    return new CastExpression(castType, castExpr);
                }
                AbstractExpression expr = parseExpression(sintaxe);
                expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' para fechar a expressão.");
                return expr;
            default:
                throw BrasilangException.syntactic("Expressão não suportada: " + token.type, token);
        }
    }

    private FunctionCall parseFunctionCallAfterName(Sintaxe sintaxe, Token nameToken) {
        expect(sintaxe, EnumTokenType.LPAR, "Esperado '(' após o nome da função.");
        List<AbstractExpression> args = new ArrayList<>();
        Token preview = sintaxe.previewNextToken();
        if (preview != null && preview.type != EnumTokenType.RPAR) {
            boolean keepParsing = true;
            while (keepParsing) {
                args.add(parseExpression(sintaxe));
                Token commaOrRpar = sintaxe.previewNextToken();
                if (commaOrRpar != null && commaOrRpar.type == EnumTokenType.COMMA) {
                    sintaxe.advanceToNextToken(); // Consome COMMA
                } else {
                    keepParsing = false;
                }
            }
        }
        expect(sintaxe, EnumTokenType.RPAR, "Esperado ')' após argumentos da função " + nameToken.lexeme + ".");
        return new FunctionCall(nameToken.lexeme, args);
    }

    // ===== utilitários =====
    private Token expect(Sintaxe sintaxe, EnumTokenType expected, String message) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null || token.type != expected) {
            throw BrasilangException.syntactic(message + " Esperado " + expected + ".", token);
        }
        return token;
    }

    private Token expectType(Sintaxe sintaxe, String message) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null || !isTypeToken(token.type)) {
            throw BrasilangException.syntactic(message + " Tipos válidos: inteiro, flutuante, duplo, booleano, caractere, string, vazio.", token);
        }
        return token;
    }

    private Token lookAhead(Sintaxe sintaxe, int offset) {
        int idx = sintaxe.getPosition() + offset;
        if (idx >= 0 && idx < sintaxe.getTokenList().size()) {
            return sintaxe.getTokenList().get(idx);
        }
        return null;
    }

    private boolean isTypeToken(EnumTokenType type) {
        return type == EnumTokenType.INT || type == EnumTokenType.FLOAT || type == EnumTokenType.DOUBLE
                || type == EnumTokenType.BOOL || type == EnumTokenType.CHAR || type == EnumTokenType.STRING
                || type == EnumTokenType.VOID;
    }
}
