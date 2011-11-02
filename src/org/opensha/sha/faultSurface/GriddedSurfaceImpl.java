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

import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;


/**
 * <p>Title: GriddedSurface</p>
 *
 * <p>Description: Creates a Arbitrary surface that takes in a list of locations.
 * </p>
 *
 * @author Nitin Gupta
 * @version 1.0
 */

public class GriddedSurfaceImpl extends AbstractEvenlyGriddedSurface {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *  Constructor for the GriddedSurface object
	 *
	 * @param  numRows  Number of grid points along width of fault
	 * @param  numCols  Number of grid points along length of fault
	 */
	public GriddedSurfaceImpl(int numRows, int numCols, double gridSpacing ) {
		super(numRows, numCols, gridSpacing);
	}

	/**
	 * This allows one to set the location
	 * @param row
	 * @param column
	 * @param location
	 */
	public void setLocation(int row, int column, Location location) {
		set(row, column, location);
	}

	@Override
	public double getAveDip() throws UnsupportedOperationException {
		throw new RuntimeException("Method not yet supported (need to implement computation)");
	}

	@Override
	public double getAveStrike() throws UnsupportedOperationException {
		return this.getRowAsTrace(0).getAveStrike();
	}

	@Override
	public double getAveDipDirection() {
		throw new RuntimeException("Method not yet supported (need to implement computation)");
	}

	@Override
	public double getAveRupTopDepth() {
		double dep=0;
		FaultTrace trace = getRowAsTrace(0);
		for(Location loc: trace)
			dep += loc.getDepth();
		return dep/trace.size();
	}

	@Override
	public LocationList getPerimeter() {
		return getEvenlyDiscritizedPerimeter();
	}

	@Override
	public FaultTrace getUpperEdge() {
		return getRowAsTrace(0);
	}



}

