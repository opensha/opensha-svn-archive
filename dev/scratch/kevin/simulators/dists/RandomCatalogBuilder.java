package scratch.kevin.simulators.dists;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.EventRecord;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;

import scratch.kevin.simulators.ElementMagRangeDescription;
import scratch.kevin.simulators.EventsInWindowsMatcher;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.RuptureIdentifier;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
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
				List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens,
				RandomDistType distType, boolean splitMultis) {
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
					Preconditions.checkState(iden instanceof ElementMagRangeDescription,
							"Can only split for element based rup idens.");
					ElementMagRangeDescription elemIden = (ElementMagRangeDescription)iden;
					List<Integer> elemIDs = elemIden.getElementIDs();
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
				eventListsToResample.add(eventsToResample);
				double[] rps = PeriodicityPlotter.getRPs(eventsToResample);
				randomRPsList.add(RandomCatalogBuilder.getReturnPeriodProvider(rupIdens.get(i), distType, events, rps, totTime));
			}
			
			for (int i=0; i<multiEvents.size(); i++) {
				List<EQSIM_Event> eventsToResample = Lists.newArrayList(multiEvents.get(i));
				Collections.sort(eventsToResample);
				eventListsToResample.add(eventsToResample);
				double[] rps = PeriodicityPlotter.getRPs(eventsToResample);
				randomRPsList.add(RandomCatalogBuilder.getReturnPeriodProvider(null, distType, events, rps, totTime));
			}
			
			List<EQSIM_Event> newList = distType.getBuilder().buildCatalog(events, randomRPsList, eventListsToResample);
			
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
	
	interface CatalogBuilder {
		public List<EQSIM_Event> buildCatalog(List<EQSIM_Event> events,
				List<RandomReturnPeriodProvider> randomRPsList,
				List<List<EQSIM_Event>> eventListsToResample);
	}
	
	public static class StandardCatalogBuilder implements CatalogBuilder {

		@Override
		public List<EQSIM_Event> buildCatalog(
				List<EQSIM_Event> events,
				List<RandomReturnPeriodProvider> randomRPsList,
				List<List<EQSIM_Event>> eventListsToResample) {
			List<EQSIM_Event> newList = Lists.newArrayList();
			for (int i=0; i<eventListsToResample.size(); i++) {
				RandomReturnPeriodProvider randomRP = randomRPsList.get(i);
				// start at a random interval through the first RP
				double time = Math.random() * randomRP.getReturnPeriod();
				for (EQSIM_Event e : eventListsToResample.get(i)) {
					double timeSecs = time * General_EQSIM_Tools.SECONDS_PER_YEAR;
					EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, timeSecs);
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
	
	public static class ProbabalisticCatalogBuilder implements CatalogBuilder {
		
		private StandardCatalogBuilder standardBuild = new StandardCatalogBuilder();
		private Random rand = new Random();
		
		private static final boolean D = true;

		@Override
		public List<EQSIM_Event> buildCatalog(List<EQSIM_Event> events,
				List<RandomReturnPeriodProvider> randomRPsList,
				List<List<EQSIM_Event>> eventListsToResample) {
			
			// separate standard from regular
			List<RandomReturnPeriodProvider> standardRPs = Lists.newArrayList();
			List<List<EQSIM_Event>> standardEventLists = Lists.newArrayList();
			List<ProbabilisticReturnPeriodProvider> probRPs = Lists.newArrayList();
			List<List<EQSIM_Event>> probEventLists = Lists.newArrayList();
			for (int i=0; i<randomRPsList.size(); i++) {
				RandomReturnPeriodProvider rp = randomRPsList.get(i);
				if (rp instanceof ProbabilisticReturnPeriodProvider) {
					probRPs.add((ProbabilisticReturnPeriodProvider)rp);
					List<EQSIM_Event> eventList = Lists.newArrayList(eventListsToResample.get(i));
					Collections.shuffle(eventList);
					probEventLists.add(eventList);
				} else {
					standardRPs.add(rp);
					standardEventLists.add(eventListsToResample.get(i));
				}
			}
			
			// first populate any standard ones
			List<EQSIM_Event> standardEvents;
			if (!standardRPs.isEmpty())
				standardEvents = standardBuild.buildCatalog(events, standardRPs, standardEventLists);
			else
				standardEvents = Lists.newArrayList();
			
			if (probRPs.isEmpty())
				return standardEvents;
			
			double startTime = 0;
			double maxTime = events.get(events.size()-1).getTimeInYears() - events.get(0).getTimeInYears();
			double timeDelta = probRPs.get(0).getPreferredWindowLength();
			// make sure all time deltas are the same (for now at least)
			// TODO allow variable deltas
			for (ProbabilisticReturnPeriodProvider probRP : probRPs)
				Preconditions.checkState((float)timeDelta == (float)probRP.getPreferredWindowLength());
			double halfDelta = timeDelta*0.5;
			
			int standardEventIndex = 0;
			List<EQSIM_Event> runningEvents = Lists.newArrayList();
			int probAdded = 0;
			
			int[] probEventIndexes = new int[probRPs.size()];
			
			// do a for loop to avoid propagating double precision errors
			int numSteps = (int)((maxTime - startTime)/timeDelta);
			System.out.println("Doing probabilisic build with "+numSteps+" steps and "+probRPs.size()+" providers!");
			System.out.println("Already have "+standardEvents.size()+" standard events");
			
			Stopwatch loopWatch = null;
			Stopwatch probWatch = null;
			if (D) {
				loopWatch = new Stopwatch();
				loopWatch.start();
				probWatch = new Stopwatch();
			}
			
//			int stepDiscr = 100;
//			double stepProbMult = 1d/(double)stepDiscr;
//			
//			double subStepDelta = timeDelta/(double)stepDiscr*0.5;
//			EvenlyDiscretizedFunc stepDiscrTimes = new EvenlyDiscretizedFunc(subStepDelta, timeDelta-subStepDelta, stepDiscr);
			
			for (int step=0; step<numSteps; step++) {
				double windowStart = startTime + timeDelta*step;
				double windowEnd = windowStart + timeDelta;
				
				if (D && step % 10000 == 0) {
					double time = startTime + halfDelta + timeDelta*step;
					long loopSecs = loopWatch.elapsed(TimeUnit.SECONDS);
					long probSecs = probWatch.elapsed(TimeUnit.SECONDS);
					double probFract = (double)probSecs/(double)loopSecs;
					double timeStepMillis = (double)loopWatch.elapsed(TimeUnit.MILLISECONDS)/(double)step;
					System.out.println("Step "+step+",\ttime="+(float)time+"\tevents="+runningEvents.size()
							+"\tprobEvents="+probAdded+"\tloopSecs="+loopSecs+",\tprobSecs="+probSecs
							+"\tprobFract="+(float)probFract+"\tstepMillis="+(float)timeStepMillis);
				}
					
				// populate any standard events before the current time
				for (int i=standardEventIndex; i<standardEvents.size(); i++) {
					EQSIM_Event e = standardEvents.get(i);
					if (e.getTimeInYears()<=windowEnd) {
//						runningEvents.add(e);
						EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, 0.5*(windowStart+windowEnd)*General_EQSIM_Tools.SECONDS_PER_YEAR);
						runningEvents.add(newE);
						standardEventIndex = i+1;
					} else {
						break;
					}
				}
				
				List<EQSIM_Event> eventsToAdd = Lists.newArrayList();

				// now do probabilistic ones
				for (int i=0; i<probRPs.size(); i++) {
					ProbabilisticReturnPeriodProvider probRP = probRPs.get(i);

					if (D) probWatch.start();
					PossibleRupture rup = probRP.getPossibleRupture(runningEvents, windowStart, windowEnd);
					if (D) probWatch.stop();
					if (rup == null)
						continue;
					double prob = rup.getProb();
					if (Double.isNaN(prob))
						continue;
					double r = rand.nextDouble();
					if (r<prob) {
						// add an event
						List<EQSIM_Event> myEvents = probEventLists.get(i);

						int ind = probEventIndexes[i]++;
						if (ind == myEvents.size()) {
							// roll back to start
							ind = 0;
							probEventIndexes[i] = 0;
						}

						EQSIM_Event e = myEvents.get(ind);
						double rupTime = rup.getEventTimeYears();
//						if (rupTime > windowEnd) {
//							// see if any standard events in this little delta
//							// TODO remove debug
//							for (int j=standardEventIndex; j<standardEvents.size(); j++) {
//								double newTime = standardEvents.get(j).getTimeInYears();
//								if (newTime<=rupTime) {
//									System.out.println("Would insert an extra before rup time!!! window: "
//											+(float)windowStart+"=>"+(float)windowEnd+", rupTime="+(float)rupTime
//											+", newTime="+(float)newTime);
//								} else {
//									break;
//								}
//							}
//						}
						double timeSecs = rupTime * General_EQSIM_Tools.SECONDS_PER_YEAR;
						EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, timeSecs);

						eventsToAdd.add(newE);
						probAdded++;
					}
				}
				
				Collections.sort(eventsToAdd);
				runningEvents.addAll(eventsToAdd);
			}
			
			for (int i=0; i<probRPs.size(); i++) {
				if (probRPs.get(i) instanceof FollowerReturnPeriodProvider) {
					System.out.println("\nFollower Stats:");
					((FollowerReturnPeriodProvider)probRPs.get(i)).printStats();
				}
			}
			
			Collections.sort(runningEvents);
			
			return runningEvents;
		}
		
	}

}
