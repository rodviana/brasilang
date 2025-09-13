package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

/** Root of the AST containing top-level statements. */
public class Program implements AbstractStatement {

    private final List<AbstractStatement> statements;

    public Program(List<AbstractStatement> statements) {
        this.statements = statements;
    }

    public List<AbstractStatement> getStatements() {
        return statements;
    }

    @Override
    public void translate(TranslationContext ctx) {
        for (AbstractStatement st : statements) {
            st.translate(ctx);
        }
    }
}
