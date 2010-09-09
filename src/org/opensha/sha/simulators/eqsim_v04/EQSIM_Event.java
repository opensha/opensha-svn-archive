package org.opensha.sha.simulators.eqsim_v04;

import java.util.ArrayList;

public class EQSIM_Event extends ArrayList<EventRecord> {
	
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
	
	public void addEventRecord(EventRecord eventRecord){
		if(isSameEvent(eventRecord))
			add(eventRecord);
		else throw new RuntimeException("Can't add because event IDs differ");
	}
	
	public boolean isSameEvent(EventRecord eventRecord) {
		return (eventRecord.getID() == event_id);
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
	 * This returns the time of the event in years
	 * @return
	 */
	public double getTimeInYears() { return time/General_EQSIM_Tools.SECONDS_PER_YEAR;}

	
	/**
	 * This tells whether the event has data for slip on each element
	 * @return
	 */
	public boolean hasElementSlipsAndIDs() {
		boolean hasThem = true;
		for (EventRecord evRec : this) {
			if(!evRec.hasElementSlipsAndIDs()) hasThem = false;  // false is any event record lacks slips and IDs
		}
		return hasThem;
	}
	
	public int getNumElements() {
		int num = 0;
		for (EventRecord evRec : this) {
			num += evRec.getElementID_List().size();
		}
		return num;
	}
	
	/**
	 * This returns a complete list of element IDs for this event 
	 * (it loops over all the event records). The results are in the same
	 * order as returned by getAllElementSlips().
	 * @return
	 */
	public ArrayList<Integer> getAllElementIDs() {
		if(hasElementSlipsAndIDs()) {
			ArrayList<Integer> idList = new ArrayList<Integer>();
			for(int r=0; r<this.size();r++) {
				EventRecord er = get(r);
				idList.addAll(er.getElementID_List());
			}
			return idList;
		}
		else return null;
	}
	
	/**
	 * This returns a complete list of element Slips for this event 
	 * (it loops over all the event records).  The results are in the same
	 * order as returned by getAllElementIDs().
	 * @return
	 */
	public ArrayList<Double> getAllElementSlips() {
		if(hasElementSlipsAndIDs()) {
			ArrayList<Double> slipList = new ArrayList<Double>();
			for(int r=0; r<this.size();r++) {
				EventRecord er = get(r);
				slipList.addAll(er.getElementSlipList());
			}
			return slipList;
		}
		else return null;
	}

	
}
