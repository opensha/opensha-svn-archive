/**
 * 
 */
package org.opensha.sha.gui;


import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagAreaRelationship;
import org.opensha.param.BooleanParameter;
import org.opensha.param.DoubleParameter;
import org.opensha.param.StringParameter;
import org.opensha.param.TreeBranchWeightsParameter;
import org.opensha.param.editor.BooleanParameterEditor;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.param.editor.DoubleParameterEditor;
import org.opensha.param.editor.TreeBranchWeightsParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DeformationModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.DeformationModelSummaryDB_DAO;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.DeformationModelSummary;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.param.MagPDF_Parameter;
import org.opensha.sha.param.editor.MagPDF_ParameterEditor;
import org.opensha.util.FileUtils;

/**
 * @author vipingupta
 *
 */
public class RuptureModelApp extends JApplet implements ParameterChangeListener {
	private final static String SEGMENT_MODELS_FILE_NAME = "SegmentModels.txt";
	
	// choose deformation model
	private StringParameter deformationModelsParam;
	private final static String DEFORMATION_MODEL_PARAM_NAME = "Choose Deformation Model";
	
	// choose segment model
	private StringParameter segmentModelParam;
	private final static String SEGMENT_MODELS_PARAM_NAME = "Choose Segment Model";
	private HashMap segmentModels = new HashMap();
	
	// text area to show segment names when segment model is chosen
	private JTextArea segmentNames = new JTextArea();
	
	// choose mag area relationship
	private StringParameter magAreaRelParam;
	private final static String MAG_AREA_RELS_PARAM_NAME = "Chooes Mag-Area Relationship";
	
	// choose Mag Sigma
	private DoubleParameter magSigmaParam;
	private final static String MAG_SIGMA_PARAM_NAME = "Mag Sigma";
	
	//Mag Truncation Type
	private StringParameter truncTypesParam;
	private final static String TRUNC_TYPE_PARAM_NAME = "Truncation Type";
	private final static String NO_TRUNCATION = "No Truncation";
	private final static String ONE_SIDED_TRUNCATION = "Upper Truncation";
	private final static String TWO_SIDED_TRUNCATION = "Upper & Lower Truncation";
	
	// Mag truncation level
	private DoubleParameter truncLevelParam;
	private final static String TRUNC_LEVEL_PARAM_NAME = "Truncation Level";
	private DoubleParameterEditor truncLevelParamEditor;
	
	// aseismic factor interpolated
	private BooleanParameter aseisFactorInterParam;
	private final static String ASEIS_INTER_PARAM_NAME = "Aseis Factor Interpolated";
	
	// floater MFD _ PDF
	private MagPDF_Parameter magPDF_Parameter;
	private final static String MAG_PDF_PARAM_NAME = "Floating Rup Mag PDF";
	
	// sceanrio weights
	private TreeBranchWeightsParameter scenarioWtsParam;
	private TreeBranchWeightsParameterEditor scenarioWtsParamEditor;
	private final static String SCENARIO_WT_PARAM_NAME = "Scenario Wts";
	private final static String SCECNARIO_PREFIX = "Scenario ";
	
	private final static String SEGMENT_MODEL_NAME_PREFIX = "-";
	private DeformationModelDB_DAO deformationModelDB_DAO = new DeformationModelDB_DAO(DB_AccessAPI.dbConnection);
	private DeformationModelSummaryDB_DAO deformationModelSummaryDB_DAO = new DeformationModelSummaryDB_DAO(DB_AccessAPI.dbConnection);
	private ArrayList deformationModelsList;
	private final static String MSG_NO_DEF_MODEL_EXISTS = "Currently, there is no Deformation Model";
	

	/**
	 * Constructor
	 *
	 */
	public RuptureModelApp() {}

	/**
	 * Initaliaze params and editor
	 *
	 */
	private void initParamsAndEditor() {
		try {
			loadDeformationModels();
			loadSegmentModels();
			makeMagAreRelationshipParamAndEditor();
			makeMagSigmaTruncParamsAndEditor();
			makeAseisFactorInterpolationParamAndEditor();
			makeFloaterPDFParam();
			makeScenarioWtsParamAndEditor();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Make Scenario Weights Param and Editor
	 *
	 */
	private void makeScenarioWtsParamAndEditor() {
		
		//scenarioWtsParam = new TreeBranchWeightsParameter(SCENARIO_WT_PARAM_NAME, );
		//private final static String SCENARIO_WT_PARAM_NAME = "Scenario Wts";
		//private final static String SCECNARIO_PREFIX = "Scenario ";
	}
	
	/**
	 * Floater Mag Freq PDF
	 *
	 */
	private void makeFloaterPDFParam() {
		ArrayList allowedMagDists = new ArrayList();
		allowedMagDists.add(GaussianMagFreqDist.NAME);
		magPDF_Parameter = new MagPDF_Parameter(MAG_PDF_PARAM_NAME, allowedMagDists);
		MagPDF_ParameterEditor magPDF_ParameterEditor = new MagPDF_ParameterEditor(magPDF_Parameter);
		add(magPDF_ParameterEditor,
	             new GridBagConstraints(0, 10, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/**
	 * Whether Aseis Factor will be interpolated
	 *
	 */
	private void makeAseisFactorInterpolationParamAndEditor() {
		this.aseisFactorInterParam = new BooleanParameter(this.ASEIS_INTER_PARAM_NAME);
		BooleanParameterEditor aseisFactorInterParamEditor= new BooleanParameterEditor(aseisFactorInterParam);
		add(aseisFactorInterParamEditor,
	             new GridBagConstraints(0, 9, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/**
	 * Make Mag Sigma, Truncation parameters
	 * @throws Exception
	 */
	private void makeMagSigmaTruncParamsAndEditor() throws Exception {
		// choose Mag Sigma
		 magSigmaParam = new DoubleParameter(MAG_SIGMA_PARAM_NAME);
		 DoubleParameterEditor magSigmaParamEditor = new DoubleParameterEditor(magSigmaParam);
		 add(magSigmaParamEditor,
	             new GridBagConstraints(0, 6, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		//Mag Truncation Type	
		ArrayList truncTypes = new ArrayList();
		truncTypes.add(NO_TRUNCATION);
		truncTypes.add(ONE_SIDED_TRUNCATION);
		truncTypes.add(TWO_SIDED_TRUNCATION);
		truncTypesParam = new StringParameter(TRUNC_TYPE_PARAM_NAME, truncTypes, (String)truncTypes.get(0));
		truncTypesParam.addParameterChangeListener(this);
		ConstrainedStringParameterEditor truncTypesParamEditor = new ConstrainedStringParameterEditor(truncTypesParam);
		add(truncTypesParamEditor,
	             new GridBagConstraints(0, 8, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		// Mag truncation level
		truncLevelParam = new DoubleParameter(TRUNC_LEVEL_PARAM_NAME);
		truncLevelParamEditor = new DoubleParameterEditor(truncLevelParam);
		add(truncLevelParamEditor,
	             new GridBagConstraints(0, 8, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	
	/**
	 * Initialize the Applet
	 */
	public void init() {
		
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
		
		deformationModelsParam = new StringParameter(DEFORMATION_MODEL_PARAM_NAME,deformationModelNames, (String)deformationModelNames.get(0) );
		ConstrainedStringParameterEditor deformationModelsParamEditor = new ConstrainedStringParameterEditor(deformationModelsParam);
		// deformation model selection editor
		add(deformationModelsParamEditor,
	             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	
	
	/**
	 * Load the Segment models from a text file
	 *
	 */
	private void loadSegmentModels() {
		ArrayList segmentModelNames = new ArrayList();
		
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
				} else segmentsList.add(getSegment(line));
				
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
		ArrayList magAreaList = new ArrayList();
		magAreaList.add(WC1994_MagAreaRelationship.NAME);
		magAreaRelParam = new StringParameter(MAG_AREA_RELS_PARAM_NAME, magAreaList, (String)magAreaList.get(0));
		ConstrainedStringParameterEditor magAreaRelParamEditor = new ConstrainedStringParameterEditor(magAreaRelParam);
		// mag area relationship selection editor
		add(magAreaRelParamEditor,
	             new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/**
	 * Make Segment Model Param and editor
	 *
	 */
	private void makeSegmentModelParamAndEditor(ArrayList segmentModelNames) {
		segmentModelParam = new StringParameter(SEGMENT_MODELS_PARAM_NAME, segmentModelNames, (String)segmentModelNames.get(0) );
		segmentModelParam.addParameterChangeListener(this);
		ConstrainedStringParameterEditor segmentModelParamEditor = new ConstrainedStringParameterEditor(segmentModelParam);
		// segment model selection editor
		add(segmentModelParamEditor,
	             new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.HORIZONTAL,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		add(new JScrollPane(this.segmentNames), new GridBagConstraints(0, 2, 1, 3, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/*
	 * Get a list of fault sections for the current segment 
	 */ 
	private ArrayList getSegment(String line) {
		ArrayList faultSectionsIdList = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(line,"\n,");
		while(tokenizer.hasMoreTokens()) 
			faultSectionsIdList.add(new Integer(Integer.parseInt(tokenizer.nextToken())));
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
		if(paramName.equalsIgnoreCase(this.TRUNC_TYPE_PARAM_NAME))
			setTruncLevelVisibility();
		else if(paramName.equalsIgnoreCase(this.SEGMENT_MODELS_PARAM_NAME))
			updateSegmentNamesAndScenarios();
		
	}
	
	/**
	 * Update the segment names and scenario names wheneever a new segment model is chosen
	 *
	 */
	private void updateSegmentNamesAndScenarios() {
		
	}
	
	/**
	 * Make the truncation level visible only if Truncation Type is not None
	 *
	 */
	private void setTruncLevelVisibility() {
		String truncType = (String)this.truncTypesParam.getValue();
		if(truncType.equalsIgnoreCase(this.NO_TRUNCATION))
			this.truncLevelParamEditor.setVisible(false);
		else truncLevelParamEditor.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
