package org.opensha.cybershake.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.cybershake.db.CybershakeHazardCurveRecord;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.HazardCurve2DB;

public class HazardCurveGUI extends JFrame implements ActionListener, ListSelectionListener {
	
	DBAccess db;
	
	HazardCurve2DB curve2db;
	
	JPanel mainPanel = new JPanel(new BorderLayout());
	
	JPanel bottomPanel = new JPanel();
	
	JButton deleteButton = new JButton("Delete Curve(s)");
	JButton reloadButton = new JButton("Reload Curves");
	
	HazardCurveTableModel model;
	JTable table;
	
	public HazardCurveGUI(DBAccess db) {
		super();
		
		this.db = db;
		curve2db = new HazardCurve2DB(db);
		
		model = new HazardCurveTableModel(db);
		
		table = new JTable(model);
		
		JScrollPane scrollpane = new JScrollPane(table);
		
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		table.getSelectionModel().addListSelectionListener(this);
		deleteButton.addActionListener(this);
		deleteButton.setEnabled(false);
		
		reloadButton.addActionListener(this);
		
		bottomPanel.add(reloadButton);
		bottomPanel.add(new JSeparator());
		bottomPanel.add(deleteButton);
		
		mainPanel.add(scrollpane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		this.setSize(900, 600);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public static void main(String args[]) throws IOException {
		HazardCurveGUI gui = new HazardCurveGUI(Cybershake_OpenSHA_DBApplication.getAuthenticatedDBAccess(true));
		
		gui.setVisible(true);
		gui.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reloadButton) {
			this.model.reloadCurves();
		} else if (e.getSource() == deleteButton) {
			ListSelectionModel lsm = table.getSelectionModel();
			
			ArrayList<Integer> rows = new ArrayList<Integer>();
			
			for (int i=lsm.getMinSelectionIndex(); i<=lsm.getMaxSelectionIndex(); i++) {
				if (lsm.isSelectedIndex(i)) {
					rows.add(i);
				}
			}
			
			for (int row : rows) {
				CybershakeHazardCurveRecord curve = model.getCurveAtRow(row);
				System.out.println("Deleting curve " + curve.getCurveID());
				
				boolean success = this.curve2db.deleteHazardCurve(curve.getCurveID());
				if (!success)
					System.err.println("Error deleting curve " + curve.getCurveID());
			}
			model.reloadCurves();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// if it's in the middle of a change, ignore
		if (e.getValueIsAdjusting())
			return;
		
		if (table.getSelectionModel().isSelectionEmpty()) {
			deleteButton.setEnabled(false);
		} else {
			deleteButton.setEnabled(true);
		}
	}

}
