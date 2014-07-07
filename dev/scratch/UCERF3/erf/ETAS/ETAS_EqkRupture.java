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

package scratch.UCERF3.erf.ETAS;

import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;


/**
 *
 * <b>Title:</b> EqkRupture<br>
 * <b>Description:</b> <br>
 *
 * @author Ned Field
 * @version 1.0
 */

public class ETAS_EqkRupture extends ObsEqkRupture {
	
	private int id=-1, nthERF_Index=-1, generation=0;
	private double distToParent=Double.NaN;
	private ETAS_EqkRupture parentRup=null;
	private int parentID=-1;	// TODO get rid of this
	private Location parentTriggerLoc = null;
	
	public ETAS_EqkRupture() {};
	
	public ETAS_EqkRupture(ProbEqkRupture probRup) {
		this.setMag(probRup.getMag());
		this.setRuptureSurface(probRup.getRuptureSurface());
		this.setAveRake(probRup.getAveRake());
	}
	
	
	public ETAS_EqkRupture(ObsEqkRupture probRup) {
		this.setMag(probRup.getMag());
		this.setRuptureSurface(probRup.getRuptureSurface());
		this.setAveRake(probRup.getAveRake());
		this.setOriginTime(probRup.getOriginTime());
		this.setEventId(probRup.getEventId());
	}
	
	public ETAS_EqkRupture(ETAS_EqkRupture parentRup, int id, long originTimeInMillis) {
		this.parentRup=parentRup;
		this.id=id;
		this.originTimeInMillis=originTimeInMillis;
		
	}

	
	/**
	 * The ID of the parent that spawned this primary aftershock
	 * Returns -1 if there is no parent
	 * @return
	 */
	public int getParentID() {
		return parentID;
//		if(parentRup instanceof ETAS_EqkRupture)
//			return ((ETAS_EqkRupture)parentRup).getID();
//		else
//			return -1;
	}
	
	
	public void setParentID(int parID) {
		parentID=parID;
	}
	
	
	public ETAS_EqkRupture getParentRup() {
		return parentRup;
	}
	
	
	/**
	 * This returns the distance to the parent (NaN if never set)
	 * @return
	 */
	public double getDistanceToParent() {
		return distToParent;
	}
	
	/**
	 * This sets the distance to parent
	 * TODO compute this and remove the setDistanceToParent method
	 * @param distToParent
	 */
	public void setDistanceToParent(double distToParent) {
		this.distToParent = distToParent;
	}
	
	/**
	 * The ID of this event
	 * @return
	 */
	public int getID() {
		return id;
	}

	
	/**
	 * Sets the ID of this event
	 * @return
	 */
	public void setID(int id) {
		this.id = id;
	}

	/**
	 * The ID of this event
	 * @return
	 */
	public int getGeneration() {
		return generation;
	}

	/**
	 * Sets the ID of this event
	 * @return
	 */
	public void setGeneration(int generation) {
		this.generation = generation;
	}


	public void setNthERF_Index(int nthERF_Index){
		this.nthERF_Index = nthERF_Index;
	}

	public int getNthERF_Index(){
		return nthERF_Index;
	}
	
	/**
	 * This is the point on the parent rupture surface from which this aftershock was triggered
	 * @param loc
	 */
	public void setParentTriggerLoc(Location loc) {
		parentTriggerLoc=loc;
	}
	
	/**
	 * This is the point on the parent rupture surface from which this aftershock was triggered
	 * @param loc
	 */
	public Location getParentTriggerLoc() {
		return parentTriggerLoc;
	}
	
	/**
	 * This returns the oldest ancestor of the given rupture,
	 * or null if this rupture is of generation 0 (or there is no
	 * parent rupture available)
	 * @return
	 */
	public ETAS_EqkRupture getOldestAncestor() {
		
		int gen = getGeneration();
		ETAS_EqkRupture oldestAncestor = getParentRup();
		if(gen==0 || oldestAncestor==null)
			return null;
		while(gen > 1) {
			if(oldestAncestor.getGeneration() != gen-1)	// test proper change in generation
				throw new RuntimeException("Problem with generation");
			gen = oldestAncestor.getGeneration();
			oldestAncestor = oldestAncestor.getParentRup();
		}
		// make sure it's spontaneous
		if(oldestAncestor.getGeneration() != 0)
			throw new RuntimeException("Problem with generation");
		return oldestAncestor;
	}
	
}
