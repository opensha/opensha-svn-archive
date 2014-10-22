package scratch.kevin.simulators.catBuild;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.EventRecord;
import org.opensha.sha.simulators.iden.ElementMagRangeDescription;
import org.opensha.sha.simulators.iden.LogicalAndRupIden;
import org.opensha.sha.simulators.iden.LogicalOrRupIden;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.dists.ActualDistReturnPeriodProvider;
import scratch.kevin.simulators.dists.RandomDistType;
import scratch.kevin.simulators.dists.RandomReturnPeriodProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RandomCatalogBuilder {

	/**
		 * This returns a catalog containing all matches from the given events/rup idens but with events randomly distributed
		 * according to their recurrence intervals following a normal distribution. Events involving multiple rup idens are maintained
		 * by using the recurrence intervals of all events using that same set of rup idens.
		 * @param events
		 * @param rupIDens
		 * @param normDist use normal distribution instead of actual exact distribution
		 * @param splitMultis split coruptures into individual events. This maintains the RI distribution perfectly for each
		 * identifier but creates duplicate events. Magnitudes are maintained for each event so this would also throw off MFDs.
		 * Use this option carefully.
		 * @return
		 */
		public static List<EQSIM_Event> getRandomResampledCatalog(
				List<EQSIM_Event> events, List<? extends RuptureIdentifier> rupIdens,
				RandomDistType distType, boolean splitMultis) {
			return getRandomResampledCatalog(events, rupIdens, distType, splitMultis, 1);
		}
		public static List<EQSIM_Event> getRandomResampledCatalog(
				List<EQSIM_Event> events, List<? extends RuptureIdentifier> rupIdens,
				RandomDistType distType, boolean splitMultis, int lengthMult) {
			System.out.println("Generating randomized catalog. DistType: "+distType.getName());
			
			int numRupIdens = rupIdens.size();
			List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
			List<HashSet<EQSIM_Event>> matchesSets = Lists.newArrayList();
			HashSet<EQSIM_Event> allEventsSet = new HashSet<EQSIM_Event>();
			Map<RuptureIdentifier, Integer> idenIndexMap = Maps.newHashMap();
			
			for (int i=0; i<rupIdens.size(); i++) {
				RuptureIdentifier rupIden = rupIdens.get(i);
				List<EQSIM_Event> matches = rupIden.getMatches(events);
				matchesLists.add(matches);
				matchesSets.add(new HashSet<EQSIM_Event>(matches));
				allEventsSet.addAll(matches);
				idenIndexMap.put(rupIden, i);
			}
			
			System.out.println("All matches size: "+allEventsSet.size());
			
			// now remove events involving multiple rup idens
			List<HashSet<RuptureIdentifier>> multiSets = Lists.newArrayList();
			List<List<EQSIM_Event>> multiEvents = Lists.newArrayList();
			HashSet<EQSIM_Event> multiEventsSet = new HashSet<EQSIM_Event>();
			
			Map<RuptureIdentifier, List<Integer>> idenElemsListMap = null;
			
			int lastID = events.get(events.size()-1).getID();
			if (splitMultis) {
				idenElemsListMap = Maps.newHashMap();
				for (int i=0; i<rupIdens.size(); i++) {
					RuptureIdentifier iden = rupIdens.get(i);
					List<RuptureIdentifier> subIdens = Lists.newArrayList();
					if (iden instanceof LogicalOrRupIden)
						subIdens.addAll(((LogicalOrRupIden)iden).getSubIdens());
					else if (iden instanceof LogicalAndRupIden)
						subIdens.addAll(((LogicalAndRupIden)iden).getSubIdens());
					else
						subIdens.add(iden);
					List<Integer> elemIDs = Lists.newArrayList();
					for (RuptureIdentifier subIden : subIdens) {
						Preconditions.checkState(subIden instanceof ElementMagRangeDescription,
								"Can only split for element based rup idens.");
						ElementMagRangeDescription elemIden = (ElementMagRangeDescription)subIden;
						elemIDs.addAll(elemIden.getElementIDs());
					}
					idenElemsListMap.put(iden, elemIDs);
				}
			}
			
			for (EQSIM_Event e : allEventsSet) {
				HashSet<RuptureIdentifier> eventRupIdens = new HashSet<RuptureIdentifier>();
				for (int i=0; i<numRupIdens; i++)
					if (matchesSets.get(i).contains(e))
						eventRupIdens.add(rupIdens.get(i));
				
				if (eventRupIdens.size() > 1) {
					// we have multiple identifiers here
	//				if (splitMultis)
	//					throw new IllegalStateException("Doesn't currently work.");
					if (splitMultis) {
						for (RuptureIdentifier rupIden : eventRupIdens) {
							int idenIndex = idenIndexMap.get(rupIden);
							
							// remove duplicate event from this iden's list
							Preconditions.checkState(matchesLists.get(idenIndex).remove(e)); // assert removal correct
							
							List<Integer> elemIDsToRemove = Lists.newArrayList();
							
							// remove element ids
							for (RuptureIdentifier oIden : eventRupIdens) {
								// for each other rup identifier
								if (oIden == rupIden)
									continue;
								
								elemIDsToRemove.addAll(idenElemsListMap.get(oIden));
							}
							
							// now find the event record which is a match
							EQSIM_Event newEvent = null;
							for (EventRecord testRec : e) {
								EQSIM_Event testEvent = new EQSIM_Event(testRec);
								if (rupIden.isMatch(testEvent)) {
									newEvent = testEvent;
									break;
								}
							}
							Preconditions.checkNotNull(newEvent, "Splitting only works for idens for single faults");
							
							// now ensure that this new event isn't a match for any other Idens
							for (RuptureIdentifier oIden : eventRupIdens) {
								// for each other rup identifier
								if (oIden == rupIden)
									continue;
								
								Preconditions.checkState(!oIden.isMatch(newEvent),
										"Another identifier must be on the same fault section, can't split");
							}
							
							// ensure unique ID
							newEvent.setID(++lastID);
							
							matchesLists.get(idenIndex).add(newEvent);
						}
					} else {
						// look for a matching set already
						int match = -1;
						setLoop:
						for (int i=0; i<multiSets.size(); i++) {
							HashSet<RuptureIdentifier> set = multiSets.get(i);
							if (set.size() != eventRupIdens.size())
								continue;
							for (RuptureIdentifier rupIden : eventRupIdens)
								if (!set.contains(rupIden))
									continue setLoop;
							// if we're here then it's a match
							match = i;
							break;
						}
						if (match < 0) {
							multiSets.add(eventRupIdens);
							List<EQSIM_Event> eList = Lists.newArrayList();
							eList.add(e);
							multiEvents.add(eList);
						} else {
							multiEvents.get(match).add(e);
						}
						multiEventsSet.add(e);
					}
				}
			}
			
			System.out.println("Detected "+multiSets.size()+" combinations of multi-events!");
			
			// now build return periods
			List<List<EQSIM_Event>> eventListsToResample = Lists.newArrayList();
			List<RandomReturnPeriodProvider> randomRPsList = Lists.newArrayList();
			
			double totTime = General_EQSIM_Tools.getSimulationDurationYears(events);
			
			for (int i=0; i<rupIdens.size(); i++) {
				List<EQSIM_Event> eventsToResample = Lists.newArrayList(matchesLists.get(i));
				Collections.sort(eventsToResample);
				eventsToResample.removeAll(multiEventsSet);
				double[] rps = PeriodicityPlotter.getRPs(eventsToResample);
				if (lengthMult>1) {
					List<EQSIM_Event> origEventsToResample = eventsToResample;
					eventsToResample = Lists.newArrayList();
					for (int j=0; j<lengthMult; j++)
						eventsToResample.addAll(origEventsToResample);
				}
				eventListsToResample.add(eventsToResample);
				randomRPsList.add(RandomCatalogBuilder.getReturnPeriodProvider(rupIdens.get(i), distType, events, rps, totTime));
			}
			
			for (int i=0; i<multiEvents.size(); i++) {
				List<EQSIM_Event> eventsToResample = Lists.newArrayList(multiEvents.get(i));
				Collections.sort(eventsToResample);
				double[] rps = PeriodicityPlotter.getRPs(eventsToResample);
				if (lengthMult>1) {
					List<EQSIM_Event> origEventsToResample = eventsToResample;
					eventsToResample = Lists.newArrayList();
					for (int j=0; j<lengthMult; j++)
						eventsToResample.addAll(origEventsToResample);
				}
				eventListsToResample.add(eventsToResample);
				randomRPsList.add(RandomCatalogBuilder.getReturnPeriodProvider(null, distType, events, rps, totTime));
			}
			
			List<EQSIM_Event> newList = distType.getBuilder().buildCatalog(events, randomRPsList, eventListsToResample, lengthMult <= 1);
			
			System.out.println("Orig start="+events.get(0).getTimeInYears()+", end="+events.get(events.size()-1).getTimeInYears());
			System.out.println("Rand start="+newList.get(0).getTimeInYears()+", end="+newList.get(newList.size()-1).getTimeInYears());
			
			return newList;
		}

	public static RandomReturnPeriodProvider getReturnPeriodProvider(
			RuptureIdentifier rupIden, RandomDistType distType, List<EQSIM_Event> events, double[] rps, double totTime) {
		if (rps.length == 0) {
			rps = new double[1];
			rps[0] = totTime;
			return new ActualDistReturnPeriodProvider(rps);
		}
		return distType.instance(rupIden, rps, events);
	}

}
