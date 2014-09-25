package scratch.kevin.simulators.synch.prediction;

import java.util.List;

import org.opensha.commons.data.Named;

public interface Predictor extends Named {
	
	/**
	 * Initialized this predictor with an initial path
	 * 
	 * @param path
	 * @param distSpacing
	 */
	public void init(List<int[]> path, double distSpacing);
	
	/**
	 * Adds a state to the current path, which can be used in future predictions
	 * @param state
	 */
	public void addState(int[] state);
	
	/**
	 * Get rupture probabilities for the time window following the current last state
	 * @return
	 */
	public double[] getRuptureProbabilities();
	
	/**
	 * Print any diagnostics
	 */
	public void printDiagnostics();

}
