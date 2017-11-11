package nabd2tetris.jack.compiler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class VMWriter {
	public static enum Segment {
		CONST("constant"), ARG("argument"), LOCAL("local"), STATIC("static"), THIS("this"), THAT("that"), POINTER(
				"pointer"), TEMP("temp"),
				;

		private final String text;

		private Segment(final String text) {
			this.text = text;
		}

		public String getString() {
			return this.text;
		}

		public static Segment fromString(String text) {
			for (Segment segment : Segment.values()) {
				if (segment.text.equalsIgnoreCase(text)) {
					return segment;
				}
			}
			return null;
		}
	}

	private static Map<String, String> opMap = new HashMap<>();
	static {
		opMap.put("+", "add");
		opMap.put("-", "sub");
		opMap.put("*", "call Math.multiply 2");
		opMap.put("/", "call Math.divide 2");
		opMap.put("<", "lt");
		opMap.put(">", "gt");
		opMap.put("=", "eq");
		opMap.put("&", "and");
	}

	private PrintWriter pw;

	public VMWriter(String outFilePath) throws IOException {
		pw = new PrintWriter(new BufferedWriter(new FileWriter(outFilePath, false)));
	}

	public void writePush(Segment segment, int index) {
		pw.println("push " + segment.getString() + " " + index);
	}

	public void writePop(Segment segment, int index) {
		pw.println("pop " + segment.getString() + " " + index);
	}

	public void writeArithmetic(String op) {
		String wrOp = opMap.get(op);
		if (wrOp == null) {
			pw.println(op);
		} else {
			pw.println(opMap.get(op));
		}
	}

	public void writeLabel(String label) {
		pw.println("label " + label);
	}

	public void writeGoto(String label) {
		pw.println("goto " + label);
	}

	public void writeIf(String label) {
		pw.println("if-goto " + label);
	}

	public void writeCall(String name, int nArgs) {
		pw.println("call " + name + " " + nArgs);
	}

	public void writeFunction(String name, int nLocals) {
		pw.println("function " + name + " " + nLocals);
	}

	public void writeReturn() {
		pw.println("return");
	}

	public void writeComment(String comment) {
//		pw.println("// " + comment);
	}

	public void close() {
		pw.flush();
		pw.close();
	}

}
