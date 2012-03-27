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

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.math.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import com.google.common.base.Preconditions;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.inversion.BatchPlotGen;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;

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
	
	private static class RateRecord implements Comparable<RateRecord> {
		
		double mean, min, max, stdDev;
		
		public RateRecord(double mean, double min, double max, double stdDev) {
			this.mean = mean;
			this.min = min;
			this.max = max;
			this.stdDev = stdDev;
		}

		@Override
		public int compareTo(RateRecord o) {
			return Double.compare(mean, o.mean);
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
		File dir = new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_03_22-unconstrained-run-like-crazy");
		File rupSetFile = new File(dir, "rupSet.zip");
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromZipFile(rupSetFile);
		int numRups = rupSet.getNumRuptures();
		System.out.println("Loaded rupSet with "+numRups+" ruptures");
		int numRuns = 1000;
		File zipFile = new File(dir, "FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Unconst_VarAseis0.2_VarOffAseis0.5_VarMFDMod1_VarNone_1000runs.zip");
		double[][] rates = loadRates(zipFile, numRups, numRuns);
		System.out.println("Loaded rates!");
		
		RateRecord[] rateRecords = new RateRecord[numRups];
		
		int numZeros = 0;
		double numZerosPer = 0;
		
		double[] meanRates = new double[numRups];
		for (int r=0; r<numRups; r++) {
			double[] rupRates = rates[r];
			double mean = StatUtils.mean(rupRates);
			for (double rate : rupRates)
				if (rate == 0)
					numZerosPer++;
			meanRates[r] = mean;
			if (mean == 0)
				numZeros++;
			double 	min = StatUtils.min(rupRates);
			double max = StatUtils.max(rupRates);
			double stdDev = Math.sqrt(StatUtils.variance(rupRates));
			
			rateRecords[r] = new RateRecord(mean, min, max, stdDev);
		}
		numZerosPer /= numRuns;
		System.out.println("num zeros: "+numZeros);
		System.out.println("avg zeros per run: "+numZerosPer);
		
		rates = null;
		
		Arrays.sort(rateRecords);
		
		EvenlyDiscretizedFunc meanFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc minFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc maxFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc stdDevAboveFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		EvenlyDiscretizedFunc stdDevBelowFunc = new EvenlyDiscretizedFunc(0d, numRups, 1d);
		
		int cnt = 0;
		for (int i=rateRecords.length; --i>=0;) {
			RateRecord rec = rateRecords[i];
			meanFunc.set(cnt, rec.mean);
			minFunc.set(cnt, rec.min);
			maxFunc.set(cnt, rec.max);
			stdDevAboveFunc.set(cnt, rec.mean + rec.stdDev);
			stdDevBelowFunc.set(cnt, rec.mean - rec.stdDev);
			
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
		funcs.add(stdDevAboveFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLUE));
		funcs.add(stdDevBelowFunc);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1, Color.BLUE));
		
		String title = "Rupture Rate Distribution";
		String xAxisLabel = "Mean Rate Rank";
		
		GraphiWindowAPI_Impl gw = new GraphiWindowAPI_Impl(funcs, title, chars);
		gw.setX_AxisLabel(xAxisLabel);
		gw.setYLog(true);
		
		String prefix = zipFile.getName().replaceAll(".zip", "");
		
		SimpleFaultSystemSolution meanSol = new SimpleFaultSystemSolution(rupSet, meanRates);
		meanSol.toZipFile(new File(dir, prefix+"_mean_sol.zip"));
		InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(meanSol);
		CommandLineInversionRunner.writeMFDPlots(invSol, dir, prefix);
		
		BatchPlotGen.makeMapPlots(meanSol, dir, prefix);
	}

}
