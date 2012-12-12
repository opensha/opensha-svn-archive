package org.opensha.sha.simulators.eqsim_v04;

import java.util.ArrayList;

/**
 * Event records are ordered such that the first is where the event nucleated
 * @author field
 *
 */
public class EQSIM_Event extends ArrayList<EventRecord> implements Comparable<EQSIM_Event> {
	
	int event_id;			
	double magnitude;		// (same for all records for the event)
	double time;			// seconds from start of simulation (same for all records for the event)
	double duration;		// seconds (same for all records for the event)

	public EQSIM_Event(EventRecord eventRecord) {
		this.add(eventRecord);
		this.event_id = eventRecord.getID();
		this.magnitude=eventRecord.getMagnitude();
		this.time=eventRecord.getTime();
		this.duration=eventRecord.getDuration();
	}
	
	public String toString() {
		String info="";
		info += "event_id="+event_id+"\n";
		info += "magnitude="+magnitude+"\n";
		info += "time="+time+"\n";
		info += "duration="+duration+"\n";
		info += "getLength()="+getLength()+"\n";
		info += "getArea()="+getArea()+"\n";
		info += "size()="+size()+"\n";
		for(int i=0;i<this.size();i++) {
			EventRecord evRec = get(i);
			info += "EventRecord "+i+":\n"+evRec.toString();
		}
		
		return info;
	}
	
	public int compareTo(EQSIM_Event event) {
		double thisTime = this.getTime();
		double thatTime = event.getTime();
		if(thisTime<thatTime)
			return -1;
		else if(thisTime>thatTime)
			return 1;
		else
			return 0;  // they're equal
	}
	
	public void addEventRecord(EventRecord eventRecord){
		if(isSameEvent(eventRecord))
			add(eventRecord);
		else throw new RuntimeException("Can't add because event IDs differ");
	}
	
	public boolean isSameEvent(EventRecord eventRecord) {
		return (eventRecord.getID() == event_id);
	}
	
	
	/**
	 * Note that das must be supplied in meters.
	 * @param sectId
	 * @param das
	 * @return
	 */
	public boolean doesEventIncludeSectionAndDAS(int sectId, double das) {
		boolean includes = false;
		for(EventRecord eventRecord: this)
			if(eventRecord.getSectionID() == sectId && das<eventRecord.das_hi && das>eventRecord.das_lo)
				includes = true;
		return includes;
	}
	
	/**
	 * @param sectId
	 * @return
	 */
	public boolean doesEventIncludeSection(int sectId) {
		for(EventRecord eventRecord: this)
			if(eventRecord.getSectionID() == sectId)
				return true;
		return false;
	}

	
	public int getID() { return event_id;}
	
	public double getMagnitude() { return magnitude;}
	
	public double getDuration() { return duration;}
	
	
	/**
	 * This returns the time of the event in seconds
	 * @return
	 */
	public double getTime() { return time;}
	
	/**
	 * This overrides the event time with the value passed in
	 * @param time
	 */
	public void setTime(double time) {
		this.time=time;
		for(EventRecord rec:this) {
			rec.setTime(time);
		}
	}
	
	/**
	 * This returns the time of the event in years
	 * @return
	 */
	public double getTimeInYears() { return time/General_EQSIM_Tools.SECONDS_PER_YEAR;}

	
	/**
	 * This tells whether the event has data for slip on each element
	 * @return
	 */
//	public boolean hasElementSlipsAndIDs() {	// CHANGED BECAUSE WARD HAS SOME RECORDS THAT HAVE NO SLIPS (MOVED TO OTHER RECORDS?)
//		boolean hasThem = true;
//		for (EventRecord evRec : this) {
//			if(!evRec.hasElementSlipsAndIDs()) hasThem = false;  // false is any event record lacks slips and IDs
//		}
//		return hasThem;
//	}
	public boolean hasElementSlipsAndIDs() {
		boolean hasThem = false;
		for (EventRecord evRec : this) {
			if(evRec.hasElementSlipsAndIDs()) hasThem = true;  // true if any event record has slips and IDs
		}
		return hasThem;
	}
	
	public int getNumElements() {
		int num = 0;
		for (EventRecord evRec : this) {
			num += evRec.getElementIDs().length;
		}
		return num;
	}
	
	/**
	 * This returns a complete list of element IDs for this event 
	 * (it loops over all the event records). The results are in the same
	 * order as returned by getAllElementSlips().
	 * @return
	 */
	public int[] getAllElementIDs() {
		if(hasElementSlipsAndIDs()) {
			ArrayList<int[]> idList = new ArrayList<int[]>();
			int totSize = 0;
			for(int r=0; r<this.size();r++) {
				int[] ids = get(r).getElementIDs();
				totSize += ids.length;
				idList.add(ids);
			}
			int[] ids = new int[totSize];
			int index = 0;
			for (int i=0; i<idList.size(); i++) {
				int[] recIDs = idList.get(i);
				System.arraycopy(recIDs, 0, ids, index, recIDs.length);
				index += recIDs.length;
			}
			return ids;
		} else return null;
	}
	
	/**
	 * This returns a complete list of element Slips for this event 
	 * (it loops over all the event records).  The results are in the same
	 * order as returned by getAllElementIDs().
	 * @return
	 */
	public double[] getAllElementSlips() {
		if(hasElementSlipsAndIDs()) {
			ArrayList<double[]> slipList = new ArrayList<double[]>();
			int totSize = 0;
			for(int r=0; r<this.size();r++) {
				double[] slips = get(r).getElementSlips();
				totSize += slips.length;
				slipList.add(slips);
			}
			double[] slips = new double[totSize];
			int index = 0;
			for (int i=0; i<slipList.size(); i++) {
				double[] recSlips = slipList.get(i);
				System.arraycopy(recSlips, 0, slips, index, recSlips.length);
				index += recSlips.length;
			}
			return slips;
		} else return null;
	}
	
	/**
	 * This returns the event area in meters squared
	 * @return
	 */
	public double getArea() {
		double area=0;
		for(EventRecord evRec:this) area += evRec.getArea();
		return area;
	}
	
	
	
	
	/**
	 * This returns the event length in meters
	 * (computed as the sum of (das_hi-das_lo) from event records
	 * @return
	 */
	public double getLength() {
		double length=0;
		for(EventRecord evRec:this) length += evRec.getLength();
		return length;
	}

	/**
	 * This returns the average slip in meters
	 * @return
	 */
	public double getMeanSlip() {
		double aveSlip=0;
		double totalArea=0;
		for(EventRecord evRec:this) {
			aveSlip += evRec.getMeanSlip()*evRec.getArea();
			totalArea+=evRec.getArea();
		}
		return aveSlip/totalArea;
	}

}
