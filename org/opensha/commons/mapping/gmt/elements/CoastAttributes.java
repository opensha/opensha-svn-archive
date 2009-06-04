package org.opensha.commons.mapping.gmt.elements;

import java.awt.Color;
import java.io.Serializable;

public class CoastAttributes implements Serializable {
	private Color fillColor = new Color(17, 73, 71);
	
	private Color lineColor = fillColor;
	private double lineSize = 1d;
	
	/**
	 * Default constructor, for filled ocean
	 */
	public CoastAttributes() {
		
	}
	
	/**
	 * Draw coastline only, black with the specified size
	 * 
	 * @param lineSize
	 */
	public CoastAttributes(double lineSize) {
		this(Color.BLACK, lineSize);
	}
	
	/**
	 * Draw coastline only with the specified color/size
	 * 
	 * @param lineColor
	 * @param lineSize
	 */
	public CoastAttributes(Color lineColor, double lineSize) {
		this.lineColor = lineColor;
		this.lineSize = lineSize;
		this.fillColor = null;
	}
	
	/**
	 * Fill the coast with the specified color, the line will be drawn with the same color.
	 * 
	 * @param fillColor
	 */
	public CoastAttributes(Color fillColor) {
		this.lineColor = fillColor;
		this.lineSize = 1d;
		this.fillColor = fillColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public double getLineSize() {
		return lineSize;
	}

	public void setLineSize(double lineSize) {
		this.lineSize = lineSize;
	}
}
