package org.scec.param.editor;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.param.event.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class ParameterListParameterEditor extends ParameterEditor {

  /** Class name for debugging. */
  protected final static String C = "TreeBranchWeightsParameterEditor";
  /** If true print out debug statements. */
  protected final static boolean D = false;
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  //static declaration for the title of the Parameters
  private final static String EDITOR_TITLE = "Prob. Model Wts.";

  /**
   *  Search path for finding editors in non-default packages.
   */
  private String[] searchPaths;
  final static String SPECIAL_EDITORS_PACKAGE = "org.scec.sha.propagation";

  private ParameterListParameter param ;
  //Editor to hold all the parameters in this parameter
  private ParameterListEditor editor;


  public ParameterListParameterEditor(ParameterAPI param) {
    setParameter(param);
  }

  /**
   * Set the values in the Parameters for the EvenlyGridded Surface
   */
  public void setParameter(ParameterAPI param)  {

    String S = C + ": Constructor(): ";
    if ( D ) System.out.println( S + "Starting:" );

    // remove the previous editor
    //removeAll();
    this.param = (ParameterListParameter) param;

    // make the params editor
    initParamListAndEditor();
    // All done
    if ( D ) System.out.println( S + "Ending:" );
  }


  /**
   * creating the GUI parameters elements for the EvenlyGriddedSurface Param
   */
  private void initParamListAndEditor(){

    editor = new ParameterListEditor((ParameterList)param.getValue());
    editor.setTitle(EDITOR_TITLE);

  }

  /**
   * sets the title for this editor
   * @param title
   */
  public void setEditorTitle(String title){
    editor.setTitle(title);
  }

  /**
   * Main GUI Initialization point. This block of code is updated by JBuilder
   * when using it's GUI Editor.
   */
  protected void jbInit() throws Exception {

    // Main component
    this.setLayout( new GridBagLayout());


    // Build package names search path
    searchPaths = new String[3];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    searchPaths[1] = SPECIAL_EDITORS_PACKAGE;

  }


  /**
   * Called when the parameter has changed independently from
   * the editor, such as with the ParameterWarningListener.
   * This function needs to be called to to update
   * the GUI component ( text field, picklist, etc. ) with
   * the new parameter value.
   */
  public void syncToModel(){
    editor.synchToModel();
  }

  /**
   * gets the Parameter for the given paramName
   * @param paramName : Gets the parameter from this paramList
   */
  public ParameterAPI getParameter(String paramName){
    return editor.getParameterList().getParameter(paramName);
  }

}