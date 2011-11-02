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

import java.util.ArrayList;
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
 * <b>Title:</b> GriddedSurface<p>
 *
 * <b>Description:</b> Base implementation of the EvenlyGriddedSurfaceAPI.
 *
 * @author
 * @created
 * @version    1.0
 */
public abstract class AbstractEvenlyGriddedSurface
extends Container2DImpl<Location>
implements EvenlyGriddedSurface {

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
	final static double SEIS_DEPTH = 3.0;


	/**
	 * No Argument constructor, called from classes extending it.
	 *
	 */
	protected AbstractEvenlyGriddedSurface(){}

	/**
	 *  Constructor for the GriddedSurface object; this sets both the grid spacing along
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
	 *  Constructor for the GriddedSurface object; this sets both the grid spacing along
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



	final static char TAB = '\t';
	/** Prints out each location and fault information for debugging */
	public String toString(){

		StringBuffer b = new StringBuffer();
		b.append( C + '\n');

		b.append( "Row" + TAB + "Col" + TAB + "Latitude" + TAB + "Longitude" + TAB + "Depth");

		String superStr = super.toString();
		//int index = superStr.indexOf('\n');
		//if( index > 0 ) superStr = superStr.substring(index + 1);
		b.append( '\n' + superStr );

		return b.toString();
	}

	@Override
	public double getGridSpacingAlongStrike() {
		return this.gridSpacingAlong;
	}

	@Override
	public double getGridSpacingDownDip() {
		return this.gridSpacingDown;
	}
	
	
	@Override
	public Boolean isGridSpacingSame() {
		return this.sameGridSpacing;
	}



	/**
	 * Gets the Nth subSurface on the surface
	 *
	 * @param numSubSurfaceCols  Number of grid points in subsurface length
	 * @param numSubSurfaceRows  Number of grid points in subsurface width
	 * @param numSubSurfaceOffsetAlong Number of grid points for offset along strike
	 * @param numSubSurfaceOffsetDown Number of grid points for offset down dip
	 * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
	 *
	 */
	public GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,
			int numSubSurfaceRows, int numSubSurfaceOffsetAlong, int numSubSurfaceOffsetDown, int n) {
		
		// number of subSurfaces along the length of fault
		int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffsetAlong +1);

		// there is only one subSurface
		if(nSubSurfaceAlong <=1) {
			nSubSurfaceAlong=1;
		}
		if(numSubSurfaceCols > numCols) numSubSurfaceCols = numCols;
		if(numSubSurfaceRows > numRows) numSubSurfaceRows = numRows;

		return getNthSubsetSurface(numSubSurfaceCols, numSubSurfaceRows, numSubSurfaceOffsetAlong, numSubSurfaceOffsetDown, nSubSurfaceAlong, n);
		//     throw new RuntimeException("EvenlyGriddeddsurface:getNthSubsetSurface::Inavlid n value for subSurface");
	}


	/**
	 * Gets the Nth subSurface on the surface
	 *
	 * @param numSubSurfaceCols  Number of grid points along length
	 * @param numSubSurfaceRows  Number of grid points along width
	 * @param numSubSurfaceOffsetAlong Number of grid points for offset along strike
	 * @param numSubSurfaceOffsetDown Number of grid points for offset down dip
	 * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
	 *
	 */
	private GriddedSubsetSurface getNthSubsetSurface(int numSubSurfaceCols,int numSubSurfaceRows,
			int numSubSurfaceOffsetAlong,int numSubSurfaceOffsetDown,int nSubSurfaceAlong, int n){
		
		//getting the row number in which that subsetSurface is present
		int startRow = n/nSubSurfaceAlong * numSubSurfaceOffsetDown;

		//getting the column from which that subsetSurface starts
		int startCol = n%nSubSurfaceAlong * numSubSurfaceOffsetAlong;  // % gives the remainder: a%b = a-floor(a/b)*b; a%b = a if b>a

		return (new GriddedSubsetSurface((int)numSubSurfaceRows,(int)numSubSurfaceCols,startRow,startCol,this));
	}


	/**
	 * Gets the Nth subSurface on the surface.
	 *
	 * @param subSurfaceLength  subsurface length in km
	 * @param subSurfaceWidth  subsurface width in km
	 * @param subSurfaceOffset offset in km
	 * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
	 *
	 */
	public GriddedSubsetSurface getNthSubsetSurface(double subSurfaceLength,
			double subSurfaceWidth,
			double subSurfaceOffset,
			int n) {
		return getNthSubsetSurface((int)Math.rint(subSurfaceLength/gridSpacingAlong+1),
				(int)Math.rint(subSurfaceWidth/gridSpacingDown+1),
				(int)Math.rint(subSurfaceOffset/gridSpacingAlong), 
				(int)Math.rint(subSurfaceOffset/gridSpacingDown), n);
	}


	/**
	 * Gets the Nth subSurface centered down dip on the surface. If surface is not perfectly centered,
	 * (numRows-numRowsInRup != even number), rupture is one grid increment closer to top then to bottom.
	 *
	 * @param subSurfaceLength  subsurface length in km
	 * @param subSurfaceWidth  subsurface width in km
	 * @param subSurfaceOffset offset in km
	 * @param n The index of the desired surface (from 0 to (getNumSubsetSurfaces - 1))
	 *
	 */
	public GriddedSubsetSurface getNthSubsetSurfaceCenteredDownDip(double subSurfaceLength,
			double subSurfaceWidth,
			double subSurfaceOffset,
			int n) {

		int numSubSurfaceCols =  (int)Math.rint(subSurfaceLength/gridSpacingAlong+1);
		int startCol = -1;

		// make sure it doesn't extend beyond the end
		if(numSubSurfaceCols>numCols){
			numSubSurfaceCols=numCols;
			startCol=0;
		}
		else {
			startCol = n * (int)Math.rint(subSurfaceOffset/gridSpacingAlong);
		}

		int numSubSurfaceRows = (int)Math.rint(subSurfaceWidth/gridSpacingDown+1);
		int startRow=-1;

		// make sure it doesn't extend beyone the end
		if(numSubSurfaceRows >= numRows){
			numSubSurfaceRows=numRows;
			startRow=0;
		}
		else {
			startRow = (int)Math.floor((numRows-numSubSurfaceRows)/2);  		
		}

		/*
		 System.out.println("subSurfaceLength="+subSurfaceLength+", subSurfaceWidth="+subSurfaceWidth+", subSurfaceOffset="+
				subSurfaceOffset+", numRows="+numRows+", numCols="+numCols+", numSubSurfaceRows="+
				numSubSurfaceRows+", numSubSurfaceCols="+numSubSurfaceCols+", startRow="+startRow+", startCol="+startCol);
		*/
		return (new GriddedSubsetSurface(numSubSurfaceRows,numSubSurfaceCols,startRow,startCol,this));
	}





	/**
	 * Get the subSurfaces on this fault
	 *
	 * @param numSubSurfaceCols  Number of grid points according to length
	 * @param numSubSurfaceRows  Number of grid points according to width
	 * @param numSubSurfaceOffset Number of grid points for offset
	 *
	 */
	public Iterator<GriddedSubsetSurface> getSubsetSurfacesIterator(int numSubSurfaceCols, int numSubSurfaceRows,
			int numSubSurfaceOffsetAlong, int numSubSurfaceOffsetDown) {

		//vector to store the GriddedSurface
		ArrayList<GriddedSubsetSurface> v = new ArrayList<GriddedSubsetSurface>();

		// number of subSurfaces along the length of fault
		int nSubSurfaceAlong = (int)Math.floor((numCols-numSubSurfaceCols)/numSubSurfaceOffsetAlong +1);

		// there is only one subSurface
		if(nSubSurfaceAlong <=1) {
			nSubSurfaceAlong=1;
			numSubSurfaceCols = numCols;
		}

		// number of subSurfaces along fault width
		int nSubSurfaceDown =  (int)Math.floor((numRows-numSubSurfaceRows)/numSubSurfaceOffsetDown +1);

		// one subSurface along width
		if(nSubSurfaceDown <=1) {
			nSubSurfaceDown=1;
			numSubSurfaceRows = numRows;
		}

		//getting the total number of subsetSurfaces
		int totalSubSetSurface = nSubSurfaceAlong * nSubSurfaceDown;
		//emptying the vector
		v.clear();

		//adding each subset surface to the ArrayList
		for(int i=0;i<totalSubSetSurface;++i)
			v.add(getNthSubsetSurface(numSubSurfaceCols,numSubSurfaceRows,numSubSurfaceOffsetAlong,numSubSurfaceOffsetDown,nSubSurfaceAlong,i));

		return v.iterator();
	}



	/**
	 * Get the subSurfaces on this fault
	 *
	 * @param subSurfaceLength  Sub Surface length in km
	 * @param subSurfaceWidth   Sub Surface width in km
	 * @param subSurfaceOffset  Sub Surface offset
	 * @return           Iterator over all subSurfaces
	 */
	public Iterator<GriddedSubsetSurface> getSubsetSurfacesIterator(double subSurfaceLength,
			double subSurfaceWidth,
			double subSurfaceOffset) {

		return getSubsetSurfacesIterator((int)Math.rint(subSurfaceLength/gridSpacingAlong+1),
				(int)Math.rint(subSurfaceWidth/gridSpacingDown+1),
				(int)Math.rint(subSurfaceOffset/gridSpacingAlong),
				(int)Math.rint(subSurfaceOffset/gridSpacingDown));

	}

	/**
	 *
	 * @param subSurfaceLength subSurface length in km
	 * @param subSurfaceWidth  subSurface Width in km
	 * @param subSurfaceOffset subSurface offset in km
	 * @return total number of subSurface along the fault
	 */
	public int getNumSubsetSurfaces(double subSurfaceLength,double subSurfaceWidth,double subSurfaceOffset){

		int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacingAlong+1);
		int widthCols =    (int)Math.rint(subSurfaceWidth/gridSpacingDown+1);
		int offsetColsAlong =   (int)Math.rint(subSurfaceOffset/gridSpacingAlong);
		int offsetColsDown =   (int)Math.rint(subSurfaceOffset/gridSpacingDown);

		// number of subSurfaces along the length of fault
		int nSubSurfaceAlong = (int)Math.floor((numCols-lengthCols)/offsetColsAlong +1);

		// there is only one subSurface
		if(nSubSurfaceAlong <=1) {
			nSubSurfaceAlong=1;
		}

		// nnmber of subSurfaces along fault width
		int nSubSurfaceDown =  (int)Math.floor((numRows-widthCols)/offsetColsDown +1);

		// one subSurface along width
		if(nSubSurfaceDown <=1) {
			nSubSurfaceDown=1;
		}

		return nSubSurfaceAlong * nSubSurfaceDown;
	}



	/**
	 * This computes the number of subset surfaces along the length only (not down dip)
	 * @param subSurfaceLength subSurface length in km
	 * @param subSurfaceOffset subSurface offset
	 * @return total number of subSurface along the fault
	 */
	public int getNumSubsetSurfacesAlongLength(double subSurfaceLength,double subSurfaceOffset){
		int lengthCols =  (int)Math.rint(subSurfaceLength/gridSpacingAlong+1);
		int offsetCols =   (int)Math.rint(subSurfaceOffset/gridSpacingAlong);

		// number of subSurfaces along the length of fault
		int nSubSurfaceAlong = (int)Math.floor((numCols-lengthCols)/offsetCols +1);

		// there is only one subSurface
		if(nSubSurfaceAlong <=1) {
			nSubSurfaceAlong=1;
		}

		return nSubSurfaceAlong;
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
	
	@Override
	public Location getLocation(int row, int column) {
		return get(row, column);
	}


	@Override
	public ListIterator<Location> getLocationsIterator() {
		return listIterator();
	}


	
	public void setLocation(int row, int column, Location loc) {
		set(row, column, loc);
	}
	
	@Override
	public FaultTrace getRowAsTrace(int row) {
		FaultTrace trace = new FaultTrace(null);
		for(int col=0; col<numCols; col++)
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
	
	private void calcPropagationDistances() {
		
		Location loc1 = locForDistCalcs;
		Location loc2;
		distanceJB = Double.MAX_VALUE;
		distanceSeis = Double.MAX_VALUE;
		distanceRup = Double.MAX_VALUE;
		
		double horzDist, vertDist, rupDist;

		int numLocs = (int) this.size();

		// flag to project to seisDepth if only one row and depth is below seisDepth
		boolean projectToDepth = false;
		if (getNumRows() == 1 && getLocation(0,0).getDepth() < SEIS_DEPTH)
			projectToDepth = true;

		// get locations to iterate over depending on dip
		ListIterator it;
		if(getAveDip() > 89) {
			it = getColumnIterator(0);
			if (getLocation(0,0).getDepth() < SEIS_DEPTH)
				projectToDepth = true;
		}
		else
			it = getLocationsIterator();

		while( it.hasNext() ){

			loc2 = (Location) it.next();

			// get the vertical distance
			vertDist = LocationUtils.vertDistance(loc1, loc2);

			// get the horizontal dist depending on desired accuracy
			horzDist = LocationUtils.horzDistanceFast(loc1, loc2);

			if(horzDist < distanceJB) distanceJB = horzDist;

			rupDist = horzDist * horzDist + vertDist * vertDist;
			if(rupDist < distanceRup) distanceRup = rupDist;

			if (loc2.getDepth() >= SEIS_DEPTH) {
				if (rupDist < distanceSeis)
					distanceSeis = rupDist;
			}
			// take care of shallow line or point source case
			else if(projectToDepth) {
				rupDist = horzDist * horzDist + SEIS_DEPTH * SEIS_DEPTH;
				if (rupDist < distanceSeis)
					distanceSeis = rupDist;
			}
		}

		distanceRup = Math.pow(distanceRup,0.5);
		distanceSeis = Math.pow(distanceSeis,0.5);

		if(D) {
			System.out.println(C+": distanceRup = " + distanceRup);
			System.out.println(C+": distanceSeis = " + distanceSeis);
			System.out.println(C+": distanceJB = " + distanceJB);
		}

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
			calcPropagationDistances();
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
			calcPropagationDistances();
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
			calcPropagationDistances();
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
			calcDistanceX();
		}
		return distanceX;
	}
	
	private void calcDistanceX() {
//		Location siteLoc this.locForDistCalcs;

		// set to zero if it's a point source
		if(getNumCols() == 1) {
			distanceX = 0;
		}
		else {
			// We should probably set something here here too if it's vertical strike-slip
			// (to avoid unnecessary calculations)

				// get points projected off the ends
				Location firstTraceLoc = getLocation(0, 0); 						// first trace point
				Location lastTraceLoc = getLocation(0, getNumCols()-1); 	// last trace point

				// get point projected from first trace point in opposite direction of the ave trace
				LocationVector dir = LocationUtils.vector(lastTraceLoc, firstTraceLoc); 		
				dir.setHorzDistance(1000); // project to 1000 km
				dir.setVertDistance(0d);
				Location projectedLoc1 = LocationUtils.location(firstTraceLoc, dir);


				// get point projected from last trace point in ave trace direction
				dir.setAzimuth(dir.getAzimuth()+180);  // flip to ave trace dir
				Location projectedLoc2 = LocationUtils.location(lastTraceLoc, dir);
				// point down dip by adding 90 degrees to the azimuth
				dir.setAzimuth(dir.getAzimuth()+90);  // now point down dip

				// get points projected in the down dip directions at the ends of the new trace
				Location projectedLoc3 = LocationUtils.location(projectedLoc1, dir);

				Location projectedLoc4 = LocationUtils.location(projectedLoc2, dir);

				LocationList locsForExtendedTrace = new LocationList();
				LocationList locsForRegion = new LocationList();

				locsForExtendedTrace.add(projectedLoc1);
				locsForRegion.add(projectedLoc1);
				for(int c=0; c<getNumCols(); c++) {
					locsForExtendedTrace.add(getLocation(0, c));
					locsForRegion.add(getLocation(0, c));     	
				}
				locsForExtendedTrace.add(projectedLoc2);
				locsForRegion.add(projectedLoc2);

				// finish the region
				locsForRegion.add(projectedLoc4);
				locsForRegion.add(projectedLoc3);

				// write these out if in debug mode
				if(D) {
					System.out.println("Projected Trace:");
					for(int l=0; l<locsForExtendedTrace.size(); l++) {
						Location loc = locsForExtendedTrace.get(l);
						System.out.println(loc.getLatitude()+"\t"+ loc.getLongitude()+"\t"+ loc.getDepth());
					}
					System.out.println("Region:");
					for(int l=0; l<locsForRegion.size(); l++) {
						Location loc = locsForRegion.get(l);
						System.out.println(loc.getLatitude()+"\t"+ loc.getLongitude()+"\t"+ loc.getDepth());
					}
				}

				Region polygon = new Region(locsForRegion, BorderType.MERCATOR_LINEAR);
				boolean isInside = polygon.contains(locForDistCalcs);

				double distToExtendedTrace = locsForExtendedTrace.minDistToLine(locForDistCalcs);

				if(isInside || distToExtendedTrace == 0.0) // zero values are always on the hanging wall
					distanceX = distToExtendedTrace;
				else 
					distanceX = -distToExtendedTrace;
		}
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


}
