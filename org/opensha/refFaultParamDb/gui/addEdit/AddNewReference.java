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
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.dao.exception.InsertException;
import org.opensha.refFaultParamDb.dao.exception.DBConnectException;
import org.opensha.refFaultParamDb.gui.event.DbAdditionFrame;
import org.opensha.refFaultParamDb.gui.view.ViewAllReferences;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import org.opensha.param.IntegerParameter;
import org.opensha.param.editor.IntegerParameterEditor;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.gui.infotools.ConnectToEmailServlet;

/**
 * <p>Title: AddNewReference.java </p>
 * <p>Description: Add a new Reference </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddNewReference extends DbAdditionFrame implements ActionListener {
  private final static String AUTHOR_PARAM_NAME="Short Citation";
  private final static String AUTHOR_PARAM_DEFAULT="e.g. Knight & Dey";
  private final static String BIBLIO_PARAM_NAME="Full Bibliographic Ref";
  private final static String BIBLIO_PARAM_DEFAULT="Enter full citation here";
  private final static String YEAR_PARAM_NAME="Year";
  private final static Integer YEAR_PARAM_DEFAULT=new Integer(1998);
  private final static String MSG_AUTHOR = "Author is missing";
  private final static String MSG_FULL_BIBLIO = "Full Bibliographic Reference is missing";
  private final static String MSG_YEAR = "Year is missing";
  private StringParameter authorParam;
  private StringParameter biblioParam;
  private IntegerParameter yearParam;
  private StringParameterEditor authorParameterEditor;
  private CommentsParameterEditor biblioParameterEditor;
  private IntegerParameterEditor yearParamEditor;
  private final static String NEW_SITE_TYPE_LABEL="Add Reference";
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private JButton viewAllRefsButton = new JButton("View All References");
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);
  private final static String MSG_INSERT_SUCCESS = "Reference added sucessfully to the database";

  public AddNewReference() {
    initParamsAndEditors();
    addEditorsToGUI();
    addActionListeners();
    this.setTitle(NEW_SITE_TYPE_LABEL);
    this.pack();
    setSize(400,400);
    this.setLocationRelativeTo(null);
    this.show();
  }

  /**
   * Add action listeners to the button
   */
  private void addActionListeners() {
    okButton.addActionListener(this);
    this.cancelButton.addActionListener(this);
    viewAllRefsButton.addActionListener(this);
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
    }else if(source==cancelButton) this.dispose();
    else if(source == viewAllRefsButton) {
       new ViewAllReferences();
    }
  }

  /**
   * Add the reference to the database
   */
  private void addReferenceToDatabase() {
    String author = (String)this.authorParam.getValue();
    String fullBiblio = (String)this.biblioParam.getValue();
    int year = ((Integer)this.yearParam.getValue()).intValue();
    // check that usr has provided both short citation as well as full Biblio reference
    if(author==null || author.trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, this.MSG_AUTHOR);
     return;
   }
   if(fullBiblio==null || fullBiblio.trim().equalsIgnoreCase("")) {
     JOptionPane.showMessageDialog(this, this.MSG_FULL_BIBLIO);
     return;
   }

   try { // catch the insert exception
     Reference reference = new Reference(author, ""+year, fullBiblio);
     ConnectToEmailServlet.sendEmail(SessionInfo.getUserName()+" trying to add new Reference to database\n"+reference.toString());
     referenceDAO.addReference(reference);
     this.sendEventToListeners(reference);
     JOptionPane.showMessageDialog(this, MSG_INSERT_SUCCESS);
     ConnectToEmailServlet.sendEmail("New Reference "+fullBiblio +" added sucessfully by "+SessionInfo.getUserName());
     this.dispose();
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
    // author parameter
    contentPane.add(authorParameterEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // year parameter
    contentPane.add(this.yearParamEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    // full bibliographic information
    contentPane.add(biblioParameterEditor,  new GridBagConstraints(0, yPos++, 2, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(this.viewAllRefsButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    // ok/cancel button
    contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    contentPane.add(cancelButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
  }

  /**
   * initialize parameters and editors
   */
  private void initParamsAndEditors() {
    authorParam = new StringParameter(AUTHOR_PARAM_NAME, AUTHOR_PARAM_DEFAULT);
    biblioParam = new StringParameter(BIBLIO_PARAM_NAME, BIBLIO_PARAM_DEFAULT);
    this.yearParam = new IntegerParameter(this.YEAR_PARAM_NAME, YEAR_PARAM_DEFAULT);
    authorParameterEditor = null;
    biblioParameterEditor = null;
    try {
      authorParameterEditor = new StringParameterEditor(authorParam);
      biblioParameterEditor = new CommentsParameterEditor(biblioParam);
      yearParamEditor = new IntegerParameterEditor(yearParam);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
