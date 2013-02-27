package scratch.UCERF3.inversion.laughTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.collect.Lists;

import scratch.UCERF3.inversion.SectionCluster;
import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.coulomb.CoulombRatesTester;
import scratch.UCERF3.inversion.coulomb.CoulombRatesTester.TestType;
import scratch.UCERF3.utils.IDPairing;

public class LaughTestFilter {
	
	private double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange,
	maxCmlJumpDist, maxCmlRakeChange, maxCmlAzimuthChange;
	private int minNumSectInRup;
	private CoulombRatesTester coulombFilter;
	private HashSet<Integer> parentSectsToIgnore;
	private boolean allowSingleSectDuringJumps;
	
	public static boolean USE_BUGGY_COULOMB = false;
	
	/**
	 * This returns the current default laugh test filter
	 * 
	 * @return
	 */
	public static LaughTestFilter getDefault() {
		// original laugh test filter
//		double maxAzimuthChange = 90;
//		double maxJumpDist = 5d;
//		double maxCumJumpDist = 10d;
//		double maxTotAzimuthChange = 90d;
//		double maxRakeDiff = Double.POSITIVE_INFINITY;
//		int minNumSectInRup = 2;
//		double maxCmlRakeChange = 360;
//		double maxCmlAzimuthChange = 540;
//		double minAverageProb = 0.1;
//		double minIndividualProb = 0.05;
//		double minimumStressExclusionCeiling = 1d;
		

		double maxAzimuthChange = 60;
		double maxJumpDist = 5d;
		double maxCumJumpDist = Double.POSITIVE_INFINITY;
		double maxTotAzimuthChange = 60d;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 180;
		double maxCmlAzimuthChange = 560;
		applyUCERF3p2Bugs();
		boolean allowSingleSectDuringJumps = false; // TODO CHANGE FOR UCERF3.3
		double minAverageProb = 0.1;
		double minIndividualProb = 0.1;
		double minimumStressExclusionCeiling = 1.5d;
		boolean applyBranchesOnly = true; // if true the coulomb filter will only be applied at branch points
//		double minimumStressExclusionCeiling = Double.POSITIVE_INFINITY;
		
		CoulombRatesTester coulombFilter = new CoulombRatesTester(
				TestType.COULOMB_STRESS, minAverageProb, minIndividualProb,
				minimumStressExclusionCeiling, applyBranchesOnly);
		
		return new LaughTestFilter(maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxCumJumpDist,
				maxCmlRakeChange, maxCmlAzimuthChange, minNumSectInRup, allowSingleSectDuringJumps, coulombFilter);
	}
	
	public static void applyUCERF3p2Bugs() {
		System.err.println("*** WARNING ***");
		System.err.println("UCERF3.2 and before laugh test bugs have been enabled for " +
				"backwards compatibility. This should be disabled before future production runs!");
		System.err.println("*** WARNING ***");
		
		USE_BUGGY_COULOMB = true;
		CoulombRatesTester.BUGGY_MIN_STRESS = true;
		CumulativeAzimuthChangeFilter.USE_BUGGY_AZ_CHANGE = true;
		AzimuthChangeFilter.INCLUDE_OWL_LAKE = false;
	}
	
	public LaughTestFilter(double maxJumpDist, double maxAzimuthChange,
			double maxTotAzimuthChange,
			double maxCumJumpDist, double maxCmlRakeChange,
			double maxCmlAzimuthChange, int minNumSectInRup, boolean allowSingleSectDuringJumps,
			CoulombRatesTester coulombFilter) {
		this.maxJumpDist = maxJumpDist;
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
		this.maxCmlJumpDist = maxCumJumpDist;
		this.maxCmlRakeChange = maxCmlRakeChange;
		this.maxCmlAzimuthChange = maxCmlAzimuthChange;
		this.minNumSectInRup = minNumSectInRup;
		this.allowSingleSectDuringJumps = allowSingleSectDuringJumps;
		this.coulombFilter = coulombFilter;
	}
	
	public List<AbstractLaughTest> buildLaughTests(
			Map<IDPairing, Double> azimuths,
			Map<IDPairing, Double> distances,
			Map<Integer, Double> rakesMap,
			CoulombRates coulombRates,
			boolean applyGarlockPintoMtnFix,
			List<List<Integer>> sectionConnectionsListList,
			List<FaultSectionPrefData> subSectData) {
		List<AbstractLaughTest> tests = Lists.newArrayList();
		
		if (minNumSectInRup > 0) {
			tests.add(new MinSectsPerParentFilter.ContinualFilter(minNumSectInRup));
			tests.add(new MinSectsPerParentFilter.CleanupFilter(minNumSectInRup,
					allowSingleSectDuringJumps, sectionConnectionsListList, subSectData));
		}
		
		tests.add(new AzimuthChangeFilter(maxAzimuthChange, maxTotAzimuthChange,
				applyGarlockPintoMtnFix, azimuths));
		
		if (!isNaNInfinite(maxCmlJumpDist))
			tests.add(new CumulativeJumpDistFilter(distances, maxCmlJumpDist));
		
		if (!isNaNInfinite(maxCmlRakeChange))
			tests.add(new CumulativeRakeChangeFilter(rakesMap, maxCmlRakeChange));
		
		if (!isNaNInfinite(maxCmlAzimuthChange))
			tests.add(new CumulativeAzimuthChangeFilter(azimuths, maxCmlAzimuthChange));
		
		if (coulombFilter != null) {
			if (USE_BUGGY_COULOMB)
				tests.add(new BuggyCoulombFilter(coulombRates, coulombFilter,
						subSectData, sectionConnectionsListList));
			else
				tests.add(new CoulombFilter(coulombRates, coulombFilter));
		}
		
		return tests;
	}
	
	private static boolean isNaNInfinite(double val) {
		if (Double.isNaN(val))
			return true;
		if (Double.isInfinite(val))
			return true;
		if (val == Double.MAX_VALUE)
			return true;
		return false;
	}

	public double getMaxJumpDist() {
		return maxJumpDist;
	}

	public void setMaxJumpDist(double maxJumpDist) {
		this.maxJumpDist = maxJumpDist;
	}

	public double getMaxAzimuthChange() {
		return maxAzimuthChange;
	}

	public void setMaxAzimuthChange(double maxAzimuthChange) {
		this.maxAzimuthChange = maxAzimuthChange;
	}

	public double getMaxTotAzimuthChange() {
		return maxTotAzimuthChange;
	}

	public void setMaxTotAzimuthChange(double maxTotAzimuthChange) {
		this.maxTotAzimuthChange = maxTotAzimuthChange;
	}

	public double getMaxCmlJumpDist() {
		return maxCmlJumpDist;
	}

	public void setMaxCmlmJumpDist(double maxCumJumpDist) {
		this.maxCmlJumpDist = maxCumJumpDist;
	}

	public double getMaxCmlRakeChange() {
		return maxCmlRakeChange;
	}

	public void setMaxCmlRakeChange(double maxCmlRakeChange) {
		this.maxCmlRakeChange = maxCmlRakeChange;
	}

	public double getMaxCmlAzimuthChange() {
		return maxCmlAzimuthChange;
	}

	public void setMaxCmlAzimuthChange(double maxCmlAzimuthChange) {
		this.maxCmlAzimuthChange = maxCmlAzimuthChange;
	}

	public int getMinNumSectInRup() {
		return minNumSectInRup;
	}

	public void setMinNumSectInRup(int minNumSectInRup) {
		this.minNumSectInRup = minNumSectInRup;
	}

	public boolean isAllowSingleSectDuringJumps() {
		return allowSingleSectDuringJumps;
	}

	public void setAllowSingleSectDuringJumps(boolean allowSingleSectDuringJumps) {
		this.allowSingleSectDuringJumps = allowSingleSectDuringJumps;
	}

	public CoulombRatesTester getCoulombFilter() {
		return coulombFilter;
	}

	public void setCoulombFilter(CoulombRatesTester coulombFilter) {
		this.coulombFilter = coulombFilter;
	}

	public HashSet<Integer> getParentSectsToIgnore() {
		return parentSectsToIgnore;
	}

	public void setParentSectsToIgnore(HashSet<Integer> parentSectsToIgnore) {
		this.parentSectsToIgnore = parentSectsToIgnore;
	}

	@Override
	public String toString() {
		return "LaughTestFilter [maxJumpDist=" + maxJumpDist
				+ ", maxAzimuthChange=" + maxAzimuthChange
				+ ", maxTotAzimuthChange=" + maxTotAzimuthChange
				+ ", maxCumJumpDist=" + maxCmlJumpDist
				+ ", maxCmlRakeChange=" + maxCmlRakeChange
				+ ", maxCmlAzimuthChange=" + maxCmlAzimuthChange
				+ ", minNumSectInRup=" + minNumSectInRup
				+ ", coulombFilter=" + coulombFilter+ "]";
	}

}
