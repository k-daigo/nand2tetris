package nabd2tetris.jack.compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nabd2tetris.Const;
import nabd2tetris.Const.KEYWORD_TYPE;
import nabd2tetris.jack.compiler.SymbolTable.Symbol;
import nabd2tetris.jack.compiler.VMWriter.Segment;
import nabd2tetris.jack.tokenizer.Token;

public class CompilationEngine {

	private List<Token> tokenList;
	private String xmlOutFilePath;
	private String vmOutFilePath;
	private int tokenPos;

	private Map<String, Integer> labelIndexMap = new HashMap<String, Integer>();

	private SymbolTable symbolTableClass;
	private SymbolTable symbolTableSub;

	private VMWriter vmWriter;

	public CompilationEngine(List<Token> tokenList, String outFilePath) throws IOException {
		this.tokenList = tokenList;
		this.vmOutFilePath = outFilePath + ".vm";
		this.xmlOutFilePath = outFilePath + ".xml";
		vmWriter = new VMWriter(vmOutFilePath);
	}

	/**
	 * コンパイルのメイン<br>
	 * "class" className "{" calssVarDec* subroutineDec* "}"
	 *
	 * @throws Exception
	 */
	public void compileClass() throws Exception {
		tokenPos = 0;
		symbolTableClass = new SymbolTable();

		try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(xmlOutFilePath), StandardCharsets.UTF_8)) {
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
			String className = token.getIdentifire();
			outputTag(bw, token.getTokenType().getString(), className);

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

				compileSubroutineDec(bw, className);
			}

			// symbol }
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			outputTag(bw, "/class");
		}

		vmWriter.close();
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
		String kind = token.getKeyword().getString();

		// keyword type
		token = getNextToken();
		String type = "";
		if (token.getTokenType() == Const.TOKEN_TYPE.KEYWORD) {
			type = token.getKeyword().getString();
		} else {
			type = token.getIdentifire();
		}
		outputTag(bw, token.getTokenType().getString(), type);

		// identifier varName
		token = getNextToken();
		symbolTableClass.define(token.getIdentifire(), type, kind);
		outputComment(bw, symbolTableClass.getSymbol(token.getIdentifire()).toString());
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
			symbolTableClass.define(token.getIdentifire(), type, kind);
			outputComment(bw, symbolTableClass.getSymbol(token.getIdentifire()).toString());
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
	private void compileSubroutineDec(BufferedWriter bw, String className) throws IOException {
		symbolTableSub = new SymbolTable();

		symbolTableSub.startSubroutine();
		labelIndexMap = new HashMap<>();

		outputTag(bw, "subroutineDec");

		// keyword ("constructor" | "function" | "method")
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		Const.KEYWORD_TYPE subroutineType = token.getKeyword();
		if (KEYWORD_TYPE.METHOD == subroutineType) {
			symbolTableSub.define("this", className, "argument");
		}

		// keyword ("void" | type)
		if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.KEYWORD) {
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());
		}

		// identifier subroutineName
		token = getNextToken();
		String subroutineName = token.getIdentifire();
		outputTag(bw, token.getTokenType().getString(), subroutineName);

		// identifier
		if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			token = getNextToken();
			subroutineName = token.getIdentifire();
			outputTag(bw, token.getTokenType().getString(), subroutineName);
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
		compileSubroutineBody(bw, className, subroutineName, subroutineType);

		outputTag(bw, "/subroutineDec");
	}

	/**
	 * parameterList<br>
	 * ((type varName) ("," type varName)*)?
	 *
	 * @param bw
	 * @throws IOException
	 */
	private int compileParameterList(BufferedWriter bw) throws IOException {
		int paramCount = 0;
		outputTag(bw, "parameterList");

		while (true) {
			if (isEndSymbol(")")) {
				break;
			}

			// keyword type
			Token token = getNextToken();
			KEYWORD_TYPE keyword = token.getKeyword();
			String type;
			if (keyword == null) {
				type = token.getIdentifire();
			} else {
				type = keyword.toString();
			}
			outputTag(bw, token.getTokenType().getString(), type);

			// identifier varName
			token = getNextToken();
			symbolTableSub.define(token.getIdentifire(), type, "argument");
			outputComment(bw, symbolTableSub.getSymbol(token.getIdentifire()).toString());
			outputTag(bw, token.getTokenType().getString(), token.getIdentifire());

			paramCount++;

			if (isEndSymbol(")")) {
				break;
			}

			// symbol ,
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		outputTag(bw, "/parameterList");

		return paramCount;
	}

	/**
	 * SubroutineBody<br>
	 * "{" varDec* statements "}"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileSubroutineBody(BufferedWriter bw, String className,
			String subroutineName, Const.KEYWORD_TYPE subroutineType) throws IOException {
		outputTag(bw, "subroutineBody");

		// symbol {
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// varDec*
		List<String> varList = new ArrayList<>();
		while (true) {
			if (!isNextKeyword("var")) {
				break;
			}

			varList = compileVarDec(bw, varList);
		}

		// vmcode
		// function className.subroutineName nLocals
		vmWriter.writeFunction(className + "." + subroutineName, varList.size());

		// コンストラクタの場合は、メモリ割り当てを行う
		if (Const.KEYWORD_TYPE.CONSTRUCTOR == subroutineType) {
			// vmcode
			vmWriter.writePush(Segment.CONST, symbolTableClass.getSizeWithoutStatic());
			vmWriter.writeCall("Memory.alloc", 1);
			vmWriter.writePop(Segment.POINTER, 0);
		}

		// constractとfunction以外の場合はthisの設定
		//		if (Const.KEYWORD_TYPE.CONSTRUCTOR != subroutineType
		//				&& !"Main".equals(className) && !"main".equals(subroutineName)) {
		if (Const.KEYWORD_TYPE.CONSTRUCTOR != subroutineType
				&& Const.KEYWORD_TYPE.FUNCTION != subroutineType) {
			vmWriter.writePush(Segment.ARG, 0);
			vmWriter.writePop(Segment.POINTER, 0);
		}

		// thisをシンボルテーブルに追加
		symbolTableSub.define("this", className, "argument");

		// debug
		vmWriter.writeComment(symbolTableSub.toString());

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
	private List<String> compileVarDec(BufferedWriter bw, List<String> varList)
			throws IOException {

		outputTag(bw, "varDec");

		// keyword "var"
		Token token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getKeyword().getString());

		// identifier type
		token = getNextToken();
		String type = "";
		if (token.getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			type = token.getIdentifire();
		} else {
			type = token.getKeyword().getString();
		}
		outputTag(bw, token.getTokenType().getString(), type);

		// identifier varName
		token = getNextToken();
		String varName = token.getIdentifire();
		symbolTableSub.define(varName, type, "var");
		outputComment(bw, symbolTableSub.getSymbol(varName).toString());
		outputTag(bw, token.getTokenType().getString(), varName);

		//		nLocals++;
		//
		//		// vmcode
		//		vmWriter.writeComment("var " + varName);
		//		vmWriter.writePush(Segment.LOCAL, symbolTableSub.indexOf(varName));
		varList.add(varName);

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
			varName = token.getIdentifire();
			symbolTableSub.define(varName, type, "var");
			outputComment(bw, symbolTableSub.getSymbol(varName).toString());
			outputTag(bw, token.getTokenType().getString(), varName);

			//			// vmcode
			//			vmWriter.writeComment("var " + varName);
			//			vmWriter.writePush(Segment.LOCAL, symbolTableSub.indexOf(varName));
			//
			//			nLocals++;
			varList.add(varName);
		}

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		outputTag(bw, "/varDec");

		return varList;

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
		String varName = token.getIdentifire();
		outputTag(bw, token.getTokenType().getString(), varName);

		// 配列
		boolean isArray = false;
		if ("[".equals(peekNextToken().getSymbol())) {
			isArray = true;

			// symbol [
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// vmcode
			vmWriter.writeComment("let " + varName + "[x]" + " compileExpression");

			compileExpression(bw);

			// vmcode
			// 配列アクセス
			Symbol symbol = getSymbol(varName);
			vmWriter.writePush(kind2segment(symbol.kind), symbol.index);
			vmWriter.writeArithmetic("add");

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

		// vmcode
		vmWriter.writeComment("let " + varName);

		// 配列への代入の場合は、POINTER、THAT経由で指定配列に格納
		if (isArray) {
			vmWriter.writePop(Segment.TEMP, 0);
			vmWriter.writePop(Segment.POINTER, 1);
			vmWriter.writePush(Segment.TEMP, 0);
			vmWriter.writePop(Segment.THAT, 0);
		}

		// 配列以外は当該変数に直接代入
		else {
			boolean isSubroutineSymbol = isSubroutineSymbol(varName);
			SymbolTable symbolTable = symbolTableClass;
			if (isSubroutineSymbol) {
				symbolTable = symbolTableSub;
			}
			int index = symbolTable.indexOf(varName);
			Segment segment = kind2segment(symbolTable.kindOf(varName));
			vmWriter.writePop(segment, index);
		}

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
		compileSubroutineCall(bw, null);

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// 戻り値の空読み
		vmWriter.writeComment("get return val");
		vmWriter.writePop(Segment.TEMP, 0);

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

		// vmcode
		// if判定
		vmWriter.writeComment("if condition judge");
		String ifTrueLabel = "IF_TRUE" + getNextLabelIndex("IF_TRUE");
		String ifFalseLabel = "IF_FALSE" + getNextLabelIndex("IF_FALSE");
		String ifEndLabel = "IF_END" + getNextLabelIndex("IF_END");
		vmWriter.writeIf(ifTrueLabel);

		// falseはジャンプ
		vmWriter.writeGoto(ifFalseLabel);

		// ここからtrue
		vmWriter.writeLabel(ifTrueLabel);

		// symbol {
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expressions
		compileStatements(bw);

		// symbol }
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// vmcode
		// else or if終了開始

		// elseが存在する場合はIF_ENDラベルを生成して、else部分をジャンプ可能とする
		boolean existsElse = false;
		if (peekNextToken().getKeyword() == Const.KEYWORD_TYPE.ELSE) {
			existsElse = true;
			vmWriter.writeGoto(ifEndLabel);
		}

		vmWriter.writeLabel(ifFalseLabel);

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

			// statements
			compileStatements(bw);

			// symbol }
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		// vmcode
		// if終了
		vmWriter.writeComment("end if");
		if (existsElse) {
			vmWriter.writeLabel(ifEndLabel);
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

		// vmcode
		String whileStartLabel = "WHILE_EXP" + getNextLabelIndex("WHILE_EXP");
		vmWriter.writeComment("while start");
		vmWriter.writeLabel(whileStartLabel);

		// smbol (
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// expression
		compileExpression(bw);

		// vmcode
		// while完了条件はtrueで-1となるので、次のgoto-ifでtrue→ループ終了とならないように結果を反転
		vmWriter.writeArithmetic("not");
		// while完了判定
		vmWriter.writeComment("while condition judge");
		String whileEndLabel = "WHILE_END" + getNextLabelIndex("WHILE_END");
		vmWriter.writeIf(whileEndLabel);

		// smbol )
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// smbol {
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// statements
		compileStatements(bw);

		// vmcode
		vmWriter.writeGoto(whileStartLabel);

		// smbol }
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// vmcode
		vmWriter.writeComment("while end");
		vmWriter.writeLabel(whileEndLabel);

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

		// 戻り値なしの場合は0を戻す
		else {
			// vmcode
			vmWriter.writeComment("return void");
			vmWriter.writePush(Segment.CONST, 0);
		}

		// symbol ;
		token = getNextToken();
		outputTag(bw, token.getTokenType().getString(), token.getSymbol());

		// vmcode
		vmWriter.writeReturn();

		outputTag(bw, "/returnStatement");
	}

	/**
	 * compileExpression<br>
	 * term (op term)*
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileExpression(BufferedWriter bw)
			throws IOException {
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
			String op = token.getSymbol();
			outputTag(bw, token.getTokenType().getString(), op);

			// term
			compileTerm(bw);

			// vmcode
			// term → term → op の順でpushを出力
			vmWriter.writeArithmetic(op);
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
	private int compileExpressionList(BufferedWriter bw) throws IOException {
		outputTag(bw, "expressionList");

		int expCount = 0;
		while (true) {
			if (")".equals(peekNextToken().getSymbol())) {
				break;
			}

			compileExpression(bw);
			expCount++;

			if (!",".equals(peekNextToken().getSymbol())) {
				break;
			}

			// ,
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());
		}

		outputTag(bw, "/expressionList");

		return expCount;
	}

	/**
	 * subroutineCall<br>
	 * subroutineName "(" expressionList ")" |<br>
	 * (className | varName ) "." subroutineName "(" expressionList ")"
	 *
	 * @param bw
	 * @throws IOException
	 */
	private void compileSubroutineCall(BufferedWriter bw, String className)
			throws IOException {

		// identifier subroutineName | className | varName
		Token callTergetToken = getNextToken();
		String callTargetName = callTergetToken.getIdentifire();
		outputTag(bw, callTergetToken.getTokenType().getString(), callTergetToken.getIdentifire());

		// subroutine call
		if ("(".equals(peekNextToken().getSymbol())) {
			String methodName = callTargetName;

			// vmcode
			// コンストラクラ以外はthisも渡す
			// 認識済みでないシンボルはstaticコールと見なす
			Symbol symbol = getSymbol(className);
			boolean isExitsThis = isExitsThis();
			boolean isInstanceMethodCall = false;

			int thisArg = 0;

			// xxx.xxx()
			if (symbol != null && isExitsThis) {
				vmWriter.writeComment("push instance");
				//				vmWriter.writePush(Segment.POINTER, 0);
				vmWriter.writePush(kind2segment(symbol.kind), symbol.index);
				thisArg = 1;
				isInstanceMethodCall = true;
			}

			// xxx()
			else if (className == null && isExitsThis) {
				vmWriter.writeComment("push instance");
				vmWriter.writePush(Segment.POINTER, 0);
				thisArg = 1;
				isInstanceMethodCall = true;
			}

			// symbol (
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// expressionList
			int nArgs = compileExpressionList(bw) + thisArg;

			// symbol )
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			String callTarget = "";
			if (symbol != null) {
				callTarget = symbol.type + "." + methodName;
			} else if (className == null) {
				String targetClassName = symbolTableSub.typeOf("this");
				callTarget = targetClassName + "." + methodName;
			} else {
				callTarget = className + "." + methodName;
			}
			vmWriter.writeCall(callTarget, nArgs);
		}

		// className | varName call
		else {
			String classOrVarName = callTargetName;

			// symbol .
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// identifier subroutineName
			token = getNextToken();
			String callSubroutineName = token.getIdentifire();
			outputTag(bw, token.getTokenType().getString(), callSubroutineName);

			// symbol (
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// vmcode
			Symbol symbol = getSymbol(classOrVarName);
			String targetClassName = symbolTableSub.typeOf(classOrVarName);
			String callTarget = "";

			// vmcode
			int thisArg = 0;
			// シンボルテーブルに存在しないクラス名はスタティック呼び出し
			if (symbol == null) {
				callTarget = classOrVarName + "." + callSubroutineName;
			}

			// 非static
			else {
				thisArg = 1;

				// LOCAL系
				if ("var".equals(symbol.kind)) {
					callTarget = targetClassName + "." + callSubroutineName;
					vmWriter.writeComment("push this");
					vmWriter.writePush(Segment.LOCAL, symbol.index);
				}

				else {
					callTarget = symbol.type + "." + callSubroutineName;
					vmWriter.writeComment("push this");
					vmWriter.writePush(Segment.THIS, symbol.index);
				}
			}

			// expressionList
			int nArgs = compileExpressionList(bw) + thisArg;

			// symbol )
			token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			vmWriter.writeCall(callTarget, nArgs);
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
			int intConst = token.getIntVal();
			outputTag(bw, token.getTokenType().getString(), String.valueOf(intConst));

			// vmcode
			vmWriter.writePush(Segment.CONST, intConst);
		}

		// stringConstant
		else if (Const.TOKEN_TYPE.STRING_CONST == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getStringVal());

			// vmcode
			writeString(token.getStringVal());
		}

		// keywordConstant
		else if (Const.TOKEN_TYPE.KEYWORD == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			String keywordConst = token.getKeyword().getString();
			outputTag(bw, token.getTokenType().getString(), keywordConst);

			// vmcode
			// TODO 「return this」のケース。あやしい
			if ("this".equals(keywordConst)) {
				vmWriter.writeComment(keywordConst);
				vmWriter.writePush(Segment.POINTER, 0);
			}

			// vmcode
			writeKeywordConstant(bw, keywordConst);
		}

		// varName | varName "[" exprettion "]" | subroutineCall
		else if (peekNextToken().getTokenType() == Const.TOKEN_TYPE.IDENTIFIER) {
			Token token = getNextToken();
			String varOrSubName = token.getIdentifire();
			outputTag(bw, token.getTokenType().getString(), varOrSubName);

			// メソッド呼び出し
			if (".".equals(peekNextToken().getSymbol())) {
				// .
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());

				compileSubroutineCall(bw, varOrSubName);
			}

			// 配列 "[" expression "]"
			else if ("[".equals(peekNextToken().getSymbol())) {
				// symbol [
				token = getNextToken();
				outputTag(bw, token.getTokenType().getString(), token.getSymbol());

				compileExpression(bw);

				Symbol symbol = getSymbol(varOrSubName);
				vmWriter.writePush(kind2segment(symbol.kind), symbol.index);
				vmWriter.writeArithmetic("add");
				vmWriter.writePop(Segment.POINTER, 1);
				vmWriter.writePush(Segment.THAT, 0);

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

			// 変数
			else {
				// vmcode
				vmWriter.writeComment(varOrSubName);
				boolean isSubroutineSymbol = isSubroutineSymbol(varOrSubName);

				// サブルーチンのシンボルの場合
				if (isSubroutineSymbol) {
					int index = symbolTableSub.indexOf(varOrSubName);
					String kind = symbolTableSub.kindOf(varOrSubName);
					Segment segment = Segment.fromString(kind);
					if (segment == null) {
						vmWriter.writePush(Segment.LOCAL, index);
					} else {
						vmWriter.writePush(segment, index);
					}
				}

				// インスタンスのシンボルの場合
				else {
					int index = symbolTableClass.indexOf(varOrSubName);
					String kind = symbolTableClass.kindOf(varOrSubName);
					Segment segment = Segment.fromString(kind);
					if (segment == null) {
						vmWriter.writePush(Segment.THIS, index);
					} else {
						vmWriter.writePush(segment, index);
					}
				}
			}
		}

		// "(" exprettion ")" | unaryOp term
		else if (Const.TOKEN_TYPE.SYMBOL == peekNextToken().getTokenType()) {
			Token token = getNextToken();
			outputTag(bw, token.getTokenType().getString(), token.getSymbol());

			// unaryOp
			if (Const.tokenUnaryOpSet.containsKey(token.getSymbol())) {

				// 「-」「~」が付与されている場合はConstを先に出力した後に反転を出力するので覚えておく
				String reverseOp = Const.tokenUnaryOpSet.get(token.getSymbol());

				// Constなど出力
				compileTerm(bw);

				// 「-」「~」が付与されていた場合は反転
				// vmcode
				if (reverseOp != null) {
					vmWriter.writeArithmetic(reverseOp);
				}
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

	private void writeKeywordConstant(BufferedWriter bw, String bool) {
		if ("true".equals(bool)) {
			vmWriter.writePush(Segment.CONST, 0);
			vmWriter.writeArithmetic("not");
		} else if ("false".equals(bool)) {
			vmWriter.writePush(Segment.CONST, 0);
		} else if ("null".equals(bool)) {
			vmWriter.writePush(Segment.CONST, 0);
		}
	}

	private Token getPrevToken() {
		return tokenList.get(tokenPos - 1);
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

	private void outputComment(BufferedWriter bw, String comment) throws IOException {
		bw.append("<!-- " + comment + " -->\r\n");
	}

	private Segment kind2segment(String kind) {
		if ("var".equals(kind)) {
			return Segment.LOCAL;
		}
		if ("field".equals(kind)) {
			return Segment.THIS;
		}

		return Segment.fromString(kind);
	}

	private boolean isSubroutineSymbol(String name) {
		Symbol symbol = symbolTableSub.getSymbol(name);
		if (symbol != null) {
			return true;
		}
		return false;
	}

	private Symbol getSymbol(String name) {
		Symbol symbol = symbolTableSub.getSymbol(name);
		if (symbol != null) {
			return symbol;
		}
		return symbolTableClass.getSymbol(name);
	}

	private boolean isExitsThis() {
		Symbol symbol = symbolTableSub.getSymbol("this");
		if (symbol != null) {
			return true;
		}
		symbol = symbolTableClass.getSymbol("this");
		if (symbol != null) {
			return true;
		}

		return false;
	}

	private int getNextLabelIndex(String labelBase) {
		int index;
		if (labelIndexMap.containsKey(labelBase)) {
			index = labelIndexMap.get(labelBase);
			index++;
		} else {
			index = 0;
		}

		labelIndexMap.put(labelBase, index);
		return index;
	}

	private void writeString(String str) throws UnsupportedEncodingException {
		// vmcode
		int strLen = str.length();
		vmWriter.writePush(Segment.CONST, strLen); // 文字列長
		vmWriter.writeCall("String.new", 1); // String.new(strLen)

		// 文字列をACSIIでばらしてString.appendCharに渡す
		byte[] asciiCodes = str.getBytes("US-ASCII");
		for (int ii = 0; ii < asciiCodes.length; ii++) {
			vmWriter.writePush(Segment.CONST, asciiCodes[ii]);
			vmWriter.writeCall("String.appendChar", 2);
		}
	}
}
