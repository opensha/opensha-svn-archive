/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;


import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterListParameter;
import org.opensha.param.StringConstraint;
import org.opensha.param.StringParameter;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.data.A_FaultsFetcher;

/**
 * Generate the test excel sheets
 * @author vipingupta
 *
 */
public class GenPredErrAnalysisTool {
	private EqkRateModel2_ERF eqkRateModelERF;
	private ParameterAPI magAreaRelParam, slipModelParam;
	private ParameterListParameter segmentedRupModelParam;
	private ParameterList adjustableParams;
	private ArrayList aFaultSourceGenerators ;
	private A_FaultsFetcher aFaultsFetcher;
	private ArrayList magAreaOptions, slipModelOptions;
	
	
	public GenPredErrAnalysisTool(EqkRateModel2_ERF eqkRateModelERF) {
		this.eqkRateModelERF = eqkRateModelERF;
		adjustableParams = eqkRateModelERF.getAdjustableParameterList();
		magAreaRelParam = eqkRateModelERF.getParameter(EqkRateModel2_ERF.MAG_AREA_RELS_PARAM_NAME);
		segmentedRupModelParam = (ParameterListParameter)eqkRateModelERF.getParameter(EqkRateModel2_ERF.SEGMENTED_RUP_MODEL_TYPE_NAME);
		slipModelParam = eqkRateModelERF.getParameter(EqkRateModel2_ERF.SLIP_MODEL_TYPE_NAME);
		aFaultsFetcher = eqkRateModelERF.getA_FaultsFetcher();
		magAreaOptions = ((StringConstraint)magAreaRelParam.getConstraint()).getAllowedStrings();
		slipModelOptions = ((StringConstraint)slipModelParam.getConstraint()).getAllowedStrings();
		
	}
	
	
	/**
	 * Generate Excel sheet for each fault.
	 * Each sheet will have all Rup solution Types
	 * 
	 */
	public void writeResults(String outFileName) {	
		try { 
			FileWriter fw = new FileWriter(outFileName);

			DecimalFormat formatter = new DecimalFormat("0.000E0");
			String[] models = {"Geological Insight", "Min Rate", "Max Rate"};
			for(int irup=0; irup<3;irup++) {

				Iterator it = this.segmentedRupModelParam.getParametersIterator();
				while(it.hasNext()) { // set the specfiied rup model in each A fault
					StringParameter param = (StringParameter)it.next();
					ArrayList<String> allowedVals = param.getAllowedStrings();
					param.setValue(allowedVals.get(irup));
				}

				for(int imag=0; imag<magAreaOptions.size();imag++) {
					//int numSlipModels = slipModelOptions.size();
					//double magRate[][] = new double[numSlipModels][2];
					for(int islip=0; islip<slipModelOptions.size();islip++) {

						magAreaRelParam.setValue(magAreaOptions.get(imag));
						slipModelParam.setValue(slipModelOptions.get(islip));
						fw.write(magAreaRelParam.getValue()+"\t"+slipModelParam.getValue()+"\t"+models[irup]+"\n");
						double aPrioriWt = 0;
						this.eqkRateModelERF.setParameter(eqkRateModelERF.REL_A_PRIORI_WT_PARAM_NAME,new Double(aPrioriWt));
						eqkRateModelERF.updateForecast();
						// do the 0.0 case
						aFaultSourceGenerators = eqkRateModelERF.get_A_FaultSourceGenerators();
						fw.write("\t");
						for(int i=0; i<aFaultSourceGenerators.size(); ++i) {
							A_FaultSegmentedSourceGenerator source = (A_FaultSegmentedSourceGenerator)aFaultSourceGenerators.get(i);
							fw.write(source.getFaultSegmentData().getFaultName()+"\t");
						}

						// do for each fault
						fw.write("\n"+aPrioriWt+"\t");
						for(int i=0; i<aFaultSourceGenerators.size(); ++i) {
							A_FaultSegmentedSourceGenerator source = (A_FaultSegmentedSourceGenerator)aFaultSourceGenerators.get(i);
							fw.write(formatter.format(source.getGeneralizedPredictionError())+"\t\t");
						}	 
						fw.write("\n");

						for(int pow=-20; pow<16;pow++) {
							aPrioriWt = Math.pow(10,pow);
							fw.write("1E"+pow+"\t");
							eqkRateModelERF.setParameter(eqkRateModelERF.REL_A_PRIORI_WT_PARAM_NAME,new Double(aPrioriWt));
							eqkRateModelERF.updateForecast();
							aFaultSourceGenerators = eqkRateModelERF.get_A_FaultSourceGenerators();
							// do for each fault
							for(int i=0; i<aFaultSourceGenerators.size(); ++i) {
								A_FaultSegmentedSourceGenerator source = (A_FaultSegmentedSourceGenerator)aFaultSourceGenerators.get(i);
								fw.write(formatter.format(source.getGeneralizedPredictionError())+"\t\t");
							}	 
							fw.write("\n");
							/*System.out.println("1E"+pow+"\t"+
								formatter.format(getGeneralPredErr())+"\t"+
								formatter.format(getModSlipRateError())+"\t"+
								formatter.format(getDataER_Err())+"\t"+
								formatter.format(getNormalizedA_PrioriRateErr())+"  ("+
								formatter.format(getNonNormalizedA_PrioriRateErr())+
								")");*/
						}
					}

				}
			}
			fw.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String args[]) {
		EqkRateModel2_ERF erRateModel2_ERF = new EqkRateModel2_ERF();
		GenPredErrAnalysisTool analysisTool = new GenPredErrAnalysisTool(erRateModel2_ERF);
		analysisTool.writeResults("PredAnalysisResults.txt");
	}
	
}


