package gov.usgs.sha.gui.beans;


import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.data.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;


/**
 * <p>Title: LocationGuiBean</p>
 *
 * <p>Description: This gui allows user to select location.</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LocationGuiBean
    extends  JPanel implements ParameterChangeListener,ParameterChangeFailListener{


  public static final String LOCATION_SELECTION_MODE_PARAM_NAME = "Set Location";
  private static final String LOCATION_SELECTION_MODE_INFO = "Provides user with modes for "+
      "setting the location";
  public static final String ZIP_CODE = "Using Zip Code";
  public static final String LAT_LON = "Using Location Lat-Lon";

  public static final String ZIP_CODE_PARAM_NAME = "Enter 5-digit Zip Code";
  public static final String LAT_PARAM_NAME = "Enter Latitude";
  public static final String LON_PARAM_NAME = "Enter Longitude";

  private StringParameter locationSelectionModeParam;

  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder locationBorder = new TitledBorder(border9, "Select Site Location");


  private ParameterList parameterList;
  private ParameterListEditor editor;
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public LocationGuiBean() {

    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Returns the Location object
   * @return Location
   */
  public Location getSelectedLocation(){
    double lat = ((Double)parameterList.getParameter(LAT_PARAM_NAME).getValue()).doubleValue();
    double lon = ((Double)parameterList.getParameter(LON_PARAM_NAME).getValue()).doubleValue();
    return new Location(lat,lon);
  }


  /**
   * Returns what how user has chosen to set the location
   * @return String
   */
  public String getLocationMode(){
    return (String)locationSelectionModeParam.getValue();
  }

  /**
   * Returns zip code
   * @return String
   */
  public String getZipCode(){
    return (String)parameterList.getParameter(ZIP_CODE_PARAM_NAME).getValue();
  }

  /**
   *
   */
  public void createLocationGUI(double minLat, double maxLat, double minLon,
                                double maxLon, boolean isZipCodeSupported) {
    DoubleParameter latParam = new DoubleParameter(LAT_PARAM_NAME, minLat,
        maxLat, "Degrees");
    DoubleParameter lonParam = new DoubleParameter(LON_PARAM_NAME, minLon,
        maxLon, "Degrees");

    //add the zip code in the location mode selection only if it is supported.
    createLocationModeParam(isZipCodeSupported);

    parameterList = new ParameterList();
    parameterList.addParameter(locationSelectionModeParam);
    parameterList.addParameter(latParam);
    parameterList.addParameter(lonParam);
    latParam.addParameterChangeFailListener(this);
    lonParam.addParameterChangeFailListener(this);

    StringParameter zipParam = null;
    if (isZipCodeSupported) {
      zipParam = new StringParameter(ZIP_CODE_PARAM_NAME,"");
      zipParam.addParameterChangeListener(this);
      parameterList.addParameter(zipParam);
    }

    editor = new ParameterListEditor(parameterList);
    editor.setTitle("");
    setVisibleParameters();
    this.removeAll();
    this.add(editor,
             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                    , GridBagConstraints.CENTER,
                                    GridBagConstraints.BOTH,
                                    new Insets(4, 4, 4, 4), 0, 0));
  }

  /**
   * Creates the LocationMode selection parameter by checking if
   * Zip code is supported by selected geographic region for the selected data edition.
   */
  private void createLocationModeParam(boolean showZipCodeOption){
    ArrayList locationModeChoices = new ArrayList();
    locationModeChoices.add(LAT_LON);
    if(showZipCodeOption)
      locationModeChoices.add(ZIP_CODE);

    locationSelectionModeParam = new StringParameter(LOCATION_SELECTION_MODE_PARAM_NAME,
        locationModeChoices,(String)locationModeChoices.get(0));
    locationSelectionModeParam.addParameterChangeListener(this);
  }



  /**
   * If user changes the location selection mode.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {
    String paramName = event.getParameterName();

    if(paramName.equals(LOCATION_SELECTION_MODE_PARAM_NAME))
      setVisibleParameters();
    else if(paramName.equals(ZIP_CODE_PARAM_NAME)){
      try{
        String zip = (String) parameterList.getParameter(ZIP_CODE_PARAM_NAME).
            getValue();
        if(zip.length() !=5)
          throw new RuntimeException("Please enter valid 5 digit numeric zip code");
        long zipCode = Long.parseLong(zip);
      }catch(NumberFormatException e){
        JOptionPane.showMessageDialog(this,"Please enter valid 5 digit numeric zip code","Zip Code Error",JOptionPane.ERROR_MESSAGE);
      }catch(RuntimeException e){
        JOptionPane.showMessageDialog(this,e.getMessage(),"Zip Code Error",JOptionPane.ERROR_MESSAGE);
      }
    }
    this.updateUI();
  }


  public void parameterChangeFailed(ParameterChangeFailEvent event){


    StringBuffer b = new StringBuffer();

    ParameterAPI param = ( ParameterAPI ) event.getSource();
    String oldValueStr = event.getOldValue().toString();
    String badValueStr = event.getBadValue().toString();
    String name = param.getName();

    //if Lat and Lon parameter constraints are violated
    if(!name.equals(ZIP_CODE)){
      ParameterConstraintAPI constraint = param.getConstraint();
      b.append("The value ");
      b.append(badValueStr);
      b.append(" is not permitted for '");
      b.append(name);
      b.append("'.\n");
      b.append("Resetting to ");
      b.append(oldValueStr);
      b.append(". The constraints are: \n");
      b.append(constraint.toString());

      JOptionPane.showMessageDialog(
          this, b.toString(),
          "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
          );
    }
  }

  /*
   * Makes the parameter visible based on the choice of location selection made by the user
   */
  private void setVisibleParameters(){
    String locationMode = (String)locationSelectionModeParam.getValue();

    if(locationMode.equals(ZIP_CODE)){
      if(parameterList.containsParameter(ZIP_CODE_PARAM_NAME))
         editor.getParameterEditor(ZIP_CODE_PARAM_NAME).setVisible(true);
      editor.getParameterEditor(LAT_PARAM_NAME).setVisible(false);
      editor.getParameterEditor(LON_PARAM_NAME).setVisible(false);
    }
    else{
      if(parameterList.containsParameter(ZIP_CODE_PARAM_NAME))
        editor.getParameterEditor(ZIP_CODE_PARAM_NAME).setVisible(false);
      editor.getParameterEditor(LAT_PARAM_NAME).setVisible(true);
      editor.getParameterEditor(LON_PARAM_NAME).setVisible(true);
    }
    editor.refreshParamEditor();
  }



  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    this.setBorder(locationBorder);
    locationBorder.setTitleColor(Color.RED);
  }

}
