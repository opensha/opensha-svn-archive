package org.scec.sha.gui.beans;


import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.text.DecimalFormat;

import org.scec.param.ParameterAPI;
import org.scec.param.StringParameter;
import org.scec.param.ParameterList;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.sha.magdist.gui.MagFreqDistParameterEditor;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.data.Location;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import java.awt.event.*;



/**
 * <p>Title: Eqk Rupture Selector GuiBean</p>
 * <p>Description: This class will show ERF and its parameters. It will
 * also allow the user to select a particular rupture for scenario maps </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class EqkRupSelectorGuiBean extends JPanel implements ParameterChangeListener
{


  /**
   * Name of the class
   */
  protected final static String C = "EqkRupSelectorGuiBean";
  // for debug purpose
  protected final static boolean D = false;

  //Deciaml format to show the Hypocenter Location Object in the StringParameter
  private DecimalFormat decimalFormat=new DecimalFormat("0.000##");

  private final static String EQK_RUP_SELECTOR_TITLE = "Eqk. Rup. Selector";


  // ERF Editor stuff
  public final static String ERF_PARAM_NAME = "Eqk Rup Forecast";

  // Source Param Name
  public final static String SOURCE_PARAM_NAME = "Source Index";
  // Rupture Param Name
  public final static String RUPTURE_PARAM_NAME = "Rupture Index";

  //Rupture Hypocenterlocation Param
  public final static String RUPTURE_HYPOLOCATIONS_PARAM_NAME="Hypocentre Locations";

  //Object of ProbEqkRupture
  ProbEqkRupture probEqkRupture;

  //ERFGuiBean Instance
  ERF_GuiBean erfGuiBean;
  private JButton erfAdjParamButton = new JButton();
  private JScrollPane sourceRupInfoScroll = new JScrollPane();
  private JTextPane sourceRupInfoText = new JTextPane();

  //ListEditor
  private ParameterListEditor listEditor;

  private String [] searchPaths;


  //Instance of the JDialog to show all the adjuatble params for the forecast model
  JDialog frame;
  private JCheckBox hypoCentreCheck = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  // vector to save which forecasts have already been selected by the user
  // which this forecast  has already been selected it will not pop up adjustable params window
  private Vector  alreadySeenERFs = new Vector();


  /**
  * Constructor : It accepts the classNames of the ERFs to be shown in the editor
  * @param erfClassNames
  */
 public EqkRupSelectorGuiBean(Vector erfClassNames) {

   // create the instance of ERFs
   erfGuiBean= new ERF_GuiBean(erfClassNames);
   try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    // Build package names search path
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

    setParamsInForecast(0,0);

 }


 /**
  * this function is called to add the paramters based on the forecast
  *  selected by the user
  * @param forecast
  */
 protected void setParamsInForecast(int sourceIndex,int ruptureIndex) {


   // get the selected forecast
   EqkRupForecast erf = (EqkRupForecast)erfGuiBean.getSelectedERF();

   // add the select forecast parameter
   ParameterAPI chooseERF_Param = erfGuiBean.getParameterList().getParameter(erfGuiBean.ERF_PARAM_NAME);
   chooseERF_Param.addParameterChangeListener(this);
   ParameterList parameterList = new ParameterList();
   parameterList.addParameter(chooseERF_Param);


   int numSources = erf.getNumSources();
   IntegerParameter sourceParam = new IntegerParameter(SOURCE_PARAM_NAME,
       0,numSources-1,new Integer(sourceIndex));


   sourceParam.addParameterChangeListener(this);
   parameterList.addParameter(sourceParam);

   //add parameter for selecting the rupture for selected source index
   int numRuptures = erf.getNumRuptures(((Integer)sourceParam.getValue()).intValue());
   IntegerParameter ruptureParam = new IntegerParameter(RUPTURE_PARAM_NAME,
       0,numRuptures-1,new Integer(ruptureIndex));
   ruptureParam.addParameterChangeListener(this);
   parameterList.addParameter(ruptureParam);

   //getting the surface of the rupture
   Vector v = new  Vector();
   int ruptureValue = ((Integer)ruptureParam.getValue()).intValue();
   int sourceValue = ((Integer)sourceParam.getValue()).intValue();
   probEqkRupture = erf.getRupture(sourceValue,ruptureValue);
   // The first row of all the rupture surfaces is the list of their hypocenter locations
   ListIterator hypoLocationsIt = probEqkRupture.getRuptureSurface().getColumnIterator(0);
   Location loc;
   while(hypoLocationsIt.hasNext()){
     //getting the object of Location from the HypocenterLocations and formatting its string to 3 placees of decimal
     loc= (Location)hypoLocationsIt.next();
     String lat = decimalFormat.format(loc.getLatitude());
     String lon = decimalFormat.format(loc.getLongitude());
     String depth = decimalFormat.format(loc.getDepth());
     v.add(lat+" "+lon+" "+depth);
   }
   StringConstraint constraints= new StringConstraint(v);
   StringParameter hypoCenterLocationParam = new StringParameter(RUPTURE_HYPOLOCATIONS_PARAM_NAME,
       constraints,v.get(0).toString());
   parameterList.addParameter(hypoCenterLocationParam);
   hypoCenterLocationParam.addParameterChangeListener(this);

   if(listEditor!=null) this.remove(listEditor);
   listEditor= new ParameterListEditor(parameterList, searchPaths);

   // now make the editor based on the parameter list
   listEditor.setTitle( EQK_RUP_SELECTOR_TITLE );

   if(!this.hypoCentreCheck.isSelected()){
     probEqkRupture.setHypocenterLocation(null);
     listEditor.getParameterEditor(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME).setVisible(false);
   }
   else{
     listEditor.getParameterEditor(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME).setVisible(true);
     //getting the HypoCenterLocation Object and setting the Rupture HypocenterLocation
     StringTokenizer token = new StringTokenizer(hypoCenterLocationParam.getValue().toString());
     double lat= Double.parseDouble(token.nextElement().toString().trim());
     double lon= Double.parseDouble(token.nextElement().toString().trim());
     double depth= Double.parseDouble(token.nextElement().toString().trim());
     loc= new Location(lat,lon,depth);
     System.out.println("Hypocenter Location:"+ loc.toString());
     probEqkRupture.setHypocenterLocation(loc);
   }

   // get the panel for increasing the font and border
   // this is hard coding for increasing the IMR font
   // the colors used here are from ParameterEditor
   JPanel panel = listEditor.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).getOuterPanel();
   TitledBorder titledBorder1 = new TitledBorder(BorderFactory.createLineBorder(new Color( 80, 80, 140 ),3),"");
   titledBorder1.setTitleColor(new Color( 80, 80, 140 ));
   Font DEFAULT_LABEL_FONT = new Font( "SansSerif", Font.BOLD, 13 );
   titledBorder1.setTitleFont(DEFAULT_LABEL_FONT);
   titledBorder1.setTitle(erfGuiBean.ERF_PARAM_NAME);
   Border border1 = BorderFactory.createCompoundBorder(titledBorder1,BorderFactory.createEmptyBorder(0,0,3,0));
   panel.setBorder(border1);
   this.add(listEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
   this.validate();
   this.repaint();
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

   // if ERF selected by the user  changes
   if( name1.equals(erfGuiBean.ERF_PARAM_NAME) ){
     // if this forecast has not been selected yet, pop up the adjustable params window
     if(!this.alreadySeenERFs.contains(event.getNewValue()))  {
       getAllERFAdjustableParams();
       alreadySeenERFs.add(event.getNewValue());
     }
     setParamsInForecast(0,0);
   }

   // if source selected by the user  changes
   if( name1.equals(this.SOURCE_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     // set the new forecast parameters. Also change the number of ruptures in this source
     setParamsInForecast(Integer.parseInt(value),0);
   }

   // if source selected by the user  changes
   if( name1.equals(this.RUPTURE_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     // set the new forecast parameters. Also change the number of ruptures in this source
     setParamsInForecast(((Integer)listEditor.getParameterList().getParameter(SOURCE_PARAM_NAME).getValue()).intValue(),Integer.parseInt(value));
   }


    //if the Hypo Center location has been set
   if(name1.equals(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME)){
     StringTokenizer token = new StringTokenizer(listEditor.getParameterList().getParameter(RUPTURE_HYPOLOCATIONS_PARAM_NAME).getValue().toString());
     double lat= Double.parseDouble(token.nextElement().toString().trim());
     double lon= Double.parseDouble(token.nextElement().toString().trim());
     double depth= Double.parseDouble(token.nextElement().toString().trim());
     Location loc= new Location(lat,lon,depth);
     System.out.println("Hypocenter Location:"+ loc.toString());
     probEqkRupture.setHypocenterLocation(loc);
   }
 }

   private void jbInit() throws Exception {
    erfAdjParamButton.setText("Set Eqk Rup Forecast Params");
    erfAdjParamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        erfAdjParamButton_actionPerformed(e);
      }
    });
    erfAdjParamButton.setForeground(new Color(80,80,133));
    erfAdjParamButton.setBackground(new Color(200,200,230));
    this.setLayout(gridBagLayout1);
    sourceRupInfoText.setEditable(false);
    hypoCentreCheck.setText("Set Hypocenter Location");
    hypoCentreCheck.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hypoCentreCheck_actionPerformed(e);
      }
    });
    this.add(sourceRupInfoScroll,  new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    sourceRupInfoScroll.getViewport().add(sourceRupInfoText, null);
    this.add(erfAdjParamButton,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    this.add(hypoCentreCheck,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
  }

  void erfAdjParamButton_actionPerformed(ActionEvent e) {
    getAllERFAdjustableParams();
  }


  /**
   * This method gets the ERF adjustable Params for the selected ERF model
   * and the user has pressed the button to see adjust all the adjustable params
   */
  private void getAllERFAdjustableParams(){

    // get the selected forecast

    //checks if the magFreqDistParameter exists inside it , if so then gets its Editor and
    //calls the method to make the update MagDist button invisible
    erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setVisible(false);
    MagFreqDistParameterEditor magDistEditor=erfGuiBean.getMagDistEditor();
    if(magDistEditor !=null)  magDistEditor.setUpdateButtonVisible(false);
    //Panel Parent
    Container parent = this;
    /*This loops over all the parent of this class until the parent is Frame(applet)
    this is required for the passing in the JDialog to keep the focus on the adjustable params
    frame*/
    while(!(parent instanceof JFrame) && parent != null)
      parent = parent.getParent();
    frame = new JDialog((JFrame)parent);
    frame.setModal(true);
    frame.setSize(300,600);
    frame.setTitle("ERF Adjustable Params");
    frame.getContentPane().setLayout(new GridBagLayout());
    frame.getContentPane().add(erfGuiBean,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));

    //Adding Button to update the forecast
    JButton button = new JButton();
    button.setText("Update Forecast");
    button.setForeground(new Color(80,80,133));
    button.setBackground(new Color(200,200,230));
    button.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       button_actionPerformed(e);
     }
    });
    frame.getContentPane().add(button,new GridBagConstraints(0, 2, 1, 1, 0.0,0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
    frame.show();
    frame.pack();
  }

  void button_actionPerformed(ActionEvent e) {
   setParamsInForecast(0,0);
   frame.dispose();
  }


  /**
   *
   * @param : Name of the Parameter
   * @returns the parameter with the name param
   */
  public ParameterAPI getParameter(String param){
    return listEditor.getParameterList().getParameter(param);
  }



  /**
    *
    * @returns the EqkRupforecast model
    */
   public EqkRupForecastAPI getSelectedERF_Instance() {
     return erfGuiBean.getSelectedERF_Instance();
  }

  /**
   *
   * @returns the ProbEqkRupture Object
   */
  public ProbEqkRupture getRupture(){
  return probEqkRupture;
  }

  /**
   * If hypocenter Location checkBox action is performed on it
   * @param e
   */
  void hypoCentreCheck_actionPerformed(ActionEvent e) {
    int sourceIndex = ((Integer)listEditor.getParameterList().getParameter(this.SOURCE_PARAM_NAME).getValue()).intValue();
    int ruptureIndex = ((Integer)listEditor.getParameterList().getParameter(this.RUPTURE_PARAM_NAME).getValue()).intValue();
    setParamsInForecast(sourceIndex,ruptureIndex);
  }

  //returns the parameterListEditor
  public ParameterListEditor getParameterListEditor(){
    return listEditor;
  }

}