package org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases;

import java.util.Vector;
import java.util.Iterator;

import org.scec.calc.magScalingRelations.MagAreaRelationship;
import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.sha.magdist.*;
import org.scec.sha.magdist.SingleMagFreqDist;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;
import org.scec.sha.surface.*;


/**
 * <p>Title: SimplePoissonFaultSource </p>
 * <p>Description: This implements a basic fault source for arbitrary inputs.  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class SimplePoissonFaultSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("SimplePoissonFaultSource");
  private boolean D = false;

  //name for this classs
  public final static String  NAME = "Simple Poisson Fault Source";

  // private fields
  private int totNumRups;
  private Vector ruptureList;
  private Vector faultCornerLocations;   // used for the getMinDistance(Site) method
  private double timeSpan;

  /**
   *
   * @param magDist - any incremental mag. freq. dist. object
   * @param rake - average rake of the fault
   * @param offsetSpacing - amount of offset for floating ruptures
   * @param faultSurface - EvenlyGriddedSurface representation of the fault surface
   * @param timeSpan - the timeSpan of interest in years (this is a Poissonian source)
   * @param magLenSigme - uncertainty of the magnitude-length relationship
   */
  public SimplePoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagAreaRelationship magAreaRel,
                                  double magLenSigma,
                                  double rupOffset,
                                  double rake,
                                  double timeSpan) {

      this.timeSpan = timeSpan;

      // get the fault corner locations (for the getMinDistance(site) method)
      int nRows = faultSurface.getNumRows();
      int nCols = faultSurface.getNumCols();
      faultCornerLocations.add(faultSurface.get(0,0));
      faultCornerLocations.add(faultSurface.get(0,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(0,nCols));
      faultCornerLocations.add(faultSurface.get(nRows,0));
      faultCornerLocations.add(faultSurface.get(nRows,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(nRows,nCols));

      // make the list of ruptures
      mkRuptureList(magDist, faultSurface, magAreaRel, magLenSigma, rupOffset, rake);
  }


  /**
   * This method makes the list of rupture for this souce
   */
  private void mkRuptureList(IncrementalMagFreqDist magDist,
                             EvenlyGriddedSurface faultSurface,
                             MagAreaRelationship magAreaRel,
                             double magLenSigma,
                             double rupOffset,
                             double rake) {

    ruptureList = new Vector();

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!

    double rupLen;
    double rupWidth;
    double numRup;
    double mag;
    double rate;
    double prob=Double.NaN;

    if( D ) System.out.println(C+": magLenSigma="+magLenSigma);

    // The magLenSigma=0 case:
    if(magLenSigma == 0.0) {
        for(int i=0;i<numMags;++i){
            // get the magnitude
            mag = magDist.getX(i);

            // make sure it has a non-zero rate & the mag is >= 5.0
            if(magDist.getY(i) > 0 && mag >= 5.0) {
              rupLen = Math.pow(10,mag/2-1.85);
              rupWidth= rupLen/2;
              numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
              rate = magDist.getY(mag);
              // Create the ruptures and add to the list
              for(int r=0; r < numRup; ++r) {
                probEqkRupture = new ProbEqkRupture();
                probEqkRupture.setAveRake(rake);
                // set rupture surface
                probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
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
              numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
              rate = magDist.getY(mag);
              // Create the ruptures and add to the list
              for(int r=0; r < numRup; ++r) {
                    probEqkRupture = new ProbEqkRupture();
                    probEqkRupture.setAveRake(rake);
                    // set rupture surface
                    probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
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
              numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
              rate = magDist.getY(mag);
              // Create the ruptures and add to the list
              for(int r=0; r < numRup; ++r) {
                    probEqkRupture = new ProbEqkRupture();
                    probEqkRupture.setAveRake(rake);
                    // set rupture surface
                    probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
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
              numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
              rate = magDist.getY(mag);
              // Create the ruptures and add to the list
              for(int r=0; r < numRup; ++r) {
                    probEqkRupture = new ProbEqkRupture();
                    probEqkRupture.setAveRake(rake);
                    // set rupture surface
                    probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
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
   * mid point of the fault trace (done also for the bottom edge of the fault).
   * @param site
   * @return minimum distance in km
   */
   public  double getMinDistance(Site site) {

      double min = Double.MAX_VALUE;
      double tempMin;

      Iterator it = this.faultCornerLocations.iterator();

      while(it.hasNext()) {
        tempMin = RelativeLocation.getHorzDistance(site.getLocation(),(Location)it.next());
        if(tempMin < min) min = tempMin;
      }

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