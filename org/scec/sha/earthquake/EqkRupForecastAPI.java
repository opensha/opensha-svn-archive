package org.scec.sha.earthquake;

import org.scec.data.NamedObjectAPI;
import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.data.Location;
import org.scec.data.region.GeographicRegion;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * <b>Title:</b> EqkRupForecast<br>
 * <b>Description: Earthquake Rupture Forecast</b> <br>
 *
 * @author Nitin Gupta & Vipin Gupta
 * @date Aug 27, 2002
 * @version 1.0
 */

public interface EqkRupForecastAPI extends NamedObjectAPI{


    /**
     * This method updates the forecast according to the currently specified
     * parameters.  Call this once before looping over the getRupture() or
     * getSource() methods to ensure a fresh forecast.  This approach was chosen
     * over checking whether parameters have changed during each getRupture() etc.
     * method call because a user might inadvertently change a parameter value in
     * the middle of the loop.  This approach is also faster.
     * @return
     */
     public void updateForecast();

     /**
      * Update and save the serialized forecast into the file
      */
     public String updateAndSaveForecast();

    /**
     * Return the name for this class
     *
     * @return : return the name for this class
     */
     public String getName();

     /**
      * This method sets the time-span field
      * @param time
      */
     public void setTimeSpan(TimeSpan time);


     /**
      * This method gets the time-span field
      */
     public TimeSpan getTimeSpan();


     /**
      * get the adjustable parameters for this forecast
      *
      * @return
      */
     public ListIterator getAdjustableParamsIterator();

     /**
      * This function finds whether a particular location lies in applicable
      * region of the forecast
      *
      * @param loc : location
      * @return: True if this location is within forecast's applicable region, else false
      */
     public boolean isLocWithinApplicableRegion(Location loc);


     /**
      * Get the region for which this forecast is applicable
      * @return : Geographic region object specifying the applicable region of forecast
      */
     public GeographicRegion getApplicableRegion() ;

     /**
      * Gets the Adjustable parameter list for the ERF
      * @return
      */
     public ParameterList getAdjustableParameterList();

}






