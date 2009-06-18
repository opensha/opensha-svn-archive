package org.opensha.commons.mapping.gmt.elements;

import java.awt.Color;

public class PSXYSymbol extends PSXYElement {
	
	public enum Symbol {
		SQUARE ("s"),
		DIAMOND ("d"),
		CIRCLE ("c"),
		STAR ("a"),
		OCTAGON ("g"),
		HEXAGON ("h"),
		INVERTED_TRIANGLE ("i"),
		PENTAGON ("n"),
		CROSS ("x"),
		Y_DASH ("y");
		
		private String val;
		Symbol(String val) {
			this.val = val;
		}
		
		public String val() {
			return val;
		}
	}
	
	private Symbol symbol;
	
	public PSXYSymbol(Symbol symbol) {
		super();
		this.symbol = symbol;
	}
	
	public PSXYSymbol(Symbol symbol, double penWidth, Color penColor, Color fillColor) {
		super(penWidth, penColor, fillColor);
		this.symbol = symbol;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

}
