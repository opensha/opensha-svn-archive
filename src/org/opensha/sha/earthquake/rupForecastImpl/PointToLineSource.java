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

package org.opensha.sha.earthquake.rupForecastImpl;

import java.util.ArrayList;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.calc.magScalingRelations.MagAreaRelationship;
import org.opensha.commons.calc.magScalingRelations.MagLengthRelationship;
import org.opensha.commons.calc.magScalingRelations.MagScalingRelationship;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;
import org.opensha.commons.data.LocationList;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.earthquake.FocalMechanism;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.griddedForecast.HypoMagFreqDistAtLoc;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

/**
 * <p>Title: PointEqkSource </p>
 * <p>Description: This converts a point source to a line source as described in the constructors.
 * If given magScalingRel is a mag-length relationship, then the rupture length is simply that
 * computed from mag.  If magScalingRel is a mag-area relationship, then rupture length is the 
 * computed area divided by the down-dip width, where the latter is computed as: 
 * (lowerSeisDepth-aveRupTopVersusMag(mag))/sin(dip).</p>
 * </UL><p>
 *
 *
 * @author Edward Field
 * @version 1.0
 */

public class PointToLineSource extends ProbEqkSource implements java.io.Serializable{

	//for Debug purposes
	private static String  C = new String("PointToLineEqkSource");
	private static String NAME = "Point-to-Line Source";
	private boolean D = false;

	protected ArrayList<ProbEqkRupture> probEqkRuptureList;
	private ArrayList<Double> rates;

	private Location location;
	private double duration=Double.NaN;
	private double minMag = Double.NaN;
	private double maxLength = 0;

	/**
	 * This constructor takes a HypoMagFreqDistAtLoc object, depth as a function of mag (aveRupTopVersusMag), 
	 * and a default depth (defaultHypoDepth).  The depth of each point source is set according to the
	 * mag using the aveRupTopVersusMag function; if mag is below the minimum x value of this function,
	 * then defaultHypoDepth is applied.  Note that the depth value in HypoMagFreqDistAtLoc.getLocation()
	 * is ignored here (that location is cloned and the depth is overwritten here).  The strike in each 
	 * FocalMechanism of HypoMagFreqDistAtLoc is applied, or a random strike is applied if this is NaN 
	 * (a different random value for each and every rupture).  This sets the source as Poissonian.  
	 */
	public PointToLineSource(HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc,
			ArbitrarilyDiscretizedFunc aveRupTopVersusMag, double defaultHypoDepth,
			MagScalingRelationship magScalingRel,double lowerSeisDepth, 
			double duration, double minMag){

		this.duration = duration;
		this.isPoissonian = true;
		this.location = hypoMagFreqDistAtLoc.getLocation();

		probEqkRuptureList = new ArrayList<ProbEqkRupture>();
		rates = new ArrayList<Double>();

		IncrementalMagFreqDist[] magFreqDists = hypoMagFreqDistAtLoc.getMagFreqDistList();
		FocalMechanism[] focalMechanisms = hypoMagFreqDistAtLoc.getFocalMechanismList();
		for (int i=0; i<magFreqDists.length; i++) {
			mkAndAddRuptures(magFreqDists[i], focalMechanisms[i], aveRupTopVersusMag, defaultHypoDepth, 
					magScalingRel, lowerSeisDepth, duration, minMag, 1.0);
		}
	}


	/**
	 * This constructor is the same as the other, but rather than using the given or a random strike,
	 * this applies a spoked source where several strikes are applied with even spacing in azimuth.
	 * numStrikes defines the number of strikes applied (e.g., numStrikes=2 would be a cross hair) and firstStrike
	 * defines the azimuth of the first one (e.g., firstStrike=0 with numStrikes=2 would be a cross-hair source
	 * that is perfectly aligned NS and EW).
	 */
	public PointToLineSource(HypoMagFreqDistAtLoc hypoMagFreqDistAtLoc,
			ArbitrarilyDiscretizedFunc aveRupTopVersusMag, double defaultHypoDepth,
			MagScalingRelationship magScalingRel,double lowerSeisDepth, 
			double duration, double minMag, int numStrikes, double firstStrike){

		this.duration = duration;
		this.isPoissonian = true;
		this.location = hypoMagFreqDistAtLoc.getLocation();

		probEqkRuptureList = new ArrayList<ProbEqkRupture>();
		rates = new ArrayList<Double>();

		// set the strikes
		double deltaStrike = 180/numStrikes;
		double[] strike = new double[numStrikes];
		for(int n=0;n<numStrikes;n++)
			strike[n]=firstStrike+n*deltaStrike;

		IncrementalMagFreqDist[] magFreqDists = hypoMagFreqDistAtLoc.getMagFreqDistList();
		FocalMechanism[] focalMechanisms = hypoMagFreqDistAtLoc.getFocalMechanismList();
		for (int i=0; i<magFreqDists.length; i++) {
			FocalMechanism focalMech = focalMechanisms[i]; // COPY THIS
			for(int s=0;s<numStrikes;s++) {
				focalMech.setStrike(strike[s]);
				double weight = 1.0/numStrikes;
				mkAndAddRuptures(magFreqDists[i], focalMechanisms[i], aveRupTopVersusMag, defaultHypoDepth, 
						magScalingRel, lowerSeisDepth, duration, minMag,weight);			  
			}
		}
	}



	/**
	 * This creates the ruptures and adds them to the list for the given inputs
	 * @param magFreqDist
	 * @param focalMech
	 * @param aveRupTopVersusMag
	 * @param defaultHypoDepth
	 * @param magScalingRel
	 * @param lowerSeisDepth
	 * @param duration
	 * @param minMag
	 * @param weight
	 */
	private void mkAndAddRuptures(IncrementalMagFreqDist magFreqDist, 
			FocalMechanism focalMech, ArbitrarilyDiscretizedFunc aveRupTopVersusMag, 
			double defaultHypoDepth, MagScalingRelationship magScalingRel,
			double lowerSeisDepth, double duration, double minMag, double weight) {

		double dip = focalMech.getDip();
		double strike = focalMech.getStrike();
		boolean isStrikeRandom=false;
		if(Double.isNaN(strike)) {
			isStrikeRandom=true;
		}

		for (int m=0; m<magFreqDist.getNum(); m++){
			double mag = magFreqDist.getX(m);
			double rate = magFreqDist.getY(m);
			double prob = 1-Math.exp(-rate*weight*duration);
			if(prob > 0 && mag >= minMag){
				// set depth of rupture
				Location loc = location.copy();
				double depth;
				if(mag < aveRupTopVersusMag.getMinX())
					depth = defaultHypoDepth;
				else
					depth = aveRupTopVersusMag.getClosestY(mag);
				loc.setDepth(depth);
				// set rupture length
				double rupLength = getRupLength(mag, aveRupTopVersusMag, lowerSeisDepth, dip, magScalingRel);

				if(rupLength>maxLength) maxLength=rupLength; 

				// get randome strike if needed
				if(isStrikeRandom) {
					strike = (Math.random()-0.5)*180.0;	// get a random strike between -90 and 90
					//System.out.println(strike);
				}
				Direction dir = new Direction(0.0,rupLength/2,strike,Double.NaN);
				Location loc1 = RelativeLocation.getLocation(loc,dir);
				dir.setAzimuth(strike-180);
				Location loc2 = RelativeLocation.getLocation(loc,dir);
				FaultTrace fltTrace = new FaultTrace(null);
				fltTrace.addLocation(loc1);
				fltTrace.addLocation(loc2);

				// make the surface
				StirlingGriddedSurface surf = new StirlingGriddedSurface(fltTrace,dip,depth,depth,1.0);

				ProbEqkRupture rupture = new ProbEqkRupture();
				rupture.setMag(mag);
				rupture.setAveRake(focalMech.getRake());
				rupture.setRuptureSurface(surf);
				rupture.setProbability(prob);

				if (D) System.out.println("\tstrike\t"+strike+"\tmag\t"+(float)mag+"\trate\t"+(float)rate+"\tprob\t"+(float)prob+"\tweight\t"+(float)weight);

				// add the rupture to the list and save the rate in case the duration changes
				probEqkRuptureList.add(rupture);
				rates.add(new Double(rate*weight));
			}
		}
	}

	/**
	 * This computes the rupture length.  If magScalingRel is a mag-length relationship, then
	 * the length from mag is returned.  If magScalingRel is a mag-area relationship, then
	 * length returned is the computed area divided by the down-dip width, where the latter
	 * is computed as: (lowerSeisDepth-aveRupTopVersusMag(mag))/sin(dip).
	 * @param mag
	 * @param aveRupTopVersusMag
	 * @param lowerSeisDepth
	 * @param dip
	 * @param magScalingRel
	 * @return
	 */
	private double getRupLength(double mag, ArbitrarilyDiscretizedFunc aveRupTopVersusMag, 
			double lowerSeisDepth, double dip, MagScalingRelationship magScalingRel) {

		double rupLength;
		if(magScalingRel instanceof MagAreaRelationship) {
			double ddw = (aveRupTopVersusMag.getClosestY(mag) - lowerSeisDepth)/Math.sin(dip*Math.PI/180);
			double area = magScalingRel.getMedianScale(mag);
			if(ddw > Math.sqrt(area))
				rupLength = ddw;
			else
				rupLength = area/ddw;
		}
		else if (magScalingRel instanceof MagLengthRelationship) {
			rupLength = magScalingRel.getMedianScale(mag);
		}
		else throw new RuntimeException("bad type of MagScalingRelationship");

		return rupLength;
	}



	/**
	 * It returns a list of all the locations which make up the surface for this
	 * source.
	 */
	public LocationList getAllSourceLocs() {
		LocationList locList = new LocationList();
		for(int r=0;r< getNumRuptures(); r++) {
			locList.addAllLocations(probEqkRuptureList.get(r).getRuptureSurface().getLocationList());
		}
		return locList;
	}

	
	/**
	 * don't know what to return here (deprecate this method?)
	 */
	public EvenlyGriddedSurfaceAPI getSourceSurface() { 
		throw new RuntimeException("Method not supported");
	}



	/**
	 * @return the number of rutures (equals number of mags with non-zero rates)
	 */
	public int getNumRuptures() {
		return probEqkRuptureList.size();
	}


	/**
	 * This makes and returns the nth probEqkRupture for this source.
	 */
	public ProbEqkRupture getRupture(int nthRupture){
		return probEqkRuptureList.get(nthRupture);
	}


	/**
	 * This sets the duration used in computing Poisson probabilities.  This assumes
	 * the same units as in the magFreqDist rates.  This is ignored if the source in non-Poissonian.
	 * @param duration
	 */
	public void setDuration(double duration) {
		this.duration=duration;
		for(int i=0; i<probEqkRuptureList.size();i++)
			probEqkRuptureList.get(i).setProbability(1-Math.exp(-rates.get(i)*duration));
	}


	/**
	 * This gets the duration used in computing Poisson probabilities (it may be NaN
	 * if the source is not Poissonian).
	 * @param duration
	 */
	public double getDuration() {
		return duration;
	}



	/**
	 * This gets the minimum magnitude to be considered from the mag-freq dist (those
	 * below are ignored in making the source).  This will be NaN if the source is not
	 * Poissonian.
	 * @return minMag
	 */
	public double getMinMag(){
		return minMag;
	}


	/**
	 * This returns the shortest horizontal dist to the point source (minus half the length of the longest rupture).
	 * @param site
	 * @return minimum distance
	 */
	public  double getMinDistance(Site site) {
		return RelativeLocation.getHorzDistance(site.getLocation(), location) - maxLength/2;
	}

	/**
	 * get the name of this class
	 *
	 * @return
	 */
	public String getName() {
		return NAME;
	}
}
