package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;

/**
 * <p>Title: ImageViewerWindow</p>
 * <p>Description: this Class thye displays the image of the GMT Map in the
 * Frame window</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class ImageViewerWindow extends JFrame {
  private JSplitPane mapSplitPane = new JSplitPane();
  private JScrollPane mapScrollPane = new JScrollPane();
  private JTextPane mapText = new JTextPane();
  private JLabel mapLabel = new JLabel();

  private String imageFile = new String();
  public ImageViewerWindow(String imageFileName) {
    imageFile = imageFileName;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.show();
    this.pack();
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(null);
    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mapSplitPane.setBounds(new Rectangle(0, 0, 648, 719));
    mapText.setEditable(false);
    mapText.setSelectionColor(Color.blue);

    //adding the image to the label
    mapLabel.setIcon(new ImageIcon(imageFile));
    this.getContentPane().add(mapSplitPane, null);
    mapSplitPane.add(mapScrollPane, JSplitPane.TOP);
    mapSplitPane.add(mapText, JSplitPane.BOTTOM);
    mapScrollPane.getViewport().add(mapLabel, null);
    mapSplitPane.setDividerLocation(600);
  }
}