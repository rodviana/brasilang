package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Literal;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Variable;
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
        String exprCode = expression.translate(ctx);
        String format = "%d";
        if (expression instanceof Literal lit) {
            Object val = lit.getValue();
            if (val instanceof String) {
                format = "%s";
            } else if (val instanceof Float || val instanceof Double) {
                format = "%f";
            }
        } else if (expression instanceof Variable var) {
            String type = ctx.getVariables().get(var.getName());
            if ("float".equals(type) || "double".equals(type)) {
                format = "%f";
            }
        }
        ctx.getBuilder().appendLine("printf(\"" + format + "\\n\", " + exprCode + ");");
    }

    @Override
    public String toString() {
        return "Print{\n" +
                "  expression=" + expression + "\n" +
                "}"; 
    }
}
