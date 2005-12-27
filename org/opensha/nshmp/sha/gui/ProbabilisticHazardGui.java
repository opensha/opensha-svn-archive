package org.opensha.nshmp.sha.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.opensha.util.ImageUtils;
import org.opensha.nshmp.util.GlobalConstants;

/**
 * <p>Title: ProbabilisticHazardGui</p>
 *
 * <p>Description: This window is launched whenever the user starts the
 * application for the first time. This window provides user with brief
 * explaination of what user can expect from this application.</p>
 *
 * @author Ned Field, Nitin Gupta, E.V.Leyendecker
 * @version 1.0
 */
public class ProbabilisticHazardGui
    extends JFrame {

  JPanel screenPanel = new JPanel();
  JTextPane applicationInfoText = new JTextPane();
  JButton exitButton = new JButton();
  Border border1 = BorderFactory.createEmptyBorder();
  Border border2 = new TitledBorder(border1, "custom");
  TitledBorder titledBorder1 = new TitledBorder("");
  JButton okButton = new JButton();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();



  private JLabel imgLabel = new JLabel(GlobalConstants.USGS_LOGO_ICON);
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  public ProbabilisticHazardGui() {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  //static initializer for setting look & feel
  static {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
    }
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    screenPanel.setLayout(gridBagLayout2);
    applicationInfoText.setBackground(UIManager.getColor("Panel.background"));
    applicationInfoText.setFont(new java.awt.Font("Arial", 0, 13));
    applicationInfoText.setEditable(false);
    exitButton.setText("Exit");
    exitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exitButton_actionPerformed(e);
      }
    });
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });

    jLabel1.setFont(new java.awt.Font("Dialog", 1, 20));
    jLabel1.setForeground(Color.red);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setText(
        "Seismic Hazard Curves, Response Parameters");
    jLabel2.setFont(new java.awt.Font("Dialog", 1, 20));
    jLabel2.setForeground(Color.red);
    jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel2.setText("and Design Parameters           ");
    applicationInfoText.setBorder(null);
    screenPanel.setMaximumSize(new Dimension(600, 500));
    imgLabel.setMaximumSize(new Dimension(200, 75));
    imgLabel.setMinimumSize(new Dimension(200, 75));
    imgLabel.setPreferredSize(new Dimension(200, 75));
    screenPanel.add(applicationInfoText,  new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 12, 0, 6), 0, 0));
    screenPanel.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(13, 38, 0, 25), 0,0));
    screenPanel.add(jLabel2,  new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 123, 0, 101), 0, 0));
    screenPanel.add(imgLabel,  new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(18, 166, 2, 41), 0, 0));
    screenPanel.add(okButton,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(23, 327, 0, 0), 0, 0));
    screenPanel.add(exitButton,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(23, 17, 0, 72), 0, 0));
    this.getContentPane().add(screenPanel, BorderLayout.CENTER);

    applicationInfoText.setText(
        "This application allows user for obtaining hazard " +
        "curves, uniform hazard response spectra " +
        "and design parameters for the design parameters for "+
        "sites in 50 states of the United States, Puerto Rico and the "+
        "U.S. Virgin Islands. Additionally, design "+
        "parameters are available for Guam and American Samoa. "+
        "Ground motion maps are also included in PDF format.\n\n" +
        "Maps are also included for obtaining values of " +
        "spectral acceleration at selected periods." +
        "Data for uniform hazard response spectra and hazard curves may be obtained by use of " +
        "latitude and longitude or zip code to locate a site.\n\n "+
        "Correct application of the " +
        "data obtained from the use of this program and/or maps is the responsibility of the user. "+
          "This software is not a substitute for technical knowledge of seismic design and/or analysis.");
    this.setTitle("Seismic Hazard Curves and Uniform Hazard Response Spectra");
    this.setSize(new Dimension(576, 378));
    //screenPanel.setSize(new Dimension(600, 500));
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation( (d.width - this.getSize().width) / 2,
                     (d.height - this.getSize().height) / 2);
   setIconImage(GlobalConstants.USGS_LOGO_ICON.getImage());
  }

  public static void main(String[] args) {
    ProbabilisticHazardGui app = new ProbabilisticHazardGui();
    app.setVisible(true);
  }

  private void exitButton_actionPerformed(ActionEvent actionEvent) {
    System.exit(0);
  }

  private void okButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
    ProbabilisticHazardApplication app = new ProbabilisticHazardApplication();
    app.setVisible(true);

  }

  //static initializer for setting look & feel
  static {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
    }
  }

}
