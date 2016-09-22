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

package org.opensha.sha.cybershake.plot;

import java.awt.Color;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.CachedSiteDataWrapper;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteData;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.data.siteData.impl.CVM2BasinDepth;
import org.opensha.commons.data.siteData.impl.CVM4BasinDepth;
import org.opensha.commons.data.siteData.impl.CVM4i26BasinDepth;
import org.opensha.commons.data.siteData.impl.CVMHBasinDepth;
import org.opensha.commons.data.siteData.impl.CVM_CCAi6BasinDepth;
import org.opensha.commons.data.siteData.impl.CVM_Vs30;
import org.opensha.commons.data.siteData.impl.ConstantValueDataProvider;
import org.opensha.commons.data.siteData.impl.WillsMap2000;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.data.siteData.impl.CVM_Vs30.CVM;
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.gui.UserAuthDialog;
import org.opensha.commons.gui.plot.GraphPanel;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.impl.DoubleDiscreteParameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeHazardCurveRecord;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeRuptureVariation;
import org.opensha.sha.cybershake.db.CybershakeSGTVariation;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeVelocityModel;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.MeanUCERF2_ToDB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.db.CybershakeIM.IMType;
import org.opensha.sha.cybershake.gui.CyberShakeDBManagementApp;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.Component;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sha.util.component.ComponentConverter;
import org.opensha.sha.util.component.ComponentTranslation;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;


public class HazardCurvePlotter {
	
	public static final PlotType PLOT_TYPE_DEFAULT = PlotType.PDF;
	
	protected static final String default_periods = "3";
	private static final IMType defaultIMType = IMType.SA;
	private static final CyberShakeComponent defaultComponent = CyberShakeComponent.GEOM_MEAN;
	
	private double maxSourceDistance = 200;
	
	public static final int PLOT_WIDTH_DEFAULT = 600;
	public static final int PLOT_HEIGHT_DEFAULT = 500;
	
	private int plotWidth = PLOT_WIDTH_DEFAULT;
	private int plotHeight = PLOT_HEIGHT_DEFAULT;
	
	private DBAccess db;
//	private int erfID;
//	private int rupVarScenarioID;
//	private int sgtVarID;
	
	private HazardCurve2DB curve2db;
	
	private HeadlessGraphPanel gp;
	
	private AbstractERF erf = null;
	private ArrayList<AttenuationRelationship> attenRels = new ArrayList<AttenuationRelationship>();
	
	private SiteInfo2DB site2db = null;
	private PeakAmplitudesFromDB amps2db = null;
	private Runs2DB runs2db = null;
	
	private CybershakeSite csSite = null;
	
	private HazardCurveCalculator calc;
	
//	CybershakeERF selectedERF;
	
	protected static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
	
	private double manualVs30  = -1;
	
	private double currentPeriod = -1;
	
	private HazardCurvePlotCharacteristics plotChars = HazardCurvePlotCharacteristics.createRobPlotChars();
	
	private OrderedSiteDataProviderList dataProviders;
	
	private String user = "";
	private String pass = "";
	
	private HazardCurveComputation curveCalc;
	
	private static DecimalFormat period_format = new DecimalFormat("0.##");
	
	private boolean useCVMVs30 = false;
	private ConstantValueDataProvider<Double> forcedVs30 = null;
	
	public HazardCurvePlotter(DBAccess db) {
		this.db = db;
		
		init();
	}
	
	private static int getRunID(Runs2DB runs2db, HazardCurve2DB curve2db, PeakAmplitudesFromDB amps2db,
			int siteID, int erfID, int rupVarScenarioID, int sgtVarID, int velModelID) {
		ArrayList<Integer> runIDs = runs2db.getRunIDs(siteID, erfID, sgtVarID, rupVarScenarioID, velModelID, null, null, null, null);
		if (runIDs == null || runIDs.size() == 0)
			return -1;
		int id = -1;
		if (runIDs.size() == 1) {
			id = runIDs.get(0);
		} else {
			// we want to select a runID that has data, if available.
			// we favor the first one with curves, or if that doesn't exist, the first one
			// with amplitudes
			int ampsID = -1;
			for (int runID : runIDs) {
				ArrayList<CybershakeHazardCurveRecord> curves = curve2db.getHazardCurveRecordsForRun(runID);
				if (curves != null && curves.size() > 0) {
					id = runID;
					break;
				} else if (amps2db.hasAmps(runID) && ampsID == -1) {
					ampsID = runID;
				}
			}
			if (id < 0 && ampsID >= 0)
				id = ampsID;
		}
		System.out.println("Detected runID '" + id + "' from " + runIDs.size() + " matches");
		return id;
	}
	
	private void init() {
		PlotPreferences plotPrefs = PlotPreferences.getDefault();
		plotPrefs.setAxisLabelFontSize(12);
		plotPrefs.setTickLabelFontSize(12);
		plotPrefs.setPlotLabelFontSize(14);
		gp = new HeadlessGraphPanel(plotPrefs);
		gp.setBackgroundColor(null);
		gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		
		runs2db = new Runs2DB(db);
		curve2db = new HazardCurve2DB(this.db);
		
		calc = new HazardCurveCalculator();
		calc.setMaxSourceDistance(maxSourceDistance);
	}
	
	public static List<SiteData<?>> getBBP_1D_Providers() {
		ArrayList<SiteData<?>> providers = new ArrayList<SiteData<?>>();
		
		// BBP LA 1D model.
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_2_5,
				SiteData.TYPE_FLAG_INFERRED, 0.0225d, "BBP 1-D LA model", "BBP-1D"));
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_1_0,
				SiteData.TYPE_FLAG_INFERRED, 3.500d, "BBP 1-D LA model", "BBP-1D"));
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_VS30,
				SiteData.TYPE_FLAG_INFERRED, 843.189d, "BBP 1-D LA model Vs30", "BBP-1D"));
		
		return providers;
	}
	
	public static List<SiteData<?>> getCCA_1D_Providers() {
		ArrayList<SiteData<?>> providers = new ArrayList<SiteData<?>>();
		
		// CCA LA 1D model, via e-mail from David Gill 9/22/16
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_2_5,
				SiteData.TYPE_FLAG_INFERRED, 0.0d, "CCA 1-D model", "CCA-1D"));
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_1_0,
				SiteData.TYPE_FLAG_INFERRED, 5.110d, "CCA 1-D model", "CCA-1D"));
		providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_VS30,
				SiteData.TYPE_FLAG_INFERRED, 1987.291d, "CCA 1-D model Vs30", "CCA-1D"));
		
		return providers;
	}
	
	private OrderedSiteDataProviderList getProviders(int velModelID) {
		if (dataProviders == null) {
			dataProviders = createProviders(velModelID, useCVMVs30);
			if (forcedVs30 != null) {
				for (int i=dataProviders.size(); --i>=0;)
					if (dataProviders.getProvider(i).getDataType().equals(SiteData.TYPE_VS30))
						dataProviders.remove(i);
				dataProviders.add(0, forcedVs30);
			}
		}

		return dataProviders;
	}
	
	public static OrderedSiteDataProviderList createProviders(int velModelID) {
		return createProviders(velModelID, false);
	}
	
	public static OrderedSiteDataProviderList createProviders(int velModelID, boolean useCVMVs30) {
		ArrayList<SiteData<?>> providers = new ArrayList<SiteData<?>>();

		if (velModelID == 6) {
			// Hadley-Kanamori 1D model. Set to 0KM (as per e-mail from David Gill 1/17/14)
			providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_2_5,
					SiteData.TYPE_FLAG_INFERRED, 0d, "Hadley-Kanamori 1D model", "HK-1D"));
			providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_DEPTH_TO_1_0,
					SiteData.TYPE_FLAG_INFERRED, 0d, "Hadley-Kanamori 1D model", "HK-1D"));
			providers.add(new ConstantValueDataProvider<Double>(SiteData.TYPE_VS30,
					SiteData.TYPE_FLAG_INFERRED, 2886d, "Hadley-Kanamori 1D model Vs30", "HK-1D"));
		} else if (velModelID == 8) {
			providers.addAll(getBBP_1D_Providers());
		} else if (velModelID == 9) {
			providers.addAll(getCCA_1D_Providers());
		} else {
			if (useCVMVs30) {
				if (velModelID == 5) {
					try {
						providers.add(new CachedSiteDataWrapper<Double>(new CVM_Vs30(CVM.CVMS4i26)));
					} catch (IOException e1) {
						ExceptionUtils.throwAsRuntimeException(e1);
					}
				} else {
					throw new IllegalStateException("Velocity Model Vs30 not yet available for Vel_Model_ID="+velModelID);
				}
			} else {
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new WillsMap2006()));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			}
			if (velModelID == 1) {
				/*		CVM4 Depth to 2.5					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_2_5)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}

				/*		CVM4 Depth to 1.0					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM4BasinDepth(SiteData.TYPE_DEPTH_TO_1_0)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			} else if (velModelID == 2 || velModelID == 4 || velModelID == 7) {
				/*		CVMH Depth to 2.5					 */
				boolean includeGTL = velModelID != 7;
				try {
					CVMHBasinDepth cvmh = new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_2_5);
					cvmh.getAdjustableParameterList().setValue(CVMHBasinDepth.GTL_PARAM_NAME, includeGTL);
					providers.add(new CachedSiteDataWrapper<Double>(cvmh));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}

				/*		CVMH Depth to 1.0					 */
				try {
					CVMHBasinDepth cvmh = new CVMHBasinDepth(SiteData.TYPE_DEPTH_TO_1_0);
					cvmh.getAdjustableParameterList().setValue(CVMHBasinDepth.GTL_PARAM_NAME, includeGTL);
					providers.add(new CachedSiteDataWrapper<Double>(cvmh));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			} else if (velModelID == 5) {
				/*		CVM4i26 Depth to 2.5					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM4i26BasinDepth(SiteData.TYPE_DEPTH_TO_2_5)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}

				/*		CVM4i26 Depth to 1.0					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM4i26BasinDepth(SiteData.TYPE_DEPTH_TO_1_0)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			} else if (velModelID == 10) {
				/*		CVM4i26 Depth to 2.5					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM_CCAi6BasinDepth(SiteData.TYPE_DEPTH_TO_2_5)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}

				/*		CVM4i26 Depth to 1.0					 */
				try {
					providers.add(new CachedSiteDataWrapper<Double>(new CVM_CCAi6BasinDepth(SiteData.TYPE_DEPTH_TO_1_0)));
				} catch (IOException e) {
					ExceptionUtils.throwAsRuntimeException(e);
				}
			} else {
				System.err.println("Unknown Velocity Model ID: "+velModelID);
				System.exit(1);
			}
		}
		return new OrderedSiteDataProviderList(providers);
	}
	
	public static ArrayList<String> commaSplit(String str) {
		str = str.trim();
		ArrayList<String> vals = new ArrayList<String>();
		for (String val : str.split(",")) {
			val = val.trim();
			vals.add(val);
		}
		return vals;
	}
	
	public static ArrayList<Double> commaDoubleSplit(String str) {
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (String val : commaSplit(str)) {
			vals.add(Double.parseDouble(val));
		}
		
		return vals;
	}
	
	protected static int getRunIDFromOptions(
			Runs2DB runs2db,
			HazardCurve2DB curve2db,
			PeakAmplitudesFromDB amps2db,
			SiteInfo2DB site2db,
			CommandLine cmd) {
		if (cmd.hasOption("run-id")) {
			int runID = Integer.parseInt(cmd.getOptionValue("run-id"));
			if (runID < 0) {
				System.err.println("Invalid run id '" + runID + "'!");
				return -1;
			}
			return runID;
		} else {
			if (!cmd.hasOption("site"))
				throw new IllegalArgumentException("Neither a run ID nor site short name was specified!");
			String siteName = cmd.getOptionValue("site");
			int siteID = site2db.getSiteId(siteName);
			int erfID;
			if (cmd.hasOption("erf-id"))
				erfID = Integer.parseInt(cmd.getOptionValue("erf-id"));
			else
				erfID = -1;
			int rupVarScenarioID;
			if (cmd.hasOption("rv-id"))
				rupVarScenarioID = Integer.parseInt(cmd.getOptionValue("rv-id"));
			else
				rupVarScenarioID = -1;
			int sgtVarID;
			if (cmd.hasOption("sgt-var-id"))
				sgtVarID = Integer.parseInt(cmd.getOptionValue("sgt-var-id"));
			else
				sgtVarID = -1;
			int velModelID;
			if (cmd.hasOption("vel-model-id"))
				velModelID = Integer.parseInt(cmd.getOptionValue("vel-model-id"));
			else
				velModelID = -1;
			int runID = getRunID(runs2db, curve2db, amps2db, siteID, erfID, rupVarScenarioID, sgtVarID, velModelID);
			if (runID < 0) {
				System.err.println("No suitable run ID found! siteID: " + siteID + ", erfID: " + erfID +
						", rupVarScenarioID: " + rupVarScenarioID + ", sgtVarID: " + sgtVarID + ", velModelID: "+velModelID);
				return -1;
			}
			return runID;
		}
	}
	
	public boolean plotCurvesFromOptions(CommandLine cmd) {
		SiteInfo2DB site2db = getSite2DB();
		PeakAmplitudesFromDB amps2db = getAmps2DB();
		
		int runID = getRunIDFromOptions(runs2db, curve2db, amps2db, site2db, cmd);
		if (runID < 0)
			return false;
		
		CybershakeRun run = runs2db.getRun(runID);
		if (run == null) {
			System.err.println("Invalid run id '" + runID + "'!");
			return false;
		}
		
		ArrayList<CybershakeRun> runCompares = null;
		if (cmd.hasOption("compare-to")) {
			runCompares = new ArrayList<CybershakeRun>();
			for (String str : commaSplit(cmd.getOptionValue("compare-to"))) {
				int compareID = Integer.parseInt(str);
				if (compareID < 0) {
					System.err.println("Invalid comparison run id: "+compareID);
					return false;
				}
				CybershakeRun runCompare = runs2db.getRun(compareID);
				if (runCompare == null) {
					System.err.println("Unknown comparison run id: "+compareID);
					return false;
				}
				System.out.println("Comparing to: "+runCompare);
				runCompares.add(runCompare);
			}
		}
		
		useCVMVs30 = cmd.hasOption("cvm-vs30");
		if (cmd.hasOption("force-vs30")) {
			double value = Double.parseDouble(cmd.getOptionValue("force-vs30"));
			forcedVs30 = new ConstantValueDataProvider<Double>(SiteData.TYPE_VS30, SiteData.TYPE_FLAG_INFERRED, value);
			System.out.println("Forcing GMPEs to use specified Vs30 value: "+value);
		} else {
			forcedVs30 = null;
		}
		
		String siteName = site2db.getSiteFromDB(run.getSiteID()).short_name;
		
		System.out.println("Plotting curve(s) for site " + siteName + "=" + run.getSiteID() + ", RunID=" + runID);
		this.setMaxSourceSiteDistance(run.getSiteID());
		
		if (run.getSiteID() < 0) {
			System.err.println("Site '" + siteName + "' unknown!");
			return false;
		}
		
		if (cmd.hasOption("plot-chars-file")) {
			String pFileName = cmd.getOptionValue("plot-chars-file");
			System.out.println("Reading plot characteristics from " + pFileName);
			try {
				plotChars = HazardCurvePlotCharacteristics.fromXMLMetadata(pFileName);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				System.err.println("WARNING: plot characteristics file not found! Using default...");
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.err.println("WARNING: plot characteristics file parsing error! Using default...");
			} catch (DocumentException e) {
				e.printStackTrace();
				System.err.println("WARNING: plot characteristics file parsing error! Using default...");
			}
		}
		
		ArrayList<CybershakeIM> ims = getIMsFromOptions(cmd, run, curve2db, amps2db);
		
		if (cmd.hasOption("ef") && cmd.hasOption("af")) {
			String erfFile = cmd.getOptionValue("ef");
			String attenFiles = cmd.getOptionValue("af");
			
			try {
				erf = ERFSaver.LOAD_ERF_FROM_FILE(erfFile);
				
				for (String attenRelFile : commaSplit(attenFiles)) {
					AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
					this.addAttenuationRelationshipComparision(attenRel);
				}
			} catch (DocumentException e) {
				e.printStackTrace();
				System.err.println("WARNING: Unable to parse ERF XML, not plotting comparison curves!");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				System.err.println("WARNING: Unable to load comparison ERF, not plotting comparison curves!");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.err.println("WARNING: Unable to load comparison ERF, not plotting comparison curves!");
			}
		}
		
		String outDir = "";
		if (cmd.hasOption("o")) {
			outDir = cmd.getOptionValue("o");
			File outDirFile = new File(outDir);
			if (!outDirFile.exists()) {
				boolean success = outDirFile.mkdir();
				if (!success) {
					System.out.println("Directory doesn't exist and couldn't be created: " + outDir);
					return false;
				}
			}
			if (!outDir.endsWith(File.separator))
				outDir += File.separator;
		}
		
		if (cmd.hasOption("vs30")) {
			String vsStr = cmd.getOptionValue("vs30");
			manualVs30 = Double.parseDouble(vsStr);
		}
		
		ArrayList<PlotType> types;
		
		if (cmd.hasOption("t")) {
			String typeStr = cmd.getOptionValue("t");
			
			types = PlotType.fromExtensions(commaSplit(typeStr));
		} else {
			types = new ArrayList<PlotType>();
			types.add(PLOT_TYPE_DEFAULT);
		}
		
		if (cmd.hasOption("w")) {
			plotWidth = Integer.parseInt(cmd.getOptionValue("w"));
		}
		
		if (cmd.hasOption("h")) {
			plotHeight = Integer.parseInt(cmd.getOptionValue("h"));
		}
		
		int periodNum = 0;
		boolean atLeastOne = false;
		
		boolean calcOnly = cmd.hasOption("calc-only");
		
		if (calcOnly) {
			System.out.println("Calculating/inserting CyberShake curves only, no plotting.");
			atLeastOne = true;
		}
		
		boolean noVMColors = cmd.hasOption("no-vm-colors");
		boolean sgtColor = cmd.hasOption("sgt-colors");
		
//		String optStr = null;
//		for (Option opt : cmd.getOptions()) {
//			if (optStr == null)
//				optStr = "Options: ";
//			else
//				optStr += ", ";
//			optStr += opt.getArgName() + " ("+opt.getValue()+")";
//		}
//		System.out.println("optStr");
		
		for (CybershakeIM im : ims) {
			// reset the cs curve color index so coloring is consistent
			// among different IM type plots of the same curves
			csColorIndex = 0;
			if (im == null) {
				System.out.println("No matching IM found for site: "+siteName
						+". Check period(s), IM type, and Component");
				return false;
			}
			periodNum++;
			AnnotatedCurve curve;
			try {
				curve = getCurve(run, im, cmd);
			} catch (Exception e1) {
				System.out.print("Error calculating/fetching curve.");
				e1.printStackTrace();
				return false;
			}
			if (curve == null)
				// skip
				continue;
			ArrayList<AnnotatedCurve> compCurves = null;
			if (runCompares != null && !runCompares.isEmpty()) {
				compCurves = new ArrayList<AnnotatedCurve>();
				for (CybershakeRun runCompare : runCompares) {
					AnnotatedCurve compCurve;
					try {
						compCurve = getCurve(runCompare, im, cmd);
					} catch (Exception e1) {
						System.out.print("Error calculating/fetching curve.");
						e1.printStackTrace();
						return false;
					}
					compCurves.add(compCurve);
				}
				
			}
//			Date date = curve2db.getDateForCurve(curve.curveID);
			Date date = curve.date;
			String dateStr = dateFormat.format(date);
			String imStr = im.getMeasure().getShortName();
			if (im.getMeasure() == IMType.SA)
				imStr += "_"+getPeriodStr(im.getVal())+"sec";
			imStr += "_"+im.getComponent().getShortName();
			String outFileName = siteName + "_ERF" + run.getERFID() + "_Run" + runID;
			if (compCurves != null) {
				boolean first = true;
				for (CybershakeRun runCompare : runCompares) {
					if (first) {
						outFileName += "_CompToRun";
						first = false;
					} else {
						outFileName += "-";
					}
					outFileName += runCompare.getRunID();
				}
			}
			outFileName += "_"+imStr+"_"+dateStr;
			String outFile = outDir + outFileName;
			if (calcOnly)
				continue;
			boolean textOnly = types.size() == 1 && types.get(0) == PlotType.TXT;
			ArrayList<DiscretizedFunc> curves = this.plotCurve(curve, run,
					compCurves, textOnly, noVMColors, sgtColor);
			if (curves == null) {
				System.err.println("No points could be fetched for curve ID " + curve.curveID + "! Skipping...");
				continue;
			}
			for (PlotType type : types) {
				
				try {
					if (type == PlotType.PDF) {
						plotCurvesToPDF(outFile + ".pdf");
						atLeastOne = true;
					} else if (type == PlotType.PNG) {
						plotCurvesToPNG(outFile + ".png");
						atLeastOne = true;
					} else if (type == PlotType.JPG || type == PlotType.JPEG) {
						plotCurvesToJPG(outFile + ".jpg");
						atLeastOne = true;
					} else if (type == PlotType.TXT) {
						plotCurvesToTXT(outFile + ".txt", curves);
						atLeastOne = true;
					} else
						System.err.println("Unknown plotting type: " + type + "...Skipping!");
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		return atLeastOne;
	}
	
	public static ArrayList<CybershakeIM> getIMsFromOptions(CommandLine cmd, CybershakeRun run,
			HazardCurve2DB curve2db, PeakAmplitudesFromDB amps2db) {
		ArrayList<CybershakeIM> ims;
		if (cmd.hasOption("im-type-id")) {
			// get IM from ID
			Preconditions.checkState(!cmd.hasOption("period"),
					"IM type ID cannot be specified alongside period");
			Preconditions.checkState(!cmd.hasOption("im-type"),
					"IM type ID cannot be specified alongside IM type");
			Preconditions.checkState(!cmd.hasOption("component"),
					"IM type ID cannot be specified alongside component");
			
			int imTypeID = Integer.parseInt(cmd.getOptionValue("im-type-id"));
			CybershakeIM im = curve2db.getIMFromID(imTypeID);
			Preconditions.checkNotNull(im, "Couldn't get IM for ID: "+imTypeID);
			ims = Lists.newArrayList(im);
		} else {
			String periodStrs;
			if (cmd.hasOption("period"))
				periodStrs = cmd.getOptionValue("period");
			else
				periodStrs = default_periods;
			ArrayList<Double> periods = commaDoubleSplit(periodStrs);
			
			IMType imType;
			if (cmd.hasOption("im-type"))
				imType = CybershakeIM.fromShortName(cmd.getOptionValue("im-type"), IMType.class);
			else
				imType = defaultIMType;
			
			CyberShakeComponent component;
			if (cmd.hasOption("component"))
				component = CybershakeIM.fromShortName(cmd.getOptionValue("component"), CyberShakeComponent.class);
			else
				component = defaultComponent;
			
			System.out.println("Matching periods to IM types...");
			ims = amps2db.getSupportedIMForPeriods(periods, imType, component, run.getRunID(), curve2db);
			
			if (ims == null) {
				System.err.println("No IM's for site="+run.getSiteID()+" run="+run.getRunID());
				for (double period : periods) {
					System.err.println("period: " + period);
				}
				return null;
			}
		}
		return ims;
	}
	
	private AnnotatedCurve getCurve(CybershakeRun run, CybershakeIM im, CommandLine cmd) {
		int curveID = curve2db.getHazardCurveID(run.getRunID(), im.getID());
		int numPoints = 0;
		if (curveID >= 0)
			numPoints = curve2db.getNumHazardCurvePoints(curveID);
		
		System.out.println("Num points: " + numPoints);
		
		boolean forceAdd = cmd.hasOption("f");
		boolean noAdd = cmd.hasOption("n");
		boolean forceRecalc = cmd.hasOption("benchmark-test-recalc");
		
		DiscretizedFunc curve;
		Date date;
		
		// if no curveID exists, or the curve has 0 points
		if (curveID < 0 || numPoints < 1 || forceRecalc) {
			if (!forceAdd && !noAdd) {
				// lets ask the user what they want to do
				if (curveID >= 0)
					System.out.println("A record for the selected curve exists in the database, but there " +
							"are no data points: " + curveID + " period=" + im.getVal());
				else
					System.out.println("The selected curve does not exist in the database: " + run.getSiteID() + " period="
							+ im.getVal() + " run=" + run.getRunID());
				boolean skip = false;
				// open up standard input
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				while (true) {
					System.out.print("Would you like to calculate and insert it? (y/n): ");
					try {
						String response = in.readLine();
						response = response.trim().toLowerCase();
						if (response.startsWith("y")) { 
							break;
						} else if (response.startsWith("n")) {
							skip = true;
							break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (skip)
					return null;
			}	
			
			// calculate it
			System.out.println("Counting amps in database");
			int count = amps2db.countAmps(run.getRunID(), im);
			System.out.println(count + " amps in DB");
			Preconditions.checkState(count >= 0, "No Peak Amps for: %s period=%s run=%s",
					run.getSiteID(), im.getVal(), run.getRunID());
			
			// calculate the curve
			if (curveCalc == null)
				curveCalc = new HazardCurveComputation(db);
			
			ArbitrarilyDiscretizedFunc func = plotChars.getHazardFunc();
			ArrayList<Double> imVals = new ArrayList<Double>();
			for (int i=0; i<func.size(); i++)
				imVals.add(func.getX(i));
			
			curve = curveCalc.computeHazardCurve(imVals, run, im);
			date = new Date(); // right now
			
			if ((!forceRecalc || curveID < 0) && !noAdd) {
				// get credentials to add
				if (user.equals("") && pass.equals("")) {
					if (cmd.hasOption("pf")) {
						String pf = cmd.getOptionValue("pf");
						try {
							String user_pass[] = CyberShakeDBManagementApp.loadPassFile(pf);
							user = user_pass[0];
							pass = user_pass[1];
						} catch (FileNotFoundException e) {
							throw new IllegalStateException("Password file not found!", e);
						} catch (IOException e) {
							throw new IllegalStateException("Password file IO error!", e);
						} catch (Exception e) {
							throw new IllegalStateException("Unknown password file IO error!", e);
						}
						if (user.equals("") || pass.equals("")) {
							System.out.println("Bad password file!");
							throw new IllegalStateException("Unknown password file error, both are blank!");
						}
					} else {
						try {
							UserAuthDialog auth = new UserAuthDialog(null, true);
							auth.setVisible(true);
							if (auth.isCanceled()) {
								noAdd = true;
								System.out.println("Will still plot without inserting");
							} else {
								user = auth.getUsername();
								pass = new String(auth.getPassword());
							}
						} catch (HeadlessException e) {
							System.out.println("It looks like you can't display windows, using less secure command line password input.");
							BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
							
							boolean hasUser = false;
							while (true) {
								try {
									if (hasUser)
										System.out.print("Database Password: ");
									else
										System.out.print("Database Username: ");
									String line = in.readLine().trim();
									if (line.length() > 0) {
										if (hasUser) {
											pass = line;
											break;
										} else {
											user = line;
											hasUser = true;
										}
									}
								} catch (IOException e1) {
									throw ExceptionUtils.asRuntimeException(e1);
								}
							}
						}
					}
				}
				
				// now add it
				DBAccess writeDB = null;
				try {
					writeDB = new DBAccess(Cybershake_OpenSHA_DBApplication.HOST_NAME,Cybershake_OpenSHA_DBApplication.DATABASE_NAME, user, pass);
				} catch (IOException e) {
					throw new IllegalStateException("Error creating DB write object", e);
				}
				HazardCurve2DB curve2db_write = new HazardCurve2DB(writeDB);
				System.out.println("Inserting curve into database...");
				if (curveID >= 0) {
					System.out.println("Inserting with Curve_ID="+curveID);
					curve2db_write.insertHazardCurvePoints(curveID, curve);
				} else {
					curve2db_write.insertHazardCurve(run, im.getID(), curve);
					curveID = curve2db.getHazardCurveID(run.getRunID(), im.getID());
					System.out.println("Inserted with Curve_ID="+curveID);
				}
			}
		} else {
			System.out.println("Fetching Curve!");
			curve = curve2db.getHazardCurve(curveID);
			date = curve2db.getDateForCurve(curveID);
		}
//		} else if (cmd.hasOption("benchmark-test-recalc")) {
//			System.out.println("Force calculating a curve for benchmark purposes");
//			Stopwatch watch = Stopwatch.createStarted();
//			// calculate a curve just for speed benchmarking, even though we won't use it
//			ArbitrarilyDiscretizedFunc func = plotChars.getHazardFunc();
//			ArrayList<Double> imVals = new ArrayList<Double>();
//			for (int i=0; i<func.size(); i++)
//				imVals.add(func.getX(i));
//			
//			if (curveCalc == null)
//				curveCalc = new HazardCurveComputation(db);
//			curveCalc.computeHazardCurve(imVals, run, im);
//			watch.stop();
//			System.out.println("Curve took "+watch.elapsed(TimeUnit.SECONDS)+" s");
//		}
		
		return new AnnotatedCurve(curveID, date, run, im, curve);
	}
	
	private static class AnnotatedCurve {
		private int curveID;
		private Date date;
		private CybershakeRun run;
		private CybershakeIM im;
		private DiscretizedFunc curve;
		
		public AnnotatedCurve(int curveID, Date date, CybershakeRun run, CybershakeIM im, DiscretizedFunc curve) {
			super();
			this.curveID = curveID;
			this.date = date;
			this.run = run;
			this.im = im;
			this.curve = curve;
		}
	}
	
	public void setMaxSourceSiteDistance(int siteID) {
		SiteInfo2DB site2db = this.getSite2DB();
		
		this.maxSourceDistance = site2db.getSiteCutoffDistance(siteID);
		System.out.println("Max source distance for site " + siteID + " is " + this.maxSourceDistance);
	}
	
	public ArrayList<DiscretizedFunc> plotCurve(AnnotatedCurve curve, CybershakeRun run) {
		return plotCurve(curve, run, false, false, false);
	}
	
	public ArrayList<DiscretizedFunc> plotCurve(AnnotatedCurve curve, CybershakeRun run, boolean textOnly,
			boolean noVMColor, boolean sgtColor) {
		return plotCurve(curve, run, null, textOnly, noVMColor, sgtColor);
	}
	
	private static List<Color> csPlotColors;
	private int csColorIndex = 0;
	
	private Color getNextCSColor() {
		if (csPlotColors == null) {
			csPlotColors = Lists.newArrayList(Color.BLUE, Color.BLACK, Color.RED, Color.GREEN,
					Color.ORANGE, Color.CYAN, Color.MAGENTA);
		}
		
		if (csColorIndex == csPlotColors.size())
			csColorIndex = 0;
		
		return csPlotColors.get(csColorIndex++);
	}
	
	private static Color getColorForVM(int vmID) {
		switch (vmID) {
		case 1:
			return Color.BLUE;
		case 2:
			return Color.ORANGE;
		case 4:
			return Color.MAGENTA;
		case 5:
			return Color.RED;
		case 7:
			return Color.GREEN;
		case 8:
			return Color.CYAN;
		default:
			return Color.BLACK;
		}
	}
	
	private static PlotSymbol getSymbolForRupVarScenID(int rupVarScenID) {
		switch (rupVarScenID) {
		case 3:
			return PlotSymbol.CIRCLE;
		case 4:
			return PlotSymbol.FILLED_CIRCLE;
		case 5:
			return PlotSymbol.TRIANGLE;
		case 6:
			return PlotSymbol.FILLED_TRIANGLE;
		case 7:
			return PlotSymbol.DIAMOND;
		case 8:
			return PlotSymbol.FILLED_DIAMOND;
		default:
			return null;
		}
	}
	
	/**
	 * Used with the --sgt-symbol option
	 * @param sgtID
	 * @return
	 */
	private static Color getColorForSGTID(int sgtID) {
//		switch (sgtID) {
//		case 5:
//			return PlotSymbol.FILLED_CIRCLE;
//		case 6:
//			return PlotSymbol.FILLED_TRIANGLE;
//		case 7:
//			return PlotSymbol.FILLED_SQUARE;
//		case 8:
//			return PlotSymbol.FILLED_INV_TRIANGLE;
//		case 9:
//			return PlotSymbol.FILLED_DIAMOND;
//		default:
//			System.out.println("WARNING: SGT Symbol coloring enabled but no symbol hardcoded for SGT ID "+sgtID);
//			return PlotSymbol.CIRCLE;
//		}
		switch (sgtID) {
		case 5:
			return Color.BLUE;
		case 6:
			return Color.RED;
		case 7:
			return Color.GREEN;
		case 8:
			return Color.MAGENTA;
		case 9:
			return Color.ORANGE;
		case 10:
			return Color.CYAN;
		default:
			System.out.println("WARNING: SGT Coloring enabled but no color hardcoded for SGT ID "+sgtID);
			return Color.BLACK;
		}
	}
	
	public ArrayList<DiscretizedFunc> plotCurve(AnnotatedCurve annotatedCurve, CybershakeRun run,
			ArrayList<AnnotatedCurve> compCurves, boolean textOnly,
			boolean noVMColors, boolean sgtColors) {
		System.out.println("Fetching Curve!");
		DiscretizedFunc curve;
		if (annotatedCurve.curve == null)
			curve = curve2db.getHazardCurve(annotatedCurve.curveID);
		else
			curve = annotatedCurve.curve;
		
		if (curve == null)
			return null;
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		Color curveColor = plotChars.getCyberShakeColor();
		if (curveColor == null)
			if (noVMColors)
				curveColor = getNextCSColor();
			else
				curveColor = getColorForVM(run.getVelModelID());
		PlotLineType curveLineType = this.plotChars.getCyberShakeLineType();
		PlotSymbol curveSymbol = this.plotChars.getCyberShakeSymbol();
		if (curveSymbol == null)
			curveSymbol = getSymbolForRupVarScenID(run.getRupVarScenID());
		if (sgtColors)
			curveColor = getColorForSGTID(run.getSgtVarID());
		chars.add(new PlotCurveCharacterstics(curveLineType, plotChars.getLineWidth(),
				curveSymbol, plotChars.getLineWidth()*4f, curveColor));
		
		
		CybershakeIM im = annotatedCurve.im;
		if (im == null) {
			System.err.println("Couldn't get IM for curve!");
			System.exit(1);
		}
		
		System.out.println("Getting Site Info.");
		int siteID = annotatedCurve.run.getSiteID();
		
		SiteInfo2DB site2db = getSite2DB();
		
		csSite = site2db.getSiteFromDB(siteID);
		
		ArrayList<DiscretizedFunc> curves = new ArrayList<DiscretizedFunc>();
		curves.add(curve);
		curve.setInfo("CyberShake Hazard Curve. Site: " + csSite.toString());
		
		// these are used to determine which fields are common
		Map<Integer, CybershakeRuptureVariation> rvNamesMap = runs2db.getRuptureVariationsMap();
		Map<Integer, CybershakeSGTVariation> sgtNamesMap = runs2db.getSGTVarsMap();
		Map<Integer, CybershakeVelocityModel> velModelNamesMap = runs2db.getVelocityModelMap();
		
		List<Integer> runIDs = Lists.newArrayList(run.getRunID());
		List<String> siteNames = Lists.newArrayList(csSite.short_name);
		List<String> erfNames = Lists.newArrayList("ERF"+run.getERFID());
		List<String> rvNames = Lists.newArrayList(rvNamesMap.get(run.getRupVarScenID()).getName());
		List<String> sgtNames = Lists.newArrayList(sgtNamesMap.get(run.getSgtVarID()).getName());
		List<String> velModelNames = Lists.newArrayList(velModelNamesMap.get(run.getVelModelID()).getName());
		
		boolean siteNamesCommon = true;
		boolean erfNamesCommon = true;
		boolean rvNamesCommon = true;
		boolean sgtNamesCommon = true;
		boolean velNamesCommon = true;
		
		if (compCurves != null && !compCurves.isEmpty()) {
			for (int i=0; i<compCurves.size(); i++) {
				AnnotatedCurve annCurve = compCurves.get(i);
				int compCurveID = annCurve.curveID;
				CybershakeRun compRun = annCurve.run;
				DiscretizedFunc compCurve = annCurve.curve;
				curves.add(compCurve);
				CybershakeSite compSite = site2db.getSiteFromDB(compRun.getSiteID());
				compCurve.setInfo("CyberShake Comparison Hazard Curve. Site: " + compSite.toString());
				
				runIDs.add(compRun.getRunID());
				siteNamesCommon = siteNamesCommon && compSite.name.equals(siteNames.get(0));
				siteNames.add(compSite.name);
				String erfName = "ERF"+compRun.getERFID();
				erfNamesCommon = erfNamesCommon && erfName.equals(erfNames.get(0));
				erfNames.add(erfName);
				String rvName = rvNamesMap.get(compRun.getRupVarScenID()).getName();
				rvNamesCommon = rvNamesCommon && rvName.equals(rvNames.get(0));
				rvNames.add(rvName);
				String sgtName = sgtNamesMap.get(compRun.getSgtVarID()).getName();
				sgtNamesCommon = sgtNamesCommon && sgtName.equals(sgtNames.get(0));
				sgtNames.add(sgtName);
				String velName = velModelNamesMap.get(compRun.getVelModelID()).getName();
				velNamesCommon = velNamesCommon && velName.equals(velModelNames.get(0));
				velModelNames.add(velName);
				
				PlotLineType compCurveLineType = this.plotChars.getCyberShakeLineType();
				PlotSymbol compSymbol = this.plotChars.getCyberShakeSymbol();
				
//				if (compCurveLineType == null)
//					compCurveLineType = PlotLineType.SOLID;
				if (compSymbol == null)
					compSymbol = getSymbolForRupVarScenID(compRun.getRupVarScenID());
				Color compCurveColor = plotChars.getCyberShakeColor();
				if (compCurveColor == null)
					if (noVMColors)
						compCurveColor = getNextCSColor();
					else
						compCurveColor = getColorForVM(compRun.getVelModelID());
				chars.add(new PlotCurveCharacterstics(compCurveLineType, plotChars.getLineWidth(),
						compSymbol, plotChars.getLineWidth()*4f, compCurveColor));
				CybershakeVelocityModel velModel = runs2db.getVelocityModel(compRun.getVelModelID());
				compCurve.setInfo(getCyberShakeCurveInfo(compCurveID, site2db.getSiteFromDB(compRun.getSiteID()),
						compRun, velModel, im, compCurveColor, compCurveLineType, compSymbol));
			}
		}
		
		// now flush out curve names
		String title = "";
		for (int i=0; i<curves.size(); i++) {
			String name = "";
			if (siteNamesCommon) {
				if (i == 0)
					title += siteNames.get(i);
			} else {
				if (i == 0)
					title += "Multiple Sites";
				name += siteNames.get(i);
			}
			// always add run ID to the name
			if (!name.isEmpty())
				name += ", ";
			name += "CS Run "+runIDs.get(i);
			
			// both name and title guarenteed to be non empty now
			if (!erfNamesCommon)
				name += ", "+erfNames.get(i);
			else if (i == 0)
				title += ", "+erfNames.get(i);
			if (!rvNamesCommon)
				name += ", "+rvNames.get(i);
			else if (i == 0)
				title += ", "+rvNames.get(i);
			if (!sgtNamesCommon)
				name += ", "+sgtNames.get(i);
			else if (i == 0)
				title += ", "+sgtNames.get(i);
			if (!velNamesCommon)
				name += ", "+velModelNames.get(i);
			else if (i == 0)
				title += ", "+velModelNames.get(i);
			curves.get(i).setName(name);
		}
		
		if (erf != null && attenRels.size() > 0) {
			System.out.println("Plotting comparisons!");
			this.plotComparisions(curves, im, chars, run);
		}
		
		CybershakeVelocityModel velModel = runs2db.getVelocityModel(run.getVelModelID());
		curve.setInfo(getCyberShakeCurveInfo(annotatedCurve.curveID, csSite, run, velModel, im, curveColor, curveLineType, curveSymbol));
		
		// old version
//		String title = HazardCurvePlotCharacteristics.getReplacedTitle(plotChars.getTitle(), csSite);
		
		if (!textOnly) {
			System.out.println("Plotting Curve!");
			
			plotCurvesToGraphPanel(chars, im, curves, title);
		}
		return curves;
	}

	private void plotCurvesToPDF(String outFile) throws IOException {
		System.out.println("Saving PDF to: " + outFile);
		this.gp.saveAsPDF(outFile, plotWidth, plotHeight);
	}
	
	private void plotCurvesToPNG(String outFile) throws IOException {
		System.out.println("Saving PNG to: " + outFile);
		ChartUtilities.saveChartAsPNG(new File(outFile), gp.getChartPanel().getChart(), plotWidth, plotHeight);
	}
	
	private void plotCurvesToJPG(String outFile) throws IOException {
		System.out.println("Saving JPG to: " + outFile);
		ChartUtilities.saveChartAsJPEG(new File(outFile), gp.getChartPanel().getChart(), plotWidth, plotHeight);
	}
	
	private void plotCurvesToTXT(String outFile, ArrayList<DiscretizedFunc> curves) throws IOException {
		System.out.println("Saving TXT to: " + outFile);
		
		FileWriter fw = new FileWriter(outFile);
		
		fw.write("# Curves: " + curves.size() + "\n");
		
		for (int i=0; i<curves.size(); i++) {
			DiscretizedFunc curve = curves.get(i);
			
			String header = "# Name: "+curve.getName();
			
			fw.write(header + "\n");
			
			for (int j=0; j<curve.size(); j++) {
				fw.write(curve.getX(j) + "\t" + curve.getY(j) + "\n");
			}
		}
		fw.close();
	}

	private void plotCurvesToGraphPanel( ArrayList<PlotCurveCharacterstics> chars, CybershakeIM im,
			ArrayList<DiscretizedFunc> curves, String title) {
		
		String xAxisName = HazardCurvePlotCharacteristics.getReplacedXAxisLabel(plotChars.getXAxisLabel(), im.getVal());
		
		this.currentPeriod = im.getVal();
		
		Range xRange, yRange;
		if (plotChars.isCustomAxis()) {
			if (currentPeriod > 0)
				xRange = new Range(plotChars.getXMin(), plotChars.getXMax(currentPeriod));
			else
				xRange = new Range(plotChars.getXMin(), plotChars.getXMax());
			yRange = new Range(plotChars.getYMin(), plotChars.getYMax());
		} else {
			xRange = null;
			yRange = null;
		}
		
		PlotSpec spec = new PlotSpec(curves, chars, title, xAxisName, plotChars.getYAxisLabel());
		spec.setLegendVisible(true);
		spec.setLegendLocation(RectangleEdge.BOTTOM);
		
		this.gp.drawGraphPanel(spec, plotChars.isXLog(), plotChars.isYLog(), xRange, yRange);
		this.gp.setVisible(true);
		
		this.gp.validate();
		this.gp.repaint();
	}
	
	public GraphPanel getGraphPanel() {
		return this.gp;
	}
	
	public static String getPeriodStr(double period) {
		return period_format.format(period);
	}
	
	private void plotComparisions(ArrayList<DiscretizedFunc> curves, CybershakeIM im,
			ArrayList<PlotCurveCharacterstics> chars, CybershakeRun run) {
		ArrayList<String> names = new ArrayList<String>();
		System.out.println("Setting ERF Params");
		this.setERFParams();
		
		int i = 0;
		ArrayList<Color> colors = plotChars.getAttenRelColors();
		for (AttenuationRelationship attenRel : attenRels) {
			Color color;
			if (i >= colors.size())
				color = colors.get(colors.size() - 1);
			else
				color = colors.get(i);
			chars.add(new PlotCurveCharacterstics(this.plotChars.getAttenRelLineType(), plotChars.getAttenRelLineWidth(),
					plotChars.getAttenRelSymbol(), plotChars.getAttenRelLineWidth()*4f, color));
			
			System.out.println("Setting params for Attenuation Relationship: " + attenRel.getName());
			Site site = this.setAttenRelParams(attenRel, im, run);
			
			System.out.print("Calculating comparison curve for " + site.getLocation().getLatitude() + "," + site.getLocation().getLongitude() + "...");
			DiscretizedFunc curve = plotChars.getHazardFunc();
			DiscretizedFunc logHazFunction = this.getLogFunction(curve);
			calc.getHazardCurve(logHazFunction, site, attenRel, erf);
			curve = this.unLogFunction(curve, logHazFunction);
			curve = getScaledCurveForComponent(attenRel, im, curve);
			curve.setName(attenRel.getShortName());
			curve.setInfo(this.getCurveParametersInfoAsString(attenRel, erf, site, maxSourceDistance));
			System.out.println("done!");
			curves.add(curve);
			i++;
		}
	}
	
	public static String getCyberShakeCurveInfo(int curveID, CybershakeSite site, CybershakeRun run,
			CybershakeVelocityModel velModel, CybershakeIM im,
			Color color,PlotLineType lineType, PlotSymbol symbol) {
		String infoString = "Site: "+ site.getFormattedName() + ";\n";
		if (lineType != null || symbol != null) {
			infoString += "Plot Type: Line: "+lineType+" Symbol: "+symbol+" Color: "+
					HazardCurvePlotCharacteristics.getColorName(color)+";\n";
		}
		if (run != null)
			infoString += "Run: "+run.toString() + ";\n";
		if (velModel != null)
			infoString += "Velocity Model: "+velModel.toString()+";\n";
		infoString += "SA Period: " + im.getVal() + ";\n";
		infoString += "Hazard_Curve_ID: "+curveID+";\n";
		
		return infoString;
	}
	
	public static String getCurveParametersInfoAsString(AttenuationRelationship imr, AbstractERF erf, Site site,
			double maxSourceDistance) {
		return getCurveParametersInfoAsHTML(imr, erf, site, maxSourceDistance).replace("<br>", "\n");
	}

	public static String getCurveParametersInfoAsHTML(AttenuationRelationship imr, AbstractERF erf, Site site,
			double maxSourceDistance) {
		ListIterator<Parameter<?>> imrIt = imr.getOtherParamsIterator();
		String imrMetadata = "IMR = " + imr.getName() + "; ";
		while (imrIt.hasNext()) {
			Parameter tempParam = imrIt.next();
			imrMetadata += tempParam.getName() + " = " + tempParam.getValue();
		}
		String siteMetadata = site.getParameterListMetadataString();
		String imtName = imr.getIntensityMeasure().getName();
		String imtMetadata = "IMT = " + imtName;
		if (imtName.toLowerCase().equals("sa")) {
			imtMetadata += "; ";
			Parameter damp = imr.getParameter(DampingParam.NAME);
			if (damp != null)
				imtMetadata += damp.getName() + " = " + damp.getValue() + "; ";
			Parameter period = imr.getParameter(PeriodParam.NAME);
			imtMetadata += period.getName() + " = " + period.getValue();
		}
//		imr.get
		String erfMetadata = "Eqk Rup Forecast = " + erf.getName();
		erfMetadata += "; " + erf.getAdjustableParameterList().getParameterListMetadataString();
		String timeSpanMetadata = "Duration = " + erf.getTimeSpan().getDuration() + " " + erf.getTimeSpan().getDurationUnits();

		return "<br>"+ "IMR Param List:" +"<br>"+
		"---------------"+"<br>"+
		imrMetadata+"<br><br>"+
		"Site Param List: "+"<br>"+
		"----------------"+"<br>"+
		siteMetadata+"<br><br>"+
		"IMT Param List: "+"<br>"+
		"---------------"+"<br>"+
		imtMetadata+"<br><br>"+
		"Forecast Param List: "+"<br>"+
		"--------------------"+"<br>"+
		erfMetadata+"<br><br>"+
		"TimeSpan Param List: "+"<br>"+
		"--------------------"+"<br>"+
		timeSpanMetadata+"<br><br>"+
		"Max. Source-Site Distance = "+maxSourceDistance;

	}

	private void setERFParams() {
//		this.erf = MeanUCERF2_ToDB.setMeanUCERF_CyberShake_Settings(this.erf);
		this.erf.updateForecast();
	}
	
	private Site setAttenRelParams(AttenuationRelationship attenRel, CybershakeIM im, CybershakeRun run) {
		OrderedSiteDataProviderList providers = getProviders(run.getVelModelID());
		ArrayList<SiteDataValue<?>> datas = providers.getBestAvailableData(csSite.createLocation());
		
		if (manualVs30 > 0) {
			datas.add(new SiteDataValue<Double>(SiteData.TYPE_VS30, SiteData.TYPE_FLAG_INFERRED, manualVs30,
					"Manually Set Vs30 Value"));
		}
		return setAttenRelParams(attenRel, im, run, csSite, datas);
	}
	
	public static Site setAttenRelParams(AttenuationRelationship attenRel, CybershakeIM im, CybershakeRun run,
			CybershakeSite csSite, List<SiteDataValue<?>> datas) {
		CyberShakeComponent comp = im.getComponent();
		double period = im.getVal();
		return setAttenRelParams(attenRel, comp, period, run, csSite, datas);
	}
	
	public static Site setAttenRelParams(AttenuationRelationship attenRel, CyberShakeComponent comp, double period,
			CybershakeRun run, CybershakeSite csSite, List<SiteDataValue<?>> datas) {
//		// set 1 sided truncation
//		StringParameter truncTypeParam = (StringParameter)attenRel.getParameter(SigmaTruncTypeParam.NAME);
//		truncTypeParam.setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
//		// set truncation at 3 std dev's
//		DoubleParameter truncLevelParam = (DoubleParameter)attenRel.getParameter(SigmaTruncLevelParam.NAME);
//		truncLevelParam.setValue(3.0);
		
		// set the component
		if (comp == null) {
			System.err.println("WARNING: Component is null, not updating GMPE component");
		} else {
			try {
				ComponentParam param = (ComponentParam) attenRel.getParameter(ComponentParam.NAME);
				Component match = comp.getSupportedComponent(param);
				
				if (match == null) {
					System.err.println("WARNING: GMPE "+attenRel.getShortName()+" doesn't have matching component"
							+ " for CyberShake value of "+comp.getShortName()+". Leaving as "+param.getValue()+".");
				} else {
					System.out.println("Setting GMPE component to "+match+" for CyberShake value of "
							+comp.getShortName());
					param.setValue(match);
				}
			} catch (ParameterException e) {
				System.err.println("WARNING: GMPE "+attenRel.getShortName()+" doesn't have component parameter, "
						+ "can't set as appropriate");
			}
//			System.err.println("WARNING: Component is null, not updating GMPE component");
		}
		
		try {
			attenRel.getParameter(StdDevTypeParam.NAME).setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
		} catch (ParameterException e1) {
			// do nothing, IMR doesn't have this parameter
		}
		
		// set IMT
		attenRel.setIntensityMeasure(SA_Param.NAME);
		DoubleDiscreteParameter saPeriodParam = (DoubleDiscreteParameter)attenRel.getParameter(PeriodParam.NAME);
		List<Double> allowedVals = saPeriodParam.getAllowedDoubles();
		
		double closestPeriod = Double.NaN;
		double minDistance = Double.POSITIVE_INFINITY;
		
		for (double testPeriod : allowedVals) {
			double dist = Math.abs(testPeriod - period);
			if (dist < minDistance) {
				minDistance = dist;
				closestPeriod = testPeriod;
			}
		}
		double pDiff = DataUtils.getPercentDiff(closestPeriod, period);
		Preconditions.checkState(pDiff < 0.01, "No match for period="+period+", closest="+closestPeriod);
		saPeriodParam.setValue(closestPeriod);
		Preconditions.checkState(attenRel.getIntensityMeasure() instanceof SA_Param
				&& ((Double)attenRel.getIntensityMeasure().getIndependentParameter(PeriodParam.NAME).getValue()).floatValue()
						== (float)closestPeriod);
//		attenRel.setIntensityMeasure(intensityMeasure)
		
		LocationList locList = new LocationList();
		Location loc = new Location(csSite.lat, csSite.lon);
		locList.add(loc);
		
		Site site = new Site(loc);
		
		SiteTranslator siteTranslator = new SiteTranslator();
		
		try{
			Iterator<Parameter<?>> it = attenRel.getSiteParamsIterator(); // get site params for this IMR
			while(it.hasNext()) {
				Parameter tempParam = it.next();
				if(!site.containsParameter(tempParam))
					site.addParameter(tempParam);
				//adding the site Params from the CVM, if site is out the range of CVM then it
				//sets the site with whatever site Parameter Value user has choosen in the application
				boolean flag = siteTranslator.setParameterValue(tempParam, datas);
				if( !flag ) {
					System.err.println("Param " + tempParam.getName() + " not set for site! Not available from web service.");
					if (tempParam.getName().equals(Vs30_Param.NAME)) {
						BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
						System.out.print("Enter Vs30 value (or hit enter for default, " + tempParam.getValue() + "): ");
						String line = in.readLine();
						line = line.trim();
						if (line.length() > 0) {
							try {
								double val = Double.parseDouble(line);
								tempParam.setValue(val);
								System.out.println(tempParam.getName() + " set to: " + tempParam.getValue());
							} catch (Exception e) {
								System.out.println("Using default value: " + tempParam.getValue());
							}
						} else {
							System.out.println("Using default value: " + tempParam.getValue());
						}
//						manualVs30 = (Double)tempParam.getValue();
					} else {
						System.out.println("Using default value: " + tempParam.getValue());
					}
				} else {
//					System.out.println("Param: "+tempParam.getName() + ", Value: " + tempParam.getValue());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return site;
	}
	
	public static boolean SCALE_PRINT_SUCCESS = true;
	
	/**
	 * This will apply an empirical scaling factor to the X values of the given hazard curve if
	 * the CyberShake and GMPE components differ and a conversion factor exists. Otherwise a
	 * warning message will be printed and nothing will be done.
	 * 
	 * Will append the info string with scaling information.
	 * @param attenRel
	 * @param component
	 * @param curve
	 * @return
	 */
	public static DiscretizedFunc getScaledCurveForComponent(
			ScalarIMR attenRel, CybershakeIM im, DiscretizedFunc curve) {
		CyberShakeComponent component = im.getComponent();
		double period = im.getVal();
		Component gmpeComponent;
		try {
			gmpeComponent = (Component) attenRel.getParameter(ComponentParam.NAME).getValue();
		} catch (ParameterException e) {
			System.err.println("WARNING: GMPE "+attenRel.getShortName()+" doesn't have component parameter, "
					+ "can't scale curve as appropriate");
			appendInfoString(curve, "NOTE: GMPE Component unknown, no scaling applied");
			return curve;
		}
		if (gmpeComponent == null) {
			System.err.println("WARNING: GMPE "+attenRel.getShortName()+" has null component, "
					+ "can't scale curve as appropriate");
			appendInfoString(curve, "NOTE: GMPE Component unknown, no scaling applied");
			return curve;
		}
		
		// first see if no translation needed (already correct)
		if (component.isComponentSupported(gmpeComponent)) {
			appendInfoString(curve, "GMPE component ("+gmpeComponent.name()+") is correct, no scaling applied");
			return curve;
		}
		
		// we'll need a translation
		for (Component to : component.getGMPESupportedComponents()) {
			if (ComponentConverter.isConversionSupported(gmpeComponent, to)) {
				// we have a valid translation!
				if (SCALE_PRINT_SUCCESS)
					System.out.println("Scaling curve from "+gmpeComponent+" to "+to);
				ComponentTranslation converter = ComponentConverter.getConverter(gmpeComponent, to);
				double factor = converter.getScalingFactor(period);
				curve = converter.convertCurve(curve, period);
				appendInfoString(curve, "GMPE component ("+gmpeComponent.name()+") scaled to "+to.name()
						+" via factor of "+factor+" from "+converter.getName());
				return curve;
			}
		}
		
		// we've made it this far, there's a mismatch that can't be scaled away
		System.err.println("NOTE: There is a GMPE/CyberShake component mismatch and no scaling factors exist."
				+ " Using the unscaled curve. CyberShake Component: "+component.getShortName()
				+", GMPE Component: "+gmpeComponent);
		
		appendInfoString(curve, "WARNING: GMPE component ("+gmpeComponent.name()
				+") cannot be scaled to "+component.getShortName());
		
		return curve;
	}
	
	private static void appendInfoString(DiscretizedFunc curve, String metadata) {
		String info = curve.getInfo();
		if (info == null)
			info = "";
		if (!info.isEmpty())
			info +="\n";
		info += metadata;
		curve.setInfo(info);
	}
	
	public SiteInfo2DB getSite2DB() {
		if (site2db == null)
			site2db = new SiteInfo2DB(db);
		
		return site2db;
	}
	
	public PeakAmplitudesFromDB getAmps2DB() {
		if (amps2db == null)
			amps2db = new PeakAmplitudesFromDB(db);
		
		return amps2db;
	}
	
	public void addAttenuationRelationshipComparision(AttenuationRelationship attenRel) {
		attenRels.add(attenRel);
	}
	
	public void clearAttenuationRelationshipComparisions() {
		attenRels.clear();
	}
	
	public void setERFComparison(AbstractERF erf) {
		this.erf = erf;
	}
	
	public void setPlottingCharactersistics(HazardCurvePlotCharacteristics chars) {
		this.plotChars = chars;
	}
	
	boolean xLogFlag = true;
	
	/**
	 * Takes the log of the X-values of the given function
	 * @param arb
	 * @return A function with points (Log(x), 1)
	 */
	private ArbitrarilyDiscretizedFunc getLogFunction(DiscretizedFunc arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < arb.size(); ++i)
				new_func.set(Math.log(arb.getX(i)), 1);
			return new_func;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}
	
	/**
	 *  Un-log the function, keeping the y values from the log function, but matching
	 *  them with the x values of the original (not log) function
	 * @param oldHazFunc - original hazard function
	 * @param logHazFunction - calculated hazard curve with log x values
	 * @return
	 */
	private ArbitrarilyDiscretizedFunc unLogFunction(
			DiscretizedFunc oldHazFunc, DiscretizedFunc logHazFunction) {
		int numPoints = oldHazFunc.size();
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < numPoints; ++i) {
				hazFunc.set(oldHazFunc.getX(i), logHazFunction.getY(i));
			}
			return hazFunc;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}
	
	/**
	 * This creates all of the options necessary to select a Run ID
	 * @return
	 */
	protected static Options createCommonOptions() {
		Options ops = new Options();
		
		// ERF
		Option erf = new Option("e", "erf-id", true, "ERF ID");
		erf.setRequired(false);
		ops.addOption(erf);
		
		Option rv = new Option("r", "rv-id", true, "Rupture Variation ID");
		rv.setRequired(false);
		ops.addOption(rv);
		
		Option sgt = new Option("sgt", "sgt-var-id", true, "STG Variation ID");
		sgt.setRequired(false);
		ops.addOption(sgt);
		
		Option vel = new Option("vel", "vel-model-id", true, "Velocity Model ID");
		vel.setRequired(false);
		ops.addOption(vel);
		
		Option run = new Option("R", "run-id", true, "Run ID");
		run.setRequired(false);
		ops.addOption(run);
		
		Option site = new Option("s", "site", true, "Site short name");
		site.setRequired(false);
		ops.addOption(site);
		
		Option imType = new Option("imt", "im-type", true, "Intensity measure type. Options: "
				+Joiner.on(",").join(CybershakeIM.getShortNames(IMType.class))
				+", Default: "+defaultIMType.getShortName());
		imType.setRequired(false);
		ops.addOption(imType);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. Options: "
				+Joiner.on(",").join(CybershakeIM.getShortNames(CyberShakeComponent.class))
				+", Default: "+defaultComponent.getShortName());
		component.setRequired(false);
		ops.addOption(component);
		
		Option period = new Option("p", "period", true, "Period(s) to calculate. Multiple "
				+ "periods should be comma separated (default: "+default_periods+")");
		period.setRequired(false);
		ops.addOption(period);
		
		Option imTypeID = new Option("imid", "im-type-id", true, "Intensity measure type ID. If not supplied,"
				+ " will be detected from im type/component/period parameters");
		imTypeID.setRequired(false);
		ops.addOption(imTypeID);
		
		Option help = new Option("?", "help", false, "Display this message");
		ops.addOption(help);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		ops.addOption(output);
		
		return ops;
	}
	
	private static Options createOptions() {
		Options ops = createCommonOptions();
		
		Option compare = new Option("comp", "compare-to", true, "Compare to  aspecific Run ID" +
				" (or multiple IDs, comma separated)");
		compare.setRequired(false);
		ops.addOption(compare);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		ops.addOption(erfFile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true, "XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		ops.addOption(attenRelFiles);
		
		Option pass = new Option("pf", "password-file", true, "Path to a file that contains the username and password for " + 
				"inserting curves into the database. Format should be \"user:pass\"");
		ops.addOption(pass);
		
		Option noAdd = new Option("n", "no-add", false, "Flag to not automatically calculate curves not in the database");
		ops.addOption(noAdd);
		
		Option force = new Option("f", "force-add", false, "Flag to add curves to db without prompt");
		ops.addOption(force);
		
		Option type = new Option("t", "type", true, "Plot save type. Options are png, pdf, jpg, and txt. Multiple types can be " + 
				"comma separated (default is " + PLOT_TYPE_DEFAULT.getExtension() + ")");
		ops.addOption(type);
		
		Option width = new Option("w", "width", true, "Plot width (default = " + PLOT_WIDTH_DEFAULT + ")");
		ops.addOption(width);
		
		Option height = new Option("h", "height", true, "Plot height (default = " + PLOT_HEIGHT_DEFAULT + ")");
		ops.addOption(height);
		
		Option vs30 = new Option("v", "vs30", true, "Specify default Vs30 for sites with no Vs30 data, or leave blank " + 
				"for default value. Otherwise, you will be prompted to enter vs30 interactively if needed.");
		ops.addOption(vs30);
		
		Option cvmVs30 = new Option("cvmvs", "cvm-vs30", false, "Option to use Vs30 value from the velocity model itself"
				+ " in GMPE calculations rather than, for example, the Wills 2006 value.");
		ops.addOption(cvmVs30);
		
		Option forceVs30 = new Option("fvs", "force-vs30", true, "Option to force the given Vs30 value to be used"
				+ " in GMPE calculations.");
		ops.addOption(forceVs30);
		
		Option calcOnly = new Option("c", "calc-only", false, "Only calculate and insert the CyberShake curves, don't make " + 
				"plots. If a curve already exists, it will be skipped.");
		ops.addOption(calcOnly);
		
		Option plotChars = new Option("pl", "plot-chars-file", true, "Specify the path to a plot characteristics XML file");
		ops.addOption(plotChars);
		
		Option noVMChars = new Option("novm", "no-vm-colors", false, "Disables Velocity Model coloring");
		ops.addOption(noVMChars);
		
		Option sgtSymbols = new Option("sgtsym", "sgt-colors", false, "Enables SGT specific symbols");
		ops.addOption(sgtSymbols);
		
		Option benchRecalc = new Option("benchmark", "benchmark-test-recalc", false,
				"Forces recalculation of hazard curves to test calculation speed. Newly recalculated curves are not kept and "
				+ "the original curves are plotted.");
		ops.addOption(benchRecalc);
		
		return ops;
	}
	
	public static void printHelp(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( appName, options, true );
		System.exit(2);
	}
	
	public static void printUsage(Options options, String appName) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 80, appName, options);
		pw.flush();
		System.exit(2);
	}
	
	private static void printTime(Stopwatch watch) {
		long seconds = watch.elapsed(TimeUnit.SECONDS);
		double mins = seconds/60d;
		
		if (mins > 1d)
			System.out.println("Took "+(float)mins+" mins");
		else
			System.out.println("Took "+seconds+" seconds");
	}

	public static void main(String args[]) throws DocumentException, InvocationTargetException {
//		String confDir = "src/org/opensha/sha/cybershake/conf/";
//		String[] newArgs = { "-R", "789",
//				"--output-dir", "/tmp", "--type", "pdf,png",
//				"--erf-file", confDir+"MeanUCERF.xml",
//				"--atten-rel-file", confDir+"cb2008.xml,"
//				+confDir+"ba2008.xml,"+confDir+"cy2008.xml,"+confDir+"as2008.xml"
//				, "--plot-chars-file", confDir+"tomPlot.xml"};
//		args = newArgs;
		Stopwatch watch = Stopwatch.createStarted();
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(HazardCurvePlotter.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				HazardCurvePlotter plotter = new HazardCurvePlotter(Cybershake_OpenSHA_DBApplication.db);
				
				boolean success = plotter.plotCurvesFromOptions(cmd);
				
				if (!success) {
					System.out.println("FAIL!");
					System.exit(1);
				}
			} catch (MissingOptionException e) {
				// TODO Auto-generated catch block
				Options helpOps = new Options();
				helpOps.addOption(new Option("h", "help", false, "Display this message"));
				try {
					CommandLine cmd = parser.parse( helpOps, args);
					
					if (cmd.hasOption("help")) {
						printHelp(options, appName);
					}
				} catch (ParseException e1) {
//					e1.printStackTrace();
				}
				System.err.println(e.getMessage());
				printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
				printUsage(options, appName);
			}
			
			System.out.println("Done!");
			watch.stop();
			printTime(watch);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			watch.stop();
			printTime(watch);
			System.exit(1);
		}
	}
}
