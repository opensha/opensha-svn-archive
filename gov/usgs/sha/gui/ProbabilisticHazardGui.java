package gov.usgs.sha.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();

  private final static String HTML_START = "<html><body>";
  private final static String HTML_END = "</body></html>";

  public ProbabilisticHazardGui() {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    screenPanel.setLayout(gridBagLayout2);
    applicationInfoText.setBackground(UIManager.getColor("Panel.background"));
    applicationInfoText.setFont(new java.awt.Font("Arial", Font.PLAIN, 16));
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

    jLabel1.setFont(new java.awt.Font("Arial", Font.BOLD, 24));
    jLabel1.setForeground(Color.red);
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setText(
        "Seismic Hazard Curves, Response Parameters");
    jLabel2.setFont(new java.awt.Font("Arial", Font.BOLD, 24));
    jLabel2.setForeground(Color.red);
    jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel2.setText("and Design Parameters           ");
    applicationInfoText.setBorder(null);
    screenPanel.setMaximumSize(new Dimension(600, 500));
    screenPanel.add(jLabel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(11, 11, 45, 13), 63, 28));
    screenPanel.add(jLabel2, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(53, 97, 0, 104), 58, 31));
    screenPanel.add(applicationInfoText,
                    new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 35, 0, 41), -1863, 7));
    screenPanel.add(okButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(24, 220, 139, 0), 38, 15));
    screenPanel.add(exitButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(24, 33, 139, 106), 38, 15));
    this.getContentPane().add(screenPanel, java.awt.BorderLayout.CENTER);
    applicationInfoText.setText("This application can be used for obtaining hazard"+
                                "curves,uniform hazard response spectra "+
                                "and design parameters for the design documents.\n\n"+
                                "Maps are also included for obtaining values of "+
                                "spectral acceleration at selected periods."+
        "Data for uniform hazard response spectra and hazard curves may be obtained by use of"+
      "latitude and longitude or zip code to locate a site. Correct application of the"+
      "data obtained from the use of this program and/or maps is the responsibility of the user.");
  this.setTitle("Seismic Hazard Curves and Uniform Hazard Response Spectra");
  this.setSize(new Dimension(600, 500));
  screenPanel.setSize(new Dimension(600, 500));
  Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
  this.setLocation((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2);
  }


  public static void main(String[] args){
    ProbabilisticHazardGui app = new ProbabilisticHazardGui();
    app.show();
  }

  private void exitButton_actionPerformed(ActionEvent actionEvent) {
    System.exit(0);
  }

  private void okButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
    ProbabilisticHazardApplication app = new ProbabilisticHazardApplication();
    app.show();

  }
}

