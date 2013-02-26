package scratch.UCERF3.inversion.laughTest;

import java.util.HashSet;
import java.util.List;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;

import com.google.common.collect.Lists;

import scratch.UCERF3.inversion.coulomb.CoulombRates;
import scratch.UCERF3.inversion.coulomb.CoulombRatesRecord;
import scratch.UCERF3.inversion.coulomb.CoulombRatesTester;
import scratch.UCERF3.utils.IDPairing;

/**
 * This is a Coulomb Filter which only applies Coulomb tests at each junction where a rupture
 * jumps to a new parent fault section.
 * 
 * @author kevin
 *
 */
public class CoulombFilter extends AbstractLaughTest {
	
	private CoulombRates rates;
	private CoulombRatesTester tester;
	private boolean minEqualsAvg;
	
	public CoulombFilter(CoulombRates rates, CoulombRatesTester tester) {
		this.rates = rates;
		this.tester = tester;
		this.minEqualsAvg = tester.getMinAverageProb() <= tester.getMinIndividualProb();
	}

	@Override
	public boolean doesLastSectionPass(List<FaultSectionPrefData> rupture,
			List<IDPairing> pairings, List<Integer> junctionIndexes) {
		if (rupture.size() < 2 || (isApplyJunctionsOnly() && junctionIndexes.isEmpty()))
			return true;
		
		List<CoulombRatesRecord> forwardRates = Lists.newArrayList();
		List<CoulombRatesRecord> backwardRates = Lists.newArrayList();
		if (minEqualsAvg) {
			// this means we can just test the last junction since the average could not possibly
			// get below the filter level
			IDPairing pair = pairings.get(pairings.size()-1);
			forwardRates.add(rates.get(pair));
			backwardRates.add(rates.get(pair.getReversed()));
		} else {
			// add all previous ones
			if (isApplyJunctionsOnly()) {
				for (int junctionIndex : junctionIndexes) {
					// index+1 here because pairing list starts with the second section
					IDPairing pair = pairings.get(junctionIndex+1);
					forwardRates.add(rates.get(pair));
					backwardRates.add(0, rates.get(pair.getReversed()));
				}
			} else {
				for (IDPairing pair : pairings) {
					forwardRates.add(rates.get(pair));
					backwardRates.add(0, rates.get(pair.getReversed()));
				}
			}
		}
		return tester.doesRupturePass(forwardRates, backwardRates);
	}

	@Override
	public boolean isContinueOnFaulure() {
		return false;
	}

	@Override
	public boolean isApplyJunctionsOnly() {
		return tester.isApplyBranchesOnly();
	}

}
