package scratch.UCERF3.simulatedAnnealing.completion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;
import org.opensha.commons.data.CSVFile;

public class ProgressTrackingCompletionCriteria implements CompletionCriteria {
	
	private ArrayList<Long> times;
	private ArrayList<Long> iterations;
	private ArrayList<Double> energies;
	
	private CompletionCriteria criteria;
	
	private File automaticFile;
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria) {
		this(criteria, null);
	}
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria, File automaticFile) {
		times = new ArrayList<Long>();
		iterations = new ArrayList<Long>();
		energies = new ArrayList<Double>();
		
		this.criteria = criteria;
		this.automaticFile = automaticFile;
	}
	
	private static ArrayList<String> toLine(String val1, String val2, String val3) {
		ArrayList<String> ret = new ArrayList<String>(3);
		ret.add(val1);
		ret.add(val2);
		ret.add(val3);
		return ret;
	}
	
	public void writeFile(File file) throws IOException {
		CSVFile<String> csv = new CSVFile<String>(true);
		
		csv.addLine(toLine("Iterations", "Time (millis)", "Energy"));
		
		for (int i=0; i<times.size(); i++) {
			csv.addLine(toLine(iterations.get(i)+"", times.get(i)+"", energies.get(i)+""));
		}
		
		csv.writeToFile(file);
	}

	@Override
	public boolean isSatisfied(StopWatch watch, long iter, double energy) {
		if (energy < Double.MAX_VALUE) {
			times.add(watch.getTime());
			iterations.add(iter);
			energies.add(energy);
		}
		if (criteria.isSatisfied(watch, iter, energy)) {
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

}
