package org.opensha.sha.cybershake.calc.mcer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import mpi.MPI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.sha.calc.hazardMap.components.BinaryCurveArchiver;
import org.opensha.sha.calc.hazardMap.components.CurveMetadata;
import org.opensha.sha.calc.mcer.ASCEDetLowerLimitCalc;
import org.opensha.sha.calc.mcer.AbstractMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.AbstractMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CachedCurveBasedMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.CachedMCErDeterministicCalc;
import org.opensha.sha.calc.mcer.CachedMCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.GMPE_MCErDeterministicCalc;
import org.opensha.sha.calc.mcer.GMPE_MCErProbabilisticCalc;
import org.opensha.sha.calc.mcer.MCErCalcUtils;
import org.opensha.sha.cybershake.calc.RuptureProbabilityModifier;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeIM.CyberShakeComponent;
import org.opensha.sha.cybershake.gui.util.AttenRelSaver;
import org.opensha.sha.cybershake.gui.util.ERFSaver;
import org.opensha.sha.cybershake.plot.HazardCurvePlotter;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.Component;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MPJ_GMPE_MCErCacheGen extends MPJTaskCalculator {
	
	private List<Double> periods;
	private DiscretizedFunc periodsFunc;
	private List<CalcTask> calcTasks;
	
//	private AbstractMCErDeterministicCalc[] detCalcs;
//	private AbstractMCErProbabilisticCalc[] probCalcs;
	private CalcRunnable[] calcs;
	private String[] cachePrefixes;
	
	
	
	private BinaryCurveArchiver archiver;
	
	public MPJ_GMPE_MCErCacheGen(CommandLine cmd) throws InvocationTargetException, MalformedURLException, DocumentException {
		super(cmd);
		
//		int numThreads = getNumThreads();
		int numThreads = 1; // TODO make thread safe
		Preconditions.checkState(numThreads >= 1);
		
		List<List<AttenuationRelationship>> attenRelsList = Lists.newArrayList();
		for (int t=0; t<numThreads; t++) {
			List<AttenuationRelationship> attenRels = Lists.newArrayList();
			
			for (String attenRelFile : HazardCurvePlotter.commaSplit(cmd.getOptionValue("atten-rel-file"))) {
				AttenuationRelationship attenRel = AttenRelSaver.LOAD_ATTEN_REL_FROM_FILE(attenRelFile);
				attenRels.add(attenRel);
			}
			
			attenRelsList.add(attenRels);
		}
		ERF erf = ERFSaver.LOAD_ERF_FROM_FILE(cmd.getOptionValue("erf-file"));
		
		RuptureProbabilityModifier detProbMod = CyberShakeMCErDeterministicCalc.getProbMod(erf);
		ERF detERF = MCERDataProductsCalc.getGMPEDetERF(erf, detProbMod);
		
		erf.updateForecast();
		
		String periodStrs;
		if (cmd.hasOption("period"))
			periodStrs = cmd.getOptionValue("period");
		else
			periodStrs = MCERDataProductsCalc.default_periods;
		periods = HazardCurvePlotter.commaDoubleSplit(periodStrs);
		
		File outputDir = new File(cmd.getOptionValue("output-dir"));
		if (rank == 0)
			Preconditions.checkArgument((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir(),
				"Output dir does not exist and could not be created");
		
		CyberShakeComponent comp = CybershakeIM.fromShortName(cmd.getOptionValue("component"), CyberShakeComponent.class);
		
		File sitesFile = new File(cmd.getOptionValue("sites"));
		Preconditions.checkState(sitesFile.exists(), "Sites file doesn't exist: %s", sitesFile);
		
		// load sites
		Document siteDoc = XMLUtils.loadDocument(sitesFile);
		List<Site> sites = Lists.newArrayList();
		
		ParameterList siteParams = MCERDataProductsCalc.getSiteParams(attenRelsList.get(0));
		
		for (Element elem : XMLUtils.getSubElementsList(siteDoc.getRootElement(), "Site")) {
			// will clone site params list
			sites.add(Site.fromXMLMetadata(elem, siteParams));
		}
		
		Collections.sort(sites, new SiteComparator());
		
		// create calcs
		calcs = new CalcRunnable[numThreads];
		cachePrefixes = new String[attenRelsList.get(0).size()];
		
		Component gmpeComp = MCErCalcUtils.getSupportedTranslationComponent(
				attenRelsList.get(0).get(0), comp.getGMPESupportedComponents());
		
		DiscretizedFunc xVals = new IMT_Info().getDefaultHazardCurve(SA_Param.NAME);
		
		periodsFunc = new ArbitrarilyDiscretizedFunc();
		for (double period : periods)
			periodsFunc.set(period, 0d);
		
		Map<String, DiscretizedFunc> xValsMap = Maps.newHashMap();
		
		for (int t=0; t<numThreads; t++) {
			List<AttenuationRelationship> attenRels = attenRelsList.get(t);
			AbstractMCErDeterministicCalc[] detCalcs = new AbstractMCErDeterministicCalc[attenRels.size()];
			AbstractMCErProbabilisticCalc[] probCalcs = new AbstractMCErProbabilisticCalc[attenRels.size()];
			for (int i=0; i<attenRels.size(); i++) {
				cachePrefixes[i] = CyberShakeMCErMapGenerator.getCachePrefix(
						-1, erf, gmpeComp, Lists.newArrayList(attenRels.get(i)));
				detCalcs[i] = new GMPE_MCErDeterministicCalc(detERF, attenRels.get(i), gmpeComp);
				probCalcs[i] = new GMPE_MCErProbabilisticCalc(erf, attenRels.get(i), gmpeComp, xVals);
				
				if (t == 0) {
					xValsMap.put(cachePrefixes[i]+"_det", periodsFunc);
					xValsMap.put(cachePrefixes[i]+"_prob", periodsFunc);
					xValsMap.put(cachePrefixes[i]+"_mcer", periodsFunc);
				}
			}
			calcs[t] = new CalcRunnable(detCalcs, probCalcs);
		}
		
		archiver = new BinaryCurveArchiver(outputDir, sites.size(), xValsMap);
		if (rank == 0)
			archiver.initialize();
		
		calcTasks = Lists.newArrayList();
		for (int i=0; i<sites.size(); i++) {
			Site site = sites.get(i);
			for (int j=0; j<cachePrefixes.length; j++)
				calcTasks.add(new CalcTask(cachePrefixes[j], i, j, site));
		}
	}
	
	private class CalcRunnable implements Runnable {
		
		private AbstractMCErDeterministicCalc[] detCalcs;
		private AbstractMCErProbabilisticCalc[] probCalcs;
		
		private Deque<CalcTask> tasks;
		
		public CalcRunnable(AbstractMCErDeterministicCalc[] detCalcs, AbstractMCErProbabilisticCalc[] probCalcs) {
			this.detCalcs = detCalcs;
			this.probCalcs = probCalcs;
		}
		
		private CalcTask popTask() {
			Preconditions.checkNotNull(tasks);
			synchronized (tasks) {
				try {
					return tasks.pop();
				} catch (NoSuchElementException e) {
					return null;
				}
			}
		}

		@Override
		public void run() {
			while (true) {
				CalcTask task = popTask();
				if (task == null)
					break;
				task.compute(detCalcs, probCalcs);
			}
		}
		
	}
	
	/**
	 * Comparator to ensure consistent ordering of site list
	 * @author kevin
	 *
	 */
	private static class SiteComparator implements Comparator<Site> {

		@Override
		public int compare(Site o1, Site o2) {
			int ret = o1.getLocation().compareTo(o2.getLocation());
			if (ret == 0 && o1.getName() != null && o2.getName() != null)
				ret = o1.getName().compareTo(o2.getName());
			return ret;
		}
		
	}
	
	private class CalcTask {
		private String cachePrefix;
		private int gmpeIndex;
		private int siteIndex;
		private Site site;
		
		public CalcTask(String cachePrefix, int siteIndex, int gmpeIndex, Site site) {
			this.cachePrefix = cachePrefix;
			this.siteIndex = siteIndex;
			this.gmpeIndex = gmpeIndex;
			this.site = site;
		}
		
		public void compute(AbstractMCErDeterministicCalc[] detCalcs, AbstractMCErProbabilisticCalc[] probCalcs) {
			String name = siteIndex+", prefix '"+cachePrefix+"'";
			CurveMetadata mcerMetadata = new CurveMetadata(site, siteIndex, null, cachePrefix+"_mcer");
			if (archiver.isCurveCalculated(mcerMetadata, periodsFunc)) {
				debug("Site "+name+" already done!");
				return;
			}
			debug("Calculating deterministic, site "+name);
			DiscretizedFunc det = AbstractMCErDeterministicCalc.toSpectrumFunc(detCalcs[gmpeIndex].calc(site, periods));
			debug("Calculating probabilistic, site "+name);
			DiscretizedFunc prob = probCalcs[gmpeIndex].calc(site, periods);
			DiscretizedFunc asceDeterm = null;
			try {
				asceDeterm = ASCEDetLowerLimitCalc.calc(
						periodsFunc, site.getParameter(Double.class, Vs30_Param.NAME).getValue(), site.getLocation());
			} catch (Exception e1) {
				System.out.println("WARNING: couldn't fetch Deterministic Lower Limit for site at "+site.getLocation());
			}
			DiscretizedFunc mcer = MCERDataProductsCalc.calcMCER(det, prob, asceDeterm);
			
			debug("Archiving site "+name);
			try {
				archiver.archiveCurve(det, new CurveMetadata(site, siteIndex, null, cachePrefix+"_det"));
				archiver.archiveCurve(prob, new CurveMetadata(site, siteIndex, null, cachePrefix+"_prob"));
				archiver.archiveCurve(mcer, mcerMetadata);
			} catch (IOException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			debug("DONE site "+name);
		}
	}

	@Override
	protected int getNumTasks() {
		return calcTasks.size();
	}

	@Override
	protected void calculateBatch(int[] batch) throws Exception {
		Deque<CalcTask> deque = new ArrayDeque<MPJ_GMPE_MCErCacheGen.CalcTask>();
		for (int index : batch)
			deque.push(calcTasks.get(index));
		
		List<Thread> threads = Lists.newArrayList();
		
		for (int i=0; i<calcs.length; i++) {
			calcs[i].tasks = deque;
			threads.add(new Thread(calcs[i]));
		}
		
		// start the threads
		for (Thread t : threads) {
			t.start();
		}
		
		// join the threads
		for (Thread t : threads) {
			t.join();
		}
		
//		for (int index : batch)
//			calcTasks.get(index).compute();
	}

	@Override
	protected void doFinalAssembly() throws Exception {
		// do nothing
	}
	
	public static Options createOptions() {
		Options ops = MPJTaskCalculator.createOptions();
		
		Option sites = new Option("s", "sites", true, "Sites XML file");
		sites.setRequired(true);
		ops.addOption(sites);
		
		Option output = new Option("o", "output-dir", true, "Output directory");
		output.setRequired(true);
		ops.addOption(output);
		
		Option period = new Option("p", "period", true, "SA periods. Default: "+MCERDataProductsCalc.default_periods);
		period.setRequired(false);
		ops.addOption(period);
		
		Option erfFile = new Option("ef", "erf-file", true, "XML ERF description file for comparison");
		erfFile.setRequired(true);
		ops.addOption(erfFile);
		
		Option attenRelFiles = new Option("af", "atten-rel-file", true,
				"XML Attenuation Relationship description file(s) for " + 
				"comparison. Multiple files should be comma separated");
		attenRelFiles.setRequired(true);
		ops.addOption(attenRelFiles);
		
		Option component = new Option("cmp", "component", true, "Intensity measure component. "
				+ "Options: "+Joiner.on(",").join(CybershakeIM.getShortNames(CyberShakeComponent.class)));
		component.setRequired(true);
		ops.addOption(component);
		
		return ops;
	}
	
	public static void main(String[] args) {
		args = MPJTaskCalculator.initMPJ(args);
		
		try {
			Options options = createOptions();
			
			CommandLine cmd = parse(options, args, MPJ_GMPE_MCErCacheGen.class);
			
			MPJ_GMPE_MCErCacheGen driver = new MPJ_GMPE_MCErCacheGen(cmd);
			
			driver.run();
			
			finalizeMPJ();
			
			System.exit(0);
		} catch (Throwable t) {
			abortAndExit(t);
		}
	}

}
