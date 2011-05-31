/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.sha.calc.hazardMap.old.applet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.sha.earthquake.ERF_Ref;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBeanAPI;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.SitesInGriddedRegionGuiBean;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIMR;

public class HazardStep extends JPanel implements IMR_GuiBeanAPI {
	
	private ERF_GuiBean erfBean;
	private IMR_GuiBean imrGuiBean;
	private IMT_GuiBean imtGuiBean;
	
	private SitesInGriddedRegionGuiBean sitesGuiBean;
	
	private JPanel imPanel = new JPanel();
	
	public HazardStep(SitesInGriddedRegionGuiBean sitesGuiBean) {
		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		this.sitesGuiBean = sitesGuiBean;
		
		erfBean = createERF_GUI_Bean();
		imrGuiBean = createIMR_GUI_Bean();
		imtGuiBean = createIMT_GUI_Bean();
		
		imPanel.setLayout(new BoxLayout(imPanel, BoxLayout.Y_AXIS));
		imPanel.add(imrGuiBean);
		imPanel.add(imtGuiBean);
		
		this.add(erfBean);
		this.add(imPanel);
	}
	
	private IMR_GuiBean createIMR_GUI_Bean() {
		return new IMR_GuiBean(this);
	}
	
	private IMT_GuiBean createIMT_GUI_Bean() {
		AttenuationRelationship attenRel = (AttenuationRelationship)imrGuiBean.getSelectedIMR_Instance();
		return new IMT_GuiBean(attenRel,attenRel.getSupportedIntensityMeasuresIterator());
	}
	
	private ERF_GuiBean createERF_GUI_Bean() {
		try{
			return new ERF_GuiBean(ERF_Ref.get(false, false, ServerPrefUtils.SERVER_PREFS));
		}catch(InvocationTargetException e){
			throw new RuntimeException("Connection to ERF servlets failed");
		}
	}

	/**
	 * Updates the IMT_GuiBean to reflect the chnaged IM for the selected AttenuationRelationship.
	 * This method is called from the IMR_GuiBean to update the application with the Attenuation's
	 * supported IMs.
	 *
	 */
	public void updateIM() {
		//get the selected IMR
		ScalarIMR imr = imrGuiBean.getSelectedIMR_Instance();
		imtGuiBean.setIM(imr,imr.getSupportedIntensityMeasuresIterator()) ;
	}

	/**
	 * Updates the SitesInGriddedRegionGuiBean to reflect the chnaged SiteParams for the selected AttenuationRelationship.
	 * This method is called from the IMR_GuiBean to update the application with the Attenuation's
	 * Site Params.
	 *
	 */
	public void updateSiteParams() {
		//get the selected IMR
		ScalarIMR imr = imrGuiBean.getSelectedIMR_Instance();
		sitesGuiBean.replaceSiteParams(imr.getSiteParamsIterator());
		sitesGuiBean.validate();
		sitesGuiBean.repaint();
	}
	
	public AbstractERF getERF() throws InvocationTargetException {
		return (AbstractERF)erfBean.getSelectedERF_Instance();
	}
	
	public ScalarIMR getIMR() {
		return imrGuiBean.getSelectedIMR_Instance();
	}
	
	public IMR_GuiBean getIMRGuiBean() {
		return imrGuiBean;
	}
	
	public IMT_GuiBean getIMTGuiBean() {
		return imtGuiBean;
	}
	
	public SitesInGriddedRegionGuiBean createSitesGUIBean(ArrayList<Region> regions) {
		sitesGuiBean = new SitesInGriddedRegionGuiBean(regions);
		sitesGuiBean.replaceSiteParams(this.getIMR().getSiteParamsIterator());
		return sitesGuiBean;
	}

}
