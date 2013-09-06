package scratch.kevin.simulators.synch;

import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import com.google.common.base.Preconditions;

/**
 * This creates a Markov chain for the given catalog
 * @author kevin
 *
 */
public class MarkovChainBuilder {
	
	private int nDims;
	
	private double distSpacing;
	private double firstBinCenter;
	
	private SparseNDimensionalHashDataset<Double> totalStatesDataset;
	private SparseNDimensionalHashDataset<PossibleStates> stateTransitionDataset;
	
	private PossibleStates possibleInitialStates;
	
	public MarkovChainBuilder(double distSpacing, List<EQSIM_Event> events, List<List<EQSIM_Event>> matchesLists) {
		this.distSpacing = distSpacing;
		this.firstBinCenter = distSpacing*0.5;
		
		this.nDims = matchesLists.size();
		
		totalStatesDataset = new SparseNDimensionalHashDataset<Double>(nDims, firstBinCenter, distSpacing);
		stateTransitionDataset = new SparseNDimensionalHashDataset<PossibleStates>(nDims, firstBinCenter, distSpacing);
		
		double maxTime = events.get(events.size()-1).getTimeInYears();
		double startTime = events.get(0).getTimeInYears();
		int numSteps = (int)((maxTime - startTime)/distSpacing);
		
		int[] lastMatchIndexBeforeWindowEnd = new int[nDims];
		for (int i=0; i<nDims; i++)
			lastMatchIndexBeforeWindowEnd[i] = -1;
		
		int[] prevState = null;
		
		possibleInitialStates = new PossibleStates();
		
		System.out.println("Assembling state transition probabilities");
		
		int skippedSteps = 0;
		
		int startStep = 0;
		
		double startWindowStart = startTime + distSpacing*startStep;
		for (int n=0; n<nDims && startStep>0; n++) {
			List<EQSIM_Event> myMatches = matchesLists.get(n);
			for (int i=lastMatchIndexBeforeWindowEnd[n]+1; i<myMatches.size(); i++) {
				double time = myMatches.get(i).getTimeInYears();
				if (time > startWindowStart)
					break;
				lastMatchIndexBeforeWindowEnd[n] = i;
			}
		}
		
		stepLoop:
		for (int step=startStep; step<numSteps; step++) {
			double windowStart = startTime + distSpacing*step;
			double windowEnd = windowStart + distSpacing;
			
			for (int n=0; n<nDims; n++) {
				List<EQSIM_Event> myMatches = matchesLists.get(n);
				for (int i=lastMatchIndexBeforeWindowEnd[n]+1; i<myMatches.size(); i++) {
					double time = myMatches.get(i).getTimeInYears();
					Preconditions.checkState(time >= windowStart);
					if (time > windowEnd)
						break;
					lastMatchIndexBeforeWindowEnd[n] = i;
				}
			}
			
			int[] curState = new int[nDims];
			
			for (int n=0; n<nDims; n++) {
				List<EQSIM_Event> myMatches = matchesLists.get(n);
				
				double prevEvent;
				if (lastMatchIndexBeforeWindowEnd[n] >= 0) {
					prevEvent = myMatches.get(lastMatchIndexBeforeWindowEnd[n]).getTimeInYears();
				} else {
					// skip places at start where state not defined
					skippedSteps++;
					Preconditions.checkState(prevState == null);
					continue stepLoop;
				}
				
				double myDelta = windowEnd - prevEvent;
				curState[n] = totalStatesDataset.indexForDimVal(n, myDelta);
			}
			
			// register current state
			Double stateCount = totalStatesDataset.get(curState);
			if (stateCount == null)
				stateCount = 0d;
			stateCount += 1d;
			totalStatesDataset.set(curState, stateCount);
			
			// register this state as a transition from the previous state
			if (prevState != null) {
				PossibleStates possibilities = stateTransitionDataset.get(prevState);
				if (possibilities == null) {
					possibilities = new PossibleStates();
					stateTransitionDataset.set(prevState, possibilities);
				}
				possibilities.add(curState, 1d);
				possibleInitialStates.add(curState, 1d);
			}
			
			prevState = curState;
		}
		
		System.out.println("DONE assembling state transition probabilities (skipped "+skippedSteps+" steps)");
	}

	public int getNDims() {
		return nDims;
	}

	public double getDistSpacing() {
		return distSpacing;
	}

	public double getFirstBinCenter() {
		return firstBinCenter;
	}

	public SparseNDimensionalHashDataset<Double> getTotalStatesDataset() {
		return totalStatesDataset;
	}

	public SparseNDimensionalHashDataset<PossibleStates> getStateTransitionDataset() {
		return stateTransitionDataset;
	}

	public PossibleStates getPossibleInitialStates() {
		return possibleInitialStates;
	}

}
