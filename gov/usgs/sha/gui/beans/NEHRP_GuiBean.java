package gov.usgs.sha.gui.beans;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import gov.usgs.util.GlobalConstants;
import gov.usgs.sha.io.NEHRP_FileReader;

import org.scec.param.event.*;

import org.scec.data.region.RectangularGeographicRegion;
import org.scec.param.StringParameter;
import org.scec.param.editor.ConstrainedStringParameterEditor;
import org.scec.data.Location;
import org.scec.data.function.DiscretizedFuncList;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import gov.usgs.sha.gui.api.ProbabilisticHazardApplicationAPI;
import gov.usgs.exceptions.ZipCodeErrorException;
import gov.usgs.sha.data.api.DataGeneratorAPI_NEHRP;
import gov.usgs.sha.data.DataGenerator_NEHRP;

/**
 * <p>Title:NEHRP_GuiBean</p>
 *
 * <p>Description: This option sets the parameter for the NEHRP analysis option.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class NEHRP_GuiBean
    extends JPanel implements ParameterChangeListener,AnalysisOptionsGuiBeanAPI {
  //Dataset selection Gui instance
  private DataSetSelectionGuiBean datasetGui;
  private LocationGuiBean locGuiBean;
  JSplitPane mainSplitPane = new JSplitPane();
  JSplitPane locationSplitPane = new JSplitPane();
  JSplitPane buttonsSplitPane = new JSplitPane();
  JPanel regionPanel = new JPanel();
  JPanel basicParamsPanel = new JPanel();
  JPanel responseSpectraButtonPanel = new JPanel();
  JButton ssButton = new JButton();
  JButton siteCoeffButton = new JButton();
  JButton smSDButton = new JButton();
  Border border9 = BorderFactory.createBevelBorder(BevelBorder.LOWERED,
      Color.white, Color.white, new Color(98, 98, 98), new Color(140, 140, 140));
  TitledBorder responseSpecBorder = new TitledBorder(border9, "Response Spectra");

  TitledBorder basicParamBorder = new TitledBorder(border9, "Basic Parameters");
  TitledBorder regionBorder = new TitledBorder(border9, "Region and DataSet Selection");
  JButton mapSpecButton = new JButton();
  JButton smSpecButton = new JButton();
  JButton sdSpecButton = new JButton();
  JButton viewMapSpectrumButton = new JButton();
  JButton viewSMSpecButton = new JButton();
  JButton viewSDSpecButton = new JButton();

  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  GridBagLayout gridBagLayout3 = new GridBagLayout();


  GridBagLayout gridBagLayout4 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();

  private NEHRP_FileReader fileReader = new NEHRP_FileReader();


  private boolean locationVisible;


  //creating the Ground Motion selection parameter
  StringParameter groundMotionParam;
  ConstrainedStringParameterEditor groundMotionParamEditor;
  private static final String GROUND_MOTION_PARAM_NAME = "Ground Motion";
  private static final String MCE_GROUND_MOTION = "MCE Ground Motion";


  private DataGeneratorAPI_NEHRP dataGenerator = new DataGenerator_NEHRP();

  //instance of the application using this GUI bean
  private ProbabilisticHazardApplicationAPI application;

  public NEHRP_GuiBean(ProbabilisticHazardApplicationAPI api) {
    application = api;
    try {

      jbInit();
      createGroundMotionParameter();
      datasetGui = new DataSetSelectionGuiBean();
      locGuiBean = new LocationGuiBean();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    createGeographicRegionSelectionParameter();
    createEditionSelectionParameter();
    //creating the datasetEditor to show the geographic region and edition dataset.
    datasetGui.createDataSetEditor();
    createLocation();
    basicParamsPanel.add(groundMotionParamEditor,
                         new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(4, 4, 4, 4), 0, 0));

    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(4, 4, 4, 4), 0, 0));

    this.updateUI();

  }



  private void createGroundMotionParameter(){

    ArrayList supportedGroundMotion = new ArrayList();
    supportedGroundMotion.add(MCE_GROUND_MOTION);
    groundMotionParam = new StringParameter(GROUND_MOTION_PARAM_NAME,
                                            supportedGroundMotion,
                                            (String) supportedGroundMotion.get(0));
    groundMotionParamEditor = new ConstrainedStringParameterEditor(groundMotionParam);
  }




  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    this.setMinimumSize(new Dimension(540, 740));
    this.setPreferredSize(new Dimension(540, 740));
    mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    locationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    buttonsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    basicParamsPanel.setLayout(gridBagLayout4);
    basicParamsPanel.setBorder(basicParamBorder);
    basicParamBorder.setTitleColor(Color.RED);
    ssButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    ssButton.setText("<html>Calculate<br>Ss and S1</br></html>");
    ssButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        ssButton_actionPerformed(actionEvent);
      }
    });
    siteCoeffButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    siteCoeffButton.setActionCommand("siteCoeffButton");
    siteCoeffButton.setText("<html>Calculate<br>Site Coefficient</br></html>");
    siteCoeffButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        siteCoeffButton_actionPerformed(actionEvent);
      }
    });
    smSDButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    smSDButton.setActionCommand("smSDButton");
    smSDButton.setText("<html>Calculate <br>SM and SD Values</br></html>");
    smSDButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        smSDButton_actionPerformed(actionEvent);
      }
    });
    responseSpectraButtonPanel.setBorder(responseSpecBorder);
    responseSpecBorder.setTitleColor(Color.RED);
    responseSpectraButtonPanel.setLayout(gridBagLayout3);
    mapSpecButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    mapSpecButton.setText("<html>Calculate <br>Map Spectrum</br></html>");
    mapSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        mapSpecButton_actionPerformed(actionEvent);
      }
    });
    smSpecButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    smSpecButton.setText("<html>Calculate <br>SM Spectrum</br></html>");
    smSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        smSpecButton_actionPerformed(actionEvent);
      }
    });
    sdSpecButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    sdSpecButton.setActionCommand("sdSpecButton");
    sdSpecButton.setText("<html>View<br>SD Spectrum");
    sdSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        sdSpecButton_actionPerformed(actionEvent);
      }
    });
    viewMapSpectrumButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    viewMapSpectrumButton.setActionCommand("viewMapSpecButton");
    viewMapSpectrumButton.setText("<html>View <br>Map Spectrum</br></html>");
    viewMapSpectrumButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewMapSpectrumButton_actionPerformed(actionEvent);
      }
    });
    viewSMSpecButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    viewSMSpecButton.setActionCommand("viewSMSpecButton");
    viewSMSpecButton.setText("<html>View <br>SM Spectrum</br></html>");
    viewSMSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewSMSpecButton_actionPerformed(actionEvent);
      }
    });
    viewSDSpecButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
    viewSDSpecButton.setText("<html>View <br>SD Spectrum</br></html>");
    viewSDSpecButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewSDSpecButton_actionPerformed(actionEvent);
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
    responseSpectraButtonPanel.add(viewSDSpecButton,
                                   new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 34, 0, 39), 15, 6));
    responseSpectraButtonPanel.add(viewMapSpectrumButton,
                                   new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 42, 0, 0), 8, 6));
    responseSpectraButtonPanel.add(mapSpecButton,
                                   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 42, 0, 0), 8, 6));
    responseSpectraButtonPanel.add(smSpecButton,
                                   new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 27, 0, 0), 13, 6));
    responseSpectraButtonPanel.add(viewSMSpecButton,
                                   new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 27, 0, 0), 13, 6));
    responseSpectraButtonPanel.add(sdSpecButton,
                                   new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 34, 0, 39), 15, 6));
    basicParamsPanel.add(ssButton, new GridBagConstraints(0,1, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(10, 13, 7, 0), 52, 8));
    basicParamsPanel.add(smSDButton,
                         new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 27, 7, 18), 1, 8));
    basicParamsPanel.add(siteCoeffButton,
                         new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(10, 27, 7, 0), 22, 8));
    this.add(mainSplitPane, java.awt.BorderLayout.CENTER);
    mainSplitPane.setDividerLocation(380);
    locationSplitPane.setDividerLocation(170);
    buttonsSplitPane.setDividerLocation(180);
  }

  /**
   * If GuiBean parameter is changed.
   * @param event ParameterChangeEvent
   */
  public void parameterChange(ParameterChangeEvent event) {

    String paramName = event.getParameterName();

    if (paramName.equals(datasetGui.GEOGRAPHIC_REGION_SELECTION_PARAM_NAME)||
        paramName.equals(datasetGui.EDITION_PARAM_NAME))
      createLocation();
  }


  /**
   * Returns the instance of itself
   * @return JPanel
   */
  public JPanel getGuiBean(){
    return this;
  }

  /**
   * Creating the location gui bean
   */
  private void createLocation() {
    RectangularGeographicRegion region = getRegionConstraint();
    Component comp = locationSplitPane.getBottomComponent();
    if(comp != null)
      locationSplitPane.remove(locationSplitPane.getBottomComponent());
    if (region != null) {
      locationVisible = true;
      //checking if Zip code is supported by the selected choice
      boolean zipCodeSupported = isZipCodeSupportedBySelectedEdition();
      locGuiBean.createLocationGUI(region.getMinLat(), region.getMaxLat(),
                                   region.getMinLon(), region.getMaxLon(),
                                   zipCodeSupported);
      locationSplitPane.add(locGuiBean, JSplitPane.BOTTOM);
      locationSplitPane.setDividerLocation(170);
    }
    else if(region == null)
      locationVisible = false;

  }

  /**
   *
   * @return RectangularGeographicRegion
   */
  private RectangularGeographicRegion getRegionConstraint() {
    String selectedGeographicRegion = datasetGui.getSelectedGeographicRegion();
    String editionDataset = datasetGui.getSelectedDataSetEdition();
    if (selectedGeographicRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      return new RectangularGeographicRegion(24.6, 50, -125, -65);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ALASKA)) {
      return new RectangularGeographicRegion(48, 72, -200, -125);
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.HAWAII)) {
      return new RectangularGeographicRegion(18, 23, -161, -154);
    }
    if (editionDataset.equals(GlobalConstants.NEHRP_2003)) {
      if (selectedGeographicRegion.equals(GlobalConstants.PUERTO_RICO)) {
        return new RectangularGeographicRegion(17.89, 18.55, -67.36, -65.47);
      }
      else if (selectedGeographicRegion.equals(GlobalConstants.CULEBRA)) {
        return new RectangularGeographicRegion(18.27, 18.36, -65.39, -65.21);
      }
      else if (selectedGeographicRegion.equals(GlobalConstants.ST_CROIX)) {
        return new RectangularGeographicRegion(17.67, 17.8, -64.93, -65.54);
      }
      else if (selectedGeographicRegion.equals(GlobalConstants.ST_JOHN)) {
        return new RectangularGeographicRegion(18.29, 18.38, -64.85, -64.65);
      }
      else if (selectedGeographicRegion.equals(GlobalConstants.ST_THOMAS)) {
        return new RectangularGeographicRegion(18.26, 18.43, -65.10, -64.80);
      }
      else if (selectedGeographicRegion.equals(GlobalConstants.VIEQUES)) {
        return new RectangularGeographicRegion(18.07, 18.17, -65.6, -65.25);
      }
    }
    return null;
  }

  /**
   *
   * @return boolean
   */
  private boolean isZipCodeSupportedBySelectedEdition() {
    String selectedGeographicRegion = datasetGui.getSelectedGeographicRegion();
    if (selectedGeographicRegion.equals(GlobalConstants.CONTER_48_STATES)) {
      return true;
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.ALASKA)) {
      return true;
    }
    else if (selectedGeographicRegion.equals(GlobalConstants.HAWAII)) {
      return true;
    }

    return false;
  }

  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  private void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.NEHRP_2003);
    supportedEditionList.add(GlobalConstants.NEHRP_2000);
    supportedEditionList.add(GlobalConstants.NEHRP_1997);
    datasetGui.createEditionSelectionParameter(supportedEditionList);
    datasetGui.getEditionSelectionParameter().addParameterChangeListener(this);
  }

  /**
   *
   * Creating the parameter that allows user to choose the geographic region list
   * if selected Analysis option is NEHRP.
   *
   */
  private void createGeographicRegionSelectionParameter() {
    ArrayList supportedRegionList = new ArrayList();
    supportedRegionList.add(GlobalConstants.CONTER_48_STATES);
    supportedRegionList.add(GlobalConstants.ALASKA);
    supportedRegionList.add(GlobalConstants.HAWAII);
    supportedRegionList.add(GlobalConstants.PUERTO_RICO);
    supportedRegionList.add(GlobalConstants.CULEBRA);
    supportedRegionList.add(GlobalConstants.ST_CROIX);
    supportedRegionList.add(GlobalConstants.ST_JOHN);
    supportedRegionList.add(GlobalConstants.ST_THOMAS);
    supportedRegionList.add(GlobalConstants.VIEQUES);
    supportedRegionList.add(GlobalConstants.TUTUILA);
    supportedRegionList.add(GlobalConstants.GUAM);
    datasetGui.createGeographicRegionSelectionParameter(supportedRegionList);
    datasetGui.getGeographicRegionSelectionParameter().
        addParameterChangeListener(this);
  }

  /**
   * Gets the SA Period and Values from datafiles
   */
  private void getDataForSA_Period() {

    String selectedGeographicRegion = datasetGui.getSelectedGeographicRegion();
    String selectedDataEdition = datasetGui.getSelectedDataSetEdition();
    dataGenerator.setRegion(selectedGeographicRegion);
    dataGenerator.setEdition(selectedDataEdition);

    //doing the calculation if not territory and Location GUI is visible
    if (locationVisible) {
      String locationMode = locGuiBean.getLocationMode();
      if (locationMode.equals(locGuiBean.LAT_LON)) {
        Location loc = locGuiBean.getSelectedLocation();
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        dataGenerator.calculateSsS1(lat,lon);

      }
      else if (locationMode.equals(locGuiBean.ZIP_CODE)) {
        String zipCode = locGuiBean.getZipCode();
        try {
          dataGenerator.calculateSsS1(zipCode);
        }
        catch (ZipCodeErrorException e) {
          JOptionPane.showMessageDialog(this, e.getMessage(), "Zip Code Error",
                                        JOptionPane.OK_OPTION);
          e.printStackTrace();
          return;
        }
      }
    }
    else { // if territory and location Gui is not visible
      dataGenerator.calculateSsS1();
    }
  }

  /**
   * Returns the list of Arbitrary Discretized functions.
   * @return ArrayList
   */
  public ArrayList getComputedFunctions(){
    return dataGenerator.getData();
  }


  private void ssButton_actionPerformed(ActionEvent actionEvent) {
    getDataForSA_Period();
    application.setDataInWindow(dataGenerator.getDataInfo());
  }



  private void siteCoeffButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void smSDButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void mapSpecButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void smSpecButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void sdSpecButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void viewMapSpectrumButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void viewSMSpecButton_actionPerformed(ActionEvent actionEvent) {

  }

  private void viewSDSpecButton_actionPerformed(ActionEvent actionEvent) {

  }
}
