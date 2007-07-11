package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2.UCERF2;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This class writes Ruptures probabilities and gains into an excel sheet. It loops over logic tree branches and writes prob and gains for each branch
 * 
 * @author vipingupta
 *
 */
public class WriteTimeDepRupProbAndGain {
	private ArrayList<String> paramNames;
	private ArrayList<ParamOptions> paramValues;
	private int lastParamIndex;
	private int mfdIndex;
	
	/**
	 *  it just reads the data from the file wihtout recalculation
	 * 
	 */
	public WriteTimeDepRupProbAndGain () {
		fillAdjustableParams();
		lastParamIndex = paramNames.size()-1;
	}
		
	/**
	 * Paramters that are adjusted in the runs
	 *
	 */
	private void fillAdjustableParams() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		
		
		// Mag Area Rel
		paramNames.add(UCERF2.MAG_AREA_RELS_PARAM_NAME);
		ParamOptions options = new ParamOptions();
		options.addValueWeight(Ellsworth_B_WG02_MagAreaRel.NAME, 0.5);
		options.addValueWeight(HanksBakun2002_MagAreaRel.NAME, 0.5);
		paramValues.add(options);
		
		// Aprioti wt param
		paramNames.add(UCERF2.REL_A_PRIORI_WT_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Double(1e-4), 0.5);
		options.addValueWeight(new Double(1e10), 0.5);
		paramValues.add(options);
		
		// Mag Correction
		paramNames.add(UCERF2.MEAN_MAG_CORRECTION);
		options = new ParamOptions();
		options.addValueWeight(new Double(-0.1), 0.2);
		options.addValueWeight(new Double(0), 0.6);
		options.addValueWeight(new Double(0.1), 0.2);
		paramValues.add(options);
		
		// Aperiodicity
		paramNames.add(UCERF2.SEG_DEP_APERIODICITY_PARAM_NAME);
		options = new ParamOptions();
		options.addValueWeight(new Boolean(false), 0.5);
		options.addValueWeight(new Boolean(true), 0.5);
		paramValues.add(options);
	
	}
	
	
	public static void main(String []args) {
		WriteTimeDepRupProbAndGain rupProbWriter = new WriteTimeDepRupProbAndGain();
	}
}
