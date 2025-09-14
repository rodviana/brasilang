package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.pucgoias.brasilang.translate.TranslationContext;

/** Expression representing a function invocation. */
public class FunctionCall implements AbstractExpression {

    private final String name;
    private final List<AbstractExpression> arguments;

    public FunctionCall(String name, List<AbstractExpression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String translate(TranslationContext ctx) {
        return name + "(" + arguments.stream()
                .map(a -> a.translate(ctx))
                .collect(Collectors.joining(", ")) + ")";
    }
}
