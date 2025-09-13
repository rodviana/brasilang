package br.edu.pucgoias.brasilang.model.sintaxe.statement;


import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.translate.TranslationContext;

public class Assign implements AbstractStatement {

	private String variableName;
	private AbstractExpression newValue;
	
        public Assign(String variableName, AbstractExpression newValue) {
                super();
                this.variableName = variableName;
                this.newValue = newValue;
        }

        @Override
        public void translate(TranslationContext ctx) {
                ctx.getBuilder()
                        .appendLine(variableName + " = " + newValue.translate(ctx) + ";");
        }

        @Override
        public String toString() {
                return String.format("Assign{variableName='%s', newValue=%s}", variableName, newValue);
        }
}
