package org.scec.sha.earthquake;

import java.util.ListIterator;
import java.util.Vector;
import java.util.Iterator;
import java.util.EventObject;
import java.util.GregorianCalendar;

import org.scec.param.ParameterList;
import org.scec.param.ParameterAPI;
import org.scec.param.event.TimeSpanChangeListener;
import org.scec.data.Location;
import org.scec.data.TimeSpan;
import org.scec.data.region.GeographicRegion;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI, ERF_API,
    TimeSpanChangeListener {

  // adjustable params for each forecast
  protected ParameterList adjustableParams = new ParameterList();
  // timespan object for each forecast
  protected TimeSpan timeSpan;

  // it is flag which indiactes whether any parameter have changed.
  // if it is true it means that forecast needs to be updated
  protected boolean parameterChangeFlag = true;


  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
   public ListIterator getAdjustableParamsIterator() {
     return adjustableParams.getParametersIterator();
   }

   /**
   * This function finds whether a particular location lies in applicable
   * region of the forecast
   *
   * @param loc : location
   * @return: True if this location is within forecast's applicable region, else false
   */
  public boolean isLocWithinApplicableRegion(Location loc) {
    return true;
  }


  /**
   * Get the region for which this forecast is applicable
   * @return : Geographic region object specifying the applicable region of forecast
   */
   public GeographicRegion getApplicableRegion() {
     return null;
   }

   /**
    * This function returns the parameter with specified name from adjustable param list
    * @param paramName : Name of the parameter needed from adjustable param list
    * @return : ParamterAPI instance
    */
   public ParameterAPI getParameter(String paramName) {
     return adjustableParams.getParameter(paramName);
   }

   /**
    * set the TimeSpan in the ERF
    * @param timeSpan : TimeSpan object
    */
   public void setTimeSpan(TimeSpan time) {
     // set the start time
     this.timeSpan.setStartTime( time.getStartTimeCalendar());
     //set the duration as well
     this.timeSpan.setDuration(time.getDuration(), time.getDurationUnits());
   }

   /**
    * return the time span object
    *
    * @return : time span object is returned which contains start time and duration
    */
   public TimeSpan getTimeSpan() {
     return this.timeSpan;
   }

   /**
    *  Function that must be implemented by all Timespan Listeners for
    *  ParameterChangeEvents.
    *
    * @param  event  The Event which triggered this function call
    */
   public void parameterChange( EventObject event ) {
     this.parameterChangeFlag = true;
   }


  /**
   * Get the number of earthquake sources
   *
   * @return integer value spcifying the number of earthquake sources
   */
  public abstract int getNumSources();


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
  public abstract ProbEqkSource getSource(int iSource) ;


  /**
   * Get the list of all earthquake sources. Clone is returned.
   * So, list can be save in Vector and this object subsequently destroyed
   *
   * @return Vector of Prob Earthquake sources
   */
  public abstract Vector  getSourceList();


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
  * Get the ith rupture of the source. this method DOES NOT return reference
  * to the object. So, when you call this method again, result from previous
  * method call is valid. This behavior is in contrast with
  * getRupture(int source, int i) method
  *
  * @param source
  * @param i
  * @return
  */
 public ProbEqkRupture getRuptureClone(int iSource, int nRupture) {
   return getSource(iSource).getRuptureClone(nRupture);
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
  * Return  iterator over all the earthquake sources
  *
  * @return Iterator over all earhtquake sources
  */
 public Iterator getSourcesIterator() {
   Iterator i = getSourceList().iterator();
   return i;
 }

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
  public ProbEqkSource getSourceClone(int iSource) {
    return null;
   }


   /**
    *
    * @returns the adjustable ParameterList for the ERF
    */
   public ParameterList getAdjustableParameterList(){
     return this.adjustableParams;
   }


   /**
    * sets the value for the parameter change flag
    * @param flag
    */
   public void setParameterChangeFlag(boolean flag){
     this.parameterChangeFlag = flag;
   }
}