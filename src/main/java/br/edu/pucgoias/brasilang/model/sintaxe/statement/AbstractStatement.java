package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

/**
 * Base interface for all statement nodes in the AST. Statements append their
 * translated C code directly to the {@link TranslationContext}'s
 * {@code CodeBuilder}.
 */
public interface AbstractStatement {

    /**
     * Emits the C code representation for this statement using the provided
     * translation context.
     *
     * @param ctx translation context with helper utilities
     */
    void translate(TranslationContext ctx);
}

