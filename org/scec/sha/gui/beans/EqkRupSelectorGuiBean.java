package org.scec.sha.gui.beans;


import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.scec.data.Location;
import org.scec.param.IntegerParameter;
import org.scec.param.ParameterAPI;
import org.scec.param.ParameterList;
import org.scec.param.StringConstraint;
import org.scec.param.StringParameter;
import org.scec.param.editor.ParameterListEditor;
import org.scec.param.event.ParameterChangeEvent;
import org.scec.param.event.ParameterChangeListener;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.sha.earthquake.EqkRupForecastAPI;
import org.scec.sha.earthquake.ProbEqkRupture;
import org.scec.sha.gui.infoTools.CalcProgressBar;
import java.awt.*;


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
  private JTextArea sourceRupInfoText = new JTextArea();

  //ListEditor
  private ParameterListEditor listEditor;

  //see if we have to show all the Adjustable Params for the ERF in a seperate window
  //when user selects different ERF.
  private boolean showAllAdjustableParamForERF= true;



  //Instance of the JDialog to show all the adjuatble params for the forecast model
  JDialog frame;
  private JCheckBox hypoCentreCheck = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  // vector to save which forecasts have already been selected by the user
  // which this forecast  has already been selected it will not pop up adjustable params window
  private ArrayList  alreadySeenERFs = new ArrayList();

  //progressBar class to be shown when ruptures are being updated
  CalcProgressBar progress;

  /**
  * Constructor : It accepts the classNames of the ERFs to be shown in the editor
  * @param erfClassNames
  */
 public EqkRupSelectorGuiBean(ArrayList erfClassNames) throws InvocationTargetException{

   // create the instance of ERFs
   erfGuiBean= new ERF_GuiBean(erfClassNames);
   try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    setParamsInForecast(0,0);

 }


 /**
  * this function is called to add the paramters based on the forecast
  *  selected by the user.
  * This updates the selecetd forecast and sets the sourceIndex and RuptureIndex
  * for the ScenarioMap.
  *
  * @param sourceIndex : "0" as the default
  * @param ruptureIndex : "0" as the default
  */
 public void setParamsInForecast(int sourceIndex,int ruptureIndex) {


   // get the selected forecast
   EqkRupForecast erf = null;
   try{
     //gets the instance of the selected ERF
     erf = (EqkRupForecast)erfGuiBean.getSelectedERF();
   }catch(Exception e){
     e.printStackTrace();
   }

   // add the select forecast parameter
   ParameterAPI chooseERF_Param = erfGuiBean.getParameterList().getParameter(erfGuiBean.ERF_PARAM_NAME);
   chooseERF_Param.addParameterChangeListener(this);
   ParameterList parameterList = new ParameterList();
   parameterList.addParameter(chooseERF_Param);

   int numSources = erf.getNumSources();
   ArrayList sourcesVector = new ArrayList();

   if(progress == null)
     progress = new CalcProgressBar("Updating Ruptures","Please wait while ruptures are being updated");
   else
     progress.show();

   for(int i=0;i<numSources;++i)
     sourcesVector.add(i+" ( "+erf.getSource(i).getName()+" )");

   StringParameter sourceParam = new StringParameter(SOURCE_PARAM_NAME,sourcesVector,(String)sourcesVector.get(sourceIndex));

   sourceParam.addParameterChangeListener(this);
   parameterList.addParameter(sourceParam);

   //add parameter for selecting the rupture for selected source index
   int sourceValue = Integer.parseInt((((String)sourceParam.getValue()).substring(0,((String)sourceParam.getValue()).indexOf("("))).trim());
   int numRuptures = erf.getNumRuptures(sourceValue);
   IntegerParameter ruptureParam = new IntegerParameter(RUPTURE_PARAM_NAME,
       0,numRuptures-1,new Integer(ruptureIndex));
   ruptureParam.addParameterChangeListener(this);
   parameterList.addParameter(ruptureParam);

   //getting the surface of the rupture
   ArrayList v = new  ArrayList();
   int ruptureValue = ((Integer)ruptureParam.getValue()).intValue();

   //writing the ruptures info. for each selected source in the text Area below the rupture
   String rupturesInfo = "Rupture info for \"";
   rupturesInfo += ((String)sourceParam.getValue()).substring(((String)sourceParam.getValue()).indexOf("(")+1,((String)sourceParam.getValue()).indexOf(")")).trim();
   rupturesInfo += "\":\n";
   for(int i=0;i< numRuptures;++i)
     rupturesInfo += "\n  rupture #"+i+": \n\n"+erf.getSource(sourceValue).getRupture(i).getInfo();
   sourceRupInfoText.setText(rupturesInfo);
   progress.showProgress(false);

   //getting the selected rupture fro the source
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
   listEditor= new ParameterListEditor(parameterList);

   // now make the editor based on the parameter list
   listEditor.setTitle( EQK_RUP_SELECTOR_TITLE );

   if(!this.hypoCentreCheck.isSelected()){
     probEqkRupture.setHypocenterLocation(null);
     listEditor.getParameterEditor(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME).setVisible(false);
   }
   else{
     listEditor.getParameterEditor(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME).setVisible(true);
     //getting the HypoCenterLocation Object and setting the Rupture HypocenterLocation
     probEqkRupture.setHypocenterLocation(getHypocenterLocation());
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
   showAllAdjustableParamForERF= true;
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
     if(showAllAdjustableParamForERF){
       // if this forecast has not been selected yet, pop up the adjustable params window
       if(!this.alreadySeenERFs.contains(event.getNewValue()))  {
         getAllERFAdjustableParams();
         alreadySeenERFs.add(event.getNewValue());
       }
       setParamsInForecast(0,0);
     }
   }

   // if source selected by the user  changes
   if( name1.equals(this.SOURCE_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     int sourceValue = Integer.parseInt(value.substring(0,value.indexOf("(")).trim());
     // set the new forecast parameters. Also change the number of ruptures in this source
     setParamsInForecast(sourceValue,0);
   }

   // if source selected by the user  changes
   if( name1.equals(this.RUPTURE_PARAM_NAME) ){
     String value = event.getNewValue().toString();
     String sourceParamVal = (String)listEditor.getParameterList().getParameter(SOURCE_PARAM_NAME).getValue();
     int sourceValue = Integer.parseInt(sourceParamVal.substring(0,sourceParamVal.indexOf("(")).trim());
     // set the new forecast parameters. Also change the number of ruptures in this source
     setParamsInForecast(sourceValue,Integer.parseInt(value));
   }


    //if the Hypo Center location has been set
   if(name1.equals(this.RUPTURE_HYPOLOCATIONS_PARAM_NAME)){
     probEqkRupture.setHypocenterLocation(getHypocenterLocation());
   }
 }

   private void jbInit() throws Exception {
    erfAdjParamButton.setText("Set Eqk Rup Forecast Params");
    erfAdjParamButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        erfAdjParamButton_actionPerformed(e);
      }
    });
    erfAdjParamButton.setBackground(SystemColor.control);
    this.setLayout(gridBagLayout1);
    sourceRupInfoText.setLineWrap(true);
    sourceRupInfoText.setForeground(Color.blue);
    sourceRupInfoText.setSelectedTextColor(new Color(80, 80, 133));
    sourceRupInfoText.setSelectionColor(Color.blue);
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
   * see if we have to show all the Adjustable Params for the ERF in a seperate window
   * when user selects different ERF.
   * @param flag: Based on the boolean flag the ERF adjuatable Param List is shown
   * if user changes the selcetd ERF.
   */
  public void showAllParamsForForecast(boolean flag){
    showAllAdjustableParamForERF = flag;
  }

  /**
   *
   * @returns the instance of the ERF_GuiBean that holds all the Adjustable Params
   * for the selecetd ERF.
   */
  public ERF_GuiBean getERF_ParamEditor(){
    return erfGuiBean;
  }

  /**
   *
   * @returns the Metadata String of parameters that constitute the making of this
   * ERF_RupSelectorGUI  bean.
   */
  public String getParameterListMetadataString(){
    erfGuiBean.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setVisible(false);
    String metadata = getParameterListEditor().getVisibleParameters().getParameterListMetadataString()+";"+
                      erfGuiBean.getVisibleParameters().getParameterListMetadataString()+"<br>"+
                      "<br>\nRupture Info: "+probEqkRupture.getInfo();
    return metadata;
  }

  /**
   * Sets the instance of ERF_GuiBean from the application
   * @param erfGuiBean
   */
  public void setERF_ParamEditor(ERF_GuiBean erfGuiBean){
    this.erfGuiBean = erfGuiBean;
    setParamsInForecast(0,0);
  }

  /**
   * Adds the ERF's to the existing ERF List in the gui bean to be displayed in the gui.
   * This function allows user to add the more ERF's names to the existing list from the application.
   * This function allows user with the flexibility that he does not always have to specify the erfNames
   * at time of instantiating this ERF gui bean.
   * @param erfList
   * @throws InvocationTargetException
   */
  public void addERFs_ToList(ArrayList erfList) throws InvocationTargetException{
    erfGuiBean.addERFs_ToList(erfList);
  }

  /**
   * Removes the ERF's from the existing ERF List in the gui bean to be displayed in the gui.
   * This function allows user to remove ERF's names from the existing list from the application.
   * This function allows user with the flexibility that he can always remove the erfNames
   * later after instantiating this ERF gui bean.
   * @param erfList
   * @throws InvocationTargetException
   */
  public void removeERFs_FromList(ArrayList erfList) throws InvocationTargetException{
    erfGuiBean.removeERFs_FromList(erfList);
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
    //button.setForeground(new Color(80,80,133));
    //button.setBackground(new Color(200,200,230));
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
     EqkRupForecastAPI erfAPI=null;
     try{
       erfAPI = erfGuiBean.getSelectedERF_Instance();
     }catch(Exception e){
       e.printStackTrace();
     }
     return erfAPI;
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
    String sourceValue = (String)listEditor.getParameterList().getParameter(this.SOURCE_PARAM_NAME).getValue();

    int sourceIndex = Integer.parseInt(sourceValue.substring(0,sourceValue.indexOf("(")).trim());
    int ruptureIndex = ((Integer)listEditor.getParameterList().getParameter(this.RUPTURE_PARAM_NAME).getValue()).intValue();
    setParamsInForecast(sourceIndex,ruptureIndex);
  }

  //returns the parameterListEditor
  public ParameterListEditor getParameterListEditor(){
    return listEditor;
  }

  /**
   *
   * @returns the selected source number for the EarthquakeRuptureForecast
   */
  public int getSourceIndex(){
    String sourceValue = (String)listEditor.getParameterList().getParameter(this.SOURCE_PARAM_NAME).getValue();
    int sourceIndex = Integer.parseInt(sourceValue.substring(0,sourceValue.indexOf("(")).trim());
    return sourceIndex;
  }

  /**
   *
   * @returns the selected rupture number for the selected source.
   */
  public int getRuptureIndex(){
    int ruptureIndex = ((Integer)listEditor.getParameterList().getParameter(this.RUPTURE_PARAM_NAME).getValue()).intValue();
    return ruptureIndex;
  }

  /**
   *
   * @returns the Hypocenter Location if selected else return null
   */
  public Location getHypocenterLocation(){
    if(this.hypoCentreCheck.isSelected()){
      StringTokenizer token = new StringTokenizer(listEditor.getParameterList().getParameter(RUPTURE_HYPOLOCATIONS_PARAM_NAME).getValue().toString());
      double lat= Double.parseDouble(token.nextElement().toString().trim());
      double lon= Double.parseDouble(token.nextElement().toString().trim());
      double depth= Double.parseDouble(token.nextElement().toString().trim());
      Location loc= new Location(lat,lon,depth);
      return loc;
    }
    return null;
  }
}