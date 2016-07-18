package org.opensha.sha.simulators.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FaultUtils;
import org.opensha.commons.util.XMLUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.faultSurface.CompoundSurface;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.EventRecord;
import org.opensha.sha.simulators.SimulatorElement;
import org.opensha.sha.simulators.iden.MagRangeRuptureIdentifier;
import org.opensha.sha.simulators.parsers.RSQSimFileReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.inversion.laughTest.LaughTestFilter;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.UCERF3_DataUtils;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoProbabilityModel;
import scratch.kevin.simulators.erf.SimulatorFaultSystemSolution;

public class RSQSimUtils {

	public static EqkRupture buildSubSectBasedRupture(EQSIM_Event event, List<FaultSectionPrefData> subSects,
			List<SimulatorElement> elements) {
		int minElemSectID = getSubSectIndexOffset(elements, subSects);
		double mag = event.getMagnitude();

		List<Double> rakes = Lists.newArrayList();
		for (SimulatorElement elem : event.getAllElements())
			rakes.add(elem.getFocalMechanism().getRake());
		double rake = FaultUtils.getAngleAverage(rakes);
		if (rake > 180)
			rake -= 360;

		Map<IDPairing, Double> distsCache = Maps.newHashMap();

		List<List<FaultSectionPrefData>> rupSectsListBundled = getSectionsForRupture(event, minElemSectID, subSects, distsCache);

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
	
	public static int getSubSectIndexOffset(List<SimulatorElement> elements, List<FaultSectionPrefData> subSects) {
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
		return minElemSectID;
	}
	
	private static List<List<FaultSectionPrefData>> getSectionsForRupture(EQSIM_Event event, int minElemSectID,
			List<FaultSectionPrefData> subSects, Map<IDPairing, Double> distsCache) {
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

		List<List<FaultSectionPrefData>> rupSectsListBundled = Lists.newArrayList();
		for (List<FaultSectionPrefData> sects : rupSectsBundled.values()) {
			Collections.sort(sects, sectIDCompare);
			rupSectsListBundled.add(sects);
		}
		
		if (rupSectsListBundled.size() > 1)
			rupSectsListBundled = SimulatorFaultSystemSolution.sortRupture(subSects, rupSectsListBundled, distsCache);
		
		return rupSectsListBundled;
	}
	
	private static Comparator<FaultSectionPrefData> sectIDCompare = new Comparator<FaultSectionPrefData>() {

		@Override
		public int compare(FaultSectionPrefData o1, FaultSectionPrefData o2) {
			return new Integer(o1.getSectionId()).compareTo(o2.getSectionId());
		}
	};
	
	private static File getCacheDir() {
		File scratchDir = UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR;
		if (scratchDir.exists()) {
			// eclipse project
			File dir = new File(scratchDir, "SubSections");
			if (!dir.exists())
				Preconditions.checkState(dir.mkdir());
			return dir;
		} else {
			// use home dir
			String path = System.getProperty("user.home");
			File homeDir = new File(path);
			Preconditions.checkState(homeDir.exists(), "user.home dir doesn't exist: "+path);
			File openSHADir = new File(homeDir, ".opensha");
			if (!openSHADir.exists())
				Preconditions.checkState(openSHADir.mkdir(),
						"Couldn't create OpenSHA store location: "+openSHADir.getAbsolutePath());
			File uc3Dir = new File(openSHADir, "ucerf3_sub_sects");
			if (!uc3Dir.exists())
				Preconditions.checkState(uc3Dir.mkdir(),
						"Couldn't create UCERF3 ERF store location: "+uc3Dir.getAbsolutePath());
			return uc3Dir;
		}
	}

	public static List<FaultSectionPrefData> getUCERF3SubSectsForComparison(FaultModels fm, DeformationModels dm) {
		File cacheDir = getCacheDir();
		File xmlFile = new File(cacheDir, fm.encodeChoiceString()+"_"+dm.encodeChoiceString()+"_sub_sects.xml");
		if (xmlFile.exists()) {
			try {
				return FaultModels.loadStoredFaultSections(xmlFile);
			} catch (Exception e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
		}
		System.out.println("No sub section cache exists for "+fm.getShortName()+", "+dm.getShortName());
		List<FaultSectionPrefData> sects = new DeformationModelFetcher(
				fm, dm, UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1).getSubSectionList();
		// write to XML
		Document doc = XMLUtils.createDocumentWithRoot();
		FaultSystemIO.fsDataToXML(doc.getRootElement(), FaultModels.XML_ELEMENT_NAME, fm, null, sects);
		try {
			XMLUtils.writeDocumentToFile(xmlFile, doc);
		} catch (IOException e) {
			System.err.println("WARNING: Couldn't write cache file: "+xmlFile.getAbsolutePath());
			e.printStackTrace();
		}
		return sects;
	}

	public static FaultSystemSolution buildFaultSystemSolution(List<FaultSectionPrefData> subSects,
			List<SimulatorElement> elements, List<EQSIM_Event> events, double minMag) {
		int minElemSectID = getSubSectIndexOffset(elements, subSects);
		
		if (minMag > 0)
			events = new MagRangeRuptureIdentifier(minMag, 10d).getMatches(events);
		
		// for each rup
		double[] mags = new double[events.size()];
		double[] rupRakes = new double[events.size()];
		double[] rupAreas = new double[events.size()];
		double[] rupLengths = new double[events.size()];
		double[] rates = new double[events.size()];
		double durationYears = General_EQSIM_Tools.getSimulationDurationYears(events);
		double rateEach = 1d/(durationYears);
		List<List<Integer>> sectionForRups = Lists.newArrayList();

		Comparator<FaultSectionPrefData> fsdIndexSorter = new Comparator<FaultSectionPrefData>() {

			@Override
			public int compare(FaultSectionPrefData o1, FaultSectionPrefData o2) {
				return new Integer(o1.getSectionId()).compareTo(new Integer(o2.getSectionId()));
			}
		};

		// cache sub section distances, used for rupture section ordering later
		//				System.out.print("Caching Distances...");
		Map<IDPairing, Double> distsCache = Maps.newHashMap();
		//				for (int i=0; i<fsd.size(); i++) {
		//					for (int j=i+1; j<fsd.size(); j++) {
		//						if (i == j)
		//							continue;
		//						double minDist = Double.POSITIVE_INFINITY;
		//						for (Location loc1 : fsd.get(i).getFaultTrace()) {
		//							for (Location loc2 : fsd.get(j).getFaultTrace()) {
		//								double dist = LocationUtils.horzDistance(loc1, loc2);
		//								if (dist < minDist)
		//									minDist = dist;
		//							}
		//						}
		//						IDPairing pair = new IDPairing(i, j);
		//						distsCache.put(pair, minDist);
		//						distsCache.put(pair.getReversed(), minDist);
		//					}
		//				}
		//				System.out.println("DONE.");

		//				Table<String, String, Double> distsCache = HashBasedTable.create();

		System.out.print("Building ruptures...");
		for (int i=0; i<events.size(); i++) {
			EQSIM_Event e = events.get(i);
			mags[i] = e.getMagnitude();
			rupAreas[i] = e.getArea();
			rupLengths[i] = e.getLength();
			rates[i] = rateEach;
			
			List<List<FaultSectionPrefData>> subSectsForFaults = getSectionsForRupture(e, minElemSectID, subSects, distsCache);

			List<Double> rakes = Lists.newArrayList();
			List<Integer> rupSectIndexes = Lists.newArrayList();
			for (List<FaultSectionPrefData> faultList : subSectsForFaults) {
				for (FaultSectionPrefData subSect : faultList) {
					rupSectIndexes.add(subSect.getSectionId());
					rakes.add(subSect.getAveRake());
				}
			}
			sectionForRups.add(rupSectIndexes);

			double avgRake = FaultUtils.getAngleAverage(rakes);
			if (avgRake > 180)
				avgRake -= 360;
			rupRakes[i] = avgRake;
		}
		System.out.println("DONE.");

		// for each section
		double[] sectSlipRates = new double[subSects.size()];
		double[] sectSlipRateStdDevs = null;
		double[] sectAreas = new double[subSects.size()];

		for (int s=0; s<subSects.size(); s++) {
			FaultSectionPrefData sect = subSects.get(s);
			sectSlipRates[s] = sect.getReducedAveSlipRate();
			sectAreas[s] = sect.getReducedDownDipWidth()*sect.getTraceLength()*1e6; // in meters
		}

		String info = "Fault Simulators Solution\n"
				+ "# Elements: "+elements.size()+"\n"
				+ "# Sub Sections: "+subSects.size()+"\n"
				+ "# Events/Rups: "+events.size()+"\n"
				+ "Duration: "+durationYears+"\n"
				+ "Indv. Rup Rate: "+(1d/durationYears);

		FaultSystemRupSet rupSet = new FaultSystemRupSet(subSects, sectSlipRates, sectSlipRateStdDevs, sectAreas,
				sectionForRups,mags, rupRakes, rupAreas, rupLengths, info);
		FaultSystemSolution sol = new FaultSystemSolution(rupSet, rates);
		return sol;
	}
	
	public static void writeUCERF3ComparisonPlots(FaultSystemSolution sol, FaultModels fm, DeformationModels dm,
			File dir, String prefix) throws GMT_MapException, RuntimeException, IOException {
//		InversionFaultSystemRupSet invRupSet = new InversionFaultSystemRupSet(
//				sol.getRupSet(), LogicTreeBranch.fromValues(fm, dm), null, null, null, null, null);
//		invRupSet.getSlipOnSectionsForRup(rthRup)
//		InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(invRupSet, sol.getRateForAllRups());
		Region region = new CaliforniaRegions.RELM_TESTING();

		// map plots
		FaultBasedMapGen.plotOrigNonReducedSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotOrigCreepReducedSlipRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotTargetSlipRates(sol, region, dir, prefix, false);
//		FaultBasedMapGen.plotSolutionSlipRates(sol, region, dir, prefix, false);
//		FaultBasedMapGen.plotSolutionSlipMisfit(sol, region, dir, prefix, false, true);
//		FaultBasedMapGen.plotSolutionSlipMisfit(sol, region, dir, prefix, false, false);
//		FaultSystemSolution ucerf2 = getUCERF2Comparision(sol.getRupSet().getFaultModel(), dir);
		for (double[] range : BatchPlotGen.partic_mag_ranges) {
			FaultBasedMapGen.plotParticipationRates(sol, region, dir, prefix, false, range[0], range[1]);
//			FaultBasedMapGen.plotParticipationRatios(sol, ucerf2, region, dir, prefix, false, range[0], range[1], true);
		}
		FaultBasedMapGen.plotSectionPairRates(sol, region, dir, prefix, false);
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 0, 10);
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 7, 10);
		FaultBasedMapGen.plotSegmentation(sol, region, dir, prefix, false, 7.5, 10);
		
		// regular plots
//		CommandLineInversionRunner.writeMFDPlots(sol, dir, prefix);
		
//		if (!hasJumpPlots) {
//			try {
//				DeformationModels dm = sol.getRupSet().getFaultModel().getFilterBasis();
//				if (dm == null)
//					dm = sol.getRupSet().getDeformationModel();
//				Map<IDPairing, Double> distsMap = new DeformationModelFetcher(
//						sol.getRupSet().getFaultModel(), dm,
//						UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, 0.1).getSubSectionDistanceMap(
//								LaughTestFilter.getDefault().getMaxJumpDist());
//				CommandLineInversionRunner.writeJumpPlots(sol, distsMap, dir, prefix);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		ArrayList<PaleoRateConstraint> paleoRateConstraints =
				CommandLineInversionRunner.getPaleoConstraints(fm, sol.getRupSet());
		List<AveSlipConstraint> aveSlipConstraints = AveSlipConstraint.load(sol.getRupSet().getFaultSectionDataList());
//		CommandLineInversionRunner.writePaleoPlots(paleoRateConstraints, aveSlipConstraints, sol, dir, prefix);
//		CommandLineInversionRunner.writeSAFSegPlots(sol, dir, prefix);
//		CommandLineInversionRunner.writePaleoCorrelationPlots(sol,
//						new File(dir, CommandLineInversionRunner.PALEO_CORRELATION_DIR_NAME), UCERF3_PaleoProbabilityModel.load());
		CommandLineInversionRunner.writeParentSectionMFDPlots(sol,
						new File(dir, CommandLineInversionRunner.PARENT_SECT_MFD_DIR_NAME));
//		CommandLineInversionRunner.writePaleoFaultPlots(paleoRateConstraints, aveSlipConstraints, sol,
//						new File(dir, CommandLineInversionRunner.PALEO_FAULT_BASED_DIR_NAME));
		CommandLineInversionRunner.writeRupPairingSmoothnessPlot(sol, prefix, dir);
	}
	
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators/UCERF3_35kyrs");
		File geomFile = new File(dir, "UCERF3.1km.tri.flt");
		List<SimulatorElement> elements = RSQSimFileReader.readGeometryFile(geomFile, 11, 'S');
		System.out.println("Loaded "+elements.size()+" elements");
//		for (Location loc : elements.get(0).getVertices())
//			System.out.println(loc);
		File eListFile = new File(dir, "UCERF3_35kyrs.eList");
		File pListFile = new File(dir, "UCERF3_35kyrs.pList");
		File dListFile = new File(dir, "UCERF3_35kyrs.dList");
		File tListFile = new File(dir, "UCERF3_35kyrs.tList");
		
		List<EQSIM_Event> events = RSQSimFileReader.readEventsFile(eListFile, pListFile, dListFile, tListFile, elements,
				Lists.newArrayList(new MagRangeRuptureIdentifier(7d, 10d)));
		
		FaultSystemSolution sol = buildFaultSystemSolution(getUCERF3SubSectsForComparison(
				FaultModels.FM3_1, DeformationModels.ZENGBB), elements, events, 6d);
	}

}
