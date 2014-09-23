package scratch.kevin.simulators.dists;

import java.util.List;

import org.opensha.sha.simulators.EQSIM_Event;

public interface ProbabilisticReturnPeriodProvider extends
		RandomReturnPeriodProvider {
	
	public PossibleRupture getPossibleRupture(List<EQSIM_Event> prevEvents, double windowStart, double windowEnd);
	
	public double getPreferredWindowLength();

}
