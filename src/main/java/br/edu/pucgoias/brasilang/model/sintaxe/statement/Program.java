package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AbstractStatement st : statements) {
            sb.append("    ").append(indent(st.toString())).append("\n");
        }
        return "Program{\n" +
                "  statements=[\n" + sb.toString() + "  ]\n" +
                "}";
    }

    private static String indent(String str) {
        return str.replace("\n", "\n    ");
    }
}
