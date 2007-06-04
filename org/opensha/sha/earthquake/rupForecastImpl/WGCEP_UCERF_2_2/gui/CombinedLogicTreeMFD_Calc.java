/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.EqkRateModel2_ERF;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * 
 * It calculates the final MFD after combining various logic tree branches 
 * 
 * @author vipingupta
 *
 */
public class CombinedLogicTreeMFD_Calc implements GraphWindowAPI {
	// Eqk Rate Model 2 ERF
	private EqkRateModel2_ERF eqkRateModel2ERF = new EqkRateModel2_ERF();
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String Y_AXIS_LABEL = "Rate";
	private ArrayList funcs;
	
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 5);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	private final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		      Color.RED, 5);

	
	public CombinedLogicTreeMFD_Calc() {
		createFunctionList();
	}
	
	
	/**
	 * Create Function List
	 *
	 */
	private void createFunctionList( ) {
		
		
		eqkRateModel2ERF.setParamDefaults();

		SummedMagFreqDist totalMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG, EqkRateModel2_ERF.NUM_MAG);
		int count=1;
		for(int defModel=0; defModel<2; ++defModel) { // deformation model
			
			if(defModel==0) eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.1");
			else eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME).setValue("D2.4");
			
			for(int solType=0; solType<2; ++solType) { // A-Fault solution type
				if(solType==0) eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).setValue(EqkRateModel2_ERF.SEGMENTED_A_FAULT_MODEL);
				else eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME).setValue(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL);

				for(int wt=0; wt<2; ++wt) { // Wt on A-Priori Rates
					if(solType!=1) { // Apriori weights do not matter for unsegmneted option
						if(wt==0) eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.REL_A_PRIORI_WT_PARAM_NAME).setValue(new Double(1e-4));
						else eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.REL_A_PRIORI_WT_PARAM_NAME).setValue(new Double(1e7));
					}
					for(int magArea=0; magArea<2; ++magArea) { // Mag Area Relationship
						if(magArea==0) eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(Ellsworth_B_WG02_MagAreaRel.NAME);
						else eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME).setValue(HanksBakun2002_MagAreaRel.NAME);
							
						for(int connectBFaults=0; connectBFaults<2; ++connectBFaults) { // connect More B-faults
							if(connectBFaults==0) eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME).setValue(new Boolean(false));
							else eqkRateModel2ERF.getParameter(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME).setValue(new Boolean(true));
							
							eqkRateModel2ERF.updateForecast();
							totalMFD.addResampledMagFreqDist(eqkRateModel2ERF.getTotalMFD(), true);
							System.out.println("Run "+count++);

						}
					}

				}

			}


		}
	
		// CREATE FUNCTION LIST
		funcs = new ArrayList();
		//	Total cum Dist
		EvenlyDiscretizedFunc cumDist = totalMFD.getCumRateDist();
		cumDist.setInfo("Total  Mag Freq Dist");
		funcs.add(cumDist);
		boolean includeAfterShocks = eqkRateModel2ERF.areAfterShocksIncluded();
		// historical best fit cum dist
		funcs.add(eqkRateModel2ERF.getObsBestFitCumMFD(includeAfterShocks));
		
		// historical cum dist
		funcs.addAll(eqkRateModel2ERF.getObsCumMFD(includeAfterShocks));
		
		
		// SHOW THE PLOT
		GraphWindow graphWindow= new GraphWindow(this);
	    graphWindow.setPlotLabel("Total MFD");
	    graphWindow.plotGraphUsingPlotPreferences();
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
	
	public static void main(String[] args) {
		CombinedLogicTreeMFD_Calc mfdPlotter = new CombinedLogicTreeMFD_Calc();
	}
	
}
