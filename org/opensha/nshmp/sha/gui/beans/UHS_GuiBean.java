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
import org.opensha.nshmp.sha.gui.infoTools.*;
import org.opensha.nshmp.util.*;

/**
 * <p>Title: UHS_GuiBean</p>
 *
 * <p>Description: This option sets the parameter for the NEHRP analysis option.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class UHS_GuiBean
    extends JPanel implements ParameterChangeListener,
    AnalysisOptionsGuiBeanAPI {

  //Dataset selection Gui instance
  protected DataSetSelectionGuiBean datasetGui;
  protected LocationGuiBean locGuiBean;
  JSplitPane mainSplitPane = new JSplitPane();
  JSplitPane locationSplitPane = new JSplitPane();
  JSplitPane buttonsSplitPane = new JSplitPane();
  JPanel regionPanel = new JPanel();
  JPanel basicParamsPanel = new JPanel();
  JPanel responseSpectraButtonPanel = new JPanel();
  JButton uhsButton = new JButton();
  JButton approxUHSButton = new JButton();
  JButton viewUHSButton = new JButton();
  Border border9 = BorderFactory.createLineBorder(new Color(80,80,140),1);
  TitledBorder responseSpecBorder = new TitledBorder(border9,
      "Approximate UHS and UHS-based Design Spectra");

  TitledBorder basicParamBorder = new TitledBorder(border9,
      "Uniform Hazard Spectra (UHS)");
  TitledBorder regionBorder = new TitledBorder(border9,
                                               "Region and DataSet Selection");
  JButton smSpecButton = new JButton();
  JButton sdSpecButton = new JButton();
  JButton viewButton = new JButton();

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout3 = new GridBagLayout();

  GridBagLayout gridBagLayout4 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  //creating the Ground Motion selection parameter
  protected StringParameter groundMotionParam;
  protected ConstrainedStringParameterEditor groundMotionParamEditor;
  protected static final String GROUND_MOTION_PARAM_NAME = "Ground Motion";

  protected DataGeneratorAPI_UHS dataGenerator = new DataGenerator_UHS();

  //site coeffiecient window instance
  SiteCoefficientInfoWindow siteCoefficientWindow;

  //instance of the application using this GUI bean
  protected ProbabilisticHazardApplicationAPI application;

  protected boolean smSpectrumCalculated, sdSpectrumCalculated, uhsCalculated,
      approxUHS_Calculated;

  protected String selectedRegion, selectedEdition, spectraType;

  //checks if site coefficient has been set.
  private boolean siteCoeffWindowShow = false;

  public UHS_GuiBean(ProbabilisticHazardApplicationAPI api) {
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
      locationSplitPane.add(locGuiBean, JSplitPane.BOTTOM);
      locationSplitPane.setDividerLocation(155);
      jbInit();

    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 0, 0, 0), 0, 0));

    this.updateUI();

  }

  protected void createGroundMotionParameter() {

    if (groundMotionParamEditor != null) {
      basicParamsPanel.remove(groundMotionParamEditor);
    }

    ArrayList supportedGroundMotion = getSupportedSpectraTypes();
    groundMotionParam = new StringParameter(GROUND_MOTION_PARAM_NAME,
                                            supportedGroundMotion,
                                            (String) supportedGroundMotion.get(
                                                0));

    groundMotionParam.addParameterChangeListener(this);
    groundMotionParamEditor = new ConstrainedStringParameterEditor(
        groundMotionParam);
    spectraType = (String) groundMotionParam.getValue();

    basicParamsPanel.add(groundMotionParamEditor,
                         new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
                                                , GridBagConstraints.NORTH,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(2, 2, 2, 2), 0, 0));

    groundMotionParamEditor.refreshParamEditor();
    basicParamsPanel.updateUI();
  }

  protected ArrayList getSupportedSpectraTypes() {
    ArrayList supportedSpectraTypes = new ArrayList();
    if (selectedEdition.equals(GlobalConstants.data_1996)) {
      supportedSpectraTypes.add(GlobalConstants.PE_2);
      supportedSpectraTypes.add(GlobalConstants.PE_5);
      supportedSpectraTypes.add(GlobalConstants.PE_10);
    }
    else {
      supportedSpectraTypes.add(GlobalConstants.PE_2);
      supportedSpectraTypes.add(GlobalConstants.PE_10);
    }
    return supportedSpectraTypes;
  }

  protected void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    basicParamsPanel.setLayout(gridBagLayout4);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);

    uhsButton.setText("Calculate");
    uhsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        uhsButton_actionPerformed(actionEvent);
      }
    });

    approxUHSButton.setText("Calc approx.");
    approxUHSButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        approxUHSButton_actionPerformed(actionEvent);
      }
    });


    viewUHSButton.setText("View UHS");
    viewUHSButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewUHSButton_actionPerformed(actionEvent);
      }
    });
    responseSpectraButtonPanel.setBorder(responseSpecBorder);
    responseSpecBorder.setTitleColor(Color.RED);
    responseSpectraButtonPanel.setLayout(gridBagLayout3);


    smSpecButton.setText("Calc SM spec.");
    smSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        smSpecButton_actionPerformed(actionEvent);
      }
    });

    sdSpecButton.setActionCommand("sdSpecButton");
    sdSpecButton.setText("Calc SD spec.");
    sdSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        sdSpecButton_actionPerformed(actionEvent);
      }
    });

    viewButton.setActionCommand("viewButton");
    viewButton.setText("View spec.");
    viewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewButton_actionPerformed(actionEvent);
      }
    });

    regionPanel.setBorder(regionBorder);
    regionBorder.setTitleColor(Color.RED);
    regionPanel.setLayout(gridBagLayout2);

    mainSplitPane.add(locationSplitPane, JSplitPane.TOP);
    mainSplitPane.add(buttonsSplitPane, JSplitPane.BOTTOM);
    locationSplitPane.add(regionPanel, JSplitPane.TOP);

    buttonsSplitPane.add(basicParamsPanel, JSplitPane.TOP);
    buttonsSplitPane.add(responseSpectraButtonPanel, JSplitPane.BOTTOM);

    responseSpectraButtonPanel.add(viewButton,
                                   new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.NORTH, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));

    responseSpectraButtonPanel.add(smSpecButton,
                                   new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.NORTH, GridBagConstraints.NONE,
        new Insets( 2, 2, 2, 2), 0, 0));

    responseSpectraButtonPanel.add(sdSpecButton,
                                   new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.NORTH, GridBagConstraints.NONE,
        new Insets( 2, 2, 2, 2), 0, 0));
    basicParamsPanel.add(uhsButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    basicParamsPanel.add(viewUHSButton,
                         new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0,
                                                0));
    basicParamsPanel.add(approxUHSButton,
                         new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0, 0));
    this.add(mainSplitPane, java.awt.BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(300);
    buttonsSplitPane.setDividerLocation(120);
    setButtonsEnabled(false);
    createGroundMotionParameter();
    basicParamsPanel.setMinimumSize(new Dimension(0,0));
    regionPanel.setMinimumSize(new Dimension(0,0));
    responseSpectraButtonPanel.setMinimumSize(new Dimension(0,0));
    this.updateUI();
  }

  protected void setButtonsEnabled(boolean enableButtons) {
    approxUHSButton.setEnabled(enableButtons);
    viewUHSButton.setEnabled(enableButtons);
    smSpecButton.setEnabled(enableButtons);
    sdSpecButton.setEnabled(enableButtons);
    viewButton.setEnabled(enableButtons);
    if(enableButtons == false)
      siteCoeffWindowShow= false;
  }

  /**
   * Removes all the output from the window
   */
  public void clearData() {
    dataGenerator.clearData();
    setButtonsEnabled(false);
  }


  /**
   * Returns the selected Region
   * @return String
   */
  public String getSelectedRegion(){
    return selectedRegion;
  }

  /**
   * Returns the selected data edition
   * @return String
   */
  public String getSelectedDataEdition(){
    return selectedEdition;
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
      setButtonsEnabled(false);
    }
    else if (paramName.equals(datasetGui.EDITION_PARAM_NAME)) {
      selectedEdition = datasetGui.getSelectedDataSetEdition();
      createGroundMotionParameter();
      setButtonsEnabled(false);
    }
    else if (paramName.equals(GROUND_MOTION_PARAM_NAME)) {
      spectraType = (String) groundMotionParam.getValue();
    }

    else if (paramName.equals(locGuiBean.LAT_PARAM_NAME) ||
             paramName.equals(locGuiBean.LON_PARAM_NAME) ||
             paramName.equals(locGuiBean.ZIP_CODE_PARAM_NAME)) {
      setButtonsEnabled(false);
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

    if (region != null) {
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
    }
    else
      locGuiBean.createNoLocationGUI();

  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  protected RectangularGeographicRegion getRegionConstraint() {
    return RegionUtil.getRegionConstraint(selectedRegion);
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      supportedEditionList.add(GlobalConstants.data_2002);
      supportedEditionList.add(GlobalConstants.data_1996);
    }
    else if (selectedRegion.equals(GlobalConstants.ALASKA) ||
             selectedRegion.equals(GlobalConstants.HAWAII)) {
      supportedEditionList.add(GlobalConstants.data_1998);
    }
    else {
      supportedEditionList.add(GlobalConstants.data_2003);
    }

    datasetGui.createEditionSelectionParameter(supportedEditionList);
    datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
    selectedEdition = datasetGui.getSelectedDataSetEdition();
    createGroundMotionParameter();
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
        getSupportedGeographicalRegions(GlobalConstants.NEHRP);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }

  /**
   * Gets the SA Period and Values from datafiles
   */
  protected void getDataForSA_Period() throws ZipCodeErrorException,
      LocationErrorException,RemoteException{

    dataGenerator.setSpectraType(spectraType);
    dataGenerator.setRegion(selectedRegion);
    dataGenerator.setEdition(selectedEdition);

    if (locGuiBean.getLocationMode()) {
      Location loc = locGuiBean.getSelectedLocation();
      double lat = loc.getLatitude();
      double lon = loc.getLongitude();
      dataGenerator.calculateUHS(lat, lon);
    }
    else {
      String zipCode = locGuiBean.getZipCode();
      dataGenerator.calculateUHS(zipCode);
    }
  }



  protected void uhsButton_actionPerformed(ActionEvent actionEvent) {
    try {
      getDataForSA_Period();
    }
    catch (ZipCodeErrorException ee) {
      JOptionPane.showMessageDialog(this, ee.getMessage(), "Zip Code Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
    catch (LocationErrorException ee) {
      JOptionPane.showMessageDialog(this, ee.getMessage(), "Location Error",
                                    JOptionPane.OK_OPTION);
      return;
    }
    catch (RemoteException ee) {
      JOptionPane.showMessageDialog(this,
                                    ee.getMessage() + "\n" +
                                    "Please check your network connection",
                                    "Server Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    application.setDataInWindow(getData());
    setButtonsEnabled(true);
    viewUHSButton.setEnabled(true);
    uhsCalculated = true;
  }

  /**
   *
   * @return String
   */
  public String getData() {
    return dataGenerator.getDataInfo();
  }

  /**
   * This function pops up the site coefficient window and allows user to set
   * Site coefficient for the calculation.
   */
  protected void setSiteCoeff() {

    if(!siteCoeffWindowShow){
      if (siteCoefficientWindow == null) {
        siteCoefficientWindow = new SiteCoefficientInfoWindow(dataGenerator.getSs(),
            dataGenerator.getSa(), dataGenerator.getSelectedSiteClass());
      }
      siteCoefficientWindow.show();

      dataGenerator.setFa(siteCoefficientWindow.getFa());
      dataGenerator.setFv(siteCoefficientWindow.getFv());
      dataGenerator.setSiteClass(siteCoefficientWindow.getSelectedSiteClass());
      smSpecButton.setEnabled(true);
      sdSpecButton.setEnabled(true);
      siteCoeffWindowShow = true;
    }
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  protected void viewUHSButton_actionPerformed(ActionEvent actionEvent) {
    viewCurves();
  }

  protected void approxUHSButton_actionPerformed(ActionEvent actionEvent) {
    try {
      dataGenerator.calculateApproxUHS();
    }
    catch (RemoteException e) {
      JOptionPane.showMessageDialog(this,
                                    e.getMessage() + "\n" +
                                    "Please check your network connection",
                                    "Server Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    application.setDataInWindow(getData());
    approxUHS_Calculated = true;
  }

  protected void smSpecButton_actionPerformed(ActionEvent actionEvent) {
    setSiteCoeff();
    try {
      dataGenerator.calculateSMSpectrum();
    }
    catch (RemoteException e) {
      JOptionPane.showMessageDialog(this,
                                    e.getMessage() + "\n" +
                                    "Please check your network connection",
                                    "Server Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    application.setDataInWindow(getData());
    if (!viewButton.isEnabled()) {
      viewButton.setEnabled(true);
    }
    smSpectrumCalculated = true;
  }

  protected void sdSpecButton_actionPerformed(ActionEvent actionEvent) {
    setSiteCoeff();
    try {
      dataGenerator.calculateSDSpectrum();
    }
    catch (RemoteException e) {
      JOptionPane.showMessageDialog(this,
                                    e.getMessage() + "\n" +
                                    "Please check your network connection",
                                    "Server Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    application.setDataInWindow(getData());
    if (!viewButton.isEnabled()) {
      viewButton.setEnabled(true);
    }
    sdSpectrumCalculated = true;
  }

  /**
   *
   */
  private void viewCurves() {
    ArrayList functions = dataGenerator.getFunctionsToPlotForSA(uhsCalculated,
        approxUHS_Calculated, sdSpectrumCalculated, smSpectrumCalculated);
    GraphWindow window = new GraphWindow(functions);
    window.show();
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  protected void viewButton_actionPerformed(ActionEvent actionEvent) {
    viewCurves();
  }

}
