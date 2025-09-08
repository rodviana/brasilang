package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;

public class ConditionalStruct implements AbstractStatement {
	
	AbstractExpression flag;
	List<AbstractStatement> ifBody;
	List<AbstractStatement> elseBody;
	public ConditionalStruct(AbstractExpression flag, List<AbstractStatement> ifBody,
			List<AbstractStatement> elseBody) {
		super();
		this.flag = flag;
		this.ifBody = ifBody;
		this.elseBody = elseBody;
	}
	
}
