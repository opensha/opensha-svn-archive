package org.scec.sha.gui.servlets.erf;

import java.util.*;

import org.scec.param.*;

/**
 * <p>Title: STEP_ERF_AlaskanPipeAdjustableParamClass</p>
 * <p>Description: This class creates the Adjustable Params for the
 *  Alaskan Pipe STEP_ERF.</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class STEP_ERF_AlaskanPipeAdjustableParamClass {



  private ParameterList adjustableParams;

  //class default constructor to initialise the ParameterList
  public STEP_ERF_AlaskanPipeAdjustableParamClass() {
    initAdjParams();
  }

  //initialises the Adjustable Param List for the STEP forecast model.
  private void initAdjParams() {
    adjustableParams= new ParameterList();
  }

  /**
   *
   * @returns the Adjuatble ParamList for the STEP ERF model
   */
  public ParameterList getAdjustableParams(){
    return adjustableParams;
  }
}
