/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

import java.util.ArrayList;

/**
 * This class is used to hold the segment rates and ruptures rates after reading
 * from the excel File
 * @author vipingupta
 *
 */
public class RupSegRates {
	private String faultName;
	//segment rates
	private ArrayList meanSegRecurInterval = new ArrayList();
//	segment rates
	private ArrayList lowSegRecurInterval = new ArrayList();
//	segment rates
	private ArrayList highSegRecurInterval = new ArrayList();
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
	public RupSegRates(String faultName) {
		this.faultName = faultName;
	}
	
	/**
	 * Get the segment name
	 * 
	 * @return
	 */
	public String getFaultName() {
		return faultName;
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
		return meanSegRecurInterval.size();
	}
	
	/**
	 * Get the mean segment recurrence interval
	 * @param index
	 * @return
	 */
	public double getMeanSegRecurInterv(int index) {
		return ((Double)meanSegRecurInterval.get(index)).doubleValue();
	}
	
	/**
	 * Add mean segment recurrence interval
	 * 
	 * @param recurInterval
	 */
	public void setMeanSegRecurInterv(int segIndex, double recurInterval) {
		if(segIndex<meanSegRecurInterval.size())
			this.meanSegRecurInterval.set(segIndex, new Double(recurInterval));
		else this.meanSegRecurInterval.add(new Double(recurInterval));
	}
	
	
	/**
	 * Get the low segment recurrence interval
	 * @param index
	 * @return
	 */
	public double getLowSegRecurInterv(int index) {
		return ((Double)lowSegRecurInterval.get(index)).doubleValue();
	}
	
	/**
	 * Add Low segment recurrence interval
	 * 
	 * @param recurInterval
	 */
	public void setLowSegRecurInterv(int segIndex, double recurInterval) {
		if(segIndex<lowSegRecurInterval.size())
			this.lowSegRecurInterval.set(segIndex, new Double(recurInterval));
		else this.lowSegRecurInterval.add(new Double(recurInterval));
	}
	
	/**
	 * Get the High segment recurrence interval
	 * @param index
	 * @return
	 */
	public double getHighSegRecurInterv(int index) {
		return ((Double)highSegRecurInterval.get(index)).doubleValue();
	}
	
	/**
	 * Add High segment recurrence interval
	 * 
	 * @param recurInterval
	 */
	public void setHighSegRecurInterv(int segIndex, double recurInterval) {
		if(segIndex<highSegRecurInterval.size())
			this.highSegRecurInterval.set(segIndex, new Double(recurInterval));
		else this.highSegRecurInterval.add(new Double(recurInterval));
			
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
