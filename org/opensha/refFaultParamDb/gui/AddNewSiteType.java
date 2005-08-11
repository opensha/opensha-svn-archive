package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.editor.StringParameterEditor;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;

/**
 * <p>Title: AddNewSiteType.java </p>
 * <p>Description: Add a new site type </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewSiteType extends JFrame {
  private final static String SITE_TYPE_NAME_PARAM_NAME="Site Type Name";
  private StringParameter siteTypeParamName;
  private final static String NEW_SITE_TYPE_LABEL="Add New Site Type";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");

  public AddNewSiteType() {
    this.getContentPane().setLayout(new GridBagLayout());
    siteTypeParamName = new StringParameter(SITE_TYPE_NAME_PARAM_NAME);
    StringParameterEditor stringParameterEditor = null;
    try {
      stringParameterEditor = new StringParameterEditor(siteTypeParamName);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    // add string parameter editor so that user can type in site type name
    this.getContentPane().add(stringParameterEditor,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // ok/cancel button
    this.getContentPane().add(okButton,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(cancelButton,  new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    this.show();
  }

  //static initializer for setting look & feel
 static {
   String osName = System.getProperty("os.name");
   try {
     if(osName.startsWith("Mac OS"))
       UIManager.setLookAndFeel(QuaquaManager.getLookAndFeelClassName());
     else
       UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
   }
   catch(Exception e) {
   }
 }

  public static void main(String[] args) {
    AddNewSiteType addNewSiteType = new AddNewSiteType();
  }
}