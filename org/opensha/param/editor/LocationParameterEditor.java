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

  //default class constructor
  public LocationParameterEditor() {}

  public LocationParameterEditor(ParameterAPI model) {
    super(model);
  }

  /**
   * Set the values in the parameters in this parameterList parameter
   */
  public void setParameter(ParameterAPI param) {
    setParameterInEditor(param);
    valueEditor = new JButton(param.getName());
    ( (JButton) valueEditor).addActionListener(this);
    add(valueEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.HORIZONTAL,
                                            new Insets(0, 0, 0, 0), 0, 0));
    String S = C + ": Constructor(): ";

    // remove the previous editor
    //removeAll();
    this.param = (LocationParameter) param;
    // make the params editor
    initParamListAndEditor();

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
   * Main GUI Initialization point. This block of code is updated by JBuilder
   * when using it's GUI Editor.
   */
  protected void jbInit() throws Exception {

    // Main component
    this.setLayout(new GridBagLayout());
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
