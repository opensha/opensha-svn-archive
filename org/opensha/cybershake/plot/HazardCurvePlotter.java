package org.opensha.cybershake.plot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JFrame;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
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
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
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
	
	private double maxSourceDistance = 200;
	
	private double minX = 0;
	private double maxX = 2;
	private double minY = Double.parseDouble("1.0E-6");
	private double maxY = 1.0;
	
	private boolean xLog = false;
	private boolean yLog = true;
	
	private DBAccess db;
	private int erfID;
	private int rupVarScenarioID;
	private int sgtVarID;
	
	private HazardCurve2DB curve2db;
	
	private GraphPanel gp;
	
	private String yAxisName = "Probability Rate (1/yr)";
	
	private EqkRupForecast erf = null;
	private ArrayList<AttenuationRelationship> attenRels = new ArrayList<AttenuationRelationship>();
	
	private SiteInfo2DB site2db = null;
	private PeakAmplitudesFromDB amps2db = null;
	
	CybershakeSite csSite = null;
	
	SiteTranslator siteTranslator = new SiteTranslator();
	
	HazardCurveCalculator calc;
	
	private boolean customAxis = true;
	
	CybershakeERF selectedERF;
	
	private ArrayList<Color> attenRelColors = new ArrayList<Color>();
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yyyy");
	
	private double manualVs30  = -1;
	
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
		
		setRobColors();
		try {
			calc = new HazardCurveCalculator();
			calc.setMaxSourceDistance(maxSourceDistance);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void setRobColors() {
		attenRelColors.clear();
		attenRelColors.add(Color.blue);
		attenRelColors.add(Color.green);
		attenRelColors.add(Color.orange);
		attenRelColors.add(Color.YELLOW);
		attenRelColors.add(Color.RED);
	}
	
	public static ArrayList<String> commaSplit(String str) {
		str = str.trim();
		ArrayList<String> vals = new ArrayList<String>();
		for (String val : str.split(",")) {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		HazardCurveComputation curveCalc = null;
		String user = "";
		String pass = "";
		
		String outDir = cmd.getOptionValue("o");
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
		
		int periodNum = 0;
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
								// TODO Auto-generated catch block
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
										user = split[1];
										return false;
									}
								}
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println("Password file not found!");
								return false;
							} catch (IOException e) {
								// TODO Auto-generated catch block
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
							UserAuthDialog auth = new UserAuthDialog(null, true);
							auth.setVisible(true);
							if (auth.isCanceled())
								return false;
							user = auth.getUsername();
							pass = new String(auth.getPassword());
						}
					}
					
					DBAccess writeDB = null;
					try {
						writeDB = new DBAccess(Cybershake_OpenSHA_DBApplication.HOST_NAME,Cybershake_OpenSHA_DBApplication.DATABASE_NAME, user, pass);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					
					// calculate the curve
					if (curveCalc == null)
						curveCalc = new HazardCurveComputation(db);
					
					ArbitrarilyDiscretizedFunc func = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
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
			String outFileName = siteName + "_" + "ERF" + erfID + "_" + periodStr + "_" + dateStr + ".pdf";
			String outFile = outDir + outFileName;
			this.plotCurve(curveID, outFile);
		}
		
		return true;
	}
	
	public void plotCurve(int siteID, int imTypeID, String outFile) {
		int curveID = curve2db.getHazardCurveID(siteID, erfID, rupVarScenarioID, sgtVarID, imTypeID);
		this.plotCurve(curveID, outFile);
	}
	
	public void plotCurve(int curveID, String outFile) {
		System.out.println("Fetching Curve!");
		DiscretizedFuncAPI curve = curve2db.getHazardCurve(curveID);
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.LINE_AND_CIRCLES, Color.BLACK, 1));
		
		
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
		
		String title = csSite.name;
		
		if (!csSite.name.equals(csSite.short_name))
			title += " (" + csSite.short_name + ")";
		
		
		
		System.out.println("Plotting Curve!");
		
		String periodStr = getPeriodStr(im.getVal());
		
		String xAxisName = periodStr + "s SA (g)";
		
		int periodInt = (int)(im.getVal() + 0.5);
		if (periodInt == 5)
			this.maxX = 1;
		else if (periodInt == 10)
			this.maxX = 0.5;
		
		this.gp.setCurvePlottingCharacterstic(chars);
		
		this.gp.drawGraphPanel(xAxisName, yAxisName, curves, xLog, yLog, customAxis, title, this);
		this.gp.setVisible(true);
		
		this.gp.togglePlot(null);
		
		this.gp.validate();
		this.gp.repaint();
		
		JFrame frame = new JFrame();
		
		frame.setContentPane(gp);
		frame.setSize(600, 500);
		frame.setVisible(true);
		
		try {
			System.out.println("Saving PDF to: " + outFile);
			this.gp.saveAsPDF(outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.setVisible(false);
	}
	
	private String getPeriodStr(double period) {
		int periodInt = (int)(period * 100 + 0.5);
		
		return (periodInt / 100) + "";
	}
	
	private void plotComparisions(ArrayList<DiscretizedFuncAPI> curves, CybershakeIM im, int curveID, ArrayList<PlotCurveCharacterstics> chars) {
		System.out.println("Setting ERF Params");
		this.setERFParams();
		
		int i = 0;
		for (AttenuationRelationship attenRel : attenRels) {
			chars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE, attenRelColors.get(i), 1));
			
			System.out.println("Setting params for Attenuation Relationship: " + attenRel.getName());
			Site site = this.setAttenRelParams(attenRel, im);
			
			System.out.print("Calculating comparison curve for " + site.getLocation().getLatitude() + "," + site.getLocation().getLongitude() + "...");
			try {
				ArbitrarilyDiscretizedFunc curve = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
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
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinX() {
		return minX;
	}

	public double getMinY() {
		return minY;
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
		this.xLog = flag;
	}

	public void setYLog(boolean flag) {
		this.yLog = flag;
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
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true, "XML Attenuation Relationship description file(s) for comparison. Multiple files should be comma separated");
		ops.addOption(attenRelFiles);
		
		Option site = new Option("s", "site", true, "Site short name");
		site.setRequired(true);
		ops.addOption(site);
		
		Option period = new Option("p", "period", true, "Period(s) to calculate. Multiple periods should be comma separated");
		period.setRequired(true);
		ops.addOption(period);
		
		Option pass = new Option("pf", "password-file", true, "Path to a file that contains the username and password for inserting curves into the database. Format should be \"user:pass\"");
		ops.addOption(pass);
		
		Option noAdd = new Option("n", "no-add", false, "Flag to not automatically calculate curves not in the database");
		ops.addOption(noAdd);
		
		Option force = new Option("f", "force-add", false, "Flag to add curves to db without prompt");
		ops.addOption(force);
		
		Option help = new Option("h", "help", false, "Display this message");
		ops.addOption(help);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		ops.addOption(output);
		
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
	}
}
