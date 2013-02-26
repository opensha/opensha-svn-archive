package scratch.UCERF3.inversion.coulomb;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import scratch.UCERF3.utils.IDPairing;

/**
 * Laugh test filter based on Coulomb Rates
 * 
 * Based on the following e-mail from Tom Parsons 2/28/12:
 * 
 * For example: for a rupture to pass it might have an average P>10%, a minimum P>5%, but if minimum DCFF> 1 bar, it still passes.
 * I just guessed at those numbers; maybe we pass >5%? I think we'd want to experiment to see what gets excluded
 * 
 * @author kevin
 *
 */
public class CoulombRatesTester {
	
	public static boolean BUGGY_MIN_STRESS = true;
	
	public enum TestType {
		/** just test the coulomb values */
		COULOMB_STRESS,
		/** just test the shear values */
		SHEAR_STRESS,
		/** test that both pass */
		BOTH,
		/** test that either pass */
		EITHER;
	}
	
	private double minAverageProb;
	private double minIndividualProb;
	// if the minimum stress value is above this ceiling, it will be included no matter what
	private double minimumStressExclusionCeiling;
	private TestType testType;
	private boolean applyBranchesOnly;
	
	public CoulombRatesTester(TestType testType, double minAverageProb, double minIndividualProb,
			double minimumStressExclusionCeiling, boolean applyBranchesOnly) {
		this.minAverageProb = minAverageProb;
		this.minIndividualProb = minIndividualProb;
		this.minimumStressExclusionCeiling = minimumStressExclusionCeiling;
		Preconditions.checkNotNull(testType, "Test type must be specified!");
		this.testType = testType;
		this.applyBranchesOnly = applyBranchesOnly;
		
		if (BUGGY_MIN_STRESS)
			System.err.println("WARNING: buggy coulomb min stress exclusion implementation being used.");
	}
	
	public boolean isApplyBranchesOnly() {
		return applyBranchesOnly;
	}
	
	/**
	 * Tests the given rupture both directions for the specified criteria. This will return true if 
	 * the rupture passes in either direction.
	 * 
	 * @param rup
	 * @param rates
	 * @return
	 */
	public boolean doesRupturePass(List<CoulombRatesRecord> forwardRates, List<CoulombRatesRecord> backwardRates) {
		if (forwardRates.isEmpty())
			return true; // return true if no rates to check
		// check simple cases first
		if (testType == TestType.SHEAR_STRESS || testType == TestType.COULOMB_STRESS)
			return doesRupturePassEitherWay(forwardRates, backwardRates, testType);
		// this means we need to test both!
		// first test coulomb
		boolean coulombPass = doesRupturePassEitherWay(forwardRates, backwardRates, TestType.COULOMB_STRESS);
		// if coulomb passed and it's an "either" criteria, go ahead and pass
		if (testType == TestType.EITHER && coulombPass)
			return true;
		// if coulomb didn't pass and it's a "both" criteria, go ahead and fail 
		if (testType == TestType.BOTH && !coulombPass)
			return false;
		// getting here means passing depends only on shear passing
		return doesRupturePassEitherWay(forwardRates, backwardRates, TestType.SHEAR_STRESS);
	}
	
	private boolean doesRupturePassEitherWay(List<CoulombRatesRecord> forwardRates, List<CoulombRatesRecord> backwardRates, TestType type) {
		return doesRupturePassOneWay(forwardRates, type) || doesRupturePassOneWay(backwardRates, type);
	}
	
	/**
	 * Tests if the given rupture passes. Note that this only tests one direction.
	 * @return
	 */
	private boolean doesRupturePassOneWay(List<CoulombRatesRecord> rates, TestType type) {
		
		double minStress = Double.POSITIVE_INFINITY;
		double minProb = Double.POSITIVE_INFINITY;
		double sumProbs = 0;
		
		int pairs = rates.size();
		
		int num = 0;
		for (CoulombRatesRecord record : rates) {
			double stress = getStress(type, record);
			double prob = getProbability(type, record);
			
			// see if the stress change is already above our ceiling, which means 
			// that we can ignore this record
			if (!BUGGY_MIN_STRESS && stress >= minimumStressExclusionCeiling)
				continue;
			
			if (stress < minStress)
				minStress = stress;
			if (prob < minProb)
				minProb = prob;
			sumProbs += prob;
			num++;
		}
		
		if (num == 0)
			// all tests were skipped for minimum exclusion ceiling
			return true;
		
		double avgProb = sumProbs / (double)num;
		
		// see if the minimum stress change is already above our ceiling (which means pass no matter what
		if (minStress > minimumStressExclusionCeiling)
			return true;
		
		return avgProb > minAverageProb && minProb > minIndividualProb;
	}
	
	private static double getStress(TestType type, CoulombRatesRecord record) {
		switch (type) {
		case COULOMB_STRESS:
			return record.getCoulombStressChange();
		case SHEAR_STRESS:
			return record.getShearStressChange();

		default:
			throw new IllegalStateException();
		}
	}
	
	private static double getProbability(TestType type, CoulombRatesRecord record) {
		switch (type) {
		case COULOMB_STRESS:
			return record.getCoulombStressProbability();
		case SHEAR_STRESS:
			return record.getShearStressProbability();

		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public String toString() {
		return "CoulombRatesFilter [minAverageProb=" + minAverageProb
				+ ", minIndividualProb=" + minIndividualProb
				+ ", minimumStressExclusionCeiling="
				+ minimumStressExclusionCeiling + ", testType=" + testType
				+ "]";
	}

	public double getMinAverageProb() {
		return minAverageProb;
	}

	public void setMinAverageProb(double minAverageProb) {
		this.minAverageProb = minAverageProb;
	}

	public double getMinIndividualProb() {
		return minIndividualProb;
	}

	public void setMinIndividualProb(double minIndividualProb) {
		this.minIndividualProb = minIndividualProb;
	}

	public double getMinimumStressExclusionCeiling() {
		return minimumStressExclusionCeiling;
	}

	public void setMinimumStressExclusionCeiling(
			double minimumStressExclusionCeiling) {
		this.minimumStressExclusionCeiling = minimumStressExclusionCeiling;
	}

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}

	public void setApplyBranchesOnly(boolean applyBranchesOnly) {
		this.applyBranchesOnly = applyBranchesOnly;
	}

}
