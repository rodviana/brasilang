package br.edu.pucgoias.brasilang.model.sintaxe.expression;

import java.util.List;
import java.util.stream.Collectors;

import br.edu.pucgoias.brasilang.model.translate.TranslationContext;

/**
 * Representa um acesso a um elemento de um vetor ou matriz. Ex: `meuVetor[i]`
 * ou `minhaMatriz[i][j]`
 */
public class ArrayAccess implements AbstractExpression {

    private final String variableName;
    private final List<AbstractExpression> indices;

    public ArrayAccess(String variableName, List<AbstractExpression> indices) {
        this.variableName = variableName;
        this.indices = indices;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public String translate(TranslationContext ctx) {
        StringBuilder access = new StringBuilder(variableName);
        for (AbstractExpression idx : indices) {
            access.append("[").append(idx.translate(ctx)).append("]");
        }
        return access.toString();
    }

    @Override
    public String toString() {
        String indicesStr = indices.stream().map(Object::toString).collect(Collectors.joining("]["));
        return "ArrayAccess{variableName='" + variableName + "', indices=[" + indicesStr + "]}";
    }
}