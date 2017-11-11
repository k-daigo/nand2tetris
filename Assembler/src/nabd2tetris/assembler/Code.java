package nabd2tetris.assembler;

public class Code {

	public int dest(String mnemonic) {
		if ("".equals(mnemonic)) {
			return 0;
		}

		if ("0".equals(mnemonic)) {
			return 0;
		}
		if ("M".equals(mnemonic)) {
			return 1;
		}
		if ("D".equals(mnemonic)) {
			return 2;
		}
		if ("MD".equals(mnemonic)) {
			return 3;
		}
		if ("A".equals(mnemonic)) {
			return 4;
		}
		if ("AM".equals(mnemonic)) {
			return 5;
		}
		if ("AD".equals(mnemonic)) {
			return 6;
		}
		if ("AMD".equals(mnemonic)) {
			return 7;
		}

		throw new IllegalArgumentException();
	}

	public int comp(String mnemonic) {
		if ("".equals(mnemonic)) {
			return 0;
		}

		if ("0".equals(mnemonic)) {
			return Integer.parseInt("0101010", 2);
		}
		if ("1".equals(mnemonic)) {
			return Integer.parseInt("0111111", 2);
		}
		if ("-1".equals(mnemonic)) {
			return Integer.parseInt("0111010", 2);
		}
		if ("D".equals(mnemonic)) {
			return Integer.parseInt("0001100", 2);
		}
		if ("A".equals(mnemonic)) {
			return Integer.parseInt("0110000", 2);
		}
		if ("M".equals(mnemonic)) {
			return Integer.parseInt("1110000", 2);
		}
		if ("!D".equals(mnemonic)) {
			return Integer.parseInt("0001101", 2);
		}
		if ("!A".equals(mnemonic)) {
			return Integer.parseInt("0110001", 2);
		}
		if ("!M".equals(mnemonic)) {
			return Integer.parseInt("1110001", 2);
		}
		if ("-D".equals(mnemonic)) {
			return Integer.parseInt("0001111", 2);
		}
		if ("-A".equals(mnemonic)) {
			return Integer.parseInt("0110011", 2);
		}
		if ("-M".equals(mnemonic)) {
			return Integer.parseInt("1110011", 2);
		}
		if ("D+1".equals(mnemonic)) {
			return Integer.parseInt("0011111", 2);
		}
		if ("A+1".equals(mnemonic)) {
			return Integer.parseInt("0110111", 2);
		}
		if ("M+1".equals(mnemonic)) {
			return Integer.parseInt("1110111", 2);
		}
		if ("D-1".equals(mnemonic)) {
			return Integer.parseInt("0001110", 2);
		}
		if ("A-1".equals(mnemonic)) {
			return Integer.parseInt("0110010", 2);
		}
		if ("M-1".equals(mnemonic)) {
			return Integer.parseInt("1110010", 2);
		}
		if ("D+A".equals(mnemonic)) {
			return Integer.parseInt("0000010", 2);
		}
		if ("D+M".equals(mnemonic)) {
			return Integer.parseInt("1000010", 2);
		}
		if ("D-A".equals(mnemonic)) {
			return Integer.parseInt("0010011", 2);
		}
		if ("D-M".equals(mnemonic)) {
			return Integer.parseInt("1010011", 2);
		}
		if ("A-D".equals(mnemonic)) {
			return Integer.parseInt("0000111", 2);
		}
		if ("M-D".equals(mnemonic)) {
			return Integer.parseInt("1000111", 2);
		}
		if ("D&A".equals(mnemonic)) {
			return Integer.parseInt("0000000", 2);
		}
		if ("D&M".equals(mnemonic)) {
			return Integer.parseInt("1000000", 2);
		}
		if ("D|A".equals(mnemonic)) {
			return Integer.parseInt("0010101", 2);
		}
		if ("D|M".equals(mnemonic)) {
			return Integer.parseInt("1010101", 2);
		}

		throw new IllegalArgumentException();
	}

	public int jump(String mnemonic) {
		if ("".equals(mnemonic)) {
			return 0;
		}

		if ("JGT".equals(mnemonic)) {
			return 1;
		}
		if ("JEQ".equals(mnemonic)) {
			return 2;
		}
		if ("JGE".equals(mnemonic)) {
			return 3;
		}
		if ("JLT".equals(mnemonic)) {
			return 4;
		}
		if ("JNE".equals(mnemonic)) {
			return 5;
		}
		if ("JLE".equals(mnemonic)) {
			return 6;
		}
		if ("JMP".equals(mnemonic)) {
			return 7;
		}

		throw new IllegalArgumentException();
	}
}
