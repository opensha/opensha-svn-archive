package scratch.UCERF3.simulatedAnnealing.completion;

import org.apache.commons.lang.time.StopWatch;

public class TimeCompletionCriteria implements CompletionCriteria {
	
	private long milis;
	
	/**
	 * Creates a TimeCompletionCriteria that will be statisfied after the given number of miliseconds.
	 * 
	 * @param milis
	 */
	public TimeCompletionCriteria(long milis) {
		this.milis = milis;
	}

	@Override
	public boolean isSatisfied(StopWatch watch, long iter, double energy) {
		return watch.getTime() >= milis;
	}
	
	@Override
	public String toString() {
		return "TimeCompletionCriteria(milis: "+milis+" = "+(float)(milis / 1000d)+" seconds)";
	}

}
