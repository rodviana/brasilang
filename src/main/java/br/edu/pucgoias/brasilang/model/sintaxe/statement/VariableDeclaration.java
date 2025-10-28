package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class VariableDeclaration implements AbstractStatement {

        private String variableName;
        private EnumTokenType tokenType;
        private AbstractExpression initialization;

        public VariableDeclaration(String variableName, EnumTokenType tokenType, AbstractExpression initialization) {
                super();
                this.variableName = variableName;
                this.tokenType = tokenType;
                this.initialization = initialization;
        }

        @Override
        public void translate(TranslationContext ctx) {
                String cType = ctx.toCType(tokenType);
                ctx.declareVariable(variableName, cType);
                String line = cType + " " + variableName;
                if (initialization != null) {
                        line += " = " + initialization.translate(ctx);
                }
                line += ";";
                ctx.getBuilder().appendLine(line);
        }

        @Override
        public String toString() {
                return "VariableDeclaration{\n" +
                                "  variableName='" + variableName + "',\n" +
                                "  tokenType=" + tokenType + ",\n" +
                                "  initialization=" + initialization + "\n" +
                                "}";
        }

}
