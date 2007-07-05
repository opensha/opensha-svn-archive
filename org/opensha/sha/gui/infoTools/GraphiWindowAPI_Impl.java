/**
 * 
 */
package org.opensha.sha.gui.infoTools;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;

/**
 * This class accepts a function list and plots it using Jfreechart
 * 
 * @author vipingupta
 *
 */
public class GraphiWindowAPI_Impl implements GraphWindowAPI {

	private final static String DEFAULT_X_AXIS_LABEL = "X";
	private final static String DEFAULT_Y_AXIS_LABEL = "Y";
	
	private ArrayList funcs;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.LIGHT_GRAY, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 5);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	
	
	private String xAxisLabel = DEFAULT_X_AXIS_LABEL, yAxisLabel = DEFAULT_Y_AXIS_LABEL;
	private boolean isCustomAxis = false, xLog = false, yLog = false;
	private double minX, minY, maxX, maxY;
	private ArrayList<PlotCurveCharacterstics> plotChars;
	
	
	public GraphiWindowAPI_Impl(ArrayList funcs, String plotTitle) {
		this.funcs = funcs;
		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
		list.add(this.PLOT_CHAR1);
		list.add(this.PLOT_CHAR2);
		list.add(this.PLOT_CHAR3);
		list.add(this.PLOT_CHAR4);
		list.add(this.PLOT_CHAR5);
		list.add(this.PLOT_CHAR6);
		list.add(this.PLOT_CHAR7);
		int numChars = list.size();
		plotChars = new ArrayList<PlotCurveCharacterstics>();
		for(int i=0; i<funcs.size(); ++i)
			plotChars.add(list.get(i%numChars));
		
		GraphWindow graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(plotTitle);
		graphWindow.plotGraphUsingPlotPreferences();
		graphWindow.setVisible(true);
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
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public void setYLog(boolean yLog) {
		 this.yLog = yLog;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return xLog;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return yLog;
	}

	public void setX_AxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return xAxisLabel;
	}

	public void setY_AxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return yAxisLabel;
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
	}
	
	
	/**
	 * When set to True, please set minX, maxX, minY and maxY as well
	 * @param isCustomAxis
	 */
	public void setCustomAxis(boolean isCustomAxis) {
		this.isCustomAxis = isCustomAxis;
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return isCustomAxis;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		return minX;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return maxX;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return minY;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return maxY;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public void setMinX(double minX) {
		 this.minX = minX;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public void setMaxX(double maxX) {
		 this.maxX = maxX;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public void setMinY(double minY) {
		 this.minY = minY;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public void setMaxY(double maxY) {
		 this.maxY = maxY;
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
		
	}
	
}
