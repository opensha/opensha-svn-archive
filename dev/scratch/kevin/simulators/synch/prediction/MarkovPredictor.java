package scratch.kevin.simulators.synch.prediction;

import java.util.List;

import scratch.kevin.simulators.synch.MarkovChainBuilder;
import scratch.kevin.simulators.synch.PossibleStates;

public class MarkovPredictor implements Predictor {
	
	private MarkovChainBuilder chain;
	private int numMisses = 0;
	private int totPredictions = 0;
	
	private Predictor backupPredictor;
	
	public MarkovPredictor() {
		this(null);
	}
	
	public MarkovPredictor(Predictor backupPredictor) {
		this.backupPredictor = backupPredictor;
	}

	@Override
	public String getName() {
		if (chain == null)
			return "Markov";
		return chain.getNDims()+"D Markov";
	}

	@Override
	public void init(List<int[]> path, double distSpacing) {
		chain = new MarkovChainBuilder(path, distSpacing);
		if (backupPredictor != null)
			backupPredictor.init(path, distSpacing);
	}

	@Override
	public void addState(int[] state) {
		chain.addState(state);
		if (backupPredictor != null)
			backupPredictor.addState(state);
	}

	@Override
	public double[] getRuptureProbabilities() {
		List<int[]> fullPath = chain.getFullPath();
		int[] prevState = fullPath.get(fullPath.size()-1);
		
		double[] ret = new double[prevState.length];
		
		PossibleStates possible = chain.getStateTransitionDataset().get(prevState);
		if (possible != null) {
			// first fill ret with frequencies
			List<int[]> states = possible.getStates();
			double totFreq = 0;
			for (int i=0; i<states.size(); i++) {
				int[] state = states.get(i);
				double freq = possible.getFrequency(state);
				
				for (int j=0; j<state.length; j++)
					if (state[j] == 0)
						ret[j] += freq;
				
				totFreq += freq;
			}
			// now normalize
			for (int i=0; i<ret.length; i++)
				ret[i] = ret[i]/totFreq;
		} else {
			numMisses++;
			if (backupPredictor != null)
				ret = backupPredictor.getRuptureProbabilities();
		}
		totPredictions++;
		
		return ret;
	}

	@Override
	public void printDiagnostics() {
		String str = "Markov Misses: "+numMisses+"/"+totPredictions+" ("
				+100f*((float)numMisses/(float)totPredictions)+" %)";
		if (backupPredictor != null)
			str += ", replaced with probs from "+backupPredictor.getName();
		System.out.println(str);
	}

}
