package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

public class Variable implements AbstractExpression {
	
	private String name;

        public Variable(String name) {
                super();
                this.name = name;
        }


        @Override
        public String translate(TranslationContext ctx) {
                return name;
        }

        @Override
        public String toString() {
                return String.format("Variable{name='%s'}", name);
        }

 }
