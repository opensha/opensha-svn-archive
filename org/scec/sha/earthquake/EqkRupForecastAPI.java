package org.scec.sha.earthquake;

import org.scec.data.NamedObjectAPI;
import org.scec.data.TimeSpan;
import org.scec.param.ParameterList;
import org.scec.data.Location;
import org.scec.data.region.GeographicRegion;

import java.util.Iterator;
import java.util.Vector;
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
     * Return  iterator over all the earthquake sources
     *
     * @return Iterator over all earhtquake sources
     */
    public Iterator getSourcesIterator() ;


    /**
     * Get the number of earthquake sources
     *
     * @return integer value spcifying the number of earthquake sources
     */
    public int getNumSources();


    /**
     * Return the earhthquake source at index i. This methos returns the reference to
     * the class variable. So, when you call this method again, result from previous
     * method call is no longer valid.
     * this is secret, fast but dangerous method
     *
     * @param i : index of the source needed
     *
     * @return Returns the ProbEqkSource at index i
     *
     */
    public ProbEqkSource getSource(int iSource) ;



    /**
     * Return the earhthquake source at index i. This methos DOES NOT return the
     *  reference to the class variable. So, when you call this method again,
     * result from previous method call is still valid. This behavior is in contrast
     * with the behavior of method getSource(int i)
     *
     * @param i : index of the source needed
     *
     * @return Returns the ProbEqkSource at index i
     *
     */
    public ProbEqkSource getSourceClone(int iSource) ;


    /**
     * Get the list of all earthquake sources. Clone is returned.
     * So, list can be save in Vector and this object subsequently destroyed
     *
     * @return Vector of Prob Earthquake sources
     */
    public Vector  getSourceList();


    /**
     * Get the ith rupture of this source. this method also returns reference
     * to the object. So, when you call this method again, result from previous
     * method call is no longer valid.
     * This is secret, fast but dangerous method
     *
     * @param source
     * @param i
     * @return
     */
    public EqkRupture getRupture(int iSource, int nRupture);

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
    public EqkRupture getRuptureClone(int iSource, int nRupture) ;


    /**
     * Get number of ruptures for source at index iSource
     *
     * @param iSource index of source whose ruptures need to be found
     */
    public int getNumRuptures(int iSource);

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
   * This method sets the tim span field
   * @param time
   */
   public void setTimeSpan(double yrs);


   /**
    * get the adjustable parameters for this forecast
    *
    * @return
    */
   public ListIterator getAdjustableParamsList();

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
