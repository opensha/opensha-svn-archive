/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;


import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.data.ValueWeight;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.DoubleValueWeightParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.ParameterListParameter;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.A_FaultSegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.data.A_FaultsFetcher;

/**
 * @author vipingupta
 *
 */
public class A_FaultSourceApp extends JFrame implements ParameterChangeListener, ActionListener {
		// choose deformation model
	private final static String DEFORMATION_MODEL_PARAM_NAME = "Deformation Model";	
	// choose segment model
	private final static String SEGMENT_MODELS_PARAM_NAME = "Segment Model";
	private final static String NONE = "None";
	private final static String MSG_FROM_DATABASE = "Retrieving Data from database. Please wait....";
	private JTextArea magAreasTextArea = new JTextArea();
	// choose mag area relationship
	private final static String MAG_AREA_RELS_PARAM_NAME = "Mag-Area Relationship";
	private final static String MAG_AREA_RELS_PARAM_INFO = "Mag-Area Relationship for mean mag of characteristic events";
	// slip model
	private final static String SLIP_MODEL_NAME = "Slip Model";
	private final static String CHAR_SLIP_MODEL = "Char. Slip (Dsr=Ds)";
	private final static String WG02_SLIP_MODEL = "WG02 Slip (Dsr prop. to Vs)";
	private final static String UNIFORM_SLIP_MODEL = "Uniform/Boxcar Slip";
	// rupture model type
	private final static String RUP_MODEL_TYPE = "Rup Model Type";
	// recurrence interval parameter name
	private final static String RECUR_INTV_PARAM_NAME = "Mean Recurrence Interval";
	// slip per event
	private final static String SLIP_PER_EVENT_PARAM_NAME = "Slip Per Event";
	// a priori rup rates
	private final static String APRIORI_RUP_RATES = "A Priori Rupture Rates";
	
	// choose Mag Sigma
	//private final static String MAG_SIGMA_PARAM_NAME = "Mag Sigma";
	//private final static String MAG_SIGMA_PARAM_INFO = "Standard Deviation for characteristic events";
	//private final static Double MAG_SIGMA_DEFAULT = new Double(0.12);
	
	//Mag Truncation Type
	/*private final static String TRUNC_TYPE_PARAM_NAME = "Truncation Type";
	private final static String TRUNC_TYPE_PARAM_INFO = "Truncation of gaussian for characteristic events";
	private final static String NO_TRUNCATION = "No Truncation";
	private final static String ONE_SIDED_TRUNCATION = "Upper Truncation";
	private final static String TWO_SIDED_TRUNCATION = "Upper & Lower Truncation";
	private final static String TRUNC_TYPE_DEFAULT = TWO_SIDED_TRUNCATION;
	// Mag truncation level
	private final static String TRUNC_LEVEL_PARAM_NAME = "Truncation Level";
	private final static Double TRUNC_LEVEL_DEFAULT = new Double(2.0);*/
	
	// aseismic factor interpolated
	
	private final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor reduces section area";
	private final static String ASEIS_INTER_PARAM_INFO = "Otherwise it reduces section slip rate";
	
	// floater MFD _ PDF
	//private final static String MAG_PDF_PARAM_NAME = "Floating Rup Mag PDF";
	
	private DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList deformationModelsList;
	private final static String MSG_NO_DEF_MODEL_EXISTS = "Currently, there is no Deformation Model";
	
	private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private JPanel leftPanel = new JPanel(new GridBagLayout());
	private JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final static String PARAM_EDITOR_TITLE = "Rupture Model Params";
	
	private ParameterList paramList;
	private ParameterListEditor paramListEditor;
	private A_FaultsFetcher aFaultsFetcher;
		
	private JButton calcButton  = new JButton("Calculate");
	private final static int W = 900;
	private final static int H = 700;
	private ArrayList magAreaRelationships;
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat SLIP_FORMAT = new DecimalFormat("0.000");
	private JTabbedPane tabbedPane = new JTabbedPane();
	private SegmentDataTableModel segmentTableModel = new SegmentDataTableModel();
	private FaultSectionTableModel faultSectionTableModel = new FaultSectionTableModel();
	private RuptureTableModel rupTableModel = new RuptureTableModel();
	private final static String MSG_ASEIS_REDUCES_AREA = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of area (as requested) in the table above; this will also influence the segment slip rates for any segments composed of more than one section (because the slip rates are weight-averaged according to section areas)";
	private final static String MSG_ASEIS_REDUCES_SLIPRATE = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of slip rate (as requested); keep this in mind when interpreting the segment slip rates (which for any segments composed of more than one section are a weight average by section areas)";
	private final static String TITLE = " Type A Fault Source App";
	
	/**
	 * Constructor
	 *
	 */
	public A_FaultSourceApp() {
		initParamsAndEditor();
		this.createGUI();
		setTitle(TITLE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(W, H);
		this.show();
	}

	/**
	 * Initaliaze params and editor
	 *
	 */
	private void initParamsAndEditor() {
		try {
			paramList = new ParameterList();
			loadSegmentModels(); // load segment models, segment rates and rup rates from excel file
			loadDeformationModels();
			makeAseisFactorInterpolationParamAndEditor();
			makeMagAreRelationshipParamAndEditor();
			makeSlipModelParamAndEditor();
			makeRupModelParamAndEditor();
			//make  mean recurrence interval param for all segments
			makeMeanRecurrenceIntervalParams(null);
			// ave slip per event for all segments
			makeAveSlipPerEventParams(null);
			// a priori ruptures rates
			makeAPrioriRupRatesParams(null,null);
			//makeMagSigmaTruncParamsAndEditor();
			//makeFloaterPDFParam();
			//makeScenarioWtsParamAndEditor(1);
			paramListEditor = new ParameterListEditor(this.paramList);
			paramListEditor.setTitle(PARAM_EDITOR_TITLE);
			//setTruncLevelVisibility();
			updateSegmentAndRupNames();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load all segment models
	 *
	 */
	private void loadSegmentModels() {
		 aFaultsFetcher = new A_FaultsFetcher();
		 ArrayList segmentModelNames = aFaultsFetcher.getAllFaultNames();
		 segmentModelNames.add(0, NONE);
		 makeSegmentModelParamAndEditor(segmentModelNames);
	}
	
	/**
	 * Make rupture model param
	 *
	 */
	private void makeRupModelParamAndEditor() {
		ArrayList rupModels = new ArrayList();
		rupModels.add(A_FaultsFetcher.MIN_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.MAX_RATE_RUP_MODEL);
		rupModels.add(A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL);
		StringParameter rupModelParam = new StringParameter(RUP_MODEL_TYPE, rupModels, (String)rupModels.get(0));
		rupModelParam.addParameterChangeListener(this);
		paramList.addParameter(rupModelParam);
	}
	
	/**
	 * Slip Models parameter
	 *
	 */
	private void makeSlipModelParamAndEditor() {
		ArrayList slipModels = new ArrayList();
		slipModels.add(CHAR_SLIP_MODEL);
		slipModels.add(WG02_SLIP_MODEL);
		slipModels.add(UNIFORM_SLIP_MODEL);
		StringParameter slipModelParam = new StringParameter(this.SLIP_MODEL_NAME, slipModels, (String)slipModels.get(0));
		paramList.addParameter(slipModelParam);
	}
	
	/**
	 * Create GUI
	 *
	 */
	private void createGUI() {
		getContentPane().setLayout(new GridBagLayout());
		// main split pane
		getContentPane().add(this.mainSplitPane,
	             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		mainSplitPane.add(this.leftPanel, JSplitPane.LEFT);
		mainSplitPane.add(this.tabbedPane, JSplitPane.RIGHT);
		tabbedPane.addTab("Segment Data", rightSplitPane);
		tabbedPane.addTab("Ruptures Info", new JScrollPane(new JTable(this.rupTableModel)));
		leftPanel.add(this.paramListEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		leftPanel.add(this.calcButton, new GridBagConstraints(0, 1, 1, 0, 1.0, 0.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.NONE,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		calcButton.addActionListener(this);
		
		magAreasTextArea.setEditable(false);
		magAreasTextArea.setLineWrap(true);
		magAreasTextArea.setWrapStyleWord(true);
		JTable sectionDataTable = new JTable(faultSectionTableModel);
		JSplitPane sectionDataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sectionDataSplitPane.add(new JScrollPane(sectionDataTable),JSplitPane.BOTTOM);
		sectionDataSplitPane.add(new JScrollPane(this.magAreasTextArea),JSplitPane.TOP);
		JTable segmentTable = new JTable(this.segmentTableModel);
		rightSplitPane.add(new JScrollPane(segmentTable), JSplitPane.TOP);
		rightSplitPane.add(sectionDataSplitPane, JSplitPane.BOTTOM);
		rightSplitPane.setDividerLocation(150);
		sectionDataSplitPane.setDividerLocation(200);
		mainSplitPane.setDividerLocation(300);
	}
	
	/**
	 * This function is called when user clicks on "calculate" button
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if(source == this.calcButton) { // if calculate button is clicked
			calculate();
		}
	}
	
	/**
	 * Calculate the segment and rupture data
	 *
	 */
	private void calculate() {
		MagAreaRelationship magAreaRel = getMagAreaRelationship();
		//double magSigma = getMagSigma();
		//double magTruncLevel = getMagTruncLevel();
		//int truncType = getTruncType();
		boolean isAseisReducesArea = getAseisReducesArea();
		//IncrementalMagFreqDist floatingRup_PDF = getFloatingRup_PDF();
		
	}
	
	/**
	 * Get metadata 
	 * @return
	 */
	private String getMetadata() {
		String metadata = "";
		metadata+=this.paramList.getParameter(SEGMENT_MODELS_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(DEFORMATION_MODEL_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(MAG_AREA_RELS_PARAM_NAME).getMetadataString()+"\n";
		//metadata+=this.paramList.getParameter(MAG_SIGMA_PARAM_NAME).getMetadataString()+"\n";
		//metadata+=this.paramList.getParameter(TRUNC_TYPE_PARAM_NAME).getMetadataString()+"\n";
		//metadata+=this.paramList.getParameter(TRUNC_LEVEL_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(ASEIS_INTER_PARAM_NAME).getMetadataString()+"\n";
		//metadata+=SCENARIO_WT_PARAM_NAME+":\n";
		/*ParameterList wtParams = ((ParameterListParameter)paramList.getParameter(SCENARIO_WT_PARAM_NAME)).getParameter();
		Iterator it = wtParams.getParametersIterator();
		int i=1;
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			if(param.getName().equalsIgnoreCase(this.FLOATER_RUP_WT_NAME))
				metadata+="   "+param.getMetadataString()+"\n";
			else metadata+="   "+(i++)+" = "+(float)((Double)param.getValue()).doubleValue()+"\n";
		}
		metadata+=this.paramList.getParameter(MAG_PDF_PARAM_NAME).getMetadataString()+"\n";*/
		return metadata;
	}
	
	/**
	 * Get the segment data
	 * @return
	 */
	private FaultSegmentData getSegmentData() {
		// show the progress bar
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new GridBagLayout());
		JProgressBar progressBar = new JProgressBar();
		frame.getContentPane().add(progressBar,new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0
        	      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 0, 0, 0, 0 ), 0, 0 ));
		frame.setLocationRelativeTo(this);
		frame.setSize(450, 50);
		frame.show();
		progressBar.setStringPainted(true);
		progressBar.setVisible(true);
		progressBar.setString(MSG_FROM_DATABASE);
		progressBar.paintImmediately(progressBar.getBounds());
		
		String selectedSegmentModel = (String)this.paramList.getValue(SEGMENT_MODELS_PARAM_NAME);
		int selectdDeformationModelId = getSelectedDeformationModelId();
		
		FaultSegmentData segFaultData = this.aFaultsFetcher.getFaultSegmentData(selectedSegmentModel, selectdDeformationModelId, this.getAseisReducesArea());
		
		this.faultSectionTableModel.setFaultSectionData(segFaultData.getPrefFaultSectionDataList());
		faultSectionTableModel.fireTableDataChanged();
		frame.dispose();
		return segFaultData;
	}
	
	private int getSelectedDeformationModelId() {
		String selectedDefModel  = (String)this.paramList.getValue(DEFORMATION_MODEL_PARAM_NAME);
		for(int i=0; i<this.deformationModelsList.size(); ++i) {
			DeformationModelSummary deformationModel = (DeformationModelSummary)deformationModelsList.get(i);
			if(deformationModel.getDeformationModelName().equalsIgnoreCase(selectedDefModel)) {
				return deformationModel.getDeformationModelId();
			}
		}
		return -1;
	}
	
	/**
	 * Get the PDF for floating rup
	 * @return
	 */
	/*private IncrementalMagFreqDist getFloatingRup_PDF() {
		MagFreqDistParameter param = (MagFreqDistParameter)paramList.getParameter(MAG_PDF_PARAM_NAME);
		param.setMagDist();
		return (IncrementalMagFreqDist)param.getValue();
	}*/
	
	/**
	 * Whether Aseis reduces area
	 * @return
	 */
	private boolean getAseisReducesArea() {
		return ((Boolean)paramList.getValue(ASEIS_INTER_PARAM_NAME)).booleanValue();
	}
	
	
	/**
	 * Get the selected Mag Area relationship
	 * @return
	 */
	private MagAreaRelationship getMagAreaRelationship() {
		String magAreaRelName = (String)this.paramList.getValue(MAG_AREA_RELS_PARAM_NAME);
		// iterate over all Mag Area relationships to find the selected one
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			if(magAreaRel.getName().equalsIgnoreCase(magAreaRelName))
				return magAreaRel;
		}
		
		return null;
	}
	
	/**
	 * Get the sigma
	 *
	 */
	/*private double getMagSigma() {
		return ((Double)this.paramList.getValue(MAG_SIGMA_PARAM_NAME)).doubleValue();
	}*/
	
	
	/**
	 * Mag trunc level
	 * 
	 * @return
	 */
	/*private double getMagTruncLevel() {
		return ((Double)this.paramList.getValue(TRUNC_LEVEL_PARAM_NAME)).doubleValue();
	}*/
	
	/**
	 * Get the truncation type
	 * 
	 * @return
	 */
	/*private int getTruncType() {
		String truncType = (String)paramList.getValue(TRUNC_TYPE_PARAM_NAME);
		if(truncType.equalsIgnoreCase(NO_TRUNCATION)) return 0;
		if(truncType.equalsIgnoreCase(ONE_SIDED_TRUNCATION)) return 1;
		if(truncType.equalsIgnoreCase(TWO_SIDED_TRUNCATION)) return 2;
		throw new RuntimeException("Unsupported truncation type");
	}*/
	
	/**
	 * Make Scenario Weights Param and Editor
	 *
	 */
	/*private void makeScenarioWtsParamAndEditor(int numScenarios) {
		ParameterList scenarioWeightsParamList = new ParameterList();
		Double defaultVal = new Double(1.0/(numScenarios+1));
		for(int i=0; i<numScenarios; ++i) {
			DoubleParameter wtParam = new DoubleParameter(SCECNARIO_PREFIX+" "+(i+1), 
					MIN_SCEN_WEIGHT, MAX_SCEN_WEIGHT, defaultVal );
			if(scenarioNames!=null)
				wtParam.setInfo(this.scenarioNames[i]);
			scenarioWeightsParamList.addParameter(wtParam);
		}
		// flaoter Rup wt
		DoubleParameter wtParam = new DoubleParameter(FLOATER_RUP_WT_NAME, 
				MIN_SCEN_WEIGHT, MAX_SCEN_WEIGHT, defaultVal );
		scenarioWeightsParamList.addParameter(wtParam);
		scenarioWtsParam = new TreeBranchWeightsParameter(SCENARIO_WT_PARAM_NAME, scenarioWeightsParamList);
		if(this.paramList.containsParameter(SCENARIO_WT_PARAM_NAME)) {
			paramList.replaceParameter(SCENARIO_WT_PARAM_NAME, scenarioWtsParam);
			this.paramListEditor.replaceParameterForEditor(SCENARIO_WT_PARAM_NAME, scenarioWtsParam);
		}
		else paramList.addParameter(scenarioWtsParam);
	}*/
	
	
	/**
	 * Floater Mag Freq PDF
	 *
	 */
	/*private void makeFloaterPDFParam() {
		ArrayList allowedMagDists = new ArrayList();
		allowedMagDists.add(SingleMagFreqDist.NAME);
		allowedMagDists.add(GutenbergRichterMagFreqDist.NAME);
		allowedMagDists.add(GaussianMagFreqDist.NAME);
		allowedMagDists.add(YC_1985_CharMagFreqDist.NAME);
		allowedMagDists.add(SummedMagFreqDist.NAME);
		allowedMagDists.add(ArbIncrementalMagFreqDist.NAME);
		MagFreqDistParameter magPDF_Parameter = new MagFreqDistParameter(MAG_PDF_PARAM_NAME, allowedMagDists);
		paramList.addParameter(magPDF_Parameter);*/
		/*MagPDF_ParameterEditor magPDF_ParameterEditor = new MagPDF_ParameterEditor(magPDF_Parameter);
		add(magPDF_ParameterEditor,
	             new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	//}
	
	/**
	 * Whether Aseis Factor will be interpolated
	 *
	 */
	private void makeAseisFactorInterpolationParamAndEditor() {
		BooleanParameter aseisFactorInterParam = new BooleanParameter(ASEIS_INTER_PARAM_NAME, new Boolean(true));
		aseisFactorInterParam.setInfo(ASEIS_INTER_PARAM_INFO);
		paramList.addParameter(aseisFactorInterParam);
		aseisFactorInterParam.addParameterChangeListener(this);
		/*BooleanParameterEditor aseisFactorInterParamEditor= new BooleanParameterEditor(aseisFactorInterParam);
		add(aseisFactorInterParamEditor,
	             new GridBagConstraints(0, 9, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	/**
	 * Make Mag Sigma, Truncation parameters
	 * @throws Exception
	 */
	/*private void makeMagSigmaTruncParamsAndEditor() throws Exception {
		// choose Mag Sigma
		 DoubleParameter magSigmaParam = new DoubleParameter(MAG_SIGMA_PARAM_NAME, MAG_SIGMA_DEFAULT);
		 magSigmaParam.setInfo(MAG_SIGMA_PARAM_INFO);
		 paramList.addParameter(magSigmaParam);
	
		//Mag Truncation Type	
		ArrayList truncTypes = new ArrayList();
		truncTypes.add(NO_TRUNCATION);
		truncTypes.add(ONE_SIDED_TRUNCATION);
		truncTypes.add(TWO_SIDED_TRUNCATION);
		StringParameter truncTypesParam = new StringParameter(TRUNC_TYPE_PARAM_NAME, truncTypes, TRUNC_TYPE_DEFAULT);
		truncTypesParam.addParameterChangeListener(this);
		truncTypesParam.setInfo(TRUNC_TYPE_PARAM_INFO);
		paramList.addParameter(truncTypesParam);
		// Mag truncation level
		DoubleParameter truncLevelParam = new DoubleParameter(TRUNC_LEVEL_PARAM_NAME, TRUNC_LEVEL_DEFAULT);
		paramList.addParameter(truncLevelParam);
	}*/
	
	
	/**
	 * Load the deformation models from the database
	 *
	 */
	private void loadDeformationModels() {
		deformationModelsList = this.deformationModelSummaryDB_DAO.getAllDeformationModels();
		// make a list of deformation model names
		ArrayList deformationModelNames = new ArrayList();
		for(int i=0; i<deformationModelsList.size(); ++i) {
			deformationModelNames.add(((DeformationModelSummary)deformationModelsList.get(i)).getDeformationModelName());
		}
		
		// make parameter and editor
		if(deformationModelNames==null || deformationModelNames.size()==0)  {
			JOptionPane.showMessageDialog(this, MSG_NO_DEF_MODEL_EXISTS);
			return;
		}
		
		StringParameter deformationModelsParam = new StringParameter(DEFORMATION_MODEL_PARAM_NAME,deformationModelNames, (String)deformationModelNames.get(0) );
		deformationModelsParam.addParameterChangeListener(this);
		paramList.addParameter(deformationModelsParam);
		/*ConstrainedStringParameterEditor deformationModelsParamEditor = new ConstrainedStringParameterEditor(deformationModelsParam);
		// deformation model selection editor
		leftPanel.add(deformationModelsParamEditor,
	             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	
	
	
	
	private void makeMagAreRelationshipParamAndEditor() {
		// make objects if Mag Area Relationships
		magAreaRelationships = new ArrayList();
		magAreaRelationships.add(new Ellsworth_A_WG02_MagAreaRel() );
		magAreaRelationships.add(new Ellsworth_B_WG02_MagAreaRel());
		magAreaRelationships.add(new HanksBakun2002_MagAreaRel());
		magAreaRelationships.add(new Somerville_2006_MagAreaRel());
		magAreaRelationships.add(new WC1994_MagAreaRelationship());
		
		// array List of Mag Area Rel names
		ArrayList magAreaNamesList = new ArrayList();
		for(int i=0; i<magAreaRelationships.size(); ++i)
			magAreaNamesList.add(((MagAreaRelationship)magAreaRelationships.get(i)).getName());
		
		StringParameter magAreaRelParam = new StringParameter(MAG_AREA_RELS_PARAM_NAME, magAreaNamesList, (String)magAreaNamesList.get(0));
		magAreaRelParam.setInfo(MAG_AREA_RELS_PARAM_INFO);
		paramList.addParameter(magAreaRelParam);
		/*ConstrainedStringParameterEditor magAreaRelParamEditor = new ConstrainedStringParameterEditor(magAreaRelParam);
		// mag area relationship selection editor
		leftPanel.add(magAreaRelParamEditor,
	             new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	/**
	 * Make Segment Model Param and editor
	 *
	 */
	private void makeSegmentModelParamAndEditor(ArrayList segmentModelNames) {
		StringParameter segmentModelParam = new StringParameter(SEGMENT_MODELS_PARAM_NAME, segmentModelNames, (String)segmentModelNames.get(0) );
		segmentModelParam.addParameterChangeListener(this);
		paramList.addParameter(segmentModelParam);
		/*ConstrainedStringParameterEditor segmentModelParamEditor = new ConstrainedStringParameterEditor(segmentModelParam);
		// segment model selection editor
		leftPanel.add(segmentModelParamEditor,
	             new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		rightPanel.add(new JScrollPane(this.segmentNames), new GridBagConstraints(0, 0, 1, 3, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	
	
	
	
	/**
	 * This function is called on change of a parameter
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		/*if(paramName.equalsIgnoreCase(TRUNC_TYPE_PARAM_NAME))
			setTruncLevelVisibility();
		else*/ if(paramName.equalsIgnoreCase(SEGMENT_MODELS_PARAM_NAME))
			updateSegmentAndRupNames();
		else if(paramName.equalsIgnoreCase(DEFORMATION_MODEL_PARAM_NAME))
			updateSegmentAndRupNames();
		else if(paramName.equalsIgnoreCase(ASEIS_INTER_PARAM_NAME))
			updateSegmentAndRupNames();
		else if(paramName.equalsIgnoreCase(RUP_MODEL_TYPE))
			updateSegmentAndRupNames();
	}
	

	
	/**
	 * Update the segment names and scenario names wheneever a new segment model is chosen
	 *
	 */
	private void updateSegmentAndRupNames() {
		String selectedSegmentModel = (String)this.paramList.getValue(SEGMENT_MODELS_PARAM_NAME);
		if(selectedSegmentModel.equalsIgnoreCase(NONE)) {
			this.faultSectionTableModel.setFaultSectionData(null);
			faultSectionTableModel.fireTableDataChanged();
			this.segmentTableModel.setSegmentedFaultData(null);
			segmentTableModel.fireTableDataChanged();
			magAreasTextArea.setText("");
			this.calcButton.setEnabled(false); // if no Segment model is chosen, disable the calc button
			return;
		}
		this.calcButton.setEnabled(true); // if a Segment model is chosen, enable the calc button

		FaultSegmentData segmetedFaultData = this.getSegmentData();
		this.segmentTableModel.setSegmentedFaultData(segmetedFaultData);
		segmentTableModel.fireTableDataChanged();
		setMagAndSlipsString(segmetedFaultData);
			// make  mean recurrence interval param for all segments
		makeMeanRecurrenceIntervalParams(segmetedFaultData.getSegmentNames());
		// ave slip per event for all segments
		makeAveSlipPerEventParams(segmetedFaultData.getSegmentNames());
		// a priori rup rates
		// get the rupture names
		String longRupNames[] = A_FaultSegmentedSource.getAllLongRuptureNames(segmetedFaultData);
		String shortRupNames[] = A_FaultSegmentedSource.getAllShortRuptureNames(segmetedFaultData);
		makeAPrioriRupRatesParams(shortRupNames, longRupNames);
		ValueWeight[] aprioriRupRates = getAprioriRupRates(selectedSegmentModel);
		A_FaultSegmentedSource segmentedFaultSource = new A_FaultSegmentedSource(segmetedFaultData, this.getMagAreaRelationship(),
				A_FaultSegmentedSource.CHAR_SLIP_MODEL, aprioriRupRates, 0.12, 2, 0.0, 0.0);
		this.rupTableModel.setFaultSegmentedSource(segmentedFaultSource);
		rupTableModel.fireTableDataChanged();
	}
	
	
	/**
	 * Rup model type
	 * @return
	 */
	private int getRupModelType() {
		String selectedRupModel = (String)this.paramList.getValue(RUP_MODEL_TYPE);
		if(selectedRupModel.equalsIgnoreCase(A_FaultsFetcher.GEOL_INSIGHT_RUP_MODEL)) return 3;
		else if(selectedRupModel.equalsIgnoreCase(A_FaultsFetcher.MIN_RATE_RUP_MODEL)) return 0;
		else if (selectedRupModel.equalsIgnoreCase(A_FaultsFetcher.MAX_RATE_RUP_MODEL)) return 1;
		return 2;
	}
	
	
	/**
	 * Get apriori rupture rates
	 * @param selectedSegmentModel
	 * @return
	 */
	private ValueWeight[] getAprioriRupRates(String selectedSegmentModel) {
		String selectedRupModel = (String)this.paramList.getValue(RUP_MODEL_TYPE);
		return this.aFaultsFetcher.getAprioriRupRates(selectedSegmentModel, selectedRupModel);
	}
	

	
	private void setMagAndSlipsString(FaultSegmentData segmetedFaultData ) {
		int numSegs = segmetedFaultData.getNumSegments();
		String summaryString = "MAGS & AVE SLIPS IMPLIED BY M(A) RELATIONS\n"+
								"------------------------------------------\n\n";
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			summaryString+="Segment  Mag       Ave-slip (m) for  ("+magAreaRel.getName()+")\n";
			for(int j=0; j<numSegs; ++j) {
				double mag = magAreaRel.getMedianMag(segmetedFaultData.getSegmentArea(j)/1e6);
				double moment = MomentMagCalc.getMoment(mag);
				summaryString+=(j+1)+"              "+MAG_FORMAT.format(mag)+"      "+SLIP_FORMAT.format(FaultMomentCalc.getSlip(segmetedFaultData.getSegmentArea(j), moment))+"\n";
			}
			double mag = magAreaRel.getMedianMag(segmetedFaultData.getTotalArea()/1e6);
			double moment = MomentMagCalc.getMoment(mag);
			summaryString+="All            "+MAG_FORMAT.format(mag)+"      "+SLIP_FORMAT.format(FaultMomentCalc.getSlip(segmetedFaultData.getTotalArea(), moment))+"\n\n";		
		}
		String text = MSG_ASEIS_REDUCES_SLIPRATE;
		if(this.getAseisReducesArea()) text = MSG_ASEIS_REDUCES_AREA;
		magAreasTextArea.setText(text+"\n\n"+summaryString);
		magAreasTextArea.setCaretPosition(0);
	}
	
	
	
	/**
	 * A priori rup rates
	 * 
	 * @param rupNames
	 */
	private void makeAPrioriRupRatesParams(String [] shortRupNames, String[] longRupNames) {
		ParameterList aaPrioriRupRatesParamList = new ParameterList();
		for(int i=0;  shortRupNames!=null && i<shortRupNames.length; ++i) {
			DoubleValueWeightParameter aPrioriRupRateParam = new DoubleValueWeightParameter("Rup "+(i+1)+": "+shortRupNames[i]);
			aPrioriRupRateParam.setInfo(longRupNames[i]);
			//slipPerEventParam.getConstraint().setNullAllowed(true);
			aaPrioriRupRatesParamList.addParameter(aPrioriRupRateParam);
		}
		ParameterListParameter aPrioriRupRateParamListParameter = new ParameterListParameter(APRIORI_RUP_RATES, aaPrioriRupRatesParamList);

		if(this.paramList.containsParameter(APRIORI_RUP_RATES)) {
			paramList.replaceParameter(APRIORI_RUP_RATES, aPrioriRupRateParamListParameter);
			this.paramListEditor.replaceParameterForEditor(APRIORI_RUP_RATES, aPrioriRupRateParamListParameter);
		}
		else paramList.addParameter(aPrioriRupRateParamListParameter);
	}
	
	/**
	 * Avg slip per event for each segment
	 * @param segmentNames
	 */
	private void makeAveSlipPerEventParams(String[] segmentNames) {
		
		ParameterList slipPerEventParamList = new ParameterList();
		for(int i=0;  segmentNames!=null && i<segmentNames.length; ++i) {
			DoubleParameter slipPerEventParam = new DoubleParameter(segmentNames[i]);
			//slipPerEventParam.getConstraint().setNullAllowed(true);
			slipPerEventParamList.addParameter(slipPerEventParam);
		}
		ParameterListParameter slipPerEventParamListParameter = new ParameterListParameter(SLIP_PER_EVENT_PARAM_NAME, slipPerEventParamList);

		if(this.paramList.containsParameter(SLIP_PER_EVENT_PARAM_NAME)) {
			paramList.replaceParameter(SLIP_PER_EVENT_PARAM_NAME, slipPerEventParamListParameter);
			this.paramListEditor.replaceParameterForEditor(SLIP_PER_EVENT_PARAM_NAME, slipPerEventParamListParameter);
		}
		else paramList.addParameter(slipPerEventParamListParameter);
	}
	
	
	
	
	/**
	 * Mean recurrence interval for each segment
	 * @param segmentNames
	 */
	private void makeMeanRecurrenceIntervalParams(String[] segmentNames) {
		
		ParameterList recurIntvParamList = new ParameterList();
		for(int i=0; segmentNames!=null && i<segmentNames.length; ++i) {
			DoubleParameter recIntvParam = new DoubleParameter(segmentNames[i]);
			//recIntvParam.getConstraint().setNullAllowed(true);
			recurIntvParamList.addParameter(recIntvParam);
		}
		ParameterListParameter recurIntvParamListParameter = new ParameterListParameter(RECUR_INTV_PARAM_NAME, recurIntvParamList);
		
		if(this.paramList.containsParameter(RECUR_INTV_PARAM_NAME)) {
			paramList.replaceParameter(RECUR_INTV_PARAM_NAME, recurIntvParamListParameter);
			this.paramListEditor.replaceParameterForEditor(RECUR_INTV_PARAM_NAME, recurIntvParamListParameter);
		}
		else paramList.addParameter(recurIntvParamListParameter);
	}
	
	
	/**
	 * Make the truncation level visible only if Truncation Type is not None
	 *
	 */
	/*private void setTruncLevelVisibility() {
		String truncType = (String)this.paramList.getValue(TRUNC_TYPE_PARAM_NAME);
		if(truncType.equalsIgnoreCase(NO_TRUNCATION))
			this.paramListEditor.getParameterEditor(TRUNC_LEVEL_PARAM_NAME).setVisible(false);
		else paramListEditor.getParameterEditor(TRUNC_LEVEL_PARAM_NAME).setVisible(true);
	}*/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		A_FaultSourceApp faultSourceApp = new A_FaultSourceApp();

	}
}

/**
 * Fault Section Table Model
 * 
 * @author vipingupta
 *
 */
class FaultSectionTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Section Name", "Slip Rate (mm/yr)", 
		"Aseismic Factor","Length (km)","Down Dip Width (km)", "Area (sq-km)",
		"Upper Depth (km)", "Lower Depth (km)", "Ave Dip (degrees)"};
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private ArrayList faultSectionsPrefDataList = new ArrayList();
	
	/**
	 * default constructor
	 *
	 */
	public FaultSectionTableModel() {
		this(null);
	}
	
	/**
	 *  Preferred Fault section data
	 *  
	 * @param faultSectionsPrefDataList  ArrayList of PrefFaultSedctionData
	 */
	public FaultSectionTableModel(ArrayList faultSectionsPrefDataList) {
		setFaultSectionData(faultSectionsPrefDataList);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setFaultSectionData(ArrayList faultSectionsPrefDataList) {
		this.faultSectionsPrefDataList =   faultSectionsPrefDataList;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(faultSectionsPrefDataList==null) return 0;
		return (faultSectionsPrefDataList.size()); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(faultSectionsPrefDataList==null) return "";
		FaultSectionPrefData faultSectionPrefData = (FaultSectionPrefData) faultSectionsPrefDataList.get(rowIndex);
		
		//"Name", "Slip Rate (cm/yr)", 
			//"Aseismic Factor","Length (km)","Down Dip Width (km)", "Area (sq-km)",
			//"Upper Depth (km)", "Lower Depth (km)", "Ave Dip (degrees)"};
		
		switch(columnIndex) {
			case 0:
				return faultSectionPrefData.getSectionName();
			case 1: // convert to mm/yr
				return SLIP_RATE_FORMAT.format(faultSectionPrefData.getAveLongTermSlipRate());
			case 2:
				return ""+faultSectionPrefData.getAseismicSlipFactor();
			case 3:
				// km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getLength());
			case 4:
				// convert to km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getDownDipWidth());
			case 5:
				// sq km
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getDownDipWidth() *
						faultSectionPrefData.getLength());
			case 6:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveUpperDepth());
			case 7:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveLowerDepth());
			case 8:
				return AREA_LENGTH_FORMAT.format(faultSectionPrefData.getAveDip());
		}
		return "";
	}
}

/**
* Fault Section Table Model
* 
* @author vipingupta
*
*/
class RuptureTableModel extends AbstractTableModel {
//	 column names
	private final static String[] columnNames = { "Rup Index", "Rup Area (sq km)", "Rup Mag", 
		"Rup Rate","Short Name", "Long Name"};
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	private final static DecimalFormat RATE_FORMAT = new DecimalFormat("0.00000");
	private A_FaultSegmentedSource aFaultSegmentedSource;
	
	/**
	 * default constructor
	 *
	 */
	public RuptureTableModel() {
		this(null);
	}
	
	/**
	 *  Preferred Fault section data
	 *  
	 * @param faultSectionsPrefDataList  ArrayList of PrefFaultSedctionData
	 */
	public RuptureTableModel(A_FaultSegmentedSource aFaultSegmentedSource) {
		setFaultSegmentedSource(aFaultSegmentedSource);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setFaultSegmentedSource(A_FaultSegmentedSource aFaultSegmentedSource) {
		this.aFaultSegmentedSource =   aFaultSegmentedSource;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(aFaultSegmentedSource==null) return 0;
		return (aFaultSegmentedSource.getNumRuptures()); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		
		//{ "Rup Index", "Rup Area", "Rup Mag", 
			//"Short Name", "Long Name"};
		
		if(aFaultSegmentedSource==null) return "";
		
		switch(columnIndex) {
			case 0:
				return ""+(rowIndex+1);
			case 1: 
				return AREA_LENGTH_FORMAT.format(aFaultSegmentedSource.getRupArea(rowIndex)/1e6);
			case 2:
				return MAG_FORMAT.format(aFaultSegmentedSource.getRupMeanMag(rowIndex));
			case 3:
				return RATE_FORMAT.format(aFaultSegmentedSource.getRupRate(rowIndex));
			case 4:
				return aFaultSegmentedSource.getShortRupName(rowIndex);
			case 5:
				return aFaultSegmentedSource.getLongRupName(rowIndex);
		}
		return "";
	}
}


/**
 * Segment Table Model
 * 
 * @author vipingupta
 *
 */
class SegmentDataTableModel extends AbstractTableModel {
	// column names
	private final static String[] columnNames = { "Segment Index", "Rec Interv (yr)","Slip Rate (mm/yr)", "Area (sq-km)",
		"Length (km)", "Moment Rate", "Segment Name"};
	private FaultSegmentData segFaultData;
	private final static DecimalFormat SLIP_RATE_FORMAT = new DecimalFormat("0.#####");
	private final static DecimalFormat AREA_LENGTH_FORMAT = new DecimalFormat("0.#");
	private final static DecimalFormat MOMENT_FORMAT = new DecimalFormat("0.#####E0");
	
	
	
	/**
	 * default constructor
	 *
	 */
	public SegmentDataTableModel() {
		this(null);
	}
	
	/**
	 * Segmented Fault data
	 * @param segFaultData
	 */
	public SegmentDataTableModel( FaultSegmentData segFaultData) {
		setSegmentedFaultData(segFaultData);
	}
	
	/**
	 * Set the segmented fault data
	 * @param segFaultData
	 */
	public void setSegmentedFaultData(FaultSegmentData segFaultData) {
		this.segFaultData =   segFaultData;
	}
	
	/**
	 * Get number of columns
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	
	/**
	 * Get column name
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}
	
	/*
	 * Get number of rows
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		if(segFaultData==null) return 0;
		return (segFaultData.getNumSegments()+1); 
	}
	
	
	/**
	 * 
	 */
	public Object getValueAt (int rowIndex, int columnIndex) {
		if(segFaultData==null) return "";
		if(rowIndex == segFaultData.getNumSegments()) return getTotalValues(columnIndex);
		switch(columnIndex) {
			case 0:
				return ""+(rowIndex+1);
			case 1:
				return ""+segFaultData.getRecurInterval(rowIndex);
			case 2: 
				// convert to mm/yr
				return SLIP_RATE_FORMAT.format(segFaultData.getSegmentSlipRate(rowIndex)*1e3);
			case 3:
				// convert to sq km
				return AREA_LENGTH_FORMAT.format(segFaultData.getSegmentArea(rowIndex)/1e6);
			case 4:
				// convert to km
				return AREA_LENGTH_FORMAT.format(segFaultData.getSegmentLength(rowIndex)/1e3);
			case 5:
				return MOMENT_FORMAT.format(segFaultData.getSegmentMomentRate(rowIndex));
			case 6:
				return ""+segFaultData.getSegmentName(rowIndex);
		}
		return "";
	}
	
	private Object getTotalValues(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return "Total";
		case 1: 
			// convert to mm/yr
			return "";
		case 2:
			// convert to sq km
			return AREA_LENGTH_FORMAT.format(segFaultData.getTotalArea()/1e6);
		case 3:
			// convert to km
			return AREA_LENGTH_FORMAT.format(segFaultData.getTotalLength()/1000);
		case 4:
			return MOMENT_FORMAT.format(segFaultData.getTotalMomentRate());
		case 5:
			return "";
	}
	return "";
	}
}
