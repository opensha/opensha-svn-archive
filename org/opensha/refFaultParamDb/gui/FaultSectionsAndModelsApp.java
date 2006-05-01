/**
 * 
 */
package org.opensha.refFaultParamDb.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;

import org.opensha.refFaultParamDb.gui.view.ViewFaultSection;
import org.opensha.refFaultParamDb.gui.addEdit.deformationModel.EditDeformationModel;
import org.opensha.refFaultParamDb.gui.addEdit.faultModel.AddEditFaultModel;

/**
 *  This class creates the GUI to allow the user to view/edit fault sections, fault models and deformation models
 * 
 * @author vipingupta
 *
 */
public class FaultSectionsAndModelsApp extends JFrame {
	private JTabbedPane tabbedPane = new JTabbedPane();
	private final static String FAULT_SECTION = "Fault Section";
	private final static String FAULT_MODEL = "Fault Model";
	private final static String DEFORMATION_MODEL = "Deformation Model";
	
	public FaultSectionsAndModelsApp() {
		tabbedPane.addTab(FAULT_SECTION, new JScrollPane(new ViewFaultSection()));
		tabbedPane.addTab(FAULT_MODEL, new JScrollPane(new AddEditFaultModel()));
		tabbedPane.addTab(DEFORMATION_MODEL, new JScrollPane(new EditDeformationModel()));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.show();
	}

}
