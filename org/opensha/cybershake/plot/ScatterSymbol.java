package org.opensha.cybershake.plot;

import org.opensha.cybershake.db.CybershakeSite;

public class ScatterSymbol {
	
	public static final String SYMBOL_SQUARE = "s";
	public static final String SYMBOL_DIAMOND = "d";
	public static final String SYMBOL_CIRCLE = "c";
	public static final String SYMBOL_STAR = "a";
	public static final String SYMBOL_OCTAGON = "g";
	public static final String SYMBOL_HEXAGON = "h";
	public static final String SYMBOL_INVERTED_TRIANGLE = "i";
	public static final String SYMBOL_PENTAGON = "n";
	public static final String SYMBOL_CROSS = "x";
	public static final String SYMBOL_Y_DASH = "y";
	
	public static final String SYMBOL_INVISIBLE = "DO NOT DISPLAY";
	
	String sym;
	
	int siteTypeID;
	
	double scale = 1;
	
	public ScatterSymbol(String sym, int siteTypeID, double scale) {
		this.sym = sym;
		this.siteTypeID = siteTypeID;
		this.scale = scale;
	}
	
	public ScatterSymbol(String sym, int siteTypeID) {
		this(sym, siteTypeID, 1d);
	}
	
	public boolean use(CybershakeSite site) {
		return site.type_id == siteTypeID;
	}
	
	public String getSymbol() {
		return sym;
	}
	
	public void setSymbol(String symbol) {
		this.sym = symbol;
	}
	
	public int getSiteTypeID() {
		return siteTypeID;
	}
	
	public double getScaleFactor() {
		return scale;
	}
}