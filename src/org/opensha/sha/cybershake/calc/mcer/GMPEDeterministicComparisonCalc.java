package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
import org.apache.commons.math3.distribution.NormalDistribution;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.siteData.OrderedSiteDataProviderList;
import org.opensha.commons.data.siteData.SiteDataValue;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.util.ClassUtils;
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.calc.UCERF2_AleatoryMagVarRemovalMod;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.CybershakeSiteInfo2DB;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.param.OtherParams.Component;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.util.component.ComponentConverter;
import org.opensha.sha.util.component.ComponentTranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class GMPEDeterministicComparisonCalc {
	
	private static final CyberShakeComponent default_component = CyberShakeComponent.RotD100;
	private static final String default_periods = "1,1.5,2,3,4,5,7.5,10";
	public static final double default_percentile = 84;
	
	private CybershakeRun run;
	private CybershakeSite site;
	private CyberShakeComponent comp;
	
	private List<Double> periods;
	
	private double percentile;
	
	private ERF erf;
	private RuptureProbabilityModifier probMod;
	private List<AttenuationRelationship> attenRels;
	
	private File outputDir;
	
	private Table<Double, AttenuationRelationship, DeterministicResult> resultsTable;
	
	private List<SiteDataValue<?>> siteDatas;
	
	// used to add cybershake results to files
	private List<DeterministicResult> csDeterms;
	
	public GMPEDeterministicComparisonCalc(CommandLine cmd, DBAccess db)
			throws MalformedURLException, DocumentException, InvocationTargetException {
		CybershakeSiteInfo2DB sites2db = new CybershakeSiteInfo2DB(db);
		// get from run ID
		Runs2DB runs2db = new Runs2DB(db);
		CybershakeRun run = runs2db.getRun(Integer.parseInt(cmd.getOptionValue("run-id")));
		CybershakeSite site = sites2db.getSiteFromDB(run.getSiteID());
		
		String periodStr;
		if (cmd.hasOption("period"))
			periodStr = cmd.getOptionValue("period");
		else
			periodStr = default_periods;
		List<Double> periods = HazardCurvePlotter.commaDoubleSplit(periodStr);
		
		double percentile;
		if (cmd.hasOption("percentile"))
			percentile = Double.parseDouble(cmd.getOptionValue("percentile"));
		else
			percentile = default_percentile;
		Preconditions.checkArgument(percentile >= 0 && percentile <= 100,
				"Percentile must be between 0 and 100, inclusive.");
		
		CyberShakeComponent comp;
		if (cmd.hasOption("component"))
			comp = CybershakeIM.fromShortName(cmd.getOptionValue("component"), CyberShakeComponent.class);
		else
			comp = default_component;
		
		String erfFile = cmd.getOptionValue("ef");
		String attenFiles = cmd.getOptionValue("af");

		ERF erf = ERFSaver.LOAD_ERF_FROM_FILE(erfFile);
		List<AttenuationRelationship> attenRels = Lists.newArrayList();

		for (String attenRelFile : HazardCurvePlotter.commaSplit(attenFiles)) {
			AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
			attenRels.add(attenRel);
		}
		Preconditions.checkArgument(!attenRels.isEmpty(), "Must specify at least 1 GMPE");

		erf.updateForecast();
		
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		Preconditions.checkArgument(outputDir != null, "Output dir must me supplied");
		Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		
		init(run, site, comp, periods, percentile, erf, attenRels, outputDir);
	}
	
	public GMPEDeterministicComparisonCalc(CybershakeRun run, CybershakeSite site, CyberShakeComponent comp, List<Double> periods,
			double percentile, ERF erf, List<AttenuationRelationship> attenRels, File outputDir) {
		init(run, site, comp, periods, percentile, erf, attenRels, outputDir);
	}
	
	private void init(CybershakeRun run, CybershakeSite site, CyberShakeComponent comp, List<Double> periods,
			double percentile, ERF erf, List<AttenuationRelationship> attenRels, File outputDir) {
		this.run = run;
		this.site = site;
		this.comp = comp;
		this.periods = periods;
		this.percentile = percentile;
		this.erf = erf;
		this.attenRels = attenRels;
		this.outputDir = outputDir;
		
		if (CyberShakeDeterministicCalc.stripUCERF2Aleatory && erf instanceof MeanUCERF2)
			probMod = new UCERF2_AleatoryMagVarRemovalMod(erf);
	}
	
	public void setSiteData(List<SiteDataValue<?>> siteDatas) {
		this.siteDatas = siteDatas;
	}
	
	public List<SiteDataValue<?>> getSiteData() {
		return siteDatas;
	}
	
	public void setCyberShakeData(List<DeterministicResult> csDeterms) {
		this.csDeterms = csDeterms;
	}
	
	public void calc() throws IOException {
		resultsTable = HashBasedTable.create();
		
		// get site data
		if (siteDatas == null) {
			int velModelID = run.getVelModelID();
			OrderedSiteDataProviderList providers = HazardCurvePlotter.createProviders(velModelID);
			siteDatas = providers.getBestAvailableData(site.createLocation());
		}
		
		CSVFile<String> csv = new CSVFile<String>(true);
		List<String> header = Lists.newArrayList();
		header.add("Period");
		if (csDeterms != null) {
			header.add("CyberShake Source ID");
			header.add("CyberShake Rup ID");
			header.add("CyberShake Name");
			header.add("CyberShake Value (g)");
		}
		for (AttenuationRelationship attenRel : attenRels) {
			header.add(attenRel.getShortName()+" Source ID");
			header.add(attenRel.getShortName()+" Rup ID");
			header.add(attenRel.getShortName()+" Name");
			header.add(attenRel.getShortName()+" Value (g)");
		}
		csv.addLine(header);
		
		for (int i=0; i<periods.size(); i++) {
			double period = periods.get(i);
			List<String> line = Lists.newArrayList();
			line.add(period+"");
			if (csDeterms != null) {
				DeterministicResult csDeterm = csDeterms.get(i);
				if (csDeterm == null) {
					line.add("");
					line.add("");
					line.add("");
					line.add("");
				} else {
					line.add(csDeterm.getSourceID()+"");
					line.add(csDeterm.getRupID()+"");
					line.add(csDeterm.getSourceName()+" (M="+(float)csDeterm.getMag()+")");
					line.add(csDeterm.getVal()+"");
				}
			}
			for (AttenuationRelationship attenRel : attenRels) {
				System.out.println("Calculating deterministic value for "
						+attenRel.getShortName()+", period="+period);
				Site gmpeSite = HazardCurvePlotter.setAttenRelParams(
						attenRel, comp, period, run, site, siteDatas);
				attenRel.setSite(gmpeSite);
				DeterministicResult maxVal = null;
				for (int sourceID=0; sourceID<erf.getNumSources(); sourceID++) {
					ProbEqkSource source = erf.getSource(sourceID);
					if (source.getMinDistance(gmpeSite) > 200d)
						continue;
					List<Integer> rupIDs = CyberShakeDeterministicCalc.getRupIDsForDeterm(erf, sourceID, probMod);
					for (int rupID : rupIDs) {
						ProbEqkRupture rup = source.getRupture(rupID);
						attenRel.setEqkRupture(rup);
						double logMean = attenRel.getMean();
						double stdDev = attenRel.getStdDev();
						NormalDistribution norm = new NormalDistribution(logMean, stdDev);
						double val = Math.exp(norm.inverseCumulativeProbability(percentile/100d));
						if (maxVal == null || val > maxVal.getVal()) {
							maxVal = new DeterministicResult(
									sourceID, rupID, rup.getMag(), source.getName(), val);
						}
					}
				}
				Preconditions.checkNotNull(maxVal);
				maxVal.setVal(getScaledValue(attenRel, period, maxVal.getVal()));
				line.add(maxVal.getSourceID()+"");
				line.add(maxVal.getRupID()+"");
				line.add(maxVal.getSourceName()+" (M="+(float)maxVal.getMag()+")");
				line.add(maxVal.getVal()+"");
				resultsTable.put(period, attenRel, maxVal);
			}
			csv.addLine(line);
		}
		
		if (outputDir == null)
			return;
		
		String perStr;
		if ((float)percentile == (float)((int)percentile))
			perStr = (int)percentile+"";
		else
			perStr = (float)percentile+"";
		
		String name = site.short_name+"_run"+run.getRunID();
		if (csDeterms == null)
			name +="_GMPE_Deterministic_";
		else
			name +="_Deterministic_";
		name += comp.getShortName()+"_"+perStr+"per_"+RTGMCalc.dateFormat.format(new Date())+".csv";
		
		File outputFile = new File(outputDir, name);
		csv.writeToFile(outputFile);
	}
	
	public Table<Double, AttenuationRelationship, DeterministicResult> getResults() {
		return resultsTable;
	}
	
	private double getScaledValue(AttenuationRelationship attenRel, double period, double val) {
		Component gmpeComponent;
		try {
			gmpeComponent = (Component) attenRel.getParameter(ComponentParam.NAME).getValue();
		} catch (ParameterException e) {
			System.err.println("WARNING: GMPE "+attenRel.getShortName()+" doesn't have component parameter, "
					+ "can't scale val as appropriate");
			return val;
		}
		if (gmpeComponent == null) {
			System.err.println("WARNING: GMPE "+attenRel.getShortName()+" has null component, "
					+ "can't scale val as appropriate");
			return val;
		}
		
		// first see if no translation needed (already correct)
		if (comp.isComponentSupported(gmpeComponent)) {
			return val;
		}
		
		// we'll need a translation
		for (Component to : comp.getGMPESupportedComponents()) {
			if (ComponentConverter.isConversionSupported(gmpeComponent, to)) {
				// we have a valid translation!
				System.out.println("Scaling value from "+gmpeComponent+" to "+to);
				ComponentTranslation converter = ComponentConverter.getConverter(gmpeComponent, to);
				double factor = converter.getScalingFactor(period);
				return val*factor;
			}
		}
		
		// we've made it this far, there's a mismatch that can't be scaled away
		System.err.println("NOTE: There is a GMPE/CyberShake component mismatch and no scaling factors exist."
				+ " Using the unscaled curve. CyberShake Component: "+comp.getShortName()
				+", GMPE Component: "+gmpeComponent);
		
		return val;
	}

	private static Options createOptions() {
		Options ops = new Options();
		
		Option run = new Option("R", "run-id", true, "Run ID, needed for site and velocity model");
		run.setRequired(true);
		ops.addOption(run);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. "
				+ "Default: "+default_component.getShortName());
		component.setRequired(false);
		ops.addOption(component);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		output.setRequired(true);
		ops.addOption(output);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file");
		erfFile.setRequired(true);
		ops.addOption(erfFile);
		
		Option period = new Option("p", "period", true, "Period(s) to calculate. Multiple "
				+ "periods should be comma separated (default: "+default_periods+")");
		period.setRequired(false);
		ops.addOption(period);
		
		Option percentile = new Option("per", "percentile", true, "Percentile to calculate. "
				+"Default: "+default_percentile);
		percentile.setRequired(false);
		ops.addOption(percentile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true,
				"XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		attenRelFiles.setRequired(true);
		ops.addOption(attenRelFiles);
		
		Option help = new Option("?", "help", false, "Display this message");
		help.setRequired(false);
		ops.addOption(help);
		
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
			
			String appName = ClassUtils.getClassNameWithoutPackage(GMPEDeterministicComparisonCalc.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				GMPEDeterministicComparisonCalc calc = new GMPEDeterministicComparisonCalc(cmd, Cybershake_OpenSHA_DBApplication.db);
				
				calc.calc();
			} catch (MissingOptionException e) {
				Options helpOps = new Options();
				helpOps.addOption(new Option("h", "help", false, "Display this message"));
				try {
					CommandLine cmd = parser.parse( helpOps, args);
					
					if (cmd.hasOption("help")) {
						printHelp(options, appName);
					}
				} catch (ParseException e1) {}
				System.err.println(e.getMessage());
				printUsage(options, appName);
//			e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
				printUsage(options, appName);
			}
			
			System.out.println("Done!");
//			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
