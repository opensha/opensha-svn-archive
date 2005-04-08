package gov.usgs.sha.gui.beans;

import java.rmi.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.scec.data.*;
import org.scec.data.region.*;
import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import gov.usgs.exceptions.*;
import gov.usgs.sha.data.*;
import gov.usgs.sha.data.api.*;
import gov.usgs.sha.gui.api.*;
import gov.usgs.sha.gui.infoTools.*;
import gov.usgs.util.*;

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
  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder responseSpecBorder = new TitledBorder(border9,
      "Approximate UHS and UHS-based Design Spectra");

  TitledBorder basicParamBorder = new TitledBorder(border9,
      "Uniform Hazard Spectra (UHS)");
  TitledBorder regionBorder = new TitledBorder(border9,
                                               "Region and DataSet Selection");
  JButton siteCoeffButton = new JButton();
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
      jbInit();

    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(4, 4, 4, 4), 0, 0));

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
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(4, 4, 4, 4), 0, 0));

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
    this.setMinimumSize(new Dimension(500, 680));
    this.setPreferredSize(new Dimension(500, 680));
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    basicParamsPanel.setLayout(gridBagLayout4);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);

    uhsButton.setText("<html>Calculate<br>UHS</br></html>");
    uhsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        uhsButton_actionPerformed(actionEvent);
      }
    });

    approxUHSButton.setActionCommand("approxUHSButton");
    approxUHSButton.setText("<html>Calculate<br>Approx. UHS</br></html>");
    approxUHSButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        approxUHSButton_actionPerformed(actionEvent);
      }
    });

    viewUHSButton.setActionCommand("viewUHSButton");
    viewUHSButton.setText("<html>View <br>UHS</br></html>");
    viewUHSButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewUHSButton_actionPerformed(actionEvent);
      }
    });
    responseSpectraButtonPanel.setBorder(responseSpecBorder);
    responseSpecBorder.setTitleColor(Color.RED);
    responseSpectraButtonPanel.setLayout(gridBagLayout3);
    siteCoeffButton.setText("<html>Calculate Site<br> Coefficients</br></html>");
    siteCoeffButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        siteCoeffButton_actionPerformed(actionEvent);
      }
    });

    smSpecButton.setText("<html>Calculate <br>SM Spectrum</br></html>");
    smSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        smSpecButton_actionPerformed(actionEvent);
      }
    });

    sdSpecButton.setActionCommand("sdSpecButton");
    sdSpecButton.setText("<html>View<br>SD Spectrum");
    sdSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        sdSpecButton_actionPerformed(actionEvent);
      }
    });

    viewButton.setActionCommand("viewButton");
    viewButton.setText("<html>View <br>Spectrum</br></html>");
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
                                   new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 27, 0, 0), 20, 13));
    responseSpectraButtonPanel.add(siteCoeffButton,
                                   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 42, 0, 0), 10, 20));
    responseSpectraButtonPanel.add(smSpecButton,
                                   new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 27, 0, 0), 10, 6));

    responseSpectraButtonPanel.add(sdSpecButton,
                                   new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 34, 0, 39), 10, 6));
    basicParamsPanel.add(uhsButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(10, 13, 7, 0), 10, 8));
    basicParamsPanel.add(viewUHSButton,
                         new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 27, 7, 18), 30,
                                                8));
    basicParamsPanel.add(approxUHSButton,
                         new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 27, 7, 0), 10, 8));
    this.add(mainSplitPane, java.awt.BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(380);
    locationSplitPane.setDividerLocation(170);
    buttonsSplitPane.setDividerLocation(180);
    setButtonsEnabled(false);
    createGroundMotionParameter();
  }

  protected void setButtonsEnabled(boolean disableButtons) {
    approxUHSButton.setEnabled(disableButtons);
    viewUHSButton.setEnabled(disableButtons);
    siteCoeffButton.setEnabled(disableButtons);
    smSpecButton.setEnabled(disableButtons);
    sdSpecButton.setEnabled(disableButtons);
    viewButton.setEnabled(disableButtons);
  }

  /**
   * Removes all the output from the window
   */
  public void clearData() {
    dataGenerator.clearData();
    setButtonsEnabled(false);
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
    Component comp = locationSplitPane.getBottomComponent();
    if (comp != null) {
      locationSplitPane.remove(locationSplitPane.getBottomComponent());
    }

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
    locationSplitPane.setDividerLocation(170);

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
  protected void getDataForSA_Period() {

    dataGenerator.setSpectraType(spectraType);
    dataGenerator.setRegion(selectedRegion);
    dataGenerator.setEdition(selectedEdition);

    String locationMode = locGuiBean.getLocationMode();
    if (locationMode.equals(locGuiBean.LAT_LON)) {
      try {
        Location loc = locGuiBean.getSelectedLocation();
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        dataGenerator.calculateUHS(lat, lon);
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
        dataGenerator.calculateUHS(zipCode);
      }
      catch (RemoteException e) {
        JOptionPane.showMessageDialog(this,
                                      e.getMessage() + "\n" +
                                      "Please check your network connection",
                                      "Server Connection Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
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
    }
  }

  protected void uhsButton_actionPerformed(ActionEvent actionEvent) {
    getDataForSA_Period();
    application.setDataInWindow(getData());
    approxUHSButton.setEnabled(true);
    viewUHSButton.setEnabled(true);
    siteCoeffButton.setEnabled(true);
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
   *
   * @param actionEvent ActionEvent
   */
  protected void siteCoeffButton_actionPerformed(ActionEvent actionEvent) {
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

  private void viewCurves() {
    ArrayList functions = dataGenerator.getFunctionsToPlotForSA(uhsCalculated,
        approxUHS_Calculated, sdSpectrumCalculated, smSpectrumCalculated);
    GraphWindow window = new GraphWindow(functions);
    window.show();

  }

  protected void viewButton_actionPerformed(ActionEvent actionEvent) {
    viewCurves();
  }

}
