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
  private final static int W=550;
  private final static int H=680;

  private JSplitPane mapSplitPane = new JSplitPane();
  private JScrollPane mapScrollPane = new JScrollPane();
  private JTextPane mapText = new JTextPane();
  private JLabel mapLabel = new JLabel();

  private String imageFile = new String();

  private String mapInfo= new String();
  private BorderLayout borderLayout1 = new BorderLayout();
  public ImageViewerWindow(String imageFileName,String mapInfo) {
    imageFile = imageFileName;
    mapInfo = this.mapInfo;
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
    this.setSize(W,H);
    this.setTitle(imageFile+" ShakeMap");
    this.getContentPane().setLayout(borderLayout1);
    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mapText.setText(mapInfo);
    mapText.setEditable(false);
    mapText.setSelectionColor(Color.blue);

    //adding the image to the label
    mapLabel.setIcon(new ImageIcon(imageFile));
    this.getContentPane().add(mapSplitPane, BorderLayout.CENTER);
    mapSplitPane.add(mapScrollPane, JSplitPane.TOP);
    mapSplitPane.add(mapText, JSplitPane.BOTTOM);
    mapScrollPane.getViewport().add(mapLabel, null);
    mapSplitPane.setDividerLocation(600);
  }
}