package org.scec.sha.gui.servlets.erf;

import java.util.*;

import org.scec.param.*;

/**
 * <p>Title: STEP_ERF_AdjustableParamClass</p>
 * <p>Description: This class creates the Adjustable Params for the
 * STEP_ERF.</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class STEP_ERF_AdjustableParamClass {


  // seismicity type parameter stuff
  public final static String SEIS_TYPE_NAME = new String ("Seismicity Type");
  public final static String SEIS_TYPE_ADD_ON = new String ("STEP Add-On Rates");
  public final static String SEIS_TYPE_BACKGROUND = new String ("Background Rates");
  public final static String SEIS_TYPE_BOTH = new String ("Both");
  public final static String SEIS_TYPE_INFO = new String ("Seismicity-type to use in the forecast");
  private StringParameter seisTypeParam;


// minimum magnitude parameter stuff
  public final static String MIN_MAG_PARAM_NAME ="Minimum Magnitude";
  public Double MIN_MAG_PARAM_DEFAULT = new Double(4.0);
  private final static String MIN_MAG_PARAM_UNITS = null;
  private final static String MIN_MAG_PARAM_INFO = "The minimum magnitude to be considered (those below are ignored)";
  private final static double MIN_MAG_PARAM_MIN = 4.0;
  private final static double MIN_MAG_PARAM_MAX = 8.0;
  DoubleParameter minMagParam;

  private ParameterList adjustableParams;

  //class default constructor to initialise the ParameterList
  public STEP_ERF_AdjustableParamClass() {
    initAdjParams();
  }

  //initialises the Adjustable Param List for the USGS/CGS 1996 forecast model.
  private void initAdjParams() {

    adjustableParams= new ParameterList();
    // add the adjustable parameters to the list
    // make the seisTypeParam
    Vector seisOptionsStrings = new Vector();
    seisOptionsStrings.add(SEIS_TYPE_ADD_ON);
    seisOptionsStrings.add(SEIS_TYPE_BACKGROUND);
    seisOptionsStrings.add(SEIS_TYPE_BOTH);
    StringConstraint constraint = new StringConstraint(seisOptionsStrings);
    seisTypeParam = new StringParameter(SEIS_TYPE_NAME,constraint,SEIS_TYPE_ADD_ON);
    seisTypeParam.setInfo(SEIS_TYPE_INFO);


    // make the minMagParam
    minMagParam = new DoubleParameter(MIN_MAG_PARAM_NAME,MIN_MAG_PARAM_MIN,
                                      MIN_MAG_PARAM_MAX, MIN_MAG_PARAM_DEFAULT);
    minMagParam.setInfo(MIN_MAG_PARAM_INFO);

    //add these to the adjustable parameters list
    adjustableParams.addParameter(seisTypeParam);
    adjustableParams.addParameter(minMagParam);
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
