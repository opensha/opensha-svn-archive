package gov.usgs.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SiteCoefficientInfoWindow
    extends JDialog {
  JPanel mainPanel = new JPanel();
  JPanel fafvPanel = new JPanel();
  JPanel faPanel = new JPanel();
  Border border3 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder fafvBorder = new TitledBorder(border3,
      "Soil Factors as Function of Site Class and Spectral Accelaration");

  TitledBorder faBorder = new TitledBorder(border3,
      "Values of Fa as a function of Site Class and 0.2 sec MCE Spectral Acceleration");
  JPanel fvPanel = new JPanel();

  Border fvBorder = new TitledBorder(border3,
      "Values of Fv asa Function of Site Class and 1.0 sec MCE Spectral Acceleration");
  JPanel notesPanel = new JPanel();

  Border notesBorder = new TitledBorder(border3, "Notes:");
  JTextPane infoText = new JTextPane();
  JPanel siteCoefficientPanel = new JPanel();

  Border calcSiteCoeffBorder = new TitledBorder(border3, "Calculate Site Coefficient");
  JPanel saPanel = new JPanel();

  Border saBorder = new TitledBorder(border3, "Spectral Accelerations");
  JPanel siteClassPanel = new JPanel();

  Border siteClassBorder = new TitledBorder(border3, "Site Class");
  JPanel coeffValPanel = new JPanel();

  Border border11 = new TitledBorder(border3, "Site Coefficients");
  JButton discussionButton = new JButton();
  JButton okButton = new JButton();
  public SiteCoefficientInfoWindow(Frame frame, String string, boolean _boolean) {
    super(frame, string, _boolean);
    try {
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jbInit();
      pack();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public SiteCoefficientInfoWindow() {
    this(new Frame(), "SiteCoefficientInfoWindow", false);
  }

  private void jbInit() throws Exception {
    mainPanel.setLayout(null);
    this.getContentPane().setLayout(null);
    fafvPanel.setLayout(null);
    fafvPanel.setBorder(fafvBorder);
    faPanel.setBorder(faBorder);
    faPanel.setBounds(new Rectangle(9, 25, 543, 268));
    faPanel.setLayout(null);
    fvPanel.setBorder(fvBorder);
    fvPanel.setBounds(new Rectangle(12, 322, 539, 292));
    fvPanel.setLayout(null);
    notesPanel.setBorder(notesBorder);
    notesPanel.setBounds(new Rectangle(16, 637, 532, 120));
    notesPanel.setLayout(null);
    infoText.setBackground(UIManager.getColor("ProgressBar.background"));
    infoText.setEnabled(false);

    infoText.setContentType("html/text");
    infoText.setText(
        "Use straight-line interpolation for intermediate value of Sa and " +
        "S1. Note a: Site-specific geotechnical investigation and dynamic " +
        "site response analyses shall be performed.");

    infoText.setBounds(new Rectangle(7, 19, 516, 94));
    siteCoefficientPanel.setBorder(calcSiteCoeffBorder);
    siteCoefficientPanel.setBounds(new Rectangle(587, 9, 300, 770));
    siteCoefficientPanel.setLayout(null);
    saPanel.setBorder(saBorder);
    saPanel.setBounds(new Rectangle(12, 41, 277, 209));
    saPanel.setLayout(null);
    siteClassPanel.setBorder(siteClassBorder);
    siteClassPanel.setBounds(new Rectangle(16, 277, 270, 201));
    siteClassPanel.setLayout(null);
    coeffValPanel.setBorder(border11);
    coeffValPanel.setBounds(new Rectangle(17, 501, 271, 257));
    coeffValPanel.setLayout(null);
    discussionButton.setBounds(new Rectangle(57, 153, 148, 29));
    discussionButton.setText("Discussion");
    okButton.setBounds(new Rectangle(64, 214, 138, 28));
    okButton.setText("OK");
    this.getContentPane().add(mainPanel, null);
    mainPanel.add(fafvPanel);
    mainPanel.add(siteCoefficientPanel);
    siteCoefficientPanel.add(saPanel);
    siteCoefficientPanel.add(siteClassPanel);
    siteClassPanel.add(discussionButton);
    siteCoefficientPanel.add(coeffValPanel);
    coeffValPanel.add(okButton);
    fafvPanel.add(faPanel);
    fafvPanel.add(fvPanel);
    fafvPanel.add(notesPanel);
    notesPanel.add(infoText);
    fafvPanel.setBounds(new Rectangle(9, 10, 567, 769));
    mainPanel.setBounds(new Rectangle(10, 10, 890, 784));
  }
}
