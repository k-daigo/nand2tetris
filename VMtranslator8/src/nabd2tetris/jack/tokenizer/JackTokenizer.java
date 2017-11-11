package nabd2tetris.jack.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

import nabd2tetris.Const;
import nabd2tetris.jack.Token;

public class JackTokenizer {
	private static final String REG_INT = "^[0-9]+";

	private BufferedReader br;

	public JackTokenizer(String filePath) throws IOException {
		br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
	}

	public Token advance() throws IOException {
		String tokenStr = getString();

		while (true) {
			if (tokenStr == null) {
				return null;
			}

			// スペース、タブ、改行を読み飛ばし
			if (" ".equals(tokenStr) || "\t".equals(tokenStr) || "\n".equals(tokenStr)) {
				tokenStr = getString();
				continue;
			}

			// 先頭が「.」などの文字のケース
			if (Const.tokenSepalateSet.contains(tokenStr)) {
				return judge(tokenStr);
			}

			// 2文字目（またはそれ以降）を読み込み
			String ch = getString();
			if (ch == null) {
				break;
			}

			// 読んだ文字が定義に該当する場合は1文字戻して、１つ前のトークンで判定
			if (Const.tokenSepalateSet.contains(ch)) {
				br.reset();
				return judge(tokenStr);
			}

			tokenStr += ch;
			tokenStr = tokenStr.trim();

			// 改行のみの
			if ("".equals(tokenStr)) {
				tokenStr = getString();
				continue;
			}

			// ラインコメント
			if (tokenStr.startsWith(Const.ANA_COMMENT_LINE)) {
				// 改行までスキップ
				skipString("\n");
				tokenStr = getString();
				continue;
			}

			// ブロックコメント
			if (tokenStr.startsWith(Const.ANA_COMMENT_BLOCK)) {
				// 閉じコメントまでスキップ
				skipString("*/");
				tokenStr = getString();
				continue;
			}
		}

		return null;
	}

	private Token judge(String tokenStr) throws IOException {

		// keywordに一致
		if (Const.tokenSetKeyword.containsKey(tokenStr)) {
			Token token = new Token(Const.TOKEN_TYPE.KEYWORD);
			token.setKeyword(Const.tokenSetKeyword.get(tokenStr));
			return token;
		}

		// symbolに一致
		if (Const.tokenSymbolMap.containsKey(tokenStr)) {
			Token token = new Token(Const.TOKEN_TYPE.SYMBOL);
			token.setSymbol(Const.tokenSymbolMap.get(tokenStr));
			return token;
		}

		// integerConstantに一致
		if (tokenStr.matches(REG_INT)) {
			// 数値以外の出現まで収集
			tokenStr += skipString(Const.tokenNumSet);

			Token token = new Token(Const.TOKEN_TYPE.INTEGER_CONST);
			token.setIntVal(Integer.valueOf(tokenStr));
			return token;
		}

		// 文字リテラル
		if (tokenStr.startsWith(Const.ANA_STR_LITERAL_PERIOD)) {
			// 閉じダブルクォーテーションまで文字列を収集
			tokenStr = skipString("\"");

			Token token = new Token(Const.TOKEN_TYPE.STRING_CONST);
			token.setStringVal(tokenStr);
			return token;
		}

		// 上記以外はidentifer
		Token token = new Token(Const.TOKEN_TYPE.IDENTIFIER);
		token.setIdentifire(tokenStr);

		return token;
	}

	private String getString() throws IOException {
		if (!br.ready()) {
			return null;
		}
		br.mark(1024);
		return String.valueOf(((char) br.read()));
	}

	private String skipString(String target) throws IOException {
		String str = "";
		while (true) {
			String ch = getString();
			if (ch == null) {
				return null;
			}

			str += ch;

			int pos = str.indexOf(target);
			if (pos >= 0) {

				// targetの文字は読み捨てて返す
				return str.substring(0, str.length() - 1);
			}
		}
	}

	private String skipString(Set<String> validSet) throws IOException {
		String str = "";
		while (true) {
			// 数値以外を1文字余計に読むことになるので、事前にマーク
			br.mark(1024);

			String ch = getString();
			if (ch == null) {
				return null;
			}

			if (!validSet.contains(ch)) {

				// 数値以外を1文字多く読んでいるので読み込み位置をmarkの位置に戻す
				br.reset();
				return str;
			}

			str += ch;
		}
	}
}
