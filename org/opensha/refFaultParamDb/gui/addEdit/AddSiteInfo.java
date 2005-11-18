package org.opensha.refFaultParamDb.gui.addEdit;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.dao.db.TimeInstanceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.dao.db.CombinedEventsInfoDB_DAO;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.dao.db.EventSequenceDB_DAO;
import org.opensha.refFaultParamDb.gui.event.DbAdditionFrame;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.vo.Reference;
import org.opensha.refFaultParamDb.vo.CombinedSlipRateInfo;
import org.opensha.refFaultParamDb.vo.CombinedDisplacementInfo;
import org.opensha.refFaultParamDb.vo.CombinedNumEventsInfo;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.gui.infotools.ConnectToEmailServlet;

/**
 * <p>Title: AddSiteInfo.java </p>
 * <p>Description: This GUI allows the user to enter a timespan and related info
 * (slip rate or displacement, number of events) about a new site. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddSiteInfo extends DbAdditionFrame implements ActionListener{
  private JSplitPane mainSplitPane = new JSplitPane();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private boolean isSlipVisible, isDisplacementVisible, isNumEventsVisible, isSequenceVisible;
  private ArrayList referenceList;
  private AddEditNumEvents addEditNumEvents;
  private AddEditSlipRate addEditSlipRate;
  private AddEditCumDisplacement addEditCumDisp;
  private AddEditSequence addEditSequence;
  private AddEditTimeSpan addEditTimeSpan;
  private JTabbedPane tabbedPane = new JTabbedPane();
  private final static String NUM_EVENTS_TITLE = "Num Events Est";
  private final static String SLIP_RATE_TITLE = "Slip Rate Est";
  private final static String DISPLACEMENT_TITLE = "Displacement Est";
  private final static String SEQUENCE_TITLE = "Sequence";
  private final static String ATLEAT_ONE_MSG = "Atleast one of Slip, Cumulative Displacement, Num events or Sequence should be specified";
  private final static int W = 900;
  private final static int H = 650;
  private final static String TITLE = "Add Data for this Site";
  private final static String MSG_DB_OPERATION_SUCCESS = "Site Info successfully inserted into the database";
  private int siteId;
  private String siteEntryDate;
  private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);

  public AddSiteInfo(int siteId, String siteEntryDate,
                     boolean isSlipVisible, boolean isDisplacementVisible,
                     boolean isNumEventsVisible, boolean isSequenceVisible)  {


    this.siteId = siteId;
    // user should provide info about at least one of slip, cum disp or num events
    if(!isSlipVisible && !isDisplacementVisible && !isNumEventsVisible && !isSequenceVisible)
      throw new RuntimeException(ATLEAT_ONE_MSG);
    this.isSlipVisible = isSlipVisible;
    this.isDisplacementVisible = isDisplacementVisible;
    this.isNumEventsVisible = isNumEventsVisible;
    this.isSequenceVisible = isSequenceVisible;

    this.siteEntryDate = siteEntryDate;
    try {
      if (this.isSequenceVisible)
        this.addEditSequence = new AddEditSequence(siteId, siteEntryDate);
    }catch(RuntimeException e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      this.isSequenceVisible = false;
      this.dispose();
      return;
    }
    if(isSlipVisible) this.addEditSlipRate = new AddEditSlipRate();
    if(isDisplacementVisible) this.addEditCumDisp = new AddEditCumDisplacement();
    if(isNumEventsVisible) this.addEditNumEvents = new AddEditNumEvents();
    jbInit();
    addActionListeners();
    this.setSize(W,H);
    setTitle(TITLE);
    this.setLocationRelativeTo(null);
    show();
    // show window to get the reference
    JFrame referencesDialog = new ChooseReference(this);
    referencesDialog.show();
    this.setEnabled(false);

  }

  public void setReference(Reference reference) {
    referenceList = new ArrayList();
    referenceList.add(reference);
    int pubYear = Integer.parseInt(reference.getRefYear());
    this.addEditTimeSpan.setNowYearVal(pubYear);
    this.setEnabled(true);
  }

  /**
   * Add the action listeners on the buttons
   */
  private void addActionListeners() {
    this.okButton.addActionListener(this);
    this.cancelButton.addActionListener(this);
  }

  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if(source==this.cancelButton) this.dispose(); // cancel button is clicked
    else if(source==this.okButton) { // ok button is clicked
      try {
        if(this.isSlipVisible || this.isDisplacementVisible ||
           this.isNumEventsVisible || this.isSequenceVisible)
          putSiteInfoInDatabase(); // put site info in database
        JOptionPane.showMessageDialog(this, MSG_DB_OPERATION_SUCCESS);
        ConnectToEmailServlet.sendEmail("New Site Info added for site Id="+this.siteId +" by "+SessionInfo.getUserName());
        this.dispose();
      }catch(Exception e){
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
  }

  /**
   * Put the site info in the database
   */
  private void putSiteInfoInDatabase() {
    CombinedEventsInfo combinedEventsInfo = new CombinedEventsInfo();
    // it is not expert opinion. this is publication info
    combinedEventsInfo.setIsExpertOpinion(false);
    // set the time span info
    TimeAPI startTime  = addEditTimeSpan.getStartTime();
    startTime.setReferencesList(this.referenceList);
    TimeAPI endTime  = addEditTimeSpan.getEndTime();
    endTime.setReferencesList(this.referenceList);
    combinedEventsInfo.setStartTime(startTime);
    combinedEventsInfo.setEndTime(endTime);
    combinedEventsInfo.setReferenceList(referenceList);
    combinedEventsInfo.setDatedFeatureComments(addEditTimeSpan.getTimeSpanComments());
    // set the site
    combinedEventsInfo.setSiteEntryDate(this.siteEntryDate);
    combinedEventsInfo.setSiteId(this.siteId);
    // set the slip rate info
    if (isSlipVisible) {
      CombinedSlipRateInfo combinedSlipRateInfo = addEditSlipRate.getCombinedSlipRateInfo();
      combinedEventsInfo.setCombinedSlipRateInfo(combinedSlipRateInfo);
    }
    // set the diplacement info
    if(this.isDisplacementVisible) {
      CombinedDisplacementInfo combinedDisplacementInfo = addEditCumDisp.getCombinedDisplacementInfo();
      combinedEventsInfo.setCombinedDisplacementInfo(combinedDisplacementInfo);
    }
    //set the num events info
    if(this.isNumEventsVisible) {
      CombinedNumEventsInfo combinedNumEventsInfo = addEditNumEvents.getCombinedNumEventsInfo();
      combinedEventsInfo.setCombinedNumEventsInfo(combinedNumEventsInfo);
    }
    // set the sequence info
    if(this.isSequenceVisible) {
      combinedEventsInfo.setEventSequenceList(addEditSequence.getAllSequences());
    }

    combinedEventsInfoDAO.addCombinedEventsInfo(combinedEventsInfo);
    this.sendEventToListeners(combinedEventsInfo);
  }


  /**
   * intialize the GUI components
   *
   * @throws java.lang.Exception
   */
  private void jbInit(){
    this.getContentPane().setLayout(gridBagLayout1);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    okButton.setText("OK");
    cancelButton.setText("Cancel");
    this.getContentPane().add(mainSplitPane,
                              new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(1, 3, 0, 0), 237, 411));
    this.getContentPane().add(okButton,
                              new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 155, 11, 0), 39, -1));
    this.getContentPane().add(cancelButton,
                              new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 22, 11, 175), 8, 0));
    String constraints = "";
    addEditTimeSpan = new AddEditTimeSpan();
    mainSplitPane.add(addEditTimeSpan, JSplitPane.LEFT);
    mainSplitPane.add(this.tabbedPane, JSplitPane.RIGHT);
    if (this.isSlipVisible) // if slip rate estimate is visible
      tabbedPane.add(this.SLIP_RATE_TITLE, this.addEditSlipRate);
    if (this.isDisplacementVisible) // if displacement is visible
      tabbedPane.add(this.DISPLACEMENT_TITLE, this.addEditCumDisp);
    if (isNumEventsVisible) // if num events estimate is visible
      tabbedPane.add(this.NUM_EVENTS_TITLE, addEditNumEvents);
    if(this.isSequenceVisible) // if sequence is visible
      tabbedPane.add(SEQUENCE_TITLE, this.addEditSequence);
    mainSplitPane.setDividerLocation(2*W/3);
  }
}