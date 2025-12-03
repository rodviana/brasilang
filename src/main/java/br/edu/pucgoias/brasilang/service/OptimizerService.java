package br.edu.pucgoias.brasilang.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.optimizer.EnumOptimizerParameter;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.service.optimizer.DeadCodeEliminationService;
import br.edu.pucgoias.brasilang.service.optimizer.InlineOptimizerService;
import br.edu.pucgoias.brasilang.service.optimizer.PrintOptimizerService;

/**
 * Serviço gerenciador centralizado de otimizações.
 * Orquestra múltiplos otimizadores especializados baseado em parâmetros.
 */
@Service
public class OptimizerService {

    @Autowired
    private PrintOptimizerService printOptimizerService;

    @Autowired
    private InlineOptimizerService inlineOptimizerService;

    @Autowired
    private DeadCodeEliminationService deadCodeEliminationService;

    /**
     * Aplica otimizações conforme especificado na lista de parâmetros.
     */
    public Program optimize(Program program, List<EnumOptimizerParameter> parameterList) {
        Program optimized = program;

        for (EnumOptimizerParameter param : parameterList) {
            optimized = applyOptimization(optimized, param);
        }

        return optimized;
    }

    private Program applyOptimization(Program program, EnumOptimizerParameter param) {
        switch (param) {
            case REMOVE_PRINTS:
                int printCount = printOptimizerService.countPrints(program);
                Program result = printOptimizerService.optimize(program);
                System.out.println("Otimização REMOVE_PRINTS: " + printCount + " statements removidos");
                return result;

            case INLINE_FUNCTIONS:
                int inlineCount = inlineOptimizerService.countInlinableFunctions(program);
                Program inlineResult = inlineOptimizerService.optimize(program);
                System.out.println("Otimização INLINE_FUNCTIONS: " + inlineCount + " funções inlináveis removidas");
                return inlineResult;

            case REMOVE_DEAD_CODE:
                int deadCount = deadCodeEliminationService.countDeadStatements(program);
                Program deadResult = deadCodeEliminationService.optimize(program);
                System.out.println("Otimização REMOVE_DEAD_CODE: " + deadCount + " statements mortos removidos");
                return deadResult;

            case ALL:
                Program allOptimized = program;
                allOptimized = applyOptimization(allOptimized, EnumOptimizerParameter.REMOVE_PRINTS);
                allOptimized = applyOptimization(allOptimized, EnumOptimizerParameter.INLINE_FUNCTIONS);
                allOptimized = applyOptimization(allOptimized, EnumOptimizerParameter.REMOVE_DEAD_CODE);
                return allOptimized;

            default:
                return program;
        }
    }

    /**
     * Retorna informações sobre otimizações disponíveis.
     */
    public void printAvailableOptimizations() {
        System.out.println("=== Otimizações disponíveis ===");
        for (EnumOptimizerParameter param : EnumOptimizerParameter.values()) {
            System.out.println("  - " + param.name() + ": " + param.getDescription());
        }
    }
}
