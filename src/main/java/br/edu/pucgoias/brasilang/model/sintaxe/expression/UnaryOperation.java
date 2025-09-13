package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

public class UnaryOperation implements AbstractExpression {
        private String operator;
        private AbstractExpression expression;

        public UnaryOperation(String operator, AbstractExpression expression) {
                this.operator = operator;
                this.expression = expression;
        }

        @Override
        public String translate(TranslationContext ctx) {
                return operator + expression.translate(ctx);
        }

        @Override
        public String toString() {
                return String.format("UnaryOperation{operator='%s', expression=%s}", operator, expression);
        }
}
