/**
 * 
 */
package scratch.ned.URS;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.ValueWeight;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkSourceAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.EmpiricalModel;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.A_Faults.A_FaultSegmentedSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.analysis.ParamOptions;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.A_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.NonCA_FaultsFetcher;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelSummaryFinal;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.earthquake.util.EqkSourceNameComparator;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;



/**
 * This was checked to make sure this is equal to the mean of what is returned from the 
 * UCERF2 Epistemic List.  
 * 
 * 
 * @author 
 *
 */
public class URS_MeanUCERF2 extends MeanUCERF2 {
	//for Debug purposes
	private static String  C = new String("MeanUCERF2");
	private boolean D = true;

	// name of this ERF
	public final static String NAME = new String("URS Modified UCERF2");
	
	// for Cybershake Correction
	public final static String FILTER_PARAM_NAME ="URS Filter";
	public final static Boolean FILTER_PARAM_DEFAULT= new Boolean(false);
	protected final static String FILTER_PARAM_INFO = "Apply the URS Source Filter";
	protected BooleanParameter filterParam;



	/**
	 *
	 * No argument constructor
	 */
	public URS_MeanUCERF2() {
		super();
		
		filterParam = new BooleanParameter(FILTER_PARAM_NAME);
		filterParam.setInfo(FILTER_PARAM_INFO);
		filterParam.setDefaultValue(FILTER_PARAM_DEFAULT);
		adjustableParams.addParameter(filterParam);
		filterParam.addParameterChangeListener(this);
	}
	
	/**
	 * update the forecast
	 **/

	public void updateForecast() {
		super.updateForecast();
		
		// FILTER OUT DESIRED SOURCES
		if(filterParam.getValue().booleanValue()) {
			ArrayList<ProbEqkSource> newAllSources = new ArrayList<ProbEqkSource>();
			String srcName;
			for(int i=0;i<allSources.size();i++) {
				srcName = allSources.get(i).getName();
//				System.out.println(srcName);
				if (!srcName.equals("Sierra Madre") &&
					!srcName.equals("Sierra Madre (San Fernando)") &&
					!srcName.equals("Sierra Madre Connected") &&
					!srcName.equals("Santa Susana, alt 1") &&
					!srcName.equals("Verdugo")){
					
					newAllSources.add(allSources.get(i));
				}
				else {
					System.out.println(srcName+" was filetered");
				}			
			}
			
			// OVERIDE THE SOURCE LIST
			allSources = newAllSources;			
		}
	}
	
	public void parameterChange(ParameterChangeEvent event) {
		super.parameterChange(event);
		if(!adjustableParams.containsParameter(filterParam.getName()))
			adjustableParams.addParameter(filterParam);
	}


	
	/**
	 * Return the name for this class
	 *
	 * @return : return the name for this class
	 */
	public String getName(){
		return NAME;
	}



	// this is temporary for testing purposes
	public static void main(String[] args) {
		URS_MeanUCERF2 meanUCERF2 = new URS_MeanUCERF2();
		meanUCERF2.calcSummedMFDs  =false;
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2.updateForecast();
	}
}