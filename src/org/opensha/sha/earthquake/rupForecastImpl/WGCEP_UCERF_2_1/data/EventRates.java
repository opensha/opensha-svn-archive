/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.data;

/**
 * This class is used to save the site names, locations and event rates from 
 * Tom Parson's excel sheet
 * 
 * @author vipingupta
 *
 */
public class EventRates {
	private String siteName;
	private double latitude;
	private double longitude;
	private double obsEventRate;
	private double obsSigma;
	private double predictedRate;
	
	public EventRates() { }
	
	public EventRates(String siteName, double latitude, double longitude,
			double obsEventRate, double obsSigma) {
		setAll( siteName,  latitude,  longitude, obsEventRate,  obsSigma);
	}
	
	public void setAll(String siteName, double latitude, double longitude,
			double obsEventRate, double obsSigma) {
		setSiteName(siteName);
		setLatitude(latitude);
		setLongitude(longitude);
		this.setObsEventRate(obsEventRate);
		this.setObsSigma(obsSigma);
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getObsEventRate() {
		return obsEventRate;
	}
	public void setObsEventRate(double obsEventRate) {
		this.obsEventRate = obsEventRate;
	}
	public double getObsSigma() {
		return obsSigma;
	}
	public void setObsSigma(double obsSigma) {
		this.obsSigma = obsSigma;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public double getPredictedRate() {
		return predictedRate;
	}

	public void setPredictedRate(double predictedRate) {
		this.predictedRate = predictedRate;
	}
}
