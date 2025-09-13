package br.edu.pucgoias.brasilang.model.lexico;

public class KeywordResolver {

        public EnumTokenType resolve(String s) {
            return switch (s) {
                case "funcao" -> EnumTokenType.FUNCAO;
	        case "retorne"-> EnumTokenType.RETORNE;

	        case "se"     -> EnumTokenType.SE;
	        case "senao"  -> EnumTokenType.SENAO;
                case "enquanto" -> EnumTokenType.ENQUANTO;
                case "para"     -> EnumTokenType.PARA;
                case "imprima"  -> EnumTokenType.IMPRIMA;

                case "inteiro"       -> EnumTokenType.INT;
	        case "flutuante"   -> EnumTokenType.FLOAT;
	        case "duplo"      -> EnumTokenType.DOUBLE;
	        case "vazio"        -> EnumTokenType.VOID;

                default -> null;
            };
        }

        @Override
        public String toString() {
            return String.format("KeywordResolver{}");
        }
}
