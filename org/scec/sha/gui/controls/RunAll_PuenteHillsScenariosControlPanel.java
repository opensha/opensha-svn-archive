package org.scec.sha.gui.controls;

import java.util.*;
import org.scec.sha.gui.controls.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.sha.gui.beans.*;
import org.scec.util.RunScript;
import org.scec.sha.param.editor.MagFreqDistParameterEditor;
import org.scec.sha.param.*;
import org.scec.sha.magdist.SingleMagFreqDist;


/**
 * <p>Title: RunAll_PuenteHillsScenariosControlPanel</p>
 * <p>Description: Automate the process of running all the scenarios for the
 * Puente Hills.</p>
 * @author : Edward (Ned) Field and Nitin Gupta
 * @version 1.0
 */

public class RunAll_PuenteHillsScenariosControlPanel {


  //Vector to store the magnitudes
  Vector magnitudes = new Vector();
  Vector attenuationRelationships = new Vector();

  //instance of the application using this control panel
  RunAll_PuenteHillsScenariosControlPanelAPI application;

  /**
   * Class Constructor
   * @param puenteHillsControl
   */
  public RunAll_PuenteHillsScenariosControlPanel(RunAll_PuenteHillsScenariosControlPanelAPI api){
    application = api;
    //adding the magnitudes to the Vector List
    magnitudes.add(new Double(7.1));
    //magnitudes.add(new Double(7.2));
    //magnitudes.add(new Double(7.4));
    magnitudes.add(new Double(7.5));

    //adding the supported AttenuationRelationshipsName to the Vector List
    attenuationRelationships.add(AS_1997_AttenRel.NAME);
    //attenuationRelationships.add(BJF_1997_AttenRel.NAME);
    //attenuationRelationships.add(CB_2003_AttenRel.NAME);
    //attenuationRelationships.add(Field_2000_AttenRel.NAME);
    //attenuationRelationships.add(SCEMY_1997_AttenRel.NAME);
    attenuationRelationships.add(ShakeMap_2003_AttenRel.NAME);
  }


  /**
   * Runs all the cases for the Puente Hill Scenarios
   * @param puenteHillsControl
   * @param hazusControl: Handle to the class to generate the shape files for input to Hazus
   * @param imrGuiBean
   */
  public void runAllScenarios(PuenteHillsScenarioControlPanel puenteHillsControl,
                              GenerateHazusFilesControlPanel hazusControl,IMR_GuiBean imrGuiBean,
                              EqkRupSelectorGuiBean erfGuiBean){
    String COMMAND_PATH = "/bin/";
    int magSize = magnitudes.size();
    int attenRelSize = attenuationRelationships.size();
    String[] command ={"sh","-c",""};
    hazusControl.getRegionAndMapType();
    for(int i=0;i<magSize;++i){

      //set the magnitude
      ERF_GuiBean erfParamGuiBean =erfGuiBean.getERF_ParamEditor();
      MagFreqDistParameterEditor magEditor = erfParamGuiBean.getMagDistEditor();
      magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
      magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
      magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(((Double)magnitudes.get(i)).doubleValue()));
      erfParamGuiBean.refreshParamEditor();
      magEditor.setMagDistFromParams();
      erfGuiBean.setParamsInForecast(0,0);

      for(int j=0;j<attenRelSize;++j){
        imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).setValue(attenuationRelationships.get(j));
        //calls the Hazus Control method to generate the XYZ datset for generating shapefiles
        //for hazus.
        hazusControl.generateShapeFilesForHazus();
        application.addButton();
        // Make a directory and move all the files into it
        StringTokenizer st = new StringTokenizer((String)attenuationRelationships.get(j));
        String dirName = "PH_"+st.nextToken()+"_"+((Double)magnitudes.get(i)).doubleValue();

        Vector scriptLines = new Vector();
        command[2] = COMMAND_PATH+"mkdir "+dirName;
        RunScript.runScript(command);
//        command[2] = COMMAND_PATH+"mv *.txt *.ps *.jpg *.shx *.shp *.dbf  "+dirName;
        command[2] = COMMAND_PATH+"mv *map*  "+dirName;
        RunScript.runScript(command);
      }
    }

  }

}