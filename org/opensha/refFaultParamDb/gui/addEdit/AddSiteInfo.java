package org.opensha.refFaultParamDb.gui.addEdit;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.dao.db.TimeInstanceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.CombinedEventsInfo;
import org.opensha.refFaultParamDb.dao.db.CombinedEventsInfoDB_DAO;

/**
 * <p>Title: AddSiteInfo.java </p>
 * <p>Description: This GUI allows the user to enter a timespan and related info
 * (slip rate or displacement, number of events) about a new site. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class AddSiteInfo extends JFrame implements ActionListener{
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane infoSplitPane1 = new JSplitPane();
  private JSplitPane infoSplitPane2 = new JSplitPane();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private boolean isSlipVisible, isDisplacementVisible, isNumEventsVisible;
  private AddEditNumEvents addEditNumEvents;
  private AddEditSlipRate addEditSlipRate;
  private AddEditCumDisplacement addEditCumDisp;
  private AddEditTimeSpan addEditTimeSpan;
  private final static String ATLEAT_ONE_MSG = "Atleast one of Slip, Cumulative Displacement or Num events should be specified";
  private final static int W = 900;
  private final static int H = 650;
  private final static String TITLE = "Add Data for this Site";
  private final static String MSG_DB_OPERATION_SUCCESS = "Site Info successfully inserted into the database";
  private int siteId;
  private String siteEntryDate;
  private CombinedEventsInfoDB_DAO combinedEventsInfoDAO = new CombinedEventsInfoDB_DAO(DB_AccessAPI.dbConnection);

  public AddSiteInfo(int siteId, String siteEntryDate,
                     boolean isSlipVisible, boolean isDisplacementVisible,
                     boolean isNumEventsVisible)  {
    this.siteId = siteId;
    this.siteEntryDate = siteEntryDate;
    // user should provide info about at least one of slip, cum disp or num events
    if(!isSlipVisible && !isDisplacementVisible && !isNumEventsVisible)
      throw new RuntimeException(ATLEAT_ONE_MSG);
    this.isSlipVisible = isSlipVisible;
    if(isSlipVisible) this.addEditSlipRate = new AddEditSlipRate();
    if(isDisplacementVisible) this.addEditCumDisp = new AddEditCumDisplacement();
    if(isNumEventsVisible) this.addEditNumEvents = new AddEditNumEvents();
    this.isDisplacementVisible = isDisplacementVisible;
    this.isNumEventsVisible = isNumEventsVisible;
    jbInit();
    addActionListeners();
    this.setSize(W,H);
    setTitle(TITLE);
    this.setLocationRelativeTo(null);
    show();
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
    if(source==this.cancelButton) this.dispose();
    else if(source==this.okButton) {
      try {
        putSiteInfoInDatabase(); // put site info in database
        JOptionPane.showMessageDialog(this, MSG_DB_OPERATION_SUCCESS);
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
    // set the time span info
    combinedEventsInfo.setStartTime(addEditTimeSpan.getStartTime());
    combinedEventsInfo.setEndTime(addEditTimeSpan.getEndTime());
    combinedEventsInfo.setShortCitationList(addEditTimeSpan.getTimeSpanShortCitationList());
    combinedEventsInfo.setDatedFeatureComments(addEditTimeSpan.getTimeSpanComments());
    // set the site
    combinedEventsInfo.setSiteEntryDate(this.siteEntryDate);
    combinedEventsInfo.setSiteId(this.siteId);
    // set the slip rate info
    if (isSlipVisible) {
      combinedEventsInfo.setSlipRateComments(this.addEditSlipRate.getSlipRateComments());
      combinedEventsInfo.setASeismicSlipFactorEstimate(this.addEditSlipRate.getAseismicEstimate());
      combinedEventsInfo.setSlipRateEstimate(this.addEditSlipRate.getSlipRateEstimate());
    }
    // set the diplacement info
    if(this.isDisplacementVisible) {
      combinedEventsInfo.setDisplacementComments(this.addEditCumDisp.getDisplacementComments());
      combinedEventsInfo.setASeismicSlipFactorEstimate(addEditCumDisp.getAseismicEstimate());
      combinedEventsInfo.setDisplacementEstimate(addEditCumDisp.getDisplacementEstimate());
    }
    //set the num events info
    if(this.isNumEventsVisible) {
      combinedEventsInfo.setNumEventsComments(this.addEditNumEvents.getNumEventsComments());
      combinedEventsInfo.setNumEventsEstimate(this.addEditNumEvents.getNumEventsEstimate());
    }

    combinedEventsInfoDAO.addCombinedEventsInfo(combinedEventsInfo);
  }


  /**
   * intialize the GUI components
   *
   * @throws java.lang.Exception
   */
  private void jbInit(){
    this.getContentPane().setLayout(gridBagLayout1);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    infoSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    infoSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
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

    JSplitPane splitPane = null;
    String constraints = "";
    this.mainSplitPane.setDividerLocation(W / 2);
    addEditTimeSpan = new AddEditTimeSpan();
    mainSplitPane.add(addEditTimeSpan, JSplitPane.LEFT);

    if (isNumEventsVisible && isDisplacementVisible && isSlipVisible) {
      // if user wants to put data for all the 3, add 2 additional split panes
      mainSplitPane.add(infoSplitPane1, JSplitPane.RIGHT);
      infoSplitPane1.add(infoSplitPane2, JSplitPane.LEFT);
      infoSplitPane1.add(this.addEditNumEvents, JSplitPane.RIGHT);
      infoSplitPane2.add(this.addEditSlipRate, JSplitPane.LEFT);
      infoSplitPane2.add(this.addEditCumDisp, JSplitPane.RIGHT);
      mainSplitPane.setDividerLocation(W/2);
      infoSplitPane1.setDividerLocation(W/3);
      infoSplitPane2.setDividerLocation(W/6);
    }
    else if ( (isNumEventsVisible && isDisplacementVisible) ||
             (isNumEventsVisible && isSlipVisible) ||
             (isDisplacementVisible && isSlipVisible)) {
      // if user wants to add data for just 2, only add 1 additional split pane
      mainSplitPane.add(infoSplitPane1, JSplitPane.RIGHT);

      if(isNumEventsVisible && isDisplacementVisible)  {
        infoSplitPane1.add(this.addEditCumDisp, JSplitPane.LEFT);
        infoSplitPane1.add(this.addEditNumEvents, JSplitPane.RIGHT);
      }

      if(isNumEventsVisible && isSlipVisible)  {
        infoSplitPane1.add(this.addEditSlipRate, JSplitPane.LEFT);
        infoSplitPane1.add(this.addEditNumEvents, JSplitPane.RIGHT);
      }

      if(isDisplacementVisible && isSlipVisible)  {
        infoSplitPane1.add(this.addEditCumDisp, JSplitPane.LEFT);
        infoSplitPane1.add(this.addEditSlipRate, JSplitPane.RIGHT);
      }
      mainSplitPane.setDividerLocation(W/2);
      infoSplitPane1.setDividerLocation(W/4);
    }
    else {
      // no need to add another split pane, if user just wants to enter 1 parameter
      if (isNumEventsVisible) // if num events estimate is visible
        mainSplitPane.add(this.addEditNumEvents, JSplitPane.RIGHT);
      else if (this.isSlipVisible) // if slip rate estimate is visible
        mainSplitPane.add(this.addEditSlipRate, JSplitPane.RIGHT);
      else if (this.isDisplacementVisible) // if displacement is visible
        mainSplitPane.add(this.addEditCumDisp, JSplitPane.RIGHT);
      mainSplitPane.setDividerLocation(W/2);
    }
  }
}