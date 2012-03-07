package scratch.UCERF3.simulatedAnnealing.completion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.time.StopWatch;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;

import com.google.common.collect.Lists;

public class ProgressTrackingCompletionCriteria implements CompletionCriteria {
	
	private ArrayList<Long> times;
	private ArrayList<Long> iterations;
	private ArrayList<Long> perturbs;
	private ArrayList<double[]> energies;
	
	private CompletionCriteria criteria;
	
	private long autoPlotMillis;
	private long nextPlotMillis;
	private GraphiWindowAPI_Impl gw;
	private ArrayList<ArbitrarilyDiscretizedFunc> funcs;
	
	private File automaticFile;
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria) {
		this(criteria, null, 0);
	}
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria, double autoPlotMins) {
		this(criteria, null, autoPlotMins);
	}
	
	public ProgressTrackingCompletionCriteria(CompletionCriteria criteria, File automaticFile) {
		this(criteria, automaticFile, 0);
	}
	
	public ProgressTrackingCompletionCriteria(
			CompletionCriteria criteria, File automaticFile, double autoPlotMins) {
		times = new ArrayList<Long>();
		iterations = new ArrayList<Long>();
		energies = new ArrayList<double[]>();
		perturbs = new ArrayList<Long>();
		
		this.criteria = criteria;
		this.automaticFile = automaticFile;
		if (autoPlotMins > 0) {
			this.autoPlotMillis = (long)(autoPlotMins * 60d * 1000d);
			this.nextPlotMillis = autoPlotMillis;
		} else {
			this.autoPlotMillis = 0;
			this.nextPlotMillis = -1;
		}
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
		if (autoPlotMillis > 0 && watch.getTime() > nextPlotMillis) {
			try {
				updatePlot();
			} catch (Throwable t) {
				// you never want a plot error to stop an inversion!
				t.printStackTrace();
			}
			nextPlotMillis = watch.getTime() + autoPlotMillis;
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
	
	private void updatePlot() {
		if (energies.isEmpty())
			return;
		if (gw == null) {
			funcs = new ArrayList<ArbitrarilyDiscretizedFunc>();
			funcs.add(new ArbitrarilyDiscretizedFunc("Total Energy"));
			funcs.add(new ArbitrarilyDiscretizedFunc("Equality Energy"));
			funcs.add(new ArbitrarilyDiscretizedFunc("Entropy Energy"));
			funcs.add(new ArbitrarilyDiscretizedFunc("Inequality Energy"));
			ArrayList<PlotCurveCharacterstics> chars = ThreadedSimulatedAnnealing.getEnergyBreakdownChars();
			updatePlotFuncs();
			gw = new GraphiWindowAPI_Impl(funcs, "Energy vs Iterations", chars);
		} else {
			updatePlotFuncs();
			gw.refreshPlot();
		}
	}
	
	private void updatePlotFuncs() {
		int start = funcs.get(0).getNum();
		for (int i=start; i<energies.size(); i++) {
			long iter = iterations.get(i);
			double[] energy = energies.get(i);
			for (int j=0; j<energy.length; j++)
				funcs.get(j).set((double)iter, energy[j]);
		}
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
