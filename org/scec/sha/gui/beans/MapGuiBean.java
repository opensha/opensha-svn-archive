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

public class MapGuiBean extends JPanel {


  private final static String GMT_TITLE = new String("Set GMT Parameters");

  //instance of the GMT Control Panel to get the GMT parameters value.
  private GMT_MapGenerator gmtMap= new GMT_MapGenerator();



  // PATH where the gmt commands and some others exist.
  private static String GMT_PATH = "/sw/bin/";

  // this is the path where general data (e.g., topography) are found:
  private static String SCEC_GMT_DATA_PATH = "/usr/scec/data/gmt/";


  // this is the path to find the "cat" command
  private static String COMMAND_PATH = "/bin/";

  //counter that keeps tracks of the outputfile jpg file generated
  //it is declared static so that all objects of this class share the same variable.
  private static int outputFilePrefixCounter=0;


  //Label to show the imageFile
  private JLabel gmtMapLabel = new JLabel();
  private Border border;
  private GridBagLayout gridBagLayout = new GridBagLayout();

  //GMT Param List
  ParameterList paramList = new ParameterList();
  ParameterListEditor listEditor;



  /**
   * Clas constructor accepts the GMT parameters list
   * @param gmtMap
   */
  public MapGuiBean() {

    // search path needed for making editors
    String [] searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    //get the adjustableParam List from the GMT_MapGenerator
    ListIterator it=gmtMap.getAdjustableParamsList();
    while(it.hasNext())
      paramList.addParameter((ParameterAPI)it.next());
    listEditor= new ParameterListEditor(paramList,searchPaths);
  }

  /**
   *
   * @param regionParamsFlag: boolean flag to check if the region params are to be shown in the
   */
  public ParameterListEditor showGMTParams(boolean regionParamsFlag){
    if(regionParamsFlag){
      listEditor.getParameterEditor(gmtMap.MAX_LAT_PARAM_NAME).setVisible(false);
      listEditor.getParameterEditor(gmtMap.MIN_LAT_PARAM_NAME).setVisible(false);
      listEditor.getParameterEditor(gmtMap.MAX_LON_PARAM_NAME).setVisible(false);
      listEditor.getParameterEditor(gmtMap.MIN_LON_PARAM_NAME).setVisible(false);
      listEditor.getParameterEditor(gmtMap.GRID_SPACING_PARAM_NAME).setVisible(false);
    }
    listEditor.setTitle(GMT_TITLE);
    return listEditor;
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
    paramList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).setValue(new Double(minLat));
    paramList.getParameter(gmtMap.MAX_LAT_PARAM_NAME).setValue(new Double(maxLat));
    paramList.getParameter(gmtMap.MIN_LON_PARAM_NAME).setValue(new Double(minLon));
    paramList.getParameter(gmtMap.MAX_LON_PARAM_NAME).setValue(new Double(maxLat));
    paramList.getParameter(gmtMap.GRID_SPACING_PARAM_NAME).setValue(new Double(gridSpacing));
  }



  /**
   * this function generates and displays a GMT map for an XYZ dataset using
   * the settings in the GMT_SettingsControlPanel.
   *
   * @param fileName: name of the XYZ file
   */
  public void makeMap(String fileName){

    double minLat = ((Double) paramList.getParameter(gmtMap.MIN_LAT_PARAM_NAME).getValue()).doubleValue();
    double maxLat = ((Double) paramList.getParameter(gmtMap.MAX_LAT_PARAM_NAME).getValue()).doubleValue();
    double minLon = ((Double) paramList.getParameter(gmtMap.MIN_LON_PARAM_NAME).getValue()).doubleValue();
    double maxLon = ((Double) paramList.getParameter(gmtMap.MAX_LON_PARAM_NAME).getValue()).doubleValue();
    double gridSpacing= ((Double) paramList.getParameter(gmtMap.GRID_SPACING_PARAM_NAME).getValue()).doubleValue();
    String region = "-R" + minLon + "/" + maxLon + "/" + minLat + "/" + maxLat;
    try {

      //command to be executed during the runtime.
       String[] command ={"sh","-c",GMT_PATH+"xyz2grd "+ fileName+" -Gdata.grd -I"+gridSpacing+" "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
       RunScript.runScript(command);
       gmtMap.makeMap("data.grd");

    } catch (Exception e) {
      // report to the user whether the operation was successful or not
      e.printStackTrace();
    }

    //adding the image to the Panel and returning that to the applet
    String imgName= (String)paramList.getParameter(gmtMap.OUTPUT_FILE_PREFIX_PARAM_NAME).getValue()+outputFilePrefixCounter;
    gmtMapLabel.setBorder(border);
    gmtMapLabel.setMaximumSize(new Dimension(0, 800));
    gmtMapLabel.setMinimumSize(new Dimension(0, 600));
    gmtMapLabel.setPreferredSize(new Dimension(0, 600));
    this.setLayout(gridBagLayout);
    this.setMinimumSize(new Dimension(0, 800));
    this.setPreferredSize(new Dimension(0, 800));
    this.add(gmtMapLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 557, 200));
    gmtMapLabel.setIcon(new ImageIcon(imgName));

    // increment jpg file index
      ++outputFilePrefixCounter;
  }
}