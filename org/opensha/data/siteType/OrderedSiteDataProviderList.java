package org.opensha.data.siteType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.data.Location;
import org.opensha.data.NamedObjectAPI;
import org.opensha.data.region.GeographicRegion;
import org.opensha.data.siteType.impl.CVM2BasinDepth;
import org.opensha.data.siteType.impl.CVM4BasinDepth;
import org.opensha.data.siteType.impl.USGSBayAreaBasinDepth;
import org.opensha.data.siteType.impl.WaldGlobalVs2007;
import org.opensha.data.siteType.impl.WillsMap2000;
import org.opensha.data.siteType.impl.WillsMap2006;
import org.opensha.metadata.XMLSaveable;
import org.opensha.util.XMLUtils;

public class OrderedSiteDataProviderList implements Iterable<SiteDataAPI<?>>, XMLSaveable {
	
	public static final String XML_METADATA_NAME = "OrderedSiteDataProviderList";
	
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
		return getCheckedDataFromProvider(provider, loc, checkValues);
	}
	
	private SiteDataValue<?> getCheckedDataFromProvider(SiteDataAPI provider, Location loc, boolean checkValid) throws IOException {
		if (provider.hasDataForLocation(loc, false)) {
			SiteDataValue<?> val = provider.getAnnotatedValue(loc);
			if (!checkValid || provider.isValueValid(val.getValue())) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * This method returns a list of the data from every enabled provider
	 * 
	 * @param loc
	 * @return
	 */
	public ArrayList<SiteDataValue<?>> getAllAvailableData(Location loc) {
		ArrayList<SiteDataValue<?>> vals = new ArrayList<SiteDataValue<?>>();
		
		for (int i=0; i<providers.size(); i++) {
			if (!this.isEnabled(i))
				continue;
			SiteDataAPI<?> provider = providers.get(i);
			try {
				SiteDataValue<?> val = provider.getAnnotatedValue(loc);
				if (val != null) {
					vals.add(val);
				}
			} catch (IOException e) {
				System.out.println("IOException...skipping provider: " + provider.getShortName());
				continue;
			}
		}
		
		return vals;
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
			SiteDataAPI<?> provider = providers.get(i);
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
	
	public void set(int index, SiteDataAPI<?> data) {
		this.providers.set(index, data);
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
	
	/**
	 * Creates the default list of site data providers:
	 * 
	 * <UL>
	 * <LI> 1. Wills 2006 (servlet access)
	 * <LI> 2. Wills 2000 (servlet access)
	 * <LI> 3. CVM 4 Depth 2.5 (servlet access)
	 * <LI> 4. CVM 4 Depth 1.0 (servlet access)
	 * <LI> 5. USGS Bay Area Depth 2.5 (servlet access)
	 * <LI> 6. USGS Bay Area Depth 1.0 (servlet access)
	 * <LI> 7. CVM 2 Depth 2.5 (servlet access)
	 * </UL>
	 * 
	 * @return
	 */
	public static OrderedSiteDataProviderList createSiteDataProviderDefaults() {
		ArrayList<SiteDataAPI<?>> providers = new ArrayList<SiteDataAPI<?>>();
		
		/*		Wills 2006			*/
		try {
			providers.add(new WillsMap2006());
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*		Wills 2000			*/
		providers.add(new WillsMap2000());
		/*		CVM 4 Depth 2.5		*/
		try {
			providers.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*		CVM 4 Depth 1.0		*/
		try {
			providers.add(new CVM4BasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*		USGS Bay Area Depth 2.5		*/
		try {
			providers.add(new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_2_5));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*		USGS Bay Area Depth 1.0		*/
		try {
			providers.add(new USGSBayAreaBasinDepth(SiteDataAPI.TYPE_DEPTH_TO_1_0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*		CVM 2 Depth 2.5		*/
		try {
			providers.add(new CVM2BasinDepth());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new OrderedSiteDataProviderList(providers);
	}
	
	/**
	 * Same as createSiteDataProviderDefaults, but returns a cached version of each one
	 * 
	 * @return
	 */
	public static OrderedSiteDataProviderList createCachedSiteDataProviderDefaults() {
		OrderedSiteDataProviderList list = createSiteDataProviderDefaults();
		for (int i=0; i<list.size(); i++) {
			CachedSiteDataWrapper<?> cached = new CachedSiteDataWrapper(list.getProvider(i));
			list.set(i, cached);
		}
		return list;
	}
	
	/**
	 * Creates the debugging list of site data providers:
	 * 
	 * @return
	 */
	public static OrderedSiteDataProviderList createDebugSiteDataProviders() {
		OrderedSiteDataProviderList list = createSiteDataProviderDefaults();
		try {
			list.add(new WaldGlobalVs2007());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public void printList() {
		int size = size();
		for (int i=0; i<size; i++) {
			SiteDataAPI<?> provider = this.getProvider(i);
			boolean enabled = this.isEnabled(i);
			
			if (enabled)
				System.out.println(i + ". " + provider);
			else
				System.out.println(i + ". <disabled> " + provider);
		}
	}

	public Element toXMLMetadata(Element root) {
		Element el = root.addElement(XML_METADATA_NAME);
		
		Element listEl = el.addElement("ProviderList");
		
		for (int i=0; i<size(); i++) {
			SiteDataAPI<?> provider = this.getProvider(i);
			
			Element provEl = listEl.addElement("Provider");
			provEl.addAttribute("Priority", i + "");
			provEl.addAttribute("Enabled", this.isEnabled(i) + "");
			
			provider.toXMLMetadata(provEl);
		}
		
		return root;
	}
	
	public static OrderedSiteDataProviderList fromXMLMetadata(Element orderedListEl) throws IOException {
		Element listEl = orderedListEl.element("ProviderList");
		
		Iterator<Element> it = listEl.elementIterator();
		
		ArrayList<SiteDataAPI<?>> providers = new ArrayList<SiteDataAPI<?>>();
		ArrayList<Boolean> enableds = new ArrayList<Boolean>();
		ArrayList<Integer> priorities = new ArrayList<Integer>();
		
		while (it.hasNext()) {
			Element provEl = it.next();
			int priority = Integer.parseInt(provEl.attributeValue("Priority"));
			boolean enabled = Boolean.parseBoolean(provEl.attributeValue("Enabled"));
			Element subEl = provEl.element(SiteDataAPI.XML_METADATA_NAME);
			
			SiteDataAPI<?> provider = AbstractSiteData.fromXMLMetadata(subEl);
			
			providers.add(provider);
			priorities.add(priority);
			enableds.add(enabled);
		}
		
		ArrayList<SiteDataAPI<?>> ordered = new ArrayList<SiteDataAPI<?>>();
		ArrayList<Boolean> orderedEnableds = new ArrayList<Boolean>();
		
		for (int i=0; i<providers.size(); i++) {
			SiteDataAPI<?> provider = null;
			boolean enabled = false;
			for (int j=0; j<priorities.size(); j++) {
				int priority = priorities.get(j);
				if (priority == i) {
					provider = providers.get(j);
					enabled = enableds.get(j);
					break;
				}
			}
			
			if (provider ==  null) {
				throw new RuntimeException("Malformed list!");
			}
			
			ordered.add(provider);
			orderedEnableds.add(enabled);
		}
		
		OrderedSiteDataProviderList list = new OrderedSiteDataProviderList(ordered);
		
		for (int i=0; i<orderedEnableds.size(); i++) {
			list.setEnabled(i, orderedEnableds.get(i));
		}
		
		return list;
	}
	
	public static void main(String args[]) throws IOException {
		System.out.println("Orig:");
		OrderedSiteDataProviderList list = createSiteDataProviderDefaults();
		list.setEnabled(0, false);
		list.setEnabled(3, false);
		list.printList();
		
		Document doc = XMLUtils.createDocumentWithRoot();
		Element root = doc.getRootElement();
		
		list.toXMLMetadata(root);
		
		Element el = root.element(XML_METADATA_NAME);
		
		XMLUtils.writeDocumentToFile("/tmp/list.xml", doc);
		
		list = OrderedSiteDataProviderList.fromXMLMetadata(el);
		
		System.out.println("After:");
		
		list.printList();
	}
}
