package org.opensha.sha.gui.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.opensha.cybershake.db.CybershakeERF;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.ERF2DB;
import org.opensha.cybershake.db.ERF2DBAPI;
import org.opensha.cybershake.db.HazardCurveComputation;
import org.opensha.cybershake.db.PeakAmplitudesFromDBAPI;
import org.opensha.data.Location;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.exceptions.ParameterException;
import org.opensha.param.DoubleDiscreteParameter;
import org.opensha.param.DoubleParameter;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBeanAPI;
import org.opensha.sha.gui.beans.EqkRuptureFromERFSelectorPanel;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.imr.AttenuationRelationship;

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
    extends JFrame implements ParameterChangeListener {
	
	public static final String ERF_NAME = MeanUCERF2.NAME;


  private static final boolean D = false;
  public static final String SITE_SELECTOR_PARAM = "CyberShake Site";
  public static final String ERF_SELECTOR_PARAM = "Earthquake Rupture Forecast";
  public static final String SA_PERIOD_SELECTOR_PARAM = "SA Period";
  public static final String SRC_INDEX_PARAM = "Source Index";
  public static final String RUP_INDEX_PARAM = "Rupture Index";
  public static final String SGT_VAR_PARAM = "SGT Variation ID";
  public static final String RUP_VAR_SCENARIO_PARAM = "Rupture Variation Scenario ID";

  private static final String DETER_PROB_SELECTOR_PARAM = "Curve Type";
  private static final String PROB_CURVE = "Probabilistic Curve";
  private static final String DETER_CURVE = "Deterministic Curve";
  
  private static final String NONE_AVAILABLE_STRING = "None Available";

  JPanel guiPanel = new JPanel();
  JButton submitButton = new JButton();
  JButton paramSettingButton = new JButton();
  JLabel controlPanelLabel = new JLabel();
  BorderLayout borderLayout1 = new BorderLayout();
  
  int selectedSGTVariation = 5;
  int selectedRupVarScenario = 3;
  
  CalcProgressBar calcProgress = null;

  //Curve type selector param
  private StringParameter curveTypeSelectorParam;

  //Site selection param
  private StringParameter siteSelectionParam;
  
  //source index parameter
  private StringParameter sgtVarParam;

  //rupture index parameter
  private StringParameter rupVarScenarioParam;
  
  //SA Period selection param
  private StringParameter saPeriodParam;
  
  //SA Period selection param
  private StringParameter erfParam;

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
  
  private ERF2DBAPI erf2db = new ERF2DB(db);
  
  private HazardCurveComputation hazCurve = new HazardCurveComputation(db);
  PeakAmplitudesFromDBAPI peakAmps2DB = hazCurve.getPeakAmpsAccessor();
  
  //current selection of site, srcId and rupId from the cyberShake database
  private CybershakeSite selectedSite;
  private CybershakeERF selectedERF;
  private int selectedSrcId,selectedRupId;
  private String saPeriod;
  
  ArrayList<CybershakeSite> sites;
  ArrayList<String> siteNames;
  
  ArrayList<CybershakeERF> erfs;
  ArrayList<String> erfNames;


  public CyberShakePlotFromDBControlPanel(CyberShakePlotControlPanelAPI app) {
	  this.setTitle("Plot CyberShake Data");
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
//    hazCurve.addProgressListener(this);
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    guiPanel.setLayout(gridBagLayout1);
    submitButton.setText("Plot Curve");
    paramSettingButton.setText("Set ERF Params in App for Comparison");
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
    sites = this.csSites.getAllSitesFromDB();
    siteNames = new ArrayList<String>();
    for (CybershakeSite site : sites) {
    	siteNames.add(site.id + ". " + site.name + " (" + site.short_name + ")");
    }
  

    ArrayList supportedCurvesType = new ArrayList();
    supportedCurvesType.add(PROB_CURVE);
    supportedCurvesType.add(DETER_CURVE);

    curveTypeSelectorParam = new StringParameter(this.DETER_PROB_SELECTOR_PARAM,
                                                 supportedCurvesType,
                                                 (String)
                                                 supportedCurvesType.get(0));

    paramList = new ParameterList();
    
    // erf param
    erfs = erf2db.getAllERFs();
    erfNames = new ArrayList<String>();
    for (CybershakeERF erf : erfs) {
    	erfNames.add(erf.id + ": " + erf.description);
    }
    erfParam = new StringParameter(ERF_SELECTOR_PARAM, erfNames, erfNames.get(0));
    erfParam.addParameterChangeListener(this);
    selectedERF = erfs.get(0);
    
    // sgt variations
    initSGTVarIDsParam();
    initRupVarScenarioIDsParam();
    
    
    //rupVarScenarioIDs

    siteSelectionParam = new StringParameter(SITE_SELECTOR_PARAM,
    		siteNames,siteNames.get(0));
    selectedSite = sites.get(siteNames.indexOf((String)siteSelectionParam.getValue()));
    loadSA_PeriodParam();
    this.saPeriod = (String)saPeriodParam.getValue();
    initSrcIndexParam();
    initRupIndexParam();
    paramList.addParameter(curveTypeSelectorParam);
    paramList.addParameter(siteSelectionParam);
    paramList.addParameter(erfParam);
    paramList.addParameter(sgtVarParam);
    paramList.addParameter(rupVarScenarioParam);
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
  private void loadSA_PeriodParam(){
    ArrayList<String> saPeriods = new ArrayList<String>();
    
//    saPeriods = hazCurve.getSupportedSA_PeriodStrings(selectedSite.id, this.selectedERF.id, selectedSGTVariation, selectedRupVarScenario);
    
    // TEMPORARY HACK UNTIL PERIODS GET CHANGED ON NEW SERVER
    if (this.selectedERF.id == 34) {
    	saPeriods.add("SA_Period_2.0");
    	saPeriods.add("SA_Period_3.00003");
    	saPeriods.add("SA_Period_5.0");
    	saPeriods.add("SA_Period_10.0");

    } else {
    	saPeriods = hazCurve.getSupportedSA_PeriodStrings();
    }
    
//    if (saPeriods.size() == 0) {
//    	System.out.println("No periods for these settings!");
//    	saPeriods.add(NONE_AVAILABLE_STRING);
//    }
    saPeriod = saPeriods.get(0);
    saPeriodParam = new StringParameter(this.SA_PERIOD_SELECTOR_PARAM,
        saPeriods,saPeriod);
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
  
  private void initSGTVarIDsParam() {
	  ArrayList<Integer> ids = peakAmps2DB.getSGTVarIDs();
	  ArrayList<String> vals = new ArrayList<String>();
	  for (int val : ids) {
		  vals.add(val + "");
	  }
	  sgtVarParam = new StringParameter(SGT_VAR_PARAM, vals, vals.get(0));
	  sgtVarParam.addParameterChangeListener(this);
  }
  
  private void initRupVarScenarioIDsParam() {
	  ArrayList<Integer> ids = peakAmps2DB.getRupVarScenarioIDs();
	  ArrayList<String> vals = new ArrayList<String>();
	  for (int val : ids) {
		  vals.add(val + "");
	  }
	  rupVarScenarioParam = new StringParameter(RUP_VAR_SCENARIO_PARAM, vals, vals.get(0));
	  rupVarScenarioParam.addParameterChangeListener(this);
  }

  /**
   * Creates the Src Id selection parameter displaying all the src ids for a given Cybershake
   * site for which deterministic calculations can be done.
   */
  private void initSrcIndexParam(){
	  System.out.println("Updating SRC Indices with ERF ID="+selectedERF.id);
    ArrayList srcIdList = this.csSites.getSrcIDsForSite(selectedSite.short_name, selectedERF.id);
    selectedSrcId = ((Integer)srcIdList.get(0));
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
	  System.out.println("Updating Rup Indices with ERF ID="+selectedERF.id);
	 ArrayList rupIdList = this.csSites.getRupIDsForSite(selectedSite.short_name, selectedERF.id, selectedSrcId);
	 int size = rupIdList.size();
	 for(int i=0;i<size;++i)
	     rupIdList.set(i, ""+rupIdList.get(i));
	 rupIndexParam = new StringParameter(RUP_INDEX_PARAM,rupIdList,(String)rupIdList.get(0));   
	 rupIndexParam.addParameterChangeListener(this);
  }
  
  private void reloadParams() {
	  initSrcIndexParam();
      initRupIndexParam();
      this.loadSA_PeriodParam();
      listEditor.replaceParameterForEditor(SA_PERIOD_SELECTOR_PARAM,saPeriodParam);
      listEditor.replaceParameterForEditor(SRC_INDEX_PARAM,srcIndexParam);
      listEditor.replaceParameterForEditor(RUP_INDEX_PARAM,rupIndexParam);
  }

  /**
   * Updates the list editor when user changes the Cybershake site
   * @param e ParameterChangeEvent
   */
  public void parameterChange (ParameterChangeEvent e){
    String paramName = e.getParameterName();
    if(paramName.equals(SITE_SELECTOR_PARAM)){
    	selectedSite = sites.get(siteNames.indexOf((String)siteSelectionParam.getValue()));
//      selectedSite = (String)siteSelectionParam.getValue();
      //initSA_PeriodParam();
      this.reloadParams();
    } else if (paramName.equals(ERF_SELECTOR_PARAM)) {
    	selectedERF = erfs.get(erfNames.indexOf((String)erfParam.getValue()));
    	this.reloadParams();
    } else if (paramName.equals(SGT_VAR_PARAM)) {
    	selectedSGTVariation = Integer.parseInt((String)sgtVarParam.getValue());
    	this.reloadParams();
    } else if (paramName.equals(RUP_VAR_SCENARIO_PARAM)) {
    	selectedRupVarScenario = Integer.parseInt((String)rupVarScenarioParam.getValue());
    	this.reloadParams();
    }
    else if(paramName.equals(SRC_INDEX_PARAM)){
    	String srcId = (String)this.srcIndexParam.getValue();
        selectedSrcId = Integer.parseInt(srcId);
        initRupIndexParam();
        listEditor.replaceParameterForEditor(RUP_INDEX_PARAM,rupIndexParam);
    }
    else if(paramName.equals(RUP_INDEX_PARAM))
    	selectedRupId = Integer.parseInt((String)this.rupIndexParam.getValue());
    else if(paramName.equals(SA_PERIOD_SELECTOR_PARAM)){
    	saPeriod = (String)saPeriodParam.getValue();
    	System.out.println("SA Period = "+saPeriod);
    }
    else if(paramName.equals(DETER_PROB_SELECTOR_PARAM)) {
    	initSrcIndexParam();
        initRupIndexParam();
        listEditor.replaceParameterForEditor(SRC_INDEX_PARAM,srcIndexParam);
        listEditor.replaceParameterForEditor(RUP_INDEX_PARAM,rupIndexParam);
        this.makeParamVisible();
    }

    listEditor.refreshParamEditor();
  }


  /**
   * Gets the hazard data from the Cybershake site for the given SA period.
   * @return ArrayList Hazard Data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getHazardData(ArrayList imlVals) {
	  System.out.println("Computing a hazard curve for " + selectedSite);
    DiscretizedFuncAPI cyberShakeHazardData= hazCurve.computeHazardCurve(imlVals,selectedSite.short_name,
    		this.selectedERF.id, selectedSGTVariation, selectedRupVarScenario, saPeriod);
 
    return cyberShakeHazardData;
  }


  /**
   * 
   * @return ArbitrarilyDiscretizedFunc Determinitic curve data
   * @throws RuntimeException
   */
  private DiscretizedFuncAPI getDeterministicData(ArrayList imlVals) throws
      RuntimeException {
    DiscretizedFuncAPI cyberShakeDeterminicticHazardCurve = hazCurve.computeDeterministicCurve(imlVals, selectedSite.short_name,
    		this.selectedERF.id, selectedSGTVariation, selectedRupVarScenario,
    		                                      selectedSrcId, selectedRupId, saPeriod);

    return cyberShakeDeterminicticHazardCurve;
  }


  /**
   * Sets the parameters in the OpenSHA application similar to what
   * is required  by the Cybershake.
   * @param actionEvent ActionEvent
   */
  private void paramSettingButton_actionPerformed(ActionEvent actionEvent) {
    setSiteParams();
    setIMR_Params();
    boolean imtSet = setIMT_Params();

    if(!imtSet)
      return;
    application.setX_ValuesForHazardCurve(createUSGS_PGA_Function());

    if(isDeterministic)
      setEqkSrcRupSelectorParams();
    else
      setEqkRupForecastParams();
    
    setPlotParams();
  }

  /**
   * Retreives the Cybershake data and plots it in the application.
   * @param actionEvent ActionEvent
   */
  private void submitButton_actionPerformed(ActionEvent actionEvent) {
	  ArrayList imlVals = application.getIML_Values();
	  DiscretizedFuncAPI curveData = null;

	  String infoString = "Site: "+ sites.get(siteNames.indexOf((String)siteSelectionParam.getValue())) + ";\n";
	  infoString += "ERF: " + this.selectedERF + ";\n";
	  infoString += "SGT Variation ID: " + this.selectedSGTVariation + "; Rup Var Scenario ID: " + this.selectedRupVarScenario + ";\n";
	  infoString += "SA Period: " + (String)saPeriodParam.getValue() + ";\n";

	  if(isDeterministic){
		  curveData = getDeterministicData(imlVals);
		  String name = "Cybershake deterministic curve";
		  infoString += "SourceIndex = "+selectedSrcId+
		  "; RuptureIndex = "+selectedRupId;
		  curveData.setName(name);
		  curveData.setInfo(infoString);
		  application.addCybershakeCurveData(curveData);
	  }
	  else{
//		  if (calcProgress == null)
//			  calcProgress = new CalcProgressBar("CyberShake Calculation Progress", "Source");
//		  calcProgress.setVisible(true);
//		  calcProgress.displayProgressBar();
		  curveData = this.getHazardData(imlVals);
//		  calcProgress.setVisible(false);
		  if (curveData == null) {
			  JOptionPane.showMessageDialog(this,
              "There are no Peak Amplitudes in the database for the selected paremters.\n ");
			  return;
		  }
		  String name = "Cybershake hazard curve";
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
    String cyberShakeSite = sites.get(siteNames.indexOf((String)siteSelectionParam.getValue())).name;
    Location loc = csSites.getCyberShakeSiteLocation(cyberShakeSite);
    site.getParameterListEditor().getParameterEditor(site.LATITUDE).setValue(new Double(loc.getLatitude()));
    site.getParameterListEditor().getParameterEditor(site.LONGITUDE).setValue(new Double(loc.getLongitude()));
    site.getParameterListEditor().refreshParamEditor();
    application.getCVMControl().setSelectedIMRButton_actionPerformed(null);
  }

  /**
   * Set the Eqk Rup Forecast in the OpenSHA application similar to eqk forecast
   * params used to do the cybershake calculations.
   */
  private void setEqkRupForecastParams(){
    ERF_GuiBean gui = application.getEqkRupForecastGuiBeanInstance();
    ParameterListEditor editorList = gui.getERFParameterListEditor();
    gui.getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(selectedERF.name);
    gui.getERFParameterListEditor().refreshParamEditor();
    gui.getParameter(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_EXCLUDE);
    gui.getParameter(MeanUCERF2.RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));
    gui.getParameter(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME).setValue(new Boolean(true));
    gui.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);

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
        setValue(ERF_NAME);
    ERF_GuiBean erfGuiBean = rupGuiBean.getERF_ParamEditor();
    ParameterListEditor editorList = erfGuiBean.getERFParameterListEditor();
    editorList.getParameterEditor(erfGuiBean.ERF_PARAM_NAME).setValue(ERF_NAME);
    editorList.getParameterEditor(UCERF2.BACK_SEIS_NAME).setValue(UCERF2.BACK_SEIS_EXCLUDE);
    editorList.getParameterEditor(MeanUCERF2.RUP_OFFSET_PARAM_NAME).setValue(new Double(5.0));
    editorList.getParameterEditor(MeanUCERF2.CYBERSHAKE_DDW_CORR_PARAM_NAME).setValue(new Boolean(true));
    editorList.getParameterEditor(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);

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
  
  private void setPlotParams() {
	  float period = (float)this.getPeriodDouble();
	  application.setY_Log(true);
	  double xMin = 0.0;
	  double xMax = 2;
	  if (Math.abs(period - 3) < 0.05)
	  	xMax = 2.0;
	  else if (Math.abs(period - 5) < 0.05)
		  	xMax = 1.0;
	  else if (Math.abs(period - 10) < 0.05)
		  	xMax = 0.5;
	  double yMin = Double.parseDouble("1.0E-6");
	  double yMax = 1.0;
	  application.setAxisRange(xMin, xMax, yMin, yMax);
  }
  
  private void setIMR_Params(){
	  IMR_GuiBean imrGui = application.getIMRGuiBeanInstance();
	  
//	  AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED;
	  
	  try {
		StringParameter truncTypeParam = (StringParameter)imrGui.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
		
		truncTypeParam.setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
		
		DoubleParameter truncLevelParam = (DoubleParameter)imrGui.getParameterList().getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME);
		
		truncLevelParam.setValue(3.0);
		
		imrGui.refreshParamEditor();
	} catch (ParameterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
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
  
  private double getPeriodDouble() {
	  String saPeriodString = saPeriod.substring(10);//trimming the "SA Period" string in front of the Period value
	  DecimalFormat format = new DecimalFormat("0.00");
	  return Double.parseDouble(format.format(Double.parseDouble(saPeriodString.trim())));
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

//public void setProgressIndeterminate(boolean indeterminate) {
//	if (calcProgress != null)
//		calcProgress.setProgressIndeterminate(indeterminate);
//}
//
//public void setProgressMessage(String message) {
//	if (calcProgress != null)
//		calcProgress.setProgressMessage(message);
//}
//
//public void setProgress(int currentIndex, int total) {
//	if (calcProgress != null) {
//		System.out.println("Updating progress: " + currentIndex + " " + total);
//		calcProgress.updateProgress(currentIndex, total);
//	}
//}

}
