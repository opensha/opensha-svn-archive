package scratch.kevin.ucerf3.etas;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.sha.calc.hazardMap.components.BinaryCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CurveMetadata;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mpi.MPI;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;
import scratch.UCERF3.erf.ETAS.ETAS_CatalogIO;
import scratch.UCERF3.erf.ETAS.ETAS_EqkRupture;
import scratch.UCERF3.utils.FaultSystemIO;

public class MPJ_ETAS_HazardMapCalc extends MPJTaskCalculator {
	
	private List<List<ETAS_EqkRupture>> catalogs;
	
	// for precomputed shakemaps
	private RandomAccessFile raFile;
	private long[] filePositions;
	private int[] fileLengths;
	// else for on the fly shakemaps
	private FaultSystemSolutionERF faultERF;
	private ProbEqkSource[] sourcesForFSSRuptures;
	private double distCutoff = 200;
	
	private GriddedRegion region;
	
	private String imt;
	private double period;
	
	private ETAS_HazardMapCalc mapCalc;
	
	private File gmpeFile;
	private Deque<ScalarIMR> gmpeDeque;
	
	private List<Site> sites;
	
	private boolean calcGridded;
	private boolean calcFault;
	
	private boolean griddedConditional = true; // TODO
	private ETAS_CatalogGridSourceProvider griddedSources;
	
	private ExecutorService executor;
	
	private String archiverNameFault = "results_fault";
	private String archiverNameGridded = "results_gridded";
	private BinaryCurveArchiver archiver;
	private DiscretizedFunc xVals;
	
	private boolean printEach;

	public MPJ_ETAS_HazardMapCalc(CommandLine cmd) throws IOException, DocumentException {
		super(cmd);
		
		File catalogsFile = new File(cmd.getOptionValue("catalogs"));
		catalogs = ETAS_CatalogIO.loadCatalogsBinary(catalogsFile);
		
		if (cmd.hasOption("fault-data-file")) {
			// precalc mode
			File faultDataFile = new File(cmd.getOptionValue("fault-data-file"));
			Preconditions.checkState(faultDataFile.exists());
			raFile = new RandomAccessFile(faultDataFile, "r");
		} else {
			Preconditions.checkArgument(cmd.hasOption("solution-file"),
					"Must supply fault system solution file if no fault data precalc file");
			File solFile = new File(cmd.getOptionValue("solution-file"));
			Preconditions.checkState(solFile.exists());
			FaultSystemSolution sol = FaultSystemIO.loadSol(solFile);
			faultERF = new FaultSystemSolutionERF(sol);
			faultERF.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
			faultERF.updateForecast();
			// organize by FSS index
			sourcesForFSSRuptures = new ProbEqkSource[sol.getRupSet().getNumRuptures()];
			for (int sourceID=0; sourceID<faultERF.getNumFaultSystemSources(); sourceID++) {
				int fssIndex = faultERF.getFltSysRupIndexForSource(sourceID);
				sourcesForFSSRuptures[fssIndex] = faultERF.getSource(sourceID);
			}
			if (rank == 0)
				debug("Created "+faultERF.getNumFaultSystemSources()+"/"+sourcesForFSSRuptures.length+" sources");
		}
		
		double spacing = Double.parseDouble(cmd.getOptionValue("spacing"));
		region = new CaliforniaRegions.RELM_TESTING_GRIDDED(spacing);
		
		calcGridded = !cmd.hasOption("no-gridded");
		calcFault = !cmd.hasOption("no-fault");
		
		Preconditions.checkArgument(calcGridded || calcFault);
		
		imt = cmd.getOptionValue("imt").toUpperCase();
		if (imt.equals(SA_Param.NAME)) {
			Preconditions.checkArgument(cmd.hasOption("period"), "Must supply period if Sa");
			period = Double.parseDouble(cmd.getOptionValue("period"));
			String imtStr;
			if (period == Math.floor(period))
				imtStr = "sa_"+(int)period+"s";
			else
				imtStr = "sa_"+(float)period+"s";
			archiverNameFault = "results_"+imtStr+"_fault";
			archiverNameGridded = "results_"+imtStr+"_gridded";
		} else {
			archiverNameFault = "results_"+imt.toLowerCase()+"_fault";
			archiverNameGridded = "results_"+imt.toLowerCase()+"_gridded";
		}
		
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		if (rank == 0)
			Preconditions.checkState(outputDir.exists() && outputDir.isDirectory() || outputDir.mkdir(),
				"Output directory doesn't exist or couldn't be created: %s", outputDir.getAbsoluteFile());
		
		xVals = new IMT_Info().getDefaultHazardCurve(imt);
		// don't give it the fault file, we're reading externally
		mapCalc = new ETAS_HazardMapCalc(catalogs, null, region, xVals);
		
		gmpeFile = new File(cmd.getOptionValue("gmpe-file"));
		Preconditions.checkState(gmpeFile.exists(), "GMPE file doesn't exist: %s", gmpeFile.getAbsolutePath());
		
		debug("Loading sites");
		File sitesFile = new File(cmd.getOptionValue("sites-file"));
		Document siteDoc;
		try {
			siteDoc = XMLUtils.loadDocument(sitesFile);
		} catch (Exception e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
		Element sitesRoot = siteDoc.getRootElement();
		ArrayList<Parameter<?>> paramsToAdd = new ArrayList<Parameter<?>>();
		ScalarIMR gmpe = checkOutGMPE();
		for (Parameter<?> param : gmpe.getSiteParams())
			paramsToAdd.add(param);
		checkInGMPE(gmpe);
		sites = Site.loadSitesFromXML(sitesRoot.element(Site.XML_METADATA_LIST_NAME), paramsToAdd);
		Preconditions.checkState(sites.size() == region.getNodeCount(), "Supplied sites file is wrong size");
		
		double griddedSpacing = Double.parseDouble(cmd.getOptionValue("gridded-spacing"));
		
		if (calcGridded)
			griddedSources = new ETAS_CatalogGridSourceProvider(catalogs, griddedSpacing, griddedConditional);
		
		executor = mapCalc.createExecutor(getNumThreads());
		
		if (calcFault && raFile != null)
			loadFilePositions();
		
		Map<String, DiscretizedFunc> xValsMap = Maps.newHashMap();
		if (calcFault)
			xValsMap.put(archiverNameFault, xVals);
		if (calcGridded)
			xValsMap.put(archiverNameGridded, xVals);
		
		archiver = new BinaryCurveArchiver(outputDir, getNumTasks(), xValsMap);
		if (rank == 0)
			archiver.initialize();
		
		printEach = getNumTasks() < 50000;
	}
	
	private void loadFilePositions() throws IOException {
		filePositions = new long[getNumTasks()];
		fileLengths = new int[getNumTasks()];
		if (rank == 0) {
			debug("Loading file positions");
			
			byte[] buf = new byte[4];
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf));
			
			raFile.seek(0l);
			raFile.readFully(buf);
			long pos = 4;
			int count = in.readInt();
			Preconditions.checkState(count == getNumTasks(), "Bad count in file! Expected %s, got %s", getNumTasks(), count);
			
			for (int index=0; index<filePositions.length; index++) {
				filePositions[index] = pos;
				raFile.readFully(buf);
				pos += 4;
				in.reset();
				int checkIndex = in.readInt();
				Preconditions.checkState(index == checkIndex, "Bad index. Expected %s, got %s", index, checkIndex);
				// skip 2 double vals for lat/lon
				pos += 16;
				raFile.seek(pos);
				// read in number of ruptures
				raFile.readFully(buf);
				pos += 4;
				in.reset();
				int numRups = in.readInt();
				Preconditions.checkState(numRups >= 0);
				// now skip ahead to next index
				// 20 bytes per rupture: index (int=4), mean (double=8), stdDev (double=8)
				pos = pos + numRups*20l;
				fileLengths[index] = (int)(pos - filePositions[index]);
				raFile.seek(pos);
			}
			
			debug("Distributing file positions");
		}
		
		MPI.COMM_WORLD.Bcast(filePositions, 0, filePositions.length, MPI.LONG, 0);
		if (rank == 0)
			debug("Distributing file lengths");
		MPI.COMM_WORLD.Bcast(fileLengths, 0, fileLengths.length, MPI.INT, 0);
	}

	@Override
	protected int getNumTasks() {
		return region.getNodeCount();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		List<Future<Integer>> futures = Lists.newArrayList();
		
		for (int index : batch)
			futures.add(executor.submit(new CalcRunnable(index), index));
		
		for (Future<Integer> future : futures)
			future.get();
	}
	
	private class CalcRunnable implements Runnable {
		
		private int index;
		
		public CalcRunnable(int index) {
			this.index = index;
		}

		@Override
		public void run() {
			try {
				if (calcFault)
					calcFault(index);
				if (calcGridded)
					calcGridded(index);
				if (printEach) debug("done with "+index);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		
	}
	
	private void calcFault(int index) throws IOException {
		CurveMetadata meta = new CurveMetadata(sites.get(index), index, null, archiverNameFault);
		if (archiver.isCurveCalculated(meta, xVals)) {
			debug("fault "+index+" already done, skipping");
			return;
		}
		
		DiscretizedFunc curve;
		if (raFile == null) {
			// calculate it
			if (printEach) debug("calculating fault "+index);
			Map<Integer, DiscretizedFunc> rupExceeds = calcFaultIMs(sites.get(index));
			
			curve = mapCalc.calcFaultHazardCurveFromExceed(rupExceeds);
		} else {
			// load it
			long pos = filePositions[index];
			int len = fileLengths[index];
			if (printEach) debug("calculating fault "+index+", pos="+pos+", len="+len);
			
			DataInputStream in;
			synchronized (raFile) {
				byte[] buf = new byte[len];
				in = new DataInputStream(new ByteArrayInputStream(buf));
				
				raFile.seek(pos);
				raFile.readFully(buf);
			}
			
			Map<Integer, double[]> rupVals = mapCalc.loadSiteFromInputStream(in, index);
			
			curve = mapCalc.calcFaultHazardCurve(rupVals);
		}
		
		archiver.archiveCurve(curve, meta);
	}
	
	private Map<Integer, DiscretizedFunc> calcFaultIMs(Site site) {
		// used if no precomputed data file
		Map<Integer, DiscretizedFunc> rupVals = Maps.newHashMap();
		ScalarIMR gmpe = checkOutGMPE();
		for (Integer fssIndex : mapCalc.getFaultIndexesTriggered()) {
			ProbEqkSource source = sourcesForFSSRuptures[fssIndex];
			if (source == null)
				continue;
			Preconditions.checkState(source.getNumRuptures() == 1, "Must be a single rupture source");
			ProbEqkRupture rup = source.getRupture(0);
			
			double minDist = source.getMinDistance(site);
			if (minDist > distCutoff)
				continue;
			
			gmpe.setSite(site);
			gmpe.setEqkRupture(rup);
			
			rupVals.put(fssIndex, gmpe.getExceedProbabilities(mapCalc.getCalcXVals().deepClone()));
		}
		checkInGMPE(gmpe);
		return rupVals;
	}
	
	private synchronized ScalarIMR checkOutGMPE() {
		if (gmpeDeque == null)
			gmpeDeque = new ArrayDeque<ScalarIMR>();
		if (gmpeDeque.isEmpty()) {
			// build a new one
			ScalarIMR gmpe;
			try {
				gmpe = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(gmpeFile.getAbsolutePath());
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			gmpe.setParamDefaults();
			gmpe.setIntensityMeasure(imt);
			if (imt.equals(SA_Param.NAME))
				SA_Param.setPeriodInSA_Param(gmpe.getIntensityMeasure(), period);
			return gmpe;
		}
		return gmpeDeque.pop();
	}
	
	private synchronized void checkInGMPE(ScalarIMR gmpe) {
		gmpeDeque.push(gmpe);
	}
	
	private void calcGridded(int index) throws IOException {
		CurveMetadata meta = new CurveMetadata(sites.get(index), index, null, archiverNameGridded);
		if (archiver.isCurveCalculated(meta, xVals)) {
			debug("gridded "+index+" already done, skipping");
			return;
		}
		if (printEach) debug("calculating gridded "+index);
		
		ScalarIMR gmpe = checkOutGMPE();
		
		DiscretizedFunc curve = mapCalc.calcGriddedHazardCurve(gmpe, sites.get(index), index, griddedSources);
		archiver.archiveCurve(curve, meta);
		
		checkInGMPE(gmpe);
	}
	
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option catalogs = new Option("c", "catalogs", true, "ETAS Catalogs Binary File");
		catalogs.setRequired(true);
		ops.addOption(catalogs);
		
		Option faultFile = new Option("f", "fault-data-file", true,
				"Fault shakemap precalc data file. Must supply this, or --solution-file.");
		faultFile.setRequired(false);
		ops.addOption(faultFile);
		
		Option solFile = new Option("sol", "solution-file", true,
				"FaultSystemSolution file for calculating fault IMs on the fly. Must supply this, or --fault-data-file.");
		solFile.setRequired(false);
		ops.addOption(solFile);
		
		Option spacing = new Option("s", "spacing", true, "Grid spacing in degrees");
		spacing.setRequired(true);
		ops.addOption(spacing);
		
		Option imt = new Option("i", "imt", true, "IMT. One of 'SA','PGA','PGV'");
		imt.setRequired(true);
		ops.addOption(imt);
		
		Option period = new Option("p", "period", true, "Period, required if IMT is SA");
		period.setRequired(false);
		ops.addOption(period);
		
		Option outputDir = new Option("o", "output-dir", true, "Output directory");
		outputDir.setRequired(true);
		ops.addOption(outputDir);
		
		Option gmpeFile = new Option("g", "gmpe-file", true, "GMPE XML file");
		gmpeFile.setRequired(true);
		ops.addOption(gmpeFile);
		
		Option siteFile = new Option("sites", "sites-file", true, "Sites XML file");
		siteFile.setRequired(true);
		ops.addOption(siteFile);
		
		Option griddedSpacing = new Option("gs", "gridded-spacing", true, "Spacing for gridded ruptures in degrees");
		griddedSpacing.setRequired(true);
		ops.addOption(griddedSpacing);
		
		Option noGridded = new Option("ng", "no-gridded", false, "Flag to disable gridded calculation");
		noGridded.setRequired(false);
		ops.addOption(noGridded);
		
		Option noFault = new Option("nf", "no-fault", false, "Flag to disable fault calculation");
		noFault.setRequired(false);
		ops.addOption(noFault);
		
		return ops;
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		executor.shutdown();
		archiver.close();
		if (raFile != null)
			raFile.close();
	}
	
	public static void main(String args[]) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJ_ETAS_HazardMapCalc.class);
			
			MPJ_ETAS_HazardMapCalc driver = new MPJ_ETAS_HazardMapCalc(cmd);
			
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
