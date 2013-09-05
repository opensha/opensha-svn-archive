package scratch.kevin.simulators.catBuild;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.function.HistogramFunction;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotElement;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSpec;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.simulators.eqsim_v04.EQSIM_Event;
import org.opensha.sha.simulators.eqsim_v04.General_EQSIM_Tools;
import org.opensha.sha.simulators.eqsim_v04.iden.ElementMagRangeDescription;
import org.opensha.sha.simulators.eqsim_v04.iden.EventsInWindowsMatcher;
import org.opensha.sha.simulators.eqsim_v04.iden.RuptureIdentifier;

import scratch.UCERF3.utils.IDPairing;
import scratch.kevin.magDepth.NoCollissionFunc;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.catBuild.SparseNDimensionalHashDataset.IndicesKey;
import scratch.kevin.simulators.catBuild.StateBasedCatalogBuilder.PossibleStates;
import scratch.kevin.simulators.dists.RandomDistType;
import scratch.kevin.simulators.dists.RandomReturnPeriodProvider;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

public class StateBasedCatalogBuilder implements CatalogBuilder {
	
	private SparseNDimensionalHashDataset<Double> totalStatesDataset;
	private SparseNDimensionalHashDataset<StateBasedCatalogBuilder.PossibleStates> stateTransitionDataset;
	private double distSpacing;

	static class PossibleStates {
		List<int[]> states = Lists.newArrayList();
		private Map<IndicesKey, Integer> stateIndexMap = Maps.newHashMap();
		private List<Double> frequencies = Lists.newArrayList();
		private double tot;
		
		private int[] fromIndices;
		
		public PossibleStates(int[] fromIndices) {
			this.fromIndices = fromIndices;
			this.tot = 0d;
		}
		
		public void add(int[] state, double frequency) {
			IndicesKey key = new IndicesKey(state);
			Integer index = stateIndexMap.get(key);
			if (index == null) {
				stateIndexMap.put(key, states.size());
				states.add(state);
				frequencies.add(frequency);
			} else {
				frequencies.set(index, frequencies.get(index)+frequency);
			}
			tot += frequency;
		}
		
		public double getFrequency(int[] indices) {
			Integer index = stateIndexMap.get(new IndicesKey(indices));
			if (index == null)
				return 0d;
			return frequencies.get(index);
		}
		
		public int[] drawState() {
			double rand = Math.random()*tot;
			double runningTot = 0d;
			
			for (int i=0; i<states.size(); i++) {
				runningTot += frequencies.get(i);
				if (rand <= runningTot)
					return states.get(i);
			}
			throw new IllegalStateException("Frequencies don't add up...");
		}
	}

	@Override
	public List<EQSIM_Event> buildCatalog(List<EQSIM_Event> events,
			List<RandomReturnPeriodProvider> randomRPsList,
			List<List<EQSIM_Event>> matchesLists) {
		
		// this is the randomized sequence of events for each fault from which to sample
		List<List<EQSIM_Event>> eventsToReuse = Lists.newArrayList();
		int[] eventsToReuseIndexes = new int[matchesLists.size()];
		for (int i=0; i<matchesLists.size(); i++) {
			List<EQSIM_Event> matches = matchesLists.get(i);
			List<EQSIM_Event> rand = Lists.newArrayList(matches);
			Collections.shuffle(rand);
			eventsToReuse.add(rand);
			eventsToReuseIndexes[i] = 0;
		}
		
		int nDims = matchesLists.size();
		
		distSpacing = 10d;
		double minVal = 0.5d*distSpacing;
		
		totalStatesDataset = new SparseNDimensionalHashDataset<Double>(nDims, minVal, distSpacing);
		stateTransitionDataset = new SparseNDimensionalHashDataset<StateBasedCatalogBuilder.PossibleStates>(nDims, minVal, distSpacing);
		
		double maxTime = events.get(events.size()-1).getTimeInYears();
		double startTime = events.get(0).getTimeInYears();
		int numSteps = (int)((maxTime - startTime)/distSpacing);
		
		int[] lastMatchIndexBeforeWindowEnd = new int[nDims];
		for (int i=0; i<nDims; i++)
			lastMatchIndexBeforeWindowEnd[i] = -1;
		
		int[] prevState = null;
		
		StateBasedCatalogBuilder.PossibleStates possibleInitialStates = new StateBasedCatalogBuilder.PossibleStates(null);
		
		System.out.println("Assembling state transition probabilities");
		
		int skippedSteps = 0;
		
		int startStep = 0;
		
		double startWindowStart = startTime + distSpacing*startStep;
		for (int n=0; n<nDims && startStep>0; n++) {
			List<EQSIM_Event> myMatches = matchesLists.get(n);
			for (int i=lastMatchIndexBeforeWindowEnd[n]+1; i<myMatches.size(); i++) {
				double time = myMatches.get(i).getTimeInYears();
				if (time > startWindowStart)
					break;
				lastMatchIndexBeforeWindowEnd[n] = i;
			}
		}
		
		stepLoop:
		for (int step=startStep; step<numSteps; step++) {
			double windowStart = startTime + distSpacing*step;
			double windowEnd = windowStart + distSpacing;
			
			for (int n=0; n<nDims; n++) {
				List<EQSIM_Event> myMatches = matchesLists.get(n);
				for (int i=lastMatchIndexBeforeWindowEnd[n]+1; i<myMatches.size(); i++) {
					double time = myMatches.get(i).getTimeInYears();
					Preconditions.checkState(time >= windowStart);
					if (time > windowEnd)
						break;
					lastMatchIndexBeforeWindowEnd[n] = i;
				}
			}
			
			int[] curState = new int[nDims];
			
			for (int n=0; n<nDims; n++) {
				List<EQSIM_Event> myMatches = matchesLists.get(n);
				
				double prevEvent;
				if (lastMatchIndexBeforeWindowEnd[n] >= 0) {
					prevEvent = myMatches.get(lastMatchIndexBeforeWindowEnd[n]).getTimeInYears();
				} else {
					// skip places at start where state not defined
					skippedSteps++;
					Preconditions.checkState(prevState == null);
					continue stepLoop;
				}
				
				double myDelta = windowEnd - prevEvent;
				curState[n] = totalStatesDataset.indexForDimVal(n, myDelta);
			}
			
			// register current state
			Double stateCount = totalStatesDataset.get(curState);
			if (stateCount == null)
				stateCount = 0d;
			stateCount += 1d;
			totalStatesDataset.set(curState, stateCount);
			
			// register this state as a transition from the previous state
			if (prevState != null) {
				StateBasedCatalogBuilder.PossibleStates possibilities = stateTransitionDataset.get(prevState);
				if (possibilities == null) {
					possibilities = new StateBasedCatalogBuilder.PossibleStates(prevState);
					stateTransitionDataset.set(prevState, possibilities);
				}
				possibilities.add(curState, 1d);
				possibleInitialStates.add(curState, 1d);
			}
			
			prevState = curState;
		}
		
		// now pick random initial state from the distribution of total states
		prevState = possibleInitialStates.drawState();
		
		// shift so that rupTime falls in the middle of windows
		startTime += distSpacing*0.5d;
		
		List<EQSIM_Event> randomizedEvents = Lists.newArrayList();
		
		System.out.println("Assembling random catalog ("+skippedSteps+" skipped steps)");
		
		int[] counts = new int[nDims];
		
		int eventID = 0;
		int numBailouts = 0;
		
		List<int[]> statesTracker = Lists.newArrayList();
		statesTracker.add(prevState);
		
		for (int step=0; step<numSteps; step++) {
			// choose current state randomly from previous state's transition states
			StateBasedCatalogBuilder.PossibleStates possibilities = stateTransitionDataset.get(prevState);
			
			if (possibilities == null) {
				// this means we found the last state in the system, and it was only reached
				// in that last state
				System.out.println("Reached orig last state in system, no transitions! (step="+step+")");
				// find all theoretical neighbors
				numBailouts++;
				possibilities = StateBasedCatalogBuilder.findPossibleBailoutStates(prevState, totalStatesDataset);
			}
			
			int[] curState = null;
			if (possibilities.tot == 0) {
				System.out.println("Possibilities are empty, backing up!");
				// go back up and get out of this path
				
				for (int redoStep=step-1; redoStep>=0; redoStep--) {
					int[] redoPrevIndices = statesTracker.get(redoStep);
					possibilities = stateTransitionDataset.get(redoPrevIndices);
					if (possibilities.states.size() > 1) {
						// this means there was another option
						int[] newDestState = possibilities.drawState();
						while (!Arrays.equals(newDestState, statesTracker.get(redoStep+1)))
							newDestState = possibilities.drawState();
						// now make sure
						
						System.out.println("Backed up to step "+redoStep+" ("+(step-redoStep)+" steps). New State: "
								+Joiner.on(",").join(Ints.asList(newDestState)));
						
						boolean newStateStuck = false;
						// now make sure this state doesn't lead to it as well!
						int[] prevTestState = newDestState;
						for (int i=0; i<2*(step-redoStep); i++) {
							PossibleStates testPossibilities = stateTransitionDataset.get(prevTestState);
							if (testPossibilities == null || testPossibilities.tot == 0) {
								newStateStuck = true;
								break;
							}
							prevTestState = testPossibilities.drawState();
						}
						
						if (newStateStuck) {
							System.out.println("Nevermind, this one can get stuck as well. Backing up more.");
						} else {
							prevState = redoPrevIndices;
							curState = newDestState;
							
							// remove any added events
							double windowStart = startTime + distSpacing*(redoStep);
							for (int i=randomizedEvents.size(); --i>=0;) {
								EQSIM_Event e = randomizedEvents.get(i);
								if (e.getTimeInYears() > windowStart)
									randomizedEvents.remove(i);
								else
									break;
							}
							step = redoStep;
							break;
						}
					}
				}
				Preconditions.checkNotNull(curState);
			} else {
				curState = possibilities.drawState();
			}
			
			double rupTimeYears = startTime + distSpacing*step;
			double rupTimeSecs = rupTimeYears * General_EQSIM_Tools.SECONDS_PER_YEAR;
			
			// now look for any hits
			for (int n=0; n<curState.length; n++) {
				if (curState[n] == 0) {
					// state was reset to zero for this fault, this means a rupture happens in this window
					List<EQSIM_Event> myEvents = eventsToReuse.get(n);
					if (eventsToReuseIndexes[n] == myEvents.size())
						eventsToReuseIndexes[n] = 0;
					
					EQSIM_Event e = myEvents.get(eventsToReuseIndexes[n]++);
					EQSIM_Event newE = EventsInWindowsMatcher.cloneNewTime(e, rupTimeSecs, eventID++);
					randomizedEvents.add(newE);
					
					counts[n]++;
				}
			}
			
			statesTracker.add(curState);
			prevState = curState;
		}
		
		System.out.println("Needed "+numBailouts+" bailouts.");
		for (int n=0; n<nDims; n++)
			System.out.println("iden "+n+": rand="+counts[n]+"\torig="+matchesLists.get(n).size());
		
		return randomizedEvents;
	}

	static void findPossibleStates(int[] curState, int index,
			SparseNDimensionalHashDataset<Double> totalStatesDataset, List<int[]> runningPossibleStates) {
		int[] stateNoRup = Arrays.copyOf(curState, curState.length);
		int[] stateWithRup = Arrays.copyOf(curState, curState.length);
		
		stateNoRup[index]++;
		stateWithRup[index] = 0;
		
		if (index == curState.length-1) {
			runningPossibleStates.add(stateWithRup);
			runningPossibleStates.add(stateNoRup);
		} else {
			findPossibleStates(stateNoRup, index+1, totalStatesDataset, runningPossibleStates);
			findPossibleStates(stateWithRup, index+1, totalStatesDataset, runningPossibleStates);
		}
	}

	static StateBasedCatalogBuilder.PossibleStates findPossibleBailoutStates(int[] fromState,
			SparseNDimensionalHashDataset<Double> totalStatesDataset) {
		List<int[]> possibilities = Lists.newArrayList();
		
		StateBasedCatalogBuilder.findPossibleStates(fromState, 0, totalStatesDataset, possibilities);
		
		StateBasedCatalogBuilder.PossibleStates states = new StateBasedCatalogBuilder.PossibleStates(fromState);
		
		System.out.println("Stuck at a dead end! FromState="+Joiner.on(",").join(Ints.asList(fromState)));
		
		for (int[] state : possibilities) {
			Double val = totalStatesDataset.get(state);
//			System.out.println("Possible state:\t"+Joiner.on(",").join(Ints.asList(state))+"\tval="+val);
			if (val != null)
				states.add(state, val);
		}
		
		System.out.println("Found "+states.states.size()+"/"+possibilities.size()+" bailout states");
		
		return states;
	}
	
	private List<File> write2DDists(File writeDir, int index1, String name1, List<EQSIM_Event> matches1,
			int index2, String name2, List<EQSIM_Event> matches2) throws IOException {
		String probFName = "prob_dists_"+PeriodicityPlotter.getFileSafeString(name1)+"_"+PeriodicityPlotter.getFileSafeString(name2);
		File probFile = new File(writeDir, probFName+".pdf");
		String synchFName = "synch_dists_"+PeriodicityPlotter.getFileSafeString(name1)+"_"+PeriodicityPlotter.getFileSafeString(name2);
		File synchFile = new File(writeDir, synchFName+".pdf");
		
		// now we need distributions just between the two
		double maxTimeDiff = 1000d;
		double distDeltaYears = 10d;
		int num = (int)(maxTimeDiff/distDeltaYears - 1);
		double min = 0.5*distDeltaYears;
		
		EvenlyDiscrXYZ_DataSet p_1_n2 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet p_n1_2 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet p_1_2 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet p_n1_n2 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet synchData = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet synchNormData = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet synchOverIndepData1 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		EvenlyDiscrXYZ_DataSet synchOverIndepData2 = new EvenlyDiscrXYZ_DataSet(num, num, min, min, distDeltaYears);
		
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		matchesLists.add(matches1);
		matchesLists.add(matches2);
		
		double[] freq1s = new double[num];
		double[] freq2s = new double[num];
		double[] tot1s = new double[num];
		double[] tot2s = new double[num];
		
		
		
		double totStateCount = 0;
		for (int[] indices : totalStatesDataset.getPopulatedIndices()) {
			PossibleStates possible = stateTransitionDataset.get(indices);
			double freq1 = 0;
			double freq2 = 0;
			for (int[] state : possible.states) {
				double freq = possible.getFrequency(state);
				if (state[0] == 0)
					freq1 += freq;
				if (state[1] == 0)
					freq2 += freq;
			}
			if (indices[0] < num) {
				freq1s[indices[0]] += freq1;
				tot1s[indices[0]] += possible.tot;
			}
			if (indices[1] < num) {
				freq2s[indices[1]] += freq2;
				tot2s[indices[1]] += possible.tot;
			}
			totStateCount += totalStatesDataset.get(indices);
		}
		
		// probabilities in each state independent of other fault
		double[] indepProb1s = new double[num];
		double[] indepProb2s = new double[num];
		
		for (int i=0; i<num; i++) {
			indepProb1s[i] = freq1s[i] / tot1s[i];
			indepProb2s[i] = freq2s[i] / tot2s[i];
		}
		
		for (int x=0; x<num; x++) {
			for (int y=0; y<num; y++) {
				int[] indices = { x, y };
				
				Double tot = totalStatesDataset.get(indices);
				if (tot == null)
					continue;
				
				PossibleStates possible = stateTransitionDataset.get(indices);
				
				int[] ind_1_n2 = { 0, y+1 };
				int[] ind_n1_2 = { x+1, 0 };
				int[] ind_1_2 = { 0, 0 };
				int[] ind_n1_n2 = { x+1, y+1 };
				
				double prob_1_n2 = possible.getFrequency(ind_1_n2)/tot;
				double prob_n1_2 = possible.getFrequency(ind_n1_2)/tot;
				double prob_1_2 = possible.getFrequency(ind_1_2)/tot;
				double prob_n1_n2 = possible.getFrequency(ind_n1_n2)/tot;
				
				p_1_n2.set(x, y, prob_1_n2);
				p_n1_2.set(x, y, prob_n1_2);
				p_1_2.set(x, y, prob_1_2);
				p_n1_n2.set(x, y, prob_n1_n2);
				
//				double prob_1 = prob1s[x];
//				double prob_2 = prob2s[y];
				
				double prob_1 = prob_1_n2 + prob_1_2;
				double prob_2 = prob_n1_2 + prob_1_2;
				
				double synch = prob_1_2/(prob_1*prob_2);
				if (Double.isInfinite(synch))
					synch = Double.NaN;
				
				double prob_state = tot / totStateCount;
				
				double synchTimesProb = synch*prob_state;
//				Preconditions.checkState(Double.isNaN(synch) || (synch >= 0 && synch <= 1),
//						"Synch param is bad: "+prob_1_2+"/("+prob_1+"*"+prob_2+") = "+synch+". synch*P(Sk)="+synchTimesProb);
				
				double synch_norm = synch*prob_state;
				
				synchData.set(x, y, synch);
				synchNormData.set(x, y, synch_norm);
				synchOverIndepData1.set(x, y, prob_1/indepProb1s[x]);
				synchOverIndepData2.set(x, y, prob_2/indepProb2s[y]);
			}
		}
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		boolean synch_log = true;
		CPT synch_cpt;
		CPT synch_norm_cpt;
		if (synch_log) {
			synchData.log10();
			synchNormData.log10();
			synch_cpt = cpt.rescale(-synchData.getMaxZ(), synchData.getMaxZ());
			synch_norm_cpt = cpt.rescale(-5, synchNormData.getMaxZ());
		} else {
			synch_cpt = cpt.rescale(0d, synchData.getMaxZ());
			synch_norm_cpt = cpt.rescale(0d, synchNormData.getMaxZ());
		}
		CPT logRatioCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-2d, 2d);
		synchOverIndepData1.log10();
		synchOverIndepData2.log10();
		
		System.out.println(synchData.getMaxZ());
		System.out.println(synchNormData.getMaxZ());
		
		String title = name1+" vs "+name2;
		String xAxisLabel = "Years since prev "+name1;
		String yAxisLabel = "Years since prev "+name2;
		
		List<Range> xRanges = Lists.newArrayList(new Range(0d, maxTimeDiff));
		Range yRange = new Range(0d, 0.6d*maxTimeDiff);
		List<Range> yRanges = Lists.newArrayList(yRange, yRange, yRange, yRange);
		
		List<XYZPlotSpec> specs = Lists.newArrayList();
		specs.add(new XYZPlotSpec(p_1_n2, cpt, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(p_n1_2, cpt, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(p_1_2, cpt, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(p_n1_n2, cpt, title, xAxisLabel, yAxisLabel, null));
		
		XYZGraphPanel gp = new XYZGraphPanel();
		gp.drawPlot(specs, false, false, xRanges, yRanges, null);
		gp.getChartPanel().setSize(1000, 2500);
		gp.saveAsPDF(probFile.getAbsolutePath());
		
		specs = Lists.newArrayList();
		title = "Synchronization: "+title;
		
//		xRanges = Lists.newArrayList(new Range(0d, 0.75*maxTimeDiff));
//		yRange = new Range(0d, maxTimeDiff);
//		yRanges = Lists.newArrayList(yRange, yRange);
		
		specs.add(new XYZPlotSpec(synchData, synch_cpt, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(synchNormData, synch_norm_cpt, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(synchOverIndepData1, logRatioCPT, title, xAxisLabel, yAxisLabel, null));
		specs.add(new XYZPlotSpec(synchOverIndepData2, logRatioCPT, title, xAxisLabel, yAxisLabel, null));
		
		gp = new XYZGraphPanel();
		gp.drawPlot(specs, false, false, xRanges, yRanges, null);
		gp.getChartPanel().setSize(1000, 2500);
		gp.saveAsPDF(synchFile.getAbsolutePath());
		
		return Lists.newArrayList(probFile, synchFile);
	}
	
	private double findGoodLogMin(EvenlyDiscrXYZ_DataSet logData, double absMin) {
		double minAbove = Double.POSITIVE_INFINITY;
		for (int i=0; i<logData.size(); i++) {
			double z = logData.get(i);
			if (z >= absMin && z < minAbove)
				minAbove = z;
		}
		return minAbove;
	}
	
	private void writeTransitionStats(File writeDir) {
//		NoCollissionFunc noRupProbs = new NoCollissionFunc();
//		NoCollissionFunc rupProbs = new NoCollissionFunc();
		HistogramFunction noRupProbs = new HistogramFunction(0.025, 20, 0.05d);
		HistogramFunction rupProbs = new HistogramFunction(0.025, 20, 0.05d);

		HistogramFunction allCounts = new HistogramFunction(0d, 10, 1d);
		HistogramFunction noRupCounts = new HistogramFunction(0d, 10, 1d);
		HistogramFunction rupCounts = new HistogramFunction(0d, 10, 1d);
		
		int numStates = 0;
		int numStatesWithRup = 0;
		int numStatesWithMulti = 0;
		
		for (int[] indices : stateTransitionDataset.getPopulatedIndices()) {
			PossibleStates states = stateTransitionDataset.get(indices);
			
			int noRupCount = 0;
			int rupCount = 0;
			
			for (int i=0; i<states.states.size(); i++) {
				int[] newState = states.states.get(i);
				double freq = states.frequencies.get(i);
				double prob = freq/states.tot;
				
				if (Ints.contains(newState, 0)) {
					rupProbs.add(prob, 1d);
					rupCount++;
				} else {
					noRupProbs.add(prob, 1d);
					noRupCount++;
				}
			}
			
			numStates++;
			if (rupCount > 0)
				numStatesWithRup++;
			if ((rupCount + noRupCount) > 1)
				numStatesWithMulti++;
			
			if (noRupCount < noRupCounts.getNum())
				noRupCounts.add(noRupCount, 1d);
			if (rupCount < rupCounts.getNum())
				rupCounts.add(rupCount, 1d);
			allCounts.add(rupCount+noRupCount, 1d);
		}
		
		float percentMulti = 100f*(float)numStatesWithMulti/(float)numStates;
		float percentRupMulti = 100f*(float)numStatesWithMulti/(float)numStatesWithRup;
		
		System.out.println("States with multiple transitions: "
				+numStatesWithMulti+"/"+numStates+" ("+percentMulti+" %)");
		System.out.println("Rupture trans states with multiple transitions: "
				+numStatesWithMulti+"/"+numStatesWithRup+" ("+percentRupMulti+" %)");

		List<PlotCurveCharacterstics> chars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, Color.BLACK));
		new GraphWindow(asList(allCounts), "# Total Transitions Per State", chars);
		new GraphWindow(asList(rupCounts), "# Rupture Transitions Per State", chars);
		new GraphWindow(asList(noRupCounts), "# No Rupture Transitions Per State", chars);
		new GraphWindow(asList(rupProbs), "Rupture Transition Probailities", chars);
		new GraphWindow(asList(noRupProbs), "No Rupture Transition Probailities", chars);
	}
	
	private static List<PlotElement> asList(PlotElement... elems) {
		return Lists.newArrayList(elems);
	}
	
	private void writeSynchParamsTable(File file, List<RuptureIdentifier> idens) throws IOException {
		int nDims = totalStatesDataset.getNDims();
		
		int lagMax = 20;
		int lags = lagMax*2+1;
		
		double[][][] params = new double[nDims][nDims][lags];
		
//		double totStateCount = 0;
//		for (int[] indices : totalStatesDataset.getPopulatedIndices())
//			totStateCount += totalStatesDataset.get(indices);
		
		List<PlotSpec> lagSpecs = Lists.newArrayList();
		List<PlotCurveCharacterstics> lagChars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		String lagTitle = "Synchronization Lag Functions";
		String lagXAxisLabel = "Lag (years)";
		String lagYAxisLabel = "Ln(Synchronization)";

		double lagFuncMin = -lagMax*distSpacing;
		
		Range lagXRange = new Range(lagFuncMin, -lagFuncMin);
		Range lagYRange = new Range(-2, 2);
		
		List<File> synch2DPDFs = Lists.newArrayList();
		File synchXYZDir = new File(file.getParentFile(), "synch_xyz_param_plots");
		if (!synchXYZDir.exists())
			synchXYZDir.mkdir();
		
		List<File> synchScatterPDFs = Lists.newArrayList();
		File synchScatterDir = new File(file.getParentFile(), "synch_scatter_plots");
		if (!synchScatterDir.exists())
			synchScatterDir.mkdir();
		
		for (int m=0; m<nDims; m++) {
			// TODO start at m+1?
			for (int n=m; n<nDims; n++) {
				// first bin by only the indices we care about
				Map<IndicesKey, List<int[]>> binnedIndices = Maps.newHashMap();
				for (int[] indices : totalStatesDataset.getPopulatedIndices()) {
					int[] myInd = { indices[m], indices[n] };
					IndicesKey key = new IndicesKey(myInd);
					List<int[]> binned = binnedIndices.get(key);
					if (binned == null) {
						binned = Lists.newArrayList();
						binnedIndices.put(key, binned);
					}
					binned.add(indices);
				}
				
				String name1 = idens.get(m).getName();
				String name2 = idens.get(n).getName();
				
				int lagIndex = 0;
//				EvenlyDiscretizedFunc synchLagFunc = new EvenlyDiscretizedFunc((double)-lagMax, lags, 1d);
				EvenlyDiscretizedFunc synchLagFunc = new EvenlyDiscretizedFunc(lagFuncMin, lags, distSpacing);
				
				for (int lag=-lagMax; lag<=lagMax; lag++) {
					int numSums = 0;
					int numPossibleSums = 0;
					int numSubSums = 0;
					int numSubBails = 0;
					
					List<Double> synchs = Lists.newArrayList();
					List<Double> probMs = Lists.newArrayList();
					List<Double> probNs = Lists.newArrayList();
					List<Double> probMNs = Lists.newArrayList();
					List<Double> freqEithers = Lists.newArrayList();
					List<IndicesKey> synch_indices = Lists.newArrayList();
					
					double totEithers = 0d;
					
//					for (int[] indices : totalStatesDataset.getPopulatedIndices()) {
//					for (List<int[]> indicesList : binnedIndices.values()) {
					for (IndicesKey indicesKey : binnedIndices.keySet()) {
						List<int[]> indicesList = binnedIndices.get(indicesKey);
						double freqM = 0;
						double freqN = 0;
						double freqMN = 0;
						double freqEither = 0;
						double tot = 0;
						for (int[] indices : indicesList) {
							PossibleStates possible = stateTransitionDataset.get(indices);
							if (possible == null || possible.states == null || possible.tot == 0) {
								// last state in the catalog can be a dead end if never reached earlier
								numSubBails++;
								continue;
							}
							
							for (int[] state : possible.states) {
								// incorporates lag
								int mCheckIndex, nCheckIndex;
								if (lag == 0) {
									mCheckIndex = 0;
									nCheckIndex = 0;
								} else if (lag < 0) {
									// n precedes m
									// we want m=0, and n=abs(lag)
									mCheckIndex = 0;
									nCheckIndex = -lag; 
								} else {
									// lag > 0
									// m precedes n
									// we want n=0, and m=lag
									nCheckIndex = 0;
									mCheckIndex = lag; 
								}
								double freq = possible.getFrequency(state);
								if (state[m] == mCheckIndex)
									freqM += freq;
								if (state[n] == nCheckIndex)
									freqN += freq;
								if (state[m] == mCheckIndex && state[n] == nCheckIndex)
									freqMN += freq;
								if (state[m] == mCheckIndex || state[n] == nCheckIndex)
									freqEither += freq;
							}
							
							numSubSums++;
							
							tot += possible.tot;
						}
						
						// convert to probs
						freqM /= tot;
						freqN /= tot;
						freqMN /= tot;
						
						double synch = freqMN/(freqM*freqN);
//						double prob_state = tot / totStateCount;
						
						numPossibleSums++;
						
//						if (!Double.isInfinite(synch) && !Double.isNaN(synch)) {
//						if (freqEither > 0) {
						if (freqM > 0 && freqN > 0) {
							if (Double.isInfinite(synch) || Double.isNaN(synch))
								synch = 0d;
							totEithers += freqEither;
							numSums++;
							
							synchs.add(synch);
							freqEithers.add(freqEither);
							synch_indices.add(indicesKey);
							
							probMs.add(freqM);
							probNs.add(freqN);
							probMNs.add(freqMN);
						}
					}
					double numerator = 0d;
					double denominator = 0d;
					for (int i=0; i<synchs.size(); i++) {
						double weight = freqEithers.get(i)/totEithers;
						numerator += synchs.get(i)*weight;
						denominator += weight;
					}
					double rBar = numerator/denominator;
					params[m][n][lagIndex] = rBar;
					params[n][m][lagIndex] = rBar;
					
					synchLagFunc.set(lagIndex, rBar);
					lagIndex++;
					
					if (lag != 0)
						continue;
					
					System.out.println(name1+" vs "+name2);
					System.out.println(numerator+"/"+denominator+" = "+params[n][m][lagIndex-1]);
					System.out.println("Sums: "+numSums+"/"+numPossibleSums
							+" ("+numSubSums+" sub, "+numSubBails+" bails)");
					
					if (m == n)
						continue;
					
					DefaultXY_DataSet synchFunc = new DefaultXY_DataSet();
					DefaultXY_DataSet contribFunc = new DefaultXY_DataSet();
					
					double highestContrib = Double.NEGATIVE_INFINITY;
					int contribIndex = -1;
					
					EvenlyDiscrXYZ_DataSet freqXYZ = new EvenlyDiscrXYZ_DataSet(
							100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
					
					EvenlyDiscrXYZ_DataSet rXYZ = new EvenlyDiscrXYZ_DataSet(
							100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
					
					EvenlyDiscrXYZ_DataSet weightedR_XYZ = new EvenlyDiscrXYZ_DataSet(
							100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
					
					for (int x=0; x<rXYZ.getNumX(); x++) {
						for (int y=0; y<rXYZ.getNumY(); y++) {
							freqXYZ.set(x, y, Double.NaN);
							rXYZ.set(x, y, Double.NaN);
							weightedR_XYZ.set(x, y, Double.NaN);
						}
					}
					
					for (int i=0; i<synchs.size(); i++) {
						double synch = synchs.get(i);
						double weight = freqEithers.get(i)/totEithers;
						
						double rBar_without = (numerator - synch*weight)/(denominator - weight);
						double contrib = rBar - rBar_without;
						
						if (contrib > highestContrib) {
							highestContrib = contrib;
							contribIndex = i;
						}
						
						int[] indices = synch_indices.get(i).getIndices();
						if (indices[0] < freqXYZ.getNumX() && indices[1] < freqXYZ.getNumY()) {
							freqXYZ.set(indices[0], indices[1], freqEithers.get(i));
							rXYZ.set(indices[0], indices[1], synch);
							if (synch == 0)
								weightedR_XYZ.set(indices[0], indices[1], 1e-14);
							else
								weightedR_XYZ.set(indices[0], indices[1], synch*weight);
						}
						
						if (synch == 0)
							synch = 1e-14;
						synchFunc.set(synch, weight);
						if (contrib < 1e-14 || weight < 1e-14)
							continue;
						contribFunc.set(synch, contrib);
					}
					
					CPT freqCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0, freqXYZ.getMaxZ());
					freqCPT.setNanColor(Color.WHITE);
					CPT rCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-1, 1);
					rXYZ.log10();
					rCPT.setNanColor(Color.WHITE);
					CPT weightedCPT = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(-3, -1);
					weightedR_XYZ.log10();
					weightedCPT.setNanColor(Color.WHITE);
					String title = name1+" vs "+name2;
					String xAxisLabel = "Years since prev "+name1;
					String yAxisLabel = "Years since prev "+name2;
					XYZPlotSpec freqSpec = new XYZPlotSpec(freqXYZ, freqCPT, title, xAxisLabel, yAxisLabel, null);
//					title = "r: freqMN/(freqM*freqN)";
					XYZPlotSpec rSpec = new XYZPlotSpec(rXYZ, rCPT, title, xAxisLabel, yAxisLabel, null);
					XYZPlotSpec rWeightedSpec = new XYZPlotSpec(weightedR_XYZ, weightedCPT, title, xAxisLabel, yAxisLabel, null);
					
					Range xyzXRange = new Range(0d, rXYZ.getMaxX()+0.5*distSpacing);
					Range xyzYRange = new Range(0d, 0.6*rXYZ.getMaxX()+0.5*distSpacing);
					List<Range> xyzXRanges = Lists.newArrayList(xyzXRange);
					List<Range> xyzYRanges = Lists.newArrayList(xyzYRange, xyzYRange, xyzYRange);
					
					double annX = xyzXRange.getUpperBound()*0.95;
					double annY = xyzYRange.getUpperBound()*0.9;
					Font font = new Font(Font.SERIF, Font.PLAIN, 18);
					XYTextAnnotation freqAnn = new XYTextAnnotation("Freq A OR B", annX, annY);
					freqAnn.setFont(font);
					freqAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
					freqSpec.setPlotAnnotations(Lists.newArrayList(freqAnn));
					XYTextAnnotation rAnn = new XYTextAnnotation("Log10(r=freqMN/(freqM*freqN))", annX, annY);
					rAnn.setFont(font);
					rAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
					rSpec.setPlotAnnotations(Lists.newArrayList(rAnn));
					XYTextAnnotation weightedAnn = new XYTextAnnotation("Log10(r*weight)", annX, annY);
					weightedAnn.setFont(font);
					weightedAnn.setTextAnchor(TextAnchor.TOP_RIGHT);
					rWeightedSpec.setPlotAnnotations(Lists.newArrayList(weightedAnn));
					
					if (name2.toLowerCase().contains("garlock")) {
						System.out.println("SWAPPING!!!");
						title = name2+" vs "+name1;
						// swap
						freqSpec = swapSpec(freqSpec);
						rSpec = swapSpec(rSpec);
						rWeightedSpec = swapSpec(rWeightedSpec);
					}
					
					List<XYZPlotSpec> xyzSpecs = Lists.newArrayList(rSpec, freqSpec, rWeightedSpec);
					XYZGraphPanel xyzGP = new XYZGraphPanel();
					xyzGP.drawPlot(xyzSpecs, false, false, xyzXRanges, xyzYRanges, null);
					xyzGP.getChartPanel().setSize(1000, 1500);
					File xyzFile = new File(synchXYZDir, PeriodicityPlotter.getFileSafeString(name1)
							+"_"+PeriodicityPlotter.getFileSafeString(name2)+".pdf");
					xyzGP.saveAsPDF(xyzFile.getAbsolutePath());
					synch2DPDFs.add(xyzFile);
					
					if (contribIndex >= 0) {
						System.out.println("Highest contrib of "+(float)highestContrib+" at "+synch_indices.get(contribIndex)+". r="
								+probMNs.get(contribIndex).floatValue()+"/("+probMs.get(contribIndex).floatValue()
								+"*"+probNs.get(contribIndex).floatValue()+")="
								+synchs.get(contribIndex).floatValue()+", Weight(Sij)="+(float)(freqEithers.get(contribIndex)/totEithers));
					}
					
					
					File scatterPlotFile = new File(synchScatterDir, "synch_scatter_"
							+PeriodicityPlotter.getFileSafeString(name1)+"_"
							+PeriodicityPlotter.getFileSafeString(name2));
					
					List<PlotCurveCharacterstics> chars = Lists.newArrayList(
							new PlotCurveCharacterstics(PlotSymbol.CROSS, 3f, Color.BLACK));
					title = "Synch Param "+name1+" vs "+name2+" ("+nDims+"D): "+params[m][n][lagIndex-1];
					xAxisLabel = "r: freqMN/(freqM*freqN)";
					yAxisLabel = "Weight";
					PlotSpec spec = new PlotSpec(asList(synchFunc), chars, title, xAxisLabel, yAxisLabel);
					
					HeadlessGraphPanel gp = new HeadlessGraphPanel();
					gp.setBackgroundColor(Color.WHITE);
					gp.setTickLabelFontSize(14);
					gp.setAxisLabelFontSize(16);
					gp.setPlotLabelFontSize(18);

					Range xRange = new Range(1e-15, 1e3);
					Range yRange = new Range(1e-6, 1e-0);
					gp.drawGraphPanel(spec, true, true, xRange, yRange);
					
					gp.getCartPanel().setSize(1000, 800);
					gp.saveAsPNG(scatterPlotFile.getAbsolutePath()+".png");
					gp.saveAsPDF(scatterPlotFile.getAbsolutePath()+".pdf");
					synchScatterPDFs.add(new File(scatterPlotFile.getAbsolutePath()+".pdf"));
					
					File contribScatterFile = new File(synchScatterDir, "synch_scatter_contrib_"
							+PeriodicityPlotter.getFileSafeString(name1)+"_"
							+PeriodicityPlotter.getFileSafeString(name2));
					title = "Synch Param "+name1+" vs "+name2+" ("+nDims+"D): "+params[m][n][lagIndex-1];
					xAxisLabel = "r: freqMN/(freqM*freqN)";
					yAxisLabel = "Contribution: rBar - rBar(without point)";
					spec = new PlotSpec(asList(contribFunc), chars, title, xAxisLabel, yAxisLabel);
					
					gp = new HeadlessGraphPanel();
					gp.setBackgroundColor(Color.WHITE);
					gp.setTickLabelFontSize(14);
					gp.setAxisLabelFontSize(16);
					gp.setPlotLabelFontSize(18);
					
					yRange = new Range(1e-6, 1e1);
					gp.drawGraphPanel(spec, true, true, xRange, yRange);
					
					gp.getCartPanel().setSize(1000, 800);
					gp.saveAsPNG(contribScatterFile.getAbsolutePath()+".png");
				}
				if (m == n)
					continue;
				
				// lag plot spec
//				String title = "Synch Param "+name1+" vs "+name2+" ("+nDims+"D): "+params[m][n];
				EvenlyDiscretizedFunc lnSynchFunch = new EvenlyDiscretizedFunc(
						synchLagFunc.getMinX(), synchLagFunc.getNum(), synchLagFunc.getDelta());
				for (int i=0; i<synchLagFunc.getNum(); i++)
					lnSynchFunch.set(i, Math.log(synchLagFunc.getY(i)));
				PlotSpec spec = new PlotSpec(asList(lnSynchFunch), lagChars, lagTitle, lagXAxisLabel, lagYAxisLabel);
				double annY = lagYRange.getUpperBound()*0.95;
				double annX = lagXRange.getLowerBound()*0.9;
				Font font = new Font(Font.SERIF, Font.PLAIN, 14);
				XYTextAnnotation leftAnn = new XYTextAnnotation(name1+" vs "+name2, annX, annY);
				leftAnn.setFont(font);
				leftAnn.setTextAnchor(TextAnchor.TOP_LEFT);
				List<XYTextAnnotation> annotations = Lists.newArrayList(leftAnn);
				spec.setPlotAnnotations(annotations);
				lagSpecs.add(spec);
			}
		}
		
		List<List<PlotSpec>> lagSpecPages = Lists.newArrayList();
		lagSpecPages.add(new ArrayList<PlotSpec>());
		int specsToBin = 4;
		for (int i=0; i<lagSpecs.size(); i++) {
			PlotSpec spec = lagSpecs.get(i);
			List<PlotSpec> specs = lagSpecPages.get(lagSpecPages.size()-1);
			if (specs.size() == specsToBin) {
				specs = Lists.newArrayList();
				lagSpecPages.add(specs);
			}
			specs.add(spec);
		}
		
		File synchLagDir = new File(file.getParentFile(), "synch_lag");
		if (!synchLagDir.exists())
			synchLagDir.mkdir();
		List<File> synchLagFiles = Lists.newArrayList();
		EvenlyDiscretizedFunc blankFunc = new EvenlyDiscretizedFunc(lagFuncMin, lags, distSpacing);
		List<PlotCurveCharacterstics> lagBlankChars = Lists.newArrayList(
				new PlotCurveCharacterstics(PlotLineType.SOLID, 0f, Color.BLACK));
		List<Range> lagXRanges = Lists.newArrayList(lagXRange);
		List<Range> lagYRanges = Lists.newArrayList();
		for (int i=0; i<specsToBin; i++)
			lagYRanges.add(lagYRange);
		for (int i=0; i<lagSpecPages.size(); i++) {
			List<PlotSpec> lagSpecPage = lagSpecPages.get(i);
			while (lagSpecPage.size() < specsToBin) {
				// add blank plots
				lagSpecPage.add(new PlotSpec(asList(blankFunc),
						lagBlankChars, lagTitle, lagXAxisLabel, lagYAxisLabel));
			}
			
			File synchLagFile = new File(synchLagDir, "synch_lag_page"+i+".pdf");
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setBackgroundColor(Color.WHITE);
			gp.setTickLabelFontSize(14);
			gp.setAxisLabelFontSize(16);
			gp.setPlotLabelFontSize(18);
			
			gp.setCombinedOnYAxis(false);

			gp.drawGraphPanel(lagSpecPage, false, false, lagXRanges, lagYRanges);
			
			// 8.5x11
			gp.getCartPanel().setSize(850, 1100);
			gp.saveAsPDF(synchLagFile.getAbsolutePath());
			synchLagFiles.add(synchLagFile);
		}
		if (!synchLagFiles.isEmpty())
			PeriodicityPlotter.combinePDFs(synchLagFiles, new File(synchLagDir, "synch_lags.pdf"));
		if (!synch2DPDFs.isEmpty())
			PeriodicityPlotter.combinePDFs(synch2DPDFs, new File(synchXYZDir, "synch_xyzs.pdf"));
		if (!synchScatterPDFs.isEmpty())
			PeriodicityPlotter.combinePDFs(synchScatterPDFs, new File(synchScatterDir, "synch_scatters.pdf"));
		
		CSVFile<String> csv = new CSVFile<String>(false);
		
		List<String> header = Lists.newArrayList("");
		for (RuptureIdentifier iden : idens)
			header.add(iden.getName());
		
		int lag0Index = lagMax;
		
		for (int type=0; type<3; type++) {
			double[][] myParams;
			switch (type) {
			case 0:
				csv.addLine("Linear");
				myParams = new double[nDims][nDims];
				for (int m=0; m<nDims; m++)
					for (int n=0; n<nDims; n++)
						myParams[m][n] = params[m][n][lag0Index];
				break;
			case 1:
				csv.addLine("Log10");
				myParams = new double[nDims][nDims];
				for (int m=0; m<nDims; m++)
					for (int n=0; n<nDims; n++)
						myParams[m][n] = Math.log10(params[m][n][lag0Index]);
				break;
			case 2:
				csv.addLine("Ln");
				myParams = new double[nDims][nDims];
				for (int m=0; m<nDims; m++)
					for (int n=0; n<nDims; n++)
						myParams[m][n] = Math.log(params[m][n][lag0Index]);
				break;

			default:
				throw new IllegalStateException();
			}
			csv.addLine(header);
			for (int i=0; i<nDims; i++) {
				List<String> line = Lists.newArrayList();
				
				line.add(idens.get(i).getName());
				for (int j=0; j<nDims; j++) {
					if (i == j)
						line.add("");
					else
						line.add(myParams[i][j]+"");
				}
				
				csv.addLine(line);
			}
			csv.addLine("");
			csv.addLine("");
		}
		
		csv.writeToFile(file);
	}
	
	private static XYZPlotSpec swapSpec(XYZPlotSpec spec) {
		EvenlyDiscrXYZ_DataSet orig = (EvenlyDiscrXYZ_DataSet) spec.getXYZ_Data();
		EvenlyDiscrXYZ_DataSet swapped = new EvenlyDiscrXYZ_DataSet(
				orig.getNumX(), orig.getNumY(), orig.getMinX(), orig.getMinY(), orig.getGridSpacing());
		for (int x=0; x<orig.getNumX(); x++)
			for (int y=0; y<orig.getNumY(); y++)
				swapped.set(y, x, orig.get(x, y));
		
		XYZPlotSpec swappedSpec = new XYZPlotSpec(swapped, spec.getCPT(), spec.getTitle(),
				spec.getYAxisLabel(), spec.getXAxisLabel(), spec.getZAxisLabel());
		swappedSpec.setPlotAnnotations(spec.getPlotAnnotations());
		return swappedSpec;
	}
	
	private static void loadIdens(int[] include_elems, List<RuptureIdentifier> rupIdens, List<Color> colors) {
		for (int elemID : include_elems) {
			String name;
			Color color;
			switch (elemID) {
			case ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID:
				name = "SAF Cholame 7+";
				color = Color.RED;
				break;
			case ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID:
				name = "SAF Carrizo 7+";
				color = Color.BLUE;
				break;
			case ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID:
				name = "Garlock 7+";
				color = Color.GREEN;
				break;
			case ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID:
				name = "SAF Mojave 7+";
				color = Color.BLACK;
				break;
			case ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID:
				name = "SAF Coachella 7+";
				color = Color.RED;
				break;
			case ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID:
				name = "San Jacinto 7+";
				color = Color.CYAN;
				break;

			default:
				throw new IllegalStateException("Unknown elem: "+elemID);
			}
			rupIdens.add(new ElementMagRangeDescription(name,
					elemID, 7d, 10d));
			if (colors != null)
				colors.add(color);
		}
	}
	
	public static void main(String[] args) throws IOException {
		File dir = new File("/home/kevin/Simulators");
		File geomFile = new File(dir, "ALLCAL2_1-7-11_Geometry.dat");
		System.out.println("Loading geometry...");
		General_EQSIM_Tools tools = new General_EQSIM_Tools(geomFile);
//		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.barall");
		File eventFile = new File(dir, "eqs.ALLCAL2_RSQSim_sigma0.5-5_b=0.015.long.barall");
		System.out.println("Loading events...");
		
		int[] include_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		boolean gen_2d_corr_pdfs = false;
		
		RandomDistType randDistType = RandomDistType.STATE_BASED;
		
		File writeDir = new File(dir, "period_plots");
		if (!writeDir.exists())
			writeDir.mkdir();
		
		List<RuptureIdentifier> rupIdens = Lists.newArrayList();
		List<Color> colors = Lists.newArrayList();
		
		loadIdens(include_elems, rupIdens, colors);
		
		int[] all_elems = {
				ElementMagRangeDescription.SAF_CHOLAME_ELEMENT_ID,
				ElementMagRangeDescription.SAF_CARRIZO_ELEMENT_ID,
				ElementMagRangeDescription.GARLOCK_WEST_ELEMENT_ID,
				ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID,
				ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID,
				ElementMagRangeDescription.SAN_JACINTO__ELEMENT_ID
				};
		List<RuptureIdentifier> allIdens = Lists.newArrayList();
		loadIdens(all_elems, allIdens, null);
		
//		tools.read_EQSIMv04_EventsFile(eventFile, rupIdens);
		tools.read_EQSIMv04_EventsFile(eventFile, allIdens);
		List<EQSIM_Event> events = tools.getEventsList();
		
		writeDir = new File("/tmp");
		
		writeDir = new File(writeDir, randDistType.getFNameAdd()+"_corr_plots");
		if (!writeDir.exists())
			writeDir.mkdir();

		Map<IDPairing, HistogramFunction> origFuncs =
				PeriodicityPlotter.plotTimeBetweenAllIdens(writeDir, events, rupIdens, colors,
						null, null, 2000d, 10d);
		PeriodicityPlotter.	plotTimeBetweenAllIdens(writeDir, events, rupIdens, colors,
				randDistType, origFuncs, 2000d, 10d);
		
		File myCorrCombined = new File(new File(writeDir, randDistType.getFNameAdd()+"_corr_plots"),
				"corr_combined_rand_state_based.pdf");
		File myCorrCombinedNew = new File(new File(writeDir, randDistType.getFNameAdd()+"_corr_plots"),
				rupIdens.size()+"D_corr_combined_rand_state_based.pdf");
		FileUtils.copyFile(myCorrCombined, myCorrCombinedNew);

		//			File subDir = new File(writeDir, "round2");
		//			if (!subDir.exists())
		//				subDir.mkdir();
		//			PeriodicityPlotter.	plotTimeBetweenAllIdens(subDir, rand_events, rupIdens, rupIdenNames, colors,
		//					RandomDistType.MOJAVE_DRIVER, origFuncs, 2000d, 10d);

		System.out.println("DONE");
		
		File pdfDir = new File("/tmp/state_pdfs");
		if (!pdfDir.exists())
			pdfDir.mkdir();
		
		List<File> pdfs = Lists.newArrayList();
		
		List<List<EQSIM_Event>> matchesLists = Lists.newArrayList();
		for (int i=0; i<rupIdens.size(); i++)
			matchesLists.add(rupIdens.get(i).getMatches(events));
		
		List<File> twoD_corr_pdfs = Lists.newArrayList();
		
		for (int i=0; i<rupIdens.size(); i++) {
			for (int j=i+1; j<rupIdens.size(); j++) {
				String name1 = rupIdens.get(i).getName();
				String name2 = rupIdens.get(j).getName();
				System.out.println("Writing PDF for "+name1+" vs "+name2);
				StateBasedCatalogBuilder builder = new StateBasedCatalogBuilder();
				builder.buildCatalog(events, null, Lists.newArrayList(matchesLists.get(i), matchesLists.get(j)));
				pdfs.addAll(builder.write2DDists(pdfDir, i, name1, matchesLists.get(i), j, name2, matchesLists.get(j)));
				
				
				
				// now 2d-only ACDFs/CCDFs
				if (gen_2d_corr_pdfs && rupIdens.size() > 2) {
					List<RuptureIdentifier> subIdens = Lists.newArrayList(rupIdens.get(i), rupIdens.get(j));
					File subWriteDir = new File(writeDir,
							"2d_"+PeriodicityPlotter.getFileSafeString(name1)+"_"+PeriodicityPlotter.getFileSafeString(name2));
					if (!subWriteDir.exists())
						subWriteDir.mkdir();
					origFuncs = PeriodicityPlotter.plotTimeBetweenAllIdens(subWriteDir, events, subIdens, colors,
									null, null, 2000d, 10d);
					PeriodicityPlotter.	plotTimeBetweenAllIdens(subWriteDir, events, subIdens, colors,
							randDistType, origFuncs, 2000d, 10d);
					
					twoD_corr_pdfs.add(new File(new File(subWriteDir, randDistType.getFNameAdd()+"_corr_plots"),
							"corr_combined_"+randDistType.getFNameAdd()+".pdf"));
				}
			}
		}
		
		if (!pdfs.isEmpty())
			PeriodicityPlotter.combinePDFs(pdfs, new File(pdfDir, "state_dists.pdf"));
		
		if (!twoD_corr_pdfs.isEmpty())
			PeriodicityPlotter.combinePDFs(twoD_corr_pdfs, new File(
					new File(writeDir, randDistType.getFNameAdd()+"_corr_plots"), "2D_corr_combined_rand_state_based.pdf"));
		
		
		StateBasedCatalogBuilder builder = new StateBasedCatalogBuilder();
		builder.buildCatalog(events, null, matchesLists);
//		builder.writeTransitionStats(null);
		
		File synchCSVFile = new File("/tmp/synch_params.csv");
		builder.writeSynchParamsTable(synchCSVFile, rupIdens);
		
		// testing time
		int m = Ints.indexOf(include_elems, ElementMagRangeDescription.SAF_MOJAVE_ELEMENT_ID);
		int n = Ints.indexOf(include_elems, ElementMagRangeDescription.SAF_COACHELLA_ELEMENT_ID);
		List<List<EQSIM_Event>> subMatchesLists = Lists.newArrayList();
		subMatchesLists.add(matchesLists.get(m));
		subMatchesLists.add(matchesLists.get(n));
		StateBasedCatalogBuilder test = new StateBasedCatalogBuilder();
		test.buildCatalog(events, null, subMatchesLists);
		
		// first bin by only the indices we care about
		Map<IndicesKey, List<int[]>> binnedIndices = Maps.newHashMap();
		for (int[] indices : builder.totalStatesDataset.getPopulatedIndices()) {
			int[] myInd = { indices[m], indices[n] };
			IndicesKey key = new IndicesKey(myInd);
			List<int[]> binned = binnedIndices.get(key);
			if (binned == null) {
				binned = Lists.newArrayList();
				binnedIndices.put(key, binned);
			}
			binned.add(indices);
		}
		
		// first bin by only the indices we care about
		Map<IndicesKey, List<int[]>> testBinnedIndices = Maps.newHashMap();
		for (int[] indices : test.totalStatesDataset.getPopulatedIndices()) {
			int[] myInd = { indices[0], indices[1] };
			IndicesKey key = new IndicesKey(myInd);
			List<int[]> binned = testBinnedIndices.get(key);
			if (binned == null) {
				binned = Lists.newArrayList();
				testBinnedIndices.put(key, binned);
			}
			binned.add(indices);
		}
		
		System.out.println("6D has "+binnedIndices.size());
		System.out.println("2D has "+testBinnedIndices.size());
		
		// now find discrepancies
		Joiner j = Joiner.on(",");
		for (IndicesKey key : binnedIndices.keySet()) {
			if (!testBinnedIndices.containsKey(key)) {
				List<IndicesKey> subInds = Lists.newArrayList();
				for (int[] indices : binnedIndices.get(key))
					subInds.add(new IndicesKey(indices));
				System.out.println("6-D yes 2-D no: "+key+"\tvals: "+j.join(subInds));
			}
		}
		
		// now find discrepancies
		for (IndicesKey key : testBinnedIndices.keySet()) {
			if (!binnedIndices.containsKey(key)) {
				List<IndicesKey> subInds = Lists.newArrayList();
				for (int[] indices : testBinnedIndices.get(key))
					subInds.add(new IndicesKey(indices));
				System.out.println("6-D no 2-D yes: "+key+"\tvals: "+j.join(subInds));
			}
		}
		
		System.out.println("Done!");
	}
	
}