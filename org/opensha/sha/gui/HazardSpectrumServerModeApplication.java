/**
 * 
 */
package org.opensha.sha.gui;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import org.opensha.sha.gui.controls.ERF_EpistemicListControlPanel;
import org.opensha.sha.gui.controls.PlottingOptionControl;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.ExceptionWindow;
import org.opensha.sha.gui.infoTools.WeightedFuncListforPlotting;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;

/**
 * @author nitingupta
 *
 */
public class HazardSpectrumServerModeApplication extends HazardCurveServerModeApplication{

	  //Static String to tell the IMT as the SA becuase it is the only supported IMT for this Application
	  protected static String SA_NAME = "SA";
	  private static String SA_PERIOD = "SA Period";
	  //Axis Labels
	  private static final String IML = "SA (g)";
	  private static final String PROB_AT_EXCEED = "Probability of Exceedance";
	  private static final String X_AXIS_LABEL = "Period (sec)";


	  private IMLorProbSelectorGuiBean imlProbGuiBean;

	  //ArrayList that stores the SA Period values for the IMR
	  private ArrayList saPeriodVector ;
	  //Total number of the SA Period Values
	  private int numSA_PeriodVals;
	  //Total number of the values for which we have ran the Hazard Curve
	  private int numSA_PeriodValDone=0;
	
	
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
	      try{
	        initERF_GuiBean();
	      }catch(RuntimeException e){
	        JOptionPane.showMessageDialog(this,"Connection to ERF's failed","Internet Connection Problem",
	                                      JOptionPane.OK_OPTION);
	        e.printStackTrace();
	        System.exit(0);
	      }
	    }
	    catch(Exception e) {
	      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),"Exception occured while creating the GUI.\n"+
	          "No Parameters have been set");
	      bugWindow.setVisible(true);
	      bugWindow.pack();
	      //e.printStackTrace();
	    }

	    ((JPanel)getContentPane()).updateUI();
	  }
	  

	  /**
	   * Gets the probabilities functiion based on selected parameters
	   * this function is called when add Graph is clicked
	   */
	  private void computeHazardCurve() {

	    //starting the calculation
	    isHazardCalcDone = false;
	    

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
	      progressClass = new CalcProgressBar("Hazard-Curve Calc Status",
	                                          "Beginning Calculation ");
	      progressClass.displayProgressBar();
	      timer.start();
	    }

	    // get the selected IMR
	    AttenuationRelationship imr = (AttenuationRelationship) imrGuiBean.getSelectedIMR_Instance();

	    // make a site object to pass to IMR
	    Site site = siteGuiBean.getSite();

	    //initialize the values in condProbfunc with log values as passed in hazFunction
	    // intialize the hazard function
	    ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
	    ArbitrarilyDiscretizedFunc tempHazFunction = new ArbitrarilyDiscretizedFunc();

	    
	    //what selection does the user have made, IML@Prob or Prob@IML
	    String imlOrProb=imlProbGuiBean.getSelectedOption();
	    //gets the IML or Prob value filled in by the user
	    double imlProbValue=imlProbGuiBean.getIML_Prob();
	    boolean imlAtProb = false, probAtIML = false;
	    if(imlOrProb.equalsIgnoreCase(imlProbGuiBean.IML_AT_PROB)){
	      yAxisName =IML;
	      imlAtProb=true;
	    }
	    else{
	    	  yAxisName =PROB_AT_EXCEED;
	      probAtIML=true;
	    }
	    xAxisName = X_AXIS_LABEL;
	    // check whether this forecast is a Forecast List
	    // if this is forecast list , handle it differently
	    boolean isEqkForecastList = false;
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
	      handleForecastList(site, imr, forecast,imlProbValue,imlAtProb,probAtIML);
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
	      if (distanceControlPanel != null) calc.setMaxSourceDistance(
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
	    // initialize the values in condProbfunc with log values as passed in hazFunction
	    initX_Values(tempHazFunction,imlProbValue,imlAtProb,probAtIML);

	    //System.out.println("22222222HazFunction: "+hazFunction.toString());
	    try {
	      // calculate the hazard curve
	      //eqkRupForecast = (EqkRupForecastAPI)FileUtils.loadObject("erf.obj");
	      try {
	        if (isProbCurve){
//	        	iterating over all the SA Periods for the IMR's
	            for(int i=0;i< numSA_PeriodVals;++i){
	              double saPeriodVal = ((Double)this.saPeriodVector.get(i)).doubleValue();
	              imr.getParameter(this.SA_PERIOD).setValue(this.saPeriodVector.get(i));
	              tempHazFunction = (ArbitrarilyDiscretizedFunc) calc.getHazardCurve(
	            		  tempHazFunction, site,imr, (EqkRupForecastAPI) forecast);
//	              number of SA Periods for which we have ran the Hazard Curve
	              this.numSA_PeriodValDone =i;
	              double val = getHazFuncIML_ProbValues(tempHazFunction,imlProbValue,imlAtProb,probAtIML);
	              hazFunction.set(saPeriodVal,val);
	            }
	        }
	        else {
	          progressCheckBox.setSelected(false);
	          progressCheckBox.setEnabled(false);
	          if(probAtIML){
	//	        	iterating over all the SA Periods for the IMR's
		          for(int i=0;i< this.numSA_PeriodVals;++i){
		        	  double saPeriodVal = ((Double)this.saPeriodVector.get(i)).doubleValue();
		              imr.getParameter(this.SA_PERIOD).setValue(this.saPeriodVector.get(i));
		              double imlLogVal = Math.log(imlProbValue);
		              //double val = 0.4343*Math.log(imr.getExceedProbability(imlLogVal));
		              double val = imr.getExceedProbability(imlLogVal);
		              //adding values to the hazard function
		              hazFunction.set(saPeriodVal,val);
		              //number of SA Periods for which we have ran the Hazard Curve
		              this.numSA_PeriodValDone =i;
		          }
	          }
	          else{  //if the user has selected IML@prob
	              //iterating over all the SA Periods for the IMR
	              for(int i=0;i<this.numSA_PeriodVals;++i){
	                double saPeriodVal = ((Double)(saPeriodVector.get(i))).doubleValue();
	                imr.getParameter(this.SA_PERIOD).setValue(this.saPeriodVector.get(i));
	                imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(imlProbValue));
	                //double val = 0.4343*imr.getIML_AtExceedProb();
	                //adding values to the Hazard Function
	                double val = Math.exp(imr.getIML_AtExceedProb());
	                hazFunction.set(saPeriodVal,val);
	                //number of SA Periods for which we have ran the Hazard Curve
	                this.numSA_PeriodValDone =i;
	              }
	          }
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
	      hazFunction.setInfo(getParametersInfoAsString());
	    }
	    catch (RuntimeException e) {
	      JOptionPane.showMessageDialog(this, e.getMessage(),
	                                    "Parameters Invalid",
	                                    JOptionPane.INFORMATION_MESSAGE);
	      //e.printStackTrace();
	      setButtonsEnable(true);
	      return;
	    }

	    // add the function to the function list
	    functionList.add(hazFunction);

	  }

	  /**
	   * set x values back from the log space to the original linear values
	   * for Hazard Function after completion of the Hazard Calculations
	   * and returns back to the user IML or Prob value
	   * It accepts 1 parameters
	   *
	   * @param hazFunction :  this is the function with X values set
	   */
	  private double getHazFuncIML_ProbValues(ArbitrarilyDiscretizedFunc hazFunc,
	                                     double imlProbVal,boolean imlAtProb, boolean probAtIML) {

	    //gets the number of points in the function
	    int numPoints = hazFunc.getNum();
	    //prob at iml is selected just return the Y Value back
	    if(probAtIML)
	      return hazFunc.getY(numPoints-1);
	    else{ //if iml at prob is selected just return the interpolated IML value.
	      ArbitrarilyDiscretizedFunc tempFunc = new ArbitrarilyDiscretizedFunc();
	      for(int i=0; i<numPoints; ++i)
	        tempFunc.set(function.getX(i),hazFunc.getY(i));

	      /*we are calling the function (getFirst InterpolatedX ) becuase x values for the PEER
	      * are the X values and the function we get from the Hazard Curve Calc are the
	      * Y Values for the Prob., now we have to find the interpolated IML which corresponds
	      * X value and imlProbVal is the Y value parameter which this function accepts
	      */
	      //returns the interpolated IML value for the given prob.
	      return tempFunc.getFirstInterpolatedX_inLogXLogYDomain(imlProbVal);

	    }
	  }
	  
	  
	  
	  /**
	   * Initialise the IMT_Prob Selector Gui Bean
	   */
	  private void initImlProb_GuiBean(){
	    imlProbGuiBean = new IMLorProbSelectorGuiBean();
	    this.imtPanel.add(imlProbGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
	                GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
	  }

	  
	  /**
	   * Initialize the items to be added to the control list
	   */
	  protected void initControlList() {
	    controlComboBox.addItem(CONTROL_PANELS);
	   	controlComboBox.addItem(DISTANCE_CONTROL);
	    controlComboBox.addItem(SITES_OF_INTEREST_CONTROL);
	    controlComboBox.addItem(CVM_CONTROL);
	    controlComboBox.addItem(X_VALUES_CONTROL);
	    controlComboBox.addItem(PLOTTING_OPTION);
	    controlComboBox.addItem(XY_Values_Control);
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
	  public void parameterChange( ParameterChangeEvent event ) {

	    String S = ": parameterChange(): ";
	    if ( D )  System.out.println( "\n" + S + "starting: " );

	    String name1 = event.getParameterName();

	    // if IMR selection changed, update the site parameter list and supported IMT
	    if ( name1.equalsIgnoreCase(imrGuiBean.IMR_PARAM_NAME)) {
	    		AttenuationRelationshipAPI imr = 	imrGuiBean.getSelectedIMR_Instance();
	        //set the intensity measure fo	r the IMR
	        imr.setIntensityMeasure(SA_NAME);
	        //gets the SA Period Values fo	r the IMR
	        this.getSA_PeriodForIMR(imr);
	        siteGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
	        siteGuiBean.validate();
	        siteGuiBean.repaint();
	      }
	      if(name1.equalsIgnoreCase(this.erfGuiBean.ERF_PARAM_NAME)) {

	        String plottingOption = null;
	        if(plotOptionControl !=null)
	          plottingOption=this.plotOptionControl.getSelectedOption();
	        controlComboBox.removeAllItems();
	        this.initControlList();
	        // add the Epistemic control panel option if Epistemic ERF is selected
	        if(erfGuiBean.isEpistemicList()) {
	          this.controlComboBox.addItem(EPISTEMIC_CONTROL);
	          controlComboBox.setSelectedItem(EPISTEMIC_CONTROL);
	        }
	        else if(plottingOption!= null && plottingOption.equalsIgnoreCase(PlottingOptionControl.ADD_TO_EXISTING)){
	          JOptionPane.showMessageDialog(this,"Cannot add to existing without selecting ERF Epistemic list",
	                                        "Input Error",JOptionPane.INFORMATION_MESSAGE);
	          plotOptionControl.setSelectedOption(PlottingOptionControl.PLOT_ON_TOP);
	          setButtonsEnable(true);
	        }
	      }
	  }

	  /**
	   * Gets the SA Period Values for the IMR
	   * @param imr
	   */
	  private void getSA_PeriodForIMR(AttenuationRelationshipAPI imr){
	    ListIterator it =imr.getSupportedIntensityMeasuresIterator();
	    while(it.hasNext()){
	      DependentParameterAPI  tempParam = (DependentParameterAPI)it.next();
	      if(tempParam.getName().equalsIgnoreCase(this.SA_NAME)){
	        ListIterator it1 = tempParam.getIndependentParametersIterator();
	        while(it1.hasNext()){
	          ParameterAPI independentParam = (ParameterAPI)it1.next();
	          if(independentParam.getName().equalsIgnoreCase(this.SA_PERIOD)){
	            saPeriodVector = ((DoubleDiscreteParameter)independentParam).getAllowedDoubles();
	            numSA_PeriodVals = saPeriodVector.size();
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
	  private void handleForecastList(Site site,
              AttenuationRelationshipAPI imr,
              ERF_API eqkRupForecast,
              double imlProbValue,boolean imlAtProb,
              boolean probAtIML) {

	    ERF_List erfList  = (ERF_List)eqkRupForecast;

	    numERFsInEpistemicList = erfList.getNumERFs(); // get the num of ERFs in the list


	    if(addData) //add new data on top of the existing data
	      weightedFuncList = new WeightedFuncListforPlotting();
	    //if we are adding to the exsintig data then there is no need to create the new instance
	    //weighted functon list.
	    else if(!addData && weightedFuncList == null){
	      JOptionPane.showMessageDialog(this,"No ERF List Exists","Wrong selection",JOptionPane.OK_OPTION);
	      return;
	    }

	    try{
	      // calculate the hazard curve
	      if(distanceControlPanel!=null) calc.setMaxSourceDistance(distanceControlPanel.getDistance());
	    }catch(Exception e){
	      setButtonsEnable(true);
	      ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),getParametersInfoAsString());
	      bugWindow.setVisible(true);
	      bugWindow.pack();
	      e.printStackTrace();
	    }

	    DiscretizedFuncList hazardFuncList = new DiscretizedFuncList();
	    for(int i=0; i<numERFsInEpistemicList; ++i) {
	      //current ERF's being used to calculated Hazard Curve
	      currentERFInEpistemicListForHazardCurve = i;
	      ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
	      ArbitrarilyDiscretizedFunc tempHazFunction = new ArbitrarilyDiscretizedFunc();

	      //intialize the hazard function
	      initX_Values(tempHazFunction,imlProbValue,imlAtProb,probAtIML);
	      try {
	        try{
//	        	iterating over all the SA Periods for the IMR's
	            for(int j=0;j< this.numSA_PeriodVals;++j){
	              double saPeriodVal = ((Double)this.saPeriodVector.get(j)).doubleValue();
	              imr.getParameter(this.SA_PERIOD).setValue(this.saPeriodVector.get(j));
	              // calculate the hazard curve
	              tempHazFunction=(ArbitrarilyDiscretizedFunc)calc.getHazardCurve(tempHazFunction, site, imr, erfList.getERF(i));
	              //number of SA Periods for which we have ran the Hazard Curve
	              this.numSA_PeriodValDone =j;
	              double val= getHazFuncIML_ProbValues(tempHazFunction,imlProbValue,imlAtProb,probAtIML);
	              hazFunction.set(saPeriodVal,val);
	            }
	          //System.out.println("Num points:" +hazFunction.toString());
	        }catch(Exception e){
	          setButtonsEnable(true);
	          ExceptionWindow bugWindow = new ExceptionWindow(this,e.getStackTrace(),getParametersInfoAsString());
	          bugWindow.setVisible(true);
	          bugWindow.pack();
	          e.printStackTrace();
	        }
	      }catch (RuntimeException e) {
	        JOptionPane.showMessageDialog(this, e.getMessage(),
	                                      "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
	        setButtonsEnable(true);
	        //e.printStackTrace();
	        return;
	      }
	      hazardFuncList.add(hazFunction);
	    }
	    weightedFuncList.addList(erfList.getRelativeWeightsList(),hazardFuncList);
	    //setting the information inside the weighted function list if adding on top of exisintg data
	    if(addData)
	      weightedFuncList.setInfo(getParametersInfoAsString());
	    else //setting the information inside the weighted function list if adding the data to the existing data
	      weightedFuncList.setInfo(getParametersInfoAsString()+"\n"+"Previous List Info:\n"+
	                               "--------------------\n"+weightedFuncList.getInfo());




	   //individual curves are to be plotted
	   if(!isAllCurves)
	     weightedFuncList.setIndividualCurvesToPlot(false);
	   else
	     weightedFuncList.setIndividualCurvesToPlot(true);

	   // if custom fractile needed to be plotted
	   if(this.fractileOption.equalsIgnoreCase
	      (ERF_EpistemicListControlPanel.CUSTOM_FRACTILE)) {
	     weightedFuncList.setFractilesToPlot(true);
	     weightedFuncList.addFractiles(epistemicControlPanel.getSelectedFractileValues());
	   }
	   else weightedFuncList.setFractilesToPlot(false);

	   // calculate average
	   if(this.avgSelected) {
	     weightedFuncList.setMeanToPlot(true);
	     weightedFuncList.addMean();
	   }else weightedFuncList.setMeanToPlot(false);

	   //adding the data to the functionlist if adding on top
	   if(addData)
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
	  private void initX_Values(DiscretizedFuncAPI arb, double imlProbVal,boolean imlAtProb,
	                            boolean probAtIML){

	    if(probAtIML) //prob@iml
	      arb.set(Math.log(imlProbVal),1);
	    else{ //iml@Prob then we have to interpolate over a range of X-Values
	      if(!useCustomX_Values)
	        function = imtInfo.getDefaultHazardCurve(SA_NAME);

	      if (imtInfo.isIMT_LogNormalDist(SA_NAME)) {
	        for(int i=0;i<function.getNum();++i)
	          arb.set(Math.log(function.getX(i)),1);
	      }
	    }
	  }
	  
	  
	  
	  //Main method
	  public static void main(String[] args) {
		HazardSpectrumServerModeApplication applet = new HazardSpectrumServerModeApplication();
	    //applet.checkAppVersion();
	    applet.init();
	    applet.setVisible(true);
	  }	
}
