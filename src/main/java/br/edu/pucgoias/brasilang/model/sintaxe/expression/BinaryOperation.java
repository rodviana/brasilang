package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class BinaryOperation implements AbstractExpression{
        private String operator;
        private AbstractExpression leftExpression;
        private AbstractExpression rightExpression;

        public BinaryOperation(String operator, AbstractExpression leftExpression, AbstractExpression rightExpression) {
                this.operator = operator;
                this.leftExpression = leftExpression;
                this.rightExpression = rightExpression;
        }

        @Override
        public String translate(TranslationContext ctx) {
                String op = switch (operator) {
                        case "e" -> "&&";
                        case "ou" -> "||";
                        default -> operator; // outros operadores permanecem iguais
                };
                return leftExpression.translate(ctx) + " " + op + " " + rightExpression.translate(ctx);
        }

        @Override
        public String toString() {
                return String.format("BinaryOperation{operator='%s', leftExpression=%s, rightExpression=%s}", operator, leftExpression, rightExpression);
        }
}
