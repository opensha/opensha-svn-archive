package scratch.UCERF3;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.math.stat.StatUtils;
import org.dom4j.DocumentException;
import org.opensha.commons.util.FileNameComparator;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;

import scratch.UCERF3.utils.MatrixIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * This class represents the average of multiple fault system solutions. Currently all solutions are weighted
 * equally.
 * 
 * @author kevin
 *
 */
public class AverageFaultSystemSolution extends SimpleFaultSystemSolution {
	
	private int numSols;
	private double[][] ratesByRup;
	private double[][] ratesBySol;
	
	private HashMap<Integer, SimpleFaultSystemSolution> solsMap = new HashMap<Integer, SimpleFaultSystemSolution>();
	
	private static double[][] toArrays(List<double[]> ratesList) {
		int numRups = ratesList.get(0).length;
		int numSols = ratesList.size();
		double[][] rates = new double[numRups][numSols];
		
		for (int s=0; s<numSols; s++) {
			double[] sol = ratesList.get(s);
			for (int r=0; r<numRups; r++) {
				rates[r][s] = sol[r];
			}
		}
		
		return rates;
	}
	
	@Override
	public void clearCache() {
		for (SimpleFaultSystemSolution sol : solsMap.values())
			sol.clearCache();
		super.clearCache();
	}

	private static double[] getMeanRates(double[][] rates) {
		double[] mean = new double[rates.length];
		
		for (int r=0; r<rates.length; r++)
			mean[r] = StatUtils.mean(rates[r]);
		
		return mean;
	}

	public AverageFaultSystemSolution(FaultSystemRupSet rupSet,
			List<double[]> ratesList) {
		this(rupSet, toArrays(ratesList));
	}
	
	/**
	 * @param rupSet
	 * @param rates 2 dimensional array of rates ordered by rupture index [numRups][numSols]
	 */
	public AverageFaultSystemSolution(FaultSystemRupSet rupSet,
			double[][] rates) {
		super(rupSet, getMeanRates(rates));
		
		this.ratesByRup = rates;
		
		String info = getInfoString();
		
		numSols = ratesByRup[0].length;
		int numRups = getNumRuptures();
		ratesBySol = new double[numSols][numRups];
		
		String newInfo = "";
		newInfo += "************** Average Fault System Solution *****************\n";
		newInfo += "Number of solutions averaged: "+numSols;
		newInfo += "**************************************************************\n";
		
		info = newInfo+"\n\n"+info;
		
		setInfoString(info);
		
		for (int s=0; s<numSols; s++) {
			for (int r=0; r<numRups; r++) {
				ratesBySol[s][r] = ratesByRup[r][s];
			}
		}
	}
	
	/**
	 * Calculates the standard deviation of rates for the given rupture
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getRateStdDev(int rupIndex) {
		return Math.sqrt(StatUtils.variance(ratesByRup[rupIndex], getRateForRup(rupIndex)));
	}
	
	/**
	 * Returns the number of solutions that constitute this average fault system solution
	 * 
	 * @return
	 */
	public int getNumSolutions() {
		return numSols;
	}
	
	/**
	 * Returns a double array containing all of the rates for the given solution
	 * 
	 * @param solIndex
	 * @return
	 */
	public double[] getRates(int solIndex) {
		return ratesBySol[solIndex];
	}
	
	/**
	 * Returns a SimpleFaultSystemSolution for the given solution index. Solutions are cached locally and only
	 * built once for each index.
	 * 
	 * @param solIndex
	 * @return
	 */
	public SimpleFaultSystemSolution getSolution(int solIndex) {
		Preconditions.checkArgument(solIndex >= 0 && solIndex < numSols, "");
		SimpleFaultSystemSolution sol = solsMap.get(solIndex);
		if (sol == null) {
			sol = new SimpleFaultSystemSolution(this, ratesBySol[solIndex]);
			solsMap.put(solIndex, sol);
		}
		return sol;
	}

	@Override
	public void toZipFile(File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		
		ArrayList<String> zipFileNames = new ArrayList<String>();
		
		File ratesFile = new File(tempDir, "rates.bin"); // mean rates
		MatrixIO.doubleArrayToFile(getRateForAllRups(), ratesFile);
		zipFileNames.add(ratesFile.getName());
		
		int digits = new String(""+(numSols-1)).length();
		for (int s=0; s<numSols; s++) {
			double[] rates = getRates(s);
			String rateStr = s+"";
			while (rateStr.length()<digits)
				rateStr = "0"+rateStr;
			File rateSubFile = new File(tempDir, "sol_rates_"+rateStr+".bin");
			MatrixIO.doubleArrayToFile(rates, rateSubFile);
			zipFileNames.add(rateSubFile.getName());
		}
		
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.toSimple(rupSet);
		simpleRupSet.toZipFile(file, tempDir, zipFileNames);
	}
	
	public double[][] calcParticRates(double magLow, double magHigh) throws InterruptedException {
		double[][] particRates = new double[getNumSections()][getNumSolutions()];
		
		calcThreaded(ratesByRup, particRates, true, magLow, magHigh, rupSet);
		
		return particRates;
	}
	
	public double[][] calcSlipRates() throws InterruptedException {
		double[][] particRates = new double[getNumSections()][getNumSolutions()];
		
		calcThreaded(ratesByRup, particRates, false, 0d, 10d, rupSet);
		
		return particRates;
	}
	
	public static void calcThreaded(double[][] rates, double[][] output, boolean partic, double magLow, double magHigh, FaultSystemRupSet rupSet) throws InterruptedException {
		ArrayList<AverageFaultSystemSolution.ParticipationComputeTask> tasks = new ArrayList<AverageFaultSystemSolution.ParticipationComputeTask>();
		for (int i=0; i<output[0].length; i++)
			tasks.add(new AverageFaultSystemSolution.ParticipationComputeTask(rates, output, i, partic, magLow, magHigh, rupSet));
		
		ThreadedTaskComputer comp = new ThreadedTaskComputer(tasks);
		comp.computThreaded();
	}

	public static AverageFaultSystemSolution fromZipFile(File zipFile)
	throws ZipException, IOException, DocumentException {
		SimpleFaultSystemRupSet simpleRupSet = SimpleFaultSystemRupSet.fromZipFile(zipFile);
		ZipFile zip = new ZipFile(zipFile);
		
		List<double[]> rates = Lists.newArrayList();
		
		ArrayList<? extends ZipEntry> entries = Collections.list(zip.entries());
		for (int i=entries.size(); --i>=0;)
			if (!entries.get(i).getName().startsWith("sol_rates_"))
				entries.remove(i);
		
		Collections.sort(entries, new SolRatesEntryComparator());
		
		// check the numbering
		String lastName = entries.get(entries.size()-1).getName();
		String lastNum = lastName.substring("sol_rates_".length());
		lastNum = lastNum.substring(0, lastNum.indexOf(".bin"));
		int numSols = Integer.parseInt(lastNum)+1;
		Preconditions.checkState(numSols == entries.size(), "Number of solutions incosistant, expected="+numSols
				+", actual="+entries.size()+" (inferred from last filename: "+lastName+")");
		
		int numRups = simpleRupSet.getNumRuptures();
		
		for (ZipEntry entry : entries) {
			double[] solRates = MatrixIO.doubleArrayFromInputStream(
					new BufferedInputStream(zip.getInputStream(entry)), entry.getSize());
			Preconditions.checkState(solRates.length == numRups,
					"Rate file is wrong size: "+solRates.length+" != "+numRups
					+" ("+entry.getName()+")");
			rates.add(solRates);
		}
		
		return new AverageFaultSystemSolution(simpleRupSet, rates);
	}
	
	private static class SolRatesEntryComparator implements Comparator<ZipEntry> {
		
		private Collator c = Collator.getInstance();

		@Override
		public int compare(ZipEntry o1, ZipEntry o2) {
			String n1 = o1.getName();
			String n2 = o2.getName();
			
			return c.compare(n1, n2);
		}
		
	}
	
	private static class ParticipationComputeTask implements Task {
		
		private double[][] rates;
		private double[][] output;
		private int i;
		private boolean partic;
		private FaultSystemRupSet rupSet;
		private double magLow, magHigh;
	
		public ParticipationComputeTask(double[][] rates, double[][] output, int i, boolean partic, double magLow, double magHigh,
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
			mySol.clearSolutionCacheOnly();
			
			for (int s=0; s<rupSet.getNumSections();s++) {
				output[s][i] = myAnswer[s];
			}
		}
		
	}

	public static AverageFaultSystemSolution fromDirectory(FaultSystemRupSet rupSet, File dir, String prefix) throws IOException {
		ArrayList<File> files = new ArrayList<File>();
		
		System.out.println("Loading average solution from: "+dir.getAbsolutePath());
		System.out.println("Prefix: "+prefix);
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
			String name = file.getName();
			if (!name.endsWith(".bin"))
				continue;
			if (!name.startsWith(prefix))
				continue;
			if (!name.contains("_run"))
				continue;
			if (name.contains("_noMinRates"))
				continue;
			
			files.add(file);
		}
		
		Collections.sort(files, new FileNameComparator());
		
		int numSols = files.size();
		Preconditions.checkState(numSols > 1, "must have at least 2 solutions! (found="+numSols+")");
		System.out.println("Loading "+numSols+" solutions!");
		int numRups = rupSet.getNumRuptures();
		
		List<double[]> rates = Lists.newArrayList();
		
		for (int i=0; i<numSols; i++) {
			double[] runRates = MatrixIO.doubleArrayFromFile(files.get(i));
			Preconditions.checkState(runRates.length == numRups,
					"Rate file is wrong size: "+runRates.length+" != "+numRups
					+" ("+files.get(i).getName()+")");
			
			rates.add(runRates);
		}
		
		return new AverageFaultSystemSolution(rupSet, rates);
	}
	
	public static void main(String[] args) throws IOException, DocumentException {
		File dir = new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_04_30-fm2-a-priori-test/" +
				"results/VarAPrioriZero_VarAPrioriWt1000_VarWaterlevel0");
		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(new File(dir, "rupSet.zip"));
		String prefix = "FM2_1_UC2ALL_MaAvU2_DsrTap_DrAveU2_Char_VarAPrioriZero_VarAPrioriWt1000_VarWaterlevel0";
		AverageFaultSystemSolution avg = fromDirectory(rupSet, dir, prefix);
		
		System.out.println("num solutions: "+avg.getNumSolutions());
		
		File avgFile = new File(dir, "avg_sol.zip");
		avg.toZipFile(avgFile);
		
		AverageFaultSystemSolution avg2 = fromZipFile(avgFile);
		Preconditions.checkState(avg2.getNumSolutions() == avg.getNumSolutions());
		for (int r=0; r<rupSet.getNumRuptures(); r++) {
			Preconditions.checkState(avg.getRateForRup(r) == avg2.getRateForRup(r));
		}
		
		Preconditions.checkState(SimpleFaultSystemSolution.fromFileAsApplicable(avgFile) instanceof AverageFaultSystemSolution);
	}

}
