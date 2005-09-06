package org.opensha.refFaultParamDb.gui.infotools;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import org.opensha.gui.TitledBorderPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GUI_Utils {
  public final static GridBagLayout gridBagLayout = new GridBagLayout();

  /**
   * Get Bordered Panel
   *
   * @param infoLabel
   * @param borderTitle
   * @return
   */
   public static JPanel getPanel(InfoLabel infoLabel, String borderTitle) {
     JPanel panel = new TitledBorderPanel(borderTitle+":");
     panel.setLayout(gridBagLayout);
     panel.add(infoLabel,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
     return panel;
   }


}