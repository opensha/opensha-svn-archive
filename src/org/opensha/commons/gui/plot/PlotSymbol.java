package org.opensha.commons.gui.plot;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.NoSuchElementException;

import org.jfree.util.ShapeUtilities;

import com.google.common.base.Preconditions;

public enum PlotSymbol {
	
	X("X symbols", false),
	CROSS("+ symbols", false),
	BOLD_X("Bold X symbols", true),
	BOLD_CROSS("Bold + symbols", true),
	FILLED_CIRCLE("Filled Circles", true),
	CIRCLE("Circles", false),
	FILLED_SQUARE("Filled Squares", true),
	SQUARE("Squares", false),
	FILLED_TRIANGLE("Filled Triangles", true),
	TRIANGLE("Triangles", false),
	FILLED_INV_TRIANGLE("Filled Inv. Triangles", true),
	INV_TRIANGLE("Inv. Triangles", false),
	FILLED_DIAMOND("Filled Diamonds", true),
	DIAMOND("Diamonds", false);
	
	private String desc;
	private boolean filled;
	
	private PlotSymbol(String desc, boolean filled) {
		this.desc = desc;
		this.filled = filled;
	}
	
	public boolean isFilled() {
		return filled;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	public static PlotSymbol forString(String desc) {
		for (PlotSymbol sym : values()) {
			if (sym.desc.equalsIgnoreCase(desc))
				return sym;
		}
		throw new NoSuchElementException("No symbol exists for '"+desc+"'");
	}
	
	private final static double SIZE = 2;
	private final static double DELTA = SIZE / 2.0;
	
	public Shape buildShape(float width) {
		Preconditions.checkArgument(width>0, "width must be >0");
		if (this == CIRCLE || this == FILLED_CIRCLE)
			return new Ellipse2D.Double(-DELTA-width/2,
					-DELTA-width/2, SIZE+width, SIZE+width);
		else if (this == SQUARE || this == FILLED_SQUARE)
			return new Rectangle.Double(-DELTA-width/2,
					-DELTA-width/2, SIZE+width, SIZE+width);
		else if (this == TRIANGLE || this == FILLED_TRIANGLE)
			return ShapeUtilities.createUpTriangle(width);
		else if (this == INV_TRIANGLE || this == FILLED_INV_TRIANGLE)
			return ShapeUtilities.createDownTriangle(width);
		else if (this == DIAMOND || this == FILLED_DIAMOND)
			return ShapeUtilities.createDiamond(width);
		else if (this == X)
			return ShapeUtilities.createDiagonalCross(width,0.1f);
		else if (this == BOLD_X)
			return ShapeUtilities.createDiagonalCross(width,width*0.25f);
		else if (this == CROSS)
			return ShapeUtilities.createRegularCross(width,0.1f);
		else if (this == BOLD_CROSS)
			return ShapeUtilities.createRegularCross(width,width*0.25f);
		else
			throw new UnsupportedOperationException("Can't build shape for symbol: "+toString());
	}

}
