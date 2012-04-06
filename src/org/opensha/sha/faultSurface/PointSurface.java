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

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.FaultUtils;
import org.opensha.sha.faultSurface.utils.GriddedSurfaceUtils;
import org.opensha.sha.faultSurface.utils.PtSrcDistCorr;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceSeisParameter;


/**
 * <b>Title:</b> PointSurface<p>
 *
 * <b>Description:</b> This is a special case of RuptureSurface
 * that is a point surface (has only one Location). <p>
 *
 *
 * @author     Ned Field (completely rewritten)
 * @created    February 26, 2002
 * @version    1.0
 */

public class PointSurface implements RuptureSurface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Location pointLocation;
	
	// for distance measures
	Location siteLocForDistCalcs= new Location(Double.NaN,Double.NaN);
	Location siteLocForDistXCalc= new Location(Double.NaN,Double.NaN);
	double distanceJB, distanceSeis, distanceRup, distanceX;
	
	final static double SEIS_DEPTH = DistanceSeisParameter.SEIS_DEPTH;   // minimum depth for Campbell model
	
	// flag to indicate that the ptLocChanged
	boolean ptLocChanged;
	
	double vertDist;
	
	/** variables for the point-source distance correction */
	PtSrcDistCorr.Type corrType = PtSrcDistCorr.Type.NONE;
	double corrMag = Double.NaN;
	

	/**
	 * The average strike of this surface on the Earth. Even though this is a
	 * point source, an average strike can be assigned to it to assist with
	 * particular scientific caculations. Initially set to NaN.
	 */
	protected double aveStrike=Double.NaN;

	/**
	 * The average dip of this surface into the Earth. Even though this is a
	 * point source, an average dip can be assigned to it to assist with
	 * particular scientific caculations. Initially set to NaN.
	 */
	protected double aveDip=Double.NaN;

	/** The name of this point source.  */
	protected String name;

	/**
	 *  Constructor for the PointSurface object. Sets all the fields
	 *  for a Location object. Mirrors the Location constructor.
	 *
	 * @param  lat    latitude for the Location of this point source.
	 * @param  lon    longitude for the Location of this point source.
	 * @param  depth  depth below the earth for the Location of this point source.
	 */
	public PointSurface( double lat, double lon, double depth ) {
		this(new Location(lat, lon, depth));
	}

	/**
	 *  Constructor for the PointSurface object. Sets all the fields
	 *  for a Location object.
	 *
	 * @param  loc    the Location object for this point source.
	 */
	public PointSurface( Location loc ) {
		setLocation(loc);
	}


	/**
	 * Sets the average strike of this surface on the Earth. An InvalidRangeException
	 * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
	 * Even though this is a point source, an average strike can be assigned to
	 * it to assist with particular scientific caculations.
	 */
	public void setAveStrike( double aveStrike ) throws InvalidRangeException{
		FaultUtils.assertValidStrike( aveStrike );
		this.aveStrike = aveStrike ;
	}

	
	/** Returns the average strike of this surface on the Earth.  */
	public double getAveStrike() { return aveStrike; }


	/**
	 * Sets the average dip of this surface into the Earth. An InvalidRangeException
	 * is thrown if the ave strike is not a valid value, i.e. must be > 0, etc.
	 * Even though this is a point source, an average dip can be assigned to
	 * it to assist with particular scientific caculations.
	 */
	public void setAveDip( double aveDip ) throws InvalidRangeException{
		FaultUtils.assertValidDip( aveDip );
		this.aveDip =  aveDip ;
	}

	/** Returns the average dip of this surface into the Earth.  */
	public double getAveDip() { return aveDip; }


	/** Since this is a point source, the single Location can be set without indexes. Does a clone copy. */
	public void setLocation(Location location ) {
		pointLocation = location;
		ptLocChanged = true;
	}
	

	public double getDepth() { return pointLocation.getDepth(); }

	
	public void setDepth(double depth) {
		Location newLocation = new Location(pointLocation.getLatitude(), pointLocation.getLongitude(), depth);
		setLocation(newLocation);
	}


	/**
	 * Gets the location for this point source.
	 * 
	 * @return
	 */
	public Location getLocation() {
		return pointLocation;
	}


	/** Sets the name of this PointSource. Uesful for lookup in a list */
	public void setName(String name) { this.name = name; }
	
	/** Gets the name of this PointSource. Uesful for lookup in a list */
	public String getName() { return name; }


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
		String surfaceMetadata;
		surfaceMetadata = (float)aveDip + "\t";
		surfaceMetadata += (float)getAveLength() + "\t";
		surfaceMetadata += (float)getAveWidth() + "\t";
		surfaceMetadata += (float)Double.NaN + "\t";
		surfaceMetadata += "1" + "\t";
		surfaceMetadata += "1" + "\t";
		surfaceMetadata += "1" + "\n";
		surfaceMetadata += "#Surface locations (Lat Lon Depth) \n";
		surfaceMetadata += (float) pointLocation.getLatitude() + "\t";
		surfaceMetadata += (float) pointLocation.getLongitude() + "\t";
		surfaceMetadata += (float) pointLocation.getDepth();

		return surfaceMetadata;
	}

	@Override
	public double getAveDipDirection() {
		throw new RuntimeException("Method not yet implemented");
	}

	@Override
	public double getAveRupTopDepth() {
		return pointLocation.getDepth();
	}

	@Override
	public LocationList getPerimeter() {
		// TODO Auto-generated method stub
		return getEvenlyDiscritizedPerimeter();
	}
	
	private FaultTrace getFaultTrace() {
		FaultTrace trace = new FaultTrace(null);
		trace.add(pointLocation);
		return trace;
	}

	@Override
	public FaultTrace getUpperEdge() {
		return getFaultTrace();
	}
	
	
	/**
	 * This sets the magnitude and type for the point-source distance corrections
	 * @param mag
	 * @param type
	 */
	public void setDistCorrMagAndType(double mag, PtSrcDistCorr.Type type) {
		corrMag=mag;
		corrType = type;
	}


	/**
	 * This sets the three propagation distances (distanceJB, distanceRup, & distanceSeis)
	 * @param siteLoc
	 */
	private void calcPropagationDistances(Location siteLoc) {

		// calc distances if either location has changed
		// IS THIS REALLY SAVING MUCH TIME (is the check faster than recomputing the distances)?
		if(!siteLocForDistCalcs.equals(siteLoc) || ptLocChanged) {
			siteLocForDistCalcs = siteLoc;
			vertDist = LocationUtils.vertDistance(pointLocation, siteLocForDistCalcs);
			distanceJB = LocationUtils.horzDistanceFast(pointLocation, siteLocForDistCalcs);
		}
		
		// always do this point source-distance correction since mag is generally always changing 
		// (looping over point source) & type NONE returns 1.0
		distanceJB *= PtSrcDistCorr.getCorrection(distanceJB, corrMag, corrType);		
		
		// set distanceRup & distanceSeis
		distanceRup = Math.sqrt(distanceJB * distanceJB + vertDist * vertDist);
		if (pointLocation.getDepth() < SEIS_DEPTH)
			distanceSeis = Math.sqrt(distanceJB * distanceJB + SEIS_DEPTH * SEIS_DEPTH);
		else
			distanceSeis = distanceRup;
	}
	
	
	/**
	 * This returns rupture distance (kms to closest point on the 
	 * rupture surface), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return 
	 */
	@Override
	public synchronized double getDistanceRup(Location siteLoc){
		calcPropagationDistances(siteLoc);
		return distanceRup;
	}

	/**
	 * This returns distance JB (shortest horz distance in km to surface projection 
	 * of rupture), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	@Override
	public synchronized double getDistanceJB(Location siteLoc){
		calcPropagationDistances(siteLoc);
		return distanceJB;
	}

	/**
	 * This returns "distance seis" (shortest distance in km to point on rupture 
	 * deeper than 3 km), assuming the location has zero depth (for numerical 
	 * expediency).
	 * @return
	 */
	@Override
	public synchronized double getDistanceSeis(Location siteLoc){
		calcPropagationDistances(siteLoc);
		return distanceSeis;
	}

	/**
	 * This returns distance X (the shortest distance in km to the rupture 
	 * trace extended to infinity), where values >= 0 are on the hanging wall
	 * and values < 0 are on the foot wall.  The given site location is assumed to be at zero
	 * depth (for numerical expediency).  This always returns zero since this is a point surface.
	 * @return
	 */
	@Override
	public double getDistanceX(Location siteLoc){
		return 0.0;
	}
	
	@Override
	public String getInfo() {
        return new String("\tPoint-Surface Location (lat, lon, depth (km):" +
                "\n\n" +
                "\t\t" + (float) pointLocation.getLatitude() + ", " +
                (float) pointLocation.getLongitude() +
                ", " + (float) pointLocation.getDepth());
	}

	@Override
	public boolean isPointSurface() {
		return true;
	}

	@Override
	public double getArea() {
		return 0;
	}

	@Override
	public double getAveGridSpacing() {
		return 0;
	}

	@Override
	public double getAveLength() {
		return 0;
	}

	@Override
	public double getAveWidth() {
		return 0;
	}
	
	private LocationList getLocationList() {
		LocationList list = new LocationList();
		list.add(pointLocation);
		return list;
	}

	@Override
	public LocationList getEvenlyDiscritizedListOfLocsOnSurface() {
		return getLocationList();
	}

	@Override
	public LocationList getEvenlyDiscritizedPerimeter() {
		return getLocationList();
	}

	@Override
	public FaultTrace getEvenlyDiscritizedUpperEdge() {
		return getFaultTrace();
	}

	@Override
	public Location getFirstLocOnUpperEdge() {
		return pointLocation;
	}

	@Override
	public Location getLastLocOnUpperEdge() {
		return pointLocation;
	}

	@Override
	public double getFractionOfSurfaceInRegion(Region region) {
		if(region.contains(pointLocation))
			return 1.0;
		else
			return 0.0;
	}

	@Override
	public ListIterator<Location> getLocationsIterator() {
		return getLocationList().listIterator();
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

}
