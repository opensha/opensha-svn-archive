package org.scec.param.editor;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.param.event.*;

/**
 * <p>Title: ParameterListParameterEditor</p>
 * <p>Description: This class is more like a parameterList consisting of any number of
 * parameters. This parameterList considered as a single Parameter.
 * This parameter editor will show up as the button on th GUI interface and
 * when the user punches the button, all the parameters will pop up in a seperate window
 * showing all the parameters contained within this parameter.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 14,2003
 * @version 1.0
 */

public class ParameterListParameterEditor extends ParameterEditor implements
ActionListener,ParameterChangeListener{

  /** Class name for debugging. */
  protected final static String C = "ParameterListParameterEditor";
  /** If true print out debug statements. */
  protected final static boolean D = false;
  private Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  protected ParameterListParameter param ;
  //Editor to hold all the parameters in this parameter
  protected ParameterListEditor editor;

  //Instance for the framee to show the all parameters in this editor
  protected JDialog frame;

  //checks if parameter has been changed
  protected boolean parameterChangeFlag=true;

  //default class constructor
  public ParameterListParameterEditor() {}

  public ParameterListParameterEditor(ParameterAPI model){
    super(model);
  }


  /**
   * Set the values in the parameters in this parameterList parameter
   */
  public void setParameter(ParameterAPI param)  {
    setParameterInEditor(param);
    valueEditor = new JButton(param.getName());
    ((JButton)valueEditor).addActionListener(this);
    add(valueEditor,  new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
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
   * creating the GUI parameters elements for the parameterlistparameter Param
   */
  protected void initParamListAndEditor(){
    ParameterList paramList = (ParameterList)param.getValue();
    ListIterator it = paramList.getParametersIterator();
    while(it.hasNext())
      ((ParameterAPI)it.next()).addParameterChangeListener(this);
    editor = new ParameterListEditor(paramList);
    editor.setTitle("Set "+param.getName());
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
  }


  /**
   * Called when the parameter has changed independently from
   * the editor, such as with the ParameterWarningListener.
   * This function needs to be called to to update
   * the GUI component ( text field, picklist, etc. ) with
   * the new parameter value.
   */
  public void refreshParamEditor(){
    editor.refreshParamEditor();
  }

  /**
   * gets the Parameter for the given paramName
   * @param paramName : Gets the parameter from this paramList
   */
  public ParameterAPI getParameter(String paramName){
    return editor.getParameterList().getParameter(paramName);
  }

  /**
   *
   * @returns the parameterList contained in this editor.
   */
  public ParameterList getParameterList(){
    return editor.getParameterList();
  }

  /**
   * Keeps track when parameter has been changed
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event){
    parameterChangeFlag = true;
  }

  /**
   * This function is called when the user click for the ParameterListParameterEditor Button
   *
   * @param ae
   */
  public void actionPerformed(ActionEvent ae ) {

      frame = new JDialog();
      frame.setModal(true);
      frame.setSize(300,400);
      frame.setTitle(param.getName());
      frame.getContentPane().setLayout(new GridBagLayout());
      frame.getContentPane().add(editor,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
          ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

      //Adding Button to update the forecast
      JButton button = new JButton();
      button.setText("Update "+param.getName());
      button.setForeground(new Color(80,80,133));
      button.setBackground(new Color(200,200,230));
      button.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          button_actionPerformed(e);
        }
      });
      frame.getContentPane().add(button,new GridBagConstraints(0, 2, 1, 1, 0.0,0.0
          ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
      frame.show();
      frame.pack();
  }


  /**
   * This function is called when user punches the button to update the ParameterList Parameter
   * @param e
   */
  void button_actionPerformed(ActionEvent e) {
    ParameterList paramList = editor.getParameterList();
    if(parameterChangeFlag){
      param.setValue(paramList);
      parameterChangeFlag = false;
    }
    frame.dispose();
  }
}
