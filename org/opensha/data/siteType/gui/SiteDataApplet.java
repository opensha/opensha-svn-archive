package org.opensha.data.siteType.gui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opensha.data.Location;
import org.opensha.data.siteType.OrderedSiteDataProviderList;
import org.opensha.data.siteType.SiteDataValue;
import org.opensha.data.siteType.gui.beans.OrderedSiteDataGUIBean;
import org.opensha.exceptions.InvalidRangeException;

public class SiteDataApplet extends Applet implements ActionListener {
	
	private OrderedSiteDataGUIBean bean;
	
	private JTextField latField = new JTextField(8);
	private JTextField lonField = new JTextField(8);
	
	private JButton prefButton = new JButton("View Preffered Data");
	private JButton allButton = new JButton("View All Available Data");
	
	public SiteDataApplet() {
		bean = new OrderedSiteDataGUIBean(OrderedSiteDataProviderList.createSiteDataProviderDefaults());
		
		this.setLayout(new BorderLayout());
		
		JPanel locationPanel = new JPanel();
		locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
		locationPanel.add(new JLabel("Latitude: "));
		locationPanel.add(latField);
		locationPanel.add(new JLabel("Longitude: "));
		locationPanel.add(lonField);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(prefButton);
		buttonPanel.add(allButton);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(locationPanel, BorderLayout.NORTH);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.add(bean, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		prefButton.addActionListener(this);
		allButton.addActionListener(this);
		
		this.setPreferredSize(new Dimension(500, 600));
//		this.setSize(500, 800);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == prefButton || e.getSource() == allButton) {
			boolean all = e.getSource() == allButton;
			
			Location loc;
			
			try {
				double lat = Double.parseDouble(latField.getText());
				double lon = Double.parseDouble(lonField.getText());
				
				loc = new Location(lat, lon);
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "Please enter a valid location!", "Invalid location!",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			OrderedSiteDataProviderList list = this.bean.getProviderList();
			ArrayList<SiteDataValue<?>> datas;
			if (all)
				datas = list.getAllAvailableData(loc);
			else
				datas = list.getBestAvailableData(loc);
			OrderedSiteDataGUIBean.showDataDisplayDialog(datas, this);
		}
	}
	
	/**
	 * Main class for running this as a regular java application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		SiteDataApplet applet = new SiteDataApplet();
		frame.setContentPane(applet);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(applet.getPreferredSize());
		frame.setVisible(true);
	}

}
