package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;


public class VariableDeclaration implements AbstractStatement{

	private String variableName;
	private EnumTokenType tokenType;
	private AbstractExpression initialization;
	
        public VariableDeclaration(String variableName, EnumTokenType tokenType, AbstractExpression initialization) {
                super();
                this.variableName = variableName;
                this.tokenType = tokenType;
                this.initialization = initialization;
        }

        @Override
        public String toString() {
                return String.format("VariableDeclaration{variableName='%s', tokenType=%s, initialization=%s}", variableName, tokenType, initialization);
        }

 }
