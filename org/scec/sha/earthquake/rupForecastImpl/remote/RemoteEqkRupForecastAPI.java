
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
 *
 * <p>Title: RemoteEqkRupForecastAPI</p>
 * <p>Description: This class provides the interface to the Remotely existing
 * ERF's.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */
public interface RemoteEqkRupForecastAPI extends Remote {

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
          * save the forecast in a file
          * @throws RemoteException
          */
         public String saveForecast() throws RemoteException ;

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

}
