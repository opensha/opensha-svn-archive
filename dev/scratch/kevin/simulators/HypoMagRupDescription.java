package scratch.kevin.simulators;

import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

public class HypoMagRupDescription implements RuptureIdentifier {
	
	public HypoMagRupDescription(double mag, Location hypocenter) {
		
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<EQSIM_Event> getMatches(List<EQSIM_Event> events) {
		// TODO Auto-generated method stub
		return null;
	}

}
