package scratch.UCERF3.simulatedAnnealing.completion;

import org.apache.commons.lang3.time.StopWatch;

public class TimeCompletionCriteria implements CompletionCriteria {
	
	private long millis;
	
	/**
	 * Creates a TimeCompletionCriteria that will be statisfied after the given number of miliseconds.
	 * 
	 * @param milis
	 */
	public TimeCompletionCriteria(long millis) {
		this.millis = millis;
	}

	@Override
	public boolean isSatisfied(StopWatch watch, long iter, double energy) {
		return watch.getTime() >= millis;
	}
	
	@Override
	public String toString() {
		return "TimeCompletionCriteria(milis: "+millis+" = "+(float)(millis / 1000d)+" seconds)";
	}
	
	public long getMillis() {
		return millis;
	}
	
	public static TimeCompletionCriteria getInSeconds(long secs) {
		return new TimeCompletionCriteria(secs * 1000);
	}
	
	public static TimeCompletionCriteria getInMinutes(long mins) {
		return getInSeconds(mins * 60);
	}
	
	public static TimeCompletionCriteria getInHours(long hours) {
		return getInMinutes(hours * 60);
	}
	
	public String getTimeStr() {
		if (millis % 1000 != 0)
			return millis+"";
		long secs = millis / 1000;
		if (secs % 60 != 0)
			return secs+"s";
		long mins = secs / 60;
		if (mins % 60 != 0)
			return mins+"m";
		long hours = mins / 60;
		return hours+"h";
	}

}
