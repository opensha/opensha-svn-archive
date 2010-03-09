/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with the Southern California
 * Earthquake Center (SCEC, http://www.scec.org) at the University of Southern
 * California and the UnitedStates Geological Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.opensha.commons.data;

import org.opensha.commons.calc.RelativeLocation;

/**
 * This class encapsulates information describing a vector between two
 * <code>Location</code>s. This vector is defined by the azimuth (bearing) from
 * a point p1 to a point p2, and also by the horizontal and vertical separation
 * between the points. Note that a <code>LocationVector</code> from point A
 * to point B is not the complement of that from point B to A. Although
 * the sign of the horizontal and vertical components will reverse, the azimuth
 * will likely change by some value other than 180&#176;.<br/>
 * <br/>
 * <b>Note:</b> Although a <code>LocationVector</code> will function in any
 * reference frame, the convention in seismology and that adopted in OpenSHA is
 * for depth to be positive down.<br/>
 * <br/>
 * <b>Note:</b> Azimuth is stored internally in radians for computational
 * convenience. Be sure to use the {@link #getAzimuth()} (decimal-degrees) or 
 * {@link #getAzimuthRad()} (radians) where appropriate.
 * 
 * @author Peter Powers
 * @author Sid Hellman
 * @author Steven W. Rock
 * @version $Id$
 */
// TODO refactor to LocationVector
public class Direction {

	/*
	 * Developer Notes: The previous incarnation of this class as 'Direction'
	 * included back azimuth. There are (were) instances in OpenSHA where
	 * this was taken to be the azimuth from point B to A (for an azimuth
	 * from point A to B). As Back azimuth is generally defined, this interp
	 * is incorrect. Back azimuth is simply the 180 degree complement of the
	 * azimuth at an origin point. Under the assumed interpretation, each
	 * Direction was implicitely Location dependent, but that information was
	 * never stored as part of this class. Furthermore, the onus was on the 
	 * user to provide the correct value for back azimuth. This property of 
	 * the class has been removed and users are directed in 
	 * LocationUtils.azimuth() to simply reverse the points of interest if 
	 * the bearing from B to A is required.
	 */

	private double azimuth;
	private double vertical;
	private double horizontal;

	private double backAzimuth;

	/**
	 * Initializes a new <code>LocationVector</code> with azimuth and 
	 * horizontal and vertical components all initialized to 0.
	 */
	public Direction() {}

	/**
	 * Initializes a new <code>LocationVector</code> with the supplied values.
	 * Note that <code>azimuth</code> is expected in <i>radians</i>.
	 * 
	 * @param azimuth value to set in <i>radians</i>
	 * @param horizontal component value to set
	 * @param vertical component value to set
	 */
	public Direction(
			double azimuth, double horizontal, double vertical) {
		set(azimuth, horizontal, vertical);
	}

	/**
	 * Initializes a new <code>LocationVector</code> with the supplied values.
	 * Note that azimuth is expected to be supplied in <i>radians</i>.
	 * 
	 * @param vDist vertical component 
	 * @param hDist horizontal component
	 * @param azimuth angular component in <i>radians</i>
	 * 
	 * @param backAz back azimuth
	 */
	public Direction(double vDist, double hDist, double azimuth, double backAz) {

		// this.azimuth.setValue( new Double(az) );
		// this.backAzimuth.setValue( new Double(backAz) );
		this.horizontal = hDist;
		this.vertical = vDist;
		this.azimuth = azimuth;
		this.backAzimuth = backAz;
	}

	/**
	 * Sets this <code>LocationVector</code>'s internal fields to the supplied
	 * values. Note that <code>azimuth</code> is expected in <i>radians</i>.
	 * 
	 * @param azimuth value to set in <i>radians</i>
	 * @param horizontal component value to set
	 * @param vertical component value to set
	 */
	public void set(double azimuth, double horizontal, double vertical) {
		this.azimuth = azimuth;
		this.horizontal = horizontal;
		this.vertical = vertical;
	}
	
	/**
	 * Returns the azimuth of this <code>LocationVector</code> in 
	 * decimal degrees.
	 * @return the azimuth value in decimal degrees
	 * @see #getAzimuthRad()
	 */
	public double getAzimuth() {
		return azimuth;
//		return azimuth * RelativeLocation.TO_DEG;
	}

	/**
	 * Returns the azimuth of this <code>LocationVector</code> in radians.
	 * @return the azimuth value in decimal degrees
	 * @see #getAzimuth()
	 */
//	public double getAzimuthRad() {
//		return azimuth;
//	}

	/**
	 * Sets the azimuth of this <code>LocationVector</code>.
	 * @param azimuth value to set in <i>radians</i>
	 */
	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	/**
	 * Gets the vertical component of this <code>LocationVector</code>.
	 * @return the vertical component value
	 */
	public double getVertDistance() {
		return vertical;
	}

	/**
	 * Sets the vertical component of this <code>LocationVector</code>.
	 * @param vertical component value to set
	 */
	public void setVertDistance(double vertical) {
		this.vertical = vertical;
	}

	/**
	 * Gets the horizontal component of this <code>LocationVector</code>.
	 * @return the horizontal component value
	 */
	public double getHorzDistance() {
		return horizontal;
	}

	/**
	 * Sets the horizontal component of this <code>LocationVector</code>.
	 * @param horizontal component value to set
	 */
	public void setHorzDistance(double horizontal) {
		this.horizontal = horizontal;
	}

	/**
	 * Sets the backAzimuth attribute of the Direction object
	 * 
	 * @param backAzimuth The new backAzimuth value
	 * @exception Exception Description of the Exception
	 */
	public void setBackAzimuth(double backAzimuth) {
		this.backAzimuth = backAzimuth;
		// this.backAzimuth.setValue( new Double( backAzimuth ) );
	}


	/**
	 * Gets the backAzimuth attribute of the Direction object
	 * 
	 * @return The backAzimuth value
	 */
	public double getBackAzimuth() {
		return backAzimuth;
		// return ( ( Double ) backAzimuth.getValue() ).doubleValue();
	}

	/** Debug printout of all the field values */
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(this.getClass().getSimpleName());
		b.append(":  az = ");
		b.append(getAzimuth());
		b.append("  dH = ");
		b.append(horizontal);
		b.append("  dV = ");
		b.append(vertical);
		return b.toString();
	}

	/**
	 * Checks to see if another Direction object has the same field values. If
	 * it does, they are considered equal.
	 * 
	 * @param dir
	 * @return
	 */
	public boolean equalsDirection(Direction dir) {

		if (horizontal != dir.horizontal) return false;
		if (vertical != dir.vertical) return false;
		if (azimuth != dir.azimuth) return false;
		if (this.backAzimuth != dir.backAzimuth) return false;

		return true;
	}

	/**
	 * Calls equalsDirection(Direction dir) if passed in object is a Direction,
	 * else returns false. A different class could never be considered equals,
	 * like comparing apples to oranges.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Direction)
			return equalsDirection((Direction) obj);
		else
			return false;
	}

}
