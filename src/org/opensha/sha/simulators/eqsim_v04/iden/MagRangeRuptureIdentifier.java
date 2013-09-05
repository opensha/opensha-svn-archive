package org.opensha.sha.simulators.eqsim_v04.iden;

import java.util.Set;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;

public class MagRangeRuptureIdentifier extends AbstractRuptureIdentifier {
	
	private double minMag, maxMag;
	private Set<Integer> elementsInRegion;
	
	public MagRangeRuptureIdentifier(double minMag, double maxMag) {
		this(minMag, maxMag, null);
	}
	
	public MagRangeRuptureIdentifier(double minMag, double maxMag, Set<Integer> elementsInRegion) {
		this.minMag = minMag;
		this.maxMag = maxMag;
		this.elementsInRegion = elementsInRegion;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		double mag = event.getMagnitude();
		if (mag < minMag || mag >= maxMag)
			return false;
		if (elementsInRegion == null)
			return true;
		for (int elementID : event.getAllElementIDs())
			if (elementsInRegion.contains(elementID))
				return true;
		return false;
	}

	@Override
	public String getName() {
		return "Mag Range: "+minMag+" => "+maxMag;
	}

}
