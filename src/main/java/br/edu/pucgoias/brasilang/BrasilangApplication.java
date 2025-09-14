package br.edu.pucgoias.brasilang;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
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
        List<Token> tokenList = new ArrayList<>();
        String src = """
                        inteiro g = 10;
                        flutuante f = 1.5;
                        inteiro i = 0;
                        para (i < 3) {
                          imprima(i);
                          i = i + 1;
                        }
                        enquanto (g > 0) {
                          se (g == 5) {
                            imprima("metade");
                          } senao {
                            imprima(g);
                          }
                          g = g - 1;
                        }
                        imprima(f);
                        """;
    	Lexer lexer = new Lexer(src);
    	Token currentToken;
        do {
        	currentToken = lexerService.next(lexer);
        	tokenList.add(currentToken);
        } while (currentToken.type != EnumTokenType.EOF);

        tokenList.forEach(token -> System.out.println(token.toString()));
        Sintaxe sintaxe = new Sintaxe(tokenList);
        List<AbstractStatement> statements = sintaxeService.buildProgramStatementList(sintaxe);

        statements.forEach(statement -> System.out.println(statement.toString()));

        Program program = new Program(statements);
        String cCode = translateService.generateCode(program);
        System.out.println(cCode);

    }
}
