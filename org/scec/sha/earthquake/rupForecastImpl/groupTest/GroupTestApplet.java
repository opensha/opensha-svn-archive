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
  protected final static boolean D = false;



  private boolean isStandalone = false;
  private JSplitPane chartSplit = new JSplitPane();
  private JPanel chartPanel = new JPanel();
  private JPanel buttonPanel = new JPanel();
  private Border border1;
  private JSplitPane controlsSplit = new JSplitPane();
  private JSplitPane magDistSplit = new JSplitPane();
  private JPanel controlPanel = new JPanel();
  private JPanel sitePanel = new JPanel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private JPanel magDistControlPanel = new JPanel();
  private JPanel magDistIndependentPanel = new JPanel();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();

  // default insets
  Insets defaultInsets = new Insets( 4, 4, 4, 4 );

 // height and width of the applet
  protected final static int W = 915;
  protected final static int H = 670;


  // make the GroupTestGUIBean instance
  GroupTestGuiBean groupTestBean;

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
    this.setSize(new Dimension(925, 644));
    this.setLayout(null);
    chartSplit.setBounds(new Rectangle(18, 9, 727, 514));
    buttonPanel.setBorder(border1);
    buttonPanel.setBounds(new Rectangle(19, 536, 882, 79));
    buttonPanel.setLayout(null);
    controlsSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    magDistSplit.setBounds(new Rectangle(753, 9, 167, 513));
    controlPanel.setLayout(gridBagLayout1);
    sitePanel.setLayout(gridBagLayout2);
    magDistControlPanel.setLayout(gridBagLayout3);
    magDistIndependentPanel.setLayout(gridBagLayout4);
    this.add(chartSplit, null);
    chartSplit.add(chartPanel, JSplitPane.TOP);
    chartSplit.add(controlsSplit, JSplitPane.BOTTOM);
    controlsSplit.add(controlPanel, JSplitPane.TOP);
    controlsSplit.add(sitePanel, JSplitPane.BOTTOM);
    this.add(buttonPanel, null);
    this.add(magDistSplit, null);
    magDistSplit.add(magDistControlPanel, JSplitPane.TOP);
    magDistSplit.add(magDistIndependentPanel, JSplitPane.BOTTOM);
    chartSplit.setDividerLocation(525);
    controlsSplit.setDividerLocation(250);
    magDistSplit.setDividerLocation(250);

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
    frame.setTitle("Applet Frame");
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
     *  Used for synch applet with new Mag Dist choosen. Updates lables and
     *  initializes the Mag Dist if needed.
     */
  public void updateChoosenMagDist() {

        // Starting
        String S = C + ": updateChoosenMagDist(): ";

        // add the control editor
        controlPanel.removeAll();
        this.controlPanel.add(groupTestBean.getControlsEditor(),
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                 );

       // add the site editor
        this.sitePanel.removeAll();
        sitePanel.add( groupTestBean.getSiteEditor(),
                new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 )
                 );

        // add the mag dist control editor
        this.magDistControlPanel.removeAll();
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

        validate();
        repaint();

        // Ending
        if ( D )
            System.out.println( S + "Ending" );

    }

}