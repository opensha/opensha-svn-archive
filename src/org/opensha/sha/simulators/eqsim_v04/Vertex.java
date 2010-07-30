/**
 * 
 */
package org.opensha.sha.simulators.eqsim_v04;

import org.opensha.commons.geo.Location;

/**
 * @author field
 *
 */
public class Vertex extends Location {
	
	private int index, traceFlag;
	private double das;
	
	
	/**
	 * 
	 * @param latitute
	 * @param longitude
	 * @param depth
	 * @param index - this is an integer ID for this vertex (should be unique among all vertices)
	 * @param das - distance along trace
	 * @param traceFlag - tells whether is on the fault trace  (0 means no; 1 means yes, but not
	 * 		              the first or last point; 2 means yes & it's the first; and 3 means yes 
	 *                    & it's the last point)
	 */
	public Vertex(double latitute,double longitude,double depth, int index, double das, int traceFlag) {
		super(latitute, longitude, depth);
		this.index=index;
		this.das=das;
		this.traceFlag=traceFlag;
	}
	
	public Vertex(double latitute,double longitude,double depth) {
		super(latitute, longitude, depth);
		das = Double.NaN;
		index = -1;
		traceFlag = -1;
	}

	public Vertex(Location loc) {
		super(loc.getLatitude(), loc.getLongitude(), loc.getDepth());
		das = Double.NaN;
		index = -1;
		traceFlag = -1;
	}


	public int getIndex() {return index;}
	
	public double getDAS() {return das;}

	public int getTraceFlag() {return traceFlag;}
	
}
