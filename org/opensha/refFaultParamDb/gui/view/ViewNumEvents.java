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


/**
 * <p>Title: ViewNumEvents.java </p>
 * <p>Description: Voew num event information for a site for a time period</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewNumEvents extends LabeledBoxPanel {
  private final static String NUM_EVENTS_TITLE = "Number of Events";
  // edit buttons
  private JButton editNumEventsButton = new JButton("Edit");

  public ViewNumEvents() {
    super(GUI_Utils.gridBagLayout);
    try {
      viewNumEventsForTimePeriod();
      setTitle(this.NUM_EVENTS_TITLE);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
  * display the Num events info for the selected time period
  */
 private void viewNumEventsForTimePeriod() throws Exception {

   // Num Events Estimate
   ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
   func.set(4.0, 0.2);
   func.set(5.0, 0.3);
   func.set(6.0, 0.1);
   func.set(7.0, 0.4);
   IntegerEstimate numEventsEstimate = new IntegerEstimate(func, false);
   JPanel slipRateEstimatePanel = GUI_Utils.getPanel(new InfoLabel(numEventsEstimate), "Num Events Estimate");


   // comments
   String comments = "Pertinent comments will be displayed here";
   StringParameter commentsParam = new StringParameter("Num Events Comments", comments);
   CommentsParameterEditor commentsPanel = new CommentsParameterEditor(commentsParam);
   commentsPanel.setEnabled(false);


   // references
   ArrayList references = new ArrayList();
   references.add("Ref 5");
   references.add("Ref 7");
   JPanel referencesPanel = GUI_Utils.getPanel(new InfoLabel(references), "References");

   // add the slip rate info the panel
   int yPos=0;
   add(this.editNumEventsButton,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 0.0
       ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(slipRateEstimatePanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(commentsPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
   add(referencesPanel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
 }

}