package nabd2tetris.vmtranslator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
	private PrintWriter pw;
	private int labelIndex = 0;

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
				pw.println("D=A    // D=" + index + ")");
				pw.println("@LCL   // Areg=300");
				pw.println("D=D+M  // D=local address(LCL+index)");
				pw.println("A=D    // A=local address(LCL+index)");
				pw.println("D=M    // D=local val");

				pw.println("@SP    // Areg=0");
				pw.println("A=M    // A=M[SP]");
				pw.println("M=D    // push");

				pw.println("@SP   // Areg=0");
				pw.println("M=M+1  // SP inc // M[SP]=M[SP]+1");

				return;
			}

			if (Const.C_POP.equals(command)) {
				pw.println("@SP   // ***pop constant " + index + "***");
				pw.println("M=M-1 // SP-1");
				pw.println("A=M   // A=M[SP](SP Address)");
				pw.println("D=M   // D=M(val to D)");

				pw.println("@LCL  //");
				pw.println("A=M   // A=M[LCL](LCL Address)");
				pw.println("M=D   // M[local+index]=D");

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
				pw.println("@SP   // ***pop that " + index + "***");
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

	public void close() {
		pw.close();
	}
}
