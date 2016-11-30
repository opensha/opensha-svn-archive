package scratch.kevin.ucerf3.etas;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.HistogramFunction;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class ETAS_CatalogEAL_MultiCombine {

	public static void main(String[] args) throws IOException {
		File fullTD_csvFile = null;
		File noERT_csvFile = null;
		File outputDir = null;
		String outputPrefix = null;
		int nForConf = -1;
		int catDuration = -1;
		if (args.length == 1 && args[0].equals("--hardcoded")) {
			File baseDir = new File("/home/kevin/OpenSHA/UCERF3/etas/simulations");
			File lossCombDir = new File(baseDir, "losses_combined");
			Preconditions.checkState(lossCombDir.exists() || lossCombDir.mkdir());
			
			File fullTD_dir = new File(baseDir,
					"2016_02_19-mojave_m7-10yr-full_td-subSeisSupraNucl-gridSeisCorr-scale1.14-combined100k/loss_results/combined");
			File noERT_dir = new File(baseDir,
					"2016_02_22-mojave_m7-10yr-no_ert-subSeisSupraNucl-gridSeisCorr-combined100k/loss_results/combined");
			nForConf = 100000;
			catDuration = -1;
			
			outputDir = new File(lossCombDir, "mojave_100k_both_models");
			Preconditions.checkState(outputDir.exists() || outputDir.mkdir());
			outputPrefix = "mojave_combined";
			
			fullTD_csvFile = new File(fullTD_dir, "gmpes_combined_exceed.csv");
			noERT_csvFile = new File(noERT_dir, "gmpes_combined_exceed.csv");
		} else if (args.length < 5 || args.length > 6) {
			System.err.println("USAGE: <FullTD-exceed-CSV> <NoERT-exceed-CSV> <output-dir> <output-prefix> <n-for-conf> [<cat-duration-for-all-sub>]");
			System.exit(2);
		} else {
			fullTD_csvFile = new File(args[0]);
			Preconditions.checkState(fullTD_csvFile.exists(), "Doesn't exist: %s", fullTD_csvFile.getAbsolutePath());
			noERT_csvFile = new File(args[1]);
			Preconditions.checkState(noERT_csvFile.exists(), "Doesn't exist: %s", noERT_csvFile.getAbsolutePath());
			outputDir = new File(args[2]);
			Preconditions.checkState(outputDir.exists() || outputDir.mkdir(), "Doesn't exist: %s", outputDir.getAbsolutePath());
			outputPrefix = args[3];
			nForConf = Integer.parseInt(args[4]);
			if (args.length == 6)
				catDuration = Integer.parseInt(args[5]);
		}
		
		boolean isLogX = false;
		boolean triggeredOnly = false;
		double maxX = 200;
		
		CSVFile<String> fullTD_csv = CSVFile.readFile(fullTD_csvFile, true);
		fullTD_csv = checkFixCSV(fullTD_csv, fullTD_csvFile);
		CSVFile<String> noERT_csv = CSVFile.readFile(noERT_csvFile, true);
		noERT_csv = checkFixCSV(noERT_csv, noERT_csvFile);
		
		if (fullTD_csvFile.getAbsolutePath().contains("coachella") || fullTD_csvFile.getAbsolutePath().contains("bernardino"))
			maxX = 50;
		
		Table<String, Double, HistogramFunction> lossHists = HashBasedTable.create();
		
		Map<Double, HistogramFunction> fullTD = loadCSV(fullTD_csv);
		Map<Double, HistogramFunction> noERT = loadCSV(noERT_csv);
		
		for (Double duration : fullTD.keySet())
			lossHists.put("FullTD", duration, fullTD.get(duration));
		for (Double duration : noERT.keySet())
			lossHists.put("NoERT", duration, noERT.get(duration));
		
		String xAxisLabel = fullTD_csv.get(0, 0);
		
		boolean allSubDurations = catDuration > 0;
		ETAS_CatalogEALCalculator.writeLossExceed(outputDir, outputPrefix, lossHists, true, isLogX,
				triggeredOnly, xAxisLabel, maxX, nForConf, catDuration, allSubDurations, true);
	}
	
	private static int realLineSize(List<String> line) {
		int size = 0;
		for (String val : line) {
			if (val == null || val.isEmpty())
				break;
			size++;
		}
		return size;
	}
	
	private static CSVFile<String> checkFixCSV(CSVFile<String> csv, File csvFile) throws IOException {
		int size1 = realLineSize(csv.getLine(0));
		int size2 = realLineSize(csv.getLine(1));
		System.out.println("Sizes: "+size1+" "+size2);
		if (size1 == 1+2*(size2-1)) {
			System.out.println("Fixing offset in: "+csvFile.getAbsolutePath());
			// needs fixing
			CSVFile<String> fixed = new CSVFile<String>(true);
			List<String> header = csv.getLine(0).subList(0, size2);
			fixed.addLine(header);
			for (int row=1; row<csv.getNumRows(); row++) {
				List<String> line = Lists.newArrayList();
				line.add(csv.get(row, 0)); // x value
				// get data from above
				int col;
				if (row == 1) {
					col = size2;
				} else {
					col = 1;
				}
				while (line.size() < size2)
					line.add(csv.get(row-1, col++));
				fixed.addLine(line);
			}
			csv.writeToFile(new File(csvFile.getAbsolutePath()+".bak"));
			fixed.writeToFile(csvFile);
			return fixed;
		}
		return csv;
	}
	
	private static Map<Double, HistogramFunction> loadCSV(CSVFile<String> csv) {
		Map<Double, HistogramFunction> hists = Maps.newHashMap();
		
		double min = Double.parseDouble(csv.get(1, 0));
		double max = Double.parseDouble(csv.get(csv.getNumRows()-1, 0));
		int num = csv.getNumRows()-1;
		for (double duration : ETAS_CatalogEALCalculator.durations) {
			String durStr = ETAS_CatalogEALCalculator.getDurationLabel(duration);
			int matchCol = -1;
			for (int col = 1; col<csv.getNumCols(); col++) {
				if (csv.get(0, col).equals(durStr)) {
					matchCol = col;
					break;
				}
			}
			if (matchCol < 0) {
				System.out.println("No match for duration: "+durStr);
				continue;
			}
			
			HistogramFunction hist = new HistogramFunction(min, max, num);
			for (int i=0; i<num; i++) {
				int row = i+1;
				double y = Double.parseDouble(csv.get(row, matchCol));
				if ((float)y == 0f)
					y = 0;
				if (y < 0 || y < 1e-12) {
					Preconditions.checkState(y > -1e-10);
					y = 0;
				}
				hist.set(i, y);
			}
			hists.put(duration, hist);
		}
		return hists;
	}

}
