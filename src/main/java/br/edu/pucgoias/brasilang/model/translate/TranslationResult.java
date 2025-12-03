package br.edu.pucgoias.brasilang.model.translate;

/** Resultado de tradução contendo o código C e, opcionalmente, o assembly gerado. */
public record TranslationResult(String cCode, String assembly, OptimizationLevel optimizationLevel) {

    public boolean hasAssembly() {
        return assembly != null && !assembly.isEmpty();
    }
}
