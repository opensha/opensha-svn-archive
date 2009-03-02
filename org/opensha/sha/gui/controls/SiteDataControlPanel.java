package org.opensha.sha.gui.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.opensha.data.Location;
import org.opensha.data.siteType.OrderedSiteDataProviderList;
import org.opensha.data.siteType.SiteDataValue;
import org.opensha.data.siteType.gui.beans.OrderedSiteDataGUIBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeEvent;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeListener;
import org.opensha.sha.util.SiteTranslator;

public class SiteDataControlPanel extends JFrame implements AttenuationRelationshipChangeListener, ActionListener {
	
	private IMR_GuiBean imrGuiBean;
	private Site_GuiBean siteGuiBean;
	private OrderedSiteDataGUIBean dataGuiBean;
	
	private AttenuationRelationshipAPI attenRel;
	
	private JPanel mainPanel = new JPanel(new BorderLayout());
	
	private JButton setButton = new JButton("Set IMR Params");
	private JButton viewButton = new JButton("View Data");
	
	private SiteTranslator trans = null;
	
	public SiteDataControlPanel(Component parent, IMR_GuiBean imrGuiBean,
            Site_GuiBean siteGuiBean) {
		this.imrGuiBean = imrGuiBean;
		this.siteGuiBean = siteGuiBean;
		
		attenRel = imrGuiBean.getSelectedIMR_Instance();
		imrGuiBean.addAttenuationRelationshipChangeListener(this);
		
		dataGuiBean = new OrderedSiteDataGUIBean(OrderedSiteDataProviderList.createCachedSiteDataProviderDefaults(), attenRel);
		
		viewButton.addActionListener(this);
		setButton.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		bottomPanel.add(setButton);
		bottomPanel.add(viewButton);
		
		mainPanel.add(dataGuiBean, BorderLayout.CENTER);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
		this.setSize(OrderedSiteDataGUIBean.width, 600);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void attenuationRelationshipChange(AttenuationRelationshipChangeEvent event) {
		dataGuiBean.setAttenuationRelationship(event.getNewAttenRel());
	}
	
	public void setSiteParams() {
		ArrayList<SiteDataValue<?>> data = loadData();
		
		if (data == null || data.size() == 0)
			return;
		
		for (SiteDataValue<?> val : data) {
			System.out.println(val);
		}
		
		if (trans == null) {
			trans = new SiteTranslator();
		}
		
		trans.setAllSiteParams(imrGuiBean.getSelectedIMR_Instance(), data);
		
		this.siteGuiBean.getParameterListEditor().refreshParamEditor();
		this.dispose();
	}
	
	public void displayData(ArrayList<SiteDataValue<?>> datas) {
		OrderedSiteDataGUIBean.showDataDisplayDialog(datas, this);
	}
	
	private ArrayList<SiteDataValue<?>> loadData() {
		return loadData(false);
	}
	
	private ArrayList<SiteDataValue<?>> loadData(boolean all) {
		OrderedSiteDataProviderList list = dataGuiBean.getProviderList();
		Location loc = siteGuiBean.getSite().getLocation();
		
		if (all)
			return list.getAllAvailableData(loc);
		else
			return list.getBestAvailableData(loc);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == setButton) {
			this.setSiteParams();
		} else if (e.getSource() == viewButton) {
			ArrayList<SiteDataValue<?>> data = loadData(true);
			this.displayData(data);
		}
	}
}
