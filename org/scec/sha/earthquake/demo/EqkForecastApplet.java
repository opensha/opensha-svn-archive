package org.scec.sha.earthquake.demo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.lang.reflect.*;

import org.scec.param.event.*;
import org.scec.param.*;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.imr.gui.IMRGuiBean;
import org.scec.sha.imr.ClassicIMRAPI;
import javax.swing.border.*;
/**
 * <p>Title: EqkForecastApplet</p>
 * <p>Description: Earthquake forecast Demo Applet</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
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



  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String BJF_CLASS_NAME = "org.scec.sha.imr.classicImpl.BJF_1997_IMR";
  protected final static String AS_CLASS_NAME = "org.scec.sha.imr.classicImpl.AS_1997_IMR";
  protected final static String C_CLASS_NAME = "org.scec.sha.imr.classicImpl.Campbell_1997_IMR";
  protected final static String SCEMY_CLASS_NAME = "org.scec.sha.imr.classicImpl.SCEMY_1997_IMR";
  protected final static String F_CLASS_NAME = "org.scec.sha.imr.classicImpl.Field_2000_IMR";
  protected final static String A_CLASS_NAME = "org.scec.sha.imr.classicImpl.Abrahamson_2000_IMR";

  /**
   *  Temp until figure out way to dynamically load classes during runtime
   */
  protected final static String BJF_NAME = "Boore, Joyner, & Fumal (1997)";
  protected final static String AS_NAME = "Abrahamson & Silva (1997)";
  protected final static String C_NAME = "Cambell (1997) w/ erratum (2000) changes";
  protected final static String SCEMY_NAME = "Sadigh et al. (1997)";
  protected final static String F_NAME = "Field (2000)";
  protected final static String A_NAME = "Abrahamson (2000)";

  protected final static String SA = "SA";
  protected final static String SA_PERIOD = "SA Period";
  protected final static String FRANKEL_1996_FORECAST = "Frankel 1996";
  protected final static int W = 880;
  protected final static int H = 590;

  // it maps the IMR names and supported IMT for each IMR
  protected ArrayList[] imtMap;

  //it maps the IMR names and site supported by each IMR
  protected ArrayList[] siteMap;

  final static GridBagLayout GBL = new GridBagLayout();

  Color background = new Color(200,200,230);
  Color foreground = new Color(80,80,133);

  /**
   *  Hashmap that maps picklist imr string names to the real fully qualified
   *  class names
   */
  protected static HashMap imrNames = new HashMap();
  private JPanel jEqkForecastPanel = new JPanel();

  // combobox to show all the IMTs supported
  private JComboBox jIMTComboBox = new JComboBox();
  private JLabel jIMTLabel = new JLabel();

  // combo box to show the supported Earthforecast Types
  private JComboBox jEqkForeType = new JComboBox();

  // text field where time span(in yrs.) is filled up
  private JTextField jTimeField = new JTextField();
  private JLabel jForecastLabel = new JLabel();
  private JLabel jTimeSpan = new JLabel();
  private JLabel jYears = new JLabel();
  private Border border1;
  private JLabel jIMR = new JLabel();

  // array of checkboxes. One check box for each supported IMR
  private JCheckBox[] jIMRNum;


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
  private DoubleParameter longitude = new DoubleParameter("Longitude");
  private DoubleParameter latitude = new DoubleParameter("Latitude");


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
      imrNames.put( A_CLASS_NAME, A_NAME );

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
  private BorderLayout borderLayout1 = new BorderLayout();
  private Border border3;
  private JPanel jPanel1 = new JPanel();
  private Border border4;

  //Get a parameter value
  public String getParameter(String key, String def) {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public EqkForecastApplet() {
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
    border1 = BorderFactory.createEtchedBorder(new Color(200, 200, 230),new Color(80, 80, 133));
    border2 = BorderFactory.createEtchedBorder(new Color(200, 200, 230),new Color(80, 80, 133));
    border3 = BorderFactory.createLineBorder(new Color(80, 80, 133),1);
    border4 = BorderFactory.createLineBorder(new Color(80, 80, 133),1);
    jForecastLabel.setBackground(new Color(200, 200, 230));
    jForecastLabel.setForeground(new Color(80, 80, 133));
    jTimeSpan.setBackground(new Color(200, 200, 230));
    jTimeSpan.setFont(new java.awt.Font("Dialog", 1, 11));
    jTimeSpan.setForeground(new Color(80, 80, 133));
    jYears.setBackground(new Color(200, 200, 230));
    jYears.setFont(new java.awt.Font("Dialog", 1, 11));
    jYears.setForeground(new Color(80, 80, 133));
    jYears.setText("# yrs");
    jYears.setBounds(new Rectangle(605, 18, 38, 25));
    jTimeField.setBackground(Color.white);
    jTimeField.setFont(new java.awt.Font("SansSerif", 1, 11));
    jTimeField.setForeground(new Color(80, 80, 133));
    jEqkForeType.setBackground(new Color(200, 200, 230));
    jEqkForeType.setFont(new java.awt.Font("Dialog", 1, 11));
    jEqkForeType.setForeground(new Color(80, 80, 133));
    jIMTComboBox.setBackground(new Color(200, 200, 230));
    jIMTComboBox.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMTComboBox.setForeground(new Color(80, 80, 133));
    jIMTComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jIMTComboBox_actionPerformed(e);
      }
    });
    jIMR.setBackground(new Color(200, 200, 230));
    jIMR.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMR.setForeground(new Color(80, 80, 133));
    jIMR.setText("Select IMR :");
    jIMR.setBounds(new Rectangle(10, 56, 137, 25));
    jIMRList.setBackground(Color.white);
    jIMRList.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMRList.setForeground(Color.white);
    jIMRList.setBorder(border2);
    jIMRList.setBounds(new Rectangle(7, 80, 227, 174));
    jIMRList.setLayout(gridLayout1);
    gridLayout1.setColumns(1);
    gridLayout1.setRows(0);
    gridLayout1.setVgap(1);
    sitePanel.setBackground(Color.white);
    sitePanel.setFont(new java.awt.Font("Dialog", 1, 11));
    sitePanel.setForeground(new Color(200, 200, 230));
    sitePanel.setBorder(border3);
    sitePanel.setBounds(new Rectangle(6, 266, 225, 265));
    sitePanel.setLayout(borderLayout1);
    jEqkForecastPanel.setBackground(Color.white);
    jEqkForecastPanel.setForeground(new Color(80, 80, 133));
    this.getContentPane().setBackground(Color.white);
    this.setForeground(new Color(80, 80, 133));
    jEqkForeType.setBounds(new Rectangle(317, 23, 103, 21));
    jTimeField.setBounds(new Rectangle(500, 22, 101, 22));
    jForecastLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jForecastLabel.setText("Select Eqk Forecast :");
    jForecastLabel.setBounds(new Rectangle(203, 24, 112, 20));
    jTimeSpan.setText("Time Span :");
    jTimeSpan.setBounds(new Rectangle(434, 22, 66, 22));
    this.setSize(new Dimension(874, 561));
    this.getContentPane().setLayout(null);
    jEqkForecastPanel.setBorder(border1);
    jEqkForecastPanel.setBounds(new Rectangle(5, 4, 863, 550));
    jEqkForecastPanel.setLayout(null);
    jIMTComboBox.setBounds(new Rectangle(78, 26, 103, 20));
    jIMTLabel.setBackground(new Color(200, 200, 230));
    jIMTLabel.setFont(new java.awt.Font("Dialog", 1, 11));
    jIMTLabel.setForeground(new Color(80, 80, 133));
    jIMTLabel.setText("Select IMT:");
    jIMTLabel.setBounds(new Rectangle(11, 25, 68, 23));
    jPanel1.setBackground(Color.white);
    jPanel1.setBorder(border4);
    jPanel1.setBounds(new Rectangle(250, 55, 602, 478));
    jEqkForecastPanel.add(jIMTComboBox, null);
    jEqkForecastPanel.add(jForecastLabel, null);
    jEqkForecastPanel.add(jEqkForeType, null);
    jEqkForecastPanel.add(jYears, null);
    jEqkForecastPanel.add(jIMTLabel, null);
    jEqkForecastPanel.add(jTimeSpan, null);
    jEqkForecastPanel.add(jTimeField, null);
    jEqkForecastPanel.add(jPanel1, null);
    jEqkForecastPanel.add(jIMR, null);
    jEqkForecastPanel.add(jIMRList, null);
    jEqkForecastPanel.add(sitePanel, null);
    this.getContentPane().add(jEqkForecastPanel, null);
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
   ClassicIMRAPI imr;
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
     imr = ( ClassicIMRAPI ) createIMRClassInstance(className ,this);

     // get the list of sites supported by this IMR
     ListIterator listIt = imr.getSiteParamsIterator();

     // save the Sit Types supported by this IMR in a list
     Parameter tempParam;
     while(listIt.hasNext()) {
       tempParam = (Parameter)listIt.next();
       siteMap[numOfIMT].add(tempParam.clone());
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

    jEqkForeType.addItem(FRANKEL_1996_FORECAST);
}


 /**
     * Creates a class instance from a string of the full class name including packages.
     * This is how you dynamically make objects at runtime if you don't know which\
     * class beforehand. For example, if you wanted to create a BJF_1997_IMR you can do
     * it the normal way:<P>
     *
     * <code>BJF_1997_IMR imr = new BJF_1997_IMR()</code><p>
     *
     * If your not sure the user wants this one or AS_1997_IMR you can use this function
     * instead to create the same class by:<P>
     *
     * <code>BJF_1997_IMR imr =
     * (BJF_1997_IMR)ClassUtils.createNoArgConstructorClassInstance("org.scec.sha.imt.classicImpl.BJF_1997_IMR");
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

            //if this paramter has not been added till now
            if(!siteParamList.containsParameter(paramTemp.getName()))
               siteParamList.addParameter(paramTemp);
          }
      }

    }

  this.siteEditor = new ParameterListEditor(siteParamList, this, this);
  siteEditor.setTitle("Site Params");
  sitePanel.add(siteEditor,BorderLayout.CENTER);
  validate();
  repaint();
 }

}
