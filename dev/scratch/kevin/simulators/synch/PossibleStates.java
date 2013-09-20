package scratch.kevin.simulators.synch;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PossibleStates {
	List<int[]> states = Lists.newArrayList();
	private Map<IndicesKey, Integer> stateIndexMap = Maps.newHashMap();
	List<Double> frequencies = Lists.newArrayList();
	double tot = 0d;
	
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
	
	public double getTot() {
		return tot;
	}
	
	public List<int[]> getStates() {
		return states;
	}
	
	public int getNumStates() {
		return states.size();
	}
}