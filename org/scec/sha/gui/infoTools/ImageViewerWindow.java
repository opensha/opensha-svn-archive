package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import javax.swing.event.*;

import org.scec.util.BrowserLauncher;

/**
 * <p>Title: ImageViewerWindow</p>
 * <p>Description: this Class thye displays the image of the GMT Map in the
 * Frame window</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class ImageViewerWindow extends JFrame implements HyperlinkListener{
  private final static int W=650;
  private final static int H=800;

  private JSplitPane mapSplitPane = new JSplitPane();
  private JScrollPane mapScrollPane = new JScrollPane();
  private JLabel mapLabel = new JLabel();

  private String imageFile = new String();
  private boolean gmtFromServer = false;

  private String mapInfo= new String();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane mapInfoScrollPane = new JScrollPane();
  private JTextPane mapText = new JTextPane();
  private final static String HTML_START = "<html><body>";
  private final static String HTML_END = "</body></html>";
  public ImageViewerWindow(String imageFileName,String mapInfo,boolean gmtFromServer) {
    imageFile = imageFileName;
    this.mapInfo = mapInfo;
    this.gmtFromServer = gmtFromServer;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }    this.show();

  }
  private void jbInit() throws Exception {
    this.setSize(W,H);
    this.setTitle(imageFile);
    this.getContentPane().setLayout(borderLayout1);
    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);


    //adding the image to the label
    if(!this.gmtFromServer)
      mapLabel.setIcon(new ImageIcon(imageFile));
    else
      mapLabel.setIcon(new ImageIcon(new URL(imageFile)));

    mapInfoScrollPane.getViewport().add(mapText, null);

    mapText.setContentType("text/html");
    mapText.setText(HTML_START+mapInfo+HTML_END);
    mapText.setEditable(false);
    mapText.setForeground(Color.blue);
    mapText.setEditable(false);
    mapText.setSelectedTextColor(new Color(80, 80, 133));
    mapText.setSelectionColor(Color.blue);
    mapText.addHyperlinkListener(this);
    this.getContentPane().add(mapSplitPane, BorderLayout.CENTER);
    mapSplitPane.add(mapScrollPane, JSplitPane.TOP);
    mapSplitPane.add(mapInfoScrollPane, JSplitPane.BOTTOM);

    mapScrollPane.getViewport().add(mapLabel, null);
    mapSplitPane.setDividerLocation(550);
  }


  /** This method implements HyperlinkListener.  It is invoked when the user
   * clicks on a hyperlink, or move the mouse onto or off of a link
   **/
  public void hyperlinkUpdate(HyperlinkEvent e) {
    HyperlinkEvent.EventType type = e.getEventType();  // what happened?
    if (type == HyperlinkEvent.EventType.ACTIVATED) {     // Click!
      try{
       org.scec.util.BrowserLauncher.openURL(e.getURL().toString());
      }catch(Exception ex) { ex.printStackTrace(); }

      //displayPage(e.getURL());   // Follow the link; display new page
    }
  }

}


