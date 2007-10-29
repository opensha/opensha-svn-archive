/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.data.TimeSpan;
import org.opensha.data.estimate.MinMaxPrefEstimate;

import org.opensha.param.DoubleParameter;
import org.opensha.param.IntegerParameter;
import org.opensha.param.ParameterList;
import org.opensha.param.estimate.EstimateConstraint;
import org.opensha.param.estimate.EstimateParameter;
import org.opensha.sha.earthquake.ERF_EpistemicList;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis.ParamOptions;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.param.MagFreqDistParameter;
import org.opensha.util.FileUtils;

/**
 * Yucca Mountain ERF List that iterates over all logic tree branches
 * 
 * @author vipingupta
 *
 */
public class YuccaMountainERF_List  extends ERF_EpistemicList{
	public static final String  NAME = new String("Yucca Mountain ERF Epistemic List");
	protected YuccaMountainERF yuccaMountainERF = new YuccaMountainERF();
	private final static double DURATION_DEFAULT = 30;


	private final static double DELTA_MAG = 0.1;

	private String FAULT_LOGIC_TREE_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/FaultsLogicTree.txt";
	private String BACKGROUND_LOGIC_TREE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/BackgroundLogicTree.txt";

	private final static String MAG="Mag-";
	private final static String MOMENT_RATE="MomentRate-";
	private final static String BACKGROUND = "Background";

	// This hashmap saves Background MFD corresponding to each CumRate
	private HashMap<Double, GutenbergRichterMagFreqDist> backgroundOptions;

	//	 For num realizations parameter
	private final static String NUM_REALIZATIONS_PARAM_NAME ="Num Realizations";
	private Integer DEFAULT_NUM_REALIZATIONS_VAL= new Integer(1000);
	private int NUM_REALIZATIONS_MIN = 1;
	private int NUM_REALIZATIONS_MAX = 10000;
	private final static String NUM_REALIZATIONS_PARAM_INFO = "Number of Monte Carlo ERF realizations";
	IntegerParameter numRealizationsParam;

	private EstimateConstraint minMaxEstimateConstraint;


	public YuccaMountainERF_List() {

		// number of Monte carlo realizations
		numRealizationsParam = new IntegerParameter(NUM_REALIZATIONS_PARAM_NAME,NUM_REALIZATIONS_MIN,
				NUM_REALIZATIONS_MAX, DEFAULT_NUM_REALIZATIONS_VAL);
		numRealizationsParam.setInfo(NUM_REALIZATIONS_PARAM_INFO);
		adjustableParams.addParameter(numRealizationsParam);

		// constraint that only allows Min/Max/Pref Estimate
		ArrayList<String> allowedEstimates = new ArrayList<String>();
		allowedEstimates.add(MinMaxPrefEstimate.NAME);
		minMaxEstimateConstraint = new EstimateConstraint(allowedEstimates);

		fillFaultsLogicTree();

		addBackgroundBranches();
		// Backgroud is MagFreqDistParameter
		//ArrayList<String> allowedMagDists = new ArrayList<String>();
		//allowedMagDists.add(GutenbergRichterMagFreqDist.NAME);
		//paramList.addParameter(new MagFreqDistParameter(BACKGROUND, allowedMagDists));

		// create the time-ind timespan object with start time and duration in years
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(DURATION_DEFAULT);
	}




	/**
	 * Fill the logic tree branches in faults
	 *
	 */
	private void fillFaultsLogicTree() {
		try {
			ArrayList<String> faultBranchesLines = FileUtils.loadFile(FAULT_LOGIC_TREE_FILENAME);

			for(int i=11; i<faultBranchesLines.size(); ) {
				// get the source name
				String sourceCodeNameLine = faultBranchesLines.get(i);
				System.out.println(sourceCodeNameLine);
				StringTokenizer st = new StringTokenizer(sourceCodeNameLine);
				String srcCode = st.nextToken();		
				++i;
				++i;
				String paramName = MAG+srcCode;
				MinMaxPrefEstimate magEstimate = getMinMaxPrefEstimate(faultBranchesLines, i);
				EstimateParameter magParam = new EstimateParameter(paramName,  minMaxEstimateConstraint, null, magEstimate);
				this.adjustableParams.addParameter(magParam);

				i+=4;
				paramName = MOMENT_RATE+srcCode;
				MinMaxPrefEstimate moRateEstimate = getMinMaxPrefEstimate(faultBranchesLines, i);
				EstimateParameter moRateParam = new EstimateParameter(paramName,  minMaxEstimateConstraint, null, moRateEstimate);
				this.adjustableParams.addParameter(moRateParam);
				i+=3;
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get Min/Max/Pref Estimate based on lines in files
	 * 
	 * @param faultBranchesLines
	 * @param lineIndex
	 * @return
	 */
	private MinMaxPrefEstimate getMinMaxPrefEstimate(ArrayList<String> faultBranchesLines, 
			int lineIndex) {
		StringTokenizer tokenizer = new StringTokenizer(faultBranchesLines.get(lineIndex));
		double prefMag = Double.parseDouble(tokenizer.nextToken());
		double prefWt = Double.parseDouble(tokenizer.nextToken());
		tokenizer = new StringTokenizer(faultBranchesLines.get(++lineIndex));
		double minMag = Double.parseDouble(tokenizer.nextToken());
		double minWt = Double.parseDouble(tokenizer.nextToken());
		tokenizer = new StringTokenizer(faultBranchesLines.get(++lineIndex));
		double maxMag = Double.parseDouble(tokenizer.nextToken());
		double maxWt = Double.parseDouble(tokenizer.nextToken());

		prefWt+=minWt;
		maxWt+=prefWt;

		return new MinMaxPrefEstimate(minMag, maxMag, prefMag, minWt, maxWt, prefWt);
	}


	/**
	 * Add Background logic tree branches
	 *
	 */
	private void addBackgroundBranches() {
		backgroundOptions = new HashMap<Double, GutenbergRichterMagFreqDist>();
		try {
			ArrayList<String> backgroundLines = FileUtils.loadFile(BACKGROUND_LOGIC_TREE_FILE_NAME);
			int index=0;
			double prefCumRate=0, minCumRate=0, maxCumRate=0, prefWt=0, minWt=0, maxWt=0;
			for(int i=6; i<backgroundLines.size(); ++i) {
				String line = backgroundLines.get(i);
				StringTokenizer tokenizer = new StringTokenizer(line);
				double cumRate = Double.parseDouble(tokenizer.nextToken());
				double bValue = Double.parseDouble(tokenizer.nextToken());
				double minMag = Double.parseDouble(tokenizer.nextToken());
				double maxMag = Double.parseDouble(tokenizer.nextToken());
				double weight = Double.parseDouble(tokenizer.nextToken());	
				int numMag =  (int)Math.round((maxMag-minMag)/DELTA_MAG)+1;
				GutenbergRichterMagFreqDist grMFD = 
					new GutenbergRichterMagFreqDist(bValue, cumRate, minMag, maxMag, numMag);
				backgroundOptions.put(cumRate, grMFD);
				if(index == 0) {
					prefCumRate = cumRate;
					prefWt = weight;
				} else if(index==1) {
					minCumRate = cumRate;
					minWt = weight;
				} else if(index==2) {
					maxCumRate = cumRate;
					maxWt = weight;
				}
				++index;
			}

			prefWt+=minWt;
			maxWt+=prefWt;

			MinMaxPrefEstimate backgroundEst = new MinMaxPrefEstimate(minCumRate, maxCumRate, prefCumRate, minWt, maxWt, prefWt);
			EstimateParameter backgroundParam = new EstimateParameter(BACKGROUND, minMaxEstimateConstraint, null, backgroundEst);
			this.adjustableParams.addParameter(backgroundParam);
		}catch(Exception e) {
			e.printStackTrace();
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
		return (Integer)numRealizationsParam.getValue();
	}


	/**
	 * Get the ERF in the list with the specified index. 
	 * It returns the updated forecast
	 * Index can range from 0 to getNumERFs-1
	 * 
	 * 
	 * @param index : index of Eqk rup forecast to return
	 * @return
	 */
	public EqkRupForecastAPI getERF(int index) {
		Iterator<String> it = this.adjustableParams.getParameterNamesIterator();
		while(it.hasNext()) {
			String paramName = it.next();
			Object value = adjustableParams.getValue(paramName);
			// Background
			if(paramName.equalsIgnoreCase(BACKGROUND)) {
				yuccaMountainERF.setBackgroundMFD((GutenbergRichterMagFreqDist)value);
			}
			// Mag
			if(paramName.startsWith(MAG)) {
				String faultName = paramName.substring(MAG.length());
				yuccaMountainERF.setMeanMagForSource(faultName, (Double)value);
			}
			// Moment Rate
			if(paramName.startsWith(MOMENT_RATE)) {
				String faultName = paramName.substring(MOMENT_RATE.length());
				yuccaMountainERF.setMomentRateForSource(faultName, (Double)value);
			}
		}
		yuccaMountainERF.getTimeSpan().setDuration(this.timeSpan.getDuration());
		yuccaMountainERF.updateForecast();
		return yuccaMountainERF;
	}


	/**
	 * get the weight of the ERF at the specified index. 
	 * It always returns 1 because we are doing Monte Carlo simulations
	 * 
	 * @param index : index of ERF
	 * @return : relative weight of ERF
	 */
	public double getERF_RelativeWeight(int index) {
		return 1;
	}



	public static void main(String[] args) {
		YuccaMountainERF_List ymEpistemicList = new YuccaMountainERF_List();
		int numERFs = ymEpistemicList.getNumERFs();
		System.out.println("Num Branches="+numERFs);
		for(int i=0; i<numERFs; ++i) {
			System.out.println("Weight of Branch "+i+"="+ymEpistemicList.getERF_RelativeWeight(i));
		}

	}
}
