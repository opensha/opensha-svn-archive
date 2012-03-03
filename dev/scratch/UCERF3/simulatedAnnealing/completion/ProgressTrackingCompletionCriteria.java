package scratch.UCERF3.simulatedAnnealing.completion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;
import org.opensha.commons.data.CSVFile;

import com.google.common.collect.Lists;

public class ProgressTrackingCompletionCriteria implements CompletionCriteria {
	
	private ArrayList<Long> times;
	private ArrayList<Long> iterations;
	private ArrayList<Long> perturbs;
	private ArrayList<double[]> energies;
	
	private CompletionCriteria criteria;
	
	private File automaticFile;
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria) {
		this(criteria, null);
	}
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria, File automaticFile) {
		times = new ArrayList<Long>();
		iterations = new ArrayList<Long>();
		energies = new ArrayList<double[]>();
		perturbs = new ArrayList<Long>();
		
		this.criteria = criteria;
		this.automaticFile = automaticFile;
	}
	
	public void writeFile(File file) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		csv.addLine(Lists.newArrayList("Iterations", "Time (millis)", "Energy (total)",
				"Energy (equality)", "Energy (entropy)", "Energy (inequality)",
				"Total Perterbations Kept"));
		
		for (int i=0; i<times.size(); i++) {
			double[] energy = energies.get(i);
			csv.addLine(Lists.newArrayList(iterations.get(i)+"", times.get(i)+"",
					energy[0]+"", energy[1]+"", energy[2]+"", energy[3]+"", perturbs.get(i)+""));
		}
		
		csv.writeToFile(file);
	}

	@Override
	public boolean isSatisfied(StopWatch watch, long iter, double[] energy, long numPerturbsKept) {
		if (energy[0] < Double.MAX_VALUE) {
			times.add(watch.getTime());
			iterations.add(iter);
			energies.add(energy);
			perturbs.add(numPerturbsKept);
		}
		if (criteria.isSatisfied(watch, iter, energy, numPerturbsKept)) {
			if (automaticFile != null) {
				System.out.println("Writing progress to file: "+automaticFile.getAbsolutePath());
				// write out results first
				try {
					writeFile(automaticFile);
				} catch (IOException e) {
					System.err.println("Error writing results file!");
					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return criteria.toString();
	}

	public ArrayList<Long> getTimes() {
		return times;
	}

	public ArrayList<Long> getIterations() {
		return iterations;
	}

	public ArrayList<Long> getPerturbs() {
		return perturbs;
	}

	public ArrayList<double[]> getEnergies() {
		return energies;
	}

}
