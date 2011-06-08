/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.gui.plot.PlotLineType;
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
public class GraphWindowAPI_Impl implements GraphWindowAPI {
	protected ArrayList funcs;
	private String xAxisLabel, yAxisLabel;

	// SOLID LINES
	protected final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLUE);
	protected final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.RED);
	protected final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GREEN);
	protected final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLACK);
	protected final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.MAGENTA);
	protected final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.YELLOW);

	protected final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.CYAN);
	protected final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.DARK_GRAY);
	protected final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.ORANGE);
	protected final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.PINK);
	protected final PlotCurveCharacterstics PLOT_CHAR11 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.LIGHT_GRAY);
	protected final PlotCurveCharacterstics PLOT_CHAR12 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GRAY);


	// DASHED LINES
	protected final PlotCurveCharacterstics PLOT_CHAR13 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.BLUE);
	protected final PlotCurveCharacterstics PLOT_CHAR14 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.RED);
	protected final PlotCurveCharacterstics PLOT_CHAR15 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.GREEN);
	protected final PlotCurveCharacterstics PLOT_CHAR16 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.BLACK);
	protected final PlotCurveCharacterstics PLOT_CHAR17 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.MAGENTA);
	protected final PlotCurveCharacterstics PLOT_CHAR18 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.YELLOW);

	protected final PlotCurveCharacterstics PLOT_CHAR19 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.CYAN);
	protected final PlotCurveCharacterstics PLOT_CHAR20 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.DARK_GRAY);
	protected final PlotCurveCharacterstics PLOT_CHAR21 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.ORANGE);
	protected final PlotCurveCharacterstics PLOT_CHAR22 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.PINK);
	protected final PlotCurveCharacterstics PLOT_CHAR23 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.LIGHT_GRAY);
	protected final PlotCurveCharacterstics PLOT_CHAR24 = new PlotCurveCharacterstics(PlotLineType.DASHED,
			2f, Color.GRAY);
	private GraphWindow graphWindow;
	/**
	 * ArrayList of ArbitrarilyDiscretizedFunctions
	 */
	public GraphWindowAPI_Impl(ArrayList funcs, String xAxisLabel, String yAxisLabel, String plotLabel) {
		this.funcs = funcs;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		graphWindow= new GraphWindow(this);
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
		if(numFuncs>6) list.add(this.PLOT_CHAR7);
		if(numFuncs>7) list.add(this.PLOT_CHAR8);
		if(numFuncs>8) list.add(this.PLOT_CHAR9);
		if(numFuncs>9) list.add(this.PLOT_CHAR10);
		if(numFuncs>10) list.add(this.PLOT_CHAR11);
		if(numFuncs>11) list.add(this.PLOT_CHAR12);
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
	public double getUserMinX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getUserMaxX() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getUserMinY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getUserMaxY() {
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/**
	 * Save the plot as a PNG file
	 * 
	 * @param fpngFileName
	 */
	public void saveAsPNG(String pngFileName) {
		try {
			graphWindow.saveAsPNG(pngFileName);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroy the plot window
	 *
	 */
	public void destroy() {
		graphWindow.setVisible(false);
		graphWindow = null;
	}

}
