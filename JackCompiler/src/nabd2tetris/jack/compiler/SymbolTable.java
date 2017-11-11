package nabd2tetris.jack.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SymbolTable {
	private Map<String, Symbol> identiferMap = new HashMap<>();

	public void startSubroutine() {
		identiferMap = new HashMap<>();
	}

	public int getSize() {
		return identiferMap.size();
	}

	public void define(String name, String type, String kind) {
		int index = varCount(kind);
		identiferMap.put(name, new Symbol(name, type, kind, index));
	}

	public Symbol getSymbol(String name) {
		return identiferMap.get(name);
	}

	public int varCount(String kind) {
		return (int) identiferMap.entrySet().stream()
				.filter(value -> kind.equals(value.getValue().kind))
				.count();
	}

	public String kindOf(String name) {
		Symbol symbol = identiferMap.get(name);
		if (symbol == null) {
			return null;
		}

		return symbol.kind;
	}

	public String typeOf(String name) {
		Symbol symbol = identiferMap.get(name);
		if (symbol == null) {
			return null;
		}

		return symbol.type;
	}

	public int indexOf(String name) {
		Symbol symbol = identiferMap.get(name);
		if (symbol == null) {
			return -1;
		}

		return symbol.index;
	}

	public class Symbol {
		String name;
		String type;
		String kind;
		int index = 0;

		public Symbol(String name, String type, String kind, int index) {
			this.name = name;
			this.type = type;
			this.kind = kind;
			this.index = index;
		}

		@Override
		public String toString() {
			return "Symbol [name=" + name + ", type=" + type + ", kind=" + kind + ", index=" + index + "]";
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Entry<String, Symbol> entry : identiferMap.entrySet()) {
			sb.append(entry.getValue()).append(", ");
		}

		return sb.toString();
	}
}
