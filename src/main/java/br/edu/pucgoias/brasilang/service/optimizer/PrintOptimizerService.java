package br.edu.pucgoias.brasilang.service.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Print;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;

/**
 * Serviço de otimização especializado em remoção de statements Print (imprima).
 */
@Service
public class PrintOptimizerService {

    /**
     * Remove todos os statements de impressão (Print) da AST.
     */
    public Program optimize(Program program) {
        List<AbstractStatement> optimized = new ArrayList<>();
        for (AbstractStatement st : program.getStatements()) {
            AbstractStatement optimizedSt = optimizeStatement(st);
            if (optimizedSt != null) {
                optimized.add(optimizedSt);
            }
        }
        return new Program(optimized);
    }

    private AbstractStatement optimizeStatement(AbstractStatement st) {
        if (st instanceof Print) {
            return null;
        }

        if (st instanceof ConditionalStruct cond) {
            List<AbstractStatement> optimizedIf = optimizeBlock(cond.getIfBody());
            List<AbstractStatement> optimizedElse = cond.getElseBody() != null 
                ? optimizeBlock(cond.getElseBody()) 
                : null;
            return new ConditionalStruct(cond.getFlag(), optimizedIf, optimizedElse);
        }

        if (st instanceof RepetitionStruct rep) {
            List<AbstractStatement> optimizedBody = optimizeBlock(rep.getLoopBody());
            return new RepetitionStruct(rep.getType(), rep.getFlag(), optimizedBody);
        }

        if (st instanceof FunctionDeclaration func) {
            List<AbstractStatement> optimizedBody = optimizeBlock(func.getBody());
            return new FunctionDeclaration(func.getReturnType(), func.getName(), 
                func.getParameters(), optimizedBody);
        }

        return st;
    }

    private List<AbstractStatement> optimizeBlock(List<AbstractStatement> block) {
        List<AbstractStatement> optimized = new ArrayList<>();
        for (AbstractStatement st : block) {
            AbstractStatement optimizedSt = optimizeStatement(st);
            if (optimizedSt != null) {
                optimized.add(optimizedSt);
            }
        }
        return optimized;
    }

    /**
     * Conta quantos statements Print existem na AST.
     */
    public int countPrints(Program program) {
        int count = 0;
        for (AbstractStatement st : program.getStatements()) {
            count += countPrintsInStatement(st);
        }
        return count;
    }

    private int countPrintsInStatement(AbstractStatement st) {
        int count = 0;
        if (st instanceof Print) {
            return 1;
        }
        if (st instanceof ConditionalStruct cond) {
            for (AbstractStatement s : cond.getIfBody()) {
                count += countPrintsInStatement(s);
            }
            if (cond.getElseBody() != null) {
                for (AbstractStatement s : cond.getElseBody()) {
                    count += countPrintsInStatement(s);
                }
            }
        }
        if (st instanceof RepetitionStruct rep) {
            for (AbstractStatement s : rep.getLoopBody()) {
                count += countPrintsInStatement(s);
            }
        }
        if (st instanceof FunctionDeclaration func) {
            for (AbstractStatement s : func.getBody()) {
                count += countPrintsInStatement(s);
            }
        }
        return count;
    }
}
