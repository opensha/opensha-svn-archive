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

package scratch.ned.ETAS_Tests;

import org.opensha.commons.exceptions.InvalidRangeException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.FaultUtils;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;


/**
 *
 * <b>Title:</b> EqkRupture<br>
 * <b>Description:</b> <br>
 *
 * @author Ned Field
 * @version 1.0
 */

public class PrimaryAftershock extends EqkRupture {
	
	private int parentID, srcIndex, rupIndex;
	private double originTime;
	
	public PrimaryAftershock() {};
	
	public PrimaryAftershock(ProbEqkRupture probRup) {
		this.setMag(probRup.getMag());
		this.setRuptureSurface(probRup.getRuptureSurface());
		this.setAveRake(probRup.getAveRake());
	}
	
	/**
	 * The ID of the parent that spawned this primary aftershock
	 * @return
	 */
	public int getParentID() {
		return parentID;
	}
	
	/**
	 * Sets the ID of the parent that spawned this primary aftershock
	 * @return
	 */
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	
	/**
	 * The origin time of this event (units are unspecified)
	 * @return
	 */
	public double getOriginTime(){
		return originTime;
	}
	
	/**
	 * Sets the origin time of this event (units are unspecified)
	 * @return
	 */
	public void setOriginTime(double originTime){
		this.originTime = originTime;
	}

	public void setSourceIndex(int srcIndex){
		this.srcIndex = srcIndex;
	}

	public int getSourceIndex(){
		return srcIndex;
	}
	
	public void setRupIndex(int rupIndex){
		this.rupIndex = rupIndex;
	}

	public int getRupIndex(){
		return rupIndex;
	}



}