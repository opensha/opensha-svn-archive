/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.analysis;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.data.Location;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.data.region.EvenlyGriddedRELM_TestingRegion;
import org.opensha.sha.calc.ERF2GriddedSeisRatesCalc;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.griddedForecast.GriddedHypoMagFreqDistForecast;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;

import scratchJavaDevelopers.vipin.relm.RELM_ERF_ToGriddedHypoMagFreqDistForecast;

/**
 * It generates statewide bulge analysis files that can be plotted using GMT.
 *  
 * @author vipingupta
 *
 */
public class GenerateFilesForParticipationProbMaps {
	
	public static void main(String[] args) {
		int duration = 30;
		double mags[] = { 5.0, 6.0, 6.5, 6.7, 7.2, 7.5, 7.7, 8.0};
		// region to view the rates
		EvenlyGriddedRELM_TestingRegion evenlyGriddedRegion  = new EvenlyGriddedRELM_TestingRegion();

		// UCERF 2
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
	    // include background sources as point sources
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_CROSSHAIR);
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		
		// min mag, maxMag, These are Centers of first and last bin
		double minMag=5.0, maxMag=9.00;
		int numMag = 41; // number of Mag bins

		// FOR POISSON
		ERF_ToGriddedParticipationRatesMFD_Forecast griddedHypoMagFeqDistForecast1 =
			new ERF_ToGriddedParticipationRatesMFD_Forecast(meanUCERF2, evenlyGriddedRegion,
					minMag, maxMag, numMag, duration); 
		// minLat=31.5, maxLat=43.0, minLon=-125.4, MaxLon=-113.1
		generateRatesFiles("UCERF2_Poiss", griddedHypoMagFeqDistForecast1, mags, duration);
	
		// FOR PREFERRED BLEND
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(MeanUCERF2.PROB_MODEL_WGCEP_PREF_BLEND);	
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		ERF_ToGriddedParticipationRatesMFD_Forecast griddedHypoMagFeqDistForecast2 =
			new ERF_ToGriddedParticipationRatesMFD_Forecast(meanUCERF2, evenlyGriddedRegion,
					minMag, maxMag, numMag, duration); 
		// minLat=31.5, maxLat=43.0, minLon=-125.4, MaxLon=-113.1
		generateRatesFiles("UCERF2_PrefBlend", griddedHypoMagFeqDistForecast2, mags, duration);

		generateRatioFiles("UCERF2_PrefBlend_Poiss", griddedHypoMagFeqDistForecast2, griddedHypoMagFeqDistForecast1, mags, duration);
	}
	
	
	/**
	 * It generates 2 files:
	 * 1. File that has ratio of cumulative rates at Mag 5 for each location
	 * 2. File that has ratio of cumulative rates at Mag 6.5 for each location
	 * 
	 * @param fileNamePrefix
	 * @param griddedHypoMagFeqDistForecast1
	 * @param griddedHypoMagFeqDistForecast2
	 */
	public static void generateRatioFiles(String fileNamePrefix, 
			ERF_ToGriddedParticipationRatesMFD_Forecast griddedHypoMagFeqDistForecast1,
			ERF_ToGriddedParticipationRatesMFD_Forecast griddedHypoMagFeqDistForecast2,
			double[] mags, double duration) {
		
		 double predictedRate1, probability1, predictedRate2, probability2;
		  try {
			  FileWriter[] fileWriters;
			  fileWriters = new FileWriter[mags.length];
			  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				  fileWriters[magIndex] = new FileWriter(fileNamePrefix+"_Ratio"+mags[magIndex]+".txt"); // predicted rates at Mag 5
			  }
			  int numLocs = griddedHypoMagFeqDistForecast1.getNumHypoLocs();
			  float latitude, longitude;
			  for(int i=0; i<numLocs; ++i) {
				  HypoMagFreqDistAtLoc mfdAtLoc1 = griddedHypoMagFeqDistForecast1.getHypoMagFreqDistAtLoc(i);
				  HypoMagFreqDistAtLoc mfdAtLoc2 = griddedHypoMagFeqDistForecast2.getHypoMagFreqDistAtLoc(i);
				  Location loc = mfdAtLoc1.getLocation();
				  EvenlyDiscretizedFunc cumDist1  = mfdAtLoc1.getFirstMagFreqDist().getCumRateDist();
				  EvenlyDiscretizedFunc cumDist2  = mfdAtLoc2.getFirstMagFreqDist().getCumRateDist();
				  latitude = (float)loc.getLatitude();
				  longitude = (float)loc.getLongitude();
				  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
					  predictedRate1 = cumDist1.getInterpolatedY(mags[magIndex]);
					  probability1 = 1-Math.exp(-predictedRate1*duration);
					  predictedRate2 = cumDist2.getInterpolatedY(mags[magIndex]);
					  probability2 = 1-Math.exp(-predictedRate2*duration);
					  fileWriters[magIndex].write(latitude+"\t"+longitude+"\t"+(float)(probability1/probability2)+"\n");
				  }
			  }
			  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				  fileWriters[magIndex].close(); 
			  }
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
	}
	
	 /**
	   * This function generates as many files as the number of Mags
	   */
	  public  static void generateRatesFiles(String fileNamePrefix, 
			  ERF_ToGriddedParticipationRatesMFD_Forecast griddedHypoMagFeqDistForecast,
			  double[] mags, double duration) {
		  double predictedRate, probability;
		  try {
			  FileWriter[] fileWriters;
			  fileWriters = new FileWriter[mags.length];
			  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				  fileWriters[magIndex] = new FileWriter(fileNamePrefix+"_Pred"+mags[magIndex]+".txt"); // predicted rates at Mag 5
			  }
			  int numLocs = griddedHypoMagFeqDistForecast.getNumHypoLocs();
			  float latitude, longitude;
			  for(int i=0; i<numLocs; ++i) {
				  HypoMagFreqDistAtLoc mfdAtLoc = griddedHypoMagFeqDistForecast.getHypoMagFreqDistAtLoc(i);
				  Location loc = mfdAtLoc.getLocation();
				  EvenlyDiscretizedFunc cumDist  = mfdAtLoc.getFirstMagFreqDist().getCumRateDist();
				  latitude = (float)loc.getLatitude();
				  longitude = (float)loc.getLongitude();
				  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
					  predictedRate = cumDist.getInterpolatedY(mags[magIndex]);
					  probability = 1-Math.exp(-predictedRate*duration);
					  fileWriters[magIndex].write(latitude+"\t"+longitude+"\t"+(float)probability+"\n");
				  }
			  }
			  for(int magIndex=0; magIndex<mags.length; ++magIndex) {
				  fileWriters[magIndex].close(); 
			  }
		  }catch(Exception e) {
			  e.printStackTrace();
		  }
	  }
}

class ERF_ToGriddedParticipationRatesMFD_Forecast  extends GriddedHypoMagFreqDistForecast {
	  private EqkRupForecast eqkRupForecast;
	  private HypoMagFreqDistAtLoc magFreqDistForLocations[];

	  /**
	   * Accepts a forecast and a region. It calculates Magnitude-Freq distribution for
	   * each location within the region.
	   *
	   * @param forecast - the EqkRupForecast to be converted to GriddedHypoMagFreqDistForecast
	   * @param griddedRegion - EvenlyGriddedRegion for calculating magnitude frequency distribution
	   * @param minMag - Center of first magnitude bin to make IncrementalMagFreqDist.
	   * @param maxMag - Center of last magnitude bin to make IncrementalMagFreqDist
	   * @param numMags - Total number of  magnitude bins in IncrementalMagFreqDist
	   *
	   *
	   */
	  public ERF_ToGriddedParticipationRatesMFD_Forecast(EqkRupForecast eqkRupForecast,
	                                              EvenlyGriddedGeographicRegionAPI griddedRegion,
	                                              double minMag,
	                                              double maxMag,
	                                              int numMagBins,
	                                              double duration) {
	    this.eqkRupForecast = eqkRupForecast;
	    this.region = griddedRegion;

	    SummedMagFreqDist[] summedMFDs  =  calcMFD_ForGriddedRegion(minMag, maxMag, numMagBins, duration);
	    // make HypoMagFreqDist for each location in the region
	    magFreqDistForLocations = new HypoMagFreqDistAtLoc[this.getNumHypoLocs()];
	    for(int i=0; i<magFreqDistForLocations.length; ++i ) {
	      IncrementalMagFreqDist[] magFreqDistArray = new IncrementalMagFreqDist[1];
	      magFreqDistArray[0] = summedMFDs[i];
	      magFreqDistForLocations[i] = new HypoMagFreqDistAtLoc(magFreqDistArray,griddedRegion.getGridLocation(i));
	    }
	  }

	  /**
	   * gets the Hypocenter Mag.
	   *
	   * @param ithLocation int : Index of the location in the region
	   * @return HypoMagFreqDistAtLoc Object using which user can retrieve the
	   *   Magnitude Frequency Distribution.
	   * @todo Implement this
	   *   org.opensha.sha.earthquake.GriddedHypoMagFreqDistAtLocAPI method
	   */
	  public HypoMagFreqDistAtLoc getHypoMagFreqDistAtLoc(int ithLocation) {
	    return magFreqDistForLocations[ithLocation];
	  }
	  
	  
	  /*
	   * computes the Mag-Rate distribution for each location within the provided region.
	   */
	  private SummedMagFreqDist[] calcMFD_ForGriddedRegion(double minMag, double maxMag, int numMagBins, double duration) {

	    int numSources = eqkRupForecast.getNumSources();

	    int numLocations = region.getNumGridLocs();
	    SummedMagFreqDist[] summedMFDs = new SummedMagFreqDist[numLocations];
	   
	    for(int i=0; i<numLocations; ++i) summedMFDs[i] = new SummedMagFreqDist(minMag, maxMag, numMagBins);
	    //Going over each and every source in the forecast
	    for (int sourceIndex = 0; sourceIndex < numSources; ++sourceIndex) {
	      // get the ith source
	      ProbEqkSource source = eqkRupForecast.getSource(sourceIndex);
	      int numRuptures = source.getNumRuptures();
	      
	      //going over all the ruptures in the source
	      for (int rupIndex = 0; rupIndex < numRuptures; ++rupIndex) {
	        ProbEqkRupture rupture = source.getRupture(rupIndex);
	        double mag = rupture.getMag();
	        double meanAnnualRate = rupture.getMeanAnnualRate(duration);
	        ArrayList<Integer> locIndices = new ArrayList<Integer>();
	        EvenlyGriddedSurfaceAPI rupSurface = rupture.getRuptureSurface();
	        //getting the iterator for all points on the rupture
	        ListIterator it = rupSurface.getAllByRowsIterator();
	        int locIndex;
	        //looping over all the rupture pt location and finding the nearest location
	        //to them in the Geographical Region.
	        while (it.hasNext()) {
	          Location ptLoc = (Location) it.next();
	          //returns -1 if location not in the region
	          locIndex = region.getNearestLocationIndex(ptLoc);
	          if(locIndices.contains(locIndex) || locIndex<0) continue;
	          locIndices.add(locIndex);
	          summedMFDs[locIndex].addResampledMagRate(mag, meanAnnualRate, true);        }
	      }
	    }
	    return summedMFDs;
	  }

	}

