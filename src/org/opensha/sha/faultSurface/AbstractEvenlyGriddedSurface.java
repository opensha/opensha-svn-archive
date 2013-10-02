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
import java.util.Iterator;
import java.util.ListIterator;

import org.opensha.commons.data.Container2DImpl;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.LocationVector;
import org.opensha.commons.geo.Region;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;


/**
 * <b>Title:</b> EvenlyGriddedSurface<p>
 * <b>Description:</b>
 *
 * This represents 2D container of Location objects defining a geographical surface.
 * There are no constraints on what locations are put where (this is specified by subclasses), 
 * but the presumption is that the the grid of locations map out the surface in some evenly 
 * discretized way.  It is also presumed that the zeroeth row represent the top edge (or trace). <p>
 * 
 * There are also methods for getting info about the surface (e.g., ave dip, ave strike, and various distance metrics). <p>
 *
 * @author revised by field
 * @created
 * @version    1.0
 */
public abstract class AbstractEvenlyGriddedSurface  extends Container2DImpl<Location> implements EvenlyGriddedSurface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Class name for debugging. */
	protected final static String C = "EvenlyGriddedSurface";
	/** If true print out debug statements. */
	protected final static boolean D = false;

	protected double gridSpacingAlong;
	protected double gridSpacingDown;
	protected Boolean sameGridSpacing;
	
	// for distance measures
	Location siteLocForDistCalcs= new Location(Double.NaN,Double.NaN);
	Location siteLocForDistXCalc= new Location(Double.NaN,Double.NaN);
	double distanceJB, distanceSeis, distanceRup, distanceX;
	
	
	// no argument constructor needed by subclasses
	public AbstractEvenlyGriddedSurface() {}
	
	
	/**
	 *  Constructor for the EvenlyGriddedSurface object; this sets both the grid spacing along
	 *  and down dip to the value passed in
	 *
	 * @param  numRows  Number of grid points along width of fault
	 * @param  numCols  Number of grid points along length of fault
	 * @param  gridSpacing  Grid Spacing
	 */
	public AbstractEvenlyGriddedSurface( int numRows, int numCols,double gridSpacing ) {
		super( numRows, numCols );
		gridSpacingAlong = gridSpacing;
		gridSpacingDown = gridSpacing;
		sameGridSpacing = true;
	}
	
	/**
	 *  Constructor for the EvenlyGriddedSurface object; this sets both the grid spacing along
	 *  and down dip to the value passed in
	 *
	 * @param  numRows  Number of grid points along width of fault
	 * @param  numCols  Number of grid points along length of fault
	 * @param  gridSpacing  Grid Spacing
	 */
	public AbstractEvenlyGriddedSurface( int numRows, int numCols,double gridSpacingAlong, double gridSpacingDown) {
		super( numRows, numCols );
		this.gridSpacingAlong = gridSpacingAlong;
		this.gridSpacingDown = gridSpacingDown;
		if(gridSpacingAlong == gridSpacingDown)
			sameGridSpacing = true;
		else
			sameGridSpacing = false;
	}



	@Override
	public LocationList getEvenlyDiscritizedListOfLocsOnSurface() {
		LocationList locList = new LocationList();
		Iterator<Location> it = listIterator();
		while(it.hasNext()) locList.add((Location)it.next());
		return locList;
	}



	/**
	 * Returns the grid spacing along strike
	 * @return
	 */
	public double getGridSpacingAlongStrike() {
		return this.gridSpacingAlong;
	}

	/**
	 * returns the grid spacing down dip
	 * @return
	 */
	public double getGridSpacingDownDip() {
		return this.gridSpacingDown;
	}
	
	/**
	 * tells whether along-strike and down-dip grid spacings are the same
	 * @return
	 */
	public Boolean isGridSpacingSame() {
		return this.sameGridSpacing;
	}
	
	@Override
	public LocationList getEvenlyDiscritizedPerimeter() {
		return GriddedSurfaceUtils.getEvenlyDiscritizedPerimeter(this);
	}
	
	@Override
	/**
	 * Default is to return the evenly discretized version
	 */
	public LocationList getPerimeter() {
		return getEvenlyDiscritizedPerimeter();
	}

	/**
	 * gets the location from the 2D container
	 * @param row
	 * @param column
	 * @return
	 */
	public Location getLocation(int row, int column) {
		return get(row, column);
	}


	@Override
	public ListIterator<Location> getLocationsIterator() {
		return listIterator();
	}

	/**
	 * Gets a specified row as a fault trace
	 * @param row
	 * @return
	 */
	public FaultTrace getRowAsTrace(int row) {
		FaultTrace trace = new FaultTrace(null);
		for(int col=0; col<getNumCols(); col++)
			trace.add(get(row, col));
		return trace;
	}
	
	/**
	 * This returns the minimum distance as the minimum among all location
	 * pairs between the two surfaces
	 * @param surface RuptureSurface 
	 * @return distance in km
	 */
	@Override
	public double getMinDistance(RuptureSurface surface) {
		return GriddedSurfaceUtils.getMinDistanceBetweenSurfaces(surface, this);
	}
	
	private void setPropagationDistances() {
		double[] dists = GriddedSurfaceUtils.getPropagationDistances(this, siteLocForDistCalcs);
		distanceRup = dists[0];
		distanceJB = dists[1];
		distanceSeis = dists[2];
	}
	
	
	/**
	 * This returns rupture distance (kms to closest point on the 
	 * rupture surface), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return 
	 */
	public synchronized double getDistanceRup(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceRup;
	}

	/**
	 * This returns distance JB (shortest horz distance in km to surface projection 
	 * of rupture), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public synchronized double getDistanceJB(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceJB;
	}

	/**
	 * This returns "distance seis" (shortest distance in km to point on rupture 
	 * deeper than 3 km), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	public synchronized double getDistanceSeis(Location siteLoc){
		if(!siteLocForDistCalcs.equals(siteLoc)) {
			siteLocForDistCalcs = siteLoc;
			setPropagationDistances();
		}
		return distanceSeis;
	}

	/**
	 * This returns distance X (the shortest distance in km to the rupture 
	 * trace extended to infinity), where values >= 0 are on the hanging wall
	 * and values < 0 are on the foot wall.  The location is assumed to be at zero
	 * depth (for numerical expediency).
	 * @return
	 */
	public synchronized double getDistanceX(Location siteLoc){
		if(!siteLocForDistXCalc.equals(siteLoc)) {
			siteLocForDistXCalc = siteLoc;
			distanceX = GriddedSurfaceUtils.getDistanceX(getEvenlyDiscritizedUpperEdge(), siteLocForDistXCalc);
		}
		return distanceX;
	}
	
	

	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		return getRowAsTrace(0);
	}

	@Override
	public FaultTrace getEvenlyDiscritizedLowerEdge() {
		return getRowAsTrace(getNumRows()-1);
	}
	
	@Override
	/**
	 * Default is to return the evenly discretized version
	 */
	public FaultTrace getUpperEdge() {
		return getEvenlyDiscritizedUpperEdge();
	}



	@Override
	public double getFractionOfSurfaceInRegion(Region region) {
		double numInside=0;
		for(Location loc: this) {
			if(region.contains(loc))
				numInside += 1;
		}
		return numInside/size();
	}


	/**
	 * This returns the first location on row zero
	 * (which should be the same as the first loc of the FaultTrace)
	 */
	@Override
	public Location getFirstLocOnUpperEdge() {
		return get(0,0);
	}
	
	/**
	 * This returns the last location on row zero (which may not be the 
	 * same as the last loc of the FaultTrace depending on the discretization)
	 */
	@Override
	public Location getLastLocOnUpperEdge() {
		return get(0,getNumCols()-1);
	}

	@Override
	public double getAveLength() {
		return getGridSpacingAlongStrike() * (getNumCols()-1);
	}

	@Override
	public double getAveWidth() {
		return getGridSpacingDownDip() * (getNumRows()-1);
	}

	@Override
	public double getArea() {
		return getAveWidth()*getAveLength();
	}
	
	@Override
	public double getAveGridSpacing() {
		return (gridSpacingAlong+gridSpacingDown)/2;
	}
	
	@Override
	public String getInfo() {
	      return GriddedSurfaceUtils.getSurfaceInfo(this);
	}
	
	@Override
	public boolean isPointSurface() {
		return (size() == 1);
	}
	
}
