package org.scec.sha.calc;

import org.scec.data.function.DiscretizedFuncList;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.data.function.ArbDiscrEmpiricalDistFunc;

import java.util.Vector;
/**
 * <p>Title:  FractileCurveCalculator</p>
 * <p>Description: This class calculates fractiles based on various hazard curves
 * and their relative weights</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class FractileCurveCalculator {

  // function list to save the curves
  private DiscretizedFuncList funcList;
  // save the relative weight of each curve
  private Vector relativeWeights;
  // save the number of X values
  private int num;
  // vector to save the empirical distributions
  private Vector empiricalDists;

  // Error Strings to be dispalyed
  private final static String ERROR_WEIGHTS =
     "Error! Number of weights should be equal to number of curves";
  private final static String ERROR_LIST = "No curves exist in the list";
  private final static String ERROR_POINTS =
      "Number of points in each curve should be same";


  /**
   * Constructor : Calls the set function
   * @param functionList : List of curves for which fractile needs to be calculated
   * @param relativeWts : weight assigned to each curves. It expects the Vector
   *  to contain Double values
   */
  public FractileCurveCalculator(DiscretizedFuncList functionList,
                               Vector relativeWts) {
    set(functionList, relativeWts);
  }



  /**
   * It accepts the function list for curves and relative weight
   * assigned to each curve.
   * It checks for following condition :
   *   1. Number of weights = number of curves in  list
   *          (i.e. functionList.size() = relativeWts.size()  )
   *   2. Number of X values in all curves are same
   *
   * It makes following asssumption:
   *   X values for in the curves are same
   *
   * @param functionList : List of curves for which fractile needs to be calculated
   * @param relativeWts : weight assigned to each curves. It expects the Vector
   *  to contain Double values
   */
  public void set(DiscretizedFuncList functionList,
                                 Vector relativeWts) {


    // check that number of weights are equal to number of curves give
    if(functionList.size()!=relativeWts.size()) throw new RuntimeException(ERROR_WEIGHTS);

    // check that curve list is not empty
    int numFunctions = functionList.size();
    if(numFunctions==0) throw new RuntimeException(ERROR_LIST);

    // check  that all curves in list have same number of X values
    int numPoints = functionList.get(0).getNum();
    for(int i=1; i<numFunctions; ++i)
      if(functionList.get(i).getNum()!=numPoints) throw new RuntimeException(ERROR_POINTS);

     /* Save the functionlist
    It is deep cloned here. The reason being that the original function list
    can be changed in program without affecting this function list else whenever
    calling program changes its functionlist in any way, this program will be affected
    */
    this.funcList = functionList.deepClone();
    relativeWeights = relativeWts;
    // save the number of X values
    this.num = numPoints;

    //Vector for saving empirical distributions
    empiricalDists = new Vector();

    // make a empirical dist for each X value
    for(int i=0; i<num; ++i) {
      ArbDiscrEmpiricalDistFunc empirical = new ArbDiscrEmpiricalDistFunc();
      for(int j=0; j<numFunctions; ++j)
        empirical.set(funcList.get(j).getY(i),
                      ((Double)relativeWeights.get(j)).doubleValue());
      empiricalDists.add(empirical);
      System.out.println("111  i="+i+"; dist="+empirical.toString());
    }

  }

  /**
   *
   * @param fraction
   * @return
   */
  public ArbitrarilyDiscretizedFunc getFractile(double fraction) {
    // function to save the result
    ArbitrarilyDiscretizedFunc result = new ArbitrarilyDiscretizedFunc();
    for(int i=0; i<num; ++i) {
       System.out.println("2222  i="+i+"; dist="+((ArbDiscrEmpiricalDistFunc)empiricalDists.get(i)).toString());
      result.set(funcList.get(0).getX(i),
                 ((ArbDiscrEmpiricalDistFunc)empiricalDists.get(i)).getFractile(fraction));
    }
    return result;
  }
}