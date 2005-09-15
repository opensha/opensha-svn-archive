package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.editor.StringParameterEditor;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <p>Title: AddNewSiteType.java </p>
 * <p>Description: Add a new site type </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewSiteType extends JFrame implements ActionListener {
  private final static String SITE_TYPE_NAME_PARAM_NAME="Site Type Name";
  private final static String SITE_TYPE_COMMENTS_PARAM_NAME="Site Type Comments";
  private final static String SITE_TYPE_NAME_PARAM_DEFAULT="Enter Name Here";
  private StringParameter siteTypeParam;
  private StringParameter siteTypeCommentsParam;

  private StringParameterEditor siteTypeNameParameterEditor = null;
  private CommentsParameterEditor siteTypeCommentsParamEditor = null;

  private final static String NEW_SITE_TYPE_LABEL="Add New Site Type";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");

  public AddNewSiteType() {
    //intialize the parameters and editors
    initParamsAndEditors();
    // add the editors to GUI
    addEditorsToGUI();
    // add action listeners  to the buttons
    addActionListeners();
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    this.setLocationRelativeTo(null);
    this.show();
  }

  /**
   * Add the action listeners to the buttons
   */
  private void addActionListeners() {
    this.okButton.addActionListener(this);
    this.cancelButton.addActionListener(this);
  }

  /**
   * This fucntion is called when user clicks on "Ok" or "Cancel" button
   * @param event
   */
  public void actionPerformed(ActionEvent event) {

  }

  /**
   * Add editors to GUI
   */
  private void addEditorsToGUI() {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(GUI_Utils.gridBagLayout);
    // add string parameter editor so that user can type in site type name
    int yPos =0;
    contentPane.add(siteTypeNameParameterEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(siteTypeCommentsParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // ok/cancel button
    contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(cancelButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
  }

  /**
   * Initialize parameters and editors
   */
  private void initParamsAndEditors() {
    siteTypeParam = new StringParameter(SITE_TYPE_NAME_PARAM_NAME, SITE_TYPE_NAME_PARAM_DEFAULT);
    siteTypeCommentsParam = new StringParameter(SITE_TYPE_COMMENTS_PARAM_NAME);
    try {
      siteTypeNameParameterEditor = new StringParameterEditor(siteTypeParam);
      siteTypeCommentsParamEditor = new CommentsParameterEditor(siteTypeCommentsParam);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}