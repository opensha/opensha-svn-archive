package org.opensha.nshmp.sha.gui.beans;

import java.rmi.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.opensha.data.*;
import org.opensha.data.region.*;
import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.nshmp.exceptions.*;
import org.opensha.nshmp.sha.data.*;
import org.opensha.nshmp.sha.data.api.*;
import org.opensha.nshmp.sha.gui.api.*;
import org.opensha.nshmp.util.*;

/**
 * <p>Title: IRC_GuiBean</p>
 *
 * <p>Description: This class creates the GUI interface for the International
 * Residential Code.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */


public class IRC_GuiBean
    extends JPanel implements ParameterChangeListener,
    AnalysisOptionsGuiBeanAPI {

  //Dataset selection Gui instance
  protected DataSetSelectionGuiBean datasetGui;
  protected LocationGuiBean locGuiBean;
  JSplitPane mainSplitPane = new JSplitPane();
  JSplitPane locationSplitPane = new JSplitPane();
  JPanel regionPanel = new JPanel();
  JPanel basicParamsPanel = new JPanel();

  JButton residentialSiteCategoryButton = new JButton();

  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder basicParamBorder = new TitledBorder(border9,
      "Calculate IRC site Values");
  TitledBorder regionBorder = new TitledBorder(border9,
                                               "Region and DataSet Selection");

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout3 = new GridBagLayout();

  GridBagLayout gridBagLayout4 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  protected boolean locationVisible;

  //creating the Ground Motion selection parameter
  protected StringParameter groundMotionParam;
  protected ConstrainedStringParameterEditor groundMotionParamEditor;
  protected static final String GROUND_MOTION_PARAM_NAME = "Ground Motion";

  protected DataGeneratorAPI_NEHRP dataGenerator = new DataGenerator_IRC();

  //instance of the application using this GUI bean
  protected ProbabilisticHazardApplicationAPI application;

  protected String selectedRegion, selectedEdition, spectraType;

  public IRC_GuiBean(ProbabilisticHazardApplicationAPI api) {
    application = api;
    try {

      datasetGui = new DataSetSelectionGuiBean();
      locGuiBean = new LocationGuiBean();
      try {
        createGeographicRegionSelectionParameter();
      }
      catch (AnalysisOptionNotSupportedException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                                      "Analysis Option selection error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
      createEditionSelectionParameter();
      //creating the datasetEditor to show the geographic region and edition dataset.
      datasetGui.createDataSetEditor();
      createLocation();

      createGroundMotionParameter();
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    basicParamsPanel.add(groundMotionParamEditor,
                         new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.NORTH,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 2, 2, 2), 0, 0));

    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 0, 0, 0), 0, 0));
    this.updateUI();
  }

  protected void createGroundMotionParameter() {

    ArrayList supportedGroundMotion = getSupportedSpectraTypes();
    groundMotionParam = new StringParameter(GROUND_MOTION_PARAM_NAME,
                                            supportedGroundMotion,
                                            (String) supportedGroundMotion.get(
        0));
    groundMotionParamEditor = new ConstrainedStringParameterEditor(
        groundMotionParam);
    spectraType = (String) groundMotionParam.getValue();
  }

  protected ArrayList getSupportedSpectraTypes() {
    ArrayList supportedSpectraTypes = new ArrayList();
    supportedSpectraTypes.add(GlobalConstants.MCE_GROUND_MOTION);
    return supportedSpectraTypes;
  }

  protected void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

    basicParamsPanel.setLayout(gridBagLayout4);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);

    residentialSiteCategoryButton.setText(
        "Calc site coeff.");
    residentialSiteCategoryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        residentialSiteCategoryButton_actionPerformed(actionEvent);
      }
    });

    regionPanel.setBorder(regionBorder);
    regionBorder.setTitleColor(Color.RED);
    regionPanel.setLayout(gridBagLayout2);

    mainSplitPane.add(locationSplitPane, JSplitPane.TOP);
    mainSplitPane.add(basicParamsPanel, JSplitPane.BOTTOM);
    locationSplitPane.add(regionPanel, JSplitPane.TOP);

    basicParamsPanel.add(residentialSiteCategoryButton,
                         new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.NORTH,
                                                GridBagConstraints.NONE,
                                                new Insets(4, 30, 4,110), 0, 0));
    this.add(mainSplitPane, java.awt.BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(470);
    locationSplitPane.setDividerLocation(200);

  }

  /**
   * Removes all the output from the window
   */
  public void clearData() {
    dataGenerator.clearData();

  }

  /**
   * If GuiBean parameter is changed.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {

    String paramName = event.getParameterName();

    if (paramName.equals(datasetGui.GEOGRAPHIC_REGION_SELECTION_PARAM_NAME)) {
      selectedRegion = datasetGui.getSelectedGeographicRegion();
      createEditionSelectionParameter();
      createLocation();
    }
    else if (paramName.equals(datasetGui.EDITION_PARAM_NAME)) {
      selectedEdition = datasetGui.getSelectedDataSetEdition();
    }
  }

  /**
   * Returns the instance of itself
   * @return JPanel
   */
  public JPanel getGuiBean() {
    return this;
  }

  /**
   * Creating the location gui bean
   */
  protected void createLocation() {
    RectangularGeographicRegion region = getRegionConstraint();
    Component comp = locationSplitPane.getBottomComponent();
    if (comp != null) {
      locationSplitPane.remove(locationSplitPane.getBottomComponent());
    }
    if (region != null) {
      locationVisible = true;
      //checking if Zip code is supported by the selected choice
      boolean zipCodeSupported = LocationUtil.
          isZipCodeSupportedBySelectedEdition(selectedRegion);
      locGuiBean.createLocationGUI(region.getMinLat(), region.getMaxLat(),
                                   region.getMinLon(), region.getMaxLon(),
                                   zipCodeSupported);
      ParameterList paramList = locGuiBean.getLocationParameters();
      ListIterator it = paramList.getParametersIterator();
      while (it.hasNext()) {
        ParameterAPI param = (ParameterAPI) it.next();
        param.addParameterChangeListener(this);
      }
      locationSplitPane.add(locGuiBean, JSplitPane.BOTTOM);
      locationSplitPane.setDividerLocation(200);
    }
    else if (region == null) {
      locationVisible = false;
    }

  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  protected RectangularGeographicRegion getRegionConstraint() {

    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES) ||
        selectedRegion.equals(GlobalConstants.ALASKA) ||
        selectedRegion.equals(GlobalConstants.HAWAII)) {

      return RegionUtil.getRegionConstraint(selectedRegion);
    }

    return null;
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.IRC_2003);
    supportedEditionList.add(GlobalConstants.IRC_2000);
    if (!selectedRegion.equals(GlobalConstants.ALASKA) &&
        !selectedRegion.equals(GlobalConstants.HAWAII)) {
      supportedEditionList.add(GlobalConstants.IRC_2004);
      supportedEditionList.add(GlobalConstants.IRC_2006);
    }
    datasetGui.createEditionSelectionParameter(supportedEditionList);
    datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
    selectedEdition = datasetGui.getSelectedDataSetEdition();
  }

  /**
   *
   * Creating the parameter that allows user to choose the geographic region list
   * if selected Analysis option is NEHRP.
   *
   */
  protected void createGeographicRegionSelectionParameter() throws
      AnalysisOptionNotSupportedException {

    ArrayList supportedRegionList = RegionUtil.
        getSupportedGeographicalRegions(GlobalConstants.INTL_RESIDENTIAL_CODE);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }

  /**
   * Gets the SA Period and Values from datafiles
   */
  protected void getDataForSA_Period() {

    dataGenerator.setSpectraType(spectraType);
    dataGenerator.setRegion(selectedRegion);
    dataGenerator.setEdition(selectedEdition);

    //doing the calculation if not territory and Location GUI is visible
    if (locationVisible) {
      String locationMode = locGuiBean.getLocationMode();
      if (locationMode.equals(locGuiBean.LAT_LON)) {
        try {
          Location loc = locGuiBean.getSelectedLocation();
          double lat = loc.getLatitude();
          double lon = loc.getLongitude();
          dataGenerator.calculateSsS1(lat, lon);
        }
        catch (LocationErrorException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Location Error",
                                        JOptionPane.OK_OPTION);
          return;
        }
        catch (RemoteException e) {
          JOptionPane.showMessageDialog(this,
                                        e.getMessage() + "\n" +
                                        "Please check your network connection",
                                        "Server Connection Error",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }

      }
      else if (locationMode.equals(locGuiBean.ZIP_CODE)) {
        try {
          String zipCode = locGuiBean.getZipCode();
          dataGenerator.calculateSsS1(zipCode);
        }
        catch (ZipCodeErrorException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Zip Code Error",
                                        JOptionPane.OK_OPTION);
          return;
        }
        catch (LocationErrorException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Location Error",
                                        JOptionPane.OK_OPTION);
          return;
        }
        catch (RemoteException e) {
          JOptionPane.showMessageDialog(this,
                                        e.getMessage() + "\n" +
                                        "Please check your network connection",
                                        "Server Connection Error",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
    }
    else { // if territory and location Gui is not visible
      try {
        dataGenerator.calculateSsS1();
      }
      catch (RemoteException e) {
        JOptionPane.showMessageDialog(this,
                                      e.getMessage() + "\n" +
                                      "Please check your network connection",
                                      "Server Connection Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
  }

  /**
   *
   * @return String
   */
  public String getData() {
    return dataGenerator.getDataInfo();
  }

  protected void residentialSiteCategoryButton_actionPerformed(ActionEvent
      actionEvent) {
    getDataForSA_Period();
    application.setDataInWindow(getData());
  }
}
