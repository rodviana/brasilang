package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class Assign implements AbstractStatement {

        private AbstractExpression target;
        private AbstractExpression newValue;

        public Assign(AbstractExpression target, AbstractExpression newValue) {
                super();
                this.target = target;
                this.newValue = newValue;
        }

        public AbstractExpression getTarget() {
                return target;
        }

        public AbstractExpression getNewValue() {
                return newValue;
        }

        @Override
        public void translate(TranslationContext ctx) {
                ctx.getBuilder()
                                .appendLine(target.translate(ctx) + " = " + newValue.translate(ctx) + ";");
        }

        @Override
        public String toString() {
                return "Assign{\n" +
                                "  target=" + target + ",\n" +
                                "  newValue=" + newValue + "\n" +
                                "}";
        }
}
