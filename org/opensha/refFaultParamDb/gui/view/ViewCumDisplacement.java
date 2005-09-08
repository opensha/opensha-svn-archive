package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import java.awt.LayoutManager;
import javax.swing.JButton;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import org.opensha.refFaultParamDb.gui.addEdit.AddEditCumDisplacement;

/**
 * <p>Title: ViewCumDisplacement.java </p>
 * <p>Description: View cumulative displacement for a site for a time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewCumDisplacement extends LabeledBoxPanel implements ActionListener {
  private final static String DISPLACEMENT_TITLE = "Displacement";
  // edit buttons
  private JButton editCumDisplacementButton = new JButton("Edit");
  private final static String EDIT_TITLE = "Edit Cumulative Displacement";
  InfoLabel commentsLabel = new InfoLabel();

  public ViewCumDisplacement() {
    super(GUI_Utils.gridBagLayout);
    viewDisplacementForTimePeriod();
    setTitle(this.DISPLACEMENT_TITLE);
    editCumDisplacementButton.addActionListener(this);

  }

  /**
  * Display the displacement info for the selected time period
  */
 private void viewDisplacementForTimePeriod() {

   // comments
   String comments = "Implied from Slip Rate";
   commentsLabel.setTextAsHTML(comments);
   int yPos=0;
   add(commentsLabel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

 }

 /**
  * This function is called when edit button is clicked
  * @param event
  */
 public void actionPerformed(ActionEvent event) {
   JFrame frame= new JFrame(EDIT_TITLE);
   AddEditCumDisplacement addEditCumDisp =  new AddEditCumDisplacement();
   Container contentPane = frame.getContentPane();
   contentPane.setLayout(GUI_Utils.gridBagLayout);
   contentPane.add(addEditCumDisp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
       , GridBagConstraints.CENTER,
       GridBagConstraints.BOTH,
       new Insets(0, 0, 0, 0), 0, 0));
   frame.pack();
   frame.setSize(300,750);
   frame.setLocationRelativeTo(null);
   frame.show();
 }


}