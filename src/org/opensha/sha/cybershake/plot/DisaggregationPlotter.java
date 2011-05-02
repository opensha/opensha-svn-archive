package org.opensha.sha.cybershake.plot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.calc.disaggregation.DisaggregationPlotData;
import org.opensha.sha.calc.params.IncludeMagDistFilterParam;
import org.opensha.sha.calc.params.MagDistCutoffParam;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.calc.params.NonSupportedTRT_OptionsParam;
import org.opensha.sha.calc.params.SetTRTinIMR_FromSourceParam;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeIMR;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeUCERFWrapper_ERF;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import com.google.common.base.Preconditions;

public class DisaggregationPlotter {
	
	private DBAccess db;
	private Runs2DB runs2db;
	private HazardCurve2DB curve2db;
	private PeakAmplitudesFromDB amps2db;
	private SiteInfo2DB site2db;
	
	private CybershakeRun run;
	private ArrayList<CybershakeIM> ims;
	
	private ArrayList<Double> probLevels;
	private ArrayList<Double> imlLevels;
	
	private CybershakeSite csSite;
	private Site site;
	
	private CyberShakeUCERFWrapper_ERF erf;
	private CyberShakeIMR imr;
	
	private DisaggregationCalculator disaggCalc;
	private ParameterList disaggParams;
	
	private File outputDir;
	
	// disagg plot settings
	private double minMag = 5;
	private int numMags = 10;
	private double deltaMag = 0.5;
	
	private int numSourcesForDisag = 100;
	
	private boolean showSourceDistances = true;
	
	private double maxZAxis = Double.NaN;
	
	
	public DisaggregationPlotter(
			int runID,
			ArrayList<Double> periods,
			ArrayList<Double> probLevels,
			ArrayList<Double> imlLevels,
			File outputDir) {
		initDB();
		init(runID, periods, probLevels, imlLevels, outputDir);
	}
	
	public DisaggregationPlotter(CommandLine cmd) {
		initDB();
		init(cmd);
	}
	
	private void initDB() {
		db = Cybershake_OpenSHA_DBApplication.db;
		runs2db = new Runs2DB(db);
		curve2db = new HazardCurve2DB(db);
		amps2db = new PeakAmplitudesFromDB(db);
		site2db = new SiteInfo2DB(db);
	}
	
	public void init(CommandLine cmd) {
		int runID = HazardCurvePlotter.getRunIDFromOptions(runs2db, curve2db, amps2db, site2db, cmd);
		String periodSTR;
		if (cmd.hasOption("period"))
			periodSTR = cmd.getOptionValue("period");
		else
			periodSTR = HazardCurvePlotter.default_periods;
		ArrayList<Double> periods = HazardCurvePlotter.commaDoubleSplit(periodSTR);
		ArrayList<Double> probLevels = null;
		if (cmd.hasOption("probs"))
			probLevels = HazardCurvePlotter.commaDoubleSplit(cmd.getOptionValue("probs"));
		ArrayList<Double> imlLevels = null;
		if (cmd.hasOption("imls"))
			imlLevels = HazardCurvePlotter.commaDoubleSplit(cmd.getOptionValue("imls"));
		
		File outputDir;
		if (cmd.hasOption("o")) {
			String outDirStr = cmd.getOptionValue("o");
			outputDir = new File(outDirStr);
			if (!outputDir.exists()) {
				boolean success = outputDir.mkdir();
				if (!success) {
					throw new RuntimeException("Directory doesn't exist and couldn't be created: " + outDirStr);
				}
			}
		} else {
			outputDir = new File("");
		}
		
		init(runID, periods, probLevels, imlLevels, outputDir);
	}
	
	public void init(
			int runID,
			ArrayList<Double> periods,
			ArrayList<Double> probLevels,
			ArrayList<Double> imlLevels,
			File outputDir) {
		// get the full run description from the DB
		this.run = runs2db.getRun(runID);
		Preconditions.checkNotNull(run, "Error fetching runs from DB");
		
		// get the IM type IDs from the periods
		this.ims = amps2db.getIMForPeriods(periods, runID, curve2db);
		Preconditions.checkNotNull(ims, "Error fetching IMs from DB");
		Preconditions.checkState(!ims.isEmpty(), "must have at least 1 IM");
		if (probLevels == null)
			probLevels = new ArrayList<Double>();
		this.probLevels = probLevels;
		if (imlLevels == null)
			imlLevels = new ArrayList<Double>();
		this.imlLevels = imlLevels;
		this.csSite = site2db.getSiteFromDB(run.getSiteID());
		this.site = new Site(csSite.createLocation());
		
		erf = new CyberShakeUCERFWrapper_ERF();
		erf.updateForecast();
		imr = new CyberShakeIMR(null);
		imr.setParamDefaults();
		
		imr.setSite(site);
		
		imr.setForcedRunID(runID);
		
		// now set the IMR params
		// hard code the IMT to SA. the period gets set later
		imr.setIntensityMeasure(SA_Param.NAME);
		// now we set CyberShake specific imr params
		imr.getParameter(CyberShakeIMR.RUP_VAR_SCENARIO_PARAM).setValue(run.getRupVarScenID());
		imr.getParameter(CyberShakeIMR.SGT_VAR_PARAM).setValue(run.getSgtVarID());
		
		String velModelStr = runs2db.getVelocityModel(run.getVelModelID()).toString();
		imr.getParameter(CyberShakeIMR.VEL_MODEL_PARAM).setValue(velModelStr);
		
		try {
			disaggCalc = new DisaggregationCalculator();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		
		disaggParams = new ParameterList();
		disaggParams.addParameter(new MaxDistanceParam());
		disaggParams.addParameter(new IncludeMagDistFilterParam());
		disaggParams.addParameter(new MagDistCutoffParam());
		disaggParams.addParameter(new SetTRTinIMR_FromSourceParam());
		disaggParams.addParameter(new NonSupportedTRT_OptionsParam());
		
		this.outputDir = outputDir;
	}
	
	public void disaggregate() throws IOException {
		for (CybershakeIM im : ims) {
			double period = im.getVal();
			SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), (int)period);
			
			System.out.println("IMR Metadata: "+imr.getAllParamMetadata());
			
			int curveID = curve2db.getHazardCurveID(run.getRunID(), im.getID());
			DiscretizedFuncAPI curve = curve2db.getHazardCurve(curveID);
			
			// convert prob values to IMLs
			for (double probLevel : probLevels) {
				if (probLevel > curve.getY(0)
						|| probLevel < curve.getY(curve.getNum() - 1)) {
					System.err.println("Cannot produce plot at prob="+probLevel+" as it is outside of" +
							" the range of the hazard curve");
					continue;
				}
				double imLevel = curve.getFirstInterpolatedX_inLogXLogYDomain(probLevel);
				System.out.println("converted prob of: "+probLevel+" to IML of: "+imLevel);
				imlLevels.add(imLevel);
			}
			
			for (double iml : imlLevels) {
				try {
					System.out.println("Disaggregating");
					disaggCalc.setMagRange(minMag, numMags, deltaMag);
					disaggCalc.setNumSourcestoShow(numSourcesForDisag);
					disaggCalc.setShowDistances(showSourceDistances);
					boolean success = disaggCalc.disaggregate(Math.log(iml), site, imr, erf, disaggParams);
					if (!success)
						throw new RuntimeException("Disagg calc failed (see errors above, if any).");
					disaggCalc.setMaxZAxisForPlot(maxZAxis);
					System.out.println("Done Disaggregating");
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
				String metadata = "temp metadata";
				try {
					
					
//					System.out.println("Doing test local disagg plot");
//					String dir = "/tmp/disagg";
//					System.out.println("getting disagg data");
//					DisaggregationPlotData data = disaggCalc.getDisaggPlotData();
//					double[][][] pdf3d = data.getPdf3D();
//					for (double[][] twoD : pdf3d) {
//						String line = null;
//						for (double[] oneD : twoD) {
//							String cell = null;
//							for (double d : oneD) {
//								if (cell == null)
//									cell = "[";
//								else
//									cell += ",";
//								cell += d;
//							}
//							cell += "]";
//							if (line == null)
//								line = "";
//							else
//								line += " ";
//							line += cell;
//						}
//						System.out.println(line);
//					}
//					System.out.println("creating disagg script");
//					ArrayList<String> gmtMapScript =
//						DisaggregationCalculator.createGMTScriptForDisaggregationPlot(data, dir);
//					System.out.println("writing disagg script");
//					FileWriter fw = new FileWriter(dir+"/gmtScript.txt");
//					BufferedWriter bw = new BufferedWriter(fw);
//					int size = gmtMapScript.size();
//					for (int i = 0; i < size; ++i) {
//						bw.write( (String) gmtMapScript.get(i) + "\n");
//					}
//					bw.close();
//					System.out.println("DONE. script in: "+dir);
					
					
					System.out.println("Fetching plot...");
					String address = disaggCalc.getDisaggregationPlotUsingServlet(metadata);
					address += DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME;
					Date date = curve2db.getDateForCurve(curveID);
					String dateStr = HazardCurvePlotter.dateFormat.format(date);
					String periodStr = "SA_" + HazardCurvePlotter.getPeriodStr(im.getVal()) + "sec";
					String outFileName = csSite.short_name + "_ERF" + run.getERFID() + "_Run" + run.getRunID();
					outFileName += "_Disagg_"+iml+"_G_" + periodStr + "_" + dateStr+".pdf";
					File outFile = new File(outputDir.getAbsolutePath()+File.separator+outFileName);
					System.out.println("Downloading disagg plot to: "+outFile.getAbsolutePath());
					FileUtils.downloadURL(address, outFile);
					System.out.println("DONE.");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * This creates the apache cli options
	 * 
	 * @return
	 */
	private static Options createOptions() {
		Options ops = HazardCurvePlotter.createCommonOptions();
		
		Option probs = new Option("pr", "probs", true, "Probabilities (1 year) to disaggregate at. " +
				"Multiple probabilities should be comma separated.");
		probs.setRequired(false);
		ops.addOption(probs);
		
		Option imls = new Option("i", "imls", true, "Intensity Measure Levels (IMLs) to disaggregate at. " +
		"Multiple IMLs should be comma separated.");
		imls.setRequired(false);
		ops.addOption(imls);
		
		
		
		return ops;
	}

	public static void main(String args[]) throws DocumentException, InvocationTargetException {
//		String[] newArgs = {"-R", "247", "-p", "3", "-pr", "4.0e-4", "-i", "0.2,0.5", "-o", "/tmp"};
//		String[] newArgs = {"--help"};
//		String[] newArgs = {"-R", "792", "-p", "3", "-pr", "4.0e-4", "-o", "/tmp"};
//		args = newArgs;
		
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(DisaggregationPlotter.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				HazardCurvePlotter.printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					HazardCurvePlotter.printHelp(options, appName);
				}
				
				DisaggregationPlotter disagg = new DisaggregationPlotter(cmd);
				disagg.disaggregate();
			} catch (MissingOptionException e) {
				// TODO Auto-generated catch block
				Options helpOps = new Options();
				helpOps.addOption(new Option("h", "help", false, "Display this message"));
				try {
					CommandLine cmd = parser.parse( helpOps, args);
					
					if (cmd.hasOption("help")) {
						HazardCurvePlotter.printHelp(options, appName);
					}
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
//				e1.printStackTrace();
				}
				System.err.println(e.getMessage());
				HazardCurvePlotter.printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				HazardCurvePlotter.printUsage(options, appName);
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
