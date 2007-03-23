package org.opensha.sha.gui.controls;


import java.util.*;

import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.sha.gui.beans.*;
import org.opensha.sha.calc.IM_EventSetCalc.IM_EventSetScenarioForCEA;
import org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF;
import org.opensha.calc.magScalingRelations.magScalingRelImpl.*;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.opensha.sha.param.editor.SimpleFaultParameterEditor;
import org.opensha.sha.param.editor.MagFreqDistParameterEditor;
import org.opensha.sha.param.*;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Field_2000_AttenRel;
import org.opensha.data.Location;
import org.opensha.data.Direction;
import org.opensha.calc.RelativeLocation;
import org.opensha.sha.fault.*;


/**
 * <p>Title:IM_EventSetCEA_ControlPanel </p>
 * <p>Description: It tests IMEventSetScenario </p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class IM_EventSetCEA_ControlPanel {

  //for debugging
  protected final static boolean D = false;


  private EqkRupSelectorGuiBean erfGuiBean;
  private AttenuationRelationshipGuiBean imrGuiBean;
  private SitesInGriddedRectangularRegionGuiBean regionGuiBean;
  private MapGuiBean mapGuiBean;

  private SimpleFaultData simpleFaultData;
  private double aveDipDir;

  //default magnitude.
  private double magnitude = 7.15;

  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, AttenuationRelationshipGuiBean, SitesInGriddedRectangularRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   * @param MapGuiBean
   */
  public IM_EventSetCEA_ControlPanel(EqkRupSelectorGuiBean erfGuiBean,
      AttenuationRelationshipGuiBean imrGuiBean, SitesInGriddedRectangularRegionGuiBean regionGuiBean,
      MapGuiBean mapGuiBean) {
    //getting the instance for variuos GuiBeans from the applet required to set the
    //default values for the Params for the Puente Hills Scenario.
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    this.mapGuiBean = mapGuiBean;
    //mkFaultTrace();
  }






  /**
   * Sets the default Parameters in the Application for the Puente Hill Scenario
   */
  public void setParamsForSanAndreasScenario(){
    //making the ERF Gui Bean Adjustable Param not visible to the user, becuase
    //this control panel will set the values by itself.
    //This is done in the EqkRupSelectorGuiBean
    ParameterEditor paramEditor = erfGuiBean.getParameterEditor(erfGuiBean.RUPTURE_SELECTOR_PARAM_NAME);
    paramEditor.setValue(erfGuiBean.CREATE_RUPTURE);
    paramEditor.refreshParamEditor();
    EqkRuptureCreationPanel erfPanel= (EqkRuptureCreationPanel)erfGuiBean.getEqkRuptureSelectorPanel();

    //changing the ERF to SimpleFaultERF
    paramEditor = erfPanel.getParameterEditor(erfPanel.SRC_TYP_PARAM_NAME);
    paramEditor.setValue(erfPanel.FINITE_SRC_NAME);
    paramEditor.refreshParamEditor();


    // Set rake value to 90 degrees
    erfPanel.getParameter(erfPanel.RAKE_PARAM_NAME).setValue(new Double(90));

    IM_EventSetScenarioForCEA eventSet = new IM_EventSetScenarioForCEA(); 
    SimpleFaultParameter faultParameter = eventSet.createSimpleFaultParam();
    erfPanel.getParameterEditor(erfPanel.FAULT_PARAM_NAME).setParameter(faultParameter);
    erfPanel.getParameter(erfPanel.FAULT_PARAM_NAME).setValue(faultParameter.getValue());
     
    erfPanel.getParameter(erfPanel.MAG_PARAM_NAME).setValue(new Double(magnitude));
    erfPanel.getParameterListEditor().refreshParamEditor();


    //checking if the single AttenRel is selected
    boolean isSingleAttenRelSelected =imrGuiBean.isSingleAttenRelTypeSelected();
    //if single attenRel gui is not selected then toggle to the single attenRel gui Panel
    if(!isSingleAttenRelSelected)
      imrGuiBean.toggleBetweenSingleAndMultipleAttenRelGuiSelection();
    // Set the imt as PGA
    ParameterListEditor editor = imrGuiBean.getIntensityMeasureParamEditor();
    editor.getParameterList().getParameter(imrGuiBean.IMT_PARAM_NAME).setValue(AttenuationRelationship.PGA_NAME);
    editor.refreshParamEditor();
    //Updating the IMR Gui Bean with the ShakeMap attenuation relationship
    imrGuiBean.setIMR_Selected(BA_2006_AttenRel.NAME);
    imrGuiBean.getSingleAttenRelParamListEditor().refreshParamEditor();

    //Updating the SitesInGriddedRectangularRegionGuiBean with the Puente Hills resion setting
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(33));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(35));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-119));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-117));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.01667));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.SITE_PARAM_NAME).setValue(regionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE);


    // Set some of the mapping params:
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.CPT_FILE_PARAM_NAME).
    setValue(GMT_MapGenerator.CPT_FILE_MAX_SPECTRUM);
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME).
    setValue(GMT_MapGenerator.COLOR_SCALE_MODE_FROMDATA);
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.GMT_WEBSERVICE_NAME).setValue(new Boolean(true));
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.LOG_PLOT_NAME).setValue(new Boolean(false));
    mapGuiBean.refreshParamEditor();
  }
}
