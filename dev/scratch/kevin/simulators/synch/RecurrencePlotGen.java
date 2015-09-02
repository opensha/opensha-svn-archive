package scratch.kevin.simulators.synch;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.gui.plot.PlotPreferences;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZGraphPanel;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotSpec;
import org.opensha.commons.gui.plot.jfreechart.xyzPlot.XYZPlotWindow;
import org.opensha.commons.mapping.gmt.elements.GMT_CPT_Files;
import org.opensha.commons.util.ComparablePairing;
import org.opensha.commons.util.DataUtils.MinMaxAveTracker;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.cpt.CPT;
import org.opensha.commons.util.cpt.CPTVal;
import org.opensha.sha.earthquake.param.MagDependentAperiodicityOptions;
import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.RectangularElement;
import org.opensha.sha.simulators.iden.RuptureIdentifier;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.utils.FaultSystemIO;
import scratch.kevin.simulators.MarkovChainBuilder;
import scratch.kevin.simulators.SimAnalysisCatLoader;
import scratch.kevin.simulators.SynchIdens;
import scratch.kevin.simulators.SynchIdens.SynchFaults;
import scratch.kevin.simulators.momRateVariation.UCERF3ComparisonAnalysis;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class RecurrencePlotGen {
	
	public enum DistanceMetric {
		L1_NORM {
			@Override
			public double calc(double[] state1, double[] state2) {
				Preconditions.checkArgument(state1.length == state2.length);
				double sum = 0d;
				for (int i=0; i<state1.length; i++)
					sum += Math.abs(state1[i] - state2[i]);
				return sum;
			}
		},
		L2_NORM {
			@Override
			public double calc(double[] state1, double[] state2) {
				Preconditions.checkArgument(state1.length == state2.length);
				double sum = 0d;
				for (int i=0; i<state1.length; i++)
					sum += Math.pow(state1[i] - state2[i], 2);
				return sum;
			}
		},
		LINFINITY_NORM {
			@Override
			public double calc(double[] state1, double[] state2) {
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
		
		public abstract double calc(double[] state1, double[] state2);
	}
	
	public static BitSet[] calcBitSet(List<double[]> fullPath, DistanceMetric distCalc, double threshold) {
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
	
	public static double[][] calcDist(List<double[]> fullPath, DistanceMetric distCalc) {
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
	
	public static double[][] calcRotated(List<double[]> fullPath, DistanceMetric distCalc, int width) {
		Preconditions.checkArgument(width % 2 == 1, "Width must be odd so that there's a point on the axis");
		
		long len = fullPath.size()*width;
		System.out.println("Calculating rotated for "+distCalc.name()+", size: "+fullPath.size()+" x "+width+" = "+len);
		
		int numBefore = width / 2;
		
		double[][] data = new double[fullPath.size()][width];
		
		MinMaxAveTracker track = new MinMaxAveTracker();
		
		for (int i=0; i<fullPath.size(); i++) {
			for (int n=0; n<width; n++) {
				int j = i - numBefore + n;
				double val;
				if (j < 0 || j >= fullPath.size()) {
					val = Double.NaN;
				} else {
					val = distCalc.calc(fullPath.get(i), fullPath.get(j));
					track.addValue(val);
				}
				data[i][n] = val;
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
	
	private static CPT discreteCPT;
	static {
		discreteCPT = new CPT();
		discreteCPT.setBelowMinColor(Color.WHITE);
		discreteCPT.add(new CPTVal(0f, Color.WHITE, 0.5f, Color.WHITE));
		discreteCPT.add(new CPTVal(0.5f, Color.BLACK, 1f, Color.BLACK));
		discreteCPT.setAboveMaxColor(Color.BLACK);
		discreteCPT.setNanColor(Color.GRAY);
	}
	
	public static void plotDiscrete(BitSet[] data, DistanceMetric distCalc, double threshold, File outputFile, int zoom,
			double distSpacing) 	throws IOException {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(
				data.length, data.length, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		for (int i=0; i<data.length; i++)
			for (int j=0; j<data.length; j++)
				if (data[i].get(j))
					xyz.set(i, j, 1d);
		
		String title = "Recurrence Plot, "+distCalc.name()+", thresh="+(float)threshold;
		plotSquare(xyz, title, discreteCPT, outputFile, zoom);
	}
	
	public static void plotContinuous(double[][] data, DistanceMetric distCalc, double maxZ, File outputFile, int zoom,
			double distSpacing) throws IOException {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(data, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().reverse();
		if (maxZ > 0)
			cpt = cpt.rescale(0d, maxZ);
		else
			cpt = cpt.rescale(0d, xyz.getMaxZ());
		String title = "Recurrence Plot, "+distCalc.name();
		
		plotSquare(xyz, title, cpt, outputFile, zoom);
	}
	
	public static void plotHybrid(double[][] data, DistanceMetric distCalc, double threshold, double maxZ,
			File outputFile, int zoom, double distSpacing) throws IOException {
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(data, 0.5*distSpacing, 0.5*distSpacing, distSpacing);
		CPT cpt = getHybridCPT(threshold, maxZ);
		String title = "Recurrence Plot, "+distCalc.name()+", thresh="+(float)threshold;
		
		plotSquare(xyz, title, cpt, outputFile, zoom);
	}
	
	private static void plotSquare(EvenlyDiscrXYZ_DataSet xyz, String title, CPT cpt, File outputFile, int zoom)
			throws IOException {
		XYZPlotSpec spec = new XYZPlotSpec(xyz, cpt, title, "Years", "Years", "");
		
		if (zoom <= 0)
			zoom = xyz.getNumX();
		double max = (zoom+0.5)*xyz.getGridSpacingX();
		XYZGraphPanel panel = new XYZGraphPanel();
		panel.drawPlot(spec, false, false, new Range(0, max), new Range(0, max));
		
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
	
	private static CPT getHybridCPT(double threshold, double maxZ) throws IOException {
		Preconditions.checkState(threshold > 0);
		Preconditions.checkState(maxZ > threshold);
		
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().reverse();
		cpt = cpt.rescale(0d, maxZ);
		cpt.setNanColor(Color.GRAY);
		
		// saturate
		for (CPTVal v : cpt) {
			v.minColor = saturate(v.minColor);
			v.maxColor = saturate(v.maxColor);
		}
		cpt.setAboveMaxColor(cpt.getMaxColor());
		
		// now threshold
		Color cAtThresh = cpt.getColor((float)threshold);
		for (int i=cpt.size(); --i >= 0;)
			if (cpt.get(i).start <= threshold)
				cpt.remove(i);
		Preconditions.checkState(!cpt.isEmpty(), "Threshold and maxZ too close!");
		cpt.add(0, new CPTVal((float)threshold, cAtThresh, cpt.get(0).start, cpt.get(0).minColor));
		cpt.add(0, new CPTVal(0f, Color.BLACK, (float)threshold, Color.BLACK));
		
		return cpt;
	}
	
	private static Color saturate(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		int saturationSteps = 2;
		
		for (int i=0; i<saturationSteps; i++) {
			r = (int)(0.5d*(r + 255d)+0.5);
			g = (int)(0.5d*(g + 255d)+0.5);
			b = (int)(0.5d*(b + 255d)+0.5);
		}
		
		return new Color(r, g, b);
	}
	
	private static boolean plot_rotated_preserve_min = true;
	
	public static void plotContinuousRotated(double[][] data, DistanceMetric distCalc, int widthEach,
			double maxZ, double distSpacing, File outputFile)
			throws IOException {
		CPT cpt = GMT_CPT_Files.MAX_SPECTRUM.instance().reverse();
		if (maxZ <= 0) {
			maxZ = 0;
			for (double[] vals : data)
				maxZ = Math.max(maxZ, StatUtils.max(vals));
		}
		cpt = cpt.rescale(0d, maxZ);
		cpt.setNanColor(Color.GRAY);
		
		plotRotated(data, distCalc, -1, cpt, widthEach, distSpacing, outputFile);
	}
	
	public static void plotDiscreteRotated(double[][] data, DistanceMetric distCalc, int widthEach,
			double threshold, double distSpacing, File outputFile) throws IOException {
		plotRotated(data, distCalc, threshold, discreteCPT, widthEach, distSpacing, outputFile);
	}
	
	public static void plotHybridRotated(double[][] data, DistanceMetric distCalc, int widthEach,
			double threshold, double maxZ, double distSpacing, File outputFile) throws IOException {
		plotRotated(data, distCalc, -1, getHybridCPT(threshold, maxZ), widthEach, distSpacing, outputFile);
	}
	
	private static void plotRotated(double[][] data, DistanceMetric distCalc, double threshold, CPT cpt, int widthEach,
			double distSpacing, File outputFile) throws IOException {
		List<double[][]> datas = Lists.newArrayList();
		datas.add(data);
		plotRotated(datas, null, distCalc, threshold, cpt, widthEach, distSpacing, outputFile);
	}
	
	private static void plotRotated(List<double[][]> datas, List<String> labels, DistanceMetric distCalc,
			double threshold, CPT cpt, int widthEach, double distSpacing, File outputFile) throws IOException {
		// now rotate to pixel space
		int shiftDist = widthEach / 2;
		int overlapEach = 1; // 0.5 on each end\
		
		Preconditions.checkState(widthEach % 2 == 1);
		Preconditions.checkState(widthEach >= 3);
		
		double cptTickUnit;
		if (cpt.getMaxValue() <= 1f)
			cptTickUnit = 0.1;
		else if (cpt.getMaxValue() <= 3f)
			cptTickUnit = 0.25;
		else
			cptTickUnit = (float)(Math.round(100d*cpt.getMaxValue()/100d)/10d);
		
		List<Range> yRanges = Lists.newArrayList();
		Range xRange = null;
		int plotWidth = 0;
		List<XYZPlotSpec> specs = Lists.newArrayList();
		
		for (int d=0; d<datas.size(); d++) {
			double[][] data = datas.get(d);
			int jWidth = data[0].length;
			
			int totalWidth = (data.length + data[0].length/2)*(widthEach - overlapEach) + widthEach;
			int totalHeight = (widthEach - shiftDist) + shiftDist*jWidth;
			
			double gridSpacingX = distSpacing*0.5d/shiftDist;
			double gridSpacingY = distSpacing/shiftDist;
			double deltaY = distSpacing*(jWidth+1d); // +1 to cover the height of each pixel, half on top and half on bottom
			double minY = -0.5*deltaY;
			
			double xOffset = distSpacing*0.25*(jWidth-1d);
//			double minX = -xOffset - 0.5;
			double minX = -xOffset;
			
			short[][] counts = new short[totalWidth][totalHeight];
			EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(totalWidth, totalHeight, minX, minY, gridSpacingX, gridSpacingY);
			// initialize to dummy val
			if (plot_rotated_preserve_min)
				for (int x=0; x<totalWidth; x++)
					for (int y=0; y<totalHeight; y++)
						xyz.set(x, y, Double.POSITIVE_INFINITY);
			
			for (int i=0; i<data.length; i++) {
				for (int j=0; j<data[i].length; j++) {
					// find center of this rotated pixel
					
					int negJ = (jWidth-1)-j;
					int y = shiftDist + shiftDist*negJ;
					
					int x = shiftDist + (widthEach-overlapEach)*i + shiftDist*j;
					
					double val = data[i][j];
					
					for (int yDist=0; yDist<=widthEach; yDist++) {
						int[] yAdds;
						if (yDist == 0)
							yAdds = new int[] {0};
						else
							yAdds = new int[] { yDist, -yDist};
						
						for (int yAdd : yAdds) {
							int xDelta = shiftDist - yDist;
							for (int xAdd=-xDelta; xAdd<=xDelta; xAdd++) {
								setRotated(xyz, counts, x+xAdd, y+yAdd, val);
							}
						}
					}
				}
			}
			
			// now post process
			for (int x=0; x<totalWidth; x++) {
				for (int y=0; y<totalHeight; y++) {
					if (counts[x][y] == 0)
						xyz.set(x, y, Double.NaN);
					else {
						if (counts[x][y] > 1 && !plot_rotated_preserve_min)
							xyz.set(x, y, xyz.get(x, y)/(double)counts[x][y]);
						if (threshold > 0) {
							// discrete plot
							double newVal;
							if (xyz.get(x, y) <= threshold)
								newVal = 1d;
							else
								newVal = 0d;
							xyz.set(x, y, newVal);
						}
					}
				}
			}
			
			String title = "Recurrence Plot, "+distCalc.name();
			XYZPlotSpec spec = new XYZPlotSpec(xyz, cpt, title, "Years", "Δ Years", "");
			
			yRanges.add(new Range(xyz.getMinY(), xyz.getMaxY()));
			int myPlotWidth = 100 + (int)(1.75d*(height-140d)*data.length/data[0].length);
			if (myPlotWidth > plotWidth)
				plotWidth = myPlotWidth;
			Range myXRange = new Range(-0.5d, xyz.getMaxX()-xOffset);
			if (xRange == null)
				xRange = myXRange;
			else
				xRange = new Range(Math.min(xRange.getLowerBound(), myXRange.getLowerBound()),
						Math.max(xRange.getUpperBound(), myXRange.getUpperBound()));
			
			spec.setCPTTickUnit(cptTickUnit);
			
			specs.add(spec);
		}
		
		if (labels != null) {
			Color transWhite = new Color(255, 255, 255, 170);
			Font font = new Font(Font.SANS_SERIF, Font.BOLD, 60);
			for (int i=0; i<specs.size(); i++) {
				String label = labels.get(i);
				
				double x = xRange.getLowerBound() + 0.05*(xRange.getUpperBound()-xRange.getLowerBound());
				Range yRange = yRanges.get(i);
				double y = yRange.getLowerBound() + 0.9*(yRange.getUpperBound()-yRange.getLowerBound());
				
				XYTextAnnotation ann = new XYTextAnnotation(label, x, y);
				ann.setFont(font);
				ann.setTextAnchor(TextAnchor.TOP_LEFT);
				ann.setPaint(Color.BLACK);
				ann.setBackgroundPaint(transWhite);
				
				List<XYTextAnnotation> annotations = Lists.newArrayList(ann);
				specs.get(i).setPlotAnnotations(annotations);
			}
		}
		
//		if (zoom <= 0)
//			zoom = data.length;
		PlotPreferences prefs = XYZGraphPanel.getDefaultPrefs();
		prefs.setPlotLabelFontSize(50);
		prefs.setAxisLabelFontSize(40);
		prefs.setTickLabelFontSize(25);
		XYZGraphPanel panel = new XYZGraphPanel(prefs);
		panel.drawPlot(specs, false, false, Lists.newArrayList(xRange), yRanges);
		
		if (outputFile == null) {
			// display it
			XYZPlotWindow window = new XYZPlotWindow(panel);
			window.setSize(width, (height-140)/4+140);
			window.setDefaultCloseOperation(XYZPlotWindow.EXIT_ON_CLOSE);
		} else {
			// write plot
			int myHeight = height;
			if (specs.size() > 0)
				myHeight = (height-200)*specs.size()+500;
			panel.getChartPanel().setSize(plotWidth, myHeight);
			panel.saveAsPNG(outputFile.getAbsolutePath());
		}
	}
	
	private static void setRotated(EvenlyDiscrXYZ_DataSet xyz, short[][] counts, int x, int y, double val) {
		if (Double.isNaN(val))
			return;
		counts[x][y]++;
		if (plot_rotated_preserve_min)
			val = Math.min(val, xyz.get(x, y));
		else
			val = val + xyz.get(x, y);
		xyz.set(x, y, val);
	}

	public static void main(String[] args) throws IOException, DocumentException {
		File outputDir = new File("/home/kevin/Simulators/recurrence_plots");
		
		List<SynchFaults[]> faultSets = Lists.newArrayList();
		
		faultSets.add(new SynchFaults[] {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAF_CARRIZO});
		faultSets.add(new SynchFaults[] {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_CARRIZO, SynchFaults.SAF_COACHELLA,
				SynchFaults.SAF_CHOLAME, SynchFaults.SAN_JACINTO});
		
		File ucerf3MainDir = new File("/home/kevin/Simulators/time_series/ucerf3_compare");
		MagDependentAperiodicityOptions[] ucerf3Comparisons = {MagDependentAperiodicityOptions.MID_VALUES};
		String[] ucerf3DirNames = {"2015_07_30-MID_VALUES"};
		
//		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAF_CARRIZO};
//		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO};
//		SynchFaults[] faults = {SynchFaults.SAF_COACHELLA, SynchFaults.SAN_JACINTO};
//		SynchFaults[] faults = {SynchFaults.SAF_CARRIZO, SynchFaults.SAF_CHOLAME};
//		SynchFaults[] faults = {SynchFaults.SAF_MOJAVE, SynchFaults.SAF_CARRIZO, SynchFaults.SAF_COACHELLA,
//									SynchFaults.SAF_CHOLAME, SynchFaults.SAN_JACINTO};
//		List<RuptureIdentifier> rupIdens = SynchIdens.getStandardSoCal();
		
		double distSpacing = 10d;
		boolean normalize = true;
		
		boolean[] poissons = { false, true };
		
		SimAnalysisCatLoader loader = new SimAnalysisCatLoader(true, null, true);
		List<EQSIM_Event> events = loader.getEvents();
		List<RectangularElement> elems = loader.getElements();
		
		List<List<List<EQSIM_Event>>> ucerf3Catalogs = null;
		FaultSystemSolution ucerf3Sol = null;
		Map<Integer, RectangularElement> ucerf3Elems = null;
		int ucerf3StartYear = 2014;
		int numUCERF3Catalogs = 5;
		if (ucerf3Comparisons != null && ucerf3Comparisons.length > 0) {
			ucerf3Catalogs = Lists.newArrayList();
			ucerf3Sol = FaultSystemIO.loadSol(new File("/home/kevin/workspace/OpenSHA/dev/scratch/"
					+ "UCERF3/data/scratch/InversionSolutions/"
					+ "2013_05_10-ucerf3p3-production-10runs_COMPOUND_SOL_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
			ucerf3Elems = UCERF3ComparisonAnalysis.loadElements(ucerf3Sol.getRupSet());
			for (int i=0; i<ucerf3Comparisons.length; i++) {
				if (ucerf3Comparisons[i] == null) {
					// poisson
					List<EQSIM_Event> cat = UCERF3ComparisonAnalysis.calcPoissonCatalog(ucerf3Sol, ucerf3Elems, 30000);
					List<List<EQSIM_Event>> myCatalogs = Lists.newArrayList();
					myCatalogs.add(cat);
					ucerf3Catalogs.add(myCatalogs);
				} else {
					File ucerf3Dir = new File(ucerf3MainDir, ucerf3DirNames[i]);
					List<List<EQSIM_Event>> myCatalogs = UCERF3ComparisonAnalysis.loadUCERF3Catalogs(
							ucerf3Dir, ucerf3Sol, null, ucerf3Elems, ucerf3StartYear);
					List<Double> durations = Lists.newArrayList();
					for (List<EQSIM_Event> catalog : myCatalogs)
						durations.add(UCERF3ComparisonAnalysis.getDurationYears(catalog));
					
					// sort by duration decreasing
					myCatalogs = ComparablePairing.getSortedData(durations, myCatalogs);
					Collections.reverse(myCatalogs);
					
					if (myCatalogs.size() > numUCERF3Catalogs)
						myCatalogs = myCatalogs.subList(0, numUCERF3Catalogs);
					
					ucerf3Catalogs.add(myCatalogs);
				}
			}
		}
		
		List<DistanceMetric> metrics = Lists.newArrayList();
		List<Double> thresholds = Lists.newArrayList();
		
		populateDefaultMetrics(metrics, thresholds);
		
		for (SynchFaults[] faults : faultSets) {
			List<RuptureIdentifier> rupIdens = SynchIdens.getIndividualFaults(7, 10d, faults);
			
			String faultNames = Joiner.on("_").join(Lists.newArrayList(faults));
			
			List<List<int[]>> comboPlotPaths = Lists.newArrayList();
			List<String> comboPlotNames = Lists.newArrayList();
			
			for (boolean poisson : poissons) {
				File subDir;
				if (poisson)
					subDir = new File(outputDir, "rsqsim_poisson");
				else
					subDir = new File(outputDir, "rsqsim_standard");
				Preconditions.checkState(subDir.exists() || subDir.mkdir());
				
				List<EQSIM_Event> myEvents;
				
				if (poisson) {
					List<EQSIM_Event> trueRandom = Lists.newArrayList();
					double start = events.get(0).getTime();
					double end = events.get(events.size()-1).getTime();
					int id = 0;
					for (EQSIM_Event e : events)
						trueRandom.add(e.cloneNewTime((end-start)*Math.random()+start, id++));
					Collections.sort(trueRandom);
					myEvents = trueRandom;
				} else {
					myEvents = events;
				}
				
				File mySubDir = new File(subDir, faultNames);
				Preconditions.checkState(mySubDir.exists() || mySubDir.mkdir());
				
				List<int[]> fullPath = MarkovChainBuilder.getStatesPath(distSpacing, myEvents, rupIdens, 0d);
				// skip the first 10,000 years while things are getting ramped up
				fullPath = fullPath.subList(1000, fullPath.size());
//				System.out.println("Path length: "+fullPath.size());
				
				plotRecurrence(mySubDir, fullPath, distSpacing, normalize, metrics, thresholds);
				
				comboPlotPaths.add(fullPath);
				if (poisson)
					comboPlotNames.add("RSQSim Poisson");
				else
					comboPlotNames.add("RSQSim");
			}
			
			if (ucerf3Catalogs != null) {
				List<RuptureIdentifier> ucerf3Idens = UCERF3ComparisonAnalysis.buildUCERF3_EquivIdens(
						rupIdens, elems, ucerf3Elems, ucerf3Sol.getRupSet());
				for (int i=0; i<ucerf3Catalogs.size(); i++) {
					List<List<EQSIM_Event>> catalogs = ucerf3Catalogs.get(i);
					
					MagDependentAperiodicityOptions cov = ucerf3Comparisons[i];
					File subDir;
					if (cov == null)
						subDir = new File(outputDir, "ucerf3_poisson");
					else
						subDir = new File(outputDir, "ucerf3_"+cov.name());
					Preconditions.checkState(subDir.exists() || subDir.mkdir());
					
					subDir = new File(subDir, faultNames);
					if (subDir.exists())
						FileUtils.deleteRecursive(subDir);
					Preconditions.checkState(subDir.mkdir());
					
					List<EQSIM_Event> preEvents = null;
					int skipStates = 0;
					
					if (cov != null) {
						preEvents = UCERF3ComparisonAnalysis.getFakePreEvents(
								ucerf3Sol.getRupSet(), ucerf3Idens, ucerf3Elems, ucerf3StartYear);
						double oiBefore = preEvents.get(preEvents.size()-1).getTimeInYears();
						Preconditions.checkState(oiBefore < 0);
						skipStates = (int)(-oiBefore/distSpacing + 0.5);
						System.out.println("Fake event OI established at "+oiBefore
								+" years, skipping "+skipStates+" states");
					}
					
					List<List<int[]>> catalogPaths = Lists.newArrayList();
					List<String> catalogNames = Lists.newArrayList();
					
					for (int j=0; j<catalogs.size(); j++) {
						List<EQSIM_Event> catalog = catalogs.get(j);
						double origCatLen = UCERF3ComparisonAnalysis.getDurationYears(catalog);
						if (preEvents != null) {
							catalog.addAll(0, preEvents);
							double newCatLen = catalog.get(catalog.size()-1).getTimeInYears() - catalog.get(0).getTimeInYears();
							double addedYears = newCatLen - origCatLen;
							int approxAddedStates = (int)(addedYears/distSpacing + 0.5);
							System.out.println("Catalog "+j+" lenght: "+origCatLen);
							System.out.println("Length with pre events: "+newCatLen+", added "
									+addedYears+" ("+approxAddedStates+" states)");
						}
						
						List<int[]> fullPath = MarkovChainBuilder.getStatesPath(
								distSpacing, catalog, ucerf3Idens, 0);
						fullPath = fullPath.subList(skipStates, fullPath.size());
						
						catalogPaths.add(fullPath);
						String name;
						if (cov == null)
							name = "UCERF3 Poisson";
						else
							name = "UCERF3 "+cov.name().replaceAll("_", " ")
								.replaceAll("VALUES", "COV");
						catalogNames.add(name+" Catalog "+j);
						
						if (j == 0) {
							comboPlotPaths.add(fullPath);
							comboPlotNames.add(name);
						}
						
						File mySubDir;
						if (catalogs.size() > 1) {
							mySubDir = new File(subDir, "catalog"+j);
							Preconditions.checkState(mySubDir.exists() || mySubDir.mkdir());
						} else {
							mySubDir = subDir;
						}
						
//						fullPath = fullPath.subList(0, maxStates);
						
						RecurrencePlotGen.plotRecurrence(mySubDir, fullPath, distSpacing, normalize,
								metrics, thresholds);
					}
					// now all catalogs combined
					if (catalogPaths.size() > 1) {
						for (int m=0; m<metrics.size(); m++) {
							plotMultiRecurrence(distSpacing, normalize, metrics.get(m),
									thresholds.get(m), catalogPaths, catalogNames, subDir);
						}
					}
				}
			}
			
			// now combo plots
			File comboDir = new File(outputDir, "combined_plots");
			Preconditions.checkState(comboDir.exists() || comboDir.mkdir());
			comboDir = new File(comboDir, faultNames);
			Preconditions.checkState(comboDir.exists() || comboDir.mkdir());
			
			for (int i=0; i<metrics.size(); i++) {
				plotMultiRecurrence(distSpacing, normalize, metrics.get(i),
						thresholds.get(i), comboPlotPaths, comboPlotNames, comboDir);
			}
		}
	}

	private static void plotMultiRecurrence(double distSpacing, boolean normalize, DistanceMetric metric,
			double threshold, List<List<int[]>> comboPlotPaths, List<String> comboPlotNames, File comboDir)
					throws IOException {
		int startIndex = 0;
		int rotatedEndIndex = 500;
		for (List<int[]> fullPath : comboPlotPaths)
			if (fullPath.size() < rotatedEndIndex)
				rotatedEndIndex = fullPath.size();
		int rotatedWidth = 151;
		int rotatedPixelWidth = 9;
		if (normalize)
			threshold /= 10d;
		String threshStr = "thresh";
		if (normalize)
			threshStr += "Norm";
		double maxZ = threshold*5d;
		threshStr += (float)threshold;
		
		CPT cpt = getHybridCPT(threshold, maxZ);
		
		List<double[][]> datas = Lists.newArrayList();
		
		for (int j=0; j<comboPlotPaths.size(); j++) {
			List<int[]> fullPath = comboPlotPaths.get(j);
			
			List<double[]> rotatedSubPath = calcNormalizedPath(
					fullPath.subList(startIndex, rotatedEndIndex), calcMeanRIs(fullPath));
			
			// now rotated
			double[][] rotatedData = calcRotated(rotatedSubPath, metric, rotatedWidth);
			datas.add(rotatedData);
		}
		
		String name = "rp_"+metric.name()+"_hybrid_"+threshStr+"_rotated.png";
		plotRotated(datas, comboPlotNames, metric, -1, cpt, rotatedPixelWidth, distSpacing,
				new File(comboDir, name));
	}
	
	public static List<double[]> toDoublePath(List<int[]> fullPath) {
		List<double[]> doubles = Lists.newArrayList();
		
		for (int[] state : fullPath) {
			double[] newState = new double[state.length];
			for (int i=0; i<state.length; i++)
				newState[i] = state[i];
			doubles.add(newState);
		}
		
		return doubles;
	}
	
	public static double[] calcMeanRIs(List<int[]> path) {
		List<List<Double>> ris = Lists.newArrayList();
		
		for (int i=0; i<path.get(0).length; i++)
			ris.add(new ArrayList<Double>());
		
		for (int i=1; i<path.size(); i++) {
			int[] state = path.get(i);
			for (int j=0; j<state.length; j++) {
				if (state[j] == 0) {
					// just ruptured, add previous state
					ris.get(j).add((double)path.get(i-1)[j]);
				}
			}
		}
		
		double[] meanRIs = new double[ris.size()];
		
		for (int i=0; i<meanRIs.length; i++)
			meanRIs[i] = StatUtils.mean(Doubles.toArray(ris.get(i)));
		
		return meanRIs;
	}
	
	public static List<double[]> calcNormalizedPath(List<int[]> path, double[] meanRIs) {
		List<double[]> ret = Lists.newArrayList();
		
		for (int[] state : path) {
			double[] doubles = new double[state.length];
			
			for (int i=0; i<state.length; i++)
				doubles[i] = state[i]/meanRIs[i];
			
			ret.add(doubles);
		}
		
		return ret;
	}
	
	private static void populateDefaultMetrics(List<DistanceMetric> metrics, List<Double> thresholds) {
		// units here are un normalized, will be converted assuming meanRI = 100;
		metrics.add(DistanceMetric.LINFINITY_NORM);
		thresholds.add(2d);

		//				metrics.add(DistanceMetric.L2_NORM);
		//				thresholds.add(100d);

		metrics.add(DistanceMetric.L1_NORM);
		thresholds.add(5d);
	}

	public static void plotRecurrence(File outputDir, List<int[]> fullPath, double distSpacing,
			boolean normalize) throws IOException {
		List<DistanceMetric> metrics = Lists.newArrayList();
		List<Double> thresholds = Lists.newArrayList();
		
		populateDefaultMetrics(metrics, thresholds);
		
		plotRecurrence(outputDir, fullPath, distSpacing, normalize, metrics, thresholds);
	}

	public static void plotRecurrence(File outputDir, List<int[]> fullPath, double distSpacing,
			boolean normalize, List<DistanceMetric> metrics, List<Double> thresholds) throws IOException {
//		File rotatedDir = new File(outputDir, "rotated");
//		Preconditions.checkState(rotatedDir.exists() || rotatedDir.mkdir());
		
		int startIndex = 0;
		int squareEndIndex = 1000;
		int squareZoomLevel = 300;
		int rotatedEndIndex = 3000;
		
		if (squareEndIndex >= fullPath.size())
			squareEndIndex = fullPath.size()-1;
		if (rotatedEndIndex >= fullPath.size())
			rotatedEndIndex = fullPath.size()-1;
		
//		int rotatedWidth = 51;
		int rotatedWidth = 151;
		int rotatedPixelWidth = 9;
		
//		List<int[]> squareSubPath = fullPath.subList(startIndex, squareEndIndex);
//		List<int[]> rotatedSubPath = fullPath.subList(startIndex, rotatedEndIndex);
		
		List<double[]> squareSubPath;
		List<double[]> rotatedSubPath;
		
		if (normalize) {
			double[] ris = calcMeanRIs(fullPath);
			squareSubPath = calcNormalizedPath(fullPath.subList(startIndex, squareEndIndex), ris);
			rotatedSubPath = calcNormalizedPath(fullPath.subList(startIndex, rotatedEndIndex), ris);
		} else {
			squareSubPath = toDoublePath(fullPath.subList(startIndex, squareEndIndex));
			rotatedSubPath = toDoublePath(fullPath.subList(startIndex, rotatedEndIndex));
		}
		
		for (int i=0; i<metrics.size(); i++) {
			DistanceMetric metric = metrics.get(i);
			double threshold = thresholds.get(i);
			if (normalize)
				threshold /= 10d;
			
//			double[][] dists = calcDist(squareSubPath, metric);
//			BitSet[] bitSets = distToBitSet(dists, threshold);
			
			String threshStr = "thresh";
			if (normalize)
				threshStr += "Norm";
			threshStr += (float)threshold;
			
			double maxZ = threshold*5d;
			
//			for (int zoom : new int[] { -1, squareZoomLevel }) {
//				
//				String name = "rp_"+metric.name()+"_"+threshStr;
//				if (zoom > 0)
//					name += "_zoom"+zoom;
//				name += ".png";
//				plotDiscrete(bitSets, metric, threshold, new File(outputDir, name), zoom, distSpacing);
//				
//				name = "rp_"+metric.name()+"_continuous";
//				if (zoom > 0)
//					name += "_zoom"+zoom;
//				name += ".png";
//				plotContinuous(dists, metric, maxZ, new File(outputDir, name), zoom, distSpacing);
//				
//				String name = "rp_"+metric.name()+"_hybird_"+threshStr;
//				if (zoom > 0)
//					name += "_zoom"+zoom;
//				name += ".png";
//				plotHybrid(dists, metric, threshold, maxZ, new File(outputDir, name), zoom, distSpacing);
//			}
//			
//			dists = null;
//			bitSets = null;
			
			// now rotated
			double[][] rotatedData = calcRotated(rotatedSubPath, metric, rotatedWidth);
			
//			String name = "rp_"+metric.name()+"_"+threshStr+"_rotated.png";
//			plotDiscreteRotated(rotatedData, metric, rotatedPixelWidth, threshold, distSpacing, new File(rotatedDir, name));
//			
//			name = "rp_"+metric.name()+"_continuous_rotated.png";
//			plotContinuousRotated(rotatedData, metric, rotatedPixelWidth, maxZ, distSpacing, new File(rotatedDir, name));
			
			String name = "rp_"+metric.name()+"_hybrid_"+threshStr+"_rotated.png";
			plotHybridRotated(rotatedData, metric, rotatedPixelWidth, threshold, maxZ, distSpacing, new File(outputDir, name));
		}
	}

}
