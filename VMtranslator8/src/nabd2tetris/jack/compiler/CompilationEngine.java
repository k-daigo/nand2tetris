package nabd2tetris.jack.compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import nabd2tetris.Const;
import nabd2tetris.jack.Token;

public class CompilationEngine {

	private List<Token> tokenList;
	private String outFilePath;
	private int tokenPos;

	public CompilationEngine(List<Token> tokenList, String outFilePath) throws IOException {
		this.tokenList = tokenList;
		this.outFilePath = outFilePath;
	}

	/**
	 * コンパイルのメイン<br>
	 * "class" className "{" calssVarDec* subroutineDec* "}"
	 *
	 * @throws Exception
	 */
	public void compileClass() throws Exception {
		tokenPos = 0;

		try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(outFilePath), StandardCharsets.UTF_8)) {
			outputTag(bw, "class");

			// keyword class
			Token token = getNextToken();
			if (Const.TOKEN_TYPE.KEYWORD != token.getTokenType()) {
				throw new Exception("syntax error");
			}
			if (Const.KEYWORD_TYPE.CLASS != token.getKeyword()) {
				throw new Exception("syntax error");
			}

			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

			// idenfifier className
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

			// symbol {
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// calssVarDec*
			while (true) {
				if (!isNextKeywordClassVarDec()) {
					break;
				}

				compileClassVarDec(bw);
			}

			// subroutineDec*
			while (true) {
				if (!isNextKeywordSubroutineDec()) {
					break;
				}

				compileSubroutineDec(bw);
			}

			// symbol }
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			outputTag(bw, "/class");
		}
	}

	/**
	 * classVarDec.<br>
	 * ("static" | "field") type varName ("," varName)* ";"
	 *
	 * @throws IOException
	 */
	private void compileClassVarDec(BufferedWriter bw) throws IOException {
		outputTag(bw, "classVarDec");

		// keyword "static" | "field"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// keyword type
		token = getNextToken();
		if (token.getTokenType() == Const.TOKEN_TYPE.KEYWORD) {
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		} else {
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());
		}

		// identifier varName
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

		while (true) {
			if (!",".equals(peekNextToken().getSymbol())) {
				break;
			}

			// symbol ,
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// identifier varName
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());
		}

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/classVarDec");
	}

	/**
	 * subroutineDec<br>
	 * ("constructor" | "function" | "method") ("void" | type)<br>
	 * subroutineName "(" parameterList ")" subroutineBody
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileSubroutineDec(BufferedWriter bw) throws IOException {
		outputTag(bw, "subroutineDec");

		// keyword ("constructor" | "function" | "method")
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// keyword ("void" | type)
		if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.KEYWORD) {
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		}

		// identifier subroutineName
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

		// identifier
		if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());
		}

		// symbol (
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// parameterList
		compileParameterList(bw);

		// symbol )
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// subroutineBody
		compileSubroutineBody(bw);

		outputTag(bw, "/subroutineDec");
	}

	/**
	 * parameterList<br>
	 * ((type varName) ("," type varName)*)?
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileParameterList(BufferedWriter bw) throws IOException {
		outputTag(bw, "parameterList");

		while (true) {
			if (isEndSymbol(")")) {
				break;
			}

			// keyword type
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

			// identifier varName
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

			if (isEndSymbol(")")) {
				break;
			}

			// symbol ,
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		outputTag(bw, "/parameterList");
	}

	/**
	 * SubroutineBody<br>
	 * "{" varDec* statements "}"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileSubroutineBody(BufferedWriter bw) throws IOException {
		outputTag(bw, "subroutineBody");

		// symbol {
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// varDec*
		while (true) {
			if (!isNextKeyword("var")) {
				break;
			}

			compileVarDec(bw);
		}

		// statements
		compileStatements(bw);

		// symbol }
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/subroutineBody");
	}

	/**
	 * compileVarDec.<br>
	 * "var" type varName ("," varName)* ";"
	 *
	 * @throws IOException
	 */
	private void compileVarDec(BufferedWriter bw) throws IOException {
		outputTag(bw, "varDec");

		// keyword "var"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// identifier type
		token = getNextToken();
		if (token.getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());
		} else {
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		}

		// identifier varName
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

		// ("," varName)*
		while (true) {
			if (isEndSymbol(";")) {
				break;
			}

			// symbol ,
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// identifier varName
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());
		}

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/varDec");
	}

	/**
	 * statement*
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileStatements(BufferedWriter bw) throws IOException {
		outputTag(bw, "statements");

		// compileStatement
		while (true) {
			if (!isNextTokenStatement()) {
				break;
			}

			compileStatement(bw);
		}

		outputTag(bw, "/statements");
	}

	/**
	 * compileStatement<br>
	 * letStatement | ifStatement | whileStatement | doStatement |
	 * returnStatement
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileStatement(BufferedWriter bw) throws IOException {
		// letStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.LET) {
			compileLet(bw);
		}

		// ifStatement
		else if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.IF) {
			compileIf(bw);
		}

		// whileStatement
		else if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.WHILE) {
			compileWhile(bw);
		}

		// doStatement
		else if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.DO) {
			compileDo(bw);
		}

		// returnStatement
		else if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.RETURN) {
			compileReturn(bw);
		}
	}

	/**
	 * compileLet<br>
	 * "let" varName ("[" expression "]" )? "=" expression ";"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileLet(BufferedWriter bw) throws IOException {
		outputTag(bw, "letStatement");

		// keyword "let"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// identifier varName
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

		// symbol [
		if ("[".equals(peekNextToken().getSymbol())) {
			// symbol [
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			compileExpression(bw);

			// symbol ]
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		// symbol =
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expression
		compileExpression(bw);

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/letStatement");
	}

	/**
	 * do<br>
	 * "do" subroutineCall ";"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileDo(BufferedWriter bw) throws IOException {
		outputTag(bw, "doStatement");

		// keyword "do"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// subroutineCall
		compileSubroutineCall(bw);

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/doStatement");
	}

	/**
	 * if<br>
	 * "if" "(" expression ")" "{" statements "}"<br>
	 * ("else" "{" statements "}")?
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileIf(BufferedWriter bw) throws IOException {
		outputTag(bw, "ifStatement");

		// keyword if
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// symbol (
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expression
		compileExpression(bw);

		// symbol )
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// symbol {
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expressions
		compileStatements(bw);

		// symbol }
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		while (true) {
			if (!(peekNextToken().getKeyword() == Const.KEYWORD_TYPE.ELSE)) {
				break;
			}

			// keyword else
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

			// symbol {
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// expressions
			compileStatements(bw);

			// symbol }
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		outputTag(bw, "/ifStatement");
	}

	/**
	 * while<br>
	 * "while" "(" expression ")" "{" statements "}"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileWhile(BufferedWriter bw) throws IOException {
		outputTag(bw, "whileStatement");

		// keyword while
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// smbol (
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expression
		compileExpression(bw);

		// smbol )
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// smbol {
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// statements
		compileStatements(bw);

		// smbol }
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/whileStatement");
	}

	/**
	 * return<br>
	 * "return" expression? ";"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileReturn(BufferedWriter bw) throws IOException {
		outputTag(bw, "returnStatement");

		// keyword "return"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// expression?
		if (!";".equals(peekNextToken().getSymbol())) {
			compileExpression(bw);
		}

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/returnStatement");
	}

	/**
	 * compileExpression<br>
	 * term (op term)*
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileExpression(BufferedWriter bw) throws IOException {
		outputTag(bw, "expression");

		// term
		compileTerm(bw);

		// (op term)*
		while (true) {
			if (!Const.tokenOpSet.contains(peekNextToken().getSymbol())) {
				break;
			}

			// op
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// term
			compileTerm(bw);
		}

		outputTag(bw, "/expression");
	}

	/**
	 * expression list<br>
	 * (expression ("," expression)* )?
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileExpressionList(BufferedWriter bw) throws IOException {
		outputTag(bw, "expressionList");

		while (true) {
			if (")".equals(peekNextToken().getSymbol())) {
				break;
			}

			compileExpression(bw);

			if (!",".equals(peekNextToken().getSymbol())) {
				break;
			}

			// ,
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		outputTag(bw, "/expressionList");
	}

	/**
	 * subroutineCall<br>
	 * subroutineName "(" expressionList ")" |<br>
	 * (className | varName ) "." subroutineName "(" expressionList ")"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileSubroutineCall(BufferedWriter bw) throws IOException {

		// identifier subroutineName | className | varName
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

		// subroutine call
		if ("(".equals(peekNextToken().getSymbol())) {

			// symbol (
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// expressionList
			compileExpressionList(bw);

			// symbol )
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		// className | varName call
		else {

			// symbol .
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// identifier subroutineName
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

			// symbol (
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// expressionList
			compileExpressionList(bw);

			// symbol )
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

	}

	/**
	 * term<br>
	 * integerConstant | stringConstant | keywordConstant<br>
	 * | varName | varName "[" exprettion "]" | subroutineCall<br>
	 * | "(" exprettion ")" | unaryOp term
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileTerm(BufferedWriter bw) throws IOException {
		outputTag(bw, "term");

		// integerConstant
		if (Const.TOKEN_TYPE.INTEGER_CONST == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), String.valueOf(token.getIntVal()));
		}

		// stringConstant
		else if (Const.TOKEN_TYPE.STRING_CONST == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getStringVal());
		}

		// keywordConstant
		else if (Const.TOKEN_TYPE.KEYWORD == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		}

		// varName | varName "[" exprettion "]" | subroutineCall
		else if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

			// 変数 varName
			if (".".equals(peekNextToken().getSymbol())) {
				// .
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());

				compileSubroutineCall(bw);

			}

			// 配列 "[" expression "]"
			else if ("[".equals(peekNextToken().getSymbol())) {
				// symbol [
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());

				compileExpression(bw);

				// symbol ]
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());
			}

			// subroutineCall
			else if ("(".equals(peekNextToken().getSymbol())) {
				// symbol (
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());

				compileExpression(bw);

				// symbol )
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());
			}
		}

		// "(" exprettion ")" | unaryOp term
		else if (Const.TOKEN_TYPE.SYMBOL == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// unaryOp
			if (Const.tokenUnaryOpSet.contains(token.getSymbol())) {
				compileTerm(bw);
			}

			// "(" expression ")"
			else if ("(".equals(token.getSymbol())) {
				compileExpression(bw);

				// ")"
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());
			}
		}

		outputTag(bw, "/term");
	}

	private Token getNextToken() {
		Token token = tokenList.get(tokenPos);
		tokenPos++;
		return token;
	}

	private Token peekNextToken() {
		return tokenList.get(tokenPos);
	}

	private boolean isNextKeywordSubroutineDec() {
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.CONSTRUCTOR) {
			return true;
		}
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.FUNCTION) {
			return true;
		}
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.METHOD) {
			return true;
		}

		return false;
	}

	private boolean isNextKeywordClassVarDec() {
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.STATIC) {
			return true;
		}
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.FIELD) {
			return true;
		}
		return false;
	}

	private boolean isNextKeyword(String nextKeyword) {
		if (nextKeyword.equals(peekNextToken().getKeyword().getString())) {
			return true;
		}
		return false;
	}

	private boolean isEndSymbol(String endSymbol) {
		if (endSymbol.equals(peekNextToken().getSymbol())) {
			return true;
		}
		return false;
	}

	private boolean isNextTokenStatement() throws IOException {
		// letStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.LET) {
			return true;
		}

		// ifStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.IF) {
			return true;
		}

		// whileStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.WHILE) {
			return true;
		}

		// doStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.DO) {
			return true;
		}

		// returnStatement
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.RETURN) {
			return true;
		}

		return false;
	}

	private void outputTag(BufferedWriter bw, String tag) throws IOException {
		bw.append("<" + tag + ">\r\n");
	}

	private void outputTag(BufferedWriter bw, String tag, String val) throws IOException {
		bw.append("<" + tag + "> " + val + " </" + tag + ">\r\n");
	}
}
