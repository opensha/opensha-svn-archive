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
import org.scec.sha.surface.*;


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

  // list of ruptures
  private Vector ruptureList;

  /**
   *
   * @param magDist= It the object of the selected MagDist class
   * @param rake
   * @param offsetSpacing
   * @param faultSurface := Fault Surface
   */
  public Set1_Fault_Source(IncrementalMagFreqDist magDist,double rake,
                           double offsetSpacing,
                           EvenlyGriddedSurface faultSurface, double timeSpan) {

    this.rake=rake;
    this.surface=faultSurface;
    this.rupOffset=offsetSpacing;
    this.magDist = magDist;
    this.timeSpan = timeSpan;

    mkRuptureList();

  }


  /**
   * This method makes the rupture list
   */
  private void mkRuptureList() {

    ruptureList = new Vector();

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!

    double rupLen;
    double rupWidth;
    double numRup;
    double mag;
    double rate;
    double prob=Double.NaN;

    for(int i=0;i<numMags;++i){
      // make sure it has a non-zero rate
      if(magDist.getY(i) > 0) {
        mag = magDist.getX(i);
        rupLen = Math.pow(10,mag/2-1.85);
        rupWidth= rupLen/2;
        numRup = surface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
        rate = magDist.getY(mag);
        // Create the ruptures and add to the list
        for(int r=0; r < numRup; ++r) {
            probEqkRupture = new ProbEqkRupture();
            probEqkRupture.setAveRake(rake);
            // set rupture surface
            probEqkRupture.setRuptureSurface(surface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
            probEqkRupture.setMag(mag);
            prob = 1- Math.exp(-timeSpan*rate/numRup);
            probEqkRupture.setProbability(prob);
            ruptureList.add(probEqkRupture);

            GriddedSurfaceAPI temp = probEqkRupture.getRuptureSurface();
            Location tempLoc = temp.getLocation(0,0);
            if( D ) System.out.println("Location(0,0): rup: "+r+"  "+tempLoc.getLatitude()+"  "+tempLoc.getLongitude()+"  "+tempLoc.getDepth());
        }
        if( D ) System.out.println("Set1_Fault_Source: mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+"; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
      }
    }
    if( D ) System.out.println("Set1_Fault_Source:totNumRups:"+ruptureList.size());
  }

  /**
   * this functions sums up all the ruptures for all magnitudes
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return ruptureList.size(); }


  /**
   * This method sets the probability of the different rupture surface for different mag
   * @param nthRupture : it is to find the mag and rate to which that rupture number correspond
   * @return the object of the ProbEqkRupture class after setting the probability
   */
  public ProbEqkRupture getRupture(int nthRupture){ return (ProbEqkRupture) ruptureList.get(nthRupture); }

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