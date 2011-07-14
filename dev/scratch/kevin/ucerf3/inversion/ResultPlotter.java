package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
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
	
	private static void showGraphWindow(ArrayList<ArbitrarilyDiscretizedFunc> funcs, String title,
			ArrayList<PlotCurveCharacterstics> chars) {
		new GraphiWindowAPI_Impl(funcs, title, chars);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		File mainDir = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/");
		File mainDir = new File("D:\\Documents\\temp\\Inversion Results");
		File tsaDir = new File(mainDir, "results_5");
		File dsaDir = new File(mainDir, "dsa_results_1");
		
		String coolType = "VERYFAST";
		int threads = -1;
		int nodes = 1;
		boolean includeStartSubZero = false;
		
		File[] tsaFiles = tsaDir.listFiles();
		File[] dsaFiles = dsaDir.listFiles();
		
		File[] files = new File[tsaFiles.length+dsaFiles.length];
		System.arraycopy(tsaFiles, 0, files, 0, tsaFiles.length);
		System.arraycopy(dsaFiles, 0, files, tsaFiles.length, dsaFiles.length);
		
		int mod = 1;
		
		ArrayList<ArbitrarilyDiscretizedFunc> energyVsIter = new ArrayList<ArbitrarilyDiscretizedFunc>();
		ArrayList<ArbitrarilyDiscretizedFunc> energyVsTime = new ArrayList<ArbitrarilyDiscretizedFunc>();
		ArrayList<ArbitrarilyDiscretizedFunc> iterVsTime = new ArrayList<ArbitrarilyDiscretizedFunc>();
		
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
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
				if (name.contains("5nodes"))
					c = Color.BLUE;
				else if (name.contains("10nodes"))
					c = Color.GREEN;
				else
					c = Color.RED;
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
			
			float size;
			if (name.contains("dsa"))
				size = 3f;
			else if (name.contains("startSubIterationsAtZero"))
				size = 1f;
			else
				size = 2f;
			
			chars.add(new PlotCurveCharacterstics(type, size, c));
		}
		
//		Collections.sort(energyVsIter, new EnergyComparator());
//		Collections.sort(energyVsTime, new EnergyComparator());
		
		showGraphWindow(energyVsIter, "Energy Vs Iterations", chars);
		showGraphWindow(energyVsTime, "Energy Vs Time (m)", chars);
		showGraphWindow(iterVsTime, "Iterations Vs Time (m)", chars);
	}

}
