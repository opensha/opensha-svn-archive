package scratch.UCERF3;
import java.awt.Color;
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
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.util.FileNameComparator;
import org.opensha.commons.util.FileUtils;
import org.opensha.commons.util.threads.Task;
import org.opensha.commons.util.threads.ThreadedTaskComputer;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;

import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

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
	 * Returns the minimum rate from any solution for the given rupture
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getRateMin(int rupIndex) {
		return StatUtils.min(ratesByRup[rupIndex]);
	}
	
	/**
	 * Returns the maximum rate from any solution for the given rupture
	 * 
	 * @param rupIndex
	 * @return
	 */
	public double getRateMax(int rupIndex) {
		return StatUtils.max(ratesByRup[rupIndex]);
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
	public synchronized SimpleFaultSystemSolution getSolution(int solIndex) {
		Preconditions.checkArgument(solIndex >= 0 && solIndex < numSols, "");
		SimpleFaultSystemSolution sol = solsMap.get(solIndex);
		if (sol == null) {
			// make room in the cache
			while (solsMap.keySet().size() > 3)
				solsMap.remove(solsMap.keySet().iterator().next());
			sol = new SimpleFaultSystemSolution(this, ratesBySol[solIndex]);
			solsMap.put(solIndex, sol);
		}
		return sol;
	}
	
	public void clearSolCache() {
		solsMap.clear();
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
	
	public void writePaleoPlots(File dir) throws IOException {
		String prefix = new InversionFaultSystemSolution(this).getBranch().buildFileName();
		int digits = ((getNumSolutions()-1)+"").length();
		
		ArrayList<PaleoRateConstraint> paleoRateConstraints =
				UCERF3_PaleoRateConstraintFetcher.getConstraints(getFaultSectionDataList());
		
		for (int i=0; i<getNumSolutions(); i++) {
			SimpleFaultSystemSolution sol = getSolution(i);
			
			String runStr = i+"";
			while (runStr.length() < digits)
				runStr = "0"+runStr;
			
			String myPrefix = prefix+"_run"+runStr;
			
			if (CommandLineInversionRunner.doPaleoPlotsExist(dir, myPrefix))
				continue;
			
			CommandLineInversionRunner.writePaleoPlots(paleoRateConstraints, sol, dir, myPrefix);
		}
	}
	
	public void writePaleoBoundsPlot(File dir) throws IOException {
		String prefix = new InversionFaultSystemSolution(this).getBranch().buildFileName();
		
		ArrayList<PaleoRateConstraint> paleoRateConstraints =
				UCERF3_PaleoRateConstraintFetcher.getConstraints(getFaultSectionDataList());
		
		ArrayList<DiscretizedFunc> otherFuncs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> otherChars = Lists.newArrayList();
		ArrayList<EvenlyDiscretizedFunc> minFuncs = Lists.newArrayList();
		ArrayList<EvenlyDiscretizedFunc> maxFuncs = Lists.newArrayList();
		ArrayList<EvenlyDiscretizedFunc> meanFuncs = Lists.newArrayList();
		
		ArrayList<Integer> solFuncIndexes = Lists.newArrayList();
		
		for (int i=0; i<getNumSolutions(); i++) {
			SimpleFaultSystemSolution sol = getSolution(i);
			
			ArrayList<FaultSystemSolution> sols = Lists.newArrayList();
			sols.add(sol);
			PlotSpec spec = UCERF3_PaleoRateConstraintFetcher.getSegRateComparisonSpec(
					paleoRateConstraints, sols);
			
			ArrayList<? extends DiscretizedFunc> funcs = spec.getFuncs();
			
			if (i == 0) {
				for (int j=0; j<funcs.size(); j++) {
					DiscretizedFunc func = funcs.get(j);
					if (func.getInfo().contains("Solution")) {
						// this means that it is a solution line
						solFuncIndexes.add(j);
						continue;
					}
					otherFuncs.add(func);
					otherChars.add(spec.getChars().get(j));
				}
				
				for (int j=0; j<solFuncIndexes.size(); j++) {
					DiscretizedFunc func = funcs.get(solFuncIndexes.get(j));
					double min = func.getMinX();
					double max = func.getMaxX();
					int num = func.getNum();
					minFuncs.add(new EvenlyDiscretizedFunc(min, max, num));
					maxFuncs.add(new EvenlyDiscretizedFunc(min, max, num));
					meanFuncs.add(new EvenlyDiscretizedFunc(min, max, num));
				}
			}
			
			for (int j=0; j<solFuncIndexes.size(); j++) {
				DiscretizedFunc func = funcs.get(solFuncIndexes.get(j));
				
				EvenlyDiscretizedFunc minFunc = minFuncs.get(j);
				EvenlyDiscretizedFunc maxFunc = maxFuncs.get(j);
				EvenlyDiscretizedFunc meanFunc = meanFuncs.get(j);
				
				for (int k=0; k<func.getNum(); k++) {
					double val = func.getY(k);
					
					double minVal = minFunc.getY(k);
					if (minVal == 0 || val < minVal)
						minFunc.set(k, val);
					if (val > maxFunc.getY(k))
						maxFunc.set(k, val);
					meanFunc.set(k, meanFunc.getY(k)+val);
				}
			}
		}
		
		for (int i=0; i<meanFuncs.size(); i++) {
			EvenlyDiscretizedFunc meanFunc = meanFuncs.get(i);
			for (int index=0; index<meanFunc.getNum(); index++)
				meanFunc.set(index, meanFunc.getY(index) / (double)getNumSolutions());
		}
		
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		funcs.addAll(otherFuncs);
		chars.addAll(otherChars);
		
		PlotCurveCharacterstics meanChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED);
		PlotCurveCharacterstics minChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE);
		PlotCurveCharacterstics maxChar = new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE);
		
		for (int i=0; i<meanFuncs.size(); i++) {
			funcs.add(minFuncs.get(i));
			funcs.add(maxFuncs.get(i));
			funcs.add(meanFuncs.get(i));
			
			chars.add(minChar);
			chars.add(maxChar);
			chars.add(meanChar);
		}
		
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		
		gp.setYLog(true);
		
		gp.drawGraphPanel("", "Event Rate Per Year", funcs, chars, false, "Paleosiesmic Constraint Fit");
		File file = new File(dir, prefix+"_paleo_bounds");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
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
		File file = new File("/home/kevin/workspace/OpenSHA/dev/scratch/UCERF3/data/scratch/" +
				"InversionSolutions/FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_" +
				"SpatSeisU3_VarPaleo0.1_mean_sol.zip");
		
		AverageFaultSystemSolution avg = fromZipFile(file);
		File dir = new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_07_21-zeng-ref-lowpaleo-100runs/paleo");
		avg.writePaleoBoundsPlot(dir);
		avg.writePaleoPlots(dir);
		
//		File dir = new File("/home/kevin/OpenSHA/UCERF3/inversions/2012_04_30-fm2-a-priori-test/" +
//				"results/VarAPrioriZero_VarAPrioriWt1000_VarWaterlevel0");
//		SimpleFaultSystemRupSet rupSet = SimpleFaultSystemRupSet.fromFile(new File(dir, "rupSet.zip"));
//		String prefix = "FM2_1_UC2ALL_MaAvU2_DsrTap_DrAveU2_Char_VarAPrioriZero_VarAPrioriWt1000_VarWaterlevel0";
//		AverageFaultSystemSolution avg = fromDirectory(rupSet, dir, prefix);
//		
//		System.out.println("num solutions: "+avg.getNumSolutions());
//		
//		File avgFile = new File(dir, "avg_sol.zip");
//		avg.toZipFile(avgFile);
//		
//		AverageFaultSystemSolution avg2 = fromZipFile(avgFile);
//		Preconditions.checkState(avg2.getNumSolutions() == avg.getNumSolutions());
//		for (int r=0; r<rupSet.getNumRuptures(); r++) {
//			Preconditions.checkState(avg.getRateForRup(r) == avg2.getRateForRup(r));
//		}
//		
//		Preconditions.checkState(SimpleFaultSystemSolution.fromFileAsApplicable(avgFile) instanceof AverageFaultSystemSolution);
	}

}
