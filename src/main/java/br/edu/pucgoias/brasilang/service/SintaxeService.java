package br.edu.pucgoias.brasilang.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.lexico.Token;
import br.edu.pucgoias.brasilang.model.sintaxe.Sintaxe;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Literal;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Variable;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.BinaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.UnaryOperation;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Assign;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.VariableDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Print;

@Service
public class SintaxeService {

    public List<AbstractStatement> buildProgramStatementList(Sintaxe sintaxe) {
        List<AbstractStatement> program = new ArrayList<>();
        try {
            while (!this.isNextTokenEOF(sintaxe))
                program.add(this.getNextStatement(sintaxe));
        } catch (Exception e) {
            program.forEach(statement -> System.out.println(statement.toString()));
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
                return parseVariableDeclaration(sintaxe, token);
            case ID:
                return parseAssign(sintaxe, token);
            case SE:
                return parseConditionalStruct(sintaxe);
            case IMPRIMA:
                return parsePrint(sintaxe);
            case ENQUANTO:
            case REPITA:
            case PARA:
                return parseRepetitionStruct(sintaxe, token);
            default:
                throw new RuntimeException("Token inesperado: " + token.type);
        }
    }

    // Parse declaração de variável: inteiro g = 10;
    private VariableDeclaration parseVariableDeclaration(Sintaxe sintaxe, Token typeToken) {
        Token varName = sintaxe.advanceToNextToken(); // ID
        Token assignToken = sintaxe.advanceToNextToken(); // ASSIGN
        AbstractExpression expr = parseExpression(sintaxe);
        Token semiToken = sintaxe.advanceToNextToken(); // SEMI
        return new VariableDeclaration(varName.lexeme, typeToken.type, expr);
    }

    // Parse atribuição: x = 5;
    private Assign parseAssign(Sintaxe sintaxe, Token idToken) {
        Token assignToken = sintaxe.advanceToNextToken(); // ASSIGN
        AbstractExpression expr = parseExpression(sintaxe);
        Token semiToken = sintaxe.advanceToNextToken(); // SEMI
        return new Assign(idToken.lexeme, expr);
    }

    // Parse comando de impressão: imprima(expr);
    private Print parsePrint(Sintaxe sintaxe) {
        Token lpar = sintaxe.advanceToNextToken(); // LPAR
        AbstractExpression expr = parseExpression(sintaxe);
        Token rpar = sintaxe.advanceToNextToken(); // RPAR
        Token semiToken = sintaxe.advanceToNextToken(); // SEMI
        return new Print(expr);
    }

    // Parse estrutura condicional: se (...) { ... } senao { ... }
    private ConditionalStruct parseConditionalStruct(Sintaxe sintaxe) {
        Token lpar = sintaxe.advanceToNextToken(); // LPAR
        AbstractExpression condition = parseExpression(sintaxe);
        Token rpar = sintaxe.advanceToNextToken(); // RPAR
        Token lbrace = sintaxe.advanceToNextToken(); // LBRACE
        List<AbstractStatement> thenBlock = buildBlock(sintaxe);
        List<AbstractStatement> elseBlock = null;
        Token next = sintaxe.previewNextToken();
        if (next != null && next.type == EnumTokenType.SENAO) {
            Token senaoToken = sintaxe.advanceToNextToken(); // SENAO
            Token elseLbrace = sintaxe.advanceToNextToken(); // LBRACE
            elseBlock = buildBlock(sintaxe);
        }
        return new ConditionalStruct(condition, thenBlock, elseBlock);
    }

    // Parse estrutura de repetição: enquanto (...) { ... } ou para (...) { ... }
    private RepetitionStruct parseRepetitionStruct(Sintaxe sintaxe, Token typeToken) {
        if (typeToken.type == EnumTokenType.REPITA) {
            // Estrutura: repita { ... } enquanto (condicao);
            Token lbrace = sintaxe.advanceToNextToken(); // LBRACE
            List<AbstractStatement> body = buildBlock(sintaxe);
            Token enquantoToken = sintaxe.advanceToNextToken(); // ENQUANTO
            Token lpar = sintaxe.advanceToNextToken(); // LPAR
            AbstractExpression condition = parseExpression(sintaxe);
            Token rpar = sintaxe.advanceToNextToken(); // RPAR
            Token semiToken = sintaxe.advanceToNextToken(); // SEMI
            return new RepetitionStruct(typeToken.type, condition, body);
        } else { // ENQUANTO ou PARA
            Token lpar = sintaxe.advanceToNextToken(); // LPAR
            AbstractExpression condition = parseExpression(sintaxe);
            Token rpar = sintaxe.advanceToNextToken(); // RPAR
            Token lbrace = sintaxe.advanceToNextToken(); // LBRACE
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
        Token rbrace = sintaxe.advanceToNextToken(); // RBRACE
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
    //     AbstractExpression expr = parseEquality(sintaxe);
    //     Token next = sintaxe.previewNextToken();
    //     while (next != null && (next.type == EnumTokenType.AND || next.type == EnumTokenType.OR)) {
    //         Token operator = sintaxe.advanceToNextToken();
    //         AbstractExpression right = parseEquality(sintaxe);
    //         expr = new BinaryOperation(operator.lexeme, expr, right);
    //         next = sintaxe.previewNextToken();
    //     }
    //     return expr;
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

    // unários (-, +)
    private AbstractExpression parseUnary(Sintaxe sintaxe) {
        Token next = sintaxe.previewNextToken();
        if (next != null && (next.type == EnumTokenType.MINUS || next.type == EnumTokenType.PLUS || next.type == EnumTokenType.NOT)) {
            Token operator = sintaxe.advanceToNextToken();
            AbstractExpression right = parseUnary(sintaxe);
            return new UnaryOperation(operator.lexeme, right);
        }
        
        return parsePrimary(sintaxe);
    }

    // primários (literais, variáveis, parênteses)
    private AbstractExpression parsePrimary(Sintaxe sintaxe) {
        Token token = sintaxe.advanceToNextToken();
        if (token == null) {
            throw new RuntimeException("Expressão incompleta");
        }
        switch (token.type) {
            case INTLIT:
                return new Literal(Integer.parseInt(token.lexeme));
            case FLOATLIT:
                return new Literal(Double.parseDouble(token.lexeme));
            case STRINGLIT:
                return new Literal(token.lexeme);
            case TRUE:
                return new Literal(true);
            case FALSE:
                return new Literal(false);
            case ID:
                return new Variable(token.lexeme);
            case LPAR:
                AbstractExpression expr = parseExpression(sintaxe);
                Token rpar = sintaxe.advanceToNextToken(); // RPAR
                return expr;
            default:
                throw new RuntimeException("Expressão não suportada: " + token.type);
}
    }

}
