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
import org.opensha.param.editor.ConstrainedStringParameterEditor;
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
 * <p>Title:ProbHazCurvesGuiBean</p>
 *
 * <p>Description: This option sets the parameter for the NEHRP analysis option.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class ProbHazCurvesGuiBean
    extends JPanel implements ParameterChangeListener,
    AnalysisOptionsGuiBeanAPI {
  //Dataset selection Gui instance
  private DataSetSelectionGuiBean datasetGui;
  private LocationGuiBean locGuiBean;
  JSplitPane mainSplitPane = new JSplitPane();
  JSplitPane locationSplitPane = new JSplitPane();
  JSplitPane buttonsSplitPane = new JSplitPane();
  JPanel regionPanel = new JPanel();
  JPanel basicParamsPanel = new JPanel();
  JPanel singleHazardValPanel = new JPanel();
  Border border9 = BorderFactory.createLineBorder(new Color(80,80,140),1);
  TitledBorder responseSpecBorder = new TitledBorder(border9,
      "Single Hazard curve");

  TitledBorder basicParamBorder = new TitledBorder(border9,
      "Basic Hazard Curve");
  TitledBorder regionBorder = new TitledBorder(border9,
                                               "Region and DataSet Selection");
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();
  private GridBagLayout gridBagLayout4 = new GridBagLayout();
  private GridBagLayout gridBagLayout3 = new GridBagLayout();

  private boolean locationVisible;
  private JButton hazCurveCalcButton = new JButton();
  private JButton viewCurveButton = new JButton();
  private JButton singleHazardCurveValButton = new JButton();
  private JRadioButton returnPdButton = new JRadioButton();
  private JRadioButton exceedProbAndExpTimeButton = new JRadioButton();
  private BorderLayout borderLayout1 = new BorderLayout();
  private ButtonGroup buttonGroup = new ButtonGroup();

  //creating the Hazard curve selection parameter
  StringParameter hazardCurveIMTPeriodSelectionParam;
  ConstrainedStringParameterEditor hazardCurveIMTPeriodSelectionParamEditor;
  private static final String HAZ_CURVE_IMT_PERIOD_PARAM_NAME =
      "Select Hazard Curve";

  private DataGeneratorAPI_HazardCurves dataGenerator = new
      DataGenerator_HazardCurves();



  private static final String RETURN_PERIOD_PARAM_NAME = "Return Period";
  private static final String PROB_EXCEED_PARAM_NAME = "Prob. of Exceedance";
  private static final String EXP_TIME_PARAM_NAME = "Exposure Time";

  private JPanel singleHazardValEditorPanel;
  private ConstrainedStringParameterEditor returnPdEditor;
  private ConstrainedStringParameterEditor exceedProbEditor;
  private ConstrainedStringParameterEditor expTimeEditor;

  //instance of the application using this GUI bean
  private ProbabilisticHazardApplicationAPI application;

  private String selectedRegion, selectedEdition, imt, returnPeriod,
      exceedProbVal, expTimeVal,
      singleHazardCalcMethod;
  //checks if Single Value for the Hazard Curve has to be computed using Return Pd.
  private boolean returnPdSelected = true;

  public ProbHazCurvesGuiBean(ProbabilisticHazardApplicationAPI api) {
    application = api;
    try {
      createSingleHazardValEditor();
      jbInit();

      datasetGui = new DataSetSelectionGuiBean();
      locGuiBean = new LocationGuiBean();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }

    try {
      createGeographicRegionSelectionParameter();
    }
    catch (AnalysisOptionNotSupportedException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(),
                                    "Analysis Option selection error",
                                    JOptionPane.ERROR_MESSAGE);
    }
    createIMT_PeriodsParameter();
    createEditionSelectionParameter();
    //creating the datasetEditor to show the geographic region and edition dataset.
    datasetGui.createDataSetEditor();
    try {
      createLocation();
    }
    catch (RegionConstraintException ex1) {
      ExceptionWindow bugWindow = new ExceptionWindow(this, ex1.getStackTrace(),
          "Exception occured while initializing the  region parameters in NSHMP application." +
          "Parameters values have not been set yet.");
      bugWindow.show();
      bugWindow.pack();

    }
    locationSplitPane.add(locGuiBean, JSplitPane.BOTTOM);
    locationSplitPane.setDividerLocation(120);
    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 0, 0, 0), 0, 0));


    this.updateUI();
  }

  private void createIMT_PeriodsParameter() {
    if (hazardCurveIMTPeriodSelectionParamEditor != null) {
      basicParamsPanel.remove(hazardCurveIMTPeriodSelectionParamEditor);
    }
    ArrayList supportedImtPeriods = RegionUtil.getSupportedIMT_PERIODS(
        selectedRegion);
    hazardCurveIMTPeriodSelectionParam = new StringParameter(
        HAZ_CURVE_IMT_PERIOD_PARAM_NAME,
        supportedImtPeriods,
        (String) supportedImtPeriods.get(0));
    hazardCurveIMTPeriodSelectionParam.addParameterChangeListener(this);
    hazardCurveIMTPeriodSelectionParamEditor = new
        ConstrainedStringParameterEditor(hazardCurveIMTPeriodSelectionParam);
    hazardCurveIMTPeriodSelectionParamEditor.refreshParamEditor();
    imt = (String) hazardCurveIMTPeriodSelectionParam.getValue();

    basicParamsPanel.add(hazardCurveIMTPeriodSelectionParamEditor,
                         new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(4, 4, 4, 4), 0, 0));

  }

  private void createSingleHazardValEditor() {
    ArrayList supportedReturnPds = GlobalConstants.getSupportedReturnPeriods();
    StringConstraint returnPdConstraint = new StringConstraint(
        supportedReturnPds);
    StringParameter returnPeriodParam = new StringParameter(
        RETURN_PERIOD_PARAM_NAME, returnPdConstraint,
        "Years", (String) supportedReturnPds.get(0));

    ArrayList exceedProbsList = GlobalConstants.getSupportedExceedanceProb();
    StringParameter exceedProbParam = new StringParameter(
        PROB_EXCEED_PARAM_NAME, exceedProbsList, (String) exceedProbsList.get(0));

    ArrayList supportedExpTimeList = GlobalConstants.getSupportedExposureTime();
    StringConstraint expTimeConstraint = new StringConstraint(
        supportedExpTimeList);
    StringParameter expTimeParam = new StringParameter(
        EXP_TIME_PARAM_NAME, expTimeConstraint,
        "Years", (String) supportedExpTimeList.get(0));


    returnPeriod = (String) returnPeriodParam.getValue();
    exceedProbVal = (String) exceedProbParam.getValue();
    expTimeVal = (String) expTimeParam.getValue();



    returnPeriodParam.addParameterChangeListener(this);
    exceedProbParam.addParameterChangeListener(this);
    expTimeParam.addParameterChangeListener(this);

    try{
      returnPdEditor = new ConstrainedStringParameterEditor(returnPeriodParam);
      exceedProbEditor = new ConstrainedStringParameterEditor(exceedProbParam);
      expTimeEditor = new ConstrainedStringParameterEditor(expTimeParam);

      singleHazardValEditorPanel = new JPanel();
      singleHazardValEditorPanel.setLayout(new GridBagLayout());

      singleHazardValEditorPanel.add(returnPdEditor,
                                     new GridBagConstraints(0, 0, 2, 1, 1.0,
          1.0
          , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(1,1,1,1), 0, 0));

      singleHazardValEditorPanel.add(exceedProbEditor,
                                     new GridBagConstraints(0, 0, 1, 1, 1.0,
          1.0
          , GridBagConstraints.NORTH, GridBagConstraints.WEST,
          new Insets(1,1,1,1), 0, 0));

      singleHazardValEditorPanel.add(expTimeEditor,
                                     new GridBagConstraints(1, 0, 1, 1, 1.0,
          1.0
          , GridBagConstraints.NORTH, GridBagConstraints.WEST,
          new Insets(1,1,1,1), 0, 0));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    setParametersForSingleHazardValueVisible();
  }

  /**
   * Making the parameters for the Single Value Hazard Curve visible or invisible
   * based on the mode user has chosen to calculate.
   */
  private void setParametersForSingleHazardValueVisible() {

    exceedProbEditor.setVisible(!returnPdSelected);
    expTimeEditor.setVisible(!returnPdSelected);
    returnPdEditor.setVisible(returnPdSelected);

    singleHazardValPanel.updateUI();
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    basicParamsPanel.setLayout(gridBagLayout3);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);
    singleHazardValPanel.setBorder(responseSpecBorder);
    responseSpecBorder.setTitleColor(Color.RED);
    singleHazardValPanel.setLayout(gridBagLayout4);
    regionPanel.setBorder(regionBorder);
    regionBorder.setTitleColor(Color.RED);
    regionPanel.setLayout(gridBagLayout2);
    hazCurveCalcButton.setText("Calculate");
    hazCurveCalcButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        hazCurveCalcButton_actionPerformed(e);
      }
    });
    viewCurveButton.setText("View");
    viewCurveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewCurveButton_actionPerformed(e);
      }
    });
    singleHazardCurveValButton.setText(
        "Calculate");
    singleHazardCurveValButton.addActionListener(new java.awt.event.
                                                 ActionListener() {
      public void actionPerformed(ActionEvent e) {
        singleHazardCurveValButton_actionPerformed(e);
      }
    });
    returnPdButton.setText("Return Pd");
    exceedProbAndExpTimeButton.setText("Prob & Time");
    mainSplitPane.add(locationSplitPane, JSplitPane.TOP);
    mainSplitPane.add(buttonsSplitPane, JSplitPane.BOTTOM);
    locationSplitPane.add(regionPanel, JSplitPane.TOP);

    buttonsSplitPane.add(basicParamsPanel, JSplitPane.TOP);
    buttonsSplitPane.add(singleHazardValPanel, JSplitPane.BOTTOM);

    this.add(mainSplitPane, BorderLayout.CENTER);
    basicParamsPanel.add(hazCurveCalcButton,
                         new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.NORTH,
                                                GridBagConstraints.NONE,
                                                new Insets(4, 70, 4, 0), 0, 0));
    basicParamsPanel.add(viewCurveButton,
                         new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.NORTH,
                                                GridBagConstraints.NONE,
                                                new Insets(4, 9, 4, 110), 0,0));
    singleHazardValPanel.add(singleHazardValEditorPanel,
                             new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(1, 1, 1, 1), 0,0 ));
    singleHazardValPanel.add(returnPdButton,
                             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.NORTH, GridBagConstraints.WEST,
        new Insets(1,-20, 1, 60), 0,0));
    singleHazardValPanel.add(exceedProbAndExpTimeButton,
                             new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.NORTH, GridBagConstraints.WEST,
        new Insets(1, -20, 1, 60), 0, 0));

    singleHazardValPanel.add(singleHazardCurveValButton,
                             new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
        , GridBagConstraints.NORTH, GridBagConstraints.NONE,
        new Insets(1, 150,1,1), 0, 0));


  returnPdButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          returnPdButton_actionPerformed(actionEvent);
        }
      });

      exceedProbAndExpTimeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          exceedProbAndExpTimeButton_actionPerformed(actionEvent);
        }
    });


    buttonGroup.add(returnPdButton);
    buttonGroup.add(exceedProbAndExpTimeButton);
    buttonGroup.setSelected(returnPdButton.getModel(), true);
    mainSplitPane.setDividerLocation(260);
    buttonsSplitPane.setDividerLocation(117);
    singleHazardCurveValButton.setEnabled(false);
    viewCurveButton.setEnabled(false);
    singleHazardValPanel.setMinimumSize(new Dimension(0,0));
    basicParamsPanel.setMinimumSize(new Dimension(0,0));
    regionPanel.setMinimumSize(new Dimension(0,0));
  }

  /**
   * Removes all the output from the window
   */
  public void clearData() {
    dataGenerator.clearData();
    singleHazardCurveValButton.setEnabled(false);
  }

  /**
   * If GuiBean parameter is changed.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {

    String paramName = event.getParameterName();

    if (paramName.equals(datasetGui.GEOGRAPHIC_REGION_SELECTION_PARAM_NAME)) {
      selectedRegion = datasetGui.getSelectedGeographicRegion();
      //creating the edition parameter when user changes the region
      createEditionSelectionParameter();
      selectedEdition = datasetGui.getSelectedDataSetEdition();
      try {
        createLocation();
      }
      catch (RegionConstraintException ex) {
        ExceptionWindow bugWindow = new ExceptionWindow(this, ex.getStackTrace(),
            "Exception occured while initializing the  region parameters in NSHMP application." +
            "Parameters values have not been set yet.");
        bugWindow.show();
        bugWindow.pack();
      }
      createIMT_PeriodsParameter();
      viewCurveButton.setEnabled(false);
      singleHazardCurveValButton.setEnabled(false);
    }
    else if (paramName.equals(datasetGui.EDITION_PARAM_NAME)) {
      selectedEdition = datasetGui.getSelectedDataSetEdition();
      viewCurveButton.setEnabled(false);
      singleHazardCurveValButton.setEnabled(false);
    }

    else if (paramName.equals(HAZ_CURVE_IMT_PERIOD_PARAM_NAME)) {
      imt = (String) hazardCurveIMTPeriodSelectionParam.getValue();
      viewCurveButton.setEnabled(false);
      singleHazardCurveValButton.setEnabled(false);
    }
    else if (paramName.equals(RETURN_PERIOD_PARAM_NAME)) {
      returnPeriod = (String)returnPdEditor.getValue();
    }
    else if (paramName.equals(PROB_EXCEED_PARAM_NAME)) {
      exceedProbVal = (String) exceedProbEditor.getValue();
    }
    else if (paramName.equals(EXP_TIME_PARAM_NAME)) {
      expTimeVal = (String) expTimeEditor.getValue();
    }
    else if (paramName.equals(locGuiBean.LAT_PARAM_NAME) ||
             paramName.equals(locGuiBean.LON_PARAM_NAME) ||
             paramName.equals(locGuiBean.ZIP_CODE_PARAM_NAME)) {
      viewCurveButton.setEnabled(false);
      singleHazardCurveValButton.setEnabled(false);
    }

    this.updateUI();
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
  private void createLocation() throws RegionConstraintException {
    RectangularGeographicRegion region = RegionUtil.getRegionConstraint(
        selectedRegion);
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


    }
    else if (region == null) {
      locationVisible = false;
      locGuiBean.createNoLocationGUI();

    }

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
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  private void createEditionSelectionParameter() {

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
  }

  /**
   *
   * Creating the parameter that allows user to choose the geographic region list
   * if selected Analysis option is NEHRP.
   *
   */
  private void createGeographicRegionSelectionParameter() throws
      AnalysisOptionNotSupportedException {
    ArrayList supportedRegionList = RegionUtil.
        getSupportedGeographicalRegions(GlobalConstants.PROB_HAZ_CURVES);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
    selectedRegion = datasetGui.getSelectedGeographicRegion();
  }

  /**
   * Gets the SA Period and Values from datafiles
   */
  private void getDataForSA_Period() throws ZipCodeErrorException,
      LocationErrorException,
      RemoteException {

    dataGenerator.setRegion(selectedRegion);
    dataGenerator.setEdition(selectedEdition);

    //doing the calculation if not territory and Location GUI is visible
    if (locationVisible) {
      if (locGuiBean.getLocationMode()) {
        Location loc = locGuiBean.getSelectedLocation();
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        dataGenerator.calculateHazardCurve(lat, lon, imt);

      }
      else {
        String zipCode = locGuiBean.getZipCode();
        dataGenerator.calculateHazardCurve(zipCode, imt);

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

  void viewCurveButton_actionPerformed(ActionEvent e) {
    GraphWindow window = new GraphWindow(dataGenerator.getHazardCurveFunction());
    window.show();
  }

  void hazCurveCalcButton_actionPerformed(ActionEvent e) {
    try{
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
    viewCurveButton.setEnabled(true);
    singleHazardCurveValButton.setEnabled(true);
  }

  /**
   * Calculates the Single Hazard Curve Value
   * @param e ActionEvent
   */
  void singleHazardCurveValButton_actionPerformed(ActionEvent e) {

    boolean isLogInterpolation = true;
    //linear interpolation in case if the selected data edition is 1996 or 1998
    if(selectedEdition.equals(GlobalConstants.data_1996) || selectedEdition.equals(GlobalConstants.data_1998))
      isLogInterpolation = false;
    //else always perform the log interpolation

    if (!returnPdSelected) {
      try {
        dataGenerator.calcSingleValueHazardCurveUsingPEandExptime(Double.
            parseDouble(exceedProbVal),
            Double.parseDouble(expTimeVal), isLogInterpolation);
      }
      catch (RemoteException ee) {
        JOptionPane.showMessageDialog(this,
                                      ee.getMessage() + "\n" +
                                      "Please check your network connection",
                                      "Server Connection Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    else {
      try {
        dataGenerator.calcSingleValueHazardCurveUsingReturnPeriod(Double.
            parseDouble(returnPeriod),
            isLogInterpolation);
      }
      catch (RemoteException ee) {
        JOptionPane.showMessageDialog(this,
                                      ee.getMessage() + "\n" +
                                      "Please check your network connection",
                                      "Server Connection Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }

    }
    application.setDataInWindow(getData());
  }


  private void returnPdButton_actionPerformed(ActionEvent e){
   returnPdSelected = true;
   setParametersForSingleHazardValueVisible();
  }

  private void exceedProbAndExpTimeButton_actionPerformed(ActionEvent e){
    returnPdSelected = false;
    setParametersForSingleHazardValueVisible();
  }

}
