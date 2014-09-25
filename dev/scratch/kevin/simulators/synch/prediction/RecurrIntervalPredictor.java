package scratch.kevin.simulators.synch.prediction;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class RecurrIntervalPredictor implements Predictor {
	
	private List<List<Double>> rupFreqs;
	private List<List<Double>> occFreqs;
	private List<int[]> path;
	private int nDims;

	@Override
	public String getName() {
		return "Single Fault Recurrence Interval";
	}

	@Override
	public void init(List<int[]> initialPath, double distSpacing) {
		rupFreqs = Lists.newArrayList();
		occFreqs = Lists.newArrayList();
		nDims = initialPath.get(0).length;
		this.path = Lists.newArrayList();
		
		for (int i=0; i<nDims; i++) {
			rupFreqs.add(new ArrayList<Double>());
			occFreqs.add(new ArrayList<Double>());
		}
		
		for (int[] state : initialPath) {
			addState(state);
		}
	}

	@Override
	public void addState(int[] state) {
		if (!path.isEmpty()) {
			int[] prevState = path.get(path.size()-1);
			for (int i=0; i<nDims; i++) {
				boolean rupture = state[i] == 0;
				int faultState = prevState[i];
				ensureListsCapacity(i, faultState+1);
				increment(occFreqs.get(i), faultState);
				if (rupture)
					increment(rupFreqs.get(i), faultState);
			}
		}
		
		path.add(state);
	}
	
	private void increment(List<Double> list, int index) {
		double prev = list.get(index);
		list.set(index, prev+1d);
	}
	
	private void ensureListsCapacity(int index, int size) {
		ensureListCapacity(size, rupFreqs.get(index));
		ensureListCapacity(size, occFreqs.get(index));
	}
	
	private void ensureListCapacity(int size, List<Double> list) {
		while (list.size() < size)
			list.add(0d);
	}

	@Override
	public double[] getRuptureProbabilities() {
		double[] ret = new double[nDims];
		
		int[] curState = path.get(path.size()-1);
		
		for (int i=0; i<nDims; i++) {
			double occFreq = occFreqs.get(i).get(curState[i]);
			double rupFreq = rupFreqs.get(i).get(curState[i]);
			ret[i] = rupFreq/occFreq;
		}
		
		return ret;
	}

	@Override
	public void printDiagnostics() {
		// do nothing
	}

}
