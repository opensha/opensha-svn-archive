package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.editor.StringParameterEditor;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import java.awt.*;
import ch.randelshofer.quaqua.QuaquaManager;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.ReferenceDAO_API;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.DBConnectException;

/**
 * <p>Title: AddNewReference.java </p>
 * <p>Description: Add a new Reference </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewReference extends JFrame implements ActionListener {
  private final static String CITATION_PARAM_NAME="Short Citation";
  private final static String CITATION_PARAM_DEFAULT="e.g. Knight & Dey (1988)";
  private final static String BIBLIO_PARAM_NAME="Full Bibliographic Ref";
  private final static String BIBLIO_PARAM_DEFAULT="Enter full citation here";
  private final static String SHORT_CITATION_MSG = "Short Citation is missing";
  private final static String FULL_BIBLIO_MSG = "Full Bibliographic Reference is missing";
  private StringParameter citationParam;
  private StringParameter biblioParam;
  private StringParameterEditor citationParameterEditor;
  private StringParameterEditor biblioParameterEditor;
  private final static String NEW_SITE_TYPE_LABEL="Add Reference";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private ReferenceDAO_API referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);

  public AddNewReference() {
    initParamsAndEditors();
    addEditorsToGUI();
    addActionListeners();
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    setSize(200,200);
    this.setLocationRelativeTo(null);
    this.show();
  }

  /**
   * Add action listeners to the button
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    this.cancelButton.addActionListener(this);
  }

  /**
   * This function is called when ok or cancel button is clicked
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if(source == okButton) {
      // add the reference to the database
      addReferenceToDatabase();
    }
  }

  /**
   * Add the reference to the database
   */
  private void addReferenceToDatabase() {
    String citation = (String)this.citationParam.getValue();
    String fullBiblio = (String)this.biblioParam.getValue();
    // check that usr has provided both short citation as well as full Biblio reference
    if(citation==null || citation.trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, this.SHORT_CITATION_MSG);
     return;
   }
   if(fullBiblio==null || fullBiblio.trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, this.FULL_BIBLIO_MSG);
     return;
   }

   try { // catch the insert exception
     Reference reference = new Reference(citation, fullBiblio);
     referenceDAO.addReference(reference);
   }catch(InsertException insertException) { // if there is problem inserting the reference
      JOptionPane.showMessageDialog(this, insertException.getMessage());
   }catch(DBConnectException connectException) {
      JOptionPane.showMessageDialog(this, connectException.getMessage());
   }
  }

  /**
   * Add editors to the GUI
   */
  private void addEditorsToGUI() {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(GUI_Utils.gridBagLayout);
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
  }

  /**
   * initialize parameters and editors
   */
  private void initParamsAndEditors() {
    citationParam = new StringParameter(CITATION_PARAM_NAME, CITATION_PARAM_DEFAULT);
    biblioParam = new StringParameter(BIBLIO_PARAM_NAME, BIBLIO_PARAM_DEFAULT);
    citationParameterEditor = null;
    biblioParameterEditor = null;
    try {
      citationParameterEditor = new StringParameterEditor(citationParam);
      biblioParameterEditor = new StringParameterEditor(biblioParam);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
