package org.scec.sha.earthquake.demo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;
import javax.swing.border.*;
import java.io.*;



import com.jrefinery.chart.*;
import com.jrefinery.chart.axis.*;
import com.jrefinery.chart.renderer.*;
import com.jrefinery.chart.tooltips.*;
import com.jrefinery.data.*;

import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.imr.attenRelImpl.gui.AttenuationRelationshipGuiBean;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.earthquake.*;
import org.scec.data.Site;
import org.scec.data.Location;
import org.scec.data.function.*;
import org.scec.gui.plot.jfreechart.*;
import org.scec.exceptions.ParameterException;

import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast;
import org.scec.sha.earthquake.rupForecastImpl.WardTest.WardGridTestEqkRupForecast;

/**
 * <p>Title: EqkForecastApplet</p>
 * <p>Description: Earthquake forecast Demo Applet</p>
 *
 * @author :Nitin Gupta & Vipin Gupta
 * @version 1.0
 */

public class EqkForecastApplet extends JApplet
    implements  ParameterChangeWarningListener,
                ParameterChangeFailListener,
                ParameterChangeListener {
  private boolean isStandalone = false;

  private String C = "EqkForecastApplet";
  private boolean D = true ; // flag only used for debug purposes

  private Insets plotInsets = new Insets( 4, 10, 4, 4 );

  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String BJF_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.BJF_1997_AttenRel";
  protected final static String AS_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.AS_1997_AttenRel";
  protected final static String C_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Campbell_1997_AttenRel";
  protected final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.SCEMY_1997_AttenRel";
  protected final static String F_CLASS_NAME = "org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel";


  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String BJF_NAME = "Boore, Joyner, & Fumal (1997)";
  protected final static String AS_NAME = "Abrahamson & Silva (1997)";
  protected final static String C_NAME = "Cambell (1997) w/ erratum (2000) changes";
  protected final static String SCEMY_NAME = "Sadigh et al. (1997)";
  protected final static String F_NAME = "Field (2000)";

  protected final static String SA = "SA";
  protected final static String SA_PERIOD = "SA Period";

  // title for site paramter panel
  protected final static String SITE_PARAMS = "Site Params";
  protected final static String VS30_STRING = "Vs30";
  protected final static String BASIN_DEPTH_STRING = "Basin Depth (Phase III)";

  /**
   * forecast models supported by this model
   */
  protected final static String FRANKEL_1996_FORECAST = "Frankel 1996";
  protected final static String WARD_GRID_TEST = "Ward Grid Test";


  protected final static String LONGITUDE = "Longitude";
  protected final static String LATITUDE = "Latitude";
  protected final static int W = 915;
  protected final static int H = 725;
  private final static String NO_PLOT_MSG = "No Plot Data Available";

  // maximum permitted distance between fault and site to consider source in hazard analysis for that site
  protected final double MAX_DISTANCE = 200;

  /**
   * for Y-log, 0 values will be converted to this small value
   */
  private double Y_MIN_VAL = 1e-8;

  private String Y_AXIS_NAME = "Exceed Probability";

  // it maps the IMR names and supported IMT for each IMR
  protected ArrayList[] imtMap;

  //it maps the IMR names and site supported by each IMR
  protected ArrayList[] siteMap;

  final static GridBagLayout GBL = new GridBagLayout();

  Color background = new Color(200,200,230);
  Color foreground = new Color(80,80,133);

  protected ChartPanel chartPanel;
  JTextArea pointsTextArea = new JTextArea();

  /**
   * adding scroll pane for showing data
   */
  JScrollPane dataScrollPane = new JScrollPane();

  /**
   *  Hashmap that maps picklist imr string names to the real fully qualified
   *  class names
   */
  protected static HashMap imrNames = new HashMap();
  private JPanel jEqkForecastPanel = new JPanel();
  protected Vector imrObject = new Vector();

  // combobox to show all the IMTs supported
  private JComboBox jIMTComboBox = new JComboBox();
  private JLabel jIMTLabel = new JLabel();

  // combo box to show the supported Earthforecast Types

  // text field where time span(in yrs.) is filled up
  private Border border1;
  private JLabel jIMR = new JLabel();

  // array of checkboxes. One check box for each supported IMR
  private JCheckBox[] jIMRNum;

  // name of current IMR in processing
  private String currIMR_Name;


  protected boolean graphOn = false;
  /**
   *  This is the paramater list editor. The site parameters will be made
   *  removed . This is done through this editor.
   */
  protected ParameterListEditor siteEditor = null;

   /**
    *  Parameters for site related to each IMR
    */
  protected ParameterList siteParamList = new ParameterList();

  /**
   * Longitude and Latitude paramerts to be added to the site params list
   */
  private DoubleParameter longitude = new DoubleParameter(LONGITUDE, new Double(-118), new Double(-114), new Double(-118));
  private DoubleParameter latitude = new DoubleParameter(LATITUDE, new Double(32.0), new Double(36.0), new Double(34.0));

  /**
   * FunctionList declared
   */
  DiscretizedFuncList totalProbFuncs = new DiscretizedFuncList();
  DiscretizedFunctionXYDataSet totalData = new DiscretizedFunctionXYDataSet();

  ArbitrarilyDiscretizedFunc[] hazFunction;

  private String TITLE = new String("Seismic Hazard Analysis");

  /**
   * objects of diffrent earthquake forecasts
   */
  Frankel96_EqkRupForecast frankelRupForecast;
  WardGridTestEqkRupForecast wardRupForecast;

  // generic forecast API
  EqkRupForecast eqkRupForecast ;

  // whther Add Graph button is clicked or not
  private boolean buttonClicked =  false;

  Color lightBlue = new Color( 200, 200, 230 );

  /**
   *  NED - Here is where you can add the new IMRS, follow my examples below
   *  Populates the imrs hashmap with the strings in the picklist for the
   *  applet mapped to the class names of the imrs. This will use the class
   *  loader to load these
   */
  static {
      if ( imrNames == null ) imrNames = new HashMap();
      imrNames.clear();
      imrNames.put( BJF_CLASS_NAME, BJF_NAME );
      imrNames.put( AS_CLASS_NAME, AS_NAME );
      imrNames.put( C_CLASS_NAME, C_NAME );
      imrNames.put( SCEMY_CLASS_NAME, SCEMY_NAME );
      imrNames.put( F_CLASS_NAME, F_NAME );

      try { UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() ); }
      catch ( Exception e ) {
         e.printStackTrace();
      }
  }

  protected javax.swing.JFrame frame;
  private JPanel jIMRList = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private Border border2;
  private JPanel sitePanel = new JPanel();
  private Border border3;
  private JPanel panel  = new JPanel();
  private Border border4;
  private JLabel jTimeSpan = new JLabel();
  private JTextField jTimeField = new JTextField();
  private JLabel jYears = new JLabel();
  private JLabel jForecastLabel = new JLabel();
  private JComboBox jEqkForeType = new JComboBox();
  private JCheckBox jCheckCVM = new JCheckBox();
  private JButton jBCalc = new JButton();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  JCheckBox jCheckBasin = new JCheckBox();
  private JButton jToggleButton = new JButton();

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public EqkForecastApplet() {

    totalData.setFunctions(totalProbFuncs);
    totalData.setConvertZeroToMin(true,Y_MIN_VAL);
    hazFunction = new ArbitrarilyDiscretizedFunc[imrNames.size()];
    longitude.addParameterChangeFailListener(this);
    latitude.addParameterChangeFailListener(this);

    // set Y axis label
    totalProbFuncs.setYAxisName(Y_AXIS_NAME);
  }
  //Initialize the applet
  public void init() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    initEqkForecastGui();

  }
  //Component initialization
  private void jbInit() throws Exception {
    jBCalc.setBackground(new Color(200, 200, 230));
    jBCalc.setBounds(new Rectangle(635, 630, 122, 33));
    jBCalc.setFont(new java.awt.Font("Dialog", 1, 11));
    jBCalc.setForeground(new Color(80, 80, 133));
    jBCalc.setText("Add Graph");
    jBCalc.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jBCalc_actionPerformed(e);
      }
    });
    jCheckCVM.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckCVM_actionPerformed(e);
      }
    });
    panel.setLayout(gridBagLayout2);
    jCheckBasin.setBounds(new Rectangle(638, 540, 233, 22));
    jCheckBasin.setText("Set Basin Depth from SCEC CVM");
    jCheckBasin.setForeground(new Color(80, 80, 133));
    jCheckBasin.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckBasin.setBackground(Color.white);
    jCheckBasin.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jCheckBasin_actionPerformed(e);
      }
    });
    jToggleButton.setBackground(new Color(200, 200, 230));
    jToggleButton.setBounds(new Rectangle(763, 629, 103, 34));
    jToggleButton.setFont(new java.awt.Font("Dialog", 1, 11));
    jToggleButton.setForeground(new Color(80, 80, 133));
    jToggleButton.setMaximumSize(new Dimension(83, 37));
    jToggleButton.setMinimumSize(new Dimension(83, 37));
    jToggleButton.setPreferredSize(new Dimension(83, 37));
    jToggleButton.setText("Show Data");
    jToggleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jToggleButton_actionPerformed(e);
      }
    });
    dataScrollPane.setBorder( BorderFactory.createEtchedBorder() );
    dataScrollPane.getViewport().add( pointsTextArea, null );
    jEqkForecastPanel.add(panel, null);
    jEqkForecastPanel.add(jIMR, null);
    jEqkForecastPanel.add(jIMRList, null);
    jEqkForecastPanel.add(sitePanel, null);
    jEqkForecastPanel.add(jCheckCVM, null);
    jEqkForecastPanel.add(jIMTLabel, null);
    jEqkForecastPanel.add(jIMTComboBox, null);
    jEqkForecastPanel.add(jForecastLabel, null);
    jEqkForecastPanel.add(jTimeSpan, null);
    jEqkForecastPanel.add(jTimeField, null);
    jEqkForecastPanel.add(jYears, null);
    jEqkForecastPanel.add(jEqkForeType, null);
    jEqkForecastPanel.add(jCheckBasin, null);
    jEqkForecastPanel.add(jBCalc, null);
    jEqkForecastPanel.add(jToggleButton, null);
    border1 = BorderFactory.createEtchedBorder(new Color(200, 200, 230),new Color(80, 80, 133));
    border2 = BorderFactory.createEtchedBorder(new Color(200, 200, 230),new Color(80, 80, 133));
    border3 = BorderFactory.createLineBorder(new Color(80, 80, 133),1);
    border4 = BorderFactory.createLineBorder(new Color(80, 80, 133),1);
    jIMTComboBox.setBackground(new Color(200, 200, 230));
    jIMTComboBox.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMTComboBox.setForeground(new Color(80, 80, 133));
    jIMTComboBox.setBounds(new Rectangle(702, 15, 138, 20));
    jIMTComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jIMTComboBox_actionPerformed(e);
      }
    });
    jIMR.setBackground(new Color(200, 200, 230));
    jIMR.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMR.setForeground(new Color(80, 80, 133));
    jIMR.setText("Select IMR :");
    jIMR.setBounds(new Rectangle(637, 43, 76, 19));
    jIMRList.setBackground(Color.white);
    jIMRList.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMRList.setForeground(Color.white);
    jIMRList.setBorder(border2);
    jIMRList.setBounds(new Rectangle(638, 63, 244, 178));
    jIMRList.setLayout(gridLayout1);
    gridLayout1.setColumns(1);
    gridLayout1.setRows(0);
    gridLayout1.setVgap(1);
    sitePanel.setBackground(Color.white);
    sitePanel.setFont(new java.awt.Font("Dialog", 1, 11));
    sitePanel.setForeground(new Color(200, 200, 230));
    sitePanel.setBorder(border3);
    sitePanel.setBounds(new Rectangle(640, 246, 240, 270));
    sitePanel.setLayout(borderLayout1);
    jEqkForecastPanel.setBackground(Color.white);
    jEqkForecastPanel.setForeground(new Color(80, 80, 133));
    this.getContentPane().setBackground(Color.white);
    this.setForeground(new Color(80, 80, 133));
    this.setSize(new Dimension(909, 693));
    this.getContentPane().setLayout(null);
    jEqkForecastPanel.setBorder(border1);
    jEqkForecastPanel.setBounds(new Rectangle(10, 9, 891, 671));
    jEqkForecastPanel.setLayout(null);
    jIMTLabel.setBackground(new Color(200, 200, 230));
    jIMTLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMTLabel.setForeground(new Color(80, 80, 133));
    jIMTLabel.setText("Select IMT:");
    jIMTLabel.setBounds(new Rectangle(636, 16, 68, 23));
    panel.setBackground(Color.white);
    panel.setBorder(border4);
    panel.setBounds(new Rectangle(6, 6, 625, 655));
    jTimeSpan.setBackground(new Color(200, 200, 230));
    jTimeSpan.setFont(new java.awt.Font("Dialog", 1, 11));
    jTimeSpan.setForeground(new Color(80, 80, 133));
    jTimeSpan.setText("Time Span :");
    jTimeSpan.setBounds(new Rectangle(638, 599, 66, 22));
    jTimeField.setBackground(Color.white);
    jTimeField.setFont(new java.awt.Font("SansSerif", 1, 11));
    jTimeField.setForeground(new Color(80, 80, 133));
    jTimeField.setBounds(new Rectangle(752, 598, 99, 22));
    jYears.setBackground(new Color(200, 200, 230));
    jYears.setFont(new java.awt.Font("Dialog", 1, 11));
    jYears.setForeground(new Color(80, 80, 133));
    jYears.setText("# yrs");
    jYears.setBounds(new Rectangle(857, 597, 31, 25));
    jForecastLabel.setBackground(new Color(200, 200, 230));
    jForecastLabel.setForeground(new Color(80, 80, 133));
    jForecastLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jForecastLabel.setText("Select Eqk Forecast :");
    jForecastLabel.setBounds(new Rectangle(637, 573, 112, 20));
    jEqkForeType.setBackground(new Color(200, 200, 230));
    jEqkForeType.setFont(new java.awt.Font("Dialog", 1, 11));
    jEqkForeType.setForeground(new Color(80, 80, 133));
    jEqkForeType.setBounds(new Rectangle(752, 573, 127, 21));
    jCheckCVM.setBackground(Color.white);
    jCheckCVM.setFont(new java.awt.Font("Dialog", 1, 11));
    jCheckCVM.setForeground(new Color(80, 80, 133));
    jCheckCVM.setText("Set Vs30 from SCEC CVM");
    jCheckCVM.setBounds(new Rectangle(638, 520, 215, 22));
    this.getContentPane().add(jEqkForecastPanel, null);
    pointsTextArea.setBorder( BorderFactory.createEtchedBorder() );
    pointsTextArea.setText( NO_PLOT_MSG );
    dataScrollPane.getViewport().add( pointsTextArea, null );
  }


  //Get Applet information
  public String getAppletInfo() {
    return "EqkForecastApplet";
  }
  //Get parameter info
  public String[][] getParameterInfo() {
    return null;
  }

  /**
   *  THis must be called to create IMR objects
   */
  private void initEqkForecastGui() {
   // starting
   String S = C + ": initEqkForecastGui(): ";
   if ( this.imrNames.size() < 1 )
      throw new RuntimeException( S + "No IMRs specified, unable to continue" );

   // create the object for each IMR and get all the supported IMTs for each IMR
   Iterator it = this.imrNames.keySet().iterator();
   AttenuationRelationshipAPI imr;
   String className;

   // hashmap used so that we do not get duplicate IMTs
   HashMap imt = new HashMap();

   // number of IMRs
   int imtSize =imrNames.size();

   // imtMap mantains mapping of each IMR with its supported IMT
   imtMap =new ArrayList[imtSize];

   // siteMap mantains mapping of each IMR with its supported sites
   siteMap =new ArrayList[imtSize];


   //jIMRNum is the array of check boxes. There is one check box for each IMR
   this.jIMRNum=new JCheckBox[imtSize];

   int numOfIMT=0;

   //jIMRList is the panel in which checkboxes are drawn
   jIMRList.setLayout(GBL);

   while (it.hasNext()) {

     // imtMap mantains mapping of each IMR with its supported IMT
    imtMap[numOfIMT] = new ArrayList();

    // sitemap mantains the mapping of each IMR with its supported sites
    siteMap[numOfIMT] = new ArrayList();

    className = it.next().toString(); // class Name to create the object
    imtMap[numOfIMT].add(className);
    jIMRNum[numOfIMT]=new JCheckBox((String)imrNames.get(className));
    jIMRList.add(jIMRNum[numOfIMT],new GridBagConstraints( 0, numOfIMT, 1, 1, 1.0, 1.0
                       , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );
    jIMRNum[numOfIMT].setBackground(background);
    jIMRNum[numOfIMT].setForeground(foreground);


     // this adds listener for events on the check box
     // whenever a check box is selectdee or deselected we have to change the site params
     jIMRNum[numOfIMT].addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jCheckBox_itemStateChanged(e);
      }
    });


     // create the imr instance
     imr = ( AttenuationRelationshipAPI ) createIMRClassInstance(className ,this);
     imrObject.add(imr);

     // get the list of sites supported by this IMR
     ListIterator listIt = imr.getSiteParamsIterator();

     // save the Sit Types supported by this IMR in a list
     Parameter tempParam;
     while(listIt.hasNext()){
       tempParam = (Parameter)listIt.next();
       Parameter cloneParam = (Parameter)tempParam.clone();
       cloneParam.addParameterChangeFailListener(this);

       siteMap[numOfIMT].add(cloneParam);

       if(tempParam instanceof StringParameter) {
         StringParameter strConstraint = (StringParameter)tempParam;
         tempParam.setValue(strConstraint.getAllowedStrings().get(0));
         siteMap[numOfIMT].add(tempParam);
       }
     }



     // get the supported IMTs for this IMR
     Iterator it1 = imr.getSupportedIntensityMeasuresIterator();
     while ( it1.hasNext() ) {
        DependentParameterAPI param = ( DependentParameterAPI ) it1.next();
        if(!param.getName().equalsIgnoreCase(SA)) {
          imt.put(new String(param.getName()),new String(param.getName()));
          imtMap[numOfIMT].add(param.getName());
          continue;
        }

        // get SA peiod paramter
        ListIterator it2 = param.getIndependentParametersIterator();
        while ( it2.hasNext() ) {
          DependentParameterAPI param2 = ( DependentParameterAPI ) it2.next();
          // if it is not SA Period then  get next parameter
          if(!param2.getName().equalsIgnoreCase(SA_PERIOD))
            continue;

          //if it is SA period, get the allowed period values
          DoubleDiscreteConstraint  values = (DoubleDiscreteConstraint) param2.getConstraint();
          ListIterator it3 = values.listIterator();
          while(it3.hasNext())  {// add all the periods realting to the SA
          String  temp = SA+" "+it3.next().toString();
          imt.put(new String(temp),new String(temp));
          imtMap[numOfIMT].add(temp);
        }
      }
     }
     ++numOfIMT;
   }

   // print the values in IMT hashmap
   it = imt.keySet().iterator();
   java.util.ArrayList l = new java.util.ArrayList();
 // copy the contents of hash map to array list so that we can sort it
   while(it.hasNext())
     l.add(it.next());

   // sort the list
   Collections.sort(l);
   it  = l.iterator();

   // print the list
   while(it.hasNext()) {
    // if(D) System.out.println(it.next().toString());
     // add it to the combo box as well
     this.jIMTComboBox.addItem(it.next());
   }
   /**
    * add the supported forecast types to the combo box
    */
    jEqkForeType.addItem(FRANKEL_1996_FORECAST);
    jEqkForeType.addItem(WARD_GRID_TEST);
}


   /**
     * Creates a class instance from a string of the full class name including packages.
     * This is how you dynamically make objects at runtime if you don't know which\
     * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
     * it the normal way:<P>
     *
     * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
     *
     * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
     * instead to create the same class by:<P>
     *
     * <code>BJF_1997_AttenRel imr =
     * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.attenRelImpl.BJF_1997_AttenRel");
     * </code><p>
     *
     */
    public Object createIMRClassInstance( String className, org.scec.param.event.ParameterChangeWarningListener listener){
        String S = C + ": createIMRClassInstance(): ";
        try {

            Class listenerClass = Class.forName( "org.scec.param.event.ParameterChangeWarningListener" );
            Object[] paramObjects = new Object[]{ listener };
            Class[] params = new Class[]{ listenerClass };
            Class imrClass = Class.forName( className );
            Constructor con = imrClass.getConstructor( params );
            Object obj = con.newInstance( paramObjects );
            return obj;
        } catch ( ClassCastException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( ClassNotFoundException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( NoSuchMethodException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( InvocationTargetException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( IllegalAccessException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        } catch ( InstantiationException e ) {
            System.out.println(S + e.toString());
            throw new RuntimeException( S + e.toString() );
        }

    }


  //Main method
  public static void main(String[] args) {
    EqkForecastApplet applet = new EqkForecastApplet();
    applet.isStandalone = true;
    JFrame frame = new JFrame();
    //EXIT_ON_CLOSE == 3
    frame.setDefaultCloseOperation(3);
    frame.setTitle("Applet Frame");
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    applet.setFrame( frame );
    frame.setTitle( applet.getAppletInfo() + ":  [ EarthQuake Forecast Model ]" );
    frame.setSize( W, H );
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation( ( d.width - frame.getSize().width ) / 2, ( d.height - frame.getSize().height ) / 2 );
    frame.setVisible( true );
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

         try{
          // only show messages for site parameters
          if(this.siteParamList.getParameter(name)==null)
            return;
          } catch(ParameterException paramException) {
          // we do not need to do anything in case this paramter is not in site paramters list
            return;
         }

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




    // only display messages if paramters are set at back
    StringBuffer b = new StringBuffer();

    WarningParameterAPI param = e.getWarningParameter();

    // do not display messages for focus lost
    if(!this.buttonClicked &&
       !param.getName().equalsIgnoreCase(LATITUDE) &&
       !param.getName().equalsIgnoreCase(LONGITUDE)) {
         param.setValueIgnoreWarning( e.getNewValue() );
         return;
    }

    try{
        Double min = (Double)param.getWarningMin();
        Double max = (Double)param.getWarningMax();

        String name = param.getName();

        // only show messages for site parameters
        if(this.siteParamList.getParameter(name)==null)
          return;

        b.append( "You have exceeded the recommended range for "+currIMR_Name+"\n");
        b.append( name );
        b.append( ": (" );
        b.append( min.toString() );

        b.append( " to " );
        b.append( max.toString() );
        b.append( ")\n" );
        b.append( "Click Yes to accept the new value: " );
        b.append( e.getNewValue().toString() );
    }

    catch(ParameterException paramException) {
       // we do not need to do anything in case this paramter is not in site paramters list
       param.setValueIgnoreWarning( e.getNewValue() );
       return;
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

    if(D) System.out.println(S + "You choose" + result);

    switch (result) {
        case JOptionPane.YES_OPTION:
            if(D) System.out.println(S + "You choose yes, changing value to " + e.getNewValue().toString() );
            param.setValueIgnoreWarning( e.getNewValue());
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
  *  Sets the frame attribute of the EqkForecast object
  *
  * @param  newFrame  The new frame value
  */
  public void setFrame( JFrame newFrame ) {
      frame = newFrame;
  }



  /**
   * When we select the IMT, disable the IMRs name based on whether they support that IMT or not
   *
   * @param e : ActionEvent object
   */
  void jIMTComboBox_actionPerformed(ActionEvent e) {


    // check to see whther the selecetd IMT is supported by this IMR
    for(int i=0;i<imtMap.length;++i) {

      //initially disable the check box
      jIMRNum[i].setSelected(false);
      jIMRNum[i].setEnabled(false);
      for(int j=0;j<imtMap[i].size();++j) {
        String str = (String)imtMap[i].get(j);
        if(str.equalsIgnoreCase((String)jIMTComboBox.getSelectedItem())){
          // if it is supported then enable the checkbox
          jIMRNum[i].setSelected(true);
          jIMRNum[i].setEnabled(true);
         }
      }
    }
  }


  /**
   * This method is called whenever any check box is selected or deselected
   * We have to change the site params accordingly
   *
   * @param e
   */
  void jCheckBox_itemStateChanged(ItemEvent e) {
    this.siteParamList.clear();
    sitePanel.removeAll();

    // make a paramter variable as it is used frequently in this function
    Parameter paramTemp;

    // disable the Vs30 checkbox. Enable later only if paramter contains Vs30 in it
    this.jCheckCVM.setEnabled(false);
    this.jCheckCVM.setSelected(false);

    // disable the Basin Depth checkbox. Enable later only if paramter contains Basin Depth in it
    this.jCheckBasin.setEnabled(false);
    this.jCheckBasin.setSelected(false);

    // get the number of IMRs
    int numOfIMRs = imrNames.size();

   // add the longitude and latitude paramters
    siteParamList.addParameter(longitude);
    siteParamList.addParameter(latitude);

    // check which IMR has been selected
    for(int i=0; i <numOfIMRs ; ++i) {

      //if ith IMR is selected then add its site params
      if(this.jIMRNum[i].isSelected()) {
          // number of sites for this IMR
          int numSites = siteMap[i].size();
          for(int j=0; j < numSites; ++j) {
            paramTemp = (Parameter)siteMap[i].get(j);

            if(paramTemp instanceof StringParameter) {
              StringParameter strConstraint = (StringParameter)paramTemp;
              paramTemp.setValue(strConstraint.getAllowedStrings().get(0));
            }
            //if this paramter has not been added till now
            if(!siteParamList.containsParameter(paramTemp.getName())) {
               siteParamList.addParameter(paramTemp);
               // if it is Vs30 paramter, then enable the button to get it from the CVM servlet
               if(paramTemp.getName().equalsIgnoreCase(this.VS30_STRING))
                  this.jCheckCVM.setEnabled(true);
               else if(paramTemp.getName().equalsIgnoreCase(this.BASIN_DEPTH_STRING))
                 this.jCheckBasin.setEnabled(true);
            }
          }
      }

    }

  this.siteEditor = new ParameterListEditor(siteParamList);
  siteEditor.setTitle(SITE_PARAMS);
  sitePanel.add(siteEditor,BorderLayout.CENTER);
  validate();
  repaint();
 }

 /**
  * this function is called when we want to get the paramters from the CVM servlet
  * That servlet will return basin depth and Vs30
  *
  * @param e
  */
  void jCheckCVM_actionPerformed(ActionEvent e) {

    // if the check box to get Vs30 from servlet is unselected
    if(!this.jCheckCVM.isSelected())
       return;

    // if we want to the paramter from the servlet
    try{

    // make connection with servlet
     URL velocityServlet = new URL("http://scec.usc.edu:9999/examples/servlet/Vs30Servlet");
     URLConnection servletConnection = velocityServlet.openConnection();

     servletConnection.setDoOutput(true);

     // Don't use a cached version of URL connection.
     servletConnection.setUseCaches (false);
     servletConnection.setDefaultUseCaches (false);

     // Specify the content type that we will send binary data
     servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

     // send the student object to the servlet using serialization
     ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

   // give latitude and longitude to the servlet
     Double longitude_value = (Double)siteParamList.getParameter(LONGITUDE).getValue();
     Double latitude_value = (Double)siteParamList.getParameter(LATITUDE).getValue();

     // if values in longitude and latitude are invalid
     if(longitude_value == null || latitude_value == null) {
       JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
       this.jCheckCVM.setSelected(false);
       return;
     }
     outputToServlet.writeObject(longitude_value);
     outputToServlet.writeObject(latitude_value);
     outputToServlet.flush();
     outputToServlet.close();

  // now read the connection again to get the vs30 as sent by the servlet
    ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
    Double vs30=(Double)ois.readObject();
    ois.close();

    if (D) System.out.println("Vs30 is:"+vs30.doubleValue());

    // set the value in the list
    siteParamList.getParameter(this.VS30_STRING).setValue(new Double(vs30.doubleValue()));

    // refresh the panel with the value
    sitePanel.removeAll();
    this.siteEditor = new ParameterListEditor(siteParamList);
    siteEditor.setTitle(SITE_PARAMS);
    sitePanel.add(siteEditor,BorderLayout.CENTER);
    validate();
    repaint();
    JOptionPane.showMessageDialog(this,"We have got the Vs30 from SCEC CVM");

   }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
   }catch (Exception exception) {
     System.out.println("Exception in connection with servlet:" +exception);
   }
  }

  /**
   * this function is called when addGraph button is clicked
   * @param e
   */

  void jBCalc_actionPerformed(ActionEvent e) {

    // check whther all values are filled in
    if(jTimeField.getText()=="")
      JOptionPane.showMessageDialog(this,new String("Must enter the time field")
                                    ,"Incomplete Data Entered",JOptionPane.ERROR_MESSAGE);
    if(Double.parseDouble(jTimeField.getText()) == Double.NaN)
      JOptionPane.showMessageDialog(this,new String("Must enter a valid numerical value in the TimeSpan")
                                    ,"Wrong Data Entered",JOptionPane.ERROR_MESSAGE);
    else{

      buttonClicked = true;

      // get the selected forecast model
      String selectedForecast = (String)jEqkForeType.getSelectedItem();

      // if frankel 1996 forecast is selected
      if(selectedForecast.equalsIgnoreCase(this.FRANKEL_1996_FORECAST)) {
          if(frankelRupForecast == null)
              frankelRupForecast = new Frankel96_EqkRupForecast();
          eqkRupForecast = frankelRupForecast;
      } else if(selectedForecast.equalsIgnoreCase(this.WARD_GRID_TEST)) {
        // if ward frid test forecast is selected
          if(wardRupForecast == null)
              wardRupForecast = new WardGridTestEqkRupForecast();
          eqkRupForecast = wardRupForecast;
      }


      // intialize the hazFunction for each IMR
       int imrSize=imrNames.size();
       ArbitrarilyDiscretizedFunc condProbFunc = new ArbitrarilyDiscretizedFunc();

       // set the time span for the forecats model
      eqkRupForecast.setTimeSpan(Double.parseDouble(jTimeField.getText()));

      // clear the function list
      this.totalProbFuncs.clear();

      // get the longitude and latitude values
      double longVal=((Double)siteParamList.getParameter(LONGITUDE).getValue()).doubleValue();
      double latVal = ((Double)siteParamList.getParameter(LATITUDE).getValue()).doubleValue();

      // get the selected IMT, if it is SA, get the period as well
      String imt = (String)jIMTComboBox.getSelectedItem();
      // set the X-axis name for the graph
      totalProbFuncs.setXAxisName(imt);
      double period = 0;
      if((imt.substring(0,2)).equalsIgnoreCase(SA)) {
          StringTokenizer st = new StringTokenizer(imt);
          st.nextToken();
          period = Double.parseDouble(st.nextToken());
          imt = new String(SA);
       }

       // make a site object to pass to each IMR
      Site site = new Site(new Location(latVal,longVal));
      site.addParameterList(this.siteParamList);

      Iterator it = siteParamList.getParametersIterator();

      int count=0;
      while(it.hasNext()){
        ++count;
        if(D)System.out.println("Site Params::::"+count +":::"+((ParameterAPI)it.next()).getName());
      }


      // do for each IMR
      for(int i=0;i<imrSize;++i) {

        if(jIMRNum[i].isSelected()){ // if this IMR is selected

          // make new Arbitrarily Discr Function for each IMT
          hazFunction[i] = new ArbitrarilyDiscretizedFunc();
          initDiscretizeValues(hazFunction[i]);
          hazFunction[i].setInfo(jIMRNum[i].getText());
          if (D) System.out.println("selected IMR:"+jIMRNum[i].getText());
          // add the function of each IMR to the function list
          totalProbFuncs.add(hazFunction[i]);

          // get the selected IMT

          if(!imt.equalsIgnoreCase(SA)) {
            // if it is not SA
            ((AttenuationRelationshipAPI)imrObject.get(i)).setIntensityMeasure(imt);
          }
          else{
              //if it is SA, set SA and period as well
              ((AttenuationRelationshipAPI)imrObject.get(i)).setIntensityMeasure(SA);
              ParameterAPI periodParam = ((AttenuationRelationshipAPI)imrObject.get(i)).getParameter("SA Period");
              periodParam.setValue(new Double(period));
           }
          // pass the site object to each IMR
          try {
             if(D) System.out.println("siteString:::"+site.toString());
              currIMR_Name = new String(jIMRNum[i].getText());
             ((AttenuationRelationshipAPI)imrObject.get(i)).setSite(site);
          } catch (Exception ex) {
                 if(D) System.out.println(C + ":Param warning caught"+ex);
                 ex.printStackTrace();

           }
         }
       }
      // get total sources
      int numSources = eqkRupForecast.getNumSources();

      if(D) System.out.println("number of sources::" +numSources);
      for(int i=0;i < numSources ;i++) {
        if (D) System.out.println("source number:"+i);
        // set the value for progress bar
        //progress.setValue(i+1);
        // get source and get its distance from the site
        ProbEqkSource source = eqkRupForecast.getSource(i);
        double distance = source.getMinDistance(site);
        if(D) System.out.println("Distance::"+distance);
        if(distance > MAX_DISTANCE)
           continue;
        // for each source, get the number of ruptures
        int numRuptures = eqkRupForecast.getNumRuptures(i);
         if(D) System.out.println("number of ruptures::" +numRuptures);
         for(int n=0; n < numRuptures ;n++){
           // for each rupture, set in IMR and do computation
           if (D) System.out.println("RuptureNumber:"+n);
           double qkProb = ((ProbEqkRupture)source.getRupture(n)).getProbability();
           for(int imr=0;imr<imrSize;imr++) {
              if (D) System.out.println("Source:"+i+",rupture="+n+",imr="+imr);
              if(jIMRNum[imr].isSelected()){
                // initialize the values in condProbfunc
                initLogDiscretizeValues(condProbFunc);
                try {
                  if(D) System.out.println("imr:::"+imr);
                   ((AttenuationRelationshipAPI)imrObject.get(imr)).setProbEqkRupture((ProbEqkRupture)eqkRupForecast.getRupture(i,n));
                } catch (Exception ex) {
                   if(D) System.out.println(C + ":Param warning caught");
                }
                   condProbFunc=(ArbitrarilyDiscretizedFunc)((AttenuationRelationshipAPI)imrObject.get(imr)).getExceedProbabilities(condProbFunc);

                for(int k=0;k<condProbFunc.getNum();k++){
                    hazFunction[imr].set(k,hazFunction[imr].getY(k)*Math.pow(1-qkProb,condProbFunc.getY(k)));
                    if (D) System.out.println("k="+k+",hazfunction[k] for imr="+hazFunction[imr].getY(k));
                    if (D) System.out.println("qkProb="+qkProb+",condProbFunc(k)="+condProbFunc.getY(k));
                }
              }
           }
        }
      }

      // now set the final values in hazFunction
      for(int imr=0;imr<imrSize;++imr){
        for(int j=0;j<condProbFunc.getNum();++j){
          if(jIMRNum[imr].isSelected()){
             hazFunction[imr].set(j,1-hazFunction[imr].getY(j));
          }

        }
      }
    }

    // add the text area
    int imrSize=this.imrNames.size();
    for(int imr=0;imr<imrSize;++imr){
      if(this.jIMRNum[imr].isSelected())
        pointsTextArea.setText(hazFunction[imr].getInfo() + ": " + '\n' + totalProbFuncs.toString() );
    }
    // draw the graph
    this.addGraphPanel();
  }


  /**
   * Initialize the X values and the prob as 1
   *
   * @param arb
   */
  private void initDiscretizeValues(ArbitrarilyDiscretizedFunc arb){
              arb.set(.001,1);
              arb.set(.01,1);
              arb.set(.05,1);
              arb.set(.15,1);
              arb.set(.2,1);
              arb.set(.25,1);
              arb.set(.3,1);
              arb.set(.4,1);
              arb.set(.5,1);
              arb.set(.6,1);
              arb.set(.7,1);
              arb.set(.8,1);
              arb.set(.9,1);
              arb.set(1.0,1);
              arb.set(1.1,1);
              arb.set(1.2,1);
              arb.set(1.3,1);
              arb.set(1.4,1);
              arb.set(1.5,1);
  }

  /**
   * set x values in log space
   *
   * @param arb
   */
  private void initLogDiscretizeValues(ArbitrarilyDiscretizedFunc arb){
    arb.set(Math.log(.001),1);
    arb.set(Math.log(.01),1);
    arb.set(Math.log(.05),1);
    arb.set(Math.log(.15),1);
    arb.set(Math.log(.2),1);
    arb.set(Math.log(.25),1);
    arb.set(Math.log(.3),1);
    arb.set(Math.log(.4),1);
    arb.set(Math.log(.5),1);
    arb.set(Math.log(.6),1);
    arb.set(Math.log(.7),1);
    arb.set(Math.log(.8),1);
    arb.set(Math.log(.9),1);
    arb.set(Math.log(1.0),1);
    arb.set(Math.log(1.1),1);
    arb.set(Math.log(1.2),1);
    arb.set(Math.log(1.3),1);
    arb.set(Math.log(1.4),1);
    arb.set(Math.log(1.5),1);
    }





  /**
   *  Adds a feature to the GraphPanel attribute of the EqkForecastApplet object
   */
  protected void addGraphPanel() {

      // Starting
      String S = C + ": addGraphPanel(): ";

      String newXYAxisName = this.totalProbFuncs.getXYAxesName();


      // create a default chart based on some sample data...

      // Determine which IM to add to the axis labeling
      String xAxisLabel = totalProbFuncs.getXAxisName();
      String yAxisLabel = totalProbFuncs.getYAxisName();




      HorizontalNumberAxis xAxis = new HorizontalNumberAxis( xAxisLabel );
      xAxis.setAutoRangeIncludesZero( false );
      xAxis.setTickMarksVisible(false);


      VerticalNumberAxis yAxis = new VerticalNumberAxis( yAxisLabel );
      yAxis.setAutoRangeIncludesZero( false );
      yAxis.setTickMarksVisible(false);



      int type = com.jrefinery.chart.renderer.StandardXYItemRenderer.LINES;


      LogXYItemRenderer renderer = new LogXYItemRenderer( type, new StandardXYToolTipGenerator() );
      //StandardXYItemRenderer renderer = new StandardXYItemRenderer( type, new StandardXYToolTipGenerator() );


      // build the plot

      org.scec.gui.PSHALogXYPlot plot = new org.scec.gui.PSHALogXYPlot(this.totalData, xAxis, yAxis, renderer);
      plot.setBackgroundAlpha( .8f );
      plot.setDomainCrosshairLockedOnData(false);
      plot.setDomainCrosshairVisible(false);
      plot.setRangeCrosshairLockedOnData(false);
      plot.setRangeCrosshairVisible(false);

      plot.setRenderer( renderer );


      JFreeChart chart = new JFreeChart(TITLE, JFreeChart.DEFAULT_TITLE_FONT, plot, true );

      chart.setBackgroundPaint( lightBlue );

      // chart.setBackgroundImage(image);
      // chart.setBackgroundImageAlpha(.3f);

      // Put into a panel
      chartPanel = new ChartPanel(chart, true, true, true, true, false);
      //panel.setMouseZoomable(true);

      chartPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED ) );
      chartPanel.setMouseZoomable(true);
      chartPanel.setDisplayToolTips(true);
      chartPanel.setHorizontalAxisTrace(false);
      chartPanel.setVerticalAxisTrace(false);

      //panel.removeAll();
      //panel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        //                      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );


      if(D) System.out.println(this.totalProbFuncs.toString());
      //validate();
      //repaint();
      graphOn=false;
      togglePlot();

   }

   /**
    * this function is called when basin depth is to be fetched from the server
    *
    * @param e
    */
  void jCheckBasin_actionPerformed(ActionEvent e) {
    // if the check box to get Vs30 from servlet is unselected
    if(!this.jCheckBasin.isSelected())
       return;

    // if we want to the paramter from the servlet
    try{

    // make connection with servlet
     URL velocityServlet = new URL("http://scec.usc.edu:9999/examples/servlet/BasinDepthServlet");
     URLConnection servletConnection = velocityServlet.openConnection();

     servletConnection.setDoOutput(true);

     // Don't use a cached version of URL connection.
     servletConnection.setUseCaches (false);
     servletConnection.setDefaultUseCaches (false);

     // Specify the content type that we will send binary data
     servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

     // send the student object to the servlet using serialization
     ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

   // give latitude and longitude to the servlet
     Double longitude_value = (Double)siteParamList.getParameter(LONGITUDE).getValue();
     Double latitude_value = (Double)siteParamList.getParameter(LATITUDE).getValue();

     // if values in longitude and latitude are invalid
     if(longitude_value == null || latitude_value == null) {
       JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
       this.jCheckBasin.setSelected(false);
       return;
     }
     outputToServlet.writeObject(longitude_value);
     outputToServlet.writeObject(latitude_value);
     outputToServlet.flush();
     outputToServlet.close();

  // now read the connection again to get the vs30 as sent by the servlet
    ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
    Double basinDepth=(Double)ois.readObject();
    ois.close();

    // print th basin depth
    if (D) System.out.println("Basin Depth is:"+basinDepth.doubleValue());

    //truncate the value got from servlet to 2 digits after decimal point
    // also convert to kms
    String strBasin = new String(""+basinDepth.doubleValue()/1000);
    strBasin = strBasin.substring(0,4);

    siteParamList.getParameter(this.BASIN_DEPTH_STRING).setValue(new Double(strBasin));

    // refresh the panel with the value
    sitePanel.removeAll();
    this.siteEditor = new ParameterListEditor(siteParamList);
    siteEditor.setTitle(SITE_PARAMS);
    sitePanel.add(siteEditor,BorderLayout.CENTER);
    validate();
    repaint();
    JOptionPane.showMessageDialog(this,"We have got the Basin Depth from SCEC CVM");

   }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
   }catch (Exception exception) {
     System.out.println("Exception in connection with servlet:" +exception);
   }
  }


  /**
   *  Description of the Method
   */
  protected void togglePlot() {

      // Starting
      String S = C + ": togglePlot(): ";
      panel.removeAll();
      if ( graphOn ) {

          this.jToggleButton.setText( "Show Plot" );
          graphOn = false;

          panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                  , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );
      }
      else {
          graphOn = true;
          // dataScrollPane.setVisible(false);
          this.jToggleButton.setText( "Show Data" );
                        // panel added here
          if(chartPanel !=null)
              panel.add( chartPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ) );

          else
              // innerPlotPanel.setBorder(oval);
              panel.add( dataScrollPane, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
                      , GridBagConstraints.CENTER, GridBagConstraints.BOTH, plotInsets, 0, 0 ) );

      }


      validate();
      repaint();

      if ( D ) System.out.println( S + "Ending" );

    }

    /**
     *  write out data to file. Needs to be enhanced. No checking is done to
     *  make sure it's not a dir, the file is writable if it exists, etc.
     *
     * @param  e  Description of the Parameter
     */
    void pointsTextArea_mouseClicked( MouseEvent e ) {

        // Starting
        String S = C + ": pointsTextArea_mouseClicked(): ";
        if ( D )
            System.out.println( S + "Starting" );

        // right mouse button not clicked
        if ( !( ( e.getModifiers() & InputEvent.BUTTON3_MASK ) != 0 ) )
            return;

        if ( pointsTextArea.getText().equals( NO_PLOT_MSG ) )
            return;

         if ( D )
            System.out.println( S + "Ending" );

    }

    /**
     * This function is called when Show Data button is clicked
     *
     * @param e
     */
    void jToggleButton_actionPerformed(ActionEvent e) {
      togglePlot();
    }

}
