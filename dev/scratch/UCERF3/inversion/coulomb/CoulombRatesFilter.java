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
public class CoulombRatesFilter {
	
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
	
	public CoulombRatesFilter(TestType testType, double minAverageProb, double minIndividualProb, double minimumStressExclusionCeiling) {
		this.minAverageProb = minAverageProb;
		this.minIndividualProb = minIndividualProb;
		this.minimumStressExclusionCeiling = minimumStressExclusionCeiling;
		Preconditions.checkNotNull(testType, "Test type must be specified!");
		this.testType = testType;
	}
	
	/**
	 * Tests the given rupture both directions for the specified criteria. This will return true if 
	 * the rupture passes in either direction.
	 * 
	 * @param rup
	 * @param rates
	 * @return
	 */
	public boolean doesRupturePass(List<Integer> rup, CoulombRates rates) {
		ArrayList<CoulombRatesRecord> forwardRates = new ArrayList<CoulombRatesRecord>();
		ArrayList<CoulombRatesRecord> backwardRates = new ArrayList<CoulombRatesRecord>();
		
		for (int i=1; i<rup.size(); i++) {
			IDPairing pairing = new IDPairing(rup.get(i-1), rup.get(i));
			CoulombRatesRecord record = rates.get(pairing);
			Preconditions.checkNotNull(record, "No mapping exists for pairing: "+pairing);
			
			forwardRates.add(record);
			
			pairing = pairing.getReversed();
			record = rates.get(pairing);
			Preconditions.checkNotNull(record, "No mapping exists for pairing: "+pairing);
			backwardRates.add(0, record);
		}
		
		return doesRupturePass(forwardRates, backwardRates);
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
		
		for (CoulombRatesRecord record : rates) {
			double stress = getStress(type, record);
			double prob = getProbability(type, record);
			
			if (stress < minStress)
				minStress = stress;
			if (prob < minProb)
				minProb = prob;
			sumProbs += prob;
		}
		
		double avgProb = sumProbs / (double)pairs;
		
		// see if the minimum stress change is already above our ceiling (whcih means pass no matter what
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

}
