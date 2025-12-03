package br.edu.pucgoias.brasilang.model.optimizer;

/**
 * Enum que define os tipos de otimizações disponíveis no compilador.
 */
public enum EnumOptimizerParameter {
    REMOVE_PRINTS("Remove todos os statements de impressão (imprima)"),
    INLINE_FUNCTIONS("Faz inlining de funções triviais (apenas return)"),
    REMOVE_DEAD_CODE("Remove código morto após return"),
    ALL("Aplica todas as otimizações disponíveis");

    private final String description;

    EnumOptimizerParameter(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
