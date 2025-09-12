package br.edu.pucgoias.brasilang.model.sintaxe.expression;

public class BinaryOperation implements AbstractExpression{
        private String operator;
        private AbstractExpression leftExpression;
        private AbstractExpression rightExpression;

        public BinaryOperation(String operator, AbstractExpression leftExpression, AbstractExpression rightExpression) {
                this.operator = operator;
                this.leftExpression = leftExpression;
                this.rightExpression = rightExpression;
        }
}
