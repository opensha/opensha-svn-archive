package org.opensha.nshmp.sha.gui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.opensha.util.*;
import org.opensha.nshmp.sha.gui.api.*;
import org.opensha.nshmp.sha.gui.beans.*;
import org.opensha.nshmp.util.*;
import org.opensha.nshmp.sha.gui.infoTools.AddProjectNameDateWindow;

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
  JMenu helpMenu = new JMenu();
  JMenuItem fileExitMenu = new JMenuItem();
  JMenuItem filePrintMenu = new JMenuItem();
  JMenuItem fileSaveMenu = new JMenuItem();
  JMenuItem fileAddProjNameMenu = new JMenuItem();
  JMenuItem helpAnalysisOptionExplainationMenu = new JMenuItem();

  // height and width of the applet
  private final static int W = 900;
  private final static int H = 660;
  JPanel jPanel1 = new JPanel();
  JSplitPane mainSplitPane = new JSplitPane();
  JLabel analysisOptionLabel = new JLabel();
  JComboBox analysisOptionSelectionCombo = new JComboBox();
  JSplitPane dataSplitPane = new JSplitPane();
  JScrollPane dataScrollPane = new JScrollPane();
  JScrollPane parametersScrollPane = new JScrollPane();
  JPanel buttonPanel = new JPanel();
  JTextArea dataTextArea = new JTextArea();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  JButton clearDataButton = new JButton();
  JButton viewMapsButton = new JButton();
  FlowLayout flowLayout1 = new FlowLayout();
  private JButton explainButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JDialog frame;
  private JTextPane explainationText;
  private JDialog analysisOptionExpFrame;
  private JTextPane analysisText;
  private JScrollPane analysisScrollPane;

  private JLabel imgLabel = new JLabel(GlobalConstants.USGS_LOGO_ICON);

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
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  private AddProjectNameDateWindow projectNameWindow;

  public ProbabilisticHazardApplication() {
    try {
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      jbInit();
      setIconImage(GlobalConstants.USGS_LOGO_ICON.getImage());
      //setExplainationForSelectedAnalysisOption(previousSelectedAnalysisOption);
      createGuiBeanInstance();
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

  /**
   * Component initialization.
   *
   * @throws java.lang.Exception
   */
  private void jbInit() throws Exception {
    contentPane = (JPanel) getContentPane();
    contentPane.setLayout(borderLayout1);
    setTitle("Seismic Hazard Curves and Uniform Hazard Response Spectra");
    this.setDefaultCloseOperation(this.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }
    });
    fileMenu.setText("File");
    helpMenu.setText("Help");
    fileExitMenu.setText("Exit");
    fileSaveMenu.setText("Save");
    filePrintMenu.setText("Print");
    fileAddProjNameMenu.setText("Add Name & Date");
    helpAnalysisOptionExplainationMenu.setText("Analysis Option Explaination");

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

    fileAddProjNameMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fileAddProjNameMenu_actionPerformed(e);
      }
    });


    filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filePrintMenu_actionPerformed(e);
      }
    });

    helpAnalysisOptionExplainationMenu.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            helpAnalysisOptionExplainationMenu_actionPerformed(e);
          }
    });

    jPanel1.setLayout(gridBagLayout1);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setForeground(Color.black);
    analysisOptionLabel.setFont(new java.awt.Font("Arial", Font.PLAIN, 15));
    analysisOptionLabel.setForeground(Color.red);
    analysisOptionLabel.setText("Select Analysis Option:");
    analysisOptionSelectionCombo.setFont(new java.awt.Font("Arial", Font.PLAIN,
        15));
    analysisOptionSelectionCombo.setForeground(Color.BLUE);

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
    dataSplitPane.setMinimumSize(new Dimension(10,10));
    outputBorder.setTitleColor(Color.RED);
    dataTextArea.setText("");

    buttonPanel.setLayout(gridBagLayout2);
    clearDataButton.setText("Clear Data");
    viewMapsButton.setText("View Maps");

    explainButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        explainButton_actionPerformed(actionEvent);
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
    applicationMenu.add(helpMenu);
    fileMenu.add(fileSaveMenu);
    fileMenu.add(fileAddProjNameMenu);
    fileMenu.add(filePrintMenu);
    fileMenu.add(fileExitMenu);
    helpMenu.add(helpAnalysisOptionExplainationMenu);
    mainSplitPane.add(dataSplitPane, JSplitPane.RIGHT);
    parametersScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    parametersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    mainSplitPane.add(parametersScrollPane, JSplitPane.LEFT);
    dataSplitPane.add(dataScrollPane, JSplitPane.TOP);
    dataSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    contentPane.add(jPanel1, java.awt.BorderLayout.CENTER);

    buttonPanel.setMinimumSize(new Dimension(0,0));
    dataScrollPane.getViewport().add(dataTextArea, null);

    explainButton.setText("Explain");
    buttonPanel.add(imgLabel, new GridBagConstraints(0, 1, 2, 2, 1.0, 1.0
        , GridBagConstraints.NORTH, GridBagConstraints.NONE,
        new Insets(2, 60, 0, 60), 0, 0));
    buttonPanel.add(viewMapsButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.NORTH, GridBagConstraints.EAST,
        new Insets(4, 120, 0, 0), 0, 0));
    buttonPanel.add(clearDataButton,
                    new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.NORTH,
                                           GridBagConstraints.WEST,
                                           new Insets(4, 12, 0, 120), 0, 0));

    jPanel1.add(analysisOptionLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                       , GridBagConstraints.EAST,
                                       GridBagConstraints.NONE,
                                       new Insets(9, 4, 0, 0), 0, 0));
    jPanel1.add(analysisOptionSelectionCombo,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                       , GridBagConstraints.CENTER,
                                       GridBagConstraints.HORIZONTAL,
                                       new Insets(9, 0, 0, 0), 0, 0));
    jPanel1.add(explainButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.EAST, GridBagConstraints.NONE,
        new Insets(6, 4, 2, 60), 0, 0));
    jPanel1.add(mainSplitPane, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(10, 8, 5, 10), 0, 0));
    parametersScrollPane.getViewport().setLayout(new BorderLayout());
    setJMenuBar(applicationMenu);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(W,H);
    this.setLocation( (d.width - this.getSize().width) / 2, 0);
    mainSplitPane.setDividerLocation(410);
    dataSplitPane.setDividerLocation(414);
    buttonPanel.updateUI();
    viewMapsButton.setEnabled(false);
    contentPane.updateUI();
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
   * File | Add Name and Date to the Data action performed.
   *
   * @param actionEvent ActionEvent
   */
  void fileAddProjNameMenu_actionPerformed(ActionEvent actionEvent) {
    if(projectNameWindow ==null){
      projectNameWindow = new AddProjectNameDateWindow();
    }
    projectNameWindow.show();
  }

  /**
   * Help | Explaination to all the Analysis Options
   *
   * @param actionEvent ActionEvent
   */
  void helpAnalysisOptionExplainationMenu_actionPerformed(ActionEvent
      actionEvent) {

    if(analysisOptionExpFrame == null){
      //Panel Parent
      Container parent = this;
      /*
       This loops over all the parent of this class until the parent is Frame(applet)
            this is required for the passing in the JDialog to keep the focus on the adjustable params
            frame
       */
      while (! (parent instanceof JFrame) && parent != null) {
        parent = parent.getParent();
      }

      analysisOptionExpFrame = new JDialog( (JFrame) parent);
      analysisOptionExpFrame.setModal(true);
      analysisOptionExpFrame.setSize(500, 350);
      analysisOptionExpFrame.getContentPane().setLayout(new GridBagLayout());
      analysisText = new JTextPane();
      analysisText.setText(GlobalConstants.getAllExplainationsForAnalysisOption());
      analysisText.setEditable(false);
      analysisScrollPane = new JScrollPane();
      analysisScrollPane.getViewport().add(analysisText, null);

      analysisOptionExpFrame.getContentPane().add(analysisScrollPane,
                                                  new GridBagConstraints(0, 0, 1, 1,
          1.0, 1.0
          , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(4, 4, 4, 4), 0, 0));
      analysisOptionExpFrame.setLocation( (getSize().width - analysisOptionExpFrame.getWidth()) / 3,
                                         (getSize().height - analysisOptionExpFrame.getHeight()) / 3);
     analysisOptionExpFrame.setTitle("Analysis Options Explaination");
    }
    analysisOptionExpFrame.show();
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
    analysisOptionHash.put(previousSelectedAnalysisOption, guiBeanAPI);
    String selectedAnalysisOption = (String) analysisOptionSelectionCombo.
        getSelectedItem();
    guiBeanAPI = (AnalysisOptionsGuiBeanAPI) analysisOptionHash.get(
        selectedAnalysisOption);
    parametersScrollPane.getViewport().removeAll();
    if (guiBeanAPI == null) {
      createGuiBeanInstance();
    }
    else {
      parametersScrollPane.getViewport().add(guiBeanAPI.getGuiBean(),BorderLayout.CENTER);
      parametersScrollPane.updateUI();
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
      guiBeanAPI = new UHS_GuiBean(this);
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
      guiBeanAPI = new IRC_GuiBean(this);
    }
    else if (selectedAnalysisOption.equals(GlobalConstants.ASCE_NFPA)) {
      guiBeanAPI = new ASCE7_NFPA_GuiBean(this);
    }
    if (guiBeanAPI != null) {
      parametersScrollPane.getViewport().add(guiBeanAPI.getGuiBean(),BorderLayout.CENTER);
    }

    parametersScrollPane.updateUI();
  }


  /**
   * Sets the information from the Gui beans in Data window
   * @param dataInfo String
   */
  public void setDataInWindow(String dataInfo) {
    String data="";
    if(projectNameWindow !=null){
      String name = projectNameWindow.getProjectName();
      String date = projectNameWindow.getDate();
      if(name !=null && !name.trim().equals(""))
        data +="Project Name = "+name+"\n";
      if(date !=null)
        data +="Date = "+date +"\n\n";
    }
    dataTextArea.setText(data+dataInfo);
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  private void explainButton_actionPerformed(ActionEvent actionEvent) {
    String analysisOption = (String) analysisOptionSelectionCombo.getSelectedItem();
    setExplainationForSelectedAnalysisOption(analysisOption);
    //if frame is null the create the frame
    if (frame == null)
      showSelectedAnalysisExplaination();

    frame.setTitle(analysisOption);
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
    frame.setLocation((getSize().width - frame.getWidth())/3 , (getSize().height - frame.getHeight())/ 3);
  }

  /**
   *
   * @param selectedAnalysisOption String : Selected Analysis option
   */
  private void setExplainationForSelectedAnalysisOption(String
      selectedAnalysisOption) {

    if(explainationText == null){
      explainationText = new JTextPane();
      explainationText.setEditable(false);
    }
    explainationText.setText(GlobalConstants.
                             getExplainationForSelectedAnalysisOption(
        selectedAnalysisOption));
  }

  void this_windowClosing(WindowEvent e) {
    closeWindow();
  }
}
