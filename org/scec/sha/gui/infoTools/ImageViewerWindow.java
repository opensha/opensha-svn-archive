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
  private final static int H=730;


  private final static String MAP_WINDOW = "Maps using GMT";
  private JSplitPane mapSplitPane = new JSplitPane();
  private JScrollPane mapScrollPane = new JScrollPane();


  private boolean gmtFromServer = true;

  private String mapInfo= new String();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane mapInfoScrollPane = new JScrollPane();
  private JPanel mapPanel = new JPanel();
  private GridBagLayout layout = new GridBagLayout();
  private JTextPane mapText = new JTextPane();
  private final static String HTML_START = "<html><body>";
  private final static String HTML_END = "</body></html>";

  /**
   * Class constructor
   * @param imageFileName : Name of the image file to be shown
   * @param mapInfo : Metadata about the Map
   * @param gmtFromServer : boolean to check if map to be generated using the Server GMT
   * @throws RuntimeException
   */
  public ImageViewerWindow(String imageFileName,String mapInfo,boolean gmtFromServer)
      throws RuntimeException{
    this.mapInfo = mapInfo;
    this.gmtFromServer = gmtFromServer;
    try {
      jbInit();
    }catch(RuntimeException e) {
      throw new RuntimeException(e.getMessage());
    }
    addImageToWindow(imageFileName);
    this.show();
  }

  /**
   * Class constructor
   * @param imageFileName : String array containing names of the image files to be shown(if
   * more than one image is to be shown in the same window.
   * @param mapInfo : Metadata about the Map
   * @param gmtFromServer : boolean to check if map to be generated using the Server GMT
   * @throws RuntimeException
   */
  public ImageViewerWindow(String[] imageFileNames,String mapInfo,boolean gmtFromServer)
      throws RuntimeException{
    this.mapInfo = mapInfo;
    this.gmtFromServer = gmtFromServer;
    try {
      jbInit();
    }catch(RuntimeException e) {
      throw new RuntimeException(e.getMessage());
    }
    addImagesToWindow(imageFileNames);
    this.show();
  }


  private void jbInit() throws RuntimeException {
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(W,H);
    this.setTitle(MAP_WINDOW);
    this.getContentPane().setLayout(borderLayout1);
    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

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
    mapPanel.setLayout(layout);
    mapScrollPane.getViewport().add(mapPanel, null);
    mapSplitPane.setDividerLocation(550);
  }

  /**
   * This function plots all the images for the dataset in a single map window
   * @param imageURLs : String array of all the images URL/Absolute Path.
   */
  private void addImagesToWindow(String[] imageURLs){
    int size  = imageURLs.length;
    JLabel[] mapLabel = new JLabel[size];
    for(int i=0; i<size;++i){
      mapLabel[i] = new JLabel();
      if(gmtFromServer){
        try{
          mapLabel[i].setIcon(new ImageIcon(new URL((String)imageURLs[i])));
        }catch(Exception e){
          throw new RuntimeException("No Internet connection available");
        }
      }
      else
        mapLabel[i].setIcon(new ImageIcon((String)imageURLs[i]));
      mapPanel.add(mapLabel[i],new GridBagConstraints(0, i, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 0, 0));
    }
  }


  /**
   * This function plots the image for the dataset in a single map window
   * @param imageFile : Absolute Path/URL to the image.
   */
  private void addImageToWindow(String imageFile){
    JLabel mapLabel = new JLabel();
    //adding the image to the label
    if(!this.gmtFromServer)
      mapLabel.setIcon(new ImageIcon(imageFile));
    else
      try{
      mapLabel.setIcon(new ImageIcon(new URL(imageFile)));
    }catch(Exception e){
      throw new RuntimeException("No Internet connection available");
    }
    mapPanel.add(mapLabel,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 5, 5), 0, 0));
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


