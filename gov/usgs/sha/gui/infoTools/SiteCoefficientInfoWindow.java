package gov.usgs.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gov.usgs.util.GlobalConstants;

import java.util.ArrayList;
import org.scec.param.*;
import org.scec.param.editor.*;

/**
 * <p>Title: SiteCoefficientInfoWindow</p>
 *
 * <p>Description: This class displays the Site Coefficient window. This also
 * user to choose which Site Class to aply to the Hazard computations. </p>
 * @author Ned Field,Nitin Gupta and E.V.Leyendecker
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
  JTextPane coeffValText = new JTextPane();


  private final static String SiteClassParamName = "Set Site Class";


  private ConstrainedStringParameterEditor siteClassEditor;

  JLabel ssLabel = new JLabel();
  JTextField ssText = new JTextField();
  JLabel s1Label = new JLabel();
  JTextField s1Text = new JTextField();
  JTextField faText = new JTextField();
  JLabel faLabel = new JLabel();
  JLabel fvLabel = new JLabel();
  JTextField fvText = new JTextField();

  private SiteCoefficientInfoWindow(Frame frame, boolean _boolean) {
    super(frame, _boolean);
    try {
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      jbInit();
      pack();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }


  /**
   * Class default constructor
   */
  public SiteCoefficientInfoWindow() {
    this(new Frame(), true);
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

    infoText.setText(
        "Use straight-line interpolation for intermediate value of Sa and " +
        "S1.\n\nNote a: Site-specific geotechnical investigation and dynamic " +
        "site response analyses shall be performed.");

    infoText.setBounds(new Rectangle(7, 19, 516, 94));
    siteCoefficientPanel.setBorder(calcSiteCoeffBorder);
    siteCoefficientPanel.setBounds(new Rectangle(587, 9, 300, 770));
    siteCoefficientPanel.setLayout(null);
    saPanel.setBorder(saBorder);
    saPanel.setBounds(new Rectangle(12, 41, 277, 142));
    saPanel.setLayout(null);
    siteClassPanel.setBorder(siteClassBorder);
    siteClassPanel.setBounds(new Rectangle(16, 214, 270, 209));
    siteClassPanel.setLayout(null);
    coeffValPanel.setBorder(border11);
    coeffValPanel.setBounds(new Rectangle(17, 434, 271, 324));
    coeffValPanel.setLayout(null);
    discussionButton.setBounds(new Rectangle(56, 165, 148, 29));
    discussionButton.setText("Discussion");


    discussionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        discussionButton_actionPerformed(actionEvent);
      }
    });
    okButton.setBounds(new Rectangle(67, 267, 138, 28));
    okButton.setText("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        okButton_actionPerformed(actionEvent);
      }
    });
    coeffValText.setBackground(UIManager.getColor("ProgressBar.background"));
    coeffValText.setForeground(UIManager.getColor(
        "FormattedTextField.selectionBackground"));
    coeffValText.setEditable(false);
    coeffValText.setText(
        "Interpolated soil factors for the conditions shown. Values may also " +
        "be entered manually.");
    coeffValText.setBounds(new Rectangle(9, 24, 253, 69));
    ssLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    ssLabel.setHorizontalAlignment(SwingConstants.CENTER);
    ssLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    ssLabel.setText("Ss, g");
    ssLabel.setBounds(new Rectangle(12, 37, 85, 28));
    ssLabel.setLabelFor(ssText);
    ssText.setEditable(false);
    ssText.setBounds(new Rectangle(21, 63, 86, 37));
    s1Label.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    s1Label.setText("S1, g");
    s1Label.setBounds(new Rectangle(176, 37, 73, 25));
    s1Label.setLabelFor(s1Text);
    s1Text.setEditable(false);
    s1Text.setBounds(new Rectangle(159, 63, 86, 37));
    faText.setText("");
    faText.setBounds(new Rectangle(101, 113, 98, 40));
    faLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    faLabel.setHorizontalAlignment(SwingConstants.CENTER);
    faLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    faLabel.setText("Fa :");
    faLabel.setLabelFor(faText);
    faLabel.setBounds(new Rectangle(18, 117, 63, 29));
    fvLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    fvLabel.setHorizontalAlignment(SwingConstants.CENTER);
    fvLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    fvLabel.setText("Fv :");
    fvLabel.setLabelFor(fvText);
    fvLabel.setBounds(new Rectangle(18, 184, 64, 32));
    fvText.setText("");

    fvText.setBounds(new Rectangle(101, 186, 98, 40));
    this.getContentPane().add(mainPanel, null);
    mainPanel.add(fafvPanel);
    mainPanel.add(siteCoefficientPanel);
    siteCoefficientPanel.add(saPanel);
    saPanel.add(ssLabel);
    saPanel.add(ssText);
    saPanel.add(s1Label);
    saPanel.add(s1Text);
    siteCoefficientPanel.add(siteClassPanel);
    siteClassPanel.add(discussionButton);
    siteCoefficientPanel.add(coeffValPanel);
    coeffValPanel.add(coeffValText);
    coeffValPanel.add(faText);
    coeffValPanel.add(faLabel);
    coeffValPanel.add(fvLabel);
    coeffValPanel.add(fvText);
    coeffValPanel.add(okButton);
    fafvPanel.add(faPanel);
    fafvPanel.add(fvPanel);
    fafvPanel.add(notesPanel);
    notesPanel.add(infoText);
    fafvPanel.setBounds(new Rectangle(9, 10, 567, 769));
    mainPanel.setBounds(new Rectangle(10, 10, 890, 784));

    createParameters();

  }

  private void createParameters(){

    ArrayList  supportedSiteClasses = GlobalConstants.getSupportedSiteClasses();
    StringParameter siteClassParam = new StringParameter(this.SiteClassParamName,
        supportedSiteClasses,(String)supportedSiteClasses.get(0));
    siteClassEditor = new ConstrainedStringParameterEditor(siteClassParam);
  }



  public void discussionButton_actionPerformed(ActionEvent actionEvent) {

  }

  public void okButton_actionPerformed(ActionEvent actionEvent) {

  }
}
