/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.data.region.GeographicRegion;
import org.scec.param.ParameterList;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.earthquake.ProbEqkSource;

/**
 * @author cmeutils
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface RemoteERF_API extends RemoteEqkRupForecastAPI {


  /**
   *
   * @returns the total number os sources
   */
  public int getNumSources() throws RemoteException;

  /**
   *
   * @returns the sourceList
   */
  public ArrayList getSourceList() throws RemoteException;

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
  public ProbEqkSource getSource(int iSource) throws RemoteException;


  /**
   *
   * @param iSource
   * @returns the number of ruptures for the ithSource
   */
  public int getNumRuptures(int iSource) throws RemoteException;



  /**
   *
   * @param iSource
   * @param nRupture
   * @returns the ProbEqkRupture object for the ithSource and nth rupture
   */
  public ProbEqkRupture getRupture(int iSource,int nRupture) throws RemoteException;


}