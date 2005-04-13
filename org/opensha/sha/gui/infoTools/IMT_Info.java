package org.opensha.sha.gui.infoTools;

import java.text.DecimalFormat;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.param.*;
import org.opensha.sha.imr.attenRelImpl.*;
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
  public final static double MIN_SA = .0001;
  public final static double MAX_SA = 10;
  public final static double NUM_SA = 51;
  public final static double DEFAULT_SA = 0.1;

  //Default values for the PGA
  public final static double MIN_PGA = .0001;
  public final static double MAX_PGA = 10;
  public final static double NUM_PGA = 51;
  public final static double DEFAULT_PGA = 0.1;


  //Default values for the PGV
  public final static double MIN_PGV = .01;
  public final static double MAX_PGV = 1000;
  public final static double NUM_PGV = 51;
  public final static double DEFAULT_PGV = 50;

  // default values for WC94_DisplMagRel FAULT_DISPL_NAME
  public final static double MIN_FAULT_DISPL = .001;
  public final static double MAX_FAULT_DISPL = 100;
  public final static double NUM_FAULT_DISPL = 51;
  public final static double DEFAULT_FAULT_DISPL = 1.0;


  //default values for the ShakeMapAttenRel MMI
  public final static double MIN_MMI = 1;
  public final static double MAX_MMI = 10;
  public final static double NUM_MMI = 51;
  public final static double DEFAULT_MMI = 7.0;

  public double discretization_pga;
  public double discretization_sa;
  public double discretization_pgv;
  public double discretization_fault_displ;
  public double discretization_mmi;

  private DecimalFormat format = new DecimalFormat("0.00000##");

  public IMT_Info() {
    discretization_pga = (Math.log(MAX_PGA) - Math.log(MIN_PGA))/(NUM_PGA-1);
    discretization_sa = (Math.log(MAX_SA) - Math.log(MIN_SA))/(NUM_SA-1);
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
    if(imtName.equals(AttenuationRelationship.SA_NAME)){
      for(int i=0; i < NUM_SA ;++i){
        double xVal =Double.parseDouble(format.format(Math.exp(Math.log(MIN_SA)+i*discretization_sa)));
        function.set(xVal,1.0);
      }
      return function;
    }
    else if(imtName.equals(AttenuationRelationship.PGA_NAME)){
      for(int i=0; i < NUM_PGA ;++i){
        double xVal =Double.parseDouble(format.format(Math.exp(Math.log(MIN_PGA)+i*discretization_pga)));
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
   *  Returns the minimum default value for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getMinIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return MIN_SA;
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return MIN_PGA;
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return MIN_PGV;
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return MIN_FAULT_DISPL;
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return MIN_MMI;
    return 0;
  }

  /**
   *  Returns the maximum default value for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getMaxIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return MAX_SA;
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return MAX_PGA;
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return MAX_PGV;
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return MAX_FAULT_DISPL;
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return MAX_MMI;
    return 0;
  }

  /**
   * Returns the total number of values for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getNumIMT_Val(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return NUM_SA;
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return NUM_PGA;
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return NUM_PGV;
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return NUM_FAULT_DISPL;
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return NUM_MMI;
    return 0;
  }

  /**
   * Returns the default values for the selectd IMT
   * @param imt: Selected IMT
   * @return
   */
  public static double getDefaultIMT_VAL(String imt){
    if(imt.equals(AttenuationRelationship.SA_NAME))
      return DEFAULT_SA;
    else if(imt.equals(AttenuationRelationship.PGA_NAME))
      return DEFAULT_PGA;
    else if(imt.equals(AttenuationRelationship.PGV_NAME))
      return DEFAULT_PGV;
    else if(imt.equals(WC94_DisplMagRel.FAULT_DISPL_NAME))
      return DEFAULT_FAULT_DISPL;
    else if(imt.equals(ShakeMap_2003_AttenRel.MMI_NAME))
      return DEFAULT_MMI;
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
    System.out.println("Dis: "+hazardCurve.discretization_pga);
    System.out.println(func.toString());
    func = hazardCurve.getDefaultHazardCurve("PGV");
    System.out.println("For PGV: ");
    System.out.println("Dis: "+hazardCurve.discretization_pgv);
    System.out.println(func.toString());
  }
}
