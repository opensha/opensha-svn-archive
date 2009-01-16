package org.opensha.cybershake.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.CybershakeSiteManager;
import org.opensha.cybershake.db.DBAccess;

public class SitesGUI extends JFrame implements ActionListener, ListSelectionListener {
	
	public static final int MAX_SHORT_NAME_CHARS = 5;
	
	DBAccess db;
	
	JPanel mainPanel = new JPanel(new BorderLayout());
	
	JPanel bottomPanel = new JPanel();
	
	JButton editButton = new JButton("Edit Site");
	JButton insertButton = new JButton("Add Site");
	JButton deleteButton = new JButton("Delete Site(s)");
	JButton reloadButton = new JButton("Reload Sites");
	
	SitesTableModel model;
	JTable table;
	
	private boolean readOnly = false;

	public SitesGUI(DBAccess db) {
		super("CyberShake Sites");
		
		this.db = db;
		this.readOnly = db.isReadOnly();
		
		model = new SitesTableModel(db);
		
		table = new JTable(model);
		
		JScrollPane scrollpane = new JScrollPane(table);
		
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		table.getSelectionModel().addListSelectionListener(this);
		
		reloadButton.addActionListener(this);
		
		insertButton.addActionListener(this);
		insertButton.setEnabled(!readOnly);
		
		editButton.addActionListener(this);
		editButton.setEnabled(false);
		
		deleteButton.addActionListener(this);
		deleteButton.setEnabled(false);
		
		bottomPanel.add(reloadButton);
		bottomPanel.add(new JSeparator());
		bottomPanel.add(insertButton);
		bottomPanel.add(new JSeparator());
		bottomPanel.add(editButton);
		bottomPanel.add(new JSeparator());
		bottomPanel.add(deleteButton);
		
		mainPanel.add(scrollpane, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		
		this.setSize(900, 600);
		
		this.setLocationRelativeTo(null);
		
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reloadButton) {
			model.reloadSites();
		} else if (e.getSource() == deleteButton) {
			ListSelectionModel lsm = table.getSelectionModel();
			
			ArrayList<Integer> rows = new ArrayList<Integer>();
			
			for (int i=lsm.getMinSelectionIndex(); i<=lsm.getMaxSelectionIndex(); i++) {
				if (lsm.isSelectedIndex(i)) {
					rows.add(i);
				}
			}
			
			for (int row : rows) {
				deleteSite(model.getSiteAtRow(row));
				model.reloadSites();
			}
		} else if (e.getSource() == insertButton) {
			SingleSiteAddEditGUI add = new SingleSiteAddEditGUI(db, model, null);
			
			add.setVisible(true);
		} else if (e.getSource() == editButton) {
			ListSelectionModel lsm = table.getSelectionModel();
			CybershakeSite site = model.getSiteAtRow(lsm.getMinSelectionIndex());
			SingleSiteAddEditGUI add = new SingleSiteAddEditGUI(db, model, site);
			
			add.setVisible(true);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		// if it's in the middle of a change, ignore
		if (e.getValueIsAdjusting())
			return;
		
		ListSelectionModel lsm = table.getSelectionModel();
		boolean isSingle = lsm.getMinSelectionIndex() == lsm.getMaxSelectionIndex();
		
		if (lsm.isSelectionEmpty()) {
			deleteButton.setEnabled(false);
			editButton.setEnabled(false);
		} else {
			deleteButton.setEnabled(!readOnly);
			editButton.setEnabled(!readOnly && isSingle);
		}
	}
	
	public boolean deleteSite(CybershakeSite site) {
		// make sure they really want to do this!
		
		String title = "Really delete CyberShake Site?";
		String message = "Are you sure that you want to delete site: " + site.getFormattedName() + "?\n\n" +
				"This will also delete the following for this site:\n" +
				" * Peak Amplitudes\n" +
				" * Hazard Curves\n" +
				" * Regional Bounds\n" +
				" * Site Ruptures\n\n" +
				"This cannot be undone!!!";
		int response = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
		
		if (response != JOptionPane.YES_OPTION)
			return false;
		
		return CybershakeSiteManager.deleteCybershakeSite(db, site);
	}

}
