package org.scec.sha.gui.infoTools;

import java.text.DecimalFormat;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.param.*;
/**
 * <p>Title: DefaultHazardCurveForIMTs</p>
 * <p>Description: This class provides the provide the default X values for the selected
 * IMT. The discretization is done in the </p>
 * @author : Edward (Ned) Field and Nitin  Gupta
 * @created : Nov 24,2003
 * @version 1.0
 */

public final class DefaultHazardCurveForIMTs {

  private final static String SA_NAME = "SA";
  private final static String PGA_NAME = "PGA";
  private final static String PGV_NAME = "PGV";

  //Default values for the SA and PGA
  private final static double MIN_SA_PGA = .0001;
  private final static double MAX_SA_PGA = 10;
  private final static double NUM_SA_PGA = 51;

  //Default values for the PGV
  private final static double MIN_PGV = .01;
  private final static double MAX_PGV = 1000;
  private final static double NUM_PGV = 51;

  private double discretization_pga_sa;
  private double discretization_pgv;
  private DecimalFormat format = new DecimalFormat("0.00000##");

  public DefaultHazardCurveForIMTs() {
    discretization_pga_sa = (Math.log(MAX_SA_PGA) - Math.log(MIN_SA_PGA))/(NUM_SA_PGA-1);
    discretization_pgv = (Math.log(MAX_PGV) - Math.log(MIN_PGV))/(NUM_PGV-1);
    format.setMaximumFractionDigits(5);
  }

  /**
   * This function returns the ArbitrarilyDiscretizedFunc X values for the Hazard
   * Curve in the linear space after discretizing them in the log space.
   * @param param : Selected IMT Param
   * @return
   */
  public  ArbitrarilyDiscretizedFunc getHazardCurve(ParameterAPI param){
    String paramVal =(String)param.getValue();
   return getHazardCurve(paramVal);
  }

  /**
   * This function returns the ArbitrarilyDiscretizedFunc X values for the Hazard
   * Curve in the linear space after discretizing them in the log space.
   * @param imtName : Name of the selected IMT
   * @return
   */
  public ArbitrarilyDiscretizedFunc getHazardCurve(String imtName){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    if(imtName.equals(SA_NAME) || imtName.equals(PGA_NAME)){
      for(int i=0; i < NUM_SA_PGA ;++i){
        double xVal =Double.parseDouble(format.format(Math.exp(Math.log(MIN_SA_PGA)+i*discretization_pga_sa)));
        function.set(xVal,1.0);
      }
      return function;
    }
    else if((imtName.equals(PGV_NAME))){
      for(int i=0; i < NUM_PGV ;++i){
        double xVal = Double.parseDouble(format.format(Math.exp(Math.log(MIN_PGV)+i*discretization_pgv)));
        function.set(xVal,1.0);
      }
      return function;
    }
    return null;
  }

  /**
   *
   * @returns the minimum X Value for SA
   */
  public static double getSA_Min(){
    return MIN_SA_PGA;
  }

  /**
   *
   * @returns the maximum X Value for SA
   */
  public static double getSA_Max(){
    return MAX_SA_PGA;
  }

  /**
   *
   * @returns the total number of default X Values for SA
   */
  public static double getSA_NUM(){
    return NUM_SA_PGA;
  }

  /**
   *
   * @returns the minimum X Value for PGA
   */
  public static double getPGA_Min(){
    return MIN_SA_PGA;
  }

  /**
   *
   * @returns the maximum X Value for PGA
   */
  public static double getPGA_Max(){
    return MAX_SA_PGA;
  }

  /**
   *
   * @returns the total number of default X Values for PGA
   */
  public static double getPGA_NUM(){
    return NUM_SA_PGA;
  }

  /**
   *
   * @returns the minimum X Value for PGV
   */
  public static double getPGV_Min(){
    return MIN_PGV;
  }

  /**
   *
   * @returns the maximum X Value for PGV
   */
  public static double getPGV_Max(){
    return MAX_PGV;
  }

  /**
   *
   * @returns the total number of default X Values for PGV
   */
  public static double getPGV_Num(){
    return NUM_PGV;
  }

  //added for debugging purposes
  public static void main(String args[]){
    DefaultHazardCurveForIMTs hazardCurve = new DefaultHazardCurveForIMTs();
    ArbitrarilyDiscretizedFunc func = hazardCurve.getHazardCurve("SA");
    System.out.println("For SA and PGA: ");
    System.out.println("Dis: "+hazardCurve.discretization_pga_sa);
    System.out.println(func.toString());
    func = hazardCurve.getHazardCurve("PGV");
    System.out.println("For PGV: ");
    System.out.println("Dis: "+hazardCurve.discretization_pgv);
    System.out.println(func.toString());
  }
}