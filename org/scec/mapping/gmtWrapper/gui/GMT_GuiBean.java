package org.scec.mapping.gmtWrapper.gui;


import java.util.Vector;
import java.util.ListIterator;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.mapping.gmtWrapper.*;



/**
 * <p>Title: GMT_GUIBean</p>
 * <p>Description: </p>
 * @author :Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

class GMT_GuiBean {

  private final static String C="GMT_GuiBean";
  private final static boolean D=false;

  private final static String GMT_PARAMETER_TITLE = "GMT Map Parameters";


  // search path needed for making editors
  private String[] searchPaths;

  //GMT Params List and Editor
  ParameterList gmtParamList = new ParameterList();
  ParameterListEditor mapParamListEditor=null;

  GMT_MapGenerator gmtMap = new GMT_MapGenerator();

  public GMT_GuiBean() {
    // search path needed for making editors
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    ListIterator it=gmtMap.getAdjustableParamsList();
    while(it.hasNext())
      gmtParamList.addParameter((ParameterAPI)it.next());
    mapParamListEditor = new ParameterListEditor(gmtParamList,searchPaths);
    mapParamListEditor.setTitle(GMT_PARAMETER_TITLE);
  }

  /**
   *
   * @return the GMT paramlist editor to the MAP generator Applet
   */
  public ParameterListEditor getGMT_ParamEditor(){
    return mapParamListEditor;
  }

  /**
   * calls the functions to generate the Map using the GMT script from the class
   * GMT_MapGenerator and it returns the name of the output jpg file
   * @return
   */
  public String generateMap(){
    //gmtMap.makeMap();
    return gmtParamList.getParameter(GMT_MapGenerator.OUTPUT_FILE_PREFIX_PARAM_NAME).getValue().toString()+
        (GMT_MapGenerator.i-1);
  }
}