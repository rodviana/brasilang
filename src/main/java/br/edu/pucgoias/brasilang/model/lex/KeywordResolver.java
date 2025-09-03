package br.edu.pucgoias.brasilang.model.lex;

public class KeywordResolver {
	
	public EnumTokenType resolve(String s) {
	    return switch (s) {
	        case "var"    -> EnumTokenType.VAR;
	        case "funcao" -> EnumTokenType.FUNCAO;
	        case "retorne"-> EnumTokenType.RETORNE;

	        // PT-BR
	        case "se"     -> EnumTokenType.SE;
	        case "senao"  -> EnumTokenType.SENAO;
	        case "enquanto" -> EnumTokenType.ENQUANTO;
	        case "para"     -> EnumTokenType.PARA;

	        // EN aliases (to match your snippet)
	        case "if"     -> EnumTokenType.SE;
	        case "else"   -> EnumTokenType.SENAO;
	        case "while"  -> EnumTokenType.ENQUANTO;
	        case "for"    -> EnumTokenType.PARA;

	        // types
	        case "int", "inteiro"       -> EnumTokenType.INT;
	        case "float", "flutuante"   -> EnumTokenType.FLOAT;
	        case "double", "duplo"      -> EnumTokenType.DOUBLE;
	        case "void", "vazio"        -> EnumTokenType.VOID;

	        default -> null;
	    };
	}
}
