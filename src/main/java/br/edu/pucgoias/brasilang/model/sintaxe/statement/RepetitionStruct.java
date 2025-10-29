package br.edu.pucgoias.brasilang.model.sintaxe.statement;

import java.util.List;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

public class RepetitionStruct implements AbstractStatement {

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
        public void translate(TranslationContext ctx) {
                switch (type) {
                        case ENQUANTO:
                                ctx.getBuilder().appendLine("while (" + flag.translate(ctx) + ") {");
                                ctx.getBuilder().indent();
                                for (AbstractStatement st : loopBody) {
                                        st.translate(ctx);
                                }
                                ctx.getBuilder().outdent();
                                ctx.getBuilder().appendLine("}");
                                break;
                        case REPITA: // Assumindo que REPITA seja o token para um loop do-while
                                ctx.getBuilder().appendLine("do {");
                                ctx.getBuilder().indent();
                                for (AbstractStatement st : loopBody) {
                                        st.translate(ctx);
                                }
                                ctx.getBuilder().outdent();
                                ctx.getBuilder().appendLine("} while (" + flag.translate(ctx) + ");");
                                break;
                }
        }

        @Override
        public String toString() {
                StringBuilder body = new StringBuilder();
                for (AbstractStatement st : loopBody) {
                        body.append("    ").append(indent(st.toString())).append("\n");
                }
                return "RepetitionStruct{\n" +
                                "  type=" + type + ",\n" +
                                "  flag=" + flag + ",\n" +
                                "  loopBody=[\n" + body.toString() + "  ]\n" +
                                "}";
        }

        private static String indent(String str) {
                return str.replace("\n", "\n    ");
        }

}
