package org.scec.sha.earthquake;

import java.util.Vector;
import java.util.Iterator;

import org.scec.data.Site;
import org.scec.data.NamedObjectAPI;

/**
 * <p>Title: ProbEqkSource</p>
 * <p>Description: Class for Probabilistic earthquake source</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta and Vipin Gupta
 * @date Aug 27, 2002
 * @version 1.0
 */

public abstract class ProbEqkSource implements EqkSourceAPI, NamedObjectAPI {

  /**
   * Name of this class
   */
  private String name = new String("ProbEqkSource");


  /**
   * This is private variable which saves a earthquake rupture
   */
  protected ProbEqkRupture probEqkRupture;





  /**
   * Get the iterator over all ruptures
   * This function returns the iterator for the rupturelist after calling the method getRuptureList()
   * @return the iterator object for the RuptureList
   */
  public Iterator getRupturesIterator() {
   Vector v= getRuptureList();
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

    ProbEqkRupture eqkRuptureClone=new ProbEqkRupture();
    eqkRuptureClone.setAveRake(eqkRupture.getAveRake());
    eqkRuptureClone.setMag(eqkRupture.getMag());
    eqkRuptureClone.setRuptureSurface(eqkRupture.getRuptureSurface());
    eqkRuptureClone.setHypocenterLocation(eqkRupture.getHypocenterLocation());
    eqkRuptureClone.setProbability(eqkRupture.getProbability());
    eqkRuptureClone.setTimeSpan(eqkRupture.getTimeSpan());

    return eqkRuptureClone;
  }

  /**
   * Returns the Vector consisting of all ruptures for this source
   * all the objects are cloned. so this vector can be saved by the user
   *
   * @return Vector consisting of
   */
  public abstract Vector getRuptureList();

  /**
   * get the name of this class
   *
   * @return
   */
  public String getName() {
    return name;
  }
}