package br.edu.pucgoias.brasilang;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.edu.pucgoias.brasilang.model.lexico.Lexer;
import br.edu.pucgoias.brasilang.model.lexico.Token;
import br.edu.pucgoias.brasilang.model.sintaxe.Sintaxe;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.AbstractStatement;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.Program;
import br.edu.pucgoias.brasilang.model.translate.OptimizationLevel;
import br.edu.pucgoias.brasilang.model.translate.TranslationResult;
import br.edu.pucgoias.brasilang.service.LexerService;
import br.edu.pucgoias.brasilang.service.SintaxeService;
import br.edu.pucgoias.brasilang.service.TranslateService;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BrasilangApplication {

  public static void main(String[] args) {
    SpringApplication.run(BrasilangApplication.class, args);
  }

  @Autowired
  LexerService lexerService;
  @Autowired
  SintaxeService sintaxeService;
  @Autowired
  TranslateService translateService;

  @PostConstruct
  void executar() {
    String src = """
        funcao inteiro fatorial(inteiro n) {
            se (n <= 1) {
                retorne 1;
            }
            retorne n * fatorial(n - 1);
        }

        funcao flutuante paraFlutuante(inteiro valor) {
            retorne (flutuante) valor;
        }

        imprima("--- Testando funcoes, recursao e casting ---");
        inteiro numero = 5;
        inteiro resultado = fatorial(numero);
        imprima("Fatorial:");
        imprima(resultado);

        flutuante numeroReal = paraFlutuante(numero);
        imprima("Numero como flutuante:");
        imprima(numeroReal);
        """;
    Lexer lexer = new Lexer(src);
    List<Token> tokenList = lexerService.buildTokenList(lexer);

    tokenList.forEach(token -> System.out.println(token.toString()));
    Sintaxe sintaxe = new Sintaxe(tokenList);
    List<AbstractStatement> statements = sintaxeService.buildProgramStatementList(sintaxe);

    statements.forEach(statement -> System.out.println(statement.toString()));

    Program program = new Program(statements);
    TranslationResult result = translateService.translate(program, OptimizationLevel.O2, true);
    System.out.println(result.cCode());
    if (result.hasAssembly()) {
      System.out.println("\n/* Assembly gerado (O2) */");
      System.out.println(result.assembly());
    }

  }
}
