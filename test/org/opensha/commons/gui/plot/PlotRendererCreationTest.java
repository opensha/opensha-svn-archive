package org.opensha.commons.gui.plot;

import static junit.framework.Assert.*;

import java.awt.Shape;
import java.awt.Stroke;

import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.junit.Test;

public class PlotRendererCreationTest {
	
	public static void testSymbolRendererCorrect(PlotSymbol sym, XYItemRenderer renderer) {
		assertTrue("renderer should have symbols, but isn't correct type for sym="+sym,
				renderer instanceof StandardXYItemRenderer);
		StandardXYItemRenderer stdRend = (StandardXYItemRenderer)renderer;
//		Shape fromRend = stdRend.getSeriesShape(0);
		Shape fromRend = stdRend.getBaseShape();
		Shape fromSym = sym.buildShape(1f);
		assertNotNull("shape should not be null!", fromRend);
		assertTrue("shape is of wrong instance (expected: "+fromSym.getClass()
				+" but got: "+fromRend.getClass()+")", fromSym.getClass().isInstance(fromRend));
	}
	
	public static void testLineRendererCorrect(PlotLineType plt, XYItemRenderer renderer) {
		if (plt.isSymbolCompatible()) {
			assertTrue("renderer should have lines & is symbol compatible, but isn't correct type for sym="+plt,
					renderer instanceof StandardXYItemRenderer);
			
			StandardXYItemRenderer stdRend = (StandardXYItemRenderer)renderer;
			Stroke fromRend = stdRend.getBaseStroke();
			assertNotNull("stroke should not be null!", fromRend);
		} else {
			assertTrue("renderer should have lines & isn'tsymbol compatible, but isn't correct type for sym="+plt,
					renderer instanceof StackedXYBarRenderer);
		}
	}

	@Test
	public void testAllCombinations() {
		for (PlotLineType plt : PlotLineType.values()) {
			for (PlotSymbol sym : PlotSymbol.values()) {
				if (plt.isSymbolCompatible()) {
					XYItemRenderer renderer = PlotLineType.buildRenderer(plt, sym, 1f);
					testSymbolRendererCorrect(sym, renderer);
					testLineRendererCorrect(plt, renderer);
				}
			}
		}
	}
	
	@Test (expected=IllegalStateException.class)
	public void testBothNull() {
		PlotLineType.buildRenderer(null, null, 1f);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testColorerWhenNotSupported() {
		PlotLineType.buildRenderer(PlotLineType.HISTOGRAM, PlotSymbol.CIRCLE, 1f);
	}
	
	@Test
	public void testOnlyLine() {
		for (PlotLineType plt : PlotLineType.values()) {
			XYItemRenderer renderer = PlotLineType.buildRenderer(plt, null, 1f);
			testLineRendererCorrect(plt, renderer);
		}
	}
	
	@Test
	public void testOnlySymbols() {
		for (PlotSymbol sym : PlotSymbol.values()) {
			XYItemRenderer renderer = PlotLineType.buildRenderer(null, sym, 1f);
			testSymbolRendererCorrect(sym, renderer);
		}
	}
}
