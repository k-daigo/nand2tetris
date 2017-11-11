package nabd2tetris;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Const {
	public static final String C_ARITHMETIC = "arithmetic";
	public static final String C_PUSH = "push";
	public static final String C_POP = "pop";
	public static final String C_LABEL = "label";
	public static final String C_GOTO = "goto";
	public static final String C_IF = "if";
	public static final String C_FUNCTION = "function";
	public static final String C_RETURN = "return";
	public static final String C_CALL = "call";

	public static final String JACK_ANA_TOKEN_TYPE_KEYWORD = "KEYWORD";
	public static final String JACK_ANA_TOKEN_TYPE_SYMBOL = "SYMBOL";
	public static final String JACK_ANA_TOKEN_TYPE_IDENTOFIER = "IDENTOFIER";
	public static final String JACK_ANA_TOKEN_TYPE_INT_CONST = "INT_CONST";
	public static final String JACK_ANA_TOKEN_TYPE_STRING_CONST = "STRING_CONST";

	public static final String ANA_COMMENT_LINE = "//";
	public static final String ANA_COMMENT_BLOCK = "/*";
	public static final String ANA_STR_LITERAL_PERIOD = "\"";

	public static enum TOKEN_TYPE {
		KEYWORD("keyword"),
		SYMBOL("symbol"),
		IDENTIFIER("identifier"),
		STRING_CONST("stringConstant"),
		INTEGER_CONST("integerConstant"),
		;

	    private final String text;

	    private TOKEN_TYPE(final String text) {
	        this.text = text;
	    }

	    public String getString() {
	    	 return this.text;
	    }
	}

	public static enum KEYWORD_TYPE {
		CLASS("class"),
		CONSTRUCTOR("constructor"),
		FUNCTION("function"),
		METHOD("method"),
		INT("int"),
		BOOLRAN("boolean"),
		CHAR("char"),
		VOID("void"),
		VAR("var"),
		STATIC("static"),
		FIELD("field"),
		LET("let"),
		DO("do"),
		IF("if"),
		ELSE("else"),
		WHILE("while"),
		RETURN("return"),
		TRUE("true"),
		FALSE("false"),
		NULL("null"),
		THIS("this"),
		;

	    private final String text;

	    private KEYWORD_TYPE(final String text) {
	        this.text = text;
	    }

	    public String getString() {
	    	 return this.text;
	    }

	}

	public static final Map<String, KEYWORD_TYPE> tokenSetKeyword = new HashMap<>();
	static {
		tokenSetKeyword.put("class", KEYWORD_TYPE.CLASS);
		tokenSetKeyword.put("method", KEYWORD_TYPE.METHOD);
		tokenSetKeyword.put("function", KEYWORD_TYPE.FUNCTION);
		tokenSetKeyword.put("constructor", KEYWORD_TYPE.CONSTRUCTOR);
		tokenSetKeyword.put("int", KEYWORD_TYPE.INT);
		tokenSetKeyword.put("boolean", KEYWORD_TYPE.BOOLRAN);
		tokenSetKeyword.put("char", KEYWORD_TYPE.CHAR);
		tokenSetKeyword.put("void", KEYWORD_TYPE.VOID);
		tokenSetKeyword.put("static", KEYWORD_TYPE.STATIC);
		tokenSetKeyword.put("var", KEYWORD_TYPE.VAR);
		tokenSetKeyword.put("field", KEYWORD_TYPE.FIELD);
		tokenSetKeyword.put("let", KEYWORD_TYPE.LET);
		tokenSetKeyword.put("do", KEYWORD_TYPE.DO);
		tokenSetKeyword.put("if", KEYWORD_TYPE.IF);
		tokenSetKeyword.put("else", KEYWORD_TYPE.ELSE);
		tokenSetKeyword.put("while", KEYWORD_TYPE.WHILE);
		tokenSetKeyword.put("return", KEYWORD_TYPE.RETURN);
		tokenSetKeyword.put("true", KEYWORD_TYPE.TRUE);
		tokenSetKeyword.put("false", KEYWORD_TYPE.FALSE);
		tokenSetKeyword.put("null", KEYWORD_TYPE.NULL);
		tokenSetKeyword.put("this", KEYWORD_TYPE.THIS);
	}

	public static final Map<String, String> tokenSymbolMap = new HashMap<>();
	static {
		tokenSymbolMap.put("{", "{");
		tokenSymbolMap.put("}", "}");
		tokenSymbolMap.put("(", "(");
		tokenSymbolMap.put(")", ")");
		tokenSymbolMap.put("[", "[");
		tokenSymbolMap.put("]", "]");
		tokenSymbolMap.put(".", ".");
		tokenSymbolMap.put(",", ",");
		tokenSymbolMap.put(";", ";");
		tokenSymbolMap.put("+", "+");
		tokenSymbolMap.put("-", "-");
		tokenSymbolMap.put("=", "=");
		tokenSymbolMap.put("*", "*");
		tokenSymbolMap.put("/", "/");
		tokenSymbolMap.put("&", "&amp;");
		tokenSymbolMap.put("|", "|");
		tokenSymbolMap.put("<", "&lt;");
		tokenSymbolMap.put(">", "&gt;");
		tokenSymbolMap.put("~", "~");
	}

	public static final Set<String> tokenNumSet = new HashSet<>();
	static {
		tokenNumSet.add("0");
		tokenNumSet.add("1");
		tokenNumSet.add("2");
		tokenNumSet.add("3");
		tokenNumSet.add("4");
		tokenNumSet.add("5");
		tokenNumSet.add("6");
		tokenNumSet.add("7");
		tokenNumSet.add("8");
		tokenNumSet.add("9");
		tokenNumSet.add(".");
	}


	public static final Set<String> tokenSepalateSet = new HashSet<>();
	static {
		tokenSepalateSet.add("\"");
		tokenSepalateSet.add(" ");
		tokenSepalateSet.add(";");
		tokenSepalateSet.add("(");
		tokenSepalateSet.add(")");
		tokenSepalateSet.add(".");
		tokenSepalateSet.add(",");
		tokenSepalateSet.add("[");
		tokenSepalateSet.add("]");
		tokenSepalateSet.add("{");
		tokenSepalateSet.add("}");
		tokenSepalateSet.add("-");
		tokenSepalateSet.add("~");
	}

	public static final Set<String> tokenOpSet = new HashSet<>();
	static {
		tokenOpSet.add("+");
		tokenOpSet.add("-");
		tokenOpSet.add("*");
		tokenOpSet.add("/");
		tokenOpSet.add("&");
		tokenOpSet.add("|");
		tokenOpSet.add("<");
		tokenOpSet.add(">");
		tokenOpSet.add("=");
		tokenOpSet.add("&lt;");
		tokenOpSet.add("&gt;");
		tokenOpSet.add("&amp;");
	}

	public static final Set<String> tokenUnaryOpSet = new HashSet<>();
	static {
		tokenUnaryOpSet.add("-");
		tokenUnaryOpSet.add("~");
	}
}
