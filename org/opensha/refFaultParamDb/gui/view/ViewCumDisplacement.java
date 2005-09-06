package org.opensha.refFaultParamDb.gui.view;

import org.opensha.gui.LabeledBoxPanel;
import java.awt.LayoutManager;
import javax.swing.JButton;
import java.awt.*;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;

/**
 * <p>Title: ViewCumDisplacement.java </p>
 * <p>Description: View cumulative displacement for a site for a time period </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ViewCumDisplacement extends LabeledBoxPanel {
  private final static String DISPLACEMENT_TITLE = "Displacement";
  // edit buttons
  private JButton editCumDisplacementButton = new JButton("Edit");


  public ViewCumDisplacement() {
    super(GUI_Utils.gridBagLayout);
    viewDisplacementForTimePeriod();
    setTitle(this.DISPLACEMENT_TITLE);
  }

  /**
  * Display the displacement info for the selected time period
  */
 private void viewDisplacementForTimePeriod() {

   // comments
   String comments = "Displacement is implied when Slip Rate is provided";
   InfoLabel commentsLabel = new InfoLabel(comments);
   int yPos=0;
   add(commentsLabel,new GridBagConstraints( 0, yPos++, 1, 1, 1.0, 1.0
       ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

 }



}