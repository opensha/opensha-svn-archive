package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.util.*;


import org.opensha.param.editor.*;
import org.opensha.param.*;
import org.opensha.param.event.*;
import org.opensha.sha.gui.servlets.CyberShakeHazardDataSelectorServlet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurveComputation;
import org.opensha.cybershake.db.NSHMP2002_ToDB;
import org.opensha.cybershake.db.SiteInfo2DBAPI;
import org.opensha.data.Location;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.sha.gui.beans.*;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import java.lang.reflect.*;
import org.opensha.sha.earthquake.EqkRupForecastAPI;

import java.text.DecimalFormat;

/**
 * <p>Title: CyberShakePlotFromDBControlPanel </p>
 *
 * <p>Description: This allows to view the Deterministic Cybershake Curves with
 * that of OpenSHA using teh Empirical based AttenuationRelationships.
 * </p>
 *
 * @author Nitin Gupta
 * @since March 7,2006
 * @version 1.0
 */
public class CyberShakePlotFromDBControlPanel
    extends JFrame implements ParameterChangeListener{


  private static final boolean D = false;
  public static final String SITE_SELECTOR_PARAM = "CyberShake Site";
  public static final String SA_PERIOD_SELECTOR_PARAM = "SA Period";
  public static final String SRC_INDEX_PARAM = "Source Index";
  public static final String RUP_INDEX_PARAM = "Rupture Index";

  private static final String DETER_PROB_SELECTOR_PARAM = "Curve Type";
  private static final String PROB_CURVE = "Probabilistic Curve";
  private static final String DETER_CURVE = "Deterministic Curve";

  JPanel guiPanel = new JPanel();
  JButton submitButton = new JButton();
  JButton paramSettingButton = new JButton();
  JLabel controlPanelLabel = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();

  //Curve type selector param
  private StringParameter curveTypeSelectorParam;

  //Site selection param
  private StringParameter siteSelectionParam;
  //SA Period selection param
  private StringParameter saPeriodParam;

  //source index parameter
  private StringParameter srcIndexParam;

  //rupture index parameter
  private StringParameter rupIndexParam;

  //Editor to show the parameters in the panel
  private ParameterListEditor listEditor;
  //list to show the parameters
  private ParameterList paramList;

  //handle to the application using this control panel
  private CyberShakePlotControlPanelAPI application;

  //if deterministic curve needs to be plotted
  private boolean isDeterministic ;
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  
  //Database connection 
  private static String HOST_NAME = "intensity.usc.edu";
  private static String DATABASE_NAME = "CyberShake";
  private static final DBAccess db = new DBAccess(HOST_NAME,DATABASE_NAME);
  
  /**
   * Handle to Cybershake Sites info in DB
   */
  private CybershakeSiteInfo2DB csSites = new CybershakeSiteInfo2DB(db);
  
  /**
   * Handle to NSHMP 2002 ERF in DB
   */
  private NSHMP2002_ToDB erfDb = new NSHMP2002_ToDB(db);
  
  private HazardCurveComputation hazCurve = new HazardCurveComputation(db);
  
  //current selection of site, srcId and rupId from the cyberShake database
  private String selectedSite;
  private int selectedSrcId,selectedRupId;
  private String saPeriod;


  public CyberShakePlotFromDBControlPanel(CyberShakePlotControlPanelAPI app) {
    application = app;
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    this.pack();
    Component parent = (Component)app;
    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                     parent.getY());
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    guiPanel.setLayout(gridBagLayout1);
    submitButton.setText("OK");
    paramSettingButton.setText("Set Pathway-1 params");
    paramSettingButton.setToolTipText("Sets the same parameters in the Pathway-1\n "+
        "application as in Cybershake calculations.");
    controlPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
    controlPanelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    controlPanelLabel.setText("Cybershake Hazard Data Plot Control");
    //creating the Site and SA Period selection for the Cybershake control panel
    initCyberShakeControlPanel();
    this.getContentPane().add(guiPanel, java.awt.BorderLayout.CENTER);
    guiPanel.add(controlPanelLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
        , GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(6, 10, 0, 12), 145, 23));
    guiPanel.add(listEditor, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 2, 0, 2), 386, 210));
    guiPanel.add(submitButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 17, 1, 114), 20, 0));
    guiPanel.add(paramSettingButton,
                 new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                                        , GridBagConstraints.CENTER,
                                        GridBagConstraints.NONE,
                                        new Insets(0, 72, 1, 0), 5, 0));
    submitButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        submitButton_actionPerformed(e);
      }
    });
    paramSettingButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        paramSettingButton_actionPerformed(e);
      }
    });
    this.setSize(80,300);
  }


  /**
   * Creates the Cybershake site and SA Period GUI elements.
   * Allows the user to select the Site and SA period value for which
   * hazard curve needs to be plotted.
   */
  private void initCyberShakeControlPanel(){
    ArrayList siteList  = this.csSites.getCS_SitesList();
  

    ArrayList supportedCurvesType = new ArrayList();
    supportedCurvesType.add(PROB_CURVE);
    supportedCurvesType.add(DETER_CURVE);

    curveTypeSelectorParam = new StringParameter(this.DETER_PROB_SELECTOR_PARAM,
                                                 supportedCurvesType,
                                                 (String)
                                                 supportedCurvesType.get(0));

    paramList = new ParameterList();

    siteSelectionParam = new StringParameter(SITE_SELECTOR_PARAM,
                                             siteList,(String)siteList.get(0));
    initSA_PeriodParam();
    this.saPeriod = (String)saPeriodParam.getValue();
    selectedSite = (String)siteSelectionParam.getValue();
    initSrcIndexParam();
    String srcId = (String)this.srcIndexParam.getValue();
    selectedSrcId = Integer.parseInt(srcId);
    initRupIndexParam();
    selectedRupId = Integer.parseInt((String)this.rupIndexParam.getValue());
    paramList.addParameter(curveTypeSelectorParam);
    paramList.addParameter(siteSelectionParam);
    paramList.addParameter(saPeriodParam);
    paramList.addParameter(srcIndexParam);
    paramList.addParameter(rupIndexParam);
    siteSelectionParam.addParameterChangeListener(this);
    curveTypeSelectorParam.addParameterChangeListener(this);
    listEditor = new ParameterListEditor(paramList);
    listEditor.setTitle("Set Params for Cybershake Curve");
    makeParamVisible();
  }


  /**
   * Creates the SA Period Parameter which allows the user to select the
   * SA Period for a given site for which hazard data needs to be plotted.
   */
  private void initSA_PeriodParam(){
    String siteName = (String)siteSelectionParam.getValue();
    ArrayList<String> saPeriods = hazCurve.getSupportedSA_PeriodStrings();
    saPeriodParam = new StringParameter(this.SA_PERIOD_SELECTOR_PARAM,
        saPeriods,saPeriods.get(0));
    saPeriodParam.addParameterChangeListener(this);
  }


  /**
   * Makes the parameters visible or invisible based on if it is deterministic
   * or prob. curve.
   */
  private void makeParamVisible() {
    String curveType = (String)curveTypeSelectorParam.getValue();
    if(curveType.equals(PROB_CURVE))
      this.isDeterministic = false;
    else
      this.isDeterministic = true;
    application.setCurveType(isDeterministic);
    listEditor.getParameterEditor(SRC_INDEX_PARAM).setVisible(isDeterministic);
    listEditor.getParameterEditor(RUP_INDEX_PARAM).setVisible(isDeterministic);
  }

  /**
   * Creates the Src Id selection parameter displaying all the src ids for a given Cybershake
   * site for which deterministic calculations can be done.
   */
  private void initSrcIndexParam(){
    ArrayList srcIdList = this.csSites.getSrcIDsForSite(selectedSite);
    int size = srcIdList.size();
    for(int i=0;i<size;++i)
    	srcIdList.set(i, ""+srcIdList.get(i));
    
    srcIndexParam = new StringParameter(SRC_INDEX_PARAM,srcIdList,(String)srcIdList.get(0));
    srcIndexParam.addParameterChangeListener(this);
  }

  /**
   * Creates the Rupture Id selection parameter displaying all the rup ids for a given Cybershake
   * site for which deterministic calculations can be done.
   */
  private void initRupIndexParam(){
	 ArrayList rupIdList = this.csSites.getRupIDsForSite(selectedSite, selectedSrcId);
	 int size = rupIdList.size();
	 for(int i=0;i<size;++i)
	     rupIdList.set(i, ""+rupIdList.get(i));
	 rupIndexParam = new StringParameter(RUP_INDEX_PARAM,rupIdList,(String)rupIdList.get(0));   
	 rupIndexParam.addParameterChangeListener(this);
  }

  /**
   * Updates the list editor when user changes the Cybershake site
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(SITE_SELECTOR_PARAM)){
      selectedSite = (String)siteSelectionParam.getValue();
      initSA_PeriodParam();
      initSrcIndexParam();
      initRupIndexParam();
      listEditor.replaceParameterForEditor(SA_PERIOD_SELECTOR_PARAM,saPeriodParam );
      listEditor.replaceParameterForEditor(SRC_INDEX_PARAM,srcIndexParam);
      listEditor.replaceParameterForEditor(RUP_INDEX_PARAM,rupIndexParam);
    }
    else if(paramName.equals(this.SRC_INDEX_PARAM)){
    	String srcId = (String)this.srcIndexParam.getValue();
        selectedSrcId = Integer.parseInt(srcId);
        initRupIndexParam();
        listEditor.replaceParameterForEditor(RUP_INDEX_PARAM,rupIndexParam);
    }
    else if(paramName.equals(this.RUP_INDEX_PARAM))
    	selectedRupId = Integer.parseInt((String)this.rupIndexParam.getValue());
    else if(paramName.equals(SA_PERIOD_SELECTOR_PARAM)){
    	saPeriod = (String)saPeriodParam.getValue();
    	System.out.println("SA Period = "+saPeriod);
    }
    else if(paramName.equals(DETER_PROB_SELECTOR_PARAM))
      this.makeParamVisible();

    listEditor.refreshParamEditor();
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @return ArrayList Hazard Data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getHazardData(ArrayList imlVals) throws RuntimeException{
    DiscretizedFuncAPI cyberShakeHazardData= hazCurve.computeHazardCurve(imlVals,selectedSite,
    		                            Frankel02_AdjustableEqkRupForecast.NAME, saPeriod);
 
    return cyberShakeHazardData;
  }


  /**
   * 
   * @return ArbitrarilyDiscretizedFunc Determinitic curve data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getDeterministicData(ArrayList imlVals) throws
      RuntimeException {
    DiscretizedFuncAPI cyberShakeDeterminicticHazardCurve = hazCurve.computeDeterministicCurve(imlVals, selectedSite,
    		                                      Frankel02_AdjustableEqkRupForecast.NAME, selectedSrcId,
    		                                      selectedRupId, saPeriod);

    return cyberShakeDeterminicticHazardCurve;
  }


  /**
   * Sets the parameters in the OpenSHA application similar to what
   * is required  by the Cybershake.
   * @param actionEvent ActionEvent
   */
  private void paramSettingButton_actionPerformed(ActionEvent actionEvent) {
    setSiteParams();
    boolean imtSet = setIMT_Params();

    if(!imtSet)
      return;
    application.setX_ValuesForHazardCurve(createUSGS_PGA_Function());

    if(isDeterministic)
      setEqkSrcRupSelectorParams();
    else
      setEqkRupForecastParams();
  }

  /**
   * Retreives the Cybershake data and plots it in the application.
   * @param actionEvent ActionEvent
   */
  private void submitButton_actionPerformed(ActionEvent actionEvent) {
    ArrayList imlVals = application.getIML_Values();
    DiscretizedFuncAPI curveData = null;

    if(isDeterministic){
      curveData = getDeterministicData(imlVals);
      String name = "Cybershake deterministic curve";
      String infoString = "Site = "+ selectedSite+
          "; SA-Period = "+saPeriod+"; SourceIndex = "+selectedSrcId+
          "; RuptureIndex = "+selectedRupId;
      curveData.setName(name);
      curveData.setInfo(infoString);
      application.addCybershakeCurveData(curveData);
    }
    else{
      curveData = this.getHazardData(imlVals);
      String name = "Cybershake hazard curve";
      String infoString = "Site = "+ selectedSite+
          "; SA-Period = "+saPeriod;
      curveData.setName(name);
      curveData.setInfo(infoString);
      application.addCybershakeCurveData(curveData);

    }
  }


  /**
   * This sets the site parameters in the OpenSHA application
   * based on the chosen Cybershake site
   */
  private void setSiteParams(){
    Site_GuiBean site = application.getSiteGuiBeanInstance();
    String cyberShakeSite = (String)siteSelectionParam.getValue();
    Location loc = csSites.getCyberShakeSiteLocation(cyberShakeSite);
    site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(loc.getLatitude()));
    site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(loc.getLongitude()));
    site.getParameterListEditor().refreshParamEditor();
  }

  /**
   * Set the Eqk Rup Forecast in the OpenSHA application similar to eqk forecast
   * params used to do the cybershake calculations.
   */
  private void setEqkRupForecastParams(){
    ERF_GuiBean gui = application.getEqkRupForecastGuiBeanInstance();
    gui.getERFParameterListEditor().getParameterEditor(gui.ERF_PARAM_NAME).setValue(Frankel02_AdjustableEqkRupForecast.NAME);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        FAULT_MODEL_NAME).setValue(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
    gui.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));

    TimeSpanGuiBean timespan = gui.getSelectedERFTimespanGuiBean();
    timespan.getTimeSpan().setDuration(1.0);
    gui.getERFParameterListEditor().refreshParamEditor();
    timespan.getParameterListEditor().refreshParamEditor();
  }

  /**
   * Select the same source and rupture in the OpenSHA application for deterministic calculations,
   * similar to eqk forecast params used to do the cybershake calculations.
   */
  private void setEqkSrcRupSelectorParams(){
    EqkRupSelectorGuiBean erfRupSelectorGuiBean = application.getEqkSrcRupSelectorGuiBeanInstance();
    erfRupSelectorGuiBean.getParameterEditor(erfRupSelectorGuiBean.RUPTURE_SELECTOR_PARAM_NAME).
        setValue(erfRupSelectorGuiBean.RUPTURE_FROM_EXISTING_ERF);

    EqkRupSelectorGuiBeanAPI erfRupSelGuiBean = erfRupSelectorGuiBean.getEqkRuptureSelectorPanel();
    EqkRuptureFromERFSelectorPanel rupGuiBean = (EqkRuptureFromERFSelectorPanel)erfRupSelGuiBean;
    rupGuiBean.showAllParamsForForecast(false);
    rupGuiBean.getParameterListEditor().getParameterEditor(rupGuiBean.ERF_PARAM_NAME).
        setValue(Frankel02_AdjustableEqkRupForecast.NAME);
    ERF_GuiBean erfGuiBean = rupGuiBean.getERF_ParamEditor();

    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        FAULT_MODEL_NAME).setValue(Frankel02_AdjustableEqkRupForecast.FAULT_MODEL_STIRLING);
    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        BACK_SEIS_NAME).setValue(Frankel02_AdjustableEqkRupForecast.BACK_SEIS_EXCLUDE);
    erfGuiBean.getERFParameterListEditor().getParameterEditor(Frankel02_AdjustableEqkRupForecast.
        RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));

    TimeSpanGuiBean timespan = erfGuiBean.getSelectedERFTimespanGuiBean();
    timespan.getTimeSpan().setDuration(1.0);
    erfGuiBean.getERFParameterListEditor().refreshParamEditor();
    timespan.getParameterListEditor().refreshParamEditor();
    //rupGuiBean.updateERFAndSourceRupList();
    //rupGuiBean.getParameterListEditor().refreshParamEditor();
    try {
      EqkRupForecastAPI erf = (EqkRupForecastAPI) erfGuiBean.getSelectedERF();
      rupGuiBean.setEqkRupForecast(erf);
    }
    catch (InvocationTargetException ex) {
    }
    String srcIndex = (String)srcIndexParam.getValue();
    int srcNum = Integer.parseInt(srcIndex.trim());
    String rupIndex = (String)rupIndexParam.getValue();
    rupGuiBean.setSourceFromSelectedERF(srcNum);
    rupGuiBean.setRuptureForSelectedSource(Integer.parseInt(rupIndex));
    rupGuiBean.showAllParamsForForecast(true);
  }

  /**
   * Select the IMT and SA Period in the OpenSHA application similar to that chosen
   * in Cybershake control panel.
   */
  private boolean setIMT_Params(){
    IMT_GuiBean imtGui = application.getIMTGuiBeanInstance();
    DecimalFormat format = new DecimalFormat("0.00");
    imtGui.getParameterEditor(imtGui.IMT_PARAM_NAME).setValue("SA");
    String saPeriodString = saPeriod.substring(10);//trimming the "SA Period" string in front of the Period value 
    double saPeriodVal = Double.parseDouble(format.format(Double.parseDouble(saPeriodString.trim())));
    DoubleDiscreteParameter saPeriodParam = (DoubleDiscreteParameter)imtGui.getParameterEditor("SA Period").getParameter();
    ArrayList allowedVals = saPeriodParam.getAllowedDoubles();
    int size = allowedVals.size();
    double minSaVal = ((Double)allowedVals.get(0)).doubleValue();
    double maxSaVal = ((Double)allowedVals.get(size -1)).doubleValue();
    if ( (saPeriodVal < minSaVal) || (saPeriodVal > maxSaVal)) {
      JOptionPane.showMessageDialog(this,
                                    "This attenuation does not support the SA Period\n " +
                                    "selected in cybershake control panel. Either choose a \n different Attenuation " +
                                    "Relationship or a different SA Period");
      return false;
    }
    else {
      for (int i = 0; i < size-1; ++i) {
        double saVal_first = Double.parseDouble(format.format(((Double)allowedVals.get(i)).doubleValue()));
        double saVal_second = Double.parseDouble(format.format(((Double)allowedVals.get(i+1)).doubleValue()));
        if(saPeriodVal >= saVal_first && saPeriodVal <= saVal_second){
          if((saPeriodVal - saVal_first) <= (saVal_second - saPeriodVal))
            imtGui.getParameterEditor("SA Period").setValue((Double)allowedVals.get(i));
          else
            imtGui.getParameterEditor("SA Period").setValue((Double)allowedVals.get(i+1));
          break;
        }
      }
    }
    imtGui.refreshParamEditor();
    return true;
  }

  /**
   * initialises the function with the x and y values if the user has chosen the USGS-PGA X Vals
   * the y values are modified with the values entered by the user
   */
  private ArbitrarilyDiscretizedFunc createUSGS_PGA_Function() {
    ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();
    function.set(.005, 1);
    function.set(.007, 1);
    function.set(.0098, 1);
    function.set(.0137, 1);
    function.set(.0192, 1);
    function.set(.0269, 1);
    function.set(.0376, 1);
    function.set(.0527, 1);
    function.set(.0738, 1);
    function.set(.103, 1);
    function.set(.145, 1);
    function.set(.203, 1);
    function.set(.284, 1);
    function.set(.397, 1);
    function.set(.556, 1);
    function.set(.778, 1);
    function.set(1.09, 1);
    function.set(1.52, 1);
    function.set(2.13, 1);
    return function;
  }

}
