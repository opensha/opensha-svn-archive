package org.scec.sha.gui.controls;

import java.util.*;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.*;
import org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF;
import org.scec.calc.magScalingRelations.magScalingRelImpl.*;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.param.*;
import org.scec.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.*;
import org.scec.sha.magdist.SingleMagFreqDist;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;

/**
 * <p>Title: PuenteHillsTestControlPanel</p>
 * <p>Description: Sets the param value to replicate the official scenario shakemap
 * for the Puente Hill Scenario (http://www.trinet.org/shake/Puente_Hills_se)</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class PuenteHillsScenarioTestControlPanel {

  private EqkRupSelectorGuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private SitesInGriddedRegionGuiBean regionGuiBean;
  private MapGuiBean mapGuiBean;
  private IMT_GuiBean imtGuiBean;

  //class default constructor
  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, IMR_GuiBean, SitesInGriddedRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   * @param MapGuiBean
   * @param IMT_GuiBean
   */
  public PuenteHillsScenarioTestControlPanel(EqkRupSelectorGuiBean erfGuiBean, IMR_GuiBean imrGuiBean,
      SitesInGriddedRegionGuiBean regionGuiBean, MapGuiBean mapGuiBean, IMT_GuiBean imtGuiBean) {
    //getting the instance for variuos GuiBeans from the applet required to set the
    //default values for the Params for the Puente Hills Scenario.
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    this.mapGuiBean = mapGuiBean;
    this.imtGuiBean = imtGuiBean;
    //setParamsForPuenteHillsScenario();
  }

  /**
   * Sets the default Parameters in the Application for the Puente Hill Scenario
   */
  public void setParamsForPuenteHillsScenario(){
    //making the ERF Gui Bean Adjustable Param not visible to the user, becuase
    //this control panel will set the values by itself.
    //This is done in the EqkRupSelectorGuiBean
    erfGuiBean.showAllParamsForForecast(false);
    //changing the ERF ro SimpleFaultERF
    erfGuiBean.getParameterListEditor().getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);
    erfGuiBean.getParameterListEditor().refreshParamEditor();

    //Getting the instance for the editor that holds all the adjustable params for the selcetd ERF
    ERF_GuiBean erfParamGuiBean =erfGuiBean.getERF_ParamEditor();
    //As the Selecetd ERF is simple FaultERF so updating the rake value to -90 (so the ALL or UKNOWN category is used to be consistent with online shakemaps).
    erfParamGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(-90));
    erfParamGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(WC1994_MagLengthRelationship.NAME);

    //getting the instance for the SimpleFaultParameterEditorPanel from the GuiBean to adjust the fault Params
    SimpleFaultParameterEditorPanel faultPanel= erfParamGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
    //creating the Lat vector for the SimpleFaultParameter
    Vector lats = new Vector();
    lats.add(new Double(33.92690));
    lats.add(new Double(33.93150));
    lats.add(new Double(33.95410));
    lats.add(new Double(34.05860));

    //creating the Lon vector for the SimpleFaultParameter
    Vector lons = new Vector();
    lons.add(new Double(-117.86730));
    lons.add(new Double(-118.04320));
    lons.add(new Double(-118.14350));
    lons.add(new Double(-118.29760));

    //creating the dip vector for the SimpleFaultParameter
    Vector dips = new Vector();
    dips.add(new Double(25));

    //creating the depth vector for the SimpleFaultParameter
    Vector depths = new Vector();
    depths.add(new Double(5));
    depths.add(new Double(13));

    //setting the FaultParameterEditor with the default values for Puente Hills Scenario
    faultPanel.setAll(((SimpleFaultParameterCalculator)faultPanel.getParameter()).DEFAULT_GRID_SPACING,lats,lons,dips,depths,((SimpleFaultParameterCalculator)faultPanel.getParameter()).FRANKEL);
    faultPanel.refreshParamEditor();
    //updaing the faultParameter to update the faultSurface
    faultPanel.setEvenlyGriddedSurfaceFromParams();

    //updating the magEditor with the values for the Puente Hills Scenario
    MagFreqDistParameterEditor magEditor = erfParamGuiBean.getMagDistEditor();
    magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
    magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
    magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(7.1));
    erfParamGuiBean.refreshParamEditor();
    // now have the editor create the magFreqDist
    magEditor.setMagDistFromParams();

    //updating the EQK_RupSelectorGuiBean with the Source and Rupture Index respectively.
    erfGuiBean.setParamsInForecast(0,0);

    //Updating the IMR Gui Bean with the ShakeMap attenuation relationship.
    imrGuiBean.getParameterList().getParameter(imrGuiBean.IMR_PARAM_NAME).setValue(ShakeMap_2003_AttenRel.NAME);
    imrGuiBean.getSelectedIMR_Instance().getParameter(ShakeMap_2003_AttenRel.COMPONENT_NAME).setValue(ShakeMap_2003_AttenRel.COMPONENT_GREATER_OF_TWO_HORZ);
    imrGuiBean.refreshParamEditor();

    //Updating the SitesInGriddedRegionGuiBean with the Puente Hills resion setting
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(33.2));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(34.66));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-119.05));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-116.85));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.016667));
    regionGuiBean.getParameterList().getParameter(regionGuiBean.SITE_PARAM_NAME).setValue(regionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE);

    // Set the imt as PGA
    imtGuiBean.getParameterList().getParameter(imtGuiBean.IMT_PARAM_NAME).setValue(ShakeMap_2003_AttenRel.PGA_NAME);
    imtGuiBean.refreshParamEditor();

    // Set some of the mapping params:
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.GMT_WEBSERVICE_NAME).setValue(new Boolean(false));
    mapGuiBean.getParameterList().getParameter(GMT_MapGenerator.LOG_PLOT_NAME).setValue(new Boolean(false));
    mapGuiBean.refreshParamEditor();
  }
}