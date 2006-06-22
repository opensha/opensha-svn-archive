/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 *  This class will used to plot the Mag Freq dist for RuptureModelApp
 *  
 * @author vipingupta
 *
 */
public class WG02_RuptureModelsGraphWindowAPI_Impl implements GraphWindowAPI {
	private ArrayList funcs;
	private String xAxisLabel, yAxisLabel;
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.YELLOW, 2);
	
	/**
	 * ArrayList of ArbitrarilyDiscretizedFunctions
	 */
	public WG02_RuptureModelsGraphWindowAPI_Impl(ArrayList funcs, String xAxisLabel, String yAxisLabel, String plotLabel) {
		this.funcs = funcs;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		GraphWindow graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel(plotLabel);
	    graphWindow.plotGraphUsingPlotPreferences();
	    //graphWindow.pack();
	    graphWindow.setVisible(true);;
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
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return xAxisLabel;
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
		 int numFuncs = this.funcs.size();
		 if(numFuncs>0) list.add(this.PLOT_CHAR1);
		 if(numFuncs>1) list.add(this.PLOT_CHAR2);
		 if(numFuncs>2) list.add(this.PLOT_CHAR3);
		 if(numFuncs>3) list.add(this.PLOT_CHAR4);
		 if(numFuncs>4) list.add(this.PLOT_CHAR5);
		 if(numFuncs>5) list.add(this.PLOT_CHAR6);
		 return list;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

}
