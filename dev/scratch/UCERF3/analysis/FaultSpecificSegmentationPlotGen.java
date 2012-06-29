package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;

public class FaultSpecificSegmentationPlotGen {
	
	public static void plotSegmentation(List<Integer> parentSects, FaultSystemSolution sol, double minMag, boolean endsOnly) {
		PlotSpec spec = buildSegmentationPlot(parentSects, sol, minMag, endsOnly);
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(spec.getFuncs(), spec.getTitle(), spec.getChars(), false);
		gw.setX_AxisLabel(spec.getxAxisLabel());
		gw.setY_AxisLabel(spec.getyAxisLabel());
		gw.getGraphWindow().getGraphPanel().setxAxisInverted(true);
		gw.getGraphWindow().setVisible(true);
	}
	
	public static HeadlessGraphPanel getSegmentationHeadlessGP(List<Integer> parentSects, FaultSystemSolution sol,
			double minMag, boolean endsOnly) throws IOException {
		PlotSpec spec = buildSegmentationPlot(parentSects, sol, minMag, endsOnly);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setxAxisInverted(true);
		
		gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(), spec.getFuncs(), spec.getChars(), false, spec.getTitle());
		
		return gp;
	}
	
	private static PlotSpec buildSegmentationPlot(List<Integer> parentSects, FaultSystemSolution sol, double minMag, boolean endsOnly) {
		// first assemble subsections by parent
		Map<Integer, List<FaultSectionPrefData>> subSectsByParent = Maps.newHashMap();
		int prevParentID = -2;
		List<FaultSectionPrefData> curSects = null;
		for (int sectIndex=0; sectIndex<sol.getNumSections(); sectIndex++) {
			FaultSectionPrefData sect = sol.getFaultSectionData(sectIndex);
			int parent = sect.getParentSectionId();
			if (parent != prevParentID) {
				prevParentID = parent;
				curSects = Lists.newArrayList();
				subSectsByParent.put(parent, curSects);
			}
			curSects.add(sect);
		}
		
		Map<Location, List<Integer>> stoppingPoints = Maps.newHashMap();
		List<Location> parentSectEnds = Lists.newArrayList();
		
		double toleranceKM = 3;
		
		for (Integer parent : parentSects) {
			List<FaultSectionPrefData> sects = subSectsByParent.get(parent);
			
			List<Location> sectStoppingPoints = Lists.newArrayList();
			for (int i=0; i<sects.size(); i++) {
				FaultSectionPrefData sect = sects.get(i);
				FaultTrace trace = sect.getFaultTrace();
				sectStoppingPoints.add(trace.get(0));
				
				if (i == sects.size()-1)
					sectStoppingPoints.add(trace.get(trace.size()-1));
			}
			
			for (int i=0; i<sectStoppingPoints.size(); i++) {
				if (endsOnly && i > 0 && i < sectStoppingPoints.size()-1)
					continue;
				Location loc = sectStoppingPoints.get(i);
				Location testLoc = searchForMatch(loc, stoppingPoints.keySet(), toleranceKM);
				Location parentTestLoc;
				List<Integer> startSects;
				if (testLoc == null) {
					startSects = Lists.newArrayList();
					stoppingPoints.put(loc, startSects);
					parentTestLoc = searchForMatch(loc, stoppingPoints.keySet(), 5);
					if (parentTestLoc == null)
						parentTestLoc = loc;
				} else {
					startSects = stoppingPoints.get(testLoc);
					parentTestLoc = testLoc;
				}
				if (i == 0) {
					// this is the first loc, just add the first section
					startSects.add(sects.get(0).getSectionId());
					if (testLoc == null)
						parentSectEnds.add(loc);
				} else if (i == sectStoppingPoints.size()-1) {
					// this is the last loc, just add the last section
					startSects.add(sects.get(sects.size()-1).getSectionId());
					if (testLoc == null)
						parentSectEnds.add(loc);
				} else {
					// this is in the middle, add before and after
					startSects.add(sects.get(i-1).getSectionId());
					startSects.add(sects.get(i).getSectionId());
				}
			}
		}
		
		ArbitrarilyDiscretizedFunc stopFunc = new ArbitrarilyDiscretizedFunc();
		stopFunc.setName("Fract of rate that stops at this point");
		ArbitrarilyDiscretizedFunc continueFunc = new ArbitrarilyDiscretizedFunc();
		continueFunc.setName("Fract of rate that continues through this point");
		
		for (Location loc : stoppingPoints.keySet()) {
			List<Integer> sects = stoppingPoints.get(loc);
			
			double stopRate = 0;
			double continueRate = 0;
			
			HashSet<Integer> alreadyCounted = new HashSet<Integer>();
			for (int sectIndex : sects) {
				for (int rupIndex : sol.getRupturesForSection(sectIndex)) {
					if (alreadyCounted.contains(rupIndex))
						continue;
					
					if (sol.getMagForRup(rupIndex) < minMag)
						continue;
					
					double rate = sol.getRateForRup(rupIndex);
					
					List<Integer> sectsForRup = sol.getSectionsIndicesForRup(rupIndex);
					if (sectIndex == sectsForRup.get(0) || sectIndex == sectsForRup.get(sectsForRup.size()-1))
						stopRate += rate;
					else
						continueRate += rate;
					
					alreadyCounted.add(rupIndex);
				}
			}
			double tot = stopRate + continueRate;
			stopRate /= tot;
			continueRate /= tot;
			
			double x = loc.getLatitude();
			Preconditions.checkState(stopFunc.getXIndex(x) == -1, "duplicate latitude!! "+loc);
			
			stopFunc.set(x, stopRate);
			continueFunc.set(x, continueRate);
		}
		
		ArbitrarilyDiscretizedFunc sectEnds = new ArbitrarilyDiscretizedFunc();
		sectEnds.setName("Parent Section End Points");
		
		for (Location loc : parentSectEnds)
			sectEnds.set(loc.getLatitude(), 1.5);
		
		System.out.println("Parent stopping pts: "+parentSectEnds.size());
		
		ArrayList<ArbitrarilyDiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(continueFunc);
		funcs.add(stopFunc);
		funcs.add(sectEnds);
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CIRCLE, 5f, Color.GREEN.darker()));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.FILLED_SQUARE, 5f, Color.RED));
		float endsSize;
		if (endsOnly)
			endsSize = 0f;
		else
			endsSize = 8f;
		chars.add(new PlotCurveCharacterstics(PlotSymbol.BOLD_X, endsSize, Color.BLUE));
		
		String title = "Fault Segmentation";
		if (minMag > 5)
			title += " ("+(float)minMag+"+)";
		else
			title += " (All Mags)";
		
		return new PlotSpec(funcs, chars, title, "Latitude", "Rate Ratio");
	}
	
	private static Location searchForMatch(Location loc, Collection<Location> locs, double toleranceKM) {
		double best = Double.MAX_VALUE;
		Location bestLoc = null;
		for (Location testLoc : locs) {
			double dist = LocationUtils.horzDistance(loc, testLoc);
			if (dist < best && dist <= toleranceKM) {
				best = dist;
				bestLoc = testLoc;
			}
		}
		return bestLoc;
	}
	
	public static List<Integer> getSAFParents(FaultModels fm) {
		if (fm == FaultModels.FM2_1)
			return Lists.newArrayList(295, 284, 283, 119, 301, 286, 287, 300, 285, 32, 57, 56, 67, 27, 26, 13);
		else
			return Lists.newArrayList(97, 170, 295, 284, 283, 282, 301, 286, 287, 300,
					285, 32, 658, 657, 655, 654, 653, 13);
	}
	
	public static void main(String args[]) throws IOException, DocumentException {
//		File solFile = new File("/tmp/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
//		File solFile = new File("/tmp/FM3_1_GEOL_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
//		File solFile = new File("/tmp/FM3_1_UCERF2_COMPARISON_sol.zip");
		File solFile = new File("/tmp/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol_high_a_priori.zip");
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(solFile);
		
//		Map<Integer, List<Integer>> namedMap = sol.getFaultModel().getNamedFaultsMap();
//		List<Integer> parents = namedMap.get(32);
		// NBMK: 294
		List<Integer> parents = getSAFParents(sol.getFaultModel());
		
		System.out.println(Joiner.on(", ").join(parents));
		
//		plotSegmentation(parents, sol, 0, true);
//		plotSegmentation(parents, sol, 0, false);
//		plotSegmentation(parents, sol, 7, true);
//		plotSegmentation(parents, sol, 6.5, false);
		plotSegmentation(parents, sol, 7, false);
//		plotSegmentation(parents, sol, 7.5, true);
//		plotSegmentation(parents, sol, 7.5, false);
	}

}
