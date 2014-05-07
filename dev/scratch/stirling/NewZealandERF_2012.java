package scratch.stirling;

import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkSource;

/**
 * An ERF for New Zealand using updated 2012 sources.
 *
 * @author Mark Stirling
 */
public class NewZealandERF_2012 extends AbstractERF {

	public static final String NAME = "New Zealand ERF 2012";
	
	public NewZealandERF_2012() {
		
	}

	@Override
	public int getNumSources() {
		return 0;
		// TODO do nothing
		
	}

	@Override
	public ProbEqkSource getSource(int idx) {
		return null;
		// TODO do nothing
		
	}

	@Override
	public void updateForecast() {
		// TODO do nothing
		
	}

	@Override
	public String getName() {
		return NAME;
	}
	
}
