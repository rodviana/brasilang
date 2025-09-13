package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;

public class RepetitionStruct implements AbstractStatement{
	
	private EnumTokenType type;
	private AbstractExpression flag;
	private List<AbstractStatement> loopBody;
        public RepetitionStruct(EnumTokenType type, AbstractExpression flag, List<AbstractStatement> loopBody) {
                super();
                this.type = type;
                this.flag = flag;
                this.loopBody = loopBody;
        }



        @Override
        public String toString() {
                return String.format("RepetitionStruct{type=%s, flag=%s, loopBody=%s}", type, flag, loopBody);
        }

 }
