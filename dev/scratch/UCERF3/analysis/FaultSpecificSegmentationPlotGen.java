package scratch.UCERF3.analysis;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import scratch.UCERF3.utils.UCERF3_DataUtils;

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
		boolean normalize = true;
		
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
		
		String info = null;
		
		List<Location> stoppingKeysSorted = Lists.newArrayList();
		stoppingKeysSorted.addAll(stoppingPoints.keySet());
		// sort by latitude decending
		Collections.sort(stoppingKeysSorted, new Comparator<Location>() {

			@Override
			public int compare(Location o1, Location o2) {
				return -Double.compare(o1.getLatitude(), o2.getLatitude());
			}
		});
		
		HashMap<Integer, List<Integer>> rupStopCountMap = Maps.newHashMap();
		HashMap<Integer, List<Location>> rupStopLocationsMap = Maps.newHashMap();
		
		for (Location loc : stoppingKeysSorted) {
			List<Integer> sects = stoppingPoints.get(loc);
			
			double stopRate = 0;
			double continueRate = 0;
			
//			// if a rup includes only 1 section at this stopping point, then it stops there
//			// otherwise it continues through
//			
//			// list of all ruptures involving this stopping point
//			HashSet<Integer> allRups = new HashSet<Integer>();
//			// list of all rups where the first of last section of the rup is at the stopping point
//			// these are possible stops, but could be just next to stopping points
//			HashSet<Integer> possibleStops = new HashSet<Integer>();
//			// this is the same as above but for each section at this stopping point
//			List<HashSet<Integer>> possibleStopsPerSect = Lists.newArrayList();
//			
//			for (int sectIndex : sects) {
//				HashSet<Integer> sectPossibleStops = new HashSet<Integer>();
//				possibleStopsPerSect.add(sectPossibleStops);
//				for (int rupIndex : sol.getRupturesForSection(sectIndex)) {
//					if (sol.getMagForRup(rupIndex) < minMag)
//						continue;
//					
//					if (!allRups.contains(rupIndex))
//						allRups.add(rupIndex);
//					
//					List<Integer> sectsForRup = sol.getSectionsIndicesForRup(rupIndex);
//					boolean stoppingPoint = false;
//					
//					if (sectIndex == sectsForRup.get(0) && !sects.contains(sectsForRup.get(1)))
//						stoppingPoint = true;
//					
//					if (sectIndex == sectsForRup.get(0) || sectIndex == sectsForRup.get(sectsForRup.size()-1)) {
//						if (!possibleStops.contains(rupIndex))
//							possibleStops.add(rupIndex);
//						sectPossibleStops.add(rupIndex);
//					}
//				}
//			}
//			
//			// now go through each rupture
//			for (Integer rupIndex : allRups) {
//				double rate = sol.getRateForRup(rupIndex);
//				
//				if (rate == 0)
//					continue;
//				
//				boolean stoppingPoint = false;
//				if (possibleStops.contains(rupIndex)) {
//					// it could be a stop, but only if only one section here is involved
//					stoppingPoint = true;
//					Integer stopSect = null;
//					for (int i=0; i<possibleStopsPerSect.size(); i++) {
//						HashSet<Integer> sectPossibleStops = possibleStopsPerSect.get(i);
//						if (sectPossibleStops.contains(rupIndex)) {
//							if (stopSect == null)
//								stopSect = sects.get(i);
//							else {
//								stoppingPoint = false;
//								break;
//							}
//						}
//					}
//					Preconditions.checkNotNull(stopSect);
//					
//				}
//				
//				if (stoppingPoint)
//					stopRate += rate;
//				else
//					continueRate += rate;
//			}
			
			
			HashSet<Integer> alreadyCounted = new HashSet<Integer>();
			for (int sectIndex : sects) {
				for (int rupIndex : sol.getRupturesForSection(sectIndex)) {
					if (alreadyCounted.contains(rupIndex))
						continue;
					
					if (sol.getMagForRup(rupIndex) < minMag)
						continue;
					
					double rate = sol.getRateForRup(rupIndex);
					
					List<Integer> sectsForRup = sol.getSectionsIndicesForRup(rupIndex);
					boolean stoppingPoint = false;
					
					if (sectIndex == sectsForRup.get(0) && !sects.contains(sectsForRup.get(1)))
						stoppingPoint = true;
					else if (sectIndex == sectsForRup.get(sectsForRup.size()-1) && !sects.contains(sectsForRup.get(sectsForRup.size()-2)))
						stoppingPoint = true;
					
					if (stoppingPoint) {
						List<Integer> ends = rupStopCountMap.get(rupIndex);
						List<Location> endLocs = rupStopLocationsMap.get(rupIndex);
						if (ends == null) {
							ends = Lists.newArrayList();
							rupStopCountMap.put(rupIndex, ends);
							endLocs = Lists.newArrayList();
							rupStopLocationsMap.put(rupIndex, endLocs);
						}
						ends.add(sectIndex);
						endLocs.add(loc);
						if (ends.size() > 2) {
							String endsStr = null;
							for (int i=0; i<ends.size(); i++) {
								if (i == 0)
									endsStr = "";
								else
									endsStr += ", ";
								endsStr += ends.get(i)+" ["
										+Joiner.on(",").join(stoppingPoints.get(endLocs.get(i)))+"]";
							}
							throw new IllegalStateException("Stop count over 2 for rup "+rupIndex
									+". Stops at: "+endsStr);
						}
					}
					
					if (stoppingPoint)
						stopRate += rate;
					else
						continueRate += rate;
					
					alreadyCounted.add(rupIndex);
				}
			}
			double tot = stopRate + continueRate;
			double normStopRate = stopRate / tot;
			double normContinueRate = continueRate / tot;
			
			double x = loc.getLatitude();
			Preconditions.checkState(stopFunc.getXIndex(x) == -1, "duplicate latitude!! "+loc);
			
			if (normStopRate > 0.075) {
				if (info == null)
					info = "";
				else
					info += "\n";
				String parents = null;
				for (int sectID : sects) {
					String parentName = sol.getFaultSectionData(sectID).getParentSectionName();
					if (parents == null)
						parents = "";
					else
						parents += ", ";
					parents += parentName;
				}
				info += "Lat="+(float)loc.getLatitude()+"\tnumSects="+sects.size()+"\tparents=("+parents+")"
						+"\n\tstopRate="+stopRate+"\tcontinueRate="+continueRate+"\tnormStopRate="+normStopRate;
			}
			
			if (normalize) {
				stopFunc.set(x, normStopRate);
				continueFunc.set(x, normContinueRate);
			} else {
				stopFunc.set(x, stopRate);
				continueFunc.set(x, continueRate);
			}
		}
		stopFunc.setInfo(info);
		
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
		else if (fm == FaultModels.FM3_1)
			return Lists.newArrayList(97, 170, 295, 284, 283, 282, 301, 286, 287, 300,
					285, 32, 658, 657, 655, 654, 653, 13);
		else
			return Lists.newArrayList(97, 171, 295, 284, 283, 282, 301, 286, 287, 300,
					285, 32, 658, 657, 655, 654, 653, 13);
	}
	
	public static void main(String args[]) throws IOException, DocumentException {
//		File solFile = new File("/tmp/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip");
//		File solFile = new File("/tmp/FM3_1_GEOL_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_sol.zip");
//		File solFile = new File("/tmp/FM3_1_UCERF2_COMPARISON_sol.zip");
//		File solFile = new File("/tmp/FM2_1_UCERF2_COMPARISON_sol.zip");
//		File solFile = new File("/tmp/FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol_high_a_priori.zip");
		File solFile = new File(new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions"), "FM3_1_UCERF2_COMPARISON_sol.zip");
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
