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

public class WeightedFuncList extends DiscretizedFuncList {

  //relative Wts for each
  private ArrayList relativeWts;

  // Error Strings to be dispalyed
  private final static String ERROR_WEIGHTS =
      "Error! Number of weights should be equal to number of curves";

  private FractileCurveCalculator fractileCalc;

  /**
   * list of percentiles at which we will be calculating the fractiles
   */
  private ArrayList percentileList;
  //if we have to calculate just one fratile for the given percentile
  private double percentile;

  private boolean isMeanFractileCalculated = false;

  public WeightedFuncList() {}

  /**
   * sets the relative wt for each function in the list
   * @param relativeWts
   */
  public void setRelativeWeights(ArrayList relativeWts){
    this.relativeWts = relativeWts;
    int size = relativeWts.size();
    if(size != size()){
      throw new RuntimeException(ERROR_WEIGHTS);
    }
  }




  /**
   * Currently only returns true. Further
   * functionality may be added in the future or by subclasses.
   * @param function
   * @return
   */
  public boolean isFuncAllowed(DiscretizedFuncAPI function){
    // check  that all curves in list have same number of X values
    int listSize= size();
    if(listSize !=0){
      int numPoints = ((DiscretizedFuncAPI)functions.get(0)).getNum();
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
      fractileCalc = new FractileCurveCalculator(this,relativeWts);
    else
      fractileCalc.set(this,relativeWts);
  }


  /**
   *  @returns the DiscretizedFuncList object constituting list of functions at the
   * fractions
   */
  public void addFractileCurveList(){
    int size = percentileList.size();
    for(int i=0;i<size;++i)
      functions.add(fractileCalc.getFractile(((Double)percentileList.get(i)).doubleValue()));
  }


  /**
   *
   * @param list : list of percentiles at which we need to calculate the fractiles
   */
  public void setPercentileList(ArrayList list){
    percentileList = list;
    setFractileCurveCalcuations();
  }

  /**
   *
   * @param percentile : percentile at which we need to calculate the fractile.
   */
  public void setPercentile(double percentile){
    this.percentile = percentile;
    setFractileCurveCalcuations();
  }

  /**
   *
   * @returns the DiscretizedFunction for the selected percentile
   */
  public  void addFractile(){
    functions.add(fractileCalc.getFractile(percentile));
  }


  /**
   *
   * @returns the mean fractile from the list of functions.
   */
  public void addMeanFractile(){
    functions.add(fractileCalc.getMeanCurve());
    isMeanFractileCalculated = true;
  }


  /**
   * This function returns the Weighted function in the list without the
   * fractile and mean functions.
   * @return
   */
  public DiscretizedFuncList getWeightedFunctionList(){
    DiscretizedFuncList functionList = new DiscretizedFuncList();
    //size of the relative wt array and functions array would be the same, as each function is
    //associated with some weight.
    int size = relativeWts.size();
    for(int i=0;i<size;++i)
      functionList.add((DiscretizedFuncAPI)functions.get(i));
    return functionList;
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
    return relativeWts.size();
  }

  /**
   *
   * @returns the number of fractile function in the list.
   * This number return does not take into account if mean fractile was calculated.
   */
  public int getNumFractileFunctions(){
    //get the total number of function in the list less the num of associted with relative wts.
    if(isMeanFractileCalculated)
      return size()-getNumWeightedFunctions()-1;
    else
      return size()-getNumWeightedFunctions();
  }

  /**
   *
   * @returns boolean. true if mean curve was calculated and false if not.
   */
  public boolean isMeanFractileFunctionCalculated(){
    return isMeanFractileCalculated;
  }
}