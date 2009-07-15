/**
 * 
 */
package org.opensha.sha.faultSurface;

import java.util.ArrayList;

import org.opensha.commons.calc.RelativeLocation;
import org.opensha.commons.data.Direction;
import org.opensha.commons.data.Location;

/**
 *  <b>Title:</b> EqualLengthSubSectionsTrace<p>
 *
 *  <b>Description:</b> This class divides a fault trace into a number of subTraces.
 *  All the traces are equal in length. Length of each trace is equal to or less than the provided
 *  sub section length <p>
 *
 * @author     Vipin Gupta
 * @created    Sep 18, 2006
 * @version    1.0
 */
public class EqualLengthSubSectionsTrace {
	private int numSubSections;
	private ArrayList<FaultTrace> subSectionTraceList;
	
	/**
	 * It accepts a fault trace and divides into sub sections of equal lengths
	 * @param faultTrace 
	 * @param maxSubSectionLen Maximum length of each subsection
	 */
	public EqualLengthSubSectionsTrace(FaultTrace faultTrace, double maxSubSectionLen) {
		
		// find the number of sub sections
		double numSubSec= faultTrace.getTraceLength()/maxSubSectionLen;
		if(Math.floor(numSubSec)!=numSubSec) numSubSections=(int)Math.floor(numSubSec)+1;
		else numSubSections=(int)numSubSec;
		// find the length of each sub section
		double subSecLength = faultTrace.getTraceLength()/numSubSections;
		double distance = 0, distLocs=0;;
		int numLocs = faultTrace.getNumLocations();
		int index=0;
		subSectionTraceList = new ArrayList<FaultTrace>();
		Location prevLoc = faultTrace.getLocationAt(index);
		while(index<numLocs && subSectionTraceList.size()<numSubSections) {
			FaultTrace subSectionTrace = new FaultTrace(faultTrace.getName()+" "+(subSectionTraceList.size()+1));
			subSectionTraceList.add(subSectionTrace);
			subSectionTrace.addLocation(prevLoc);
			++index;
			distance = 0;
			while(true && index<faultTrace.getNumLocations()) {
				Location nextLoc = faultTrace.getLocationAt(index);
				distLocs = RelativeLocation.getApproxHorzDistance(prevLoc, nextLoc);
				distance+= distLocs;
				if(distance<subSecLength) { // if sub section length is greater than distance, then get next point on trace
					prevLoc = nextLoc;
					subSectionTrace.addLocation(prevLoc);
					++index;
				} else {
					Direction direction = RelativeLocation.getDirection(prevLoc, nextLoc);
					direction.setHorzDistance(subSecLength-(distance-distLocs));
					prevLoc = RelativeLocation.getLocation(prevLoc, direction);
					subSectionTrace.addLocation(prevLoc);
					--index;
					break;
				}
			}
		}
	}
	
	/**
	 * Get the number of sub sections
	 * 
	 * @return
	 */
	public int getNumSubSections() {
		return subSectionTraceList.size();
	}
	
	/**
	 * Get the fault trace for Sub-Section specified by the index
	 * @param index
	 * @return
	 */
	public FaultTrace getSubSectionTrace(int index) {
		return subSectionTraceList.get(index);
	}
	
}
