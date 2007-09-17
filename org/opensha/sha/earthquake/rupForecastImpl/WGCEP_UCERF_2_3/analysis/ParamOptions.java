/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.util.ArrayList;

/**
 * @author vipingupta
 *
 */
/**
 * Various parameter values and their corresponding weights.
 * This class is used to generate logic tree branches.
 * 
 * @author vipingupta
 *
 */
public class ParamOptions {
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

