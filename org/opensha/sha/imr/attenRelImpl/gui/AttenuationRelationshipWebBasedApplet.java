package org.opensha.sha.imr.attenRelImpl.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.opensha.sha.gui.infoTools.*;
import org.opensha.util.*;

/**
 * <p>Title: AttenuationRelationshipWebBasedApplet</p>
 *
 * <p>Description: This class extends AttenuationRelationshipApplet so that its
 * GUI can be customized for giving it the look and feel so that it looks good
 * when launched in the Web Browser.</p>
 * @author : Nitin Gupta
 * @since Feb 17 2005,
 * @version 1.0
 */
public class AttenuationRelationshipWebBasedApplet
    extends AttenuationRelationshipApplet {

  /**
   *  Component initialization
   *
   * @exception  Exception  Description of the Exception
   */
  protected void jbInit() throws Exception {

    String S = C + ": jbInit(): ";

    border1 = BorderFactory.createLineBorder(new Color(80, 80, 133), 2);
    this.setFont(new java.awt.Font("Dialog", 0, 10));
    this.setSize(new Dimension(900, 690));
    this.getContentPane().setLayout(GBL);
    outerPanel.setLayout(GBL);
    mainPanel.setBorder(border1);
    mainPanel.setLayout(GBL);
    titlePanel.setBorder(bottomBorder);
    titlePanel.setMinimumSize(new Dimension(40, 40));
    titlePanel.setPreferredSize(new Dimension(40, 40));
    titlePanel.setLayout(GBL);
    //creating the Object the GraphPaenl class
    graphPanel = new GraphPanel(this);

    imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        imgLabel_mouseClicked(e);
      }
    });
    plotPanel.setLayout(GBL);
    innerPlotPanel.setLayout(GBL);
    innerPlotPanel.setBorder(null);
    controlPanel.setLayout(GBL);
    controlPanel.setBorder(BorderFactory.createEtchedBorder(1));
    outerControlPanel.setLayout(GBL);

    clearButton.setText("Clear Plot");

    clearButton.addActionListener(
        new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    }
    );

    addButton.setText("Add Curve");

    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });

    buttonPanel.setBorder(topBorder);
    buttonPanel.setLayout(flowLayout1);

    parametersSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    parametersSplitPane.setBorder(null);
    parametersSplitPane.setDividerSize(5);

    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setBorder(null);
    mainSplitPane.setDividerSize(5);

    attenRelLabel.setForeground(darkBlue);
    attenRelLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 13));
    attenRelLabel.setText("Choose Model:    ");

    attenRelComboBox.setFont(new java.awt.Font("Dialog", Font.BOLD, 16));

    attenRelComboBox.addItemListener(this);

    plotColorCheckBox.setText("Black Background");

    plotColorCheckBox.addItemListener(this);

    //setting the layout for the Parameters panels
    parametersPanel.setLayout(GBL);
    controlPanel.setLayout(GBL);
    sheetPanel.setLayout(GBL);
    inputPanel.setLayout(GBL);

    //loading the OpenSHA Logo
    imgLabel.setText("");
    imgLabel.setIcon(new ImageIcon(ImageUtils.loadImage(this.POWERED_BY_IMAGE)));
    xyDatasetButton.setText("Add Data Points");
    xyDatasetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        xyDatasetButton_actionPerformed(e);
      }
    });
    peelOffButton.setText("Peel Off");
    peelOffButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        peelOffButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(outerPanel,
                              new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, emptyInsets, 0, 0));

    outerPanel.add(mainPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 5, 0, 5), 0, 0));

    titlePanel.add(this.attenRelLabel,
                   new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
                                          , GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          emptyInsets, 0, 0));

    titlePanel.add(this.attenRelComboBox,
                   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                          , GridBagConstraints.CENTER,
                                          GridBagConstraints.HORIZONTAL,
                                          emptyInsets, 0, 0));

    mainPanel.add(mainSplitPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 4, 4, 4), 0, 0));

    mainPanel.add(buttonPanel,
                  new GridBagConstraints(0, 2, GridBagConstraints.REMAINDER,
                                         GridBagConstraints.REMAINDER, 1.0, 0.0
                                         , GridBagConstraints.CENTER,
                                         GridBagConstraints.HORIZONTAL,
                                         new Insets(1, 1, 1, 1), 0, 0));

    controlPanel.add(parametersPanel,
                     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.BOTH,
                                            emptyInsets, 0, 0));

    outerControlPanel.add(controlPanel,
                          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 5, 0, 0), 0, 0));

    parametersPanel.add(parametersSplitPane,
                        new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                               , GridBagConstraints.CENTER,
                                               GridBagConstraints.BOTH,
                                               emptyInsets, 0, 0));

    plotPanel.add(titlePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(4, 4, 2, 4), 0, 0));

    plotPanel.add(innerPlotPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0,
        0));

    //object for the ButtonControl Panel
    buttonControlPanel = new ButtonControlPanel(this);
    buttonPanel.add(addButton, 0);
    buttonPanel.add(clearButton, 1);
    buttonPanel.add(peelOffButton, 2);
    buttonPanel.add(xyDatasetButton, 3);
    buttonPanel.add(buttonControlPanel, 4);
    buttonPanel.add(plotColorCheckBox, 5);

    outerPanel.add(imgLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(12, 0, 0, 0), 0, 0));

    parametersSplitPane.setBottomComponent(sheetPanel);
    parametersSplitPane.setTopComponent(inputPanel);
    parametersSplitPane.setDividerLocation(220);

    parametersSplitPane.setOneTouchExpandable(false);

    mainSplitPane.setBottomComponent(outerControlPanel);
    mainSplitPane.setTopComponent(plotPanel);
    mainSplitPane.setDividerLocation(600);
    mainSplitPane.setOneTouchExpandable(false);

    // Big function here, sets all the AttenuationRelationship stuff and puts in sheetsPanel and
    // inputsPanel
    updateChoosenAttenuationRelationship();
  }

  /**
   *  Main method
   *
   * @param  args  The command line arguments
   */
  public static void main( String[] args ) {

      AttenuationRelationshipWebBasedApplet applet = new AttenuationRelationshipWebBasedApplet();

      Color c = new Color( .9f, .9f, 1.0f, 1f );
      Font f = new Font( "Dialog", Font.PLAIN, 11 );

      UIManager.put( "ScrollBar.width", new Integer( 12 ) );
      UIManager.put( "ScrollPane.width", new Integer( 12 ) );

      UIManager.put( "PopupMenu.font", f );
      UIManager.put( "Menu.font", f );
      UIManager.put( "MenuItem.font", f );

      UIManager.put( "ScrollBar.border", BorderFactory.createEtchedBorder( 1 ) );

      UIManager.put( "PopupMenu.background", c );

      //UIManager.put("PopupMenu.selectionBackground", c );
      //UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.red, 1 ) );

      UIManager.put( "Menu.background", c );
      //UIManager.put("Menu.selectionBackground", c );

      UIManager.put( "MenuItem.background", c );
      UIManager.put( "MenuItem.disabledBackground", c );
      //UIManager.put("MenuItem.selectionBackground", c );

      // UIManager.put("MenuItem.borderPainted", new Boolean(false) );
      UIManager.put( "MenuItem.margin", new Insets( 0, 0, 0, 0 ) );

      UIManager.put( "ComboBox.background", c );
      //UIManager.put("ComboBox.selectionBackground", new Color(220, 230, 170));


      applet.isStandalone = true;
      JFrame frame = new JFrame();
      //EXIT_ON_CLOSE == 3
      frame.setDefaultCloseOperation( 3 );

      frame.getContentPane().add( applet, BorderLayout.CENTER );

      applet.init();
      applet.start();
      applet.setFrame( frame );

      //frame.setTitle( applet.getAppletInfo() + ":  [" + applet.getCurrentAttenuationRelationshipName() + ']' );
      frame.setTitle( applet.getAppletInfo() + " (Version:"+applet.version+")");
      frame.setSize( W, H );
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      frame.setLocation( ( d.width - frame.getSize().width ) / 2, ( d.height - frame.getSize().height ) / 2 );
      frame.setVisible( true );
  }


}
