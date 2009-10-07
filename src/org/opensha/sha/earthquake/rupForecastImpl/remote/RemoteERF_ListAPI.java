package org.opensha.sha.earthquake.rupForecastImpl.remote;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * <p>Title: RemoteERF_ListAPI</p>
 * <p>Description: This class provides the interface to remote ERF List.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 05, 2004
 * @version 1.0
 */

public interface  RemoteERF_ListAPI extends RemoteERF_API{

  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk Rup forecast to return
   * @return
   */
  public RemoteEqkRupForecastAPI getRemoteERF(int index) throws RemoteException;


  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index)  throws RemoteException;


  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : ArrayList of Double values
   */
  public ArrayList getRelativeWeightsList() throws RemoteException;


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs() throws RemoteException;

}
