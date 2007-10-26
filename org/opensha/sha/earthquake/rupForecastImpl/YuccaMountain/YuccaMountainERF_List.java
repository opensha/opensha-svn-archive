/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.data.TimeSpan;

import org.opensha.param.DoubleParameter;
import org.opensha.param.ParameterList;
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
	private ArrayList<Double> weights = null;
	protected YuccaMountainERF yuccaMountainERF = new YuccaMountainERF();
	private final static double DURATION_DEFAULT = 30;
	
	private final static int NUM_MAGS=3;
	private final static int NUM_MO_RATES=3;
	private final static double DELTA_MAG = 0.1;
	
	private String FAULT_LOGIC_TREE_FILENAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/FaultsLogicTree.txt";
	private String BACKGROUND_LOGIC_TREE_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/YuccaMountain/BackgroundLogicTree.txt";

	private ArrayList<String> paramNames; // parameters that are adjusted for logic tree
	private ArrayList<ParamOptions> paramValues; // paramter values and their weights

	private final static String MAG="Mag-";
	private final static String MOMENT_RATE="MomentRate-";
	private final static String BACKGROUND = "Background";
	private int lastParamIndex;
	private ArrayList<ParameterList> logicTreeParamList;
	private ParameterList paramList;
	
	
	public YuccaMountainERF_List() {
		paramList = new ParameterList();
		
		fillFaultsLogicTree();
		
		// make parameter List. All parameters are double parameters except background
		for(int i=0; i<paramNames.size(); ++i)
			paramList.addParameter(new DoubleParameter(paramNames.get(i)));
		
		addBackgroundBranches();
		// Backgroud is MagFreqDistParameter
		paramList.addParameter(new MagFreqDistParameter(BACKGROUND));
		
		
		lastParamIndex = paramNames.size()-1;
		weights = new ArrayList<Double>();
		// create the time-ind timespan object with start time and duration in years
		timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(DURATION_DEFAULT);
		timeSpan.addParameterChangeListener(this);
		logicTreeParamList = new ArrayList<ParameterList>();
		fillParamsForEachBranch(0, 1);
	}
	
	
	/**
	 * Fill parameters for each branch
	 * 
	 * @param paramIndex
	 * @param weight
	 */
	private void fillParamsForEachBranch(int paramIndex, double weight) {
		ParamOptions options = this.paramValues.get(paramIndex);
		String paramName = this.paramNames.get(paramIndex);
		int numValues = options.getNumValues();
		for(int i=0; i<numValues; ++i) {
			double newWt =  weight * options.getWeight(i);
			paramList.getParameter(paramName).setValue(options.getValue(i));	
			if(paramIndex==lastParamIndex) { // if it is last paramter in list, save paramList
				logicTreeParamList.add((ParameterList)paramList.clone());
				weights.add(newWt);
			} else { // recursion 
				fillParamsForEachBranch(paramIndex+1, newWt);
			}
		}
	}
	
	/**
	 * Fill the logic tree branches in faults
	 *
	 */
	private void fillFaultsLogicTree() {
		this.paramNames = new ArrayList<String>();
		this.paramValues = new ArrayList<ParamOptions>();
		try {
			ArrayList<String> faultBranchesLines = FileUtils.loadFile(FAULT_LOGIC_TREE_FILENAME);
			
			for(int i=11; i<faultBranchesLines.size(); ) {
				// get the source name
				String sourceCodeNameLine = faultBranchesLines.get(i);
				System.out.println(sourceCodeNameLine);
				StringTokenizer st = new StringTokenizer(sourceCodeNameLine);
				String srcCode = st.nextToken();
				int srcCodeLength = srcCode.length();
				String sourceName = sourceCodeNameLine.substring(srcCodeLength).trim();
				paramNames.add(MAG+sourceName);
				++i;
				++i;
				
				// read the mag values and weights
				ParamOptions magOptions = new ParamOptions();
				for(int magIndex=0; magIndex<this.NUM_MAGS; ++magIndex) {
					StringTokenizer tokenizer = new StringTokenizer(faultBranchesLines.get(i));
					double mag = Double.parseDouble(tokenizer.nextToken());
					double weight = Double.parseDouble(tokenizer.nextToken());
					magOptions.addValueWeight(mag, weight);
					++i;
				}
				paramValues.add(magOptions);
				++i;
				paramNames.add(MOMENT_RATE+sourceName);
				// read the moRate values and weights
				ParamOptions moRateOptions = new ParamOptions();
				for(int magIndex=0; magIndex<NUM_MO_RATES; ++magIndex) {
					StringTokenizer tokenizer = new StringTokenizer(faultBranchesLines.get(i));
					double moRate = Double.parseDouble(tokenizer.nextToken());
					double weight = Double.parseDouble(tokenizer.nextToken());
					moRateOptions.addValueWeight(moRate, weight);
					++i;
				}
				paramValues.add(moRateOptions);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Add Background logic tree branches
	 *
	 */
	private void addBackgroundBranches() {
		try {
			ArrayList<String> backgroundLines = FileUtils.loadFile(BACKGROUND_LOGIC_TREE_FILE_NAME);
			paramNames.add(BACKGROUND);
			ParamOptions grOptions = new ParamOptions();
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
				grOptions.addValueWeight(grMFD, weight);
			}
			this.paramValues.add(grOptions);
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
		return this.weights.size();
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
		ParameterList paramList = this.logicTreeParamList.get(index);
		// iterate over all parameters
		for(int i=0; i<paramNames.size(); ++i) {
			String paramName = paramNames.get(i);
			Object value = paramList.getValue(paramName);
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
	 * 
	 * @return : ArrayList of Double values
	 */
	public ArrayList<Double> getRelativeWeightsList() {
		return weights;
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
