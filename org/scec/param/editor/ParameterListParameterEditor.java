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

  private ParameterListParameter param ;
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

    ListIterator it = param.getParametersIterator();
    ParameterList paramList = new ParameterList();
    while(it.hasNext())
      paramList.addParameter((ParameterAPI)it.next());

    editor = new ParameterListEditor(paramList);
    editor.setTitle(EDITOR_TITLE);
    param.setValue(paramList);
  }

  /**
   * sets the title for this editor
   * @param title
   */
  public void setEditorTitle(String title){
    editor.setTitle(title);
  }




}