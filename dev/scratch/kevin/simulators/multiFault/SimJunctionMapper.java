package scratch.kevin.simulators.multiFault;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.FaultUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.coulomb.CoulombRatesRecord;
import scratch.UCERF3.inversion.coulomb.CoulombRatesTester;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.kevin.simulators.erf.SimulatorFaultSystemSolution;

public class SimJunctionMapper {
	
	private FaultSystemSolution ucerfSol;
	private FaultSystemSolution simSol;
	
	private Map<IDPairing, Double> simDistances;
	private Map<IDPairing, Double> ucerfDistances;
	
	private Map<Integer, Integer> simToUCERF_parentsMap;
	private File resultsDir;
	
	// lowest always first
	private Map<IDPairing, Double> simParentTogetherRates;
	private Map<IDPairing, Double> normSimParentTogetherRates;
	private Map<IDPairing, Double> ucerfParentTogetherRates;
	private Map<IDPairing, Double> normUCERFParentTogetherRates;
	
	private CoulombRates coulombRates;
	
	// mapped with UCERF parent IDs
	private Map<IDPairing, CoulombRatesRecord[]> parentCoulombs;
	
	private CoulombRatesTester tester;

	public SimJunctionMapper(FaultSystemSolution ucerfSol, FaultSystemSolution simSol, CoulombRates coulombRates, File resultsDir)
			throws IOException {
		this.ucerfSol = ucerfSol;
		this.simSol = simSol;
		this.resultsDir = resultsDir;
		
		simDistances = loadDistances(simSol.getRupSet(), "rsqsim");
		// fill out simulator distances as they can get really screwy with "X" ruptures
		FaultSystemRupSet simRupSet = simSol.getRupSet();
		for (int i=0; i<simRupSet.getNumSections(); i++) {
			for (int j=0; j<simRupSet.getNumSections(); j++) {
				IDPairing pair = new IDPairing(i, j);
				if (!simDistances.containsKey(pair))
					simDistances.put(pair, Double.POSITIVE_INFINITY);
			}
		}
		ucerfDistances = loadDistances(ucerfSol.getRupSet(), "ucerf");
		
		mapParentSects();
		
		// only for sucessfully mapped pairs
		simParentTogetherRates = calcParentTogetherRates(simSol, new HashSet<Integer>(simToUCERF_parentsMap.keySet()));
		normSimParentTogetherRates = calcNormParentTogetherRates(simParentTogetherRates, simSol);
		ucerfParentTogetherRates = calcParentTogetherRates(ucerfSol, new HashSet<Integer>(simToUCERF_parentsMap.values()));
		normUCERFParentTogetherRates = calcNormParentTogetherRates(ucerfParentTogetherRates, ucerfSol);
		
		// now remap sim parents to ucerf parents
		simParentTogetherRates = remapSimParentTogetherRates(simParentTogetherRates);
		normSimParentTogetherRates = remapSimParentTogetherRates(normSimParentTogetherRates);
		
		this.coulombRates = coulombRates;
		parentCoulombs = Maps.newHashMap();
		
		FaultSystemRupSet ucerfRupSet = ucerfSol.getRupSet();
		for (CoulombRatesRecord rec : coulombRates.values()) {
			IDPairing subSectPair = rec.getPairing();
			int parent1 = ucerfRupSet.getFaultSectionData(subSectPair.getID1()).getParentSectionId();
			int parent2 = ucerfRupSet.getFaultSectionData(subSectPair.getID2()).getParentSectionId();
			if (parent1 == parent2)
				continue;
			IDPairing parentPair = getPairing(parent1, parent2);
			if (parentCoulombs.containsKey(parentPair))
				// already exists, put in 2nd slot
				parentCoulombs.get(parentPair)[1] = rec;
			else
				// first one, put in first slot
				parentCoulombs.put(parentPair, new CoulombRatesRecord[] { rec, null });
		}
		for (CoulombRatesRecord[] recs : parentCoulombs.values())
			Preconditions.checkState(recs[0] != null && recs[1] != null);
		System.out.println("Sim has "+simParentTogetherRates.size()+" parent connections");
		System.out.println("UCERF has "+ucerfParentTogetherRates.size()+" parent connections");
		System.out.println("Coulomb has "+parentCoulombs.size()+" parent connections");
		
		tester = LaughTestFilter.getDefault().getCoulombFilter();
	}
	
	private Map<IDPairing, Double> loadDistances(FaultSystemRupSet rupSet, String prefix) throws IOException {
		File distFile = new File(resultsDir, "distances_"+prefix+"_"+rupSet.getNumRuptures()+".txt");
		if (distFile.exists())
			return DeformationModelFetcher.readMapFile(distFile);
		Map<IDPairing, Double> distances = DeformationModelFetcher.calculateDistances(50d, rupSet.getFaultSectionDataList());
		
		HashMap<IDPairing, Double> reversed = new HashMap<IDPairing, Double>();

		// now add the reverse distance
		for (IDPairing pair : distances.keySet()) {
			IDPairing reverse = pair.getReversed();
			reversed.put(reverse, distances.get(pair));
		}
		distances.putAll(reversed);
		
		DeformationModelFetcher.writeMapFile(distances, distFile);
		return distances;
	}
	
//	private static List<IDPairing> loadJunctions() {
//		return null;
//	}
	
	private void mapParentSects() throws IOException {
		simToUCERF_parentsMap = Maps.newHashMap();
		
		File matchCSVFile = new File(resultsDir, "parent_matches.csv");
		if (matchCSVFile.exists()) {
			// load it
			System.out.println("Loading precomputed parent sects...");
			CSVFile<String> csv = CSVFile.readFile(matchCSVFile, true);
			for (int row=1; row<csv.getNumRows(); row++) {
				List<String> line = csv.getLine(row);
				Integer simParent = Integer.parseInt(line.get(1));
				Integer ucerfParent = Integer.parseInt(line.get(3));
				simToUCERF_parentsMap.put(simParent, ucerfParent);
			}
			return;
		}
		File noMatchCSVFile = new File(resultsDir, "parent_nomatches.csv");
		System.out.println("Mapping parent sects...");
		
		List<ParentSectInfo> simParents = getParentSects(simSol.getRupSet());
		List<ParentSectInfo> ucerfParents = getParentSects(ucerfSol.getRupSet());
		
		double maxEndDistTol = 20d;
		double maxClosestDistTol = 1; // make sure that the trace is close. can be overridden if names match
		double maxDipDiff = 10;
		double maxRakeDiff = 30;
		
		CSVFile<String> matchCSV = new CSVFile<String>(true);
		CSVFile<String> noMatchCSV = new CSVFile<String>(true);
		List<String> header = Lists.newArrayList("Sim Name", "Sim ID", "UCERF3 Name",
				"UCERF3 ID", "Lengh Discrep", "End Loc Discrep", "Min Trace Dist", "Rake Diff", "Dip Diff");
		matchCSV.addLine(header);
		noMatchCSV.addLine(header);
		
		for (ParentSectInfo simParent : simParents) {
			ParentSectInfo closest = null;
			double closestDist = Double.MAX_VALUE;
			double traceDist = Double.NaN;
			for (ParentSectInfo ucerfParent : ucerfParents) {
				if (ucerfParent.parentID == 230)
					// hack for pathological case
					continue;
				double dist = simParent.calcMaxDist(ucerfParent);
				double myTraceDist = simParent.calcMinDiscrTraceDist(ucerfParent);
				double myRakeDiff = simParent.getRakeDiff(ucerfParent);
				double myDipDiff = simParent.getDipDiff(ucerfParent);
				boolean passTrace = myTraceDist <= maxClosestDistTol;
				if (!passTrace && myTraceDist < 5) {
					// lets check name
					passTrace = StringUtils.getCommonPrefix(simParent.parentName, ucerfParent.parentName).length() > 4;
					if (!passTrace)
						passTrace = StringUtils.getLevenshteinDistance(simParent.parentName, ucerfParent.parentName) < 4
								&& simParent.parentName.length() > 4;
				}
				if (dist < closestDist && passTrace
						&& myRakeDiff < maxRakeDiff && myDipDiff < maxDipDiff) {
					closest = ucerfParent;
					closestDist = dist;
					traceDist = myTraceDist;
				}
			}
			double distDiscrep;
			List<String> line;
			if (closest == null) {
				distDiscrep = Double.NaN;
				line = Lists.newArrayList(simParent.parentName, simParent.parentID+"",
						"", "", distDiscrep+"", Double.NaN+"", Double.NaN+"", Double.NaN+"", Double.NaN+"");
			} else {
				distDiscrep = Math.abs(closest.length - simParent.length);
				line = Lists.newArrayList(simParent.parentName, simParent.parentID+"",
						closest.parentName, closest.parentID+"", (float)distDiscrep+"",
						(float)closestDist+"", (float)traceDist+"",
						(float)simParent.getRakeDiff(closest)+"", (float)simParent.getDipDiff(closest)+"");
			}
			if (closestDist <= maxEndDistTol) {
				// it's a match
				simToUCERF_parentsMap.put(simParent.parentID, closest.parentID);
				matchCSV.addLine(line);
			} else {
				noMatchCSV.addLine(line);
			}
		}
		
		System.out.println("Matched "+simToUCERF_parentsMap.size()+"/"+simParents.size()+" parents.");
		matchCSV.writeToFile(matchCSVFile);
		noMatchCSV.writeToFile(noMatchCSVFile);
	}
	
	private List<ParentSectInfo> getParentSects(FaultSystemRupSet rupSet) {
		Map<Integer, List<FaultSectionPrefData>> map = Maps.newHashMap();
		for (FaultSectionPrefData sect : rupSet.getFaultSectionDataList()) {
			Integer parentID = sect.getParentSectionId();
			List<FaultSectionPrefData> sects = map.get(parentID);
			if (sects == null) {
				sects = Lists.newArrayList();
				map.put(parentID, sects);
			}
			sects.add(sect);
		}
		
		List<ParentSectInfo> parents = Lists.newArrayList();
		for (List<FaultSectionPrefData> sects : map.values())
			parents.add(new ParentSectInfo(sects));
		
		return parents;
	}
	
	private static Map<IDPairing, Double> calcParentTogetherRates(FaultSystemSolution sol, HashSet<Integer> parentsToConsider) {
		Map<IDPairing, Double> map = Maps.newHashMap();
		FaultSystemRupSet rupSet = sol.getRupSet();
		for (int r=0; r<rupSet.getNumRuptures(); r++) {
			List<Integer> parents = rupSet.getParentSectionsForRup(r);
			double rate = sol.getRateForRup(r);
			if (rate == 0)
				continue;
			for (int i=1; i<parents.size(); i++) {
				IDPairing pair = getPairing(parents.get(i-1), parents.get(i));
				if (!parentsToConsider.contains(pair.getID1()) || !parentsToConsider.contains(pair.getID2()))
					continue;
				Double val = map.get(pair);
				if (val == null)
					val = 0d;
				val += rate;
				map.put(pair, val);
			}
		}
		return map;
	}
	
	private static Map<IDPairing, Double> calcNormParentTogetherRates(Map<IDPairing, Double> togetherRates, FaultSystemSolution sol) {
		FaultSystemRupSet rupSet = sol.getRupSet();
		
		Map<IDPairing, Double> normTogetherRates = Maps.newHashMap();
		
		MinMaxAveTracker sectsAtConnectTrack = new MinMaxAveTracker();
		
		for (IDPairing pair : togetherRates.keySet()) {
			double togetherRate = togetherRates.get(pair);
			// sections of parent 1 that connect to parent 2
			HashSet<Integer> sects1 = new HashSet<Integer>();
			// sections of parent 2 that connect to parent 1
			HashSet<Integer> sects2 = new HashSet<Integer>();
			for (int rupIndex : rupSet.getRupturesForParentSection(pair.getID1())) {
				List<Integer> parentsForRup = rupSet.getParentSectionsForRup(rupIndex);
				if (parentsForRup.contains(pair.getID2())) {
					// this is a multi fault with both parents
					List<FaultSectionPrefData> sects = rupSet.getFaultSectionDataForRupture(rupIndex);
					// find the connecting sub section
					for (int i=1; i<sects.size(); i++) {
						FaultSectionPrefData s1 = sects.get(i-1);
						FaultSectionPrefData s2 = sects.get(i);
						if (s1.getParentSectionId() == pair.getID1() && s2.getParentSectionId() == pair.getID2()) {
							sects1.add(s1.getSectionId());
							sects2.add(s2.getSectionId());
						} else if (s1.getParentSectionId() == pair.getID2() && s2.getParentSectionId() == pair.getID1()) {
							sects1.add(s2.getSectionId());
							sects2.add(s1.getSectionId());
						}
					}
				}
			}
			Preconditions.checkState(!sects1.isEmpty());
			Preconditions.checkState(!sects2.isEmpty());
			sectsAtConnectTrack.addValue(sects1.size());
			sectsAtConnectTrack.addValue(sects2.size());
			
			// total rate of section on parent 1 that connects to parent 2
			double s1Rate = getTotSectRates(sects1, sol);
			// total rate of section on parent 2 that connects to parent 1
			double s2Rate = getTotSectRates(sects2, sol);
			double avg = 0.5*(s1Rate+s2Rate);
			normTogetherRates.put(pair, togetherRate/avg);
		}
		System.out.println("Sections that connect: "+sectsAtConnectTrack);
		return normTogetherRates;
	}
	
	private static double getTotSectRates(HashSet<Integer> sects, FaultSystemSolution sol) {
		FaultSystemRupSet rupSet = sol.getRupSet();
		HashSet<Integer> rups = new HashSet<Integer>();
		for (int sect : sects)
			rups.addAll(rupSet.getRupturesForSection(sect));
		double rate = 0;
		for (int rupIndex : rups)
			rate += sol.getRateForRup(rupIndex);
		return rate;
	}
	
	private Map<IDPairing, Double> remapSimParentTogetherRates(Map<IDPairing, Double> togetherRates) {
		Map<IDPairing, Double> simRemappedParentTogetherRates = Maps.newHashMap();
		for (IDPairing pair : togetherRates.keySet()) {
			Integer mapped1 = simToUCERF_parentsMap.get(pair.getID1());
			Integer mapped2 = simToUCERF_parentsMap.get(pair.getID2());
			Preconditions.checkNotNull(mapped1);
			Preconditions.checkNotNull(mapped2);
			simRemappedParentTogetherRates.put(getPairing(mapped1, mapped2), togetherRates.get(pair));
		}
		return simRemappedParentTogetherRates;
	}
	
	private static IDPairing getPairing(int id1, int id2) {
		if (id1 < id2)
			return new IDPairing(id1, id2);
		return new IDPairing(id2, id1);
	}
	
	private static final double trace_discr_km = 1d;
	
	private class ParentSectInfo {
		private int parentID;
		private String parentName;
		private double length;
		private Location loc1; // always the northermonst
		private Location loc2; // always the southernmost
		List<Location> discrTrace = Lists.newArrayList();
		
		private double avgRake;
		private double avgDip;

		public ParentSectInfo(List<FaultSectionPrefData> sectsForParent) {
			this.parentID = sectsForParent.get(0).getParentSectionId();
			this.parentName = sectsForParent.get(0).getParentSectionName();
			
			length = 0;
			List<Double> rakes = Lists.newArrayList();
			for (FaultSectionPrefData sect: sectsForParent) {
				FaultTrace trace = sect.getFaultTrace();
				double subLen = trace.getTraceLength();
				length += subLen;
				int num = (int)Math.round(subLen/trace_discr_km);
				if (num < 2)
					num = 2;
				discrTrace.addAll(FaultUtils.resampleTrace(trace, num));
				avgDip += sect.getAveDip();
				rakes.add(sect.getAveRake());
			}
			avgDip /= (double)sectsForParent.size();
			avgRake = FaultUtils.getInRakeRange(FaultUtils.getAngleAverage(rakes));
			
			loc1 = sectsForParent.get(0).getFaultTrace().first();
			loc2 = sectsForParent.get(sectsForParent.size()-1).getFaultTrace().last();
			if (loc1.getLatitude() < loc2.getLatitude()) {
				// swap em
				Location tempLoc = loc1;
				loc1 = loc2;
				loc2 = tempLoc;
			}
		}
		
		public double calcMaxDist(ParentSectInfo o) {
			return Math.max(LocationUtils.horzDistanceFast(loc1, o.loc1), LocationUtils.horzDistanceFast(loc2, o.loc2));
		}
		
		public double calcMinDiscrTraceDist(ParentSectInfo o) {
			double val = Double.MAX_VALUE;
			for (Location loc1 : discrTrace)
				for (Location loc2 : o.discrTrace)
					val = Math.min(val, LocationUtils.horzDistance(loc1, loc2));
			return val;
		}
		
		public double getDipDiff(ParentSectInfo o) {
			return Math.abs(avgDip - o.avgDip);
		}
		
		public double getRakeDiff(ParentSectInfo o) {
			double r1 = avgRake;
			double r2 = o.avgRake;
			if (r1 < -90 && r2 > 90)
				r1 += 360;
			return Math.abs(r1 - r2);
		}
	}
	
	public void plotMultiRateVsCoulomb(Map<IDPairing, Double> parentTogetherRates, String name, boolean norm) {
		// first make sure that every parent together rate has a coulomb mapping
		// nevermind, RSQ sim can connect in a few extra places, ignore check
//		for (IDPairing pair : parentTogetherRates.keySet())
//			Preconditions.checkNotNull(parentCoulombs.get(pair), "No coulomb for "+pair);
		DefaultXY_DataSet passXY = new DefaultXY_DataSet();
		DefaultXY_DataSet failXY = new DefaultXY_DataSet();
		
		for (IDPairing pair : parentCoulombs.keySet()) {
			CoulombRatesRecord[] recs = parentCoulombs.get(pair);
			double dcff = Math.max(recs[0].getCoulombStressChange(), recs[1].getCoulombStressChange());
			Double rate = parentTogetherRates.get(pair);
			if (rate == null)
				continue;
//				rate = 0d;
			if (tester.doesRupturePass(Lists.newArrayList(recs[0]), Lists.newArrayList(recs[1])))
				passXY.set(dcff, rate);
			else
				failXY.set(dcff, rate);
		}
		
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		if (passXY.getNum() > 0) {
			funcs.add(passXY);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.GREEN));
		}
		
		if (failXY.getNum() > 0) {
			funcs.add(failXY);
			chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.RED));
		}
		
		// now do regression
		SimpleRegression reg = new SimpleRegression(true);
		MinMaxAveTracker xTrack = new MinMaxAveTracker();
		for (Point2D pt : passXY) {
			if (pt.getX() > 0 && pt.getY() > 0) {
				reg.addData(Math.log10(pt.getX()), Math.log10(pt.getY()));
				xTrack.addValue(pt.getX());
			}
		}
		for (Point2D pt : failXY) {
			if (pt.getX() > 0 && pt.getY() > 0) {
				reg.addData(Math.log10(pt.getX()), Math.log10(pt.getY()));
				xTrack.addValue(pt.getX());
			}
		}
		ArbitrarilyDiscretizedFunc regFunc = new ArbitrarilyDiscretizedFunc();
		regFunc.setName("Best Fit Line. MSE="+reg.getMeanSquareError());
		regFunc.set(xTrack.getMin(), Math.pow(10, reg.predict(Math.log10(xTrack.getMin()))));
		regFunc.set(xTrack.getMax(), Math.pow(10, reg.predict(Math.log10(xTrack.getMax()))));
		
		funcs.add(0, regFunc);
		chars.add(0, new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		
		String title = name+" Junction Rate vs DCFF";
		if (norm)
			title = "Normalized "+title;
		
		GraphWindow gw = new GraphWindow(funcs, title, chars);
		gw.setX_AxisLabel("Coulomb DCFF");
		if (norm)
			gw.setY_AxisLabel("Normalized Junction Corupture Rate");
		else
			gw.setY_AxisLabel("Junction Corupture Rate");
		gw.setXLog(true);
		gw.setYLog(true);
	}
	
	public void plotSimVsUCERFParentRates(boolean norm) {
		Map<IDPairing, Double> simRates;
		Map<IDPairing, Double> ucerfRates;
		if (norm) {
			simRates = normSimParentTogetherRates;
			ucerfRates = normUCERFParentTogetherRates;
		} else {
			simRates = simParentTogetherRates;
			ucerfRates = ucerfParentTogetherRates;
		}
		
		DefaultXY_DataSet xy = new DefaultXY_DataSet();
		
		HashSet<IDPairing> pairs = new HashSet<IDPairing>();
		pairs.addAll(simRates.keySet());
		pairs.addAll(ucerfRates.keySet());
		
		for (IDPairing pair : pairs) {
			Double simRate = simRates.get(pair);
			if (simRate == null)
//				simRate = 0d;
				continue;
			Double ucerfRate = ucerfRates.get(pair);
			if (ucerfRate == null)
//				ucerfRate = 0d;
				continue;
			xy.set(simRate, ucerfRate);
		}
		
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.add(xy);
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.BLACK));
		
		// now do regression
		MinMaxAveTracker xTrack = new MinMaxAveTracker();
		for (Point2D pt : xy)
			if (pt.getX() > 0 && pt.getY() > 0)
				xTrack.addValue(pt.getX());
		
		ArbitrarilyDiscretizedFunc oneToOne = new ArbitrarilyDiscretizedFunc();
		oneToOne.set(xTrack.getMin(), xTrack.getMin());
		oneToOne.set(xTrack.getMax(), xTrack.getMax());
		
		funcs.add(0, oneToOne);
		chars.add(0, new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		String title = " UCERF vs RSQSim Junction Rates";
		if (norm)
			title = "Normalized "+title;
		
		GraphWindow gw = new GraphWindow(funcs, title, chars);
		if (norm) {
			gw.setX_AxisLabel("Normalized RSQSim Junction Corupture Rate");
			gw.setY_AxisLabel("Normalized UCERF Junction Corupture Rate");
		} else {
			gw.setX_AxisLabel("RSQSim Junction Corupture Rate");
			gw.setY_AxisLabel("UCERF Junction Corupture Rate");
		}
		gw.setXLog(true);
		gw.setYLog(true);
	}
	
	public void plotJumps(double jumpDist, double minMag) {
		EvenlyDiscretizedFunc ucerfFunc = CommandLineInversionRunner.getJumpFuncs(ucerfSol, ucerfDistances, jumpDist, minMag, null)[0];
		EvenlyDiscretizedFunc simFunc = CommandLineInversionRunner.getJumpFuncs(simSol, simDistances, jumpDist, minMag, null)[0];
		
		simFunc.setName("RSQSim");
		ucerfFunc.setName("UCERF");
		
		List<PlotElement> funcs = Lists.newArrayList();
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		funcs.add(simFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
		funcs.add(ucerfFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		String xLabel = "Number of Jumps > "+(float)jumpDist+" km";
		String yLabel = "Rate";
		
		GraphWindow gw = new GraphWindow(funcs, "Multi Fault M"+(float)minMag+"+ Rupture Jumps > "+(float)jumpDist+" km", chars);
		gw.setX_AxisLabel(xLabel);
		gw.setY_AxisLabel(yLabel);
	}

	public static void main(String[] args) throws IOException, DocumentException {
		File invSolDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		System.out.println("Loading UCERF sol");
		FaultSystemSolution ucerfSol = FaultSystemIO.loadSol(new File(invSolDir,
						"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		CoulombRates coulombRates = CoulombRates.loadUCERF3CoulombRates(FaultModels.FM3_1);
		
		File simMainDir = new File("/home/kevin/Simulators");
		File resultsDir = new File(simMainDir, "multiFault");
		if (!resultsDir.exists())
			resultsDir.mkdir();
		
		File simSolFile = new File(simMainDir, "rsqsim_long_sol.zip");
		FaultSystemSolution simSol;
		if (simSolFile.exists()) {
			System.out.println("Loading Sim sol");
			simSol = FaultSystemIO.loadSol(simSolFile);
		} else {
			File geomFile = new File(simMainDir, "ALLCAL2_1-7-11_Geometry.dat");
			System.out.println("Loading geometry from "+geomFile.getAbsolutePath());
			General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//			File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
			File eventFile = new File(simMainDir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
			System.out.println("Loading events...");
			tools.read_EQSIMv04_EventsFile(eventFile);
			List<EQSIM_Event> events = tools.getEventsList();
			
			simSol = new SimulatorFaultSystemSolution(tools.getElementsList(), events, tools.getSimulationDurationYears());
			FaultSystemIO.writeSol(simSol, simSolFile);
		}
		
		SimJunctionMapper mapper = new SimJunctionMapper(ucerfSol, simSol, coulombRates, resultsDir);

		mapper.plotMultiRateVsCoulomb(mapper.ucerfParentTogetherRates, "UCERF3", false);
		mapper.plotMultiRateVsCoulomb(mapper.normUCERFParentTogetherRates, "UCERF3", true);
		mapper.plotMultiRateVsCoulomb(mapper.simParentTogetherRates, "RSQSim", false);
		mapper.plotMultiRateVsCoulomb(mapper.normSimParentTogetherRates, "RSQSim", true);
		
		mapper.plotSimVsUCERFParentRates(false);
		mapper.plotSimVsUCERFParentRates(true);
		
		mapper.plotJumps(0.1d, 6d);
		mapper.plotJumps(1d, 6d);
		
		mapper.plotJumps(0.1d, 7d);
		mapper.plotJumps(1d, 7d);
	}

}
