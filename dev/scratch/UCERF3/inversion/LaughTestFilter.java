package scratch.UCERF3.inversion;

import scratch.UCERF3.inversion.coulomb.CoulombRatesFilter;
import scratch.UCERF3.inversion.coulomb.CoulombRatesFilter.TestType;

public class LaughTestFilter {
	
	private double maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff,
	maxCmlJumpDist, maxCmlRakeChange, maxCmlAzimuthChange;
	private int minNumSectInRup;
	private CoulombRatesFilter coulombFilter;
	
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
		double maxCumJumpDist = 5d;
		double maxTotAzimuthChange = 60d;
		double maxRakeDiff = Double.POSITIVE_INFINITY;
		int minNumSectInRup = 2;
		double maxCmlRakeChange = 180;
		double maxCmlAzimuthChange = 560;
		double minAverageProb = 0.1;
		double minIndividualProb = 0.1;
		double minimumStressExclusionCeiling = 1.5d;
		boolean applyBranchesOnly = true; // if true the coulomb filter will only be applied at branch points
//		double minimumStressExclusionCeiling = Double.POSITIVE_INFINITY;
		
		CoulombRatesFilter coulombFilter = new CoulombRatesFilter(
				TestType.COULOMB_STRESS, minAverageProb, minIndividualProb,
				minimumStressExclusionCeiling, applyBranchesOnly);
		
		return new LaughTestFilter(maxJumpDist, maxAzimuthChange, maxTotAzimuthChange, maxRakeDiff, maxCumJumpDist,
				maxCmlRakeChange, maxCmlAzimuthChange, minNumSectInRup, coulombFilter);
	}
	
	public LaughTestFilter(double maxJumpDist, double maxAzimuthChange,
			double maxTotAzimuthChange, double maxRakeDiff,
			double maxCumJumpDist, double maxCmlRakeChange,
			double maxCmlAzimuthChange, int minNumSectInRup,
			CoulombRatesFilter coulombFilter) {
		this.maxJumpDist = maxJumpDist;
		this.maxAzimuthChange = maxAzimuthChange;
		this.maxTotAzimuthChange = maxTotAzimuthChange;
		this.maxRakeDiff = maxRakeDiff;
		this.maxCmlJumpDist = maxCumJumpDist;
		this.maxCmlRakeChange = maxCmlRakeChange;
		this.maxCmlAzimuthChange = maxCmlAzimuthChange;
		this.minNumSectInRup = minNumSectInRup;
		this.coulombFilter = coulombFilter;
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

	public double getMaxRakeDiff() {
		return maxRakeDiff;
	}

	public void setMaxRakeDiff(double maxRakeDiff) {
		this.maxRakeDiff = maxRakeDiff;
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

	public CoulombRatesFilter getCoulombFilter() {
		return coulombFilter;
	}

	public void setCoulombFilter(CoulombRatesFilter coulombFilter) {
		this.coulombFilter = coulombFilter;
	}

	@Override
	public String toString() {
		return "LaughTestFilter [maxJumpDist=" + maxJumpDist
				+ ", maxAzimuthChange=" + maxAzimuthChange
				+ ", maxTotAzimuthChange=" + maxTotAzimuthChange
				+ ", maxRakeDiff=" + maxRakeDiff + ", maxCumJumpDist=" + maxCmlJumpDist
				+ ", maxCmlRakeChange=" + maxCmlRakeChange
				+ ", maxCmlAzimuthChange=" + maxCmlAzimuthChange
				+ ", minNumSectInRup=" + minNumSectInRup
				+ ", coulombFilter=" + coulombFilter+ "]";
	}

}
