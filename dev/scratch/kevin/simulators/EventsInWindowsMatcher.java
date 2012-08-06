package scratch.kevin.simulators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class EventsInWindowsMatcher {
	
	// inputs
	private List<EQSIM_Event> events;
	private RuptureIdentifier rupIden;
	private double minWindowDurationYears;
	private double windowDurationYears;
	private boolean randomizeEventTimes;
	
	// outputs
	private List<EQSIM_Event> eventsInWindows;
	private List<TimeWindow> timeWindows;
	private HashSet<Integer> matchIDs;
	private double totalWindowDurationYears;
	
	public EventsInWindowsMatcher(List<EQSIM_Event> events,
			RuptureIdentifier rupIden,
			double minWindowDurationYears,
			double windowDurationYears,
			boolean randomizeEventTimes) {
		this.events = events;
		this.rupIden = rupIden;
		this.minWindowDurationYears = minWindowDurationYears;
		this.windowDurationYears = windowDurationYears;
		this.randomizeEventTimes = randomizeEventTimes;
		
		update();
	}
	
	private List<EQSIM_Event> update() {
		eventsInWindows = Lists.newArrayList();
		
		if (events == null || events.isEmpty())
			return null;
		
		if (randomizeEventTimes) {
			double startTime = events.get(0).getTime();
			double simDuration = General_EQSIM_Tools.getSimulationDuration(events);
			
			ArrayList<EQSIM_Event> randomizedEvents = new ArrayList<EQSIM_Event>();
			
			for (EQSIM_Event e : events) {
				EQSIM_Event r = new EQSIM_Event(e.get(0));
				if (e.size() > 1)
					r.addAll(e.subList(1, e.size()));
				double[] origTimes = new double[e.size()];
				for (int i=0; i<e.size(); i++)
					origTimes[i] = e.get(i).getTime();
				r.setTime(startTime+Math.random()*simDuration);
				// this also sets it in the EventRecord instances themselves - reset it
				for (int i=0; i<e.size(); i++)
					r.get(i).setTime(origTimes[i]);
				randomizedEvents.add(r);
			}
			
			Collections.sort(randomizedEvents);
			
			events = randomizedEvents;
		}
		
		List<EQSIM_Event> matches = rupIden.getMatches(events);
		
		if (matches.isEmpty()) {
			System.out.println("No matches found!");
			return Lists.newArrayList();
		}
		
		double duration = windowDurationYears * General_EQSIM_Tools.SECONDS_PER_YEAR;
		
		double minDuration = minWindowDurationYears * General_EQSIM_Tools.SECONDS_PER_YEAR;
		
		// [start, end] in seconds
		timeWindows = Lists.newArrayList();
		
		double simEndTime = events.get(events.size()-1).getTime();
		
		double windowDurationSum = 0;
		int overlaps = 0;
		
		// find the time windows and total time covered (accounting for overlap)
		TimeWindow prev = null;
		matchIDs = new HashSet<Integer>();
		for (EQSIM_Event e : matches) {
			double start = e.getTime();
			double end = start + duration;
			start += minDuration; // do this afterward so that the end time doesn't get bumped back
			
			if (end > simEndTime)
				end = simEndTime;
			
			double noOverlapStart = start;
			if (prev != null && noOverlapStart < prev.getEnd()) {
				noOverlapStart = prev.getEnd();
				overlaps++;
			}
			double noOverlapDuration = end - noOverlapStart;
			Preconditions.checkState(noOverlapDuration >= 0);
			windowDurationSum += noOverlapDuration;
			
			matchIDs.add(e.getID());
			
			TimeWindow window = new TimeWindow(start, end, e.getID());
			timeWindows.add(window);
			prev = window;
		}
		
		totalWindowDurationYears = windowDurationSum / General_EQSIM_Tools.SECONDS_PER_YEAR;
		double rate = 1d / windowDurationYears;
		
		System.out.println("Got "+matches.size()+" matches in "+timeWindows.size()+" windows ("
				+overlaps+" overlaps).");
		System.out.println("Total window duration: "+windowDurationYears+" years");
		System.out.println("In-window event rate: "+rate);
		
		int windowIndex = 0;
		int numEventsInWindows = 0;
		mainloop:
		for (EQSIM_Event e : events) {
			double time = e.getTime();
			while (time > timeWindows.get(windowIndex).getEnd()) {
				// while this event happened after the current window ends
				// get the next window
				windowIndex++;
				if (windowIndex >= timeWindows.size())
					break mainloop;
			}
			TimeWindow matchingWindow = null;
			for (int i=windowIndex; i<timeWindows.size(); i++) {
				TimeWindow window = timeWindows.get(i);
				if (window.isContained(time) && !window.isInitiator(e.getID())) {
					matchingWindow = window;
					break;
				}
			}
			if (matchingWindow == null)
				continue;
			
			if (matchIDs.contains(e.getID())) {
				System.out.print("Matching event "+e.getID()
						+" made it in due to "+matchingWindow.getInitiatorID()+"'s window! ");
				double diff = e.getTime() - matchingWindow.getStart();
				double diffMins = diff / 60d;
				double diffHours = diffMins / 60d;
				double diffDays = diffHours / 24;
				double diffYears = diffDays / 365;
				if (Math.abs(diffYears) > 1)
					System.out.println("Diff time: "+(float)diffYears+" years");
				else if (Math.abs(diffDays) > 1)
					System.out.println("Diff time: "+(float)diffDays+" days");
				else if (Math.abs(diffHours) > 1)
					System.out.println("Diff time: "+(float)diffHours+" hours");
				else if (Math.abs(diffMins) > 1)
					System.out.println("Diff time: "+(float)diffMins+" mins");
				else
					System.out.println("Diff time: "+(float)diff+" secs");
			}
			
			numEventsInWindows++;
			eventsInWindows.add(e);
		}
		System.out.println("Found "+numEventsInWindows+" events in the given windows/mag range");
		
		return eventsInWindows;
	}

	public List<EQSIM_Event> getInputEvents() {
		return events;
	}

	public RuptureIdentifier getRupIden() {
		return rupIden;
	}

	public double getMinWindowDurationYears() {
		return minWindowDurationYears;
	}

	public double getWindowDurationYears() {
		return windowDurationYears;
	}

	public boolean isRandomizeEventTimes() {
		return randomizeEventTimes;
	}

	public List<EQSIM_Event> getEventsInWindows() {
		return eventsInWindows;
	}

	public List<TimeWindow> getTimeWindows() {
		return timeWindows;
	}

	public HashSet<Integer> getMatchIDs() {
		return matchIDs;
	}

	public double getTotalWindowDurationYears() {
		return totalWindowDurationYears;
	}

}