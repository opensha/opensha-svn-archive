package org.scec.sha.earthquake;

import java.util.Vector;
import java.util.Iterator;

import org.scec.calc.magScalingRelations.*;
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
  protected String  NAME = C;

  // private fields
  private int totNumRups;
  private Vector ruptureList;
  private Vector faultCornerLocations;   // used for the getMinDistance(Site) method
  private double timeSpan;

  /* Note that none of the input objects are saved after the ruptureList is created
     by the constructor.  This is deliberate to save memory.  A setName() method was
     added here to enable users to give a unique identifier once created.
  */

  /**
   * This creates a Simple Poisson Fault Source using a minMag of 5.0 (magnitudes
   * lower than this are ignored in building the ruptures).
   * @param magDist - any incremental mag. freq. dist. object
   * @param faultSurface - any EvenlyGriddedSurface representation of the fault
   * @param magScalingRel - any magAreaRelationship or magLengthRelationthip
   * @param magScalingSigma - uncertainty of the length(mag) or area(mag) relationship
   * @param rupAspectRatio - ratio of rupture length to rupture width
   * @param rupOffset - amount of offset for floating ruptures
   * @param rake - average rake of the ruptures
   * @param timeSpan - the timeSpan of interest in years (this is a Poissonian source)
   */
  public SimplePoissonFaultSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface faultSurface,
                                  MagScalingRelationship magScalingRel,
                                  double magScalingSigma,
                                  double rupAspectRatio,
                                  double rupOffset,
                                  double rake,
                                  double timeSpan) {

      this.timeSpan = timeSpan;

      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(faultSurface);

      // make the rupture list
      ruptureList = new Vector();
      if(magScalingSigma == 0.0)
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma, rupAspectRatio, rupOffset, rake, 5.0, 0.0, 1.0);
      else {
//        The branch-tip weights (0.6, 0.2, and 0.2) for the mean, -1.64sigma, and +1.64sigma,
//       respectively, are from WG99's Table 1.1
        addRupturesToList(magDist, faultSurface, magScalingRel, magScalingSigma,
                          rupAspectRatio, rupOffset, rake, 5.0, 0.0, 0.6);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, 5.0, 1.64, 0.2);
        addRupturesToList(magDist, faultSurface, magScalingRel,
                          magScalingSigma, rupAspectRatio, rupOffset, rake, 5.0, -1.64, 0.2);
      }
  }


  /**
   * This computes the rupture length from the information supplied
   * @param magScalingRel - a MagLengthRelationship or a MagAreaRelationship
   * @param magScalingSigma - the standard deviation of the Area or Length estimate
   * @param numSigma - the number of sigmas from the mean for which the estimate is for
   * @param rupAspectRatio
   * @param mag
   * @return
   */
  private double getRupLength(MagScalingRelationship magScalingRel,
                              double magScalingSigma,
                              double numSigma,
                              double rupAspectRatio,
                              double mag) throws RuntimeException {

    // if it's a mag-area relationship
    if(magScalingRel instanceof MagAreaRelationship) {
      double area = magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
      return Math.sqrt(area*rupAspectRatio);
    }
    else if (magScalingRel instanceof MagLengthRelationship) {
      return magScalingRel.getMedianScale(mag) * Math.pow(10,numSigma*magScalingSigma);
    }
    else throw new RuntimeException("bad type of MagScalingRelationship");
  }



  /**
   * This method makes and adds ruptures to the list
   */
  private void addRupturesToList(IncrementalMagFreqDist magDist,
                             EvenlyGriddedSurface faultSurface,
                             MagScalingRelationship magScalingRel,
                             double magScalingSigma,
                             double rupAspectRatio,
                             double rupOffset,
                             double rake,
                             double minMag,
                             double numSigma,
                             double weight) {

    int numMags = magDist.getNum();  // Note that some of these may have zero rates!

    double rupLen;
    double rupWidth;
    double numRup;
    double mag;
    double rate;
    double prob=Double.NaN;

    if( D ) System.out.println(C+": magScalingSigma="+magScalingSigma);

    for(int i=0;i<numMags;++i){
      mag = magDist.getX(i);
      // make sure it has a non-zero rate & the mag is >= minMag
      if(magDist.getY(i) > 0 && mag >= minMag) {
        rupLen = getRupLength(magScalingRel,magScalingSigma,numSigma,rupAspectRatio,mag);
        rupWidth= rupLen/rupAspectRatio;
        numRup = faultSurface.getNumSubsetSurfaces(rupLen,rupWidth,rupOffset);
        rate = magDist.getY(mag);
        // Create the ruptures and add to the list
        for(int r=0; r < numRup; ++r) {
          probEqkRupture = new ProbEqkRupture();
          probEqkRupture.setAveRake(rake);
          // set rupture surface
          probEqkRupture.setRuptureSurface(faultSurface.getNthSubsetSurface(rupLen,rupWidth,rupOffset,r));
          probEqkRupture.setMag(mag);
          prob = weight*(1.0 - Math.exp(-timeSpan*rate/numRup));
          probEqkRupture.setProbability(prob);
          ruptureList.add(probEqkRupture);
        }
          if( D ) System.out.println(C+": mag="+mag+"; rupLen="+rupLen+"; rupWidth="+rupWidth+
                                      "; rate="+rate+"; timeSpan="+timeSpan+"; numRup="+numRup+"; prob="+prob);
      }
    }
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

      Iterator it = faultCornerLocations.iterator();

      while(it.hasNext()) {
        tempMin = RelativeLocation.getHorzDistance(site.getLocation(),(Location)it.next());
        if(tempMin < min) min = tempMin;
      }

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
      faultCornerLocations.add(faultSurface.get(0,0));
      faultCornerLocations.add(faultSurface.get(0,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(0,nCols-1));
      faultCornerLocations.add(faultSurface.get(nRows-1,0));
      faultCornerLocations.add(faultSurface.get(nRows-1,(int)(nCols/2)));
      faultCornerLocations.add(faultSurface.get(nRows-1,nCols-1));


    }

    /**
     * set the name of this class
     *
     * @return
     */
    public void setName(String name) {
      NAME=name;
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