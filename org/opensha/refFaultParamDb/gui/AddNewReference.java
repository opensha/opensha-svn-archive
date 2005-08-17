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
  private final static String CITATION_PARAM_NAME="Short Citation";
  private final static String BIBLIO_PARAM_NAME="Full Bibliographic Ref";
  private StringParameter citationParam;
  private StringParameter biblioParam;
  private final static String NEW_SITE_TYPE_LABEL="Add Reference";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");

  public AddNewReference() {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    citationParam = new StringParameter(CITATION_PARAM_NAME);
    biblioParam = new StringParameter(BIBLIO_PARAM_NAME);
    StringParameterEditor citationParameterEditor = null;
    StringParameterEditor biblioParameterEditor = null;
    try {
      citationParameterEditor = new StringParameterEditor(citationParam);
      biblioParameterEditor = new StringParameterEditor(biblioParam);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    int yPos = 0;
    // short citation parameter
    contentPane.add(citationParameterEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // full bibliographic information
    contentPane.add(biblioParameterEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // ok/cancel button
    contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(cancelButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    this.show();
  }


  public static void main(String[] args) {
    AddNewReference addNewReference = new AddNewReference();
  }
}
