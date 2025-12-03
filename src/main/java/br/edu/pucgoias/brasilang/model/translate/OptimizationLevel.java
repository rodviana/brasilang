package br.edu.pucgoias.brasilang.model.translate;

/** Mapeia níveis de otimização aceitos pelo GCC. */
public enum OptimizationLevel {
    O0, O1, O2;

    public String toFlag() {
        return "-" + name();
    }
}
