package org.opensha.sha.earthquake;


import org.opensha.data.TimeSpan;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.data.Location;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.NamedObjectAPI;
import java.util.ListIterator;


/**
 * <p>Title: EqkRupForecastBaseAPI</p>
 * <p>Description: This defines the common interface that applies to both an EqkRupForecast 
 * and an ERF_LIST (the methods that are common betwen the two).</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created Sept 30,2004
 * @version 1.0
 */

public interface EqkRupForecastBaseAPI extends NamedObjectAPI{

  /**
   * This method tells the forecast that the user is done setting parameters and that
   * it can now prepare itself for use.  We could avoid needing this method if the 
   * forecast updated every time a parameter was changed, but this would be very inefficient
   * with forecasts that take a lot of time to update.  This also avoids problems associated
   * with accidentally changing a parameter in the middle of a calculation.
   * @return
   */
  public void updateForecast();

  /**
   * Update and save the serialized forecast into a file
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
   * This will set the parameter with the given
   * name to the given value.
   * @param name String Name of the Adjustable Parameter
   * @param value Object Parameter Value
   * @return boolean boolean to see if it was successful in setting the parameter
   * value.
   */
  public boolean setParameter(String name, Object value);

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
  public ListIterator<ParameterAPI> getAdjustableParamsIterator();
  
  /**
   * Gets the Adjustable parameter list for the ERF
   * @return
   */
  public ParameterList getAdjustableParameterList();


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


}
