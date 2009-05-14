package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.region.GeographicRegion;
import org.opensha.param.ParameterAPI;
import org.opensha.param.ParameterList;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeListener;

/**
 * This is a class that takes and SiteDataAPI class and wraps it to add a cache for faster retrieval
 * of data in situations where the same location may be requested often.
 * 
 * @author Kevin Milner
 *
 * @param <Element>
 */
public class CachedSiteDataWrapper<Element> implements SiteDataAPI<Element>, ParameterChangeListener {
	
	/** If true print out debug statements. */
	protected final static boolean D = false;
	
	private int cacheSize;
	private int cacheSizeMinusOne;
	private SiteDataAPI<Element> provider;
	
	private ArrayList<Location> locsCache = new ArrayList<Location>();
	private ArrayList<Element> dataCache = new ArrayList<Element>();
	
	public CachedSiteDataWrapper(SiteDataAPI<Element> provider) {
		this(provider, 5);
	}
	
	public CachedSiteDataWrapper(SiteDataAPI<Element> provider, int cacheSize) {
		this.cacheSize = cacheSize;
		cacheSizeMinusOne = cacheSize - 1;
		this.provider = provider;
		ParameterList params = this.provider.getAdjustableParameterList();
		for (ParameterAPI param : params) {
			param.addParameterChangeListener(this);
		}
	}
	
	public SiteDataValue<Element> getAnnotatedValue(Location loc) throws IOException {
		Element val = this.getValue(loc);
		return new SiteDataValue<Element>(provider.getDataType(), provider.getDataMeasurementType(), val, provider.getName());
	}
	
	/**
	 * Returns the value from the underlying site data object, keeping track of the value in
	 * the cache. If the value is already in the cache, then just use that value.
	 */
	public Element getValue(Location loc) throws IOException {
		int size = locsCache.size();
		// first we see if its in the cache
		for (int i=0; i<size; i++) {
			Location dataLoc = locsCache.get(i);
			if (loc.equals(dataLoc)) {
				if (i > 0) {
					// move it up to the front of the cache
					Element data = dataCache.remove(i);
					locsCache.remove(i);
					dataCache.add(i, data);
					locsCache.add(i, dataLoc);
					return data;
				}
				return dataCache.get(i);
			}
		}
		// if we made it this far, then its not in the cache
		Element data = provider.getValue(loc);
		
		// if we need, make room
		if (size == cacheSize) {
			locsCache.remove(cacheSizeMinusOne);
			dataCache.remove(cacheSizeMinusOne);
		}
		
		locsCache.add(0, loc);
		dataCache.add(0, data);
		
		return data;
	}
	
	/**
	 * Clear the cache. This is called if parameters are updated.
	 */
	public void clearCache() {
		this.locsCache.clear();
		this.dataCache.clear();
	}

	public ArrayList<Element> getValues(LocationList locs) throws IOException {
		return provider.getValues(locs);
	}

	public ParameterList getAdjustableParameterList() {
		return provider.getAdjustableParameterList();
	}

	public GeographicRegion getApplicableRegion() {
		return provider.getApplicableRegion();
	}

	public Location getClosestDataLocation(Location loc) throws IOException {
		return provider.getClosestDataLocation(loc);
	}

	public String getMetadata() {
		return provider.getMetadata();
	}

	public String getName() {
		return provider.getName();
	}

	public ParameterListEditor getParameterListEditor() {
		return provider.getParameterListEditor();
	}

	public double getResolution() {
		return provider.getResolution();
	}

	public String getShortName() {
		return provider.getShortName();
	}

	public String getDataType() {
		return provider.getDataType();
	}

	public String getDataMeasurementType() {
		return provider.getDataMeasurementType();
	}

	public boolean hasDataForLocation(Location loc, boolean checkValid) {
		return provider.hasDataForLocation(loc, checkValid);
	}

	public boolean isValueValid(Element el) {
		return provider.isValueValid(el);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	public org.dom4j.Element toXMLMetadata(org.dom4j.Element root) {
		return provider.toXMLMetadata(root);
	}

	public SiteDataValueList<Element> getAnnotatedValues(LocationList locs)
			throws IOException {
		return provider.getAnnotatedValues(locs);
	}

	public void parameterChange(ParameterChangeEvent event) {
		if (D) System.out.println("Parameter changed...clearing cache!");
		this.clearCache();
	}

}
