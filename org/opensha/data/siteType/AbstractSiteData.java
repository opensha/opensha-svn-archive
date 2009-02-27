package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.param.ParameterList;

public abstract class AbstractSiteData<Element> implements SiteDataAPI<Element> {
	
	protected ParameterList paramList;
	
	public AbstractSiteData() {
		paramList = new ParameterList();
	}
	
	public SiteDataValue<Element> getAnnotatedValue(Location loc) throws IOException {
		Element val = this.getValue(loc);
		return new SiteDataValue<Element>(this.getType(), this.getTypeFlag(), val, this.getName());
	}

	/**
	 * Returns a list of the values at each location.
	 * 
	 * This should be overridden if there is a more efficient way of accessing the data,
	 * like through a servlet where you can request all of the values at once.
	 */
	public ArrayList<Element> getValues(LocationList locs) throws IOException {
		ArrayList<Element> vals = new ArrayList<Element>();
		
		for (Location loc : locs) {
			vals.add(this.getValue(loc));
		}
		
		return vals;
	}
	
	public boolean hasDataForLocation(Location loc, boolean checkValid) {
		if (this.getApplicableRegion().isLocationInside(loc)) {
			if (checkValid) {
				try {
					Element val = this.getValue(loc);
					return this.isValueValid(val);
				} catch (IOException e) {
					return false;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public ParameterList getAdjustableParameterList() {
		return paramList;
	}
	
}
