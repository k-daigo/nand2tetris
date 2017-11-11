package nabd2tetris.vmtranslator;

import java.io.IOException;

public class VMtranslator {

	public static void main(String[] args) throws IOException {
		String filePath = args[0];

		VMtranslator translator = new VMtranslator();
		translator.exec(filePath);
	}

	public void exec(String filePath) throws IOException {
		int point = filePath.lastIndexOf(".");
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

		}

		codeWriter.close();
	}

}
