package scratch.kevin.simulators.erf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.jfree.data.Range;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotCurveCharacterstics;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.DataUtils;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;

import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TimeDepFSS_ERF_SimulatorPlot {

	public static void main(String[] args) throws IOException {
		int numTrialsPer = 2500;
		File dir = new File("/tmp/2013_10_21-erf-audit-cov-0.2");
		String prefix = "erf_audit_";
		
		List<File> erfFiles = Lists.newArrayList();
		List<File> simFiles = Lists.newArrayList();
		
		for (File file : dir.listFiles()) {
			String name = file.getName();
			if (!name.startsWith(prefix) || !name.endsWith(".bin"))
				continue;
			if (name.contains("sim_occur"))
				simFiles.add(file);
			else if (name.contains("erf_occur"))
				erfFiles.add(file);
		}
		Preconditions.checkState(erfFiles.size() == simFiles.size());
		
		double[] simulatorOccurances = null;
		double[] forecastOccurances = null;
		
		for (int i=0; i<erfFiles.size(); i++) {
			if (simulatorOccurances == null) {
				simulatorOccurances = MatrixIO.doubleArrayFromFile(simFiles.get(i));
				forecastOccurances = MatrixIO.doubleArrayFromFile(erfFiles.get(i));
			} else {
				double[] newSimulatorOccurances = MatrixIO.doubleArrayFromFile(simFiles.get(i));
				double[] newForecastOccurances = MatrixIO.doubleArrayFromFile(erfFiles.get(i));
				for (int j=0; j<simulatorOccurances.length; j++) {
					simulatorOccurances[j] += newSimulatorOccurances[j];
					forecastOccurances[j] += newForecastOccurances[j];
				}
			}
		}
		System.out.println("Loaded in "+simFiles.size()+" files");
		int numTrials = simFiles.size()*numTrialsPer;
		
		double[] simPerWindows = new double[simulatorOccurances.length];
		double[] erfPerWindows = new double[forecastOccurances.length];
		for (int i=0; i<simPerWindows.length; i++) {
			simPerWindows[i] = simulatorOccurances[i]/(double)numTrials;
			erfPerWindows[i] = forecastOccurances[i]/(double)numTrials;
		}
		Arrays.sort(simPerWindows);
		Arrays.sort(erfPerWindows);
		System.out.println("sim per. min="+simPerWindows[0]+"\tmax="+simPerWindows[simPerWindows.length-1]
				+"\tavg="+StatUtils.mean(simPerWindows)+"\tmedian="+DataUtils.median_sorted(simPerWindows));
		System.out.println("erf per. min="+erfPerWindows[0]+"\tmax="+simPerWindows[erfPerWindows.length-1]
				+"\tavg="+StatUtils.mean(erfPerWindows)+"\tmedian="+DataUtils.median_sorted(erfPerWindows));
		
		DefaultXY_DataSet ratioData = new DefaultXY_DataSet(simulatorOccurances, forecastOccurances);
		double max = StatUtils.max(simulatorOccurances);
		max = Math.max(max, StatUtils.max(forecastOccurances));
		ArbitrarilyDiscretizedFunc eventRatio = new ArbitrarilyDiscretizedFunc();
		eventRatio.set(0d, 0d);
		eventRatio.set(1e-1, 1e-1);
		eventRatio.set(max, max);
		
		List<XY_DataSet> elems = Lists.newArrayList();
		elems.add(ratioData);
		elems.add(eventRatio);
		List<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 1f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		
		double numOccur = StatUtils.sum(simulatorOccurances);
		double numForecast = StatUtils.sum(forecastOccurances);
		System.out.println("Tot sim occurences: "+numOccur);
		System.out.println("Tot predicted: "+numForecast);
		
		GraphWindow gw = new GraphWindow(elems, "Time Dependent Simulator Audit ("+numTrials+" sims)", chars);
		gw.setX_AxisLabel("Actual RSQSim Occurances (sum="+(float)numOccur+")");
		gw.setY_AxisLabel("FSS ERF Predicted Occurances (sum="+(float)numForecast+")");
		Range range = new Range(0, max);
		gw.setAxisRange(range, range);
		gw.setSize(1000, 800);
		gw.saveAsPNG("/tmp/fss_erf_audit.png");
		gw.setXLog(true);
		gw.setYLog(true);
		range = new Range(1d, max);
		gw.setAxisRange(range, range);
		gw.saveAsPNG("/tmp/fss_erf_audit_log.png");
		
		
	}

}
