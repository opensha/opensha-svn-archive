package org.opensha.sha.earthquake;

import java.util.ArrayList;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.*;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;

/**
 * <p>Title: ERF_ListAPI</p>
 * <p>Description: This represents an epistemic list of earthquake rupture forecasts and their associated weights.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created June 19,2003
 * @version 1.0
 */

public interface ERF_ListAPI extends ERF_API{



  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk Rup forecast to return
   * @return
   */
  public EqkRupForecastAPI getERF(int index);


  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index.
   * **NOTE: All the functionality in this functionality remains same as that of getERF but only differs
   * when returning each ERF from the ERF List. getERF() return the instance of the
   * EqkRupForecastAPI which is transferring the whole object on to the user's machine, but this function
   * return back the RemoteEqkRupForecastAPI. This is useful because whole ERF object does not
   * get transfer to the users machine, just a stub of the remotely existing ERF gets
   * transferred.
   */
  public RemoteEqkRupForecastAPI getRemoteERF(int index);

  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index)  ;


  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : ArrayList of Double values
   */
  public ArrayList getRelativeWeightsList();


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs();

}
