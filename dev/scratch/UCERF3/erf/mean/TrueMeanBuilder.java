package scratch.UCERF3.erf.mean;

import java.awt.geom.Point2D;
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
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
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
import scratch.UCERF3.FaultSystemSolution;
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

/**
 * This class builds a "true mean" solution from the full UCERF3 logic tree.
 * 
 * <br>First all subsections are combined between the two fault models to avoid
 * overlap. Then each logic tree branch is loaded and ruptures that are identical
 * on all counts that affect hazard (surface, mag, rake) are combined into a single
 * rupture. Most subsections are identical in area across all branches and these are
 * also combined except for cases with varying aseismicity among branches.
 * 
 * <br>This all results in a FaultSystemSolution which contains the minimum set of
 * ruptures to exactly describe the full logic tree branch (gridded seismicity is
 * averaged). Hazard calculations with this solution, including averaged gridded
 * seismicity nail full logic tree hazard calculations almost exactly (likely just
 * rounding/averaging/precision errors causing the tiny discrepancies).
 * @author kevin
 *
 */
public class TrueMeanBuilder {
	
	/**
	 * Class that describes a unique rupture, used for combining. Mag's are stored
	 * in an MFD
	 * @author kevin
	 *
	 */
	private static class UniqueRupture {
		// part of unique checks
		private int id;
		private double rake;
		private double area;
		
		// not part of unique checks
		private DiscretizedFunc rupMFD;
		private List<UniqueSection> sects;
		private int cnt = 0;
		
		public UniqueRupture(int id, double rake, double area) {
			super();
			this.id = id;
			this.rake = rake;
			this.area = area;
			
			rupMFD = new ArbitrarilyDiscretizedFunc();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Float.floatToIntBits((float)area);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + id;
			temp = Float.floatToIntBits((float)rake);
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
			if (Float.floatToIntBits((float)area) != Float.floatToIntBits((float)other.area))
				return false;
			if (id != other.id)
				return false;
			if (Float.floatToIntBits((float)rake) != Float.floatToIntBits((float)other.rake))
				return false;
			return true;
		}
	}
	
	/**
	 * Class that describes a unique subsection as described by its id, dip,
	 * and upper/lower depths.
	 * 
	 * @author kevin
	 *
	 */
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
			temp = Float.floatToIntBits((float)aveDip);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Float.floatToIntBits((float)aveLowerDepth);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Float.floatToIntBits((float)aveUpperDepth);
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
			if (Float.floatToIntBits((float)aveDip) != Float.floatToIntBits((float)other.aveDip))
				return false;
			if (Float.floatToIntBits((float)aveLowerDepth) != Float.floatToIntBits((float)other.aveLowerDepth))
				return false;
			if (Float.floatToIntBits((float)aveUpperDepth) != Float.floatToIntBits((float)other.aveUpperDepth))
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
//		FaultModels[] fms = { FaultModels.FM3_1, FaultModels.FM3_2 };
//		String nameAdd = "";
//		FaultModels[] fms = { FaultModels.FM3_1};
//		String nameAdd = "_FM3_1";
		FaultModels[] fms = { FaultModels.FM3_2 };
		String nameAdd = "_FM3_2";
		HashSet<FaultModels> fmSet = new HashSet<FaultModels>();
		for (FaultModels fm : fms)
			fmSet.add(fm);
		
		List<LogicTreeBranch> branches = Lists.newArrayList(cfss.getBranches());
		
		// remove any branches for excluded fault models
		for (int i=branches.size(); --i>=0;) {
			LogicTreeBranch branch = branches.get(i);
			FaultModels fm = branch.getValue(FaultModels.class);
			if (!fmSet.contains(fm))
				branches.remove(i);
		}
		
		Collections.shuffle(branches);
		
		// unique elements: mag, surface, rake
		// we use area to detect surface changes
		
		// first generate global rupture IDs
		// this combines both Fault Models into a single model. each rupture
		// and subsection is assigned a new "global ID"
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
				if (globalID == null) {
					globalID = globalRupCount++;
					rupSectNamesToGlobalIDMap.put(sectNames, globalID);
				}
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
				if (globalID == null) {
					globalID = globalSectCount++;
					sectNamesToGlobalIDMap.put(sectName, globalID);
				}
				globalSectIDsMap.put(s, globalID);
			}
			System.out.println(fm.getShortName()+": globalSectCount="+globalSectCount);
		}
		
		// these store UniqueRupture instances for each new global rup ID
		List<HashMap<UniqueRupture, UniqueRupture>> uniqueRupturesList = Lists.newArrayList();
		for (int i=0; i<globalRupCount; i++)
			uniqueRupturesList.add(new HashMap<UniqueRupture, UniqueRupture>());
		
		// same for subsection IDs
		List<HashMap<UniqueSection, UniqueSection>> uniqueSectionsList = Lists.newArrayList();
		for (int i=0; i<globalSectCount; i++)
			uniqueSectionsList.add(new HashMap<UniqueSection, UniqueSection>());
		
		// find the total branch weight for the tree
		double totWeight = 0d;
		for (LogicTreeBranch branch : branches)
			totWeight += weightProvider.getWeight(branch);
		
		// counts for progress tracking
		int branchCnt = 0;
		int uniqueRupCount = 0;
		int origNumRups = 0;
		int uniqueSectCount = 0;
		int origNumSects = 0;
		int rupSetCount = 0;
		int lastChangedBranch = 0;
		
		// used for sanity checks
		double origTotalRate = 0;
		double mfdMin = 5d;
		double mfdDelta = 0.01;
		int mfdNum = 500;
		IncrementalMagFreqDist origAvgMFD = new IncrementalMagFreqDist(mfdMin, mfdNum, mfdDelta);
		origAvgMFD.setTolerance(mfdDelta);
		
		for (LogicTreeBranch branch : branches) {
			FaultModels fm = branch.getValue(FaultModels.class);
			// mapping from FM IDs to global IDs
			Map<Integer, Integer> globalRupIDsMap = fmGlobalRupIDsMaps.get(fm);
			Map<Integer, Integer> globalSectIDsMap = fmGlobalSectIDsMaps.get(fm);

			// loading things this way is more efficient than loading the whole solution in
			// we only do that if needed.
			double[] mags = cfss.getMags(branch);
			double[] rates = cfss.getRates(branch);
			double[] areas = cfss.loadDoubleArray(branch, "rup_areas.bin");
			double[] rakes = cfss.loadDoubleArray(branch, "rakes.bin");
			
			origNumRups += mags.length;
			origNumSects += fmGlobalSectIDsMaps.get(fm).size();

			double scaledWt = weightProvider.getWeight(branch) / totWeight;

			List<FaultSectionPrefData> fsd = null;
			InversionFaultSystemRupSet rupSet = null;
			
			boolean print = false;
			for (int r=0; r<mags.length; r++) {
				int globalRupID = globalRupIDsMap.get(r);
				HashMap<UniqueRupture, UniqueRupture> rupRates = uniqueRupturesList.get(globalRupID);

				UniqueRupture rup = new UniqueRupture(globalRupID, rakes[r], areas[r]);
				double scaledRate = rates[r] * scaledWt;
				
				// see if we already have a matching rupture
				UniqueRupture matchedRup = rupRates.get(rup);
				if (matchedRup == null) {
					// this is a new rupture (either first for this global ID, or has a property
					// change such as rake/area
					
					// see if we're done and it's just belowSectMinMag rups
					// if we haven't added anything for a ton of branches then assume done
					int numSinceChanged = branchCnt - lastChangedBranch;
					if (numSinceChanged > 150 || (numSinceChanged > 10 && uniqueRupCount == 1634466))
						continue;
					// FM3.1
					if (fms.length == 1 && numSinceChanged > 10 && uniqueRupCount == 930563)
						continue;
					// FM3.2
					if (fms.length == 1 && numSinceChanged > 10 && uniqueRupCount == 1128358)
						continue;
					
					// set fault section data
					List<Integer> subSectIndexes = subSectIndexesMap.get(fm).get(r);
					if (rupSet == null) {
						// we need to load the rupSet
						rupSet = cfss.getSolution(branch).getRupSet();
						fsd = rupSet.getFaultSectionDataList();
						rupSetCount++;
					}
					// check if it's below sect min mag (and should be skipped)
					if (rupSet.isRuptureBelowSectMinMag(r))
						continue;
					List<UniqueSection> rupSects = Lists.newArrayList();
					for (int ind : subSectIndexes) {
						// get UniqueSection instances for each subsection
						// this will add new UniqueSections to the list if there are upper depth
						// changes
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
					lastChangedBranch = branchCnt;
					rupRates.put(rup, rup);
					matchedRup = rup;
				}
				// add my rate to the matched rate
				double mag = mags[r];
				int index = matchedRup.rupMFD.getXIndex(mag);
				if (index >= 0)
					matchedRup.rupMFD.set(index, matchedRup.rupMFD.getY(index)+scaledRate);
				else
					matchedRup.rupMFD.set(mag, scaledRate);
				matchedRup.cnt++;
				
				// sanity checks
				origTotalRate += scaledWt*rates[r];
				origAvgMFD.add(mags[r], rates[r]*scaledWt);
			}
			branchCnt++;
			print = print || branchCnt % 10 == 0;
			if (print)
				System.out.println("unique rup count: "+uniqueRupCount
						+"; unique sect count: "+uniqueSectCount+"; branch count: "+branchCnt
						+"; loaded rupSet count: "+rupSetCount);
		}
		// metrics
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
		// IDs are not sorted and are somewhat arbitrary
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
		DiscretizedFunc[] mfds = new DiscretizedFunc[uniqueRupCount];
		
		IncrementalMagFreqDist newMFD = new IncrementalMagFreqDist(mfdMin, mfdNum, mfdDelta);
		newMFD.setTolerance(mfdDelta);
		
		int rupIndex = 0;
		for (Map<UniqueRupture, UniqueRupture> uniqueRups : uniqueRupturesList) {
			for (UniqueRupture rup : uniqueRups.keySet()) {
				List<Integer> sects = Lists.newArrayList();
				for (UniqueSection sect : rup.sects)
					sects.add(uniqueSectIndexMap.get(sect));
				rakes[rupIndex] = rup.rake;
				rupAreas[rupIndex] = rup.area;
				sectionForRups.add(sects);
				
				DiscretizedFunc mfd = rup.rupMFD;
				double totRate = 0;
				double runningMag = 0;
				for (Point2D pt : mfd) {
					totRate += pt.getY();
					runningMag += pt.getX()*pt.getY();
					newMFD.add(pt.getX(), pt.getY());
				}
				rates[rupIndex] = totRate;
				mags[rupIndex] = runningMag/totRate;
				mfds[rupIndex] = mfd;
				
				rupIndex++;
			}
		}
		
		double newTotRate = StatUtils.sum(rates);
		
		// make sure we didn't screw anything up
		checkEqual(origTotalRate, newTotRate, "Rates");
		for (int i=0; i<mfdNum; i++)
			checkEqual(origAvgMFD.getY(i), newMFD.getY(i), "MFD pt "+i+", mag="+origAvgMFD.getX(i));
		
		// now get total rate from MFDs
		newTotRate = 0;
		for (DiscretizedFunc mfd : mfds)
			for (Point2D pt : mfd)
				newTotRate += pt.getY();
		
		// check again
		checkEqual(origTotalRate, newTotRate, "MFD Rates");
		
		String info = "UCERF3 Mean Solution";
		
		// assemble rupSet/solution
		FaultSystemRupSet rupSet = new FaultSystemRupSet(faultSectionData, null, null, null, sectionForRups,
				mags, rakes, rupAreas, null, info);
		FaultSystemSolution sol = new FaultSystemSolution(rupSet, rates);
		
		// load in branch averages and build average grid source provider
		Map<FaultModels, File> branchAvgFiles = Maps.newHashMap();
		branchAvgFiles.put(FaultModels.FM3_1, new File(invDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		branchAvgFiles.put(FaultModels.FM3_2, new File(invDir,
				"2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_2_MEAN_BRANCH_AVG_SOL.zip"));
		sol.setGridSourceProvider(buildAvgGridSources(branchAvgFiles, fms));
		sol.setRupMagDists(mfds);
		
		String outputFileName = compoundFile.getName().replaceAll(".zip", "")+nameAdd+"_TRUE_HAZARD_MEAN_SOL.zip";
		File outputFile = new File(invDir, outputFileName);
		FaultSystemIO.writeSol(sol, outputFile);
	}
	
	private static void checkEqual(double origVal, double newVal, String description) {
		double pDiff = DataUtils.getPercentDiff(newVal, origVal);
//		System.out.println(description+":\tpDiff="+pDiff+" %\torig="+origVal+"\tnew="+newVal);
		// check within 0.0001%
		Preconditions.checkState(pDiff < 0.0001, description+": "+origVal+" != "+newVal+" (pDiff="+pDiff+" %)");
	}
	
	private static GridSourceFileReader buildAvgGridSources(Map<FaultModels, File> branchAvgFiles, FaultModels[] fms)
			throws IOException, DocumentException {
		List<GridSourceProvider> providers = Lists.newArrayList();
		for (FaultModels fm : fms)
			providers.add(FaultSystemIO.loadInvSol(branchAvgFiles.get(fm)).getGridSourceProvider());
		
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
