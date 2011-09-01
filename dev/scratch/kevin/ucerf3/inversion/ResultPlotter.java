package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

public class ResultPlotter {
	
	private static final int max_curve_pts = 800;
	private static final double pDiffMins = 5;
	private static final boolean do_extrap = true;
	
	private static final String time_label = "Time (minutes)";
	private static final String serial_time_label = "Serial Time (minutes)";
	private static final String parallel_time_label = "Parallel Time (minutes)";
	private static final String energy_label = "Energy";
	private static final String iterations_label = "Iterations";
	private static final String time_speedup_label = "Time (serial) / Time (parallel)";
	private static final String energy_speedup_label = "Energy (serial) / Energy (parallel)";
	private static final String std_dev_label = "Std. Dev.";
	private static final String improvement_label = "% Improvement";
	
	protected static final String avg_energy_vs_time_title = "Averaged Energy Vs Time (m)";
	protected static final String improvement_vs_time_title = "% Improvement (over "+pDiffMins+" min invervals)";
	protected static final String time_speedup_vs_energy_title = "Time Speedup Vs Energy";
	protected static final String time_speedup_vs_time_title = "Time Speedup Vs Time";
	protected static final String time_comparison_title = "Serial Time Vs Parallel Time";
	protected static final String energy_speedup_vs_time_title = "Energy Speedup Vs Time";
	protected static final String energy_vs_iterations_title = "Energy Vs Iterations";
	protected static final String energy_vs_time_title = "Energy Vs Time (m)";
	protected static final String std_dev_vs_time_title = "Std. Dev. Vs Time (m)";
	protected static final String iterations_vs_time_title = "Iterations Vs Time (m)";
	
	private static ArbitrarilyDiscretizedFunc[] loadCSV (
			File file, int targetPPM) throws IOException {
		String name = file.getName();
		CSVFile<String> csv = CSVFile.readFile(file, true);
		ArbitrarilyDiscretizedFunc energyVsIter = new ArbitrarilyDiscretizedFunc();
		energyVsIter.setName("Energy Vs Iterations ("+name+")");
		energyVsIter.setYAxisName("Energy");
		energyVsIter.setXAxisName("Iterations");
		ArbitrarilyDiscretizedFunc energyVsTime = new ArbitrarilyDiscretizedFunc();
		energyVsTime.setName("Energy Over Time ("+name+")");
		energyVsTime.setYAxisName("Energy");
		energyVsTime.setXAxisName("Time (m)");
		ArbitrarilyDiscretizedFunc iterVsTime = new ArbitrarilyDiscretizedFunc();
		iterVsTime.setName("Iterations Over Time ("+name+")");
		iterVsTime.setYAxisName("Iterations");
		iterVsTime.setXAxisName("Time (m)");
		
		System.out.print("Loading: "+name+" ...");
		
		int rows = csv.getNumRows();
		long totMilis = Long.parseLong(csv.getLine(rows-1).get(1));
		double totMins = totMilis / 1000d / 60d;
		double targetPts = targetPPM * totMins;
		if (targetPts > max_curve_pts)
			targetPts = max_curve_pts;
		double newMod = (double)rows/targetPts;
		
		int mod = (int)(newMod + 0.5);
		
		for (int i=1; i<rows; i++) {
			if (i % mod > 0)
				continue;
			
			List<String> line = csv.getLine(i);
			
//			for (String val : line)
//				System.out.println(val);
			
			long iter = Long.parseLong(line.get(0));
			long millis = Long.parseLong(line.get(1));
			double energy = Double.parseDouble(line.get(2));
			
			double secs = millis / 1000d;
			double mins = secs / 60d;
			
			energyVsIter.set((double)iter, energy);
			energyVsTime.set((double)mins, energy);
			iterVsTime.set((double)mins, (double)iter);
		}
		ArbitrarilyDiscretizedFunc[] ret = { energyVsIter, energyVsTime, iterVsTime };
		
		System.out.println("DONE (size="+energyVsTime.getNum()+")");
		
		return ret;
	}
	
	private static GraphiWindowAPI_Impl getGraphWindow(ArrayList<? extends DiscretizedFunc> funcs, String title,
			ArrayList<PlotCurveCharacterstics> chars, String xAxisName, String yAxisName, boolean visible) {
		GraphiWindowAPI_Impl gwAPI = new GraphiWindowAPI_Impl(funcs, title, chars, visible);
		GraphWindow gw = gwAPI.getGraphWindow();
//		gw.getGraphPanel().setBackgroundColor(Color.WHITE);
		gw.setXAxisLabel(xAxisName);
		gw.setYAxisLabel(yAxisName);
		gw.setSize(1000, 1000);
		return gwAPI;
	}
	
	private static ArbitrarilyDiscretizedFunc[] asArray(List<ArbitrarilyDiscretizedFunc> funcs) {
		return funcs.toArray(new ArbitrarilyDiscretizedFunc[funcs.size()]);
	}
	
	public static EvenlyDiscretizedFunc calcAvg(List<ArbitrarilyDiscretizedFunc> funcs, int numX) {
		return calcAvgAndStdDev(funcs, numX, false)[0];
	}
	
	public static EvenlyDiscretizedFunc[] calcAvgAndStdDev(List<ArbitrarilyDiscretizedFunc> funcs, int numX,
			boolean includeStdDev) {
		ArbitrarilyDiscretizedFunc[] funcsArray = asArray(funcs);
		double largestMin = Double.MIN_VALUE;
		double smallestMax = Double.MAX_VALUE;
		
		for (ArbitrarilyDiscretizedFunc func : funcsArray) {
			double min = func.getMinX();
			double max = func.getMaxX();
			if (min > largestMin)
				largestMin = min;
			if (max < smallestMax)
				smallestMax = max;
		}
		
		largestMin += 0.001;
		smallestMax -= 0.001;
		
		int num = funcsArray.length;
		
		EvenlyDiscretizedFunc avg = new EvenlyDiscretizedFunc(largestMin, smallestMax, numX);
		EvenlyDiscretizedFunc stdDevs = null;
		if (includeStdDev)
			stdDevs = new EvenlyDiscretizedFunc(largestMin, smallestMax, numX);
		
		double[] values = new double[num];
		for (int i=0; i<numX; i++) {
			double x = avg.getX(i);
			for (int j=0; j<num; j++) {
//				values[j] = funcsArray[j].getInterpolatedY_inLogYDomain(x);
				values[j] = funcsArray[j].getInterpolatedY(x);
			}
			double mean = StatUtils.mean(values);
			
			avg.set(i, mean);
			if (includeStdDev) {
				double stdDev = Math.sqrt(StatUtils.variance(values, mean));
				stdDevs.set(i, stdDev);
			}
		}
		EvenlyDiscretizedFunc[] ret = { avg, stdDevs };
		return ret;
	}
	
	private static ArbitrarilyDiscretizedFunc getSwapped(DiscretizedFunc func) {
		ArbitrarilyDiscretizedFunc swapped = new ArbitrarilyDiscretizedFunc();
		for (int i=0; i<func.getNum(); i++)
			swapped.set(func.getY(i), func.getX(i));
		return swapped;
	}
	
	private static ArrayList<DiscretizedFunc> generateEnergySpeedup(
			List<DiscretizedFunc> funcs, DiscretizedFunc ref, int numX) {
		double refMin = ref.getMinY();
		double refMax = ref.getMaxY();
		
		DiscretizedFunc refSwapped = getSwapped(ref);
		
		ArrayList<DiscretizedFunc> speedups = new ArrayList<DiscretizedFunc>();
		
		for (DiscretizedFunc func : funcs) {
			DiscretizedFunc funcSwapped = getSwapped(func);
			
			double min = funcSwapped.getMinX();
			double max = funcSwapped.getMaxX();
			if (min < refMin)
				min = refMin;
			min += 0.001;
			if (max > refMax)
				max = refMax;
			max -= 0.001;
			
			EvenlyDiscretizedFunc speedupFunc = new EvenlyDiscretizedFunc(min, max, numX);
			
			for (int i=0; i<numX; i++) {
				double energy = speedupFunc.getX(i);
				double refTime = refSwapped.getInterpolatedY(energy);
				double myTime = funcSwapped.getInterpolatedY(energy);
				
				double speedup = refTime / myTime;
				speedupFunc.set(energy, speedup);
			}
			
			speedups.add(speedupFunc);
		}
		
		return speedups;
	}
	
	private static double getLowestEnergy(List<DiscretizedFunc> funcs) {
		double min = Double.MAX_VALUE;
		for (DiscretizedFunc func : funcs) {
			double minE = func.getY(func.getNum()-1);
			if (minE < min)
				min = minE;
		}
		return min;
	}
	
//	private static DiscretizedFunc getExtrapolatedRef(DiscretizedFunc ref, double targetEnergy) {
//		double energy = ref.getY(ref.getNum()-1);
//		if (targetEnergy > energy)
//			// ref func already goes low enough!
//			return null;
//		
//		ArbitrarilyDiscretizedFunc arbRef;
//		if (ref instanceof ArbitrarilyDiscretizedFunc) {
//			arbRef = (ArbitrarilyDiscretizedFunc)ref;
//		} else {
//			arbRef = new ArbitrarilyDiscretizedFunc();
//			for (int i=ref.getNum()-100; i<ref.getNum(); i++) {
//				if (i < 0)
//					continue;
//				arbRef.set(ref.get(i));
//			}
//		}
//		
//		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
//		double startTime = ref.getX(ref.getNum()-1);
//		double timeDelta = startTime - ref.getX(ref.getNum()-2);
//		
//		double maxTime = startTime * 4;
//		for (double time=startTime; energy>targetEnergy && time < maxTime; time += timeDelta) {
//			energy = arbRef.getInterpExterpY_inLogXLogYDomain(time);
//			ret.set(time, energy);
//		}
//		return ret;
//	}
	
	private static final double extrap_slope_pt_fraction = 0.90;
	
	private static DiscretizedFunc getExtrapolatedRef(DiscretizedFunc ref, double targetEnergy) {
		double energy = ref.getY(ref.getNum()-1);
		if (targetEnergy > energy)
			// ref func already goes low enough!
			return null;
		
		ArbitrarilyDiscretizedFunc ret = new ArbitrarilyDiscretizedFunc();
		double startTime = ref.getX(ref.getNum()-1);
		double timeDelta = startTime - ref.getX(ref.getNum()-2);
		
		int i1 = ref.getNum()-1;
		int i0 = -1;
		double extrapPtTime = startTime * extrap_slope_pt_fraction;
		for (int i=0; i<ref.getNum(); i++) {
			double time = ref.getX(i);
			if (time > extrapPtTime) {
				i0 = i;
				break;
			}
		}
		
		double x0 = Math.log(ref.getX(i0));
		double x1 = Math.log(ref.getX(i1));
		
		double y0 = Math.log(ref.getY(i0));
		double y1 = Math.log(ref.getY(i1));
		
		double logSlope = (y1 - y0) / (x1 - x0);
		double logIntercept = y1 - (logSlope * x1);
		
		double maxTime = startTime * 4;
		for (double time=startTime; energy>targetEnergy && time < maxTime; time += timeDelta) {
//			double logEnergy = (logSlope * Math.log(time)) + logIntercept;
			energy = Math.exp(logSlope * Math.log(time) + logIntercept);
//			energy = arbRef.getInterpExterpY_inLogXLogYDomain(time);
			ret.set(time, energy);
		}
		return ret;
	}
	
	private static ArrayList<DiscretizedFunc> generateEnergyTimeSpeedup(
			List<DiscretizedFunc> energyTimeComparisons) {
		
		ArrayList<DiscretizedFunc> speedups = new ArrayList<DiscretizedFunc>();
		
		for (DiscretizedFunc comp : energyTimeComparisons) {
			ArbitrarilyDiscretizedFunc speedupFunc = new ArbitrarilyDiscretizedFunc();
			
			for (int i=0; i<comp.getNum(); i++) {
				double time = comp.getX(i);
				double refTime = comp.getY(i);
				
				double speedup = refTime / time;
				speedupFunc.set(time, speedup);
			}
			
			speedups.add(speedupFunc);
		}
		
		return speedups;
	}
	
	private static ArrayList<DiscretizedFunc> generateEnergyTimeComparison(
			List<DiscretizedFunc> funcs, DiscretizedFunc ref, int numX) {
		double refMinEnergy = ref.getMinY();
		double refMaxEnergy = ref.getMaxY();
		
		ArrayList<DiscretizedFunc> speedups = new ArrayList<DiscretizedFunc>();
		
		for (DiscretizedFunc func : funcs) {
			double funcMinEnergy = func.getMinY();
			double funcMaxEnergy = func.getMaxY();
			
			if (funcMinEnergy > refMaxEnergy) {
				speedups.add(null);
				continue;
			}
			
			double maxEnergy = funcMaxEnergy;
			if (maxEnergy > refMaxEnergy)
				maxEnergy = refMaxEnergy;
			double minEnergy = refMinEnergy;
			if (minEnergy < funcMinEnergy)
				minEnergy = funcMinEnergy;
			
			if (Double.isNaN(minEnergy) || Double.isNaN(maxEnergy)) {
				speedups.add(null);
				continue;
			}
			
			double minTime = func.getFirstInterpolatedX(maxEnergy);
			minTime += 0.001;
			double maxTime = func.getFirstInterpolatedX(minEnergy);
			maxTime -= 0.001;
			
			if (minTime >= maxTime) {
				speedups.add(null);
				continue;
			}
			
			EvenlyDiscretizedFunc speedupFunc = new EvenlyDiscretizedFunc(minTime, maxTime, numX);
			
			for (int i=0; i<numX; i++) {
				double time = speedupFunc.getX(i);
				double energy = func.getInterpolatedY(time);
				double refTime = ref.getFirstInterpolatedX(energy);
				speedupFunc.set(time, refTime);
				
//				double speedup = refTime / time;
//				speedupFunc.set(time, speedup);
			}
			
			speedups.add(speedupFunc);
		}
		
		return speedups;
	}
	
	private static ArrayList<DiscretizedFunc> generateTimeSpeedup(
			List<DiscretizedFunc> funcs, DiscretizedFunc ref, int numX) {
		double refMin = ref.getMinX();
		double refMax = ref.getMaxX();
		
		ArrayList<DiscretizedFunc> speedups = new ArrayList<DiscretizedFunc>();
		
		for (DiscretizedFunc func : funcs) {
			double min = func.getMinX();
			double max = func.getMaxX();
			if (min < refMin)
				min = refMin;
			if (max > refMax)
				max = refMax;
			
			EvenlyDiscretizedFunc speedupFunc = new EvenlyDiscretizedFunc(min, max, numX);
			
			for (int i=0; i<numX; i++) {
				double time = speedupFunc.getX(i);
				double refEnergy = ref.getInterpolatedY(time);
				double myEnergy = func.getInterpolatedY(time);
				
				double speedup = refEnergy / myEnergy;
				speedupFunc.set(time, speedup);
			}
			
			speedups.add(speedupFunc);
		}
		
		return speedups;
	}
	
	
	
	private static ArrayList<DiscretizedFunc> generatePercentImprovementOverTime(
			List<DiscretizedFunc> funcs, double mins) {
		ArrayList<DiscretizedFunc> ret = new ArrayList<DiscretizedFunc>();
		
		for (DiscretizedFunc func : funcs) {
			// x is time in m
			// y is energy
			
			DiscretizedFunc retFunc = new ArbitrarilyDiscretizedFunc();
			
			double prevEnergy = -1;
			for (double time=0; time<func.getMaxX(); time += mins) {
				if (time < func.getMinX())
					continue;
				
				double energy = func.getInterpolatedY(time);
				
				if (prevEnergy < 0) {
					prevEnergy = energy;
					continue;
				}
				double deltaE = prevEnergy - energy;
				double improvement = deltaE / prevEnergy;
				double percent = improvement * 100d;
				
				prevEnergy = energy;
				
				retFunc.set(time, percent);
			}
			
			ret.add(retFunc);
		}
		
		return ret;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/");
//		File mainDir = new File("D:\\Documents\\temp\\Inversion Results");
		
		File tsaDir = null;
		File dsaDir = null;
		
//		tsaDir = new File(mainDir, "results_5");
//		tsaDir = new File(mainDir, "results_6");
//		dsaDir = new File(mainDir, "dsa_results_8");
//		dsaDir = new File(mainDir, "mult_state_1_7hrs");
//		dsaDir = new File(mainDir, "mult_ncal_1");
//		dsaDir = new File(mainDir, "mult_ncal_2");
//		dsaDir = new File(mainDir, "multi/ncal_1");
//		dsaDir = new File(mainDir, "mult_state_2_comb_3");
//		dsaDir = new File(mainDir, "multi/ranger_ncal_1");
		
//		dsaDir = new File(mainDir, "poster/ncal_constrained");
//		dsaDir = new File(mainDir, "poster/ncal_unconstrained");
		dsaDir = new File(mainDir, "poster/state_constrained");
//		dsaDir = new File(mainDir, "poster/state_unconstrained");
		
		ArrayList<String> plots = new ArrayList<String>();
//		plots.add(energy_vs_time_title);
		plots.add(avg_energy_vs_time_title);
//		plots.add(std_dev_vs_time_title);
//		plots.add(improvement_vs_time_title);
//		plots.add(time_speedup_vs_energy_title);
		plots.add(time_comparison_title);
		plots.add(time_speedup_vs_time_title);
//		plots.add(speedup_vs_time_title);
//		plots.add(energy_vs_iterations_title);
//		plots.add(iterations_vs_time_title);
		
		String highlight = null;
		
//		highlight = "dsa_8threads_10nodes_FAST_SA_dSub200_sub100";
		
//		String coolType = "VERYFAST";
		String coolType = null;
		int threads = -1;
//		int threads = 6;
		int nodes = -1;
		boolean includeStartSubZero = false;
		boolean plotAvg = true;
		boolean bundleDsaBySubs = false;
		boolean bundleTsaBySubs = false;
		
		int avgNumX = 400;
		int targetPPM = 2;
		
		generatePlots(tsaDir, dsaDir, highlight, coolType, threads, nodes,
				includeStartSubZero, plotAvg, bundleDsaBySubs, bundleTsaBySubs,
				avgNumX, targetPPM, true, plots);
	}

	protected static HashMap<String, GraphiWindowAPI_Impl> generatePlots(File tsaDir, File dsaDir,
			String highlight, String coolType, int threads, int nodes,
			boolean includeStartSubZero, boolean plotAvg,
			boolean bundleDsaBySubs, boolean bundleTsaBySubs, int avgNumX, int targetPPM,
			boolean visible, Collection<String> plots)
			throws IOException {
		File[] tsaFiles;
		if (tsaDir == null)
			tsaFiles = new File[0];
		else
			tsaFiles = tsaDir.listFiles();
		File[] dsaFiles;
		if (dsaDir == null)
			dsaFiles = new File[0];
		else
			dsaFiles = dsaDir.listFiles();
		
		File[] files = new File[tsaFiles.length+dsaFiles.length];
		System.arraycopy(tsaFiles, 0, files, 0, tsaFiles.length);
		System.arraycopy(dsaFiles, 0, files, tsaFiles.length, dsaFiles.length);
		
		ArrayList<DiscretizedFunc> energyVsIter = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> energyVsTime = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> iterVsTime = new ArrayList<DiscretizedFunc>();
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
		HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>> runsEnergyTimeMap =
			new HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>>();
		HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>> runsIterTimeMap =
			new HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>>();
		HashMap<String, PlotCurveCharacterstics> runsChars = new HashMap<String, PlotCurveCharacterstics>();
		
		DiscretizedFunc refFunc = null;
		
		for (File file : files) {
			String name = file.getName();
			if (!name.endsWith(".csv"))
				continue;
			
//			if (name.contains("dSub600"))
//				continue;
			
			if (coolType != null && !name.contains(coolType))
				continue;
			if (threads > 0 && !name.contains(threads+"thread"))
				continue;
			if (nodes == 1 && name.contains("nodes"))
				continue;
			if (nodes > 1 && !name.contains(nodes+"nodes"))
				continue;
			if (!includeStartSubZero && name.contains("startSub"))
				continue;
			if (tsaDir != null && name.startsWith("tsa") && file.getAbsolutePath().contains("mult_"))
				continue;
			System.out.println("Loading CSV: "+name);
			ArbitrarilyDiscretizedFunc[] funcs = loadCSV(file, targetPPM);
			
			energyVsIter.add(funcs[0]);
			energyVsTime.add(funcs[1]);
			iterVsTime.add(funcs[2]);
			
			PlotLineType type;
			if (name.contains("CLASSICAL_SA"))
				type = PlotLineType.DOTTED;
			else if (name.contains("VERYFAST_SA"))
				type = PlotLineType.DASHED;
			else
				type = PlotLineType.SOLID;
			
			float size = 1f;
			
			Color c;
			if (name.contains("dsa")) {
				if (bundleDsaBySubs && name.contains("dSub")) {
					if (name.contains("dSub200_"))
						c = Color.BLACK;
					else if (name.contains("dSub500_"))
						c = Color.DARK_GRAY;
					else if (name.contains("dSub1000_"))
						c = Color.BLUE;
					else if (name.contains("dSub1500_"))
						c = Color.CYAN;
					else if (name.contains("dSub2000_"))
						c = Color.GREEN;
					else if (name.contains("dSub5000_"))
						c = Color.YELLOW;
					else if (name.contains("dSub10000_"))
						c = Color.ORANGE;
					else if (name.contains("dSub15000_"))
						c = Color.RED;
					else
						c = Color.MAGENTA;
					
//					if (name.contains("5nodes"))
//						size = 2f;
//					else if (name.contains("10nodes"))
//						size = 3f;
//					else if (name.contains("20nodes"))
//						size = 4f;
					if (name.contains("50nodes"))
						size = 3f;
				} else {
					if (name.contains("2nodes"))
						c = Color.BLACK;
					else if (name.contains("5nodes"))
						c = Color.BLUE;
					else if (name.contains("10nodes"))
						c = Color.GREEN;
					else if (name.contains("20nodes"))
						c = Color.RED;
					else if (name.contains("50nodes"))
						c = Color.ORANGE;
					else if (name.contains("100nodes"))
						c = Color.MAGENTA;
					else
						c = Color.PINK;
					if (name.contains("1thread"))
						type = PlotLineType.DASHED;
				}
			} else {
				if (bundleTsaBySubs && name.contains("sub")) {
					if (name.contains("sub50_"))
						c = Color.BLACK;
					else if (name.contains("sub100_"))
						c = Color.DARK_GRAY;
					else if (name.contains("sub200_"))
						c = Color.BLUE;
					else if (name.contains("sub400_"))
						c = Color.CYAN;
					else if (name.contains("sub600_"))
						c = Color.GREEN;
//					else if (name.contains("dSub5000_"))
//						c = Color.YELLOW;
//					else if (name.contains("dSub10000_"))
//						c = Color.ORANGE;
//					else if (name.contains("dSub15000_"))
//						c = Color.RED;
					else
						c = Color.MAGENTA;
					
					if (name.contains("4threads"))
						size = 2f;
					else if (name.contains("6threads"))
						size = 3f;
					else if (name.contains("8threads"))
						size = 4f;
				} else {
					if (name.contains("1thread"))
						c = Color.BLACK;
					else if (name.contains("_2threads"))
						c = Color.BLUE;
					else if (name.contains("4threads"))
						c = Color.GREEN;
					else if (name.contains("6threads"))
						c = Color.MAGENTA;
					else if (name.contains("8threads"))
						c = Color.RED;
					else
						c = Color.ORANGE;
				}
			}
			
			if (highlight != null && name.startsWith(highlight)) {
				c = Color.PINK;
				size *= 2;
			}
			
			if (includeStartSubZero && name.contains("startSubIterationsAtZero"))
				size += 1f;
			
			if (nodes == 1 || name.contains("dsa"))
				size += 1f;
			
			if (plotAvg && name.contains("run")) {
				String shortName = name.substring(0, name.indexOf("run"));
				if (!runsEnergyTimeMap.containsKey(shortName)) {
					runsEnergyTimeMap.put(shortName, new ArrayList<ArbitrarilyDiscretizedFunc>());
					runsIterTimeMap.put(shortName, new ArrayList<ArbitrarilyDiscretizedFunc>());
					runsChars.put(shortName, new PlotCurveCharacterstics(type, size, c));
				}
				
				runsEnergyTimeMap.get(shortName).add(funcs[1]);
				runsIterTimeMap.get(shortName).add(funcs[2]);
			}
			
			chars.add(new PlotCurveCharacterstics(type, size, c));
		}
		System.gc();
		
		System.out.println("Averaging");
		ArrayList<DiscretizedFunc> averages = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> variances = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> iterTimeAvgs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> avgChars = new ArrayList<PlotCurveCharacterstics>();
		if (plots.contains(avg_energy_vs_time_title)
				|| plots.contains(std_dev_vs_time_title)
				|| plots.contains(improvement_vs_time_title)
				|| plots.contains(time_speedup_vs_energy_title)) {
			for (String name : runsEnergyTimeMap.keySet()) {
				ArrayList<ArbitrarilyDiscretizedFunc> runs = runsEnergyTimeMap.get(name);
				if (runs == null || runs.size() <= 1)
					continue;
				
				String cName = name + " (average of "+runs.size()+" curves)";
				
				System.out.println("Averaging: "+name);
				
				DiscretizedFunc[] ret = calcAvgAndStdDev(runs, avgNumX, true);
				DiscretizedFunc avg = ret[0];
				avg.setName(cName);
				averages.add(avg);
				
				DiscretizedFunc stdDevs = ret[1];
				stdDevs.setName(cName);
				variances.add(stdDevs);
				
				DiscretizedFunc iterTimeAvg = calcAvg(runsIterTimeMap.get(name), avgNumX);
				iterTimeAvg.setName(cName);
				iterTimeAvgs.add(iterTimeAvg);
				
				avgChars.add(runsChars.get(name));
				
				if (refFunc == null) {
					if (coolType == null && name.startsWith("tsa_1threads_FAST")
							|| name.startsWith("tsa_1threads_"+coolType))
						refFunc = avg;
				}
//				if (refFunc == null && name.startsWith("tsa_1threads_VERYFAST"))
//					refFunc = avg;
			}
		}
		
		ArrayList<DiscretizedFunc> timeComparisons = null;
		ArrayList<PlotCurveCharacterstics> timeCompChars = null;
		if (refFunc != null && 
				(plots.contains(time_comparison_title)
						|| plots.contains(time_speedup_vs_time_title))) {
			
			System.out.println("generating time comparisons");
			timeComparisons = generateEnergyTimeComparison(averages, refFunc, avgNumX);
			
			timeCompChars = avgChars;
			int numOrig = timeComparisons.size();
			if (do_extrap) {
				DiscretizedFunc extrapRef = getExtrapolatedRef(refFunc, getLowestEnergy(averages));
				if (extrapRef != null) {
					timeComparisons.addAll(generateEnergyTimeComparison(averages, extrapRef, avgNumX/2));
					timeCompChars = new ArrayList<PlotCurveCharacterstics>();
					timeCompChars.addAll(avgChars);
					for (int i=numOrig-1; i>=0; i--) {
						int extrapI = i+numOrig;
						if (timeComparisons.get(extrapI) == null) {
							timeComparisons.remove(extrapI);
						} else {
							PlotCurveCharacterstics cloned = (PlotCurveCharacterstics)avgChars.get(i).clone();
							cloned.setLineType(PlotLineType.DOTTED);
							timeCompChars.add(numOrig, cloned);
						}
					}
				}
			}
		}
		
		HashMap<String, GraphiWindowAPI_Impl> windows = new HashMap<String, GraphiWindowAPI_Impl>();
		
		for (String plot : plots) {
			if (plot.equals(avg_energy_vs_time_title)) {
				if (averages.size() > 0) {
					System.out.println("displaying "+avg_energy_vs_time_title);
					windows.put(avg_energy_vs_time_title,
							getGraphWindow(averages, avg_energy_vs_time_title, avgChars, time_label, energy_label, visible));
				}
			} else if (plot.equals(std_dev_vs_time_title)) {
				if (variances.size() > 0) {
					System.out.println("displaying "+std_dev_vs_time_title);
					windows.put(std_dev_vs_time_title,
							getGraphWindow(variances, std_dev_vs_time_title, avgChars, time_label, std_dev_label, visible));
				}
			} else if (plot.equals(improvement_vs_time_title)) {
				if (averages.size() > 0) {
					System.out.println("generating percent improvements over "+pDiffMins+" mins");
					ArrayList<DiscretizedFunc> pImpFuncs = generatePercentImprovementOverTime(averages, pDiffMins);
					System.out.println("displaying "+improvement_vs_time_title);
					windows.put(improvement_vs_time_title,
							getGraphWindow(pImpFuncs, improvement_vs_time_title, avgChars,
							time_label, improvement_label, visible));
				}
			} else if (plot.equals(time_speedup_vs_energy_title)) {
				if (refFunc != null) {
					System.out.println("generating energy speedup");
					ArrayList<DiscretizedFunc> energySpeedups = generateEnergySpeedup(averages, refFunc, avgNumX);
					System.out.println("displaying "+time_speedup_vs_energy_title);
					windows.put(time_speedup_vs_energy_title,
							getGraphWindow(energySpeedups, time_speedup_vs_energy_title, avgChars, energy_label, time_speedup_label, visible));
				}
			} else if (plot.equals(time_comparison_title)) {
				if (timeComparisons != null) {
					System.out.println("displaying "+time_comparison_title);
					windows.put(time_comparison_title,
							getGraphWindow(timeComparisons, time_comparison_title, timeCompChars,
									parallel_time_label, serial_time_label, visible));
				}
			} else if (plot.equals(time_speedup_vs_time_title)) {
				if (timeComparisons != null) {
					System.out.println("displaying "+time_speedup_vs_time_title);
					ArrayList<DiscretizedFunc> energySpeedups = generateEnergyTimeSpeedup(timeComparisons);
					windows.put(time_speedup_vs_time_title,
							getGraphWindow(energySpeedups, time_speedup_vs_time_title, timeCompChars,
									parallel_time_label, time_speedup_label, visible));
				}
			} else if (plot.equals(energy_speedup_vs_time_title)) {
				if (refFunc != null) {
					System.out.println("generating time speedup");
					ArrayList<DiscretizedFunc> timeSpeedups = generateTimeSpeedup(averages, refFunc, avgNumX);
					System.out.println("displaying "+energy_speedup_vs_time_title);
					windows.put(energy_speedup_vs_time_title,
							getGraphWindow(timeSpeedups, energy_speedup_vs_time_title, avgChars, time_label, energy_speedup_label, visible));
				}
			} else if (plot.equals(energy_vs_iterations_title)) {
				System.out.println("displaying "+energy_vs_iterations_title);
				windows.put(energy_vs_iterations_title,
						getGraphWindow(energyVsIter, energy_vs_iterations_title, chars, iterations_label, energy_label, visible));
			} else if (plot.equals(energy_vs_time_title)) {
				System.out.println("displaying "+energy_vs_time_title);
				windows.put(energy_vs_time_title,
						getGraphWindow(energyVsTime, energy_vs_time_title, chars, time_label, energy_label, visible));
			} else if (plot.equals(energy_vs_iterations_title)) {
				System.out.println("displaying "+iterations_vs_time_title);
				if (iterTimeAvgs.size() > 0)
					windows.put(iterations_vs_time_title,
							getGraphWindow(iterTimeAvgs, iterations_vs_time_title, avgChars, time_label, iterations_label, visible));
				else
					windows.put(iterations_vs_time_title,
							getGraphWindow(iterVsTime, iterations_vs_time_title, chars, time_label, iterations_label, visible));
			} else {
				System.out.println("Unknown plot type: "+plot);
			}
		}
		
		return windows;
	}

}
