package org.scec.sha.gui.infoTools;

import java.util.*;

import org.scec.data.function.WeightedFuncList;


/**
 * <p>Title: WeightedFuncListforPlotting</p>
 * <p>Description: This class creates the plotting capabilities for Weighted function
 * list using the JFreechart classes</p>
 * @author : Ned Field, Nitin Gupta
 * @version 1.0
 */

public class WeightedFuncListforPlotting {

  //ArrayList that store the Discretized func list for wt func's ,list of fractiles and mean.
  private ArrayList funcList ;

  private WeightedFuncList weightedFuncList;

  /**
   *
   * @param weightedFuncList : WeightedFuncList object
   */
  public WeightedFuncListforPlotting(WeightedFuncList weightedFuncList) {
    this.weightedFuncList = weightedFuncList;
  }


  /**
   *
   * Based on the below boolean parameters it creates the function list from the
   * weighted function list class to be plotted using the JFreechart.
   * @param showIndividualCurves : boolean to see if individual curves needs to be plotted
   * @param showFractiles : boolean to see if fractile curves needs to be plotted
   * @param showMean : boolean to see if mean need to ne plotted
   */
  public void addFunctionForPlotting(boolean showIndividualCurves, boolean showFractiles,
                                     boolean showMean){

    funcList = new ArrayList();
    //adding individual curves if they needed to be added
    if(showIndividualCurves){
      ListIterator it =weightedFuncList.getWeightedFunctionList().listIterator();
      //while(it.hasNext())
      funcList.add(it.next());
    }
    //adding fractile function if they need to be shown
    if(showFractiles){
      ListIterator it =weightedFuncList.getFractileList().listIterator();
      //while(it.hasNext())
        funcList.add(it.next());
    }
    //adding mean function if they need to be shown
    if(showMean){
      if(weightedFuncList.isMeanFunctionCalculated())
        funcList.add(weightedFuncList.getMean());
    }
  }


  /**
   *
   * @returns the functionlist containing the list of functions from
   * WeightedFuncList object that needs to be plotted.
   */
  public ArrayList getFunctionListToPlot(){
    return funcList;
  }

  /**
   * return the info for the weighted function list
   */
  public String getInfo(){
    return weightedFuncList.getInfo();
  }
}