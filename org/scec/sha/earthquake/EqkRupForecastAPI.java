package org.scec.sha.earthquake;

import org.scec.data.NamedObjectAPI;
import org.scec.data.TimeSpan;

import java.util.Iterator;
import java.util.Vector;
/**
 * <b>Title:</b> EqkRupForecast<br>
 * <b>Description: Earthquake Rupture Forecast</b> <br>
 * <b>Copyright:</b> Copyright (c) 2001<br>
 * <b>Company:</b> <br>
 * @author Nitin Gupta & Vipin Gupta
 * @date Aug 27, 2002
 * @version 1.0
 */

public interface EqkRupForecastAPI extends NamedObjectAPI{




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
    * this method is being used temporarily for setting the timespan in yrs
    * after taking the input from the user from the EqkForecast Applet
    * @param yrs
    */

   public void setTimeSpan(int yrs);

}
