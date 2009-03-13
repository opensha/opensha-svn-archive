package org.opensha.data.siteType.gui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.OrderedSiteDataProviderList;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.gui.beans.OrderedSiteDataGUIBean;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.mapping.gmtWrapper.GMT_MapGenerator;
import org.opensha.mapping.gui.beans.GMT_MapGuiBean;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;

public class SiteDataMapApplet extends Applet implements ActionListener, ListSelectionListener {
	
	private OrderedSiteDataGUIBean dataBean;
	private GMT_MapGuiBean mapBean;
	
	private JButton mapButton = new JButton("Create Map");
	private JButton regionButton = new JButton("Set Region from Data");
	
	public SiteDataMapApplet() {
		dataBean = new OrderedSiteDataGUIBean(OrderedSiteDataProviderList.createSiteDataMapProviders());
		dataBean.addListSelectionListener(this);
		mapBean = new GMT_MapGuiBean();
		mapBean.getParameterList().getParameter(GMT_MapGenerator.LOG_PLOT_NAME).setValue(new Boolean(false));
		mapBean.getParameterList().getParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME).setValue(new Boolean(false));
		mapBean.getParameterList().getParameter(
				GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME).setValue(GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapBean.getParameterList().getParameter(
				GMT_MapGenerator.COAST_PARAM_NAME).setValue(GMT_MapGenerator.COAST_DRAW);
		mapBean.refreshParamEditor();
		
		this.setLayout(new BorderLayout());
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(mapButton);
		bottomPanel.add(regionButton);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		
		centerPanel.add(dataBean);
		centerPanel.add(mapBean);
		this.add(centerPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		mapButton.addActionListener(this);
		regionButton.addActionListener(this);
		
		this.setPreferredSize(new Dimension(900, 600));
		valueChanged(null);
//		this.setSize(500, 800);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mapButton) {
			System.out.println("Making a map...");
			ArrayList<SiteDataAPI<?>> providers = dataBean.getSelectedProviders();
			if (providers.size() == 0) {
				System.out.println("No data provider selected!");
				return;
			}
			for (SiteDataAPI<?> provider : providers) {
				SiteDataAPI<Double> doubProvider = (SiteDataAPI<Double>)provider;
				try {
					ParameterAPI customParam = mapBean.getParameterList().getParameter(
							GMT_MapGenerator.CUSTOM_SCALE_LABEL_PARAM_CHECK_NAME);
					// if the user didn't specify a custom one, then do it for them
					boolean custom = (Boolean)customParam.getValue();
					if (!custom) {
						customParam.setValue(new Boolean(true));
						String label = doubProvider.getName() + " -  " + doubProvider.getType();
						
						if (label.length() > 20)
							label = doubProvider.getShortName() + " - " + doubProvider.getType();
						if (label.length() > 20)
							label = doubProvider.getType();
						System.out.println("Label: " + label);
						label = "'" + label + "'";
						mapBean.getParameterList().getParameter(
								GMT_MapGenerator.SCALE_LABEL_PARAM_NAME).setValue(label);
					}
					
					EvenlyGriddedRectangularGeographicRegion region = mapBean.getEvenlyGriddedGeographicRegion();
					LocationList locs = region.getGridLocationsList();
					ArrayList<Double> xVals = new ArrayList<Double>();
					ArrayList<Double> yVals = new ArrayList<Double>();
					
					for (Location loc : locs) {
						// for some reason x and y are reversed here, but that's what the servlet expects
						yVals.add(loc.getLongitude());
						xVals.add(loc.getLatitude());
					}
					
					ArrayList<Double> zVals = doubProvider.getValues(locs);
					
					ArbDiscretizedXYZ_DataSet xyz = new ArbDiscretizedXYZ_DataSet(xVals, yVals, zVals);
					
					String meta = doubProvider.getName();
					
					mapBean.makeMap(xyz, meta);
					
					customParam.setValue(custom);
				} catch (RegionConstraintException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (e.getSource() == regionButton) {
			SiteDataAPI<?> provider = dataBean.getSelectedProvider();
			if (provider == null) {
				System.out.println("No data provider selected!");
				return;
			}
			GeographicRegion region = provider.getApplicableRegion();
			
			ParameterList paramList = mapBean.getParameterList();
			paramList.getParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME).setValue(new Double(region.getMinLat()));
			paramList.getParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME).setValue(new Double(region.getMaxLat()));
			paramList.getParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME).setValue(new Double(region.getMinLon()));
			paramList.getParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME).setValue(new Double(region.getMaxLon()));
			mapBean.refreshParamEditor();
		}
	}
	
	/**
	 * Main class for running this as a regular java application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		SiteDataMapApplet applet = new SiteDataMapApplet();
		frame.setContentPane(applet);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(applet.getPreferredSize());
		frame.setVisible(true);
	}

	public void valueChanged(ListSelectionEvent e) {
		boolean selected = dataBean.isSelected();
		mapButton.setEnabled(selected);
		regionButton.setEnabled(selected);
	}

}
