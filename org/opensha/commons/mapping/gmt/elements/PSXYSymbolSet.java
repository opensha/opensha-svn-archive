package org.opensha.commons.mapping.gmt.elements;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.mapping.gmt.elements.PSXYSymbol.Symbol;
import org.opensha.commons.util.cpt.CPT;

public class PSXYSymbolSet extends PSXYElement {
	
	private CPT cpt;
	private ArrayList<PSXYSymbol> symbols;
	private ArrayList<Double> vals;
	
	public PSXYSymbolSet() {
		symbols = new ArrayList<PSXYSymbol>();
		vals = new ArrayList<Double>();
	}
	
	public PSXYSymbolSet(CPT cpt, ArrayList<PSXYSymbol> symbols, ArrayList<Double> vals) {
		this(cpt, symbols, vals, 0, null, null);
	}
	
	public PSXYSymbolSet(CPT cpt, ArrayList<PSXYSymbol> symbols, ArrayList<Double> vals,
					double penWidth, Color penColor, Color fillColor) {
		super(penWidth, penColor, fillColor);
		this.cpt = cpt;
		this.symbols = symbols;
		this.vals = vals;
	}
	
	public void addSymbol(PSXYSymbol symbol, double val) {
		symbols.add(symbol);
		vals.add(val);
	}

	public CPT getCpt() {
		return cpt;
	}

	public void setCpt(CPT cpt) {
		this.cpt = cpt;
	}

	public ArrayList<PSXYSymbol> getSymbols() {
		return symbols;
	}

	public void setSymbols(ArrayList<PSXYSymbol> symbols) {
		this.symbols = symbols;
	}

	public ArrayList<Double> getVals() {
		return vals;
	}

	public void setVals(ArrayList<Double> vals) {
		this.vals = vals;
	}

}
