package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.translate.TranslationContext;

public class Print implements AbstractStatement {

    private final AbstractExpression expression;

    public Print(AbstractExpression expression) {
        this.expression = expression;
    }

    public AbstractExpression getExpression() {
        return expression;
    }

    @Override
    public void translate(TranslationContext ctx) {
        ctx.addInclude("<stdio.h>");
        ctx.getBuilder().appendLine("printf(" + expression.translate(ctx) + ");");
    }

@Override
public String toString() {
return "Print{\n" +
"  expression=" + expression + "\n" +
"}";
}
}
