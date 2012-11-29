package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.faultSurface.FaultTrace;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.DeformationModelFileParser;
import scratch.UCERF3.utils.DeformationModelFileParser.DeformationSection;

public class MiniSectRecurrenceGen {

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		File file = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/InversionSolutions/" +
				"2012_10_14-fm3-logic-tree-sample-x5_MEAN_BRANCH_AVG_SOL.zip");
		
		SimpleFaultSystemSolution sol = SimpleFaultSystemSolution.fromFile(file);
		Map<Integer, DeformationSection> origDM =
				DeformationModelFileParser.load(DeformationModels.GEOLOGIC.getDataFileURL(FaultModels.FM3_1));
		
		
		Map<Integer, List<FaultSectionPrefData>> sectsMap = Maps.newHashMap();
		for (FaultSectionPrefData sect : sol.getFaultSectionDataList()) {
			Integer parentID = sect.getParentSectionId();
			List<FaultSectionPrefData> sects = sectsMap.get(parentID);
			if (sects == null) {
				sects = Lists.newArrayList();
				sectsMap.put(parentID, sects);
			}
			sects.add(sect);
		}
		
		for (Integer parentID : origDM.keySet()) {
			List<FaultSectionPrefData> sects = sectsMap.get(parentID);
			
//			FaultTrace trace = sect.getFaultTrace();
			
			DeformationSection dmSect = origDM.get(parentID);
			LocationList trace = dmSect.getLocsAsTrace();
			
			for (int mini=0; mini<dmSect.getSlips().size(); mini++) {
				Location dmStart = dmSect.getLocs1().get(mini);
				Location dmEnd = dmSect.getLocs2().get(mini);
				
				List<Double> lengths = Lists.newArrayList();
				List<Double> rates = Lists.newArrayList();
				
				for (FaultSectionPrefData sect : sects) {
					Location sectStart = sect.getFaultTrace().get(0);
					Location sectEnd = sect.getFaultTrace().get(sect.getFaultTrace().size()-1);
					
					boolean startBefore = isBefore(dmStart, dmEnd, sectStart);
					boolean startAfter = isAfter(dmStart, dmEnd, sectStart);
					boolean startBetween = !startBefore && !startAfter;
					
					boolean endBefore = isBefore(dmStart, dmEnd, sectEnd);
					boolean endAfter = isAfter(dmStart, dmEnd, sectEnd);
					boolean endBetween = !endBefore && !endAfter;
					
					if (startAfter)
						continue;
					if (endBefore)
						continue;
					
					double lenContained;
					
					if (startBetween && endBetween) {
						// sect is completely contained in mini
						lenContained = sect.getFaultTrace().getTraceLength();
					} else if (startBefore && endAfter) {
						// mini is completely contained in sect
						lenContained = 0;
						for (int i=1; i<trace.size(); i++)
							lenContained += LocationUtils.horzDistance(trace.get(i-1), trace.get(i));
					} else if (startBefore && endBetween) {
						int firstBetweenIndex = -1;
						for (int i=0; i<sect.getFaultTrace().size(); i++) {
							Location loc = sect.getFaultTrace().get(i);
							if (isBetween(dmStart, dmEnd, loc)) {
								firstBetweenIndex = i;
								break;
							}
						}
						Preconditions.checkState(firstBetweenIndex > 0);
						lenContained = 0;
						for (int i=firstBetweenIndex; i<sect.getFaultTrace().size(); i++) {
							Location prevLoc;
							if (i == firstBetweenIndex)
								prevLoc = dmStart;
							else
								prevLoc = sect.getFaultTrace().get(i-1);
							Location loc = sect.getFaultTrace().get(i);
							lenContained += LocationUtils.horzDistance(prevLoc, loc);
						}
					} else if (startBetween && endAfter) {
						int firstAfterIndex = -1;
						for (int i=0; i<sect.getFaultTrace().size(); i++) {
							Location loc = sect.getFaultTrace().get(i);
							if (isAfter(dmStart, dmEnd, loc)) {
								firstAfterIndex = i;
								break;
							}
						}
						Preconditions.checkState(firstAfterIndex > 0);
						lenContained = 0;
						for (int i=0; i<firstAfterIndex; i++) {
							Location loc = sect.getFaultTrace().get(i);
							Location afterLoc;
							if (i+1 == firstAfterIndex)
								afterLoc = dmEnd;
							else
								afterLoc = sect.getFaultTrace().get(i+1);
							lenContained += LocationUtils.horzDistance(loc, afterLoc);
						}
					} else {
						throw new IllegalStateException("Shouldn't get here...");
					}
					
					lengths.add(lenContained);
					double particRate = sol.calcParticRateForSect(sect.getSectionId(), 6.7d, 10d);
					Preconditions.checkState(lenContained > 0, "Invalid length contained: "+lenContained);
					Preconditions.checkState(particRate >= 0, "Invalid partic rate: "+particRate);
					rates.add(particRate);
				}
				
				Preconditions.checkState(!lengths.isEmpty(), "No mappings found!!!");
				
				double avgRate = DeformationModelFetcher.calcLengthBasedAverage(lengths, rates);
				double avgRP = 1d/avgRate;
				
				dmSect.getRakes().set(mini, avgRP);
			}
		}
		
		DeformationModelFileParser.write(origDM, new File("/tmp/fm3_1_avg_sol_rps.csv"));
	}
	
	/**
	 * Determines if the given point, pt, is before or equal to the start point. This is
	 * done by determining that pt is closer to start than end, and is further from end
	 * than start is.
	 * 
	 * @param start
	 * @param end
	 * @param pt
	 * @return
	 */
	private static boolean isBefore(Location start, Location end, Location pt) {
		if (start.equals(pt) || LocationUtils.areSimilar(start, pt))
			return true;
		double pt_start_dist = LocationUtils.linearDistanceFast(pt, start);
		if (pt_start_dist == 0)
			return true;
		double pt_end_dist = LocationUtils.linearDistanceFast(pt, end);
		double start_end_dist = LocationUtils.linearDistanceFast(start, end);

		return pt_start_dist < pt_end_dist && pt_end_dist > start_end_dist;
	}

	/**
	 * Determines if the given point, pt, is after or equal to the end point. This is
	 * done by determining that pt is closer to end than start, and is further from start
	 * than end is.
	 * 
	 * @param start
	 * @param end
	 * @param pt
	 * @return
	 */
	private static boolean isAfter(Location start, Location end, Location pt) {
		if (end.equals(pt) || LocationUtils.areSimilar(end, pt))
			return true;
		double pt_end_dist = LocationUtils.linearDistanceFast(pt, end);
		if (pt_end_dist == 0)
			return true;
		double pt_start_dist = LocationUtils.linearDistanceFast(pt, start);
		double start_end_dist = LocationUtils.linearDistanceFast(start, end);

		return pt_end_dist < pt_start_dist && pt_start_dist > start_end_dist;
	}
	
	private static boolean isBetween(Location start, Location end, Location pt) {
		return !isBefore(start, end, pt) && !isAfter(start, end, pt);
	}

}
