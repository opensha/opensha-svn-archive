package org.scec.sha.earthquake.rupForecastImpl.groupTest;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.data.function.*;
import org.scec.gui.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.magdist.gui.MagFreqDistTesterAPI;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class GroupTestApplet extends Applet
    implements  ParameterChangeWarningListener,
    ParameterChangeFailListener,
    MagFreqDistTesterAPI {

  /**
   * Name of the class
   */
  protected final static String C = "GroupTestApplet";
  // for debug purpose
  protected final static boolean D = true;


  private boolean isStandalone = false;
  private JSplitPane chartSplit = new JSplitPane();
  private JPanel chartPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private Border border1;
  private JSplitPane controlsSplit = new JSplitPane();
  private JSplitPane magDistSplit = new JSplitPane();

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

  // height and width of the applet
  protected final static int W = 915;
  protected final static int H = 625;


  // make the GroupTestGUIBean instance
  GroupTestGuiBean groupTestBean;
  private JSplitPane imtSplitPane = new JSplitPane();
  private JSplitPane siteSplitPane = new JSplitPane();
  private JPanel testCasesPanel = new JPanel();
  private JPanel imtPanel = new JPanel();
  private JPanel imrPanel = new JPanel();
  private JPanel sitePanel = new JPanel();
  private JSplitPane sourceSplitPane = new JSplitPane();
  private JSplitPane timespanSplitPane = new JSplitPane();
  private JPanel sourcePanel = new JPanel();
  private JPanel magDistControlPanel = new JPanel();
  private JPanel magDistIndependentPanel = new JPanel();
  private JPanel timespanPanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout5 = new GridBagLayout();
  private GridBagLayout gridBagLayout6 = new GridBagLayout();
  private GridBagLayout gridBagLayout7 = new GridBagLayout();
  private GridBagLayout gridBagLayout8 = new GridBagLayout();
  private GridBagLayout gridBagLayout9 = new GridBagLayout();
  private GridBagLayout gridBagLayout10 = new GridBagLayout();
  private GridBagLayout gridBagLayout11 = new GridBagLayout();
  JButton addButton = new JButton();
  JButton clearButton = new JButton();
  JButton dataButton = new JButton();
  JCheckBox jCheckBox1 = new JCheckBox();
  JCheckBox jCheckBox2 = new JCheckBox();
  private GridBagLayout gridBagLayout12 = new GridBagLayout();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
        (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public GroupTestApplet() {

  }
  //Initialize the applet
  public void init() {
    try {
      // make the GroupTestGuiBean
      groupTestBean = new GroupTestGuiBean(this);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  //Component initialization
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    this.setSize(new Dimension(925, 590));
    this.setLayout(null);
    chartSplit.setBounds(new Rectangle(18, 9, 727, 514));
    buttonPanel.setBorder(border1);
    buttonPanel.setBounds(new Rectangle(19, 536, 882, 44));
    buttonPanel.setLayout(gridBagLayout12);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setBounds(new Rectangle(753, 9, 167, 513));
    imtSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    siteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    sourceSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    timespanSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    testCasesPanel.setLayout(gridBagLayout1);
    imtPanel.setLayout(gridBagLayout2);
    imrPanel.setLayout(gridBagLayout3);
    sourcePanel.setLayout(gridBagLayout5);
    timespanPanel.setLayout(gridBagLayout8);
    sitePanel.setLayout(gridBagLayout9);
    addButton.setText("Add Graph");
    clearButton.setText("Clear Plot");
    dataButton.setText("Show Button");
    jCheckBox1.setText("X Log");
    jCheckBox2.setText("Y Log");
    buttonPanel.add(dataButton,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), -9, -2));
    buttonPanel.add(clearButton,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 9, 10, 0), 9, -2));
    this.add(chartSplit, null);
    chartSplit.add(chartPanel, JSplitPane.TOP);
    chartSplit.add(controlsSplit, JSplitPane.BOTTOM);
    controlsSplit.add(imtSplitPane, JSplitPane.TOP);
    this.add(buttonPanel, null);
    buttonPanel.add(addButton,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 25, 10, 0), 3, -2));
    this.add(magDistSplit, null);
    magDistSplit.add(sourceSplitPane, JSplitPane.TOP);
    magDistSplit.add(timespanSplitPane, JSplitPane.BOTTOM);
    controlsSplit.add(siteSplitPane, JSplitPane.BOTTOM);
    siteSplitPane.add(imrPanel, JSplitPane.TOP);
    siteSplitPane.add(sitePanel, JSplitPane.BOTTOM);
    imtSplitPane.add(testCasesPanel, JSplitPane.TOP);
    imtSplitPane.add(imtPanel, JSplitPane.BOTTOM);
    sourceSplitPane.add(sourcePanel, JSplitPane.TOP);
    sourceSplitPane.add(magDistControlPanel, JSplitPane.BOTTOM);
    timespanSplitPane.add(magDistIndependentPanel, JSplitPane.TOP);
    timespanSplitPane.add(timespanPanel, JSplitPane.BOTTOM);
    buttonPanel.add(jCheckBox1,  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 20, 10, 0), 20, 2));
    buttonPanel.add(jCheckBox2,  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 0, 10, 337), 22, 2));
    chartSplit.setDividerLocation(525);
    controlsSplit.setDividerLocation(240);
    magDistSplit.setDividerLocation(300);

    imtSplitPane.setDividerLocation(85);
    siteSplitPane.setDividerLocation(120);
    sourceSplitPane.setDividerLocation(125);
    timespanSplitPane.setDividerLocation(130);

    updateChoosenTestCase();
    updateChoosenIMT();
    updateChoosenIMR();
    updateChoosenEqkSource();
    updateChoosenTimespan();
    updateChoosenMagDist();
  }
  //Start the applet
  public void start() {
  }
  //Stop the applet
  public void stop() {
  }
  //Destroy the applet
  public void destroy() {
  }
  //Get Applet information
  public String getAppletInfo() {
    return "Applet Information";
  }
  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }
  //Main method
  public static void main(String[] args) {
    GroupTestApplet applet = new GroupTestApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Peer Group Tests");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(W,H);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }

  //static initializer for setting look & feel
  static {
    try {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    catch(Exception e) {
    }
  }

  /**
   *  This is the main function of this interface. Any time a control
   *  paramater or independent paramater is changed by the user in a GUI this
   *  function is called, and a paramater change event is passed in. This
   *  function then determines what to do with the information ie. show some
   *  paramaters, set some as invisible, basically control the paramater
   *  lists.
   *
   * @param  event
   */
  public void parameterChange( ParameterChangeEvent event ) {

    String S = C + ": parameterChange(): ";
    if ( D )
      System.out.println( "\n" + S + "starting: " );
    String name1 = event.getParameterName();
  }


  /**
   *  Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {

    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");



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



    StringBuffer b = new StringBuffer();

    WarningParameterAPI param = e.getWarningParameter();


    try{
      Double min = param.getWarningMin();
      Double max = param.getWarningMax();

      String name = param.getName();

      b.append( "You have exceeded the recommended range\n");
      b.append( name );
      b.append( ": (" );
      b.append( min.toString() );

      b.append( " to " );
      b.append( max.toString() );
      b.append( ")\n" );
      b.append( "Click Yes to accept the new value: " );
      b.append( e.getNewValue().toString() );
    }
    catch( Exception ee){

      String name = param.getName();

      b.append( "You have exceeded the recommended range for: \n");
      b.append( name + '\n' );
      b.append( "Click Yes to accept the new value: " );
      b.append( e.getNewValue().toString() );
      b.append( name );


    }
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
   *  update the GUI with the timespan
   */
  public void updateChoosenTimespan() {
    timespanPanel.removeAll();
    timespanPanel.add(groupTestBean.getTimespanEditor(),
                      new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                      GridBagConstraints.CENTER,
                      GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   *  update the GUI with the test case choosen
   */
  public void updateChoosenTestCase() {
    testCasesPanel.removeAll();
    testCasesPanel.add(groupTestBean.getTestCasesEditor(),
                       new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                       GridBagConstraints.CENTER,
                       GridBagConstraints.BOTH, defaultInsets, 0, 0 ));

  }

  /**
   *  update the GUI with the IMT choosen
   */
  public void updateChoosenIMT() {
    imtPanel.removeAll();
    imtPanel.add(groupTestBean.getIMTEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imtPanel.validate();
    imtPanel.repaint();
  }

  /**
   *  update the GUI with the IMR choosen
   *  refresh the sites params as well
   */
  public void updateChoosenIMR() {
    // update the IMR panel
    imrPanel.removeAll();
    imrPanel.add(groupTestBean.getImrEditor(),
                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                 GridBagConstraints.CENTER,
                 GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    // update the sites
    sitePanel.removeAll();
    sitePanel.add(groupTestBean.getSiteEditor(),
                  new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                  GridBagConstraints.CENTER,
                  GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    imrPanel.validate();
    imrPanel.repaint();
    sitePanel.validate();
    sitePanel.repaint();
  }

  /**
   *  update the GUI with the choosen Eqk source
   */
  public void updateChoosenEqkSource() {
    // update the EqkSource panel
    this.sourcePanel.removeAll();
    sourcePanel.add(groupTestBean.getEqkSourceEditor(),
                    new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
                    GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
    sourcePanel.validate();
    sourcePanel.repaint();
  }


  /**
   *  Used for synch applet with new Mag Dist choosen. Updates lables and
   *  initializes the Mag Dist if needed.
   */
  public void updateChoosenMagDist() {

    // Starting
    String S = C + ": updateChoosenMagDist(): ";

    System.out.println(S);
    // add the mag dist control editor
    this.magDistControlPanel.removeAll();
    magDistControlPanel.setLayout(gridBagLayout10);
    magDistIndependentPanel.setLayout(gridBagLayout11);
    magDistControlPanel.add( groupTestBean.getMagDistControlsEditor(),
                             new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                             , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                             );

    // add the mag dist independent editor
    this.magDistIndependentPanel.removeAll();
    magDistIndependentPanel.add( groupTestBean.getMagDistIndependentsEditor(),
                                 new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                                 , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                                 );

    magDistIndependentPanel.validate();
    magDistIndependentPanel.repaint();
    magDistControlPanel.validate();
    magDistControlPanel.repaint();
    // Ending
    if ( D )
      System.out.println( S + "Ending" );

  }

}