package gov.usgs.sha.gui.beans;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import gov.usgs.util.*;
import org.scec.param.event.*;

import org.scec.data.region.RectangularGeographicRegion;
import org.scec.param.StringParameter;
import org.scec.param.editor.ConstrainedStringParameterEditor;
import org.scec.data.Location;
import org.scec.param.ParameterList;
import org.scec.param.ParameterAPI;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.util.ListIterator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import gov.usgs.sha.gui.api.ProbabilisticHazardApplicationAPI;
import gov.usgs.exceptions.ZipCodeErrorException ;
import gov.usgs.exceptions.AnalysisOptionNotSupportedException;
import gov.usgs.sha.data.api.DataGeneratorAPI_NEHRP;
import gov.usgs.sha.data.DataGenerator_NEHRP;
import gov.usgs.sha.gui.infoTools.SiteCoefficientInfoWindow;
import gov.usgs.sha.gui.infoTools.GraphWindow;
import gov.usgs.exceptions.LocationErrorException;


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
  protected DataSetSelectionGuiBean datasetGui;
  protected LocationGuiBean locGuiBean;
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
  JButton viewButton = new JButton();

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


  protected DataGeneratorAPI_NEHRP dataGenerator = new DataGenerator_NEHRP();

  //site coeffiecient window instance
  SiteCoefficientInfoWindow siteCoefficientWindow;

  //instance of the application using this GUI bean
  protected ProbabilisticHazardApplicationAPI application;

  protected boolean mapSpectrumCalculated,smSpectrumCalculated,sdSpectrumCalculated ;

  protected String selectedRegion,selectedEdition,spectraType;

  public NEHRP_GuiBean(ProbabilisticHazardApplicationAPI api) {
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
                         new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
                                                , GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(4, 4, 4, 4), 0, 0));


    regionPanel.add(datasetGui.getDatasetSelectionEditor(),
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(4, 4, 4, 4), 0, 0));
    updateUI();

  }



  protected void createGroundMotionParameter(){


    ArrayList supportedGroundMotion = getSupportedSpectraTypes();
    groundMotionParam = new StringParameter(GROUND_MOTION_PARAM_NAME,
                                            supportedGroundMotion,
                                            (String) supportedGroundMotion.get(0));
    groundMotionParamEditor = new ConstrainedStringParameterEditor(groundMotionParam);
    spectraType = (String)groundMotionParam.getValue();
  }


  protected ArrayList getSupportedSpectraTypes() {
    ArrayList supportedSpectraTypes = new ArrayList();
    supportedSpectraTypes.add(GlobalConstants.MCE_GROUND_MOTION);
    return supportedSpectraTypes;
  }

  protected void jbInit() throws Exception {
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
    viewButton.setFont(new java.awt.Font("Arial", Font.BOLD, 13));
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
        new Insets(2, 2, 2, 2), 8, 6));
    responseSpectraButtonPanel.add(mapSpecButton,
                                   new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 42, 0, 0), 8, 6));
    responseSpectraButtonPanel.add(smSpecButton,
                                   new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets( -1, 27, 0, 0), 13, 6));

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
    setButtonsEnabled(false);
  }


  protected void setButtonsEnabled(boolean disableButtons){
    siteCoeffButton.setEnabled(disableButtons);
    smSDButton.setEnabled(disableButtons);
    mapSpecButton.setEnabled(disableButtons);
    smSpecButton.setEnabled(disableButtons);
    sdSpecButton.setEnabled(disableButtons);
    viewButton.setEnabled(false);
    if(disableButtons == false)
      mapSpectrumCalculated=smSpectrumCalculated=sdSpectrumCalculated=false;
  }




  /**
   * Removes all the output from the window
   */
  public void clearData(){
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
      createLocation();
      setButtonsEnabled(false);
    }
    else if (paramName.equals(datasetGui.EDITION_PARAM_NAME)) {
      selectedEdition = datasetGui.getSelectedDataSetEdition();
      setButtonsEnabled(false);
    }
    else if(paramName.equals(locGuiBean.LAT_PARAM_NAME) ||
            paramName.equals(locGuiBean.LON_PARAM_NAME) ||
            paramName.equals(locGuiBean.ZIP_CODE_PARAM_NAME))
      setButtonsEnabled(false);

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
  protected void createLocation() {
    RectangularGeographicRegion region = getRegionConstraint();
    Component comp = locationSplitPane.getBottomComponent();
    if(comp != null)
      locationSplitPane.remove(locationSplitPane.getBottomComponent());
    if (region != null) {
      locationVisible = true;
      //checking if Zip code is supported by the selected choice
      boolean zipCodeSupported = LocationUtil.isZipCodeSupportedBySelectedEdition(selectedRegion);
      locGuiBean.createLocationGUI(region.getMinLat(), region.getMaxLat(),
                                   region.getMinLon(), region.getMaxLon(),
                                   zipCodeSupported);
      ParameterList paramList = locGuiBean.getLocationParameters();
      ListIterator it = paramList.getParametersIterator();
      while(it.hasNext()){
        ParameterAPI param = (ParameterAPI)it.next();
        param.addParameterChangeListener(this);
      }
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
  protected RectangularGeographicRegion getRegionConstraint() {

    if (selectedRegion.equals(GlobalConstants.CONTER_48_STATES) ||
        selectedRegion.equals(GlobalConstants.ALASKA) ||
        selectedRegion.equals(GlobalConstants.HAWAII) ||
        selectedEdition.equals(GlobalConstants.NEHRP_2003))

      return RegionUtil.getRegionConstraint(selectedRegion);

    return null;
  }


  /**
   * Creates the Parameter that allows user to select  the Editions based on the
   * selected Analysis and choosen geographic region.
   */
  protected void createEditionSelectionParameter() {

    ArrayList supportedEditionList = new ArrayList();

    supportedEditionList.add(GlobalConstants.NEHRP_2003);
    supportedEditionList.add(GlobalConstants.NEHRP_2000);
    supportedEditionList.add(GlobalConstants.NEHRP_1997);
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
  protected void createGeographicRegionSelectionParameter() throws AnalysisOptionNotSupportedException{

    ArrayList supportedRegionList = RegionUtil.
        getSupportedGeographicalRegions(GlobalConstants.NEHRP) ;
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
        try{
          Location loc = locGuiBean.getSelectedLocation();
          double lat = loc.getLatitude();
          double lon = loc.getLongitude();
          dataGenerator.calculateSsS1(lat,lon);
        }catch(LocationErrorException e){
          JOptionPane.showMessageDialog(this,e.getMessage(),"Location Error",JOptionPane.OK_OPTION);
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
        }catch(LocationErrorException e){
          JOptionPane.showMessageDialog(this,e.getMessage(),"Location Error",JOptionPane.OK_OPTION);
          return;
        }
      }
    }
    else { // if territory and location Gui is not visible
      dataGenerator.calculateSsS1();
    }
  }


  protected void ssButton_actionPerformed(ActionEvent actionEvent) {
    getDataForSA_Period();
    application.setDataInWindow(getData());
    siteCoeffButton.setEnabled(true);
    mapSpecButton.setEnabled(true);
  }

  /**
   *
   * @return String
   */
  public String  getData(){
    return dataGenerator.getDataInfo();
  }


  protected void siteCoeffButton_actionPerformed(ActionEvent actionEvent) {
    if(siteCoefficientWindow == null)
      siteCoefficientWindow = new SiteCoefficientInfoWindow(dataGenerator.getSs(),
          dataGenerator.getSa(),dataGenerator.getSelectedSiteClass());
    siteCoefficientWindow.pack();
    siteCoefficientWindow.show();

    dataGenerator.setFa(siteCoefficientWindow.getFa());
    dataGenerator.setFv(siteCoefficientWindow.getFv());
    dataGenerator.setSiteClass(siteCoefficientWindow.getSelectedSiteClass());

    setButtonsEnabled(true);
  }

  protected void smSDButton_actionPerformed(ActionEvent actionEvent) {
    dataGenerator.calculateSMSsS1();
    dataGenerator.calculatedSDSsS1();
    application.setDataInWindow(getData());
  }

  protected void mapSpecButton_actionPerformed(ActionEvent actionEvent) {
    dataGenerator.calculateMapSpectrum();
    application.setDataInWindow(getData());
    if(!viewButton.isEnabled())
      viewButton.setEnabled(true);
    mapSpectrumCalculated = true;
  }

  protected void smSpecButton_actionPerformed(ActionEvent actionEvent) {
    dataGenerator.calculateSMSpectrum();
    application.setDataInWindow(getData());
    if(!viewButton.isEnabled())
      viewButton.setEnabled(true);
    smSpectrumCalculated = true;
  }

  protected void sdSpecButton_actionPerformed(ActionEvent actionEvent) {
    dataGenerator.calculateSDSpectrum();
    application.setDataInWindow(getData());
    if(!viewButton.isEnabled())
      viewButton.setEnabled(true);
   sdSpectrumCalculated = true;
  }

  protected void viewButton_actionPerformed(ActionEvent actionEvent) {
   ArrayList functions = dataGenerator.getFunctionsToPlotForSA(
        mapSpectrumCalculated, sdSpectrumCalculated,smSpectrumCalculated);
    GraphWindow window = new GraphWindow(functions);
    window.show();
  }

}
