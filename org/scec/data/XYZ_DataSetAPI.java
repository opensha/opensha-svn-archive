package org.scec.data;

import java.util.*;
/**
 * <p>Title: XYZ_DataSetAPI</p>
 * <p>Description: This interface defines the DataSet for the X,Y and Z.
 * It is the quick and dirty solution for the time being, and we need to fix
 * it with the same fuctionality as our 2-D representation of the data based
 * on the requirement.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public interface XYZ_DataSetAPI {


  //gets the Min of the  X values
  public double getMinX();

  //gets the Max of the X values
  public double getMaxX();

  //gets the Min of the Y values
  public double getMinY();

  //gets the Max of the Y values
  public double getMaxY();

  //gets the Min of the Z values
  public double getMinZ();

  //gets the Max of the Z values
  public double getMaxZ();

  //sets the X, Y , Z values as the vector of double
  public void setXYZ_DataSet(Vector xVals, Vector yVals,Vector zVals);

  //gets the X values dataSet
  public Vector getX_DataSet();

  //gets the Y values dataSet
  public Vector getY_DataSet();

  //gets the Z values dataSet
  public Vector getZ_DataSet();

  //returns true if size Vector for X,Y and Z dataset values is equal else return false
  public boolean checkXYZ_NumVals();
}