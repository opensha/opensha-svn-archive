package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;

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
 * <p>Title: PEER_FaultSource </p>
 * <p>Description: This implements a basic fault source for arbitrary inputs.  Note that
 * magnitudes below 5.0 are ignored in creating the ProbEqkRuptures.  This object is quite
 * general.  The only customized feature is the Mag-length relationship specified for the
 * test cases, and how to handle it's non-zero uncertaintiy.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin & Vipin Gupta, and Ned Field
 * @date Oct 23, 2002
 * @version 1.0
 */

public class PEER_FaultSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("PEER Fault Source");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = C;

  // private variables
  private double rake;
  private double timeSpan;
  private double rupOffset;
  private double magLenSigma;
  private int totNumRups;
  private EvenlyGriddedSurface surface;
  private IncrementalMagFreqDist magDist;

  // list of ruptures
  private Vector ruptureList;

  /**
   *
   * @param magDist - any incremental mag. freq. dist. object
   * @param rake - average rake of the fault
   * @param offsetSpacing - amount of offset for floating ruptures
   * @param faultSurface - EvenlyGriddedSurface representation of the fault surface
   * @param timeSpan - the timeSpan of interest in years (this is a Poissonian source)
   * @param magLenSigme - uncertainty of the magnitude-length relationship
   */
  public PEER_FaultSource(IncrementalMagFreqDist magDist,double rake,
                           double offsetSpacing,
                           EvenlyGriddedSurface faultSurface, double timeSpan,
                           double magLenSigma) {

    this.rake=rake;
    this.surface=faultSurface;
    this.rupOffset=offsetSpacing;
    this.magDist = magDist;
    this.timeSpan = timeSpan;
    this.magLenSigma = magLenSigma;

    mkRuptureList();

  }


  /**
   * This method makes the list of rupture for this souce
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

    if( D ) System.out.println("PEER_Fault_Source: magLenSigma="+magLenSigma);


    // The magLenSigma=0 case:
    if(magLenSigma == 0.0) {
        for(int i=0;i<numMags;++i){
            // get the magnitude
            mag = magDist.getX(i);

            // make sure it has a non-zero rate & the mag is >= 5.0
            if(magDist.getY(i) > 0 && mag >= 5.0) {
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
              }
              if( D ) System.out.println("PEER_FaultSource: mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                          "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
            }
        }
    }


    /* if magLenSigma > 0 case:

       The branch-tip weights (0.6, 0.2, and 0.2) for the mean, -1.64sigma, and +1.64sigma
       (respectively) are from WG99's Table 1.1
    */
    else {
        // the mean case
        for(int i=0;i<numMags;++i){
            mag = magDist.getX(i);
            // make sure it has a non-zero rate & the mag is >= 5.0
            if(magDist.getY(i) > 0 && mag >= 5.0) {
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
                    prob = 0.6* (1- Math.exp(-timeSpan*rate/numRup));
                    probEqkRupture.setProbability(prob);
                    ruptureList.add(probEqkRupture);
              }
              if( D ) System.out.println("PEER_FaultSource: mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                          "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
            }
        }
        // the mean-1.64sigma case
        for(int i=0;i<numMags;++i){
            mag = magDist.getX(i);
            // make sure it has a non-zero rate & the mag is >= 5.0
            if(magDist.getY(i) > 0 && mag >= 5.0) {
              rupLen = Math.pow(10,mag/2-1.85-1.64*magLenSigma);
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
                    prob = 0.2* (1- Math.exp(-timeSpan*rate/numRup));
                    probEqkRupture.setProbability(prob);
                    ruptureList.add(probEqkRupture);
              }
              if( D ) System.out.println("PEER_FaultSource: mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                          "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
            }
        }
        // the mean+1.64sigma case
        for(int i=0;i<numMags;++i){
            mag = magDist.getX(i);
            // make sure it has a non-zero rate & the mag is >= 5.0
            if(magDist.getY(i) > 0 && mag >= 5.0) {
              rupLen = Math.pow(10,mag/2-1.85+1.64*magLenSigma);
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
                    prob = 0.2* (1- Math.exp(-timeSpan*rate/numRup));
                    probEqkRupture.setProbability(prob);
                    ruptureList.add(probEqkRupture);
              }
              if( D ) System.out.println("PEER_FaultSource: mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                          "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
            }
        }
    }



    if( D ) System.out.println("PEER_FaultSource:totNumRups:"+ruptureList.size());
  }

  /**
   * @return the total num of rutures for all magnitudes
   */
  public int getNumRuptures() { return ruptureList.size(); }


  /**
   * This method returns the nth Rupture in the list
   */
  public ProbEqkRupture getRupture(int nthRupture){ return (ProbEqkRupture) ruptureList.get(nthRupture); }



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