package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.translate.TranslationContext;

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

        @Override
        public void translate(TranslationContext ctx) {
                ctx.getBuilder().appendLine("if (" + flag.translate(ctx) + ") {");
                ctx.getBuilder().indent();
                for (AbstractStatement st : ifBody) {
                        st.translate(ctx);
                }
                ctx.getBuilder().outdent();
                if (elseBody != null && !elseBody.isEmpty()) {
                        ctx.getBuilder().appendLine("} else {");
                        ctx.getBuilder().indent();
                        for (AbstractStatement st : elseBody) {
                                st.translate(ctx);
                        }
                        ctx.getBuilder().outdent();
                }
                ctx.getBuilder().appendLine("}");
        }

        @Override
        public String toString() {
                return String.format("ConditionalStruct{flag=%s, ifBody=%s, elseBody=%s}", flag, ifBody, elseBody);
        }

 }
