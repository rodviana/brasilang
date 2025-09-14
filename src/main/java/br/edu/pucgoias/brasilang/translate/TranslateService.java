package br.edu.pucgoias.brasilang.translate;

import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;

/** Service responsible for orchestrating AST translation to C code. */
public class TranslateService {

    /** Generates complete C source code for the provided AST. */
    public String generateCode(Program program) {
        CodeBuilder builder = new CodeBuilder();
        TranslationContext ctx = new TranslationContext(builder);

        // Emit function declarations before main
        for (AbstractStatement st : program.getStatements()) {
            if (st instanceof FunctionDeclaration) {
                st.translate(ctx);
            }
        }

        builder.appendLine("int main() {");
        builder.indent();
        for (AbstractStatement st : program.getStatements()) {
            if (!(st instanceof FunctionDeclaration)) {
                st.translate(ctx);
            }
        }
        builder.appendLine("return 0;");
        builder.outdent();
        builder.appendLine("}");

        StringBuilder finalCode = new StringBuilder();
        for (String inc : ctx.getIncludes()) {
            finalCode.append("#include ").append(inc).append("\n");
        }
        if (!ctx.getIncludes().isEmpty()) {
            finalCode.append("\n");
        }
        finalCode.append(builder.build());
        return finalCode.toString();
    }
}
