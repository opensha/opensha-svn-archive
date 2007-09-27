/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.util.ArrayList;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedNoCalRegion;
import org.opensha.data.region.EvenlyGriddedSoCalRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This class is used to generate MFDs for NoCal and SoCal Regions.
 * This accepts a region and hence can be used to generate MFDs for any region
 * 
 * @author vipingupta
 *
 */
public class NoCalSoCalMFDsPlotter extends LogicTreeMFDsPlotter {
	private final static String NO_CAL_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/logicTreeMFDs/NoCal/";
	private final static String SO_CAL_PATH = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_3/data/logicTreeMFDs/SoCal/";
	private final static double MIN_MAG = UCERF2.MIN_MAG-UCERF2.DELTA_MAG/2;
	private final static double MAX_MAG = UCERF2.MAX_MAG-UCERF2.DELTA_MAG/2;
	private final static int NUM_MAG = UCERF2.NUM_MAG;
	
	private GeographicRegion region;
	
	/**
	 * Set the region for which MFDs need to be calculated
	 * @param region
	 */
	public NoCalSoCalMFDsPlotter (GeographicRegion region) {
		this.region = region;
	}
	
	/**
	 * Get A_Faults MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_A_FaultsMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotal_A_FaultsProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	
	/**
	 * Get B_Faults Char MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_B_FaultsCharMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotal_B_FaultsProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	
	/**
	 * Get B_Faults GR MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_B_FaultsGR_MFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		return cumMFD;
	}

	/**
	 * Get Non CA B_Faults MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_NonCA_B_FaultsMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotal_NonCA_B_FaultsProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	
	/**
	 * Get Total MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotalMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotalProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	
	/**
	 * Get Observed Cum MFD
	 * 
	 * @param ucerf2
	 * @return
	 */
	protected  ArrayList<EvenlyDiscretizedFunc> getObsCumMFD(UCERF2 ucerf2) {
		if(region instanceof EvenlyGriddedNoCalRegion) return ucerf2.getObsCumNoCalMFD();
		else if (region instanceof EvenlyGriddedSoCalRegion) return ucerf2.getObsCumSoCalMFD();
		else throw new RuntimeException("Unsupported region");
	}
	
	/**
	 * Get C-Zones MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_C_ZoneMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotal_C_ZoneProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	
	/**
	 * Get Background MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_BackgroundMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist cumMFD = new IncrementalMagFreqDist(MIN_MAG, MAX_MAG, NUM_MAG);
		ucerf2.getTotal_BackgroundProb(cumMFD, region);
		convertProbToPoissonRates(cumMFD, ucerf2.getTimeSpan().getDuration());
		return cumMFD;
	}
	

	/**
	 * Get Observed Incr MFD
	 * 
	 * @param ucerf2
	 * @return
	 */
	protected  ArrayList<ArbitrarilyDiscretizedFunc> getObsIncrMFD(UCERF2 ucerf2) {
		if(region instanceof EvenlyGriddedNoCalRegion) return ucerf2.getObsIncrNoCalMFD();
		else if (region instanceof EvenlyGriddedSoCalRegion) return ucerf2.getObsIncrSoCalMFD();
		else throw new RuntimeException("Unsupported region");
	}
	
	/**
	 * Convert Probs to Poisson rates
	 * 
	 * @param incrMFD
	 * @param duration
	 */
	private void convertProbToPoissonRates(IncrementalMagFreqDist cumMFD, double duration) {
		for(int i=0; i <cumMFD.getNum();i++){
			cumMFD.set(i,-Math.log(1-cumMFD.getY(i))/duration);
	
		}
	}
	
	public static void main(String args[]) {
		NoCalSoCalMFDsPlotter plotter = new NoCalSoCalMFDsPlotter(new EvenlyGriddedNoCalRegion());
		//plotter.generateMFDsData(NoCalSoCalMFDsPlotter.NO_CAL_PATH);
		plotter.plotMFDs(NoCalSoCalMFDsPlotter.NO_CAL_PATH, true);
		//NoCalSoCalMFDsPlotter plotter = new NoCalSoCalMFDsPlotter(new EvenlyGriddedSoCalRegion());
		//plotter.generateMFDsData(NoCalSoCalMFDsPlotter.SO_CAL_PATH);
		//plotter.plotMFDs(NoCalSoCalMFDsPlotter.SO_CAL_PATH, true);
	}
	
}
