/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.data;

import java.util.ArrayList;

/**
 * This class is used to hold the segment rates and ruptures rates after reading
 * from the excel File
 * @author vipingupta
 *
 */
public class RupSegRates {
	private String segmentName;
	//segment rates
	private ArrayList segmentRecurInterval = new ArrayList();
	// rupture rates for geologic insight model
	private ArrayList geolInsightRupRate = new ArrayList();
	// rup rates for min rup model
	private ArrayList minRupRate = new ArrayList();
	// rup rates for max rup model
	private ArrayList maxRupRate = new ArrayList();
	
	/**
	 * constructor : Aceepts the segment name
	 * @param segmentName
	 */
	public RupSegRates(String segmentName) {
		this.segmentName = segmentName;
	}
	
	/**
	 * Get the segment name
	 * 
	 * @return
	 */
	public String getSegmentName() {
		return segmentName;
	}
	
	/**
	 * Add the rup rate in the 3 models  to the list
	 * @param prefRate 
	 * @param minRate
	 * @param maxRate
	 */
	public void addRupRate(double prefRate, double minRate, double maxRate) {
		this.geolInsightRupRate.add(new Double(prefRate));
		this.minRupRate.add(new Double(minRate));
		this.maxRupRate.add(new Double(maxRate));
	}
	
	/**
	 * Get the number of segments
	 * 
	 * @return
	 */
	public int getNumSegments() {
		return segmentRecurInterval.size();
	}
	
	/**
	 * Get the segment rate
	 * @param index
	 * @return
	 */
	public double getSegRecurInterv(int index) {
		return ((Double)segmentRecurInterval.get(index)).doubleValue();
	}
	
	/**
	 * Add segment recurrence interval
	 * 
	 * @param recurInterval
	 */
	public void addSegRecurInterv(double recurInterval) {
		this.segmentRecurInterval.add(new Double(recurInterval));
	}
	
	/**
	 * Get the number of ruptures
	 * 
	 * @return
	 */
	public int getNumRups() {
		return geolInsightRupRate.size();
	}
	
	/**
	 * Get rupture rate for geologic insight model
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getPrefModelRupRate(int rupIndex) {
		return ((Double)geolInsightRupRate.get(rupIndex)).doubleValue();
	}
	
	/**
	 * Get rupture rate for min rup Rate model
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getMinModelRupRate(int rupIndex) {
		return ((Double)minRupRate.get(rupIndex)).doubleValue();
	}
	
	/**
	 * Get rupture rate for max rup Rate model
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getMaxModelRupRate(int rupIndex) {
		return ((Double)maxRupRate.get(rupIndex)).doubleValue();
	}
	
}
