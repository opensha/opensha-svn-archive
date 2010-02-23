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

package org.opensha.sha.imr.attenRelImpl.peer;

import static org.opensha.sha.param.MagFreqDistParameter.*;

import static org.opensha.sha.imr.attenRelImpl.peer.TestSet.*;
import static org.opensha.sha.imr.attenRelImpl.peer.TestCase.*;
import static org.opensha.sha.imr.attenRelImpl.peer.TestSite.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.PEER_testsMagAreaRelationship;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.ERF_EpistemicList;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.EqkRupForecastBaseAPI;
import org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast;
import org.opensha.sha.gui.beans.ERF_GuiBean;
import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.IMT_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.gui.beans.TimeSpanGuiBean;
import org.opensha.sha.gui.controls.CalculationSettingsControlPanelAPI;
import org.opensha.sha.gui.controls.PEER_TestCaseSelectorControlPanelAPI;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationship;
import org.opensha.sha.imr.IntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.magdist.GaussianMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SingleMagFreqDist;
import org.opensha.sha.magdist.YC_1985_CharMagFreqDist;
import org.opensha.sha.param.MagFreqDistParameter;
import org.opensha.sha.param.SimpleFaultParameter;
import org.opensha.sha.param.editor.MagFreqDistParameterEditor;
import org.opensha.sha.param.editor.gui.SimpleFaultParameterEditorPanel;

import com.sun.jdi.connect.Connector.SelectedArgument;

public class TestConfig {

	// maximum permitted distance between fault and site to consider source in
	// hazard analysis for that site; this default value is to allow all PEER
	// test cases to pass through
	public static double MAX_DISTANCE = 300;

	
	// some of the universal parameter settings
	//private double GRID_SPACING = 1.0;
	//private String FAULT_TYPE = SimpleFaultParameter.STIRLING;

	// various gui beans
	//private IMT_GuiBean imtGuiBean;
	//private IMR_GuiBean imrGuiBean;
	//private Site_GuiBean siteGuiBean;
	//private ERF_GuiBean erfGuiBean;
	//private TimeSpanGuiBean timeSpanGuiBean;
	CalculationSettingsControlPanelAPI application;
	
	private AttenuationRelationship imr;
	private Site site;
	private EqkRupForecast erf;
	private ArbitrarilyDiscretizedFunc function;

	// Stores the test case,
	private TestCase selectedCase;
	private TestSite selectedSite;
	private TestSet selectedSet;
	
//	private JLabel jLabel2 = new JLabel();
//	private JComboBox testCaseComboBox = new JComboBox();
//	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	// Test set descriptors
	private static ArrayList<PeerTest> desc1;
	private static ArrayList<PeerTest> desc2;

	// lats, lons, dips, and depths of the faults used in the
	// FloatingPoissonFaultERF
	private ArrayList<Double> fault1and2_Lats;
	private ArrayList<Double> fault1and2_Lons;
	private ArrayList<Double> fault1_Dips;
	private ArrayList<Double> fault2_Dips;
	private ArrayList<Double> fault1_Depths;
	private ArrayList<Double> fault2_Depths;
	private ArrayList<Double> faultE_Lats;
	private ArrayList<Double> faultE_Lons;
	private ArrayList<Double> faultE_Dips;
	private ArrayList<Double> faultE_Depths;

	// Instance of the application implementing the
	// PEER_TestCaseSelectorControlPanelAPI
	//PEER_TestCaseSelectorControlPanelAPI api;

	// Stores the X Values for generating the hazard curve using the PEER
	// values.
	private static double[] xVals = {
		0.001, 
		0.01, 
		0.05, 
		0.1, 
		0.15, 
		0.2, 
		0.25, 
		0.3, 
		0.35, 
		0.4, 
		0.45, 
		0.5,
		0.55, 
		0.6, 
		0.7, 
		0.8, 
		0.9, 
		1.0 };

	//ArbitrarilyDiscretizedFunc function = new ArbitrarilyDiscretizedFunc();

	public static void main(String[] args) {
		SadighEtAl_1997_AttenRel sea = new SadighEtAl_1997_AttenRel(null);
		System.out.println(sea.getParameter(SigmaTruncTypeParam.NAME).getValue());
	}
	
	public TestConfig(PeerTest test) {
		selectedSet = test.getSet();
		selectedCase = test.getCase();
		selectedSite = test.getSite();
		initFaultData();
		initFunction();
		initTest();
	}

	public AttenuationRelationship getIMR() {
		return imr;
	}
	
	public Site getSite() {
		return site;
	}
	
	public EqkRupForecast getERF() {
		return erf;
	}
	
	public ArbitrarilyDiscretizedFunc getFunction() {
		return function;
	}
	
//	public PEER_TestConfig(Component parent,
//			PEER_TestCaseSelectorControlPanelAPI api,
//			IMR_GuiBean imrGuiBean,
//			Site_GuiBean siteGuiBean,
//			IMT_GuiBean imtGuiBean,
//			ERF_GuiBean erfGuiBean,
//			TimeSpanGuiBean timeSpanGuiBean,
//			CalculationSettingsControlPanelAPI application) {

		//this.api = api;
		// if (D)
		// System.out.println(C+" Constructor: starting initializeFaultData()");
		//initializeFaultData();

//		try {
//			jbInit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		// save the instances of the beans
		//this.imrGuiBean = imrGuiBean;
		//this.siteGuiBean = siteGuiBean;
		//this.imtGuiBean = imtGuiBean;
		//this.erfGuiBean = erfGuiBean;
		//this.timeSpanGuiBean = timeSpanGuiBean;
		//this.application = application;

		
		// if (D)
		// System.out.println(C+" Constructor: starting initializeTestsAndSites()");
		// fill the combo box with tests and sites
		//initTestDescriptors();

		// show the window at center of the parent component
//		setLocation(parent.getX() + parent.getWidth() / 2, parent.getY()
//				+ parent.getHeight() / 2);

		// function to create the PEER supported X Values
		//initFunctionVals();
		// sets the PEER supported X values in the application
//		this.setPEER_XValues();
//	}

//	private void jbInit() throws Exception {
//		this.getContentPane().setLayout(gridBagLayout1);
//		jLabel2.setForeground(Color.black);
//		jLabel2.setText("Select Test and Site:");
//		testCaseComboBox.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//testCaseComboBox_actionPerformed(e);
//			}
//		});
//		this.setTitle("PEER Test Case Selector");
//		this.getContentPane().add(
//				jLabel2,
//				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
//						GridBagConstraints.WEST, GridBagConstraints.NONE,
//						new Insets(15, 7, 2, 240), 22, 5));
//		this.getContentPane().add(
//				testCaseComboBox,
//				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
//						GridBagConstraints.CENTER,
//						GridBagConstraints.HORIZONTAL,
//						new Insets(7, 145, 2, 13), 92, -1));
//	}

	/**
	 * This method sets the X values for the hazard Curve to the PEER supported
	 * X Values.
	 */
//	public void setPEER_XValues() {
//		// sets X Value to the PEER supported x vlaues for the hazard curve
//		api.setX_ValuesForHazardCurve(function);
//	}

	
//	public void setTest(TestDescriptor test) {
//		selectedSet = test.getSet();
//		selectedCase = test.getCase();
//		selectedSite = test.getSite();
//		initTest();
//	}

	private void initTest() {
		site = new Site();
		if (is(SET_1)) {
			init_Set1();
		} else if (is(SET_2)) {
			init_Set2();
		}
		// String S = C + ":setParams()";
		// if (D) System.out.println(S+"::entering");

		// Gets the siteParamList
//		ParameterList siteParams =
//				siteGuiBean.getParameterListEditor().getParameterList();
		
		
		// set the distance in control panel
//		application.getCalcAdjustableParams().getParameter(
//				HazardCurveCalculator.MAX_DISTANCE_PARAM_NAME).setValue(
//				MAX_DISTANCE);
//		application.getCalcAdjustableParams().getParameter(
//				HazardCurveCalculator.INCLUDE_MAG_DIST_FILTER_PARAM_NAME)
//				.setValue(false);

//		if (selectedSet.equals(SET_1)) {
//			set_Set1Params(siteParams);
			//set_Set1Params();
//		} else if (selectedSet.equals(SET_2)) {
//			set_Set2Params(siteParams);
//			set_Set2Params();
//		}

		// refresh the editor according to parameter values
		//imrGuiBean.refreshParamEditor();
		//imtGuiBean.refreshParamEditor();
		//siteGuiBean.getParameterListEditor().refreshParamEditor();
		//erfGuiBean.getERFParameterListEditor().refreshParamEditor();
		//timeSpanGuiBean.getParameterListEditor().refreshParamEditor();
	}

	// sets the parameter values for the selected test cases in Set-1
//	private void set_Set1Params(ParameterList siteParams) {
	private void init_Set1() {

		// ******* Set the IMR, IMT, & Site-Related Parameters 
		// (except lat and lon) first ************
		
		// the following settings apply to most test cases; 
		// these are subsequently overridded where needed below
//		imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SadighEtAl_1997_AttenRel.NAME);
//		imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
//		imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
//		imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);
//		siteParams.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		imr = new SadighEtAl_1997_AttenRel(null);
		imr.setParamDefaults();
		imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
		imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
		imr.setIntensityMeasure(PGA_Param.NAME);
		//imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		site.addParameter(imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME));
		site.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME, SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);
		
		if (is(CASE_8A)) {
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.setIntensityMeasure(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
		
		} else if (is(CASE_8B)) {
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(2.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(2.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.setIntensityMeasure(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
		
		} else if (is(CASE_8C)) {
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.setIntensityMeasure(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
		
		} else if (is(CASE_9A)) {
//			imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SadighEtAl_1997_AttenRel.NAME);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);
//			siteParams.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);
			imr = new SadighEtAl_1997_AttenRel(null);
			imr.setParamDefaults();
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.setIntensityMeasure(PGA_Param.NAME);
			//imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

			//site.addParameter(imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME));
			//site.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME, SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		} else if (is(CASE_9B)){
//			imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(AS_1997_AttenRel.NAME);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0)); // this shouldn't matter
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);
//			siteParams.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME).setValue(AS_1997_AttenRel.SITE_TYPE_ROCK);
			imr = new AS_1997_AttenRel(null);
			imr.setParamDefaults();
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0)); // this shouldn't matter
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
			imr.setIntensityMeasure(PGA_Param.NAME);
			
			site.addParameter(imr.getParameter(AS_1997_AttenRel.SITE_TYPE_NAME));
			site.setValue(AS_1997_AttenRel.SITE_TYPE_NAME, AS_1997_AttenRel.SITE_TYPE_ROCK);
			
			//site.addParameter(imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME));
			//site.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME, SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		} else if (is(CASE_9C)){
//			imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(Campbell_1997_AttenRel.NAME);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL_PGA_DEP);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);
//			siteGuiBean.getParameterListEditor().getParameterList().getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
//			siteParams.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
			imr = new Campbell_1997_AttenRel(null);
			imr.setParamDefaults();
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL_PGA_DEP);
			imr.setIntensityMeasure(PGA_Param.NAME);
			//imr.getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME).setValue(Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
			//imr.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME).setValue(new Double(2.0));
			
			site.addParameter(imr.getParameter(Campbell_1997_AttenRel.SITE_TYPE_NAME));
			site.setValue(Campbell_1997_AttenRel.SITE_TYPE_NAME, Campbell_1997_AttenRel.SITE_TYPE_SOFT_ROCK);
			site.addParameter(imr.getParameter(Campbell_1997_AttenRel.BASIN_DEPTH_NAME));
			site.setValue(Campbell_1997_AttenRel.BASIN_DEPTH_NAME, new Double(2.0));

		} else if (is(CASE_12)){
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
//			imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.setIntensityMeasure(PGA_Param.NAME);  // needed because IMT gets reset to SA afer the above
		}


		// *********** Now fill in the ERF parameters ************************
		
		// if it's one of the "PEER fault" problems (cases 1-9 or 12)
		
		if (!is(CASE_10) && !is(CASE_11)) {

			// set the ERF
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(FloatingPoissonFaultERF.NAME);
			erf = new FloatingPoissonFaultERF();
		
			// set offset and fault grid spacing (these were determined by trial and error)
			double gridSpacing;
			if (is(CASE_1) || 
					is(CASE_2) || 
					is(CASE_4) || 
					is(CASE_9B) ) {
				gridSpacing = 0.05;
		   
			} else if (is(CASE_3)) {
				gridSpacing = 0.25;
		   
			} else {
				gridSpacing = 0.5;
			}
		
			// set the special cases (improvements found by hand using GUI)
			if (is(CASE_8C) && is(SITE_5)) {
				gridSpacing = 0.05;
			}
			if (is(CASE_9C) && is(SITE_7)) {
				gridSpacing = 0.1;
			}
			if (is(CASE_2) && 
					(is(SITE_1) || 
							is(SITE_4) || 
							is(SITE_6))) {
				gridSpacing = 0.025;
			}

			// set the common parameters like timespan
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(gridSpacing));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
			erf.getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(gridSpacing));
			erf.getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
			erf.getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
			erf.getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
			erf.getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
			erf.getTimeSpan().setDuration(1.0);

			// magScalingSigma parameter is changed if the test case chosen is 3
			if (is(CASE_3)) {
				erf.getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0.25));
			}

			// set the rake for all cases
			if (is(CASE_4) ||
					is(CASE_9A) ||
					is(CASE_9B) ||
					is(CASE_9C) ) {
				erf.getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(90.0));
			
			} else {
				erf.getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
			}

			// set the Fault Parameter
//			SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
			SimpleFaultParameter fault = (SimpleFaultParameter) erf.getParameter(FloatingPoissonFaultERF.FAULT_PARAM_NAME);
			if (is(CASE_4) ||
					is(CASE_9A) ||
					is(CASE_9B) ||
					is(CASE_9C) ) {
//				faultPanel.setAll(gridSpacing,fault1and2_Lats,fault1and2_Lons,fault2_Dips,fault2_Depths,SimpleFaultParameter.STIRLING);
				fault.setAll(gridSpacing,fault1and2_Lats,fault1and2_Lons,fault2_Dips,fault2_Depths,SimpleFaultParameter.STIRLING);
			
			} else {
//				faultPanel.setAll(gridSpacing,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,SimpleFaultParameter.STIRLING);
				fault.setAll(gridSpacing,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,SimpleFaultParameter.STIRLING);
			}
			
			fault.setEvenlyGriddedSurfaceFromParams();
			
		// it's an area ERF (case 10 or 11)
		} else {
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_AreaForecast.NAME);
//			erfGuiBean.getERFParameterList().getParameter(PEER_AreaForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
//			erfGuiBean.getERFParameterList().getParameter(PEER_AreaForecast.DIP_PARAM_NAME).setValue(new Double(90));
//			erfGuiBean.getERFParameterList().getParameter(PEER_AreaForecast.RAKE_PARAM_NAME).setValue(new Double(0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
			erf = new PEER_AreaForecast();
			erf.getParameter(PEER_AreaForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
			erf.getParameter(PEER_AreaForecast.DIP_PARAM_NAME).setValue(new Double(90));
			erf.getParameter(PEER_AreaForecast.RAKE_PARAM_NAME).setValue(new Double(0));
			erf.getTimeSpan().setDuration(1.0);

			if (is(CASE_10)) {
				erf.getParameter(PEER_AreaForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(5));
				erf.getParameter(PEER_AreaForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
			
			} else {
				erf.getParameter(PEER_AreaForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
				erf.getParameter(PEER_AreaForecast.GRID_PARAM_NAME).setValue(new Double(0.25));   	 
			}
		}
		
		// set magFreqDist parameters using seperate method
//		MagFreqDistParameterEditor magDistEditor = erfGuiBean.getMagDistEditor();
//		setMagDistParams_Set1(magDistEditor);
		initMFD_Set1();
		
		erf.updateForecast();
		
		// *********** set the Site latitude and longitude  *****************

		if (!is(CASE_10) && !is(CASE_11)) {

			// for fault site 1
			if (is(SITE_1)) {
//				siteGuiBean.getParameterListEditor().getParameterList().getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
				site.setLocation(new Location(38.113,-122.000));
			
			} else if (is(SITE_2)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.114));
				site.setLocation(new Location(38.113,-122.114));

			} else if (is(SITE_3)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.111));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.570));
				site.setLocation(new Location(38.111,-122.570));

			} else if (is(SITE_4)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.000));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
				site.setLocation(new Location(38.000,-122.000));

			} else if (is(SITE_5)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.910));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
				site.setLocation(new Location(37.910,-122.000));

			} else if (is(SITE_6)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.225));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
				site.setLocation(new Location(38.225,-122.000));

			} else if (is(SITE_7)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.113));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.886));
				site.setLocation(new Location(38.113,-121.886));
			}
			
		} else { // for area sites

			//siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
			
			if (is(SITE_1)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.0));
				site.setLocation(new Location(38.000,-122.000));
			
			} else if (is(SITE_2)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.550));
				site.setLocation(new Location(37.550,-122.000));

			} else if (is(SITE_3)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.099));
				site.setLocation(new Location(37.099,-122.000));

			} else if (is(SITE_4)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(36.875));
				site.setLocation(new Location(36.875,-122.000));
			}
		}
	}

	// Sets the default magdist values for set-1
//	private void setMagDistParams_Set1(MagFreqDistParameterEditor magEditor) {
	private void initMFD_Set1() {

		// NOTE kinda klunky; each time the mfd is set, the fix and all_but
		// constraints need to be updated manually; too much info encapsulated
		// in mfd param???
		
		MagFreqDistParameter mfd;
		ParameterList plist;
		
		if (!is(CASE_10) && !is(CASE_11)) {
			mfd = (MagFreqDistParameter) erf.getParameter(FloatingPoissonFaultERF.MAG_DIST_PARAM_NAME);
			plist = mfd.getAdjustableParams();
		} else {
			mfd = (MagFreqDistParameter) erf.getParameter(PEER_AreaForecast.MAG_DIST_PARAM_NAME);
			plist = mfd.getAdjustableParams();
		}
		
		// these apply to most (overridden below where not)
//		magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(6));
//		magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(6.5));
//		magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(6));
		plist.getParameter(MIN).setValue(new Double(6));
		plist.getParameter(MAX).setValue(new Double(6.5));
		plist.getParameter(NUM).setValue(new Integer(6));

		if (is(CASE_1) || is(CASE_12)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.5));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.5));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_2)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_3)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_4)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.905e16));

		} else if (is(CASE_5)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.005));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.995));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1000));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.005));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.495));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
			plist.getParameter(DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGRSetAllButOptions());
			plist.getParameter(FIX).setConstraint(mfd.getGRFixOptions());
			plist.getParameter(MIN).setValue(new Double(0.005));
			plist.getParameter(MAX).setValue(new Double(9.995));
			plist.getParameter(NUM).setValue(new Integer(1000));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(0.005));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.495));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(TOT_MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);

		} else if (is(CASE_6)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.005));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.995));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1000));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
//			magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
//			magEditor.getParameter(MagFreqDistParameter.STD_DEV).setValue(new Double(0.25));
//			magEditor.getParameter(MagFreqDistParameter.MEAN).setValue(new Double(6.2));
//			magEditor.getParameter(MagFreqDistParameter.TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
//			magEditor.getParameter(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.19));
			plist.getParameter(DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGaussianDistSetAllButOptions());
			plist.getParameter(MIN).setValue(new Double(0.005));
			plist.getParameter(MAX).setValue(new Double(9.995));
			plist.getParameter(NUM).setValue(new Integer(1000));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
			plist.getParameter(TOT_MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(STD_DEV).setValue(new Double(0.25));
			plist.getParameter(MEAN).setValue(new Double(6.2));
			plist.getParameter(TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
			plist.getParameter(TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.19));

		} else if (is(CASE_7)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.005));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(10.005));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1001));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_CHAR).setValue(new Double(0.49));
//			magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.005));
//			magEditor.getParameter(MagFreqDistParameter.YC_MAG_PRIME).setValue(new Double(5.945));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.445));
//			magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getYCSetAllButOptions());
			plist.getParameter(MIN).setValue(new Double(0.005));
			plist.getParameter(MAX).setValue(new Double(10.005));
			plist.getParameter(NUM).setValue(new Integer(1001));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(YC_DELTA_MAG_CHAR).setValue(new Double(0.49));
			plist.getParameter(YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(0.005));
			plist.getParameter(YC_MAG_PRIME).setValue(new Double(5.945));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.445));
			plist.getParameter(TOT_MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_8A)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_8B)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_8C)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.8e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.8e16));
		
		} else if (is(CASE_9A)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.905e16));
		
		} else if (is(CASE_9B)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.905e16));
		
		} else if (is(CASE_9C)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MAG).setValue(new Double(6.0));
//			magEditor.getParameter(MagFreqDistParameter.MO_RATE).setValue(new Double(1.905e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(SingleMagFreqDist.NAME);
			plist.getParameter(FIX).setConstraint(mfd.getSingleDistFixOptions());
			plist.getParameter(SINGLE_PARAMS_TO_SET).setValue(MagFreqDistParameter.MAG_AND_MO_RATE);
			plist.getParameter(MAG).setValue(new Double(6.0));
			plist.getParameter(MO_RATE).setValue(new Double(1.905e16));
		
		} else if (is(CASE_10)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(5.05));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(.0395));
			plist.getParameter(DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGRSetAllButOptions());
			plist.getParameter(FIX).setConstraint(mfd.getGRFixOptions());
			plist.getParameter(MIN).setValue(new Double(0.05));
			plist.getParameter(MAX).setValue(new Double(9.95));
			plist.getParameter(NUM).setValue(new Integer(100));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(5.05));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.45));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(TOT_CUM_RATE).setValue(new Double(.0395));
		
		} else if (is(CASE_11)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(5.05));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(.0395));
			plist.getParameter(DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGRSetAllButOptions());
			plist.getParameter(FIX).setConstraint(mfd.getGRFixOptions());
			plist.getParameter(MIN).setValue(new Double(0.05));
			plist.getParameter(MAX).setValue(new Double(9.95));
			plist.getParameter(NUM).setValue(new Integer(100));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(5.05));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.45));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(TOT_CUM_RATE).setValue(new Double(.0395));
		}

		// now have the editor create the magFreqDist
//			magEditor.setMagDistFromParams();
		mfd.setMagDist();
	}

	// sets the parameter values for the selected test cases in Set-2
//	private void set_Set2Params(ParameterList siteParams) {
	private void init_Set2() {
		
		// ******* Set the IMR, IMT, & Site-Related Parameters 
		// (except lat and lon) first ************

//		imrGuiBean.getParameterList().getParameter(IMR_GuiBean.IMR_PARAM_NAME).setValue(SadighEtAl_1997_AttenRel.NAME);
//		imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
//		imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
//		imtGuiBean.getParameterList().getParameter(IMT_GuiBean.IMT_PARAM_NAME).setValue(PGA_Param.NAME);
//		siteParams.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);
		imr = new SadighEtAl_1997_AttenRel(null);
		imr.setParamDefaults();
		imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_NONE);
		imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_NONE);
		imr.setIntensityMeasure(PGA_Param.NAME);
		//imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME).setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		site.addParameter(imr.getParameter(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME));
		site.setValue(SadighEtAl_1997_AttenRel.SITE_TYPE_NAME, SadighEtAl_1997_AttenRel.SITE_TYPE_ROCK);

		// change IMR sigma if it's Case 2
		if (is(CASE_2) || is(CASE_5)){
//			imrGuiBean.getParameterList().getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//			imrGuiBean.getParameterList().getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
//			imrGuiBean.getParameterList().getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
			imr.getParameter(SigmaTruncTypeParam.NAME).setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			imr.getParameter(SigmaTruncLevelParam.NAME).setValue(new Double(3.0));
			imr.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
		}

		// ********* set the site latitude and longitude ************
		if (is(CASE_1) || is(CASE_5)) {
			
			if (is(SITE_1) || is(SITE_4)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.1126));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.8860));
				site.setLocation(new Location(38.1126,-121.8860));
			
			} else if (is(SITE_2) || is(SITE_5)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.1800));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.8860));
				site.setLocation(new Location(38.1800,-121.8860));
			
			} else if (is(SITE_3) || is(SITE_6)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.2696));
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.1140));
				site.setLocation(new Location(38.2696,-122.1140));
			}
		
		} else if (is(CASE_2)) {
			//siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122));
			
			if (is(SITE_1)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.5495));
				site.setLocation(new Location(37.5495,-122.000));
			
			} else if (is(SITE_2)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(37.0990));
				site.setLocation(new Location(37.0990,-122.000));
		
			} else if (is(SITE_3)) {
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(36.8737));
				site.setLocation(new Location(36.8737,-122.000));
			}
		
		} else { // all others have the same set of sites
			
			if (is(SITE_1)) {
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-121.886));
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.1126));
				site.setLocation(new Location(38.1126,-121.886));
		
			} else if (is(SITE_2)) {
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.2252));
				site.setLocation(new Location(38.2252,-121.000));
		
			} else if (is(SITE_3)) {
//				siteParams.getParameter(Site_GuiBean.LONGITUDE).setValue(new Double(-122.0));
//				siteParams.getParameter(Site_GuiBean.LATITUDE).setValue(new Double(38.0));
				site.setLocation(new Location(38.000,-122.000));
			}
		}

		// ************ Set the ERF parameters ********************
		
		if (is(CASE_1)){
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_NonPlanarFaultForecast.NAME);
//			// add sigma for maglength(0-1)
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.GR_MAG_UPPER).setValue(new Double(6.95));
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.SLIP_RATE_NAME).setValue(new Double(2.0));
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.SEGMENTATION_NAME).setValue(PEER_NonPlanarFaultForecast.SEGMENTATION_NO);
//			erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.FAULT_MODEL_NAME).setValue(PEER_NonPlanarFaultForecast.FAULT_MODEL_STIRLING);
			erf = new PEER_NonPlanarFaultForecast();
			// add sigma for maglength(0-1)
			erf.getParameter(PEER_NonPlanarFaultForecast.SIGMA_PARAM_NAME).setValue(new Double(0));
			erf.getTimeSpan().setDuration(1.0);
			erf.getParameter(PEER_NonPlanarFaultForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
			erf.getParameter(PEER_NonPlanarFaultForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
			erf.getParameter(PEER_NonPlanarFaultForecast.GR_MAG_UPPER).setValue(new Double(6.95));
			erf.getParameter(PEER_NonPlanarFaultForecast.SLIP_RATE_NAME).setValue(new Double(2.0));
			erf.getParameter(PEER_NonPlanarFaultForecast.SEGMENTATION_NAME).setValue(PEER_NonPlanarFaultForecast.SEGMENTATION_NO);
			erf.getParameter(PEER_NonPlanarFaultForecast.FAULT_MODEL_NAME).setValue(PEER_NonPlanarFaultForecast.FAULT_MODEL_STIRLING);
			
			// set the dip direction depending on the chosen
			if (is(SITE_1) || is(SITE_2) || is(SITE_3)) {
				
//				erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_EAST);
				erf.getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_EAST);

			} else {
//				erfGuiBean.getERFParameterList().getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_WEST);
				erf.getParameter(PEER_NonPlanarFaultForecast.DIP_DIRECTION_NAME).setValue(PEER_NonPlanarFaultForecast.DIP_DIRECTION_WEST);
			}
			
		} else if (is(CASE_2)) {
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_MultiSourceForecast.NAME);
//			erfGuiBean.getERFParameterList().getParameter(PEER_MultiSourceForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
//			erfGuiBean.getERFParameterList().getParameter(PEER_MultiSourceForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
//			erfGuiBean.getERFParameterList().getParameter(PEER_MultiSourceForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(PEER_MultiSourceForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
			erf = new PEER_MultiSourceForecast();
			erf.getParameter(PEER_MultiSourceForecast.DEPTH_LOWER_PARAM_NAME).setValue(new Double(10));
			erf.getParameter(PEER_MultiSourceForecast.DEPTH_UPPER_PARAM_NAME).setValue(new Double(5));
			erf.getParameter(PEER_MultiSourceForecast.GRID_PARAM_NAME).setValue(new Double(1.0));
			erf.getParameter(PEER_MultiSourceForecast.OFFSET_PARAM_NAME).setValue(new Double(1.0));
			erf.getTimeSpan().setDuration(1.0);
		
		} else if (is(CASE_3) || is(CASE_4) ) {

//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(FloatingPoissonFaultERF.NAME);
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
			erf = new FloatingPoissonFaultERF();
			erf.getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
			erf.getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
			erf.getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
			erf.getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
			erf.getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
			erf.getTimeSpan().setDuration(1.0);
			erf.getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));

			// set the Fault Parameter
//			SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
//			faultPanel.setAll(1.0,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,SimpleFaultParameter.STIRLING);
//			faultPanel.setEvenlyGriddedSurfaceFromParams();
			SimpleFaultParameter fault = (SimpleFaultParameter) erf.getParameter(FloatingPoissonFaultERF.FAULT_PARAM_NAME);
			fault.setAll(1.0,fault1and2_Lats,fault1and2_Lons,fault1_Dips,fault1_Depths,SimpleFaultParameter.STIRLING);
			fault.setEvenlyGriddedSurfaceFromParams();


		} else if (is(CASE_5) ) {
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(PEER_LogicTreeERF_List.NAME);
//			erfGuiBean.getERFParameterList().getParameter(PEER_LogicTreeERF_List.FAULT_MODEL_NAME).setValue(PEER_LogicTreeERF_List.FAULT_MODEL_STIRLING);
//			erfGuiBean.getERFParameterList().getParameter(PEER_LogicTreeERF_List.OFFSET_PARAM_NAME).setValue(new Double(1));
//			erfGuiBean.getERFParameterList().getParameter(PEER_LogicTreeERF_List.GRID_PARAM_NAME).setValue(new Double(1));
//			erfGuiBean.getERFParameterList().getParameter(PEER_LogicTreeERF_List.SIGMA_PARAM_NAME).setValue(new Double(0.0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));

			// NOTE Set2 Case5 disabled as PEER test runners are not 
			// yet set up to handle epistemic lists
//			erf = new PEER_LogicTreeERF_List(); 
//			erf.getParameter(PEER_LogicTreeERF_List.FAULT_MODEL_NAME).setValue(PEER_LogicTreeERF_List.FAULT_MODEL_STIRLING);
//			erf.getParameter(PEER_LogicTreeERF_List.OFFSET_PARAM_NAME).setValue(new Double(1));
//			erf.getParameter(PEER_LogicTreeERF_List.GRID_PARAM_NAME).setValue(new Double(1));
//			erf.getParameter(PEER_LogicTreeERF_List.SIGMA_PARAM_NAME).setValue(new Double(0.0));
//			erf.getTimeSpan().setDuration(1.0);
		
		} else if (is(CASE_6)){
//			erfGuiBean.getERFParameterList().getParameter(ERF_GuiBean.ERF_PARAM_NAME).setValue(FloatingPoissonFaultERF.NAME);
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
//			timeSpanGuiBean.getParameterList().getParameter(TimeSpan.DURATION).setValue(new Double(1.0));
//			erfGuiBean.getERFParameterList().getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));
			erf = new FloatingPoissonFaultERF();
			erf.getParameter(FloatingPoissonFaultERF.OFFSET_PARAM_NAME).setValue(new Double(1.0));
			erf.getParameter(FloatingPoissonFaultERF.MAG_SCALING_REL_PARAM_NAME).setValue(PEER_testsMagAreaRelationship.NAME);
			erf.getParameter(FloatingPoissonFaultERF.SIGMA_PARAM_NAME).setValue(new Double(0));
			erf.getParameter(FloatingPoissonFaultERF.ASPECT_RATIO_PARAM_NAME).setValue(new Double(2.0));
			erf.getParameter(FloatingPoissonFaultERF.MIN_MAG_PARAM_NAME).setValue(new Double(5.0));
			erf.getTimeSpan().setDuration(1.0);
			erf.getParameter(FloatingPoissonFaultERF.RAKE_PARAM_NAME).setValue(new Double(0.0));

			// set the Fault Parameter
//			SimpleFaultParameterEditorPanel faultPanel = erfGuiBean.getSimpleFaultParamEditor().getParameterEditorPanel();
//			faultPanel.setAll(1.0,faultE_Lats,faultE_Lons,faultE_Dips,faultE_Depths,SimpleFaultParameter.STIRLING);
//			faultPanel.setEvenlyGriddedSurfaceFromParams();
			SimpleFaultParameter fault = (SimpleFaultParameter) erf.getParameter(FloatingPoissonFaultERF.FAULT_PARAM_NAME);
			fault.setAll(1.0,faultE_Lats,faultE_Lons,faultE_Dips,faultE_Depths,SimpleFaultParameter.STIRLING);
			fault.setEvenlyGriddedSurfaceFromParams();
		}

		
		// now set the magFreqDist parameters (if there is one) using the separate method
//		MagFreqDistParameterEditor magDistEditor = erfGuiBean.getMagDistEditor();
//		if (magDistEditor !=null)  setMagDistParams_Set2(magDistEditor);
		initMFD_Set2();
		
		erf.updateForecast();
	}

	// sets the default magdist values for the set 2
	// (only cases 3, 4, and 6 have magFreqDist as an adjustable parameter)
//	private void setMagDistParams_Set2(MagFreqDistParameterEditor magEditor) {
	private void initMFD_Set2() {
		
		MagFreqDistParameter mfd;
		if (is(CASE_3) || is(CASE_4) || is(CASE_6)) {
			mfd = (MagFreqDistParameter) erf.getParameter(FloatingPoissonFaultERF.MAG_DIST_PARAM_NAME);
			ParameterList plist = mfd.getAdjustableParams();
		
		if (is(CASE_3)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.0));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(10));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(1001));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_CHAR).setValue(new Double(.5));
//			magEditor.getParameter(MagFreqDistParameter.YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.01));
//			magEditor.getParameter(MagFreqDistParameter.YC_MAG_PRIME).setValue(new Double(5.95));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.YC_TOT_CHAR_RATE).setValue(new Double(1e-3));
			plist.getParameter(DISTRIBUTION_NAME).setValue(YC_1985_CharMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getYCSetAllButOptions());
			plist.getParameter(MIN).setValue(new Double(0.0));
			plist.getParameter(MAX).setValue(new Double(10));
			plist.getParameter(NUM).setValue(new Integer(1001));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(YC_DELTA_MAG_CHAR).setValue(new Double(.5));
			plist.getParameter(YC_DELTA_MAG_PRIME).setValue(new Double(1.0));
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(0.01));
			plist.getParameter(YC_MAG_PRIME).setValue(new Double(5.95));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.45));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
			plist.getParameter(YC_TOT_CHAR_RATE).setValue(new Double(1e-3));
		
		} else if (is(CASE_4)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
//			magEditor.getParameter(MagFreqDistParameter.TOT_CUM_RATE).setValue(new Double(1e-3));
//			magEditor.getParameter(MagFreqDistParameter.STD_DEV).setValue(new Double(0.25));
//			magEditor.getParameter(MagFreqDistParameter.MEAN).setValue(new Double(6.2));
//			magEditor.getParameter(MagFreqDistParameter.TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
//			magEditor.getParameter(MagFreqDistParameter.TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
			plist.getParameter(DISTRIBUTION_NAME).setValue(GaussianMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGaussianDistSetAllButOptions());
			plist.getParameter(MIN).setValue(new Double(0.05));
			plist.getParameter(MAX).setValue(new Double(9.95));
			plist.getParameter(NUM).setValue(new Integer(100));
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_MO_RATE);
			plist.getParameter(TOT_CUM_RATE).setValue(new Double(1e-3));
			plist.getParameter(STD_DEV).setValue(new Double(0.25));
			plist.getParameter(MEAN).setValue(new Double(6.2));
			plist.getParameter(TRUNCATION_REQ).setValue(MagFreqDistParameter.TRUNCATE_UPPER_ONLY);
			plist.getParameter(TRUNCATE_NUM_OF_STD_DEV).setValue(new Double(1.0));
		
		} else if (is(CASE_6)) {
//			magEditor.getParameter(MagFreqDistParameter.DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
//			magEditor.getParameter(MagFreqDistParameter.SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
//			magEditor.getParameter(MagFreqDistParameter.MIN).setValue(new Double(0.05));
//			magEditor.getParameter(MagFreqDistParameter.MAX).setValue(new Double(9.95));
//			magEditor.getParameter(MagFreqDistParameter.NUM).setValue(new Integer(100));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_LOWER).setValue(new Double(0.05));
//			magEditor.getParameter(MagFreqDistParameter.GR_MAG_UPPER).setValue(new Double(6.45));
//			magEditor.getParameter(MagFreqDistParameter.GR_BVALUE).setValue(new Double(0.9));
//			magEditor.getParameter(MagFreqDistParameter.TOT_MO_RATE).setValue(new Double(3.8055e16));
			plist.getParameter(DISTRIBUTION_NAME).setValue(GutenbergRichterMagFreqDist.NAME);
			plist.getParameter(SET_ALL_PARAMS_BUT).setConstraint(mfd.getGRSetAllButOptions());
			plist.getParameter(FIX).setConstraint(mfd.getGRFixOptions());
			plist.getParameter(SET_ALL_PARAMS_BUT).setValue(MagFreqDistParameter.TOT_CUM_RATE);
			plist.getParameter(MIN).setValue(new Double(0.05));
			plist.getParameter(MAX).setValue(new Double(9.95));
			plist.getParameter(NUM).setValue(new Integer(100));
			plist.getParameter(GR_MAG_LOWER).setValue(new Double(0.05));
			plist.getParameter(GR_MAG_UPPER).setValue(new Double(6.45));
			plist.getParameter(GR_BVALUE).setValue(new Double(0.9));
			plist.getParameter(TOT_MO_RATE).setValue(new Double(3.8055e16));
		}

		// now have the editor create the magFreqDist
//		magEditor.setMagDistFromParams();
		mfd.setMagDist();
		}
	}

	// fault-data vectors needed for the tests that utilize
	// FloatingPoissonFaultERF
	private void initFaultData() {

		// Set1 faults
		fault1and2_Lats = new ArrayList();
		fault1and2_Lats.add(new Double(38.22480));
		fault1and2_Lats.add(new Double(38.0));

		fault1and2_Lons = new ArrayList();
		fault1and2_Lons.add(new Double(-122.0));
		fault1and2_Lons.add(new Double(-122.0));

		fault1_Dips = new ArrayList();
		fault1_Dips.add(new Double(90.0));

		fault1_Depths = new ArrayList();
		fault1_Depths.add(new Double(0.0));
		fault1_Depths.add(new Double(12.0));

		fault2_Dips = new ArrayList();
		fault2_Dips.add(new Double(60.0));

		fault2_Depths = new ArrayList();
		fault2_Depths.add(new Double(1.0));
		fault2_Depths.add(new Double(12.0));

		// Set2 faults
		faultE_Lats = new ArrayList();
		faultE_Lats.add(new Double(38.0));
		faultE_Lats.add(new Double(38.2248));

		faultE_Lons = new ArrayList();
		faultE_Lons.add(new Double(-122.0));
		faultE_Lons.add(new Double(-122.0));

		faultE_Dips = new ArrayList();
		faultE_Dips.add(new Double(50.0));
		faultE_Dips.add(new Double(20.0));

		faultE_Depths = new ArrayList();
		faultE_Depths.add(new Double(0.0));
		faultE_Depths.add(new Double(6.0));
		faultE_Depths.add(new Double(12.0));
	}

	// init function with log of xVals
	private void initFunction() {
		function = new ArbitrarilyDiscretizedFunc();
		for (double val : xVals) {
			function.set(Math.log(val), 1.0);
		}
		
//		function.set(.001, 1);
//		function.set(.01, 1);
//		function.set(.05, 1);
//		function.set(.1, 1);
//		function.set(.15, 1);
//		function.set(.2, 1);
//		function.set(.25, 1);
//		function.set(.3, 1);
//		function.set(.35, 1);
//		function.set(.4, 1);
//		function.set(.45, 1);
//		function.set(.5, 1);
//		function.set(.55, 1);
//		function.set(.6, 1);
//		function.set(.7, 1);
//		// function.set(.75,1);
//		function.set(.8, 1);
//		function.set(.9, 1);
//		function.set(1.0, 1);
//		// function.set(1.1,1);
//		// function.set(1.2,1);
//		// function.set(1.25,1);
//		// function.set(1.3,1);
//		// function.set(1.4,1);
//		// function.set(1.5,1);
		
		
	}

	// revert function to non-log X values
	public static DiscretizedFuncAPI functionFromLogX(DiscretizedFuncAPI in) {
		DiscretizedFuncAPI out = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<xVals.length; i++) {
			out.set(xVals[i], in.getY(i));
		}
		return out;
	}

	public static ArrayList<PeerTest> getSetOneDecriptors() {
		return desc1;
	}

	public static ArrayList<PeerTest> getSetTwoDecriptors() {
		return desc2;
	}

	static {

		desc1 = new ArrayList<PeerTest>();

		// indices and sample run times in comments
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_1));  //   0
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_2));  //   1
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_3));  //   2
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_4));  //   3
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_5));  //   4
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_6));  //   5
		desc1.add(new PeerTest(SET_1, CASE_1, SITE_7));  //   6 

		desc1.add(new PeerTest(SET_1, CASE_2, SITE_1));  //   7
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_2));  //   8
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_3));  //   9
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_4));  //  10
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_5));  //  11
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_6));  //  12
		desc1.add(new PeerTest(SET_1, CASE_2, SITE_7));  //  13

		desc1.add(new PeerTest(SET_1, CASE_3, SITE_1));  //  14
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_2));  //  15
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_3));  //  16
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_4));  //  17
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_5));  //  18
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_6));  //  19
		desc1.add(new PeerTest(SET_1, CASE_3, SITE_7));  //  20

		desc1.add(new PeerTest(SET_1, CASE_4, SITE_1));  //  21
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_2));  //  22
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_3));  //  23
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_4));  //  24
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_5));  //  25
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_6));  //  26
		desc1.add(new PeerTest(SET_1, CASE_4, SITE_7));  //  27

		desc1.add(new PeerTest(SET_1, CASE_5, SITE_1));  //  28
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_2));  //  29
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_3));  //  30
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_4));  //  31
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_5));  //  32
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_6));  //  33
		desc1.add(new PeerTest(SET_1, CASE_5, SITE_7));  //  34

		desc1.add(new PeerTest(SET_1, CASE_6, SITE_1));  //  35
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_2));  //  36
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_3));  //  37
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_4));  //  38
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_5));  //  39
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_6));  //  40
		desc1.add(new PeerTest(SET_1, CASE_6, SITE_7));  //  41

		desc1.add(new PeerTest(SET_1, CASE_7, SITE_1));  //  42
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_2));  //  43
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_3));  //  44
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_4));  //  45
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_5));  //  46
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_6));  //  47
		desc1.add(new PeerTest(SET_1, CASE_7, SITE_7));  //  48

		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_1)); //  49
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_2)); //  50
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_3)); //  51
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_4)); //  52
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_5)); //  53
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_6)); //  54
		desc1.add(new PeerTest(SET_1, CASE_8A, SITE_7)); //  55

		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_1)); //  56
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_2)); //  57
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_3)); //  58
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_4)); //  59
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_5)); //  60
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_6)); //  61
		desc1.add(new PeerTest(SET_1, CASE_8B, SITE_7)); //  62

		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_1)); //  63
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_2)); //  64
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_3)); //  65
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_4)); //  66
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_5)); //  67
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_6)); //  68
		desc1.add(new PeerTest(SET_1, CASE_8C, SITE_7)); //  69

		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_1)); //  70
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_2)); //  71
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_3)); //  72
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_4)); //  73
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_5)); //  74
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_6)); //  75
		desc1.add(new PeerTest(SET_1, CASE_9A, SITE_7)); //  76

		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_1)); //  77
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_2)); //  78
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_3)); //  79
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_4)); //  80
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_5)); //  81
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_6)); //  82
		desc1.add(new PeerTest(SET_1, CASE_9B, SITE_7)); //  83

		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_1)); //  84
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_2)); //  85
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_3)); //  86
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_4)); //  87
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_5)); //  88
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_6)); //  89
		desc1.add(new PeerTest(SET_1, CASE_9C, SITE_7)); //  90

		desc1.add(new PeerTest(SET_1, CASE_10, SITE_1)); //  91
		desc1.add(new PeerTest(SET_1, CASE_10, SITE_2)); //  92
		desc1.add(new PeerTest(SET_1, CASE_10, SITE_3)); //  93
		desc1.add(new PeerTest(SET_1, CASE_10, SITE_4)); //  94

		desc1.add(new PeerTest(SET_1, CASE_11, SITE_1)); //  95
		desc1.add(new PeerTest(SET_1, CASE_11, SITE_2)); //  96
		desc1.add(new PeerTest(SET_1, CASE_11, SITE_3)); //  97
		desc1.add(new PeerTest(SET_1, CASE_11, SITE_4)); //  98

		desc1.add(new PeerTest(SET_1, CASE_12, SITE_1)); //  99
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_2)); // 100
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_3)); // 101
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_4)); // 102
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_5)); // 103
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_6)); // 104
		desc1.add(new PeerTest(SET_1, CASE_12, SITE_7)); // 105

		desc2 = new ArrayList<PeerTest>();

		desc2.add(new PeerTest(SET_2, CASE_1, SITE_1)); // 
		desc2.add(new PeerTest(SET_2, CASE_1, SITE_2)); // 
		desc2.add(new PeerTest(SET_2, CASE_1, SITE_3)); // 
		desc2.add(new PeerTest(SET_2, CASE_1, SITE_4)); // 
		desc2.add(new PeerTest(SET_2, CASE_1, SITE_5)); // 
		desc2.add(new PeerTest(SET_2, CASE_1, SITE_6)); // 
		 
		desc2.add(new PeerTest(SET_2, CASE_2, SITE_1)); // 
		desc2.add(new PeerTest(SET_2, CASE_2, SITE_2)); // 
		desc2.add(new PeerTest(SET_2, CASE_2, SITE_3)); // 

		desc2.add(new PeerTest(SET_2, CASE_3, SITE_1)); // 
		desc2.add(new PeerTest(SET_2, CASE_3, SITE_2)); // 
		desc2.add(new PeerTest(SET_2, CASE_3, SITE_3)); // 

		desc2.add(new PeerTest(SET_2, CASE_4, SITE_1)); // 
		desc2.add(new PeerTest(SET_2, CASE_4, SITE_2)); // 
		desc2.add(new PeerTest(SET_2, CASE_4, SITE_3)); // 

		// NOTE Set2 Case5 disabled as PEER test runners are not 
		// yet set up to handle epistemic lists
		// desc2.add(new TestDescriptor(SET_2, CASE_5, SITE_1)); // 
		// desc2.add(new TestDescriptor(SET_2, CASE_5, SITE_2)); // 
		// desc2.add(new TestDescriptor(SET_2, CASE_5, SITE_3)); // 

		desc2.add(new PeerTest(SET_2, CASE_6, SITE_1)); // 
		desc2.add(new PeerTest(SET_2, CASE_6, SITE_2)); // 
		desc2.add(new PeerTest(SET_2, CASE_6, SITE_3)); // 
	}
	
	private boolean is(TestSet testSet) {
		return selectedSet.equals(testSet);
	}

	private boolean is(TestCase testCase) {
		return selectedCase.equals(testCase);
	}

	private boolean is(TestSite testSite) {
		return selectedSite.equals(testSite);
	}

}
