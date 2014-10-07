package scratch.kevin.simulators.synch.prediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;
import scratch.kevin.simulators.SynchIdens.SynchFaults;
import scratch.kevin.simulators.synch.MarkovChainBuilder;

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
		
		for (int i=0; i<predictors.size(); i++)
			calcInformationGain(i);
	}
	
	private void calcInformationGain(int predictorIndex) {
		Predictor predictor = predictors.get(predictorIndex);
		List<double[]> predictions = this.predictions.get(predictorIndex);
		double[] predictedNs = this.predictedNs.get(predictorIndex);
		double predictedN = StatUtils.sum(predictedNs);
		System.out.println("Predicted N for "+predictor.getName()+": "+predictedN
				+", ["+Joiner.on(",").join(getFloatList(predictedNs))+"]");
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
	}
	
	private static List<Float> getFloatList(double[] array) {
		List<Float> l = Lists.newArrayList();
		for (double val : array)
			l.add((float)val);
		return l;
	}

	public static void main(String[] args) throws IOException {
		double minMag = 7d; 
		double maxMag = 10d;
		double distSpacing = 10d; // years
		
		List<RuptureIdentifier> rupIdens = SynchIdens.getIndividualFaults(minMag, maxMag,
				SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO, SynchFaults.SAF_CARRIZO);
//				SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO);
		
		List<EQSIM_Event> events = new SimAnalysisCatLoader(true, rupIdens).getEvents();
		
		List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, events, rupIdens, 0d);
		
		int learningIndex = fullPath.size()/2;
		
		List<Predictor> predictors = Lists.newArrayList();
		predictors.add(new MarkovPredictor(new RecurrIntervalPredictor()));
		predictors.add(new MarkovPredictor());
		predictors.add(new RecurrIntervalPredictor());
		predictors.add(new PoissonPredictor());
		int nullHypothesisIndex = predictors.size()-1;
		
		double[] minRates = { 0d, 1e-6, 1e-5, 1e-4, 1e-3, 1e-2, 1e-1, 1 };
		
		for (double minRate : minRates) {
			System.out.println("****************************");
			System.out.println("min rate: "+minRate);
			System.out.println("****************************");
			
			PredictionTests tests = new PredictionTests(predictors, nullHypothesisIndex, distSpacing);
			
			tests.skipZero = minRate == 0d;
			tests.minRate = minRate;
			
			tests.doTests(fullPath, learningIndex);
		}
	}

}
