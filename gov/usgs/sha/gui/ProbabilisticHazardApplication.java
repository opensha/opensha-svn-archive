package gov.usgs.sha.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import gov.usgs.util.GlobalConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import gov.usgs.sha.gui.beans.AnalysisOptionsGuiBeanAPI;

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
  JSplitPane mainSplitPane = new JSplitPane();
  JLabel analysisOptionLabel = new JLabel();
  JComboBox analysisOptionSelectionCombo = new JComboBox();
  JSplitPane dataSplitPane = new JSplitPane();
  JScrollPane dataScrollPane = new JScrollPane();
  JPanel dataPanel = new JPanel();
  JPanel parametersPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JTextArea dataTextArea = new JTextArea();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JButton clearDataButton = new JButton();
  JButton viewMapsButton = new JButton();
  FlowLayout flowLayout1 = new FlowLayout();
  private JButton ExplainButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JDialog frame;
  private JTextPane explainationText;

  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder outputBorder = new TitledBorder(border9, "Output for All Calculations");

  //instance of the gui bean for the selected analysis option
  private AnalysisOptionsGuiBeanAPI guiBeanAPI;

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
    contentPane.setLayout(borderLayout1);
    setSize(new Dimension(W, H));
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
    jPanel1.setLayout(gridBagLayout1);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setForeground(Color.black);
    analysisOptionLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 20));
    analysisOptionLabel.setForeground(Color.red);
    analysisOptionLabel.setText("Select Analysis Option:");
    analysisOptionSelectionCombo.setFont(new java.awt.Font("Arial", Font.BOLD,
        20));
    analysisOptionSelectionCombo.setForeground(new Color(200, 200, 230));

    //adding the supported Analysis option to the combo selection
    ArrayList supportAnalysisOptions = GlobalConstants.getSupportedAnalysisOptions();
    int size = supportAnalysisOptions.size();

    for(int i=0;i<size;++i)
      analysisOptionSelectionCombo.addItem(supportAnalysisOptions.get(i));


    analysisOptionSelectionCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        analysisOptionSelectionCombo_itemStateChanged(itemEvent);
      }
    });

    dataPanel.setLayout(borderLayout3);
    dataSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    dataSplitPane.setBorder(outputBorder);
    outputBorder.setTitleColor(Color.RED);
    dataTextArea.setText("");
    dataScrollPane.setBounds(new Rectangle(10, 10, 484, 548));
    dataPanel.setBounds(new Rectangle(0, 0, 484, 548));
    parametersPanel.setLayout(gridBagLayout2);
    buttonPanel.setLayout(flowLayout1);
    clearDataButton.setText("Clear Data");
    viewMapsButton.setText("View Maps");
    ExplainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        ExplainButton_actionPerformed(actionEvent);
      }
    });
    jMenuBar1.add(jMenuFile);
    jMenuFile.add(jMenuFileSave);
    jMenuFile.add(jMenuFilePrint);
    jMenuFile.add(jMenuFileExit);
    mainSplitPane.add(dataSplitPane, JSplitPane.BOTTOM);
    mainSplitPane.add(parametersPanel, JSplitPane.TOP);
    dataSplitPane.add(dataScrollPane, JSplitPane.LEFT);
    dataSplitPane.add(buttonPanel, JSplitPane.RIGHT);
    contentPane.add(jPanel1, java.awt.BorderLayout.CENTER);
    ExplainButton.setText("Explain");

    dataScrollPane.add(dataPanel, null);
    dataPanel.add(dataTextArea, java.awt.BorderLayout.CENTER);
    buttonPanel.add(clearDataButton, null);
    buttonPanel.add(viewMapsButton, null);
    jPanel1.add(analysisOptionLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.WEST,
                                       GridBagConstraints.NONE,
                                       new Insets(14, 8, 0, 0), 21, 16));
    jPanel1.add(analysisOptionSelectionCombo,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                       , GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(14, 0, 0, 0), 385, 21));
    jPanel1.add(ExplainButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(14, 11, 0, 303), 44, 11));
    jPanel1.add(mainSplitPane, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(10, 8, 5, 10), 777, 597));
    setJMenuBar(jMenuBar1);}

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  void jMenuFileExit_actionPerformed(ActionEvent actionEvent) {
    System.exit(0);
  }

  private void analysisOptionSelectionCombo_itemStateChanged(ItemEvent
      itemEvent) {
    showAnalysisOptionInWindow();
    createGuiBeanInstance();
  }

  private void createGuiBeanInstance(){

  }

  private void showAnalysisOptionInWindow(){
    String frameTitle = (String) analysisOptionSelectionCombo.getSelectedItem();
    if (frame != null) {
      frame.setTitle(frameTitle);
      setExplainationForSelectedAnalysisOption(frameTitle);
    }

  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void ExplainButton_actionPerformed(ActionEvent actionEvent) {
    if(frame == null)
      showSelectedAnalysisExplaination();
    frame.pack();
    frame.show();
  }

  /**
   *
   *
   */
  private void showSelectedAnalysisExplaination() {

    this.explainationText = new JTextPane();

    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
         this is required for the passing in the JDialog to keep the focus on the adjustable params
         frame*/
    while (! (parent instanceof JFrame) && parent != null)
      parent = parent.getParent();
    frame = new JDialog( (JFrame) parent);
    frame.setModal(true);
    frame.setSize(300, 300);
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(explainationText,
                               new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    JButton button = new JButton();
    button.setText("OK");
    button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        button_actionPerformed(e);
      }
    });
    frame.getContentPane().add(button,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 0, 0));
    frame.show();
    frame.pack();

  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void button_actionPerformed(ActionEvent actionEvent) {
    frame.dispose();
  }
  /**
   *
   * @param frameTitle String
   */
  private void setExplainationForSelectedAnalysisOption(String
      selectedAnalysisOption) {
    if (selectedAnalysisOption.equals(GlobalConstants.PROB_HAZ_CURVES)) {
      this.explainationText.setText("Probabilistic Hazard Curves  - " +
                                    "This option allows the user to obtain " +
                                    "hazard curves for a number of acceleration " +
                                    "parameters, such as peak ground acceleration " +
                                    "or response spectral accleration.    " +
                                    "Data sets include the following: 48 conterminous states " +
                                    "- 1996 and 2002, Alaska - 1998, Hawaii - 1998, " +
                                    "Puerto Rico and the Virgin Islands - 2003.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.PROB_UNIFORM_HAZ_RES)) {
      this.explainationText.setText(
          "Probabilistic Uniform Hazard Response Spectra  - " +
          "This option allows the user to obtain uniform hazard " +
          "response spectra for 2% probabililty of " +
          "exceedance in 50 years, 10% probability of " +
          "exceedance in 50 years, and in a few cases " +
          "for 5% probability of exceedance in 50 years.   " +
          "Data sets include the following: 48 conterminous " +
          "states - 1996 and 2002, Alaska - 1998, Hawaii - 1998, " +
          "Puerto Rico and the Virgin Islands - 2003. ");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.NEHRP)) {
      this.explainationText.setText("NEHRP Recommended Provisions for Seismic " +
                                    "Regulations for New Buildings and Other " +
                                    "Structures  - This option may be used for " +
                                    "the 1997, 2000, and 2003 editions of the  " +
                                    "NEHRP Recommended Provisions for Seismic " +
                                    "Regulations for New Buildings and Other Structures.  " +
                                    "The user may calculate seismic design parameters " +
                                    "and response spectra (both for period and displacement), " +
                                    "for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_273)) {
      this.explainationText.setText("FEMA 273, MCE Guidelines for the Seismic " +
                                    "Rehabilitation of Buildings  - " +
                                    "This option may be used for FEMA 273,  " +
                                    "MCE Guidelines for the Seismic Rehabilitation of Buildings " +
                                    "(1997).  The user may calculate seismic " +
                                    "design parameters and response spectra " +
                                    "(both for period and displacement), for " +
                                    "Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_356)) {
      this.explainationText.setText("FEMA 356, Prestandard and Commentary for " +
                                    "the Seismic Rehabilitation of Buildings  - " +
                                    "This option may be used for FEMA 356,  " +
                                    "Prestandard and Commentary for the Seismic " +
                                    "Rehabilitation of Buildings (2000).  The " +
                                    "user may calculate seismic design parameters " +
                                    "and response spectra (both for period and " +
                                    "displacement), for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.INTL_BUILDING_CODE)) {
      this.explainationText.setText("International Building Code  - This " +
                                    "option may be used for the 2000 and 2003 " +
                                    "editions of the  International Building Code.  " +
                                    "The user may calculate seismic design parameters " +
                                    "and response spectra (both for period and displacement), " +
                                    "for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.INTL_RESIDENTIAL_CODE)) {
      this.explainationText.setText("International Residential Code  - " +
                                    "This option may be used for the 2000, " +
                                    "2003, and 2004 editions of the  " +
                                    "International Residential Code.  The " +
                                    "user may determine the Seismic Design " +
                                    "Categories for the default Site Class D.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.INTL_EXIST_CODE)) {
      this.explainationText.setText("International Existing Building Code  - " +
                                    "This option may be used for the 1997, 2000, " +
                                    "and 2003 editions of the  International Existing " +
                                    "Building Code.  The user may calculate seismic " +
                                    "design parameters and response spectra " +
                                    "(both for period and displacement), " +
                                    "for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.NFPA_5000)) {
      this.explainationText.setText(
          "NFPA 5000 Building Construction and Safety Code " +
          "- This option may be used for the 2000 edition " +
          "of the  NFPA 5000 Building Construction and " +
          "Safety Code.  The user may calculate seismic " +
          "design parameters and response spectra (both " +
          "for period and displacement), for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.ASCE_7)) {
      this.explainationText.setText(
          "ASCE 7 Standard, Minimum Design Loads for " +
          "Buildings and Other Structures  - This option " +
          "may be used for the 1998 and 2002 editions " +
          "of the ASCE 7 Standard,  Minimum Design Loads " +
          "for Buildings and Other Structures.  " +
          "The user may calculate seismic design " +
          "parameters and response spectra (both for " +
          "period and displacement), for Site Class A through E.");
    }
  }

}
