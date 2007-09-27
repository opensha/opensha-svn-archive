/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

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
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		ucerf2.getTotal_A_FaultsProb(incrMFD, region);
		convertProbToPoissonRates(incrMFD, ucerf2.getTimeSpan().getDuration());
		return incrMFD;
	}
	
	/**
	 * Get B_Faults Char MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_B_FaultsCharMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		ucerf2.getTotal_B_FaultsProb(incrMFD, region);
		convertProbToPoissonRates(incrMFD, ucerf2.getTimeSpan().getDuration());
		return incrMFD;
	}
	
	/**
	 * Get B_Faults GR MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_B_FaultsGR_MFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		return incrMFD;
	}

	/**
	 * Get Non CA B_Faults MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotal_NonCA_B_FaultsMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		ucerf2.getTotal_NonCA_B_FaultsProb(incrMFD, region);
		convertProbToPoissonRates(incrMFD, ucerf2.getTimeSpan().getDuration());
		return incrMFD;
	}
	
	/**
	 * Get Total MFD
	 * @param ucerf2
	 * @return
	 */
	protected IncrementalMagFreqDist getTotalMFD(UCERF2 ucerf2) {
		IncrementalMagFreqDist incrMFD = new IncrementalMagFreqDist(UCERF2.MIN_MAG, UCERF2.MAX_MAG, UCERF2.NUM_MAG);
		ucerf2.getTotalProb(incrMFD, region);
		convertProbToPoissonRates(incrMFD, ucerf2.getTimeSpan().getDuration());
		return incrMFD;
	}
	
	
	/**
	 * Convert Probs to Poisson rates
	 * 
	 * @param incrMFD
	 * @param duration
	 */
	private void convertProbToPoissonRates(IncrementalMagFreqDist incrMFD, double duration) {
		for(int i=0; i <incrMFD.getNum();i++){
			incrMFD.set(i,-Math.log(1-incrMFD.getY(i))/duration);
	
		}
	}
	
	public static void main(String args[]) {
		NoCalSoCalMFDsPlotter plotter = new NoCalSoCalMFDsPlotter(new EvenlyGriddedNoCalRegion());
		plotter.generateMFDsData(NoCalSoCalMFDsPlotter.NO_CAL_PATH);
		plotter.plotMFDs(NoCalSoCalMFDsPlotter.NO_CAL_PATH, true);
		//NoCalSoCalMFDsPlotter plotter = new NoCalSoCalMFDsPlotter(new EvenlyGriddedSoCalRegion());
		//plotter.generateMFDsData(NoCalSoCalMFDsPlotter.SO_CAL_PATH);
		//plotter.plotMFDs(NoCalSoCalMFDsPlotter.SO_CAL_PATH, true);
	}
	
}
