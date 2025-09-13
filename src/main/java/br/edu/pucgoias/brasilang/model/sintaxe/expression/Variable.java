package br.edu.pucgoias.brasilang.model.sintaxe.expression;

public class Variable implements AbstractExpression {
	
	private String name;

        public Variable(String name) {
                super();
                this.name = name;
        }


        @Override
        public String toString() {
                return String.format("Variable{name='%s'}", name);
        }

 }
