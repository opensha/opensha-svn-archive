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
  private JTable faTable = new JTable(GlobalConstants.faData, GlobalConstants.faColumnNames);
  private JTable fvTable = new JTable(GlobalConstants.fvData, GlobalConstants.fvColumnNames);
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  GridBagLayout gridBagLayout5 = new GridBagLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  BorderLayout borderLayout4 = new BorderLayout();




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
    mainPanel.setLayout(borderLayout3);
    this.getContentPane().setLayout(borderLayout4);
    fafvPanel.setLayout(gridBagLayout5);
    fafvPanel.setBorder(fafvBorder);
    faPanel.setBorder(faBorder);
    faPanel.setLayout(borderLayout2);
    fvPanel.setBorder(fvBorder);
    fvPanel.setLayout(borderLayout1);
    notesPanel.setBorder(notesBorder);
    notesPanel.setLayout(null);
    infoText.setBackground(UIManager.getColor("ProgressBar.background"));
    infoText.setEnabled(false);

    infoText.setText(
        "Use straight-line interpolation for intermediate value of Sa and " +
        "S1.\n\nNote a: Site-specific geotechnical investigation and dynamic " +
        "site response analyses shall be performed.");

    infoText.setBounds(new Rectangle(7, 19, 516, 94));
    siteCoefficientPanel.setBorder(calcSiteCoeffBorder);
    siteCoefficientPanel.setLayout(gridBagLayout4);
    saPanel.setBorder(saBorder);
    saPanel.setLayout(gridBagLayout3);
    siteClassPanel.setBorder(siteClassBorder);
    siteClassPanel.setLayout(gridBagLayout2);
    coeffValPanel.setBorder(border11);
    coeffValPanel.setLayout(gridBagLayout1);
    discussionButton.setText("Discussion");


    discussionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        discussionButton_actionPerformed(actionEvent);
      }
    });
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
    ssLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    ssLabel.setHorizontalAlignment(SwingConstants.CENTER);
    ssLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    ssLabel.setText("Ss, g");
    ssLabel.setLabelFor(ssText);
    ssText.setEditable(false);
    s1Label.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    s1Label.setText("S1, g");
    s1Label.setLabelFor(s1Text);
    s1Text.setEditable(false);
    faText.setText("");
    faLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    faLabel.setHorizontalAlignment(SwingConstants.CENTER);
    faLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    faLabel.setText("Fa :");
    faLabel.setLabelFor(faText);
    fvLabel.setFont(new java.awt.Font("Lucida Grande", Font.BOLD, 14));
    fvLabel.setHorizontalAlignment(SwingConstants.CENTER);
    fvLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    fvLabel.setText("Fv :");
    fvLabel.setLabelFor(fvText);
    fvText.setText("");

    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(fafvPanel, BorderLayout.CENTER);
    mainPanel.add(siteCoefficientPanel, BorderLayout.EAST);
    siteCoefficientPanel.add(saPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 6, 0, 5), 0, -2));
    saPanel.add(ssLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 6, 0, 10), 49, 9));
    saPanel.add(ssText, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 15, 36, 0), 80, 16));
    saPanel.add(s1Label, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(16, 69, 0, 22), 36, 6));
    saPanel.add(s1Text, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 52, 36, 22), 80, 16));
    siteCoefficientPanel.add(siteClassPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(31, 6, 0, 5), 0, 0));
    createParameters();
    siteClassPanel.add(this.siteClassEditor, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
    siteClassPanel.add(discussionButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
    siteCoefficientPanel.add(coeffValPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(11, 6, 6, 5), -1, 0));
    coeffValPanel.add(coeffValText, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 0, 3), -179, 33));
    coeffValPanel.add(faText, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 20, 0, 66), 92, 19));
    coeffValPanel.add(faLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(24, 12, 7, 0), 35, 10));
    coeffValPanel.add(fvLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(31, 12, 10, 0), 37, 13));
    coeffValPanel.add(fvText, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(33, 20, 0, 66), 92, 19));
    coeffValPanel.add(okButton, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(41, 61, 23, 60), 89, 3));
    fafvPanel.add(faPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 3, 0, 9), 531, 241));
    faPanel.add(faTable, BorderLayout.CENTER);
    fafvPanel.add(fvPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(29, 3, 0, 9), 527, 265));
    fvPanel.add(fvTable, BorderLayout.CENTER);
    fafvPanel.add(notesPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(23, 10, 6, 9), 531, 119));
    notesPanel.add(infoText);



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
