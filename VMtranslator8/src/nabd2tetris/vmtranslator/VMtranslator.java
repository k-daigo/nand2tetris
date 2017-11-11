package nabd2tetris.vmtranslator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nabd2tetris.Const;

public class VMtranslator {
	public static void main(String[] args) throws IOException {
		String filePath = args[0];

		VMtranslator translator = new VMtranslator();
		translator.exec(filePath);
	}

	private void exec(String filePath) throws IOException {
		File inFile = new File(filePath);

		List<String> list = new ArrayList<>();

		if (inFile.isDirectory()) {
			File[] fileFist = inFile.listFiles();
			for (File file : fileFist) {
				list.add(file.getCanonicalPath());
			}
		} else {
			list.add(filePath);
		}

		List<String> outAsmPathList = new ArrayList<>();

		for (String path : list) {
			String outAsmPath = asm(path);
			if (outAsmPath != null) {
				outAsmPathList.add(outAsmPath);
			}
		}

		// 複数asmをマージ
		if (inFile.isDirectory()) {
			String outPath = inFile.getCanonicalPath();
			String outFileName = outPath + File.separatorChar + inFile.getName() + ".asm";

			// BootStrap部を出力
			CodeWriter codeWriter = new CodeWriter(outFileName);
			codeWriter.writeInit();
			codeWriter.close();

			try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outFileName, true)))) {

				for (String outAsmPath : outAsmPathList) {
					try (BufferedReader br = new BufferedReader(new FileReader(outAsmPath))) {
						String line;
						while ((line = br.readLine()) != null) {
							pw.println(line);
						}
					}
				}
			}
		}
	}

	private String asm(String filePath) throws IOException {
		int point = filePath.lastIndexOf(".");
		String extName = filePath.substring(point, filePath.length());
		if (!".vm".equals(extName)) {
			return null;
		}

		String outPath = filePath.substring(0, point) + ".asm";
		CodeWriter codeWriter = new CodeWriter(outPath);

		Parser parser = new Parser(filePath);
		while (parser.advance()) {
			String commandType = parser.commandType();

			if (Const.C_ARITHMETIC.equals(commandType)) {
				codeWriter.writeArithmetic(parser.arg1());
				continue;
			}

			if (Const.C_PUSH.equals(commandType) || Const.C_POP.equals(commandType)) {
				codeWriter.writePushPop(commandType, parser.arg1(), Integer.parseInt(parser.arg2()));
				continue;
			}

			if (Const.C_LABEL.equals(commandType)) {
				codeWriter.writeLabel(parser.arg1());
				continue;
			}

			if (Const.C_IF.equals(commandType)) {
				codeWriter.writeIf(parser.arg1());
				continue;
			}

			if (Const.C_GOTO.equals(commandType)) {
				codeWriter.writeGoto(parser.arg1());
				continue;
			}

			if (Const.C_FUNCTION.equals(commandType)) {
				codeWriter.writeFunction(parser.arg1(), Integer.parseInt(parser.arg2()));
				continue;
			}

			if (Const.C_CALL.equals(commandType)) {
				codeWriter.writeCall(parser.arg1(), Integer.parseInt(parser.arg2()));
				continue;
			}

			if (Const.C_RETURN.equals(commandType)) {
				codeWriter.writeReturn();
				continue;
			}

		}

		codeWriter.close();

		return outPath;
	}

}
