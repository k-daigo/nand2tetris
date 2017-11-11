package nabd2tetris.assembler;

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

	public String getAddress() {
		return currLine.substring(1);
	}

	public String getCommand() {
		return currLine;
	}

	public boolean advance() throws IOException {
		while (br.ready()) {
			String line = br.readLine();
			// 空白、タブは除去
			line = line.replaceAll(" ", "");
			line = line.replaceAll("\t", "");

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
		if ("@".equals(currLine.substring(0, 1))) {
			return Const.A_COMMAND;
		}
		if (currLine.indexOf("=") >= 0) {
			return Const.C_COMMAND;
		}
		if (currLine.indexOf(";") >= 0) {
			return Const.C_COMMAND;
		}
		return Const.L_COMMAND;
	}

	public String symbol() {
		if ("@".equals(currLine.substring(0, 1))) {
			return currLine.substring(1);
		}
		return currLine;
	}

	public String dest() {
		String[] strs = currLine.split("=");
		if (strs.length == 2) {
			return strs[0];
		}

//		strs = currLine.split(";");
//		if (strs.length == 2) {
//			return strs[0];
//		}

		return "";
	}

	public String comp() {
		String[] strs = currLine.split("=");
		if (strs.length == 2) {
			return strs[1];
		}
		strs = currLine.split(";");
		if (strs.length == 2) {
			return strs[0];
		}

		return "";
	}

	public String jump() {
		String[] strs = currLine.split(";");
		if (strs.length <= 1) {
			return "";
		}

		return strs[1];
	}
}
