package org.opensha.commons.gui.plot;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.NoSuchElementException;

import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

import com.google.common.base.Preconditions;

/**
 * These are the supported line syles used in OpenSHA JFreeChart plots.
 * 
 * @author kevin
 *
 */
public enum PlotLineType {
	
	SOLID("Solid"),
	DOTTED("Dotted"),
	DASHED("Dashed"),
	DOTTED_AND_DASHED("Dotted & Dashed"),
	HISTOGRAM("Histogram"),
	STACKED_BAR("Stacked Bar");
	
	private String desc;
	
	private PlotLineType(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	public Stroke buildStroke(float lineWidth) {
		Preconditions.checkArgument(lineWidth>0, "Line width must be >0");
		if (this == SOLID)
			return new BasicStroke(lineWidth);
		else if (this == DOTTED)
			return new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL,0,new float[] {1},0);
		else if (this == DASHED)
			return new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL,0,new float[] {9},0);
		else if (this == DOTTED_AND_DASHED)
			return new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_BEVEL,0,new float[] {5,3,2,3},0);
		else
			throw new IllegalStateException("Stroke not applicable for lineType: "+this);
	}
	
	public static PlotLineType forString(String desc) {
		for (PlotLineType plt : values()) {
			if (plt.desc.equalsIgnoreCase(desc))
				return plt;
		}
		throw new NoSuchElementException("No line type exists for '"+desc+"'");
	}
	
	/**
	 * Builds a render for the given <code>PlotLineType</code> and/or <code>PlotSymbol</code>.
	 * 
	 * @param plt plot line type, or null for none
	 * @param sym plot symbol type, or null for none
	 * @param width width of the line or symbol. if line and symbol, symbol width will equal <code>4*width</code>
	 * @throws IllegalStateException when both plt and sym are null
	 * @return
	 */
	public static XYItemRenderer buildRenderer(PlotLineType plt, PlotSymbol sym, float width)
	throws IllegalStateException {
		if (plt == null)
			return buildRenderer(plt, sym, width, width);
		else
			return buildRenderer(plt, sym, width, width*4);
	}
	
	/**
	 * @return true if the line type is compatible with symbols, false otherwise
	 */
	public boolean isSymbolCompatible() {
		return !(this == HISTOGRAM || this == STACKED_BAR);
	}
	
	public static void checkValidConfiguration(PlotLineType plt, PlotSymbol sym) {
		Preconditions.checkState(sym != null || plt != null,
				"Must supply either a plot line type, or a symbol.");
		Preconditions.checkState(plt == null || (sym == null || plt.isSymbolCompatible()),
				"A symbol cannot be suplied with a line type that doesn't support symbols.");
	}
	
	/**
	 * Builds a render for the given <code>PlotLineType</code> and/or <code>PlotSymbol</code>.
	 * 
	 * @param plt plot line type, or null for none
	 * @param sym plot symbol type, or null for none
	 * @param lineWidth width of the line, if not null
	 * @param symWidth width of the symbols, if not null
	 * @throws IllegalStateException when both plt and sym are null
	 * @return
	 */
	public static XYItemRenderer buildRenderer(PlotLineType plt, PlotSymbol sym, float lineWidth, float symWidth)
	throws IllegalStateException {
		checkValidConfiguration(plt, sym);
		XYItemRenderer renderer = null;
		if (plt != null) {
			Preconditions.checkArgument(lineWidth > 0, "line widht must be >0");
			if (plt == HISTOGRAM || plt == STACKED_BAR) {
				StackedXYBarRenderer sbRend = new StackedXYBarRenderer();
				sbRend.setShadowVisible(false);
				if (plt == HISTOGRAM) {
					sbRend.setMargin(0.1);
					sbRend.setBarPainter(new StandardXYBarPainter());
				}
				renderer = sbRend;
			} else {
				int type = sym == null ? StandardXYItemRenderer.LINES : StandardXYItemRenderer.SHAPES_AND_LINES;
				renderer = new StandardXYItemRenderer(type);
				Stroke stroke = plt.buildStroke(lineWidth);
				renderer.setStroke(stroke);
//				renderer.setBaseStroke(stroke);
			}
		}
		if (sym != null) {
			StandardXYItemRenderer stdRend;
			if (renderer == null)
				stdRend = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES);
			else {
				Preconditions.checkState(renderer instanceof StandardXYItemRenderer,
						"Renderer already exists but isn't correct type for plt="+plt+" and sym="+sym);
				stdRend = (StandardXYItemRenderer)renderer;
			}
			Preconditions.checkArgument(symWidth > 0, "symbol widht must be >0");
			Shape shape = sym.buildShape(symWidth);
			Preconditions.checkNotNull(shape, "Couldn't build shape for symbol: "+sym);
			stdRend.setShape(shape);
//			stdRend.setBaseShape(shape);
			stdRend.setShapesFilled(sym.isFilled());
//			stdRend.setBaseShapesFilled(sym.isFilled());
			renderer = stdRend;
			
		}
		return renderer;
	}

}
