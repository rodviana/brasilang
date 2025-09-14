package br.edu.pucgoias.brasilang.translate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.pucgoias.brasilang.model.lexico.EnumTokenType;
import br.edu.pucgoias.brasilang.model.sintaxe.expression.*;
import br.edu.pucgoias.brasilang.model.sintaxe.statement.*;

public class TranslateServiceTest {

    @Test
    void testVariableDeclarationTranslation() {
        Program program = new Program(List.of(
                new VariableDeclaration("x", EnumTokenType.INT, new Literal(10))));
        String code = new TranslateService(program).generateCode();
        String expected = "int main() {\n" +
                "    int x = 10;\n" +
                "    return 0;\n" +
                "}\n";
        assertEquals(expected, code);
    }

    @Test
    void testFunctionTranslation() {
        FunctionDeclaration soma = new FunctionDeclaration(
                EnumTokenType.INT,
                "soma",
                List.of(new FunctionDeclaration.Parameter("a", EnumTokenType.INT),
                        new FunctionDeclaration.Parameter("b", EnumTokenType.INT)),
                List.of(new ReturnStatement(new BinaryOperation("+", new Variable("a"), new Variable("b")))));

        Program program = new Program(List.of(
                soma,
                new Print(new FunctionCall("soma",
                        List.of(new Literal(2), new Literal(3))))));

        String code = new TranslateService(program).generateCode();
        String expected = "#include <stdio.h>\n" +
                "\n" +
                "int soma(int a, int b) {\n" +
                "    return a + b;\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    printf(soma(2, 3));\n" +
                "    return 0;\n" +
                "}\n";
        assertEquals(expected, code);
    }

    @Test
    void testControlStructuresTranslation() {
        VariableDeclaration i = new VariableDeclaration("i", EnumTokenType.INT, new Literal(0));
        RepetitionStruct whileLoop = new RepetitionStruct(
                EnumTokenType.ENQUANTO,
                new BinaryOperation("<", new Variable("i"), new Literal(3)),
                List.of(new Assign("i", new BinaryOperation("+", new Variable("i"), new Literal(1)))));
        RepetitionStruct forLoop = new RepetitionStruct(
                EnumTokenType.PARA,
                new BinaryOperation("<", new Variable("i"), new Literal(5)),
                List.of(new Assign("i", new BinaryOperation("+", new Variable("i"), new Literal(1)))));
        ConditionalStruct cond = new ConditionalStruct(
                new BinaryOperation("==", new Variable("i"), new Literal(5)),
                List.of(new Print(new Literal("done"))),
                List.of(new Print(new Literal("not done"))));

        Program program = new Program(List.of(i, whileLoop, forLoop, cond));
        String code = new TranslateService(program).generateCode();
        String expected = "#include <stdio.h>\n" +
                "\n" +
                "int main() {\n" +
                "    int i = 0;\n" +
                "    while (i < 3) {\n" +
                "        i = i + 1;\n" +
                "    }\n" +
                "    for (; i < 5; ) {\n" +
                "        i = i + 1;\n" +
                "    }\n" +
                "    if (i == 5) {\n" +
                "        printf(\"done\");\n" +
                "    } else {\n" +
                "        printf(\"not done\");\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n";
        assertEquals(expected, code);
    }

    @Test
    void testHelloWorldTranslation() {
        Program program = new Program(List.of(
                new Print(new Literal("Hello, World!"))));
        String code = new TranslateService(program).generateCode();
        String expected = "#include <stdio.h>\n" +
                "\n" +
                "int main() {\n" +
                "    printf(\"Hello, World!\");\n" +
                "    return 0;\n" +
                "}\n";
        assertEquals(expected, code);
    }
}
