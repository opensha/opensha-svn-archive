package org.scec.sha.gui.infoTools;

import java.text.DecimalFormat;

import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.param.*;
import org.scec.sha.imr.attenRelImpl.*;
/**
 * <p>Title: IMT_Info</p>
 * <p>Description: This class provides the default X values for the selected
 * IMT. The discretization is done in the </p>
 * @author : Edward (Ned) Field and Nitin  Gupta
 * @created : Nov 24,2003
 * @version 1.0
 */

public final class IMT_Info {

  private String S = "IMT_Info()";

  //Default values for the SA and PGA
  private final static double MIN_SA_PGA = .0001;
  private final static double MAX_SA_PGA = 10;
  private final static double NUM_SA_PGA = 51;

  //Default values for the PGV
  private final static double MIN_PGV = .01;
  private final static double MAX_PGV = 1000;
  private final static double NUM_PGV = 51;

  // default values for WC94_DisplMagRel FAULT_DISPL_NAME
  private final static double MIN_FAULT_DISPL = .001;
  private final static double MAX_FAULT_DISPL = 100;
  private final static double NUM_FAULT_DISPL = 51;

  //default values for the ShakeMapAttenRel MMI
  private final static double MIN_MMI = 1;
  private final static double MAX_MMI = 10;
  private final static double NUM_MMI = 51;

  private double discretization_pga_sa;
  private double discretization_pgv;
  private double discretization_fault_displ;
  private double discretization_mmi;

  private DecimalFormat format = new DecimalFormat("0.00000##");

  public IMT_Info() {
    discretization_pga_sa = (Math.log(MAX_SA_PGA) - Math.log(MIN_SA_PGA))/(NUM_SA_PGA-1);
    discretization_pgv = (Math.log(MAX_PGV) - Math.log(MIN_PGV))/(NUM_PGV-1);
    discretization_fault_displ = (Math.log(MAX_FAULT_DISPL) - Math.log(MIN_FAULT_DISPL))/(NUM_FAULT_DISPL-1);
    discretization_mmi = (Math.log(MAX_MMI) - Math.log(MIN_MMI))/(NUM_MMI-1);
    format.setMaximumFractionDigits(5);
  }

  /**
   * This function returns the ArbitrarilyDiscretizedFunc X values for the Hazard
   * Curve in the linear space after discretizing them in the log space.
   * @param param : Selected IMT Param
   * @return
   */
  public  ArbitrarilyDiscretizedFunc getDefaultHazardCurve(ParameterAPI imtParam){
    String paramVal =(String)imtParam.getValue();
   return getDefaultHazardCurve(paramVal);
  }

  /**
   * This function returns the ArbitrarilyDiscretizedFunc X values for the Hazard
   * Curve in the linear space after discretizing them in the log space.
   * @param imtName : Name of the selected IMT
   * @return
   */
  public ArbitrarilyDiscretizedFunc getDefaultHazardCurve(String imtName){
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    if(imtName.equals(AttenuationRelationship.SA_NAME) || imtName.equals(AttenuationRelationship.PGA_NAME)){
      for(int i=0; i < NUM_SA_PGA ;++i){
        double xVal =Double.parseDouble(format.format(Math.exp(Math.log(MIN_SA_PGA)+i*discretization_pga_sa)));
        function.set(xVal,1.0);
      }
      return function;
    }
    else if((imtName.equals(AttenuationRelationship.PGV_NAME))){
      for(int i=0; i < NUM_PGV ;++i){
        double xVal = Double.parseDouble(format.format(Math.exp(Math.log(MIN_PGV)+i*discretization_pgv)));
        function.set(xVal,1.0);
      }
      return function;
    }
    else if((imtName.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))){
      for(int i=0; i < NUM_FAULT_DISPL ;++i){
        double xVal = Double.parseDouble(format.format(Math.exp(Math.log(MIN_FAULT_DISPL)+i*discretization_fault_displ)));
        function.set(xVal,1.0);
      }
      return function;
    }
    else if((imtName.equals(ShakeMap_2003_AttenRel.MMI_NAME))){
      for(int i=0; i < NUM_MMI ;++i){
        double xVal = Double.parseDouble(format.format(Math.exp(Math.log(MIN_MMI)+i*discretization_mmi)));
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
  public static double getSA_Num(){
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
  public static double getPGA_Num(){
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

  /**
   *
   * @returns the minimum X Value for WC94_DisplMagRel Fault Displ
   */
  public static double getFaultDispl_Min(){
    return MIN_FAULT_DISPL;
  }

  /**
   *
   * @returns the maximum X Value for WC94_DisplMagRel Fault Displ
   */
  public static double getFaultDispl_Max(){
    return MAX_FAULT_DISPL;
  }

  /**
   *
   * @returns the total number of default X Values for WC94_DisplMagRel Fault Displ
   */
  public static double getFaultDispl_Num(){
    return NUM_FAULT_DISPL;
  }


  /**
   *
   * @returns the minimum X Value for MMI
   */
  public static double getMMI_Min(){
    return MIN_MMI;
  }

  /**
   *
   * @returns the maximum X Value for MMI
   */
  public static double getMMI_Max(){
    return MAX_MMI;
  }

  /**
   *
   * @returns the total number of default X Values for MMI
   */
  public static double getMMI_Num(){
    return NUM_MMI;
  }


  /**
   *  Returns the minimum default value for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getMinIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return getSA_Min();
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return getPGA_Min();
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return getPGV_Min();
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return getFaultDispl_Min();
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return getMMI_Min();
    return 0;
  }

  /**
   *  Returns the maximum default value for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getMaxIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return getSA_Max();
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return getPGA_Max();
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return getPGV_Max();
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return getFaultDispl_Max();
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return getMMI_Max();
    return 0;
  }

  /**
   * Returns the total number of values for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getNumIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return getSA_Num();
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return getPGA_Num();
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return getPGV_Num();
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return getFaultDispl_Num();
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return getMMI_Num();
    return 0;
  }

  /**
   *
   * @param imt : Name of the seleceted IMT
   * @return true if the selected IMT is PGA, PGV or SA
   * else returns false
   */
  public static boolean isIMT_LogNormalDist(String imt){
    if(imt.equalsIgnoreCase(AttenuationRelationship.PGA_NAME) ||
       imt.equalsIgnoreCase(AttenuationRelationship.PGV_NAME) ||
       imt.equalsIgnoreCase(AttenuationRelationship.SA_NAME)  ||
       imt.equalsIgnoreCase(ShakeMap_2003_AttenRel.MMI_NAME)  ||
       imt.equalsIgnoreCase(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return true;
    return false;
  }

  /**
   *
   * @param imtParam : IMT Parameter
   * @return true if the selected IMT is PGA, PGV or SA
   * else returns false
   */
  public static boolean isIMT_LogNormalDist(ParameterAPI imtParam){
     String paramVal =(String)imtParam.getValue();
    return isIMT_LogNormalDist(paramVal);
  }

  //added for debugging purposes
  public static void main(String args[]){
    IMT_Info hazardCurve = new IMT_Info();
    ArbitrarilyDiscretizedFunc func = hazardCurve.getDefaultHazardCurve("SA");
    System.out.println("For SA and PGA: ");
    System.out.println("Dis: "+hazardCurve.discretization_pga_sa);
    System.out.println(func.toString());
    func = hazardCurve.getDefaultHazardCurve("PGV");
    System.out.println("For PGV: ");
    System.out.println("Dis: "+hazardCurve.discretization_pgv);
    System.out.println(func.toString());
  }
}
