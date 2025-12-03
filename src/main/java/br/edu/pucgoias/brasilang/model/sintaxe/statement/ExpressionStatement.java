package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/** Statement wrapper para expressões usadas como instruções (ex.: chamadas de função). */
public class ExpressionStatement implements AbstractStatement {

    private final AbstractExpression expression;

    public ExpressionStatement(AbstractExpression expression) {
        this.expression = expression;
    }

    @Override
    public void translate(TranslationContext ctx) {
        ctx.getBuilder().appendLine(expression.translate(ctx) + ";");
    }

    @Override
    public String toString() {
        return "ExpressionStatement{expression=" + expression + "}";
    }
}
