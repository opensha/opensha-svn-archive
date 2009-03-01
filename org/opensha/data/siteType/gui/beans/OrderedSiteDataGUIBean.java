package org.opensha.data.siteType.gui.beans;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensha.data.siteType.OrderedSiteDataProviderList;
import org.opensha.data.siteType.SiteDataAPI;
import org.opensha.data.siteType.SiteDataValue;
import org.opensha.data.siteType.util.SiteDataTypeParameterNameMap;
import org.opensha.param.ParameterList;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.util.SiteTranslator;

public class OrderedSiteDataGUIBean extends JPanel implements ActionListener, ListSelectionListener {
	
	private OrderedSiteDataProviderList list;
	
	private AttenuationRelationshipAPI attenRel;
	private SiteDataTypeParameterNameMap map = SiteTranslator.DATA_TYPE_PARAM_NAME_MAP;
	
	// list editing buttons
	private JButton upButton = new JButton("Up");
	private JButton downButton = new JButton("Down");
	private JButton enableButton = new JButton("Enable");
	private JButton disableButton = new JButton("Disable");
	
	private JButton helpButton = new JButton("Help");
	
	private SiteDataAPI<?> currentData;
	
	private JTextArea metadataArea = new JTextArea(8, 50);
	
	private JPanel dataPanel = new JPanel(new BorderLayout());
	
	private JList dataList;
	
	public static int width = 400;
	
	private ParameterListEditor paramEdit = null;
	
	private SiteDataCellRenderer cellRenderer = null;
	
	public OrderedSiteDataGUIBean(OrderedSiteDataProviderList list) {
		this(list, null);
	}
	
	public OrderedSiteDataGUIBean(OrderedSiteDataProviderList list, AttenuationRelationshipAPI attenRel) {
		super(new BorderLayout());
		
		this.attenRel = attenRel;
		this.list = list;
		
		cellRenderer = new SiteDataCellRenderer(list.size());
		
		dataList = new JList();
		dataList.setLayoutOrientation(JList.VERTICAL);
//		dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dataList.addListSelectionListener(this);
		dataList.setCellRenderer(cellRenderer);
		
		dataList.setSelectedIndex(-1);
		currentData = list.getProvider(0);
		
		updateList();
		updateDataGUI();
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
		Font titleFont = new Font("Title", Font.BOLD, 20);
		Font subtitleFont = new Font("Title", Font.ITALIC, 14);
		JLabel title = new JLabel("Available Data Sources");
		title.setFont(titleFont);
		JLabel subtitle = new JLabel("Sorted by priority, colored by data type.");
		subtitle.setFont(subtitleFont);
		
		northPanel.add(title);
		northPanel.add(subtitle);
		
		JPanel selectorPanel = new JPanel(new BorderLayout());
		selectorPanel.add(northPanel, BorderLayout.NORTH);
		selectorPanel.add(dataList, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel leftButtonPanel = new JPanel();
		leftButtonPanel.setLayout(new BoxLayout(leftButtonPanel, BoxLayout.X_AXIS));
		JPanel rightButtonPanel = new JPanel();
		rightButtonPanel.setLayout(new BoxLayout(rightButtonPanel, BoxLayout.X_AXIS));
		leftButtonPanel.add(enableButton);
		leftButtonPanel.add(disableButton);
		rightButtonPanel.add(upButton);
		rightButtonPanel.add(downButton);
		rightButtonPanel.add(new JSeparator(JSeparator.VERTICAL));
		rightButtonPanel.add(helpButton);
		buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
		buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
		
		upButton.addActionListener(this);
		downButton.addActionListener(this);
		enableButton.addActionListener(this);
		disableButton.addActionListener(this);
		helpButton.addActionListener(this);
		
		enableButton.setEnabled(false);
		disableButton.setEnabled(false);
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		
		selectorPanel.add(buttonPanel, BorderLayout.SOUTH);
		metadataArea.setLineWrap(true);
		metadataArea.setWrapStyleWord(true);
		
		JScrollPane scroll = new JScrollPane(metadataArea);
		scroll.setSize(width, 300);
		scroll.setMaximumSize(new Dimension(width, 300));
		
		dataPanel.add(scroll, BorderLayout.NORTH);
		
		this.add(selectorPanel, BorderLayout.NORTH);
		this.add(dataPanel, BorderLayout.CENTER);
	}
	
	private void updateList() {
		ArrayList<String> names = new ArrayList<String>();
		
		int num = 1;
		for (int i=0; i<list.size(); i++) {
			SiteDataAPI<?> provider = list.getProvider(i);
			cellRenderer.setType(i, provider.getType());
			if (attenRel == null || map.isTypeApplicable(provider.getType(), attenRel)) {
				if (list.isEnabled(i)) {
					names.add((num) + ". " + provider.getName() + " (" + provider.getType() + ")");
					num++;
					cellRenderer.setEnabled(i, true);
					cellRenderer.setApplicable(i, true);
				} else {
					names.add("<disabled> " + provider.getName() + " (" + provider.getType() + ")");
					cellRenderer.setEnabled(i, false);
					cellRenderer.setApplicable(i, true);
				}
			} else {
				list.setEnabled(i, false);
				cellRenderer.setEnabled(i, false);
				cellRenderer.setApplicable(i, false);
				names.add("<not applicable> " + provider.getName() + " (" + provider.getType() + ")");
			}
		}
		
		dataList.setListData(names.toArray());
		dataList.validate();
	}
	
	public void setAttenuationRelationship(AttenuationRelationshipAPI attenRel) {
		this.attenRel = attenRel;
		for (int i=0; i<list.size(); i++)
			list.setEnabled(i, true);
		this.updateList();
		dataList.setSelectedIndex(-1);
		currentData = list.getProvider(0);
		updateDataGUI();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == upButton || e.getSource() == downButton) {
			boolean up = e.getSource() == upButton;
			
			int indexes[] = this.dataList.getSelectedIndices();
			int selected[] = new int[indexes.length];
			int maxIndex = list.size() - 1;
			
			ArrayList<SiteDataAPI<?>> toMove = new ArrayList<SiteDataAPI<?>>();
			for (int index : indexes)
				toMove.add(list.getProvider(index));
			
			for (int i=0; i<toMove.size(); i++) {
				int j;
				if (up)
					j = i;
				else
					j = toMove.size() - 1 - i;
				
				SiteDataAPI<?> data = toMove.get(j);
				
				int index = list.getIndexOf(data);
				if (up) {
					if (index == 0) {
						boolean enabled = list.isEnabled(0);
						list.remove(0);
						list.add(data);
						list.setEnabled(maxIndex, enabled);
						continue;
					}
					this.list.promote(index);
				} else {
					if (index == maxIndex) {
						boolean enabled = list.isEnabled(maxIndex);
						list.remove(maxIndex);
						list.add(0, data);
						list.setEnabled(0, enabled);
						continue;
					}
					this.list.demote(index);
				}
			}
			for (int i=0; i<toMove.size(); i++) {
				selected[i] = list.getIndexOf(toMove.get(i));
			}
			this.updateList();
			this.dataList.setSelectedIndices(selected);
		} else if (e.getSource() == enableButton || e.getSource() == disableButton) {
			boolean enabled = e.getSource() == enableButton;
			
			int indexes[] = this.dataList.getSelectedIndices();
			for (int i=0; i<indexes.length; i++) {
				int index = indexes[i];
				this.list.setEnabled(index, enabled);
			}
			this.updateList();
			this.dataList.setSelectedIndices(indexes);
		} else if (e.getSource() == helpButton) {
			showHelp();
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		int index = dataList.getSelectedIndex();
		
		boolean somethingSelected = index >= 0;
		
		if (!somethingSelected) {
			index = 0;
		}
		
		enableButton.setEnabled(somethingSelected);
		disableButton.setEnabled(somethingSelected);
		upButton.setEnabled(somethingSelected);
		downButton.setEnabled(somethingSelected);
		
		SiteDataAPI<?> newData = list.getProvider(index);
		if (newData != currentData) {
			currentData = newData;
			updateDataGUI();
		}
	}
	
	private void showHelp() {
		String help = "This is used to set syte type information for a site or gridded region." +
				"The list shows all of the available sources of site data, " +
				"in order of priority. When setting site parameters for the " +
				"given Attenuation Relationship, the highest priority applicable " +
				"data source with valid data for the given location will be used. " +
				"\n\n" +
				"For example, if a Vs30 value and a Wills Site classification are " +
				"available, and the Attenuation Relationship uses Vs30, then the Vs30 " +
				"parameter will be set by whichever has higher priority. If the Vs30 " +
				"data source is higher priority, and is a valid value (>0 and not NaN), " +
				"that will be used. If the Wills Site Classification data source has " +
				"higher priority, it will be translated  to a Vs30 value and used." +
				"\n\n" +
				"You can adjust priority for data sources by selecting the source in " +
				"the list, and pressing the Up/Down button. Additionally, data sources" +
				"can be enabled or disabled with the Enable/Disable buttons.";
		
		JTextArea area = new JTextArea(15, 40);
		
		area.setText(help);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(area);
		area.setCaretPosition(0);
		
		JOptionPane.showMessageDialog(this, scroll, "Site Data Selection Help", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void updateDataGUI() {
		String meta = "Name: " + currentData.getName() + "\n";
		meta += "Type: " + currentData.getType() + "\n";
		meta += "Type Flag: " + currentData.getTypeFlag() + "\n";
		meta += "Resolution: " + currentData.getResolution() + " degrees\n\n";
		meta += currentData.getMetadata() + "\n";
		metadataArea.setText(meta);
		
		metadataArea.setCaretPosition(0);
		
		ParameterList paramList = currentData.getAdjustableParameterList();
		if (paramEdit != null) {
			this.dataPanel.remove(paramEdit);
		}
		paramEdit = currentData.getParameterListEditor();
		this.dataPanel.add(paramEdit, BorderLayout.CENTER);
		
		this.dataPanel.validate();
	}
	
	public OrderedSiteDataProviderList getProviderList() {
		return list;
	}
	
	public static Component getDataDisplayComponent(ArrayList<SiteDataValue<?>> datas) {
		String text;
		
		if (datas == null || datas.size() == 0) {
			text = "No data available";
		} else {
			text = "";
			for (SiteDataValue<?> data : datas) {
				if (text.length() > 0)
					text += "\n\n";
				text += "Source: " + data.getSourceName() + "\n";
				text += "\tType: " + data.getType() + "\n";
				text += "\tType Flag: " + data.getFlag() + "\n";
				text += "\tValue: " + data.getValue();
			}
		}
		
		JTextArea area = new JTextArea(15, 40);
		
		area.setText(text);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setEditable(false);
		
		JScrollPane scroll = new JScrollPane(area);
		area.setCaretPosition(0);
		
		return scroll;
	}
	
	public static void showDataDisplayDialog(ArrayList<SiteDataValue<?>> datas, Component parent) {
		Component comp = getDataDisplayComponent(datas);
		
		JOptionPane.showMessageDialog(parent, comp, "Site Data Values", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame window = new JFrame();
		OrderedSiteDataProviderList list = OrderedSiteDataProviderList.createDebugSiteDataProviders();
		
		AttenuationRelationshipAPI attenRel = null;
		
		OrderedSiteDataGUIBean bean = new OrderedSiteDataGUIBean(list, attenRel);
		
		window.setContentPane(bean);
		window.setSize(width, 600);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

}
