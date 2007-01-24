package org.opensha.sha.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.param.DependentParameterAPI;
import org.opensha.param.DoubleDiscreteParameter;
import org.opensha.param.ParameterAPI;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.sha.earthquake.ERF_API;
import org.opensha.sha.earthquake.ERF_List;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.gui.beans.IMLorProbSelectorGuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.controls.ERF_EpistemicListControlPanel;
import org.opensha.sha.gui.controls.PlottingOptionControl;
import org.opensha.sha.gui.infoTools.ApplicationVersionInfoWindow;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.sha.gui.infoTools.WeightedFuncListforPlotting;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.DisaggregationCalculator;
import org.opensha.sha.calc.SpectrumCalculator;
import org.opensha.sha.calc.SpectrumCalculatorAPI;
import org.opensha.util.FileUtils;

import java.net.URL;
import java.rmi.RemoteException;

/**
 * @author nitingupta
 *
 */
public class HazardSpectrumLocalModeApplication
    extends HazardCurveLocalModeApplication {

  //Static String to tell the IMT as the SA becuase it is the only supported IMT for this Application
  protected static String SA_NAME = "SA";
  private static String SA_PERIOD = "SA Period";
  //Axis Labels
  private static final String IML = "SA (g)";
  private static final String PROB_AT_EXCEED = "Probability of Exceedance";
  private static final String X_AXIS_LABEL = "Period (sec)";

  private IMLorProbSelectorGuiBean imlProbGuiBean;

  //ArrayList that stores the SA Period values for the IMR
  private ArrayList saPeriodVector;

  protected final static String version = "0.0.11";

  //Graph Title
  protected String TITLE = new String("Response Spectra Curves");


  protected final static String versionURL = "http://www.opensha.org/applications/hazSpectrumApp/HazardSpectrumApp_Version.txt";
  protected final static String appURL = "http://www.opensha.org/applications/hazSpectrumApp/HazardSpectrumLocalModeApp.jar";
  protected final static String versionUpdateInfoURL = "http://www.opensha.org/applications/hazSpectrumApp/versionUpdate.html";
  //instances of various calculators
  protected SpectrumCalculatorAPI calc;
  //Prob@IML or IML@Prob
  boolean probAtIML;

  
  /**
   * Returns the Application version
   * @return String
   */
  
  public static String getAppVersion(){
    return version;
  }

  
  
  /**
   * Checks if the current version of the application is latest else direct the
   * user to the latest version on the website.
   */
  protected void checkAppVersion(){
      ArrayList hazCurveVersion = null;
      try {
    	  hazCurveVersion = FileUtils.loadFile(new URL(versionURL));
      }
      catch (Exception ex1) {
        return;
      }
      String appVersionOnWebsite = (String)hazCurveVersion.get(0);
      if(!appVersionOnWebsite.trim().equals(version.trim())){
        try{
          ApplicationVersionInfoWindow messageWindow =
              new ApplicationVersionInfoWindow(appURL,
                                               this.versionUpdateInfoURL,
                                               "App Version Update", this);
          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          messageWindow.setLocation( (dim.width -
                                      messageWindow.getSize().width) / 2,
                                    (dim.height -
                                     messageWindow.getSize().height) / 2);
          messageWindow.setVisible(true);
        }catch(Exception e){
          e.printStackTrace();
        }
      }

    return;

  }  
    
  

  /**
   * Initialize the IMR Gui Bean
   */
  protected void initIMR_GuiBean() {

     imrGuiBean = new IMR_GuiBean(this);
     imrGuiBean.getParameterEditor(imrGuiBean.IMR_PARAM_NAME).getParameter().addParameterChangeListener(this);
     //sets the Intensity measure for the IMR
     imrGuiBean.getSelectedIMR_Instance().setIntensityMeasure(this.SA_NAME);
     // show this gui bean the JPanel
     imrPanel.add(this.imrGuiBean,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
         GridBagConstraints.CENTER, GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
     imrPanel.updateUI();
  }


  //Initialize the applet
  public void init() {
    try {

      // initialize the control pick list
      initControlList();
      //initialise the list to make selection whether to show ERF_GUIBean or ERF_RupSelectorGuiBean
      initProbOrDeterList();
      // initialize the GUI components
      jbInit();

      // initialize the various GUI beans
      initIMR_GuiBean();
      initImlProb_GuiBean();
      initSiteGuiBean();
      try {
        initERF_GuiBean();
      }
      catch (RuntimeException e) {
        JOptionPane.showMessageDialog(this, "Connection to ERF's failed",
                                      "Internet Connection Problem",
                                      JOptionPane.OK_OPTION);
        e.printStackTrace();
        System.exit(0);
      }
    }
    catch (Exception e) {
      ExceptionWindow bugWindow = new ExceptionWindow(this, e.getStackTrace(),
          "Exception occured while creating the GUI.\n" +
          "No Parameters have been set");
      bugWindow.setVisible(true);
      bugWindow.pack();
      //e.printStackTrace();
    }
    this.setTitle("Hazard Spectrum Application ("+version+")");
    ( (JPanel) getContentPane()).updateUI();
  }


  /**
   * This method creates the HazardCurveCalc and Disaggregation Calc(if selected) instances.
   * Calculations are performed on the user's own machine, no internet connection
   * is required for it.
   */
  protected void createCalcInstance(){
    try{
      if(calc == null)
        calc = new SpectrumCalculator();
      /*if(disaggregationFlag)
        if(disaggCalc == null)
          disaggCalc = new DisaggregationCalculator();*/
    }catch(Exception e){

      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),this.getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
 //     e.printStackTrace();
    }
  }



  /**
   * Gets the probabilities functiion based on selected parameters
   * this function is called when add Graph is clicked
   */
  protected void computeHazardCurve() {

    //starting the calculation
    isHazardCalcDone = false;
    numERFsInEpistemicList = 0;
    ERF_API forecast = null;
    ProbEqkRupture rupture = null;
    if (!this.isProbCurve)
      rupture = (ProbEqkRupture)this.erfRupSelectorGuiBean.getRupture();

    // get the selected forecast model
    try {
      if (this.isProbCurve) {
        // whether to show progress bar in case of update forecast
        erfGuiBean.showProgressBar(this.progressCheckBox.isSelected());
        //get the selected ERF instance
        forecast = erfGuiBean.getSelectedERF();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Incorrect Values",
                                    JOptionPane.ERROR_MESSAGE);
      setButtonsEnable(true);
      return;
    }
    if (this.progressCheckBox.isSelected()) {
      progressClass = new CalcProgressBar("Response-Spectrum Calc Status",
                                          "Beginning Calculation ");
      progressClass.displayProgressBar();
      timer.start();
    }

    // get the selected IMR
    AttenuationRelationship imr = (AttenuationRelationship) imrGuiBean.
        getSelectedIMR_Instance();

    getSA_PeriodForIMR(imr);

    // make a site object to pass to IMR
    Site site = siteGuiBean.getSite();

    //initialize the values in condProbfunc with log values as passed in hazFunction
    // intialize the hazard function
    DiscretizedFuncAPI hazFunction =null;

    //what selection does the user have made, IML@Prob or Prob@IML
    String imlOrProb = imlProbGuiBean.getSelectedOption();
    //gets the IML or Prob value filled in by the user
    double imlProbValue = imlProbGuiBean.getIML_Prob();

    if (imlOrProb.equalsIgnoreCase(imlProbGuiBean.PROB_AT_IML)) {
      yAxisName = PROB_AT_EXCEED;
      probAtIML = true;
    }
    else {
      yAxisName = IML;
      probAtIML = false;
    }
    xAxisName = X_AXIS_LABEL;

    if (forecast instanceof ERF_List && isProbCurve) {
      //if add on top get the name of ERF List forecast
      if (addData)
        prevSelectedERF_List = forecast.getName();

      if (!prevSelectedERF_List.equals(forecast.getName()) && !addData) {
        JOptionPane.showMessageDialog(this,
                                      "Cannot add to existing without selecting same ERF Epistemic list",
                                      "Input Error",
                                      JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      this.isEqkList = true; // set the flag to indicate thatwe are dealing with Eqk list
      handleForecastList(site, imr, forecast, imlProbValue);
      //initializing the counters for ERF List to 0, for other ERF List calculations
      currentERFInEpistemicListForHazardCurve = 0;
      numERFsInEpistemicList = 0;
      isHazardCalcDone = true;
      return;
    }

    //making the previuos selected ERF List to be null
    prevSelectedERF_List = null;

    // this is not a eqk list
    this.isEqkList = false;
    // calculate the hazard curve
    try {
      if (distanceControlPanel != null)
        calc.setMaxSourceDistance(
            distanceControlPanel.getDistance());
    }
    catch (Exception e) {
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this, e.getStackTrace(),
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      e.printStackTrace();
    }
    try {
      // calculate the hazard curve
      try {
        if (isProbCurve){
          if(probAtIML)
            hazFunction = (DiscretizedFuncAPI) calc.getSpectrumCurve(
                site, imr, (EqkRupForecastAPI) forecast,
                imlProbValue,saPeriodVector);
          else{
            hazFunction = new ArbitrarilyDiscretizedFunc();

            // initialize the values in condProbfunc with log values as passed in hazFunction
            initX_Values(hazFunction);
            try {

                hazFunction = calc.getIML_SpectrumCurve(hazFunction,site,imr,
                    (EqkRupForecastAPI)forecast,imlProbValue,saPeriodVector);
              }
            catch (RuntimeException e) {
              e.printStackTrace();
              JOptionPane.showMessageDialog(this, e.getMessage(),
                                            "Parameters Invalid",
                                            JOptionPane.INFORMATION_MESSAGE);
              return;
            }
          }
        }
        else {
          progressCheckBox.setSelected(false);
          progressCheckBox.setEnabled(false);
          if (probAtIML)//if the user has selected prob@IML
            hazFunction = (DiscretizedFuncAPI) calc.getDeterministicSpectrumCurve(
                site, imr,rupture,  probAtIML, imlProbValue);
          else //if the user has selected IML@prob
            hazFunction = (DiscretizedFuncAPI) calc.getDeterministicSpectrumCurve(
                site, imr,rupture,probAtIML, imlProbValue);

          progressCheckBox.setSelected(true);
          progressCheckBox.setEnabled(true);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        setButtonsEnable(true);
        ExceptionWindow bugWindow = new ExceptionWindow(this, e.getStackTrace(),
            getParametersInfoAsString());
        bugWindow.setVisible(true);
        bugWindow.pack();
      }
      ((ArbitrarilyDiscretizedFunc)hazFunction).setInfo(getParametersInfoAsString());
    }
    catch (RuntimeException e) {
      JOptionPane.showMessageDialog(this, e.getMessage(),
                                    "Parameters Invalid",
                                    JOptionPane.INFORMATION_MESSAGE);
      e.printStackTrace();
      setButtonsEnable(true);
      return;
    }
    isHazardCalcDone = true;

    // add the function to the function list
    functionList.add(hazFunction);
  }

  /**
   * Initialise the IMT_Prob Selector Gui Bean
   */
  private void initImlProb_GuiBean() {
    imlProbGuiBean = new IMLorProbSelectorGuiBean();
    this.imtPanel.add(imlProbGuiBean,
                      new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.BOTH,
                                             defaultInsets, 0, 0));
  }

  /**
   * Initialize the items to be added to the control list
   */
  protected void initControlList() {
    controlComboBox.addItem(CONTROL_PANELS);
    controlComboBox.addItem(DISTANCE_CONTROL);
    controlComboBox.addItem(SITES_OF_INTEREST_CONTROL);
    controlComboBox.addItem(CVM_CONTROL);
    controlComboBox.addItem(PLOTTING_OPTION);
    controlComboBox.addItem(X_VALUES_CONTROL);
    controlComboBox.addItem(XY_Values_Control);
  }

  
  /**
   * It returns the IMT Gui bean, which allows the Cybershake control panel
   * to set the same SA period value in the main application
   * similar to selected for Cybershake.
   */
  public IMT_GuiBean getIMTGuiBeanInstance() {
    return null;
  }
 
  /**
   * Updates the IMT_GuiBean to reflect the chnaged IM for the selected AttenuationRelationship.
   * This method is called from the IMR_GuiBean to update the application with the Attenuation's
   * supported IMs.
   *
   */
  public void updateIM() {
    return;
  }
  
  
  
  /**
   *  Any time a control paramater or independent paramater is changed
   *  by the user in a GUI this function is called, and a paramater change
   *  event is passed in. This function then determines what to do with the
   *  information ie. show some paramaters, set some as invisible,
   *  basically control the paramater lists.
   *
   * @param  event
   */
  public void parameterChange(ParameterChangeEvent event) {

    String S = ": parameterChange(): ";
    if (D)
      System.out.println("\n" + S + "starting: ");

    String name1 = event.getParameterName();

    // if IMR selection changed, update the site parameter list and supported IMT
    if (name1.equalsIgnoreCase(imrGuiBean.IMR_PARAM_NAME)) {
      AttenuationRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
      //set the intensity measure fo	r the IMR
      imr.setIntensityMeasure(SA_NAME);
      //gets the SA Period Values fo	r the IMR
      this.getSA_PeriodForIMR(imr);
      siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
      siteGuiBean.validate();
      siteGuiBean.repaint();
    }
    if (name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {

      String plottingOption = null;
      if (plotOptionControl != null)
        plottingOption = this.plotOptionControl.getSelectedOption();
      controlComboBox.removeAllItems();
      this.initControlList();
      // add the Epistemic control panel option if Epistemic ERF is selected
      if (erfGuiBean.isEpistemicList()) {
        this.controlComboBox.addItem(EPISTEMIC_CONTROL);
        controlComboBox.setSelectedItem(EPISTEMIC_CONTROL);
      }
      else if (plottingOption != null &&
               plottingOption.equalsIgnoreCase(PlottingOptionControl.
                                               ADD_TO_EXISTING)) {
        JOptionPane.showMessageDialog(this,
            "Cannot add to existing without selecting ERF Epistemic list",
                                      "Input Error",
                                      JOptionPane.INFORMATION_MESSAGE);
        plotOptionControl.setSelectedOption(PlottingOptionControl.PLOT_ON_TOP);
        setButtonsEnable(true);
      }
    }
  }


  /**
   * this function is called to draw the graph
   */
  protected void addButton() {
    setButtonsEnable(false);
    // do not show warning messages in IMR gui bean. this is needed
    // so that warning messages for site parameters are not shown when Add graph is clicked
    imrGuiBean.showWarningMessages(false);
    if(plotOptionControl !=null){
      if(this.plotOptionControl.getSelectedOption().equals(PlottingOptionControl.PLOT_ON_TOP))
        addData = true;
      else
        addData = false;
    }
    try{
        createCalcInstance();
    }catch(Exception e){
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      e.printStackTrace();
    }

    // check if progress bar is desired and set it up if so
    if(this.progressCheckBox.isSelected())  {

      timer = new Timer(500, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          try{
            if(!isEqkList){

              int totRupture = calc.getTotRuptures();
              int currRupture = calc.getCurrRuptures();
              if (currRupture != -1)
                progressClass.updateProgress(currRupture, totRupture);

            }
            else{
              if((numERFsInEpistemicList+1) !=0 && !isHazardCalcDone)
                progressClass.updateProgress(currentERFInEpistemicListForHazardCurve,numERFsInEpistemicList);
            }
            if (isHazardCalcDone) {
              timer.stop();
              progressClass.dispose();
              drawGraph();
            }
          }catch(Exception e){
            //e.printStackTrace();
            timer.stop();
            setButtonsEnable(true);
            ExceptionWindow bugWindow = new ExceptionWindow(getApplicationComponent(),e.getStackTrace(),getParametersInfoAsString());
            bugWindow.setVisible(true);
            bugWindow.pack();
          }
        }
      });

      calcThread = new Thread(this);
      calcThread.start();
    }
    else {
      this.computeHazardCurve();
      this.drawGraph();
    }
  }

  /**
   * Gets the SA Period Values for the IMR
   * @param imr
   */
  private void getSA_PeriodForIMR(AttenuationRelationshipAPI imr) {
    ListIterator it = imr.getSupportedIntensityMeasuresIterator();
    while (it.hasNext()) {
      DependentParameterAPI tempParam = (DependentParameterAPI) it.next();
      if (tempParam.getName().equalsIgnoreCase(this.SA_NAME)) {
        ListIterator it1 = tempParam.getIndependentParametersIterator();
        while (it1.hasNext()) {
          ParameterAPI independentParam = (ParameterAPI) it1.next();
          if (independentParam.getName().equalsIgnoreCase(this.SA_PERIOD)) {
            saPeriodVector = ( (DoubleDiscreteParameter) independentParam).
                getAllowedDoubles();
            return;
          }
        }
      }
    }
  }

  /**
   * Handle the Eqk Forecast List.
   * @param site : Selected site
   * @param imr : selected IMR
   * @param eqkRupForecast : List of Eqk Rup forecasts
   */
  protected void handleForecastList(Site site,
                                    AttenuationRelationshipAPI imr,
                                    ERF_API forecast,
                                    double imlProbValue) {

    ERF_List erfList = (ERF_List) forecast;

    numERFsInEpistemicList = erfList.getNumERFs(); // get the num of ERFs in the list

    if (addData) //add new data on top of the existing data
      weightedFuncList = new WeightedFuncListforPlotting();
    //if we are adding to the exsintig data then there is no need to create the new instance
    //weighted functon list.
    else if (!addData && weightedFuncList == null) {
      JOptionPane.showMessageDialog(this, "No ERF List Exists",
                                    "Wrong selection", JOptionPane.OK_OPTION);
      return;
    }

    try {
      // calculate the hazard curve
      if (distanceControlPanel != null)
        calc.setMaxSourceDistance(distanceControlPanel.getDistance());
    }
    catch (Exception e) {
      setButtonsEnable(true);
      ExceptionWindow bugWindow = new ExceptionWindow(this, e.getStackTrace(),
          getParametersInfoAsString());
      bugWindow.setVisible(true);
      bugWindow.pack();
      e.printStackTrace();
    }

    DiscretizedFuncList hazardFuncList = new DiscretizedFuncList();
    for (int i = 0; i < numERFsInEpistemicList; ++i) {
      //current ERF's being used to calculated Hazard Curve
      currentERFInEpistemicListForHazardCurve = i;
      DiscretizedFuncAPI hazFunction = null;

      try {

        if (probAtIML)
          hazFunction = (DiscretizedFuncAPI) calc.getSpectrumCurve(
              site, imr, erfList.getERF(i),
              imlProbValue, saPeriodVector);
        else {
          hazFunction = new ArbitrarilyDiscretizedFunc();

          // initialize the values in condProbfunc with log values as passed in hazFunction
          initX_Values(hazFunction);


            hazFunction = calc.getIML_SpectrumCurve(hazFunction, site, imr,
            		erfList.getERF(i),
                                                    imlProbValue, saPeriodVector);


        	}
      }
      catch(RemoteException e){
	    	  setButtonsEnable(true);
	      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),getParametersInfoAsString());
	      bugWindow.setVisible(true);
	      bugWindow.pack();
	      e.printStackTrace();
      }
      catch (RuntimeException e) {
      	  	//e.printStackTrace();
      	  setButtonsEnable(true);
          JOptionPane.showMessageDialog(this, e.getMessage(),
                                        "Parameters Invalid",
                                        JOptionPane.INFORMATION_MESSAGE);
          return;
      }
        //System.out.println("Num points:" +hazFunction.toString());

      hazardFuncList.add(hazFunction);
    }
    weightedFuncList.addList(erfList.getRelativeWeightsList(), hazardFuncList);
    //setting the information inside the weighted function list if adding on top of exisintg data
    if (addData)
      weightedFuncList.setInfo(getParametersInfoAsString());
    else //setting the information inside the weighted function list if adding the data to the existing data
      weightedFuncList.setInfo(getParametersInfoAsString() + "\n" +
                               "Previous List Info:\n" +
                               "--------------------\n" +
                               weightedFuncList.getInfo());

    //individual curves are to be plotted
    if (!isAllCurves)
      weightedFuncList.setIndividualCurvesToPlot(false);
    else
      weightedFuncList.setIndividualCurvesToPlot(true);

    // if custom fractile needed to be plotted
    if (this.fractileOption.equalsIgnoreCase
        (ERF_EpistemicListControlPanel.CUSTOM_FRACTILE)) {
      weightedFuncList.setFractilesToPlot(true);
      weightedFuncList.addFractiles(epistemicControlPanel.
                                    getSelectedFractileValues());
    }
    else
      weightedFuncList.setFractilesToPlot(false);

    // calculate average
    if (this.avgSelected) {
      weightedFuncList.setMeanToPlot(true);
      weightedFuncList.addMean();
    }
    else
      weightedFuncList.setMeanToPlot(false);

    //adding the data to the functionlist if adding on top
    if (addData)
      functionList.add(weightedFuncList);
    // set the X, Y axis label
  }

  /**
   * set x values in log space for Hazard Function to be passed to IMR as IMT is
   * always SA
   * It accepts 1 parameters
   *
   * @param originalFunc :  this is the function with X values set
   */
  private void initX_Values(DiscretizedFuncAPI arb){

    //iml@Prob then we have to interpolate over a range of X-Values
    if (!useCustomX_Values)
      function = imtInfo.getDefaultHazardCurve(SA_NAME);


      for (int i = 0; i < function.getNum(); ++i)
        arb.set(function.getX(i), 1);
  }


  /**
   *
   * @returns the String containing the values selected for different parameters
   */
  public String getMapParametersInfoAsHTML() {
    String imrMetadata;
    if (this.isProbCurve) //if Probabilistic calculation then only add the metadata
      //for visible parameters
      imrMetadata = imrGuiBean.getVisibleParametersCloned().
          getParameterListMetadataString();
    else //if deterministic calculations then add all IMR params metadata.
      imrMetadata = imrGuiBean.getSelectedIMR_Instance().getAllParamMetadata();

    double maxSourceSiteDistance;
    if (distanceControlPanel != null)
      maxSourceSiteDistance = distanceControlPanel.getDistance();
    else
      maxSourceSiteDistance = HazardCurveCalculator.MAX_DISTANCE_DEFAULT;

    return "<br>" + "IMR Param List:" + "<br>" +
        "---------------" + "<br>" +
        imrMetadata + "<br><br>" +
        "Site Param List: " + "<br>" +
        "----------------" + "<br>" +
        siteGuiBean.getParameterListEditor().getVisibleParametersCloned().
        getParameterListMetadataString() + "<br><br>" +
        "IML/Prob Param List: " + "<br>" +
        "---------------" + "<br>" +
        imlProbGuiBean.getVisibleParametersCloned().
        getParameterListMetadataString() + "<br><br>" +
        "Forecast Param List: " + "<br>" +
        "--------------------" + "<br>" +
        erfGuiBean.getERFParameterList().getParameterListMetadataString() +
        "<br><br>" +
        "TimeSpan Param List: " + "<br>" +
        "--------------------" + "<br>" +
        erfGuiBean.getSelectedERFTimespanGuiBean().
        getParameterListMetadataString() + "<br><br>" +
        "Max. Source-Site Distance = " + maxSourceSiteDistance;
  }

  //Main method
  public static void main(String[] args) {
    HazardSpectrumLocalModeApplication applet = new
        HazardSpectrumLocalModeApplication();
    applet.checkAppVersion();
    applet.init();
    applet.setVisible(true);
  }
}
