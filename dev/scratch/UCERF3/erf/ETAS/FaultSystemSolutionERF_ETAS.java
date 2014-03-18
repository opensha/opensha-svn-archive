package scratch.UCERF3.erf.ETAS;

import org.opensha.commons.data.TimeSpan;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;

/**
 * This overrides subclass to:
 * 
 * 1) add more start-time precision (for both poisson and time-dep probability model options)
 * @author field
 *
 */
public class FaultSystemSolutionERF_ETAS extends FaultSystemSolutionERF {

	public FaultSystemSolutionERF_ETAS(FaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
	}

	public FaultSystemSolutionERF_ETAS(String fullPathInputFile) {
		super(fullPathInputFile);
	}

	public FaultSystemSolutionERF_ETAS() {
	}
	
	/**
	 * This initiates the timeSpan.
	 */
	@Override
	protected void initTimeSpan() {
			timeSpan = new TimeSpan(TimeSpan.MILLISECONDS, TimeSpan.YEARS);
			timeSpan.setDuractionConstraint(DURATION_MIN, DURATION_MAX);
			timeSpan.setDuration(DURATION_DEFAULT);
			timeSpan.setStartTimeConstraint(TimeSpan.START_YEAR, START_TIME_MIN, START_TIME_MAX);
			timeSpan.setStartTime(START_TIME_DEFAULT, 1, 1, 0, 0, 0, 0);
			timeSpan.addParameterChangeListener(this);			
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
