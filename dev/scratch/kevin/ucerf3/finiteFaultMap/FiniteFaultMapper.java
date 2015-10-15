package scratch.kevin.ucerf3.finiteFaultMap;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.parsers.UCERF3_CatalogParser;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.RuptureSurface;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.kevin.ucerf3.finiteFaultMap.JeanneFileLoader.LocComparator;

public class FiniteFaultMapper {
	
	private static final boolean D = true;
	
	private FaultSystemRupSet rupSet;
	
	private double maxLengthDiff = 75d;
	private double maxCenterDist = 75d;
	private double maxAnyDist = 50d;
	private int numSurfLocsToCheck = 500;
	
	private double surfSpacing = 5d;
	
	private RuptureSurface[] surfs;
	private Location[] centers;
	private double[] lengths;
	
	public FiniteFaultMapper(FaultSystemRupSet rupSet) {
		this.rupSet = rupSet;
		surfs = new RuptureSurface[rupSet.getNumRuptures()];
		centers = new Location[rupSet.getNumRuptures()];
		lengths = new double[rupSet.getNumRuptures()];
		
		for (int i=0; i<rupSet.getNumRuptures(); i++) {
			lengths[i] = rupSet.getLengthForRup(i)/1000d; // convert to km
			surfs[i] = rupSet.getSurfaceForRupupture(i, surfSpacing, false);
			centers[i] = calcCenter(surfs[i]);
		}
	}
	
	private static Location calcCenter(RuptureSurface surf) {
		double lat = 0;
		double lon = 0;
		double depth = 0;
		int num = 0;
		for (Location loc : surf.getEvenlyDiscritizedListOfLocsOnSurface()) {
			lat += loc.getLatitude();
			lon += loc.getLongitude();
			depth += loc.getDepth();
			num++;
		}
		lat /= num;
		lon /= num;
		depth /= num;
		
		return new Location(lat, lon, depth);
	}
	
	public int getMappedRup(ObsEqkRupture rup) {
		Stopwatch watch = null;
		if (D) watch = Stopwatch.createStarted();
		RuptureSurface surf = rup.getRuptureSurface();
		Preconditions.checkNotNull(surf);
		double length = surf.getAveLength();
		Location center = calcCenter(surf);
		
		if (D) System.out.println("Loading cadidate for "+JeanneFileLoader.getRupStr(rup)+". Len="+length+". Center: "+center);
		
		List<Integer> candidates = Lists.newArrayList();
		for (int i=0; i<rupSet.getNumRuptures(); i++) {
			// check length within possible range
			double lengthDiff = Math.abs(lengths[i] - length);
			if (lengthDiff > maxLengthDiff)
				continue;
			// check center distance
			double hDist = LocationUtils.horzDistance(center, centers[i]);
			if (hDist > maxCenterDist)
				continue;
			candidates.add(i);
		}
		
		if (D) System.out.println("Found "+candidates.size()+" candidates");
		
		List<Location> surfLocs = Lists.newArrayList();
		if (surf instanceof EvenlyGriddedSurface) {
			EvenlyGriddedSurface gridSurf = (EvenlyGriddedSurface)surf;
			for (int col=0; col<gridSurf.getNumCols(); col++)
				for (int row=0; row<gridSurf.getNumRows(); row++)
					surfLocs.add(gridSurf.get(row, col));
		} else {
			Location loc1 = surf.getFirstLocOnUpperEdge();
			Location loc2 = surf.getFirstLocOnUpperEdge();
			double latDelta = Math.abs(loc1.getLatitude() - loc2.getLatitude());
			double lonDelta = Math.abs(loc1.getLongitude() - loc2.getLongitude());
			LocComparator comp = new LocComparator(latDelta > lonDelta);
			surfLocs.addAll(surf.getEvenlyDiscritizedListOfLocsOnSurface());
			Collections.sort(surfLocs, comp);
		}
		List<Location> surfLocsToCheck;
		if (numSurfLocsToCheck >= surfLocs.size()) {
			surfLocsToCheck = surfLocs;
		} else {
			surfLocsToCheck = Lists.newArrayList();
			int mod = (int)((double)surfLocs.size()/(double)numSurfLocsToCheck);
			for (int i=0; i<surfLocs.size(); i++)
				if (i % mod == 0)
					surfLocsToCheck.add(surfLocs.get(i));
		}
		
		if (D) System.out.println("Checking distance of "+surfLocsToCheck.size()+"/"+surfLocs.size()+" surf pts");
		
		List<Double> means = Lists.newArrayList();
		List<Double> medians = Lists.newArrayList();
		List<double[]> allDists = Lists.newArrayList();
		List<Integer> sortIndexes = Lists.newArrayList(); // will be used to map back to candidate index
		
		candidateLoop:
		for (int i=0; i<candidates.size(); i++) {
			int rupIndex = candidates.get(i);
			double[] distances = new double[surfLocsToCheck.size()];
			for (int j=0; j<surfLocsToCheck.size(); j++) {
				distances[j] = surfs[rupIndex].getDistanceRup(surfLocsToCheck.get(j));
				if (distances[j] > maxAnyDist) {
					means.add(Double.POSITIVE_INFINITY);
					medians.add(Double.POSITIVE_INFINITY);
					allDists.add(null);
					sortIndexes.add(i);
					continue candidateLoop;
				}
			}
			means.add(StatUtils.mean(distances));
			medians.add(DataUtils.median(distances));
			allDists.add(distances);
			sortIndexes.add(i);
		}
		
		List<ComparablePairing<Double, Integer>> pairings = ComparablePairing.build(means, sortIndexes);
		Collections.sort(pairings);
		
		for (int i=0; i<50 && i<pairings.size() && D; i++) {
			ComparablePairing<Double, Integer> pairing = pairings.get(i);
			if (Double.isInfinite(pairing.getComparable()))
				 break;
			int index = pairing.getData();
			double mean = means.get(index);
			double median = medians.get(index);
			double[] dists = allDists.get(index);
			double min = StatUtils.min(dists);
			double max = StatUtils.max(dists);
			int rupIndex = candidates.get(index);
			
			
			List<String> parents = Lists.newArrayList();
			
			for (FaultSectionPrefData sect : rupSet.getFaultSectionDataForRupture(rupIndex)) {
				String parentName = sect.getParentSectionName();
				if (parents.isEmpty() || !parents.get(parents.size()-1).equals(parentName))
					parents.add(parentName);
			}
			
			String parStr = Joiner.on("; ").join(parents);
			
			System.out.println("Match "+i+". Mean="+(float)mean+". Median="+(float)median
					+". Range=["+(float)min+" "+(float)max+"]. Mag="+rupSet.getMagForRup(rupIndex)
					+". Len="+lengths[rupIndex]+". Center: "+centers[rupIndex]+". Parents: "+parStr);
		}
		
		if (D) {
			watch.stop();
			System.out.println("Took "+watch.elapsed(TimeUnit.SECONDS)+" seconds to find match");
		}
		
		if (pairings.isEmpty())
			return -1;
		
		ComparablePairing<Double, Integer> pairing = pairings.get(0);
		
		if (pairing.getComparable().isInfinite())
			return -1;
		
		int rupIndex = candidates.get(pairing.getData());
		
		return rupIndex;
	}

	public static void main(String[] args) throws IOException, DocumentException {
		File finiteFile = new File("/home/kevin/OpenSHA/UCERF3/historical_finite_fault_mapping/UCERF3_finite.dat");
		ObsEqkRupList inputRups = UCERF3_CatalogParser.loadCatalog(
				new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/ofr2013-1165_EarthquakeCat.txt"));
		for (ObsEqkRupture rup : inputRups) {
			if (rup.getHypocenterLocation().getDepth() > 24 && rup.getMag() >= 4)
				System.out.println(rup.getHypocenterLocation()+", mag="+rup.getMag());
		}
		System.exit(0);
		List<ObsEqkRupture> finiteRups = JeanneFileLoader.loadFiniteRups(finiteFile, inputRups);
		finiteRups = finiteRups.subList(0, 1);
		System.out.println("Loaded "+finiteRups.size()+" finite rups");
		
		FaultSystemRupSet rupSet = FaultSystemIO.loadRupSet(new File("/home/kevin/workspace/OpenSHA/dev/scratch/"
				+ "UCERF3/data/scratch/InversionSolutions/2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_"
				+ "FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		
		FiniteFaultMapper mapper = new FiniteFaultMapper(rupSet);
		
		for (ObsEqkRupture rup : finiteRups) {
			mapper.getMappedRup(rup);
		}
	}

}
