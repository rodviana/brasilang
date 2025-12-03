package br.edu.pucgoias.brasilang.service.optimizer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ReturnStatement;

/**
 * Serviço de otimização que elimina código morto.
 * Remove statements que nunca serão executados após um retorne, break ou continue.
 */
@Service
public class DeadCodeEliminationService {

    /**
     * Aplica otimização de eliminação de código morto.
     */
    public Program optimize(Program program) {
        List<AbstractStatement> optimized = new ArrayList<>();
        for (AbstractStatement st : program.getStatements()) {
            optimized.addAll(optimizeStatement(st));
        }
        return new Program(optimized);
    }

    /**
     * Otimiza um statement e retorna lista (vazia se for código morto, ou com o statement otimizado).
     */
    private List<AbstractStatement> optimizeStatement(AbstractStatement st) {
        List<AbstractStatement> result = new ArrayList<>();

        if (st instanceof ReturnStatement) {
            result.add(st);
            // Marca que o próximo código será morto
            return result;
        }

        if (st instanceof ConditionalStruct cond) {
            List<AbstractStatement> optimizedIf = optimizeBlock(cond.getIfBody());
            List<AbstractStatement> optimizedElse = cond.getElseBody() != null 
                ? optimizeBlock(cond.getElseBody()) 
                : null;
            result.add(new ConditionalStruct(cond.getFlag(), optimizedIf, optimizedElse));
            return result;
        }

        if (st instanceof RepetitionStruct rep) {
            List<AbstractStatement> optimizedBody = optimizeBlock(rep.getLoopBody());
            result.add(new RepetitionStruct(rep.getType(), rep.getFlag(), optimizedBody));
            return result;
        }

        if (st instanceof FunctionDeclaration func) {
            List<AbstractStatement> optimizedBody = optimizeBlock(func.getBody());
            result.add(new FunctionDeclaration(func.getReturnType(), func.getName(), 
                func.getParameters(), optimizedBody));
            return result;
        }

        result.add(st);
        return result;
    }

    /**
     * Otimiza bloco de statements, parando quando encontra um retorne.
     */
    private List<AbstractStatement> optimizeBlock(List<AbstractStatement> block) {
        List<AbstractStatement> optimized = new ArrayList<>();
        for (AbstractStatement st : block) {
            List<AbstractStatement> optimizedSt = optimizeStatement(st);
            optimized.addAll(optimizedSt);
            
            // Se o statement é um retorne, o resto do bloco é código morto
            if (st instanceof ReturnStatement) {
                break;
            }
        }
        return optimized;
    }

    /**
     * Conta quantos statements de código morto foram removidos.
     */
    public int countDeadStatements(Program program) {
        int count = 0;
        for (AbstractStatement st : program.getStatements()) {
            count += countDeadInStatement(st);
        }
        return count;
    }

    private int countDeadInStatement(AbstractStatement st) {
        int count = 0;

        if (st instanceof ConditionalStruct cond) {
            count += countDeadInBlock(cond.getIfBody());
            if (cond.getElseBody() != null) {
                count += countDeadInBlock(cond.getElseBody());
            }
        }

        if (st instanceof RepetitionStruct rep) {
            count += countDeadInBlock(rep.getLoopBody());
        }

        if (st instanceof FunctionDeclaration func) {
            count += countDeadInBlock(func.getBody());
        }

        return count;
    }

    private int countDeadInBlock(List<AbstractStatement> block) {
        int count = 0;
        boolean foundReturn = false;

        for (AbstractStatement st : block) {
            if (foundReturn) {
                count++; // Este statement é código morto
            }

            if (st instanceof ReturnStatement) {
                foundReturn = true;
            }

            count += countDeadInStatement(st);
        }

        return count;
    }
}
