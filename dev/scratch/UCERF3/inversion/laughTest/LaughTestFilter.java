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
	
	private List<AbstractLaughTest> laughTests;
	
	private boolean ucerf3p2Filter = false;
	
	/**
	 * This returns the current default laugh test filter
	 * 
	 * @return
	 */
	public static LaughTestFilter getUCERF3p2Filter() {
		System.err.println("*** WARNING ***");
		System.err.println("UCERF3.2 and before laugh test bugs have been enabled for " +
				"backwards compatibility. This should be disabled before future production runs!");
		System.err.println("*** WARNING ***");
		double maxAzimuthChange = 60;
		double maxJumpDist = 5d;
		double maxCumJumpDist = Double.POSITIVE_INFINITY;
		double maxTotAzimuthChange = 60d;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 180;
		double maxCmlAzimuthChange = 560;
		boolean allowSingleSectDuringJumps = false; // TODO CHANGE FOR UCERF3.3
		double minAverageProb = 0.1;
		double minIndividualProb = 0.1;
		double minimumStressExclusionCeiling = 1.5d;
		// if true the coulomb filter will only be applied at branch points
		boolean applyBranchesOnly = true;
		boolean allowAnyWay = false;
		
		CoulombRatesTester coulombFilter = new CoulombRatesTester(
				TestType.COULOMB_STRESS, minAverageProb, minIndividualProb,
				minimumStressExclusionCeiling, applyBranchesOnly, allowAnyWay);
		coulombFilter.setBuggyMinStress(true);
		
		LaughTestFilter filter =  new LaughTestFilter(maxJumpDist, maxAzimuthChange,
				maxTotAzimuthChange, maxCumJumpDist, maxCmlRakeChange, maxCmlAzimuthChange,
				minNumSectInRup, allowSingleSectDuringJumps, coulombFilter);
		filter.ucerf3p2Filter = true;
		return filter;
	}
	
	/**
	 * This returns the current default laugh test filter
	 * 
	 * @return
	 */
	public static LaughTestFilter getDefault() {
		double maxAzimuthChange = 60;
		double maxJumpDist = 5d;
		double maxCumJumpDist = Double.POSITIVE_INFINITY;
		double maxTotAzimuthChange = 60d;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 180;
		double maxCmlAzimuthChange = 560;
		boolean allowSingleSectDuringJumps = true;
		double minAverageProb = 0.05;
		double minIndividualProb = 0.05;
		double minimumStressExclusionCeiling = 1.25d;
		// if true the coulomb filter will only be applied at branch points
		boolean applyBranchesOnly = true;
		boolean allowAnyWay = true;
		
		CoulombRatesTester coulombFilter = new CoulombRatesTester(
				TestType.COULOMB_STRESS, minAverageProb, minIndividualProb,
				minimumStressExclusionCeiling, applyBranchesOnly, allowAnyWay);
		
		return new LaughTestFilter(maxJumpDist, maxAzimuthChange, maxTotAzimuthChange,
				maxCumJumpDist, maxCmlRakeChange, maxCmlAzimuthChange, minNumSectInRup,
				allowSingleSectDuringJumps, coulombFilter);
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
	
	public synchronized List<AbstractLaughTest> buildLaughTests(
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
			if (ucerf3p2Filter)
				tests.add(new BuggyCoulombFilter(coulombRates, coulombFilter,
						subSectData, sectionConnectionsListList));
			else
				tests.add(new CoulombFilter(coulombRates, coulombFilter));
		}
		
		this.laughTests = tests;
		
		if (ucerf3p2Filter) {
			getLaughTest(AzimuthChangeFilter.class).setUCERF3p2LL_List();
			if (!isNaNInfinite(maxCmlRakeChange))
				getLaughTest(CumulativeAzimuthChangeFilter.class).setBuggyAzChange(true);
		}
		
		return tests;
	}
	
	/**
	 * 
	 * @return list of laugh tests, or null if not yet built
	 */
	public List<AbstractLaughTest> getLaughTests() {
		return laughTests;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return laugh test of the specified class, or null of no such test exists or laugh tests
	 * not yet built.
	 */
	@SuppressWarnings("unchecked")
	public <E extends AbstractLaughTest> E getLaughTest(Class<E> clazz) {
		if (laughTests == null)
			return null;
		for (AbstractLaughTest test : laughTests) {
			if (clazz.isInstance(test))
				return (E)test;
		}
		return null;
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