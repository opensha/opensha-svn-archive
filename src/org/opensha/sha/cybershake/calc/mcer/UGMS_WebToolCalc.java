package org.opensha.sha.cybershake.calc.mcer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import org.dom4j.Document;
import org.dom4j.Element;
import org.jfree.data.Range;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.siteData.impl.WillsMap2006;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.Interpolate;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.hazardMap.BinaryHazardCurveReader;
import org.opensha.sha.calc.mcer.ASCEDetLowerLimitCalc;
import org.opensha.sha.calc.mcer.CurveBasedMCErProbabilisitCalc;
import org.opensha.sha.calc.mcer.DeterministicResult;
import org.opensha.sha.calc.mcer.MCErCalcUtils;
import org.opensha.sha.cybershake.db.CachedPeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.CybershakeSite;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.earthquake.ERF;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UGMS_WebToolCalc {
	
	private static DBAccess db;
	
	private Location loc;
	
	private File gmpeDir;
	private double gmpeSpacing;
	private String gmpeERF;
	private List<String> siteClassNames;
	private double userVs30;
	
	private File outputDir;
	
	public static Map<String, Double> vs30Map = Maps.newHashMap();
	private static final double minCalcVs30 = 150d;
	private static final double maxCalcVs30 = 1000d;
	static {
		vs30Map.put("AorB",         1000d);
		vs30Map.put("BBC",             880d);
		vs30Map.put("BC",               760d);
		vs30Map.put("BCC",             662d);
		vs30Map.put("C",                 564d);
		vs30Map.put("CCD",             465d);
		vs30Map.put("CD",               366d);
		vs30Map.put("CDD",             320d);
		vs30Map.put("D",                 274d);
		vs30Map.put("DDE",             229d);
		vs30Map.put("DE",               183d);
		vs30Map.put("DEE",             166d);
		vs30Map.put("E",                 150d);
		vs30Map.put("Wills",              null);
	}
	
	private static final double CS_MAX_DIST = 10d;
	private static final double GMPE_MAX_DIST = 5d;
	
	private static final int IM_TYPE_ID_FOR_SEARCH = 146; // RotD100, 3s
	private static final CyberShakeComponent component = CyberShakeComponent.RotD100;
	
	private static final double[] periods = {0.01,0.02,0.03,0.05,0.075,0.1,0.15,0.2,0.25,0.3,0.4,
			0.5,0.75,1.0,1.5,2.0,3.0,4.0,5.0,7.5,10.0};
	
	private CyberShakeSiteRun csRun; // list for future interpolation
	private File csDataFile;
	private double csDataSpacing;
	
	private Runs2DB runs2db;
	private SiteInfo2DB sites2db;
	
	private CyberShakeMCErDeterministicCalc csDetCalc;
	private CyberShakeMCErProbabilisticCalc csProbCalc;
	
	private DiscretizedFunc csDetSpectrum;
	private DiscretizedFunc csProbSpectrum;
	private DiscretizedFunc csMCER;
	
	private DiscretizedFunc gmpeMCER;
	
	private Document xmlMetadataDoc;
	private Element xmlRoot;
	private Element metadataEl;
	private Element csSiteEl;
	private Element gmpeSiteEl;
	private Element resultsEl;
	
	public UGMS_WebToolCalc(CommandLine cmd) {
		xmlMetadataDoc = XMLUtils.createDocumentWithRoot();
		xmlRoot = xmlMetadataDoc.getRootElement();
		
		metadataEl = xmlRoot.addElement("Metadata");
		metadataEl.addAttribute("startTimeMillis", System.currentTimeMillis()+"");
		metadataEl.addAttribute("dbHost", Cybershake_OpenSHA_DBApplication.ARCHIVE_HOST_NAME);
		metadataEl.addAttribute("component", component.name());
		
		outputDir = new File(cmd.getOptionValue("output-dir"));
		Preconditions.checkState(outputDir.exists() || outputDir.mkdirs(),
				"Output dir doesn't exist and couldn't be created: %s", outputDir.getAbsolutePath());
		
		gmpeDir = new File(cmd.getOptionValue("gmpe-dir"));
		Preconditions.checkState(gmpeDir.exists(), "GMPE dir doesn't exist: %s", gmpeDir.getAbsolutePath());
		gmpeSpacing = Double.parseDouble(cmd.getOptionValue("gmpe-spacing"));
		gmpeERF = cmd.getOptionValue("gmpe-erf");
		
		if (cmd.hasOption("cs-data-file")) {
			Preconditions.checkArgument(cmd.hasOption("cs-spacing"), "Must supply spacing with CS data file");
			csDataFile = new File(cmd.getOptionValue("cs-data-file"));
			Preconditions.checkState(csDataFile.exists(), "CS data file doesn't exist: %s", csDataFile.getAbsolutePath());
			
			csDataSpacing = Double.parseDouble(cmd.getOptionValue("cs-spacing"));
			
			// search by location
			Preconditions.checkState(cmd.hasOption("latitude"), "Must supply latitude when using precomputed CS data files");
			Preconditions.checkState(cmd.hasOption("longitude"), "Must supply latitude when using precomputed CS data files");
			double lat = Double.parseDouble(cmd.getOptionValue("latitude"));
			double lon = Double.parseDouble(cmd.getOptionValue("longitude"));
			loc = new Location(lat, lon);
			System.out.println("User location: "+loc);
		} else {
			db = new DBAccess(Cybershake_OpenSHA_DBApplication.ARCHIVE_HOST_NAME, Cybershake_OpenSHA_DBApplication.DATABASE_NAME);
			runs2db = new Runs2DB(db);
			sites2db = new SiteInfo2DB(db);
			
			if (cmd.hasOption("run-id")) {
				// calculation happening at a CyberShake site
				Preconditions.checkState(!cmd.hasOption("latitude"), "Can't specify both a location and a Run ID");
				
				Integer runID = Integer.parseInt(cmd.getOptionValue("run-id"));
				
				CybershakeRun run = runs2db.getRun(runID);
				Preconditions.checkNotNull(run, "No run found with id=%s", runID);
				CybershakeSite site = sites2db.getSiteFromDB(run.getSiteID());
				loc = site.createLocation();
				
				csRun = new CyberShakeSiteRun(site, run);
			} else {
				// calculation happening at an arbitrary point
				// get all completed runs
				Preconditions.checkState(cmd.hasOption("dataset-id"), "Must specify a CyberShake Dataset ID if no Run ID is specified");
				int datasetID = Integer.parseInt(cmd.getOptionValue("dataset-id"));
				List<CyberShakeSiteRun> completedRuns = getCompletedRunsForDataset(datasetID, IM_TYPE_ID_FOR_SEARCH);
				Preconditions.checkState(!completedRuns.isEmpty(),
						"No runs found for datasetID=%s with imTypeID=%s", datasetID, IM_TYPE_ID_FOR_SEARCH);
				List<CyberShakeSiteRun> csRuns = Lists.newArrayList();
				if (cmd.hasOption("site-id")) {
					// search by site ID
					Preconditions.checkState(!cmd.hasOption("latitude"), "Can't specify both a location and a Site ID");
					Preconditions.checkState(!cmd.hasOption("latitude"), "Can't specify both a Site ID and Name");
					Integer siteID = Integer.parseInt(cmd.getOptionValue("site-id"));
					for (CyberShakeSiteRun run : completedRuns)
						if (run.getCS_Site().id == siteID)
							csRuns.add(run);
					Preconditions.checkState(!csRuns.isEmpty(),
							"No runs found for datasetID=%s with imTypeID=%s and siteID=%s", datasetID, IM_TYPE_ID_FOR_SEARCH, siteID);
					loc = csRuns.get(0).getLocation();
				} else if (cmd.hasOption("site-name")) {
					// search by site name
					Preconditions.checkState(!cmd.hasOption("latitude"), "Can't specify both a location and a Site Name");
					String siteName = cmd.getOptionValue("site-name");
					for (CyberShakeSiteRun run : completedRuns)
						if (run.getCS_Site().short_name.equals(siteName) || run.getCS_Site().name.equals(siteName))
							csRuns.add(run);
					Preconditions.checkState(!csRuns.isEmpty(),
							"No runs found for datasetID=%s with imTypeID=%s and siteName='%s'", datasetID, IM_TYPE_ID_FOR_SEARCH, siteName);
					loc = csRuns.get(0).getLocation();
				} else {
					// search by location
					Preconditions.checkState(cmd.hasOption("latitude"), "Must supply latitude (or site/run ID)");
					Preconditions.checkState(cmd.hasOption("longitude"), "Must supply longitude (or site/run ID)");
					double lat = Double.parseDouble(cmd.getOptionValue("latitude"));
					double lon = Double.parseDouble(cmd.getOptionValue("longitude"));
					loc = new Location(lat, lon);
					System.out.println("User location: "+loc);
					
					System.out.println("Searching for nearby completed sites");
					Region reg = new Region(loc, CS_MAX_DIST);
					for (CyberShakeSiteRun run : completedRuns)
						if (reg.contains(run.getLocation()))
							csRuns.add(run);
					Preconditions.checkState(!csRuns.isEmpty(),
							"No runs found for datasetID=%s with imTypeID=%s within %km of %s",
							datasetID, IM_TYPE_ID_FOR_SEARCH, CS_MAX_DIST, loc);
				}
				
				System.out.println(csRuns.size()+" completed nearby sites found: ");
				List<Double> distances = Lists.newArrayList();
				for (CyberShakeSiteRun run : csRuns)
					distances.add(LocationUtils.horzDistanceFast(loc, run.getLocation()));
				csRuns = ComparablePairing.getSortedData(distances, csRuns);
				for (CyberShakeSiteRun run : csRuns)
					System.out.println("\tRunID="+run.getCS_Run().getRunID()+"\tSite="+run.getCS_Site().short_name);
				
				csRun = csRuns.get(0);
			}
			
			System.out.println("Using closest CS Site location: "+csRun.getLocation());
			
			double minDist = LocationUtils.horzDistanceFast(loc, csRun.getLocation());
			System.out.println("Min dist: "+minDist+" km");
			
			csSiteEl = xmlRoot.addElement("CyberShakeRun");
			csSiteEl.addAttribute("runID", csRun.getCS_Run().getRunID()+"");
			csSiteEl.addAttribute("siteID", csRun.getCS_Site().id+"");
			csSiteEl.addAttribute("siteShortName", csRun.getCS_Site().short_name);
			csSiteEl.addAttribute("siteName", csRun.getCS_Site().name);
			csSiteEl.addAttribute("distFromUserLoc", minDist+"");
			csSiteEl.addAttribute("latitude", csRun.getLocation().getLatitude()+"");
			csSiteEl.addAttribute("longitude", csRun.getLocation().getLongitude()+"");
			
			File csCacheDir = new File(cmd.getOptionValue("cs-dir"));
			Preconditions.checkState(csCacheDir.exists(), "CS cache dir doesn't exist: %s", csCacheDir.getAbsolutePath());
			
			ERF csERF;
			try {
				csERF = ERFSaver.LOAD_ERF_FROM_FILE(UGMS_WebToolCalc.class.getResource("/org/opensha/sha/cybershake/conf/MeanUCERF.xml"));
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			csERF.updateForecast();
			CachedPeakAmplitudesFromDB amps2db = new CachedPeakAmplitudesFromDB(db, csCacheDir, csERF);
			
			csDetCalc = new CyberShakeMCErDeterministicCalc(amps2db, csERF, component);
			csProbCalc = new CyberShakeMCErProbabilisticCalc(db, component);
		}
		
		metadataEl.addAttribute("latitude", loc.getLatitude()+"");
		metadataEl.addAttribute("longitude", loc.getLongitude()+"");
		
		System.out.println("Loc: "+loc);
		
		if (cmd.hasOption("class")) {
			// site class specified
			String siteClassName = cmd.getOptionValue("class").toUpperCase();
			Double match = null;
			for (String key : vs30Map.keySet()) {
				if (key.toUpperCase().equals(siteClassName)) {
					match = vs30Map.get(key);
					break;
				}
			}
			if (siteClassName.equals("AORB"))
				siteClassName = "AorB"; // for bin file matching
			Preconditions.checkState(match != null, "Unknown site class: %s", siteClassName);
			Preconditions.checkState(!cmd.hasOption("vs30"), "Can't specify site class and Vs30!");
			siteClassNames = Lists.newArrayList();
			siteClassNames.add(siteClassName);
			userVs30 = match;
		} else if (cmd.hasOption("vs30")) {
			double vs30 = Double.parseDouble(cmd.getOptionValue("vs30"));
			double minDiff = Double.POSITIVE_INFINITY;
			String closestClass = null;
			double secondClosest = Double.POSITIVE_INFINITY;
			String secondClosestClass = null;
			for (String category : vs30Map.keySet()) {
				Double catVs30 = vs30Map.get(category);
				if (catVs30 == null)
					continue;
				double diff = Math.abs(vs30 - catVs30);
				if (diff < minDiff) {
					secondClosest = minDiff;
					secondClosestClass = closestClass;
					minDiff = diff;
					closestClass = category;
				} else if (diff < secondClosest) {
					secondClosest = diff;
					secondClosestClass = category;
				}
			}
			System.out.println("Closest GMPE site class "+closestClass+" for user Vs30 of "+vs30+" (diff="+minDiff+")");
			siteClassNames = Lists.newArrayList();
			siteClassNames.add(closestClass);
			if (vs30 > minCalcVs30 && vs30 < maxCalcVs30 && (float)minDiff > 0f) {
				// only add second point for interp if within range and not an exact match
				System.out.println("\t2nd closest GMPE site class for interp "+secondClosestClass+" (diff="+secondClosest+")");
				siteClassNames.add(secondClosestClass);
			}
			userVs30 = vs30;
		} else {
			// use Wills map
			try {
				userVs30 = new WillsMap2006().getValue(loc);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
		metadataEl.addAttribute("vs30", userVs30+"");
		if (siteClassNames != null) {
			if (siteClassNames.size() == 1) {
				metadataEl.addAttribute("siteClass", siteClassNames.get(0));
			} else {
				Preconditions.checkState(siteClassNames.size() == 2, "Unexpected number of site classes");
				metadataEl.addAttribute("siteClass", siteClassNames.get(0));
				metadataEl.addAttribute("secondInterpSiteClass", siteClassNames.get(1));
			}
		}
		
		resultsEl = xmlRoot.addElement("Results");
	}
	
	public void calcCyberShake() {
		if (csDataFile != null) {
			System.out.println("Calculating with precomputed CS data file");
			
			try {
				GriddedSpectrumInterpolator interp = getInterpolator(csDataFile, csDataSpacing);
				csMCER = interp.getInterpolated(loc);
				csMCER.setName("CyberShake MCEr");
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		} else {
			CyberShakeSiteRun site = csRun;
			
			System.out.println("Calculating CyberShake Values");
			List<DeterministicResult> csDeterms = Lists.newArrayList();
			csDetSpectrum = new ArbitrarilyDiscretizedFunc();
			csDetSpectrum.setName("CyberShake Deterministic");
			csProbSpectrum = new ArbitrarilyDiscretizedFunc();
			csProbSpectrum.setName("CyberShake Probabilistic");
			
			for (double period : periods) {
				try {
					DeterministicResult csDet = csDetCalc.calc(site, period);
					Preconditions.checkNotNull(csDet); // will kick down to catch and skip this period if null
					csDeterms.add(csDet);
					DiscretizedFunc curve = csProbCalc.calcHazardCurves(site, Lists.newArrayList(period)).get(period);
					double csProb = CurveBasedMCErProbabilisitCalc.calcRTGM(curve);
//					double csProb = csProbCalc.calc(site, period);
					csProbSpectrum.set(period, csProb);
				} catch (RuntimeException e) {
					if (e.getMessage() != null && e.getMessage().startsWith("No CyberShake IM match")
							|| e instanceof NullPointerException) {
//						e.printStackTrace();
//						System.err.flush();
						System.out.println("Skipping period "+period+", no matching CyberShake IM");
//						System.out.flush();
						csDeterms.add(null);
						continue;
					}
					throw e;
				}
			}
			Preconditions.checkState(csDeterms.size() == periods.length);
			for (int i=0; i<periods.length; i++)
				if (csDeterms.get(i) != null)
					csDetSpectrum.set(periods[i], csDeterms.get(i).getVal());
			
			DiscretizedFunc asceDeterm = ASCEDetLowerLimitCalc.calc(csProbSpectrum, userVs30, site.getLocation());
			
			csMCER = MCERDataProductsCalc.calcMCER(csDetSpectrum, csProbSpectrum, asceDeterm);
			csMCER.setName("CyberShake MCEr");
			
			csDetSpectrum.toXMLMetadata(csSiteEl, "deterministic");
			csProbSpectrum.toXMLMetadata(csSiteEl, "probabilistic");
			asceDeterm.toXMLMetadata(csSiteEl, "detLowerLimit");
			csMCER.toXMLMetadata(csSiteEl, "mcer");
		}
		
		csMCER.toXMLMetadata(resultsEl, "CyberShakeMCER");
	}
	
	public void calcGMPE() throws Exception {
		System.out.println("Calculating GMPE");
		File gmpeDir = new File(this.gmpeDir, gmpeERF);
		Preconditions.checkState(gmpeDir.exists(), "GMPE/ERF dir doesn't exist: %s", gmpeDir.getAbsolutePath());
		
		List<String> dataFileNames = Lists.newArrayList();
		List<Double> vs30Vals = Lists.newArrayList();
		List<DiscretizedFunc> spectrum = Lists.newArrayList();
		if (siteClassNames == null) {
			dataFileNames.add("Wills.bin");
			vs30Vals.add(userVs30);
		} else {
			for (String siteClassName : siteClassNames) {
				dataFileNames.add("class"+siteClassName+".bin");
				vs30Vals.add(vs30Map.get(siteClassName));
			}
		}
		
		for (String dataFileName : dataFileNames) {
			File dataFile = new File(gmpeDir, dataFileName);
			System.out.println("Loading GMPE data file: "+dataFile.getAbsolutePath());
			Preconditions.checkState(dataFile.exists(), "Data file doesn't exist: %s", dataFile.getAbsolutePath());
			
			gmpeSiteEl = xmlRoot.addElement("GMPE_Run");
			gmpeSiteEl.addAttribute("dataFile", dataFile.getAbsolutePath());
			
			GriddedSpectrumInterpolator interp = getInterpolator(dataFile, gmpeSpacing);
			
			Location closestLoc = interp.getClosestGridLoc(loc);
			double minDist = LocationUtils.horzDistanceFast(closestLoc, loc);
			
			Preconditions.checkState(minDist <= GMPE_MAX_DIST,
					"Closest location in GMPE data file too far from user location: %s > %s", minDist, GMPE_MAX_DIST);
			
			gmpeSiteEl.addAttribute("distFromUserLoc", minDist+"");
			gmpeSiteEl.addAttribute("latitude", closestLoc.getLatitude()+"");
			gmpeSiteEl.addAttribute("longitude", closestLoc.getLongitude()+"");
			
			spectrum.add(interp.getInterpolated(loc));
		}
		
		if (spectrum.size() == 1) {
			gmpeMCER = spectrum.get(0);
		} else {
			// need to interpolate
			Preconditions.checkState(spectrum.size() == 2, "need exactly 2 for interpolation");
			Element interpEl = xmlRoot.addElement("GMPE_Interpolation");
			
			double x1 = vs30Vals.get(0);
			DiscretizedFunc s1 = spectrum.get(0);
			double x2 = vs30Vals.get(1);
			DiscretizedFunc s2 = spectrum.get(1);
			Preconditions.checkState(s1.size() == s2.size(), "Spectrum sizes inconsistent");
			
			if (x2 < x1) {
				// reverse
				double tx = x1;
				DiscretizedFunc ts = s1;
				x1 = x2;
				s1 = s2;
				x2 = tx;
				s2 = ts;
			}
			Preconditions.checkState(x2 > x1);
			Preconditions.checkState(x2 >= userVs30 && userVs30 >= x1,
					"User supplied Vs30 (%s) not in range [%s %s]", userVs30, x1, x2); 
			interpEl.addAttribute("vs30lower", x1+"");
			interpEl.addAttribute("vs30upper", x2+"");
			interpEl.addAttribute("userVs30", userVs30+"");
			s1.toXMLMetadata(interpEl, "LowerSpectrum");
			s2.toXMLMetadata(interpEl, "UpperSpectrum");
			
			double[] xs = {x1, x2};
			gmpeMCER = new ArbitrarilyDiscretizedFunc();
			for (int i=0; i<s1.size(); i++) {
				double x = s1.getX(i);
				Preconditions.checkState((float)x == (float)s2.getX(i), "Spectrum x values inconsistent");
				double[] ys = {s1.getY(i), s2.getY(i)};
				// log-log interpolation as requested by Christine, 1/17/17
//				double y = Interpolate.findY(xs, ys, userVs30);
//				double y = Interpolate.findLogY(xs, ys, userVs30);
				double y = Interpolate.findLogLogY(xs, ys, userVs30);
//				System.out.println("x="+userVs30+", x1="+x1+", x2="+x2);
//				System.out.println("y="+y+", y1="+ys[0]+", y2="+ys[1]);
				Preconditions.checkState(y >= ys[0] && y <= ys[1] || y >= ys[1] && y <= ys[0],
						"Bad interpolation, %s outside of range [%s %s]", y, ys[0], ys[1]);
				gmpeMCER.set(x, y);
			}
		}
		gmpeMCER.setName("GMPE MCEr");
		
		gmpeMCER.toXMLMetadata(resultsEl, "GMPE_MCER");
	}
	
	private GriddedSpectrumInterpolator getInterpolator(File dataFile, double spacing) throws Exception {
		System.out.println("Loading spectrum from "+dataFile.getAbsolutePath());
		Stopwatch watch = Stopwatch.createStarted();
		BinaryHazardCurveReader reader = new BinaryHazardCurveReader(dataFile.getAbsolutePath());
		Map<Location, ArbitrarilyDiscretizedFunc> map = reader.getCurveMap();
		watch.stop();
		System.out.println("Took "+watch.elapsed(TimeUnit.SECONDS)+" s to load spectrum");
		
		// filter for just surrounding points for faster gridding/interp
		int numBuffer = 50;
		double lat = loc.getLatitude();
		double lon = loc.getLongitude();
		double minLat = lat - spacing*numBuffer;
		double maxLat = lat + spacing*numBuffer;
		double minLon = lon - spacing*numBuffer;
		double maxLon = lon + spacing*numBuffer;
		Map<Location, ArbitrarilyDiscretizedFunc> filteredMap = Maps.newHashMap();
		for (Location loc : map.keySet()) {
			lat = loc.getLatitude();
			lon = loc.getLongitude();
			if (lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon)
				filteredMap.put(loc, map.get(loc));
		}
		System.out.println("Filtered input grid from "+map.size()+" to "+filteredMap.size()+" data points surrounding user loc");
		Preconditions.checkState(!filteredMap.isEmpty(), "No locs within region buffer");
		
		watch.reset();
		watch.start();
		GriddedSpectrumInterpolator interp = new GriddedSpectrumInterpolator(filteredMap, spacing);
		watch.stop();
		System.out.println("Took "+watch.elapsed(TimeUnit.SECONDS)+" s create interpolator/grid");
		
		return interp;
	}
	
	public void plot() throws IOException {
		plot(true);
		plot(false);
	}
	public void plot(boolean psv) throws IOException {
		boolean xLog = psv;
		boolean yLog = psv;
		Range xRange = new Range(1e-2, 10d);
		Range yRange;
		
		DiscretizedFunc gmpeMCER = this.gmpeMCER;
		DiscretizedFunc csMCER = this.csMCER;
		
		String prefix, yAxisLabel;
		if (psv) {
			prefix = "mcer_psv";
			yRange = new Range(2e0, 2e3);
			gmpeMCER = MCErCalcUtils.saToPsuedoVel(gmpeMCER);
			csMCER = MCErCalcUtils.saToPsuedoVel(csMCER);
			yAxisLabel = "PSV (cm/s)";
		} else {
			prefix = "mcer_sa";
//			yRange = new Range(1e-2, 1e1);
			yRange = null;
			yAxisLabel = "Sa (g)";
		}

		List<DiscretizedFunc> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		DiscretizedFunc finalMCEr = MCERDataProductsCalc.calcFinalMCER(csMCER, gmpeMCER);
		finalMCEr.setName("Final MCEr");
		
		if (!psv)
			finalMCEr.toXMLMetadata(resultsEl, "FinalMCER");

		funcs.add(gmpeMCER);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));

		funcs.add(csMCER);
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 2f, Color.RED));
		
		funcs.add(finalMCEr);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.BLACK));

		PlotSpec spec = new PlotSpec(funcs, chars, "CyberShake MCEr", "Period (s)", yAxisLabel);
		spec.setLegendVisible(true);

		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		//			gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);

		gp.drawGraphPanel(spec, xLog, yLog, xRange, yRange);
		gp.getChartPanel().setSize(1000, 800);
		gp.setVisible(true);

		gp.validate();
		gp.repaint();

		File file = new File(outputDir, prefix);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
		
		// now write CSV
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList("Period", "Final MCEr", "CyberShake MCEr", "GMPE MCEr");
		csv.addLine(header);
		
		for (double period : periods) {
			List<String> line = Lists.newArrayList((float)period+"");
			line.add(MCERDataProductsCalc.getValIfPresent(finalMCEr, period));
			line.add(MCERDataProductsCalc.getValIfPresent(csMCER, period));
			line.add(MCERDataProductsCalc.getValIfPresent(gmpeMCER, period));
			
			csv.addLine(line);
		}
		
		csv.writeToFile(new File(outputDir, prefix+".csv"));
	}
	
	public void writeMetadata() throws IOException {
		metadataEl.addAttribute("endTimeMillis", System.currentTimeMillis()+"");
		XMLUtils.writeDocumentToFile(new File(outputDir, "metadata.xml"), xmlMetadataDoc);
	}
	
	/**
	 * Fetches all runs which have a hazard curve completed and inserted into the database for the given IM, with the given dataset ID
	 * @param datasetID
	 * @param imTypeID IM type of interest
	 * @return
	 */
	private List<CyberShakeSiteRun> getCompletedRunsForDataset(int datasetID, int imTypeID) {
		String sql = "SELECT DISTINCT R.*, S.* FROM CyberShake_Runs R JOIN Hazard_Curves C JOIN CyberShake_Sites S"
				+ " ON R.Run_ID=C.Run_ID AND R.Site_ID=S.CS_Site_ID"
				+ " WHERE C.Hazard_Dataset_ID="+datasetID;
		if (imTypeID >= 0)
			sql += " AND C.IM_Type_ID="+imTypeID;
		
		ArrayList<CyberShakeSiteRun> runs = new ArrayList<CyberShakeSiteRun>();
		
		try {
			ResultSet rs = db.selectData(sql);
			boolean valid = rs.first();
			
			while (valid) {
				CybershakeRun run = CybershakeRun.fromResultSet(rs);
				CybershakeSite site = CybershakeSite.fromResultSet(rs);
				
				if (site.type_id != CybershakeSite.TYPE_TEST_SITE)
					runs.add(new CyberShakeSiteRun(site, run));
				
				valid = rs.next();
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		System.out.println("Found "+runs.size()+" completed sites for datasetID="+datasetID+", imTypeID="+imTypeID);
		return runs;
	}
	
	private static Options createOptions() {
		Options ops = new Options();
		
		Option run = new Option("r", "run-id", true, "CyberShake run ID, if a specific site is wanted");
		run.setRequired(false);
		ops.addOption(run);
		
		Option site = new Option("s", "site-id", true, "CyberShake site ID, if a specific site is wanted");
		site.setRequired(false);
		ops.addOption(site);
		
		Option siteName = new Option("s", "site-name", true, "CyberShake site short name, if a specific site is wanted");
		siteName.setRequired(false);
		ops.addOption(siteName);
		
		Option datasetID = new Option("d", "dataset-id", true, "CyberShake dataset ID");
		datasetID.setRequired(false);
		ops.addOption(datasetID);
		
		Option lat = new Option("lat", "latitude", true, "Site latitude. Must be specified if a run or site ID is not");
		lat.setRequired(false);
		ops.addOption(lat);
		
		Option lon = new Option("lon", "longitude", true, "Site longitude. Must be specified if a run or site ID is not");
		lon.setRequired(false);
		ops.addOption(lon);
		
		Option siteClass = new Option("c", "class", true,
				"Site class (e.g. CD). If neither this or vs30 is specified, Wills value will be used");
		siteClass.setRequired(false);
		ops.addOption(siteClass);
		
		Option vs30 = new Option("v", "vs30", true,
				"Vs30 (m/s). If neither this or site class is specified, Wills value will be used");
		vs30.setRequired(false);
		ops.addOption(vs30);
		
		Option gmpeDir = new Option("g", "gmpe-dir", true, "Directory containing GMPE precomputed data files");
		gmpeDir.setRequired(true);
		ops.addOption(gmpeDir);
		
		Option gmpeSpacing = new Option("gs", "gmpe-spacing", true, "Grid spacing of GMPE precomputed data files");
		gmpeSpacing.setRequired(true);
		ops.addOption(gmpeSpacing);
		
		Option csDir = new Option("csdir", "cs-dir", true, "Directory containing CS cache files");
		csDir.setRequired(false);
		ops.addOption(csDir);
		
		Option csData = new Option("csdata", "cs-data-file", true, "CS precomputed data file");
		csData.setRequired(false);
		ops.addOption(csData);
		
		Option csSpacing = new Option("csspacing", "cs-spacing", true, "Grid spacing of CS precomputed data file");
		csSpacing.setRequired(false);
		ops.addOption(csSpacing);
		
		Option gmpeERF = new Option("g", "gmpe-erf", true, "GMPE ERF ('UCERF2' or 'UCERF3')");
		gmpeERF.setRequired(true);
		ops.addOption(gmpeERF);
		
		Option outputDir = new Option("o", "output-dir", true, "Output directory, will be created if it doesn't exist");
		outputDir.setRequired(true);
		ops.addOption(outputDir);
		
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
		if (args.length == 1 && args[0].equals("--hardcoded")) {
			// hardcoded
			String argStr = "--latitude 34.026414 --longitude -118.300136";
//			String argStr = "--run-id 3870"; // doesn't require dataset ID if run ID
//			String argStr = "--site-name LADT";
//			String argStr = "--site-id 20";
			argStr += " --dataset-id 57";
			argStr += " --gmpe-dir /home/kevin/CyberShake/MCER/gmpe_cache_gen/mcer_binary_results";
			argStr += " --gmpe-spacing 0.02";
//			argStr += " --cs-dir /home/kevin/CyberShake/MCER/.amps_cache";
//			argStr += " --cs-data-file /home/kevin/CyberShake/MCER/maps/study_15_4_rotd100/interp_tests/mcer_spectrum_0.001.bin";
//			argStr += " --cs-spacing 0.001";
//			argStr += " --cs-data-file /home/kevin/CyberShake/MCER/maps/study_15_4_rotd100/interp_tests/mcer_spectrum_0.005.bin";
//			argStr += " --cs-spacing 0.005";
			argStr += " --cs-data-file /home/kevin/CyberShake/MCER/maps/study_15_4_rotd100/interp_tests/mcer_spectrum_0.002.bin";
			argStr += " --cs-spacing 0.002";
			argStr += " --output-dir /tmp/ugms_web_tool";
			argStr += " --vs30 455";
//			argStr += " --class AorB";
			argStr += " --gmpe-erf UCERF3";
			
			args = Splitter.on(" ").splitToList(argStr).toArray(new String[0]);
		}
		
		try {
			Options options = createOptions();
			
			String appName = ClassUtils.getClassNameWithoutPackage(UGMS_WebToolCalc.class);
			
			CommandLineParser parser = new GnuParser();
			
			if (args.length == 0) {
				printUsage(options, appName);
			}
			
			try {
				CommandLine cmd = parser.parse( options, args);
				
				if (cmd.hasOption("help") || cmd.hasOption("?")) {
					printHelp(options, appName);
				}
				
				UGMS_WebToolCalc calc = new UGMS_WebToolCalc(cmd);
				
				calc.calcCyberShake();
				calc.calcGMPE();
				
				calc.plot();
				calc.writeMetadata();
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
			if (db != null)
				db.destroy();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			if (db != null)
				db.destroy();
			System.exit(1);
		}
	}

}
