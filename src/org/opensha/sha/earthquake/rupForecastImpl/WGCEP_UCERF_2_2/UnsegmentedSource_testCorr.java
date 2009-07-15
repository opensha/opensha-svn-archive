/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_2;

import java.util.ArrayList;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * @author vipingupta
 *
 */
public class UnsegmentedSource_testCorr extends UnsegmentedSource {

	//  ratio of orig slip rate to predicted slip rate on each discretized location on the fault surface
	private ArbitrarilyDiscretizedFunc ratioFunc;
	private ArbitrarilyDiscretizedFunc finalSlipRateFunc;
	private ArrayList<ArbitrarilyDiscretizedFunc>  magBasedFinalSlipRateFuncs;

	/**
	 * @param segmentData
	 * @param magAreaRel
	 * @param sourceMagPDF
	 * @param moRateReduction
	 */
	public UnsegmentedSource_testCorr(FaultSegmentData segmentData,
			MagAreaRelationship magAreaRel,
			IncrementalMagFreqDist sourceMagPDF, double moRateReduction) {
		super(segmentData, magAreaRel, sourceMagPDF, moRateReduction);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param segmentData
	 * @param magAreaRel
	 * @param fractCharVsGR
	 * @param min_mag
	 * @param max_mag
	 * @param num_mag
	 * @param charMagSigma
	 * @param charMagTruncLevel
	 * @param mag_lowerGR
	 * @param b_valueGR
	 * @param moRateReduction
	 * @param fixMag
	 * @param fixRate
	 * @param meanMagCorrection
	 * @param isSlipRateCorrection
	 */
	public UnsegmentedSource_testCorr(FaultSegmentData segmentData,
			MagAreaRelationship magAreaRel, double fractCharVsGR,
			double min_mag, double max_mag, int num_mag, double charMagSigma,
			double charMagTruncLevel, double mag_lowerGR, double b_valueGR,
			double moRateReduction, double fixMag, double fixRate,
			double meanMagCorrection) {
		super(segmentData, magAreaRel, fractCharVsGR, min_mag, max_mag,
				num_mag, charMagSigma, charMagTruncLevel, mag_lowerGR,
				b_valueGR, moRateReduction, fixMag, fixRate, meanMagCorrection);
		getModifiedSlipRateAlongFault();
	}
	
	/**
	 * Get final corrected slip rate along the fault
	 * 
	 * @return
	 */
	public ArbitrarilyDiscretizedFunc getModifiedSlipRateAlongFault() {
		if(finalSlipRateFunc!=null) return finalSlipRateFunc;
			
		finalSlipRateFunc = new ArbitrarilyDiscretizedFunc();
		magBasedFinalSlipRateFuncs = new ArrayList<ArbitrarilyDiscretizedFunc>();
		//calculate ratio of original slip rate to predicted slip rate at each location
		double totRatio=0;
		ratioFunc = new ArbitrarilyDiscretizedFunc();
		int numCols = surfaceLocList.size();
		for(int i=0; i<numCols; ++i) {
			totRatio += origSlipRateFunc.getY(i)/predSlipRateFunc.getY(i);
			ratioFunc.set((double)i, totRatio);
		}
		
		this.getModifiedSlipRateAlongFault(); // calculate final slip rate along fault
		//System.out.print("Final Moment Rate:");
		computeSlipRateAlongFault(finalSlipRateFunc, magBasedFinalSlipRateFuncs, true);
		finalSlipRateFunc.setName("Modified slip rate along fault");
		return finalSlipRateFunc;
	}
	
	/**
	 * Get Mag Based slip rate func list along the fault
	 * 
	 * @return
	 */
	public ArrayList<ArbitrarilyDiscretizedFunc> getMagBasedModifiedSlipRateListAlongFault() {
		if(this.magBasedFinalSlipRateFuncs==null) getModifiedSlipRateAlongFault();
		return this.magBasedFinalSlipRateFuncs;
	}
	
	
	
	/**
	   * This gets the ProbEqkRupture object for the nth Rupture
	   * It adjusts the rupture rates to account for difference in original
	   * and predicted slip rate
	   */
	  public ProbEqkRupture getRupture(int nthRupture) {
		  ProbEqkRupture rupture = super.getRupture(nthRupture);
		  
		  EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
		  int index1 = this.surfaceLocList.getLocationIndex(rupSurface.getLocation(0, 0));
		  int index2 = this.surfaceLocList.getLocationIndex(rupSurface.getLocation(0, rupSurface.getNumCols()-1));
		  double ratio = (ratioFunc.getY(index2)-ratioFunc.getY(index1))/(index2-index1);
		  double rate = rupture.getMeanAnnualRate(this.duration)*ratio;
		  double prob = 1- Math.exp(-duration*rate);
		  rupture.setProbability(prob);
		  return rupture;
	  }

}
