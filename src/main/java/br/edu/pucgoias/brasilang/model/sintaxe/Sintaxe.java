package br.edu.pucgoias.brasilang.model.sintaxe;

import java.util.List;

import br.edu.pucgoias.brasilang.model.lexico.Token;

public class Sintaxe {
	
	private List<Token> tokenList;
	private int position;
	
	
	public Sintaxe(List<Token> tokenList){
		this.tokenList = tokenList;
		this.position = 0;
	}
	
	
	public List<Token> getTokenList() {
		return tokenList;
	}
	public void setTokenList(List<Token> tokenList) {
		this.tokenList = tokenList;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
	public Token previewNextToken() {
		if(tokenList.size() >= position+1)
			return this.tokenList.get(position+1);
		else
			return null;
	}
	
        public Token advanceToNextToken() {
                if(tokenList.size() >= position+1) {
                        this.position++;
                        return this.tokenList.get(position);
                }
                else
                        return null;
        }

        @Override
        public String toString() {
                return String.format("Sintaxe{tokenList=%s, position=%d}", tokenList, position);
        }

}
