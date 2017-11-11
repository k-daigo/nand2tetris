package nabd2tetris.jack.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nabd2tetris.Const;
import nabd2tetris.jack.tokenizer.JackTokenizer;
import nabd2tetris.jack.tokenizer.Token;

public class JackCompiler {
	public static void main(String[] args) throws IOException {
		String filePath = args[0];

		JackCompiler jackAnalyzer = new JackCompiler();
		jackAnalyzer.exec(filePath);
	}

	private void exec(String targetFilePath) throws IOException {
		File inFile = new File(targetFilePath);

		List<String> jackFilePathList = new ArrayList<>();

		if (inFile.isDirectory()) {
			File[] fileFist = inFile.listFiles();
			for (File file : fileFist) {
				jackFilePathList.add(file.getCanonicalPath());
			}
		} else {
			jackFilePathList.add(targetFilePath);
		}

		for (String jackPath : jackFilePathList) {
			int point = jackPath.lastIndexOf(".");
			String extName = jackPath.substring(point, jackPath.length());
			if (!".jack".equals(extName)) {
				continue;
			}

			StringBuffer tokenSb = new StringBuffer();

			// トークナイズ
			JackTokenizer tokenizer = new JackTokenizer(jackPath);
			List<Token> tokeList = new ArrayList<>();
			while (true) {
				Token token = tokenizer.advance();
				if (token == null) {
					break;
				}
				tokeList.add(token);

				// xxxT.xmlの出力用
				if (Const.TOKEN_TYPE.KEYWORD == token.getTokenType()) {
					addTokenTag(tokenSb, token.getTokenType().getString(), token.getKeyword().getString());

				} else if (Const.TOKEN_TYPE.IDENTIFIER == token.getTokenType()) {
					addTokenTag(tokenSb, token.getTokenType().getString(), token.getIdentifire());

				} else if (Const.TOKEN_TYPE.STRING_CONST == token.getTokenType()) {
					addTokenTag(tokenSb, token.getTokenType().getString(), token.getStringVal());

				} else if (Const.TOKEN_TYPE.INTEGER_CONST == token.getTokenType()) {
					addTokenTag(tokenSb, token.getTokenType().getString(), String.valueOf(token.getIntVal()));

				} else if (Const.TOKEN_TYPE.SYMBOL == token.getTokenType()) {
					String encSymbol = Const.tokenSymbolMap.get(token.getSymbol());
					addTokenTag(tokenSb, token.getTokenType().getString(), encSymbol);
				}
			}

			// トークナイズ後ファイルに出力
			String tokenizedFilePath = jackPath.substring(0, point) + "T.xml";
			try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(tokenizedFilePath, false)))) {
				pw.print("<tokens>\r\n");
				pw.print(tokenSb.toString());
				pw.print("</tokens>\r\n");
			}

			// コンパイル
			String baseFilePath = jackPath.substring(0, point);
			CompilationEngine compilationEngine = new CompilationEngine(tokeList, baseFilePath);
			try {
				compilationEngine.compileClass();
			} catch (Exception e) {
				System.out.println("syntax error.");
				e.printStackTrace();
				return;
			}
		}

	}

	private void addTokenTag(StringBuffer tokenSb, String type, String val) {
		tokenSb.append("<" + type + "> " + val + " </" + type + ">\r\n");
	}
}
