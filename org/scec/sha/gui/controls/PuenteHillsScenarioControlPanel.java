package org.scec.sha.gui.controls;

import java.util.*;

import org.scec.sha.gui.beans.EqkRupSelectorGuiBean;
import org.scec.sha.gui.beans.ERF_GuiBean;
import org.scec.sha.gui.beans.IMR_GuiBean;
import org.scec.sha.gui.beans.SitesInGriddedRegionGuiBean;
import org.scec.sha.earthquake.rupForecastImpl.SimplePoissonFaultERF;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.param.*;
import org.scec.sha.param.editor.gui.SimpleFaultParameterEditorPanel;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.MagFreqDistParameter;
import org.scec.sha.magdist.SingleMagFreqDist;
import org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel;

/**
 * <p>Title: PuenteHillsScenarioControlPanel</p>
 * <p>Description: Sets the Default params Value for the Puente Hill Scenario</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class PuenteHillsScenarioControlPanel {

  private EqkRupSelectorGuiBean erfGuiBean;
  private IMR_GuiBean imrGuiBean;
  private SitesInGriddedRegionGuiBean regionGuiBean;

  //class default constructor
  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, IMR_GuiBean, SitesInGriddedRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   */
  public PuenteHillsScenarioControlPanel(EqkRupSelectorGuiBean erfGuiBean, IMR_GuiBean imrGuiBean,
      SitesInGriddedRegionGuiBean regionGuiBean) {
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    setParamsForPuenteHillsScenario();
  }

  public void setParamsForPuenteHillsScenario(){
    erfGuiBean.showAllParamsForForecast(false);
    erfGuiBean.getParameterListEditor().getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);
    erfGuiBean.getParameterListEditor().synchToModel();
    ERF_GuiBean erfParamGuiBean =erfGuiBean.getERF_ParamEditor();
    erfParamGuiBean.getParameterList().getParameter(erfParamGuiBean.ERF_PARAM_NAME).setValue(SimplePoissonFaultERF.NAME);
    erfParamGuiBean.getParameterList().getParameter(SimplePoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(90));
    SimpleFaultParameterEditorPanel faultPanel= erfParamGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
    Vector lats = new Vector();
    lats.add(new Double(33.8995));
    lats.add(new Double(33.9122));
    lats.add(new Double(33.9381));
    lats.add(new Double(34.0200));

    Vector lons = new Vector();
    lons.add(new Double(-117.868));
    lons.add(new Double(-118.029));
    lons.add(new Double(-118.133));
    lons.add(new Double(-118.308));

    Vector dips = new Vector();
    dips.add(new Double(27));

    Vector depths = new Vector();
    depths.add(new Double(3));
    depths.add(new Double(15));

    faultPanel.setAll(faultPanel.DEFAULT_GRID_SPACING,lats,lons,dips,depths,faultPanel.STIRLING);
    faultPanel.synchToModel();
    faultPanel.setEvenlyGriddedSurfaceFromParams();
    MagFreqDistParameterEditor magEditor = erfParamGuiBean.getMagDistEditor();
    magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
    magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
    magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(7.5));
    erfParamGuiBean.synchToModel();
    // now have the editor create the magFreqDist
    magEditor.setMagDistFromParams();

    //updating the EQK_RupSelectorGuiBean
    erfGuiBean.setParamsInForecast(0,0);

    imrGuiBean.getParameterList().getParameter(imrGuiBean.IMR_PARAM_NAME).setValue(Campbell_1997_AttenRel.NAME);
    imrGuiBean.synchToModel();

    regionGuiBean.getGriddedRegionParameterListEditor().getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(33.25));
    regionGuiBean.getGriddedRegionParameterListEditor().getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(34.5));
    regionGuiBean.getGriddedRegionParameterListEditor().getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-119));
    regionGuiBean.getGriddedRegionParameterListEditor().getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-117.5));
  }
}