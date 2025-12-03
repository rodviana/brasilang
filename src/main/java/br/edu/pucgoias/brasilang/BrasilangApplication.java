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
        // Exemplo abrangente: demonstra tipos, arrays, funções, casts e ERROS

        imprima("=== Programa: recursos e demonstração de erros ===");

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

        imprima("=== Fim do exemplo principal ===");

        // === EXEMPLOS DE ERROS AO FINAL (descomente um por vez para testar) ===

        // ERRO LÉXICO 1: String literal não terminada
        // string nao_fechada = "Ola sem fechar;

        // ERRO LÉXICO 2: Character literal não terminado
        // caractere char_aberto = 'A;

        // ERRO LÉXICO 3: Caractere inválido não reconhecido
        // inteiro teste = 10 @ 5;

        // ERRO LÉXICO 4: Comentário de bloco não fechado
        // /* Este comentario nao fecha
        // inteiro x = 5;

        // ERRO SINTÁTICO 1: Falta de ponto e vírgula
        // inteiro sem_semi = 10

        // ERRO SINTÁTICO 2: Parêntese não fechado
        // se (a > 5 {
        //     imprima(a);
        // }

        // ERRO SINTÁTICO 3: Chave não fechada
        // enquanto (i < 10) {
        //     i = i + 1;

        // ERRO SINTÁTICO 4: Falta de ')' em chamada de função
        // inteiro resultado2 = fatorial(5;

        // ERRO SINTÁTICO 5: Falta de '(' após 'imprima'
        // imprima "erro";

        // ERRO SINTÁTICO 6: Falta de ']' no acesso a array
        // inteiro elem = arr[0;

        // ERRO SINTÁTICO 7: Token inesperado
        // funcao inteiro minhaFuncao( ) {
        //     retorne
        // }

        // ERRO SEMÂNTICO 1: Variável não declarada
        // imprima(variavel_inexistente);

        // ERRO SEMÂNTICO 2: Função não declarada
        // inteiro res = funcao_desconhecida(10);

        // ERRO SEMÂNTICO 3: Redeclaração de variável
        // inteiro a = 100;

        // ERRO SEMÂNTICO 4: Redeclaração de função
        // funcao inteiro fatorial(inteiro n) {
        //     retorne n * 2;
        // }

        // ERRO SEMÂNTICO 5: Uso de variável em escopo inválido (após bloco se)
        // se (verdadeiro) {
        //     inteiro local = 50;
        // }
        // imprima(local);
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
