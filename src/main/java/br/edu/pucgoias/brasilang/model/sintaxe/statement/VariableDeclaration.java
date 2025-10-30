package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class VariableDeclaration implements AbstractStatement {

        private String variableName;
        private EnumTokenType tokenType;
        private AbstractExpression size; // Para declaracao de vetores
        private AbstractExpression initialization;

        public VariableDeclaration(String variableName, EnumTokenType tokenType, AbstractExpression size,
                        AbstractExpression initialization) {
                super();
                this.variableName = variableName;
                this.tokenType = tokenType;
                this.size = size;
                this.initialization = initialization;
        }

        @Override
        public void translate(TranslationContext ctx) {
                String cType = ctx.toCType(tokenType);
                ctx.declareVariable(variableName, cType); // Mantem o tipo base para consulta
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(cType).append(" ").append(variableName);
                if (size != null) {
                        lineBuilder.append("[").append(size.translate(ctx)).append("]");
                }
                String line = lineBuilder.toString();
                // A inicializacao de vetores inteiros nao eh suportada nesta implementacao
                if (initialization != null && size == null) {
                        line += " = " + initialization.translate(ctx);
                }
                line += ";";
                ctx.getBuilder().appendLine(line);
        }

        @Override
        public String toString() {
                return "VariableDeclaration{\n" +
                                "  variableName='" + variableName + "',\n" +
                                "  tokenType=" + tokenType + ",\n" + // Corrigido
                                "  size=" + size + ",\n" +
                                "  initialization=" + initialization + "\n" + // Corrigido
                                "}";
        }

}
