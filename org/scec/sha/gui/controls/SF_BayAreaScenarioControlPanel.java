package org.scec.sha.gui.controls;

import java.util.*;
import java.io.*;
import java.awt.event.*;

import org.scec.mapping.gmtWrapper.GMT_MapGenerator;
import org.scec.sha.gui.beans.*;
import org.scec.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.param.*;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.*;
import org.scec.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.scec.sha.gui.infoTools.CalcProgressBar;

/**
 * <p>Title: SF_BayAreaScenarioControlPanel</p>
 * <p>Description: Sets the param value  of scenario shakemaps for SF Bay Area</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class SF_BayAreaScenarioControlPanel {

  //for debugging
  protected final static boolean D = false;


  private EqkRupSelectorGuiBean erfGuiBean;
  private AttenuationRelationshipGuiBean imrGuiBean;
  private SitesInGriddedRegionGuiBean regionGuiBean;
  private MapGuiBean mapGuiBean;
  private GenerateHazusControlPanelForSingleMultipleIMRs hazusControlPanel;


  private final static String fileToRead = "shakemaps_request.txt";


  //class default constructor
  /**
   * Accepts 3 params for the EqkRupSelectorGuiBean, IMR_GuiBean, SitesInGriddedRegionGuiBean
   * from the applet.
   * @param erfGuiBean
   * @param imrGuiBean
   * @param regionGuiBean
   */
  public SF_BayAreaScenarioControlPanel(EqkRupSelectorGuiBean erfGuiBean, AttenuationRelationshipGuiBean imrGuiBean,
      SitesInGriddedRegionGuiBean regionGuiBean,MapGuiBean mapGuiBean,
      GenerateHazusControlPanelForSingleMultipleIMRs hazusControl) {
    //getting the instance for variuos GuiBeans from the applet required to set the
    //default values for the Params for the SF Bay Area Scenarios.
    this.erfGuiBean = erfGuiBean;
    this.imrGuiBean = imrGuiBean;
    this.regionGuiBean = regionGuiBean;
    this.mapGuiBean =  mapGuiBean;
    hazusControlPanel = hazusControl;
  }

  /**
   * Sets the default Parameters in the Application for the SF Bay Area Scenarios.
   * Also generate the Hazus data and scenario shakemaps for the SF Bay area.
   */
  public void setParamsForSF_BayAreaScenario(){


    try{

      //checking if the single AttenRel is selected
      boolean isSingleAttenRelSelected =imrGuiBean.isSingleAttenRelTypeSelected();
      //if single attenRel gui is not selected then toggle to the single attenRel gui Panel
      if(!isSingleAttenRelSelected)
        imrGuiBean.toggleBetweenSingleAndMultipleAttenRelGuiSelection();

      //Updating the IMR Gui Bean with the ShakeMap attenuation relationship
      imrGuiBean.setIMR_Selected(ShakeMap_2003_AttenRel.NAME);
      imrGuiBean.getSingleAttenRelParamListEditor().refreshParamEditor();


      //Updating the SitesInGriddedRegionGuiBean with the Puente Hills resion setting
      regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LATITUDE).setValue(new Double(36.5500));
      regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LATITUDE).setValue(new Double(39.6167));
      regionGuiBean.getParameterList().getParameter(regionGuiBean.MIN_LONGITUDE).setValue(new Double(-124.7333));
      regionGuiBean.getParameterList().getParameter(regionGuiBean.MAX_LONGITUDE).setValue(new Double(-120.1333));
      regionGuiBean.getParameterList().getParameter(regionGuiBean.GRID_SPACING).setValue(new Double(.016667));
      regionGuiBean.getParameterList().getParameter(regionGuiBean.SITE_PARAM_NAME).setValue(regionGuiBean.SET_SITE_USING_WILLS_SITE_TYPE);

      //making the ERF Gui Bean Adjustable Param not visible to the user, becuase
      //this control panel will set the values by itself.
      //This is done in the EqkRupSelectorGuiBean
      erfGuiBean.showAllParamsForForecast(false);

      //changing the ERF to Frankel02_AdjustableEqkRupForecast
      erfGuiBean.getParameterListEditor().getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setValue(Frankel02_AdjustableEqkRupForecast.NAME);
      erfGuiBean.getParameterListEditor().refreshParamEditor();

      //Getting the instance for the editor that holds all the adjustable params for the selcetd ERF
      ERF_GuiBean erfParamGuiBean =erfGuiBean.getERF_ParamEditor();

      //reading the file sent by Paul to generate the shakemaps for defined sources and ruptures
      FileReader fr = new FileReader(fileToRead);
      BufferedReader br = new BufferedReader(fr);

      //reading the fileLine from the , where each line is in following order:
      //source index,rupture index, rupture offset,magnitude,source name
      String fileLines = br.readLine();
      while(fileLines !=null){
        StringTokenizer st = new StringTokenizer(fileLines);
        //getting the source number
        int sourceIndex = Integer.parseInt(st.nextToken().trim());

        //getting the rupture number
        int ruptureIndex =0;
        String rupIndex = st.nextToken().trim();
        ruptureIndex = Integer.parseInt(rupIndex);

        //getting the rupture offset.
        String ruptureOffset = st.nextToken().trim();
        double rupOffset = 100;
        rupOffset = Double.parseDouble(ruptureOffset);

        //discarding the magnitude that we are reading.
        st.nextToken();

        //getting the name of the directory
        String directoryName = st.nextToken().trim();

        fileLines = br.readLine();

        // Set rake value to 90 degrees
        erfParamGuiBean.getParameterList().getParameter(Frankel02_AdjustableEqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(new Double(rupOffset));
        //updating the forecast with the changed parameter settings.
        erfParamGuiBean.getSelectedERF().updateForecast();

        //updating the EQK_RupSelectorGuiBean with the Source and Rupture Index respectively.
        erfGuiBean.setParamsInForecast(sourceIndex,ruptureIndex);
        mapGuiBean.setDirectoryName(directoryName);
        hazusControlPanel.runToGenerateShapeFilesAndMaps();
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

