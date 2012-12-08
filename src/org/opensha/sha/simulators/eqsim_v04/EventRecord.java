package org.opensha.sha.simulators.eqsim_v04;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

/**
 * This gives information about an event on a specific section 
 * (separate event records are used when an event involves multiple sections)
 * @author field
 *
 */
public class EventRecord {
	
	int event_id;			
	double magnitude;		// (same for all records for the event)
	double time;			// seconds from start of simulation (same for all records for the event)
	double duration;		// seconds (same for all records for the event)
    int sectionID;			// section ID (for just this record)
    double depth_lo;		// meters (for just this record)
    double depth_hi; 		// meters (for just this record)
    double das_lo;			// meters (for just this record)
    double das_hi;			// meters (for just this record)
    double hypo_depth;		// meters (for just this record)
    double hypo_das;		// meters (for just this record)
    double area;			// meters-squared (for just this record)
    double mean_slip;		// meters (for just this record)
    double moment;			// Newton-meters (for just this record, not entire event)
    double shear_before;	// Pascal (for just this record)
    double shear_after;		// Pascal (for just this record)
    double normal_before;	// Pascal (for just this record)
    double normal_after;	// Pascal (for just this record)
    String comment_text;
    
    Double tempSlip;		// used to store slip when multiple elements have the same value (when type 202 records are present)
    
    boolean hasElementSlipsAndIDs = true;
    
    int numElements = 0;
    private static final int element_array_padding = 10;
    double[] elementSlips = new double[0];
    int[] elementIDs = new int[0];
    
    /**
     * No arg constructor
     */
    public EventRecord() {}

	public EventRecord(String fileLine) {
		StringTokenizer tok = new StringTokenizer(fileLine);
		int kindOfLine = Integer.parseInt(tok.nextToken());
		if(kindOfLine != 200) throw new RuntimeException("wrong line type");
		
		this.event_id = Integer.parseInt(tok.nextToken());
		this.magnitude = Double.parseDouble(tok.nextToken());
		this.time = Double.parseDouble(tok.nextToken());
		this.duration = Double.parseDouble(tok.nextToken());
	    this.sectionID = Integer.parseInt(tok.nextToken());
	    this.depth_lo = Double.parseDouble(tok.nextToken());
	    this.depth_hi = Double.parseDouble(tok.nextToken()); 
	    this.das_lo = Double.parseDouble(tok.nextToken());
	    this.das_hi = Double.parseDouble(tok.nextToken());
	    this.hypo_depth = Double.parseDouble(tok.nextToken());
	    this.hypo_das = Double.parseDouble(tok.nextToken());
	    this.area = Double.parseDouble(tok.nextToken());
	    this.mean_slip = Double.parseDouble(tok.nextToken());
	    this.moment = Double.parseDouble(tok.nextToken());
	    
	    // the following has to deal with NA in input files
	    try {
			this.shear_before = Double.parseDouble(tok.nextToken());
		} catch (NumberFormatException e) {
			this.shear_before= Double.NaN;
		}
	    try {
			this.shear_after = Double.parseDouble(tok.nextToken());
		} catch (NumberFormatException e) {
			this.shear_after= Double.NaN;
		}
	    try {
			this.normal_before = Double.parseDouble(tok.nextToken());
		} catch (NumberFormatException e) {
			this.normal_before= Double.NaN;
		}
	    try {
			this.normal_after = Double.parseDouble(tok.nextToken());
		} catch (NumberFormatException e) {
			this.normal_after= Double.NaN;
		}

	    while(tok.hasMoreTokens())
	    	comment_text += tok.nextToken()+" ";
	}
	
	/**
	 * This extracts and saves the slip and element ID info data from a Slip-Map-Record line, 
	 * but only if the slip is assigned to a specific element (the element ID on the line is >0).
	 * @param fileLine
	 */
	public void addSlipAndElementData(String fileLine) {
		
		if(hasElementSlipsAndIDs) {
			StringTokenizer tok = new StringTokenizer(fileLine);
			int kindOfLine = Integer.parseInt(tok.nextToken());
			if(kindOfLine != 201) throw new RuntimeException("not a slip-map-record line type");
			tok.nextToken();	// depth_lo
			tok.nextToken();	// depth_hi
			tok.nextToken();	// das_lo
			tok.nextToken();	// das_hi
			tok.nextToken();	// area
			double slip = Double.parseDouble(tok.nextToken());
			tok.nextToken();	// moment
			tok.nextToken();	// shear_before
			tok.nextToken();	// shear_after
			tok.nextToken();	// normal_before
			tok.nextToken();	// normal_after
			int element_id=0;
			if(tok.hasMoreTokens()) {
				try {
					element_id = Integer.parseInt(tok.nextToken());
					if (element_id <= 0) //throw new RuntimeException("Don't support zero or negative element IDs");
						tempSlip = slip;  // save for when type 202 files are read
				} catch (NumberFormatException e) {
					element_id=0;
				}
			}
			else 
				element_id=0;
/*			
			// test fix for Ward's file
			int nas = Integer.parseInt(tok.nextToken());
			int ndd = Integer.parseInt(tok.nextToken());
			element_id = (nas-1)*4+ndd;
//			System.out.println("WARNING - TEMP FIX IMPOSED ON WARD'S FILE");
			// test ends here
*/			
			if(element_id>0) {
				addSlip(element_id, slip);		
			}
			
			if(element_id==0)
				hasElementSlipsAndIDs=false;
				
		}
		
		// the rest of the line is comments
	}
	
	private void addSlip(int id, double slip) {
		int ind = numElements;
		numElements++;
		elementSlips = Doubles.ensureCapacity(elementSlips, numElements, element_array_padding);
		elementSlips[ind] = slip;
		elementIDs = Ints.ensureCapacity(elementIDs, numElements, element_array_padding);
		elementIDs[ind] = id;
	}
	
	/**
	 * This adds the element ID from a type 202 line.
	 * @param fileLine
	 */
	public void addType202_Line(String fileLine) {
			StringTokenizer tok = new StringTokenizer(fileLine);
			int kindOfLine = Integer.parseInt(tok.nextToken());
			if(kindOfLine != 202) 
				throw new RuntimeException("not a type 202 line; yours is type="+kindOfLine);
			int element_id = Integer.parseInt(tok.nextToken());
			addSlip(element_id, tempSlip);
	}

	
	public int getID() { return event_id;}
	
	public int getSectionID() {return sectionID;}
	
	public double getMagnitude() { return magnitude;}
	
	public double getDuration() { return duration;}
	
	public double getTime() { return time;}
	
	public void setTime(double time) { this.time=time;}
	
	public int[] getElementIDs() {
		if (elementIDs.length > numElements) {
			// trim down the array;
			elementIDs = Arrays.copyOf(elementIDs, numElements);
		}
		return elementIDs;
	}
	
	/**
	 * 
	 * @return mean slip in meters
	 */
	public double getMeanSlip() {return mean_slip;}
	
	/**
	 * 
	 * @return area in meters squared
	 */
	public double getArea() {return area;}
	
	/**
	 * 
	 * @return length in meters
	 */
	public double getLength() { return das_hi-das_lo;}
	
	public double getDepthLo() { return depth_lo;}
	public double getDepthHi() { return depth_hi;}
	
	/**
	 * This gives an array of element slips (meters)
	 * @return
	 */
	public double[] getElementSlips() {
		if (elementSlips.length > numElements) {
			// trim down the array;
			elementSlips = Arrays.copyOf(elementSlips, numElements);
		}
		return elementSlips;
	}
	
	public boolean hasElementSlipsAndIDs() {
		return numElements > 0;
	}
	
	public double getMoment() {
		return moment;
	}

	public String toString() {
		String info = "";
		info += "event_id="+event_id+"\n";			
		info += "magnitude="+magnitude+"\n";			
		info += "time="+time+"\n";			
		info += "duration="+duration+"\n";			
		info += "sectionID="+sectionID+"\n";			
		info += "depth_lo="+depth_lo+"\n";			
		info += "depth_hi="+depth_hi+"\n";			
		info += "das_lo="+das_lo+"\n";			
		info += "das_hi="+das_hi+"\n";			
		info += "event_id="+event_id+"\n";			
		info += "area="+area+"\n";			
		info += "mean_slip="+mean_slip+"\n";			
		info += "moment="+moment+"\n";			
	    return info;
	}
}
