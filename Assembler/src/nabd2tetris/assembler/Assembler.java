package nabd2tetris.assembler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Assembler {

	public static void main(String[] args) throws IOException {
		String filePath = args[0];

		Assembler assembler = new Assembler();
		SymbolTable symbolTable = assembler.createSymbolTable(filePath);
		assembler.exec(filePath, symbolTable);

	}

	public SymbolTable createSymbolTable(String filePath) throws IOException {
		SymbolTable symbolTable = new SymbolTable();

		int romAddr = 0;

		Parser parser = new Parser(filePath);
		while (parser.advance()) {

			String command = parser.getCommand();

			// (Xxx)のシンボル作成
			if (command.startsWith("(")) {
				String symbol = command.replaceAll("\\(", "");
				symbol = symbol.replaceAll("\\)", "");
				symbolTable.addEntry(symbol, romAddr);
			} else {
				romAddr++;

			}
		}

		return symbolTable;
	}

	public void exec(String filePath, SymbolTable symbolTable) throws IOException {
		StringBuilder sb = new StringBuilder();

		int ramAddr = 0x0010;
		Code code = new Code();

		Parser parser = new Parser(filePath);
		while (parser.advance()) {
			String commandType = parser.commandType();
			if (Const.A_COMMAND.equals(commandType)) {
				String address = parser.getAddress();

				int addr = 0;
				if (symbolTable.contains(address)) {
					addr = symbolTable.getAddress(address);
				} else {

					try {
						addr = Integer.parseInt(address);

					} catch (NumberFormatException e) {
						// 変数シンボルをシンボルテーブルに追加
						String command = parser.getCommand();
						String symbol = command.replaceAll("@", "");
						symbolTable.addEntry(symbol, ramAddr);
						addr = ramAddr;
						ramAddr++;
					}
				}

				sb.append(toBinString(addr));
				sb.append("\r\n");
				continue;
			}

			if (Const.C_COMMAND.equals(commandType)) {
				String mnComp = parser.comp();
				String mnDest = parser.dest();
				String mnJump = parser.jump();

				int comp = code.comp(mnComp);
				int dest = code.dest(mnDest);
				int jump = code.jump(mnJump);

				int codeVal = (7 << 13) | (comp << 6) | (dest << 3) | jump;
				sb.append(toBinString(codeVal));
				sb.append("\r\n");
			}
		}

		// 出力
		int point = filePath.lastIndexOf(".");
		String outPath = filePath.substring(0, point) + ".hack";

		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outPath, false)))) {
			pw.print(sb.toString());
		}

	}

	private String toBinString(int val) {
		String str = "0000000000000000" + Integer.toBinaryString(val);
		return str.substring(str.length() - 16);
	}
}
