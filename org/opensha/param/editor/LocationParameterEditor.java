package org.opensha.param.editor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.param.event.*;
import org.opensha.data.Location;

/**
 * <p>Title: LocationParameterEditor</p>
 * <p>Description: This class is more like a parameterList consisting of only
 * Double Parameters and considering this parameterList as a single Parameter.
 * This parameter editor will show up as the button on th GUI interface and
 * when the user punches the button, all the parameters will pop up in a seperate window
 * showing all the double parameters contained within this parameter.</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @created : Jan 24,2005
 * @version 1.0
 */

public class LocationParameterEditor
    extends ParameterEditor implements
    ActionListener, ParameterChangeListener {

  /** Class name for debugging. */
  protected final static String C = "LocationParameterEditor";

  private LocationParameter param;

  //hold the parameters for the Location parameter, show them in a seperate panel.
  private ParameterListEditor editor;

  //checks if parameter has been changed
  private boolean parameterChangeFlag = true;

  //Instance for the framee to show the all parameters in this editor
  protected JDialog frame;

  /* Whether to show this editor as a button.
   1. If this is set as true, a pop-up window appears on button click
   2. If this is set as false, button is not shown for pop-up window.
   */
  private boolean showEditorAsPanel = false;

  // size of the location parameter editor
  protected final static Dimension WIGET_PANEL_DIM = new Dimension( 140, 220 );

  //default class constructor
  public LocationParameterEditor() {}

  /**
   * Show the location parameter editor.
   * Editor is show nas a button which pops up window to fill lat/lon/depth.
   *
   * @param model
   */
  public LocationParameterEditor(ParameterAPI model) {
    this(model, false);
  }

  public LocationParameterEditor(ParameterAPI model, boolean showEditorAsPanel) {
    this.showEditorAsPanel = showEditorAsPanel;
    try {
      this.jbInit();
      setParameter(model);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
     * Main GUI Initialization point. This block of code is updated by JBuilder
     * when using it's GUI Editor.
     */
    protected void jbInit() throws Exception {
      this.setLayout(new GridBagLayout());
    }



  /**
   * Set the values in the parameters in this parameterList parameter
   */
  public void setParameter(ParameterAPI param) {
    setParameterInEditor(param);
    this.param = (LocationParameter) param;
    // make the params editor
    initParamListAndEditor();
    int fillConstraint;
    if(!showEditorAsPanel) { // show a button which pops up window to fill lat/lon/depth
      valueEditor = new JButton(param.getName());

      ( (JButton) valueEditor).addActionListener(this);
      fillConstraint = GridBagConstraints.HORIZONTAL;
    } else { // DO NOT show button for pop-up window
      valueEditor = editor;
      fillConstraint = GridBagConstraints.BOTH;
      valueEditor.setMinimumSize( WIGET_PANEL_DIM);
      valueEditor.setPreferredSize( WIGET_PANEL_DIM );
    }

    // add the valueEditor to the panel
    add(valueEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                                            , GridBagConstraints.CENTER,
                                            fillConstraint,
                                            new Insets(0, 0, 0, 0), 0, 0));

    String S = C + ": Constructor(): ";

    // remove the previous editor
    //removeAll();

  }

  /**
   * Keeps track when parameter has been changed
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    parameterChangeFlag = true;
  }

  /**
   * creating the GUI parameters elements for the parameterlistparameter Param
   */
  protected void initParamListAndEditor() {
    ParameterList paramList = (ParameterList) ( (ParameterListParameter) param.
                                               getLocationParameter()).
        getValue();
    ListIterator it = paramList.getParametersIterator();
    while (it.hasNext())
      ( (ParameterAPI) it.next()).addParameterChangeListener(this);
    editor = new ParameterListEditor(paramList);
    editor.setTitle("Set " + param.getName());
  }

  /**
   * This function is called when user punches the button to update the
   * ParameterList Parameter.
   * @param e
   */
  protected void button_actionPerformed(ActionEvent e) {
    if(parameterChangeFlag){
      Location loc = new Location(param.getLatitude(), param.getLongitude(),
                                  param.getDepth());
      param.setValue(loc);
    }
    frame.dispose();
  }

  /**
   * sets the title for this editor
   * @param title
   */
  public void setEditorTitle(String title) {
    editor.setTitle(title);
  }

  /**
   * It enables/disables the editor according to whether user is allowed to
   * fill in the values.
   */
  public void setEnabled(boolean isEnabled) {
    editor.setEnabled(isEnabled);
  }


  /**
   * Called when the parameter has changed independently from
   * the editor, such as with the ParameterWarningListener.
   * This function needs to be called to to update
   * the GUI component ( text field, picklist, etc. ) with
   * the new parameter value.
   */
  public void refreshParamEditor() {
    editor.refreshParamEditor();
  }

  /**
   * gets the Parameter for the given paramName
   * @param paramName : Gets the parameter from this paramList
   */
  public ParameterAPI getParameter(String paramName) {
    return editor.getParameterList().getParameter(paramName);
  }

  /**
   *
   * @returns the parameterList contained in this editor.
   */
  public ParameterList getParameterList() {
    return editor.getParameterList();
  }

  /**
   * This function is called when the user click for the ParameterListParameterEditor Button
   *
   * @param ae
   */
  public void actionPerformed(ActionEvent ae) {

    frame = new JDialog();
    frame.setModal(true);
    frame.setSize(300, 400);
    frame.setTitle(param.getName());
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(editor,
                               new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    JButton button = new JButton();
    button.setText("Update " + param.getName());
    button.setForeground(new Color(80, 80, 133));
    button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        button_actionPerformed(e);
      }
    });
    frame.getContentPane().add(button,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 0, 0));
    frame.show();
    frame.pack();
  }

}
