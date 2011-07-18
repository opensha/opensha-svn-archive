package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

public class ResultPlotter {
	
	private static ArbitrarilyDiscretizedFunc[] loadCSV(File file, int mod) throws IOException {
		
		CSVFile<String> csv = CSVFile.readFile(file, true);
		
		ArbitrarilyDiscretizedFunc energyVsIter = new ArbitrarilyDiscretizedFunc();
		energyVsIter.setName("Energy Vs Iterations ("+file.getName()+")");
		energyVsIter.setYAxisName("Energy");
		energyVsIter.setXAxisName("Iterations");
		ArbitrarilyDiscretizedFunc energyVsTime = new ArbitrarilyDiscretizedFunc();
		energyVsTime.setName("Energy Over Time ("+file.getName()+")");
		energyVsTime.setYAxisName("Energy");
		energyVsTime.setXAxisName("Time (m)");
		ArbitrarilyDiscretizedFunc iterVsTime = new ArbitrarilyDiscretizedFunc();
		iterVsTime.setName("Iterations Over Time ("+file.getName()+")");
		iterVsTime.setYAxisName("Iterations");
		iterVsTime.setXAxisName("Time (m)");
		
		System.out.println("Loading: "+file.getName());
		
		for (int i=1; i<csv.getNumRows(); i++) {
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
		
		return ret;
	}
	
	private static void showGraphWindow(ArrayList<? extends DiscretizedFunc> funcs, String title,
			ArrayList<PlotCurveCharacterstics> chars) {
		new GraphiWindowAPI_Impl(funcs, title, chars);
	}
	
	public static EvenlyDiscretizedFunc avgCurves(List<ArbitrarilyDiscretizedFunc> funcs, int numX) {
		double largestMin = Double.MIN_VALUE;
		double smallestMax = Double.MAX_VALUE;
		
		for (ArbitrarilyDiscretizedFunc func : funcs) {
			double min = func.getMinX();
			double max = func.getMaxX();
			if (min > largestMin)
				largestMin = min;
			if (max < smallestMax)
				smallestMax = max;
		}
		
		int num = funcs.size();
		
		EvenlyDiscretizedFunc avg = new EvenlyDiscretizedFunc(largestMin, smallestMax, numX);
		
		for (int i=0; i<numX; i++) {
			double x = avg.getX(i);
			double y = 0;
			for (ArbitrarilyDiscretizedFunc func : funcs) {
				y += func.getInterpolatedY_inLogYDomain(x);
			}
			y /= (double)num;
			
			avg.set(i, y);
		}
		
		return avg;
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
			double min = func.getMinY();
			double max = func.getMaxY();
			if (min < refMin)
				min = refMin;
			if (max > refMax)
				max = refMax;
			
			EvenlyDiscretizedFunc speedupFunc = new EvenlyDiscretizedFunc(min, max, numX);
			
			DiscretizedFunc funcSwapped = getSwapped(func);
			
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

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/");
		File mainDir = new File("D:\\Documents\\temp\\Inversion Results");
		File tsaDir = new File(mainDir, "results_5");
		File dsaDir = new File(mainDir, "dsa_results_5");
		
		String coolType = "VERYFAST";
		int threads = -1;
		int nodes = -1;
		boolean includeStartSubZero = false;
		boolean plotAvg = true;
		boolean bundleDsaBySubs = false;
		
		File[] tsaFiles = tsaDir.listFiles();
		File[] dsaFiles = dsaDir.listFiles();
		
		File[] files = new File[tsaFiles.length+dsaFiles.length];
		System.arraycopy(tsaFiles, 0, files, 0, tsaFiles.length);
		System.arraycopy(dsaFiles, 0, files, tsaFiles.length, dsaFiles.length);
		
		int mod = 1;
		
		ArrayList<DiscretizedFunc> energyVsIter = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> energyVsTime = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> iterVsTime = new ArrayList<DiscretizedFunc>();
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
		HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>> runsEnergyTimeMap =
			new HashMap<String, ArrayList<ArbitrarilyDiscretizedFunc>>();
		HashMap<String, PlotCurveCharacterstics> runsChars = new HashMap<String, PlotCurveCharacterstics>();
		
		DiscretizedFunc refFunc = null;
		
		for (File file : files) {
			String name = file.getName();
			if (!name.endsWith(".csv"))
				continue;
			
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
			ArbitrarilyDiscretizedFunc[] funcs = loadCSV(file, mod);
			
			energyVsIter.add(funcs[0]);
			energyVsTime.add(funcs[1]);
			iterVsTime.add(funcs[2]);
			
			PlotLineType type;
			if (name.contains("CLASSICAL_SA"))
				type = PlotLineType.DOTTED;
			else if (name.contains("VERYFAST_SA"))
				type = PlotLineType.SOLID;
			else
				type = PlotLineType.DASHED;
			
			Color c;
			if (name.contains("dsa")) {
				if (bundleDsaBySubs && name.contains("dSub")) {
					if (name.contains("dSub2000"))
						c = Color.BLACK;
					else if (name.contains("dSub5000"))
						c = Color.BLUE;
					else if (name.contains("dSub10000"))
						c = Color.GREEN;
					else
						c = Color.RED;
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
					else
						c = Color.PINK;
					if (name.contains("1thread"))
						type = PlotLineType.DASHED;
				}
			} else {
				if (name.contains("1thread"))
					c = Color.BLACK;
				else if (name.contains("2threads"))
					c = Color.BLUE;
				else if (name.contains("4threads"))
					c = Color.GREEN;
				else
					c = Color.RED;
			}
			
			float size = 1f;
			
			if (includeStartSubZero && name.contains("startSubIterationsAtZero"))
				size += 1f;
			
			if (nodes == 1 || name.contains("dsa"))
				size += 1f;
			
			if (plotAvg && name.contains("run")) {
				String shortName = name.substring(0, name.indexOf("run"));
				if (!runsEnergyTimeMap.containsKey(shortName)) {
					runsEnergyTimeMap.put(shortName, new ArrayList<ArbitrarilyDiscretizedFunc>());
					runsChars.put(shortName, new PlotCurveCharacterstics(type, size, c));
				}
				
				runsEnergyTimeMap.get(shortName).add(funcs[1]);
			}
			
			chars.add(new PlotCurveCharacterstics(type, size, c));
		}
		
		ArrayList<DiscretizedFunc> averages = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> avgChars = new ArrayList<PlotCurveCharacterstics>();
		for (String name : runsEnergyTimeMap.keySet()) {
			ArrayList<ArbitrarilyDiscretizedFunc> runs = runsEnergyTimeMap.get(name);
			if (runs == null || runs.size() <= 1)
				continue;
			DiscretizedFunc avg = avgCurves(runs, 500);
			averages.add(avg);
			avgChars.add(runsChars.get(name));
			
			if (refFunc == null && name.startsWith("tsa_1threads_VERYFAST"))
				refFunc = avg;
		}
		
		if (averages.size() > 0) {
			showGraphWindow(averages, "Averaged Energy Vs Time (m)", avgChars);
		}
		
		if (refFunc != null) {
			ArrayList<DiscretizedFunc> energySpeedups = generateEnergySpeedup(averages, refFunc, 500);
			showGraphWindow(energySpeedups, "Speedup Vs Energy", avgChars);
			

			ArrayList<DiscretizedFunc> timeSpeedups = generateTimeSpeedup(averages, refFunc, 500);
			showGraphWindow(timeSpeedups, "Speedup Vs Time", avgChars);
		}
		
//		Collections.sort(energyVsIter, new EnergyComparator());
//		Collections.sort(energyVsTime, new EnergyComparator());
		
		showGraphWindow(energyVsIter, "Energy Vs Iterations", chars);
		showGraphWindow(energyVsTime, "Energy Vs Time (m)", chars);
		showGraphWindow(iterVsTime, "Iterations Vs Time (m)", chars);
	}

}
