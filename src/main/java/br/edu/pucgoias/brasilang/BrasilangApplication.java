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
        // Exemplo abrangente: demonstra tipos, arrays, condicionais, laços, funções, chamadas recursivas e casts

        imprima("=== Exemplo: recursos principais, fatorial recursivo e casts ===");

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
            arr[i] = fatorial(i);
            imprima(arr[i]);
            i = i + 1;
        }

        // Condicional usando operadores lógicos
        se (resultado > 100 e flag) {
            imprima("Resultado grande e flag verdadeiro");
        } senao {
            imprima("Resultado pequeno ou flag falso");
        }

        // Demonstração de chamada aninhada
        funcao inteiro adiciona(inteiro x, inteiro y) {
            retorne x + y;
        }
        inteiro soma = adiciona(2, adiciona(3, 4));
        imprima("Soma aninhada:");
        imprima(soma);

        // === Exemplos de CAST ===
        imprima("=== Exemplos de Casts ===");

        // Cast de flutuante para inteiro
        flutuante num_flutuante = 3.99;
        inteiro num_inteiro = (inteiro) num_flutuante;
        imprima("Cast flutuante para inteiro:");
        imprima(num_inteiro);

        // Cast de inteiro para flutuante
        inteiro x = 42;
        flutuante x_float = (flutuante) x;
        imprima("Cast inteiro para flutuante:");
        imprima(x_float);

        // Cast em expressão aritmética
        inteiro resultado_cast = (inteiro) (2.7 + 3.5);
        imprima("Cast de soma em expressão:");
        imprima(resultado_cast);

        // Cast em chamada de função
        duplo valor_duplo = 5.999;
        inteiro valor_convertido = (inteiro) valor_duplo;
        imprima("Cast duplo para inteiro:");
        imprima(valor_convertido);

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
