package org.scec.sha.gui.servlets.erf;

import java.util.Vector;

import org.scec.sha.earthquake.ProbEqkRupture;

/**
 * <p>Title: ERF_API</p>
 * <p>Description: Interface to the details of the Sources and Ruptures</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface ERF_API {

  /**
   *
   * @returns the total number os sources
   */
  public int getNumSources();

  /**
   *
   * @returns the sourceList
   */
  public Vector getSourceList();

  /**
   *
   * @param iSource
   * @returns the number of ruptures for the ithSource
   */
  public int getNumRuptures(int iSource);

  /**
   *
   * @param iSource
   * @param nRupture
   * @returns the ProbEqkRupture object for the ithSource and nth rupture
   */
  public ProbEqkRupture getRupture(int iSource,int nRupture);
}