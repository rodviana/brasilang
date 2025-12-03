package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/**
 * Expressão para conversão explícita de tipos. Traduzida para a notação de cast
 * em C, por exemplo: (int) x.
 */
public class CastExpression implements AbstractExpression {

    private final EnumTokenType targetType;
    private final AbstractExpression expression;

    public CastExpression(EnumTokenType targetType, AbstractExpression expression) {
        this.targetType = targetType;
        this.expression = expression;
    }

    @Override
    public String translate(TranslationContext ctx) {
        return "(" + ctx.toCType(targetType) + ") (" + expression.translate(ctx) + ")";
    }

    @Override
    public String toString() {
        return "CastExpression{targetType=" + targetType + ", expression=" + expression + "}";
    }
}
