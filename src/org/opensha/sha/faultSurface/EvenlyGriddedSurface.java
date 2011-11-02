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
public abstract class EvenlyGriddedSurface  extends Container2DImpl<Location> implements GriddedSurfaceInterface, Serializable {

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
	Location locForDistCalcs=null;
	double distanceJB, distanceSeis, distanceRup, distanceX;
	
	
	// no argument constructor needed by subclasses
	public EvenlyGriddedSurface() {}
	
	
	/**
	 *  Constructor for the EvenlyGriddedSurface object; this sets both the grid spacing along
	 *  and down dip to the value passed in
	 *
	 * @param  numRows  Number of grid points along width of fault
	 * @param  numCols  Number of grid points along length of fault
	 * @param  gridSpacing  Grid Spacing
	 */
	public EvenlyGriddedSurface( int numRows, int numCols,double gridSpacing ) {
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
	public EvenlyGriddedSurface( int numRows, int numCols,double gridSpacingAlong, double gridSpacingDown) {
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



	/** Prints out each location and fault information for debugging */
	public String toString(){
		char TAB = '\t';
		StringBuffer b = new StringBuffer();
		b.append( C + '\n');

		b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

		String superStr = super.toString();
		//int index = superStr.indexOf('\n');
		//if( index > 0 ) superStr = superStr.substring(index + 1);
		b.append( '\n' + superStr );

		return b.toString();
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
		LocationList locList = new LocationList();
		for(int c=0;c<getNumCols();c++) locList.add(get(0, c));
		for(int r=0;r<getNumRows();r++) locList.add(get(r, getNumCols()-1));
		for(int c=getNumCols()-1;c>=0;c--) locList.add(get(getNumRows()-1, c));
		for(int r=getNumRows()-1;r>=0;r--) locList.add(get(r, 0));
		return locList;
	}



	/**
	 * Returns the Surface Metadata with the following info:
	 * <ul>
	 * <li>AveDip
	 * <li>Surface length
	 * <li>Surface DownDipWidth
	 * <li>GridSpacing
	 * <li>NumRows
	 * <li>NumCols
	 * <li>Number of locations on surface
	 * <p>Each of these elements are represented in Single line with tab("\t") delimitation.
	 * <br>Then follows the location of each point on the surface with the comment String
	 * defining how locations are represented.</p>
	 * <li>#Surface locations (Lat Lon Depth)
	 * <p>Then until surface locations are done each line is the point location on the surface.
	 *
	 * </ul>
	 * @return String
	 */
	public String getSurfaceMetadata() {
		String surfaceMetadata = "";
		surfaceMetadata += (float)getAveLength() + "\t";
		surfaceMetadata += (float)getAveWidth() + "\t";
		surfaceMetadata += (float)Double.NaN + "\t";
		int numRows = getNumRows();
		int numCols = getNumCols();
		surfaceMetadata += numRows + "\t";
		surfaceMetadata += numCols + "\t";
		surfaceMetadata += (numRows * numCols) + "\n";
		surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
		ListIterator<Location> it = listIterator();
		while (it.hasNext()) {
			Location loc = (Location) it.next();
			surfaceMetadata += (float)loc.getLatitude()+"\t";
			surfaceMetadata += (float)loc.getLongitude()+"\t";
			surfaceMetadata += (float)loc.getDepth()+"\n";
		}
		return surfaceMetadata;
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
	 * Calculate the minimum distance of this surface from user provided surface
	 * @param surface EvenlyGriddedSurface 
	 * @return distance in km
	 */
	public double getMinDistance(EvenlyGriddedSurface surface) {
		Iterator<Location> it = listIterator();
		double min3dDist = Double.POSITIVE_INFINITY;
		double dist;
		// find distance between all location pairs in the two surfaces
		while(it.hasNext()) { // iterate over all locations in this surface
			Location loc1 = (Location)it.next();
			Iterator<Location> it2 = surface.listIterator();
			while(it2.hasNext()) { // iterate over all locations on the user provided surface
				Location loc2 = (Location)it2.next();
				dist = LocationUtils.linearDistanceFast(loc1, loc2);
				if(dist<min3dDist){
					min3dDist = dist;
				}
			}
		}
		return min3dDist;
	}
	
	private void setPropagationDistances() {
		double[] dists = GriddedSurfaceUtils.getPropagationDistances(this, locForDistCalcs);
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
	public double getDistanceRup(Location loc){
		if(!locForDistCalcs.equals(loc)) {
			locForDistCalcs = loc;
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
	public double getDistanceJB(Location loc){
		if(!locForDistCalcs.equals(loc)) {
			locForDistCalcs = loc;
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
	public double getDistanceSeis(Location loc){
		if(!locForDistCalcs.equals(loc)) {
			locForDistCalcs = loc;
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
	public double getDistanceX(Location loc){
		if(!locForDistCalcs.equals(loc)) {
			locForDistCalcs = loc;
			distanceX = GriddedSurfaceUtils.getDistanceX(this, locForDistCalcs);
		}
		return distanceX;
	}
	
	

	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		return getRowAsTrace(0);
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


}
