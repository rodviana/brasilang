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
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Assign;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.VariableDeclaration;

@Service
public class SintaxeService {
	
	public List<AbstractStatement> buildProgramStatementList(Sintaxe sintaxe) {
		List<AbstractStatement> program = new ArrayList();
		
		while(! this.isNextTokenEOF(sintaxe))
			program.add(this.getNextStatement(sintaxe));
		
		return program;
	}
	
	private boolean isNextTokenEOF(Sintaxe sintaxe){
		return sintaxe.previewNextToken() == null || EnumTokenType.EOF.equals(sintaxe.previewNextToken().type);
	}
	
	private AbstractStatement getNextStatement(Sintaxe sintaxe) {
	    Token token = sintaxe.advanceToNextToken();

	    switch (token.type) {
	        case INT:
	            return parseVariableDeclaration(sintaxe);
	        case ID:
	            return parseAssign(sintaxe, token);
	        case SE:
	            return parseConditionalStruct(sintaxe);
	        case ENQUANTO:
	        case PARA:
	            return parseRepetitionStruct(sintaxe, token.type);
	        default:
	            throw new RuntimeException("Token inesperado: " + token.type);
	    }
	}

	// Parse declaração de variável: var x : inteiro = 10;
	private VariableDeclaration parseVariableDeclaration(Sintaxe sintaxe) {
	    Token varName = sintaxe.advanceToNextToken(); // ID
	    sintaxe.advanceToNextToken(); // COLON
	    Token typeToken = sintaxe.advanceToNextToken(); // INT, FLOAT, etc.
	    sintaxe.advanceToNextToken(); // ASSIGN
	    AbstractExpression expr = parseExpression(sintaxe);
	    sintaxe.advanceToNextToken(); // SEMI
	    return new VariableDeclaration(varName.lexeme, typeToken.type, expr);
	}

	// Parse atribuição: x = 5;
	private Assign parseAssign(Sintaxe sintaxe, Token idToken) {
	    sintaxe.advanceToNextToken(); // ASSIGN
	    AbstractExpression expr = parseExpression(sintaxe);
	    sintaxe.advanceToNextToken(); // SEMI
	    return new Assign(idToken.lexeme, expr);
	}

	// Parse estrutura condicional: se (...) { ... } senao { ... }
	private ConditionalStruct parseConditionalStruct(Sintaxe sintaxe) {
	    sintaxe.advanceToNextToken(); // LPAR
	    AbstractExpression condition = parseExpression(sintaxe);
	    sintaxe.advanceToNextToken(); // RPAR
	    sintaxe.advanceToNextToken(); // LBRACE
	    List<AbstractStatement> thenBlock = buildBlock(sintaxe);
	    List<AbstractStatement> elseBlock = null;
	    Token next = sintaxe.previewNextToken();
	    if (next != null && next.type == EnumTokenType.SENAO) {
	        sintaxe.advanceToNextToken(); // SENAO
	        sintaxe.advanceToNextToken(); // LBRACE
	        elseBlock = buildBlock(sintaxe);
	    }
	    return new ConditionalStruct(condition, thenBlock, elseBlock);
	}

	// Parse estrutura de repetição: enquanto (...) { ... } ou para (...) { ... }
	private RepetitionStruct parseRepetitionStruct(Sintaxe sintaxe, EnumTokenType type) {
	    sintaxe.advanceToNextToken(); // LPAR
	    AbstractExpression condition = parseExpression(sintaxe);
	    sintaxe.advanceToNextToken(); // RPAR
	    sintaxe.advanceToNextToken(); // LBRACE
	    List<AbstractStatement> body = buildBlock(sintaxe);
	    return new RepetitionStruct(type, condition, body);
	}

	// Parse bloco de statements entre { e }
	private List<AbstractStatement> buildBlock(Sintaxe sintaxe) {
	    List<AbstractStatement> block = new ArrayList<>();
	    while (sintaxe.previewNextToken() != null && sintaxe.previewNextToken().type != EnumTokenType.RBRACE) {
	        block.add(getNextStatement(sintaxe));
	    }
	    sintaxe.advanceToNextToken(); // RBRACE
	    return block;
	}

	// Parse expressão simples (exemplo, precisa ser expandido para sua gramática)
	private AbstractExpression parseExpression(Sintaxe sintaxe) {
	    Token token = sintaxe.advanceToNextToken();
	    // Exemplo: apenas literais e identificadores
	    switch (token.type) {
	        case INTLIT:
	        case FLOATLIT:
	        case STRINGLIT:
	            return new Literal(token.lexeme);
	        case ID:
	            return new Variable(token.lexeme);
	        default:
	            throw new RuntimeException("Expressão não suportada: " + token.type);
	    }
	}
	

}
