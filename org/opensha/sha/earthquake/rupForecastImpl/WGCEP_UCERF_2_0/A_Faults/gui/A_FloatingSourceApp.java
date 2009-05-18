/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.calc.MomentMagCalc;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_A_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Ellsworth_B_WG02_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.HanksBakun2002_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.Somerville_2006_MagAreaRel;
import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.FaultSegmentData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.A_Faults.WG_02FaultSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_0.data.A_FaultsFetcher;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;
import org.opensha.sha.magdist.YC_1985_CharMagFreqDist;
import org.opensha.sha.param.MagFreqDistParameter;

/**
 * @author vipingupta
 *
 */
public class A_FloatingSourceApp extends JFrame implements ParameterChangeListener, ActionListener {
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
	
	// aseismic factor interpolated	
	private final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor reduces section area";
	private final static String ASEIS_INTER_PARAM_INFO = "Otherwise it reduces section slip rate";
	
	// floater MFD _ PDF
	private final static String MAG_PDF_PARAM_NAME = "Floating Rup Mag PDF";

	
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
	private final static String MSG_ASEIS_REDUCES_AREA = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of area (as requested) in the table above; this will also influence the segment slip rates for any segments composed of more than one section (because the slip rates are weight-averaged according to section areas)";
	private final static String MSG_ASEIS_REDUCES_SLIPRATE = "IMPORTANT NOTE - Section Aseismicity Factors have been applied as a reduction of slip rate (as requested); keep this in mind when interpreting the segment slip rates (which for any segments composed of more than one section are a weight average by section areas)";
	private final static String TITLE = " Type A Floating Source App";
	private  FaultSegmentData segmetedFaultData;
	/**
	 * Constructor
	 *
	 */
	public A_FloatingSourceApp() {
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
			loadSegmentModels();
			loadDeformationModels();
			makeAseisFactorInterpolationParamAndEditor();
			makeMagAreRelationshipParamAndEditor();
			makeFloaterPDFParam();
			paramListEditor = new ParameterListEditor(this.paramList);
			paramListEditor.setTitle(PARAM_EDITOR_TITLE);
			updateSegmentAndRupNames(true);
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
		mainSplitPane.add(this.tabbedPane, JSplitPane.RIGHT);
		tabbedPane.addTab("Segment Data", rightSplitPane);
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
		boolean isAseisReducesArea = getAseisReducesArea();
		IncrementalMagFreqDist floatingRup_PDF = getFloatingRup_PDF();
		UnsegmentedSource faultSource = new UnsegmentedSource( this.segmetedFaultData,  magAreaRel, floatingRup_PDF,0.0);
		A_FloatingSourceOuput outputWindow  =  new A_FloatingSourceOuput(faultSource, segmetedFaultData, getMetadata());
		
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
		metadata+=this.paramList.getParameter(ASEIS_INTER_PARAM_NAME).getMetadataString()+"\n";
		metadata+=this.paramList.getParameter(MAG_PDF_PARAM_NAME).getMetadataString()+"\n";
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
		aseisFactorInterParam.addParameterChangeListener(this);
		/*BooleanParameterEditor aseisFactorInterParamEditor= new BooleanParameterEditor(aseisFactorInterParam);
		add(aseisFactorInterParamEditor,
	             new GridBagConstraints(0, 9, 1, 1, 1.0, 1.0
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
	/**
	 * Load the Segment models from a text file
	 *
	 */
	private void loadSegmentModels() {
		 aFaultsFetcher = new A_FaultsFetcher();
		 ArrayList segmentModelNames = aFaultsFetcher.getAllFaultNames();
		 segmentModelNames.add(0, NONE);
		 makeSegmentModelParamAndEditor(segmentModelNames);
		
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
			updateSegmentAndRupNames(true);
		else if(paramName.equalsIgnoreCase(DEFORMATION_MODEL_PARAM_NAME))
			updateSegmentAndRupNames(true);
		else if(paramName.equalsIgnoreCase(ASEIS_INTER_PARAM_NAME))
			updateSegmentAndRupNames(true);
	}
	

	
	/**
	 * Update the segment names and scenario names wheneever a new segment model is chosen
	 *
	 */
	private void updateSegmentAndRupNames(boolean refreshData) {
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
		segmetedFaultData = getSegmentData();
		this.segmentTableModel.setSegmentedFaultData(segmetedFaultData);
		segmentTableModel.fireTableDataChanged();
		setMagAndSlipsString(segmetedFaultData);
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
				summaryString+=j+"              "+MAG_FORMAT.format(mag)+"      "+
								SLIP_FORMAT.format(FaultMomentCalc.getSlip(segmetedFaultData.getSegmentArea(j), moment))+"\n";
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
	 * @param args
	 */
	public static void main(String[] args) {
		A_FloatingSourceApp faultSourceApp = new A_FloatingSourceApp();

	}
}

