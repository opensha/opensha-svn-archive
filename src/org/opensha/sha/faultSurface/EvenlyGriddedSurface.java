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

package org.opensha.sha.faultSurface;

import java.io.Serializable;
import java.util.ListIterator;

import org.opensha.commons.data.Container2D;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;


/**
 * <b>Title:</b> EvenlyGriddedSurface<p>
 * <b>Description:</b>
 *
 * This represents a geographical
 * surface of Location objects slicing through or on the surface of the earth.
 * Recall that a Container2DAPI represents a collection of Objects in
 * a matrix, or grid, accessed by row and column inedexes. All GriddedSurfaces
 * do is to constrain the object at each grid point to be a Location object.
 * There are also methods for getting info about the surface (e.g., ave dip,
 * ave strike, etc.). <p>
 *
 * There are no constraints on what locations are put where, but the presumption
 * is that the the grid of locations map out the surface .
 * it is also presumed that the zeroeth row represent the top edge (or trace). <p>
 *
 * @author field
 * @created
 * @version    1.0
 */
public interface EvenlyGriddedSurface extends RuptureSurface, Container2D<Location>, Serializable {


	/**
	 * returns the grid spacing along strike
	 *
	 * @return
	 */
	public double getGridSpacingAlongStrike();
	
	/**
	 * returns the grid spacing down dip
	 *
	 * @return
	 */
	public double getGridSpacingDownDip();
	
	/**
	 * this tells whether along strike and down dip grip
	 * @return
	 */
	public Boolean isGridSpacingSame();

	/** Common debug string that most Java classes implement */
	public String toString();

	/**
	 * Returns the Metadata for the surface
	 * @return String
	 */
	public String getSurfaceMetadata();

	/**
	 * Method to get location...same as get(row, column)
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public Location getLocation(int row, int column);
	
	/**
	 * This returns a given row as a FaultTrace object
	 * @param row
	 * @return FaultTrace
	 */
	public FaultTrace getRowAsTrace(int row);

}
