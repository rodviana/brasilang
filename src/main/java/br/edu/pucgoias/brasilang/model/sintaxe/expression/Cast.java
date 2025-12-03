package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/**
 * Expression que representa um cast explícito: (tipo) expr
 */
public class Cast implements AbstractExpression {

    private final EnumTokenType targetType;
    private final AbstractExpression expression;

    public Cast(EnumTokenType targetType, AbstractExpression expression) {
        this.targetType = targetType;
        this.expression = expression;
    }

    @Override
    public String translate(TranslationContext ctx) {
        return "(" + ctx.toCType(targetType) + ") " + expression.translate(ctx);
    }

    @Override
    public String toString() {
        return String.format("Cast{to=%s, expr=%s}", targetType, expression);
    }
}
