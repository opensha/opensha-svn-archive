package scratch.kevin.ucerf3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.math3.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.MatrixIO;

public class BranchAverageRobustnessTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ZipException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws ZipException, IOException, DocumentException {
		File compoundSol = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/" +
				"scratch/InversionSolutions/2013_01_14-stampede_3p2_production_runs_combined_" +
				"MEAN_COMPOUND_SOL_with_indv_runs.zip");
		ZipFile zip = new ZipFile(compoundSol);
		
		List<double[]> ratesList = load(zip, FaultModels.FM3_1);
		FaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(
				new File(compoundSol.getParentFile(),
						"2013_01_14-stampede_3p2_production_runs_combined_FM3_1_MEAN_BRANCH_AVG_SOL.zip"));
		writeCSV(ratesList, new File("/tmp/fm_3_1_rate_stability.csv"), rupSet);
		ratesList = load(zip, FaultModels.FM3_2);
		rupSet = SimpleFaultSystemRupSet.fromFile(
				new File(compoundSol.getParentFile(),
						"2013_01_14-stampede_3p2_production_runs_combined_FM3_2_MEAN_BRANCH_AVG_SOL.zip"));
		writeCSV(ratesList, new File("/tmp/fm_3_2_rate_stability.csv"), rupSet);
	}
	
	private static List<double[]> load(ZipFile file, FaultModels fm) throws IOException {
		Map<String, List<double[]>> map = Maps.newHashMap();
		
		for (ZipEntry entry : Lists.newArrayList(Iterators.forEnumeration(file.entries()))) {
			String name = entry.getName();
			if (!name.contains("_rates_"))
				continue;
			
			if (!name.startsWith(fm.getShortName()))
				continue;
			
			String rates_sub = name.substring(name.indexOf("_rates_"));
			
			List<double[]> list = map.get(rates_sub);
			if (list == null) {
				list = Lists.newArrayList();
				map.put(rates_sub, list);
			}
			
			list.add(MatrixIO.doubleArrayFromInputStream(file.getInputStream(entry), entry.getSize()));
		}
		
		List<double[]> retList = Lists.newArrayList();
		for (String key : map.keySet()) {
			List<double[]> list = map.get(key);
			System.out.println("Averaging "+list.size()+" runs for: "+key);
			double[] vals = new double[list.get(0).length];
			int numSols = list.size();
			double rateMult = 1d/(double)numSols;
			for (int i=0; i<vals.length; i++) {
				double val = 0;
				for (double[] rates : list)
					val += rateMult * rates[i];
				vals[i] = val;
			}
			retList.add(vals);
		}
		
		map = null;
		System.gc();
		
		return retList;
	}
	
	private static void writeCSV(List<double[]> ratesList, File outputFile, FaultSystemRupSet rupSet)
			throws IOException {
		int numRates = ratesList.get(0).length;
		int numLists = ratesList.size();
		List<RateEntry> entries = Lists.newArrayList();
		
		for (int r=0; r<numRates; r++) {
			double[] rates = new double[numLists];
			for (int i=0; i<numLists; i++)
				rates[i] = ratesList.get(i)[r];
			entries.add(new RateEntry(r, rates));
		}
		
		EvenlyDiscretizedFunc normStdDevFunc = new EvenlyDiscretizedFunc(0d, numRates, 1d);
		EvenlyDiscretizedFunc normRateFunc = new EvenlyDiscretizedFunc(0d, numRates, 1d);
		EvenlyDiscretizedFunc normMoRateFunc = new EvenlyDiscretizedFunc(0d, numRates, 1d);
		
		Collections.sort(entries);
		
		CSVFile<String> csv = new CSVFile<String>(true);
		
		List<String> header = Lists.newArrayList(
				"Index", "Mean", "Std Dev", "Std Dev / Mean", "Min", "Max");
		
		csv.addLine(header);
		
		double rateSum = 0;
		double moRateSum = 0;
		for (int i=0; i<numRates; i++) {
			RateEntry rate = entries.get(i);
			List<String> line = Lists.newArrayList();
			
			line.add(rate.index+"");
			line.add(rate.mean+"");
			line.add(rate.stdDev+"");
			line.add(rate.normStdDev+"");
			line.add(StatUtils.min(rate.vals)+"");
			line.add(StatUtils.max(rate.vals)+"");
			
			csv.addLine(line);
			
			normStdDevFunc.set(i, rate.normStdDev);
			rateSum += rate.mean;
			normRateFunc.set(i, rateSum);
			moRateSum += rate.mean*FaultMomentCalc.getMoment(
					rupSet.getAreaForRup(rate.index), rupSet.getAveSlipForRup(rate.index));
			normMoRateFunc.set(i, moRateSum);
		}
		
		double rateNorm = normStdDevFunc.getMaxY() / normRateFunc.getMaxY();
		double moRateNorm = normStdDevFunc.getMaxY() / normMoRateFunc.getMaxY();
		normRateFunc.scale(rateNorm);
		normMoRateFunc.scale(moRateNorm);
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(normStdDevFunc);
		funcs.add(normRateFunc);
		funcs.add(normMoRateFunc);
		new GraphiWindowAPI_Impl(funcs, "Std Dev / Mean");
		
		csv.writeToFile(outputFile);
	}
	
	private static class RateEntry implements Comparable<RateEntry> {
		int index;
		double[] vals;
		double mean;
		double stdDev;
		double normStdDev;
		public RateEntry(int index, double[] vals) {
			this.index = index;
			this.vals = vals;
			mean = StatUtils.mean(vals);
			stdDev = Math.sqrt(StatUtils.variance(vals, mean));
			normStdDev = stdDev / mean;
		}
		@Override
		public int compareTo(RateEntry o) {
			return Double.compare(o.normStdDev, normStdDev);
		}
	}

}
