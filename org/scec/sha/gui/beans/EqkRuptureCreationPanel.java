package org.scec.sha.gui.beans;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.text.DecimalFormat;

import org.scec.param.*;
import org.scec.param.event.*;
import org.scec.param.editor.*;
import org.scec.sha.param.SimpleFaultParameter;
import org.scec.sha.earthquake.EqkRupture;
import org.scec.sha.surface.*;
import org.scec.data.*;
import java.awt.event.*;

/**
 * <p>Title: EqkRuptureCreationPanel</p>
 * <p>Description: </p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class EqkRuptureCreationPanel extends  JPanel
    implements EqkRupSelectorGuiBeanAPI,ParameterChangeListener{

  // mag parameter stuff
  public final static String MAG_PARAM_NAME = "Magnitude";
  private final static String MAG_PARAM_INFO = "The  magnitude of the rupture";
  private final static String MAG_PARAM_UNITS = null;
  private Double MAG_PARAM_MIN = new Double(0);
  private Double MAG_PARAM_MAX = new Double(10);
  private Double MAG_PARAM_DEFAULT = new Double(7.0);

  // rake parameter stuff
  public final static String RAKE_PARAM_NAME = "Rake";
  private final static String RAKE_PARAM_INFO = "The rake of the rupture (direction of slip)";
  private final static String RAKE_PARAM_UNITS = "degrees";
  private Double RAKE_PARAM_MIN = new Double(-180);
  private Double RAKE_PARAM_MAX = new Double(180);
  private Double RAKE_PARAM_DEFAULT = new Double(0.0);

  // dip parameter stuff
  public final static String DIP_PARAM_NAME = "Dip";
  private final static String DIP_PARAM_INFO = "The dip of the rupture surface";
  private final static String DIP_PARAM_UNITS = "degrees";
  private Double DIP_PARAM_MIN = new Double(0);
  private Double DIP_PARAM_MAX = new Double(90);
  private Double DIP_PARAM_DEFAULT = new Double(90);

  // the source-location parameters (this should be a location parameter)
  public final static String SRC_LAT_PARAM_NAME = "Source Latitude";
  private final static String SRC_LAT_PARAM_INFO = "Latitude of the point source";
  private final static String SRC_LAT_PARAM_UNITS = "Degrees";
  private Double SRC_LAT_PARAM_MIN = new Double (-90.0);
  private Double SRC_LAT_PARAM_MAX = new Double (90.0);
  private Double SRC_LAT_PARAM_DEFAULT = new Double (35.71);

  public final static String SRC_LON_PARAM_NAME = "Source Longitude";
  private final static String SRC_LON_PARAM_INFO = "Longitude of the point source";
  private final static String SRC_LON_PARAM_UNITS = "Degrees";
  private Double SRC_LON_PARAM_MIN = new Double (-360);
  private Double SRC_LON_PARAM_MAX = new Double (360);
  private Double SRC_LON_PARAM_DEFAULT = new Double (-121.1);

  public final static String SRC_DEPTH_PARAM_NAME = "Source Depth";
  private final static String SRC_DEPTH_PARAM_INFO = "Depth of the point source";
  private final static String SRC_DEPTH_PARAM_UNITS = "km";
  private Double SRC_DEPTH_PARAM_MIN = new Double (0);
  private Double SRC_DEPTH_PARAM_MAX = new Double (50);
  private Double SRC_DEPTH_PARAM_DEFAULT = new Double (7.6);

  //Param to select "kind of rupture", finite or point rupture
  public final static String SRC_TYP_PARAM_NAME = "Rupture Type";
  private final static String SRC_TYP_PARAM_INFO = "Type of rupture";
  public final static String POINT_SRC_NAME = "Point source rupture";
  public final static String FINITE_SRC_NAME = "Finite source rupture";

  //Finite rupture parameters
  public final static String FAULT_PARAM_NAME = "Set Fault Surface";
  private final static String FAULT_PARAM_INFO = "Source location parameters for finite rupture";


  //Parameter to check if Hypocenter needs to be set
  public final static String HYPOCENTER_PARAM_NAME = "Set hypocenter";
  private final static String HYPOCENTER_PARAM_INFO = "Checks if Hypocenter needs to be set";

  //Parameter to set the Hypocenter Location
  public final static String HYPOCENTER_LOCATION_PARAM_NAME = "Hypocenter Location";
  private final static String HYPOCENTER_LOCATION_PARAM_INFO = "Sets the Hypocenter Location";

  //title for this ParamerterListEditor
  private final static String TITLE= "";


  //Parameter declarations
  private StringParameter sourceTypeParam;
  private DoubleParameter magParam;
  private DoubleParameter dipParam;
  private DoubleParameter rakeParam;
  private DoubleParameter srcLatParam;
  private DoubleParameter srcLonParam;
  private DoubleParameter srcDepthParam;
  private SimpleFaultParameter faultParam;
  private BooleanParameter hypocenterParam;
  private StringParameter hypocenterLocationParam;


  //boolean to check if any parameter has been changed
  private boolean parameterChangeFlag = true;

  private ParameterList parameterList;
  private ParameterListEditor listEditor;
  private ParameterList hypocenterParameterList;
  private ParameterListEditor hypocenterListEditor;
  private JButton createRuptureButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  //ProbEqkRupture Object
  private EqkRupture eqkRupture;

  public EqkRuptureCreationPanel() {

    // create the mag param
    magParam = new DoubleParameter(MAG_PARAM_NAME,MAG_PARAM_MIN,
        MAG_PARAM_MAX,MAG_PARAM_UNITS,MAG_PARAM_DEFAULT);
    magParam.setInfo(MAG_PARAM_INFO);


    // create the rake param
    rakeParam = new DoubleParameter(RAKE_PARAM_NAME,RAKE_PARAM_MIN,
        RAKE_PARAM_MAX,RAKE_PARAM_UNITS,RAKE_PARAM_DEFAULT);
    rakeParam.setInfo(RAKE_PARAM_INFO);


    // create the rake param
    dipParam = new DoubleParameter(DIP_PARAM_NAME,DIP_PARAM_MIN,
        DIP_PARAM_MAX,DIP_PARAM_UNITS,DIP_PARAM_DEFAULT);
    dipParam.setInfo(DIP_PARAM_INFO);

    // create src lat, lon, & depth param
    srcLatParam = new DoubleParameter(SRC_LAT_PARAM_NAME,SRC_LAT_PARAM_MIN,
        SRC_LAT_PARAM_MAX,SRC_LAT_PARAM_UNITS,SRC_LAT_PARAM_DEFAULT);
    srcLatParam.setInfo(SRC_LAT_PARAM_INFO);
    srcLonParam = new DoubleParameter(SRC_LON_PARAM_NAME,SRC_LON_PARAM_MIN,
        SRC_LON_PARAM_MAX,SRC_LON_PARAM_UNITS,SRC_LON_PARAM_DEFAULT);
    srcLonParam.setInfo(SRC_LON_PARAM_INFO);
    srcDepthParam = new DoubleParameter(SRC_DEPTH_PARAM_NAME,SRC_DEPTH_PARAM_MIN,
        SRC_DEPTH_PARAM_MAX,SRC_DEPTH_PARAM_UNITS,SRC_DEPTH_PARAM_DEFAULT);
    srcDepthParam.setInfo(SRC_DEPTH_PARAM_INFO);

    //creating the Fault Rupture Parameter
    faultParam = new SimpleFaultParameter(FAULT_PARAM_NAME);
    faultParam.setInfo(FAULT_PARAM_INFO);

    //creating the parameter to choose the type of rupture (finite or point src rupture)
    ArrayList ruptureTypeList = new ArrayList();
    ruptureTypeList.add(POINT_SRC_NAME);
    ruptureTypeList.add(FINITE_SRC_NAME);

    sourceTypeParam = new StringParameter(SRC_TYP_PARAM_NAME,ruptureTypeList,(String)ruptureTypeList.get(0));
    sourceTypeParam.setInfo(SRC_TYP_PARAM_INFO);


    hypocenterParam = new BooleanParameter(this.HYPOCENTER_PARAM_NAME,new Boolean(false));
    hypocenterParam.setInfo(HYPOCENTER_PARAM_INFO);

    parameterList = new ParameterList();
    // add the adjustable parameters to the list
    parameterList.addParameter(sourceTypeParam);
    parameterList.addParameter(magParam);
    parameterList.addParameter(rakeParam);
    parameterList.addParameter(dipParam);
    parameterList.addParameter(faultParam);
    parameterList.addParameter(srcLatParam);
    parameterList.addParameter(srcLonParam);
    parameterList.addParameter(srcDepthParam);

    sourceTypeParam.addParameterChangeListener(this);
    magParam.addParameterChangeListener(this);
    rakeParam.addParameterChangeListener(this);
    srcLatParam.addParameterChangeListener(this);
    srcLonParam.addParameterChangeListener(this);
    dipParam.addParameterChangeListener(this);
    faultParam.addParameterChangeListener(this);



    listEditor = new ParameterListEditor(parameterList);
    listEditor.setTitle(TITLE);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    setParameterVisibleBasedOnSelectedRuptureType();
    this.validate();
    this.repaint();
    createRupture();
  }
  private void jbInit() throws Exception {
    createRuptureButton.setText("Create Rupture");
    createRuptureButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createRuptureButton_actionPerformed(e);
      }
    });
    this.setLayout(gridBagLayout1);
    this.add(listEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
           ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    this.add(createRuptureButton,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
  }


  /**
   * Create the EqkRupture Object
   */
  private void createRupture(){
    if(parameterChangeFlag){
      String ruptureType = (String)sourceTypeParam.getValue();
      GriddedSurfaceAPI ruptureSurface = null;
      if(ruptureType.equals(this.POINT_SRC_NAME)){
        double lat = ((Double)srcLatParam.getValue()).doubleValue();
        double lon = ((Double)srcLonParam.getValue()).doubleValue();
        double depth = ((Double)srcDepthParam.getValue()).doubleValue();
        ruptureSurface = new PointSurface(lat,lon,depth);
        double aveDip = ((Double)dipParam.getValue()).doubleValue();
        ruptureSurface.setAveDip(aveDip);
      }
      else if(ruptureType.equals(this.FINITE_SRC_NAME))
        ruptureSurface = (GriddedSurfaceAPI)faultParam.getValue();

      eqkRupture = new EqkRupture();
      eqkRupture.setMag(((Double)magParam.getValue()).doubleValue());
      eqkRupture.setAveRake(((Double)rakeParam.getValue()).doubleValue());
      eqkRupture.setRuptureSurface(ruptureSurface);

      //Deciaml format to show the Hypocenter Location Object in the StringParameter
      DecimalFormat decimalFormat=new DecimalFormat("0.000##");

      // The first row of all the rupture surfaces is the list of their hypocenter locations
      ListIterator hypoLocationsIt = eqkRupture.getRuptureSurface().getColumnIterator(0);
      Location loc;
      ArrayList hypocenterList = new ArrayList();
      while(hypoLocationsIt.hasNext()){
        //getting the object of Location from the HypocenterLocations and formatting its string to 3 placees of decimal
        loc= (Location)hypoLocationsIt.next();
        String lat = decimalFormat.format(loc.getLatitude());
        String lon = decimalFormat.format(loc.getLongitude());
        String depth = decimalFormat.format(loc.getDepth());
        hypocenterList.add(lat+" "+lon+" "+depth);
      }

      if(hypocenterLocationParam == null){ //create the HypocenterLocation parameter if it is null
        //else we just need to change the constraint of the hypocenter location.
        hypocenterLocationParam = new StringParameter(this.HYPOCENTER_LOCATION_PARAM_NAME,hypocenterList,
            (String)hypocenterList.get(0));
        hypocenterLocationParam.setInfo(this.HYPOCENTER_LOCATION_PARAM_INFO);
        hypocenterParameterList = new ParameterList();
        hypocenterParameterList.addParameter(hypocenterParam);
        hypocenterParameterList.addParameter(hypocenterLocationParam);
        hypocenterListEditor = new ParameterListEditor(hypocenterParameterList);
        hypocenterLocationParam.addParameterChangeListener(this);
        hypocenterParam.addParameterChangeListener(this);
        this.add(hypocenterListEditor,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
           ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
        hypocenterListEditor.setTitle("");
      }
      else{
        StringConstraint hypoCenterLocations= new StringConstraint(hypocenterList);
        hypocenterLocationParam.setConstraint(hypoCenterLocations);
      }
      //depending on the Hypocenter parameter Val, makes the hypocenter location
      //parameter visible or invisible
      setHypocenterLocationParamVisible();
      hypocenterListEditor.refreshParamEditor();
    }
    parameterChangeFlag = false;
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

    String name1 = event.getParameterName();

    if(name1.equals(this.SRC_TYP_PARAM_NAME)){
      this.setParameterVisibleBasedOnSelectedRuptureType();
      parameterChangeFlag = true;
      listEditor.refreshParamEditor();
    }
    //if the Hypo Center location has been set
    else if(name1.equals(this.HYPOCENTER_PARAM_NAME)){
      setHypocenterLocationParamVisible();
      hypocenterListEditor.refreshParamEditor();
    }
    else if(name1.equals(this.HYPOCENTER_LOCATION_PARAM_NAME))
      this.eqkRupture.setHypocenterLocation(getHypocenterLocation());
    else //if rupture parameter are changed then just set parameterChangeFlag to be true.
      parameterChangeFlag = true;
  }



  /**
   *
   * @returns the Hypocenter Location if selected else return null
   */
  public Location getHypocenterLocation(){
    boolean hypocenterParamVal = ((Boolean)hypocenterParam.getValue()).booleanValue();
    if(hypocenterParamVal){
      StringTokenizer token = new StringTokenizer(hypocenterLocationParam.getValue().toString());
      double lat= Double.parseDouble(token.nextElement().toString().trim());
      double lon= Double.parseDouble(token.nextElement().toString().trim());
      double depth= Double.parseDouble(token.nextElement().toString().trim());
      Location loc= new Location(lat,lon,depth);
      return loc;
    }
    return null;
  }


  /**
   * Makes Hypocenter Location Parameter visible if hypocenter needs to be set
   * else removes it from the list of visible parameters.
   * If Hypocenter Location parameter is visible then it sets the Hypocenter location
   * in the eqkRupture
   */
  private void setHypocenterLocationParamVisible(){
    boolean hypocenterParamVal = ((Boolean)hypocenterParam.getValue()).booleanValue();
    if(hypocenterParamVal){
      hypocenterListEditor.getParameterEditor(HYPOCENTER_LOCATION_PARAM_NAME).setVisible(true);
      this.eqkRupture.setHypocenterLocation(getHypocenterLocation());
    }
    else
      hypocenterListEditor.getParameterEditor(HYPOCENTER_LOCATION_PARAM_NAME).setVisible(false);
    this.validate();
    this.repaint();
  }

  /**
   * Create new ProbEqkrupture if user has changes any parameter and pressed
   * "Create Rupture" button
   * @param e
   */
  void createRuptureButton_actionPerformed(ActionEvent e) {
    createRupture();
  }


  /**
   *
   * @returns the EqkRupture Object
   */
  public EqkRupture getRupture(){
    return eqkRupture;
  }

  /**
   *
   * @returns the timespan Metadata for the selected Rupture.
   * If no timespan exists for the rupture then it returns the Message:
   * "No Timespan exists for the selected Rupture".
   */
  public String getTimespanMetadataString(){
    return "No Timespan exists for the selected Rupture";
  }


  /**
   * This function makes the those parameters visible which pertains to the
   * selected Rupture type and removes the rest from the list of visible parameters
   *
   */
  private void setParameterVisibleBasedOnSelectedRuptureType(){
    String selectedRuptureType = (String)sourceTypeParam.getValue();
    if(selectedRuptureType.equals(this.POINT_SRC_NAME)){
      listEditor.setParameterVisible(this.SRC_LAT_PARAM_NAME,true);
      listEditor.setParameterVisible(this.SRC_DEPTH_PARAM_NAME,true);
      listEditor.setParameterVisible(this.SRC_LON_PARAM_NAME,true);
      listEditor.setParameterVisible(this.DIP_PARAM_NAME,true);
      listEditor.setParameterVisible(this.FAULT_PARAM_NAME,false);
    }
    else if(selectedRuptureType.equals(this.FINITE_SRC_NAME)){
      listEditor.setParameterVisible(this.SRC_LAT_PARAM_NAME,false);
      listEditor.setParameterVisible(this.SRC_DEPTH_PARAM_NAME,false);
      listEditor.setParameterVisible(this.SRC_LON_PARAM_NAME,false);
      listEditor.setParameterVisible(this.DIP_PARAM_NAME,false);
      listEditor.setParameterVisible(this.FAULT_PARAM_NAME,true);
    }
    listEditor.refreshParamEditor();
    this.validate();
    this.repaint();
  }


  /**
   *
   * @returns the panel which allows user to select Eqk rupture from existing
   * ERF models
   */
  public EqkRupSelectorGuiBeanAPI getEqkRuptureSelectorPanel(){
    return this;
  }



  /**
   *
   * @returns the Metadata String of parameters that constitute the making of this
   * ERF_RupSelectorGUI  bean.
   */
  public String getParameterListMetadataString(){
    String metadata = listEditor.getVisibleParameters().getParameterListMetadataString()+";"+
                      hypocenterListEditor.getVisibleParameters().getParameterListMetadataString();
                    //+"<br>"+  "<br>\nRupture Info: "+eqkRupture.getInfo();
    return metadata;
  }



  /**
   *
   * @param paramName
   * @returns the parameter from the parameterList with paramName.
   */
  public ParameterAPI getParameter(String paramName){
    if(parameterList.containsParameter(paramName)){
      if(listEditor.getParameterEditor(paramName).isVisible())
        return parameterList.getParameter(paramName);
    }
    else if(hypocenterParameterList.containsParameter(paramName)){
      if(hypocenterListEditor.getParameterEditor(paramName).isVisible())
        return hypocenterParameterList.getParameter(paramName);
    }
    return null;
  }


  /**
   *
   * @param paramName
   * @returns the ParameterEditor associated with paramName
   */
  public ParameterEditor getParameterEditor(String paramName){
    if(parameterList.containsParameter(paramName)){
      if(listEditor.getParameterEditor(paramName).isVisible())
        return listEditor.getParameterEditor(paramName);
    }
    else if(hypocenterParameterList.containsParameter(paramName)){
      if(hypocenterListEditor.getParameterEditor(paramName).isVisible())
        return hypocenterListEditor.getParameterEditor(paramName);
    }
    return null;
  }



}