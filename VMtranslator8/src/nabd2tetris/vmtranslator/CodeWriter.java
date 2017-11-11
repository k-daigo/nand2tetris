package nabd2tetris.vmtranslator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import nabd2tetris.Const;

public class CodeWriter {
	private PrintWriter pw;
	private int labelIndex = 0;
	private int callLabelIndex = 0;

	private String baseFileName;

	public CodeWriter(String outPath) throws IOException {
		// 拡張子なしのファイル名を取得
		File file = new File(outPath);
		String fileName = file.getName();
		int point = fileName.lastIndexOf(".");
		baseFileName = fileName.substring(0, point);

		// 出力先のasmファイルを開いておく
		pw = new PrintWriter(new BufferedWriter(new FileWriter(outPath, false)));
	}

	public void writeArithmetic(String command) {
		if ("add".equals(command)) {
			pw.println("@SP   // ***add***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=D+M // add");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			return;
		}

		if ("sub".equals(command)) {
			pw.println("@SP   // ***sub***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=M-D // sub");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			return;
		}

		if ("neg".equals(command)) {
			pw.println("@SP   // ***neg***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=-M  // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			return;
		}

		if ("eq".equals(command)) {
			pw.println("@SP   // ***eq***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M-D     // eq");
			pw.println("M=-1      //");
			pw.println("@eqTrue" + labelIndex);
			pw.println("D;JEQ");
			pw.println("@SP   // Areg=0");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=0       //");
			pw.println("(eqTrue" + labelIndex + ")");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			labelIndex++;
			return;
		}

		if ("gt".equals(command)) {
			pw.println("@SP   // ***gt***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M-D     // gt");
			pw.println("M=0       //");
			pw.println("@gtTrue" + labelIndex);
			pw.println("D;JLE");
			pw.println("@SP   // Areg=0");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=-1       //");
			pw.println("(gtTrue" + labelIndex + ")");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			labelIndex++;
			return;
		}

		if ("lt".equals(command)) {
			pw.println("@SP   // ***lt***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M-D     // lt");
			pw.println("M=0       //");
			pw.println("@ltTrue" + labelIndex);
			pw.println("D;JGE");
			pw.println("@SP   // Areg=0");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=-1       //");
			pw.println("(ltTrue" + labelIndex + ")");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			labelIndex++;
			return;
		}

		if ("and".equals(command)) {
			pw.println("@SP   // ***and***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=D&M     // and");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			labelIndex++;
			return;
		}

		if ("or".equals(command)) {
			pw.println("@SP   // ***or***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("D=M   // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=D|M     // or");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			labelIndex++;
			return;
		}

		if ("not".equals(command)) {
			pw.println("@SP   // ***not***");
			pw.println("M=M-1 // SP-1");
			pw.println("A=M   // A=M[SP](SP Address)");
			pw.println("M=!M  // D=M(val2 to D)");

			pw.println("@SP   // Areg=0");
			pw.println("M=M+1 // SP+1");

			return;
		}
	}

	public void writePushPop(String command, String segment, int index) {
		if ("constant".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push constant " + index + "***");
				pw.println("D=A    // D=A(constant " + index + ")");
				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push (" + index + ") // M[SP]=D(constant " + index + ")");

				pw.println("@SP    // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}
		}

		if ("local".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push local " + index + "***");
				pw.println("D=A    // D=" + index + "");
				pw.println("@LCL   //");
				pw.println("D=D+M  // D=local address(LCL+" + index + ")");
				pw.println("A=D    // A=local address(LCL+" + index + ")");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop local " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // D=M(val to D)");

				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   // M[R13] = pop val");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@LCL  //");
				pw.println("D=D+M // LCL + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("argument".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push agument " + index + "***");
				pw.println("D=A    // D=" + index + ")");
				pw.println("@ARG  //");
				pw.println("D=D+M  // D=local address(ARG+index)");
				pw.println("A=D    // A=local address(ARG+index)");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop argument " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");
				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   //");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@ARG  //");
				pw.println("D=D+M // ARG + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("this".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push this " + index + "***");
				pw.println("D=A    // D=" + index + ")");
				pw.println("@THIS  //");
				pw.println("D=D+M  // D=local address(THIS+index)");
				pw.println("A=D    // A=local address(THIS+index)");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop this " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");
				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   //");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@THIS  //");
				pw.println("D=D+M // THIS + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("that".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push that " + index + "***");
				pw.println("D=A    // D=" + index + ")");
				pw.println("@THAT  //");
				pw.println("D=D+M  // D=local address(THAT+index)");
				pw.println("A=D    // A=local address(THAT+index)");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop that " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");
				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   //");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@THAT  //");
				pw.println("D=D+M // THAT + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("temp".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + index + "   // ***push temp " + index + "***");
				pw.println("D=A    // D=" + index + ")");
				pw.println("@5     // THAT");
				pw.println("D=D+M  // D=local address(THAT+index)");
				pw.println("A=D    // A=local address(THAT+index)");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop temp " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");
				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   //");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@5  //");
				pw.println("D=D+A // TEMP(RAM[5]) + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("pointer".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + (3 + index) + "  // ***push pointer " + index + "***");
				pw.println("D=M    // D=pointer(RAM[3+index]) address");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP    // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop pointer " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");
				pw.println("@R13  // top stack to @R13");
				pw.println("M=D   //");

				pw.println("@" + index + " // ");
				pw.println("D=A   //");
				pw.println("@3    // pointer(RAM[3])");
				pw.println("D=D+A // pointer(RAM[3]) + index to D");
				pw.println("@R14  //");
				pw.println("M=D   // address to R14");

				pw.println("@R13  //");
				pw.println("D=M   // top stack to D");
				pw.println("@R14  //");
				pw.println("A=M   //");
				pw.println("M=D   //");

				return;
			}
		}

		if ("static".equals(segment)) {
			if (Const.C_PUSH.equals(command)) {
				pw.println("@" + baseFileName + "." + index + " // @Xxxx.index");
				pw.println("D=M    // ");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP    // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop static " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // top stack to D");

				pw.println("@" + baseFileName + "." + index + " // @Xxxx.index");
				pw.println("M=D   //");

				return;
			}

		}
	}

	/**
	 * label
	 *
	 * @param label
	 */
	public void writeLabel(String label) {
		pw.println("(" + label + ") // ***label***");
	}

	/**
	 * goto
	 *
	 * @param label
	 */
	public void writeGoto(String label) {
		pw.println("@" + label + " // ***goto***");
		pw.println("0;JMP");
	}

	/**
	 * if-goto
	 *
	 * @param label
	 */
	public void writeIf(String label) {
		pw.println("@SP   // ***if-goto***");
		pw.println("M=M-1 // SP-1");
		pw.println("A=M   // A=M[SP](SP Address)");
		pw.println("D=M   //");
		pw.println("@" + label);
		pw.println("D;JNE");
	}

	/**
	 * function
	 *
	 * @param label
	 */
	public void writeFunction(String functionName, int numLocals) {
		pw.println("(" + functionName + ") // ***functionName***");
		for (int ii = 0; ii < numLocals; ii++) {
			writePushPop(Const.C_PUSH, "constant", 0);
		}
	}

	/**
	 * return
	 *
	 * @param label
	 */
	public void writeReturn() {
		pw.println("@LCL   // *** return ***");
		pw.println("D=M    // *** FRAME = LCL");
		pw.println("@FRAME //");
		pw.println("M=D    //");

		pw.println("@5      // *** RET = *(FRAME-5) *** ");
		pw.println("A=D-A   //");
		pw.println("D=M     //");
		pw.println("@RET    //");
		pw.println("M=D     //");

		pw.println("@SP     // *** *ARG = pop() ***");
		pw.println("AM=M-1   //");
		pw.println("D=M     //");
		pw.println("@ARG    //");
		pw.println("A=M     //");
		pw.println("M=D     //");

		pw.println("@ARG    // *** SP = ARG+1 ***");
		pw.println("D=M+1   //");
//		pw.println("D=A+1   //");
		pw.println("@SP     //");
		pw.println("M=D     //");

		pw.println("@FRAME  // *** THAT = *(FRAME-1) ***");
		pw.println("D=M     //");
		pw.println("@1      //");
		pw.println("A=D-A   //");
		pw.println("D=M     //");
		pw.println("@THAT   //");
		pw.println("M=D     //");

		pw.println("@FRAME  // *** THIS = *(FRAME-2) ***");
		pw.println("D=M     //");
		pw.println("@2      //");
		pw.println("A=D-A   //");
		pw.println("D=M     //");
		pw.println("@THIS   //");
		pw.println("M=D     //");

		pw.println("@FRAME  // *** ARG = *(FRAME-3) ***");
		pw.println("D=M     //");
		pw.println("@3      //");
		pw.println("A=D-A   //");
		pw.println("D=M     //");
		pw.println("@ARG    //");
		pw.println("M=D     //");

		pw.println("@FRAME  // *** LCL = *(FRAME-4) ***");
		pw.println("D=M     //");
		pw.println("@4      //");
		pw.println("A=D-A   //");
		pw.println("D=M     //");
		pw.println("@LCL    //");
		pw.println("M=D     //");

		pw.println("@RET      // *** goto RET ***");
		pw.println("A=M     //");
		pw.println("0;JMP     //");
	}

	/**
	 * call
	 *
	 * @param label
	 */
	public void writeCall(String functionName, int argNum) {
		String returnAddress = baseFileName + "." + callLabelIndex;
		pw.println("@" + returnAddress + " // *** call " + baseFileName + " ***");
		pw.println("D=A    // *** push return-address ***");
		pw.println("@SP    //");
		pw.println("A=M    //");
		pw.println("M=D    //");
		pw.println("@SP    //");
		pw.println("M=M+1  //");

		pw.println("@LCL   // *** push LCL ***");
		pw.println("D=M    //");
		pw.println("@SP    //");
		pw.println("A=M    //");
		pw.println("M=D    //");
		pw.println("@SP    //");
		pw.println("M=M+1  //");

		pw.println("@ARG   // *** push ARG ***");
		pw.println("D=M    //");
		pw.println("@SP    //");
		pw.println("A=M    //");
		pw.println("M=D    //");
		pw.println("@SP    //");
		pw.println("M=M+1  //");

		pw.println("@THIS   // *** push THIS ***");
		pw.println("D=M    //");
		pw.println("@SP    //");
		pw.println("A=M    //");
		pw.println("M=D    //");
		pw.println("@SP    //");
		pw.println("M=M+1  //");

		pw.println("@THAT   // *** push THAT ***");
		pw.println("D=M    //");
		pw.println("@SP    //");
		pw.println("A=M    //");
		pw.println("M=D    //");
		pw.println("@SP    //");
		pw.println("M=M+1  //");

		pw.println("@SP     // *** ARG = SP-n-5 ***");
		pw.println("D=M     //");
		pw.println("@" + (argNum + 5) + " //");
		pw.println("D=D-A   //");
		pw.println("@ARG    //");
		pw.println("M=D     //");

		pw.println("@SP    // *** LCL = SP ***");
		pw.println("D=M    //");
		pw.println("@LCL   //");
		pw.println("M=D    //");

		pw.println("@" + functionName + "   // *** goto f ***");
		pw.println("0;JMP  //");

		pw.println("(" + returnAddress + ")  // *** (return-address) ***");

		callLabelIndex++;
	}

	public void writeInit() {
		pw.println("@256  // *** bootstrap ***");
		pw.println("D=A");
		pw.println("@SP");
		pw.println("M=D");
		writeCall("Sys.init", 0);
	}

	public void close() {
		pw.close();
	}
}
