package org.scec.sha.earthquake.rupForecastImpl;

import java.util.Vector;
import java.util.Iterator;

import org.scec.sha.surface.EvenlyGriddedSurface;
import org.scec.data.*;
import org.scec.calc.RelativeLocation;
import org.scec.sha.earthquake.*;
import org.scec.sha.surface.*;
import org.scec.sha.magdist.*;


/**
 * <p>Title: SimpleFaultRuptureSource </p>
 * <p>Description: This implements a basic fault source for arbitrary: <p>
 * <UL>
 * <LI>magnitude (or magnitude-frequncy dist.)
 * <LI>ruptureSurface - any EvenlyDiscretizedSurface
 * <LI>rake - that rake (in degrees) assigned to all ruptures.
 * <LI>probability (or duration)
 * </UL><p>
 * If magnitude/probability are given the source is set as non poissonian (and
 * duration is meaningless); If a mag-freq-dist and duration is given than the source
 * is assumed to be Poissonain. The entire surface ruptures for all cases (no floating
 * of events)  Note that duration is the only constructor argument saved internally
 * in order to conserve memory (this is why there are no associated get/set methods
 * for anything besides duration).<p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field
 * @date Sept, 2003
 * @version 1.0
 */

public class SimpleFaultRuptureSource extends ProbEqkSource {

  //for Debug purposes
  private static String  C = new String("SimpleFaultRuptureSource");
  private boolean D = false;


  //name for this classs
  protected String  NAME = C;

  protected double duration;

  private Vector ruptureList;  // keep this in case we add more mags later
  private Vector faultCornerLocations = new Vector();   // used for the getMinDistance(Site) method

  /**
   * Constructor - this is for a single mag, non-poissonian rupture.
   * @param magnitude
   * @param ruptureSurface - any EvenlyGriddedSurface representation of the fault
   * @param rake - average rake of the ruptures
   * @param probability - the probability of the source
   */
  public SimpleFaultRuptureSource(double magnitude,
                                  EvenlyGriddedSurface ruptureSurface,
                                  double rake,
                                  double probability) {

      this.isPoissonian = false;

      if (D) {
        System.out.println("mag: "+magnitude);
        System.out.println("surface rows, cols: "+ruptureSurface.getNumCols()+", "+ruptureSurface.getNumRows());
        System.out.println("rake: "+rake);
        System.out.println("probability: "+probability);

      }
      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(ruptureSurface);

      // make the rupture list
      ruptureList = new Vector();

      probEqkRupture = new ProbEqkRupture();
      probEqkRupture.setAveRake(rake);
      probEqkRupture.setRuptureSurface(ruptureSurface);
      probEqkRupture.setMag(magnitude);
      probEqkRupture.setProbability(probability);

      ruptureList.add(probEqkRupture);

  }

  /**
   * Constructor - this produces a separate rupture for each mag in the mag-freq-dist.
   * This source is set as Poissonian.
   * @param magnitude-frequency distribution
   * @param ruptureSurface - any EvenlyGriddedSurface representation of the fault
   * @param rake - average rake of the ruptures
   * @param duration - the duration in years
   */
  public SimpleFaultRuptureSource(IncrementalMagFreqDist magDist,
                                  EvenlyGriddedSurface ruptureSurface,
                                  double rake,
                                  double duration) {

      this.isPoissonian = true;
      this.duration = duration;

      if (D) {
        System.out.println("surface rows, cols: "+ruptureSurface.getNumCols()+", "+ruptureSurface.getNumRows());
        System.out.println("rake: "+rake);
        System.out.println("duration: "+duration);
      }

      // make a list of a subset of locations on the fault for use in the getMinDistance(site) method
      makeFaultCornerLocs(ruptureSurface);

      // make the rupture list
      ruptureList = new Vector();
      double mag;
      double prob;

      // Make the ruptures
      for(int i=0;i<magDist.getNum();++i){
        mag = magDist.getX(i);
        // make sure it has a non-zero rate
        if(magDist.getY(i) > 0) {
          prob = 1 - Math.exp(-duration*magDist.getY(i));
          probEqkRupture = new ProbEqkRupture();
          probEqkRupture.setAveRake(rake);
          probEqkRupture.setRuptureSurface(ruptureSurface);
          probEqkRupture.setMag(mag);
          probEqkRupture.setProbability(prob);
          ruptureList.add(probEqkRupture);
        }
      }

  }

  /**
   * This changes the duration for the case where a mag-freq dist was given in
   * the constructor (for the Poisson) case.
   * @param newDuration
   */
  public void setDuration(double newDuration) {
    if(this.isPoissonian != true)
      throw new RuntimeException(C+" Error - the setDuration method can only be used for the Poisson case");
    ProbEqkRupture eqkRup;
    double oldProb, newProb;
    for(int i = 0; i < ruptureList.size(); i++) {
      eqkRup = (ProbEqkRupture) ruptureList.get(i);
      oldProb = eqkRup.getProbability();
      newProb = 1.0 - Math.pow((1.0-oldProb), newDuration/duration);
      eqkRup.setProbability(newProb);
    }
    duration=newDuration;
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