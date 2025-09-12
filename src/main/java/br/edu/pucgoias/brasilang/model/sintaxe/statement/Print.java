package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;

public class Print implements AbstractStatement {

    private final AbstractExpression expression;

    public Print(AbstractExpression expression) {
        this.expression = expression;
    }

    public AbstractExpression getExpression() {
        return expression;
    }
}
