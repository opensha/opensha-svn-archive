package org.opensha.nshmp.sha.gui.beans;

import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import org.opensha.data.*;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.nshmp.exceptions.*;

/**
 * <p>Title: LocationGuiBean</p>
 *
 * <p>Description: This gui allows user to select location.</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LocationGuiBean
    extends JPanel implements ParameterChangeListener,
    ParameterChangeFailListener {

  public static final String LOCATION_SELECTION_MODE_PARAM_NAME =
      "Set Location";
  private static final String LOCATION_SELECTION_MODE_INFO =
      "Provides user with modes for " +
      "setting the location";
  public static final String ZIP_CODE = "Using Zip Code";
  public static final String LAT_LON = "Using Location Lat-Lon";

  public static final String ZIP_CODE_PARAM_NAME = "5-digit Zip Code";
  public static final String LAT_PARAM_NAME = "Latitude";
  public static final String LON_PARAM_NAME = "Longitude";

  private StringParameter locationSelectionModeParam;

  Border border9 = BorderFactory.createLineBorder(new Color(80,80,140),1);
  TitledBorder locationBorder = new TitledBorder(border9,
                                                 "Select Site Location");

  private static final String DEFAULT_ZIP_CODE = "91104";
  private static final double DEFAULT_LAT = 34.1670;
  private static final double DEFAULT_LON = -118.27;

  private ParameterList parameterList;
  private GridBagLayout gridBagLayout1 = new GridBagLayout();


  //ZipCode, Lat, Lon editor
  private StringParameterEditor zipCodeEditor;
  private ConstrainedStringParameterEditor locationModeEditor;
  private ConstrainedDoubleParameterEditor latEditor;
  private ConstrainedDoubleParameterEditor lonEditor;

  private JPanel locationPanel = new JPanel();

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
  public Location getSelectedLocation() throws LocationErrorException {
    Double latObj = (Double) parameterList.getParameter(LAT_PARAM_NAME).
        getValue();
    Double lonObj = (Double) parameterList.getParameter(LON_PARAM_NAME).
        getValue();

    if (latObj == null || lonObj == null) {
      throw new LocationErrorException(
          "Location not specified!\nPlease fill in the location parameter.");
    }
    else {
      double lat = latObj.doubleValue();
      double lon = lonObj.doubleValue();
      return new Location(lat, lon);
    }
  }

  /**
   * Returns the parameters constituting the location gui bean.
   * @return ParameterList
   */
  public ParameterList getLocationParameters() {
    return parameterList;
  }

  /**
   * Returns what how user has chosen to set the location
   * @return String
   */
  public String getLocationMode() {
    return (String) locationSelectionModeParam.getValue();
  }

  /**
   * Returns zip code
   * @return String
   */
  public String getZipCode() throws LocationErrorException {

    String zipCode = (String) parameterList.getParameter(ZIP_CODE_PARAM_NAME).
        getValue();

    if (zipCode == null) {
      throw new LocationErrorException(
          "Zip Code not specified!\nPlease fill in the valid location.");
    }

    return zipCode;
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

    this.locationPanel.removeAll();
    //add the zip code in the location mode selection only if it is supported.
    createLocationModeParam(isZipCodeSupported);

    parameterList = new ParameterList();
    parameterList.addParameter(locationSelectionModeParam);
    parameterList.addParameter(latParam);
    parameterList.addParameter(lonParam);
    latParam.addParameterChangeFailListener(this);
    lonParam.addParameterChangeFailListener(this);
    try {
      locationModeEditor = new ConstrainedStringParameterEditor(
          locationSelectionModeParam);
      latEditor = new ConstrainedDoubleParameterEditor(latParam);
      lonEditor = new ConstrainedDoubleParameterEditor(lonParam);
      locationPanel.add(locationModeEditor,
                        new GridBagConstraints(0, 0, 0, 1, 1.0, 1.0
                                               , GridBagConstraints.NORTH,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(2, 2, 2, 2), 0, 0));
      locationPanel.add(latEditor, new GridBagConstraints(0, 1, 0, 1, 1.0, 1.0
          , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(2, 2, 2, 2), 0, 0));
      locationPanel.add(lonEditor, new GridBagConstraints(0, 2, 0, 1, 1.0, 1.0
          , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(2, 2, 2, 2), 0, 0));
    }
    catch (Exception e) {
      e.printStackTrace();
    }




    StringParameter zipParam = null;
    if (isZipCodeSupported) {
      zipParam = new StringParameter(ZIP_CODE_PARAM_NAME, "");
      zipParam.addParameterChangeListener(this);
      parameterList.addParameter(zipParam);
      try{
        zipCodeEditor = new StringParameterEditor(zipParam);
        locationPanel.add(zipCodeEditor, new GridBagConstraints(0, 1, 0, 1, 1.0, 1.0
          , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(2, 2, 2, 2), 0, 0));
      }catch(Exception e){
        e.printStackTrace();
      }
    }
    locationPanel.setMinimumSize(new Dimension(0,0));
    setVisibleParameters();
    locationPanel.updateUI();
  }

  /**
   * Creates the LocationMode selection parameter by checking if
   * Zip code is supported by selected geographic region for the selected data edition.
   */
  private void createLocationModeParam(boolean showZipCodeOption) {
    ArrayList locationModeChoices = new ArrayList();
    locationModeChoices.add(LAT_LON);
    if (showZipCodeOption) {
      locationModeChoices.add(ZIP_CODE);
    }

    locationSelectionModeParam = new StringParameter(
        LOCATION_SELECTION_MODE_PARAM_NAME,
        locationModeChoices, (String) locationModeChoices.get(0));
    locationSelectionModeParam.addParameterChangeListener(this);
  }

  /**
   * If user changes the location selection mode.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {
    String paramName = event.getParameterName();

    if (paramName.equals(LOCATION_SELECTION_MODE_PARAM_NAME)) {
      setVisibleParameters();
    }
    else if (paramName.equals(ZIP_CODE_PARAM_NAME)) {
      try {
        String zip = (String) parameterList.getParameter(ZIP_CODE_PARAM_NAME).
            getValue();
        if (zip.length() != 5) {
          throw new RuntimeException(
              "Please enter valid 5 digit numeric zip code");
        }
        long zipCode = Long.parseLong(zip);
      }
      catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this,
            "Please enter valid 5 digit numeric zip code", "Zip Code Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Zip Code Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
    this.updateUI();
  }

  public void parameterChangeFailed(ParameterChangeFailEvent event) {

    StringBuffer b = new StringBuffer();

    ParameterAPI param = (ParameterAPI) event.getSource();
    Object oldValue = event.getOldValue();
    String oldValueStr = null;
    if (oldValue != null) {
      oldValueStr = oldValue.toString();
    }

    String badValueStr = event.getBadValue().toString();
    String name = param.getName();

    //if Lat and Lon parameter constraints are violated
    if (!name.equals(ZIP_CODE)) {
      ParameterConstraintAPI constraint = param.getConstraint();
      b.append("The value ");
      b.append(badValueStr);
      b.append(" is not permitted for '");
      b.append(name);
      b.append("'.\n");
      b.append("Resetting to ");
      if (oldValueStr != null) {
        b.append(oldValueStr);
      }
      else {
        b.append("Null");
      }
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
  private void setVisibleParameters() {
    String locationMode = (String) locationSelectionModeParam.getValue();

    if (locationMode.equals(ZIP_CODE)) {
      if (parameterList.containsParameter(ZIP_CODE_PARAM_NAME))
        zipCodeEditor.setVisible(true);

      lonEditor.setVisible(false);
      latEditor.setVisible(false);
    }
    else {
      if (parameterList.containsParameter(ZIP_CODE_PARAM_NAME))
        zipCodeEditor.setVisible(false);
      lonEditor.setVisible(true);
      latEditor.setVisible(true);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    this.setBorder(locationBorder);
    locationBorder.setTitleColor(Color.RED);
    locationPanel.setLayout(gridBagLayout1);
    this.add(locationPanel,
         new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                , GridBagConstraints.CENTER,
                                GridBagConstraints.BOTH,
                                new Insets(4, 4, 4, 4), 0, 0));

  }

}
