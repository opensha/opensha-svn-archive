package org.scec.sha.earthquake;

import java.util.ArrayList;
import org.scec.sha.earthquake.ProbEqkSource;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.remote.RemoteERF_API;

/**
 * <p>Title: ERF_ListAPI</p>
 * <p>Description: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created June 19,2003
 * @version 1.0
 */

public interface ERF_ListAPI extends ForecastAPI{



  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk Rup forecast to return
   * @return
   */
  public ERF_API getERF(int index);


  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index.
   * **NOTE: All the functionality in this functionlity remains same as that of getERF but only differs
   * when returning each ERF from the ERF List. getERF() return the instance of the
   * ERF_API which is transferring the whole object on to the user's machine, but this functin
   * return back the RemoteERF_API. This is useful becuase whole ERF object does not
   * get transfer to the users machine, just a stub of the remotely existing ERF gets
   * transferred.
   */
  public RemoteERF_API getRemoteERF(int index);

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