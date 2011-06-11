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

package org.opensha.sha.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.opensha.commons.gui.DisclaimerDialog;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.commons.util.bugReports.BugReport;
import org.opensha.commons.util.bugReports.BugReportDialog;
import org.opensha.commons.util.bugReports.DefaultExceptoinHandler;
import org.opensha.sha.calc.SpectrumCalculator;
import org.opensha.sha.calc.remoteCalc.RemoteResponseSpectrumClient;
import org.opensha.sha.earthquake.ERF_Ref;
import org.opensha.sha.earthquake.BaseERF;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.FloatingPoissonFaultERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel96_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_AreaForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_MultiSourceForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PoissonFaultERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_AlaskanPipeForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_EqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WG02_FortranWrappedERF_EpistemicListClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WGCEP_UCERF1_EqkRupForecastClient;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.util.IconFetcher;

/**
 * <p>Title: HazardSpectrumServerModeApplication </p>
 *
 * <p>Description: This class allows the  </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */
public class HazardSpectrumServerModeApplication
extends HazardSpectrumLocalModeApplication {

	public static final String APP_NAME = "Hazard Spectrum Server Mode Application";
	public static final String APP_SHORT_NAME = "HazardSpectrumServer";
	
	public HazardSpectrumServerModeApplication(String appShortName) {
		super(appShortName);
	}

	/**
	 * Initialize the ERF Gui Bean
	 */
	protected void initERF_GuiBean() {

		if (erfGuiBean == null) {
			try {
				erfGuiBean = new ERF_GuiBean(ERF_Ref.get(true, true, ServerPrefUtils.SERVER_PREFS));
				erfGuiBean.getParameter(erfGuiBean.ERF_PARAM_NAME).
				addParameterChangeListener(this);
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
				BugReport bug = new BugReport(e, getParametersInfoAsString(), appShortName, getAppVersion(), this);
				BugReportDialog bugDialog = new BugReportDialog(this, bug, true);
				bugDialog.setVisible(true);
			}
		}
		else {
			boolean isCustomRupture = erfRupSelectorGuiBean.isCustomRuptureSelected();
			if (!isCustomRupture) {
				BaseERF eqkRupForecast = erfRupSelectorGuiBean.
				getSelectedEqkRupForecastModel();
				erfGuiBean.setERF(eqkRupForecast);
			}
		}
		//    erfPanel.removeAll(); TODO clean
		//    erfPanel.add(erfGuiBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
		//        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0));
		//
		//    erfPanel.updateUI();

	}


	/**
	 * Initialize the ERF Rup Selector Gui Bean
	 */
	protected void initERFSelector_GuiBean() {

		BaseERF erf = null;
		try {
			erf = erfGuiBean.getSelectedERF();
		}
		catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
		if(erfRupSelectorGuiBean == null){

			try {
				erfRupSelectorGuiBean = new EqkRupSelectorGuiBean(erf,ERF_Ref.get(true, false, ServerPrefUtils.SERVER_PREFS));
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException("Connection to ERF's failed");
			}
		}
		//    erfPanel.removeAll(); TODO clean
		//    //erfGuiBean = null;
		//    erfPanel.add(erfRupSelectorGuiBean,
		//                 new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
		//                                        GridBagConstraints.CENTER,
		//                                        GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,
		//                                        0));
		//    erfPanel.updateUI();
	}

	/**
	 * This method creates the SpectrumCalc s.
	 * If the internet connection is available then it creates a remote instances of
	 * the calculators on server where the calculations take place, else
	 * calculations are performed on the user's own machine.
	 */
	protected void createCalcInstance() {
		try{
			if (calc == null && isProbabilisticCurve) {
				calc = (new RemoteResponseSpectrumClient()).getRemoteSpectrumCalc();
				if(this.calcParamsControl != null)
					try {
						calc.setAdjustableParams(calcParamsControl.getAdjustableCalcParams());
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			else if(calc == null && !isProbabilisticCurve) {
				calc = new SpectrumCalculator();
				calc.setAdjustableParams(calcParamsControl.getAdjustableCalcParams());
			}
		}catch (Exception ex) {
			BugReport bug = new BugReport(ex, getParametersInfoAsString(), APP_SHORT_NAME, getAppVersion(), this);
			BugReportDialog bugDialog = new BugReportDialog(this, bug, true);
			bugDialog.setVisible(true);
		}
	}

	public static void main(String[] args) throws IOException {
		new DisclaimerDialog(APP_NAME, APP_SHORT_NAME, getAppVersion());
		DefaultExceptoinHandler exp = new DefaultExceptoinHandler(
				APP_SHORT_NAME, getAppVersion(), null, null);
		Thread.setDefaultUncaughtExceptionHandler(exp);
		HazardSpectrumServerModeApplication applet = new
		HazardSpectrumServerModeApplication(APP_SHORT_NAME);
		exp.setApp(applet);
		exp.setParent(applet);
		applet.init();
		applet.setIconImages(IconFetcher.fetchIcons(APP_SHORT_NAME));
		applet.setVisible(true);
	}
}
