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
import org.scec.param.event.*;
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
    extends JDialog implements org.scec.param.event.ParameterChangeListener {
  private JPanel mainPanel = new JPanel();
  private JPanel fafvPanel = new JPanel();
  private JPanel faPanel = new JPanel();
  private Border border3 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  private TitledBorder fafvBorder = new TitledBorder(border3,
      "Soil Factors as Function of Site Class and Spectral Accelaration");

  private TitledBorder faBorder = new TitledBorder(border3,
      "Values of Fa as a function of Site Class and 0.2 sec MCE Spectral Acceleration");
  private JPanel fvPanel = new JPanel();

  private Border fvBorder = new TitledBorder(border3,
      "Values of Fv asa Function of Site Class and 1.0 sec MCE Spectral Acceleration");
  private JPanel notesPanel = new JPanel();

  private Border notesBorder = new TitledBorder(border3, "Notes:");
  private JTextPane infoText = new JTextPane();
  private JPanel siteCoefficientPanel = new JPanel();

  private Border calcSiteCoeffBorder = new TitledBorder(border3, "Calculate Site Coefficient");
  private JPanel saPanel = new JPanel();

  private Border saBorder = new TitledBorder(border3, "Spectral Accelerations");
  private JPanel siteClassPanel = new JPanel();

  private Border siteClassBorder = new TitledBorder(border3, "Site Class");
  private JPanel coeffValPanel = new JPanel();

  private Border border11 = new TitledBorder(border3, "Site Coefficients");
  private JButton discussionButton = new JButton();
  private JButton okButton = new JButton();
  private JTextPane coeffValText = new JTextPane();


  private final static String SiteClassParamName = "Set Site Class";


  private ConstrainedStringParameterEditor siteClassEditor;

  private JLabel ssLabel = new JLabel();
  private JTextField ssText = new JTextField();
  private JLabel s1Label = new JLabel();
  private JTextField s1Text = new JTextField();
  private JTextField faText = new JTextField();
  private JLabel fvLabel = new JLabel();
  private JTextField fvText = new JTextField();
  private JTable faTable = new JTable(GlobalConstants.faData, GlobalConstants.faColumnNames);
  private JTable fvTable = new JTable(GlobalConstants.fvData, GlobalConstants.fvColumnNames);
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JLabel faLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private BorderLayout borderLayout3 = new BorderLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private  GridBagLayout gridBagLayout6 = new GridBagLayout();
  private BorderLayout borderLayout4 = new BorderLayout();


  private double ss=0.72, s1=0.72;
  private String siteClass;
  private StringParameter siteClassParam;

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
    this(new JFrame(), true);
  }


  public SiteCoefficientInfoWindow(double ss, double s1, String siteClass) {
    this(new JFrame(), true);
    setSS_S1(ss,s1);
    this.siteClass = siteClass;
    siteClassParam.setValue(siteClass);
  }

  private void setSS_S1(double ss, double s1) {
    this.ss = ss;
    this.s1 = s1;
    s1Text.setEditable(true);
    s1Text.setText(""+s1);
    s1Text.setEditable(false);
    ssText.setEditable(true);
    ssText.setText(""+ss);
    ssText.setEditable(false);
  }


  private void jbInit() throws Exception {
    mainPanel.setLayout(gridBagLayout6);
    this.getContentPane().setLayout(borderLayout4);
    fafvPanel.setLayout(gridBagLayout5);
    fafvPanel.setBorder(fafvBorder);
    faPanel.setBorder(faBorder);
    faPanel.setLayout(borderLayout3);
    fvPanel.setBorder(fvBorder);
    fvPanel.setLayout(borderLayout2);
    notesPanel.setBorder(notesBorder);
    notesPanel.setLayout(borderLayout1);
    infoText.setBackground(UIManager.getColor("ProgressBar.background"));
    infoText.setEnabled(false);

    infoText.setText("Use straight-line interpolation for intermediate value of Sa and " +
    "S1.\nNote a: Site-specific geotechnical investigation and dynamic " +
    "site response analyses shall be performed.");

    siteCoefficientPanel.setBorder(calcSiteCoeffBorder);
    siteCoefficientPanel.setLayout(gridBagLayout4);
    saPanel.setBorder(saBorder);
    saPanel.setLayout(gridBagLayout3);
    siteClassPanel.setBorder(siteClassBorder);
    siteClassPanel.setLayout(gridBagLayout1);
    coeffValPanel.setBorder(border11);
    coeffValPanel.setLayout(gridBagLayout2);
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
    ssLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    ssLabel.setHorizontalAlignment(SwingConstants.CENTER);
    ssLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    ssLabel.setText("Ss, g");
    ssLabel.setLabelFor(ssText);
    ssText.setEditable(false);
    s1Label.setFont(new java.awt.Font("Dialog", 1, 12));
    s1Label.setText("S1, g");
    s1Label.setLabelFor(s1Text);
    s1Text.setEditable(false);
    faText.setText("");
    fvLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    fvLabel.setHorizontalAlignment(SwingConstants.LEFT);
    fvLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    fvLabel.setText("Fv :");
    fvLabel.setLabelFor(fvText);
    fvText.setText("");

    faLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    faLabel.setHorizontalAlignment(SwingConstants.LEFT);
    faLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    faLabel.setText("Fa :");
    faLabel.setLabelFor(faText);
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    saPanel.add(ssLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-9, 4, 0, 9), 32, 5));
    saPanel.add(s1Label,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(-9, 38, 0, 15), 14, 7));
    saPanel.add(ssText,  new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 4, 26, 0), 68, 1));
    saPanel.add(s1Text,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 16, 26, 5), 68, 1));
    siteCoefficientPanel.add(coeffValPanel,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25, 10, 11, 5), -2, -2));
    mainPanel.add(fafvPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 1, 14, 0), 0, 2));
    createParameters();
    siteClassPanel.add(this.siteClassEditor, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
    coeffValPanel.add(coeffValText,  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(-7, 4, 0, 0), -270, 3));
    coeffValPanel.add(okButton,  new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(9, 29, 8, 34), 54, -2));
    coeffValPanel.add(fvText,  new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 0, 36), 85, -1));
    coeffValPanel.add(fvLabel,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 4, 0, 0), 17, 6));
    coeffValPanel.add(faText,  new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 37), 86, -1));
    coeffValPanel.add(faLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 13, 7));
    siteCoefficientPanel.add(siteClassPanel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(33, 3, 0, 5), 4, 0));
    siteClassPanel.add(discussionButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
    siteCoefficientPanel.add(saPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(-5, 3, 0, 5), 0, -1));
    fafvPanel.add(faPanel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(-6, 3, 0, 3), 2, -251));
    faPanel.add(jScrollPane1, BorderLayout.CENTER);
    fafvPanel.add(fvPanel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 0, 3), -3, -239));
    fvPanel.add(jScrollPane2, BorderLayout.CENTER);
    fafvPanel.add(notesPanel,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 3, 2, 3), -56, 7));
    notesPanel.add(infoText, BorderLayout.CENTER);
    mainPanel.add(siteCoefficientPanel,  new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 14, 11), 0, 0));
    jScrollPane2.getViewport().add(fvTable, null);
    jScrollPane1.getViewport().add(faTable, null);



  }

  private void createParameters(){

    ArrayList  supportedSiteClasses = GlobalConstants.getSupportedSiteClasses();
    siteClassParam = new StringParameter(this.SiteClassParamName,
        supportedSiteClasses,(String)supportedSiteClasses.get(0));
    siteClassParam.addParameterChangeListener(this);
    siteClassEditor = new ConstrainedStringParameterEditor(siteClassParam);
  }

  /**
  *  Any time a control paramater or independent paramater is changed
  *  by the user in a GUI this function is called, and a paramater change
  *  event is passed in. This function then determines what to do with the
  *  information ie. show some paramaters, set some as invisible,
  *  basically control the paramater lists.
  *
  * @param  event
  */
 public void parameterChange(ParameterChangeEvent event){
   String name1 = event.getParameterName();
   if(name1.equalsIgnoreCase(SiteClassParamName))  {
     String value = (String)event.getNewValue();
     setFa(value);
   }
 }

 private void setFa(String siteClassVal) {
   char siteClass = siteClassVal.charAt(siteClassVal.length()-1);
   int rowNumber;

   //CALCULATE Fa

   // get the row number
   rowNumber = -1;
   for(int i=0; i <GlobalConstants.faData.length; ++i) {
     char val = ( (String) GlobalConstants.faData[i][0]).charAt(0);
     if (val == siteClass) {
       rowNumber = i;
       break;
     }
   }

   //get the column number
   int columnNumber = -1;
   for(int j=0; j<GlobalConstants.faColumnNames.length; ++j) {
     String columnName = GlobalConstants.faColumnNames[j];
     int index=-1;
     // find the number in the column names
     for(int k=0;k<columnName.length();++k)
       if((columnName.charAt(k)>='0' && columnName.charAt(k)<='9') ||
          columnName.charAt(k)=='.') {
         index = k;
         break;
       }
     if(index>=columnName.length() || index<0) continue;
     double colVal = Double.parseDouble(columnName.substring(index));
     // found the columnNumber
     if(this.ss<=colVal) {
       columnNumber = j;
       break;
     }
   }
   System.out.println("Value="+GlobalConstants.faData[rowNumber][columnNumber]);
 }

  public void discussionButton_actionPerformed(ActionEvent actionEvent) {

  }

  public void okButton_actionPerformed(ActionEvent actionEvent) {

  }
}
