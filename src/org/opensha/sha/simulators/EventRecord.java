package org.opensha.sha.simulators;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
    
    List<SimulatorElement> rectElementsList;	// this is all the elements, not just those used here
    
    /**
     * No arg constructor
     */
    public EventRecord(List<SimulatorElement> rectElementsList) {
    	this.rectElementsList=rectElementsList; 
    }

	public EventRecord(String fileLine, List<SimulatorElement> rectElementsList) {
    	this.rectElementsList=rectElementsList; 
		StringTokenizer tok = new StringTokenizer(fileLine);
		int kindOfLine = Integer.parseInt(tok.nextToken());
		if(kindOfLine != 200) throw new RuntimeException("wrong line type");
		String idString = tok.nextToken();
		try {;
			this.event_id = Integer.parseInt(idString);
		} catch (NumberFormatException e2) {
			// try setting it as a double (some in Ward's file are specified as, for example, "1e+05"
			this.event_id = (int)Math.round(Double.parseDouble(idString));
		}
		this.magnitude = Double.parseDouble(tok.nextToken());
		this.time = Double.parseDouble(tok.nextToken());
		this.duration = Double.parseDouble(tok.nextToken());
	    this.sectionID = Integer.parseInt(tok.nextToken());
	    this.depth_lo = Double.parseDouble(tok.nextToken());
	    this.depth_hi = Double.parseDouble(tok.nextToken()); 
	    this.das_lo = Double.parseDouble(tok.nextToken());
	    this.das_hi = Double.parseDouble(tok.nextToken());
	    try {
			this.hypo_depth = Double.parseDouble(tok.nextToken());	// some models don't have this (e.g., Ward's)
		} catch (NumberFormatException e1) {
			this.hypo_depth = Double.NaN;
		}
	    try {
			this.hypo_das = Double.parseDouble(tok.nextToken());	// some models don't have this (e.g., Ward's)
		} catch (NumberFormatException e1) {
			this.hypo_das = Double.NaN;
		}
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
	
	public void addSlip(int id, double slip) {
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
	
	public void setID(int id) {
		this.event_id = id;
	}
	
	public int getSectionID() {return sectionID;}
	
	public void setSectionID(int sectionID) {
		this.sectionID = sectionID;
	}
	
	public double getMagnitude() { return magnitude;}
	
	public void setMagnitude(double mag) {
		this.magnitude = mag;
	}
	
	public double getDuration() { return duration;}
	
	public double getTime() { return time;}
	
	public void setTime(double time) { this.time=time;}
	
	/**
	 * This returns the ID of the element closest to the hypocenter 
	 * (or first point to rupture on this EventRecord)
	 * @return
	 */
	public int getHypocenterElementID() {
		double minDist = Double.MAX_VALUE;
		int id = -1;
		for(int elemID:getElementIDs()) {
			SimulatorElement rectElem = rectElementsList.get(elemID-1);  // index is ID-1
			double dist = (rectElem.getAveDAS()-hypo_das)*(rectElem.getAveDAS()-hypo_das) + (rectElem.getAveDepth()-hypo_depth)*(rectElem.getAveDepth()-hypo_depth);
			if(dist<minDist) {
				minDist=dist;
				id = rectElem.getID();
			}
		}
		return id;
	}
	
	public synchronized int[] getElementIDs() {
		if (elementIDs.length > numElements) {
			// trim down the array;
			elementIDs = Arrays.copyOf(elementIDs, numElements);
		}
		return elementIDs;
	}
	
	public void setElementIDsAndSlips(int[] elementIDs, double[] elementSlips) {
		if (elementIDs != null && elementSlips != null)
			Preconditions.checkState(elementIDs.length == elementSlips.length);
		if (elementIDs != null) {
			this.elementIDs = elementIDs;
			numElements = elementIDs.length;
		}
		if (elementSlips != null) {
			this.elementIDs = elementIDs;
			numElements = elementIDs.length;
		}
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
	
	public void setMoment(double moment) {
		this.moment = moment;
	}
	
	/**
	 * 
	 * @return min DAS in meters
	 */
	public double getMinDAS() { return das_lo;}
	
	/**
	 * 
	 * @return max DAS in meters
	 */
	public double getMaxDAS() { return das_hi;}
	
	
	/**
	 * This returns a vertex corresponding to the minimum DAS
	 * (although there may be more than one with the same DAS)
	 * @return
	 */
	public Vertex getVertxForMinDAS() {
		Vertex minVertex = null;
		double minDAS = Double.MAX_VALUE;
		for(int elemID : getElementIDs()) {
			SimulatorElement elem = rectElementsList.get(elemID-1);	// index is ID-1
			double elemMinDAS = elem.getMinDAS();
			if(elemMinDAS < minDAS) {
				minDAS = elemMinDAS;
				minVertex = elem.getVertexForMinDAS();
			}
		}
		
		// TEST - works for RSQSIM and Ward, not for Pollitz
//		if(minVertex != null) {
//			double testDiffDAS = Math.abs(minVertex.getDAS() - getMinDAS()/1000);	// km
//			if(testDiffDAS>1.0)
//				throw new RuntimeException("testDiffMinDAS>5:  "+testDiffDAS);			
//		}
		
		
		return minVertex;
	}
	
	
	/**
	 * This returns a vertex corresponding to the maximum DAS
	 * (although there may be more than one with the same DAS)
	 * @return
	 */
	public Vertex getVertxForMaxDAS() {
		Vertex maxVertex = null;
		double maxDAS = Double.NEGATIVE_INFINITY;
		for(int elemID : elementIDs) {
			SimulatorElement elem = rectElementsList.get(elemID-1);	// index is ID-1
			double elemMaxDAS = elem.getMaxDAS();
			if(elemMaxDAS > maxDAS)
				maxDAS=elemMaxDAS;
				maxVertex = elem.getVertexForMaxDAS();
		}
		// TEST - works for RSQSIM and Ward, not for Pollitz
//		if(maxVertex != null) {
//			double testDiffDAS = Math.abs(maxVertex.getDAS() - getMaxDAS()/1000);	// km
//			if(testDiffDAS>1.0)
//				throw new RuntimeException("testDiffMaxDAS>5:  "+testDiffDAS);
//		}
		
		return maxVertex;
	}
	
	public List<SimulatorElement> getRectangularElements() {
		List<SimulatorElement> re_list = Lists.newArrayList();
		for(int elemID:getElementIDs())
			re_list.add(rectElementsList.get(elemID-1));	// index is ID-1
		return re_list;
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
