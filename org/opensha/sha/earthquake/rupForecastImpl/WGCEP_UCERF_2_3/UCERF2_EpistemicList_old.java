/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.opensha.exceptions.FaultException;
import org.opensha.param.BooleanParameter;
import org.opensha.param.DoubleParameter;
import org.opensha.param.IntegerParameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.StringParameter;
import org.opensha.sha.earthquake.ERF_EpistemicList;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast;
import org.opensha.util.FileUtils;

/**
 * This class generates a list of UCERF2 ERF that represent each logic tree branch
 * 
 * @author vipingupta
 *
 */
public class UCERF2_EpistemicList_old extends ERF_EpistemicList {
	public static final String  NAME = new String("UCERF2 ERF List");
	private final static String BRANCH_LNE_PREFIX = "#";
	private final static String INPUT_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/LogicTreeBranches.txt";
	private ArrayList<String> fileLines = null;
	private ArrayList<Double> weights = null;
	private int numBranches = -1;
	// mapping og branch number and the corresponding line number where branch starts
	private HashMap<Integer, Integer> branchLineNumberMap;
	private UCERF2 ucerf2 = new UCERF2();
	
	public UCERF2_EpistemicList_old() {
		try {
			fileLines = FileUtils.loadJarFile(INPUT_FILE_NAME);
			int numLines = fileLines.size();
			numBranches = -1;
			weights = new ArrayList<Double>();
			branchLineNumberMap = new HashMap<Integer, Integer>();
			for(int lineIndex=0; lineIndex<numLines; ++lineIndex)
				if(fileLines.get(lineIndex).startsWith("#")) {  // # signifies start of a branch
					++numBranches;
					StringTokenizer tokenizer = new StringTokenizer(fileLines.get(lineIndex), ";");
					tokenizer.nextToken();
					weights.add(Double.parseDouble(tokenizer.nextToken().trim()));
					// mapping of branch index and correspding line in the file
					branchLineNumberMap.put(numBranches, lineIndex);
				}
			++numBranches;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		return this.numBranches;
	}


	/**
	 * Get the ERF in the list with the specified index.
	 * 
	 * 
	 * @param index : index of Eqk rup forecast to return
	 * @return
	 */
	public EqkRupForecastAPI getERF(int index) {
		//System.out.println("Getting ERF:"+index);
		
		int startLineNumber = this.branchLineNumberMap.get(index);
		int endLineNumber = this.fileLines.size();
		if(branchLineNumberMap.containsKey(index+1))
			endLineNumber = branchLineNumberMap.get(index+1);
		// set the default parameters in UCERF2
		ucerf2.setParamDefaults();
		// read the parameters and values and set in  UCERF2
		for(int i=startLineNumber+1; i<endLineNumber; ++i) {
			String line = fileLines.get(i);
			setParameterValue(line);
		}
		return ucerf2;
	}

	/**
	 * Set the paramter value from the String 
	 * 
	 * @param line
	 */
	private void setParameterValue(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line,"=");
		String paramName = tokenizer.nextToken().trim();
		String value = tokenizer.nextToken().trim();
		ParameterAPI parameter = this.ucerf2.getParameter(paramName);
		if(parameter instanceof DoubleParameter)
			parameter.setValue(new Double(value));
		else if(parameter instanceof StringParameter)
			parameter.setValue(value);
		else if(parameter instanceof BooleanParameter)
			parameter.setValue(new Boolean(value));
		else if(parameter instanceof IntegerParameter)
			parameter.setValue(new Integer(value));
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
			System.out.println(ucerf2EpistemicList.getERF(i).getAdjustableParameterList().toString());
			
		}
		
	}

}
