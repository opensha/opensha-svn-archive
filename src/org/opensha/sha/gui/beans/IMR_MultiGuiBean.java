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
	private ArrayList<IMR_ParamEditor> paramEdits = null;
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
		if (regions == null) {
			singleIMRBox.setSelected(true);
		} else {
			JPanel checkPanel = new JPanel();
			checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.X_AXIS));
			checkPanel.add(singleIMRBox);
			this.add(checkPanel);
			if (regions.size() > 1) {
				singleIMRBox.setEnabled(true);
			} else {
				singleIMRBox.setEnabled(false);
				singleIMRBox.setSelected(true);
			}
			singleIMRBox.setEnabled(regions.size() > 1);
		}
		if (!refreshOnly) {
			paramEdits = new ArrayList<IMR_ParamEditor>();
			chooserBoxes = new ArrayList<ChooserComboBox>();
			showHideButtons = null;
		}
		if (!singleIMRBox.isSelected()) {
			if (!refreshOnly)
				showHideButtons = new ArrayList<ShowHideButton>();
			for (int i=0; i<regions.size(); i++) {
				TectonicRegionType region = regions.get(i);
				JLabel label = new JLabel(region.name());
				this.add(wrapInPanel(label));
				ChooserComboBox chooser;
				IMR_ParamEditor editor;
				ShowHideButton button;
				if (refreshOnly) {
					chooser = chooserBoxes.get(i);
					editor = paramEdits.get(i);
					button = showHideButtons.get(i);
				} else {
					chooser = new ChooserComboBox(i);
					chooserBoxes.add(chooser);
					editor = null;
					paramEdits.add(editor);
					button = new ShowHideButton(i, false);
					button.addActionListener(this);
					showHideButtons.add(button);
				}
				
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
				panel.add(wrapInPanel(chooser));
				panel.add(button);
				
				this.add(wrapInPanel(panel));
				if (button.isShowing()) {
					if (editor == null)
						editor = new IMR_ParamEditor(imrs.get(chooser.getSelectedIndex()));
					this.add(editor);
				}
			}
		} else {
			ChooserComboBox chooser;
			IMR_ParamEditor editor;
			if (refreshOnly) {
				chooser = chooserBoxes.get(0);
				editor = paramEdits.get(0);
			} else {
				chooser = new ChooserComboBox(0);
				chooserBoxes.add(chooser);
				editor = new IMR_ParamEditor(imrs.get(chooser.getSelectedIndex()));
				paramEdits.add(editor);
			}
			this.add(wrapInPanel(chooser));
			this.add(editor);
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
		this.regions = regions;
		this.rebuildGUI();
	}
	
	private static String showParamsTitle = "Params";
	private static String hideParamsTitle = "Hide";
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
		}
	}

}
