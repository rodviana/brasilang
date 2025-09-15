package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/**
 * Represents an expression node in the AST. Each expression knows how to
 * translate itself to C code using the provided {@link TranslationContext}.
 */
public interface AbstractExpression {

    /**
     * Translates the expression into its C representation.
     *
     * @param ctx translation context with helper utilities
     * @return the C code representing this expression
     */
    String translate(TranslationContext ctx);
}

