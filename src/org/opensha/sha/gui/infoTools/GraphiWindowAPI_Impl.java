/**
 * 
 */
package org.opensha.sha.gui.infoTools;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.Range;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;

/**
 * This class accepts a function list and plots it using Jfreechart
 * 
 * @author vipingupta
 *
 */
// TODO ArrayLists should be Lists
public class GraphiWindowAPI_Impl implements GraphWindowAPI {

	private ArrayList funcs;


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
	


	private boolean xLog = false, yLog = false;
	private ArrayList<PlotCurveCharacterstics> plotChars;
	private String plotTitle;
	private GraphWindow graphWindow;
	private boolean isCustomAxis = false;
	
	protected static ArrayList<PlotCurveCharacterstics> generateDefaultChars(List funcs) {
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
		
		if (funcs == null)
			return list;
		
		int numChars = list.size();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		for(int i=0; i<funcs.size(); ++i)
			plotChars.add(list.get(i%numChars));
		return plotChars;
	}
	
	public static List<Color> generateDefaultColors() {
		ArrayList<Color> colors = new ArrayList<Color>();
		for (PlotCurveCharacterstics pchar : generateDefaultChars(null))
			colors.add(pchar.getColor());
		return colors;
	}

	public GraphiWindowAPI_Impl(ArrayList funcs, String plotTitle) {
		this(funcs, plotTitle, generateDefaultChars(funcs));
		this.funcs = funcs;
	}
	
	public GraphiWindowAPI_Impl(XY_DataSet func, String plotTitle) {
		ArrayList funcs = new ArrayList();
		funcs.add(func);
		this.funcs = funcs;
		this.plotChars = generateDefaultChars(funcs);
		graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(plotTitle);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);
	}


	
	public GraphiWindowAPI_Impl(ArrayList funcs, String plotTitle, ArrayList<PlotCurveCharacterstics> plotChars) {
		this(funcs, plotTitle, plotChars, true);
	}
	
	public GraphiWindowAPI_Impl(ArrayList funcs, String plotTitle, ArrayList<PlotCurveCharacterstics> plotChars,
			boolean setVisible) {
		this.funcs=funcs;
		this.plotChars = plotChars;
		graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(plotTitle);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(setVisible);
	}

	/**
	 * Plot Graph using preferences
	 *
	 */
	public void refreshPlot() {
		graphWindow.plotGraphUsingPlotPreferences();
	}

	/**
	 * Set plot Title
	 * @param plotTitle
	 */
	public void setPlotTitle(String plotTitle) {
		this.plotTitle = plotTitle;
		graphWindow.setPlotLabel(plotTitle);
	}

	/**
	 * Get plot title
	 * 
	 * @return
	 */
	public String getPlotTitle() {
		return this.plotTitle;
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return funcs;
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public void setXLog(boolean xLog) {
		this.xLog = xLog;
		graphWindow.setX_Log(xLog);
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public void setYLog(boolean yLog) {
		this.yLog = yLog;
		graphWindow.setY_Log(yLog);
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return this.xLog;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return yLog;
	}

	public void setX_AxisLabel(String xAxisLabel) {
		graphWindow.setXAxisLabel(xAxisLabel);
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		if(graphWindow==null) return "X";
		return graphWindow.getXAxisLabel();
	}

	public void setY_AxisLabel(String yAxisLabel) {
		graphWindow.setYAxisLabel(yAxisLabel);
	}


	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		if(graphWindow==null) return "Y";
		return graphWindow.getYAxisLabel();
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		return plotChars;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public void setPlottingFeatures(ArrayList<PlotCurveCharacterstics> curveCharacteristics) {
		plotChars   = curveCharacteristics;
		this.graphWindow.setPlottingFeatures(curveCharacteristics);
	}
	
	public void setAllLineTypes(PlotLineType lineType, PlotSymbol symbol) {
		for(PlotCurveCharacterstics plotChar : plotChars) {
			plotChar.setLineType(lineType);
			plotChar.setSymbol(symbol);
		}
		this.setPlottingFeatures(plotChars);
	}

	/**
	 * sets the range for X and Y axis
	 * @param xMin : minimum value for X-axis
	 * @param xMax : maximum value for X-axis
	 * @param yMin : minimum value for Y-axis
	 * @param yMax : maximum value for Y-axis
	 *
	 */
	public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
		this.graphWindow.setAxisRange(xMin, xMax, yMin, yMax);
		isCustomAxis = true;
	}

	/**
	 * sets the range for X axis
	 * @param xMin : minimum value for X-axis
	 * @param xMax : maximum value for X-axis
	 *
	 */
	public void setX_AxisRange(double xMin, double xMax) {
		Range yAxisRange = graphWindow.getY_AxisRange();
		setAxisRange(xMin, xMax, yAxisRange.getLowerBound(), yAxisRange.getUpperBound());
	}
	
	/**
	 * sets the range for  Y axis
	 * @param yMin : minimum value for Y-axis
	 * @param yMax : maximum value for Y-axis
	 *
	 */
	public void setY_AxisRange(double yMin, double yMax) {
		Range xAxisRange = graphWindow.getX_AxisRange();
		setAxisRange(xAxisRange.getLowerBound(), xAxisRange.getUpperBound(), yMin, yMax);
	}
	
	
	/**
	 * Whether this is custom axis
	 */
	public boolean isCustomAxis() {
		return this.isCustomAxis;
	}

	/**
	 * set the auto range for the axis. 
	 */
	public void setAutoRange() {
		this.isCustomAxis = false;
		this.graphWindow.setAutoRange();
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getUserMinX() {
		return graphWindow.getUserMinX();
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getUserMaxX() {
		return graphWindow.getUserMaxX();
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getUserMinY() {
		return graphWindow.getUserMinY();
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getUserMaxY() {
		return graphWindow.getUserMaxY();
	}
	
	/**
	 * This returns the current Y-axis minimum value
	 * @return
	 */
	public double getY_AxisMin() {
		return graphWindow.getY_AxisRange().getLowerBound();
	}
	
	/**
	 * This returns the current Y-axis maximum value
	 * @return
	 */
	public double getY_AxisMax() {
		return graphWindow.getY_AxisRange().getUpperBound();
	}
	
	/**
	 * This returns the current X-axis minimum value
	 * @return
	 */
	public double getX_AxisMin() {
		return graphWindow.getX_AxisRange().getLowerBound();
	}
	
	/**
	 * This returns the current X-axis maximum value
	 * @return
	 */
	public double getX_AxisMax() {
		return graphWindow.getX_AxisRange().getUpperBound();
	}


	/**
	 * Set plot label font size
	 * 
	 * @param fontSize
	 */
	public void setPlotLabelFontSize(int fontSize) {
		this.graphWindow.setPlotLabelFontSize(fontSize);
	}



	/**
	 * Set the tick label font size
	 * 
	 * @param fontSize
	 */
	public void setTickLabelFontSize(int fontSize) {
		graphWindow.setTickLabelFontSize(fontSize);
	}
	
	/**
	 * Set the axis label font size
	 * 
	 * @param fontSize
	 */
	public void setAxisLabelFontSize(int fontSize) {
		graphWindow.setAxisLabelFontSize(fontSize);
	}

	/**
	 *
	 * @return the tick label font
	 * Default is 10
	 */
	public int getTickLabelFontSize(){
		return this.graphWindow.getTickLabelFontSize();
	}


	/**
	 *
	 * @return the axis label font size
	 * Default is 12
	 */
	public int getPlotLabelFontSize(){
		return this.graphWindow.getPlotLabelFontSize();
	}



	public static void main(String args[]) {
		ArrayList funcs = new ArrayList();

		ArbitrarilyDiscretizedFunc func  = new ArbitrarilyDiscretizedFunc();
		func.set(2.0, 3.0);
		func.set(0.5, 3.5);
		func.set(6.0, 1.0);
		funcs.add(func);

		func  = new ArbitrarilyDiscretizedFunc();
		func.set(1.0, 6);
		func.set(10.0, 7);
		func.set(2.0, 2);
		funcs.add(func);

		GraphiWindowAPI_Impl graphWindowImpl = new GraphiWindowAPI_Impl(funcs, "Test");
		//graphWindowImpl.setXLog(true);
		//graphWindowImpl.setYLog(true);
		graphWindowImpl.setPlotTitle("Test Title");
		graphWindowImpl.setX_AxisRange(0, 5);
		graphWindowImpl.setAutoRange();
		PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotLineType.SOLID,
				2f, Color.PINK);
		PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotLineType.SOLID,
				5f, Color.LIGHT_GRAY);
		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
		list.add(PLOT_CHAR5);
		list.add(PLOT_CHAR6);
		graphWindowImpl.setPlottingFeatures(list);
	}
	

	public void saveAsPDF(String fileName) throws IOException {
		graphWindow.saveAsPDF(fileName);
	}

	public void saveAsPNG(String fileName) throws IOException {
		graphWindow.saveAsPNG(fileName);
	}
	
	public GraphWindow getGraphWindow() {
		return graphWindow;
	}


}
