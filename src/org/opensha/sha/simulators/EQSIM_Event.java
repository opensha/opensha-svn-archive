package org.opensha.sha.simulators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.opensha.commons.geo.Location;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Event records are ordered such that the first is where the event nucleated
 * @author field
 *
 */
public class EQSIM_Event implements Comparable<EQSIM_Event>, Iterable<EventRecord>, Serializable {
	
	int event_id;			
	double magnitude;		// (same for all records for the event)
	double time;			// seconds from start of simulation (same for all records for the event)
	double duration;		// seconds (same for all records for the event)
	
	private List<EventRecord> records;

	public EQSIM_Event(EventRecord eventRecord) {
		this(Lists.newArrayList(eventRecord));
	}

	public EQSIM_Event(List<EventRecord> records) {
		this.records = records;
		Preconditions.checkArgument(!records.isEmpty());
		EventRecord firstRecord = records.get(0);
		this.event_id = firstRecord.getID();
		this.magnitude=firstRecord.getMagnitude();
		this.time=firstRecord.getTime();
		this.duration=firstRecord.getDuration();
	}
	
	// for cloning/serialization purposes
	private EQSIM_Event() {
		
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
	
	/**
	 * The compares the event times, returning
	 *   0 if they are the same
	 *  -1 if that passed in is greater
	 *   1 if that passed in is less
	 */
	public int compareTo(EQSIM_Event event) {
		double thisTime = this.getTime();
		double thatTime = event.getTime();
		int cmp = Double.compare(thisTime, thatTime);
		if (cmp == 0)
			cmp = new Integer(event_id).compareTo(event.event_id);
		return cmp;
	}
	
	public void addEventRecord(EventRecord eventRecord){
		if(isSameEvent(eventRecord))
			records.add(eventRecord);
		else throw new RuntimeException("Can't add because event IDs differ");
	}
	
	public boolean isSameEvent(EventRecord eventRecord) {
		return (eventRecord.getID() == event_id);
	}
	
	
	/**
	 * Note that das must be supplied in meters.
	 * @param sectId
	 * @param das - meters
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
	
	/**
	 * This returns true if any one section ID (from the event records) is contained in
	 * the list passed in (e.g., used in SCEC VDO).
	 * @param sectId
	 * @return
	 */
	public boolean doesEventIncludeFault(HashSet<Integer> sectsForFault) {
		for(EventRecord eventRecord: this)
			if(sectsForFault.contains(eventRecord.sectionID))
				return true;
		return false;
	}
	
	public void setID(int event_id) {
		this.event_id = event_id;
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
	 * This tells whether the event has element slips on at least one event record
	 * (the Ward event file has some records with no slips; were these moved to other 
	 * records because the mags are OK?) 
	 * @return
	 */
	public boolean hasElementSlipsAndIDs() {
		for (EventRecord evRec : this) {
			if(evRec.hasElementSlipsAndIDs())
				return true;  // true if any event record has slips and IDs
		}
		return false;
	}
	

	/**
	 * This tells whether the event has element slips on all event records
	 * (the Ward event file has some records with no slips; were these moved to other 
	 * records because the mags are OK?) 
	 * @return
	 */
	public boolean hasElementSlipsAndIDsOnAllRecords() {
		boolean hasThem = true;
		for (EventRecord evRec : this) {
			if(!evRec.hasElementSlipsAndIDs()) hasThem = false;  // false is any event record lacks slips and IDs
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
	 * This returns a complete list of elements for this event 
	 * (it loops over all the event records). The results are in the same
	 * order as returned by getAllElementSlips().
	 * @return
	 */
	public ArrayList<SimulatorElement> getAllElements() {
		if(hasElementSlipsAndIDs()) {
			ArrayList<SimulatorElement> elementList = new ArrayList<SimulatorElement>();
			for(EventRecord er:this) {
				elementList.addAll(er.getRectangularElements());
			}
			return elementList;
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
	 * This utility finds the shortest distance between the ends of two event records
	 * @param er1_1stEnd - the vertex at the first end of event record 1
	 * @param er1_2ndEnd - the vertex at the other end of event record 1
	 * @param er2_1stEnd - the vertex at the first end of event record 2
	 * @param er2_2ndEnd - the vertex at the other end of event record 2
	 * @return
	 */
	private static double getMinDistBetweenEventRecordEnds(Vertex er1_1stEnd, Vertex er1_2ndEnd, Vertex er2_1stEnd, Vertex er2_2ndEnd) {
		double min1 = Math.min(er1_1stEnd.getLinearDistance(er2_1stEnd), er1_1stEnd.getLinearDistance(er2_2ndEnd));
		double min2 = Math.min(er1_2ndEnd.getLinearDistance(er2_1stEnd), er1_2ndEnd.getLinearDistance(er2_2ndEnd));
		return Math.min(min1, min2);
	}
	
	
	
	/**
	 * This attempts to find the distance along rupture for each element,
	 * (normalized by the total length of rupture).  The start and end of the
	 * rupture is arbitrary.
	 * 
	 * The results are in the same order as returned by getAllElementIDs().
	 * 
	 * This calculation is complicated and approximate for many reasons.
	 * 
	 * For example, say you have a small branch extending off a longer rupture,
	 * then the position of this fork is determined more by when it occurs rather
	 * than where it occurs (e.g., if it slips after all others, then it is positioned
	 * at the end, even if the branch is near the middle of the long fault).
	 * 
	 * 
	 * @return
	 */
	public double[] getNormDistAlongRupForElements() {
		if(hasElementSlipsAndIDsOnAllRecords()) {
			
			// there are two tricky issues here 1) the ordering of of EventRecords is by 
			// occurrence, not position along rupture, and 2) DAS values may need to be
			// flipped when stitching the rupture together.
			
			// store info for each event record (ER)
			Vertex[] vertexForMinDAS_forER = new Vertex[size()];
			Vertex[] vertexForMaxDAS_forER = new Vertex[size()];
			double[] minDAS_forER = new double[size()];	// meters
			double[] maxDAS_forER = new double[size()];	// meters
			boolean[] flipER = new boolean[size()];
			for(int er_index=0;er_index<size();er_index++) {
				EventRecord er = get(er_index);
				minDAS_forER[er_index] = er.getMinDAS();	// meters
				maxDAS_forER[er_index] = er.getMaxDAS();	// meters
				vertexForMinDAS_forER[er_index] = er.getVertxForMinDAS();
				vertexForMaxDAS_forER[er_index] = er.getVertxForMaxDAS();
			}
			
			
			// find the correct order for ERs along rupture ("min" is short for das_lo_vertex & "max" is short for das_hi_vertex)
			ArrayList<Integer> reorderedIndices = new ArrayList<Integer>(); // the order along the length, rather than temporal order
			if(size()>1) {
				// find the two closest points between the first two event records
				double min1_min2_dist = vertexForMinDAS_forER[0].getLinearDistance(vertexForMinDAS_forER[1]);
				double min1_max2_dist = vertexForMinDAS_forER[0].getLinearDistance(vertexForMaxDAS_forER[1]);
				double distFromMin1 = Math.min(min1_min2_dist, min1_max2_dist);

				double max1_min2_dist = vertexForMaxDAS_forER[0].getLinearDistance(vertexForMinDAS_forER[1]);
				double max1_max2_dist = vertexForMaxDAS_forER[0].getLinearDistance(vertexForMaxDAS_forER[1]);
				double distFromMax1 = Math.min(max1_min2_dist, max1_max2_dist);
				
				if(distFromMin1<distFromMax1) {// closer to the min-DAS end (put second in front of first)
					reorderedIndices.add(1);
					reorderedIndices.add(0);
				} 
				else {
					reorderedIndices.add(0);
					reorderedIndices.add(1);
				}
				
				// now add any others to beginning or end of list depending on which is closer 
				for(int er_index=2; er_index<size(); er_index++) {
					// distance from first ER
					double distFromFirstER = getMinDistBetweenEventRecordEnds(
							vertexForMinDAS_forER[reorderedIndices.get(0)],
							vertexForMaxDAS_forER[reorderedIndices.get(0)],
							vertexForMinDAS_forER[er_index],
							vertexForMaxDAS_forER[er_index]);
							
					// distance from last ER
					double distFromLastER = getMinDistBetweenEventRecordEnds(
							vertexForMinDAS_forER[reorderedIndices.get(reorderedIndices.size()-1)],
							vertexForMaxDAS_forER[reorderedIndices.get(reorderedIndices.size()-1)],
							vertexForMinDAS_forER[er_index],
							vertexForMaxDAS_forER[er_index]);
					
					if(distFromFirstER<distFromLastER)// it's closest to the first ER so add to beginning
						reorderedIndices.add(0, er_index);
					else
						reorderedIndices.add(er_index);
				}
			}
			else {
				reorderedIndices.add(0);   // only one ER
			}
			
// for(Integer index:reorderedIndices) System.out.println(get(index).getSectionID());

			
			// FILL in flipER (whether event records need to be flipped when stitched together)
			// determine whether to flip the first event record (if max das is not the closest to the second event record)
			if(size()>1) {
				double min1_min2_dist = vertexForMinDAS_forER[reorderedIndices.get(0)].getLinearDistance(vertexForMinDAS_forER[reorderedIndices.get(1)]);
				double min1_max2_dist = vertexForMinDAS_forER[reorderedIndices.get(0)].getLinearDistance(vertexForMaxDAS_forER[reorderedIndices.get(1)]);
				double distFromMin1 = Math.min(min1_min2_dist, min1_max2_dist);

				double max1_min2_dist = vertexForMaxDAS_forER[reorderedIndices.get(0)].getLinearDistance(vertexForMinDAS_forER[reorderedIndices.get(1)]);
				double max1_max2_dist = vertexForMaxDAS_forER[reorderedIndices.get(0)].getLinearDistance(vertexForMaxDAS_forER[reorderedIndices.get(1)]);
				double distFromMax1 = Math.min(max1_min2_dist, max1_max2_dist);
				
				if(distFromMin1<distFromMax1) // closer to the min-DAS end
					flipER[0]=true;
				else
					flipER[0]=false;				
			}
			else {
				flipER[0]=false;   // don't flip if only one ER
			}
			// now set any other records as flipped
			for(int er_index=1; er_index<size(); er_index++) {
				// get last vertex of previous ER
				int lastER_index = reorderedIndices.get(er_index-1);
				Vertex lastVertex;
				if(flipER[lastER_index])	// get min-das vertex if last one was flipped
					lastVertex=vertexForMinDAS_forER[lastER_index];
				else
					lastVertex=vertexForMaxDAS_forER[lastER_index];
				
				// now flip present ER if lastVertex is closer to max-das on present ER
				if(lastVertex.getLinearDistance(vertexForMinDAS_forER[reorderedIndices.get(er_index)]) < lastVertex.getLinearDistance(vertexForMaxDAS_forER[reorderedIndices.get(er_index)]))
					flipER[er_index]=false;
				else
					flipER[er_index]=true;
			}
			
			// compute the start distance along for each ER
			double[] startDistAlongForReorderedER = new double[size()];
			startDistAlongForReorderedER[0]=0;
			for(int i=1;i<size();i++)
				startDistAlongForReorderedER[i]=startDistAlongForReorderedER[i-1]+get(reorderedIndices.get(i-1)).getLength();	// meters

			// now compute distance along for each element
			int rectElemIndex=-1;
			double totalRuptureLength = getLength();	// this is in meters!
			double[] distAlongForEachElement = new double[getNumElements()];
			for(int er_index=0;er_index<size();er_index++) {
				double startDistanceAlong = startDistAlongForReorderedER[reorderedIndices.indexOf(new Integer(er_index))];
//				int reorderedIndex = reorderedIndices.get(er_index);
				EventRecord er = get(er_index);
// System.out.println("er.getSectionID()="+er.getSectionID()+"\t"+"startDistanceAlong="+startDistanceAlong);
				for(SimulatorElement rectElem : er.getRectangularElements()) {
					rectElemIndex += 1;
					double aveDAS = rectElem.getAveDAS()*1000;	// convert to meters
					if(flipER[er_index]) {	// index for flipER is not reordered!
						distAlongForEachElement[rectElemIndex] = (startDistanceAlong+maxDAS_forER[er_index]-aveDAS)/totalRuptureLength;
					}
					else {
						distAlongForEachElement[rectElemIndex] = (startDistanceAlong+aveDAS-minDAS_forER[er_index])/totalRuptureLength;
					}
				}
			}
			
			double minDistAlong = Double.MAX_VALUE;
			double maxDistAlong = Double.NEGATIVE_INFINITY;
			for(double distAlong:distAlongForEachElement) {
				if(minDistAlong>distAlong) minDistAlong=distAlong;
				if(maxDistAlong<distAlong) maxDistAlong=distAlong;
			}
// System.out.println((float)totalRuptureLength+"\t"+(float)(minDistAlong*totalRuptureLength)+"\t"+(float)(maxDistAlong*totalRuptureLength));

			return distAlongForEachElement;
		} else 
			return null;
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
	 * This returns the area-averaged slip in meters
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
	
	public EQSIM_Event cloneNewTime(double timeSeconds, int newID) {
		EQSIM_Event o = new EQSIM_Event();
//		for (int i=1; i<size(); i++)
//			o.add(get(i));
		o.time = timeSeconds;
		o.event_id = newID;
		o.magnitude = magnitude;
		o.duration = duration;
		o.records = records;
		return o;
	}

	@Override
	public Iterator<EventRecord> iterator() {
		return records.iterator();
	}
	
	public int size() {
		return records.size();
	}
	
	public EventRecord get(int index) {
		return records.get(index);
	}

}
