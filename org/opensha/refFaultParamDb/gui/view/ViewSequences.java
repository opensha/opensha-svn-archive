package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import javax.swing.JButton;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import java.util.ArrayList;
import java.awt.*;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditSequence;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.vo.PaleoSite;
import org.opensha.refFaultParamDb.dao.db.EventSequenceDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.vo.EventSequence;
import org.opensha.refFaultParamDb.data.TimeAPI;
import org.opensha.refFaultParamDb.vo.PaleoEvent;
import org.opensha.refFaultParamDb.vo.Reference;

/**
 * <p>Title: ViewSequences.java </p>
 * <p>Description: This allows the user to view the event sequences for a selected
 * paleo site </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewSequences extends LabeledBoxPanel implements ParameterChangeListener {

  // various parameter names
  private final static String SEQUENCE_NAME_PARAM_NAME = "Sequence Name";
  private final static String SEQUENCE_PROB_PARAM_NAME = "Sequence Prob.";
  private final static String COMMENTS_PARAM_NAME = "Comments";
  private final static String START_TIME_PARAM_NAME = "Start Time";
  private final static String END_TIME_PARAM_NAME = "End Time";
  private final static String REFERENCES_PARAM_NAME = "References";
  private final static String MISSED_EVENTS_PROB_PARAM_NAME = "Probability of missed events";
  private final static String EVENTS_PARAM_NAME = "Events in Sequence";
  private final static String TITLE = "Sequences";

  // test sequence names
  private final static String TEST_SEQUENCE1 = "Test Sequence 1";
  private final static String TEST_SEQUENCE2 = "Test Sequence 2";

  // labels to show the information
  private InfoLabel sequenceProbLabel = new InfoLabel();
  private InfoLabel eventsLabel = new InfoLabel();
  private InfoLabel missedProbLabel = new InfoLabel();
  private InfoLabel commentsLabel = new InfoLabel();
  private InfoLabel startTimeLabel = new InfoLabel();
  private InfoLabel endTimeLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();

  // StringParameter and editor to show list of all sequences
  private StringParameter sequenceNameParam;
  private ConstrainedStringParameterEditor sequenceNamesEditor;

  // site for which seequences will be displayed
  private PaleoSite paleoSite;
  private EventSequenceDB_DAO sequenceDAO = new EventSequenceDB_DAO(DB_AccessAPI.dbConnection);
  private ArrayList sequenceNamesList;
  private ArrayList sequencesList;

  public ViewSequences() {
    try {
     this.setLayout(GUI_Utils.gridBagLayout);
     // add Parameters and editors
     createSequencesListParameterEditor();
     // add the parameter editors to the GUI componenets
     addEditorstoGUI();
     // set the title
     this.setTitle(TITLE);
   }
   catch(Exception e) {
     e.printStackTrace();
   }
  }

  /**
   * Intialize the parameters and editors and add to the GUI
   */
  private void createSequencesListParameterEditor()  {

    // event name parameter
     if(this.sequenceNamesEditor!=null) this.remove(sequenceNamesEditor);

    ArrayList sequenceNamesList = getSequenceNamesList();
    sequenceNameParam = new StringParameter(this.SEQUENCE_NAME_PARAM_NAME, sequenceNamesList,
                                         (String)sequenceNamesList.get(0));
    sequenceNameParam.addParameterChangeListener(this);
    sequenceNamesEditor = new ConstrainedStringParameterEditor(sequenceNameParam);
    add(sequenceNamesEditor ,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
    sequenceNamesEditor.refreshParamEditor();
    this.updateUI();

    // set event info according to selected event
    this.setSequenceInfo((String)sequenceNameParam.getValue());
  }

  /**
   *
   * @return
   */
  private ArrayList getSequenceNamesList() {

    this.sequencesList = null;
    this.sequenceNamesList = new ArrayList();
    if(isTestSite()) {
      sequenceNamesList.add(TEST_SEQUENCE1);
      sequenceNamesList.add(TEST_SEQUENCE2);
    } else {
      sequencesList=this.sequenceDAO.getSequences(paleoSite.getSiteId());
      if(sequencesList==null || sequencesList.size()==0) // if no event exists for this site
        sequenceNamesList.add(InfoLabel.NOT_AVAILABLE);
      else {
        // make a list of event names
        for(int i=0; i<sequencesList.size(); ++i)
          sequenceNamesList.add(((EventSequence)sequencesList.get(i)).getSequenceName());
      }
    }
    return sequenceNamesList;
  }

  /**
   * If selected site is a test site
   *
   * @return
   */
  private boolean isTestSite() {
    return paleoSite == null ||
        paleoSite.getSiteName().equalsIgnoreCase(ViewSiteCharacteristics.
                                                 TEST_SITE);
  }



  /**
  * Add all the event information to theGUI
  */
 private void addEditorstoGUI() {
   int yPos=1;
   add(GUI_Utils.getPanel(this.sequenceProbLabel,this.SEQUENCE_PROB_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(this.eventsLabel,this.EVENTS_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(missedProbLabel,this.MISSED_EVENTS_PROB_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(commentsLabel,COMMENTS_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(startTimeLabel,START_TIME_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(endTimeLabel,END_TIME_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
   add(GUI_Utils.getPanel(referencesLabel,REFERENCES_PARAM_NAME) ,  new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
 }


  /**
   * This function is called whenever a parameter is changed and we have
   * registered as listeners to that parameters
   *
   * @param event
   */
  public void parameterChange(ParameterChangeEvent event) {
    this.setSequenceInfo((String)sequenceNameParam.getValue());
  }


  /**
   * Sitename for which sequences will be displayed
   *
   * @param siteName
   */
  public void setSite(PaleoSite paleoSite) {
    this.paleoSite = paleoSite;
    createSequencesListParameterEditor();
  }


  /**
  * Show the info according to event selected by the user
  *
  * @param eventName
  */
 private void setSequenceInfo(String sequenceName) {
   // just set some fake implementation right now
   // event time estimate
   if(this.isTestSite() && (sequenceName.equalsIgnoreCase(this.TEST_SEQUENCE1) ||
      sequenceName.equalsIgnoreCase(this.TEST_SEQUENCE2))) {
     // comments
     String comments = "Comments about this sequence";
     // references
     ArrayList references = new ArrayList();
     references.add("Ref 4");
     references.add("Ref 1");
     // events in this sequence
     ArrayList eventsList = new ArrayList();
     eventsList.add("Event 5");
     eventsList.add("Event 6");
     // sequence prob
     double sequenceProb=0.5;
     // missed events prob
     double[] missedEventProb = {0.1,0.5, 0.4};
     updateLabels(sequenceProb, eventsList, missedEventProb, comments, null, null, references);
   }else if(this.sequencesList!=null && this.sequencesList.size()!=0) {
      int index  = this.sequenceNamesList.indexOf(sequenceName);
      EventSequence eventSequence = (EventSequence)this.sequencesList.get(index);
      // make a list of event names from event list
      ArrayList paleoEventsList = eventSequence.getEventsParam();
      ArrayList eventNames = new ArrayList();
      for(int i=0; i<paleoEventsList.size(); ++i) {
        PaleoEvent paleoEvent = (PaleoEvent)paleoEventsList.get(i);
        eventNames.add(paleoEvent.getEventName());
      }
      ArrayList refList = eventSequence.getStartTime().getReferencesList();
      ArrayList refSummaryList = new ArrayList();
      for(int i=0; i<refList.size(); ++i)
        refSummaryList.add(((Reference)refList.get(i)).getSummary());
      updateLabels(eventSequence.getSequenceProb(), eventNames,
                   eventSequence.getMissedEventsProbs(), eventSequence.getComments(),
                   eventSequence.getStartTime(), eventSequence.getEndTime(),
                   refSummaryList);
   }
   else {
     updateLabels(Double.NaN, null, null, null,
                  null,null,null);
   }
 }

 /**
  * Update the labels to view the information about the sequences
  * @param sequenceProb
  * @param eventsInthisSequence
  * @param missedEventProbs
  * @param comments
  * @param references
  */
 private void updateLabels(double sequenceProb, ArrayList eventsInthisSequence,
                           double[] missedEventProbs, String comments,
                           TimeAPI startTime, TimeAPI endTime,
                           ArrayList references) {

   if(Double.isNaN(sequenceProb)) {
     sequenceProbLabel.setTextAsHTML((String)null);
   }
   else sequenceProbLabel.setTextAsHTML(""+sequenceProb);
   ArrayList missedProbInfoList = null;
   if(eventsInthisSequence!=null ) {
     missedProbInfoList = new ArrayList();
     ArrayList names = AddEditSequence.getNamesForMissedEventProbs(
         eventsInthisSequence);
     for (int i = 0; i < names.size(); ++i)
       missedProbInfoList.add(names.get(i) + ": " + missedEventProbs[i]);
   }
   missedProbLabel.setTextAsHTML(missedProbInfoList);
   commentsLabel.setTextAsHTML(comments);
   eventsLabel.setTextAsHTML(eventsInthisSequence);
   this.startTimeLabel.setTextAsHTML(startTime);
   this.endTimeLabel.setTextAsHTML(endTime);
   referencesLabel.setTextAsHTML(references);
 }

}