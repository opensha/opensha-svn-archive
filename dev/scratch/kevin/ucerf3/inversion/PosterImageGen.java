package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jfree.data.Range;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

public class PosterImageGen {
	
	private static final int png_thumb_width = 400;
	private static final int png_thumb_height = 400;
	
	private static final int width = 1000;
	private static final int height = 1000;
	
	private static final boolean tableOnly = false;
	private static final boolean highQuality = true;
	
	private static ArrayList<ArrayList<String>> wikiTable = new ArrayList<ArrayList<String>>();
	static {
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(0).add("!Dataset");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(1).add("!Energy vs Time");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(2).add("!Avg Energy vs Time");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(3).add("!Serial Time vs Parallel Time");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(4).add("!Time Speedup vs Time");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(5).add("!Std. Dev. vs Time");
		wikiTable.add(new ArrayList<String>());
		wikiTable.get(6).add("!Improvement vs Energy");
	}
	private static final String opensha_files_url = "http://opensha.usc.edu/ftp/kmilner/ucerf3/dsa_poster/";
	
	private static void saveImages(GraphiWindowAPI_Impl gwAPI, File dir, String fName) throws IOException {
		GraphWindow gw = gwAPI.getGraphWindow();
		GraphPanel gp = gw.getGraphPanel();
		gp.setBackgroundColor(Color.WHITE);
		gp.setSize(width, height);
		gw.drawGraph();
		gp.setVisible(true);

		gp.togglePlot(null);
		
		gp.validate();
		gp.repaint();
		
		gp.saveAsPDF(new File(dir, fName+".pdf").getAbsolutePath(), width, height);
		gp.saveAsPNG(new File(dir, fName+".png").getAbsolutePath(), width, height);
		gp.saveAsPNG(new File(dir, fName+".small.png").getAbsolutePath(), png_thumb_width, png_thumb_height);
	}
	
	private static String getImageTableLine(String fName) {
		return "|["+opensha_files_url+fName+".png "+opensha_files_url+fName+".small.png]";
	}
	
	private static void handleDir(File dir, Range timeRange, Range energyRange,
			Range stdDevRange, Range improvementRange)
	throws IOException {
		// Range speedupRange, 
		
		int myAvgNumX = avgNumX;
		if (!highQuality)
			myAvgNumX = avgNumX/2;
		int myTargetNum = targetPPM;
		if (!highQuality)
			myTargetNum = targetPPM/2;
		
		HashMap<String, GraphiWindowAPI_Impl> windows = null;
		if (!tableOnly)
			windows = ResultPlotter.generatePlots(null, dir, highlight, coolType, threads, nodes,
				includeStartSubZero, plotAvg, bundleDsaBySubs, bundleTsaBySubs,
				myAvgNumX, myTargetNum, false, plots);
		GraphiWindowAPI_Impl gw;
		
		wikiTable.get(0).add("!"+dir.getName());
		String prefix = dir.getName()+"_";
		String fName;
		
		fName = prefix+"e_vs_t";
		wikiTable.get(1).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.energy_vs_time_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
			gw.setY_AxisRange(energyRange.getLowerBound(), energyRange.getUpperBound());
			saveImages(gw, dir, fName);
		}
		
		fName = prefix+"avg_e_vs_t";
		wikiTable.get(2).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.avg_energy_vs_time_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
			gw.setY_AxisRange(energyRange.getLowerBound(), energyRange.getUpperBound());
			saveImages(gw, dir, fName);
		}

		fName = prefix+"st_vs_pt";
		wikiTable.get(3).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.time_comparison_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
//			gw.setY_AxisRange(speedupRange.getLowerBound(), speedupRange.getUpperBound());
			saveImages(gw, dir, fName);
		}

		fName = prefix+"spd_vs_t";
		wikiTable.get(4).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.time_speedup_vs_time_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
//			gw.setY_AxisRange(speedupRange.getLowerBound(), speedupRange.getUpperBound());
			saveImages(gw, dir, fName);
		}
		
		fName = prefix+"std_dev_vs_t";
		wikiTable.get(5).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.std_dev_vs_time_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
			gw.setY_AxisRange(stdDevRange.getLowerBound(), stdDevRange.getUpperBound());
			saveImages(gw, dir, fName);
		}
		
		fName = prefix+"imp_vs_t";
		wikiTable.get(6).add(getImageTableLine(fName));
		if (!tableOnly) {
			gw = windows.get(ResultPlotter.improvement_vs_time_title);
			gw.setX_AxisRange(timeRange.getLowerBound(), timeRange.getUpperBound());
			gw.setY_AxisRange(improvementRange.getLowerBound(), improvementRange.getUpperBound());
			saveImages(gw, dir, fName);
		}
	}
	
	private static String coolType = null;
	private static int threads = -1;
	private static int nodes = -1;
	private static boolean includeStartSubZero = false;
	private static boolean plotAvg = true;
	private static boolean bundleDsaBySubs = false;
	private static boolean bundleTsaBySubs = false;
	private static String highlight = null;
	
	private static int avgNumX = 600;
	private static int targetPPM = 4;
	
	private static ArrayList<String> plots = new ArrayList<String>();
	static {
		plots.add(ResultPlotter.energy_vs_time_title);
		plots.add(ResultPlotter.avg_energy_vs_time_title);
		plots.add(ResultPlotter.std_dev_vs_time_title);
		plots.add(ResultPlotter.improvement_vs_time_title);
		plots.add(ResultPlotter.time_comparison_title);
		plots.add(ResultPlotter.time_speedup_vs_time_title);
	}
	
	private static void printTable() {
		System.out.println("***** TABLE *****\n");
		System.out.println("{| border=\"1\"");
		
		for (int i=0; i<wikiTable.size(); i++) {
			if (i > 0)
				System.out.println("|-");
			ArrayList<String> row = wikiTable.get(i);
			for (String cell : row)
				System.out.println(cell);
		}
		
		System.out.println("|}");
	}
	
	public static void main(String args[]) throws IOException {
		File main = new File("/home/kevin/OpenSHA/UCERF3/test_inversion/bench/poster");
		
		File ncalConst = new File(main, "ncal_constrained");
		File ncalUnconst = new File(main, "ncal_unconstrained");
		File stateConst = new File(main, "state_constrained");
		File stateUnonst = new File(main, "state_unconstrained");
		
		handleDir(ncalConst,
				// time range
				new Range(0, 120),
				// energy plot range
				new Range(220, 260),
//				// speedup range
//				new Range(0.5, 21),
				// std. dev. range
				new Range(0, 40),
				// % improvement range
				new Range(0, 10));
		
		handleDir(ncalUnconst,
				// time range
				new Range(0, 120),
				// energy plot range
				new Range(1.5, 8),
//				// speedup range
//				new Range(0.5, 10),
				// std. dev. range
				new Range(0, 1),
				// % improvement range
				new Range(0, 10));
		
		handleDir(stateConst,
				// time range
				new Range(0, 480),
				// energy plot range
				new Range(2200000.0, 4000000.0),
//				// speedup range
//				new Range(0.5, 6),
				// std. dev. range
				new Range(0, 55000),
				// % improvement range
				new Range(0, 1));
		
		handleDir(stateUnonst,
				// time range
				new Range(0, 480),
				// energy plot range
				new Range(7.5, 35),
//				// speedup range
//				new Range(0.5, 6.5),
				// std. dev. range
				new Range(0, 10),
				// % improvement range
				new Range(0, 10));
		
		printTable();
		
		System.exit(0);
	}

}
