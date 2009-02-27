package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.opensha.data.Location;
import org.opensha.data.NamedObjectAPI;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.impl.CVM2BasinDepth;
import org.opensha.data.siteType.impl.CVM4BasinDepth;
import org.opensha.data.siteType.impl.WillsMap2000;
import org.opensha.data.siteType.impl.WillsMap2006;

public class OrderedSiteDataProviderList implements Iterable<SiteDataAPI<?>> {
	
	// ordered list of providers...0 is highest priority
	private ArrayList<SiteDataAPI<?>> providers;
	private ArrayList<Boolean> enabled = new ArrayList<Boolean>();
	private boolean checkValues = true;
	
	public OrderedSiteDataProviderList(ArrayList<SiteDataAPI<?>> providers) {
		this.providers = providers;
		for (int i=0; i<providers.size(); i++)
			enabled.add(true);
	}
	
	/**
	 * Returns the best provider with data for the given location, or null if no
	 * provider is suitable for the given location.
	 * 
	 * @return
	 */
	public SiteDataAPI<?> getProviderForLocation(Location loc) {
		for (int i=0; i<providers.size(); i++) {
			if (!this.isEnabled(i))
				continue;
			SiteDataAPI<?> data = providers.get(i);
			
			GeographicRegion region = data.getApplicableRegion();
			// skip this one if the site's not in it's applicable region
			if (data.hasDataForLocation(loc, checkValues)) {
				return data;
			}
		}
		return null;
	}
	
	/**
	 * Returns the best data value for the given location, with metadata
	 * 
	 * @param loc
	 * @return
	 * @throws IOException
	 */
	public SiteDataValue<?> getPreferredValue(Location loc) throws IOException {
		for (int i=0; i<providers.size(); i++) {
			if (!this.isEnabled(i))
				continue;
			SiteDataAPI provider = providers.get(i);
			
			SiteDataValue<?> val = this.getCheckedDataFromProvider(provider, loc);
			
			if (val != null)
				return val;
		}
		return null;
	}
	
	private SiteDataValue<?> getCheckedDataFromProvider(SiteDataAPI provider, Location loc) throws IOException {
		if (provider.hasDataForLocation(loc, false)) {
			SiteDataValue<?> val = provider.getAnnotatedValue(loc);
			if (!checkValues || provider.isValueValid(val.getValue())) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * This method returns a list of the best available data for this location,
	 * where "best" is defined by the order of this provider list.
	 * 
	 * The result will have, at most, one of each site data type (so, for example,
	 * if there are multiple Vs30 sources, only the "best" one will be used).
	 * 
	 * @param loc
	 * @return
	 */
	public ArrayList<SiteDataValue<?>> getBestAvailableData(Location loc) {
		ArrayList<SiteDataValue<?>> vals = new ArrayList<SiteDataValue<?>>();
		ArrayList<String> completedTypes = new ArrayList<String>();
		
		for (int i=0; i<providers.size(); i++) {
			if (!this.isEnabled(i))
				continue;
			SiteDataAPI provider = providers.get(i);
			String type = provider.getType();
			// if we already have this data type, then skip it
			if (completedTypes.contains(type))
				continue;
			
			try {
				SiteDataValue<?> val = this.getCheckedDataFromProvider(provider, loc);
				if (val != null) {
					vals.add(val);
					completedTypes.add(type);
				}
			} catch (IOException e) {
				System.out.println("IOException...skipping provider: " + provider.getShortName());
				continue;
			}
		}
		
		return vals;
	}
	
	public static final OrderedSiteDataProviderList createSiteTypeDefaults() {
		ArrayList<SiteDataAPI<?>> providers = new ArrayList<SiteDataAPI<?>>();
		
		try {
			providers.add(new WillsMap2006());
		} catch (IOException e) {
			e.printStackTrace();
		}
		providers.add(new WillsMap2000());
		try {
			providers.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			providers.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		providers.add(new CVM2BasinDepth());
		
		return new OrderedSiteDataProviderList(providers);
	}
	
	public int size() {
		return providers.size();
	}
	
	public ArrayList<SiteDataAPI<?>> getList() {
		ArrayList<SiteDataAPI<?>> list = new ArrayList<SiteDataAPI<?>>();
		for (int i=0; i<providers.size(); i++) {
			if (!this.isEnabled(i))
				continue;
			SiteDataAPI<?> data = providers.get(i);
			list.add(data);
		}
		return list;
	}
	
	public int getIndexOf(SiteDataAPI<?> data) {
		return providers.indexOf(data);
	}
	
	public SiteDataAPI<?> remove(int index) {
		this.enabled.remove(index);
		return this.providers.remove(index);
	}
	
	public void add(SiteDataAPI<?> data) {
		this.providers.add(data);
		this.enabled.add(true);
	}
	
	public void add(int index, SiteDataAPI<?> data) {
		this.providers.add(index, data);
		this.enabled.add(index, true);
	}
	
	public void promote(int index) {
		this.swap(index, index - 1);
	}
	
	public void demote(int index) {
		this.swap(index, index + 1);
	}
	
	public void swap(int index1, int index2) {
		SiteDataAPI<?> one = providers.get(index1);
		boolean enabledOne = enabled.get(index1);
		SiteDataAPI<?> two = providers.get(index2);
		boolean enabledTwo = enabled.get(index2);
		
		providers.set(index1, two);
		providers.set(index2, one);
		
		enabled.set(index1, new Boolean(enabledTwo));
		enabled.set(index2, new Boolean(enabledOne));
	}
	
	public SiteDataAPI<?> getProvider(int index) {
		return providers.get(index);
	}
	
	public boolean isEnabled(int index) {
		return enabled.get(index);
	}
	
	public void setEnabled(int index, boolean enabled) {
		this.enabled.set(index, enabled);
	}

	public Iterator<SiteDataAPI<?>> iterator() {
		return providers.iterator();
	}
}
