package scratch.kevin.ucerf3;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.gui.plot.HeadlessGraphPanel;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.nshmp2.imr.impl.AB2006_140_AttenRel;
import org.opensha.refFaultParamDb.dao.db.DB_AccessAPI;
import org.opensha.refFaultParamDb.dao.db.DB_ConnectionPool;
import org.opensha.refFaultParamDb.dao.db.FaultSectionVer2_DB_DAO;
import org.opensha.refFaultParamDb.dao.db.PrefFaultSectionDataDB_DAO;
import org.opensha.refFaultParamDb.vo.FaultSectionData;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.erf.ETAS.ETAS_Utils;
import scratch.UCERF3.griddedSeismicity.UCERF3_GridSourceGenerator;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.MatrixIO;
import scratch.kevin.cybershake.ucerf3.CSDownsampledSolCreator;

public class PureScratch {
	
	private static void test1() {
		Location hypoLoc = new Location(34, -118);
		int num = 1000;
		double mag = 5d;
		
		ETAS_Utils utils = new ETAS_Utils();
		
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		
		int retries = 0;
		
		for (int i=0; i<num; i++) {
			double radius = ETAS_Utils.getRuptureRadiusFromMag(mag);
			double testRadius = radius+1;
			Location loc = null;
			while(testRadius>radius) {
				double lat = hypoLoc.getLatitude()+(2.0*utils.getRandomDouble()-1.0)*(radius/111.0);
				double lon = hypoLoc.getLongitude()+(2.0*utils.getRandomDouble()-1.0)*(radius/(111*Math.cos(hypoLoc.getLatRad())));
				double depthBottom = hypoLoc.getDepth()+radius;
				if(depthBottom>24.0)
					depthBottom=24.0;
				double depthTop = hypoLoc.getDepth()-radius;
				if(depthTop<0.0)
					depthTop=0.0;
				double depth = depthTop + utils.getRandomDouble()*(depthBottom-depthTop);
				loc = new Location(lat,lon,depth);
				testRadius=LocationUtils.linearDistanceFast(loc, hypoLoc);
				retries++;
			}
			xy.set(loc.getLongitude(), loc.getLatitude());
		}
		
		System.out.println("Retries: "+retries);
		GraphWindow gw = new GraphWindow(xy, "Circular Random Test", new PlotCurveCharacterstics(PlotSymbol.X, 2f, Color.BLACK));
		gw.setAxisRange(xy.getMinX(), xy.getMaxX(), xy.getMinY(), xy.getMaxY());
		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
	}
	
	private static void test2() throws IOException, DocumentException {
		FaultSystemSolution sol = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
			+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		FaultSystemRupSet rupSet = sol.getRupSet();
		
		HashSet<Integer> parents = new HashSet(FaultModels.FM3_1.getNamedFaultsMapAlt().get("San Andreas"));
		
		Region soCal = new CaliforniaRegions.RELM_SOCAL();
		
		Map<Integer, Boolean> safSectsInSoCal = Maps.newHashMap();
		for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++) {
			FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
			if (!parents.contains(sect.getParentSectionId()))
				continue;
			boolean inside = false;
			for (Location loc : sect.getFaultTrace()) {
				if (soCal.contains(loc)) {
					inside = true;
					break;
				}
			}
			safSectsInSoCal.put(sectIndex, inside);
//			System.out.println(sect.getName()+": "+inside);
		}
		
		int numSAF = 0;
		int numPartiallySAFSoCal = 0;
		int numOnlySAFSoCal = 0;
		rupLoop:
		for (int rupIndex = 0; rupIndex<rupSet.getNumRuptures(); rupIndex++) {
			for (int parent : rupSet.getParentSectionsForRup(rupIndex)) {
				if (!parents.contains(parent))
					continue rupLoop;
			}
			numSAF++;
			boolean partial = false;
			boolean only = true;
			for (int sectIndex : rupSet.getSectionsIndicesForRup(rupIndex)) {
				boolean inside = safSectsInSoCal.get(sectIndex);
				partial = partial || inside;
				only = only && inside;
			}
			if (partial)
				numPartiallySAFSoCal++;
			if (only)
				numOnlySAFSoCal++;
		}
		
		System.out.println(numSAF+"/"+rupSet.getNumRuptures()+" ruputures are only on SAF");
		System.out.println(numPartiallySAFSoCal+" of those are at least partially in SoCal");
		System.out.println(numOnlySAFSoCal+" of those are entirely in SoCal");
	}
	
	private static void test3() throws IOException, DocumentException {
		FaultSystemSolution sol = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
			+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		FaultSystemRupSet rupSet = sol.getRupSet();
		
		Region region = new CaliforniaRegions.SF_BOX();
		
		Map<String, Integer> parentsInBox = Maps.newHashMap();
		
		for (int sectIndex=0; sectIndex<rupSet.getNumSections(); sectIndex++) {
			FaultSectionPrefData sect = rupSet.getFaultSectionData(sectIndex);
			boolean inside = false;
			for (Location loc : sect.getFaultTrace()) {
				if (region.contains(loc)) {
					inside = true;
					break;
				}
			}
			if (inside)
				parentsInBox.put(sect.getParentSectionName(), sect.getParentSectionId());
//			System.out.println(sect.getName()+": "+inside);
		}
		
		List<String> names = Lists.newArrayList(parentsInBox.keySet());
		Collections.sort(names);
		
		for (String name : names)
			System.out.println(parentsInBox.get(name)+". "+name);
	}
	
	private static void test4() throws IOException, DocumentException {
		FaultSystemSolution sol_31 = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
			+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		FaultSystemSolution sol_32 = FaultSystemIO.loadSol(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
			+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_2_MEAN_BRANCH_AVG_SOL.zip"));
		
		System.out.println("FM3.1: "+sol_31.getRupSet().getNumRuptures());
		System.out.println("FM3.2: "+sol_32.getRupSet().getNumRuptures());
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
//		test1();
//		test3();
		test4();
		
////		FaultSystemSolution sol3 = FaultSystemIO.loadSol(new File("/tmp/avg_SpatSeisU3/"
////				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
////		System.out.println(sol3.getSubSeismoOnFaultMFD_List().size());
////		System.exit(0);
////		CompoundFaultSystemSolution cfss2 = CompoundFaultSystemSolution.fromZipFile(new File(
////				"/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
////				+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip"));
////		FaultSystemSolution sol1 = cfss2.getSolution(LogicTreeBranch.DEFAULT);
//		FaultSystemSolution inputSol = FaultSystemIO.loadSol(
////				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/"
////						+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/UCERF3_ERF/"
//						+ "cached_dep10.0_depMean_rakeGEOLOGIC.zip"));
//		
//		Region region = new CaliforniaRegions.RELM_SOCAL();
//		
//		FaultSystemSolution sol1 = CSDownsampledSolCreator.getDownsampledSol(inputSol, region);
//		
//		double[] counts = new double[sol1.getRupSet().getNumSections()];
//		for (int sectIndex=0; sectIndex<sol1.getRupSet().getNumSections(); sectIndex++)
//			counts[sectIndex] = sol1.getRupSet().getRupturesForSection(sectIndex).size();
//		HistogramFunction numHist = HistogramFunction.getEncompassingHistogram(0d, 50000d, 1000d);
////		HistogramFunction numHist = new HistogramFunction(50d, 10050d, (100));
//		for (double count : counts)
//			numHist.add(numHist.getClosestXIndex(count), 1d);
//		
//		List<DiscretizedFunc> funcs = Lists.newArrayList();
//		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
//		
//		funcs.add(numHist);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
//		
//		GraphWindow gw = new GraphWindow(funcs, "Ruptures Per Subsection", chars);
//		gw.setX_AxisLabel("Num Ruptures Including Subsection");
//		gw.setY_AxisLabel("Num Subsections");
//		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
//		gw.saveAsPNG("/tmp/rup_count_hist.png");
//		
//		// now fract
//		EvenlyDiscretizedFunc fractFunc = new EvenlyDiscretizedFunc(1d, 100000, 1d);
//		Arrays.sort(counts);
//		for (int i=0; i<fractFunc.size(); i++) {
//			double x = fractFunc.getX(i);
//			int index = Arrays.binarySearch(counts, x);
//			if (index < 0) {
//				index = -(index + 1);
//			}
//			double y = 1d - (double)(index)/(double)(counts.length-1);
//			fractFunc.set(i, y);
//		}
//		
//		funcs = Lists.newArrayList();
//		chars = Lists.newArrayList();
//		
//		funcs.add(fractFunc);
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
//		
//		gw = new GraphWindow(funcs, "Ruptures Per Subsection", chars);
//		gw.setX_AxisLabel("Num Ruptures Including Subsection");
//		gw.setY_AxisLabel("Fraction of Subsections >= Num");
//		gw.setDefaultCloseOperation(GraphWindow.EXIT_ON_CLOSE);
//		gw.saveAsPNG("/tmp/rup_count_fract_func.png");
//		
//		while (" ".length() > 0) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
//		int subSect = 1267;
//		System.out.println(sol1.getSubSeismoOnFaultMFD_List().get(subSect));
//		int numContained = 0;
//		Region reg = sol1.getGridSourceProvider().getGriddedRegion();
//		System.out.println("Poly overlap? "+(Region.intersect(reg,
//				sol1.getRupSet().getInversionTargetMFDs().getGridSeisUtils().getPolyMgr().getPoly(subSect)) != null));
////		((UCERF3_GridSourceGenerator)sol1.getGridSourceProvider()).get
//		for (Location loc : sol1.getRupSet().getFaultSectionData(subSect).getFaultTrace()) {
//			if (reg.contains(loc))
//				numContained++;
//		}
		
//		System.out.println(numContained+" points contained in "+reg.getName());
//		File sol1File = new File("/tmp/sol1.zip");
//		FaultSystemIO.writeSol(sol1, sol1File);
//		FaultSystemSolution sol2 = FaultSystemIO.loadSol(sol1File);
//		System.out.println("# sub seismos: "+sol2.getSubSeismoOnFaultMFD_List().size());
//		System.out.println("Orig first: "+sol1.getSubSeismoOnFaultMFD_List().get(0));
//		System.out.println("New first: "+sol2.getSubSeismoOnFaultMFD_List().get(0));
//		System.exit(0);
//		RegionUtils.regionToKML(new CaliforniaRegions.LA_BOX(), "la_box", Color.BLACK);
//		RegionUtils.regionToKML(new CaliforniaRegions.SF_BOX(), "sf_box", Color.BLACK);
//		System.exit(0);
//		FaultSystemSolution theSol = FaultSystemIO.loadSol(
//				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/"
//						+ "InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
//		FaultSystemRupSet rupSet = theSol.getRupSet();
//		int numWith = 0;
//		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList())
//			if (sect.getDateOfLastEvent() > Long.MIN_VALUE)
//				numWith++;
//		System.out.println(numWith+"/"+rupSet.getNumSections()+" have last event data");
//		System.exit(0);
//		HistogramFunction hist = new HistogramFunction(0.25, 40, 0.5);
//		for (FaultSectionPrefData sect : theSol.getRupSet().getFaultSectionDataList()) {
//			double len = sect.getTraceLength();
//			hist.add(len, 1d);
//		}
//		HeadlessGraphPanel gp = new HeadlessGraphPanel();
//		List<HistogramFunction> elems = Lists.newArrayList();
//		elems.add(hist);
//		List<PlotCurveCharacterstics> chars = Lists.newArrayList(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
//		PlotSpec spec = new PlotSpec(elems, chars, "Sub Sect Lenghts", "Length (km)", "Number");
//		gp.drawGraphPanel(spec);
//		gp.getCartPanel().setSize(1000, 800);
//		gp.setBackground(Color.WHITE);
//		gp.saveAsPNG("/tmp/sub_sect_length_hist.png");
//		System.exit(0);
//		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(new File("/tmp/compound/2013_05_10-ucerf3p3-production-10runs_run1_COMPOUND_SOL.zip"));
//		List<LogicTreeBranch> branches = Lists.newArrayList(cfss.getBranches());
//		Collections.shuffle(branches);
//		HashSet<FaultModels> models = new HashSet<FaultModels>();
//		for (int i=0; i<branches.size(); i++) {
//			LogicTreeBranch branch = branches.get(i);
//			FaultModels model = branch.getValue(FaultModels.class);
//			if (models.contains(model))
//				continue;
//			InversionFaultSystemSolution sol = cfss.getSolution(branch);
//			System.out.println(model.getShortName()+": "+sol.getRupSet().getNumRuptures());
//			System.out.println(sol.getClass().getName());
//			if (sol instanceof AverageFaultSystemSolution)
//				System.out.println(((AverageFaultSystemSolution)sol).getNumSolutions()+" sols");
//			models.add(model);
//		}
////		InversionFaultSystemSolution sol = cfss.getSolution(cfss.getBranches().iterator().next());
////		CommandLineInversionRunner.writeParentSectionMFDPlots(sol, new File("/tmp/newmfd"));
//		System.exit(0);
//		File f = new File("/tmp/FM3_2_ZENGBB_EllBsqrtLen_DsrTap_CharConst_M5Rate9.6_MMaxOff7.6_NoFix_SpatSeisU2_run0_sol.zip");
//		InversionFaultSystemSolution invSol = FaultSystemIO.loadInvSol(f);
//		System.out.println(invSol.getClass());
//		System.out.println(invSol.getLogicTreeBranch());
//		System.out.println(invSol.getInversionConfiguration());
//		System.out.println(invSol.getInvModel());
//		System.out.println(invSol.getMisfits().size());
//		invSol.getGridSourceProvider();
//		
//		cfss = CompoundFaultSystemSolution.fromZipFile(new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_05_03-ucerf3p3-production-first-five_MEAN_COMPOUND_SOL.zip"));
//		invSol = cfss.getSolution(invSol.getLogicTreeBranch());
//		invSol.getGridSourceProvider();
//		System.out.println("Got it from a grid source provider");
//		System.exit(0);
//		
//		System.out.println("LEGACY MEAN SOLUTION");
//		f = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/2013_01_14-stampede_3p2_production_runs_combined_FM3_1_MEAN_BRANCH_AVG_SOL.zip");
//		invSol = FaultSystemIO.loadInvSol(f);
//		System.out.println(invSol.getLogicTreeBranch());
//		System.out.println(invSol.getInversionConfiguration());
//		System.out.println(invSol.getInvModel());
//		System.out.println(invSol.getMisfits().size());
//		
//		System.out.println("LEGACY NORMAL SOLUTION");
//		f = new File("/tmp/FM3_1_ZENGBB_EllB_DsrUni_CharConst_M5Rate7.9_MMaxOff7.6_NoFix_SpatSeisU3_VarSlipTypeBOTH_VarSlipWtUnNorm100_sol.zip.1");
//		invSol = FaultSystemIO.loadInvSol(f);
//		System.out.println(invSol.getLogicTreeBranch());
//		System.out.println(invSol.getInversionConfiguration());
//		System.out.println(invSol.getInvModel());
//		System.out.println(invSol.getMisfits().size());
//		
//		// loading in AVFSS
//		f = new File("/tmp/compound_tests_data/subset/FM3_1_NEOK_Shaw09Mod_DsrUni_CharConst_M5Rate7.9_MMaxOff7.6_NoFix_SpatSeisU3_mean/FM3_1_NEOK_Shaw09Mod_DsrUni_CharConst_M5Rate7.9_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
//		AverageFaultSystemSolution avgSol = FaultSystemIO.loadAvgInvSol(f);
//		System.out.println("Average sol with "+avgSol.getNumSolutions()+" sols");
//		System.exit(0);
//		
//		double a1 = 8.62552e32;
//		double a2 = 1.67242e34;
//		double a3 = 1.77448e20;
//		double a4 = 9.05759e20;
//		double c = 8.0021909e37;
//		double me = 5.9742e24;
//		double re = 6371000;
//		
//		double p1 = (c - a1*me/a3) / (a2-a1*a4/a3);
//		double p2 = (me - a4*p1)/a3;
//		
//		System.out.println("p1: "+p1);
//		System.out.println("p2: "+p2);
//		
//		System.exit(0);
//		
//		
//		CB_2008_AttenRel imr = new CB_2008_AttenRel(null);
//		imr.setParamDefaults();
//		MeanUCERF2 ucerf = new MeanUCERF2();
//		ucerf.updateForecast();
//		ProbEqkSource src = ucerf.getSource(3337);
//		System.out.println(src.getName());
//		ProbEqkRupture theRup = src.getRupture(77);
//		System.out.println("Rup 77 pts: "+theRup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface().size());
//		imr.setEqkRupture(theRup);
//		imr.setIntensityMeasure(PGA_Param.NAME);
//		Site site = new Site(new Location(34.10688, -118.22060));
//		site.addParameterList(imr.getSiteParams());
//		imr.setAll(theRup, site, imr.getIntensityMeasure());
//		imr.getMean();
//		System.exit(0);
//		
//		UCERF2_TimeIndependentEpistemicList ti_ep = new UCERF2_TimeIndependentEpistemicList();
//		UCERF2_TimeDependentEpistemicList td_ep = new UCERF2_TimeDependentEpistemicList();
////		ep.updateForecast();
//		System.out.println(ti_ep.getNumERFs());
//		System.out.println(td_ep.getNumERFs());
//		
//		System.exit(0);
//		
//		
//		// this get's the DB accessor (version 3)
//		DB_AccessAPI db = DB_ConnectionPool.getDB3ReadOnlyConn();
//
//		PrefFaultSectionDataDB_DAO faultSectionDB_DAO = new PrefFaultSectionDataDB_DAO(db);
//
//		List<FaultSectionPrefData> sections = faultSectionDB_DAO.getAllFaultSectionPrefData(); 
//		for (FaultSectionPrefData data : sections)
//			System.out.println(data);
//		System.exit(0);
//		
//		
//		double minX = -9d;
//		double maxX = 0d;
//		int num = 200;
//		EvenlyDiscretizedFunc ucerf2Func = new EvenlyDiscretizedFunc(minX, maxX, num);
//		double delta = ucerf2Func.getDelta();
//		ucerf2Func.setName("UCERF2");
//		
//		boolean doUCERF2 = true;
//		
//		if (doUCERF2) {
//			System.out.println("Creating UCERF2");
//			ERF erf = new MeanUCERF2();
//			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_EXCLUDE);
//			erf.getTimeSpan().setDuration(1, TimeSpan.YEARS);
//			erf.updateForecast();
//			
//			System.out.println("Setting UCERF2 rates");
//			for (ProbEqkSource source : erf) {
//				for (ProbEqkRupture rup : source) {
//					if (Math.random() > 0.2d)
//						continue;
//					double prob = rup.getProbability();
//					double log10prob = Math.log10(prob);
//					if (log10prob < minX || log10prob > maxX) {
//						System.out.println("Prob outside of bounds: "+prob + " (log10: "+log10prob+")");
//					}
//					int ind = (int)Math.round((log10prob-minX)/delta);
//					ucerf2Func.set(ind, ucerf2Func.getY(ind)+1);
//				}
//			}
//		}
//		
//		File dir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/2011_09_08-morgan-CS_fixed");
//		File binFile = new File(dir, "run1.mat");
//		
//		double[] rupRateSolution = MatrixIO.doubleArrayFromFile(binFile);
//		int numNonZero = 0;
//		for (double rate : rupRateSolution)
//			if (rate > 0)
//				 numNonZero++;
//		double[] nonZeros = new double[numNonZero];
//		int cnt = 0;
//		for (double rate : rupRateSolution) {
//			if (rate > 0)
//				nonZeros[cnt++] = rate;
//		}
//		EvenlyDiscretizedFunc inversionFunc = new EvenlyDiscretizedFunc(minX, maxX, num);
//		inversionFunc.setName("UCERF3 Inversion");
//		
//		
//		System.out.println("Setting inversion rates");
//		for (int i=0; i<nonZeros.length; i++) {
//			double log10rate = Math.log10(nonZeros[i]);
//			if (log10rate < minX || log10rate > maxX) {
//				System.out.println("Prob outside of bounds: "+nonZeros[i] + " (log10: "+log10rate+")");
//			}
//			int ind = (int)Math.round((log10rate-minX)/delta);
//			inversionFunc.set(ind, inversionFunc.getY(ind)+1);
//		}
//		
//		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
//		funcs.add(ucerf2Func);
//		funcs.add(inversionFunc);
//		
//		chars = new ArrayList<PlotCurveCharacterstics>();
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLACK));
//		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE));
//		
//		System.out.println("Displaying graph!");
//		
//		new GraphWindow(funcs, "Rupture Rates", chars);
//		System.out.println("DONE");
	}

}
