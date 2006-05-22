package org.opensha.sha.earthquake;


import org.opensha.data.TimeSpan;
import org.opensha.param.ParameterList;
import org.opensha.data.Location;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.NamedObjectAPI;
import java.util.ListIterator;


/**
 * <p>Title: ERF_API</p>
 * <p>Description: This defines the interface for the ERF_LIST and EqkRupForecast
 * classes. Both ERF_List and EqkRupForcast classes implements this interface.
 * It is the parent interface that both ERF_List and EqkRupForecast have to implement.
 * This interface is needed so that common functions for both list and single forecast
 * can go in this interface. In the application one does not have care if it is a list
 * or single ERF because it will call the respective methods of the classes automatically
 * based on who soever object was created.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @created Sept 30,2004
 * @version 1.0
 */

public interface ERF_API extends NamedObjectAPI{

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
   * Loops over all the adjustable parameters and set parameter with the given
   * name to the given value.
   * @param name String Name of the Adjustable Parameter
   * @param value Object Parameeter Value
   * @return boolean boolean to see if it was successful in setting the parameter
   * value.
   */
  public boolean setParameter(String name, Object value);

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
