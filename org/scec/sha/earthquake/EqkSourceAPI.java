package org.scec.sha.earthquake;

import java.util.Iterator;
import java.util.Vector;

import org.scec.data.NamedObjectAPI;

/**
 * <p>Title: EqkSourceAPI</p>
 * <p>Description: THis interface is for getting rupture information about each earhquake source</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Aug 27 20002
 * @version 1.0
 */

public interface EqkSourceAPI extends NamedObjectAPI{


  /**
   * Get the iterator over all ruptures
   *
   * @return Iterator on vector for all ruptures
   */
  public Iterator getRupturesIterator();


  /**
   * Get the number of ruptures for this source
   *
   * @return returns an integer value specifying the number of ruptures for this source
   */
  public int getNumRuptures();


  /**
   * Get the ith rupture for this source
   * This is a handle(or reference) to existing class variable. If this function
   *  is called again, then output from previous function call will not remain valid
   *  because of passing by reference
   * It is a secret, fast but dangerous method
   *
   * @param i  ith rupture
   */
  public ProbEqkRupture getRupture(int nRupture);


  /**
   * this function can be used if a clone is wanted instead of handle to class variable
   * Subsequent calls to this function will not affect the result got previously.
   * This is in contrast with the getRupture(int i) function
   *
   * @param i
   * @return
   */
  public ProbEqkRupture getRuptureClone(int nRupture);

  /**
   * Returns the Vector consisting of all ruptures for this source
   * all the objects are cloned. so this vector can be saved by the user
   *
   * @return Vector consisting of
   */
  public Vector getRuptureList();




}