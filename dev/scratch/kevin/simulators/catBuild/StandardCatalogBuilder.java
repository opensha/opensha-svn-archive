package scratch.kevin.simulators.catBuild;

import java.util.Collections;
import java.util.List;

import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;
import org.opensha.sha.simulators.eqsim_v04.iden.EventsInWindowsMatcher;

import scratch.kevin.simulators.dists.RandomReturnPeriodProvider;

import com.google.common.collect.Lists;

public class StandardCatalogBuilder implements CatalogBuilder {

	@Override
	public List<EQSIM_Event> buildCatalog(
			List<EQSIM_Event> events,
			List<RandomReturnPeriodProvider> randomRPsList,
			List<List<EQSIM_Event>> eventListsToResample) {
		
		int eventID = 0;
		
		List<EQSIM_Event> newList = Lists.newArrayList();
		for (int i=0; i<eventListsToResample.size(); i++) {
			RandomReturnPeriodProvider randomRP = randomRPsList.get(i);
			// start at a random interval through the first RP
			double time = Math.random() * randomRP.getReturnPeriod();
			for (EQSIM_Event e : eventListsToResample.get(i)) {
				double timeSecs = time * General_EQSIM_Tools.SECONDS_PER_YEAR;
				EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, timeSecs, eventID++);
				newList.add(newE);
				
				// move forward one RP
				time += randomRP.getReturnPeriod();
			}
		}
		
		// now sort to make it in order
		Collections.sort(newList);
		
		System.out.println("New matches size: "+newList.size());
		int origListSize = newList.size();
		
		double oldLastTime = events.get(events.size()-1).getTimeInYears()-events.get(0).getTimeInYears();
		
		int numRemoved = 0;
		for (int i=newList.size(); --i>=0;) {
			if (newList.get(i).getTimeInYears() > oldLastTime) {
				numRemoved++;
				newList.remove(i);
			}
		}
		
		System.out.println("Removed "+numRemoved+"/"+origListSize+" at tail of random catalog");
		return newList;
	}
	
}