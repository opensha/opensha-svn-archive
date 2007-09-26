/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import org.opensha.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;

import scratchJavaDevelopers.vipin.relm.RELM_ERF_ToGriddedHypoMagFreqDistForecast;

/**
 * It generates statewide bulge analysis files that can be plotted using GMT.
 *  
 * @author vipingupta
 *
 */
public class GenerateFilesForBulgeAnalysisMaps {
	public static void main(String[] args) {
		  // region to view the rates
		  EvenlyGriddedRELM_TestingRegion evenlyGriddedRegion  = new EvenlyGriddedRELM_TestingRegion();
	   
		  UCERF2 eqkRupForecast = new UCERF2();
		  eqkRupForecast.getTimeSpan().setDuration(5.0);
		  eqkRupForecast.updateForecast();
		  // min mag, maxMag, These are Centers of first and last bin
		  double minMag=5.0, maxMag=9.00;
		  int numMag = 41; // number of Mag bins
		  //	 make GriddedHypoMFD Forecast from the EqkRupForecast
		  RELM_ERF_ToGriddedHypoMagFreqDistForecast griddedHypoMagFeqDistForecast =
			  new RELM_ERF_ToGriddedHypoMagFreqDistForecast(eqkRupForecast, evenlyGriddedRegion,
					  minMag, maxMag, numMag, 5.0); // 5 year rates
		      
		  // minLat=31.5, maxLat=43.0, minLon=-125.4, MaxLon=-113.1
		  griddedHypoMagFeqDistForecast.generateNedsBulgeFiles("EqkRateModel2_ERF");
	  }
}
