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


import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;


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
  //private ChartPanel panel = new ChartPanel(null);

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

  public MagFreqDistTesterApplet() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
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
    //plotColorCheckBox.addItemListener(this);
    clearButton.setBackground(new Color(200, 200, 230));
    clearButton.setFont(BUTTON_FONT);
    clearButton.setForeground(new Color(80, 80, 133));
    clearButton.setBorder(BorderFactory.createRaisedBevelBorder());
    clearButton.setFocusPainted(false);
    clearButton.setText("Clear Plot");
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
    //jCheckylog.addItemListener(this);
    toggleButton.setBackground(new Color(200, 200, 230));
    toggleButton.setFont(BUTTON_FONT);
    toggleButton.setForeground(new Color(80, 80, 133));
    toggleButton.setBorder(BorderFactory.createRaisedBevelBorder());
    toggleButton.setFocusPainted(false);
    toggleButton.setText("Show Data");
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
    clearButton.addMouseListener(new java.awt.event.MouseAdapter() {
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
    parametersSplitPane.setDividerLocation(180 );


    mainSplitPane.setBottomComponent( outerControlPanel );
    mainSplitPane.setTopComponent( plotPanel );
    mainSplitPane.setDividerLocation(580);



    outerControlPanel.add(controlPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 5, 0, 0 ), 0, 0 ));



    controlPanel.add(parametersPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));



    parametersPanel.add(parametersSplitPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    dataScrollPane.add(pointsTextArea, null);

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

}