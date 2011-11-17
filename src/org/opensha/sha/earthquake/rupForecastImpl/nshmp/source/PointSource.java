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

package org.opensha.sha.earthquake.rupForecastImpl.nshmp.source;

import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech.*;

import java.util.Map;

import org.opensha.commons.data.Site;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FocalMech;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.PointSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * This is a custom point earthquake source representation used for the NSHMP.
 * 
 * It was initially created to provide built in approximations of distance and
 * hanging wall effects as well as to override getMinDistance to provide
 * consistence with distances determined during hazard calcs.
 * 
 * Currently configured to handle 2 rupture top depths; depth #1 is applied to
 * M&lt;6.5 and depth #2 to M&ge;6.5. Set both values the same for single depth
 * across all magnitudes.
 * 
 * M&ge;6 uses finite source; M&lt;6 uses points NOT USED -- NSHMP IMRs should
 * override
 * 
 * @author P. Powers
 * @version: $Id$
 */
public class PointSource extends ProbEqkSource {

	private static final String NAME = "NSHMP Point Source";
	private static final double M_FINITE_CUT = 6.0; // TODO ??
	private static final double M_DEPTH_CUT = 6.5;
	private static final double SS_DIP = 90;
	private static final double RN_DIP = 50;

	private IncrementalMagFreqDist mfd;
	private double duration;
	private double lgMagDepth;
	private double smMagDepth;
	private Map<FocalMech, Double> mechWts;

	private int mechCount; // mechs with weight 1-3;
	private int ssIdx, revIdx; // normal not needed
	
	// Rupture indexing: no array index out of bounds are checked, it is assumed
	// that users will only request values in the range getNumRuptures()-1
	// Focal mech is determined using the max indices for each type of mech
	// determined using the Math.ceil(wt) [scales to 1] * num_M
	
	private PointSurface ptSurface;

	/**
	 * The Constructor for the case where either a random strike is computed and
	 * applied, or a cross-hair source is applied (rather than assigned)
	 * @param loc <code>Location</code> of the point source
	 * @param mfd magnitude frequency distribution of the source
	 * @param duration forecast duration
	 * @param depths 2 element array of rupture top depths;
	 *        <code>depths[0]</code> used for M&lt;6.5, <code>depths[1]</code>
	 *        used for M&ge;6.5
	 * @param mechWtMap <code>Map</code> of focal mechanism weights
	 */
	public PointSource(Location loc, IncrementalMagFreqDist mfd,
		double duration, double[] depths, Map<FocalMech, Double> mechWtMap) {

		name = NAME; // super
		this.mfd = mfd;
		this.duration = duration;
		smMagDepth = depths[0];
		lgMagDepth = depths[1];
		this.mechWts = mechWtMap;

		// rupture indexing
		mechCount = countMechs(mechWtMap);
		setMechIndices();

		// single reused eqkRupture and ptSurface per source
		probEqkRupture = new ProbEqkRupture(); // super
		ptSurface = new PointSurface(loc);
		probEqkRupture.setPointSurface(ptSurface);
	}

	@Override
	public ProbEqkRupture getRupture(int idx) {

		FocalMech mech = mechForIndex(idx);
		double dip = dipForMech(mech);
		double wt = mechWts.get(mech);
		int magIdx = idx % mfd.getNum();
		double mag = mfd.getX(magIdx);
		double depth = depthForMag(mag);

		probEqkRupture.setMag(mag);
		probEqkRupture.setAveRake(mech.rake());

		// NGA notes:
		// rake is used to set fault type
		// dip determines whether hanging wall approximation is used

		double prob = 1 - Math.exp(-duration * wt * mfd.getY(magIdx));
//		System.out.println(mech);
//		System.out.println(mechWts);
//		System.out.println("wt: " + wt);
		
		probEqkRupture.setProbability(prob);

		// check as this builds a new internal Location in ptSurface
		if (ptSurface.getDepth() != depth) ptSurface.setDepth(depth);
		if (ptSurface.getAveDip() != dip) ptSurface.setAveDip(dip);

		return probEqkRupture;
	}

	@Override
	public LocationList getAllSourceLocs() {
		LocationList locList = new LocationList();
		locList.add(ptSurface.getLocation());
		return locList;
	}

	@Override
	public EvenlyGriddedSurface getSourceSurface() {
		// NOTE this returns the shared mutable (possibly depth varying) point
		// surface instance
		return ptSurface;
	}

	@Override
	public int getNumRuptures() {
		return mfd.getNum() * mechCount;
	}

	@Override
	public double getMinDistance(Site site) {
		return LocationUtils.horzDistanceFast(site.getLocation(),
			ptSurface.getLocation());
	}

	private double dipForMech(FocalMech mech) {
		return (mech == STRIKE_SLIP) ? SS_DIP : RN_DIP;
	}

	private FocalMech mechForIndex(int idx) {
		// iteration order is always SS -> REV -> NOR. Any unused mech will have
		// a max index of -1 and should never be used
		return (idx < ssIdx) ? STRIKE_SLIP : (idx < revIdx) ? REVERSE : NORMAL;
	}

	private double depthForMag(double mag) {
		return (mag >= M_DEPTH_CUT) ? lgMagDepth : smMagDepth;
	}

	private int countMechs(Map<FocalMech, Double> map) {
		int count = 0;
		for (Double d : map.values()) {
			if (d > 0.0) count++;
		}
		return count;
	}
	
	private void setMechIndices() {
		int ssCount = (int) Math.ceil(mechWts.get(STRIKE_SLIP)) * mfd.getNum();
		int revCount = (int) Math.ceil(mechWts.get(REVERSE)) * mfd.getNum();
		ssIdx = (ssCount > 0) ? ssCount : -1;
		revIdx = (revCount > 0) ? ssCount + revCount : -1;
	}

}
