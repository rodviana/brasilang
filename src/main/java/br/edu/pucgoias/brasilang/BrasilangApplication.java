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
        // Exemplo abrangente: demonstra tipos, arrays, condicionais, laços, funções e chamada recursiva

        imprima("=== Exemplo: recursos principais e fatorial recursivo ===");

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

        // Função recursiva para calcular fatorial de n
        funcao inteiro fatorial(inteiro n) {
            se (n <= 1) {
                retorne 1;
            } senao {
                retorne n * fatorial(n - 1);
            }
        }

        // Usa a função fatorial para calcular fatorial de 5
        inteiro n = 5;
        inteiro resultado = fatorial(n);
        imprima("Fatorial de 5:");
        imprima(resultado);

        // Loop e array
        inteiro arr[6];
        inteiro i = 0;
        enquanto (i < 6) {
            arr[i] = fatorial(i); // chama fatorial dentro do loop
            imprima(arr[i]);
            i = i + 1;
        }

        // Condicional usando operadores lógicos
        se (resultado > 100 e flag) {
            imprima("Resultado grande e flag verdadeiro");
        } senao {
            imprima("Resultado pequeno ou flag falso");
        }

        // Demonstração de chamada aninhada e uso de funções auxiliares
        funcao inteiro adiciona(inteiro x, inteiro y) {
            retorne x + y;
        }
        inteiro soma = adiciona(2, adiciona(3, 4));
        imprima("Soma aninhada:");
        imprima(soma);

        imprima("=== Fim do exemplo ===");
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
