/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.EqkRateModel2_ERF;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * This class is used for plotting the MFDs for the EqkRateModel2.1
 * @author vipingupta
 *
 */
public class EqkRateModel2_MFDsPlotter implements GraphWindowAPI {
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String Y_AXIS_LABEL = "Rate";
	
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
		      Color.BLACK, 5);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	private final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      Color.RED, 5);
	
	public EqkRateModel2_MFDsPlotter(EqkRateModel2_ERF eqkRateModelERF) {
		createFunctionList(eqkRateModelERF);
	}
	
	/**
	 * Create Function List
	 *
	 */
	private void createFunctionList(EqkRateModel2_ERF eqkRateModelERF) {
	
		funcs = new ArrayList();
		
		// Type A faults cum Dist
		EvenlyDiscretizedFunc cumDist = eqkRateModelERF.getTotal_A_FaultsMFD().getCumRateDist();
		cumDist.setInfo("Type A-Faults Total Mag Freq Dist");
		funcs.add(cumDist);
		 // Type B faults Char cum Dist
		cumDist = eqkRateModelERF.getTotal_B_FaultsCharMFD().getCumRateDist();
		cumDist.setInfo("Type B-Faults Total Char Mag Freq Dist");
		funcs.add(cumDist);
		//	Type B faults GR cum Dist
		cumDist = eqkRateModelERF.getTotal_B_FaultsGR_MFD().getCumRateDist();
		cumDist.setInfo("Type B-Faults Total GR Mag Freq Dist");
		funcs.add(cumDist);
		//	Background cum Dist
		cumDist = eqkRateModelERF.getTotal_BackgroundMFD().getCumRateDist();
		cumDist.setInfo("BackGround Total  Mag Freq Dist");
		funcs.add(cumDist);
		//	C zone cum Dist
		cumDist = eqkRateModelERF.getTotal_C_ZoneMFD().getCumRateDist();
		cumDist.setInfo("C Zone Total  Mag Freq Dist");
		funcs.add(cumDist);
		//	Total cum Dist
		cumDist = eqkRateModelERF.getTotalMFD().getCumRateDist();
		cumDist.setInfo("Total  Mag Freq Dist");
		funcs.add(cumDist);
		
		boolean includeAfterShocks = eqkRateModelERF.areAfterShocksIncluded();
		// historical best fit cum dist
		funcs.add(eqkRateModelERF.getObsBestFitCumMFD(includeAfterShocks));
		
		// historical cum dist
		funcs.addAll(eqkRateModelERF.getObsCumMFD(includeAfterShocks));
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
		return Y_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		 ArrayList list = new ArrayList();
		 list.add(this.PLOT_CHAR1);
		 list.add(this.PLOT_CHAR2);
		 list.add(this.PLOT_CHAR3);
		 list.add(this.PLOT_CHAR4);
		 list.add(this.PLOT_CHAR5);
		 list.add(this.PLOT_CHAR6);
		 list.add(this.PLOT_CHAR7);
		 list.add(this.PLOT_CHAR8);
		 list.add(this.PLOT_CHAR8);
		 list.add(this.PLOT_CHAR8);
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
		return 5.0;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return 9.255;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return 1e-4;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return 10;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}
}
