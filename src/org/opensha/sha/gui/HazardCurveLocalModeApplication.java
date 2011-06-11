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

import org.opensha.commons.gui.DisclaimerDialog;
import org.opensha.commons.util.ServerPrefUtils;
import org.opensha.commons.util.bugReports.BugReport;
import org.opensha.commons.util.bugReports.BugReportDialog;
import org.opensha.commons.util.bugReports.DefaultExceptoinHandler;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.earthquake.ERF_Ref;
import org.opensha.sha.earthquake.BaseERF;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.EqkRupSelectorGuiBean;
import org.opensha.sha.gui.util.IconFetcher;



/**
 * <p>Title: HazardCurveLocalModeApplication</p>
 * <p>Description: This application is extension of HazardCurveApplication, where
 * everything take place on the user's own machine. This version of application
 * does not require any internet connection, the only difference between this
 * application and its parent class that it uses user's system memory for doing
 * any computation. Whereas , in the HazardCurve application all calculations
 * take place on the users machine.</p>
 * @author : Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class HazardCurveLocalModeApplication extends HazardCurveServerModeApplication {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String APP_NAME = "Hazard Curve Local Mode Application";
	public static final String APP_SHORT_NAME = "HazardCurveLocal";
	
	public HazardCurveLocalModeApplication(String appShortName) {
		super(appShortName);
	}

	/**
	 * Initialize the ERF Gui Bean
	 */
	protected void initERF_GuiBean() {

		if(erfGuiBean == null){
			// create the ERF Gui Bean object

			try {
				erfGuiBean = new ERF_GuiBean(ERF_Ref.get(false, true, ServerPrefUtils.SERVER_PREFS));
				erfGuiBean.getParameter(ERF_GuiBean.ERF_PARAM_NAME).
				addParameterChangeListener(this);
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
				
				BugReport bug = new BugReport(e, errorInInitializationMessage, appShortName, getAppVersion(), this);
				BugReportDialog bugDialog = new BugReportDialog(this, bug, true);
				bugDialog.setVisible(true);
			}
		}
		else{
			boolean isCustomRupture = erfRupSelectorGuiBean.isCustomRuptureSelected();
			if(!isCustomRupture){
				BaseERF eqkRupForecast = erfRupSelectorGuiBean.getSelectedEqkRupForecastModel();
				erfGuiBean.setERF(eqkRupForecast);
			}
		}
//		erfPanel.removeAll(); TODO clean
//		erfPanel.add(erfGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
//				GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(0,0,0,0), 0, 0 ));
//		erfPanel.updateUI();
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

				erfRupSelectorGuiBean = new EqkRupSelectorGuiBean(erf, ERF_Ref.get(false, false, ServerPrefUtils.SERVER_PREFS));
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException("Connection to ERF's failed");
			}
		}
		else
			erfRupSelectorGuiBean.setEqkRupForecastModel(erf);
//		erfPanel.removeAll(); TODO clean
//		//erfGuiBean = null;
//		erfPanel.add(erfRupSelectorGuiBean, new GridBagConstraints( 0, 0, 1, 1, 1.0, 1.0,
//				GridBagConstraints.CENTER,GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
//		erfPanel.updateUI();
	}

	/**
	 * This method creates the HazardCurveCalc and Disaggregation Calc(if selected) instances.
	 * Calculations are performed on the user's own machine, no internet connection
	 * is required for it.
	 */
	protected void createCalcInstance(){
		try{
			if(calc == null) {
				calc = new HazardCurveCalculator();
				if(this.calcParamsControl != null)
					calc.setAdjustableParams(calcParamsControl.getAdjustableCalcParams());
//System.out.println("Created new calc from LocalModeApp");
			}
			if(disaggregationFlag)
				if(disaggCalc == null)
					disaggCalc = new DisaggregationCalculator();
		}catch(Exception e){
			e.printStackTrace();
			BugReport bug = new BugReport(e, this.getParametersInfoAsString(), appShortName, getAppVersion(), this);
			BugReportDialog bugDialog = new BugReportDialog(this, bug, true);
			bugDialog.setVisible(true);
		}
		
	}

	public static void main(String[] args) throws IOException {
		new DisclaimerDialog(APP_NAME, APP_SHORT_NAME, getAppVersion());
		DefaultExceptoinHandler exp = new DefaultExceptoinHandler(
				APP_SHORT_NAME, getAppVersion(), null, null);
		Thread.setDefaultUncaughtExceptionHandler(exp);
		HazardCurveLocalModeApplication applet = new HazardCurveLocalModeApplication(APP_SHORT_NAME);
		exp.setApp(applet);
		exp.setParent(applet);
		applet.init();
		applet.setTitle("Hazard Curve Local mode Application "+"("+getAppVersion()+")" );
		applet.setIconImages(IconFetcher.fetchIcons(APP_SHORT_NAME));
		applet.setVisible(true);
		applet.createCalcInstance();
	}
}
