package org.scec.sha.gui.controls;

import java.util.*;
import org.scec.sha.gui.controls.*;
import org.scec.sha.imr.attenRelImpl.*;
import org.scec.sha.gui.beans.IMR_GuiBean;
import org.scec.util.RunScript;

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
    //magnitudes.add(new Double(7.5));

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
   * @param imrGuiBean
   */
  public void runAllScenarios(PuenteHillsScenarioControlPanel puenteHillsControl,IMR_GuiBean imrGuiBean){
    String COMMAND_PATH = "/bin/";
    int magSize = magnitudes.size();
    int attenRelSize = attenuationRelationships.size();
    String[] command ={"sh","-c",""};
    for(int i=0;i<magSize;++i){
      puenteHillsControl.setMagnitude(((Double)magnitudes.get(i)).doubleValue());
      for(int j=0;j<attenRelSize;++j){
        imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).setValue(attenuationRelationships.get(j));
        application.addButton();
        // Make a directory and move all the files into it
        StringTokenizer st = new StringTokenizer((String)attenuationRelationships.get(j));
        String dirName = st.nextToken()+"_"+((Double)magnitudes.get(i)).doubleValue();

        Vector scriptLines = new Vector();
        command[2] = COMMAND_PATH+"mkdir "+dirName;
        RunScript.runScript(command);
        command[2] = COMMAND_PATH+"mv *.txt *.ps *.jpg *.shx *.shp *.dbf  "+dirName;
        RunScript.runScript(command);
      }
    }

  }

}