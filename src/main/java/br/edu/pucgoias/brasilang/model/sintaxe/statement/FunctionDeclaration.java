package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/** Represents a function definition. */
public class FunctionDeclaration implements AbstractStatement {

    public static class Parameter {
        public final String name;
        public final EnumTokenType type;

        public Parameter(String name, EnumTokenType type) {
            this.name = name;
            this.type = type;
        }
    }

    private final EnumTokenType returnType;
    private final String name;
    private final List<Parameter> parameters;
    private final List<AbstractStatement> body;

    public FunctionDeclaration(EnumTokenType returnType, String name, List<Parameter> parameters,
            List<AbstractStatement> body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public void translate(TranslationContext ctx) {
        String params = parameters.stream()
                .map(p -> ctx.toCType(p.type) + " " + p.name)
                .collect(Collectors.joining(", "));
        ctx.getBuilder().appendLine(ctx.toCType(returnType) + " " + name + "(" + params + ") {");
        ctx.getBuilder().indent();
        for (AbstractStatement st : body) {
            st.translate(ctx);
        }
        ctx.getBuilder().outdent();
        ctx.getBuilder().appendLine("}");
        ctx.getBuilder().appendLine("");
    }

    @Override
    public String toString() {
        StringBuilder paramSb = new StringBuilder();
        for (Parameter p : parameters) {
            paramSb.append("    ").append(p.name).append(":" + p.type).append("\n");
        }
        StringBuilder bodySb = new StringBuilder();
        for (AbstractStatement st : body) {
            bodySb.append("    ").append(indent(st.toString())).append("\n");
        }
        return "FunctionDeclaration{\n" +
                "  returnType=" + returnType + ",\n" +
                "  name='" + name + "',\n" +
                "  parameters=[\n" + paramSb.toString() + "  ],\n" +
                "  body=[\n" + bodySb.toString() + "  ]\n" +
                "}";
    }

    private static String indent(String str) {
        return str.replace("\n", "\n    ");
    }
}
