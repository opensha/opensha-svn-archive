package scratch.kevin.simulators.synch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

import scratch.kevin.simulators.synch.MarkovPath.LoopCounter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
	
	// this stores the actual path
	private List<int[]> fullPath;
	private Map<IndicesKey, List<Integer>> stateIndexesMap;
	
	private transient Map<IndicesKey, Collection<int[]>> parentStatesMap;
	
	private transient MarkovChainBuilder reversed;
	
	public MarkovChainBuilder(double distSpacing, List<EQSIM_Event> events, List<List<EQSIM_Event>> matchesLists) {
		this.distSpacing = distSpacing;
		this.firstBinCenter = distSpacing*0.5;
		
		this.nDims = matchesLists.size();
		
		totalStatesDataset = new SparseNDimensionalHashDataset<Double>(nDims, firstBinCenter, distSpacing);
		stateTransitionDataset = new SparseNDimensionalHashDataset<PossibleStates>(nDims, firstBinCenter, distSpacing);
		
		double maxTime = events.get(events.size()-1).getTimeInYears();
		double startTime = events.get(0).getTimeInYears();
		int numSteps = (int)((maxTime - startTime)/distSpacing);
		
		fullPath = Lists.newArrayList();
		stateIndexesMap = Maps.newHashMap();
		
		int[] lastMatchIndexBeforeWindowEnd = new int[nDims];
		for (int i=0; i<nDims; i++)
			lastMatchIndexBeforeWindowEnd[i] = -1;
		
		int[] prevState = null;
		
		possibleInitialStates = new PossibleStates(null);
		
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
					possibilities = new PossibleStates(prevState);
					stateTransitionDataset.set(prevState, possibilities);
				}
				possibilities.add(curState, 1d);
				possibleInitialStates.add(curState, 1d);
				
				fullPath.add(curState);
				IndicesKey key = new IndicesKey(curState);
				List<Integer> indexesForState = stateIndexesMap.get(key);
				if (indexesForState == null) {
					indexesForState = Lists.newArrayList();
					stateIndexesMap.put(key, indexesForState);
				}
				indexesForState.add(fullPath.size()-1);
			}
			
			prevState = curState;
		}
		
		System.out.println("DONE assembling state transition probabilities (skipped "+skippedSteps+" steps)");
	}

	/**
	 * Just for use interally generating collapsed chains
	 * 
	 * @param nDims
	 * @param distSpacing
	 * @param firstBinCenter
	 * @param totalStatesDataset
	 * @param stateTransitionDataset
	 * @param possibleInitialStates
	 */
	private MarkovChainBuilder(
			int nDims,
			double distSpacing,
			double firstBinCenter,
			SparseNDimensionalHashDataset<Double> totalStatesDataset,
			SparseNDimensionalHashDataset<PossibleStates> stateTransitionDataset,
			PossibleStates possibleInitialStates,
			List<int[]> fullPath,
			Map<IndicesKey, List<Integer>> stateIndexesMap) {
		this.nDims = nDims;
		this.distSpacing = distSpacing;
		this.firstBinCenter = firstBinCenter;
		this.totalStatesDataset = totalStatesDataset;
		this.stateTransitionDataset = stateTransitionDataset;
		this.possibleInitialStates = possibleInitialStates;
		this.fullPath = fullPath;
		this.stateIndexesMap = stateIndexesMap;
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
	
	public MarkovChainBuilder getCollapsedChain(int... indices) {
		int nDims = indices.length;
		
		SparseNDimensionalHashDataset<Double> collapsedTotalStatesDataset =
				new SparseNDimensionalHashDataset<Double>(nDims, firstBinCenter, distSpacing);
		SparseNDimensionalHashDataset<PossibleStates> collapsedStateTransitionDataset =
				new SparseNDimensionalHashDataset<PossibleStates>(nDims, firstBinCenter, distSpacing);
		
		PossibleStates initials = new PossibleStates(null);
		
		for (int[] state : this.stateTransitionDataset.getPopulatedIndices()) {
			int[] collapsedState = getCollapsedState(state, indices);
			double tot = this.totalStatesDataset.get(state);
			PossibleStates states = this.stateTransitionDataset.get(state);
			
			Double collapsedTot = collapsedTotalStatesDataset.get(collapsedState);
			PossibleStates collapsedPossible = collapsedStateTransitionDataset.get(collapsedState);
			if (collapsedTot == null) {
				// first time in state
				collapsedTot = 0d;
				Preconditions.checkState(collapsedPossible == null);
				collapsedPossible = new PossibleStates(collapsedState);
			}
			collapsedTot += tot;
			for (int i=0; i<states.getNumStates(); i++) {
				int[] pState = states.getStates().get(i);
				double pFreq = states.getFrequency(pState);
				int[] collapsedPState = getCollapsedState(pState, indices);
				collapsedPossible.add(collapsedPState, pFreq);
				initials.add(collapsedPState, pFreq);
			}
			
			collapsedTotalStatesDataset.set(collapsedState, collapsedTot);
			collapsedStateTransitionDataset.set(collapsedState, collapsedPossible);
		}
		
		List<int[]> collapsedFullPath = Lists.newArrayList();
		Map<IndicesKey, List<Integer>> collapsedStateIndexesMap = Maps.newHashMap();
		for (int[] state : fullPath) {
			int[] collapsedState = getCollapsedState(state, indices);
			collapsedFullPath.add(collapsedState);
			IndicesKey key = new IndicesKey(collapsedState);
			List<Integer> indexesForState = collapsedStateIndexesMap.get(key);
			if (indexesForState == null) {
				indexesForState = Lists.newArrayList();
				collapsedStateIndexesMap.put(key, indexesForState);
			}
			indexesForState.add(collapsedFullPath.size()-1);
		}
		
		return new MarkovChainBuilder(nDims, this.distSpacing, this.firstBinCenter,
				collapsedTotalStatesDataset, collapsedStateTransitionDataset, initials,
				collapsedFullPath, collapsedStateIndexesMap);
	}
	
	private static int[] getCollapsedState(int[] state, int... indices) {
		int[] collapsedState = new int[indices.length];
		for (int i=0; i<indices.length; i++)
			collapsedState[i] = state[indices[i]];
		return collapsedState;
	}
	
	// TODO must implement fullPath to be re-enabled
//	/**
//	 * This gets a reversed Markov chian with origination frequencies, instead of transition frequencies
//	 * @return
//	 */
//	public synchronized MarkovChainBuilder getReversedChain() {
//		if (reversed == null) {
//			Map<IndicesKey, Collection<int[]>> parentStatesMap = getParentStatesMap();
//			SparseNDimensionalHashDataset<PossibleStates> reversedStateTransitionDataset =
//					new SparseNDimensionalHashDataset<PossibleStates>(nDims, firstBinCenter, distSpacing);
//			
//			for (int[] state : this.stateTransitionDataset.getPopulatedIndices()) {
//				Collection<int[]> parentStates = parentStatesMap.get(new IndicesKey(state));
//				PossibleStates possible = new PossibleStates(state);
//				if (parentStates != null) {
//					for (int[] parentState : parentStates) {
//						// this is the number of times we went from the parent state to the current state
//						double transFreq = stateTransitionDataset.get(parentState).getFrequency(state);
//						possible.add(parentState, transFreq);
//					}
//				}
//				reversedStateTransitionDataset.set(state, possible);
//			}
//			
//			reversed = new MarkovChainBuilder(nDims, this.distSpacing, this.firstBinCenter,
//					totalStatesDataset, reversedStateTransitionDataset, possibleInitialStates);
//		}
//		return reversed;
//	}
	
	/**
	 * This calculates a map from child states to all possible parent states
	 * @return
	 */
	public synchronized Map<IndicesKey, Collection<int[]>> getParentStatesMap() {
		if (parentStatesMap == null) {
			parentStatesMap = Maps.newHashMap();
			
			for (int[] state : stateTransitionDataset.getPopulatedIndices()) {
//				System.out.println("Parent state: ["+state[0]+","+state[1]+"]");
//				if (state[0] == 2 && state[1] == 2)
//					System.out.println("I'm at [2,2]");
				PossibleStates poss = stateTransitionDataset.get(state);
				for (int[] toState : poss.getStates()) {
//					System.out.println("\tChild state: ["+toState[0]+","+t/oState[1]+"]");
//					if (toState[0] == 3 && toState[1] == 3)
//						System.out.println("Found a parent for [3,3]: ["+state[0]+","+state[1]+"]");
//					else if (state[0] == 2 && state[1] == 2)
//						System.out.println("Found a different child for [2,2]: ["+toState[0]+","+toState[1]+"]");
					IndicesKey key = new IndicesKey(toState);
					Collection<int[]> fromStates = parentStatesMap.get(key);
					if (fromStates == null) {
//						fromStates = new HashSet<int[]>();
						fromStates = Lists.newArrayList();
						parentStatesMap.put(key, fromStates);
					}
					fromStates.add(state);
				}
			}
		}
		return parentStatesMap;
	}
	
	/**
	 * Gets all of the possible chains of the given length between the given states.
	 * Returned lists will not include the fromState, but will include the toState.
	 * Note that toState can be reached multiple times along the path.
	 * 
	 * @param fromState
	 * @param toState -1 to indicate any state at that index
	 * @param numSteps
	 * @return
	 */
	public List<MarkovPath> getTheoreticalPathsBetweenStates(int[] fromState, int[] toState, int numSteps, int maxLoops, double minProb) {
		List<MarkovPath> res = getTheoreticalPathsBetweenStates(new LoopCounter(), fromState, fromState, toState, numSteps, maxLoops, minProb);
		pathsFinalized += res.size();
		return res;
	}
	
	private double calcProb(int[] fromState, int[] toState) {
		PossibleStates states = stateTransitionDataset.get(fromState);
		return states.getFrequency(toState) / states.tot;
	}
	
	private static long pathsFinalized = 0;
	private static long pathsRejected = 0;
	
	private List<MarkovPath> getTheoreticalPathsBetweenStates(LoopCounter counter, int[] origFromState, int[] prevState, int[] toState, int numSteps,
			int maxLoops, double minProb) {
//		if (numSteps == 0) {
//			// we're checking if this state is a match
//			for (int i=0; i<fromState.length; i++) {
//				if (fromState[i] != toState[i] && toState[i] >= 0)
//					// fails
//					return null;
//			}
//			// this means it passes
//			List<List<int[]>> ret = Lists.newArrayList();
//			ret.add(Lists.newArrayList(fromState));
//			return ret;
//		}
		PossibleStates states = stateTransitionDataset.get(prevState);
		
		List<MarkovPath> paths = Lists.newArrayList();
		for (int[] possibleState : states.getStates()) {
			counter.cloneResgister(possibleState);
			if (counter.getMaxLoops() > maxLoops)
				continue;
			if (numSteps == 1) {
				// we're at the end here
				boolean match = true;
				for (int i=0; i<prevState.length; i++) {
					if (possibleState[i] != toState[i] && toState[i] >= 0) {
						// fails
						match = false;
						break;
					}
				}
				if (match) {
					MarkovPath path = new MarkovPath(origFromState);
					path.addToStart(possibleState, calcProb(prevState, possibleState));
					paths.add(path);
				}
			} else {
				// in the middle
				// is going to this state too many loops?
				List<MarkovPath> subPaths = getTheoreticalPathsBetweenStates(counter, origFromState, possibleState,
						toState, numSteps-1, maxLoops, minProb);
				for (MarkovPath subPath : subPaths) {
					subPath = subPath.cloneAddToStart(possibleState, calcProb(prevState, possibleState));
					if (subPath.getMaxLoops() <= maxLoops && subPath.getProbability() >= minProb) {
//						if (Math.random() < 0.00001) {
//							System.out.println("fin="+pathsFinalized+", rej="+pathsRejected+". rand: "+subPath.getPathStr());
//						}
						paths.add(subPath);
					} else {
						pathsRejected++;
					}
				}
//				int loops = countLoops(counts, possibleState);
//				if (loops <= maxLoops) {
//					Map<IndicesKey, Integer> newCounts = Maps.newHashMap();
//					newCounts.putAll(counts);
//					newCounts.put(new IndicesKey(possibleState), loops+1);
//					List<MarkovPath> subPaths = getPathsBetweenStates(origFromState, possibleState,
//							toState, numSteps-1, maxLoops, newCounts);
//					for (List<int[]> subPath : subPaths) {
//						subPath = Lists.newArrayList(subPath);
//						subPath.add(0, possibleState);
//						paths.add(subPath);
//					}
//				} else {
//					System.out.println("Bailing after too many loops!");
//				}
			}
		}
		
		return paths;
	}
	
	private static final int countLoops(Map<IndicesKey, Integer> counts, int[] newState) {
		Integer count = counts.get(new IndicesKey(newState));
		if (count == null)
			// first time this state has been encountered
			return 0;
		return count;
	}
	
	public double getActualTransPathsProbBetweenStates(int[] fromState, int[] toState, int numSteps,
			int[]... requiredSubsequentStates) {
		Preconditions.checkState(requiredSubsequentStates.length <= (int)Math.abs(numSteps));
		List<Integer> indexes = stateIndexesMap.get(new IndicesKey(fromState));
		int count = 0;
		int numChecked = 0;
		stateLoop:
		for (int fromIndex : indexes) {
			// numSteps can be negative
			int toIndex = fromIndex + numSteps;
			if (toIndex < 0 || toIndex > fullPath.size())
				continue;
			// now check subsequent states if applicable
			if (requiredSubsequentStates.length > 0) {
				int cnt = 0;
				if (numSteps < 0) {
					// backwards
					for (int i=fromIndex; --i>=toIndex && cnt < requiredSubsequentStates.length;)
						if (!Arrays.equals(fullPath.get(i), requiredSubsequentStates[cnt++]))
							continue stateLoop;
				} else {
					// forwards
					for (int i=fromIndex; ++i<=toIndex && cnt < requiredSubsequentStates.length;)
						if (!Arrays.equals(fullPath.get(i), requiredSubsequentStates[cnt++]))
							continue stateLoop;
				}
			}
			numChecked++;
			int[] testToState = fullPath.get(toIndex);
			for (int i=0; i<toState.length; i++) {
				if (toState[i] >= 0 && testToState[i] != toState[i]) {
					// not a match
					continue stateLoop;
				}
			}
			count++;
		}
		Preconditions.checkState(count <= indexes.size());
		double prob = (double)count/(double)numChecked;
		if (Double.isNaN(prob))
			return 0d;
		return prob;
	}

}
