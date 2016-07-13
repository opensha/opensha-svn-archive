package org.opensha.sha.simulators.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.util.FaultUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.SimulatorElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.kevin.simulators.erf.SimulatorFaultSystemSolution;

public class RSQSimUtils {
	
	public static EqkRupture buildSubSectBasedRupture(EQSIM_Event event, List<FaultSectionPrefData> subSects,
			List<SimulatorElement> elements) {
		int minElemSectID = Integer.MAX_VALUE;
		int maxElemSectID = -1;
		for (SimulatorElement elem : elements) {
			int id = elem.getSectionID();
			if (id < minElemSectID)
				minElemSectID = id;
			if (id > maxElemSectID)
				maxElemSectID = id;
		}
		Preconditions.checkState(subSects.size()-1 == (maxElemSectID - minElemSectID),
				"Couldn't map to subsections. Have %s sub sects, range in elems is %s to %s",
				subSects.size(), minElemSectID, maxElemSectID);
		double mag = event.getMagnitude();
		
		List<Double> rakes = Lists.newArrayList();
		for (SimulatorElement elem : event.getAllElements())
			rakes.add(elem.getFocalMechanism().getRake());
		double rake = FaultUtils.getAngleAverage(rakes);
		if (rake > 180)
			rake -= 360;
		
		HashSet<Integer> rupSectIDs = new HashSet<Integer>();
		
		for (SimulatorElement elem : event.getAllElements())
			rupSectIDs.add(elem.getSectionID());
		
		// bundle by parent section id
		Map<Integer, List<FaultSectionPrefData>> rupSectsBundled = Maps.newHashMap();
		for (int sectID : rupSectIDs) {
			// convert to 0-based
			sectID -= minElemSectID;
			FaultSectionPrefData sect = subSects.get(sectID);
			List<FaultSectionPrefData> sects = rupSectsBundled.get(sect.getParentSectionId());
			if (sects == null) {
				sects = Lists.newArrayList();
				rupSectsBundled.put(sect.getParentSectionId(), sects);
			}
			sects.add(sect);
		}
		
		Map<IDPairing, Double> distsCache = Maps.newHashMap();
		List<List<FaultSectionPrefData>> rupSectsListBundled = Lists.newArrayList();
		Comparator<FaultSectionPrefData> sectNameCompare = new Comparator<FaultSectionPrefData>() {
			
			@Override
			public int compare(FaultSectionPrefData o1, FaultSectionPrefData o2) {
				return new Integer(o1.getSectionId()).compareTo(o2.getSectionId());
			}
		};
		for (List<FaultSectionPrefData> sects : rupSectsBundled.values()) {
			Collections.sort(sects, sectNameCompare);
			rupSectsListBundled.add(sects);
		}
		
		rupSectsListBundled = SimulatorFaultSystemSolution.sortRupture(subSects, rupSectsListBundled, distsCache);
		
		List<FaultSectionPrefData> rupSects = Lists.newArrayList();
		for (List<FaultSectionPrefData> sects : rupSectsListBundled)
			rupSects.addAll(sects);
		
		double gridSpacing = 1d;
		
		List<RuptureSurface> rupSurfs = Lists.newArrayList();
		for (FaultSectionPrefData sect : rupSects)
			rupSurfs.add(sect.getStirlingGriddedSurface(gridSpacing, false, false));
		
		RuptureSurface surf;
		if (rupSurfs.size() == 1)
			surf = rupSurfs.get(0);
		else
			surf = new CompoundSurface(rupSurfs);
		
		EqkRupture rup = new EqkRupture(mag, rake, surf, null);
		
		return rup;
	}
	
	public static List<FaultSectionPrefData> getUCERF3SubSectsForComparison(FaultModels fm, DeformationModels dm) {
		return new DeformationModelFetcher(fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1).getSubSectionList();
	}

}
