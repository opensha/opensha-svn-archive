package org.scec.sha.gui.servlets.erf;

import java.util.Vector;
import org.scec.sha.earthquake.ProbEqkSource;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.earthquake.*;

/**
 * <p>Title: ERF_ListAPI</p>
 * <p>Description: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created June 19,2003
 * @version 1.0
 */

public interface ERF_ListAPI{


  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk Rup forecast to return
   * @return
   */
  public ERF_API getERF(int index);


  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index)  ;


  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : Vector of Double values
   */
  public Vector getRelativeWeightsList();


  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs();

}