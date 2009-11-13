package org.opensha.sha.gui.beans;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opensha.commons.gui.LabeledBoxPanel;
import org.opensha.commons.param.editor.ParameterEditor;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class IMR_MultiGuiBean extends LabeledBoxPanel implements ActionListener {
	
	private JCheckBox singleIMRBox = new JCheckBox("Single IMR For All Tectonic Region Types");
	
	private ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs;
	
	private ArrayList<TectonicRegionType> regions = null;
	private IMR_ParamEditor paramEdit = null;
	private ArrayList<ShowHideButton> showHideButtons = null;
	private ArrayList<ChooserComboBox> chooserBoxes = null;
	
	public IMR_MultiGuiBean(ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs) {
		this.imrs = imrs;
		
		initGUI();
	}
	
	private void initGUI() {
		setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		singleIMRBox.setFont(new Font("My Font", Font.PLAIN, 10));
		singleIMRBox.addActionListener(this);
		paramEdit = new IMR_ParamEditor();
		
		rebuildGUI();
	}
	
	/**
	 * This rebuilds all components of the GUI for display
	 */
	private void rebuildGUI() {
		rebuildGUI(false);
	}
	
	/**
	 * This rebuilds all components of the GUI for display
	 */
	private void rebuildGUI(boolean refreshOnly) {
		this.removeAll();
		if (regions == null || regions.size() <= 1) {
			singleIMRBox.setSelected(true);
		} else {
			JPanel checkPanel = new JPanel();
			checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.X_AXIS));
			checkPanel.add(singleIMRBox);
			this.add(checkPanel);
			singleIMRBox.setEnabled(true);
			singleIMRBox.setEnabled(regions.size() > 1);
		}
		if (!refreshOnly) {
			chooserBoxes = new ArrayList<ChooserComboBox>();
			showHideButtons = null;
		}
		if (!singleIMRBox.isSelected()) {
			if (!refreshOnly)
				showHideButtons = new ArrayList<ShowHideButton>();
			for (int i=0; i<regions.size(); i++) {
				TectonicRegionType region = regions.get(i);
				JLabel label = new JLabel(region.toString());
				this.add(wrapInPanel(label));
				ChooserComboBox chooser;
				ShowHideButton button;
				if (refreshOnly) {
					chooser = chooserBoxes.get(i);
					button = showHideButtons.get(i);
				} else {
					chooser = new ChooserComboBox(i);
					chooser.addActionListener(this);
					chooserBoxes.add(chooser);
					button = new ShowHideButton(i, false);
					button.addActionListener(this);
					showHideButtons.add(button);
				}
				
//				JPanel panel = new JPanel();
//				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				this.add(wrapInPanel(chooser));
				this.add(wrapInPanel(button));
				
//				this.add(wrapInPanel(panel));
				if (button.isShowing()) {
					paramEdit.setIMR(imrs.get(chooser.getSelectedIndex()));
					this.add(paramEdit);
				}
			}
		} else {
			ChooserComboBox chooser;
			if (refreshOnly) {
				chooser = chooserBoxes.get(0);
			} else {
				chooser = new ChooserComboBox(0);
				chooser.addActionListener(this);
				chooserBoxes.add(chooser);
			}
			this.add(wrapInPanel(chooser));
			paramEdit.setIMR(imrs.get(chooser.getSelectedIndex()));
			this.add(paramEdit);
		}
		this.validate();
		this.paintAll(getGraphics());
	}
	
	private static JPanel wrapInPanel(JComponent comp) {
		JPanel panel = new JPanel();
		panel.add(comp);
		return panel;
	}
	
	public void setTectonicRegions(ArrayList<TectonicRegionType> regions) {
		// we can refresh only if there are none or < 2 regions, and the check box isn't showing
		boolean refreshOnly = (regions == null || regions.size() < 2) && !this.singleIMRBox.isAncestorOf(this);
		this.regions = regions;
		this.rebuildGUI(refreshOnly);
	}
	
	private static String showParamsTitle = "Edit IMR Params";
	private static String hideParamsTitle = "Hide IMR Params";
	private class ShowHideButton extends JButton {
		
		private int index;
		private boolean showing;
		
		public ShowHideButton(int index, boolean initial) {
			this.index = index;
			this.showing = initial;
			
			updateText();
		}
		
		private void updateText() {
			if (showing)
				this.setText(hideParamsTitle);
			else
				this.setText(showParamsTitle);
		}
		
		private void hideParams() {
			showing = false;
			updateText();
		}
		
		public void toggle() {
			showing = !showing;
			updateText();
		}
		
		public int getIndex() {
			return index;
		}
		
		public boolean isShowing() {
			return showing;
		}
	}
	
	private class ChooserComboBox extends JComboBox {
		private int index;
		public ChooserComboBox(int index) {
			for (ScalarIntensityMeasureRelationshipAPI imr : imrs) 
				this.addItem(imr.getName());
			
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof ShowHideButton) {
			ShowHideButton button = (ShowHideButton)source;
			button.toggle();
			for (ShowHideButton theButton : showHideButtons) {
				if (theButton == button)
					continue;
				theButton.hideParams();
			}
			rebuildGUI(true);
		} else if (source == singleIMRBox) {
			rebuildGUI();
		} else if (source instanceof ChooserComboBox) {
			ChooserComboBox chooser = (ChooserComboBox)source;
			paramEdit.setIMR(imrs.get(chooser.getSelectedIndex()));
			paramEdit.validate();
		}
	}

}
