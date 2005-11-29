package org.opensha.refFaultParamDb.gui.addEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opensha.refFaultParamDb.gui.event.DbAdditionListener;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.StringParameter;
import org.opensha.refFaultParamDb.dao.db.ReferenceDB_DAO;
import java.awt.*;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.exceptions.ConstraintException;
import org.opensha.refFaultParamDb.gui.event.DbAdditionSuccessEvent;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.gui.event.DbAdditionFrame;
import org.opensha.refFaultParamDb.gui.view.ViewAllReferences;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * <p>Title: ChooseReference.java </p>
 * <p>Description: Choose a reference for this data </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ChooseReference extends JFrame implements ActionListener,
    DbAdditionListener {
  private final static String TIMESPAN_REFERENCES_PARAM_NAME="Choose Reference";
  private final static String addNewReferenceToolTipText = "Add Reference not currently in database";
  private JButton addNewReferenceButton = new JButton("Add Reference");
  private JButton viewAllRefButtons = new JButton("View All References");
  private AddNewReference addNewReference;
  private ConstrainedStringParameterEditor referencesParamEditor;
  private StringParameter referencesParam;
  private JButton okButton = new JButton("OK");
  private JButton cancelButton = new JButton("Cancel");
  private AddSiteInfo addSiteInfo;
  private ArrayList referenceSummaryList;
  private ArrayList referenceList;

  // references DAO
  private ReferenceDB_DAO referenceDAO = new ReferenceDB_DAO(DB_AccessAPI.dbConnection);

  public ChooseReference(AddSiteInfo addSiteInfo) {
    this.setLocationRelativeTo(null);
    this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
    this.addSiteInfo = addSiteInfo;
    addActionListeners();
    addNewReferenceButton.setToolTipText(this.addNewReferenceToolTipText);
    try {
      jbInit();
    }catch(Exception e) {
      e.printStackTrace();
    }
    pack();
  }

  /**
   * Add action listeners to the buttons
   */
  private void addActionListeners() {
    addNewReferenceButton.addActionListener(this);
    this.okButton.addActionListener(this);
    viewAllRefButtons.addActionListener(this);
    this.cancelButton.addActionListener(this);
  }

  /**
   * When user chooses to add a new reference
   * @param event
   */
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if(source == addNewReferenceButton)  {
      addNewReference = new AddNewReference();
      addNewReference.addDbAdditionSuccessListener(this);
    } else if(source == okButton) {
      int index = this.referenceSummaryList.indexOf((String)this.referencesParam.getValue());
      addSiteInfo.setReference((Reference)referenceList.get(index));
      this.dispose();
    } else if (source==cancelButton) {
      addSiteInfo.dispose();
      this.dispose();
    } else if(source == this.viewAllRefButtons) {
      ViewAllReferences viewAllRefs = new ViewAllReferences();
    }
  }

  private void jbInit() throws Exception {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(GUI_Utils.gridBagLayout);
    makeReferencesParamAndEditor();
    int yPos=1;

    contentPane.add(viewAllRefButtons,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    contentPane.add(addNewReferenceButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    contentPane.add(okButton,  new GridBagConstraints(0, yPos, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
    contentPane.add(cancelButton,  new GridBagConstraints(1, yPos++, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
  }


  /**
   * make param and editor for references
   *
   * @throws ConstraintException
   */
  private void makeReferencesParamAndEditor() throws ConstraintException {
    if(referencesParamEditor!=null)
      this.remove(referencesParamEditor);
    // references
    ArrayList availableReferences = getAvailableReferences();
    this.referencesParam = new StringParameter(this.
                                               TIMESPAN_REFERENCES_PARAM_NAME, availableReferences,
                                               (String)availableReferences.get(0));
    referencesParamEditor = new ConstrainedStringParameterEditor(referencesParam);
    this.getContentPane().add(referencesParamEditor,
             new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(0, 0, 0, 0), 0, 0));
  }

  /**
   * Get a list of available references.
   * @return
   */
  private ArrayList getAvailableReferences() {
    this.referenceList  = referenceDAO.getAllReferenesSummary();
    this.referenceSummaryList = new ArrayList();
    for(int i=0; referenceList!=null && i<referenceList.size(); ++i)
      referenceSummaryList.add(((Reference)referenceList.get(i)).getSummary());
    return referenceSummaryList;
  }



  /**
   * This function is called whenever a new site type/ new Reference is added
   * to the database
   *
   * @param event
   */
  public void dbAdditionSuccessful(DbAdditionSuccessEvent event) {
    Object source  = event.getSource();
    if(source == this.addNewReference) makeReferencesParamAndEditor();
    this.validate();
    this.repaint();
  }
}