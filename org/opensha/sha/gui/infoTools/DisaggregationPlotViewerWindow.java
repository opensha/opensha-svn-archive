package org.opensha.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import javax.swing.event.*;


import org.opensha.util.BrowserLauncher;
import org.opensha.util.ImageUtils;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

//import org.jpedal.PdfDecoder;
//import org.jpedal.exception.PdfException;


/**
 * <p>Title: DisaggregationPlotViewerWindow</p>
 * <p>Description: this Class thye displays the image of the GMT Map in the
 * Frame window</p>
 * @author: Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class DisaggregationPlotViewerWindow extends JFrame implements HyperlinkListener{
  private final static int W=650;
  private final static int H=730;


  private final static String MAP_WINDOW = "Maps using GMT";
  private JSplitPane mapSplitPane = new JSplitPane();
  private JScrollPane mapScrollPane = new JScrollPane();


  private boolean gmtFromServer = true;
  JMenuBar menuBar = new JMenuBar();
  JMenu fileMenu = new JMenu();

  JMenuItem fileSaveMenu = new JMenuItem();
  JToolBar jToolBar = new JToolBar();

  JButton saveButton = new JButton();
  ImageIcon saveFileImage = new ImageIcon(ImageUtils.loadImage("saveFile.jpg"));


  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel mapPanel = new JPanel();
  private GridBagLayout layout = new GridBagLayout();
  //private JTextPane mapText = new JTextPane();
  //private final static String HTML_START = "<html><body>";
  //private final static String HTML_END = "</body></html>";

  //gets the image file name as URL to save as PDF
  private String imgFileName;

  //creates the tab panes for the user to view different information for the
  //disaggregation plot
  private JTabbedPane infoTabPane = new JTabbedPane();
  //If disaggregation info needs to be scrolled
  private JScrollPane meanModeScrollPane = new JScrollPane();
  private JScrollPane metadataScrollPane = new JScrollPane();
  private JScrollPane sourceListDataScrollPane;
  private JScrollPane binnedDataScrollPane;

  //TextPane to show different disaggregation information
  private JTextPane meanModePane = new JTextPane();
  private JTextPane metadataPane = new JTextPane();
  private JTextPane sourceListDataPane;
  private JTextPane binnedDataPane;

  //Strings for getting the different disaggregation info.
  private String meanModeText,metadataText,binDataText,sourceDataText;


  /**
   * Class constructor
   * @param imageFileName : Name of the image file to be shown
   * @param mapInfo : Metadata about the Map
   * @param gmtFromServer : boolean to check if map to be generated using the Server GMT
   * @throws RuntimeException
   */
  public DisaggregationPlotViewerWindow(String imageFileName,
                                        boolean gmtFromServer,
                                        String meanModeString, String metadataString,
                                        String binDataString, String sourceDataString)
      throws RuntimeException{

    meanModeText = meanModeString;
    metadataText = metadataString;
    binDataText = binDataString;
    sourceDataText = sourceDataString;
    this.gmtFromServer = gmtFromServer;
    imgFileName = imageFileName;
    try {
      jbInit();

      //show the bin data only if it is not  null
      if(binDataString !=null || !binDataString.trim().equals("")){
        binnedDataScrollPane = new JScrollPane();
        binnedDataPane = new JTextPane();
        //adding the text pane for the bin data
        infoTabPane.addTab("Bin Data", binnedDataScrollPane);
        binnedDataScrollPane.getViewport().add(binnedDataPane, null);
        binnedDataPane.setForeground(Color.blue);
        binnedDataPane.setText(binDataText);
        binnedDataPane.setEditable(false);
      }

      //show the source list metadata only if it not null
      if(sourceDataString !=null || !sourceDataString.trim().equals("")){
        sourceListDataScrollPane = new JScrollPane();
        sourceListDataPane = new JTextPane();
        //adding the text pane for the source list data
        infoTabPane.addTab("Source List Data", sourceListDataScrollPane);
        sourceListDataScrollPane.getViewport().add(sourceListDataPane, null);
        sourceListDataPane.setForeground(Color.blue);
        sourceListDataPane.setText(sourceDataText);
        sourceListDataPane.setEditable(false);
      }

    }catch(RuntimeException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    addImageToWindow(imageFileName);
    //addPdfImageToWindow(imageFileName);
    this.setVisible(true);
  }




  protected void jbInit() throws RuntimeException {
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(W,H);
    this.setTitle(MAP_WINDOW);
    this.getContentPane().setLayout(borderLayout1);

    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        saveButton_actionPerformed(actionEvent);
      }
    });
    fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            fileSaveMenu_actionPerformed(e);
          }
    });
    fileSaveMenu.setText("Save");
    fileMenu.setText("File");
    menuBar.add(fileMenu);
    fileMenu.add(fileSaveMenu);

    setJMenuBar(menuBar);

    Dimension d = saveButton.getSize();
    jToolBar.add(saveButton);
    saveButton.setIcon(saveFileImage);
    saveButton.setToolTipText("Save Graph as image");
    saveButton.setSize(d);
    jToolBar.add(saveButton);
    jToolBar.setFloatable(false);

    this.getContentPane().add(jToolBar, BorderLayout.NORTH);

    mapSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    //adding the mean/mode and metadata info tabs to the window
    infoTabPane.addTab("Mean/Mode", meanModeScrollPane);
    infoTabPane.addTab("Metadata", metadataScrollPane);
    meanModeScrollPane.getViewport().add(meanModePane,null);
    metadataScrollPane.getViewport().add(metadataPane,null);

    //adding the metadata text to the metatada info window
    metadataPane.setContentType("text/html");
    metadataPane.setForeground(Color.blue);
    metadataPane.setText(metadataText);
    metadataPane.setEditable(false);
    metadataPane.addHyperlinkListener(this);

    //adding the meanMode text to the meanMode info window
    meanModePane.setForeground(Color.blue);
    meanModePane.setText(meanModeText);
    meanModePane.setEditable(false);

    this.getContentPane().add(mapSplitPane, BorderLayout.CENTER);
    mapSplitPane.add(mapScrollPane, JSplitPane.TOP);
    mapSplitPane.add(infoTabPane, JSplitPane.BOTTOM);
    infoTabPane.setTabPlacement(JTabbedPane.BOTTOM);
    mapPanel.setLayout(layout);
    mapScrollPane.getViewport().add(mapPanel, null);
    mapSplitPane.setDividerLocation(480);
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

  /**
   * Displays the PDF file to the user in the window
   * @param fileName String URL to the PDF filename
   */
  /*private void addPdfImageToWindow(String fileName){
    int currentPage = 1;
    PdfDecoder pdfDecoder = new PdfDecoder();

    try {
      //this opens the PDF and reads its internal details
      pdfDecoder.openPdfFileFromURL(fileName);

      //these 2 lines opens page 1 at 100% scaling
      pdfDecoder.decodePage(currentPage);
      pdfDecoder.setPageParameters(1, 1); //values scaling (1=100%). page number
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //setup our GUI display
    Container cPane = getContentPane();
    cPane.setLayout(new BorderLayout());
    JScrollPane currentScroll = new JScrollPane();
    currentScroll.setViewportView(pdfDecoder);
    cPane.add(currentScroll,BorderLayout.CENTER);
  }*/


  /**
   * Opens a file chooser and gives the user an opportunity to save the Image and Metadata
   * in PDF format.
   *
   * @throws IOException if there is an I/O error.
   */
  protected void save() throws IOException {
    JFileChooser fileChooser = new JFileChooser();
    int option = fileChooser.showSaveDialog(this);
    String fileName = null;
    if (option == JFileChooser.APPROVE_OPTION) {
      fileName = fileChooser.getSelectedFile().getAbsolutePath();
      if (!fileName.endsWith(".pdf"))
        fileName = fileName + ".pdf";
    }
    else {
      return;
    }
    saveAsPDF(fileName);
  }

  /**
   * Allows the user to save the image and metadata as PDF.
   * This also allows to preserve the color coding of the metadata.
   * @throws IOException
   */
  protected void saveAsPDF(String fileName) throws IOException {
    // step 1: creation of a document-object
    Document document = new Document();

    try {
      // step 2:
      // we create a writer that listens to the document
      // and directs a PDF-stream to a file
      PdfWriter writer = PdfWriter.getInstance(document,
                                               new FileOutputStream(fileName));
      writer.setStrictImageSequence(true);
      // step 3: we open the document
      document.open();
      // step 4: add the images to the


      Image img = Image.getInstance(new URL(imgFileName));
      img.setAlignment(Image.RIGHT);
      //img.scalePercent(95);
      document.add(img);

      String disaggregationInfoString = "Mean/Mode Metadata :\n"+meanModeText+
          "\n\n"+"Disaggregation Plot Parameters Info :\n"+
           metadataText+"\n\n"+"Disaggregation Bin Data :\n"+binDataText+"\n\n"+
          "Disaggregation Source List Info:\n"+sourceDataText;
      document.add(new Paragraph(disaggregationInfoString));
    }
    catch (DocumentException de) {
      System.err.println(de.getMessage());
    }
    catch (IOException ioe) {
      System.err.println(ioe.getMessage());
    }

    // step 5: we close the document
    document.close();

  }

  /**
   * File | Save action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void saveButton_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }

  /**
   * File | Save action performed.
   *
   * @param actionEvent ActionEvent
   */
  private void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
    try {
      save();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
  }


  /** This method implements HyperlinkListener.  It is invoked when the user
   * clicks on a hyperlink, or move the mouse onto or off of a link
   **/
  public void hyperlinkUpdate(HyperlinkEvent e) {
    HyperlinkEvent.EventType type = e.getEventType();  // what happened?
    if (type == HyperlinkEvent.EventType.ACTIVATED) {     // Click!
      try{
       org.opensha.util.BrowserLauncher.openURL(e.getURL().toString());
      }catch(Exception ex) { ex.printStackTrace(); }

      //displayPage(e.getURL());   // Follow the link; display new page
    }
  }

}


