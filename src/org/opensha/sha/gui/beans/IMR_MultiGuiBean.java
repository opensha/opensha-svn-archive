package org.opensha.sha.gui.beans;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.opensha.commons.gui.LabeledBoxPanel;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.util.ListUtils;
import org.opensha.sha.gui.beans.event.IMTChangeEvent;
import org.opensha.sha.gui.beans.event.IMTChangeListener;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.event.ScalarIMRChangeEvent;
import org.opensha.sha.imr.event.ScalarIMRChangeListener;
import org.opensha.sha.util.TectonicRegionType;

public class IMR_MultiGuiBean extends LabeledBoxPanel implements ActionListener, IMTChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Font trtFont = new Font("TRTFont", Font.BOLD, 16);

	private ArrayList<ScalarIMRChangeListener> listeners = new ArrayList<ScalarIMRChangeListener>();

	private JPanel checkPanel;
	protected JCheckBox singleIMRBox = new JCheckBox("Single IMR For All Tectonic Region Types");

	private ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs;
	private ArrayList<Boolean> imrEnables;

	private ArrayList<TectonicRegionType> regions = null;
	
	private IMR_ParamEditor paramEdit = null;
	private int chooserForEditor = 0;
	
	private ArrayList<ShowHideButton> showHideButtons = null;
	private ArrayList<ChooserComboBox> chooserBoxes = null;

	private HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap;

	private DependentParameterAPI<Double> imt = null;
	
	private int maxChooserChars = Integer.MAX_VALUE;
	
	private int defaultIMRIndex = 0;

	public IMR_MultiGuiBean(ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs) {
		this.imrs = imrs;
		imrEnables = new ArrayList<Boolean>();
		for (int i=0; i<imrs.size(); i++) {
			imrEnables.add(new Boolean(true));
		}

		// TODO add make the multi imr bean handle warnings
		initGUI();
		updateIMRMap();
	}

	private void initGUI() {
		setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		singleIMRBox.setFont(new Font("My Font", Font.PLAIN, 10));
		singleIMRBox.addActionListener(this);
		paramEdit = new IMR_ParamEditor();
		this.setTitle("Set IMR");

		rebuildGUI();
	}

	/**
	 * This rebuilds all components of the GUI for display
	 */
	public void rebuildGUI() {
		rebuildGUI(false);
	}

	/**
	 * This rebuilds all components of the GUI for display. If refreshOnly is true,
	 * then the GUI will just be refreshed with editor panels updated, otherwise it will
	 * be rebuilt from the ground up. You can only refresh on a show/hide button click,
	 * otherwise you should rebuild for all events
	 */
	private void rebuildGUI(boolean refreshOnly) {
//		System.out.println("rebuildGUI...refreshOnly? " + refreshOnly);
		// even for a refresh, we remove all components and re-add
		this.removeAll();
		if (regions == null || regions.size() <= 1) {
			// if we don't have enough regions for multiple IMRs, make sure the
			// single IMR box is selected, but don't show it
			singleIMRBox.setSelected(true);
		} else {
			// this means there's the possibility of multiple IMRs...show the box
			checkPanel = new JPanel();
			checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.X_AXIS));
			checkPanel.add(singleIMRBox);
			this.add(checkPanel);
		}
		if (!refreshOnly) {
			// if we're rebuilding, we have to re-init the GUI elements
			chooserBoxes = new ArrayList<ChooserComboBox>();
			showHideButtons = null;
		}
		if (!singleIMRBox.isSelected()) {
			// this is for multiple IMRs
			if (!refreshOnly)
				showHideButtons = new ArrayList<ShowHideButton>();
			for (int i=0; i<regions.size(); i++) {
				// create label for tectonic region
				TectonicRegionType region = regions.get(i);
				JLabel label = new JLabel(region.toString());
				label.setFont(trtFont);
				this.add(wrapInPanel(label));
				
				// get the chooser and button. if we're rebuilding, chooser
				// and button need to be recreated
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
					// if the show params button is selected, update and add the parameter editor
					// to the GUI
					chooserForEditor = i;
					updateParamEdit(chooser);
					this.add(paramEdit);
				}
			}
		} else {
			// this is for single IMR mode
			ChooserComboBox chooser;
			if (refreshOnly) {
				chooser = chooserBoxes.get(0);
			} else {
				chooser = new ChooserComboBox(0);
				chooser.setBackground(Color.WHITE);
				chooser.addActionListener(this);
				chooserBoxes.add(chooser);
			}
			// we simply add chooser 0 to the GUI, and show the params for the selected IMR
			this.add(wrapInPanel(chooser));
			chooserForEditor = 0;
			updateParamEdit(chooser);
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
	
	public boolean isCheckBoxVisible() {
		if (checkPanel == null)
			return false;
		else
			return checkPanel.isAncestorOf(singleIMRBox) && this.isAncestorOf(checkPanel);
	}

	public void setTectonicRegions(ArrayList<TectonicRegionType> regions) {
		// we can refresh only if there are none or < 2 regions, and the check box isn't showing
		boolean refreshOnly = (regions == null || regions.size() < 2) && !isCheckBoxVisible();
		this.regions = regions;
		boolean prevSingle = !isMultipleIMRs();
		this.rebuildGUI(refreshOnly);
		boolean newSingle = !isMultipleIMRs();
		// update the IMR map if we rebuilt the GUI, and it didn't both start and end as single IMR.
		// we dont' want to fire an event if we changed TRTs from null to something, but still have single
		// IMR selected.
		if (!refreshOnly && !(prevSingle && newSingle))
			fireUpdateIMRMap();
	}
	
	public ArrayList<TectonicRegionType> getTectonicRegions() {
		return regions;
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

	public class EnableableCellRenderer extends BasicComboBoxRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (!isSelected)
				comp.setBackground(Color.white);
			if (index >= 0)
				comp.setEnabled(imrEnables.get(index));
			else
				comp.setEnabled(true);
			return comp;
		}

	}

	private class ChooserComboBox extends JComboBox {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private int comboBoxIndex;
		public ChooserComboBox(int index) {
			for (ScalarIntensityMeasureRelationshipAPI imr : imrs) {
				String name = imr.getName();
				if (name.length() > maxChooserChars) {
					name = name.substring(0, maxChooserChars);
				}
				this.addItem(name);
			}
			
			if (!imrEnables.get(defaultIMRIndex)) {
				for (int i=0; i<imrEnables.size(); i++) {
					if (imrEnables.get(i).booleanValue()) {
//						System.out.println("Const...set imr to " + imrs.get(i).getName());
						defaultIMRIndex = i;
						break;
					}
				}
			}
			this.setSelectedIndex(defaultIMRIndex);

			this.setRenderer(new EnableableCellRenderer());
			this.comboBoxIndex = index;
			this.addActionListener(new ComboListener(this));
			this.setMaximumSize(new Dimension(15, 150));
		}

		public int getIndex() {
			return comboBoxIndex;
		}
	}

	class ComboListener implements ActionListener {
		ChooserComboBox combo;

		Object currentItem;

		ComboListener(ChooserComboBox combo) {
			this.combo = combo;
			currentItem = combo.getSelectedItem();
		}

		public void actionPerformed(ActionEvent e) {
			Object tempItem = combo.getSelectedItem();
			// if the selected one isn't enabled, then go back to the old one
			if (!imrEnables.get(combo.getSelectedIndex())) {
				combo.setSelectedItem(currentItem);
				updateParamEdit(combo);
			} else {
				currentItem = tempItem;
			}
		}
	}
	
	private void updateParamEdit(ChooserComboBox chooser) {
		if (chooser.getIndex() == 0 && !isMultipleIMRs()) {
			// if we're in single mode, and this is the single chooser, then 
			// the default IMR index should be this chooser's value
			defaultIMRIndex = chooser.getSelectedIndex();
		}
		if (chooserForEditor == chooser.getIndex()) {
			// this is a check to make sure that we're updating the param editor for the
			// currently showing chooser
			
			ScalarIntensityMeasureRelationshipAPI imr = imrs.get(chooser.getSelectedIndex());
//			System.out.println("Updating param edit for chooser " + chooserForEditor + " to : " + imr.getName());
			paramEdit.setIMR(imr);
			// we only want to show the TRT param if it's in single mode
			paramEdit.setTRTParamVisible(!this.isMultipleIMRs());
			paramEdit.setTitle(IMR_ParamEditor.DEFAULT_NAME + ": " + imr.getShortName());
			paramEdit.validate();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof ShowHideButton) {
			// one of the buttons to show or hide parameters in the GUI was clicked
			
			ShowHideButton button = (ShowHideButton)source;
			// toggle the button that was clicked
			button.toggle();
			// make sure that every other button has params hidden
			for (ShowHideButton theButton : showHideButtons) {
				if (theButton == button)
					continue;
				theButton.hideParams();
			}
			// since we're just showing params, we can rebuild with
			// a simple refresh instead of re-creating each GUI element
			rebuildGUI(true);
		} else if (source == singleIMRBox) {
			// this means the user either selected or deselected the
			// single/multiple IMR check box...full GUI rebuild
			rebuildGUI();
		} else if (source instanceof ChooserComboBox) {
			// this means the user changed one of the selected IMRs in a
			// chooser list
			ChooserComboBox chooser = (ChooserComboBox)source;
			updateParamEdit(chooser);
		}
		if (source == singleIMRBox || source instanceof ChooserComboBox) {
			// if we switched between single/multiple, or changed a selected
			// attenuation relationship, then we have to update the in-memory
			// IMR map and fire an IMR Change event
			fireUpdateIMRMap();
		}
	}

	private ScalarIntensityMeasureRelationshipAPI getIMRForChooser(int chooserID) {
		ChooserComboBox chooser = chooserBoxes.get(chooserID);
		return imrs.get(chooser.getSelectedIndex());
	}

	public boolean isMultipleIMRs() {
		return !singleIMRBox.isSelected();
	}
	
	public void setMultipleIMRsEnabled(boolean enabled) {
		if (!enabled)
			setMultipleIMRs(false);
		singleIMRBox.setEnabled(enabled);
	}

	public ScalarIntensityMeasureRelationshipAPI getSelectedIMR() {
		if (isMultipleIMRs())
			throw new RuntimeException("Cannot get single selected IMR when multiple selected!");
		return getIMRForChooser(0);
	}

	public ScalarIntensityMeasureRelationshipAPI getSelectedIMR(TectonicRegionType trt) {
		if (!isMultipleIMRs()) {
			// if it's just a single, then it's easy
			return getIMRForChooser(0);
		}
		if (regions == null)
			return null;
		for (int i=0; i<regions.size(); i++) {
			TectonicRegionType region = regions.get(i);
			if (region == trt) {
				return getIMRForChooser(i);
			}
		}
		return null;
	}

	public HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> getIMRMap() {
		return imrMap;
	}

	public void updateIMRMap() {
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> map =
			new HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>();

		if (!isMultipleIMRs()) {
			ScalarIntensityMeasureRelationshipAPI imr = getIMRForChooser(0);
			map.put(TectonicRegionType.ACTIVE_SHALLOW, imr);
		} else {
			for (int i=0; i<regions.size(); i++) {
				TectonicRegionType region = regions.get(i);
				map.put(region, getIMRForChooser(i));
			}
		}

		this.imrMap = map;
	}

	public void setMultipleIMRs(boolean multipleIMRs) {
		// if they're trying to set it to multiple, but we don't have multiple tectonic regions
		// then throw an exception
		if (multipleIMRs && (regions == null || regions.size() <= 1))
			throw new RuntimeException("Cannot be set to multiple IMRs if < 2 tectonic regions" +
			" sepcified");
		boolean previous = isMultipleIMRs();
		if (previous != multipleIMRs) {
			singleIMRBox.setSelected(!multipleIMRs);
//			System.out.println("changing singleIMRBox to " + (!multipleIMRs));
			rebuildGUI(false);
			fireUpdateIMRMap();
		}
	}

	public void setSelectedSingleIMR(String imrName) {
		setMultipleIMRs(false);
		ChooserComboBox chooser = chooserBoxes.get(0);
		int index = ListUtils.getIndexByName(imrs, imrName);
		if (index < 0)
			throw new NoSuchElementException("IMR '" + imrName + "' not found");
		chooser.setSelectedIndex(index);
	}

	public void addIMRChangeListener(ScalarIMRChangeListener listener) {
		listeners.add(listener);
	}

	public void removeIMRChangeListener(ScalarIMRChangeListener listener) {
		listeners.remove(listener);
	}

	private void fireUpdateIMRMap() {
		HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> oldMap = imrMap;
		updateIMRMap();
		fireIMRChangeEvent(oldMap, imrMap);
	}

	private void fireIMRChangeEvent(
			HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> oldMap,
			HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> newMap) {
		ScalarIMRChangeEvent event = new ScalarIMRChangeEvent(this, oldMap, newMap);
//		System.out.println("Firing IMR Change Event");
		for (ScalarIMRChangeListener listener : listeners) {
			listener.imrChange(event);
		}
	}

	public Iterator<ParameterAPI<?>> getMultiIMRSiteParamIterator() {
		return getMultiIMRSiteParamIterator(imrMap);
	}

	public static Iterator<ParameterAPI<?>> getMultiIMRSiteParamIterator(
			HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI> imrMap) {
		ArrayList<ParameterAPI<?>> params = new ArrayList<ParameterAPI<?>>();
		for (TectonicRegionType trt : imrMap.keySet()) {
			ScalarIntensityMeasureRelationshipAPI imr = imrMap.get(trt);
			ListIterator<ParameterAPI<?>> siteParams = imr.getSiteParamsIterator();
			while (siteParams.hasNext()) {
				params.add(siteParams.next());
			}
		}
		return params.iterator();
	}
	
	/**
	 * the imr should be enabled if:
	 * * no imt has been selected
	 *  OR
	 * * the imt is supported
	 * @param imr
	 * @return
	 */
	private boolean shouldEnableIMR(ScalarIntensityMeasureRelationshipAPI imr) {
		return imt == null || imr.isIntensityMeasureSupported(imt);
	}

	public void setIMT(DependentParameterAPI<Double> newIMT) {
		this.imt = newIMT;

		for (int i=0; i<imrs.size(); i++) {
			ScalarIntensityMeasureRelationshipAPI imr = imrs.get(i);
			Boolean enabled = shouldEnableIMR(imr);
			imrEnables.set(i, enabled);
		}
		for (ChooserComboBox chooser : chooserBoxes) {
			// if the selected imr is disabled
			if (!imrEnables.get(chooser.getSelectedIndex())) {
				// then we select the first enabled one in the list and use that
				for (int i=0; i<chooser.getItemCount(); i++) {
					if (imrEnables.get(i)) {
						chooser.setSelectedIndex(i);
						break;
					}
				}
			}
			chooser.repaint();
		}
	}
	
	public ArrayList<ScalarIntensityMeasureRelationshipAPI> getIMRs() {
		return imrs;
	}
	
	public String getIMRMetadataHTML() {
		return "IMR METADATA!!!!"; // TODO fill this in!
	}

	@Override
	public void imtChange(IMTChangeEvent e) {
		this.setIMT(e.getNewIMT());
	}
	
	public void setMaxChooserChars(int maxChooserChars) {
		this.maxChooserChars = maxChooserChars;
	}

}
