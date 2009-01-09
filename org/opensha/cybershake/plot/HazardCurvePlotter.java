package org.opensha.cybershake.plot;

import java.awt.Color;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

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
import org.opensha.cybershake.db.CybershakeERF;
import org.opensha.cybershake.db.CybershakeIM;
import org.opensha.cybershake.db.CybershakeSite;
import org.opensha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.cybershake.db.DBAccess;
import org.opensha.cybershake.db.ERF2DB;
import org.opensha.cybershake.db.HazardCurve2DB;
import org.opensha.cybershake.db.HazardCurveComputation;
import org.opensha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.cybershake.db.SiteInfo2DB;
import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.gui.UserAuthDialog;
import org.opensha.param.DoubleDiscreteParameter;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.PlotControllerAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.util.FileUtils;

import scratchJavaDevelopers.kevin.XMLSaver.AttenRelSaver;
import scratchJavaDevelopers.kevin.XMLSaver.ERFSaver;

public class HazardCurvePlotter implements GraphPanelAPI, PlotControllerAPI {
	
	public static final String TYPE_PDF = "pdf";
	public static final String TYPE_PNG = "png";
	public static final String TYPE_JPG = "jpg";
	public static final String TYPE_JPEG = "jpeg";
	
	public static final String TYPE_DEFAULT = TYPE_PDF;
	
	private double maxSourceDistance = 200;
	
	public static final int PLOT_WIDTH_DEFAULT = 600;
	public static final int PLOT_HEIGHT_DEFAULT = 500;
	
	private int plotWidth = PLOT_WIDTH_DEFAULT;
	private int plotHeight = PLOT_HEIGHT_DEFAULT;
	
	private DBAccess db;
	private int erfID;
	private int rupVarScenarioID;
	private int sgtVarID;
	
	private HazardCurve2DB curve2db;
	
	private GraphPanel gp;
	
	private EqkRupForecast erf = null;
	private ArrayList<AttenuationRelationship> attenRels = new ArrayList<AttenuationRelationship>();
	
	private SiteInfo2DB site2db = null;
	private PeakAmplitudesFromDB amps2db = null;
	
	CybershakeSite csSite = null;
	
	SiteTranslator siteTranslator = new SiteTranslator();
	
	HazardCurveCalculator calc;
	
	CybershakeERF selectedERF;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
	
	private double manualVs30  = -1;
	
	double currentPeriod = -1;
	
	HazardCurvePlotCharacteristics plotChars = HazardCurvePlotCharacteristics.createRobPlotChars();
	
	public HazardCurvePlotter(DBAccess db, int erfID, int rupVarScenarioID, int sgtVarID) {
		this.db = db;
		this.erfID = erfID;
		this.rupVarScenarioID = rupVarScenarioID;
		this.sgtVarID = sgtVarID;
		
		init();
	}
	
	public HazardCurvePlotter(DBAccess db, CommandLine cmd) {
		erfID = Integer.parseInt(cmd.getOptionValue("erf-id"));
		rupVarScenarioID = Integer.parseInt(cmd.getOptionValue("rv-id"));
		sgtVarID = Integer.parseInt(cmd.getOptionValue("sgt-var-id"));
		
		this.db = db;
		
		init();
	}
	
	private void init() {
		gp = new GraphPanel(this);
		ERF2DB erf2db = new ERF2DB(db);
		selectedERF = erf2db.getERF(erfID);
		
		curve2db = new HazardCurve2DB(this.db);
		
		try {
			calc = new HazardCurveCalculator();
			calc.setMaxSourceDistance(maxSourceDistance);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
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
	
	public boolean plotCurvesFromOptions(CommandLine cmd) {
		String siteName = cmd.getOptionValue("site");
		SiteInfo2DB site2db = getSite2DB();
		PeakAmplitudesFromDB amps2db = getAmps2DB();
		
		int siteID = site2db.getSiteId(siteName);
		
		if (siteID < 0) {
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
		
		String periodStrs = cmd.getOptionValue("period");
		ArrayList<Double> periods = commaDoubleSplit(periodStrs);
		
		System.out.println("Matching periods to IM types...");
		ArrayList<CybershakeIM> ims = amps2db.getIMForPeriods(periods, siteID, erfID, sgtVarID, rupVarScenarioID, curve2db);
		
		if (ims == null) {
			System.err.println("No IM's for site=" + siteID + " erf=" + erfID + " sgt=" + sgtVarID + " rvid=" + rupVarScenarioID);
			for (double period : periods) {
				System.err.println("period: " + period);
			}
			return false;
		}
		
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
				System.err.println("WARNING: Unable to parse load comparison ERF, not plotting comparison curves!");
			}
		}
		
		HazardCurveComputation curveCalc = null;
		String user = "";
		String pass = "";
		
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
		
		ArrayList<String> types;
		
		if (cmd.hasOption("t")) {
			String typeStr = cmd.getOptionValue("t");
			
			types = commaSplit(typeStr);
		} else {
			types = new ArrayList<String>();
			types.add(TYPE_PDF);
		}
		
		if (cmd.hasOption("w")) {
			plotWidth = Integer.parseInt(cmd.getOptionValue("w"));
		}
		
		if (cmd.hasOption("h")) {
			plotHeight = Integer.parseInt(cmd.getOptionValue("h"));
		}
		
		int periodNum = 0;
		boolean atLeastOne = false;
		for (CybershakeIM im : ims) {
			if (im == null) {
				System.out.println("IM not found for: site=" + siteName + " period=" + periods.get(periodNum));
				return false;
			}
			periodNum++;
			int curveID = curve2db.getHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, im.getID());
			
			if (curveID < 0) {
				if (!cmd.hasOption("n")) {
					if (!cmd.hasOption("f")) {
						// lets ask the user what they want to do
						System.out.println("The selected curve does not exist in the database: " + siteID + " period="
									+ im.getVal() + " erf=" + erfID + " sgt=" + sgtVarID + " rvid=" + rupVarScenarioID);
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
							continue;
					}
					int count = amps2db.countAmps(siteID, erfID, sgtVarID, rupVarScenarioID, im);
					if (count <= 0) {
						System.err.println("No Peak Amps for: " + siteID + " period="
									+ im.getVal() + " erf=" + erfID + " sgt=" + sgtVarID + " rvid=" + rupVarScenarioID);
						return false;
					}
					System.out.println(count + " amps in DB");
					if (user.equals("") && pass.equals("")) {
						if (cmd.hasOption("pf")) {
							String pf = cmd.getOptionValue("pf");
							try {
								for (String line : (ArrayList<String>)FileUtils.loadFile(pf)) {
									line = line.trim();
									if (line.contains(":")) {
										String split[] = line.split(":");
										user = split[0];
										pass = split[1];
										break;
									}
								}
							} catch (FileNotFoundException e) {
								e.printStackTrace();
								System.out.println("Password file not found!");
								return false;
							} catch (IOException e) {
								e.printStackTrace();
								System.out.println("Password file not found!");
								return false;
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("Bad password file!");
								return false;
							}
							if (user.equals("") || pass.equals("")) {
								System.out.println("Bad password file!");
								return false;
							}
						} else {
							try {
								UserAuthDialog auth = new UserAuthDialog(null, true);
								auth.setVisible(true);
								if (auth.isCanceled())
									return false;
								user = auth.getUsername();
								pass = new String(auth.getPassword());
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
										e1.printStackTrace();
									}
								}
							}
						}
					}
					
					DBAccess writeDB = null;
					try {
						writeDB = new DBAccess(Cybershake_OpenSHA_DBApplication.HOST_NAME,Cybershake_OpenSHA_DBApplication.DATABASE_NAME, user, pass);
					} catch (IOException e) {
						e.printStackTrace();
						return false;
					}
					
					// calculate the curve
					if (curveCalc == null)
						curveCalc = new HazardCurveComputation(db);
					
					ArbitrarilyDiscretizedFunc func = plotChars.getHazardFunc();
					ArrayList<Double> imVals = new ArrayList<Double>();
					for (int i=0; i<func.getNum(); i++)
						imVals.add(func.getX(i));
					
					DiscretizedFuncAPI curve = curveCalc.computeHazardCurve(imVals, siteName, erfID, sgtVarID, rupVarScenarioID, im);
					HazardCurve2DB curve2db_write = new HazardCurve2DB(writeDB);
					System.out.println("Inserting curve into database...");
					curve2db_write.insertHazardCurve(siteID, erfID, rupVarScenarioID, sgtVarID, im.getID(), curve);
					curveID = curve2db.getHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, im.getID());
				} else {
					System.out.println("Curve not found in DB, and no-add option supplied!");
					return false;
				}
			}
			Date date = curve2db.getDateForCurve(curveID);
			String dateStr = dateFormat.format(date);
			String periodStr = "SA_" + getPeriodStr(im.getVal()) + "sec";
			String outFileName = siteName + "_" + "ERF" + erfID + "_" + periodStr + "_" + dateStr;
			String outFile = outDir + outFileName;
			this.plotCurve(curveID);
			for (String type : types) {
				type = type.toLowerCase();
				
				try {
					if (type.equals(TYPE_PDF)) {
						plotCurvesToPDF(outFile + ".pdf");
						atLeastOne = true;
					} else if (type.equals(TYPE_PNG)) {
						plotCurvesToPNG(outFile + ".png");
						atLeastOne = true;
					} else if (type.equals(TYPE_JPG) || type.equals(TYPE_JPEG)) {
						plotCurvesToJPG(outFile + ".jpg");
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
	
	public void plotCurve(int siteID, int imTypeID) {
		int curveID = curve2db.getHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, imTypeID);
		this.plotCurve(curveID);
	}
	
	public void plotCurve(int curveID) {
		System.out.println("Fetching Curve!");
		DiscretizedFuncAPI curve = curve2db.getHazardCurve(curveID);
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		chars.add(new PlotCurveCharacterstics(this.plotChars.getCyberShakeLineType(), this.plotChars.getCyberShakeColor(), 1));
		
		
		CybershakeIM im = curve2db.getIMForCurve(curveID);
		if (im == null) {
			System.err.println("Couldn't get IM for curve!");
			System.exit(1);
		}
		
		System.out.println("Getting Site Info.");
		int siteID = curve2db.getSiteIDFromCurveID(curveID);
		
		SiteInfo2DB site2db = getSite2DB();
		
		csSite = site2db.getSiteFromDB(siteID);
		
		ArrayList<DiscretizedFuncAPI> curves = new ArrayList<DiscretizedFuncAPI>();
		curves.add(curve);
		
		if (erf != null && attenRels.size() > 0) {
			System.out.println("Plotting comparisons!");
			this.plotComparisions(curves, im, curveID, chars);
		}
		
		curve.setInfo(this.getCyberShakeCurveInfo(csSite, im));
		
		String title = HazardCurvePlotCharacteristics.getReplacedTitle(plotChars.getTitle(), csSite);
		
		System.out.println("Plotting Curve!");
		
		plotCurvesToGraphPanel(chars, im, curves, title);
	}

	private void plotCurvesToPDF(String outFile) throws IOException {
		System.out.println("Saving PDF to: " + outFile);
		this.gp.saveAsPDF(outFile, plotWidth, plotHeight);
	}
	
	private void plotCurvesToPNG(String outFile) throws IOException {
		System.out.println("Saving PNG to: " + outFile);
		ChartUtilities.saveChartAsPNG(new File(outFile), gp.getCartPanel().getChart(), plotWidth, plotHeight);
	}
	
	private void plotCurvesToJPG(String outFile) throws IOException {
		System.out.println("Saving JPG to: " + outFile);
		ChartUtilities.saveChartAsJPEG(new File(outFile), gp.getCartPanel().getChart(), plotWidth, plotHeight);
	}

	private void plotCurvesToGraphPanel( ArrayList<PlotCurveCharacterstics> chars, CybershakeIM im,
			ArrayList<DiscretizedFuncAPI> curves, String title) {
		
		String xAxisName = HazardCurvePlotCharacteristics.getReplacedXAxisLabel(plotChars.getXAxisLabel(), im.getVal());
		
		this.currentPeriod = im.getVal();
		
		this.gp.setCurvePlottingCharacterstic(chars);
		
		this.gp.drawGraphPanel(xAxisName, plotChars.getYAxisLabel(), curves, plotChars.isXLog(), plotChars.isYLog(), plotChars.isCustomAxis(), title, this);
		this.gp.setVisible(true);
		
		this.gp.togglePlot(null);
		
		this.gp.validate();
		this.gp.repaint();
	}
	
	public static String getPeriodStr(double period) {
		int periodInt = (int)(period * 100 + 0.5);
		
		return (periodInt / 100) + "";
	}
	
	private void plotComparisions(ArrayList<DiscretizedFuncAPI> curves, CybershakeIM im, int curveID, ArrayList<PlotCurveCharacterstics> chars) {
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
			chars.add(new PlotCurveCharacterstics(this.plotChars.getAttenRelLineType(), color, 1));
			
			System.out.println("Setting params for Attenuation Relationship: " + attenRel.getName());
			Site site = this.setAttenRelParams(attenRel, im);
			
			System.out.print("Calculating comparison curve for " + site.getLocation().getLatitude() + "," + site.getLocation().getLongitude() + "...");
			try {
				ArbitrarilyDiscretizedFunc curve = plotChars.getHazardFunc();
				ArbitrarilyDiscretizedFunc logHazFunction = this.getLogFunction(curve);
				calc.getHazardCurve(logHazFunction, site, attenRel, erf);
				curve = this.unLogFunction(curve, logHazFunction);
				curve.setInfo(this.getCurveParametersInfoAsString(attenRel, erf, site));
				System.out.println("done!");
				curves.add(curve);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			i++;
		}
	}
	
	public String getCyberShakeCurveInfo(CybershakeSite site, CybershakeIM im) {
		String infoString = "Site: "+ site.getFormattedName() + ";\n";
		infoString += "ERF: " + this.selectedERF + ";\n";
		infoString += "SGT Variation ID: " + this.sgtVarID + "; Rup Var Scenario ID: " + this.rupVarScenarioID + ";\n";
		infoString += "SA Period: " + im.getVal() + ";\n";
		
		return infoString;
	}
	
	public String getCurveParametersInfoAsString(AttenuationRelationship imr, EqkRupForecast erf, Site site) {
		return this.getCurveParametersInfoAsHTML(imr, erf, site).replace("<br>", "\n");
	}

	public String getCurveParametersInfoAsHTML(AttenuationRelationship imr, EqkRupForecast erf, Site site) {
		ListIterator<ParameterAPI> imrIt = imr.getOtherParamsIterator();
		String imrMetadata = "IMR = " + imr.getName() + "; ";
		while (imrIt.hasNext()) {
			ParameterAPI tempParam = imrIt.next();
			imrMetadata += tempParam.getName() + " = " + tempParam.getValue();
		}
		String siteMetadata = site.getParameterListMetadataString();
		String imtName = imr.getIntensityMeasure().getName();
		String imtMetadata = "IMT = " + imtName;
		if (imtName.toLowerCase().equals("sa")) {
			imtMetadata += "; ";
			ParameterAPI damp = imr.getParameter(AttenuationRelationship.DAMPING_NAME);
			if (damp != null)
				imtMetadata += damp.getName() + " = " + damp.getValue() + "; ";
			ParameterAPI period = imr.getParameter(AttenuationRelationship.PERIOD_NAME);
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
	
	private Site setAttenRelParams(AttenuationRelationship attenRel, CybershakeIM im) {
//		// set 1 sided truncation
//		StringParameter truncTypeParam = (StringParameter)attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME);
//		truncTypeParam.setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
//		// set truncation at 3 std dev's
//		DoubleParameter truncLevelParam = (DoubleParameter)attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME);
//		truncLevelParam.setValue(3.0);
		
		attenRel.getParameter(AttenuationRelationship.STD_DEV_TYPE_NAME).setValue(AttenuationRelationship.STD_DEV_TYPE_TOTAL);
		
		// set IMT
		attenRel.setIntensityMeasure(AttenuationRelationship.SA_NAME);
		DoubleDiscreteParameter saPeriodParam = (DoubleDiscreteParameter)attenRel.getParameter(AttenuationRelationship.PERIOD_NAME);
		ArrayList<Double> allowedVals = saPeriodParam.getAllowedDoubles();
		
		double closestPeriod = Double.NaN;
		double minDistance = Double.POSITIVE_INFINITY;
		
		for (double period : allowedVals) {
			double dist = Math.abs(period - im.getVal());
			if (dist < minDistance) {
				minDistance = dist;
				closestPeriod = period;
			}
		}
		saPeriodParam.setValue(closestPeriod);
//		attenRel.setIntensityMeasure(intensityMeasure)
		
		LocationList locList = new LocationList();
		Location loc = new Location(csSite.lat, csSite.lon);
		locList.addLocation(loc);
		
		Site site = new Site(loc);
		
		String willsClass = "NA";
		double basinDepth = Double.NaN;
		
		try{
			// get the vs 30 and basin depth from cvm
			willsClass = (String)(ConnectToCVM.getWillsSiteTypeFromCVM(locList)).get(0);
			basinDepth = ((Double)(ConnectToCVM.getBasinDepthFromCVM(locList)).get(0)).doubleValue();
			
			Iterator<ParameterAPI> it = attenRel.getSiteParamsIterator(); // get site params for this IMR
			while(it.hasNext()) {
				ParameterAPI tempParam = it.next();
				if(!site.containsParameter(tempParam))
					site.addParameter(tempParam);
				//adding the site Params from the CVM, if site is out the range of CVM then it
				//sets the site with whatever site Parameter Value user has choosen in the application
				boolean flag = siteTranslator.setParameterValue(tempParam,willsClass,basinDepth);
				if( !flag ) {
					System.err.println("Param " + tempParam.getName() + " not set for site! Not available from web service.");
					if (tempParam.getName().equals(AttenuationRelationship.VS30_NAME)) {
						if (manualVs30 > 0) {
							tempParam.setValue(manualVs30);
							System.out.println("Using previously set Vs30 value of " + tempParam.getValue());
						} else {
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
							manualVs30 = (Double)tempParam.getValue();
						}
					} else {
						System.out.println("Using default value: " + tempParam.getValue());
					}
				} else {
					System.out.println("Param: "+tempParam.getName() + ", Value: " + tempParam.getValue());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return site;
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
	
	public void setERFComparison(EqkRupForecast erf) {
		this.erf = erf;
	}

	public double getMaxX() {
		if (currentPeriod > 0)
			return plotChars.getXMax(currentPeriod);
		return plotChars.getXMax();
	}

	public double getMaxY() {
		return plotChars.getYMax();
	}

	public double getMinX() {
		return plotChars.getXMin();
	}

	public double getMinY() {
		return plotChars.getYMin();
	}
	
	public int getAxisLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	public int getPlotLabelFontSize() {
		// TODO Auto-generated method stub
		return 12;
	}

	public int getTickLabelFontSize() {
		// TODO Auto-generated method stub
		return 10;
	}

	public void setXLog(boolean flag) {
		plotChars.setXLog(flag);
	}

	public void setYLog(boolean flag) {
		plotChars.setYLog(flag);
	}
	
	boolean xLogFlag = true;
	
	/**
	 * Takes the log of the X-values of the given function
	 * @param arb
	 * @return A function with points (Log(x), 1)
	 */
	private ArbitrarilyDiscretizedFunc getLogFunction(DiscretizedFuncAPI arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < arb.getNum(); ++i)
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
			DiscretizedFuncAPI oldHazFunc, DiscretizedFuncAPI logHazFunction) {
		int numPoints = oldHazFunc.getNum();
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
	
	public static Options createOptions() {
		Options ops = new Options();
		
		// ERF
		Option erf = new Option("e", "erf-id", true, "ERF ID");
		erf.setRequired(true);
		ops.addOption(erf);
		
		Option rv = new Option("r", "rv-id", true, "Rupture Variation ID");
		rv.setRequired(true);
		ops.addOption(rv);
		
		Option sgt = new Option("sgt", "sgt-var-id", true, "STG Variation ID");
		sgt.setRequired(true);
		ops.addOption(sgt);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		ops.addOption(erfFile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true, "XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		ops.addOption(attenRelFiles);
		
		Option site = new Option("s", "site", true, "Site short name");
		site.setRequired(true);
		ops.addOption(site);
		
		Option period = new Option("p", "period", true, "Period(s) to calculate. Multiple periods should be comma separated");
		period.setRequired(true);
		ops.addOption(period);
		
		Option pass = new Option("pf", "password-file", true, "Path to a file that contains the username and password for " + 
				"inserting curves into the database. Format should be \"user:pass\"");
		ops.addOption(pass);
		
		Option noAdd = new Option("n", "no-add", false, "Flag to not automatically calculate curves not in the database");
		ops.addOption(noAdd);
		
		Option force = new Option("f", "force-add", false, "Flag to add curves to db without prompt");
		ops.addOption(force);
		
		Option help = new Option("?", "help", false, "Display this message");
		ops.addOption(help);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		ops.addOption(output);
		
		Option type = new Option("t", "type", true, "Plot save type. Options are png, pdf, and jpg. Multiple types can be " + 
				"comma separated (default is " + TYPE_DEFAULT + ")");
		ops.addOption(type);
		
		Option width = new Option("w", "width", true, "Plot width (default = " + PLOT_WIDTH_DEFAULT + ")");
		ops.addOption(width);
		
		Option height = new Option("h", "height", true, "Plot width (default = " + PLOT_HEIGHT_DEFAULT + ")");
		ops.addOption(height);
		
		Option vs30 = new Option("v", "vs30", true, "Specify default Vs30 for sites with no Vs30 data, or leave blank " + 
				"for default value. Otherwise, you will be prompted to enter vs30 interactively if needed.");
		ops.addOption(vs30);
		
		Option plotChars = new Option("pl", "plot-chars-file", true, "Specify the path to a plot characteristics XML file");
		ops.addOption(plotChars);
		
		return ops;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "HazardCurvePlotter", options, true );
		System.exit(2);
	}
	
	public static void printUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 80, "HazardCurvePlotter", options);
		pw.flush();
		System.exit(2);
	}

	public static void main(String args[]) throws DocumentException, InvocationTargetException {
		
		try {
			Options options = createOptions();
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help")) {
					printHelp(options);
				}
				
				HazardCurvePlotter plotter = new HazardCurvePlotter(Cybershake_OpenSHA_DBApplication.db, cmd);
				
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
						printHelp(options);
					}
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
//				e1.printStackTrace();
				}
				System.err.println(e.getMessage());
				printUsage(options);
//			e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				printUsage(options);
			}
			
			System.out.println("Done!");
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
}
