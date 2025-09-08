package br.edu.pucgoias.brasilang.model.sintaxe.statement;


import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;

public class Assign implements AbstractStatement {

	private String variableName;
	private AbstractExpression newValue;
	
	public Assign(String variableName, AbstractExpression newValue) {
		super();
		this.variableName = variableName;
		this.newValue = newValue;
	}
}
