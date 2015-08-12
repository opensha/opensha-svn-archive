package org.opensha.sha.simulators.iden;

import java.util.concurrent.ConcurrentMap;

import org.opensha.commons.geo.Region;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.RectangularElement;

import com.google.common.collect.Maps;

public class RegionIden extends AbstractRuptureIdentifier {
	
	private Region region;
	private ConcurrentMap<RectangularElement, Boolean> insideCache;
	
	public RegionIden(Region region) {
		this.region = region;
		insideCache = Maps.newConcurrentMap();
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		for (RectangularElement elem: event.getAllElements()) {
			Boolean inside = insideCache.get(elem);
			if (inside == null) {
				inside = region.contains(elem.getCenterLocation());
				insideCache.putIfAbsent(elem, inside);
			}
			if (inside)
				return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "RegionIden: "+region.getName();
	}

}