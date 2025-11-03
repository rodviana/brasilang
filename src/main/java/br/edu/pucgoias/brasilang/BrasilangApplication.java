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
        // Teste de Matrizes
        imprima("--- Testando Matrizes ---");
        inteiro minhaMatriz[2][3];
        inteiro i = 0;
        inteiro j = 0;
        enquanto (i < 2) {
            j = 0;
            enquanto (j < 3) {
                minhaMatriz[i][j] = i * 10 + j;
                imprima(minhaMatriz[i][j]);
                j = j + 1;
            }
            i = i + 1;
        }
        imprima("Fim do teste de matrizes.");
        imprima("\\n");

        // Teste de Booleanos e Strings
        imprima("--- Testando Booleanos e Strings ---");
        booleano ok = verdadeiro;
        se (ok e (1 < 2)) {
            imprima("Teste booleano: SUCESSO");
        } senao {
            imprima("Teste booleano: FALHOU");
        }

        caractere letra = 'B';
        imprima(letra);

        string saudacao = "Ola, Brasilang!";
        imprima(saudacao);
        imprima("\\nFim dos testes.");
        """;
    Lexer lexer = new Lexer(src);
    List<Token> tokenList = lexerService.buildTokenList(lexer);

    tokenList.forEach(token -> System.out.println(token.toString()));
    Sintaxe sintaxe = new Sintaxe(tokenList);
    List<AbstractStatement> statements = sintaxeService.buildProgramStatementList(sintaxe);

    statements.forEach(statement -> System.out.println(statement.toString()));

    Program program = new Program(statements);
    String cCode = translateService.generateCode(program);
    System.out.println(cCode);

  }
}
