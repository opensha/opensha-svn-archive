package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.util.DataUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.griddedSeismicity.GridSourceFileReader;
import scratch.UCERF3.griddedSeismicity.GridSourceProvider;
import scratch.UCERF3.inversion.InversionFaultSystemRupSet;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.logicTree.APrioriBranchWeightProvider;
import scratch.UCERF3.logicTree.BranchWeightProvider;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.UCERF3.utils.UCERF3_DataUtils;

public class TrueMeanBuilder {
	
	/**
	 * Class that describes a unique rupture
	 * @author kevin
	 *
	 */
	private static class UniqueRupture {
		// part of unique checks
		private int id;
		private double mag;
		private double rake;
		private double area;
		
		// not part of unique checks
		private double rate = 0;
		private List<UniqueSection> sects;
		private int cnt = 0;
		
		public UniqueRupture(int id, double mag, double rake, double area) {
			super();
			this.id = id;
			this.mag = mag;
			this.rake = rake;
			this.area = area;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(area);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + id;
			temp = Double.doubleToLongBits(mag);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(rake);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UniqueRupture other = (UniqueRupture) obj;
			if (Double.doubleToLongBits(area) != Double
					.doubleToLongBits(other.area))
				return false;
			if (id != other.id)
				return false;
			if (Double.doubleToLongBits(mag) != Double
					.doubleToLongBits(other.mag))
				return false;
			if (Double.doubleToLongBits(rake) != Double
					.doubleToLongBits(other.rake))
				return false;
			return true;
		}
	}
	
	private static class UniqueSection {
		// part of unique checks
		private int id;
		private double aveDip;
		private double aveUpperDepth;
		private double aveLowerDepth;
		
		// not part of unique checks
		private FaultSectionPrefData sect;

		public UniqueSection(FaultSectionPrefData sect, int globalID) {
			super();
			this.id = globalID;
			this.aveDip = sect.getAveDip();
			this.aveUpperDepth = sect.getReducedAveUpperDepth();
			this.aveLowerDepth = sect.getAveLowerDepth();
			this.sect = sect.clone();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(aveDip);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(aveLowerDepth);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(aveUpperDepth);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UniqueSection other = (UniqueSection) obj;
			if (Double.doubleToLongBits(aveDip) != Double
					.doubleToLongBits(other.aveDip))
				return false;
			if (Double.doubleToLongBits(aveLowerDepth) != Double
					.doubleToLongBits(other.aveLowerDepth))
				return false;
			if (Double.doubleToLongBits(aveUpperDepth) != Double
					.doubleToLongBits(other.aveUpperDepth))
				return false;
			if (id != other.id)
				return false;
			return true;
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws ZipException, IOException, DocumentException {
		File invDir = new File(UCERF3_DataUtils.DEFAULT_SCRATCH_DATA_DIR, "InversionSolutions");
		File compoundFile = new File(invDir, "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL.zip");
		CompoundFaultSystemSolution cfss = CompoundFaultSystemSolution.fromZipFile(compoundFile);
		cfss.setCacheCopying(false);
		BranchWeightProvider weightProvider = new APrioriBranchWeightProvider();
		FaultModels[] fms = { FaultModels.FM3_1, FaultModels.FM3_2 };
		
		List<LogicTreeBranch> branches = Lists.newArrayList(cfss.getBranches());
		Collections.shuffle(branches);
		
		// unique elements: mag, surface, rake
		// we use area to detect surface changes
		
		// first generate global rupture IDs
		System.out.println("Generating global IDs");
		// rup IDs
		Map<FaultModels, Map<Integer, Integer>> fmGlobalRupIDsMaps = Maps.newHashMap();
		Map<HashSet<String>, Integer> rupSectNamesToGlobalIDMap = Maps.newHashMap();
		int globalRupCount = 0;
		// sub sects
		Map<FaultModels, Map<Integer, Integer>> fmGlobalSectIDsMaps = Maps.newHashMap();
		Map<String, Integer> sectNamesToGlobalIDMap = Maps.newHashMap();
		Map<FaultModels, List<List<Integer>>> subSectIndexesMap = Maps.newHashMap();
		int globalSectCount = 0;
		for (FaultModels fm : fms) {
			FaultSystemRupSet rupSet = null;
			for (LogicTreeBranch branch : branches) {
				if (branch.getValue(FaultModels.class) == fm) {
					rupSet = cfss.getSolution(branch).getRupSet();
					break;
				}
			}
			
			// rups
			Map<Integer, Integer> globalRupIDsMap = Maps.newHashMap();
			fmGlobalRupIDsMaps.put(fm, globalRupIDsMap);
			for (int r=0; r<rupSet.getNumRuptures(); r++) {
				HashSet<String> sectNames = new HashSet<String>();
				for (FaultSectionPrefData sect : rupSet.getFaultSectionDataForRupture(r))
					sectNames.add(sect.getSectionName());
				Integer globalID = rupSectNamesToGlobalIDMap.get(sectNames);
				if (globalID == null)
					globalID = globalRupCount++;
				globalRupIDsMap.put(r, globalID);
			}
			System.out.println(fm.getShortName()+": globalRupCount="+globalRupCount);
			
			// sects
			List<List<Integer>> subSectIndexes = rupSet.getSectionIndicesForAllRups();
			subSectIndexesMap.put(fm, subSectIndexes);
			Map<Integer, Integer> globalSectIDsMap = Maps.newHashMap();
			fmGlobalSectIDsMaps.put(fm, globalSectIDsMap);
			for (int s=0; s<rupSet.getNumSections(); s++) {
				String sectName = rupSet.getFaultSectionData(s).getSectionName();
				Integer globalID = sectNamesToGlobalIDMap.get(sectName);
				if (globalID == null)
					globalID = globalSectCount++;
				globalSectIDsMap.put(s, globalID);
			}
			System.out.println(fm.getShortName()+": globalSectCount="+globalSectCount);
		}
		
		List<HashMap<UniqueRupture, UniqueRupture>> uniqueRupturesList = Lists.newArrayList();
		for (int i=0; i<globalRupCount; i++)
			uniqueRupturesList.add(new HashMap<UniqueRupture, UniqueRupture>());
		
		List<HashMap<UniqueSection, UniqueSection>> uniqueSectionsList = Lists.newArrayList();
		for (int i=0; i<globalSectCount; i++)
			uniqueSectionsList.add(new HashMap<UniqueSection, UniqueSection>());
		
		double totWeight = 0d;
		for (LogicTreeBranch branch : branches)
			totWeight += weightProvider.getWeight(branch);
		
		int branchCnt = 0;
		int uniqueRupCount = 0;
		int origNumRups = 0;
		int uniqueSectCount = 0;
		int origNumSects = 0;
		
		for (LogicTreeBranch branch : branches) {
			FaultModels fm = branch.getValue(FaultModels.class);
			Map<Integer, Integer> globalRupIDsMap = fmGlobalRupIDsMaps.get(fm);
			Map<Integer, Integer> globalSectIDsMap = fmGlobalSectIDsMaps.get(fm);

			if (branch.getValue(fm.getClass()) != fm)
				continue;

			double[] mags = cfss.getMags(branch);
			double[] rates = cfss.getRates(branch);
			double[] areas = cfss.loadDoubleArray(branch, "rup_areas.bin");
			double[] rakes = cfss.loadDoubleArray(branch, "rakes.bin");
			
			origNumRups += mags.length;
			origNumSects += fmGlobalSectIDsMaps.get(fm).size();

			double scaledWt = weightProvider.getWeight(branch) / totWeight;

			List<FaultSectionPrefData> fsd = null;
			
			boolean print = false;
			for (int r=0; r<mags.length; r++) {
				// TODO deal with isRuptureBelowSectMinMag!
				
				int globalRupID = globalRupIDsMap.get(r);
				HashMap<UniqueRupture, UniqueRupture> rupRates = uniqueRupturesList.get(globalRupID);

				UniqueRupture rup = new UniqueRupture(globalRupID, mags[r], rakes[r], areas[r]);
				double scaledRate = rates[r] * scaledWt;
				
				UniqueRupture matchedRup = rupRates.get(rup);
				if (matchedRup == null) {
					// new rupture
					
					// set fault section data
					List<Integer> subSectIndexes = subSectIndexesMap.get(fm).get(r);
					if (fsd == null)
						fsd = cfss.getSubSects(branch);
					List<UniqueSection> rupSects = Lists.newArrayList();
					for (int ind : subSectIndexes) {
						int globalSectID = globalSectIDsMap.get(ind);
						UniqueSection sect = new UniqueSection(fsd.get(ind), globalSectID);
						UniqueSection matchedSect = uniqueSectionsList.get(globalSectID).get(sect);
						if (matchedSect == null) {
							matchedSect = sect;
							uniqueSectionsList.get(globalSectID).put(sect, sect);
							uniqueSectCount++;
						}
						rupSects.add(matchedSect);
					}
					rup.sects = rupSects;
					
					if (uniqueRupCount % 100000 == 0)
						print = true;
					uniqueRupCount++;
					rupRates.put(rup, rup);
					matchedRup = rup;
				}
				// add my rate to the matched rate
				matchedRup.rate += scaledRate;
				matchedRup.cnt++;
			}
			branchCnt++;
			print = print || branchCnt % 10 == 0;
			if (print)
				System.out.println("unique rup count: "+uniqueRupCount
						+"; unique sect count: "+uniqueSectCount+"; branch count: "+branchCnt);
		}
		double keptPercent = 100d*(double)uniqueRupCount/(double)origNumRups;
		System.out.println("Ruptures kept: "+uniqueRupCount+"/"+origNumRups+" ("+(float)keptPercent+" %)");
		
		keptPercent = 100d*(double)uniqueSectCount/(double)origNumSects;
		System.out.println("Sections kept: "+uniqueSectCount+"/"+origNumSects+" ("+(float)keptPercent+" %)");

		double[] uniquesPerRup = new double[uniqueRupturesList.size()];
		for (int r=0; r<uniqueRupturesList.size(); r++)
			uniquesPerRup[r] = uniqueRupturesList.get(r).size();
		Arrays.sort(uniquesPerRup);

		System.out.println("uniques per rup:\tmin="+uniquesPerRup[0]+"\tmax="+uniquesPerRup[uniquesPerRup.length-1]
				+"\tmean="+StatUtils.mean(uniquesPerRup)+"\tmedian="+DataUtils.median_sorted(uniquesPerRup));
		
//		HistogramFunction hist = new HistogramFunction(1d, 40, 1d);
//		for (double uniques : uniquesPerRup)
//			hist.add(uniques, 1d);
//		new GraphWindow(hist, "Uniques Rups Per Rup");
		
		// now build the solution
		
		// first build FSD list
		int fsdIndex = 0;
		List<FaultSectionPrefData> faultSectionData = Lists.newArrayList();
		Map<UniqueSection, Integer> uniqueSectIndexMap = Maps.newHashMap();
		for (Map<UniqueSection, UniqueSection> uniqueSects : uniqueSectionsList) {
			int indexInSect = 0;
			
			for (UniqueSection sect : uniqueSects.keySet()) {
				FaultSectionPrefData fsd = sect.sect;
				fsd.setSectionId(fsdIndex);
				fsd.setSectionName(fsd.getSectionName()+" (instance "+(indexInSect++)+")");
				fsd.setAveRake(Double.NaN);
				fsd.setAveSlipRate(Double.NaN);
				faultSectionData.add(fsd);
				uniqueSectIndexMap.put(sect, fsdIndex);
				fsdIndex++;
			}
		}
		
		// now build rup list
		List<List<Integer>> sectionForRups = Lists.newArrayList();
		double[] mags = new double[uniqueRupCount];
		double[] rakes = new double[uniqueRupCount];
		double[] rupAreas = new double[uniqueRupCount];
		double[] rates = new double[uniqueRupCount];
		
		int rupIndex = 0;
		for (Map<UniqueRupture, UniqueRupture> uniqueRups : uniqueRupturesList) {
			for (UniqueRupture rup : uniqueRups.keySet()) {
				List<Integer> sects = Lists.newArrayList();
				for (UniqueSection sect : rup.sects)
					sects.add(uniqueSectIndexMap.get(sect));
				mags[rupIndex] = rup.mag;
				rakes[rupIndex] = rup.rake;
				rupAreas[rupIndex] = rup.area;
				rates[rupIndex] = rup.rate;
				sectionForRups.add(sects);
				
				rupIndex++;
			}
		}
		
		String info = "UCERF3 Mean Solution";
		
		FaultSystemRupSet rupSet = new FaultSystemRupSet(faultSectionData, null, null, null, sectionForRups,
				mags, rakes, rupAreas, null, info);
		InversionFaultSystemRupSet invRupSet = new InversionFaultSystemRupSet(
				rupSet, LogicTreeBranch.getMEAN_UCERF3(FaultModels.FM3_1), null, null, null, null, null);
		InversionFaultSystemSolution sol = new InversionFaultSystemSolution(invRupSet, rates);
		
		// load in branch averages and build average grid source provider
		List<File> branchAvgFiles = Lists.newArrayList(new File(invDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"),
				new File(invDir, "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_2_MEAN_BRANCH_AVG_SOL.zip"));
		sol.setGridSourceProvider(buildAvgGridSources(branchAvgFiles));
		
		String outputFileName = compoundFile.getName().replaceAll(".zip", "")+"_TRUE_HAZARD_MEAN_SOL.zip";
		File outputFile = new File(invDir, outputFileName);
		FaultSystemIO.writeSol(sol, outputFile);
	}
	
	private static GridSourceFileReader buildAvgGridSources(List<File> branchAvgFiles) throws IOException, DocumentException {
		List<GridSourceProvider> providers = Lists.newArrayList();
		for (File branchAvgFile : branchAvgFiles)
			providers.add(FaultSystemIO.loadInvSol(branchAvgFile).getGridSourceProvider());
		
		// FAULT MODELS ASSUMED TO HAVE EQUAL WEIGHT (currently true)
		double weight = 1d/(double)providers.size();
		
		GriddedRegion region = null;
		Map<Integer, IncrementalMagFreqDist> nodeSubSeisMFDs = null;
		Map<Integer, IncrementalMagFreqDist> nodeUnassociatedMFDs = null;
		
		for (GridSourceProvider prov : providers) {
			if (region == null) {
				region = prov.getGriddedRegion();
				nodeSubSeisMFDs = Maps.newHashMap();
				nodeUnassociatedMFDs = Maps.newHashMap();
			} else {
				Preconditions.checkState(region.equals(prov.getGriddedRegion()));
			}
			for (int index=0; index<region.getNodeCount(); index++) {
				addToMFD(nodeSubSeisMFDs, index, prov.getNodeSubSeisMFD(index), weight);
				addToMFD(nodeUnassociatedMFDs, index, prov.getNodeUnassociatedMFD(index), weight);
			}
		}
		
		GridSourceFileReader avg = new GridSourceFileReader(region, nodeSubSeisMFDs, nodeUnassociatedMFDs);
		return avg;
	}
	
	private static void addToMFD(Map<Integer, IncrementalMagFreqDist> mfds, int index, IncrementalMagFreqDist newMFD, double weight) {
		if (newMFD == null)
			return;
		IncrementalMagFreqDist mfd = mfds.get(index);
		if (mfd == null) {
			mfd = new IncrementalMagFreqDist(newMFD.getMinX(), newMFD.getNum(), newMFD.getDelta());
			mfds.put(index, mfd);
		} else {
			Preconditions.checkState((float)mfd.getMinX() == (float)newMFD.getMinX());
			Preconditions.checkState((float)mfd.getMaxX() == (float)newMFD.getMaxX());
			Preconditions.checkState(mfd.getNum() == newMFD.getNum());
		}
		for (int i=0; i<mfd.getNum(); i++) {
			mfd.add(i, newMFD.getY(i)*weight);
		}
	}

}
