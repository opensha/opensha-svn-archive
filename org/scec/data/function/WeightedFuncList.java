package org.scec.data.function;

import java.util.*;

import org.scec.calc.FractileCurveCalculator;

/**
 * <p>Title: WeightedFuncList</p>
 * <p>Description: This class stores the epistemic lists with their uncertainties.
 * This class provides the collective info. for whole list. One can give a
 * arbitrary list for which percentiles have to be calculated.
 * </p>
 * @author : Nitin Gupta
 * @created September 9, 2004.
 * @version 1.0
 */

public class WeightedFuncList {

  //relative Wts for each
  private ArrayList relativeWts = new ArrayList();

  //Discretized list of functions for individual curves
  private DiscretizedFuncList functionList = new DiscretizedFuncList();

  //Discretized list of function for each percentile calculated
  private DiscretizedFuncList percentileList = new DiscretizedFuncList();

  //Discrrtized function to store the Mean function
  private ArbitrarilyDiscretizedFunc meanFunction;


  // Error Strings to be dispalyed
  private final static String ERROR_WEIGHTS =
      "Error! Number of weights should be equal to number of curves";

  private FractileCurveCalculator fractileCalc;

  /**
   * list of fractions at which we need to calculate the percentiles
   */
  private ArrayList fractionList = new ArrayList();


  //checks if mean percentile was calculated or not.
  private boolean isMeanPercentileCalculated = false;

  //weighted function info
  private String info=null;

  public WeightedFuncList() {}

  /**
   * Adds the list of relative weights and functionList to the existing list of
   * relative wt list and list of function.
   * It does not remove the existing dataset but adds new data to it.
   * The size of relative Weight list should be equal to the number of functions
   * in the functionList,
   * @param relWts : ArrayList of doubles for the relative wts of each function
   * in the list.
   * @param funcList : List of individual functions
   */
  public void addList(ArrayList relWts,DiscretizedFuncList funcList){

    int size = relativeWts.size();
    if(size != functionList.size()){
      throw new RuntimeException(ERROR_WEIGHTS);
    }

    //if the size of both list are same then add them to their corresponding lists.
    for(int i=0;i<size;++i){
      relativeWts.add(relWts.get(i));
      DiscretizedFuncAPI function = funcList.get(i);
      if(isFuncAllowed(function))
        functionList.add(function);
    }
    //if person has already calculated the percentiles and then adding more functions to the
    //existing list then we need to calculate the percentiles again and remove the existing
    //list of percentiles. We recompute percentiles for all the functions in the list if
    //any new function is added.
    if(percentileList.size() >0){
      percentileList.clear();
      addPercentiles(fractionList);
    }
    //if mean has already been calculated for the existing function list then on addition of
    //new function will result in automatic re-computation mean percentile.
    if(isMeanPercentileCalculated)
      addMean();

  }

  /**
   * Add Discretizedfunction and relative weight to the existing list of discretized functions
   * and relative wt list.
   * @param relWt
   * @param func
   */
  public void add(double relWt,DiscretizedFuncAPI func){
    relativeWts.add(new Double(relWt));
    if(isFuncAllowed(func))
      functionList.add(func);

    //if person has already calculated the percentiles and then adding more functions to the
    //existing list then we need to calculate the percentiles again and remove the existing
    //list of percentiles. We recompute percentiles for all the functions in the list if
    //any new function is added.
    if(percentileList.size() >0){
      percentileList.clear();
      addPercentiles(fractionList);
    }
    //if mean has already been calculated for the existing function list then on addition of
    //new function will result in automatic re-computation mean percentile.
    if(isMeanPercentileCalculated)
      addMean();
  }



  /**
   * Makes sure that each function has equal number of numPoints otherwise it will
   * return false and won't add the function to the list.
   * @param function
   * @return
   */
  public boolean isFuncAllowed(DiscretizedFuncAPI function){
    // check  that all curves in list have same number of X values
    int listSize= functionList.size();
    if(listSize !=0){
      int numPoints = ((DiscretizedFuncAPI)functionList.get(0)).getNum();
      for(int i=1; i<listSize; ++i)
        if(function.getNum()!=numPoints)
          return false;
    }
    return true;
  }


  /**
   * sets the fractile curve calculation
   */
  private void setFractileCurveCalcuations(){
    if(fractileCalc !=null)
      fractileCalc = new FractileCurveCalculator(functionList,relativeWts);
    else
      fractileCalc.set(functionList,relativeWts);
  }


  /**
   * This function saves the fraction for which percentile has to be computed.
   * It then adds this calculated percentile in a DiscretizedFunctionList
   * @param fraction
   */
  public void addPercentile(double fraction){
    fractionList.add(new Double(fraction));
    setFractileCurveCalcuations();
    percentileList.add(fractileCalc.getFractile(fraction));
  }

  /**
   * This function saves the list of fraction list for which percentile needed to be calculated.
   * It then adds this calculated percentiles in a DiscretizedFunctionList.
   * @param fractionList: List of fraction (Doubles) for which we need to compute
   * percentile.
   */
  public void addPercentiles(ArrayList list){
    int size  = list.size();
    setFractileCurveCalcuations();
    for(int i=0;i<size;++i){
      fractionList.add(list.get(i));
      double fraction = ((Double)list.get(i)).doubleValue();
      percentileList.add(fractileCalc.getFractile(fraction));
    }

    //creating and setting the info for the percentileList
    String percentileInfo = "Total Number of percentile calculated: "+percentileList.size()+" for "+
                            "following fractions: \n";
    for(int i=0;i<fractionList.size();++i){
      percentileInfo +=(Double)fractionList.get(i)+", ";
    }
    percentileInfo = percentileInfo.substring(0,percentileInfo.length()-2)+"\n";
    percentileList.setInfo("percentileInfo");
  }



  /**
   * Calculates mean percentile
   * @returns the mean percentile from the list of functions.
   */
  public void addMean(){
    setFractileCurveCalcuations();
    meanFunction = fractileCalc.getMeanCurve();
    isMeanPercentileCalculated = true;
    String meanInfo = "Mean percentile calculated \n";
  }


  /**
   * This function returns the weighted functions list without the
   * fractile and mean functions.
   * @return
   */
  public DiscretizedFuncList getWeightedFunctionList(){
    return functionList;
  }

  /**
   * This function returns the list of function for which percentiles were computed.
   * @return
   */
  public DiscretizedFuncList getPercentileList(){
    if(percentileList.size() >0)
      return percentileList;
    return null;
  }


  /**
   *
   * @returns the mean percentile function if it was computed
   */
  public DiscretizedFuncAPI getMean(){
    if(isMeanPercentileCalculated)
      return meanFunction;
    return null;
  }


  /**
   *
   * @returns the relative weights array associated with each function in the list.
   */
  public ArrayList getRelativeWtList(){
    return relativeWts;
  }

  /**
   *
   * @returns the number of functions in the list with relative wts associated with them
   */
  public int getNumWeightedFunctions(){
    return functionList.size();
  }

  /**
   *
   * @returns total number of functions for which percentile was computed
   * This number return does not take into account if mean fractile was calculated.
   */
  public int getNumFractileFunctions(){
    return percentileList.size();
  }


  /**
   *
   * It clears all the fraction list for which percentiles were computed.
   * It also clears teh percentile list.
   *
   * Once this function has been called, user has to give a new list of fraction
   * for which percentiles are to be computed.
   */
  public void removeAllPercentiles(){
    fractionList.clear();
    percentileList.clear();
  }


  /**
   * Remove the mean percentile and sets it to null.
   */
  public void removeMean(){
    if(isMeanPercentileCalculated)
      meanFunction= null;
  }

  /**
   * Clears the weighted functions in DiscretizedFunction List and also clears their
   * associated relative weight list.
   */
  public void clearWeightedFunctionList(){
    relativeWts.clear();
    functionList.clear();
  }


  /**
   *
   * @returns boolean. true if mean curve was calculated and false if not.
   */
  public boolean isMeanFractileFunctionCalculated(){
    return isMeanPercentileCalculated;
  }

  /**
   * Sets the info related to this weighted function list
   * @param info
   */
  public void setInfo(String info){
    this.info = info;
  }

  /**
   *
   * @returns the info associated with this weighted function list, otherwise
   * return null.
   */
  public String getInfo(){
    return info;
  }

}