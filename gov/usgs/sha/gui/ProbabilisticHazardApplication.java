package gov.usgs.sha.gui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.util.*;
import gov.usgs.sha.gui.api.*;
import gov.usgs.sha.gui.beans.*;
import gov.usgs.util.*;

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
    extends JFrame implements ProbabilisticHazardApplicationAPI {

  JPanel contentPane;

  JMenuBar applicationMenu = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenuItem fileExitMenu = new JMenuItem();
  JMenuItem filePrintMenu = new JMenuItem();
  JMenuItem fileSaveMenu = new JMenuItem();

  // height and width of the applet
  private final static int W = 1100;
  private final static int H = 850;
  JPanel jPanel1 = new JPanel();
  JSplitPane mainSplitPane = new JSplitPane();
  JLabel analysisOptionLabel = new JLabel();
  JComboBox analysisOptionSelectionCombo = new JComboBox();
  JSplitPane dataSplitPane = new JSplitPane();
  JScrollPane dataScrollPane = new JScrollPane();
  JScrollPane parametersScrollPane = new JScrollPane();
  JPanel parametersPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JTextArea dataTextArea = new JTextArea();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  JButton clearDataButton = new JButton();
  JButton viewMapsButton = new JButton();
  FlowLayout flowLayout1 = new FlowLayout();
  private JButton ExplainButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JDialog frame;
  private JTextPane explainationText;

  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder outputBorder = new TitledBorder(border9,
                                               "Output for All Calculations");

  //instance of the gui bean for the selected analysis option
  private AnalysisOptionsGuiBeanAPI guiBeanAPI;

  //This HashMap adds Guibeans for the selected Analysis options
  private HashMap analysisOptionHash = new HashMap();
  //saves which was the last selected analysis option
  private String previousSelectedAnalysisOption;
  BorderLayout borderLayout4 = new BorderLayout();

  public ProbabilisticHazardApplication() {
    try {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      jbInit();
      setExplainationForSelectedAnalysisOption(previousSelectedAnalysisOption);
      createGuiBeanInstance();
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
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }
    });
    fileMenu.setText("File");
    fileExitMenu.setText("Exit");
    fileSaveMenu.setText("Save");
    filePrintMenu.setText("Print");
    fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileExitMenu_actionPerformed(e);
      }
    });
    fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileSaveMenu_actionPerformed(e);
      }
    });

    filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filePrintMenu_actionPerformed(e);
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
    analysisOptionSelectionCombo.setForeground(new Color(200, 200, 240));

    //adding the supported Analysis option to the combo selection
    ArrayList supportAnalysisOptions = GlobalConstants.
        getSupportedAnalysisOptions();
    int size = supportAnalysisOptions.size();

    for (int i = 0; i < size; ++i) {
      analysisOptionSelectionCombo.addItem(supportAnalysisOptions.get(i));
    }
    previousSelectedAnalysisOption = (String) supportAnalysisOptions.get(0);

    analysisOptionSelectionCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        analysisOptionSelectionCombo_itemStateChanged(itemEvent);
      }
    });

    dataSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    dataSplitPane.setBorder(outputBorder);
    outputBorder.setTitleColor(Color.RED);
    dataTextArea.setText("");
    dataScrollPane.setBounds(new Rectangle(10, 10, 484, 548));
    parametersScrollPane.setBounds(new Rectangle(2, 2, 530, 720));
    parametersPanel.setLayout(borderLayout4);
    buttonPanel.setLayout(flowLayout1);
    clearDataButton.setText("Clear Data");
    viewMapsButton.setText("View Maps");
    ExplainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        ExplainButton_actionPerformed(actionEvent);
      }
    });
    clearDataButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        clearDataButton_actionPerformed(actionEvent);
      }
    });

    viewMapsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewMapsButton_actionPerformed(actionEvent);
      }
    });

    applicationMenu.add(fileMenu);
    fileMenu.add(fileSaveMenu);
    fileMenu.add(filePrintMenu);
    fileMenu.add(fileExitMenu);
    mainSplitPane.add(dataSplitPane, JSplitPane.RIGHT);
    mainSplitPane.add(parametersScrollPane, JSplitPane.LEFT);
    dataSplitPane.add(dataScrollPane, JSplitPane.TOP);
    dataSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    contentPane.add(jPanel1, java.awt.BorderLayout.CENTER);
    ExplainButton.setText("Explain");
    explainationText = new JTextPane();
    explainationText.setEditable(false);

    dataScrollPane.getViewport().add(dataTextArea, java.awt.BorderLayout.CENTER);
    parametersScrollPane.getViewport().add(parametersPanel,
                                           java.awt.BorderLayout.CENTER);
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
    setJMenuBar(applicationMenu);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setLocation( (d.width - this.getSize().width) / 2,
                     (d.height - this.getSize().height) / 2);
    mainSplitPane.setDividerLocation(550);
    dataSplitPane.setDividerLocation(600);
  }

  /**
   * File | Exit action performed.
   *
   * @param actionEvent ActionEvent
   */
  void fileExitMenu_actionPerformed(ActionEvent actionEvent) {

    closeWindow();
  }

  private void closeWindow() {
    int option = JOptionPane.showConfirmDialog(this,
                                               "Do you really want to exit the application?\n" +
                                               "You will loose any unsaved data",
                                               "Closing Application",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
      System.exit(0);
    }
    return;

  }

  /**
   * File | Save action performed.
   *
   * @param actionEvent ActionEvent
   */
  void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
    save();
  }

  /**
   * File | Print action performed.
   *
   * @param actionEvent ActionEvent
   */
  void filePrintMenu_actionPerformed(ActionEvent actionEvent) {
    print();
  }

  private void save() {
    DataUtil.save(this, guiBeanAPI.getData());
  }

  /**
   * Method to print the Data
   */
  private void print() {
    Properties p = new Properties();
    PrintJob pjob = getToolkit().getPrintJob(this, "Printing", p);
    if (pjob != null) {
      Graphics pg = pjob.getGraphics();
      if (pg != null) {
        DataUtil.print(pjob, pg, guiBeanAPI.getData());
        pg.dispose();
      }
      pjob.end();
    }
  }

  private void analysisOptionSelectionCombo_itemStateChanged(ItemEvent
      itemEvent) {
    showAnalysisOptionInWindow();
    analysisOptionHash.put(previousSelectedAnalysisOption, guiBeanAPI);
    String selectedAnalysisOption = (String) analysisOptionSelectionCombo.
        getSelectedItem();
    guiBeanAPI = (AnalysisOptionsGuiBeanAPI) analysisOptionHash.get(
        selectedAnalysisOption);
    parametersPanel.removeAll();
    if (guiBeanAPI == null) {
      createGuiBeanInstance();
    }
    else {
      parametersPanel.add(guiBeanAPI.getGuiBean(), java.awt.BorderLayout.CENTER);
      parametersPanel.updateUI();
    }
    setDataInWindow(guiBeanAPI.getData());
    previousSelectedAnalysisOption = selectedAnalysisOption;
  }

  private void createGuiBeanInstance() {
    String selectedAnalysisOption = (String) (String)
        analysisOptionSelectionCombo.getSelectedItem();
    if (selectedAnalysisOption.equals(GlobalConstants.PROB_HAZ_CURVES)) {
      guiBeanAPI = new ProbHazCurvesGuiBean(this);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.PROB_UNIFORM_HAZ_RES)) {
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.NEHRP)) {
      guiBeanAPI = new NEHRP_GuiBean(this);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_IEBC_2003)) {
      guiBeanAPI = new FEMA_GuiBean(this);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.INTL_BUILDING_CODE)) {
      guiBeanAPI = new IBC_GuiBean(this);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.
                                           INTL_RESIDENTIAL_CODE)) {
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.ASCE_NFPA)) {
      guiBeanAPI = new ASCE7_NFPA_GuiBean(this);
    }
    if (guiBeanAPI != null) {
      parametersPanel.add(guiBeanAPI.getGuiBean(), java.awt.BorderLayout.CENTER);
    }

    parametersPanel.updateUI();
  }

  private void showAnalysisOptionInWindow() {
    String frameTitle = (String) analysisOptionSelectionCombo.getSelectedItem();
    if (frame != null) {
      frame.setTitle(frameTitle);
      setExplainationForSelectedAnalysisOption(frameTitle);
    }

  }

  /**
   * Sets the information from the Gui beans in Data window
   * @param dataInfo String
   */
  public void setDataInWindow(String dataInfo) {
    dataTextArea.setText(dataInfo);
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void ExplainButton_actionPerformed(ActionEvent actionEvent) {
    if (frame == null) {
      showSelectedAnalysisExplaination();
    }

    frame.show();
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void clearDataButton_actionPerformed(ActionEvent actionEvent) {
    guiBeanAPI.clearData();
    setDataInWindow("");
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void viewMapsButton_actionPerformed(ActionEvent actionEvent) {

  }

  /**
   *
   *
   */
  private void showSelectedAnalysisExplaination() {

    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
         this is required for the passing in the JDialog to keep the focus on the adjustable params
         frame*/
    while (! (parent instanceof JFrame) && parent != null) {
      parent = parent.getParent();
    }
    frame = new JDialog( (JFrame) parent);
    frame.setModal(true);
    frame.setSize(400, 200);
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(explainationText,
                               new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 4, 4), 0, 0));
    frame.setLocation(getSize().width / 2, this.getSize().height / 2);
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
    else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_IEBC_2003)) {
      this.explainationText.setText("FEMA 273, MCE Guidelines for the Seismic " +
                                    "Rehabilitation of Buildings  - " +
                                    "This option may be used for FEMA 273,  " +
                                    "MCE Guidelines for the Seismic Rehabilitation of Buildings " +
                                    "(1997).  The user may calculate seismic " +
                                    "design parameters and response spectra " +
                                    "(both for period and displacement), for " +
                                    "Site Class A through E.\n" +
                                    "FEMA 356, Prestandard and Commentary for " +
                                    "the Seismic Rehabilitation of Buildings  - " +
                                    "This option may be used for FEMA 356,  " +
                                    "Prestandard and Commentary for the Seismic " +
                                    "Rehabilitation of Buildings (2000).  The " +
                                    "user may calculate seismic design parameters " +
                                    "and response spectra (both for period and " +
                                    "displacement), for Site Class A through E.\n" +
                                    "International Existing Building Code  - " +
                                    "This option may be used for the 1997, 2000, " +
                                    "and 2003 editions of the  International Existing " +
                                    "Building Code.  The user may calculate seismic " +
                                    "design parameters and response spectra " +
                                    "(both for period and displacement), " +
                                    "for Site Class A through E.");
    }
    /*else if (selectedAnalysisOption.equals(GlobalConstants.FEMA_356)) {
     this.explainationText.setText("FEMA 356, Prestandard and Commentary for " +
     "the Seismic Rehabilitation of Buildings  - " +
                                    "This option may be used for FEMA 356,  " +
     "Prestandard and Commentary for the Seismic " +
     "Rehabilitation of Buildings (2000).  The " +
     "user may calculate seismic design parameters " +
     "and response spectra (both for period and " +
     "displacement), for Site Class A through E.");
         }*/
    else if (selectedAnalysisOption.equals(GlobalConstants.INTL_BUILDING_CODE)) {
      this.explainationText.setText("International Building Code  - This " +
                                    "option may be used for the 2000 and 2003 " +
                                    "editions of the  International Building Code.  " +
                                    "The user may calculate seismic design parameters " +
                                    "and response spectra (both for period and displacement), " +
                                    "for Site Class A through E.");
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.
                                           INTL_RESIDENTIAL_CODE)) {
      this.explainationText.setText("International Residential Code  - " +
                                    "This option may be used for the 2000, " +
                                    "2003, and 2004 editions of the  " +
                                    "International Residential Code.  The " +
                                    "user may determine the Seismic Design " +
                                    "Categories for the default Site Class D.");
    }
    /*else if (selectedAnalysisOption.equals(GlobalConstants.INTL_EXIST_CODE)) {
      this.explainationText.setText("International Existing Building Code  - " +
                                    "This option may be used for the 1997, 2000, " +
                                    "and 2003 editions of the  International Existing " +
                                    "Building Code.  The user may calculate seismic " +
                                    "design parameters and response spectra " +
                                    "(both for period and displacement), " +
                                    "for Site Class A through E.");
    }*/
    else if (selectedAnalysisOption.equals(GlobalConstants.ASCE_NFPA)) {
      this.explainationText.setText(
          "NFPA 5000 Building Construction and Safety Code " +
          "- This option may be used for the 2000 edition " +
          "of the  NFPA 5000 Building Construction and " +
          "Safety Code.  The user may calculate seismic " +
          "design parameters and response spectra (both " +
          "for period and displacement), for Site Class A through E.\n"+
          "ASCE 7 Standard, Minimum Design Loads for " +
          "Buildings and Other Structures  - This option " +
          "may be used for the 1998 and 2002 editions " +
          "of the ASCE 7 Standard,  Minimum Design Loads " +
          "for Buildings and Other Structures.  " +
          "The user may calculate seismic design " +
          "parameters and response spectra (both for " +
          "period and displacement), for Site Class A through E.");
    }
    /*else if (selectedAnalysisOption.equals(GlobalConstants.ASCE_7)) {
      this.explainationText.setText(
          "ASCE 7 Standard, Minimum Design Loads for " +
          "Buildings and Other Structures  - This option " +
          "may be used for the 1998 and 2002 editions " +
          "of the ASCE 7 Standard,  Minimum Design Loads " +
          "for Buildings and Other Structures.  " +
          "The user may calculate seismic design " +
          "parameters and response spectra (both for " +
          "period and displacement), for Site Class A through E.");
    }*/
  }

  void this_windowClosing(WindowEvent e) {
    closeWindow();
  }

}
