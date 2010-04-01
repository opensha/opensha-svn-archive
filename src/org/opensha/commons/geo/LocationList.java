/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.geo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.metadata.XMLSaveable;

/**
 * A customized <code>ArrayList</code> of <code>Location</code>s. In addition to
 * providing the functionality of the <code>List</code> interface, this class
 * provides several custom methods for querying and manipulating a collection of
 * <code>Location</code>s.
 * 
 * @author Peter Powers
 * @author Steven W. Rock
 * @version $Id$
 */
public class LocationList extends ArrayList<Location> implements Serializable,
		XMLSaveable {

	private static final long serialVersionUID = 1L;

	public static final String XML_METADATA_NAME = "LocationList";

	/**
	 * Convenience method to reverse the <code>Location</code>s in this list.
	 * Simply calls <code>Collections.reverse()</code>.
	 */
	public void reverse() {
		Collections.reverse(this);
	}

	/**
	 * Breaks this <code>LocationList</code> into multiple parts. If 
	 * <code>size</code> is less than or equal to the size of this list, a
	 * <code>List&lt;LocationList&gt;</code> containing only this 
	 * <code>LocationList</code> is returned. The last element in the
	 * <code>List&lt;LocationList&gt;</code> will be a <code>LocationList</code>
	 * of <code>size</code> or fewer <code>Location</code>s.
	 * 
	 * @param size of the smaller lists
	 * @return a <code>List&lt;LocationList&gt;</code> of smaller 
	 *         <code>LocationList</code>s
	 */
	public List<LocationList> split(int size) {
		ArrayList<LocationList> lists = new ArrayList<LocationList>();

		// quickly handle the trivial case
		if (size <= 0 || size() <= size) {
			lists.add(this);
			return lists;
		}

		LocationList cur = new LocationList();

		for (int i = 0; i < size(); i++) {
			if (i % size == 0 && i > 0) {
				lists.add(cur);
				cur = new LocationList();
			}
			cur.add(get(i));
		}

		if (cur.size() > 0) lists.add(cur);

		return lists;
	}

	/**
	 * Computes the horizontal surface distance (in km) to the closest point in 
	 * this list from the supplied <code>Location</code>. This method uses 
	 * {@link RelativeLocation#getHorzDistance(Location, Location)} to compute
	 * the distance.
	 * 
	 * @param loc <code>Location</code> of interest
	 * @return the distance to the closest point in this 
	 *         <code>LocationList</code>
	 * @see RelativeLocation#getHorzDistance(Location, Location)
	 */
	public double minDistToLocation(Location loc) {
		double min = Double.MAX_VALUE;
		double dist = 0;
		for (Location p : this) {
			dist = RelativeLocation.getHorzDistance(loc, p);
			if (dist < min) min = dist;
		}
		return min;
	}

	/**
	 * Computes the shortest horizontal distance (in km) from the supplied
	 * <code>Location</code> to the line defined by connecting the points in 
	 * this <code>LocationList</code>. This method uses 
	 * {@link RelativeLocation#getApproxHorzDistToLine(Location, Location, Location)}
	 * and is inappropriate for for use at large separations (e.g. &gt;200 km).
	 * 
	 * @param loc <code>Location</code> of interest
	 * @return the shortest distance to the line defined by this
	 *         <code>LocationList</code>
	 */
	public double minDistToLine(Location loc) {
		double min = Double.MAX_VALUE;
		double dist = 0;
		for (int i = 1; i < size(); i++) {
			dist = RelativeLocation.getApproxHorzDistToLine(
					get(i - 1), get(i), loc);
			if (dist < min) min = dist;
		}
		return min;
	}

	/**
	 * Overriden to return a deep copy of this <code>LocationList</code>.
	 * 
	 * @return a deep copy of this list
	 */
	@Override
	public LocationList clone() {
		LocationList clone = new LocationList();
		for (Location loc : this) {
			clone.add(loc.clone());
		}
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LocationList)) return false;
		LocationList ll = (LocationList) obj;
		if (size() != ll.size()) return false;
		for (int i = 0; i < size(); i++) {
			if (!(get(i).equals(ll.get(i)))) return false;
		}
		return true;
	}

	// TODO possibly make each smaller to avoid int overrun
	@Override
	public int hashCode() {
		int v = 0;
		for (Location loc : this) {
			v += loc.hashCode();
		}
		return v;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("LocationList size: " + size() + "\n");
		b.append("LocationList data: ");
		for (Location loc : this) {
			b.append(loc + " ");
		}
		return b.toString();
	}

	public Element toXMLMetadata(Element root) {
		Element locs = root.addElement(LocationList.XML_METADATA_NAME);
		for (int i = 0; i < this.size(); i++) {
			Location loc = this.get(i);
			locs = loc.toXMLMetadata(locs);
		}

		return root;
	}

	public static LocationList fromXMLMetadata(Element locationElement) {
		LocationList locs = new LocationList();
		Iterator<Element> it = locationElement.elementIterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals(Location.XML_METADATA_NAME)) {
				locs.add(Location.fromXMLMetadata(el));
			}
		}
		return locs;
	}

}