package scratch.UCERF3.simulatedAnnealing.completion;

import org.apache.commons.lang.time.StopWatch;

public interface CompletionCriteria {
	
	/**
	 * Evaluates if the completion criteria is satisfied
	 * 
	 * @param watch stop watch for keeping track of time
	 * @param iter number of iterations completed
	 * @param energy energy of the best solution
	 * @return true if completions criteria is satisfied
	 */
	public boolean isSatisfied(StopWatch watch, long iter, double energy);

}
