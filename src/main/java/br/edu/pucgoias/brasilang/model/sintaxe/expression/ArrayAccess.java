package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/**
 * Represents an array access expression in the AST, e.g., `myArray[index]`.
 */
public class ArrayAccess implements AbstractExpression {

    private final String variableName;
    private final AbstractExpression indexExpression;

    public ArrayAccess(String variableName, AbstractExpression indexExpression) {
        this.variableName = variableName;
        this.indexExpression = indexExpression;
    }

    @Override
    public String translate(TranslationContext ctx) {
        return variableName + "[" + indexExpression.translate(ctx) + "]";
    }

    @Override
    public String toString() {
        return "ArrayAccess{\n" +
                "  variableName='" + variableName + "',\n" +
                "  indexExpression=" + indexExpression + "\n" +
                "}";
    }
}