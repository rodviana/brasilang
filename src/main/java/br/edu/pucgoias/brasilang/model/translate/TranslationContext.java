package br.edu.pucgoias.brasilang.model.translate;

import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;

/** Holds contextual information during translation to C. */
public class TranslationContext {

    private final CodeBuilder builder;
    private final Set<String> includes = new LinkedHashSet<>();
    private final Map<String, String> variables = new HashMap<>();

    public TranslationContext(CodeBuilder builder) {
        this.builder = builder;
    }

    public CodeBuilder getBuilder() {
        return builder;
    }

    public void addInclude(String include) {
        includes.add(include);
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void declareVariable(String name, String type) {
        variables.put(name, type);
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String toCType(EnumTokenType type) {
        if (type == null)
            return "int";
        return switch (type) {
            case INT -> "int";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            case VOID -> "void";
            case BOOL -> {
                this.addInclude("<stdbool.h>");
                yield "bool";
            }
            case CHAR -> "char";
            case STRING -> "char*";
            default -> throw new IllegalArgumentException("Tipo de token não mapeado para C: " + type);
        };
    }
}