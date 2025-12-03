package br.edu.pucgoias.brasilang.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.translate.CodeBuilder;
import br.edu.pucgoias.brasilang.model.translate.OptimizationLevel;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;
import br.edu.pucgoias.brasilang.model.translate.TranslationResult;

/** Service responsible for orchestrating AST translation to C code. */
@Service
public class TranslateService {

    @Autowired
    private CompilationService compilationService;

    /** Generates complete C source code for the provided AST. */
    public String generateCode(Program program) {
        return translate(program, OptimizationLevel.O0, false).cCode();
    }

    public TranslationResult translate(Program program, OptimizationLevel optimization, boolean emitAssembly) {
        String cCode = buildCSource(program);
        String assembly = emitAssembly ? compilationService.compileToAssembly(cCode, optimization) : null;
        return new TranslationResult(cCode, assembly, optimization);
    }

    private String buildCSource(Program program) {
        CodeBuilder builder = new CodeBuilder();
        TranslationContext ctx = new TranslationContext(builder);

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
