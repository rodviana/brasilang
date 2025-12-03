package br.edu.pucgoias.brasilang.service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.FunctionDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.VariableDeclaration;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Assign;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Print;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ConditionalStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.RepetitionStruct;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.ReturnStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.AbstractExpression;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.Variable;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.FunctionCall;
import br.edu.pucgoias.brasilang.exception.SemanticException;

/**
 * Analisador semântico que valida tipos, declarações e referências.
 */
@Service
public class SemanticAnalyzer {

    private Map<String, EnumTokenType> variables = new HashMap<>();
    private Map<String, FunctionDeclaration> functions = new HashMap<>();

    public void analyze(Program program) {
        // Primeira passagem: coleta todas as funções declaradas
        for (AbstractStatement st : program.getStatements()) {
            if (st instanceof FunctionDeclaration func) {
                if (functions.containsKey(func.getName())) {
                    throw new SemanticException("Função '" + func.getName() + "' já foi declarada");
                }
                functions.put(func.getName(), func);
            }
        }

        // Segunda passagem: valida statements no escopo global
        for (AbstractStatement st : program.getStatements()) {
            analyzeStatement(st);
        }
    }

    private void analyzeStatement(AbstractStatement st) {
        if (st instanceof VariableDeclaration varDecl) {
            if (variables.containsKey(varDecl.getVariableName())) {
                throw new SemanticException("Variável '" + varDecl.getVariableName() + "' já foi declarada");
            }
            variables.put(varDecl.getVariableName(), varDecl.getTokenType());
        } else if (st instanceof Assign assign) {
            analyzeExpression(assign.getTarget());
            analyzeExpression(assign.getNewValue());
        } else if (st instanceof Print print) {
            analyzeExpression(print.getExpression());
        } else if (st instanceof ConditionalStruct cond) {
            analyzeExpression(cond.getFlag());
            for (AbstractStatement s : cond.getIfBody()) {
                analyzeStatement(s);
            }
            if (cond.getElseBody() != null) {
                for (AbstractStatement s : cond.getElseBody()) {
                    analyzeStatement(s);
                }
            }
        } else if (st instanceof RepetitionStruct rep) {
            analyzeExpression(rep.getFlag());
            for (AbstractStatement s : rep.getLoopBody()) {
                analyzeStatement(s);
            }
        } else if (st instanceof ReturnStatement ret) {
            analyzeExpression(ret.getExpression());
        } else if (st instanceof FunctionDeclaration func) {
            Map<String, EnumTokenType> savedVars = new HashMap<>(variables);
            // Adiciona parâmetros ao escopo local da função
            for (FunctionDeclaration.Parameter param : func.getParameters()) {
                variables.put(param.name, param.type);
            }
            // Valida corpo da função
            for (AbstractStatement s : func.getBody()) {
                analyzeStatement(s);
            }
            variables = savedVars;
        }
    }

    private void analyzeExpression(AbstractExpression expr) {
        if (expr instanceof Variable var) {
            if (!variables.containsKey(var.getName())) {
                throw new SemanticException("Variável '" + var.getName() + "' não foi declarada");
            }
        } else if (expr instanceof FunctionCall funcCall) {
            if (!functions.containsKey(funcCall.getName())) {
                throw new SemanticException("Função '" + funcCall.getName() + "' não foi declarada");
            }
            // Aqui poderíamos validar aridade e tipos de argumentos
        }
    }

    // Getters para acesso aos nomes das funções
    public String getFunctionName(FunctionDeclaration func) {
        return func.getName();
    }

    public List<FunctionDeclaration.Parameter> getParameters(FunctionDeclaration func) {
        return func.getParameters();
    }

    public List<AbstractStatement> getBody(FunctionDeclaration func) {
        return func.getBody();
    }
}
