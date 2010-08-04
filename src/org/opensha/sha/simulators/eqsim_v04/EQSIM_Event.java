package org.opensha.sha.simulators.eqsim_v04;

import java.util.ArrayList;

public class EQSIM_Event extends ArrayList<EventRecord> {
	
	int event_id;			
	double magnitude;		// (same for all records for the event)
	double time;			// seconds from start of simulation (same for all records for the event)
	double duration;		// seconds (same for all records for the event)

	public EQSIM_Event(EventRecord eventRecord) {
		this.add(eventRecord);
		this.event_id = eventRecord.getIndex();
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
		return (eventRecord.getIndex() == event_id);
	}
	
	public int getIndex() { return event_id;}
	
	public double getMagnitude() { return magnitude;}
	
	public double getDuration() { return duration;}
	
	public double getTime() { return time;}

}
