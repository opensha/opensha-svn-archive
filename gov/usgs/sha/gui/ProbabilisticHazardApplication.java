package gov.usgs.sha.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title:ProbabilisticHazardApplication </p>
 *
 * <p>Description: This application allows users to obtain hazard curves,
 * uniform hazard response spectra and design parameters for the design
 * documents listed below:
 * <ul>
 * <li> Probabilistic Hazard curves.
 * <li> Probabilistic Uniform Hazard Response Spectra.
 * <li> NEHRP Recommended Provisions for Seismic Regulations for New Buildings and Other Structure.
 * <li> FEMA 273,MCE Guidelines for the Seismic Rehabilitation of Buildings.
 * <li> FEMA 356,Prestandard and Commentary for the Seismic Rehabilitation of Buildings.
 * <li> International Building Code
 * <li> International Residential Code
 * <li> International Existing Building Code
 * <li> NFPA 5000 Building construction and safety code
 * <li> ASCE 7 standard , Minimum Design Loads for Building and other structures.
 * </ul>
 * </p>
 * @author  Ned Field, Nitin Gupta and E.V Leyendecker
 * @version 1.0
 */
public class ProbabilisticHazardApplication
    extends JFrame {
  JPanel contentPane;

  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenuItem jMenuFilePrint = new JMenuItem();
  JMenuItem jMenuFileSave = new JMenuItem();

  // height and width of the applet
  private final static int W = 1100;
  private final static int H = 750;
  JPanel jPanel1 = new JPanel();

  public ProbabilisticHazardApplication() {
    try {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Component initialization.
   *
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    contentPane = (JPanel) getContentPane();
    contentPane.setLayout(null);
    setSize(new Dimension(1100, 750));
    setTitle("Seismic Hazard Curves and Uniform Hazard Response Spectra");
    jMenuFile.setText("File");
    jMenuFileExit.setText("Exit");
    jMenuFileSave.setText("Save");
    jMenuFilePrint.setText("Print");
    jMenuFileExit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jPanel1.setBounds(new Rectangle(11, 7, 1075, 730));

    jMenuBar1.add(jMenuFile);
    jMenuFile.add(jMenuFileSave);
    jMenuFile.add(jMenuFilePrint);
    jMenuFile.add(jMenuFileExit);
    contentPane.add(jPanel1);

    setJMenuBar(jMenuBar1);
  }

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  void jMenuFileExit_actionPerformed(ActionEvent actionEvent) {
    System.exit(0);
  }
}
