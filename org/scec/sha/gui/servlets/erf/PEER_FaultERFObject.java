package org.scec.sha.gui.servlets.erf;

import java.io.*;
import java.util.*;

import org.scec.sha.gui.servlets.erf.ERF_API;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultSource;
import org.scec.sha.earthquake.*;


/**
 * <p>Title: PEER_FaultERFObject</p>
 * <p>Description: This class implements the ERF_API to return the PEER Forecast object
 * back to the user.</p>
 * @author: Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class PEER_FaultERFObject implements ERF_API,java.io.Serializable{


  // this is the source (only 1 for this ERF)
  private PEER_FaultSource source;

  //default class constructor
  public PEER_FaultERFObject() {}

  /**
   * Returns the number of earthquake sources (always "1" here)
   *
   * @return integer value specifying the number of earthquake sources
   */
  public int getNumSources(){
    return 1;
  }


  /**
   *  This returns a list of sources (contains only one here)
   *
   * @return Vector of Prob Earthquake sources
   */
  public Vector  getSourceList(){
    Vector v =new Vector();
    v.add(source);
    return v;
  }



  /**
   * Get the ith rupture of the source. this method DOES NOT return reference
   * to the object. So, when you call this method again, result from previous
   * method call is valid. This behavior is in contrast with
   * getRupture(int source, int i) method
   *
   * @param source
   * @param i
   * @return
   */
  public ProbEqkRupture getRupture(int iSource, int nRupture) {
    return getSource(iSource).getRupture(nRupture);
  }

  /**
   * Return the earhthquake source at index i.   Note that this returns a
   * pointer to the source held internally, so that if any parameters
   * are changed, and this method is called again, the source obtained
   * by any previous call to this method will no longer be valid.
   *
   * @param iSource : index of the desired source (only "0" allowed here).
   *
   * @return Returns the ProbEqkSource at index i
   *
   */
  public ProbEqkSource getSource(int iSource) {

    // we have only one source
    if(iSource!=0)
      throw new RuntimeException("Only 1 source available, iSource should be equal to 0");

    return source;
  }

  /**
   * Get number of ruptures for source at index iSource
   * This method iterates through the list of 3 vectors for charA , charB and grB
   * to find the the element in the vector to which the source corresponds
   * @param iSource index of source whose ruptures need to be found
   */
  public int getNumRuptures(int iSource){
    return getSource(iSource).getNumRuptures();
  }

  /**
   * Sets the PEERFaultSource
   * @param src
   */
  public void setSource(PEER_FaultSource src){
    this.source = src;
  }

}