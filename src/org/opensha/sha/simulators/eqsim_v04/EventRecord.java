package org.opensha.sha.simulators.eqsim_v04;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class EventRecord {
	
	int event_id;			
	double magnitude;		// (same for all records for the event)
	double time;			// seconds from start of simulation (same for all records for the event)
	double duration;		// seconds (same for all records for the event)
    int sid;				// section ID (for just this record)
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
    
    ArrayList<Double> elementSlipList;
    ArrayList<Integer> elementID_List;
    
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
	    this.sid = Integer.parseInt(tok.nextToken());
	    this.depth_lo = Double.parseDouble(tok.nextToken());
	    this.depth_hi = Double.parseDouble(tok.nextToken()); 
	    this.das_lo = Double.parseDouble(tok.nextToken());
	    this.das_hi = Double.parseDouble(tok.nextToken());
	    this.hypo_depth = Double.parseDouble(tok.nextToken());
	    this.hypo_das = Double.parseDouble(tok.nextToken());
	    this.area = Double.parseDouble(tok.nextToken());
	    this.mean_slip = Double.parseDouble(tok.nextToken());
	    this.moment = Double.parseDouble(tok.nextToken());
	    this.shear_before = Double.parseDouble(tok.nextToken());
	    this.shear_after = Double.parseDouble(tok.nextToken());
	    this.normal_before = Double.parseDouble(tok.nextToken());
	    this.normal_after = Double.parseDouble(tok.nextToken());
	    while(tok.hasMoreTokens())
	    	comment_text += tok.nextToken()+" ";
	    
	    elementSlipList = new ArrayList<Double>();
	    elementID_List = new ArrayList<Integer>(); 


	}
	
	/**
	 * This extracts and saves the slip and element ID info
	 * from a Slip-Map-Record line.
	 * @param fileLine
	 */
	public void addSlipAndElementData(String fileLine) {
		StringTokenizer tok = new StringTokenizer(fileLine);
		int kindOfLine = Integer.parseInt(tok.nextToken());
		if(kindOfLine != 201) throw new RuntimeException("not a slip-map-record line type");

		tok.nextToken();	// depth_lo
		tok.nextToken();	// depth_hi
		tok.nextToken();	// das_lo
		tok.nextToken();	// das_hi
		tok.nextToken();	// area
		elementSlipList.add(Double.parseDouble(tok.nextToken()));	// mean_slip
		tok.nextToken();	// moment
		tok.nextToken();	// shear_before
		tok.nextToken();	// shear_after
		tok.nextToken();	// normal_before
		tok.nextToken();	// normal_after
		int element_id = Integer.parseInt(tok.nextToken());
		if (element_id <= 0) throw new RuntimeException("Don't support zero or negative element IDs");
		elementID_List.add(element_id);
		// the rest are comments
	}
	
	public int getIndex() { return event_id;}
	
	public double getMagnitude() { return magnitude;}
	
	public double getDuration() { return duration;}
	
	public double getTime() { return time;}

}
