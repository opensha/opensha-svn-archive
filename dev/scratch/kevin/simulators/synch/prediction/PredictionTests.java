package scratch.kevin.simulators.synch.prediction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.iden.RuptureIdentifier;
import org.opensha.sha.simulators.utils.General_EQSIM_Tools;

import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;
import scratch.kevin.simulators.SynchIdens.SynchFaults;
import scratch.kevin.simulators.dists.LogNormalDistReturnPeriodProvider;
import scratch.kevin.simulators.synch.MarkovChainBuilder;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class PredictionTests {
	
	private List<Predictor> predictors;
	private int nullHypothesisIndex;
	private double distSpacing;
	
	private List<List<double[]>> predictions;
	private List<double[]> predictedNs;
	private List<int[]> actual;
	private int[] actualNs;
	private int actualN;
	
	private int nDims;
	
	private double minRate = 1e-1d;
	private boolean skipZero = false;
	
	public PredictionTests(List<Predictor> predictors, int nullHypothesisIndex, double distSpacing) {
		Preconditions.checkState(predictors.size() >= 2, "Must supply at least 2 predictors");
		Preconditions.checkState(nullHypothesisIndex >=0 && nullHypothesisIndex<predictors.size(), "Must supply null hypothesis");
		Preconditions.checkState(distSpacing>0, "dist spacing must be >0");
		
		this.predictors = predictors;
		this.nullHypothesisIndex = nullHypothesisIndex;
		this.distSpacing = distSpacing;
	}
	
	public void doTests(List<int[]> fullPath, int learningIndex) {
		List<int[]> initialPath = fullPath.subList(0, learningIndex);
		
		nDims = fullPath.get(0).length;
		
		predictions = Lists.newArrayList();
		predictedNs = Lists.newArrayList();
		actual = Lists.newArrayList();
		
		System.out.println("Preparing "+predictors.size()+" predictors with "+learningIndex+" learning states");
		
		for (Predictor p : predictors) {
			p.init(initialPath, distSpacing);
			predictions.add(new ArrayList<double[]>());
			predictedNs.add(new double[nDims]);
		}
		
		System.out.println("Doing predictions");
		for (int i=learningIndex; i<fullPath.size(); i++) {
			int[] state = fullPath.get(i);
			actual.add(state);
			
			for (int j=0; j<predictors.size(); j++) {
				Predictor p = predictors.get(j);
				double[] prediction = p.getRuptureProbabilities();
				double[] predictedN = predictedNs.get(j);
				for (int k=0; k<nDims; k++)
					predictedN[k] += prediction[k];
				predictions.get(j).add(prediction);
				p.addState(state);
			}
		}
		
		actualNs = new int[nDims];
		for (int[] state : actual)
			for (int i=0; i<nDims; i++)
				if (state[i] == 0)
					actualNs[i]++;
		actualN = 0;
		for (int n : actualNs)
			actualN += n;
		
		System.out.println("Done with "+actual.size()+" predictions");
		
		System.out.println("Actual N: "+actualN+", ["+Joiner.on(",").join(Ints.asList(actualNs))+"]");
		
		List<Double> gains = Lists.newArrayList();
		for (int i=0; i<predictors.size(); i++)
			gains.add(calcInformationGain(i));
		
		List<ComparablePairing<Double, Predictor>> sorted = ComparablePairing.build(gains, predictors);
		Collections.sort(sorted);
		Collections.reverse(sorted);
		System.out.println("\n***SORTED***");
		for (ComparablePairing<Double, Predictor> p : sorted) {
			double[] predictedNs = this.predictedNs.get(predictors.indexOf(p.getData()));
			double[] nCompare = new double[actualNs.length];
			for (int i=0; i<actualNs.length; i++)
				nCompare[i] = 100d*predictedNs[i]/actualNs[i];
			System.out.println("\t"+p.getComparable().floatValue()+":\t"+p.getData().getName()
					+"\t\tCount %: ["+Joiner.on(",").join(getFloatList(nCompare))+"]");
		}
	}
	
	private double calcInformationGain(int predictorIndex) {
		Predictor predictor = predictors.get(predictorIndex);
		List<double[]> predictions = this.predictions.get(predictorIndex);
		double[] predictedNs = this.predictedNs.get(predictorIndex);
		double[] nCompare = new double[actualNs.length];
		for (int i=0; i<actualNs.length; i++)
			nCompare[i] = 100d*predictedNs[i]/actualNs[i];
		double predictedN = StatUtils.sum(predictedNs);
		System.out.println("Predicted N for "+predictor.getName()+": "+predictedN
				+", ["+Joiner.on(",").join(getFloatList(predictedNs))
				+"], %: ["+Joiner.on(",").join(getFloatList(nCompare))+"]");
		predictor.printDiagnostics();

		List<double[]> nullPredictions = this.predictions.get(nullHypothesisIndex);
		double[] nullPredictedNs = this.predictedNs.get(nullHypothesisIndex);
		double nullPredictedN = StatUtils.sum(nullPredictedNs);
		
		// A = predictor, B = null predictor
		// I(A,B) = (1/N) * sumOverN(log(rateA) - log(rateB)) - (NA - NB)/N
		
		double sumLogRateDiff = 0;
		
		double adjustedActualN = 0;
		
		for (int i=0; i<actual.size(); i++) {
			int[] state = actual.get(i);
			for (int n=0; n<nDims; n++) {
				if (state[n] == 0) {
					// it's a rupture
					double rateA = Math.max(predictions.get(i)[n], minRate);
					double rateB =  Math.max(nullPredictions.get(i)[n], minRate);
					
					if (skipZero && (rateA == 0 || rateB == 0))
						continue;
					
					sumLogRateDiff += Math.log(rateA)-Math.log(rateB);
					
					adjustedActualN++;
				}
			}
		}
		
		double ig = (1d/(double)adjustedActualN)*sumLogRateDiff - (predictedN - nullPredictedN)/(double)adjustedActualN;
		System.out.println("Information gain for "+predictor.getName()+" relative to "
				+predictors.get(nullHypothesisIndex).getName()+": "+ig
				+" (evaluated for "+adjustedActualN+"/"+actualN+" = "+100f*(float)(adjustedActualN/(double)actualN)+" %)");
		
		return ig;
	}
	
	private void write2DProbPlots(File outputDir, List<RuptureIdentifier> rupIdens) throws IOException {
		Preconditions.checkState((outputDir.exists() && outputDir.isDirectory()) || outputDir.mkdir());
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().rescale(0d, 1d);
		
		for (Predictor p : predictors) {
			File subDir = new File(outputDir, p.getShortName());
			Preconditions.checkState((subDir.exists() && subDir.isDirectory()) || subDir.mkdir());
			
			for (int m=0; m<nDims; m++) {
				for (int n=m+1; n<nDims; n++) {
					Predictor p2d;
					if (nDims == 2)
						p2d = p;
					else
						p2d = p.getCollapsed(m, n);
					EvenlyDiscrXYZ_DataSet probX = new EvenlyDiscrXYZ_DataSet(
							100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
					EvenlyDiscrXYZ_DataSet probY = new EvenlyDiscrXYZ_DataSet(
							100, 100, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
					
					String name1 = rupIdens.get(m).getName();
					String name2 = rupIdens.get(n).getName();
					
					for (int i=0; i<probX.getNumX(); i++) {
						for (int j=0; j<probX.getNumY(); j++) {
							int[] state = {i,j};
							double[] probs = p2d.getRuptureProbabilities(state);
							probX.set(i, j, probs[0]);
							probY.set(i, j, probs[1]);
						}
					}

					XYZPlotSpec xSpec = new XYZPlotSpec(probX, cpt, p2d.getName()+" Rup Probs",
							name1+" OI", name2+" OI", name1+" Probability");
					XYZPlotSpec ySpec = new XYZPlotSpec(probY, cpt, p2d.getName()+" Rup Probs",
							name1+" OI", name2+" OI", name2+" Probability");
					
					List<XYZPlotSpec> specs = Lists.newArrayList(xSpec, ySpec);
					
					XYZGraphPanel xyzGP = new XYZGraphPanel();
					xyzGP.drawPlot(specs, false, false, null, null);
					xyzGP.getChartPanel().setSize(1000, 2000);
					String prefix = PeriodicityPlotter.getFileSafeString(name1)
							+"_"+PeriodicityPlotter.getFileSafeString(name2);
					xyzGP.saveAsPDF(new File(subDir, prefix+".pdf").getAbsolutePath());
					xyzGP.saveAsPNG(new File(subDir, prefix+".png").getAbsolutePath());
				}
			}
		}
	}
	
	private static List<Float> getFloatList(double[] array) {
		List<Float> l = Lists.newArrayList();
		for (double val : array)
			l.add((float)val);
		return l;
	}
	
	/**
	 * Generates a fake series of events where the first fault is on a log-normal renewal model, and the second
	 * fault happens with every second rupture of the first.
	 * @param events
	 * @param rupIdens
	 * @return
	 */
	private static List<EQSIM_Event> generateFakeData(List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens) {
		return generateFakeData(events, rupIdens, System.currentTimeMillis());
	}
	
	private static List<EQSIM_Event> generateFakeData(List<EQSIM_Event> events, List<RuptureIdentifier> rupIdens,
			long seed) {
		Preconditions.checkState(rupIdens.size() == 2);
		RuptureIdentifier iden1 = rupIdens.get(0);
		RuptureIdentifier iden2 = rupIdens.get(1);
		
		// first find all unique events for each
		List<EQSIM_Event> events1 = iden1.getMatches(events);
		List<EQSIM_Event> events2 = iden2.getMatches(events);
		
		LogNormalDistReturnPeriodProvider riProv =
				new LogNormalDistReturnPeriodProvider(PeriodicityPlotter.getRPs(events1));
		riProv.setSeed(seed);
		
		HashSet<EQSIM_Event> events2Hash = new HashSet<EQSIM_Event>(events2);
		
		// remove duplicates
		for (int i=events1.size(); --i>=0;) {
			EQSIM_Event event = events1.get(i);
			if (events2Hash.contains(event)) {
				events1.remove(i);
				Preconditions.checkState(events2.remove(event));
			}
		}
		
		List<EQSIM_Event> fakeEvents = Lists.newArrayList();
		
		boolean includeFault2 = true;
		
		int eventID = 0;
		
		double time = 0d;
		while (!events1.isEmpty() && !events2.isEmpty()) {
			double riYears = riProv.getReturnPeriod();
			time += riYears*General_EQSIM_Tools.SECONDS_PER_YEAR;
			fakeEvents.add(events1.remove(0).cloneNewTime(time, eventID++));
			if (includeFault2)
				fakeEvents.add(events2.remove(0).cloneNewTime(time, eventID++));
			includeFault2 = !includeFault2;
		}
		
		System.out.println("Fake catalog has "+fakeEvents.size()
				+" events, length: "+(time/General_EQSIM_Tools.SECONDS_PER_YEAR));
		
		Preconditions.checkState(!fakeEvents.isEmpty());
		
		return fakeEvents;
	}

	public static void main(String[] args) throws IOException {
		double minMag = 7d; 
		double maxMag = 10d;
		double distSpacing = 10d; // years
		
		boolean do2DPlots = true;
		File predictDir = new File("/home/kevin/Simulators/predict");
		File plot2DOutputDir = new File(predictDir, "plots_2d");
		File synchPlotOutputDir = new File(predictDir, "synch_plots");
		
		boolean fakeData = true;
		List<RuptureIdentifier> rupIdens = SynchIdens.getIndividualFaults(minMag, maxMag,
//				SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO, SynchFaults.SAF_CARRIZO);
//				SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO);
				SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA);
//				SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO);
		
		List<EQSIM_Event> events = new SimAnalysisCatLoader(true, rupIdens, true).getEvents();
		if (fakeData)
			events = generateFakeData(events, rupIdens, 0l);
		
		List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, events, rupIdens, 0d);
		
		int learningIndex = fullPath.size()/2;
		
		List<Predictor> predictors = Lists.newArrayList();
		predictors.add(new MarkovPredictor(new RecurrIntervalPredictor()));
		predictors.add(new MarkovPredictor());
		predictors.add(new RecurrIntervalPredictor());
		int nullHypothesisIndex = predictors.size()-1;
		SynchRIPredictor synch = new SynchRIPredictor(50);
		predictors.add(synch);
		if (rupIdens.size() == 2 && fakeData)
			predictors.add(new SplitPredictor(new RecurrIntervalPredictor(), new SynchRIPredictor(50)));
		predictors.add(new PoissonPredictor());
//		int nullHypothesisIndex = predictors.size()-1;
		
//		double[] minRates = { 0d, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1 };
		double[] minRates = { 0d };
		
		for (double minRate : minRates) {
			System.out.println("****************************");
			System.out.println("min rate: "+minRate);
			System.out.println("****************************");
			
			PredictionTests tests = new PredictionTests(predictors, nullHypothesisIndex, distSpacing);
			
			tests.skipZero = minRate == 0d;
			tests.minRate = minRate;
			
			tests.doTests(fullPath, learningIndex);
			
			if (do2DPlots && minRate == 0d)
				tests.write2DProbPlots(plot2DOutputDir, rupIdens);
		}
		synch.writePlots(synchPlotOutputDir, rupIdens);
	}

}
