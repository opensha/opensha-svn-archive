package org.opensha.sha.calc.hazardMap.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.opensha.sha.calc.hazardMap.servlet.StatusServletAccessor;

public class DataSetSelector extends JPanel implements ActionListener {
	
	StatusServletAccessor statusAccessor = new StatusServletAccessor(StatusServletAccessor.SERVLET_URL);
	
	String datasets[][] = null;
	
	JList list = new JList();
	
	JScrollPane listScroller = new JScrollPane(list);
	
	JPanel topPanel = new JPanel(new BorderLayout());
	
	JLabel selectLabel = new JLabel("Available datasets:");
	JButton reloadButton = new JButton("Reload");
	
	public DataSetSelector() {
		super(new BorderLayout());
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
//		listScroller.setPreferredSize(new Dimension(250, 80));
		
		this.loadDatasets();
		this.populateDatasetChooser();
		
		reloadButton.addActionListener(this);
		
		topPanel.add(selectLabel, BorderLayout.WEST);
		topPanel.add(reloadButton, BorderLayout.EAST);
		
		this.add(topPanel, BorderLayout.NORTH);
		this.add(list, BorderLayout.CENTER);
	}
	
	private void loadDatasets() {
		try {
			datasets = statusAccessor.getDatasetIDs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void populateDatasetChooser() {
		String names[] = new String[datasets.length];
		for (int i=0; i<datasets.length; i++) {
			String id = datasets[i][0];
			String name = datasets[i][1];
			
			if (id.equals(name))
				names[i] = id;
			else
				names[i] = name + " (" + id + ")";
		}
		list.setListData(names);
	}
	
	public String[] getSelectedID() {
		if (this.list.getModel().getSize() <= 0)
			return null;
		
		int index = this.list.getSelectedIndex();
		
		if (index < 0)
			return null;
		
		return datasets[index];
	}
	
	public Step createStep() {
		return new Step(this, "Select Data Set");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(reloadButton)) {
			this.loadDatasets();
			this.populateDatasetChooser();
		}
	}
}
