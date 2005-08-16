package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.editor.StringParameterEditor;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: AddNewReference.java </p>
 * <p>Description: Add a new Reference </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewReference extends JFrame {
  private final static String REFERENCE_PARAM_NAME="Reference";
  private StringParameter referenceParamName;
  private final static String NEW_SITE_TYPE_LABEL="Add Reference";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");

  public AddNewReference() {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    referenceParamName = new StringParameter(REFERENCE_PARAM_NAME);
    StringParameterEditor referenceParameterEditor = null;
    try {
      referenceParameterEditor = new StringParameterEditor(referenceParamName);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    // add string parameter editor so that user can type in reference
    contentPane.add(referenceParameterEditor,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // ok/cancel button
    contentPane.add(okButton,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(cancelButton,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    this.show();
  }


  public static void main(String[] args) {
    AddNewReference addNewReference = new AddNewReference();
  }
}
