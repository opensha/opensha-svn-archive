package org.scec.sha.earthquake.PEER_test_cases;

import java.util.Vector;
import java.util.Iterator;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.magdist.*;
import org.scec.sha.magdist.SingleMagFreqDist;
import org.scec.sha.calc.WC1994_MagLengthRelationship;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;


/**
 * <p>Title: Set1_Fault_Source </p>
 * <p>Description: Fault-1 earthquake source</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 23, 2002
 * @version 1.0
 */

public class Set1_Fault_Source extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("Set1_Fault_Source");
  private boolean D = true;

  private double rake;
  private double timeSpan;
  //these are the defined varible storing the rupture offset.
  private double rupOffset;
  private int totNumRups;
  private EvenlyGriddedSurface surface;
  private IncrementalMagFreqDist magDist;

  /**
   *
   * @param magDist= It the object of the selected MagDist class
   * @param rake
   * @param offsetSpacing
   * @param rupSurface := Rupture Surface
   */
  public Set1_Fault_Source(IncrementalMagFreqDist magDist,double rake,
                           double offsetSpacing,
                           EvenlyGriddedSurface rupSurface) {

    this.rake=rake;
    this.surface=rupSurface;
    this.rupOffset=offsetSpacing;
    this.magDist = magDist;
    probEqkRupture = new ProbEqkRupture();
    probEqkRupture.setAveRake(rake);

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!
    totNumRups=0;

    for(int i=0;i<numMags;++i){
      if(magDist.getY(i) > 0) {
        double rupLen = Math.pow(10,magDist.getX(i)/2-1.85);
        double rupWidth= rupLen/2;
        if( D ) System.out.println("Set1_Fault_Source:Set1_Fault_Source:mag="+magDist.getX(i)+"; rupLen="+rupLen+"; rupWidth="+rupWidth);
        totNumRups += getNumRuptures(rupLen,rupWidth);
      }
    }
    if( D ) System.out.println("Set1_Fault_Source:Set1_Fault_Source:totNumRups::"+totNumRups);

  }

  /**
   * this functions sums up all the ruptures for all magnitudes
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return totNumRups; }


  /**
   * This method sets the probability of the different rupture surface for different mag
   * @param nthRupture : it is to find the mag and rate to which that rupture number correspond
   * @return the object of the ProbEqkRupture class after setting the probability
   */
  public ProbEqkRupture getRupture(int nthRupture){
    int numMags = magDist.getNum();  // some of these may have zero rates
    double mag=0, rupLen=0,rupWidth=0;
    int numRups=0, tempNumRups=0;

    if(nthRupture < 0 || nthRupture>=getNumRuptures())
       throw new RuntimeException(C+":getRupture():: Invalid rupture index. This index does not exist");

    // this finds the magnitude:
    for(int i=0;i<numMags;++i){
      if(magDist.getY(i) > 0) {
        mag=magDist.getX(i);
        rupLen = Math.pow(10,mag/2-1.85);
        rupWidth = rupLen/2;
        numRups = getNumRuptures(rupLen,rupWidth);
        tempNumRups += numRups;
        if(nthRupture < tempNumRups)
          break;
      }
    }

    probEqkRupture.setMag(mag);

    // set probability
    double rate = magDist.getY(mag);
    double prob = 1- Math.exp(-timeSpan*rate/numRups);
    probEqkRupture.setProbability(prob);

    // set rupture surface
    probEqkRupture.setRuptureSurface( surface.getNthSubsetSurface(rupLen,
                                      rupWidth,rupOffset,
                                      nthRupture+numRups-tempNumRups));

    return probEqkRupture;
  }


  /** Set the time span in years
   *
   * @param yrs : timeSpan as specified in  Number of years
   */
  public void setTimeSpan(double yrs) {
   //set the time span in yrs
    timeSpan = yrs;
  }



  /** Set the time span in years
   * FIX Mehthod not implemented yet
   *
   * @param yrs : timeSpan
   *
   */
  public void setTimeSpan(TimeSpan timeSpan) {

     // set the probability according to the specifed timespan
    throw new UnsupportedOperationException(C+"setTimeSpan(timeSpan) Not implemented.");
  }


 /**
  * Returns the Vector consisting of all ruptures for this source
  * all the objects are cloned. so this vector can be saved by the user
  *
  * @return Vector consisting of
  */
  public Vector getRuptureList(){
    Vector v= new Vector();
    for(int i=0;i<totNumRups;++i)
      v.add(this.getRuptureClone(i));
    return v;
  }


  /**
   * @param mag
   * @return the total number of ruptures associated with the given mag
   */
  private int getNumRuptures(double rupLen,double rupWidth){
    return surface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
 }


   /**
   * This returns the shortest dist to either end of the fault trace, or to the
   * mid point of the fault trace.
   * @param site
   * @return minimum distance
   */
   public  double getMinDistance(Site site) {

      double min;

      // get first location on fault trace
      Direction dir = RelativeLocation.getDirection(site.getLocation(), (Location) surface.get(0,0));
      min = dir.getHorzDistance();

      // get last location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(),(Location) surface.get(0,surface.getNumCols()-1));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      // get mid location on fault trace
      dir = RelativeLocation.getDirection(site.getLocation(),(Location) surface.get(0,(int) surface.getNumCols()/2));
      if (min > dir.getHorzDistance())
          min = dir.getHorzDistance();

      return min;
    }

 /**
  * get the name of this class
  *
  * @return
  */
 public String getName() {
   return C;
  }
}