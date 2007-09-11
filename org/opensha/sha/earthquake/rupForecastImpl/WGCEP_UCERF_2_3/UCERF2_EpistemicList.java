package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3;


import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.param.Parameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.sha.earthquake.ERF_EpistemicList;
import org.opensha.sha.earthquake.EqkRupForecastAPI;

/**
 * It creates UCERF2 Epistemic List for Time Independent model
 * 
 * @author vipingupta
 *
 */
public class UCERF2_EpistemicList extends ERF_EpistemicList {
	public static final String  NAME = new String("UCERF2 ERF Epistemic List");
	private ArrayList<Double> weights = null;
	private ArrayList<ParameterList> paramList = null;
	private UCERF2 ucerf2 = new UCERF2();
	private ArrayList<String> paramNames; // parameters that are adjusted for logic tree
	private ArrayList<ParamOptions> paramValues; // paramter values and their weights
	private int lastParamIndex;
	
	public UCERF2_EpistemicList() {
		fillAdjustableParams(); // fill the paramters that will be adjusted for the logic tree
		lastParamIndex = paramNames.size()-1;
		weights = new ArrayList<Double>();
		paramList = new ArrayList<ParameterList>();
		findBranches(0, 1);
	}
	
	
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParams() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		
		// Deformation model
		paramNames.add(UCERF2.DEFORMATION_MODEL_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight("D2.1", 0.25);
		options.addValueWeight("D2.2", 0.1);
		options.addValueWeight("D2.3", 0.15);
		options.addValueWeight("D2.4", 0.25);
		options.addValueWeight("D2.5", 0.1);
		options.addValueWeight("D2.6", 0.15);
		paramValues.add(options);
		
		// Mag Area Rel
		paramNames.add(UCERF2.MAG_AREA_RELS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		paramValues.add(options);
		
		// A-Fault solution type
		paramNames.add(UCERF2.RUP_MODEL_TYPE_NAME);
		options = new ParamOptions();
		options.addValueWeight(UCERF2.SEGMENTED_A_FAULT_MODEL, 0.9);
		options.addValueWeight(UCERF2.UNSEGMENTED_A_FAULT_MODEL, 0.1);
		paramValues.add(options);
		
		// Aprioti wt param
		paramNames.add(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e10), 0.5);
		paramValues.add(options);
		
		//	Connect More B-Faults?
		paramNames.add(UCERF2.CONNECT_B_FAULTS_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(true), 0.5);
		options.addValueWeight(new Boolean(false), 0.5);
		paramValues.add(options);
		
		// B-Fault bValue=0
		/*paramNames.add(UCERF2.B_FAULTS_B_VAL_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(0.8), 0.5);
		options.addValueWeight(new Double(0.0), 0.5);
		paramValues.add(options);*/
	}
	
	/**
	 * Calculate MFDs
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void findBranches(int paramIndex, double weight) {
		ParamOptions options = paramValues.get(paramIndex);
		String paramName = paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			double newWt;
			if(ucerf2.getAdjustableParameterList().containsParameter(paramName)) {
				ucerf2.getParameter(paramName).setValue(options.getValue(i));	
				newWt = weight * options.getWeight(i);
				if(paramName.equalsIgnoreCase(UCERF2.REL_A_PRIORI_WT_PARAM_NAME)) {
					ParameterAPI param = ucerf2.getParameter(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
					if(((Double)param.getValue()).doubleValue()==1e10) {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(new Double(0.0));
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(new Double(0.0));	
					} else {
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_1_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_1_DEFAULT);
						ucerf2.getParameter(UCERF2.MIN_A_FAULT_RATE_2_PARAM_NAME).setValue(UCERF2.MIN_A_FAULT_RATE_2_DEFAULT);	
					}
				}
			} else {
				if(i==0) newWt=weight;
				else return;
			}
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, save the MFDs
				paramList.add((ParameterList)ucerf2.getAdjustableParameterList().clone());
				weights.add(newWt);
			} else { // recursion 
				findBranches(paramIndex+1, newWt);
			}
		}
	}
	
	
	/**
	 * Return the name for this class
	 *
	 * @return : return the name for this class
	 */
	public String getName(){
		return NAME;
	}


	/**
	 * get the number of Eqk Rup Forecasts in this list
	 * @return : number of eqk rup forecasts in this list
	 */
	public int getNumERFs() {
		return this.weights.size();
	}


	/**
	 * Get the ERF in the list with the specified index. 
	 * Index can range from 0 to getNumERFs-1
	 * 
	 * 
	 * @param index : index of Eqk rup forecast to return
	 * @return
	 */
	public EqkRupForecastAPI getERF(int index) {
		Iterator it = paramList.get(index).getParametersIterator();
		while(it.hasNext()) {
			Parameter param = (Parameter)it.next();
			ucerf2.getParameter(param.getName()).setValue(param.getValue());
		}
		return ucerf2;
	}
	
	/**
	 * Get the ParameterList for ERF at the specified index
	 * 
	 * @param index
	 * @return
	 */
	public ParameterList getParameterList(int index) {
		return paramList.get(index);
	}

	/**
	 * get the weight of the ERF at the specified index
	 * @param index : index of ERF
	 * @return : relative weight of ERF
	 */
	public double getERF_RelativeWeight(int index) {
		return this.weights.get(index);
	}

	/**
	 * Return the Arraylist containing the Double values with
	 * relative weights for each ERF
	 * @return : ArrayList of Double values
	 */
	public ArrayList<Double> getRelativeWeightsList() {
		return weights;
	}
	
	
	public static void main(String[] args) {
		UCERF2_EpistemicList_old ucerf2EpistemicList = new UCERF2_EpistemicList_old();
		int numERFs = ucerf2EpistemicList.getNumERFs();
		System.out.println("Num Branches="+numERFs);
		for(int i=0; i<numERFs; ++i) {
			System.out.println("Weight of Branch "+i+"="+ucerf2EpistemicList.getERF_RelativeWeight(i));
			System.out.println("Parameters of Branch "+i+":");
			System.out.println(ucerf2EpistemicList.getERF(i).getAdjustableParameterList().getParameterListMetadataString("\n"));
			
		}
		
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
