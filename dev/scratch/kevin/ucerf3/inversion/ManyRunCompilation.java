package scratch.kevin.ucerf3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JFrame;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.math.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.FaultBasedMapGen;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;

public class ManyRunCompilation {
	
	private static double[][] loadRates(File zipFile, int numRups, int numRuns) throws ZipException, IOException {
		
		double[][] rates = new double[numRups][numRuns];
		
		ZipFile zip = new ZipFile(zipFile);
		List<ZipEntry> entries = EnumerationUtils.toList(zip.entries());
		Preconditions.checkState(entries.size() == numRuns, "zip entry size is off! "+entries.size()+" != "+numRuns);
		
		for (int i=0; i<numRuns; i++) {
			double[] runRates = MatrixIO.doubleArrayFromInputStream(zip.getInputStream(entries.get(i)), numRups*8l);
			
			for (int r=0; r<numRups; r++) {
				rates[r][i] = runRates[r];
			}
		}
		
		return rates;
	}
	
	public static double[][] loadRates(File dir, int numRups) throws IOException {
		ArrayList<File> files = new ArrayList<File>();
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
			if (!file.getName().endsWith(".bin"))
				continue;
			if (file.getName().contains("partic") || file.getName().contains("std_dev"))
				continue;
			
			files.add(file);
		}
		
		int numRuns = files.size();
		
		double[][] rates = new double[numRups][numRuns];
		
		for (int i=0; i<numRuns; i++) {
			double[] runRates = MatrixIO.doubleArrayFromFile(files.get(i));
			Preconditions.checkState(runRates.length == numRups,
					"Rate file is wrong size: "+runRates.length+" != "+numRups
					+" ("+files.get(i).getName()+")");
			
			for (int r=0; r<numRups; r++) {
				try {
					rates[r][i] = runRates[r];
				} catch (RuntimeException e) {
					System.out.println("r: "+r+", i: "+i+", numRups: "+numRups+", numRuns: "+numRuns);
					throw e;
				}
			}
		}
		
		return rates;
	}
	
	private static class RateRecord implements Comparable<RateRecord> {
		
		double mean, min, max, stdDev, lower, upper;
		
		public RateRecord(double mean, double min, double max, double lower, double upper, double stdDev) {
			this.mean = mean;
			this.min = min;
			this.max = max;
			this.stdDev = stdDev;
			this.lower = lower;
			this.upper = upper;
		}

		@Override
		public int compareTo(RateRecord o) {
			return Double.compare(mean, o.mean);
		}
		
	}
	
	private static double median(double[] sorted) {
		if (sorted.length % 2 == 1)
			return sorted[(sorted.length+1)/2-1];
		else
		{
			double lower = sorted[sorted.length/2-1];
			double upper = sorted[sorted.length/2];

			return (lower + upper) * 0.5;
		}	
	}

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws RuntimeException 
	 * @throws GMT_MapException 
	 */
	public static void main(String[] args) throws ZipException, IOException, DocumentException, GMT_MapException, RuntimeException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_03_28-unconstrained-run-like-crazy/results");
		File rupSetFile = new File(dir, "rupSet.zip");
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromZipFile(rupSetFile);
		int numRups = rupSet.getNumRuptures();
		System.out.println("Loaded rupSet with "+numRups+" ruptures");
//		int numRuns = 460;
//		File zipFile = new File(dir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Unconst_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_Manyruns.zip");
//		String prefix = zipFile.getName().replaceAll(".zip", "");
		double[][] rates = loadRates(dir, numRups);
		int numRuns = rates[0].length;
		String prefix = "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Unconst_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone";
		System.out.println("Loaded rates!");
		
		RateRecord[] rateRecords = new RateRecord[numRups];
		
		int numZeros = 0;
		double numZerosPer = 0;
		
		int upperInd = (int)(numRuns * 0.975+0.5);
		int lowerInd = (int)(numRuns * 0.025+0.5);
		
		double[] meanRates = new double[numRups];
		double[] medianRates = new double[numRups];
		for (int r=0; r<numRups; r++) {
			double[] rupRates = rates[r];
			double mean = StatUtils.mean(rupRates);
			for (double rate : rupRates)
				if (rate == 0)
					numZerosPer++;
			meanRates[r] = mean;
			if (mean == 0)
				numZeros++;
			double min = StatUtils.min(rupRates);
			double max = StatUtils.max(rupRates);
			double stdDev = Math.sqrt(StatUtils.variance(rupRates, mean));
			
			double[] sorted = Arrays.copyOf(rupRates, numRuns);
			Arrays.sort(sorted);
			
			medianRates[r] = median(sorted);
			
//			highRates[r] = mean + stdDev;
			double upper = sorted[upperInd];
//			lowRates[r] = mean - stdDev;
			double lower = sorted[lowerInd];
			rateRecords[r] = new RateRecord(mean, min, max, lower, upper, stdDev);
		}
		numZerosPer /= numRuns;
		System.out.println("num zeros: "+numZeros);
		System.out.println("avg zeros per run: "+numZerosPer);
		
		Arrays.sort(rateRecords);
		
		EvenlyDiscretizedFunc meanFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc minFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc maxFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc upperFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc lowerFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		
		int cnt = 0;
		for (int i=rateRecords.length; --i>=0;) {
			RateRecord rec = rateRecords[i];
			meanFunc.set(cnt, rec.mean);
			minFunc.set(cnt, rec.min);
			maxFunc.set(cnt, rec.max);
//			upperFunc.set(cnt, rec.upper);
			upperFunc.set(cnt, rec.mean + rec.stdDev);
//			lowerFunc.set(cnt, rec.lower);
			lowerFunc.set(cnt, rec.mean - rec.stdDev);
			
			cnt++;
		}
		
		ArrayList<DiscretizedFunc> funcs = new ArrayList<DiscretizedFunc>();
		ArrayList<PlotCurveCharacterstics> chars = new ArrayList<PlotCurveCharacterstics>();
		
		funcs.add(meanFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 3, Color.BLACK));
		funcs.add(minFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.GREEN));
		funcs.add(maxFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.RED));
		funcs.add(upperFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLUE));
		funcs.add(lowerFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLUE));
		
		String title = "Rupture Rate Distribution";
		String xAxisLabel = "Mean Rate Rank";
		
//		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, title, chars);
//		gw.setX_AxisLabel(xAxisLabel);
//		gw.setYLog(true);
//		gw.getGraphWindow().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setYLog(true);
		gp.drawGraphPanel(xAxisLabel, "Rate", funcs, chars, false, title);
		File rankFile = new File(dir, prefix+"_rate_dist");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(rankFile.getAbsolutePath()+".pdf");
		gp.saveAsPNG(rankFile.getAbsolutePath()+".png");
		
		SimpleFaultSystemSolution meanSol = new SimpleFaultSystemSolution(rupSet, meanRates);
		meanSol.toZipFile(new File(dir, prefix+"_mean_sol.zip"));
		InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(meanSol);
		CommandLineInversionRunner.writeMFDPlots(invSol, dir, prefix);
		
		ArrayList<PaleoRateConstraint> paleoConstraints = CommandLineInversionRunner.getPaleoConstraints(meanSol.getFaultModel(), meanSol);
		CommandLineInversionRunner.writePaleoPlots(paleoConstraints, meanSol, dir, prefix+"_mean");
		
		BatchPlotGen.makeMapPlots(meanSol, dir, prefix+"_mean");
		
		// now make the std dev plots
		int numSects = rupSet.getNumSections();
		
		Region region = new CaliforniaRegions.RELM_TESTING();
//		for (int i=0; i<numRuns; i++) {
//			if (i % 10 == 0)
//				System.out.println("Getting slip rates for solution "+i+"/"+numRuns);
//			if (i % 25 == 0)
//				System.gc();
//			double[] myRates = new double[numRups];
//			for (int r=0; r<numRups; r++)
//				myRates[r] = rates[r][i];
//			FaultSystemSolution mySol = new SimpleFaultSystemSolution(rupSet, myRates);
//			mySol.copyCacheFrom(rupSet);
//			double[] mySlipRates = mySol.calcSlipRateForAllSects();
//			
//			for (int s=0; s<numSects;s++) {
//				slipRates[s][i] = mySlipRates[s];
//			}
//		}
		
		System.out.println("Making participation plots...");
		
		rupSet.getRupturesForSection(0); // this initializes the cache
		
		ArrayList<double[]> ranges = new ArrayList<double[]>();
		ranges.add(toArray(6, 7));
		ranges.add(toArray(7, 8));
		ranges.add(toArray(8, 10));
		ranges.add(toArray(6.7, 10));
		
		for (double[] range : ranges) {
			double magLow = range[0];
			double magHigh = range[1];
			
			System.out.println("Range: "+magLow+"=>"+magHigh);
			
			double[][] partRates = new double[numSects][numRuns];
			
			try {
				doThreaded(rates, partRates, true, magLow, magHigh, rupSet);
			} catch (InterruptedException e) {
				ExceptionUtils.throwAsRuntimeException(e);
			}
			
//			for (int i=0; i<numRuns; i++) {
//				if (i % 10 == 0)
//					System.out.println("Getting partic rates for solution "+i+"/"+numRuns);
//				if (i % 25 == 0)
//					System.gc();
//				double[] myRates = new double[numRups];
//				for (int r=0; r<numRups; r++)
//					myRates[r] = rates[r][i];
//				FaultSystemSolution mySol = new SimpleFaultSystemSolution(rupSet, myRates);
//				mySol.copyCacheFrom(rupSet);
//				double[] myPartRates = mySol.calcParticRateForAllSects(magLow, magHigh);
//				
//				for (int s=0; s<numSects;s++) {
//					partRates[s][i] = myPartRates[s];
//				}
//			}
			
			FaultBasedMapGen.plotParticipationStdDevs(rupSet, partRates, region, dir, prefix, false, magLow, magHigh);
		}
		
		System.out.println("Making slip std dev plot");
		double[][] slipRates = new double[numSects][numRuns];
		
		try {
			doThreaded(rates, slipRates, false, 0, 0, rupSet);
		} catch (InterruptedException e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		FaultBasedMapGen.plotSolutionSlipRateStdDevs(rupSet, slipRates, region, dir, prefix, false);
		System.exit(0);
	}
	
	private static class ComputeTask implements Task {
		
		private double[][] rates;
		private double[][] output;
		private int i;
		private boolean partic;
		private FaultSystemRupSet rupSet;
		private double magLow, magHigh;

		public ComputeTask(double[][] rates, double[][] output, int i, boolean partic, double magLow, double magHigh,
				FaultSystemRupSet rupSet) {
			super();
			this.rates = rates;
			this.output = output;
			this.i = i;
			this.partic = partic;
			this.rupSet = rupSet;
			this.magLow = magLow;
			this.magHigh = magHigh;
		}

		@Override
		public void compute() {
			double[] myRates = new double[rupSet.getNumRuptures()];
			for (int r=0; r<rupSet.getNumRuptures(); r++)
				myRates[r] = rates[r][i];
			FaultSystemSolution mySol = new SimpleFaultSystemSolution(rupSet, myRates);
			mySol.copyCacheFrom(rupSet);
			double[] myAnswer;
			if (partic)
				myAnswer = mySol.calcParticRateForAllSects(magLow, magHigh);
			else
				myAnswer = mySol.calcSlipRateForAllSects();
			
			for (int s=0; s<rupSet.getNumSections();s++) {
				output[s][i] = myAnswer[s];
			}
		}
		
	}
	
	private static void doThreaded(double[][] rates, double[][] output, boolean partic, double magLow, double magHigh, FaultSystemRupSet rupSet) throws InterruptedException {
		ArrayList<ComputeTask> tasks = new ArrayList<ManyRunCompilation.ComputeTask>();
		for (int i=0; i<output[0].length; i++)
			tasks.add(new ComputeTask(rates, output, i, partic, magLow, magHigh, rupSet));
		
		ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
		comp.computThreaded();
	}
	
	private static double[] toArray(double... vals) {
		return vals;
	}

}
