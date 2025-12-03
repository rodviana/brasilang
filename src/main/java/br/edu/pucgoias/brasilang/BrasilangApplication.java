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
import br.edu.pucgoias.brasilang.service.SemanticAnalyzer;
import br.edu.pucgoias.brasilang.exception.LexicalException;
import br.edu.pucgoias.brasilang.exception.SyntaxException;
import br.edu.pucgoias.brasilang.exception.SemanticException;
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
  @Autowired
  SemanticAnalyzer semanticAnalyzer;

  @PostConstruct
  void executar() {
    String src = """
        // Exemplo abrangente: tipos, arrays, condicionais, laços, funções, casts e ERROS LÉXICOS

        imprima("=== Exemplo: recursos e demonstração de erros léxicos ===");

        // Tipos básicos
        inteiro a = 10;
        inteiro b = 3;
        flutuante f = 2.5;
        duplo d = 3.14159;
        caractere c = 'Z';
        string msg = "Ola, Brasilang!";
        booleano flag = verdadeiro;

        imprima(msg);
        imprima(a);
        imprima(f);
        imprima(c);

        // Função recursiva
        funcao inteiro fatorial(inteiro n) {
            se (n <= 1) {
                retorne 1;
            } senao {
                retorne n * fatorial(n - 1);
            }
        }

        inteiro n = 5;
        inteiro resultado = fatorial(n);
        imprima("Fatorial de 5:");
        imprima(resultado);

        // Loop e array
        inteiro arr[6];
        inteiro i = 0;
        enquanto (i < 6) {
            arr[i] = fatorial(i);
            imprima(arr[i]);
            i = i + 1;
        }

        // Função auxiliar
        funcao inteiro adiciona(inteiro x, inteiro y) {
            retorne x + y;
        }
        inteiro soma = adiciona(2, adiciona(3, 4));
        imprima("Soma aninhada:");
        imprima(soma);

        // === Exemplos de CAST ===
        imprima("=== Exemplos de Casts ===");

        flutuante num_flutuante = 3.99;
        inteiro num_inteiro = (inteiro) num_flutuante;
        imprima("Cast flutuante para inteiro:");
        imprima(num_inteiro);

        inteiro x = 42;
        flutuante x_float = (flutuante) x;
        imprima("Cast inteiro para flutuante:");
        imprima(x_float);

        inteiro resultado_cast = (inteiro) (2.7 + 3.5);
        imprima("Cast de soma em expressão:");
        imprima(resultado_cast);

        duplo valor_duplo = 5.999;
        inteiro valor_convertido = (inteiro) valor_duplo;
        imprima("Cast duplo para inteiro:");
        imprima(valor_convertido);

        // === EXEMPLOS DE ERROS LÉXICOS (comentados para não quebrar compilação) ===
        // imprima("=== Exemplos de ERROS LÉXICOS (descomente para testar) ===");

        // ERRO 1: String literal não terminada
        // string nao_terminada = "Ola sem fechar;

        // ERRO 2: Character literal não terminado
        // caractere char_invalido = 'A;

        // ERRO 3: Caractere inválido não reconhecido pelo lexer
        // inteiro y = 10 @ 5;

        // ERRO 4: Outro caractere especial inválido
        // inteiro z = 5 $ 3;

        // ERRO 5: Comentário de bloco não fechado
        // /* Este comentario nao fecha

        imprima("=== Fim do exemplo ===");
        """;
    try {
      Lexer lexer = new Lexer(src);
      List<Token> tokenList = lexerService.buildTokenList(lexer);

      tokenList.forEach(token -> System.out.println(token.toString()));
      Sintaxe sintaxe = new Sintaxe(tokenList);
      List<AbstractStatement> statements = sintaxeService.buildProgramStatementList(sintaxe);

      statements.forEach(statement -> System.out.println(statement.toString()));

      Program program = new Program(statements);
      
      // Análise semântica
      semanticAnalyzer.analyze(program);
      System.out.println("Análise semântica: OK");
      
      String cCode = translateService.generateCode(program);
      System.out.println(cCode);
    } catch (LexicalException e) {
      System.err.println("Erro léxico encontrado: " + e.getMessage());
      e.printStackTrace();
    } catch (SyntaxException e) {
      System.err.println("Erro sintático encontrado: " + e.getMessage());
      e.printStackTrace();
    } catch (SemanticException e) {
      System.err.println("Erro semântico encontrado: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Erro durante compilação: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
