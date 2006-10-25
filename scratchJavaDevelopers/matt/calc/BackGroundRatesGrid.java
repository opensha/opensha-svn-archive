package scratchJavaDevelopers.matt.calc;

import org.opensha.sha.earthquake.griddedForecast.*;
import org.opensha.data.region.*;
import org.opensha.data.*;
import org.opensha.sha.magdist.*;

public class BackGroundRatesGrid extends GriddedHypoMagFreqDistForecast{
	
	protected HypoMagFreqDistAtLoc[] griddedMagFreqDistForecast;
	private double minForecastMag, maxForecastMag, deltaForecastMag;
	private double[] seqIndAtNode;
	
	
	public BackGroundRatesGrid(){
		this.loadBackGroundGridFromFile();
		//region must be initialised before this can be done
		// set to a dummy value representing the background so that 
		// it can be changed to a sequence index if required later
		// this indicates the sequence that contributes rates at this
		// index.  -1 means no sequence does.
		seqIndAtNode = new double[this.region.getNumGridLocs()];
		java.util.Arrays.fill(seqIndAtNode,-1);
		
	}
	
	/**
	 * I am not sure how the background grid will be loaded but this may be
	 * needed
	 *
	 */
	public void loadBackGroundGridFromFile(){
		griddedMagFreqDistForecast = null;
	}
	
	public void setBackGroundRegion(EvenlyGriddedGeographicRegionAPI backGroundRegion){
		this.region = backGroundRegion;
	}
	
	/**
	 * setMinForecastMag
	 */
	public void setMinForecastMag(double minMag) {
		this.minForecastMag = minMag;
	}
	
	/**
	 * setMaxForecastMag
	 */
	public void setMaxForecastMag(double maxMag) {
		this.maxForecastMag = maxMag;
	}
	
	/**
	 * setDeltaForecastMag
	 */
	public void setDeltaForecastMag(double deltaMag) {
		this.deltaForecastMag = deltaMag;
	}
	
	/**
	 * setSeqIndAtNode
	 * @param ithLocation
	 * @param seqInd
	 * seqIndAtNode[] contains the index of the STEP_CombineForecast
	 * that contributes rates to this node if applicable.  if = -1, the rates
	 * come from the background.  
	 */
	public void setSeqIndAtNode(int ithLocation, int seqInd){
		seqIndAtNode[ithLocation] = seqInd;
	}
	
	public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation){
		return null;
	}
	
	/**
	 * setMagFreqDistAtLoc
	 * @param locDist
	 * @param ithLocation
	 * set the (gridded) IncrementalMagFreqDist at this location 
	 */
	
	public void setMagFreqDistAtLoc(IncrementalMagFreqDist locDist, int ithLocation){
		Location distLoc;
		distLoc = griddedMagFreqDistForecast[ithLocation].getLocation();
		HypoMagFreqDistAtLoc locFreqDist = new HypoMagFreqDistAtLoc(locDist, distLoc); 
	    griddedMagFreqDistForecast[ithLocation] = locFreqDist;
	}

}
