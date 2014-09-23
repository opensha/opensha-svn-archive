package org.opensha.sha.simulators.iden;

import java.util.ArrayList;
import java.util.List;

import org.opensha.sha.simulators.EQSIM_Event;

import com.google.common.collect.Lists;

public abstract class AbstractRuptureIdentifier implements RuptureIdentifier {
	
	public static List<EQSIM_Event> getMatches(List<EQSIM_Event> events, RuptureIdentifier id) {
		ArrayList<EQSIM_Event> matches = Lists.newArrayList();
		for (EQSIM_Event event : events)
			if (id.isMatch(event))
				matches.add(event);
		return matches;
	}

	@Override
	public List<EQSIM_Event> getMatches(List<EQSIM_Event> events) {
		return getMatches(events, this);
	}

}
