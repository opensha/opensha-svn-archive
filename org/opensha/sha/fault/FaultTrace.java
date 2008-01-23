package org.opensha.sha.fault;

import java.util.*;
import org.opensha.util.*;
import org.opensha.data.*;
import org.opensha.calc.RelativeLocation;

// Fix - Needs more comments


/**
 *  <b>Title:</b> FaultTrace<p>
 *
 *  <b>Description:</b> This simply contains a vector (or array) of Location
 *  objects representing the top trace of a fault (with non-zero depth if it
 *  buried). <p>
 *
 * @author     Sid Hellman, Steven W. Rock
 * @created    February 26, 2002
 * @version    1.0
 */

public class FaultTrace extends LocationList implements NamedObjectAPI {

    /**
     *  Fault name field
     */
    private String faultName;


    public FaultTrace(String faultName){
        super();
        this.faultName = faultName;
        
    }

    public void setName( String faultName ) { this.faultName = faultName; }

    public String getName() { return faultName; }

    public int getNumLocations() { return size(); }

    /**
     * This returns the total fault-trace length in km
     * @return
     */
    public double getTraceLength() {
      double totLength = 0;
      ListIterator it = this.listIterator();
      Location lastLoc = (Location)it.next();
      Location loc = null;
      while( it.hasNext() ){
        loc = (Location)it.next();
        totLength += RelativeLocation.getHorzDistance(lastLoc, loc);
        lastLoc = loc;
      }
      return totLength;
    }
    
    /**
     * This returns the average strike (weight average by length)
     * @return
     */
    public double getAveStrike() {
      double totLength = 0, length=0;
      ListIterator it = this.listIterator();
      Location lastLoc = (Location)it.next();
      Location loc = null;
      double aveStrike=0;
      while( it.hasNext() ){
        loc = (Location)it.next();
        length = RelativeLocation.getHorzDistance(lastLoc, loc);
        aveStrike += RelativeLocation.getAzimuth(lastLoc, loc) * length;
        totLength += length;
        lastLoc = loc;
      }
      throw new RuntimeException("This needs to be fixed for case where aximuths that cross the north direction (e.g., values of 10 & 350 average to 180");
      //return aveStrike/totLength;
    }

    /*
     * Calculates  minimum distance of this faultTrace from the user provided fault trace.
     * Returns the distance in km.
     * 
     * @param faultTrace FaultTrace from where distance needs to be calculated
     */
    public double getMinDistance(FaultTrace faultTrace) {
    	// calculate the minimum fault trace distance
		double minFaultTraceDist = Double.POSITIVE_INFINITY;
		double dist;
		for(int i=0; i<faultTrace.getNumLocations(); ++i) {
			dist = getMinHorzDistToLine(faultTrace.getLocationAt(i));
			if(dist<minFaultTraceDist) minFaultTraceDist = dist;
		}
		return minFaultTraceDist;
    }

    
    private final static String TAB = "  ";
    public String toString(){

        StringBuffer b = new StringBuffer(C);
        b.append('\n');
        b.append(TAB + "Name = " + faultName);

        b.append( super.toString() ) ;
        return b.toString();

    }


}
