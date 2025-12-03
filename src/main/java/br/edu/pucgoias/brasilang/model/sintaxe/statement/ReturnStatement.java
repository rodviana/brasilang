package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/** Statement that returns a value from a function. */
public class ReturnStatement implements AbstractStatement {

    private final AbstractExpression expression;

    public ReturnStatement(AbstractExpression expression) {
        this.expression = expression;
    }

    public AbstractExpression getExpression() {
        return expression;
    }

    @Override
    public void translate(TranslationContext ctx) {
        ctx.getBuilder().appendLine("return " + expression.translate(ctx) + ";");
    }

    @Override
    public String toString() {
        return "ReturnStatement{\n" +
                "  expression=" + expression + "\n" +
                "}";
    }
}
