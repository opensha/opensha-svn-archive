package org.opensha.commons.gui.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.data.Range;

import com.google.common.collect.Lists;

public class GraphWindow extends JFrame {
	
	private GraphWidget widget;
	
	public GraphWindow(PlotElement elem, String plotTitle) {
		this(Lists.newArrayList(elem), plotTitle);
	}
	
	public GraphWindow(List<? extends PlotElement> elems, String plotTitle) {
		this(elems, plotTitle, generateDefaultChars(elems));
	}
	
	public GraphWindow(List<? extends PlotElement> elems, String plotTitle, List<PlotCurveCharacterstics> chars) {
		this(new PlotSpec(elems, chars, plotTitle, null, null));
	}
	
	public GraphWindow(PlotSpec spec) {
		this(new GraphWidget(spec));
	}
	
	public GraphWindow(PlotSpec plotSpec, PlotPreferences plotPrefs, boolean xLog, boolean yLog, Range xRange, Range yRange) {
		this(new GraphWidget(plotSpec, plotPrefs, xLog, yLog, xRange, yRange));
	}
	
	public GraphWindow(GraphWidget widget) {
		this.widget = widget;
		this.setContentPane(widget);
		this.setVisible(true);
	}
	
	public void setXAxisLabel(String xAxisLabel) {
		widget.setXAxisLabel(xAxisLabel);
	}
	
	public void setYAxisLabel(String yAxisLabel) {
		widget.setYAxisLabel(yAxisLabel);
	}
	
	public GraphWidget getGraphWidget() {
		return widget;
	}
	
	private static final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLUE);
	private static final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLACK);
	private static final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GREEN);
	private static final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.MAGENTA);
	private static final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.PINK);
	private static final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.LIGHT_GRAY);
	private static final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.RED);
	private static final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.ORANGE);
	private static final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.CYAN);
	private static final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.DARK_GRAY);
	private static final PlotCurveCharacterstics PLOT_CHAR11 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GRAY);
	
	protected static ArrayList<PlotCurveCharacterstics> generateDefaultChars(List<? extends PlotElement> elems) {
		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
		list.add(PLOT_CHAR1);
		list.add(PLOT_CHAR2);
		list.add(PLOT_CHAR3);
		list.add(PLOT_CHAR4);
		list.add(PLOT_CHAR5);
		list.add(PLOT_CHAR6);
		list.add(PLOT_CHAR7);
		list.add(PLOT_CHAR8);
		list.add(PLOT_CHAR9);
		list.add(PLOT_CHAR10);
		list.add(PLOT_CHAR11);
		
		if (elems == null)
			return list;
		
		int numChars = list.size();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		for(int i=0; i<elems.size(); ++i)
			plotChars.add(list.get(i%numChars));
		return plotChars;
	}
	
	public static List<Color> generateDefaultColors() {
		ArrayList<Color> colors = new ArrayList<Color>();
		for (PlotCurveCharacterstics pchar : generateDefaultChars(null))
			colors.add(pchar.getColor());
		return colors;
	}

}
