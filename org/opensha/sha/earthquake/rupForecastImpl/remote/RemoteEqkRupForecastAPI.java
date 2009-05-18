
package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.region.EvenlyGriddedGeographicRegionAPI;
import org.opensha.commons.data.region.GeographicRegion;
import org.opensha.param.ParameterList;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;



/**
 *
 * <p>Title: RemoteEqkRupForecastAPI</p>
 * <p>Description: This class provides the interface to the Remotely existing
 * ERF's.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public interface RemoteEqkRupForecastAPI extends RemoteERF_API {


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
   * Return the earthquake source at index i. This methos DOES NOT return the
   * reference to the class variable. So, when you call this method again,
   * result from previous method call is still valid. This behavior is in contrast
   * with the behavior of method getSource(int i)
   *
   * @param iSource : index of the source needed
   *
   * @return Returns the ProbEqkSource at index i
   *
   * FIX:FIX :: This function has not been implemented yet. Have to give a thought on that
   *
   */
  public ProbEqkSource getSourceClone(int iSource) throws RemoteException;




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
  public ProbEqkRupture getRuptureClone(int iSource, int nRupture) throws RemoteException;


}
