package org.scec.sha.earthquake;

import java.util.ListIterator;
import java.util.Vector;
import java.util.Iterator;

import org.scec.param.ParameterList;
import org.scec.param.ParameterAPI;
import org.scec.data.Location;
import org.scec.data.region.GeographicRegion;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public abstract class EqkRupForecast implements EqkRupForecastAPI{

  protected ParameterList adjustableParams = new ParameterList();

  /**
   * get the adjustable parameters for this forecast
   *
   * @return
   */
   public ListIterator getAdjustableParamsList() {
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
   * Return  iterator over all the earthquake sources
   *
   * @return Iterator over all earhtquake sources
   */
  public abstract Iterator getSourcesIterator() ;


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
  public abstract ProbEqkSource getSourceClone(int iSource) ;


  /**
   * Get the list of all earthquake sources. Clone is returned.
   * So, list can be save in Vector and this object subsequently destroyed
   *
   * @return Vector of Prob Earthquake sources
   */
  public abstract Vector  getSourceList();


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
  public abstract EqkRupture getRupture(int iSource, int nRupture);

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
  public abstract EqkRupture getRuptureClone(int iSource, int nRupture) ;


  /**
   * Get number of ruptures for source at index iSource
   *
   * @param iSource index of source whose ruptures need to be found
   */
  public abstract int getNumRuptures(int iSource);


}