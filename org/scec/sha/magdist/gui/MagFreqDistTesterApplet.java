package org.scec.sha.magdist.gui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


import com.jrefinery.chart.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;


import org.scec.gui.*;
import org.scec.gui.plot.jfreechart.DiscretizedFunctionXYDataSet;

import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.sha.imr.gui.ShowMessage;


import org.scec.sha.magdist.*;
/**
 * <p>Title: MagFreqDistTesterApplet</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta  Date: Aug,9,2002
 * @version 1.0
 */

public class MagFreqDistTesterApplet extends JApplet
            implements ItemListener,
                      ParameterChangeFailListener,
                      ParameterChangeWarningListener {


  protected final static String C = "MagFreqDistTesterApplet";
  protected final static boolean D = true;


  protected final static int W = 850;
  protected final static int H = 670;
  protected final static int A1 = 360;
  protected final static int A2 = 430;
  protected final static Font BUTTON_FONT = new java.awt.Font( "Dialog", 1, 11 );
  final static Dimension BUTTON_DIM = new Dimension( 80, 20 );
  final static Dimension COMBO_DIM = new Dimension( 180, 20 );
  final static String NO_PLOT_MSG = "No Plot Data Available";
  private final static String MAG = new  String("Magnitude");
  private final static String INCR_RATE = new String("Incremental Rate");
  private final static String CUM_RATE = new  String("Cumulative Rate");
  private final static String MO_RATE = new  String("Moment Rate");

  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String GaussianMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GaussianMagFreqDist";
  protected final static String GuttenbergRichterMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.GuttenbergRichterMagFreqDist";
  protected final static String SingleMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SingleMagFreqDist";
  protected final static String SummedMagFreqDist_CLASS_NAME = "org.scec.sha.magdist.SummedMagFreqDist";


  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String GAUSSIAN_NAME = "Gaussian Distribution";
  protected final static String GR_NAME = "GuttenbergRichter Distribution";
  protected final static String SINGLE_NAME = "Single Distribution";
  protected final static String SUMMED_NAME = "Summed Distribution";


  /**
   *  Used to determine if should switch to new MagDist, and for display purposes
   */
  public String currentMagDistName = "";
  boolean isStandalone = false;
  protected boolean inParameterChangeWarning = false;


  Insets plotInsets = new Insets( 4, 10, 4, 4 );
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );
  Insets emptyInsets = new Insets( 0, 0, 0, 0 );
  /**
   *  Hashmap that maps picklist MagFreqDist string names to the real fully qualified
   *  class names
   */
protected static HashMap magDistNames = new HashMap();
  private JComboBox magDistComboBox = new JComboBox();
  private JPanel mainPanel = new JPanel();
  private GridBagLayout GBL = new GridBagLayout();
  private JComboBox incrComboBox = new JComboBox();
  private JCheckBox plotColorCheckBox = new JCheckBox();
  private JButton clearButton = new JButton();
  private JLabel jIncrAxisScale = new JLabel();
  private JCheckBox jCheckylog = new JCheckBox();
  private JButton toggleButton = new JButton();
  private JPanel buttonPanel = new JPanel();
  private JLabel magDistLabel = new JLabel();
  private JButton addButton = new JButton();
  private JPanel outerPanel = new JPanel();
  private JSplitPane mainSplitPane = new JSplitPane();
  private JLabel jCumAxisScale = new JLabel();
  private JComboBox cumComboBox = new JComboBox();
  private JLabel jMoRate = new JLabel();
  private JComboBox moRateComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  protected javax.swing.JFrame frame;

  private JPanel parametersPanel = new JPanel();
  private JPanel outerControlPanel = new JPanel();
  private JSplitPane parametersSplitPane = new JSplitPane();
  private JPanel controlPanel = new JPanel();
  private ChartPanel incrPanel;
  private ChartPanel cumPanel;
  private ChartPanel moPanel;

  private JScrollPane dataScrollPane = new JScrollPane();
  private JTextArea pointsTextArea = new JTextArea();
  private JPanel sheetPanel = new JPanel();
  private JPanel inputPanel = new JPanel();
  private JLabel titleLabel = new JLabel();
  private JPanel plotPanel = new JPanel();
  private JPanel titlePanel = new JPanel();
  private JPanel innerPlotPanel = new JPanel();

  Color darkBlue = new Color( 80, 80, 133 );
  Color lightBlue = new Color( 200, 200, 230 );
  protected boolean graphOn = false;
  boolean isWhite = true;
  Color background = Color.white;
  SidesBorder topBorder = new SidesBorder( darkBlue, background, background, background );
  SidesBorder bottomBorder = new SidesBorder( background, darkBlue, background, background );
  OvalBorder oval = new OvalBorder( 12, 4, darkBlue, darkBlue );

  int titleSize = 0;

   /**
     *  Currently selected IMR and related information needed for the gui to
     *  work
     */
  MagDistGuiBean magDist = null;

    /**
     *  List that contains the lazy instantiation of imrs via reflection and the
     *  imr full class names
     */
  protected MagDistGuiList magDists = new MagDistGuiList();

  /**
   * For 3 different plots we are using the different objects to refer for incrRate Data,
   * total Cum Rate Data and total Moment Rate Data.
   */

  DiscretizedFuncList incrFunctions = new DiscretizedFuncList();
  DiscretizedFuncList toCumFunctions = new DiscretizedFuncList();
  DiscretizedFuncList toMoFunctions = new DiscretizedFuncList();

  DiscretizedFunctionXYDataSet incrData = new DiscretizedFunctionXYDataSet();
  DiscretizedFunctionXYDataSet toCumData = new DiscretizedFunctionXYDataSet();
  DiscretizedFunctionXYDataSet toMoData = new DiscretizedFunctionXYDataSet();


   private boolean yLog = false;



/**
 *  NED - Here is where you can add the new MagFreqDist, follow my examples below
 *  Populates the magDist hashmap with the strings in the picklist for the
 *  applet mapped to the class names of the MagFreqDist. This will use the class
 *  loader to load these
 */
static {
    if ( magDistNames == null ) magDistNames = new HashMap();
    magDistNames.clear();
    magDistNames.put( GAUSSIAN_NAME, GaussianMagFreqDist_CLASS_NAME );
    magDistNames.put( GR_NAME, GuttenbergRichterMagFreqDist_CLASS_NAME );
    magDistNames.put( SINGLE_NAME, SingleMagFreqDist_CLASS_NAME );
    magDistNames.put( SUMMED_NAME, SummedMagFreqDist_CLASS_NAME );

    try { UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() ); }
    catch ( Exception e ) {}
}

   /**
     *  Construct the applet
     */

  public MagFreqDistTesterApplet() {
      incrData.setFunctions(incrFunctions);
      toCumData.setFunctions(toCumFunctions);
      toMoData.setFunctions(toMoFunctions);
      incrFunctions.setXAxisName(MAG);
      toCumFunctions.setXAxisName(MAG);
      toMoFunctions.setXAxisName(MAG);
      incrFunctions.setYAxisName(INCR_RATE);
      toCumFunctions.setYAxisName(CUM_RATE);
      toMoFunctions.setYAxisName(MO_RATE);
  }

  /**
   *  Initialize the applet
   */
    public void init() {

        try {
            jbInit();
            initMagDistGui();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }


  private void jbInit() throws Exception {
    mainPanel.setLayout(GBL);
    mainPanel.setBorder( oval );
    magDistComboBox.setMinimumSize(new Dimension(150, 20));
    magDistComboBox.addItemListener(this);
    magDistComboBox.setPreferredSize(new Dimension(150, 20));
    magDistComboBox.setBorder(null);
    magDistComboBox.setMaximumSize(new Dimension(150, 20));
    magDistComboBox.setBackground(new Color(200, 200, 230));
    magDistComboBox.setFont(new java.awt.Font("Dialog", 1, 12));
    magDistComboBox.setForeground(new Color(80, 80, 133));
    this.getContentPane().setLayout(GBL);
    incrComboBox.setBackground(new Color(200, 200, 230));
    incrComboBox.setForeground(new Color(80, 80, 133));
    incrComboBox.setMaximumSize(new Dimension(105, 19));
    incrComboBox.setMinimumSize(new Dimension(105, 19));
    incrComboBox.setPreferredSize(new Dimension(105, 19));
    /*incrComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
          incrComboBox_actionPerformed(e);
        }
        });*/
    plotColorCheckBox.setBackground(Color.white);
    plotColorCheckBox.setFont(new java.awt.Font("Dialog", 1, 11));
    plotColorCheckBox.setForeground(new Color(80, 80, 133));
    plotColorCheckBox.setText("Black Background");
    plotColorCheckBox.addItemListener(this);
    clearButton.setBackground(new Color(200, 200, 230));
    clearButton.setFont(BUTTON_FONT);
    clearButton.setForeground(new Color(80, 80, 133));
    clearButton.setBorder(BorderFactory.createRaisedBevelBorder());
    clearButton.setFocusPainted(false);
    clearButton.setText("Clear Plot");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clearButton_actionPerformed(e);
      }
    });
    clearButton.setPreferredSize(BUTTON_DIM);
    clearButton.setMinimumSize(BUTTON_DIM);
    jIncrAxisScale.setFont(new java.awt.Font("Dialog", 1, 12));
    jIncrAxisScale.setForeground(new Color(80, 80, 133));
    jIncrAxisScale.setToolTipText("");
    jIncrAxisScale.setText("Set  Incr Axis Scale: ");
    jCheckylog.setBackground(Color.white);
    jCheckylog.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckylog.setForeground(new Color(80, 80, 133));
    jCheckylog.setText("Y-Log");
    jCheckylog.addItemListener(this);
    toggleButton.setBackground(new Color(200, 200, 230));
    toggleButton.setFont(BUTTON_FONT);
    toggleButton.setForeground(new Color(80, 80, 133));
    toggleButton.setBorder(BorderFactory.createRaisedBevelBorder());
    toggleButton.setFocusPainted(false);
    toggleButton.setText("Show Data");
    toggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toggleButton_actionPerformed(e);
      }
    });
    toggleButton.setPreferredSize(BUTTON_DIM);
    toggleButton.setMinimumSize(BUTTON_DIM);
    buttonPanel.setLayout(GBL);
    magDistLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    magDistLabel.setForeground(new Color(80, 80, 133));
    magDistLabel.setText("Choose Distribution: ");
    addButton.setBackground(new Color(200, 200, 230));
    addButton.setFont(BUTTON_FONT);
    addButton.setForeground(new Color(80, 80, 133));
    addButton.setBorder(BorderFactory.createRaisedBevelBorder());
    addButton.setFocusPainted(false);
    addButton.setText("Add Dist");
    addButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addButton_actionPerformed(e);
      }
    });
    addButton.setPreferredSize(BUTTON_DIM);
    addButton.setActionCommand("Add Dist");
    addButton.setMinimumSize(BUTTON_DIM);
    outerPanel.setLayout(GBL);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setBorder(null);
    mainSplitPane.setDividerSize(5);
    mainSplitPane.setOneTouchExpandable(false);

    /*clearButton.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    clearButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            });
    clearButton.addActionListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                    clearButton_mouseClicked( e );
                }
            });

    toggleButton.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    toggleButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            });
    toggleButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                    toggleButton_mouseClicked( e );
                }
            });
    addButton.addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e){
                    addButtonFocusGained();
                }
                public void focusLost(FocusEvent e){ }
            });
    addButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                   // System.out.println(C + ": addButton(): Mouse Clicked: ");
                    addButton_mouseClicked( e );
                }
            });*/
    jCumAxisScale.setFont(new java.awt.Font("Dialog", 1, 12));
    jCumAxisScale.setForeground(new Color(80, 80, 133));
    jCumAxisScale.setToolTipText("");
    jCumAxisScale.setText("Set Cum Axis Scale:");
    jMoRate.setFont(new java.awt.Font("Dialog", 1, 12));
    jMoRate.setForeground(new Color(80, 80, 133));
    jMoRate.setText("Set Moment Axis Scale:");
    parametersPanel.setLayout(GBL);
    outerControlPanel.setLayout(GBL);
    parametersSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    parametersSplitPane.setBorder(null);
    parametersSplitPane.setDividerSize(5);
    parametersSplitPane.setOneTouchExpandable(false);
    controlPanel.setLayout(GBL);
    controlPanel.setBorder(BorderFactory.createEtchedBorder(1));
    dataScrollPane.setBorder(BorderFactory.createEtchedBorder());
    pointsTextArea.setBorder(BorderFactory.createEtchedBorder());
    pointsTextArea.setText(NO_PLOT_MSG);
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    titleLabel.setFont(new java.awt.Font( "Dialog", 1, 16 ));
    plotPanel.setLayout(GBL);
    titlePanel.setLayout(GBL);
    titlePanel.setBorder( bottomBorder );
    innerPlotPanel.setLayout(GBL);
    innerPlotPanel.setBorder(null);
    cumComboBox.setBackground(new Color(200, 200, 230));
    cumComboBox.setForeground(new Color(80, 80, 133));
    cumComboBox.setMaximumSize(new Dimension(105, 19));
    cumComboBox.setMinimumSize(new Dimension(105, 19));
    cumComboBox.setPreferredSize(new Dimension(105, 19));
    moRateComboBox.setBackground(new Color(200, 200, 230));
    moRateComboBox.setForeground(new Color(80, 80, 133));
    moRateComboBox.setMaximumSize(new Dimension(105, 19));
    moRateComboBox.setMinimumSize(new Dimension(105, 19));
    moRateComboBox.setPreferredSize(new Dimension(105, 19));
    this.getContentPane().setBackground(Color.white);
    outerPanel.setBackground(Color.white);
    mainPanel.setBackground(Color.white);
    buttonPanel.setBackground(Color.white);
    buttonPanel.setBorder( topBorder );
    mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 4, 4, 4), 0, 0));
    mainPanel.add(buttonPanel,         new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
    this.getContentPane().add(outerPanel,      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(9, 9, 9, 9), 109, 399));
    outerPanel.add(mainPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 5, 5, 5, 5 ), 0, 0 ));
    buttonPanel.add(toggleButton,                new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 8, 0, 0), 0, 0));
    buttonPanel.add(clearButton,             new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(plotColorCheckBox,                    new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(magDistLabel,                                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(cumComboBox,        new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    buttonPanel.add(jCumAxisScale,         new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 9, 0, 0), 0, 0));
    buttonPanel.add(addButton,                     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 12, 0));
    buttonPanel.add(jIncrAxisScale,     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    buttonPanel.add(moRateComboBox,  new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 0, 4), 0, 0));
    buttonPanel.add(jCheckylog,   new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
    buttonPanel.add(jMoRate,  new GridBagConstraints(4, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    buttonPanel.add(magDistComboBox,         new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), -5, 0));
    buttonPanel.add(incrComboBox,       new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));


    parametersSplitPane.setBottomComponent( sheetPanel );
    parametersSplitPane.setTopComponent( inputPanel );
    parametersSplitPane.setDividerLocation(240);


    mainSplitPane.setBottomComponent( outerControlPanel );
    mainSplitPane.setTopComponent( plotPanel );
    mainSplitPane.setDividerLocation(580);



    outerControlPanel.add(controlPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 5, 0, 0 ), 0, 0 ));



    controlPanel.add(parametersPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));



    parametersPanel.add(parametersSplitPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    dataScrollPane.getViewport().add(pointsTextArea, null);

    sheetPanel.setLayout(GBL);

    inputPanel.setLayout(GBL);

    plotPanel.add(titlePanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 4, 4, 2, 4 ), 0, 0 ));
    plotPanel.add(innerPlotPanel, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    titlePanel.add(titleLabel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, emptyInsets, 0, 0 ));


    titlePanel.setBackground( background );
    plotPanel.setBackground( background );
    innerPlotPanel.setBackground( background );
    controlPanel.setBackground( background );
    outerControlPanel.setBackground( background );
    parametersPanel.setBackground( background );
    inputPanel.setBackground( background );
    sheetPanel.setBackground( background );

  }


    /**
     *  Sets the frame attribute of the IMRTesterApplet object
     *
     * @param  newFrame  The new frame value
     */
    public void setFrame( JFrame newFrame ) {
        frame = newFrame;
    }



     /**
      *  Main method
      *
      * @param  args  The command line arguments
      */
     public static void main( String[] args ) {

         MagFreqDistTesterApplet applet = new MagFreqDistTesterApplet();

         Color c = new Color( .9f, .9f, 1.0f, 1f );
         Font f = new Font( "Dialog", Font.PLAIN, 11 );

         UIManager.put( "ScrollBar.width", new Integer( 12 ) );
         UIManager.put( "ScrollPane.width", new Integer( 12 ) );

         UIManager.put( "PopupMenu.font", f );
         UIManager.put( "Menu.font", f );
         UIManager.put( "MenuItem.font", f );

         UIManager.put( "ScrollBar.border", BorderFactory.createEtchedBorder( 1 ) );

         UIManager.put( "PopupMenu.background", c );

         //UIManager.put("PopupMenu.selectionBackground", c );
         //UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.red, 1 ) );

         UIManager.put( "Menu.background", c );
         //UIManager.put("Menu.selectionBackground", c );

         UIManager.put( "MenuItem.background", c );
         UIManager.put( "MenuItem.disabledBackground", c );
         //UIManager.put("MenuItem.selectionBackground", c );

         // UIManager.put("MenuItem.borderPainted", new Boolean(false) );
         UIManager.put( "MenuItem.margin", new Insets( 0, 0, 0, 0 ) );

         UIManager.put( "ComboBox.background", c );
         //UIManager.put("ComboBox.selectionBackground", new Color(220, 230, 170));


         applet.isStandalone = true;
         JFrame frame = new JFrame();
         //EXIT_ON_CLOSE == 3
         frame.setDefaultCloseOperation( 3 );

         frame.getContentPane().add( applet, BorderLayout.CENTER );

         applet.init();
         applet.start();
         applet.setFrame( frame );

         frame.setTitle( applet.getAppletInfo() + ":  [" + applet.getCurrentMagDistName() + ']' );

         frame.setSize( W, H );
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation( ( d.width - frame.getSize().width ) / 2, ( d.height - frame.getSize().height ) / 2 );
         frame.setVisible( true );
    }




    /**
     *  THis must be called before the Mag Dist is used. This is what initializes the
     *  Mag dist
     */
    protected void initMagDistGui() {

        // starting
        String S = C + ": initMagDistGui(): ";
        if ( this.magDistNames.size() < 1 )
            throw new RuntimeException( S + "No Mag Dist specified, unable to continue" );

        boolean first = true;
        String firstMagDist = "";
        Iterator it = this.magDistNames.keySet().iterator();
        while ( it.hasNext() )
          if ( first ) {
                first = false;
                String val = it.next().toString();
                magDistComboBox.addItem( val );
                magDistComboBox.setSelectedItem( val );
                firstMagDist = val;
            }
            else
                magDistComboBox.addItem( it.next().toString() );

        incrComboBox.addItem(new String("Auto Scale"));
        incrComboBox.addItem(new String("Custom Scale"));
        cumComboBox.addItem(new String("Auto Scale"));
        cumComboBox.addItem(new String("Custom Scale"));
        moRateComboBox.addItem(new String("Auto Scale"));
        moRateComboBox.addItem(new String("Custom Scale"));
  }



   /**
     *  Description of the Method
     *
     * @param  e  Description of the Parameter
     */
    public void itemStateChanged( ItemEvent e ) {

        // Starting
        String S = C + ": itemStateChanged(): ";
        if ( D ) System.out.println( S + "Starting" );

        if ( e.getSource().equals( magDistComboBox ) )
            updateChoosenMagDist();

        if( e.getSource().equals( jCheckylog ) ){

            //String title = magDist.getGraphXYAxisTitle();

            clearPlot( false );
            inParameterChangeWarning = false;

            if( jCheckylog.isSelected() ) yLog = true;
            else yLog = false;


            if( incrFunctions != null && incrData != null && toCumFunctions!=null && toCumData!=null && toMoFunctions!=null && toMoData!=null) {
                incrData.setYLog(yLog);
                toCumData.setYLog(yLog);
                toMoData.setYLog(yLog);
                pointsTextArea.setText( currentMagDistName + ": " + MAG +" vs. "+ INCR_RATE + '\n' + incrFunctions.toString());
                pointsTextArea.append(currentMagDistName + ": " + MAG +" vs. "+ CUM_RATE + '\n' + toCumFunctions.toString());
                pointsTextArea.append(currentMagDistName + ": " + MAG +" vs. "+ MO_RATE + '\n' + toMoFunctions.toString());
                addGraphPanel();
            }
        }

        else if( e.getSource().equals( plotColorCheckBox ) ){

            if( isWhite ) {
                isWhite = false;
                if( incrPanel != null )
                    incrPanel.getChart().getPlot().setBackgroundPaint(Color.black);
                if( cumPanel != null )
                    cumPanel.getChart().getPlot().setBackgroundPaint(Color.black);
                if( moPanel != null )
                    moPanel.getChart().getPlot().setBackgroundPaint(Color.black);

            }
            else{
                isWhite = true;
                if( incrPanel != null )
                    incrPanel.getChart().getPlot().setBackgroundPaint(Color.white);
                if( cumPanel != null )
                    cumPanel.getChart().getPlot().setBackgroundPaint(Color.white);
                if( moPanel != null )
                    moPanel.getChart().getPlot().setBackgroundPaint(Color.white);
            }
        }

        // Ending
        if ( D ) System.out.println( S + "Ending" );

    }



    /**
     *  Used for synch applet with new Mag Dist choosen. Updates lables and
     *  initializes the Mag Dist if needed.
     */
    protected void updateChoosenMagDist() {

        // Starting
        String S = C + ": updateChoosenMagDist(): ";

        String choice = magDistComboBox.getSelectedItem().toString();

        if ( choice.equals( currentMagDistName ) )
            return;
        else
            currentMagDistName = choice;

        if ( D )
            System.out.println( S + "Starting: New MagDist = " + choice );

        // Clear the current traces
        clearPlot( true );

        if ( titleLabel != null ) {
            titleLabel.setText( currentMagDistName );
            titleLabel.validate();
            titleLabel.repaint();
        }

        if ( frame != null )
            frame.setTitle( this.getAppletInfo() + ": " + currentMagDistName );

        if(D) System.out.println(S+" currentMagDistName:"+ currentMagDistName);
        magDist = magDists.setMagDist( currentMagDistName, this );

        sheetPanel.removeAll();
        sheetPanel.add( magDist.getIndependentsEditor(),
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                 );

        inputPanel.removeAll();
        ParameterListEditor controlsEditor = magDist.getControlsEditor();

        if ( D )
            System.out.println( S + "Controls = " + controlsEditor.getParameterList().toString() );

        inputPanel.add( controlsEditor,
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                 );

        validate();
        repaint();

        // Ending
        if ( D )
            System.out.println( S + "Ending" );

    }

  /**
   * Gets the currentMagDistName attribute of the MagFreqDistTesterApplet object
   *
   * @return    The currentMagDistName value
   */
    public String getCurrentMagDistName() {
        return currentMagDistName;
    }


    /**
     *  Clears the plot screen of all traces
     */
    void clearPlot(boolean clearFunctions) {

        if ( D )
            System.out.println( "Clearing plot area" );

        int loc = mainSplitPane.getDividerLocation();
        int newLoc = loc;
        titleSize = titlePanel.getHeight() + 6;

        innerPlotPanel.removeAll();
        //panel = null;

        pointsTextArea.setText( NO_PLOT_MSG );
        //if( clearFunctions) functions.clear();


        if ( !titlePanel.isVisible() ) {
            titlePanel.setVisible( true );
            //newLoc = loc - titleSize;
        }

        if ( titleLabel != null ) {
            titleLabel.setText( currentMagDistName );
            titleLabel.validate();
            titleLabel.repaint();
        }


        validate();
        repaint();

        mainSplitPane.setDividerLocation( newLoc );
    }

    /**
     *  Shown when a Constraint error is thrown on a ParameterEditor
     *
     * @param  e  Description of the Parameter
     */
    public void parameterChangeFailed( ParameterChangeFailEvent e ) {

        String S = C + " : parameterChangeWarning(): ";
        if(D) System.out.println(S + "Starting");

        inParameterChangeWarning = true;

        StringBuffer b = new StringBuffer();

        ParameterAPI param = ( ParameterAPI ) e.getSource();
        ParameterConstraintAPI constraint = param.getConstraint();
        String oldValueStr = e.getOldValue().toString();
        String badValueStr = e.getBadValue().toString();
        String name = param.getName();


        b.append( "The value ");
        b.append( badValueStr );
        b.append( " is not permitted for '");
        b.append( name );
        b.append( "'.\n" );
        b.append( "Resetting to ");
        b.append( oldValueStr );
        b.append( ". The constraints are: \n");
        b.append( constraint.toString() );

        JOptionPane.showMessageDialog(
            this, b.toString(),
            "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
            );

        if(D) System.out.println(S + "Ending");

  }

  /**
     *  Function that must be implemented by all Listeners for
     *  ParameterChangeWarnEvents.
     *
     * @param  event  The Event which triggered this function call
     */
    public void parameterChangeWarning( ParameterChangeWarningEvent e ){

        String S = C + " : parameterChangeWarning(): ";
        if(D) System.out.println(S + "Starting");

        inParameterChangeWarning = true;

        StringBuffer b = new StringBuffer();

        WarningParameterAPI param = e.getWarningParameter();
        DoubleConstraint constraint = param.getWarningConstraint();
        Double min = constraint.getMin();
        Double max = constraint.getMax();
        String name = param.getName();

        b.append( "You have exceeded the recommended range\n");
        b.append( name );
        b.append( ": (" );
        b.append( min.toString() );

        b.append( " - " );
        b.append( max.toString() );
        b.append( ")\n" );
        b.append( "Click Yes to accept the new value: " );
        b.append( e.getNewValue().toString() );

        if(D) System.out.println(S + b.toString());

        int result = 0;

        if(D) System.out.println(S + "Showing Dialog");

        result = JOptionPane.showConfirmDialog( this, b.toString(),
            "Exceeded Recommended Values", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if(D) System.out.println(S + "You choose " + result);

        switch (result) {
            case JOptionPane.YES_OPTION:
                if(D) System.out.println(S + "You choose yes, changing value to " + e.getNewValue().toString() );
                param.setValueIgnoreWarning( e.getNewValue() );
                break;
            case JOptionPane.NO_OPTION:
                if(D) System.out.println(S + "You choose no, keeping value = " + e.getOldValue().toString() );
                param.setValueIgnoreWarning( e.getOldValue() );
                break;
            default:
                param.setValueIgnoreWarning( e.getOldValue() );
                if(D) System.out.println(S + "Not sure what you choose, not changing value.");
                break;
        }

        if(D) System.out.println(S + "Ending");

    }

   /**
    *  Get Applet information
    *
    * @return    The appletInfo value
    */
    public String getAppletInfo() {
        return "MagFreqDist Tester Applet";
    }


  /**
   * this function is called when "Add Dist" button is clicked
   * @param e
   */
  void addButton_actionPerformed(ActionEvent e) {
     addButton();
  }



  /**
     *  This causes the model data to be calculated and a plot trace added to
     *  the current plot
     *
     * @param  e  The feature to be added to the Button_mouseClicked attribute
     */
    protected void addButton(){

        String S = C + ": addButton(): ";
        if ( D ) System.out.println( S + "Starting" );
        if ( D ) System.out.println( S + "Controls = " + this.magDist.controlsEditor.getParameterList().toString() );

        IncrementalMagFreqDist function = magDist.getChoosenFunction();

        incrData.setYLog(yLog);
        toMoData.setYLog(yLog);
        toCumData.setYLog(yLog);
        EvenlyDiscretizedFunc cumRate=(EvenlyDiscretizedFunc)function.getCumRateDist();
        EvenlyDiscretizedFunc moRate=(EvenlyDiscretizedFunc)function.getMomentRateDist();



        /** @todo may have to be switched when different x/y axis choosen */
       // if ( !incrFunctions.isFuncAllowed(function) ) {
            incrFunctions.clear();
            //data.prepForXLog();
      //  }
       // if ( !toCumFunctions.isFuncAllowed(cumRate)) {
            toCumFunctions.clear();
            //data.prepForXLog();
       // }
       // if ( !toMoFunctions.isFuncAllowed(moRate)) {
            toMoFunctions.clear();
            //data.prepForXLog();
        //}

       // if( !incrFunctions.contains( function )){
            if ( D ) System.out.println( S + "Adding new function" );
            incrFunctions.add((EvenlyDiscretizedFunc)function);
            toCumFunctions.add(cumRate);
            toMoFunctions.add(moRate);
       /* }
        else {

            if(D) System.out.println(S + "Showing Dialog");
            if( !this.inParameterChangeWarning ){

                JOptionPane.showMessageDialog(
                    null, "This graph already exists, will not add again.",
                    "Cannot Add", JOptionPane.INFORMATION_MESSAGE
                );
            }*/


            if ( D ) System.out.println( S + "Function already exists in graph, not adding .." );
         //   return;
       // }

        //if(D) System.out.println(S + "\n\nFunction = " + functions.toString() + "\n\n");

        magDist.synchToModel();

        // Add points data to text area, people can see

        pointsTextArea.setText( currentMagDistName + ": " + MAG +" vs. "+ INCR_RATE + '\n' + incrFunctions.toString());
        pointsTextArea.append(currentMagDistName + ": " + MAG +" vs. "+ CUM_RATE + '\n' + toCumFunctions.toString());
        pointsTextArea.append(currentMagDistName + ": " + MAG +" vs. "+ MO_RATE + '\n' + toMoFunctions.toString());
        //if ( D ) System.out.println( S + "Graphing function:" + function.toString() );
        addGraphPanel();

        if ( titleLabel != null ) {
            // titleLabel.setText( currentIMRName + ": " + imr.getGraphXYAxisTitle() );
            titleLabel.setText( currentMagDistName );
            titleLabel.validate();
            titleLabel.repaint();
        }

        if ( D ) System.out.println( S + "Ending" );

    }


    /**
     *  Adds a feature to the GraphPanel attribute of the IMRTesterApplet object
     */
    protected void addGraphPanel() {

        // Starting
        String S = C + ": addGraphPanel(): ";
        if(this.jCheckylog.isSelected())
          yLog=true;
        else
          yLog=false;


        // create a default chart based on some sample data...

        // Determine which labels to add to the axis labeling
        String incrXAxisLabel = incrFunctions.getXAxisName();
        String incrYAxisLabel = incrFunctions.getYAxisName();
        String cumXAxisLabel = toCumFunctions.getXAxisName();
        String cumYAxisLabel = toCumFunctions.getYAxisName();
        String moXAxisLabel = toMoFunctions.getXAxisName();
        String moYAxisLabel = toMoFunctions.getYAxisName();

        String title = this.getCurrentMagDistName();


        // Create the x-axis - either normal or log
        com.jrefinery.chart.NumberAxis incrXAxis = null;
        com.jrefinery.chart.NumberAxis cumXAxis = null;
        com.jrefinery.chart.NumberAxis moXAxis = null;

        // create X- axis for mag vs incremental rate
        incrXAxis = new com.jrefinery.chart.HorizontalNumberAxis( incrXAxisLabel );
        incrXAxis.setAutoRangeIncludesZero( false );
        incrXAxis.setCrosshairLockedOnData( false );
        incrXAxis.setCrosshairVisible(false);

        // create X- axis for mag vs cum rate
        cumXAxis = new com.jrefinery.chart.HorizontalNumberAxis( cumXAxisLabel );
        cumXAxis.setAutoRangeIncludesZero( false );
        cumXAxis.setCrosshairLockedOnData( false );
        cumXAxis.setCrosshairVisible(false);

        // create x- axis for mag vs moment rate
        moXAxis = new com.jrefinery.chart.HorizontalNumberAxis( moXAxisLabel );
        moXAxis.setAutoRangeIncludesZero( false );
        moXAxis.setCrosshairLockedOnData( false );
        moXAxis.setCrosshairVisible(false);

        com.jrefinery.chart.NumberAxis incrYAxis = null;
        com.jrefinery.chart.NumberAxis cumYAxis = null;
        com.jrefinery.chart.NumberAxis moYAxis = null;
        if (yLog)  {
          incrYAxis = new com.jrefinery.chart.VerticalLogarithmicAxis(incrYAxisLabel);
          cumYAxis = new com.jrefinery.chart.VerticalLogarithmicAxis(cumYAxisLabel);
          moYAxis = new com.jrefinery.chart.VerticalLogarithmicAxis(moYAxisLabel);
        }
        else {
          incrYAxis = new com.jrefinery.chart.VerticalNumberAxis(incrYAxisLabel);
          cumYAxis = new com.jrefinery.chart.VerticalNumberAxis(cumYAxisLabel);
          moYAxis = new com.jrefinery.chart.VerticalNumberAxis(moYAxisLabel);

       }

       // set properties for mag vs incremental rate Y- axis
        incrYAxis.setAutoRangeIncludesZero( false );
        incrYAxis.setCrosshairLockedOnData( false );
        incrYAxis.setCrosshairVisible( false);


        // set properties for mag vs incremental rate Y- axis
        cumYAxis.setAutoRangeIncludesZero( false );
        cumYAxis.setCrosshairLockedOnData( false );
        cumYAxis.setCrosshairVisible( false);

        // set properties for mag vs incremental rate Y- axis
        moYAxis.setAutoRangeIncludesZero( false );
        moYAxis.setCrosshairLockedOnData( false );
        moYAxis.setCrosshairVisible( false);




        int type = com.jrefinery.chart.StandardXYItemRenderer.LINES;
        //if ( functions. < MIN_NUMBER_POINTS )
            //type = com.jrefinery.chart.StandardXYItemRenderer.SHAPES_AND_LINES;

        LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );
        //StandardXYItemRenderer renderer = new StandardXYItemRenderer( type, new StandardXYToolTipGenerator() );


        // build the plot
        org.scec.gui.PSHALogXYPlot incrPlot = new org.scec.gui.PSHALogXYPlot(this,incrData, incrXAxis, incrYAxis, false, yLog);
        org.scec.gui.PSHALogXYPlot cumPlot = new org.scec.gui.PSHALogXYPlot(this,toCumData, cumXAxis, cumYAxis, false, yLog);
        org.scec.gui.PSHALogXYPlot moPlot = new org.scec.gui.PSHALogXYPlot(this,toMoData, moXAxis, moYAxis, false, yLog);


        incrPlot.setBackgroundAlpha( .8f );
        cumPlot.setBackgroundAlpha( .8f );
        moPlot.setBackgroundAlpha( .8f );

        if( isWhite ) {
          incrPlot.setBackgroundPaint( Color.white );
          cumPlot.setBackgroundPaint( Color.white );
          moPlot.setBackgroundPaint( Color.white );
        }
        else {
          incrPlot.setBackgroundPaint( Color.black );
          cumPlot.setBackgroundPaint( Color.black );
          moPlot.setBackgroundPaint( Color.black );
        }


        incrPlot.setXYItemRenderer( renderer );
        cumPlot.setXYItemRenderer( renderer );
        moPlot.setXYItemRenderer( renderer );


        JFreeChart incrChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, incrPlot, true );
        JFreeChart cumChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, cumPlot, true );
        JFreeChart moChart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, moPlot, true );

        incrChart.setBackgroundPaint( lightBlue );
        cumChart.setBackgroundPaint( lightBlue );
        moChart.setBackgroundPaint( lightBlue );

        // chart.setBackgroundImage(image);
        // chart.setBackgroundImageAlpha(.3f);

        // Put into a panel
        incrPanel = new ChartPanel(incrChart, true, true, true, true, false);
        cumPanel = new ChartPanel(cumChart, true, true, true, true, false);
        moPanel = new ChartPanel(moChart, true, true, true, true, false);
        //panel.setMouseZoomable(true);


        // set panel properties for mag vs incremental rate chart
        incrPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        incrPanel.setMouseZoomable(true);
        incrPanel.setGenerateToolTips(true);
        incrPanel.setHorizontalAxisTrace(false);
        incrPanel.setVerticalAxisTrace(false);

        // set panel properties for mag vs cumulative rate chart
        cumPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        cumPanel.setMouseZoomable(true);
        cumPanel.setGenerateToolTips(true);
        cumPanel.setHorizontalAxisTrace(false);
        cumPanel.setVerticalAxisTrace(false);

       // set panel properties for mag vs moment rate chart
        moPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
        moPanel.setMouseZoomable(true);
        moPanel.setGenerateToolTips(true);
        moPanel.setHorizontalAxisTrace(false);
        moPanel.setVerticalAxisTrace(false);



        if ( D ) System.out.println( S + "Toggling plot on" );
        graphOn = false;
        togglePlot();
        if ( D ) System.out.println( S + "Done" );
     }

    /**
   * This function handles the Zero values in the X and Y data set when exception is thrown,
   * it reverts back to the linear scale displaying a message box to the user.
   */
  public void invalidLogPlot(String message) {

     int xCenter=getAppletXAxisCenterCoor();
     int yCenter=getAppletYAxisCenterCoor();
     if(message.equals("Log Value of the negative values and 0 does not exist for Y-Log Plot")) {
       this.jCheckylog.setSelected(false);
       ShowMessage showMessage=new ShowMessage("      Y-Log Plot Error as it contains Zero Values");
       showMessage.setBounds(xCenter-60,yCenter-50,375,148);
       showMessage.pack();
       showMessage.show();
     }
  }

  /**
   * gets the Applets X-axis center coordinates
   * @return
   */
  private int getAppletXAxisCenterCoor() {
    return (this.getX()+this.getWidth())/2;
  }

  /**
   * gets the Applets Y-axis center coordinates
   * @return
   */
  private int getAppletYAxisCenterCoor() {
    return (this.getY() + this.getHeight())/2;
  }


   /**
     *  Description of the Method
     */
    protected void togglePlot() {

        // Starting
        String S = C + ": togglePlot(): ";

        innerPlotPanel.removeAll();

        int loc = mainSplitPane.getDividerLocation();
        titleSize = titlePanel.getHeight() + 6;

        int newLoc = loc;
        if ( graphOn ) {
            if ( D )
                System.out.println( S + "Showing Data" );
            toggleButton.setText( "Show Plot" );
            graphOn = false;

            if ( !titlePanel.isVisible() ) {
                titlePanel.setVisible( true );
                // newLoc = loc - titleSize;
            }

            // dataScrollPane.setVisible(true);
            // innerPlotPanel.setBorder(oval);
            innerPlotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
        }
        else {
            if ( D )
                System.out.println( S + "About to show Plot" );
            graphOn = true;
            // dataScrollPane.setVisible(false);
            toggleButton.setText( "Show Data" );
            if ( incrPanel != null ) {
                if ( D )
                    System.out.println( S + "Showing Plot" );


                if ( titlePanel.isVisible() ) {
                    titlePanel.setVisible( false );
                    //newLoc = loc + titleSize;
                }

                // panel for mag vs incremental-rate graph
                innerPlotPanel.add( incrPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

                // panel for mag vs cumulative-rate graph
                innerPlotPanel.add( cumPanel, new GridBagConstraints( 1, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

                // panel for mag vs moment-rate graph
               // innerPlotPanel.add( moPanel, new GridBagConstraints( 1, 0, 1, 1, 1.0, 1.0
               //        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

            }
            else {
                if ( D )
                    System.out.println( S + "No Plot - So Showing Data" );


                if ( !titlePanel.isVisible() ) {
                    titlePanel.setVisible( true );
                    // newLoc = loc - titleSize;
                }

                // innerPlotPanel.setBorder(oval);
                innerPlotPanel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                        , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
            }

        }

        if ( D ) System.out.println( S + "Calling validate and repaint" );
        mainSplitPane.setDividerLocation( newLoc );
        validate();
        repaint();

        if ( D ) System.out.println( S + "Loc = " + loc + '\t' + "New Loc = " + newLoc );
        if ( D ) System.out.println( S + "Ending" );

    }


   /**
     *  Clears the plot screen of all traces, then sychs imr to model
     *
     * @param  e  Description of the Parameter
     */
    void clearButton_actionPerformed( ActionEvent e ) {
        clearButton();
    }

    void clearButton(){
        clearPlot( true );
        magDist.synchToModel();
    }

    /**
     * This function is called when show data button is clicked
     * @param e
     */
  void toggleButton_actionPerformed(ActionEvent e) {
      togglePlot();
  }
}