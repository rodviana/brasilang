package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

public class Literal implements AbstractExpression{
	
	private Object value;

        public Literal(Object value) {
                super();
                this.value = value;
        }


        @Override
        public String translate(TranslationContext ctx) {
                if (value instanceof String) {
                        return "\"" + value + "\"";
                }
                return String.valueOf(value);
        }

        @Override
        public String toString() {
                return String.format("Literal{value=%s}", value);
        }

 }
