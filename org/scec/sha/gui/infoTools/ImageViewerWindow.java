package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.net.*;

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
  private JLabel mapLabel = new JLabel();

  private String imageFile = new String();
  private boolean gmtFromServer = false;

  private String mapInfo= new String();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea mapText = new JTextArea();
  public ImageViewerWindow(String imageFileName,String mapInfo,boolean gmtFromServer) {
    imageFile = imageFileName;
    this.mapInfo = mapInfo;
    this.gmtFromServer = gmtFromServer;
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
    this.setTitle(imageFile);
    this.getContentPane().setLayout(borderLayout1);
    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mapSplitPane.setMaximumSize(new Dimension(550, 100));

    //adding the image to the label
    if(!this.gmtFromServer)
      mapLabel.setIcon(new ImageIcon(imageFile));
    else
      mapLabel.setIcon(new ImageIcon(new URL(imageFile)));

    mapText.setText(mapInfo);
    mapText.setLineWrap(true);
    mapText.setForeground(Color.blue);
    mapText.setMaximumSize(new Dimension(550, 100));
    mapText.setEditable(false);
    mapText.setSelectedTextColor(new Color(80, 80, 133));
    mapText.setSelectionColor(Color.blue);
    this.getContentPane().add(mapSplitPane, BorderLayout.CENTER);
    mapSplitPane.add(mapScrollPane, JSplitPane.TOP);
    mapSplitPane.add(jScrollPane1, JSplitPane.BOTTOM);
    jScrollPane1.getViewport().add(mapText, null);
    mapScrollPane.getViewport().add(mapLabel, null);
    mapSplitPane.setDividerLocation(600);
  }
}