package scratch.kevin.markov;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;

/**
 * This Markov chain uses state occupancy to determine 2D state transition probabiltiies
 * @author kevin
 *
 */
public class OccupancyBasedMarkovChain2D extends MarkovChain {
	
	private Map<IndicesKey, PossibleStates> stateTransitionDataset;
	
	private PossibleStates[] marginals;
	
	public OccupancyBasedMarkovChain2D(double distSpacing, PossibleStates possibleStates) {
		Preconditions.checkState(possibleStates.getStates().get(0).length == 2, "Must be 2D!");
		init(2, distSpacing, possibleStates);
		
		stateTransitionDataset = Maps.newHashMap();
	}

	@Override
	public MarkovChain getCollapsedChain(int... indices) {
		Preconditions.checkState(indices.length == 0 && indices[0] == 0 && indices[1] == 1,
				"Two D chain, so collapsing not relevant");
		return this;
	}

	@Override
	public synchronized void addState(int[] fromState, int[] toState) {
		PossibleStates possibleStates = getPossibleInitialStates();
		possibleStates.add(toState, 1d);
		// must clear transition map because all states that can transition to this state are now invalid
		stateTransitionDataset.clear();
		marginals = null;
	}

	@Override
	public synchronized PossibleStates getDestinationStates(int[] fromState) {
		IndicesKey key = new IndicesKey(fromState);
		PossibleStates dests = stateTransitionDataset.get(key);
		if (dests == null) {
			PossibleStates occupancy = getPossibleInitialStates();
			if (marginals == null) {
				marginals = new PossibleStates[getNDims()];
				for (int i=0; i<getNDims(); i++)
					marginals[i] = occupancy.getMarginal(i);
			}
			dests = calcDestStates(fromState, occupancy, marginals);
			stateTransitionDataset.put(key, dests);
		}
		return dests;
	}
	
	private static PossibleStates calcDestStates(int[] fromState, PossibleStates occupancy, PossibleStates[] marginals) {
		double occ = occupancy.getTot();
		
		// calculate marginal probabilities
		double pE1givenS1 = calcMarginalProb(marginals[0], fromState[0]);
		double pE2givenS2 = calcMarginalProb(marginals[1], fromState[1]);
		double pNE1givenS1 = 1d-pE1givenS1;
		double pNE2givenS2 = 1d-pE2givenS2;
		
		// N(S1,S2)
		double nS1S2 = occupancy.getFrequency(fromState);
		if (nS1S2 == 0d)
			return null;
		double pS1S2 = nS1S2/occ;
		// N(S1+1,S2+1)
		double nS1p1S2p1 = occupancy.getFrequency(new int[] {fromState[0]+1, fromState[1]+1});
		
		// prob neither: P(!E1,!E2|S1,S2)
		double pNE1_NE2givenS1S2 = nS1p1S2p1/nS1S2;
		Preconditions.checkState(Doubles.isFinite(pNE1_NE2givenS1S2),
				"Not finite: "+nS1p1S2p1+"/"+nS1S2+" = "+pNE1_NE2givenS1S2);
		
		// P(!E2|S1,S2)		
		double pNE2givenS1S2 = pNE2givenS2 * pS1S2;
		
		// P(E1,!E2|S1,S2)
		double pE1_NE2givenS1S2 = pNE2givenS1S2 - pNE1_NE2givenS1S2;
		
		// P(!E1|S1,S2)
		double pNE1givenS1S2 = pNE1givenS1 * pS1S2;
		
		// P(!E1,E2|S1,S2)
		double pNE1_E2givenS1S2 = pNE1givenS1S2 - pNE1_NE2givenS1S2;
		
		// P(E1,E2|S1,S2)
		double pE1_E2givenS1S2 = 1d - (pNE1_NE2givenS1S2+pE1_NE2givenS1S2+pNE1_E2givenS1S2);
		
		PossibleStates dests = new PossibleStates(fromState);
		dests.add(new int[] {fromState[0]+1, fromState[1]+1}, occ*pNE1_NE2givenS1S2);
		dests.add(new int[] {0, fromState[1]+1}, occ*pE1_NE2givenS1S2);
		dests.add(new int[] {fromState[0]+1, 0}, occ*pNE1_E2givenS1S2);
		dests.add(new int[] {0, 0}, occ*pE1_E2givenS1S2);
		
		return dests;
	}
	
	private static double calcMarginalProb(PossibleStates marginal, int startIndex) {
		double freqAt = 0d;
		double freqAtOrAbove = 0d;
		
		for (int[] state : marginal.getStates()) {
			Preconditions.checkState(state.length == 1); // make sure it's actually a marginal
			double freq = marginal.getFrequency(state);
			if (state[0] == startIndex)
				freqAt += freq;
			if (state[0] >= startIndex)
				freqAtOrAbove += freq;
		}
		
		Preconditions.checkState(freqAtOrAbove >= freqAt);
		if (freqAtOrAbove == 0d)
			return 1d;
		
		return freqAt / freqAtOrAbove;
	}

}
