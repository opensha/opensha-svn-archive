package org.scec.data;

import java.util.*;

/**
 * <p>Title: ArbDiscretizedXYZ_DataSet</p>
 * <p>Description: This class creates a vector for the XYZ dataset.
 * FIX : FIX - The implementation is the quick and dirty solution for the time being and will needed to
 * modified later on based on our requirements.
 * Requires Fixation to be consistent with our implementation of the 2-D data representation</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class ArbDiscretizedXYZ_DataSet implements XYZ_DataSetAPI{

  Vector xValues, yValues, zValues;

  /**
   * default class constructor
   * @param xVals = Vector containing the xValues
   * @param yVals = Vector containing the yValues
   * @param zVals = Vector containing the zValues
   */
  public ArbDiscretizedXYZ_DataSet(Vector xVals, Vector yVals,
                                   Vector zVals) {

    xValues = xVals;
    yValues = yVals;
    zValues = zVals;
  }

  /**
   * Initialises the x, y and z Values Vector
   * @param xVals
   * @param yVals
   * @param zVals
   */
  public void setXYZ_DataSet(Vector xVals, Vector yVals, Vector zVals){

    xValues = xVals;
    yValues = yVals;
    zValues = zVals;
  }

  /**
   *
   * @returns the X Values dataSet
   */
  public Vector getX_DataSet(){
    return xValues;
  }

  /**
   *
   * @returns the Y value DataSet
   */
  public Vector getY_DataSet(){
    return yValues;
  }


  /**
   *
   * @returns the Z value DataSet
   */
  public Vector getZ_DataSet(){
    return zValues;
  }

  /**
   *
   * @returns the minimum of the X Values
   */
  public double getMinX(){
    return getMin(xValues);
  }

  /**
   *
   * @returns the maximum of the X Values
   */
  public double getMaxX(){
    return getMax(xValues);
  }

  /**
   *
   * @returns the minimum of the Y Values
   */
  public double getMinY(){
    return getMin(yValues);
  }

  /**
   *
   * @returns the maximum of the Y values
   */
  public double getMaxY(){
    return getMax(yValues);
  }

  /**
   *
   * @returns the minimum of the Z values
   */
  public double getMinZ(){
    return getMin(zValues);
  }

  /**
   *
   * @returns the maximum of the Z values
   */
  public double getMaxZ(){
    return getMax(zValues);
  }

  /**
   *
   * @returns true if size Vector for X,Y and Z dataset values is equal else return false
   */
  public boolean checkXYZ_NumVals(){
    if((xValues.size() == yValues.size()) && (xValues.size() == zValues.size()))
      return true;
    else
      return false;
  }

  /**
   * private function of the class that finds the minimum value in the Vector
   * @param xyz
   * @return
   */
  private double getMin(Vector xyz){
    int size = xyz.size();
    double min =((Double)xyz.get(0)).doubleValue();
    for(int i=1;i<size;++i){
      double val = ((Double)xyz.get(i)).doubleValue();
      if(val < min)
        min = val;
    }
    return min;
  }

  /**
   * private function of the class that finds the maximum value in the Vector
   * @param xyz
   * @return
   */
  private double getMax(Vector xyz){
    int size = xyz.size();
    double max =((Double)xyz.get(size-1)).doubleValue();
    for(int i=1;i<size;++i){
      double val = ((Double)xyz.get(i)).doubleValue();
      if(val > max)
        max = val;
    }
    return max;
  }


}