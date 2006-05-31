package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2.A_Faults;

import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.sha.surface.EvenlyGriddedSurface;
import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.surface.*;
import org.opensha.sha.magdist.*;
import org.opensha.calc.magScalingRelations.MagAreaRelationship;

/**
 * <p>Title: A_FaultSource </p>
 * <p>Description: 
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class A_FaultSource
    extends ProbEqkSource {

  //for Debug purposes
  private static String C = new String("A_FaultSource");
  private boolean D = false;

  //name for this classs
  protected String NAME = "Type-A Fault Source";

  protected double duration;

  private ArrayList ruptureList; // keep this in case we add more mags later
  private ArrayList faultCornerLocations = new ArrayList(); // used for the getMinDistance(Site) method
  
  private int num_seg, num_rup, num_scen;
  
  // array giving which seg (row) is in each rupture (column)
  final static int[][] segInRup = {
  			{1,0,1,0,0,1,0,0,0,1,0,0,0,0,1}, // seg 1
  			{0,1,1,0,1,1,0,0,1,1,0,0,0,1,1}, // seg 2
  			{0,0,0,1,1,1,0,1,1,1,0,0,1,1,1}, // seg 3
  			{0,0,0,0,0,0,1,1,1,1,0,1,1,1,1}, // seg 4
  			{0,0,0,0,0,0,0,0,0,0,1,1,1,1,1}};// seg 5
  
  	// array giving which scen (row) has each rupture (column)
  final static int[][] scenHasRup = {
		//   1,2,3,4,5,6,7,8,9,0,1,2,3,4,5
			{1,1,0,1,0,0,1,0,0,0,1,0,0,0,0}, // scen 1
			{0,0,1,1,0,0,1,0,0,0,1,0,0,0,0}, // scen 2
			{1,0,0,0,1,0,1,0,0,0,1,0,0,0,0}, // scen 3
			{0,0,0,0,0,1,1,0,0,0,1,0,0,0,0}, // scen 4
			{1,1,0,0,0,0,0,1,0,0,1,0,0,0,0}, // scen 5
			{0,0,1,0,0,0,0,1,0,0,1,0,0,0,0}, // scen 6
			{1,0,0,0,0,0,0,0,1,0,1,0,0,0,0}, // scen 7
			{0,0,0,0,0,0,0,0,0,1,1,0,0,0,0}, // scen 8
			{1,1,0,1,0,0,0,0,0,0,0,1,0,0,0}, // scen 9
			{0,0,1,1,0,0,0,0,0,0,0,1,0,0,0}, // scen 10
			{1,0,0,0,1,0,0,0,0,0,0,1,0,0,0}, // scen 11
			{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0}, // scen 12
			{1,1,0,0,0,0,0,0,0,0,0,0,1,0,0}, // scen 13
			{0,0,1,0,0,0,0,0,0,0,0,0,1,0,0}, // scen 14
			{1,0,0,0,0,0,0,0,0,0,0,0,0,1,0}, // scen 15
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}};// scen 16


  /**
   * Constructor 
   */
  public A_FaultSource(ArrayList segmentData, MagAreaRelationship magAreaRel, double[] scenarioWts,
		  boolean aseisReducesArea, IncrementalMagFreqDist floatingRup_PDF) {

    this.isPoissonian = true;
    
    // make sure scenarioWts sum to 1.
    // the last element in scenarioWts applies to floater

    if (D) {
      System.out.println("mag: ");
    }
    
    num_seg = segmentData.size();
    num_rup = num_seg*(num_seg+1)/2;
    num_scen = (int) Math.pow(2,num_rup-1);
    
    if(num_seg > 5 || num_seg < 2)
		throw new RuntimeException("Error: num segments must be between 2 and 5");
    if(num_scen+1 != scenarioWts.length)  // the plus 1 is for the floater
		throw new RuntimeException("Error: number of segments incompatible with number of scenarioWts");
    
    int seg,rup,scen; // for loops
   
    double[] segArea = new double[num_seg];
    double[] segMoRate = new double[num_seg];
    
    // fill in the above two
    
    double totalMoRate = 0;
    for(seg=0; seg<num_seg; seg++)  totalMoRate += segMoRate[seg];
    
    // get the moRate for the floater using the last element in the scenarioWts array.
    double floaterWt = scenarioWts[num_scen];
    double floaterMoRate = totalMoRate*floaterWt;
    
    double[] rupArea = new double[num_rup];
    double[] rupMag = new double[num_rup];
    double[] rupMaxMoRate = new double[num_rup]; // if all moment rate could go into this rupture
    double[] rupMoRate = new double[num_rup];
    
  	// compute rupArea, rupMaxMoRate, and rupMag for each rupture
    for(rup=0; rup<num_rup; rup++){
    		rupArea[rup] = 0;
    		rupMaxMoRate[rup] = 0;
    		for(seg=0; seg < num_seg; seg++) {
    			rupArea[rup] += segArea[seg]*(double)segInRup[seg][rup];
        		rupMaxMoRate[rup] += segMoRate[seg]*(double)segInRup[seg][rup];
    		}
    		rupMag[rup] = magAreaRel.getMedianMag(rupArea[rup]);
    }
    
    // compute the actual rupture MoRate (considering floater weight as well)
    double totMoRateTest = 0;
    for(rup=0; rup<num_rup; rup++){
		rupMoRate[rup] = 0;
		for(scen=0; scen < num_scen; scen++) {
			rupMoRate[rup] += (1.0-floaterWt)*rupMaxMoRate[rup]*(double)scenHasRup[scen][rup]*scenarioWts[scen];
		}
		totMoRateTest += rupMoRate[rup];
    }

    // check total moment rates
    totMoRateTest += floaterMoRate;
    if(D) {
    		System.out.println("TotMoRate from segs = "+(float) totalMoRate);
    		System.out.println("TotMoRate from ruptures = "+(float) totMoRateTest);
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
    return ruptureList.size();
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
//System.out.println(C+" minDist for source "+this.NAME+" = "+min);
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
}

