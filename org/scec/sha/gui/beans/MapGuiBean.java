package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.mapping.gmtWrapper.*;
import org.scec.param.*;
import org.scec.param.editor.*;

/**
 * <p>Title: GMT_MapGenerator</p>
 * <p>Description: This class generates and displays a GMT map for an XYZ dataset using
 * the settings in the GMT_SettingsControlPanel. It displays the image file in a JPanel.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author: Ned Field, Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class MapGuiBean extends ParameterListEditor {


  private final static String GMT_TITLE = new String("Set GMT Parameters");

  //instance of the GMT Control Panel to get the GMT parameters value.
  private GMT_MapGenerator gmtMap= new GMT_MapGenerator();

  //Label to show the imageFile
  private JLabel gmtMapLabel = new JLabel();

  /**
   * Clas constructor accepts the GMT parameters list
   * @param gmtMap
   */
  public MapGuiBean() {
    // search path needed for making editors
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    //get the adjustableParam List from the GMT_MapGenerator
    ListIterator it=gmtMap.getAdjustableParamsList();
    parameterList = new ParameterList();
    while(it.hasNext())
      parameterList.addParameter((ParameterAPI)it.next());
    addParameters();
    this.setTitle(GMT_TITLE);
  }

  /**
   *
   * @param regionParamsFlag: boolean flag to check if the region params are to be shown in the
   */
  public void showGMTParams(boolean regionParamsFlag) {
      getParameterEditor(gmtMap.MAX_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MIN_LAT_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MAX_LON_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.MIN_LON_PARAM_NAME).setVisible(regionParamsFlag);
      getParameterEditor(gmtMap.GRID_SPACING_PARAM_NAME).setVisible(regionParamsFlag);
  }

  /**
   * private function that initialises the region params for the GMT plot region
   * @param minLat
   * @param maxLat
   * @param minLon
   * @param maxLon
   * @param gridSpacing
   */
  public void setGMTRegionParams(double minLat,double maxLat,double minLon,double maxLon,
                               double gridSpacing){
    parameterList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).setValue(new Double(minLat));
    parameterList.getParameter(gmtMap.MAX_LAT_PARAM_NAME).setValue(new Double(maxLat));
    parameterList.getParameter(gmtMap.MIN_LON_PARAM_NAME).setValue(new Double(minLon));
    parameterList.getParameter(gmtMap.MAX_LON_PARAM_NAME).setValue(new Double(maxLon));
    parameterList.getParameter(gmtMap.GRID_SPACING_PARAM_NAME).setValue(new Double(gridSpacing));
  }



  /**
   * this function generates and displays a GMT map for an XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeMap(String fileName){

    double minLat = ((Double) parameterList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).getValue()).doubleValue();
    double maxLat = ((Double) parameterList.getParameter(gmtMap.MAX_LAT_PARAM_NAME).getValue()).doubleValue();
    double minLon = ((Double) parameterList.getParameter(gmtMap.MIN_LON_PARAM_NAME).getValue()).doubleValue();
    double maxLon = ((Double) parameterList.getParameter(gmtMap.MAX_LON_PARAM_NAME).getValue()).doubleValue();
    double gridSpacing= ((Double) parameterList.getParameter(gmtMap.GRID_SPACING_PARAM_NAME).getValue()).doubleValue();
    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;
    String imgName = null;
    try {

      //command to be executed during the runtime.
       String[] command ={"sh","-c",gmtMap.GMT_PATH+"xyz2grd "+ fileName+" -Gdata.grd -I"+gridSpacing+" "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
       RunScript.runScript(command);
       imgName = gmtMap.makeMap("data.grd");

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

    //adding the image to the Panel and returning that to the applet
    gmtMapLabel.setBorder(border);
    gmtMapLabel.setMaximumSize(new Dimension(0, 800));
    gmtMapLabel.setMinimumSize(new Dimension(600, 600));
    gmtMapLabel.setPreferredSize(new Dimension(600, 600));
    gmtMapLabel.setIcon(new ImageIcon(imgName));
    JFrame frame = new JFrame(imgName);
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(gmtMapLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 557, 200));
    frame.pack();
    frame.show();

  }
}