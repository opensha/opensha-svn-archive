/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * This class is used for plotting the MFDs for the EqkRateModel2.1
 * @author vipingupta
 *
 */
public class A_FaultsMFD_Plotter implements GraphWindowAPI {
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String RATE_AXIS_LABEL = "Rate (per year)";
	private final static String CUM_RATE_AXIS_LABEL = "Cum Rate (per year)";
	private String yAxisLabel;
	
	private ArrayList funcs;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.DARK_GRAY, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.YELLOW, 2);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 4);
	private final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 4);
	
	public A_FaultsMFD_Plotter(ArrayList funcs, boolean isCumRate) {
		this.funcs = funcs;
		if(isCumRate) yAxisLabel = CUM_RATE_AXIS_LABEL;
		else yAxisLabel = RATE_AXIS_LABEL;
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
	public boolean getXLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return X_AXIS_LABEL;
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
		 ArrayList list = new ArrayList();
		 list.add(PLOT_CHAR8);
		 list.add(PLOT_CHAR7);
		 list.add(PLOT_CHAR1);
		 list.add(PLOT_CHAR2);
		 list.add(PLOT_CHAR3);
		 list.add(PLOT_CHAR4);
		 list.add(PLOT_CHAR5);
		 list.add(PLOT_CHAR6);
		 return list;
	}
	

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		return 6.0;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return 8.5;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return 1e-7;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return 0.1;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}
}
