package org.scec.sha.earthquake;

import java.util.ArrayList;
import java.util.Iterator;

import org.scec.data.Site;
import org.scec.data.NamedObjectAPI;

/**
 * <p>Title: ProbEqkSource</p>
 * <p>Description: Class for Probabilistic earthquake source</p>
 *
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @date Aug 27, 2002
 * @version 1.0
 */

public abstract class ProbEqkSource implements EqkSourceAPI, NamedObjectAPI {

  /**
   * Name of this class
   */
  protected String name = new String("ProbEqkSource");


  /**
   * This is private variable which saves a earthquake rupture
   */
  protected ProbEqkRupture probEqkRupture;


  /**
   * This boolean tells whether the source is Poissonian, which will influence the
   * calculation sequence in the HazardCurveCalculator.  Note that the default value
   * is true, so non-Poissonian sources will need to overide this value.
   */
  protected boolean isPoissonian = true;

  /**
   * string to save the information about this source
   */
  private String info;


  /**
   * This method tells whether the source is Poissonian, which will influence the
   * calculation sequence in the HazardCurveCalculator
   */
  public boolean isSourcePoissonian() {
    return isPoissonian;
  }

  /**
   * Get the iterator over all ruptures
   * This function returns the iterator for the rupturelist after calling the method getRuptureList()
   * @return the iterator object for the RuptureList
   */
  public Iterator getRupturesIterator() {
   ArrayList v= getRuptureList();
   return v.iterator();
  }


  /**
   * This computes some measure of the minimum distance between the source and
   * the site passed in.  This is useful for ignoring sources that are at great
   * distanced from a site of interest.  Actual implementation depend on subclass.
   * @param site
   * @return minimum distance
   */
  public abstract double getMinDistance(Site site);

  /**
   * Get the number of ruptures for this source
   *
   * @return returns an integer value specifying the number of ruptures for this source
   */
  public abstract int getNumRuptures() ;

  /**
   * Get the ith rupture for this source
   * This is a handle(or reference) to existing class variable. If this function
   *  is called again, then output from previous function call will not remain valid
   *  because of passing by reference
   * It is a secret, fast but dangerous method
   *
   * @param i  ith rupture
   */
  public abstract ProbEqkRupture getRupture(int nRupture);


  /**
   * this function can be used if a clone is wanted instead of handle to class variable
   * Subsequent calls to this function will not affect the result got previously.
   * This is in contrast with the getRupture(int i) function
   *
   * @param nRupture
   * @return the clone of the probEqkRupture
   */
  public ProbEqkRupture getRuptureClone(int nRupture){
    ProbEqkRupture eqkRupture =getRupture(nRupture);
    ProbEqkRupture eqkRuptureClone= (ProbEqkRupture)eqkRupture.clone();
    return eqkRuptureClone;
  }

  /**
   * Returns the ArrayList consisting of all ruptures for this source
   * all the objects are cloned. so this vector can be saved by the user
   *
   * @return ArrayList consisting of the rupture clones
   */
  public ArrayList getRuptureList() {
    ArrayList v= new ArrayList();
    for(int i=0; i<getNumRuptures();i++)
      v.add(getRuptureClone(i));
    return v;
  }

  /**
   * get the name of this class
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Set the info for this Prob Eqk source
   * @param infoString : Info
   * @return
   */
  public void setInfo(String infoString) {
    this.info = new String(infoString);
  }

  /**
   * Get the info for this source
   *
   * @return
   */
  public String getInfo() {
    return new String(this.info);
  }


  /**
   * This computes the total probability for this source
   * (a sum of the probabilities of all the ruptures)
   * @return
   */
  public double computeTotalProb() {
    return computeTotalProbAbove(-10.0);
  }


  /**
 * This computes the total probability of all rutures great than or equal to the
 * given mangitude
 * @return
 */
  public double computeTotalProbAbove(double mag) {
    double totProb=0;
    ProbEqkRupture tempRup;
    if(isPoissonian) {
      for(int i=0; i<getNumRuptures(); i++) {
        tempRup = getRupture(i);
        if(tempRup.getMag() >= mag)
          totProb += Math.log(1-tempRup.getProbability());
      }
      totProb = 1 - Math.exp(totProb);
    }
    else {
      for(int i=0; i<getNumRuptures(); i++) {
        tempRup = getRupture(i);
        if(tempRup.getMag() >= mag)
          totProb += tempRup.getProbability();
      }
    }
    return totProb;
  }


/*
  public IncrementalMagFreqDist computeMagProbDist() {

    ArbDiscrEmpiricalDistFunc distFunc = new ArbDiscrEmpiricalDistFunc();
    ArbitrarilyDiscretizedFunc tempFunc = new ArbitrarilyDiscretizedFunc();
    IncrementalMagFreqDist magFreqDist = null;

    ProbEqkRupture qkRup;
    for(int i=0; i<getNumRuptures(); i++) {
      qkRup = getRupture(i);
      distFunc.set(qkRup.getMag(),qkRup.getProbability());
    }
    // duplicate the distFunce
    for(int i = 0; i < distFunc.getNum(); i++) tempFunc.set(distFunc.get(i));

    // now get the cum dist
    for(int i=tempFunc.getNum()-2; i >=0; i--)
      tempFunc.set(tempFunc.getX(i),tempFunc.getY(i)+tempFunc.getY(i+1));

    // now make the evenly discretized

for(int i = 0; i < distFunc.getNum(); i++)
      System.out.println((float)distFunc.getX(i)+"  "+(float)tempFunc.getX(i)+"  "+(float)distFunc.getY(i)+"  "+(float)tempFunc.getY(i));

    return magFreqDist;
  }
*/
}