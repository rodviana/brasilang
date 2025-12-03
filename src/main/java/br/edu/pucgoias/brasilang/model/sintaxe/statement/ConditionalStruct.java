package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

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

        public AbstractExpression getFlag() {
                return flag;
        }

        public List<AbstractStatement> getIfBody() {
                return ifBody;
        }

        public List<AbstractStatement> getElseBody() {
                return elseBody;
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
StringBuilder ifSb = new StringBuilder();
for (AbstractStatement st : ifBody) {
ifSb.append("    ").append(indent(st.toString())).append("\n");
}
String elsePart;
if (elseBody != null) {
StringBuilder elseSb = new StringBuilder();
for (AbstractStatement st : elseBody) {
elseSb.append("    ").append(indent(st.toString())).append("\n");
}
elsePart = "[\n" + elseSb.toString() + "  ]";
} else {
elsePart = "null";
}
return "ConditionalStruct{\n" +
"  flag=" + flag + ",\n" +
"  ifBody=[\n" + ifSb.toString() + "  ],\n" +
"  elseBody=" + elsePart + "\n" +
"}";
}

private static String indent(String str) {
return str.replace("\n", "\n    ");
}

 }
