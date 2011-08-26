package scratch.UCERF3.utils;

import java.util.ArrayList;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.ned.ETAS_Tests.MeanUCERF2.MeanUCERF2_ETAS;

/**
 * This class computes the following Mag Freq Dists for UCERF2 inside the supplied region:
 * 
 * totalMFD - total nucleation MFD for all ruptures in the region
 * faultMFD - total nucleation MFD for fault-based sources in the region
 * backgroundSeisMFD - total nucleation MFD for background seismicity in the region (including type C zones)
 * targetMFD - a GR distribution up to M 8.5, w/ b=1, and scaled to match totalMFD rate at M 5
 * targetMinusBackgroundMFD = targetMFD - backgroundSeisMFD (to use as equality constraint in inversion)
 * 
 * 
 * Note that if CaliforniaRegions.RELM_NOCAL() is used this does not give the same  
 * result as FindEquivUCERF2_Ruptures.getN_CalTargetMinusBackground_MFD()
 * for reasons that include:  1) background there includes non-CA b faults; 
 * 2) that ones does not include aleatory uncertainty on mag for each area;
 * 3) slightly different a-value; and 4) that class uses a modified version
 * of CaliforniaRegions.RELM_NOCAL().
 * @author field
 *
 */
public class UCERF2_MFD_ConstraintFetcher {
	
	MeanUCERF2_ETAS meanUCERF2_ETAS;	// using this because it has aftershocks added in
	Region region;
	SummedMagFreqDist totalMFD, faultMFD, backgroundSeisMFD, targetMinusBackgroundMFD;
	GutenbergRichterMagFreqDist targetMFD;
	
	// discretization params for MFDs:
	final static double MIN_MAG=5.05;
	final static int NUM_MAG=35;
	final static double DELTA_MAG=0.1;
	
	final static double B_VALUE = 1.0;	// b-value for total target distribution
	final static int LAST_FLT_SRC_INDEX = 408; // found by hand!
	
	
	public UCERF2_MFD_ConstraintFetcher(Region region) {
		
		this.region=region;

		long startRunTime=System.currentTimeMillis();

		System.out.println("Starting MeanUCERF2_ETAS instantiation");
		double forecastDuration = 1.0;	// years
		meanUCERF2_ETAS = new MeanUCERF2_ETAS();
		meanUCERF2_ETAS.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2_ETAS.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
//		meanUCERF2_ETAS.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		meanUCERF2_ETAS.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//		meanUCERF2_ETAS.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
		meanUCERF2_ETAS.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2_ETAS.getTimeSpan().setDuration(forecastDuration);
		meanUCERF2_ETAS.updateForecast();
		double runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("MeanUCERF2_ETAS instantiation took "+runtime+" seconds");
		
		startRunTime=System.currentTimeMillis();
		System.out.println("Starting computeMFDs()");
		computeMFDs();
		runtime = (System.currentTimeMillis()-startRunTime)/1000;
		System.out.println("Computing MFDs took "+runtime+" seconds");


	}
	
	/**
	 * Set a new region (and compute the MFDs)
	 * @param region
	 */
	public void setRegion(Region region) {
		this.region=region;
		this.computeMFDs();
	}
	
	/**
	 * This returns an MFD_InversionConstraint containing the targetMinusBackgroundMFD
	 * (to use for equality constraint)
	 * @return
	 */
	public MFD_InversionConstraint getTargetMinusBackgrMFDConstraint() {
		return new MFD_InversionConstraint(targetMinusBackgroundMFD, region);
	}
	
	/**
	 * This returns an MFD_InversionConstraint containing the targetMFD
	 * (to use for inequality constraint)
	 * @return
	 */
	public MFD_InversionConstraint getTargetMFDConstraint() {
		return new MFD_InversionConstraint(targetMFD, region);
	}
	
	public SummedMagFreqDist getTotalMFD() { return totalMFD; }
	
	public SummedMagFreqDist getFaultMFD() { return faultMFD; }
	
	public SummedMagFreqDist getBackgroundSeisMFD() { return backgroundSeisMFD; }
	
	public SummedMagFreqDist getTargetMinusBackgroundMFD() { return targetMinusBackgroundMFD; }
	
	public GutenbergRichterMagFreqDist targetMFD() {return targetMFD; }
	
	/**
	 * This computes the various MFDs
	 */
	private void computeMFDs() {
		if(region == null)
			throw new RuntimeException("Error: Region has not been set");
		
		 totalMFD = new SummedMagFreqDist(MIN_MAG,NUM_MAG,DELTA_MAG); 
		 faultMFD = new SummedMagFreqDist(MIN_MAG,NUM_MAG,DELTA_MAG); 
		 backgroundSeisMFD = new SummedMagFreqDist(MIN_MAG,NUM_MAG,DELTA_MAG); 
		 targetMinusBackgroundMFD = new SummedMagFreqDist(MIN_MAG,NUM_MAG,DELTA_MAG); 
		 
		  double duration = meanUCERF2_ETAS.getTimeSpan().getDuration();
		  for (int s = 0; s < meanUCERF2_ETAS.getNumSources(); ++s) {
			  ProbEqkSource source = meanUCERF2_ETAS.getSource(s);
			  for (int r = 0; r < source.getNumRuptures(); ++r) {
				  ProbEqkRupture rupture = source.getRupture(r);
				  double mag = rupture.getMag();
				  double equivRate = rupture.getMeanAnnualRate(duration);
				  double fractionInside = RegionUtils.getFractionInside(region, rupture.getRuptureSurface().getLocationList());
				  totalMFD.addResampledMagRate(mag, equivRate*fractionInside, true);
				  if(s<=LAST_FLT_SRC_INDEX)
					  faultMFD.addResampledMagRate(mag, equivRate*fractionInside, true);
				  else
					  backgroundSeisMFD.addResampledMagRate(mag, equivRate*fractionInside, true);
			  }
		  }
		  targetMFD = new GutenbergRichterMagFreqDist(MIN_MAG,NUM_MAG,DELTA_MAG,1.0,B_VALUE);
		  targetMFD.scaleToIncrRate(MIN_MAG, totalMFD.getY(MIN_MAG));
		  
		  targetMinusBackgroundMFD.addIncrementalMagFreqDist(targetMFD);
		  targetMinusBackgroundMFD.subtractIncrementalMagFreqDist(backgroundSeisMFD);
		  
		  totalMFD.setName("Total MFD for UCERF2 in Region");
		  faultMFD.setName("Total Fault MFD for UCERF2 in Region");
		  backgroundSeisMFD.setName("Total Background Seis. MFD for UCERF2 in Region");
		  targetMFD.setName("Target MFD for UCERF2 in Region");
		  targetMinusBackgroundMFD.setName("Target minus Background MFD for UCERF2 in Region");
		  totalMFD.setInfo(" ");
		  faultMFD.setInfo(" ");
		  backgroundSeisMFD.setInfo(" ");
		  targetMinusBackgroundMFD.setInfo(" ");
	}
	
	
	/**
	 * This plots the computed MFDs
	 */
	public void plotMFDs() {
		ArrayList funcs = new ArrayList();
		funcs.add(totalMFD);
		funcs.add(faultMFD);
		funcs.add(backgroundSeisMFD);
		funcs.add(targetMFD);
		funcs.add(targetMinusBackgroundMFD);
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag-Freq Dists"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setY_AxisRange(1e-4, 10);
		graph.setX_AxisRange(5, 8.5);
		graph.setYLog(true);
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Region relmOrigNCal = new CaliforniaRegions.RELM_NOCAL();
		Region region  = new Region(relmOrigNCal.getBorder(), BorderType.GREAT_CIRCLE);
		
//		Region region new CaliforniaRegions.RELM_GRIDDED();

		
		UCERF2_MFD_ConstraintFetcher fetcher = new UCERF2_MFD_ConstraintFetcher(region);
		fetcher.plotMFDs();
	}
}
