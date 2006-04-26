/**
 * 
 */
package org.opensha.refFaultParamDb.gui.view;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Insets;
import java.util.ArrayList;

import org.opensha.param.StringParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionSummary;
import org.opensha.refFaultParamDb.vo.FaultSectionVer2;
import org.opensha.sha.fault.FaultTrace;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.refFaultParamDb.gui.infotools.GUI_Utils;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.gui.addEdit.EditFaultSection;
import org.opensha.refFaultParamDb.gui.infotools.InfoLabel;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.data.estimate.Estimate;
import org.opensha.data.Location;
import org.opensha.geo3d.SCEC_VDO;


/**
 * this class allows the user to view fault sections from the database 
 * @author vipingupta
 *
 */
public class ViewFaultSection extends JPanel implements ParameterChangeListener, ActionListener {
	
	// paramter to provide a list of all fault sections in the database
	private final static String FAULT_SECTION_PARAM_NAME = "Fault Section";
	private StringParameter faultSectionParam;
	private final static String ENTRY_DATE = "Entry Date";
	private InfoLabel entryDateLabel = new InfoLabel();
	private final static String SOURCE = "Source";
	private InfoLabel sourceLabel = new InfoLabel();
	private final static String  AVE_LONG_TERM_SLIP_RATE= "Ave Long Term Slip Rate";
	private InfoLabel slipRateLabel = new InfoLabel();
	private final static String  DIP= "Ave Dip";
	private InfoLabel dipLabel = new InfoLabel();
	private final static String  DIP_DIRECTION= "Dip Direction";
	private InfoLabel dipDirectionLabel = new InfoLabel();
	private final static String  RAKE= "Ave Rake";
	private InfoLabel rakeLabel = new InfoLabel();
	private final static String  UPPER_DEPTH= "Upper Seis Depth";
	private InfoLabel upperDepthLabel = new InfoLabel();
	private final static String  LOWER_DEPTH= "Lower Seis Depth";
	private InfoLabel lowerDepthLabel = new InfoLabel();
	private final static String  ASEISMIC_SLIP= "Aseismic Slip Factor";
	private InfoLabel aseismicSlipLabel = new InfoLabel();
	private final static String  FAULT_TRACE= "Fault Section Trace";
	private InfoLabel faultTraceLabel = new InfoLabel();
	private final static String QFAULT_ID = "QFault_Id";
	private InfoLabel qfaultLabel = new InfoLabel();
	private final static String  COMMENTS= "COMMENTS";
	private final static String PROB = "Prob";
	private final static String INFO = "Info";
	private InfoLabel commentsLabel = new InfoLabel();
	private GridBagLayout gridBagLayout = new GridBagLayout();
	private FaultSectionVer2_DB_DAO faultSectionDAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection); 
	private JButton editButton = new JButton("Edit");
	private FaultSectionVer2 selectedFaultSection;
	private final static String TITLE = "View Fault Section";
	private ConstrainedStringParameterEditor faultSectionParamEditor;
	
	public ViewFaultSection() {
		this(null);
	}
	
	public ViewFaultSection(String selectedFaultSectionNameId) {
		initGUI(); // intialize the GUI
		if(selectedFaultSectionNameId!=null) this.faultSectionParam.setValue(selectedFaultSectionNameId);
		refreshFaultSectionValues(); // fill the fault section values according to selected Fault Section
		//		 do not allow edit for non authenticated users
		if(SessionInfo.getContributor()==null) editButton.setEnabled(false);
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JScrollPane(this));
		frame.setTitle(TITLE);
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.show();

	}
	
	/**
	 * Set the selected fault section
	 * 
	 * @param selectedFaultSectionNameId
	 */
	public void setSelectedFaultSectionNameId(String selectedFaultSectionNameId) {
		faultSectionParam.setValue(selectedFaultSectionNameId);
		faultSectionParamEditor.refreshParamEditor();
		refreshFaultSectionValues(); // fill the fault section values according to selected Fault Section
		
	}
	
	private void initGUI() {
		// set the Layout
		this.setLayout(gridBagLayout);
		int pos = 0;
		
		// fault section name editor 
		add(makeFaultSectionNamesEditor(), new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		
		
		// JPanel to view QfaultId, entry date, source and comments
		JPanel idPanel = getInfoPanel();	
		add(idPanel, new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));	
		
		// fault trace label
		add(GUI_Utils.getPanel( this.faultTraceLabel, FAULT_TRACE), new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		// upper depth
		add(GUI_Utils.getPanel(upperDepthLabel, UPPER_DEPTH), new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		// lower depth
		add(GUI_Utils.getPanel(lowerDepthLabel, LOWER_DEPTH), new GridBagConstraints(0, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		pos = 0;
		
		//		 button to allow editing of Fault Section 
		add(this.editButton, new GridBagConstraints(1, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
		        new Insets(0, 0, 0, 0), 0, 0));
		editButton.addActionListener(this);
		
		
		// slip rate
		add(GUI_Utils.getPanel(slipRateLabel, AVE_LONG_TERM_SLIP_RATE), new GridBagConstraints(1, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		// dip Panel
		JPanel dipPanel = getDipPanel();
		add(dipPanel, new GridBagConstraints(1, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		// rake
		add(GUI_Utils.getPanel(rakeLabel, RAKE), new GridBagConstraints(1, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		
		// aseismic lsip factor
		add(GUI_Utils.getPanel(aseismicSlipLabel, ASEISMIC_SLIP), new GridBagConstraints(1, pos++, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
		        new Insets(0, 0, 0, 0), 0, 0));
	
		
	}
	
	public void actionPerformed(ActionEvent event) {
		EditFaultSection editFaultSection = new EditFaultSection(this.selectedFaultSection, this);
	}

	private JPanel getDipPanel() {
		JPanel dipPanel = GUI_Utils.getPanel(DIP);
		
		// dip
		dipPanel.add(dipLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		// dip direction
		dipPanel.add(dipDirectionLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
		        new Insets(0, 0, 0, 0), 0, 0));
		return dipPanel;
	}

	/**
	 * JPanel to view QfaultId, entry date, source and comments
	 * @return
	 */
	private JPanel getInfoPanel() {
		// JPanel to view QfaultId, entry date, source and comments
		JPanel idPanel = GUI_Utils.getPanel(INFO);
		
		// entry date
		idPanel.add(entryDateLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		// qfault Id
		idPanel.add(qfaultLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		// source 
		idPanel.add(sourceLabel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
		
		//		 comments
		idPanel.add(commentsLabel, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
		        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
		        new Insets(0, 0, 0, 0), 0, 0));
		return idPanel;
	}
	
	/**
	 * Make parameter editor to list all availble Fault Section names
	 * @return
	 */
	private ConstrainedStringParameterEditor makeFaultSectionNamesEditor() {
		ArrayList faultSectionsSummaryList = faultSectionDAO.getAllFaultSectionsSummary();
		ArrayList faultSectionsList = new ArrayList();
		for(int i=0; i<faultSectionsSummaryList.size(); ++i)
			faultSectionsList.add(((FaultSectionSummary)faultSectionsSummaryList.get(i)).getAsString());
		faultSectionParam = new StringParameter(FAULT_SECTION_PARAM_NAME, faultSectionsList, (String)faultSectionsList.get(0));
		faultSectionParam.addParameterChangeListener(this);
		faultSectionParamEditor = new ConstrainedStringParameterEditor(faultSectionParam);
		return faultSectionParamEditor;
	}
	
	/**
	 * This function is called whenever chooses a different fault section
	 */
	public void parameterChange(ParameterChangeEvent event) {
		refreshFaultSectionValues();
	}
	
	/**
	 * Refresh the fault section values based on fault section chosen by the user
	 * @param faultSectionName
	 */
	public void refreshFaultSectionValues() {
		String faultSectionName = (String)faultSectionParam.getValue();
		// get id of the selected fault section
		FaultSectionSummary faultSectionSummary = FaultSectionSummary.getFaultSectionSummary(faultSectionName);
		selectedFaultSection = faultSectionDAO.getFaultSection(faultSectionSummary.getSectionId());
		// Entry Date
		entryDateLabel.setTextAsHTML(ENTRY_DATE, selectedFaultSection.getEntryDate());
		// Source
		sourceLabel.setTextAsHTML(SOURCE, selectedFaultSection.getSource());
		// Slip Rate Est
		Estimate slipRateEst = null;
		if(selectedFaultSection.getAveLongTermSlipRateEst()!=null)
			slipRateEst = selectedFaultSection.getAveLongTermSlipRateEst().getEstimate();
		slipRateLabel.setTextAsHTML(slipRateEst, AVE_LONG_TERM_SLIP_RATE, PROB);
		
		// Dip Est
		Estimate dipEst = null;
		if(selectedFaultSection.getAveDipEst()!=null)
			dipEst = selectedFaultSection.getAveDipEst().getEstimate();
		dipLabel.setTextAsHTML(dipEst, DIP, PROB);
		
		// Dip Direction Label
		dipDirectionLabel.setTextAsHTML(DIP_DIRECTION, ""+selectedFaultSection.getDipDirection());
		
		// rake
		Estimate rakeEst = null;
		if(selectedFaultSection.getAveRakeEst()!=null) rakeEst = selectedFaultSection.getAveRakeEst().getEstimate();
		rakeLabel.setTextAsHTML(rakeEst, RAKE, PROB);
		
		// upper depth
		Estimate upperDepthEst = null;
		if(selectedFaultSection.getAveUpperDepthEst()!=null) upperDepthEst = selectedFaultSection.getAveUpperDepthEst().getEstimate();
		upperDepthLabel.setTextAsHTML(upperDepthEst, UPPER_DEPTH, PROB);
		
		// lower Depth
		Estimate lowerDepthEst = null;
		if(selectedFaultSection.getAveLowerDepthEst()!=null) lowerDepthEst = selectedFaultSection.getAveLowerDepthEst().getEstimate();
		lowerDepthLabel.setTextAsHTML(lowerDepthEst, LOWER_DEPTH, PROB);
		
		// aseismic slip factor
		Estimate aseismicSlipEst = null;
		if(selectedFaultSection.getAseismicSlipFactorEst()!=null) aseismicSlipEst = selectedFaultSection.getAseismicSlipFactorEst().getEstimate();
		aseismicSlipLabel.setTextAsHTML(aseismicSlipEst, ASEISMIC_SLIP, PROB);
		
		// fault trace
		FaultTrace faultTrace = selectedFaultSection.getFaultTrace();
		ArrayList locsAsString = new ArrayList();
		int numLocs = faultTrace.getNumLocations();
		for(int i=0; i<numLocs; ++i) {
			Location loc = faultTrace.getLocationAt(i);
			locsAsString.add(loc.getLongitude()+","+loc.getLatitude());
		}
		faultTraceLabel.setTextAsHTML(locsAsString);
		
		// comments
		commentsLabel.setTextAsHTML(COMMENTS, selectedFaultSection.getComments());
		
		// qfault Id
		qfaultLabel.setTextAsHTML(QFAULT_ID, selectedFaultSection.getQFaultId());
	}
	

}
