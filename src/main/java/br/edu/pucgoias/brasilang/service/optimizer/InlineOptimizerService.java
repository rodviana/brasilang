package br.edu.pucgoias.brasilang.service.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ReturnStatement;

/**
 * Serviço de otimização especializado em inlining de funções triviais.
 */
@Service
public class InlineOptimizerService {

    private static final int INLINE_THRESHOLD = 1;

    /**
     * Aplica otimização de inlining: identifica e remove funções triviais.
     */
    public Program optimize(Program program) {
        return removeInlinableFunctions(program);
    }

    /**
     * Identifica funções inlináveis (corpo com apenas um return statement).
     */
    public Map<String, AbstractExpression> findInlinableFunctions(Program program) {
        Map<String, AbstractExpression> inlineFunctions = new HashMap<>();

        for (AbstractStatement st : program.getStatements()) {
            if (st instanceof FunctionDeclaration func) {
                if (isInlinable(func)) {
                    ReturnStatement retStmt = (ReturnStatement) func.getBody().get(0);
                    inlineFunctions.put(func.getName(), retStmt.getExpression());
                }
            }
        }

        return inlineFunctions;
    }

    /**
     * Verifica se uma função é candidata a inlining.
     */
    private boolean isInlinable(FunctionDeclaration func) {
        List<AbstractStatement> body = func.getBody();

        if (body.size() != INLINE_THRESHOLD) {
            return false;
        }

        return body.get(0) instanceof ReturnStatement;
    }

    /**
     * Conta quantas funções são inlináveis.
     */
    public int countInlinableFunctions(Program program) {
        int count = 0;
        for (AbstractStatement st : program.getStatements()) {
            if (st instanceof FunctionDeclaration func) {
                if (isInlinable(func)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Remove funções inlináveis da AST.
     */
    private Program removeInlinableFunctions(Program program) {
        List<AbstractStatement> filtered = new ArrayList<>();

        for (AbstractStatement st : program.getStatements()) {
            if (st instanceof FunctionDeclaration func) {
                if (!isInlinable(func)) {
                    filtered.add(st);
                }
            } else {
                filtered.add(st);
            }
        }

        return new Program(filtered);
    }
}
