package org.scec.param;

import java.util.*;

import org.scec.exceptions.ParameterException;

/**
 * <p>Title: TreeBranchWeightsParameter</p>
 * <p>Description: This is a new parameter which contains the parameterList of the
 * different weights for the branches</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class TreeBranchWeightsParameter extends ParameterListParameter
    implements  java.io.Serializable{

  /** Class name for debugging. */
  protected final static String C = "TreeBranchWeightsParameter";
  /** If true print out debug statements. */
  protected final static boolean D = false;

  private double tolerance = .001;

  /**
   *  No constraints specified for this parameter. Sets the name of this
   *  parameter.
   *
   * @param  name  Name of the parameter
   */
  public TreeBranchWeightsParameter(String name) {
    super(name);
  }

  /**
   * No constraints specified, all values allowed. Sets the name and value.
   *
   * @param  name   Name of the parameter
   * @param  paramList  ParameterList  object
   */
  public TreeBranchWeightsParameter(String name, ParameterList paramList){
    super(name,paramList);
  }


  /**
   * checks whether all the Branch Weight Values sum to One.
   * @return
   */
  public boolean doWeightsSumToOne(){
    double sum =0;
    ParameterList paramList = (ParameterList)this.getValue();
    ListIterator it = paramList.getParametersIterator();
    while(it.hasNext()){
      ParameterAPI param =(ParameterAPI)it.next();
      sum += ((Double)param.getValue()).doubleValue();
    }

    //checks if the sum of the branch weights lies within the tolerenace
    double WeightOfBranches = 1.0;
    if((sum <= (WeightOfBranches + getTolerance()) )  && (sum>=WeightOfBranches - getTolerance()))
      return true;

    return false;

  }

  /**
   * sets the tolerance for the sums of the weights
   * @param tolerance
   */
  public void setTolerence(double tolerance){
    this.tolerance = tolerance;
  }

  /**
   * gets the tolerence for the sum of branch weights
   * @return
   */
  public double getTolerance(){
    return this.tolerance;
  }

}


