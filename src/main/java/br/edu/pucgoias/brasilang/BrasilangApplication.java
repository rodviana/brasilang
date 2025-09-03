package br.edu.pucgoias.brasilang;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.edu.pucgoias.brasilang.model.lex.Lexer;
import br.edu.pucgoias.brasilang.model.lex.Token;
import br.edu.pucgoias.brasilang.model.lex.EnumTokenType;
import br.edu.pucgoias.brasilang.service.CEmitterService;
import br.edu.pucgoias.brasilang.service.LexerService;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class BrasilangApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrasilangApplication.class, args);
	}

	@Autowired
	LexerService lexerService;
	@Autowired
	CEmitterService cEmitterService;
    @PostConstruct
    void executar() {
    	List<Token> tokenList = new ArrayList();
    	String src = """
    			inteiro g = 10; // demo
    			para (inteiro i = 0; i < 10; i = i + 1) {
    			  se (i == 5) {
    			    imprima(55);
    			  }
    			  imprima(i);
    			}
    			""";
    	Lexer lexer = new Lexer(src);
    	Token currentToken;
        do {
        	currentToken = lexerService.next(lexer);
        	tokenList.add(currentToken);
        } while (currentToken.type != EnumTokenType.EOF);

        tokenList.forEach(token -> System.out.println(token.toString()));
        
        System.out.println(cEmitterService.tokensParaC(tokenList));

    }
}
