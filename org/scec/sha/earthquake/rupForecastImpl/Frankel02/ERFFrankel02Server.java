/*
 * Created on Apr 1, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.scec.sha.earthquake.rupForecastImpl.Frankel02;

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
public interface ERFFrankel02Server extends Remote {

	/**
	 * This method updates the forecast according to the currently specified
	 * parameters.  Call this once before looping over the getRupture() or
	 * getSource() methods to ensure a fresh forecast.  This approach was chosen
	 * over checking whether parameters have changed during each getRupture() etc.
	 * method call because a user might inadvertently change a parameter value in
	 * the middle of the loop.  This approach is also faster.
	 * @return
	 */
	 public void updateForecast(ParameterList list, TimeSpan timeSpan) throws RemoteException ;

	/**
	 * Return the name for this class
	 *
	 * @return : return the name for this class
	 */
	 public String getName() throws RemoteException;

	 /**
	  * This method sets the time-span field
	  * @param time
	  */
	 public void setTimeSpan(TimeSpan time) throws RemoteException;


	 /**
	  * This method gets the time-span field
	  */
	 public TimeSpan getTimeSpan() throws RemoteException;


	 /**
	  * get the adjustable parameters for this forecast
	  *
	  * @return
	  */
	 public ListIterator getAdjustableParamsIterator() throws RemoteException;

	 /**
	  * This function finds whether a particular location lies in applicable
	  * region of the forecast
	  *
	  * @param loc : location
	  * @return: True if this location is within forecast's applicable region, else false
	  */
	 public boolean isLocWithinApplicableRegion(Location loc) throws RemoteException;


	 /**
	  * Get the region for which this forecast is applicable
	  * @return : Geographic region object specifying the applicable region of forecast
	  */
	 public GeographicRegion getApplicableRegion() throws RemoteException;

	 /**
	  * Gets the Adjustable parameter list for the ERF
	  * @return
	  */
	 public ParameterList getAdjustableParameterList() throws RemoteException;
	
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
