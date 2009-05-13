package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.data.*;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.calc.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.faultSurface.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;


/**
 * <p>Title: UnsegmentedSource </p>
 * <p>Description: 	CONSIDER EFFECT OF: ASEISMICITY; VARIABLE AREA FOR GIVEN MAG, AND OVERLAPPING STEPOVERS.
 * 
 * If Asismicity is applied as a reduction of area, then effective all down-dip widths (DDW) are reduced, 
 * and given a single value applied to all segments (DDW = the total reduced area divided by total length).
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class UnsegmentedSource extends ProbEqkSource {
	
	//for Debug purposes
	private static String C = new String("UnsegmentedSource");
	private final static boolean D = true;
	
	//name for this classs
	protected String NAME = "Unsegmented Source";
	
	protected double duration;
	
	private ArrayList ruptureList; // keep this in case we add more mags later
	private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
	
	private int num_seg;
	private double[] segRate, segVisibleRate; // segment rates 
	//private double[] segAveSlipRate; // ave slip rate for segment
	private ArbDiscrEmpiricalDistFunc[] segSlipDist, segVisibleSlipDist;  // segment slip dist
	
	private IncrementalMagFreqDist sourceMFD, grMFD, charMFD; // Mag Freq dist for source
	private IncrementalMagFreqDist visibleSourceMFD; // Mag Freq dist for visible ruptures
	private IncrementalMagFreqDist[] segSourceMFD;  // Mag Freq Dist for each segment
	private IncrementalMagFreqDist[] visibleSegSourceMFD;  // Mag Freq Dist for visible ruptures on each segment
	private double sourceMag;  // this is the char mag or upper mag if mag PDF is not given
	
	// inputs:
	private FaultSegmentData segmentData;
	private MagAreaRelationship magAreaRel;
	private double fixMag, fixRate;
	
	
	
	/**
	 * Description:  The constructs the source using a supplied Mag PDF
	 * 
	 */
	public UnsegmentedSource(FaultSegmentData segmentData, MagAreaRelationship magAreaRel, 
			IncrementalMagFreqDist sourceMagPDF, double moRateReduction) {
		
		this.isPoissonian = true;
		
		// source mag undefined because PDF given
		sourceMag = Double.NaN;
		grMFD = null;
		charMFD = null;
		
		this.segmentData = segmentData;
		this.magAreaRel = magAreaRel;
		
		num_seg = segmentData.getNumSegments();
		
		// get the source MFD
		sourceMFD = (IncrementalMagFreqDist)sourceMagPDF.deepClone();
		double sourceMoRate = segmentData.getTotalMomentRate()*(1-moRateReduction);
		sourceMFD.scaleToTotalMomentRate(sourceMoRate);
		
		// get the impled MFD for "visible" ruptures (those that are large 
		// enough that their rupture will be seen at the surface)
		visibleSourceMFD = (IncrementalMagFreqDist)sourceMFD.deepClone();
		for(int i =0; i<sourceMFD.getNum(); i++)
			visibleSourceMFD.set(i,sourceMFD.getY(i)*getProbVisible(sourceMFD.getX(i)));
		
		// get the rate of ruptures on each segment (segSourceMFD[seg])
		getSegSourceMFD();
		
		// now get the visible MFD for each segment
		visibleSegSourceMFD = new IncrementalMagFreqDist[num_seg];
		for(int s=0; s< num_seg; s++) {
			visibleSegSourceMFD[s] = (IncrementalMagFreqDist) segSourceMFD[s].deepClone();
			for(int i =0; i<sourceMFD.getNum(); i++)
				visibleSegSourceMFD[s].set(i,segSourceMFD[s].getY(i)*getProbVisible(segSourceMFD[s].getX(i)));
		}
		
		// change the info in the MFDs
		String new_info = "Unsegmented Source MFD\n"+sourceMFD.getInfo();
		new_info += "|n\nRescaled to:\n\n\tMoment Rate: "+(float)sourceMoRate+"\n\n\tNew Total Rate: "+(float)sourceMFD.getCumRate(0);
		sourceMFD.setInfo(new_info);
		
		new_info = "Visible Unsegmented Source MFD\n"+visibleSourceMFD.getInfo();
		new_info += "|n\nRescaled to:\n\n\tMoment Rate: "+(float)visibleSourceMFD.getTotalMomentRate()+
		"\n\n\tNew Total Rate: "+(float)visibleSourceMFD.getCumRate(0);
		visibleSourceMFD.setInfo(new_info);
		
		
		// find the total rate of ruptures for each segment
		segRate = new double[num_seg];
		segVisibleRate = new double[num_seg];
		for(int s=0; s< num_seg; s++) {
			segRate[s] = segSourceMFD[s].getTotalIncrRate();
			segVisibleRate[s] = visibleSegSourceMFD[s].getTotalIncrRate();
		}
		
		// find the slip distribution of each segment
		computeSegSlipDist();
		//if(D)
		//  for(int i=0; i<num_seg; ++i)
		//	  System.out.println("Slip for segment "+i+":  " +segSlipDist[i] +";  "+segVisibleSlipDist[i] );
	}
	
	
	
	
	/**
	 * Description:  The constructs the source as a fraction of charateristic (Gaussian) and GR
	 * 
	 */
	public UnsegmentedSource(FaultSegmentData segmentData, MagAreaRelationship magAreaRel, 
			double fractCharVsGR, double min_mag, double max_mag, int num_mag, 
			double charMagSigma, double charMagTruncLevel, 
			double mag_lowerGR, double b_valueGR, double moRateReduction, double fixMag,
			double fixRate, double meanMagCorrection) {
		
		this.isPoissonian = true;
		
		this.segmentData = segmentData;
		this.magAreaRel = magAreaRel;
		this.fixMag = fixMag;
		this.fixRate = fixRate*(1-moRateReduction);
		double delta_mag = (max_mag-min_mag)/(num_mag-1);
		
		double moRate;
		sourceMag = magAreaRel.getMedianMag(segmentData.getTotalArea()/1e6)+meanMagCorrection;  // this area is reduced by aseis if appropriate
		sourceMag = Math.round(sourceMag/delta_mag) * delta_mag;
		moRate = segmentData.getTotalMomentRate()*(1-moRateReduction);
		
		// only apply char if mag <= lower RG mag 
		if(sourceMag <= mag_lowerGR) {
			if(Double.isNaN(fixMag)) // if it is not a B Fault Fix
				charMFD = new GaussianMagFreqDist(min_mag, max_mag, num_mag, 
						sourceMag, charMagSigma, moRate, charMagTruncLevel, 2);
			else { // if it is a B Fault Fix
				charMFD = new GaussianMagFreqDist(min_mag, max_mag, num_mag,
						fixMag, charMagSigma, 1.0, charMagTruncLevel, 2);
				charMFD.scaleToCumRate(0, this.fixRate);
				
			}
			sourceMFD = charMFD;
		}
		else {
			sourceMFD = new SummedMagFreqDist(min_mag, max_mag, num_mag);
			//	make char dist 
			if(Double.isNaN(fixMag)) // if it is not a B Fault Fix
				charMFD = new GaussianMagFreqDist(min_mag, max_mag, num_mag, 
						sourceMag, charMagSigma, moRate*fractCharVsGR, charMagTruncLevel, 2);
			else { // if it is a B Fault Fix
				charMFD = new GaussianMagFreqDist(min_mag, max_mag, num_mag,
						fixMag, charMagSigma, 1.0, charMagTruncLevel, 2);
				charMFD.scaleToCumRate(0, this.fixRate);		
			}
			((SummedMagFreqDist) sourceMFD).addIncrementalMagFreqDist(charMFD);
			grMFD = new GutenbergRichterMagFreqDist(min_mag, num_mag, delta_mag,
					mag_lowerGR, sourceMag, moRate*(1-fractCharVsGR), b_valueGR);
			((SummedMagFreqDist)sourceMFD).addIncrementalMagFreqDist(grMFD);
		}
		
		num_seg = segmentData.getNumSegments();
		
		// get the impled MFD for "visible" ruptures (those that are large 
		// enough that their rupture will be seen at the surface)
		visibleSourceMFD = (IncrementalMagFreqDist)sourceMFD.deepClone();
		for(int i =0; i<sourceMFD.getNum(); i++)
			visibleSourceMFD.set(i,sourceMFD.getY(i)*getProbVisible(sourceMFD.getX(i)));
		
		// get the rate of ruptures on each segment (segSourceMFD[seg])
		getSegSourceMFD();
		
		// now get the visible MFD for each segment
		visibleSegSourceMFD = new IncrementalMagFreqDist[num_seg];
		for(int s=0; s< num_seg; s++) {
			visibleSegSourceMFD[s] = (IncrementalMagFreqDist) segSourceMFD[s].deepClone();
			for(int i =0; i<sourceMFD.getNum(); i++)
				visibleSegSourceMFD[s].set(i,segSourceMFD[s].getY(i)*getProbVisible(segSourceMFD[s].getX(i)));
		}
		
		// change the info in the MFDs
		String new_info = "Source MFD\n"+sourceMFD.getInfo();
		new_info += "|n\nRescaled to:\n\n\tMoment Rate: "+(float)sourceMFD.getTotalMomentRate()+"\n\n\tNew Total Rate: "+(float)sourceMFD.getCumRate(0);
		sourceMFD.setInfo(new_info);
		
		new_info = "Visible Source MFD\n"+visibleSourceMFD.getInfo();
		new_info += "|n\nRescaled to:\n\n\tMoment Rate: "+(float)visibleSourceMFD.getTotalMomentRate()+
		"\n\n\tNew Total Rate: "+(float)visibleSourceMFD.getCumRate(0);
		visibleSourceMFD.setInfo(new_info);
		
		
		// find the total rate of ruptures for each segment
		segRate = new double[num_seg];
		segVisibleRate = new double[num_seg];
		for(int s=0; s< num_seg; s++) {
			segRate[s] = segSourceMFD[s].getTotalIncrRate();
			segVisibleRate[s] = visibleSegSourceMFD[s].getTotalIncrRate();
		}
		
		// find the slip distribution of each segment
		computeSegSlipDist();
		//if(D)
		//  for(int i=0; i<num_seg; ++i)
		//	  System.out.println("Slip for segment "+i+":  " +segSlipDist[i] +";  "+segVisibleSlipDist[i] );
	}
	
	/**
	 * This returns of magnitude computed for the characteristic earthquake 
	 * (and upper mag of the GR) if that constructor was used (no mag PDF given)
	 * @return
	 */
	public double getSourceMag() {
		return sourceMag;
	}
	
	/**
	 * Get B Fault Mag Fix (Some B faults have Mag fix which is specified in a text file)
	 * @return
	 */
	public double getFixMag() {
		return this.fixMag;
	}
	
	/**
	 * Get B Fault Rate Fix (Some B faults have Rate fix which is specified in a text file)
	 * @return
	 */
	public double getFixRate() {
		return this.fixRate;
	}
	
	/**
	 * Get fault segment data
	 * @return
	 */
	public FaultSegmentData getFaultSegmentData() {
		return this.segmentData;
	}
	
	/**
	 * This gets the magnitude frequency distribution for each segment by multiplying the original MFD
	 * by the average probability of observing each rupture on each segment (assuming ruptures have a
	 * uniform spatial distribution - or equal probability of landing anywhere).  If asiemicity reduces
	 * area, then the DDW here is effectively reduced (is this what we want?).
	 *
	 */
	private void getSegSourceMFD() {
		// get segment lengths
		double[] segLengths = new double[num_seg];
		for(int i = 0; i< num_seg; i++) segLengths[i] = 1e-3*segmentData.getSegmentLength(i); // converted to km
		double totalLength = segmentData.getTotalLength()*1e-3;
		double aveDDW = (segmentData.getTotalArea()*1e-6)/totalLength; // average Down dip width in km
		segSourceMFD = new IncrementalMagFreqDist[num_seg]; 
		for(int i=0; i<num_seg; ++i) segSourceMFD[i] = (IncrementalMagFreqDist) sourceMFD.deepClone(); 
		// loop over all magnitudes in flaoter MFD
		for (int i=0; i<sourceMFD.getNum(); ++i) {
			double mag = sourceMFD.getX(i);
			double rupLength = magAreaRel.getMedianArea(mag)/aveDDW;  // in km
			double[] segProbs = getProbSegObsRupture(segLengths, totalLength, rupLength);
			for(int j=0; j<num_seg; ++j) {
				segSourceMFD[j].set(i, segProbs[j]*segSourceMFD[j].getY(i));
			}
		}
	}
	
	/**
	 * This returns the probability of observing a rupture (of given length) 
	 * on each of the various segments (of given lengths) assuming the rupture has equal
	 * probabily of occurring anywhere along the fault (which means the probabilities 
	 * are greatest at the middle and taper toward the ends).  This is done by first
	 * getting the probability of observing the rupture at 100 points along the
	 * fault, and then averaging those probabilities in each segment.
	 * @param segLengths
	 * @param totalLength
	 * @param rupLength
	 * @return
	 */
	private double[] getProbSegObsRupture(double[] segLengths, double totalLength, double rupLength) {
		EvenlyDiscretizedFunc probFunc = new EvenlyDiscretizedFunc(0, totalLength, 100);
		// check whether rup length exceed fault length and shorten if so
		if(rupLength>totalLength) rupLength = totalLength;
		// first get the probability of observing the rupture at 100 points long the fault
		if(rupLength<totalLength/2) {
			double multFactor = rupLength/(totalLength-rupLength);  
			for(int i=0; i<probFunc.getNum(); ++i) {
				double l = probFunc.getX(i);
				double prob;
				if(l<rupLength) prob = l/rupLength*multFactor;
				else if(l<(totalLength-rupLength)) prob = multFactor;
				else prob = (totalLength-l)*multFactor/rupLength;
				probFunc.set(i, prob);
			}
		} else { //  if(rupLength>totalLength/2) {
			for(int i=0; i<probFunc.getNum(); ++i) {
				double l = probFunc.getX(i);
				double prob;
				if(l<(totalLength-rupLength)) prob = l/(totalLength-rupLength);
				else if(l<=rupLength) prob = 1;
				else prob = (totalLength-l)/(totalLength-rupLength);
				probFunc.set(i, prob);
			}
		} 
		//if (D) System.out.println("Prob Func="+probFunc.toString());
		
		// now average the probabilities for those points in each segment
		double[] segProbs = new double[segLengths.length];
		double firstLength = 0;
		double lastLength;
		for(int i=0 ; i<segLengths.length ; ++i) {
			int  index1 = (int)Math.ceil((firstLength-probFunc.getMinX())/probFunc.getDelta());
			lastLength = firstLength + segLengths[i];
			int index2 = (int) Math.floor((lastLength-probFunc.getMinX())/probFunc.getDelta());
			double total=0;
			for(int j=index1; j<=index2; ++j) total+=probFunc.getY(j);
			segProbs[i]= total/(index2-index1+1);
			firstLength=lastLength;
		}
		
		return segProbs;
	}
	
	/**
	 * This returns the probability that the given magnitude 
	 * event will be observed at the ground surface.  This is based
	 * on equation 4 of Youngs et al. [2003, A Methodology for Probabilistic Fault
	 * Displacement Hazard Analysis (PFDHA), Earthquake Spectra 19, 191-219] using 
	 * the coefficients they list in their appendix for "Data from Wills and 
	 * Coppersmith (1993) 276 worldwide earthquakes".
	 * @return
	 */
	private double getProbVisible(double mag) {
		return Math.exp(-12.51+mag*2.053)/(1.0 + Math.exp(-12.51+mag*2.053));
		/* Ray & Glenn's equation
		 if(mag <= 5) 
		 return 0.0;
		 else if (mag <= 7.6)
		 return -0.0608*mag*mag + 1.1366*mag + -4.1314;
		 else 
		 return 1.0;
		 */
	}
	
	/**
	 * Final, implied average, slip rate on the segment
	 */
	public double getFinalAveSegSlipRate(int ithSegment) {
		ArbDiscrEmpiricalDistFunc segmenstSlipDist = getSegmentSlipDist(ithSegment);
		double slipRate=0;
		for(int i=0; i<segmenstSlipDist.getNum(); ++i)
			slipRate+=segmenstSlipDist.getX(i)*segmenstSlipDist.getY(i);
		return slipRate;
	}
	
	/**
	 * Get rate for ith segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegmentRate(int ithSegment) {
		return segRate[ithSegment];
	}
	
	/**
	 * Get recurrence interval for the ith Segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegmentRecurrenceInterval(int ithSegment) {
		return 1.0/segRate[ithSegment];
	}
	
	
	/**
	 * Get visible rate for ith segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegmentVisibleRate(int ithSegment) {
		return segVisibleRate[ithSegment];
	}
	
	/**
	 * Get visible recurrence interval for the ith Segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegmentVisibleRecurrenceInterval(int ithSegment) {
		return 1.0/segVisibleRate[ithSegment];
	}
	
	/**
	 * Get Slip Distribution for this segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public ArbDiscrEmpiricalDistFunc getSegmentSlipDist(int ithSegment) {
		return segSlipDist[ithSegment];
	}
	
	/**
	 * Get Visible Slip Distribution for this segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public ArbDiscrEmpiricalDistFunc getSegmentVisibleSlipDist(int ithSegment) {
		return segVisibleSlipDist[ithSegment];
	}
	
	
	/**
	 * Get the Mag Freq Dist for the source
	 * 
	 * @return
	 */
	public IncrementalMagFreqDist getMagFreqDist() {
		return sourceMFD; 
	}
	
	/**
	 * The returns the characteristic mag freq dist if it exists (i.e., the constructor
	 * that specifies a fraction of char vs GR was used)
	 * @return
	 */
	public IncrementalMagFreqDist getCharMagFreqDist() {
		return charMFD; 
	}
	
	
	/**
	 * The returns the GR mag freq dist if it exists (i.e., the constructor
	 * that specifies a fraction of char vs GR was used)
	 * @return
	 */
	public IncrementalMagFreqDist getGR_MagFreqDist() {
		return grMFD;
	}
	
	
	/**
	 * Get the Mag Freq Dist for "visible" source ruptures
	 * 
	 * @return
	 */
	public IncrementalMagFreqDist getVisibleSourceMagFreqDist() {
		return visibleSourceMFD; 
	}
	
	/**
	 * Compute both total and visible slip distribution for each segment (segSlipDist[seg] & segVisibleSlipDist[seg])
	 */
	private void computeSegSlipDist() {
		
		segSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
		segVisibleSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
		
		for(int seg=0; seg<num_seg; ++seg) {
			segSlipDist[seg]=new ArbDiscrEmpiricalDistFunc();
			segVisibleSlipDist[seg]=new ArbDiscrEmpiricalDistFunc();
			IncrementalMagFreqDist segMFD =  segSourceMFD[seg];
			IncrementalMagFreqDist visibleSegMFD =  visibleSegSourceMFD[seg];
			for(int i=0; i<segMFD.getNum(); ++i) {
				double mag = segMFD.getX(i);
				double moment = MomentMagCalc.getMoment(mag);
				double slip = FaultMomentCalc.getSlip(magAreaRel.getMedianArea(mag)*1e6, moment);
				segSlipDist[seg].set(slip, segMFD.getY(i));
				segVisibleSlipDist[seg].set(slip, visibleSegMFD.getY(i));
			}
		}
	}
	
	
	
	/**
	 * Returns the Source Surface.
	 * @return GriddedSurfaceAPI
	 */
	public EvenlyGriddedSurfaceAPI getSourceSurface() {
		return null;
	}
	
	/**
	 * It returns a list of all the locations which make up the surface for this
	 * source.
	 *
	 * @return LocationList - List of all the locations which constitute the surface
	 * of this source
	 */
	public LocationList getAllSourceLocs() {
		LocationList locList = new LocationList();
		Iterator it = ( (EvenlyGriddedSurface) getSourceSurface()).
		getAllByRowsIterator();
		while (it.hasNext()) locList.addLocation( (Location) it.next());
		return locList;
	}
	
	
	/**
	 * This changes the duration.
	 * @param newDuration
	 */
	public void setDuration(double newDuration) {
		if (this.isPoissonian != true)
			throw new RuntimeException(C +
			" Error - the setDuration method can only be used for the Poisson case");
		ProbEqkRupture eqkRup;
		double oldProb, newProb;
		for (int i = 0; i < ruptureList.size(); i++) {
			eqkRup = (ProbEqkRupture) ruptureList.get(i);
			oldProb = eqkRup.getProbability();
			newProb = 1.0 - Math.pow( (1.0 - oldProb), newDuration / duration);
			eqkRup.setProbability(newProb);
		}
		duration = newDuration;
	}
	
	/**
	 * @return the total num of rutures for all magnitudes
	 */
	public int getNumRuptures() {
		return 0;
	}
	
	/**
	 * This method returns the nth Rupture in the list
	 */
	public ProbEqkRupture getRupture(int nthRupture) {
		return (ProbEqkRupture) ruptureList.get(nthRupture);
	}
	
	/**
	 * This returns the shortest dist to either end of the fault trace, or to the
	 * mid point of the fault trace (done also for the bottom edge of the fault).
	 * @param site
	 * @return minimum distance in km
	 */
	public double getMinDistance(Site site) {
		
		double min = Double.MAX_VALUE;
		double tempMin;
		
		Iterator it = faultCornerLocations.iterator();
		
		while (it.hasNext()) {
			tempMin = RelativeLocation.getHorzDistance(site.getLocation(),
					(Location) it.next());
			if (tempMin < min) min = tempMin;
		}
//		System.out.println(C+" minDist for source "+this.NAME+" = "+min);
		return min;
	}
	
	/**
	 * This makes the vector of fault corner location used by the getMinDistance(site)
	 * method.
	 * @param faultSurface
	 */
	private void makeFaultCornerLocs(EvenlyGriddedSurface faultSurface) {
		
		int nRows = faultSurface.getNumRows();
		int nCols = faultSurface.getNumCols();
		faultCornerLocations.add(faultSurface.get(0, 0));
		faultCornerLocations.add(faultSurface.get(0, (int) (nCols / 2)));
		faultCornerLocations.add(faultSurface.get(0, nCols - 1));
		faultCornerLocations.add(faultSurface.get(nRows - 1, 0));
		faultCornerLocations.add(faultSurface.get(nRows - 1, (int) (nCols / 2)));
		faultCornerLocations.add(faultSurface.get(nRows - 1, nCols - 1));
		
	}
	
	/**
	 * set the name of this class
	 *
	 * @return
	 */
	public void setName(String name) {
		NAME = name;
	}
	
	/**
	 * get the name of this class
	 *
	 * @return
	 */
	public String getName() {
		return NAME;
	}
	
	
	
	public static void main(String[] args) {
		
		
	}
}

