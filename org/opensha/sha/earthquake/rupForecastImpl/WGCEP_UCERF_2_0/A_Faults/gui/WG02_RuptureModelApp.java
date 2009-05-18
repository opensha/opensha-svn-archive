/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.gui;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.ParameterListParameter;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.TreeBranchWeightsParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.WG_02FaultSource;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.magdist.YC_1985_CharMagFreqDist;
import org.opensha.sha.param.MagFreqDistParameter;
import org.opensha.util.FileUtils;
import java.text.DecimalFormat;

/**
 * @author vipingupta
 *
 */
public class WG02_RuptureModelApp extends JFrame implements ParameterChangeListener, ActionListener {
	private final static String SEGMENT_MODELS_FILE_NAME = "org/opensha/sha/earthquake/rupForecastImpl/WGCEP_UCERF_2_0/data/SegmentModels.txt";

	// choose deformation model
	private final static String DEFORMATION_MODEL_PARAM_NAME = "Deformation Model";	
	// choose segment model
	private final static String SEGMENT_MODELS_PARAM_NAME = "Segment Model";
	private final static String NONE = "None";
	private final static String MSG_FROM_DATABASE = "Retrieving Data from database. Please wait....";
	private HashMap segmentModels = new HashMap();
	// text area to show segment names when segment model is chosen
	private JTextArea segmentAndScenarioNames = new JTextArea();
	// choose mag area relationship
	private final static String MAG_AREA_RELS_PARAM_NAME = "Mag-Area Relationship";
	private final static String MAG_AREA_RELS_PARAM_INFO = "Mag-Area Relationship for mean mag of characteristic events";
	// choose Mag Sigma
	private final static String MAG_SIGMA_PARAM_NAME = "Mag Sigma";
	private final static String MAG_SIGMA_PARAM_INFO = "Standard Deviation for characteristic events";
	private final static Double MAG_SIGMA_DEFAULT = new Double(0.12);
	//Mag Truncation Type
	private final static String TRUNC_TYPE_PARAM_NAME = "Truncation Type";
	private final static String TRUNC_TYPE_PARAM_INFO = "Truncation of gaussian for characteristic events";
	private final static String NO_TRUNCATION = "No Truncation";
	private final static String ONE_SIDED_TRUNCATION = "Upper Truncation";
	private final static String TWO_SIDED_TRUNCATION = "Upper & Lower Truncation";
	private final static String TRUNC_TYPE_DEFAULT = TWO_SIDED_TRUNCATION;
	// Mag truncation level
	private final static String TRUNC_LEVEL_PARAM_NAME = "Truncation Level";
	private final static Double TRUNC_LEVEL_DEFAULT = new Double(2.0);
	
	// aseismic factor interpolated
	
	private final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor reduces section area";
	private final static String ASEIS_INTER_PARAM_INFO = "Otherwise it reduces section slip rate";
	
	// floater MFD _ PDF
	private final static String MAG_PDF_PARAM_NAME = "Floating Rup Mag PDF";
	
	// sceanrio weights
	private TreeBranchWeightsParameter scenarioWtsParam;
	private final static String SCENARIO_WT_PARAM_NAME = "Scenario Wts";
	private final static String SCECNARIO_PREFIX = "Scenario ";
	private final static String FLOATER_RUP_WT_NAME="Floating Rup";
	private final static Double MIN_SCEN_WEIGHT = new Double(0);
	private final static Double MAX_SCEN_WEIGHT = new Double(1);
	
	private final static String SEGMENT_MODEL_NAME_PREFIX = "-";
	
	private DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList deformationModelsList;
	private final static String MSG_NO_DEF_MODEL_EXISTS = "Currently, there is no Deformation Model";
	
	private JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private JPanel leftPanel = new JPanel(new GridBagLayout());
	private JPanel rightPanel = new JPanel(new GridBagLayout());
	private final static String PARAM_EDITOR_TITLE = "Rupture Model Params";
	
	private ParameterList paramList;
	private ParameterListEditor paramListEditor;
	private ArrayList segmentData ;
	
	// DAO to access the fault section database
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	
	private JButton calcButton  = new JButton("Calculate");
	private final static int W = 800;
	private final static int H = 700;
	private String scenarioNames[];
	private String faultSectionsDataString;
	private ArrayList magAreaRelationships;
	private final static DecimalFormat MAG_FORMAT = new DecimalFormat("0.00");
	/**
	 * Constructor
	 *
	 */
	public WG02_RuptureModelApp() {
		initParamsAndEditor();
		this.createGUI();
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
			loadSegmentModels();
			loadDeformationModels();
			makeMagAreRelationshipParamAndEditor();
			makeMagSigmaTruncParamsAndEditor();
			makeAseisFactorInterpolationParamAndEditor();
			makeFloaterPDFParam();
			makeScenarioWtsParamAndEditor(1);
			paramListEditor = new ParameterListEditor(this.paramList);
			paramListEditor.setTitle(PARAM_EDITOR_TITLE);
			setTruncLevelVisibility();
			updateSegmentNamesAndScenarios();
		}catch(Exception e) {
			e.printStackTrace();
		}
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
		mainSplitPane.add(this.rightPanel, JSplitPane.RIGHT);
		leftPanel.add(this.paramListEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		leftPanel.add(this.calcButton, new GridBagConstraints(0, 1, 1, 0, 1.0, 0.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.NONE,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		calcButton.addActionListener(this);
		segmentAndScenarioNames.setEditable(false);
		rightPanel.add(new JScrollPane(this.segmentAndScenarioNames), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
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
		double magSigma = getMagSigma();
		double magTruncLevel = getMagTruncLevel();
		int truncType = getTruncType();
		double[] scenarioWts = getScenarioWts();
		boolean isAseisReducesArea = getAseisReducesArea();
		IncrementalMagFreqDist floatingRup_PDF = getFloatingRup_PDF();
		WG_02FaultSource faultSource = new WG_02FaultSource( segmentData,  magAreaRel,  magSigma,
		           magTruncLevel, truncType,  scenarioWts, isAseisReducesArea,  floatingRup_PDF);
		WG02_RuptureModelOuput outputWindow  =  new WG02_RuptureModelOuput(faultSource, getMetadata());
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
		metadata+=this.paramList.getParameter(MAG_SIGMA_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(TRUNC_TYPE_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(TRUNC_LEVEL_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(ASEIS_INTER_PARAM_NAME).getMetadataString()+"\n";
		metadata+=SCENARIO_WT_PARAM_NAME+":\n";
		ParameterList wtParams = ((ParameterListParameter)paramList.getParameter(SCENARIO_WT_PARAM_NAME)).getParameter();
		Iterator it = wtParams.getParametersIterator();
		int i=1;
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			if(param.getName().equalsIgnoreCase(this.FLOATER_RUP_WT_NAME))
				metadata+="   "+param.getMetadataString()+"\n";
			else metadata+="   "+(i++)+" = "+(float)((Double)param.getValue()).doubleValue()+"\n";
		}
		metadata+=this.paramList.getParameter(MAG_PDF_PARAM_NAME).getMetadataString()+"\n";
		return metadata;
	}
	
	/**
	 * Get the segment data
	 * @return
	 */
	private ArrayList getSegmentData() {
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
		ArrayList segmentsList = (ArrayList)this.segmentModels.get(selectedSegmentModel);
		ArrayList newSegmentsList = new ArrayList();
		// faultSectionsDataString="\n\nName,Slip Rate(mm/yr),Aseis Factor,Length(km),Down Dip Width(km),Area(sq. km),Upper Depth(km),LowerDepth(km),Dip\n";
		StringBuffer faultSectionsString = new StringBuffer("");
		double totalArea = 0;
		double totalAseisReduceArea=0;
		// iterate over all segment
		for(int i=0; i<segmentsList.size(); ++i) {
			ArrayList segment = (ArrayList)segmentsList.get(i);
			ArrayList newSegment = new ArrayList();
			// iterate over all sections in a segment
			for(int j=0; j<segment.size(); ++j) {
				int faultSectionId = ((FaultSectionSummary)segment.get(j)).getSectionId();
				FaultSectionData faultSectionData = this.faultSectionDAO.getFaultSection(faultSectionId);
				// get slip rate and aseimic slip factor from deformation model
				faultSectionData.setAseismicSlipFactorEst(this.deformationModelDB_DAO.getAseismicSlipEstimate(selectdDeformationModelId, faultSectionData.getSectionId()));
				faultSectionData.setAveLongTermSlipRateEst(this.deformationModelDB_DAO.getSlipRateEstimate(selectdDeformationModelId, faultSectionData.getSectionId()));
				FaultSectionPrefData faultSectionPrefData = faultSectionData.getFaultSectionPrefData();
				double length = faultSectionPrefData.getLength();
				double ddw = faultSectionPrefData.getDownDipWidth();
				double area = length*ddw;
				totalArea+=area;
				totalAseisReduceArea+=(1-faultSectionPrefData.getAseismicSlipFactor())*area;
				faultSectionsString.append(faultSectionPrefData.getSectionName()+" Section:\n\n");
				faultSectionsString.append("\t"+(float)faultSectionPrefData.getAveLongTermSlipRate()+" Slip Rate (mm/yr)\n");
				faultSectionsString.append("\t"+(float)faultSectionPrefData.getAseismicSlipFactor()+ " Aseismic Factor\n");
				faultSectionsString.append("\t"+(float)length+" Length (km)\n");
				faultSectionsString.append("\t"+(float)ddw+" Down Dip Width (km)\n");
				faultSectionsString.append("\t"+(float)area+" Area (sq km) \n");
				faultSectionsString.append("\t"+(float)faultSectionPrefData.getAveUpperDepth()+" Upper Depth (km)\n");
				faultSectionsString.append("\t"+(float)	faultSectionPrefData.getAveLowerDepth()+" Lower Depth (km)\n");
				faultSectionsString.append("\t"+(float)faultSectionPrefData.getAveDip()+" Ave Dip (degrees)\n\n");
				
				/*faultSectionsDataString+=faultSectionPrefData.getSectionName()+","+
										(float)faultSectionPrefData.getAveLongTermSlipRate()+","+
										(float)faultSectionPrefData.getAseismicSlipFactor()+","+
										(float)length+","+
										(float)ddw+","+
										(float)area+","+
										(float)faultSectionPrefData.getAveUpperDepth()+","+
										(float)faultSectionPrefData.getAveLowerDepth()+","+
										(float)faultSectionPrefData.getAveDip()+"\n";*/
				newSegment.add(faultSectionPrefData);
				
			}
			newSegmentsList.add(newSegment);
		}
		String summaryString="\nSegment Totals:\n   Total Area (sq km) ="+(float)totalArea+"\n";
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			summaryString+="   Mean Mag ("+magAreaRel.getName()+") = "+MAG_FORMAT.format(magAreaRel.getMedianMag(totalArea))+"\n";
		}
		summaryString+="\nSegment Totals (aseis reduced):\n   Total Area (sq km) ="+(float)totalAseisReduceArea+"\n";
		for(int i=0; i<magAreaRelationships.size(); ++i) {
			MagAreaRelationship magAreaRel = (MagAreaRelationship)magAreaRelationships.get(i);
			summaryString+="   Mean Mag ("+magAreaRel.getName()+") = "+MAG_FORMAT.format(magAreaRel.getMedianMag(totalAseisReduceArea))+"\n";
		}
		faultSectionsDataString = summaryString+"\n"+faultSectionsString;
		frame.dispose();
		return newSegmentsList;
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
	private IncrementalMagFreqDist getFloatingRup_PDF() {
		MagFreqDistParameter param = (MagFreqDistParameter)paramList.getParameter(MAG_PDF_PARAM_NAME);
		param.setMagDist();
		return (IncrementalMagFreqDist)param.getValue();
	}
	
	/**
	 * Whether Aseis reduces area
	 * @return
	 */
	private boolean getAseisReducesArea() {
		return ((Boolean)paramList.getValue(ASEIS_INTER_PARAM_NAME)).booleanValue();
	}
	
	/**
	 * Get scenario weights
	 * 
	 * @return
	 */
	private double[] getScenarioWts() {
		ParameterList wtParamList = (ParameterList)paramList.getValue(SCENARIO_WT_PARAM_NAME);
		double scenarioWts[] = new double[wtParamList.size()];
		Iterator it = wtParamList.getParametersIterator();
		int i=0;
		while(it.hasNext()) {
			ParameterAPI param = (ParameterAPI)it.next();
			scenarioWts[i++] = ((Double)param.getValue()).doubleValue();
		}
		return scenarioWts;
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
	private double getMagSigma() {
		return ((Double)this.paramList.getValue(MAG_SIGMA_PARAM_NAME)).doubleValue();
	}
	
	
	/**
	 * Mag trunc level
	 * 
	 * @return
	 */
	private double getMagTruncLevel() {
		return ((Double)this.paramList.getValue(TRUNC_LEVEL_PARAM_NAME)).doubleValue();
	}
	
	/**
	 * Get the truncation type
	 * 
	 * @return
	 */
	private int getTruncType() {
		String truncType = (String)paramList.getValue(TRUNC_TYPE_PARAM_NAME);
		if(truncType.equalsIgnoreCase(NO_TRUNCATION)) return 0;
		if(truncType.equalsIgnoreCase(ONE_SIDED_TRUNCATION)) return 1;
		if(truncType.equalsIgnoreCase(TWO_SIDED_TRUNCATION)) return 2;
		throw new RuntimeException("Unsupported truncation type");
	}
	
	/**
	 * Make Scenario Weights Param and Editor
	 *
	 */
	private void makeScenarioWtsParamAndEditor(int numScenarios) {
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
	}
	
	
	/**
	 * Floater Mag Freq PDF
	 *
	 */
	private void makeFloaterPDFParam() {
		ArrayList allowedMagDists = new ArrayList();
		allowedMagDists.add(SingleMagFreqDist.NAME);
		allowedMagDists.add(GutenbergRichterMagFreqDist.NAME);
		allowedMagDists.add(GaussianMagFreqDist.NAME);
		allowedMagDists.add(YC_1985_CharMagFreqDist.NAME);
		allowedMagDists.add(SummedMagFreqDist.NAME);
		allowedMagDists.add(ArbIncrementalMagFreqDist.NAME);
		MagFreqDistParameter magPDF_Parameter = new MagFreqDistParameter(MAG_PDF_PARAM_NAME, allowedMagDists);
		paramList.addParameter(magPDF_Parameter);
		/*MagPDF_ParameterEditor magPDF_ParameterEditor = new MagPDF_ParameterEditor(magPDF_Parameter);
		add(magPDF_ParameterEditor,
	             new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	/**
	 * Whether Aseis Factor will be interpolated
	 *
	 */
	private void makeAseisFactorInterpolationParamAndEditor() {
		BooleanParameter aseisFactorInterParam = new BooleanParameter(ASEIS_INTER_PARAM_NAME, new Boolean(true));
		aseisFactorInterParam.setInfo(ASEIS_INTER_PARAM_INFO);
		paramList.addParameter(aseisFactorInterParam);
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
	private void makeMagSigmaTruncParamsAndEditor() throws Exception {
		// choose Mag Sigma
		 DoubleParameter magSigmaParam = new DoubleParameter(MAG_SIGMA_PARAM_NAME, MAG_SIGMA_DEFAULT);
		 magSigmaParam.setInfo(MAG_SIGMA_PARAM_INFO);
		 paramList.addParameter(magSigmaParam);
		 
		 /*DoubleParameterEditor magSigmaParamEditor = new DoubleParameterEditor(magSigmaParam);
		 add(magSigmaParamEditor,
	             new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
		//Mag Truncation Type	
		ArrayList truncTypes = new ArrayList();
		truncTypes.add(NO_TRUNCATION);
		truncTypes.add(ONE_SIDED_TRUNCATION);
		truncTypes.add(TWO_SIDED_TRUNCATION);
		StringParameter truncTypesParam = new StringParameter(TRUNC_TYPE_PARAM_NAME, truncTypes, TRUNC_TYPE_DEFAULT);
		truncTypesParam.addParameterChangeListener(this);
		truncTypesParam.setInfo(TRUNC_TYPE_PARAM_INFO);
		paramList.addParameter(truncTypesParam);
		/*ConstrainedStringParameterEditor truncTypesParamEditor = new ConstrainedStringParameterEditor(truncTypesParam);
		add(truncTypesParamEditor,
	             new GridBagConstraints(0, 8, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
		// Mag truncation level
		DoubleParameter truncLevelParam = new DoubleParameter(TRUNC_LEVEL_PARAM_NAME, TRUNC_LEVEL_DEFAULT);
		paramList.addParameter(truncLevelParam);
		/*truncLevelParamEditor = new DoubleParameterEditor(truncLevelParam);
		add(truncLevelParamEditor,
	             new GridBagConstraints(0, 8, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));*/
	}
	
	
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
	
	
	
	/**
	 * Load the Segment models from a text file
	 *
	 */
	private void loadSegmentModels() {
		ArrayList segmentModelNames = new ArrayList();
		segmentModelNames.add(NONE);
		// add segment models 
		try {
			// read the text file
			ArrayList fileLines = FileUtils.loadFile(SEGMENT_MODELS_FILE_NAME);
			ArrayList segmentsList=null;
			String segmentModelName=null;
			for(int i=0; i<fileLines.size(); ++i) {
				// read the file line by line
				String line = ((String)fileLines.get(i)).trim();
				// skip the comment and blank lines
				if(line.equalsIgnoreCase("") || line.startsWith("#")) continue;
				// check if this is a segment model name
				if(line.startsWith(SEGMENT_MODEL_NAME_PREFIX)) {
					if(segmentModelName!=null ){
						// put segment model and corresponding ArrayList of segments in a HashMap
						this.segmentModels.put(segmentModelName, segmentsList);
					}
					segmentModelName = getSegmentModelName(line);
					segmentModelNames.add(segmentModelName);
					segmentsList = new ArrayList();
				} else{
					segmentsList.add(getSegment(line));
				}		
			}
			segmentModels.put(segmentModelName, segmentsList);
			makeSegmentModelParamAndEditor(segmentModelNames);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	
	/*
	 * Get a list of fault sections for the current segment 
	 */ 
	private ArrayList getSegment(String line) {
		ArrayList faultSectionsIdList = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(line,"\n,");
		while(tokenizer.hasMoreTokens()) 
			faultSectionsIdList.add(faultSectionDAO.getFaultSectionSummary(Integer.parseInt(tokenizer.nextToken().trim())));
		return faultSectionsIdList;
	}
	
	/**
	 * Get the Segment model name
	 * 
	 * @param line
	 * @return
	 */
	private String getSegmentModelName(String line) {
		int index = line.indexOf("-");
		return line.substring(index+1).trim();
	}
	
	/**
	 * This function is called on change of a parameter
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if(paramName.equalsIgnoreCase(TRUNC_TYPE_PARAM_NAME))
			setTruncLevelVisibility();
		else if(paramName.equalsIgnoreCase(SEGMENT_MODELS_PARAM_NAME))
			updateSegmentNamesAndScenarios();
		else if(paramName.equalsIgnoreCase(DEFORMATION_MODEL_PARAM_NAME))
			updateSegmentNamesAndScenarios();
	}
	

	
	/**
	 * Update the segment names and scenario names wheneever a new segment model is chosen
	 *
	 */
	private void updateSegmentNamesAndScenarios() {
		String selectedSegmentModel = (String)this.paramList.getValue(SEGMENT_MODELS_PARAM_NAME);
		if(selectedSegmentModel.equalsIgnoreCase(NONE)) {
			segmentAndScenarioNames.setText("");
			this.scenarioNames = null;
			this.calcButton.setEnabled(false); // if no Segment model is chosen, disable the calc button
			return;
		}
		this.calcButton.setEnabled(true); // if a Segment model is chosen, enable the calc button
		segmentData = getSegmentData();
		// get the segment names
		ArrayList segmentsList = (ArrayList)this.segmentModels.get(selectedSegmentModel);
		String[] segmentNames= new String[segmentsList.size()];
		for(int i=0; i<segmentsList.size(); ++i) {
			ArrayList sectionNamesList = new ArrayList();
			ArrayList segment = (ArrayList)segmentsList.get(i);
			for(int j=0; j<segment.size(); ++j)
				sectionNamesList.add(((FaultSectionSummary)segment.get(j)).getSectionName());
			segmentNames[i]=WG_02FaultSource.getSegmentName(sectionNamesList);
		}
		// get the rupture names
		String rupNames[] = WG_02FaultSource.getRuptureNames(segmentNames);
		// get the scenario names
		scenarioNames = WG_02FaultSource.getScenarioNames(rupNames, segmentNames.length);
		// updatet he text area with segment names, scenario names and rup names
		updateTextArea(segmentNames, rupNames, scenarioNames);
		makeScenarioWtsParamAndEditor(scenarioNames.length);
	}
	
	/**
	 * Update the text area with segment names, scenario names and rupture names
	 * 
	 * @param segmentNames
	 * @param rupNames
	 * @param scenarioNames
	 */
	private void updateTextArea(String[] segmentNames, String[] rupNames, String[] scenarioNames) {
		// segment names
		String text  = "SEGMENTS\n\n";
		for(int i=0; i<segmentNames.length; ++i)
			text+="Segment "+(i+1)+": "+segmentNames[i]+"\n";
		// scenario names
		text+="\n\nSCENARIOS\n\n";
		for(int i=0; i<scenarioNames.length; ++i) 
			text+="Scenario "+(i+1)+": "+scenarioNames[i]+"\n";
		// rupture names
		text+="\n\nRUPTURES\n\n";
		for(int i=0; i<rupNames.length; ++i) 
			text+="Rupture "+(i+1)+": "+rupNames[i]+"\n";
		this.segmentAndScenarioNames.setText(text+faultSectionsDataString);
		segmentAndScenarioNames.setCaretPosition(0);
	}
	
	/**
	 * Make the truncation level visible only if Truncation Type is not None
	 *
	 */
	private void setTruncLevelVisibility() {
		String truncType = (String)this.paramList.getValue(TRUNC_TYPE_PARAM_NAME);
		if(truncType.equalsIgnoreCase(NO_TRUNCATION))
			this.paramListEditor.getParameterEditor(TRUNC_LEVEL_PARAM_NAME).setVisible(false);
		else paramListEditor.getParameterEditor(TRUNC_LEVEL_PARAM_NAME).setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WG02_RuptureModelApp rupModelApp = new WG02_RuptureModelApp();

	}

}
