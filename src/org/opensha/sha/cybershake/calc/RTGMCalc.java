package org.opensha.sha.cybershake.calc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.calc.HazardCurveCalculator;
import org.opensha.sha.calc.hazardMap.HazardCurveSetCalculator;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.Component;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurve2DB;
import org.opensha.sha.cybershake.db.HazardDataset2DB;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sra.rtgm.RTGM;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public class RTGMCalc {
	
	private int runID;
	private Component component;
	private File outputDir;
	
	private DBAccess db;
	private HazardCurve2DB curves2db;
	private Runs2DB runs2db;
	private CybershakeSiteInfo2DB sites2db;
	private HazardDataset2DB dataset2db;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
	
	private ERF erf;
	private List<AttenuationRelationship> attenRels;
	
	public RTGMCalc(CommandLine cmd, DBAccess db) {
		Preconditions.checkArgument(cmd.hasOption("run-id"));
		int runID = Integer.parseInt(cmd.getOptionValue("run-id"));
		
		Component component = null;
		if (cmd.hasOption("component"))
			component = CybershakeIM.fromShortName(cmd.getOptionValue("component"), Component.class);
		
		Preconditions.checkArgument(cmd.hasOption("output-dir"));
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		
		init(runID, component, outputDir, db);
		
		if (cmd.hasOption("ef") && cmd.hasOption("af")) {
			String erfFile = cmd.getOptionValue("ef");
			String attenFiles = cmd.getOptionValue("af");
			
			try {
				erf = ERFSaver.LOAD_ERF_FROM_FILE(erfFile);
				attenRels = Lists.newArrayList();
				
				for (String attenRelFile : HazardCurvePlotter.commaSplit(attenFiles)) {
					AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
					attenRels.add(attenRel);
				}
				
				erf.updateForecast();
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
	}
	
	public RTGMCalc(int runID, Component component, File outputDir, DBAccess db) {
		init(runID, component, outputDir, db);
	}
	
	private void init(int runID, Component component, File outputDir, DBAccess db) {
		Preconditions.checkArgument(runID >= 0, "Run ID must be >= 0");
		// component CAN be null
		Preconditions.checkArgument(outputDir != null, "Output dir must me supplied");
		Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		Preconditions.checkArgument(db != null, "DB connection cannot be null");
		this.runID = runID;
		this.component = component;
		this.outputDir = outputDir;
		this.db = db;
		
		curves2db = new HazardCurve2DB(db);
		runs2db = new Runs2DB(db);
		sites2db = new CybershakeSiteInfo2DB(db);
		dataset2db = new HazardDataset2DB(db);
	}
	
	public boolean calc() throws IOException {
		List<Integer> curveIDs = curves2db.getAllHazardCurveIDs(runID, -1);
		// filter out any oddball probability models
		int origSize = curveIDs.size();
		for (int i=curveIDs.size(); --i>=0;) {
			int datasetID = curves2db.getDatasetIDForCurve(curveIDs.get(i));
			int probModelID = dataset2db.getProbModelID(datasetID);
			if (probModelID != 1)
				// 1 is time independent poisson
				curveIDs.remove(i);
		}
		if (origSize != curveIDs.size())
			System.out.println((origSize-curveIDs.size())+"/"+origSize+" were filtered out due to Prob Model ID != 1");
		List<CybershakeIM> ims = Lists.newArrayList();
		for (int curveID : curveIDs)
			ims.add(curves2db.getIMForCurve(curveID));
		if (component != null) {
			origSize = curveIDs.size();
			// remove all curve IDs that are the wrong component
			for (int i=curveIDs.size(); --i>=0;) {
				CybershakeIM im = ims.get(i);
				if (im.getComponent() != component) {
					curveIDs.remove(i);
					ims.remove(i);
				}
			}
			System.out.println(curveIDs.size()+"/"+origSize+" curves match specified component: "
					+component.getShortName());
		}
		if (curveIDs.isEmpty()) {
			System.err.println("No matching curves found in database for runID="
					+runID+", component="+component+" (null means any)");
			System.err.println("Curves must be calculated and inserted prior to calculating RTGM.");
			return false;
		}
		
		CybershakeRun run = runs2db.getRun(runID);
		int siteID = run.getSiteID();
		CybershakeSite site = sites2db.getSiteFromDB(siteID);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList("Site Short Name", "Run ID", "IM Type ID", "IM Type",
				"Component", "Period", "CyberShake RTGM (g)");
		if (attenRels != null) {
			for (AttenuationRelationship attenRel : attenRels)
				header.add(attenRel.getShortName());
		}
		csv.addLine(header);
		
		HazardCurveCalculator gmpeCalc = null;
		List<SiteDataValue<?>> datas = null;
		
		for (int i=0; i<curveIDs.size(); i++) {
			int curveID = curveIDs.get(i);
			DiscretizedFunc curve = curves2db.getHazardCurve(curveID);
			validateCurveForRTGM(curve);
			
			CybershakeIM im = ims.get(i);
			List<String> line = Lists.newArrayList(site.short_name, runID+"", im.getID()+"",
					im.getMeasure().getShortName(), im.getComponent().getShortName(), (float)im.getVal()+"");
			
			System.out.println("Calculating RTGM for: "+Joiner.on(",").join(line));
			
			// calculate RTGM
			// first null is frequency which is used for a scaling factor, which we disable
			// second null is Beta value, we want default
			double rtgm = calcRTGM(curve);
			line.add(rtgm+"");
			
			// GMPE comparisons
			if (attenRels != null) {
				if (gmpeCalc == null)
					gmpeCalc = new HazardCurveCalculator();
				if (datas == null) {
					int velModelID = run.getVelModelID();
					OrderedSiteDataProviderList providers = HazardCurvePlotter.createProviders(velModelID);
					datas = providers.getBestAvailableData(site.createLocation());
				}
				Preconditions.checkState((float)erf.getTimeSpan().getDuration() == 1f,
						"ERF duration should be 1 year");
				for (AttenuationRelationship attenRel : attenRels) {
					System.out.println("Calculating comparison RTGM value for "+attenRel.getShortName());
					DiscretizedFunc hazFunction = HazardCurveSetCalculator.getLogFunction(curve.deepClone());
					Site gmpeSite = HazardCurvePlotter.setAttenRelParams(attenRel, im, run, site, datas);
					gmpeCalc.getHazardCurve(hazFunction, gmpeSite, attenRel, erf);
					hazFunction = HazardCurveSetCalculator.unLogFunction(curve, hazFunction);
					validateCurveForRTGM(hazFunction);
					
					rtgm = calcRTGM(hazFunction);
					Preconditions.checkState(rtgm > 0, "RTGM is not positive");
					line.add(rtgm+"");
				}
			}
			
			csv.addLine(line);
		}
		
		String name = site.short_name+"_run"+runID+"_RTGM";
		if (component != null)
			name += "_"+component.getShortName();
		name += "_"+dateFormat.format(new Date())+".csv";
		
		File outputFile = new File(outputDir, name);
		csv.writeToFile(outputFile);
		
		return true; // success
	}
	
	private static void validateCurveForRTGM(DiscretizedFunc curve) {
		// make sure it's not empty
		Preconditions.checkState(curve.getNum() > 2, "curve is empty");
		// make sure it has actual values
		Preconditions.checkState(curve.getMaxY() > 0d, "curve has all zero y values");
		// make sure it is monotonically decreasing
		String xValStr = Iterators.toString(curve.getYValuesIterator());
		for (int j=1; j<curve.getNum(); j++)
			Preconditions.checkState(curve.getY(j) <= curve.getY(j-1),
				"Curve not monotonically decreasing: "+xValStr);
	}
	
	private static double calcRTGM(DiscretizedFunc curve) {
		RTGM calc = RTGM.create(curve, null, null);
		try {
			calc.call();
		} catch (RuntimeException e) {
			System.err.println("RTGM Calc failed for Hazard Curve:\n"+curve);
			System.err.flush();
			throw e;
		}
		double rtgm = calc.get();
		Preconditions.checkState(rtgm > 0, "RTGM is not positive");
		return rtgm;
	}
	
	private static Options createOptions() {
		Options ops = new Options();
		
		Option run = new Option("R", "run-id", true, "Run ID");
		run.setRequired(true);
		ops.addOption(run);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. "
				+ "All will be calculated if ommitted. Options: "
				+Joiner.on(",").join(CybershakeIM.getShortNames(Component.class)));
		component.setRequired(false);
		ops.addOption(component);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		output.setRequired(true);
		ops.addOption(output);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		erfFile.setRequired(false);
		ops.addOption(erfFile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true,
				"XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		attenRelFiles.setRequired(false);
		ops.addOption(attenRelFiles);
		
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
	
	public static void main(String[] args) {
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(RTGMCalc.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				RTGMCalc calc = new RTGMCalc(cmd, Cybershake_OpenSHA_DBApplication.db);
				
				boolean success = calc.calc();
				
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
					// TODO Auto-generated catch block
//				e1.printStackTrace();
				}
				System.err.println(e.getMessage());
				printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				printUsage(options, appName);
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
