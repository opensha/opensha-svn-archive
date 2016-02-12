package org.opensha.sha.cybershake.plot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.ListUtils;
import org.opensha.sha.calc.disaggregation.DisaggregationCalculator;
import org.opensha.sha.calc.params.IncludeMagDistFilterParam;
import org.opensha.sha.calc.params.MagDistCutoffParam;
import org.opensha.sha.calc.params.MaxDistanceParam;
import org.opensha.sha.calc.params.NonSupportedTRT_OptionsParam;
import org.opensha.sha.calc.params.PtSrcDistanceCorrectionParam;
import org.opensha.sha.calc.params.SetTRTinIMR_FromSourceParam;
import org.opensha.sha.cybershake.calc.HazardCurveComputation;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeVelocityModel;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeIMR;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeUCERFWrapper_ERF;
import org.opensha.sha.gui.infoTools.DisaggregationPlotViewerWindow;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import com.google.common.base.Preconditions;

public class DisaggregationPlotter {
	
	public static final PlotType TYPE_DEFAULT = PlotType.PDF;
	
	private DBAccess db;
	private Runs2DB runs2db;
	private HazardCurve2DB curve2db;
	private PeakAmplitudesFromDB amps2db;
	private SiteInfo2DB site2db;
	private HazardCurveComputation curveCalc;
	
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
	
	private ArrayList<PlotType> plotTypes;
	
	private HashMap<Double, Double> imlToProbsMap;
	
	public DisaggregationPlotter(
			int runID,
			ArrayList<CybershakeIM> ims,
			ArrayList<Double> probLevels,
			ArrayList<Double> imlLevels,
			File outputDir,
			ArrayList<PlotType> plotTypes) {
		initDB();
		init(runID, ims, probLevels, imlLevels, outputDir, plotTypes);
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
		this.run = runs2db.getRun(runID);
		ArrayList<CybershakeIM> ims = HazardCurvePlotter.getIMsFromOptions(cmd, run, curve2db, amps2db);
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
		
		if (cmd.hasOption("t")) {
			String typeStr = cmd.getOptionValue("t");
			
			plotTypes = PlotType.fromExtensions(HazardCurvePlotter.commaSplit(typeStr));
		} else {
			plotTypes = new ArrayList<PlotType>();
			plotTypes.add(PlotType.PDF);
		}
		
		init(runID, ims, probLevels, imlLevels, outputDir, plotTypes);
	}
	
	public void init(
			int runID,
			ArrayList<CybershakeIM> ims,
			ArrayList<Double> probLevels,
			ArrayList<Double> imlLevels,
			File outputDir,
			ArrayList<PlotType> plotTypes) {
		// get the full run description from the DB
		this.run = runs2db.getRun(runID);
		Preconditions.checkNotNull(run, "Error fetching runs from DB");
		
		this.ims = ims;
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
		
		disaggCalc = new DisaggregationCalculator();
		
		disaggParams = new ParameterList();
		disaggParams.addParameter(new MaxDistanceParam());
		disaggParams.addParameter(new IncludeMagDistFilterParam());
		disaggParams.addParameter(new MagDistCutoffParam());
		disaggParams.addParameter(new SetTRTinIMR_FromSourceParam());
		disaggParams.addParameter(new NonSupportedTRT_OptionsParam());
		disaggParams.addParameter(new PtSrcDistanceCorrectionParam());
		
		this.outputDir = outputDir;
	}
	
	public void disaggregate() throws IOException {
		for (CybershakeIM im : ims) {
			double period = im.getVal();
			SA_Param saParam = (SA_Param)imr.getIntensityMeasure();
			List<Double> periods = saParam.getPeriodParam().getAllowedDoubles();
			int closestPeriod = ListUtils.getClosestIndex(periods, period);
			if (closestPeriod < 0)
				throw new IllegalStateException("no match found for period: "+period);
			SA_Param.setPeriodInSA_Param(imr.getIntensityMeasure(), periods.get(closestPeriod));
			
			System.out.println("IMR Metadata: "+imr.getAllParamMetadata());
			
			int curveID = curve2db.getHazardCurveID(run.getRunID(), im.getID());
			System.out.println("Curve ID: "+curveID);
			DiscretizedFunc curve;
			if (curveID < 0) {
				// need to calculate it
				if (curveCalc == null)
					curveCalc = new HazardCurveComputation(db);
				
				ArbitrarilyDiscretizedFunc func = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
				ArrayList<Double> imVals = new ArrayList<Double>();
				for (int i=0; i<func.size(); i++)
					imVals.add(func.getX(i));
				
				System.out.println("Calculating Hazard Curve for "+im.getVal()+" s SA, "+im.getComponent());
				curve = curveCalc.computeHazardCurve(imVals, run, im);
			} else {
				// already in the database
				System.out.println("Fetching precalculated curve from the database");
				curve = curve2db.getHazardCurve(curveID);
			}
			
			imlToProbsMap = new HashMap<Double, Double>();
			// convert prob values to IMLs
			for (double probLevel : probLevels) {
				if (probLevel > curve.getY(0)
						|| probLevel < curve.getY(curve.size() - 1)) {
					System.err.println("Cannot produce plot at prob="+probLevel+" as it is outside of" +
							" the range of the hazard curve");
					continue;
				}
				double imLevel = curve.getFirstInterpolatedX_inLogXLogYDomain(probLevel);
				System.out.println("converted prob of: "+probLevel+" to IML of: "+imLevel);
				imlToProbsMap.put(imLevel, probLevel);
				imlLevels.add(imLevel);
			}
			
			for (double iml : imlLevels) {
				System.out.println("Disaggregating");
				disaggCalc.setMagRange(minMag, numMags, deltaMag);
				disaggCalc.setNumSourcestoShow(numSourcesForDisag);
				disaggCalc.setShowDistances(showSourceDistances);
				boolean success = disaggCalc.disaggregate(Math.log(iml), site, imr, erf, disaggParams);
				if (!success)
					throw new RuntimeException("Disagg calc failed (see errors above, if any).");
				disaggCalc.setMaxZAxisForPlot(maxZAxis);
				System.out.println("Done Disaggregating");
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
					
					boolean textOnly = plotTypes.size() == 1 && plotTypes.get(0) == PlotType.TXT;
					
					String address = null;
					
					if (!textOnly) {
						System.out.println("Fetching plot...");
						address = disaggCalc.getDisaggregationPlotUsingServlet(metadata);
					}
					
					boolean isProb = imlToProbsMap.containsKey(iml);
					double prob;
					if (isProb)
						prob = imlToProbsMap.get(iml);
					else
						prob = curve.getInterpolatedY_inLogXLogYDomain(iml);
					
					Date date;
					if (curveID < 0)
						date = new Date(); // now
					else
						date = curve2db.getDateForCurve(curveID);
					String dateStr = HazardCurvePlotter.dateFormat.format(date);
					String periodStr = "SA_" + HazardCurvePlotter.getPeriodStr(im.getVal()) + "sec";
					String outFileName = csSite.short_name + "_ERF" + run.getERFID() + "_Run" + run.getRunID();
					if (isProb)
						outFileName += "_DisaggPOE_"+(float)prob;
					else
						outFileName += "_DisaggIML_"+(float)iml+"_G";
					outFileName += "_" + periodStr + "_" + dateStr;
					
					String meanModeText;
					if (isProb)
						meanModeText = "Disaggregation Results for Prob = " + prob
						+ " (for IML = " + (float) iml + ")";
					else
						meanModeText = "Disaggregation Results for IML = " + iml
						+ " (for Prob = " + (float) prob + ")";
					
					meanModeText += disaggCalc.getMeanAndModeInfo();
					
					CybershakeVelocityModel velModel = runs2db.getVelocityModel(run.getVelModelID());
					String metadataText = HazardCurvePlotter.getCyberShakeCurveInfo(curveID, csSite, run, velModel, im,
							null, null, null);
					String binDataText = disaggCalc.getBinData();
					String sourceDataText = disaggCalc.getDisaggregationSourceInfo();
					
					for (PlotType type : plotTypes) {
						if (type == PlotType.PDF) {
							String outputFileName = outputDir.getAbsolutePath()+File.separator+outFileName+".pdf";
							DisaggregationPlotViewerWindow.saveAsPDF(
									address+DisaggregationCalculator.DISAGGREGATION_PLOT_PDF_NAME,
									outputFileName, meanModeText, metadataText, binDataText, sourceDataText);
						} else if (type == PlotType.PNG) {
							downloadPlot(address+ DisaggregationCalculator.DISAGGREGATION_PLOT_PNG_NAME, outFileName, "png");
						} else if (type == PlotType.JPG || type == PlotType.JPEG) {
							downloadPlot(address+ DisaggregationCalculator.DISAGGREGATION_PLOT_JPG_NAME, outFileName, "jpg");
						} else if (type == PlotType.TXT) {
							String outputFileName = outputDir.getAbsolutePath()+File.separator+outFileName+".txt";
							DisaggregationPlotViewerWindow.saveAsTXT(outputFileName, meanModeText, metadataText,
									binDataText, sourceDataText);
						} else {
							throw new IllegalArgumentException("Unknown plot type: "+type);
						}
					}
					
					System.out.println("DONE.");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private void downloadPlot(String address, String outFileName, String ext) throws IOException {
		File outFile = new File(outputDir.getAbsolutePath()+File.separator+outFileName+"."+ext);
		System.out.println("Downloading disagg "+ext+" plot to: "+outFile.getAbsolutePath());
		FileUtils.downloadURL(address, outFile);
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
		
		Option type = new Option("t", "type", true, "Plot save type. Options are png, pdf, and txt. Multiple types can be " + 
				"comma separated (default is " + TYPE_DEFAULT.getExtension() + ")");
		type.setRequired(false);
		ops.addOption(type);
		
		
		
		return ops;
	}

	public static void main(String args[]) throws DocumentException, InvocationTargetException {
//		String[] newArgs = {"-R", "247", "-p", "3", "-pr", "4.0e-4", "-i", "0.2,0.5", "-o", "/tmp"};
//		String[] newArgs = {"--help"};
//		String[] newArgs = {"-R", "792", "-p", "3", "-pr", "4.0e-4", "-t", "pdf", "-o", "D:\\Documents\\temp"};
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
