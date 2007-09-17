/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * This class generates histogram plots for contribution to total probability from various logic tree branches
 * 
 * @author vipingupta
 *
 */
public class ProbabilityDistHistogramPlotter implements GraphWindowAPI {
	private final static double MIN_PROB= 0.05;
	private final static double MAX_PROB= 0.95;
	private final static double DELTA_PROB= 0.1;
	private final static int NUM_PROB= Math.round((float)((MAX_PROB-MIN_PROB)/DELTA_PROB))+1;
	private final static String X_AXIS_LABEL = "Probability";
	private final static String Y_AXIS_LABEL = "Contribution";
	private final static String PLOT_LABEL = "Probability Contribution";
	
	private final PlotCurveCharacterstics PLOT_HISTOGRAM = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
		      new Color(0,0,0), 2); // black
	
	private ArrayList funcs;
	private ArrayList<PlotCurveCharacterstics> plottingCurveChars;
	
	/**
	 * Plot histograms of probability contributions from various branches
	 * 
	 * @param minMag
	 */
	public void plotTotalProbHistogramAboveMag(double minMag) {
		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(MIN_PROB, MAX_PROB, NUM_PROB);
		func.setTolerance(DELTA_PROB);
		UCERF2_TimeDependentEpistemicList ucerf2EpistemicList = new UCERF2_TimeDependentEpistemicList();
		int numERFs = ucerf2EpistemicList.getNumERFs(); 
		for(int i=0; i<numERFs; ++i) {
			System.out.println("Doing run "+(i+1)+" of "+numERFs);
			UCERF2 ucerf2 = (UCERF2)ucerf2EpistemicList.getERF(i);
			ucerf2.updateForecast();
			double wt= ucerf2EpistemicList.getERF_RelativeWeight(i);
			double prob = ucerf2.getTotalProb(minMag);
			func.add(prob, wt);
		}
		funcs = new ArrayList();
		funcs.add(func);
		plottingCurveChars = new ArrayList<PlotCurveCharacterstics>();
		plottingCurveChars.add(PLOT_HISTOGRAM);
		GraphWindow graphWindow= new GraphWindow(this);
		graphWindow.setPlotLabel(PLOT_LABEL);
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
	public ArrayList<PlotCurveCharacterstics> getPlottingFeatures() {
		 return plottingCurveChars;
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
		//return 5.0;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		//return 9.255;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		//return 1e-4;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		//return 10;
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	public static void main(String[] args) {
		ProbabilityDistHistogramPlotter plotter = new ProbabilityDistHistogramPlotter();
		plotter.plotTotalProbHistogramAboveMag(6.7);
	}

}
