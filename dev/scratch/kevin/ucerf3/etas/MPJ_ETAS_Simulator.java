package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.param.ApplyGardnerKnopoffAftershockFilterParam;
import org.opensha.sha.earthquake.param.BPTAveragingTypeOptions;
import org.opensha.sha.earthquake.param.BPTAveragingTypeParam;
import org.opensha.sha.earthquake.param.BackgroundRupParam;
import org.opensha.sha.earthquake.param.BackgroundRupType;
import org.opensha.sha.earthquake.param.HistoricOpenIntervalParam;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.ETAS.ETAS_Simulator;
import scratch.UCERF3.erf.ETAS.FaultSystemSolutionERF_ETAS;
import scratch.UCERF3.erf.utils.ProbabilityModelsCalc;
import scratch.UCERF3.griddedSeismicity.AbstractGridSourceProvider;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.LastEventData;
import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class MPJ_ETAS_Simulator extends MPJTaskCalculator {
	
	private int numSims;
	private FaultSystemSolution sol;
//	private FaultSystemSolutionERF_ETAS erf;
	private Map<Integer, List<LastEventData>> lastEventData;
	private File inputDir;
	private File outputDir;
	
	private double duration;
	private Long ot;
	
	private int fssScenarioRupID;
	
	private List<float[]> fractionSrcAtPointList;
	private List<int[]> srcAtPointList;
	
	private String simulationName;
	
	private boolean includeSpontEvents = true;
	private boolean includeIndirectTriggering = true;
	private boolean includeEqkRates = true;
	private double gridSeisDiscr = 0.1;
	
	private boolean timeIndep = false;
	
	private CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
	
	private List<ObsEqkRupture> obsEqkRuptureList;

	public MPJ_ETAS_Simulator(CommandLine cmd, File inputDir, File outputDir) throws IOException, DocumentException {
		super(cmd);
		
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		
		// nasty kludgy setup
		ETAS_Simulator.D = false;
		
		lastEventData = LastEventData.load();
		
		AbstractGridSourceProvider.SOURCE_MIN_MAG_CUTOFF = 2.55;
		
		numSims = Integer.parseInt(cmd.getOptionValue("num"));
		
		if (cmd.hasOption("duration"))
			this.duration = Double.parseDouble(cmd.getOptionValue("duration"));
		else
			this.duration = 1d;
		
		if (cmd.hasOption("indep"))
			timeIndep = true;
		
		File solFile = new File(cmd.getOptionValue("sol-file"));
		Preconditions.checkArgument(solFile.exists(), "Solution file doesn't exist: "+solFile.getAbsolutePath());
		sol = FaultSystemIO.loadSol(solFile);
		
		// if we have a triggered event
		ot = Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR); // occurs at 2014
		
		fssScenarioRupID = -1;
		
		obsEqkRuptureList = Lists.newArrayList();
		
		if (cmd.hasOption("trigger-rupture-id")) {
			// FSS rupture
			ObsEqkRupture mainshockRup = new ObsEqkRupture();
			mainshockRup.setOriginTime(ot);
			
			// Mojave M 7.05 rupture
//			int fssRupID=197792;
			fssScenarioRupID = Integer.parseInt(cmd.getOptionValue("trigger-rupture-id"));
//			int srcID = erf.getSrcIndexForFltSysRup(fssRupID);

//			ProbEqkRupture rupFromERF = erf.getSource(srcID).getRupture(0);
//			mainshockRup.setAveRake(rupFromERF.getAveRake());
//			mainshockRup.setMag(rupFromERF.getMag());
//			mainshockRup.setRuptureSurface(rupFromERF.getRuptureSurface());
			mainshockRup.setAveRake(sol.getRupSet().getAveRakeForRup(fssScenarioRupID));
			mainshockRup.setMag(sol.getRupSet().getMagForRup(fssScenarioRupID));
			mainshockRup.setRuptureSurface(sol.getRupSet().getSurfaceForRupupture(fssScenarioRupID, 1d, false));
//			debug("test Mainshock: "+erf.getSource(srcID).getName());
			
			// date of last event will be updated for this rupture in the calculateBatch method below
			
			simulationName = "FSS simulation. M="+mainshockRup.getMag()+", fss ID="+fssScenarioRupID;
			
			obsEqkRuptureList.add(mainshockRup);
		} else if (cmd.hasOption("trigger-loc")) {
			ObsEqkRupture mainshockRup = new ObsEqkRupture();
			mainshockRup.setOriginTime(ot);	

			// 3-29-14 M 5.1 La Habra Earthquake
//			Location ptSurf = new Location(33.932,-117.917,4.8);	//
//			double mag = 6.2;	// test bigger magnitude
			String locStr = cmd.getOptionValue("trigger-loc");
			String[] locSplit = locStr.split(",");
			Preconditions.checkState(locSplit.length == 3, "Location must be in format lat,lon,depth");
			Location ptSurf = new Location(Double.parseDouble(locSplit[0]),
					Double.parseDouble(locSplit[1]), Double.parseDouble(locSplit[2]));
			Preconditions.checkArgument(cmd.hasOption("trigger-mag"), "trigger magnitude not supplied");
			double mag = Double.parseDouble(cmd.getOptionValue("trigger-mag"));
			
			if (cmd.hasOption("trigger-rake"))
				mainshockRup.setAveRake(Double.parseDouble(cmd.getOptionValue("trigger-rake")));
			else
				mainshockRup.setAveRake(0.0);
			mainshockRup.setMag(mag);
			mainshockRup.setPointSurface(ptSurf);
			
			simulationName = "Pt Source. M="+mag+", "+ptSurf;
			
			obsEqkRuptureList.add(mainshockRup);
		} else {
			// only spontaneous
			
			// make sure no mag
			Preconditions.checkArgument(!cmd.hasOption("trigger-mag"), "trigger location not supplied");
			
			simulationName = "Spontaneous events";
		}
	}

	@Override
	protected int getNumTasks() {
		return numSims;
	}
	
	public String getMemoryDebug() {
		Runtime rt = Runtime.getRuntime();
		long totalMB = rt.totalMemory() / 1024 / 1024;
		long freeMB = rt.freeMemory() / 1024 / 1024;
		long usedMB = totalMB - freeMB;
		return "mem t/u/f: "+totalMB+"/"+usedMB+"/"+freeMB;
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		if (fractionSrcAtPointList == null) {
			File fractionSrcAtPointListFile = new File(inputDir, "fractionSrcAtPointList.bin");
			File srcAtPointListFile = new File(inputDir, "srcAtPointList.bin");
			Preconditions.checkState(fractionSrcAtPointListFile.exists(),
					"cache file not found: "+fractionSrcAtPointListFile.getAbsolutePath());
			Preconditions.checkState(srcAtPointListFile.exists(),
					"cache file not found: "+srcAtPointListFile.getAbsolutePath());
			debug("loading cache from "+fractionSrcAtPointListFile.getAbsolutePath()+" ("+getMemoryDebug()+")");
			fractionSrcAtPointList = MatrixIO.floatArraysListFromFile(fractionSrcAtPointListFile);
			debug("loading cache from "+srcAtPointListFile.getAbsolutePath()+" ("+getMemoryDebug()+")");
			srcAtPointList = MatrixIO.intArraysListFromFile(srcAtPointListFile);
			debug("done loading caches ("+getMemoryDebug()+")");
		}
		
		for (int index : batch) {
			System.gc();
			
			File outputDir = new File(this.outputDir, "results");
			if (!outputDir.exists())
				outputDir.mkdir();
			
			String runName = ""+index;
			int desiredLen = ((getNumTasks()-1)+"").length();
			while (runName.length() < desiredLen)
				runName = "0"+runName;
			runName = "sim_"+runName;
			File resultsDir = new File(outputDir, runName);
			
			if (isAlreadyDone(resultsDir)) {
				debug(index+" is already done: "+resultsDir.getName());
				continue;
			}
			debug("calculating "+index);
			
			// reset date of last event
			if (timeIndep) {
				for (FaultSectionPrefData sect : sol.getRupSet().getFaultSectionDataList())
					sect.setDateOfLastEvent(Long.MIN_VALUE);
			} else {
				LastEventData.populateSubSects(sol.getRupSet().getFaultSectionDataList(), lastEventData);
			}
			
			debug("Instantiationg ERF");
			FaultSystemSolutionERF_ETAS erf = new FaultSystemSolutionERF_ETAS(sol);
			// set parameters
			erf.getParameter(IncludeBackgroundParam.NAME).setValue(IncludeBackgroundOption.INCLUDE);
			erf.setParameter(BackgroundRupParam.NAME, BackgroundRupType.POINT);
			erf.setParameter(ApplyGardnerKnopoffAftershockFilterParam.NAME, false);
			erf.getParameter(ProbabilityModelParam.NAME).setValue(ProbabilityModelOptions.U3_BPT);
			erf.getParameter(MagDependentAperiodicityParam.NAME).setValue(MagDependentAperiodicityOptions.MID_VALUES);
			BPTAveragingTypeOptions aveType = BPTAveragingTypeOptions.AVE_RI_AVE_NORM_TIME_SINCE;
			erf.setParameter(BPTAveragingTypeParam.NAME, aveType);
			erf.setParameter(AleatoryMagAreaStdDevParam.NAME, 0.0);
			if (!timeIndep)
				erf.getParameter(HistoricOpenIntervalParam.NAME).setValue(2014d-1875d);	
			erf.getTimeSpan().setStartTimeInMillis(Math.round((2014.0-1970.0)*ProbabilityModelsCalc.MILLISEC_PER_YEAR)+1);
			erf.getTimeSpan().setDuration(duration);
			
			if (fssScenarioRupID >= 0) {
				// This sets the rupture as having occurred in the ERF (to apply elastic rebound)
				erf.setFltSystemSourceOccurranceTimeForFSSIndex(fssScenarioRupID, ot);
			}
			
			erf.updateForecast();
			debug("Done instantiating ERF");
			
			List<ObsEqkRupture> obsEqkRuptureList = Lists.newArrayList(this.obsEqkRuptureList);
			try {
				ETAS_Simulator.testETAS_Simulation(resultsDir, erf, griddedRegion, obsEqkRuptureList, includeSpontEvents,
						includeIndirectTriggering, includeEqkRates, gridSeisDiscr, simulationName, null, fractionSrcAtPointList, srcAtPointList);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
	}
	
	public static boolean isAlreadyDone(File resultsDir) throws IOException {
		File infoFile = new File(resultsDir, "infoString.txt");
		if (!infoFile.exists())
			return false;
		for (String line : Files.readLines(infoFile, Charset.defaultCharset())) {
			if (line.contains("Total num ruptures: "))
				return true;
		}
		return false;
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		// do nothing
	}
	
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option triggerRupID = new Option("r", "trigger-rupture-id", true, "Trigger FSS rupture ID");
		triggerRupID.setRequired(false);
		ops.addOption(triggerRupID);
		
		Option triggerLoc = new Option("l", "trigger-loc", true, "Trigger location in format lat,lon,depth");
		triggerLoc.setRequired(false);
		ops.addOption(triggerLoc);
		
		Option triggerMag = new Option("m", "trigger-mag", true, "Trigger magnitude for point source (used with --trigger-loc)");
		triggerMag.setRequired(false);
		ops.addOption(triggerMag);
		
		Option triggerRake = new Option("m", "trigger-rake", true, "Trigger rake for point source (used with --trigger-loc, default=0)");
		triggerRake.setRequired(false);
		ops.addOption(triggerRake);
		
		Option numSims = new Option("n", "num", true, "Number of simulations");
		numSims.setRequired(true);
		ops.addOption(numSims);
		
		Option solFile = new Option("s", "sol-file", true, "Solution File");
		solFile.setRequired(true);
		ops.addOption(solFile);
		
		Option duration = new Option("d", "duration", true, "Simulation duration (years), default=1yr");
		duration.setRequired(false);
		ops.addOption(duration);
		
		Option indep = new Option("i", "indep", false, "Time independent probabilities. Elastic rebound will "
				+ "still be applied for fault initiating event and any triggered events.");
		indep.setRequired(false);
		ops.addOption(indep);
		
		return ops;
	}
	
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJ_ETAS_Simulator.class);
			
			args = cmd.getArgs();
			
			if (args.length != 2) {
				System.err.println("USAGE: "+ClassUtils.getClassNameWithoutPackage(MPJ_ETAS_Simulator.class)
						+" [options] <input-dir> <output-dir>");
				abortAndExit(2);
			}
			
			File inputDir = new File(args[0]);
			Preconditions.checkArgument(inputDir.exists(), "input directory doesn't exist: "+inputDir.getAbsolutePath());
			File outputDir = new File(args[1]);
			Preconditions.checkArgument(outputDir.exists(), "output directory doesn't exist: "+outputDir.getAbsolutePath());
			
			MPJ_ETAS_Simulator driver = new MPJ_ETAS_Simulator(cmd, inputDir, outputDir);
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
