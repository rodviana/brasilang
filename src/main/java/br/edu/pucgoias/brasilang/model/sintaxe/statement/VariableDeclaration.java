package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class VariableDeclaration implements AbstractStatement {
        private final String variableName;
        private final EnumTokenType tokenType;
        private final List<AbstractExpression> dimensions; // Para declaracao de vetores/matrizes
        private final AbstractExpression initialization;

        public VariableDeclaration(String variableName, EnumTokenType tokenType, List<AbstractExpression> dimensions,
                        AbstractExpression initialization) {
                super();
                this.variableName = variableName;
                this.tokenType = tokenType;
                this.dimensions = dimensions;
                this.initialization = initialization;
        }

        @Override
        public void translate(TranslationContext ctx) {
                String cType = ctx.toCType(tokenType);
                ctx.declareVariable(variableName, cType); // Mantem o tipo base para consulta
                StringBuilder lineBuilder = new StringBuilder();
                lineBuilder.append(cType).append(" ").append(variableName);
                if (dimensions != null && !dimensions.isEmpty()) {
                        for (AbstractExpression dim : dimensions) {
                                lineBuilder.append("[").append(dim.translate(ctx)).append("]");
                        }
                }
                String line = lineBuilder.toString();
                // A inicializacao de vetores/matrizes nao eh suportada nesta implementacao
                if (initialization != null && (dimensions == null || dimensions.isEmpty())) {
                        String initValue = initialization.translate(ctx);
                        // Adiciona aspas simples para literais de char na declaração
                        if (tokenType == EnumTokenType.CHAR && initValue.length() == 1) {
                                initValue = "'" + initValue + "'";
                        }
                        line += " = " + initValue;
                }
                line += ";";
                ctx.getBuilder().appendLine(line);
        }

        @Override
        public String toString() {
                return "VariableDeclaration{\n" +
                                "  variableName='" + variableName + "',\n" +
                                "  tokenType=" + tokenType + ",\n" +
                                "  dimensions=" + dimensions + ",\n" +
                                "  initialization=" + initialization + "\n" +
                                "}";
        }

}
