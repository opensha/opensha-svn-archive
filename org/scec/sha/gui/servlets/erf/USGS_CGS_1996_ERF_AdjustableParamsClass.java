package org.scec.sha.gui.servlets.erf;

import java.util.*;

import org.scec.param.*;

/**
 * <p>Title: USGS_CGS_1996_ERF_AdjustableParamsClass</p>
 * <p>Description: This class creates the Adjustable Params for the
 * USGS_CGS_1996_ERF.</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class USGS_CGS_1996_ERF_AdjustableParamsClass {

  // fault-model parameter stuff
  public final static String FAULT_MODEL_NAME = new String ("Fault Model");
  public final static String FAULT_MODEL_FRANKEL = new String ("Frankel's");
  public final static String FAULT_MODEL_STIRLING = new String ("Stirling's");
  // make the fault-model parameter
  Vector faultModelNamesStrings = new Vector();
  StringParameter faultModelParam;

  // fault-model parameter stuff
  public final static String BACK_SEIS_NAME = new String ("Background Seismicity");
  public final static String BACK_SEIS_INCLUDE = new String ("Include");
  public final static String BACK_SEIS_EXCLUDE = new String ("Exclude");
  public final static String BACK_SEIS_ONLY = new String ("Only Background");
  // make the fault-model parameter
  Vector backSeisOptionsStrings = new Vector();
  StringParameter backSeisParam;


  // For fraction of moment rate on GR parameter
  public final static String FRAC_GR_PARAM_NAME ="GR Fraction on B Faults";
  public Double DEFAULT_FRAC_GR_VAL= new Double(0.5);
  public final static String FRAC_GR_PARAM_UNITS = null;
  public final static String FRAC_GR_PARAM_INFO = "Fraction of moment-rate put into GR dist on class-B faults";
  public final static double FRAC_GR_PARAM_MIN = 0;
  public final static double FRAC_GR_PARAM_MAX = 1;
  DoubleParameter fracGR_Param;

  // For rupture offset lenth along fault parameter
  public final static String RUP_OFFSET_PARAM_NAME ="Rupture Offset";
  public Double DEFAULT_RUP_OFFSET_VAL= new Double(10);
  public final static String RUP_OFFSET_PARAM_UNITS = "km";
  public final static String RUP_OFFSET_PARAM_INFO = "Length of offset for floating ruptures";
  public final static double RUP_OFFSET_PARAM_MIN = 1;
  public final static double RUP_OFFSET_PARAM_MAX = 100;
  DoubleParameter rupOffset_Param;


  private ParameterList adjustableParams;

  //class default constructor to initialise the ParameterList
  public USGS_CGS_1996_ERF_AdjustableParamsClass() {
    initAdjParams();
  }

  //initialises the Adjustable Param List for the USGS/CGS 1996 forecast model.
  private void initAdjParams() {

    adjustableParams= new ParameterList();
    // add the adjustable parameters to the list
    faultModelNamesStrings.add(FAULT_MODEL_FRANKEL);
    faultModelNamesStrings.add(FAULT_MODEL_STIRLING);
    faultModelParam = new StringParameter(FAULT_MODEL_NAME, faultModelNamesStrings,
        (String)faultModelNamesStrings.get(0));

    backSeisOptionsStrings.add(BACK_SEIS_EXCLUDE);
    backSeisOptionsStrings.add(BACK_SEIS_INCLUDE);
    backSeisOptionsStrings.add(BACK_SEIS_ONLY);
    backSeisParam = new StringParameter(BACK_SEIS_NAME, backSeisOptionsStrings,BACK_SEIS_EXCLUDE);

    fracGR_Param = new DoubleParameter(FRAC_GR_PARAM_NAME,FRAC_GR_PARAM_MIN,
                                       FRAC_GR_PARAM_MAX,FRAC_GR_PARAM_UNITS,DEFAULT_FRAC_GR_VAL);
    fracGR_Param.setInfo(FRAC_GR_PARAM_INFO);

    rupOffset_Param = new DoubleParameter(RUP_OFFSET_PARAM_NAME,RUP_OFFSET_PARAM_MIN,
        RUP_OFFSET_PARAM_MAX,RUP_OFFSET_PARAM_UNITS,DEFAULT_RUP_OFFSET_VAL);
    rupOffset_Param.setInfo(RUP_OFFSET_PARAM_INFO);

    // add adjustable parameters to the list
    adjustableParams.addParameter(faultModelParam);
    adjustableParams.addParameter(fracGR_Param);
    adjustableParams.addParameter(rupOffset_Param);
    adjustableParams.addParameter(backSeisParam);
  }

  /**
   *
   * @returns the Adjuatble ParamList for the USGS/CGS 1996 ERF model
   */
  public ParameterList getAdjustableParams(){
    if(this.adjustableParams !=null)
      return adjustableParams;

    return null;
  }
}
