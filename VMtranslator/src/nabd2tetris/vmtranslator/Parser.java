package nabd2tetris.vmtranslator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Parser {
	private BufferedReader br;
	private String currLine;

	public Parser(String filePath) throws IOException {
		br = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
	}

	public boolean advance() throws IOException {
		while (br.ready()) {
			String line = br.readLine();
			// 空白、タブは除去
//			line = line.replaceAll(" ", "");
//			line = line.replaceAll("\t", "");

			if ("".equals(line)) {
				continue;
			}

			// コメントの除去
			String[] strs = line.split("//");
			if (strs.length == 0) {
				continue;
			}

			currLine = strs[0];

			if ("".equals(currLine)) {
				continue;
			}

			return true;
		}

		return false;
	}

	public String commandType() {
		if (currLine.startsWith("push")) {
			return Const.C_PUSH;
		}
		if (currLine.startsWith("pop")) {
			return Const.C_POP;
		}

		return Const.C_ARITHMETIC;
	}

	public String arg1() {
		String type = commandType();
		if(Const.C_ARITHMETIC.equals(type)){
			return currLine;
		}

		String[] args = currLine.split(" ");
		return args[1];
	}

	public String arg2() {
		String[] args = currLine.split(" ");
		return args[2];
	}
}
