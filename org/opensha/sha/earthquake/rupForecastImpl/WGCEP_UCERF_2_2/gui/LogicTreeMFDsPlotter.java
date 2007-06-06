/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.gui;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.EqkRateModel2_ERF;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;


/**
 * This is used for plotting various logic tree MFDs
 * 
 * @author vipingupta
 *
 */
public class LogicTreeMFDsPlotter implements GraphWindowAPI {
	
	private final static String X_AXIS_LABEL = "Magnitude";
	private final static String Y_AXIS_LABEL = "Rate";
	
//	 Eqk Rate Model 2 ERF
	private EqkRateModel2_ERF eqkRateModel2ERF = new EqkRateModel2_ERF();
	private ArrayList<IncrementalMagFreqDist> aFaultMFDsList, bFaultCharMFDsList, bFaultGRMFDsList, totMFDsList;
	private ArrayList<String> paramNames;
	private ArrayList<ParamOptions> paramValues;
	private int lastParamIndex;
	private int mfdIndex;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2); // A-Faults
	private final PlotCurveCharacterstics PLOT_CHAR1_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.BLUE, 2); // A-Faults
	private final PlotCurveCharacterstics PLOT_CHAR1_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.BLUE, 2); // A-Faults
	private final PlotCurveCharacterstics PLOT_CHAR1_3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOTTED_LINE,
		      Color.BLUE, 2); // A-Faults
	
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.DARK_GRAY, 2); // B-Faults Char
	private final PlotCurveCharacterstics PLOT_CHAR2_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.DARK_GRAY, 2); // B-Faults Char
	private final PlotCurveCharacterstics PLOT_CHAR2_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.DARK_GRAY, 2); // B-Faults Char
	private final PlotCurveCharacterstics PLOT_CHAR12_3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOTTED_LINE,
		      Color.DARK_GRAY, 2); // B-Faults Char
	
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2); // B-Faults GR
	private final PlotCurveCharacterstics PLOT_CHAR3_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.GREEN, 2); // B-Faults GR
	private final PlotCurveCharacterstics PLOT_CHAR3_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.GREEN, 2); // B-Faults GR
	private final PlotCurveCharacterstics PLOT_CHAR3_3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOTTED_LINE,
		      Color.GREEN, 2); // B-Faults GR
	
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 2); // Tot MFD
	private final PlotCurveCharacterstics PLOT_CHAR4_1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOT_DASH_LINE,
		      Color.BLACK, 2); // Tot MFD
	private final PlotCurveCharacterstics PLOT_CHAR4_2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		      Color.BLACK, 2); // Tot MFD
	private final PlotCurveCharacterstics PLOT_CHAR4_3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DOTTED_LINE,
		      Color.BLACK, 2); // Tot MFD
	
	/**
	 * 
	 * @param paramNames 
	 * @param paramValues
	 */
	public LogicTreeMFDsPlotter () {
		fillAdjustableParams();
		lastParamIndex = paramNames.size()-1;
		aFaultMFDsList = new ArrayList<IncrementalMagFreqDist>();
		bFaultCharMFDsList = new ArrayList<IncrementalMagFreqDist>();
		bFaultGRMFDsList = new ArrayList<IncrementalMagFreqDist>();
		totMFDsList = new ArrayList<IncrementalMagFreqDist>();
		calcMFDs(0);
	}
	
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParams() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		
		// Deformation model
		paramNames.add(EqkRateModel2_ERF.DEFORMATION_MODEL_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight("D2.1", 0.5);
		options.addValueWeight("D2.4", 0.5);
		paramValues.add(options);
		
		// Mag Area Rel
		paramNames.add(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		paramValues.add(options);
		
		// A-Fault solution type
		paramNames.add(EqkRateModel2_ERF.RUP_MODEL_TYPE_NAME);
		options = new ParamOptions();
		options.addValueWeight(EqkRateModel2_ERF.SEGMENTED_A_FAULT_MODEL, 0.9);
		options.addValueWeight(EqkRateModel2_ERF.UNSEGMENTED_A_FAULT_MODEL, 0.1);
		paramValues.add(options);
		
		// Aprioti wt param
		paramNames.add(EqkRateModel2_ERF.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e7), 0.5);
		paramValues.add(options);
		
		// Mag Correction
		paramNames.add(EqkRateModel2_ERF.MEAN_MAG_CORRECTION);
		options = new ParamOptions();
		options.addValueWeight(new Double(-0.1), 0.2);
		options.addValueWeight(new Double(0), 0.6);
		options.addValueWeight(new Double(0.1), 0.2);
		paramValues.add(options);
		
		//	Connect More B-Faults?
		paramNames.add(EqkRateModel2_ERF.CONNECT_B_FAULTS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(true), 0.5);
		options.addValueWeight(new Boolean(false), 0.5);
		paramValues.add(options);
	}
	
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		 return null;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return null;
	}
	
	
	/**
	 * Calculate MFDs
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void calcMFDs(int paramIndex) {
		
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			if(eqkRateModel2ERF.getAdjustableParameterList().containsParameter(paramName))
				eqkRateModel2ERF.getParameter(paramName).setValue(options.getValue(i));
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, save the MFDs
				System.out.println("Doing run:"+(aFaultMFDsList.size()+1));
				eqkRateModel2ERF.updateForecast();
				aFaultMFDsList.add(eqkRateModel2ERF.getTotal_A_FaultsMFD());
				bFaultCharMFDsList.add(eqkRateModel2ERF.getTotal_B_FaultsCharMFD());
				bFaultGRMFDsList.add(eqkRateModel2ERF.getTotal_B_FaultsGR_MFD());
				totMFDsList.add(eqkRateModel2ERF.getTotalMFD());
			} else { // recurrsion 
				calcMFDs(paramIndex+1);
			}
		}
	}
	
	/**
	 * It returns 3 MFD: First is A-Fault MFD, Second is B-Fault char MFD, Third is B-Fault GR MFD and last is TotalMFD
	 * 
	 * @param paramName Param Name whose value needs to remain constant. Can be null 
	 * @param value Param Value for constant paramter. can be null
	 * 
	 * @return
	 */
	public ArrayList<IncrementalMagFreqDist> getMFDs(String paramName, Object value) {
		SummedMagFreqDist aFaultMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG,EqkRateModel2_ERF. NUM_MAG);
		SummedMagFreqDist bFaultCharMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG,EqkRateModel2_ERF. NUM_MAG);
		SummedMagFreqDist bFaultGRMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG,EqkRateModel2_ERF. NUM_MAG);
		SummedMagFreqDist totMFD = new SummedMagFreqDist(EqkRateModel2_ERF.MIN_MAG, EqkRateModel2_ERF.MAX_MAG,EqkRateModel2_ERF. NUM_MAG);
		
		mfdIndex = 0;
		doWeightedSum(0, paramName, value, 1.0, aFaultMFD, bFaultCharMFD, bFaultGRMFD, totMFD);
		
		ArrayList<IncrementalMagFreqDist> mfdsList = new ArrayList<IncrementalMagFreqDist>();
		mfdsList.add(aFaultMFD);
		mfdsList.add(bFaultCharMFD);
		mfdsList.add(totMFD);
		return mfdsList;
	 }
	
		
	/**
	 * Do Weighted Sum
	 * 
	 * @param paramIndex
	 * @param paramName
	 * @param value
	 * @param weight
	 * @param aFaultMFD
	 * @param bFaultMFD
	 * @param totMFD
	 */
	private void doWeightedSum( int paramIndex, String constantParamName, Object value, double weight, 
			SummedMagFreqDist aFaultTotMFD, SummedMagFreqDist bFaultTotCharMFD, SummedMagFreqDist bFaultTotGRMFD, SummedMagFreqDist totMFD) {
		
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		double newWt;
		for(int i=0; i<numValues; ++i) {
			if(paramName !=null && paramName.equalsIgnoreCase(constantParamName)) {
				if(options.getValue(i).equals(value)) newWt = 1*weight; // for constant paramter
				else newWt = 0*weight;
			} else newWt = weight * options.getWeight(i);
			
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, add to the MFDs
				if(newWt!=0) {
					addMFDs(aFaultTotMFD, aFaultMFDsList.get(mfdIndex), newWt);
					addMFDs(bFaultTotCharMFD, bFaultCharMFDsList.get(mfdIndex), newWt);
					addMFDs(bFaultTotGRMFD, bFaultGRMFDsList.get(mfdIndex), newWt);
					addMFDs(totMFD, totMFDsList.get(mfdIndex), newWt);
				}
				++mfdIndex;
			} else { // recursion 
				doWeightedSum(paramIndex+1, constantParamName,  value, newWt, 
						 aFaultTotMFD,  bFaultTotCharMFD,  bFaultTotGRMFD, totMFD);
			}
		}
	}
	
	/**
	 * Add source MFD to Target MFD after applying the  weight
	 * @param targetMFD
	 * @param sourceMFD
	 * @param wt
	 */
	private void addMFDs(SummedMagFreqDist targetMFD, IncrementalMagFreqDist sourceMFD, double wt) {
		for(int i=0; i<sourceMFD.getNum(); ++i) {
			targetMFD.add(sourceMFD.getX(i), wt*sourceMFD.getY(i));
		}
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
	
	
	public static void main(String []args) {
		LogicTreeMFDsPlotter mfdPlotter = new LogicTreeMFDsPlotter();
		System.out.println(mfdPlotter.getMFDs(null, null).get(3).toString());
	}
}


/**
 * Various parameter values and their corresponding weights
 * 
 * @author vipingupta
 *
 */
class ParamOptions {
	private ArrayList values = new ArrayList();
	private ArrayList<Double> weights = new ArrayList<Double>();
	
	/**
	 * Add a value and weight for this parameter 
	 * @param value
	 * @param weight
	 */
	public void addValueWeight(Object value, double weight) {
		values.add(value);
		weights.add(weight);
	}
	
	/**
	 * Number of different options for this parameter
	 * @return
	 */
	public int getNumValues() {
		return values.size();
	}
	
	/**
	 * Get the value at specified index
	 * 
	 * @param index
	 * @return
	 */
	public Object getValue(int index) {
		return values.get(index);
	}
	
	/**
	 * Get the weight at specified index
	 * 
	 * @param index
	 * @return
	 */
	public double getWeight(int index) {
		return weights.get(index);
	}
	
}
