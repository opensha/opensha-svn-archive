package org.scec.sha.earthquake.rupForecastImpl.step;

import java.io.*;
import javax.swing.JOptionPane;
import java.util.*;
import java.text.DecimalFormat;
/**
 * <p>Title: STEP_BackSiesDataAdditionObject</p>
 * <p>Description: This Class adds the BackSies ERF dataset the Step ERF dataSet</p>
 * @author : Edward (Ned) Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 29,2003
 * @version 1.0
 */

public class STEP_BackSiesDataAdditionObject {

  // message to display if no data exits
  private static final String NO_DATA_EXISTS = "No Hazard Map Data Exists";

  //String to store the Metadata,Lats and Lons for the Step DataSet
  String stepMetaData;

  //Stores the Region definition for the step
  String stepLatitude;
  String stepLongitude;

  //String to store the Metadata,Lats and Lons for the Step DataSet
  String backSiesMetaData;

  //Stores the Region definition for the step
  String backSeisLatitude;
  String backSiesLongitude;


  //default class constructor
  public STEP_BackSiesDataAdditionObject() {
  }


  /**
   * Adds the 2 dataset for the backSies and Step and generates a final dataset
   * @param backSies : Vector for the Step BackGround Prob
   * @param step : Vector for the Step Addon Prob
   * backGround and Addon
   */
  public Vector addDataSet(Vector backSiesDataSet,Vector stepDataSet){
    Vector resultSet = new Vector();
    int size = backSiesDataSet.size();
    for(int i=0;i<size;++i){
      double backSiesProb = ((Double)backSiesDataSet.get(i)).doubleValue();
      double stepAddonProb = ((Double)stepDataSet.get(i)).doubleValue();
      double finalProb = backSiesProb + stepAddonProb - (backSiesProb)*(stepAddonProb);
      resultSet.add(new Double(finalProb));
    }
    return resultSet;
  }

}
