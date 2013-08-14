package scratch.kevin.simulators;

import java.util.List;

import org.opensha.commons.data.Named;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

public interface RuptureIdentifier extends Named {
	
	/**
	 * Returns true if the given event is a match for this scenario.
	 * 
	 * @param event
	 * @return
	 */
	public boolean isMatch(EQSIM_Event event);
	
	/**
	 * Returns a list of all events that are a match for this scenario, as defined by
	 * the <code>isMatch(event)</code> method.
	 * @param events
	 * @return
	 */
	public List<EQSIM_Event> getMatches(List<EQSIM_Event> events);

}
