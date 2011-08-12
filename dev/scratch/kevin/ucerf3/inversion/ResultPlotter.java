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
		
		String nlower = file.getName().toLowerCase();
		if (nlower.contains("sub")) {
			String sub = nlower.substring(nlower.indexOf("sub")+3);
			sub = sub.substring(0, sub.indexOf('_'));
			
			int subI = Integer.parseInt(sub);
			if (subI <= 50)
				mod *= 80;
			else if (subI <= 100)
				mod *= 40;
			else if (subI <= 200)
				mod *= 20;
			else if (subI <= 400)
				mod *= 10;
			else if (subI <= 800)
				mod *= 5;
			else if (subI <= 1000)
				mod *= 4;
			else if (subI <= 2000)
				mod *= 2;
		}
		
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
		
		largestMin += 0.001;
		smallestMax -= 0.001;
		
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
//		dsaDir = new File(mainDir, "multi/ncal_1");
		dsaDir = new File(mainDir, "multi/ncal_4");
//		dsaDir = new File(mainDir, "multi/state_3");
		
		String highlight = null;
		
//		highlight = "dsa_8threads_10nodes_FAST_SA_dSub200_sub100_run";
		
//		String coolType = "VERYFAST";
		String coolType = null;
		int threads = -1;
//		int threads = 6;
		int nodes = -1;
		boolean includeStartSubZero = false;
		boolean plotAvg = true;
		boolean bundleDsaBySubs = false;
		boolean bundleTsaBySubs = false;
		
		double pDiffMins = 5;
		
		int avgNumX = 300;
		
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
		
		int mod = 5;
		
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
			ArbitrarilyDiscretizedFunc[] funcs = loadCSV(file, mod);
			
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
					else if (name.contains("2threads"))
						c = Color.BLUE;
					else if (name.contains("4threads"))
						c = Color.GREEN;
					else if (name.contains("6threads"))
						c = Color.MAGENTA;
					else
						c = Color.RED;
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
		
		System.out.println("Averaging");
		ArrayList<DiscretizedFunc> averages = new ArrayList<DiscretizedFunc>();
		ArrayList<DiscretizedFunc> iterTimeAvgs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> avgChars = new ArrayList<PlotCurveCharacterstics>();
		for (String name : runsEnergyTimeMap.keySet()) {
			ArrayList<ArbitrarilyDiscretizedFunc> runs = runsEnergyTimeMap.get(name);
			if (runs == null || runs.size() <= 1)
				continue;
			
			String cName = name + " (average of "+runs.size()+" curves)";
			
			DiscretizedFunc avg = avgCurves(runs, avgNumX);
			avg.setName(cName);
			averages.add(avg);
			
			DiscretizedFunc iterTimeAvg = avgCurves(runsIterTimeMap.get(name), avgNumX);
			iterTimeAvg.setName(cName);
			iterTimeAvgs.add(iterTimeAvg);
			
			avgChars.add(runsChars.get(name));
			
			if (refFunc == null) {
				if (coolType == null && name.startsWith("tsa_1threads_FAST")
						|| name.startsWith("tsa_1threads_VERYFAST"))
					refFunc = avg;
			}
//			if (refFunc == null && name.startsWith("tsa_1threads_VERYFAST"))
//				refFunc = avg;
		}
		
		if (averages.size() > 0) {
			System.out.println("displaying Averaged Energy Vs Time (m)");
			showGraphWindow(averages, "Averaged Energy Vs Time (m)", avgChars);

			System.out.println("generating percent improvements over "+pDiffMins+" mins");
			ArrayList<DiscretizedFunc> pImpFuncs = generatePercentImprovementOverTime(averages, pDiffMins);
			System.out.println("displaying percent improvements");
			showGraphWindow(pImpFuncs, "% Improvement (over "+pDiffMins+" min invervals)", avgChars);
		}
		
		if (refFunc != null) {
			System.out.println("generating energy speedup");
			ArrayList<DiscretizedFunc> energySpeedups = generateEnergySpeedup(averages, refFunc, avgNumX);
			System.out.println("displaying energy speedup");
			showGraphWindow(energySpeedups, "Speedup Vs Energy", avgChars);
			

			System.out.println("generating time speedup");
			ArrayList<DiscretizedFunc> timeSpeedups = generateTimeSpeedup(averages, refFunc, avgNumX);
			System.out.println("displaying time speedup");
			showGraphWindow(timeSpeedups, "Speedup Vs Time", avgChars);
		}
		
//		Collections.sort(energyVsIter, new EnergyComparator());
//		Collections.sort(energyVsTime, new EnergyComparator());
		
		System.out.println("displaying Energy Vs Iterations");
		showGraphWindow(energyVsIter, "Energy Vs Iterations", chars);
		System.out.println("displaying Energy Vs Time");
		showGraphWindow(energyVsTime, "Energy Vs Time (m)", chars);
		
		System.out.println("displaying Iterations Vs Time (m)");
		if (iterTimeAvgs.size() > 0)
			showGraphWindow(iterTimeAvgs, "Average Iterations Vs Time (m)", avgChars);
		else
			showGraphWindow(iterVsTime, "Iterations Vs Time (m)", chars);
	}

}
