package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import java.awt.LayoutManager;
import java.awt.LayoutManager;
import javax.swing.JButton;
import java.awt.*;
import org.opensha.param.*;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.data.estimate.IntegerEstimate;
import org.opensha.refFaultParamDb.gui.CommentsParameterEditor;
import java.util.ArrayList;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditNumEvents;
import java.awt.event.ActionListener;


/**
 * <p>Title: ViewNumEvents.java </p>
 * <p>Description: Voew num event information for a site for a time period</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewNumEvents extends LabeledBoxPanel implements ActionListener {
  private final static String NUM_EVENTS_TITLE = "Number of Events";
  // edit buttons
  private JButton editNumEventsButton = new JButton("Edit");
  private final static String EDIT_TITLE = "Edit Num Events";

  private InfoLabel numEventsEstimateLabel = new InfoLabel();
  private InfoLabel referencesLabel = new InfoLabel();
  private StringParameter commentsParam = new StringParameter("Num Events Comments");
  private CommentsParameterEditor commentsParamEditor;

  public ViewNumEvents() {
    super(GUI_Utils.gridBagLayout);
    try {
      viewNumEventsForTimePeriod();
      setTitle(this.NUM_EVENTS_TITLE);
      editNumEventsButton.addActionListener(this);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Set the information about number of events estimate, comments and references
   * based on site selected by the user.
   *
   * @param numEventsEstimate
   * @param comments
   * @param references
   */
  public void setInfo(IntegerEstimate numEventsEstimate, String comments,
                      ArrayList references) {
    numEventsEstimateLabel.setTextAsHTML(numEventsEstimate);
    referencesLabel.setTextAsHTML(references);
    commentsParam.setValue(comments);
    commentsParamEditor.refreshParamEditor();
  }

  /**
  * display the Num events info for the selected time period
  */
 private void viewNumEventsForTimePeriod() throws Exception {


   JPanel slipRateEstimatePanel = GUI_Utils.getPanel(numEventsEstimateLabel, "Num Events Estimate");

   // comments
   commentsParamEditor = new CommentsParameterEditor(commentsParam);
   commentsParamEditor.setEnabled(false);


   // references
   JPanel referencesPanel = GUI_Utils.getPanel(this.referencesLabel, "References");

   // add the slip rate info the panel
   int yPos=0;
   add(this.editNumEventsButton,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 0.0
       ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(slipRateEstimatePanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(commentsParamEditor,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(referencesPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
 }

 /**
  * This function is called when edit button is clicked
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   JFrame frame= new JFrame(EDIT_TITLE);
   AddEditNumEvents addEditNumEvents =  new AddEditNumEvents();
   Container contentPane = frame.getContentPane();
   contentPane.setLayout(GUI_Utils.gridBagLayout);
   contentPane.add(addEditNumEvents, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
       , GridBagConstraints.CENTER,
       GridBagConstraints.BOTH,
       new Insets(0, 0, 0, 0), 0, 0));
   frame.pack();
   frame.setSize(300,750);
   frame.setLocationRelativeTo(null);
   frame.show();
 }


}