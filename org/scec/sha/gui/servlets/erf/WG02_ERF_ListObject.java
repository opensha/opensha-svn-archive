package org.scec.sha.gui.servlets.erf;

import java.io.*;
import java.util.*;

import org.scec.sha.earthquake.ERF_API;
import org.scec.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_FaultSource;
import org.scec.sha.earthquake.*;
import org.scec.sha.earthquake.rupForecastImpl.WG02.*;
import org.scec.data.TimeSpan;


/**
 * <p>Title: WG02_ERF_ListObject</p>
 * <p>Description: This class implements the ERF_API to return the PEER Forecast object
 * back to the user.</p>
 * @author: Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class WG02_ERF_ListObject implements ERF_ListAPI,java.io.Serializable{


  // vector to hold the line numbers where each iteration starts
  private Vector iterationLineNumbers;

  // This is an array holding each line of the input file
  private ArrayList inputFileLines = null;

  // adjustable parameter primitives
  private int numIterations;
  private double rupOffset;
  private double deltaMag;
  private double gridSpacing;
  private String backSeis;
  private String grTail;

  private TimeSpan timeSpan;


  /**
   * Class Constructor
   * @param lines : ArrayList of the lines read from the WG-02 output file
   * @param iterationsNumber: vector to hold the line numbers where each iteration starts
   *
   * // adjustable parameter primitives
   * @param numIterations
   * @param rupOffset
   * @param deltaMag
   * @param gridSpacing
   * @param backSies
   * @param grTail
   */
  public WG02_ERF_ListObject(ArrayList lines, Vector iterationsNumber,int numIterations,
                             double rupOffset,double deltaMag,double gridSpacing,
                             String backSies,String grTail) {

    this.inputFileLines=lines;
    this.iterationLineNumbers=iterationsNumber;
    this.numIterations=numIterations;
    this.rupOffset=rupOffset;
    this.deltaMag=deltaMag;
    this.gridSpacing=gridSpacing;
    this.backSeis=backSies;
    this.grTail=grTail;
  }



  /**
   * get the number of Eqk Rup Forecasts in this list
   * @return : number of eqk rup forecasts in this list
   */
  public int getNumERFs() {
    return numIterations;
  }

  /**
   * get the ERF in the list with the specified index
   * @param index : index of Eqk rup forecast to return
   * @return
   */
  public ERF_API getERF(int index) {

    // get the sublist from the inputFileLines
    int firstLine = ((Integer) iterationLineNumbers.get(index)).intValue();
    int lastLine = ((Integer) iterationLineNumbers.get(index+1)).intValue();
    List inputFileStrings = inputFileLines.subList(firstLine,lastLine);

    return new WG02_ERFObject(inputFileStrings, rupOffset, gridSpacing,
                             deltaMag, backSeis, grTail, "no name", timeSpan);

  }

  /**
   * get the weight of the ERF at the specified index
   * @param index : index of ERF
   * @return : relative weight of ERF
   */
  public double getERF_RelativeWeight(int index) {
    return 1.0;
  }

  /**
   * Return the vector containing the Double values with
   * relative weights for each ERF
   * @return : Vector of Double values
   */
  public Vector getRelativeWeightsList() {
    Vector relativeWeight  = new Vector();
    for(int i=0; i<numIterations; i++)
      relativeWeight.add(new Double(1.0));
    return relativeWeight;
  }


  /**
   * This method sets the time-span field.
   * @param time
   */
  public void setTimeSpan(TimeSpan time) {
    this.timeSpan=time;
  }

}