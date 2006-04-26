/**
 * 
 */
package org.opensha.refFaultParamDb.gui.addEdit;

import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

import org.opensha.param.StringParameter;
import org.opensha.param.editor.ConstrainedStringParameterEditor;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultModelDB_DAO;
import org.opensha.refFaultParamDb.dao.db.FaultModelSectionDB_DAO;
import org.opensha.refFaultParamDb.gui.infotools.SessionInfo;
import org.opensha.refFaultParamDb.vo.FaultModel;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.param.event.ParameterChangeEvent;


/**
 * This class allows users to add a new fault model and associate fault sections with the fault models
 * 
 * @author vipingupta
 *
 */
public class AddEditFaultModel extends JPanel implements ActionListener, ParameterChangeListener {
	
	private ArrayList faultModelsList;
	private ArrayList faultSectionsSummaryList;
	private  FaultModelDB_DAO faultModelDB_DAO = new FaultModelDB_DAO(DB_AccessAPI.dbConnection);
	private  FaultModelSectionDB_DAO faultModelSectionDB_DAO = new FaultModelSectionDB_DAO(DB_AccessAPI.dbConnection);
	private  FaultSectionVer2_DB_DAO faultSectionDB_DAO = new FaultSectionVer2_DB_DAO(DB_AccessAPI.dbConnection);
	private StringParameter faultModelsParam;
	private final static String AVAILABLE_FAULT_MODEL_PARAM_NAME = "Choose Fault Model";
	private ConstrainedStringParameterEditor faultModelsParamEditor;
	private JButton removeModelButton = new JButton("Remove Model");
	private JButton addModelButton = new JButton("Add Model");
	private JButton updateModelButton = new JButton("Update Model");
	private FaultModelTableModel tableModel;
	private FaultModelTable table;
	private final static String TITLE = "Fault Model";
	private final static String MSG_ADD_MODEL_SUCCESS = "Model Added Successfully";
	private final static String MSG_REMOVE_MODEL_SUCCESS = "Model Removed Successfully";
	private final static String MSG_UPDATE_MODEL_SUCCESS = "Model Updated Successfully";
	
	public AddEditFaultModel() {
		if(SessionInfo.getContributor()==null) this.addModelButton.setEnabled(false);
		else addModelButton.setEnabled(true);
//		 load alla fault sections
		loadAllFaultSectionsSummary();
		// load all the available fault models
		loadAllFaultModels();
		
		 // add action listeners to the button
		addActionListeners();
		// add components to the GUI
		setupGUI();
		JFrame frame = new JFrame();
		frame.setTitle(TITLE);
		frame.getContentPane().add(new JScrollPane(this));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.show();
	}
	
	/**
	 * Add the various buttons, JTable and paramters to the Panel
	 *
	 */
	private void setupGUI() {
		setLayout(new GridBagLayout());
		int yPos=1; // a list of fault models is present at yPos==0
		// remove model button
		add(this.removeModelButton,
	             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.NONE,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		// add model button
		add(this.addModelButton,
	             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.NONE,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		// add table
		add(new JScrollPane(this.table),
	             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		// update button
		add(this.updateModelButton,
	             new GridBagConstraints(0, yPos++, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.NONE,
	                                    new Insets(0, 0, 0, 0), 0, 0));
	}
	
	/**
	 * Add action listeners for buttons
	 *
	 */
	private void addActionListeners() {
		removeModelButton.addActionListener(this);
		addModelButton.addActionListener(this);
		updateModelButton.addActionListener(this);
	}
	
	/**
	 * This function is called when a Button is clicked in this panel, then change the selected fault sections
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		try {
			if(source==this.addModelButton) { // add a new model
				// show a Window asking for new model name
				String faultModelName = JOptionPane.showInputDialog(this, "Enter Fault Model Name");
				if(faultModelName==null) return;
				else addFaultModelToDB(faultModelName);
				JOptionPane.showMessageDialog(this, MSG_ADD_MODEL_SUCCESS);
				loadAllFaultModels();
			
			} else if(source == this .removeModelButton) { // remove the model from the database
				String selectedFaultModel = (String)this.faultModelsParam.getValue();
				int faultModelId = this.getFaultModelId(selectedFaultModel);
				this.faultModelDB_DAO.removeFaultModel(faultModelId);
				JOptionPane.showMessageDialog(this, MSG_REMOVE_MODEL_SUCCESS);
				loadAllFaultModels();
			
			} else if(source == this.updateModelButton)  { // update the model in the database
				ArrayList faultSectionIdList = this.tableModel.getSelectedFaultSectionsId();
				String selectedFaultModel = (String)this.faultModelsParam.getValue();
				int faultModelId = this.getFaultModelId(selectedFaultModel);
				this.faultModelSectionDB_DAO.addFaultModelSections(faultModelId, faultSectionIdList);
				JOptionPane.showMessageDialog(this, MSG_UPDATE_MODEL_SUCCESS);
			}
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}
	
	/**
	 * Add a fault model name to the database
	 * @param faultModelName
	 */
	private void addFaultModelToDB(String faultModelName) {
		FaultModel faultModel = new FaultModel();
		faultModel.setFaultModelName(faultModelName);
		 this.faultModelDB_DAO.addFaultModel(faultModel);
	}
	
	/**
	 * When a user selects a different fault model
	 */
	public void parameterChange(ParameterChangeEvent event) {
		// whenever chosen fault model changes, also change the displayed fault sections
		setFaultSectionsBasedOnFaultModel();
	}
	
	/**
	 * Set the fault sections based on selected fault model
	 *
	 */
	private void setFaultSectionsBasedOnFaultModel() {
		String selectedFaultModel  = (String)this.faultModelsParam.getValue();
		// find the fault model id
		int faultModelId=getFaultModelId(selectedFaultModel);
		// get all the fault sections within this fault model
		ArrayList faultSectionIdList = this.faultModelSectionDB_DAO.getFaultSectionIdList(faultModelId);
		
		//deselect all the check boxes in the table
		for(int i=0; i<this.tableModel.getRowCount(); ++i)
			tableModel.setValueAt(new Boolean(false), i, 0);
		
		// only select the check boxes which are part of this fault model
		int numSectionsInFaultModel = faultSectionIdList.size();
		for(int i=0; i<numSectionsInFaultModel; ++i) {
			Integer faultSectionId = (Integer)faultSectionIdList.get(i);
			tableModel.setSelected(faultSectionId.intValue(), true);
		}
		tableModel.fireTableDataChanged();
	}

	/**
	 * Get the fault model Id based on Fault model name
	 * @param selectedFaultModel
	 * @return
	 */
	private int getFaultModelId(String selectedFaultModel) {
		for(int i=0; i<this.faultModelsList.size(); ++i) {
			FaultModel faultModel = (FaultModel)faultModelsList.get(i);
			if(faultModel.getFaultModelName().equalsIgnoreCase(selectedFaultModel)) {
				return faultModel.getFaultModelId();
			}
		}
		return -1;
	}
	
	/**
	 * Load All the available fault models
	 * 
	 */
	private void loadAllFaultModels() {
		faultModelsList = faultModelDB_DAO.getAllFaultModels();
		if(faultModelsParamEditor!=null) this.remove(faultModelsParamEditor);
		this.updateUI();
		// make a list of fault model names
		ArrayList faultModelNames = new ArrayList();
		for(int i=0; i<faultModelsList.size(); ++i) {
			faultModelNames.add(((FaultModel)faultModelsList.get(i)).getFaultModelName());
		}
		// make parameter and editor
		if(faultModelNames==null || faultModelNames.size()==0)  {
			this.updateModelButton.setEnabled(false);
			this.removeModelButton.setEnabled(false);
			return;
		}
		
		// enable the add, remove and update button only if user has read/write access
		if(SessionInfo.getContributor()!=null) {
			this.updateModelButton.setEnabled(true);
			this.removeModelButton.setEnabled(true);
		}
		faultModelsParam = new StringParameter(AVAILABLE_FAULT_MODEL_PARAM_NAME,faultModelNames, (String)faultModelNames.get(0) );
		faultModelsParam.addParameterChangeListener(this);
		setFaultSectionsBasedOnFaultModel();
		faultModelsParamEditor = new ConstrainedStringParameterEditor(faultModelsParam);
		// fault model selection editor
		add(this.faultModelsParamEditor,
	             new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
	                                    , GridBagConstraints.CENTER,
	                                    GridBagConstraints.BOTH,
	                                    new Insets(0, 0, 0, 0), 0, 0));
		this.updateUI();
	}
	
	
	/**
	 * Load faultsectionnames and ids of all the fault sections. Also make the TableModel and Table to view the fault sections
	 *
	 */
	private void loadAllFaultSectionsSummary() {
		faultSectionsSummaryList = faultSectionDB_DAO.getAllFaultSectionsSummary();
		tableModel = new FaultModelTableModel(faultSectionsSummaryList);
		table = new FaultModelTable(tableModel);
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new AddEditFaultModel();
		// TODO Auto-generated method stub
	}

}
