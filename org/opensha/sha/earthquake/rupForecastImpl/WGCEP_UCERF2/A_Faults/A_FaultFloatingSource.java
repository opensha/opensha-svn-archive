package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.data.*;
import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.calc.*;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.surface.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;


/**
 * <p>Title: WG_A_FaultFloatingSource </p>
 * <p>Description: 
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class A_FaultFloatingSource extends ProbEqkSource {
	
	//for Debug purposes
	private static String C = new String("A_FaultFloatingSource");
	private final static boolean D = true;
	
	//name for this classs
	protected String NAME = "Type-A Fault Floating Source";
	
	protected double duration;
	
	private ArrayList ruptureList; // keep this in case we add more mags later
	private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
	
	private int num_seg;
	private double[] segRate, segVisibleRate; // segment rates 
	private double[] segAveSlipRate; // ave slip rate for segment
	private ArbDiscrEmpiricalDistFunc[] segSlipDist;  // segment slip dist
	
	private IncrementalMagFreqDist floaterMFD; // Mag Freq dist for floater
	private IncrementalMagFreqDist visibleFloaterMFD; // Mag Freq dist for visible ruptures
	IncrementalMagFreqDist[] segFloaterMFD;  // Mag Freq Dist for each segment
	IncrementalMagFreqDist[] visibleSegFloaterMFD;  // Mag Freq Dist for visible ruptures on each segment


	
	SegmentedFaultData segmentData;
	MagAreaRelationship magAreaRel;
	IncrementalMagFreqDist floatingRup_PDF;
	
	
	/**
	 * Description:
	 * 
	 */
	public A_FaultFloatingSource(SegmentedFaultData segmentData, MagAreaRelationship magAreaRel, 
			IncrementalMagFreqDist floatingRup_PDF) {
		
		this.isPoissonian = true;
		
		this.segmentData = segmentData;
		this.magAreaRel = magAreaRel;
		this.floatingRup_PDF = floatingRup_PDF;
		
		num_seg = segmentData.getNumSegments();
		
		// get the floater MFD
		floaterMFD = (IncrementalMagFreqDist)floatingRup_PDF.deepClone();
		double floaterMoRate = segmentData.getTotalMomentRate();
		floaterMFD.scaleToTotalMomentRate(floaterMoRate);
		// change the info
		String new_info = floaterMFD.getInfo();
		new_info += "\n\nMoment Rate: "+(float)floaterMoRate+"\n\nNew Total Rate: "+(float)floaterMFD.getCumRate(0);
		floaterMFD.setInfo(new_info);
		
		// get the impled MFD for "visible" ruptures (those that are large 
		// enough that their rupture will be seen at the surface)
		visibleFloaterMFD = (IncrementalMagFreqDist)floaterMFD.deepClone();
		for(int i =0; i<floaterMFD.getNum(); i++)
			visibleFloaterMFD.set(i,floaterMFD.getY(i)*getProbVisible(floaterMFD.getX(i)));
		
		// get the rate of floaters on each segment (segFloaterMFD[seg])
		getSegFloaterMFD();
		
		// now get the visible MFD for each segment
		for(int s=0; s< num_seg; s++) {
			visibleSegFloaterMFD[s] = (IncrementalMagFreqDist) segFloaterMFD[s].deepClone();
			for(int i =0; i<floaterMFD.getNum(); i++)
				visibleSegFloaterMFD[s].set(i,segFloaterMFD[s].getY(i)*getProbVisible(segFloaterMFD[s].getX(i)));
		}
		
		// find the total rate of ruptures for each segment
		segRate = new double[num_seg];
		segVisibleRate = new double[num_seg];
		computeSegRate();
		
		// find the slip distribution of each rupture & segment
		ArbitrarilyDiscretizedFunc[] rupSlipDist = new ArbitrarilyDiscretizedFunc[num_rup];
		computeRupSlipDist( rupArea, rupSlipDist);
		
		
		// find the slip distribution of each segment
		segSlipDist = new ArbDiscrEmpiricalDistFunc[num_seg];
		computeSegSlipDist(rupSlipDist, segFloaterMFD, magAreaRel, segRupSlipFactor);
		/* if(D) {
		 // print the slip distribution of each segment
		  for(int i=0; i<num_seg; ++i) {
		  System.out.println("Slip for segment "+i+":");
		  System.out.println(segSlipDist[i]);
		  }
		  }*/
	}
	
	
	private void getSegFloaterMFD() {
		// get segment lengths
		double[] segLengths = new double[num_seg];
		for(int i = 0; i< num_seg; i++) segLengths[i] = 1e-3*segmentData.getSegmentLength(i); // converted to km
		double totalLength = segmentData.getTotalLength();
		double aveDDW = 1e-3*segmentData.getTotalArea()/totalLength; // average Down dip width in km
		IncrementalMagFreqDist[] segFloaterMFD = new IncrementalMagFreqDist[num_seg]; 
		for(int i=0; i<num_seg; ++i) segFloaterMFD[i] = (IncrementalMagFreqDist) floaterMFD.deepClone(); 
		// loop over all magnitudes in flaoter MFD
		for (int i=0; i<floaterMFD.getNum(); ++i) {
			double mag = floaterMFD.getX(i);
			double rupLength = magAreaRel.getMedianArea(mag)/aveDDW;  // in km
			double[] segProbs = getProbSegObsFloater(segLengths, totalLength, rupLength);
			for(int j=0; j<num_seg; ++j) {
				segFloaterMFD[j].set(i, segProbs[j]*segFloaterMFD[j].getY(i));
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
	private double[] getProbSegObsFloater(double[] segLengths, double totalLength, double rupLength) {
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
	 * event will be observed at the ground surface.
	 * @return
	 */
	private double getProbVisible(double mag) {
		return 1.0;
	}
	
	/**
	 * Final ave segment slip rate
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
		return this.segRate[ithSegment];
	}
	
	/**
	 * Get recurrence interval for the ith Segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public double getSegmentRecurrenceInterval(int ithSegment) {
		return 1.0/this.segRate[ithSegment];
	}
	
	/**
	 * Get Slip Distribution for this segment
	 * 
	 * @param ithSegment
	 * @return
	 */
	public ArbDiscrEmpiricalDistFunc getSegmentSlipDist(int ithSegment) {
		return this.segSlipDist[ithSegment];
	}
	
	
	/**
	 * Get the Mag Freq Dist for floater ruptures
	 * 
	 * @return
	 */
	public IncrementalMagFreqDist getFloaterMagFreqDist() {
		return this.floaterMFD; 
	}
	
	/**
	 * Compute the slip distribution for each segment
	 * The average slip for each event is partitioned among the different segments
	 * according to segRupSlipFactor.
	 
	 * 
	 * @param rupSlipDist
	 * @param segSlipDist
	 */
	private void computeSegSlipDist(ArbitrarilyDiscretizedFunc[] rupSlipDist, 
			IncrementalMagFreqDist[] segFloaterMFD, MagAreaRelationship magAreaRel,
			double[][] segRupSlipFactor) {
		for(int seg=0; seg<num_seg; ++seg) {
			segSlipDist[seg]=new ArbDiscrEmpiricalDistFunc();
			// Add the rates of all ruptures which are part of a segment
			for(int rup=0; rup<num_rup; rup++)
				if(rupInSeg[rup][seg]==1) {
					for(int i=0; i<rupSlipDist[rup].getNum(); ++i)
						segSlipDist[seg].set(segRupSlipFactor[rup][seg]*rupSlipDist[rup].getX(i), 
								rupSlipDist[rup].getY(i));
				}
			if(segFloaterMFD!=null) {
				IncrementalMagFreqDist segFloater =  segFloaterMFD[seg];
				for(int i=0; i<segFloater.getNum(); ++i) {
					double mag = segFloater.getX(i);
					double moment = MomentMagCalc.getMoment(mag);
					double slip = FaultMomentCalc.getSlip(magAreaRel.getMedianArea(mag)*KM_TO_METERS_CONVERT, moment);
					segSlipDist[seg].set(slip, segFloater.getY(i));
				}
			}
		}
	}
	
	
	
	/**
	 * Compute the rate for all segments.
	 *  
	 * @param segRate
	 * @param totRupRate
	 */
	private void computeSegRate( double[] totRupRate, IncrementalMagFreqDist[] segFloaterMFD) {
		for(int seg=0; seg<num_seg; ++seg) {
			segRate[seg]=0.0;
			// Sum the rates of all ruptures which are part of a segment
			for(int rup=0; rup<num_rup; rup++) 
				if(rupInSeg[rup][seg]==1) segRate[seg]+=totRupRate[rup];
			if(segFloaterMFD!=null)
				segRate[seg]+=segFloaterMFD[seg].getTotalIncrRate();
		}
	}
	
	
	/**
	 * re-sample magFreqDist 
	 * This takes the rate at each old magnitude and adds it to the 
	 * rate at the nearest new magnitude. This could be put in the MagFreqDist class.
	 * @param magFreqDist
	 * @return
	 */
	private IncrementalMagFreqDist getReSampledMFD(IncrementalMagFreqDist magFreqDist) {
		IncrementalMagFreqDist newMFD = new IncrementalMagFreqDist(MIN_MAG, NUM_MAG, DELTA_MAG);
		double magLower = newMFD.getMinX();
		double magUpper = newMFD.getMaxX();
		for(int i=0; i<magFreqDist.getNum(); ++i) {
			double mag = magFreqDist.getX(i);  //get the magnitude
			if(mag >= magLower && mag <= magUpper) {
				// find the nearest mag in the new distribution
				int j = Math.round((float)((mag-MIN_MAG)/DELTA_MAG));
				// add the rate (rather than replace) in case more than one old mag runds to a new mag
				newMFD.set(j,newMFD.getY(j)+magFreqDist.getY(i));
			}
		}
		/*
		 EvenlyDiscretizedFunc cumDist = magFreqDist.getCumRateDist();
		 for(int i=0; i<newMFD.getNum(); ++i) {
		 double x = newMFD.getX(i);
		 double x1 = x-DELTA_MAG/2;
		 double x2 = x + DELTA_MAG/2;
		 double cumRate1=0, cumRate2=0;
		 //check that the Mag lies within the range of cumDist
		  if(x1<cumDist.getMinX()) cumRate1 = 0.0;
		  else if(x1>cumDist.getMaxX()) cumRate1 = cumDist.getMaxY() ;
		  else cumRate1 = cumDist.getInterpolatedY(x1);
		  if(x2<cumDist.getMinX()) cumRate2 = 0.0;
		  else if(x2>cumDist.getMaxX()) cumRate2 = cumDist.getMaxY();
		  else cumRate2 = cumDist.getInterpolatedY(x2);
		  newMFD.set(i, cumRate2-cumRate1);
		  }
		  */
		newMFD.scaleToTotalMomentRate(magFreqDist.getTotalMomentRate());
		return newMFD;
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
		return num_rup;
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
		
		/*
		 FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
		 FaultSectionPrefData santaCruz  = faultSectionDAO.getFaultSection(56).getFaultSectionPrefData(); // San Andreas - Santa Cruz
		 FaultSectionPrefData peninsula  = faultSectionDAO.getFaultSection(67).getFaultSectionPrefData(); // San Andreas - Peninsula
		 FaultSectionPrefData northCoastSouth  = faultSectionDAO.getFaultSection(27).getFaultSectionPrefData(); // San Andreas - North Coast South
		 FaultSectionPrefData northCoastNorth  = faultSectionDAO.getFaultSection(26).getFaultSectionPrefData(); // San Andreas - North Coast North
		 if(D) System.out.println("After retrieving fault sections from database");
		 // segment1
		  ArrayList santaCruzList = new ArrayList();
		  santaCruzList.add(santaCruz);
		  
		  //segment2
		   ArrayList peninsulaList = new ArrayList();
		   peninsulaList.add(peninsula);
		   
		   //segment3
		    ArrayList northCoastSouthList = new ArrayList();
		    northCoastSouthList.add(northCoastSouth);
		    
		    //segment4
		     ArrayList northCoastNorthList = new ArrayList();
		     northCoastNorthList.add(northCoastNorth);
		     
		     // list of all segments
		      ArrayList segmentData = new ArrayList();
		      segmentData.add(santaCruzList);
		      segmentData.add(peninsulaList);
		      segmentData.add(northCoastSouthList);
		      segmentData.add(northCoastNorthList);
		      
		      
		      double[] scenarioWts = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2};
		      GutenbergRichterMagFreqDist grMagFreqDist = new GutenbergRichterMagFreqDist(1, 1.0, 6, 8, 21);
		      WG_02FaultSource faultSource = new WG_02FaultSource(segmentData,  new WC1994_MagAreaRelationship(), 
		      0.12, 2.0, 2, scenarioWts, true, grMagFreqDist);
		      
		      */
	}
}

