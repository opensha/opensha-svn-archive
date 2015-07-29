package scratch.kevin.simulators.synch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;

import org.jfree.data.Range;
import org.opensha.commons.data.xyz.ArbDiscrXYZ_DataSet;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import scratch.kevin.simulators.MarkovChainBuilder;
import scratch.kevin.simulators.PeriodicityPlotter;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;
import scratch.kevin.simulators.SynchIdens.SynchFaults;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class RecurrencePlotGen {
	
	public enum DistanceMetric {
		L1_NORM {
			@Override
			public double calc(int[] state1, int[] state2) {
				Preconditions.checkArgument(state1.length == state2.length);
				double sum = 0d;
				for (int i=0; i<state1.length; i++)
					sum += Math.abs(state1[i] - state2[i]);
				return sum;
			}
		},
		L2_NORM {
			@Override
			public double calc(int[] state1, int[] state2) {
				Preconditions.checkArgument(state1.length == state2.length);
				double sum = 0d;
				for (int i=0; i<state1.length; i++)
					sum += Math.pow(state1[i] - state2[i], 2);
				return sum;
			}
		},
		LINFINITY_NORM {
			@Override
			public double calc(int[] state1, int[] state2) {
				Preconditions.checkArgument(state1.length == state2.length);
				double ret = 0d;
				for (int i=0; i<state1.length; i++) {
					double diff = Math.abs(state1[i] - state2[i]);
					if (diff > ret)
						ret = diff;
				}
				return ret;
			}
		};
		
		public abstract double calc(int[] state1, int[] state2);
	}
	
	public static BitSet[] calcBitSet(List<int[]> fullPath, DistanceMetric distCalc, double threshold) {
		System.out.println("Calculating for "+distCalc.name()+", thresh="+threshold);
		BitSet[] ret = new BitSet[fullPath.size()];
		for (int i=0; i<fullPath.size(); i++)
			ret[i] = new BitSet(fullPath.size());
		
		MinMaxAveTracker track = new MinMaxAveTracker();
		int numBelow = 0;
		int tot = 0;
		
		boolean below;
		for (int i=0; i<fullPath.size(); i++) {
			for (int j=0; j<fullPath.size(); j++) {
				double val = distCalc.calc(fullPath.get(i), fullPath.get(j));
				track.addValue(val);
				below = val <= threshold;
				if (below) {
					numBelow++;
					ret[i].set(j);
				}
				tot++;
			}
		}
		
		double percent = 100d*numBelow/(double)tot;
		System.out.println(numBelow+"/"+tot+" below threshold of "+threshold+" ("+(float)percent+" %)");
		System.out.println(track);
		
		return ret;
	}
	
	public static double[][] calcDist(List<int[]> fullPath, DistanceMetric distCalc) {
		System.out.println("Calculating for "+distCalc.name());
		double[][] data = new double[fullPath.size()][fullPath.size()];
		
		MinMaxAveTracker track = new MinMaxAveTracker();
		
		for (int i=0; i<fullPath.size(); i++) {
			for (int j=0; j<fullPath.size(); j++) {
				double val = distCalc.calc(fullPath.get(i), fullPath.get(j));
				track.addValue(val);
				data[i][j] = val;
			}
		}
		
		System.out.println(track);
		
		return data;
	}
	
	private static BitSet[] distToBitSet(double[][] data, double threshold) {
		BitSet[] ret = new BitSet[data.length];
		for (int i=0; i<data.length; i++)
			ret[i] = new BitSet(data.length);
		
		for (int i=0; i<data.length; i++)
			for (int j=0; j<data.length; j++)
				if (data[i][j] <= threshold)
					ret[i].set(j);
		
		return ret;
	}
	
	private static int width = 650;
	private static int height = 700;
	
	public static void plotDiscrete(BitSet[] data, DistanceMetric distCalc, double threshold, File outputFile, int zoom)
			throws IOException {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(data.length, data.length, 0.5, 0.5, 1d);
		for (int i=0; i<data.length; i++)
			for (int j=0; j<data.length; j++)
				if (data[i].get(j))
					xyz.set(i, j, 1d);
		CPT cpt = new CPT();
		cpt.setBelowMinColor(Color.WHITE);
		cpt.add(new CPTVal(0f, Color.WHITE, 0.5f, Color.WHITE));
		cpt.add(new CPTVal(0.5f, Color.BLACK, 1f, Color.BLACK));
		cpt.setAboveMaxColor(Color.BLACK);
		
		String title = "Recurrence Plot, "+distCalc.name()+", thresh="+(float)threshold;
		XYZPlotSpec spec = new XYZPlotSpec(xyz, cpt, title, "Time Index", "Time Index", "");
		
		if (zoom <= 0)
			zoom = data.length;
		XYZGraphPanel panel = new XYZGraphPanel();
		panel.drawPlot(spec, false, false, new Range(0, zoom+0.5), new Range(0, zoom+0.5));
		
		if (outputFile == null) {
			// display it
			XYZPlotWindow window = new XYZPlotWindow(panel);
			window.setSize(width, height);
			window.setDefaultCloseOperation(XYZPlotWindow.EXIT_ON_CLOSE);
		} else {
			// write plot
			panel.getChartPanel().setSize(width, height);
			panel.saveAsPNG(outputFile.getAbsolutePath());
		}
	}
	
	public static void plotContinuous(double[][] data, DistanceMetric distCalc, double maxZ, File outputFile, int zoom)
			throws IOException {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(data, 0.5, 0.5, 1d);
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().reverse();
		if (maxZ > 0)
			cpt = cpt.rescale(0d, maxZ);
		else
			cpt = cpt.rescale(0d, xyz.getMaxZ());
		
		String title = "Recurrence Plot, "+distCalc.name();
		XYZPlotSpec spec = new XYZPlotSpec(xyz, cpt, title, "Time Index", "Time Index", "");
		
		if (zoom <= 0)
			zoom = data.length;
		XYZGraphPanel panel = new XYZGraphPanel();
		panel.drawPlot(spec, false, false, new Range(0, zoom+0.5), new Range(0, zoom+0.5));
		
		if (outputFile == null) {
			// display it
			XYZPlotWindow window = new XYZPlotWindow(panel);
			window.setSize(width, height);
			window.setDefaultCloseOperation(XYZPlotWindow.EXIT_ON_CLOSE);
		} else {
			// write plot
			panel.getChartPanel().setSize(width, height);
			panel.saveAsPNG(outputFile.getAbsolutePath());
		}
	}

	public static void main(String[] args) throws IOException {
		File outputDir = new File("/home/kevin/Simulators/recurrence_plots");
		
//		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAF_CARRIZO};
//		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO};
//		SynchFaults[] faults = {SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO};
//		SynchFaults[] faults = {SynchFaults.SAF_CARRIZO, SynchFaults.SAF_CHOLAME};
		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_CARRIZO, SynchFaults.SAF_COACHELLA,
									SynchFaults.SAF_CHOLAME, SynchFaults.SAN_JACINTO};
//		List<RuptureIdentifier> rupIdens = SynchIdens.getStandardSoCal();
		String dirName = Joiner.on("_").join(Lists.newArrayList(faults));
		File subDir = new File(outputDir, dirName);
		Preconditions.checkState(subDir.exists() || subDir.mkdir());
		
		List<RuptureIdentifier> rupIdens = SynchIdens.getIndividualFaults(7, 10d, faults);
		double distSpacing = 10d;
		List<EQSIM_Event> events = new SimAnalysisCatLoader(true, rupIdens, true).getEvents();
		
		List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, events, rupIdens, 0d);
		System.out.println("Path length: "+fullPath.size());
		
		List<int[]> subPath = fullPath.subList(1000, 2000);
		int zoomLevel = 300;
		
		List<DistanceMetric> metrics = Lists.newArrayList();
		List<Double> thresholds = Lists.newArrayList();
		
		metrics.add(DistanceMetric.LINFINITY_NORM);
		thresholds.add(5d);
		
		metrics.add(DistanceMetric.L2_NORM);
		thresholds.add(100d);
		
		metrics.add(DistanceMetric.L1_NORM);
		thresholds.add(5d);
		
		for (int i=0; i<metrics.size(); i++) {
			DistanceMetric metric = metrics.get(i);
			double threshold = thresholds.get(i);
			
			double[][] dists = calcDist(subPath, metric);
			BitSet[] bitSets = distToBitSet(dists, threshold);
			
			for (int zoom : new int[] { -1, zoomLevel }) {
				
				String name = "rp_"+metric.name()+"_thresh"+(float)threshold;
				if (zoom > 0)
					name += "_zoom"+zoom;
				name += ".png";
				plotDiscrete(bitSets, metric, threshold, new File(subDir, name), zoom);
				
				name = "rp_"+metric.name()+"_continuous";
				if (zoom > 0)
					name += "_zoom"+zoom;
				name += ".png";
				double maxZ = threshold*5d;
				plotContinuous(dists, metric, maxZ, new File(subDir, name), zoom);
			}
		}
	}

}
