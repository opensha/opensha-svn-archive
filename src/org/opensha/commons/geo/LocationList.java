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

	public ArrayList<LocationList> split(int pieceSize) {
		ArrayList<LocationList> lists = new ArrayList<LocationList>();

		// quickly handle the trivial case
		if (pieceSize <= 0 || this.size() <= pieceSize) {
			lists.add(this);
			return lists;
		}

		LocationList cur = new LocationList();

		for (int i = 0; i < this.size(); i++) {
			if (i % pieceSize == 0 && i > 0) {
				lists.add(cur);
				cur = new LocationList();
			}
			cur.add(this.get(i));
		}

		if (cur.size() > 0) lists.add(cur);

		return lists;
	}

	/**
	 * This computes the distance (in km) between the given loc and the closest
	 * point in the LocationList
	 * 
	 * @param loc
	 * @return
	 */
	public double getHorzDistToClosestLocation(Location loc) {
		double min = Double.MAX_VALUE, temp;
		Iterator<Location> it = iterator();
		while (it.hasNext()) {
			temp = RelativeLocation.getHorzDistance(loc, it.next());
			if (temp < min) min = temp;
		}
		return min;
	}

	/**
	 * This computes the shortest horizontal distance (in km) from the given loc
	 * to any point on the line defined by connecting the points in this
	 * location list. This is approximate in that it uses the
	 * RelativeLocation.getApproxHorzDistToLine(*) method
	 * 
	 * @param loc
	 * @return TODO cleanup
	 */
	public double getMinHorzDistToLine(Location loc) {
		double min = Double.MAX_VALUE, temp;

		// TODO this should loop over the points and then only solve at the
		// segment ??
		// loop over each line segment
		for (int i = 1; i < size(); i++) {
			temp = RelativeLocation.getApproxHorzDistToLine(get(i - 1), get(i),
					loc);
			if (temp < min) min = temp;
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
