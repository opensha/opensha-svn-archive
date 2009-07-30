package org.opensha.sha.calc.IM_EventSet.v03.gui;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListModel;

import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBeanAPI;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeEvent;
import org.opensha.sha.imr.event.AttenuationRelationshipChangeListener;

public class IMR_ChooserPanel extends NamesListPanel implements IMR_GuiBeanAPI, AttenuationRelationshipChangeListener {
	
	private IMR_GuiBean imrGuiBean;
	private IMT_ChooserPanel imtChooser;
	private ParameterListEditor imrSiteParamsEdit;
	
	public IMR_ChooserPanel(IMT_ChooserPanel imtChooser) {
		super(null, "Selected IMR(s):");
		
		imtChooser.setForceDisableAddButton(true);
		
		this.imtChooser = imtChooser;
		
		imrGuiBean = new IMR_GuiBean(this);
		imrGuiBean.addAttenuationRelationshipChangeListener(this);
		
		imrSiteParamsEdit = new ParameterListEditor();
		imrSiteParamsEdit.setTitle("Default Site Params");
		
		JPanel imPanel = new JPanel();
		imPanel.setLayout(new BoxLayout(imPanel, BoxLayout.Y_AXIS));
		imPanel.add(imrGuiBean);
		imPanel.add(imrSiteParamsEdit);
		
		updateSiteParams();
		
		setLowerPanel(imPanel);
		updateIMTs();
	}

	public void updateIM() {
		//get the selected IMR
		ScalarIntensityMeasureRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
//		imtGuiBean.setIM(imr,imr.getSupportedIntensityMeasuresIterator()) ;
	}

	public void updateSiteParams() {
		//get the selected IMR
		ScalarIntensityMeasureRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
		updateSiteParams(imr);
//		sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
//		sitesGuiBean.validate();
//		sitesGuiBean.repaint();
	}
	
	private void updateSiteParams(ScalarIntensityMeasureRelationshipAPI imr) {
		ListIterator<ParameterAPI<?>> it = imr.getSiteParamsIterator();
		ParameterList list = new ParameterList();
		while (it.hasNext()) {
			ParameterAPI<?> param = it.next();
//			System.out.println("adding: " + param.getName());
			list.addParameter(param);
		}
		imrSiteParamsEdit.setParameterList(list);
		imrSiteParamsEdit.refreshParamEditor();
		imrSiteParamsEdit.validate();
		this.validate();
	}
	
	private boolean shouldEnableAddButton(ScalarIntensityMeasureRelationshipAPI imr) {
		ListModel model = namesList.getModel();
		boolean match = false;
		for (int i=0; i<model.getSize(); i++) {
			if (model.getElementAt(i).toString().equals(imr.getName())) {
				match = true;
				break;
			}
		}
		return !match;
	}

	/**
	 * tester main method
	 * @param args
	 */
	public static void main(String args[]) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 600);
		
		frame.setContentPane(new IMR_ChooserPanel(new IMT_ChooserPanel()));
		frame.setVisible(true);
	}

	public void attenuationRelationshipChange(
			AttenuationRelationshipChangeEvent event) {
		addButton.setEnabled(shouldEnableAddButton(event.getNewAttenRel()));
	}

	@Override
	public void addButton_actionPerformed() {
		ListModel model = namesList.getModel();
		ScalarIntensityMeasureRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
		Object names[] = new Object[model.getSize()+1];
		for (int i=0; i<model.getSize(); i++) {
			names[i] = model.getElementAt(i);
		}
		names[names.length - 1] = imr.getName();
		namesList.setListData(names);
		addButton.setEnabled(false);
		updateIMTs();
	}

	@Override
	public void removeButton_actionPerformed() {
		ListModel model = namesList.getModel();
		ScalarIntensityMeasureRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
		Object names[] = new Object[model.getSize()-1];
		int selected = namesList.getSelectedIndex();
		int cnt = 0;
		for (int i=0; i<model.getSize(); i++) {
			if (selected == i) {
				// remove it
				continue;
			} else {
				names[cnt] = model.getElementAt(i);
				cnt++;
			}
		}
		namesList.setListData(names);
		updateIMTs();
	}
	
	public ArrayList<ScalarIntensityMeasureRelationshipAPI> getSelectedIMRs() {
		ListModel model = namesList.getModel();
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		for (int i=0; i<model.getSize(); i++) {
			String name = (String)model.getElementAt(i);
			imrs.add(imrGuiBean.getIMR_Instance(name));
		}
		return imrs;
	}
	
	private void updateIMTs() {
		ArrayList<ScalarIntensityMeasureRelationshipAPI> imrs = getSelectedIMRs();
		imtChooser.setIMRs(imrs);
	}

	@Override
	public boolean shouldEnableAddButton() {
		ScalarIntensityMeasureRelationshipAPI imr = imrGuiBean.getSelectedIMR_Instance();
		return shouldEnableAddButton(imr);
	}
}
