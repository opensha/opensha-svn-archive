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
import org.opensha.commons.util.FaultUtils;


/**
 * <b>Title:</b> PointSurface<p>
 *
 * <b>Description:</b> This is a special case of EvenlyGriddedSurface
 * that only has one Location. <p>
 *
 *
 * @author     Steven W. Rock and rewritten by Ned Field
 * @created    February 26, 2002
 * @version    1.0
 */

public class PointSurface extends AbstractEvenlyGriddedSurface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Location location;

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
		super(1,1,0.0);
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
	public void setLocation( Location location ) {
		this.location = location;
		set(0,0,location);
	}
	

	public double getDepth() { return location.getDepth(); }

	
	public void setDepth(double depth) {
		Location newLocation = new Location(location.getLatitude(), location.getLongitude(), depth);
		setLocation(newLocation);
	}


	/**
	 * Gets the location for this point source.
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
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
		surfaceMetadata += (float) location.getLatitude() + "\t";
		surfaceMetadata += (float) location.getLongitude() + "\t";
		surfaceMetadata += (float) location.getDepth();

		return surfaceMetadata;
	}

	@Override
	public double getAveDipDirection() {
		throw new RuntimeException("Method not yet implemented");
	}

	@Override
	public double getAveRupTopDepth() {
		return location.getDepth();
	}

	@Override
	public LocationList getPerimeter() {
		// TODO Auto-generated method stub
		return getEvenlyDiscritizedPerimeter();
	}

	@Override
	public FaultTrace getUpperEdge() {
		return this.getRowAsTrace(0);
	}




}
