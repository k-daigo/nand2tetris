package nabd2tetris.jack;

import nabd2tetris.Const;

public class Token {
	private Const.TOKEN_TYPE tokenType;
	private Const.KEYWORD_TYPE keyword;
	private String symbol;
	private String identifire;
	private int intVal;
	private String stringVal;

	public Token(Const.TOKEN_TYPE tokenType) {
		this.tokenType = tokenType;
	}

	public Const.TOKEN_TYPE getTokenType() {
		return tokenType;
	}

	public void setTokenType(Const.TOKEN_TYPE tokenType) {
		this.tokenType = tokenType;
	}

	public Const.KEYWORD_TYPE getKeyword() {
		return keyword;
	}

	public void setKeyword(Const.KEYWORD_TYPE keyword) {
		this.keyword = keyword;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getIdentifire() {
		return identifire;
	}

	public void setIdentifire(String identifire) {
		this.identifire = identifire;
	}

	public int getIntVal() {
		return intVal;
	}

	public void setIntVal(int intVal) {
		this.intVal = intVal;
	}

	public String getStringVal() {
		return stringVal;
	}

	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}
}
