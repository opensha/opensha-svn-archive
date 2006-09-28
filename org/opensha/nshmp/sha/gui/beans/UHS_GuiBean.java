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
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.gui.infoTools.ExceptionWindow;

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
  private JSplitPane mainSplitPane = new JSplitPane();
  private JSplitPane locationSplitPane = new JSplitPane();
  private JPanel regionPanel = new JPanel();
  private JPanel basicParamsPanel = new JPanel();
  private JButton uhsButton = new JButton();
  private JButton viewUHSButton = new JButton();
  private Border border9 = BorderFactory.createLineBorder(new Color(80,80,140),1);

  private TitledBorder basicParamBorder = new TitledBorder(border9,
      "Uniform Hazard Spectra (UHS)");
  private TitledBorder regionBorder = new TitledBorder(border9,
                                               "Region and DataSet Selection");

  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  //creating the Ground Motion selection parameter
  protected StringParameter groundMotionParam;
  protected ConstrainedStringParameterEditor groundMotionParamEditor;
  protected static final String GROUND_MOTION_PARAM_NAME = "Ground Motion";

  protected DataGeneratorAPI_UHS dataGenerator = new DataGenerator_UHS();

  //site coeffiecient window instance
  private SiteCoefficientInfoWindow siteCoefficientWindow;

  //instance of the application using this GUI bean
  protected ProbabilisticHazardApplicationAPI application;

  protected boolean smSpectrumCalculated, sdSpectrumCalculated, uhsCalculated,
      approxUHS_Calculated;

  protected String selectedRegion, selectedEdition, spectraType;

  //checks if site coefficient has been set.
  private boolean siteCoeffWindowShow = false;
	private boolean uhsButtonClicked = false;

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
      try{
        createLocation();
      }catch(RegionConstraintException ex){
        ExceptionWindow bugWindow = new ExceptionWindow(this, ex.getStackTrace(),
            "Exception occured while initializing the  region parameters in NSHMP application." +
            "Parameters values have not been set yet.");
        bugWindow.setVisible(true);
        bugWindow.pack();
      }
      locationSplitPane.add(locGuiBean, JSplitPane.BOTTOM);
      locationSplitPane.setDividerLocation(200);
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
		groundMotionParamEditor.getValueEditor().setToolTipText(
			"Select the parameter of interest from the list.");

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
    basicParamsPanel.setLayout(gridBagLayout4);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);

    uhsButton.setText("Calculate");
		uhsButton.setToolTipText("Calculate the uniform hazard spectrum " +
			"and the values of Ss and S1 for the B/C boundary.");
    uhsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        uhsButton_actionPerformed(actionEvent);
      }
    });

    viewUHSButton.setText("View UHS");
		viewUHSButton.setToolTipText("View the graphs of the uniform hazard " +
			"response spectra.");
    viewUHSButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewUHSButton_actionPerformed(actionEvent);
      }
    });

    regionPanel.setBorder(regionBorder);
    regionBorder.setTitleColor(Color.RED);
    regionPanel.setLayout(gridBagLayout2);

    mainSplitPane.add(locationSplitPane, JSplitPane.TOP);
    mainSplitPane.add(basicParamsPanel, JSplitPane.BOTTOM);
    locationSplitPane.add(regionPanel, JSplitPane.TOP);

    basicParamsPanel.add(uhsButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(2, 2, 2, 2), 0, 0));
    basicParamsPanel.add(viewUHSButton,
                         new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(2, 2, 2, 2), 0,
                                                0));
    this.add(mainSplitPane, java.awt.BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(350);
    setButtonsEnabled(true);
		uhsButtonClicked = false;
    createGroundMotionParameter();
    basicParamsPanel.setMinimumSize(new Dimension(0,0));
    regionPanel.setMinimumSize(new Dimension(0,0));
    this.updateUI();
  }

  protected void setButtonsEnabled(boolean enableButtons) {
    //viewUHSButton.setEnabled(enableButtons);
		viewUHSButton.setEnabled(true);
    if(enableButtons == false)
      siteCoeffWindowShow= false;
  }

  /**
   * Removes all the output from the window
   */
  public void clearData() {
    dataGenerator.clearData();
    //setButtonsEnabled(false);
		uhsButtonClicked = false;
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
      try {
        createLocation();
      }
      catch (RegionConstraintException ex) {
        //ExceptionWindow bugWindow = new ExceptionWindow(this, ex.getStackTrace(),
            //"Exception occured while initializing the  region parameters in NSHMP application." +
            //"Parameters values have not been set yet.");
        //bugWindow.setVisible(true);
        //bugWindow.pack();
		  System.out.println(ex.getMessage());
		  ex.printStackTrace();

      }
      //setButtonsEnabled(false);
			uhsButtonClicked = false;
    }
    else if (paramName.equals(datasetGui.EDITION_PARAM_NAME)) {
      selectedEdition = datasetGui.getSelectedDataSetEdition();
      createGroundMotionParameter();
      //setButtonsEnabled(false);
			uhsButtonClicked = false;
    }
    else if (paramName.equals(GROUND_MOTION_PARAM_NAME)) {
      spectraType = (String) groundMotionParam.getValue();
    }

    else if (paramName.equals(locGuiBean.LAT_PARAM_NAME) ||
             paramName.equals(locGuiBean.LON_PARAM_NAME) ||
             paramName.equals(locGuiBean.ZIP_CODE_PARAM_NAME)) {
      //setButtonsEnabled(false);
			uhsButtonClicked = false;
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
  protected void createLocation() throws RegionConstraintException {
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
  protected RectangularGeographicRegion getRegionConstraint() throws
      RegionConstraintException {
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
        getSupportedGeographicalRegions(GlobalConstants.PROB_UNIFORM_HAZ_RES);
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
    //setButtonsEnabled(true);
    //viewUHSButton.setEnabled(true);
		uhsButtonClicked = true;
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
      siteCoefficientWindow.setVisible(true);

      dataGenerator.setFa(siteCoefficientWindow.getFa());
      dataGenerator.setFv(siteCoefficientWindow.getFv());
      dataGenerator.setSiteClass(siteCoefficientWindow.getSelectedSiteClass());
      siteCoeffWindowShow = true;
    }
  }

  /**
   *
   * @param actionEvent ActionEvent
   */
  protected void viewUHSButton_actionPerformed(ActionEvent actionEvent) {
		if (!uhsButtonClicked) { uhsButton_actionPerformed(actionEvent); }
		if (!uhsButtonClicked) { return; } //in case uhsButton exits abnormally
    viewCurves();
  }

  /**
   *
   */
  private void viewCurves() {
    ArrayList functions = dataGenerator.getFunctionsToPlotForSA(uhsCalculated,
        approxUHS_Calculated, sdSpectrumCalculated, smSpectrumCalculated);
    GraphWindow window = new GraphWindow(functions);
    window.setVisible(true);
  }


}
