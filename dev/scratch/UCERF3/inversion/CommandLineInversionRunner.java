package scratch.UCERF3.inversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoRateConstraintFetcher;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CommandLineInversionRunner {
	
	public enum InversionOptions {
		DEFAULT_ASEISMICITY("aseis", "default-aseis", "Aseis", true,
				"Default Aseismicity Value"),
		A_PRIORI_CONST_FOR_ZERO_RATES("apz", "a-priori-zero", "APrioriZero", false,
				"Flag to apply a priori constraint to zero rate ruptures"),
		A_PRIORI_CONST_WT("apwt", "a-priori-wt", "APrioriWt", true, "A priori constraint weight"),
		WATER_LEVEL_FRACT("wtlv", "waterlevel", "Waterlevel", true, "Waterlevel fraction"),
		PARKFIELD_WT("pkfld", "parkfield-wt", "Parkfield", true, "Parkfield constraint weight"),
		PALEO_WT("paleo", "paleo-wt", "Paleo", true, "Paleoconstraint weight");
		
		private String shortArg, argName, fileName, description;
		private boolean hasOption;
		
		private InversionOptions(String shortArg, String argName, String fileName, boolean hasOption,
				String description) {
			this.shortArg = shortArg;
			this.argName = argName;
			this.fileName = fileName;
			this.hasOption = hasOption;
			this.description = description;
		}

		public String getShortArg() {
			return shortArg;
		}

		public String getArgName() {
			return argName;
		}
		
		public String getCommandLineArgs() {
			return getCommandLineArgs(null);
		}
		
		public String getCommandLineArgs(double option) {
			return getCommandLineArgs((float)option+"");
		}
		
		public String getCommandLineArgs(String option) {
			String args = "--"+argName;
			if (hasOption) {
				Preconditions.checkArgument(option != null && !option.isEmpty());
				args += " "+option;
			}
			return args;
		}
		
		public String getFileName() {
			return getFileName(null);
		}
		
		public String getFileName(double option) {
			return getFileName((float)option+"");
		}

		public String getFileName(String option) {
			if (hasOption) {
				Preconditions.checkArgument(option != null && !option.isEmpty());
				return fileName+option;
			}
			return fileName;
		}

		public boolean hasOption() {
			return hasOption;
		}
	}
	
	protected static Options createOptions() {
		Options ops = ThreadedSimulatedAnnealing.createOptionsNoInputs();
		
		for (InversionOptions invOp : InversionOptions.values()) {
			Option op = new Option(invOp.shortArg, invOp.argName, invOp.hasOption, invOp.description);
			op.setRequired(false);
			ops.addOption(op);
		}
		
		Option rupSetOp = new Option("branch", "branch-prefix", true, "Prefix for file names." +
				"Should be able to parse logic tree branch from this");
		rupSetOp.setRequired(true);
		ops.addOption(rupSetOp);
	
		Option lightweightOp = new Option("light", "lightweight", false, "Only write out a bin file for the solution." +
				"Leave the rup set if the prefix indicates run 0");
		lightweightOp.setRequired(false);
		ops.addOption(lightweightOp);
		
		Option dirOp = new Option("dir", "directory", true, "Directory to store inputs");
		dirOp.setRequired(true);
		ops.addOption(dirOp);
		
		return ops;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				ClassUtils.getClassNameWithoutPackage(CommandLineInversionRunner.class),
				options, true );
		System.exit(2);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = createOptions();
		
		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			
			boolean lightweight = cmd.hasOption("lightweight");
			
			// get the directory/logic tree branch
			File dir = new File(cmd.getOptionValue("directory"));
			if (!dir.exists())
				dir.mkdir();
			String prefix = cmd.getOptionValue("branch-prefix");
			LogicTreeBranch branch = LogicTreeBranch.fromFileName(prefix);
			Preconditions.checkState(branch.isFullySpecified(),
					"Branch is not fully fleshed out! Prefix: "+prefix+", branch: "+branch);
			
			LaughTestFilter laughTest = LaughTestFilter.getDefault();
			String aseisArg = InversionOptions.DEFAULT_ASEISMICITY.argName;
			double defaultAseis = InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE;
			if (cmd.hasOption(aseisArg)) {
				String aseisVal = cmd.getOptionValue(aseisArg);
				defaultAseis = Double.parseDouble(aseisVal);
			}
			
			// first build the rupture set
			System.out.println("Building RupSet");
			InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					laughTest, defaultAseis, branch);
			
			// now build the inversion inputs
			
			// mfd relax flag
			double mfdEqualityConstraintWt = InversionConfiguration.DEFAULT_MFD_EQUALITY_WT;
			double mfdInequalityConstraintWt = InversionConfiguration.DEFAULT_MFD_INEQUALITY_WT;
			
			if (branch.getValue(MomentRateFixes.class).isRelaxMFD()) {
				mfdEqualityConstraintWt = 1;
				mfdInequalityConstraintWt = 1;
			}
			
			System.out.println("Building Inversion Configuration");
			InversionConfiguration config = InversionConfiguration.forModel(branch.getValue(InversionModels.class),
					rupSet, mfdEqualityConstraintWt, mfdInequalityConstraintWt);
			
			if (cmd.hasOption(InversionOptions.A_PRIORI_CONST_WT.argName)) {
				double wt = Double.parseDouble(cmd.getOptionValue(InversionOptions.A_PRIORI_CONST_WT.argName));
				System.out.println("Setting a priori constraint wt: "+wt);
				config.setRelativeRupRateConstraintWt(wt);
			}
			
			if (cmd.hasOption(InversionOptions.WATER_LEVEL_FRACT.argName)) {
				double fract = Double.parseDouble(cmd.getOptionValue(InversionOptions.WATER_LEVEL_FRACT.argName));
				System.out.println("Setting waterlevel fract: "+fract);
				config.setMinimumRuptureRateFraction(fract);
			}
			
			if (cmd.hasOption(InversionOptions.PARKFIELD_WT.argName)) {
				double wt = Double.parseDouble(cmd.getOptionValue(InversionOptions.PARKFIELD_WT.argName));
				System.out.println("Setting parkfield constraint wt: "+wt);
				config.setRelativeParkfieldConstraintWt(wt);
			}
			
			if (cmd.hasOption(InversionOptions.PALEO_WT.argName)) {
				double wt = Double.parseDouble(cmd.getOptionValue(InversionOptions.PALEO_WT.argName));
				System.out.println("Setting paleo constraint wt: "+wt);
				config.setRelativePaleoRateWt(wt);
			}
			
			ArrayList<PaleoRateConstraint> paleoRateConstraints = getPaleoConstraints(branch.getValue(FaultModels.class), rupSet);
			
			PaleoProbabilityModel paleoProbabilityModel =
				PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();
			
			InversionInputGenerator gen = new InversionInputGenerator(rupSet, config,
					paleoRateConstraints, null, paleoProbabilityModel);
			
			if (cmd.hasOption(InversionOptions.A_PRIORI_CONST_FOR_ZERO_RATES.argName)) {
				System.out.println("Setting a prior constraint for zero rates");
				gen.setAPrioriConstraintForZeroRates(true);
			}
			
			System.out.println("Building Inversion Inputs");
			gen.generateInputs();
			
			System.out.println("Writing RupSet");
			config.updateRupSetInfoString(rupSet);
			String info = rupSet.getInfoString();
			
			File rupSetFile = new File(dir, prefix+"_rupSet.zip");
			new SimpleFaultSystemRupSet(rupSet).toZipFile(rupSetFile);
			// now clear it out of memory
			config = null;
			rupSet = null;
			gen.setRupSet(null);
			System.gc();
			
			System.out.println("Column Compressing");
			gen.columnCompress();
			
			DoubleMatrix2D A = gen.getA();
			double[] d = gen.getD();
			double[] initialState = gen.getInitial();
			DoubleMatrix2D A_ineq = gen.getA_ineq();
			double[] d_ineq = gen.getD_ineq();
			double[] minimumRuptureRates = gen.getMinimumRuptureRates();
			List<Integer> rangeEndRows = gen.getRangeEndRows();
			List<String> rangeNames = gen.getRangeNames();
			
			for (int i=0; i<rangeEndRows.size(); i++) {
				System.out.println(i+". "+rangeNames.get(i)+": "+rangeEndRows.get(i));
			}
			
			gen = null;
			System.gc();
			
			System.out.println("Creating TSA");
			ThreadedSimulatedAnnealing tsa = ThreadedSimulatedAnnealing.parseOptions(cmd, A, d,
					initialState, A_ineq, d_ineq, minimumRuptureRates, rangeEndRows, rangeNames);
			initialState = Arrays.copyOf(initialState, initialState.length);
			CompletionCriteria criteria = ThreadedSimulatedAnnealing.parseCompletionCriteria(cmd);
			if (!(criteria instanceof ProgressTrackingCompletionCriteria)) {
				File csvFile = new File(dir, prefix+".csv");
				criteria = new ProgressTrackingCompletionCriteria(criteria, csvFile);
			}
			System.out.println("Starting Annealing");
			tsa.iterate(criteria);
			System.out.println("Annealing DONE");
			info += "\n";
			info += "\n****** Simulated Annealing Metadata ******";
			info += "\n"+tsa.getMetadata(args, criteria);
			// add perturbation info
			ProgressTrackingCompletionCriteria pComp = (ProgressTrackingCompletionCriteria)criteria;
			long numPerturbs = pComp.getPerturbs().get(pComp.getPerturbs().size()-1);
			int numRups = initialState.length;
			info += "\nAvg Perturbs Per Rup: "+numPerturbs+"/"+numRups+" = "
						+((double)numPerturbs/(double)numRups);
			int rupsPerturbed = 0;
			double[] solution_no_min_rates = tsa.getBestSolution();
			for (int i=0; i<numRups; i++)
				if ((float)solution_no_min_rates[i] != (float)initialState[i])
					rupsPerturbed++;
			info += "\nNum rups actually perturbed: "+rupsPerturbed+"/"+numRups+" ("
					+(float)(100d*((double)rupsPerturbed/(double)numRups))+" %)";
			info += "\nAvg Perturbs Per Perturbed Rup: "+numPerturbs+"/"+rupsPerturbed+" = "
					+((double)numPerturbs/(double)rupsPerturbed);
			info += "\n******************************************";
			System.out.println("Writing solution bin files");
			tsa.writeBestSolution(new File(dir, prefix+".bin"));
			
			if (!lightweight) {
				System.out.println("Loading RupSet");
				FaultSystemRupSet loadedRupSet = SimpleFaultSystemRupSet.fromZipFile(rupSetFile);
				loadedRupSet.setInfoString(info);
				double[] rupRateSolution = tsa.getBestSolution();
				rupRateSolution = InversionInputGenerator.adjustSolutionForMinimumRates(
						rupRateSolution, minimumRuptureRates);
				SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(loadedRupSet, rupRateSolution);
				
				File solutionFile = new File(dir, prefix+"_sol.zip");
				
				// add moments to info string
				info += "\n\nOriginal File Name: "+solutionFile.getName()
						+"\nNum Ruptures: "+loadedRupSet.getNumRuptures();
				info += "\nOrig (creep reduced) Fault Moment Rate: "+loadedRupSet.getTotalOrigMomentRate();
				double momRed = loadedRupSet.getTotalMomentRateReduction();
				info += "\nMoment Reduction (subseismogenic & coupling coefficient): "+momRed;
				info += "\nMoment Reduction Fraction: "+loadedRupSet.getTotalMomentRateReductionFraction();
				info += "\nFault Moment Rate: "
						+loadedRupSet.getTotalReducedMomentRate();
				double totalSolutionMoment = sol.getTotalFaultSolutionMomentRate();
				info += "\nFault Solution Moment Rate: "+totalSolutionMoment;
				
				InversionFaultSystemSolution invSol = new InversionFaultSystemSolution(sol);
				
//				double totalOffFaultMomentRate = invSol.getTotalOffFaultSeisMomentRate(); // TODO replace - what is off fault moment rate now?
//				info += "\nTotal Off Fault Seis Moment Rate (excluding subseismogenic): "
//						+(totalOffFaultMomentRate-momRed);
//				info += "\nTotal Off Fault Seis Moment Rate (inluding subseismogenic): "
//						+totalOffFaultMomentRate;
				info += "\nTotal Moment Rate From Off Fault MFD: "+invSol.getImpliedOffFaultStatewideMFD().getTotalMomentRate();
//				info += "\nTotal Model Seis Moment Rate: "
//						+(totalOffFaultMomentRate+totalSolutionMoment);

				int numNonZeros = 0;
				for (double rate : sol.getRateForAllRups())
					if (rate != 0)
						numNonZeros++;
				float percent = (float)numNonZeros / loadedRupSet.getNumRuptures() * 100f;
				info += "\nNum Non-Zero Rups: "+numNonZeros+"/"+loadedRupSet.getNumRuptures()+" ("+percent+" %)";
				
				// parent fault moment rates
				ArrayList<ParentMomentRecord> parentMoRates = getSectionMoments(sol);
				info += "\n\n****** Larges Moment Rate Discrepancies ******";
				for (int i=0; i<10 && i<parentMoRates.size(); i++) {
					ParentMomentRecord p = parentMoRates.get(i);
					info += "\n"+p.parentID+". "+p.name+"\ttarget: "+p.targetMoment
							+"\tsolution: "+p.solutionMoment+"\tdiff: "+p.getDiff();
				}
				info += "\n**********************************************";
				CSVFile<String> moRateCSV = new CSVFile<String>(true);
				moRateCSV.addLine(Lists.newArrayList("ID", "Name", "Target", "Solution", "Diff"));
				for (ParentMomentRecord p : parentMoRates)
					moRateCSV.addLine(Lists.newArrayList(p.parentID+"", p.name, p.targetMoment+"",
							p.solutionMoment+"", p.getDiff()+""));
				moRateCSV.writeToFile(new File(dir, prefix+"_sect_mo_rates.csv"));
				
				sol.setInfoString(info);
				
				System.out.println("Writing solution");
				sol.toZipFile(solutionFile);
				
				System.out.println("Writing Plots");
				tsa.writePlots(criteria, new File(dir, prefix));
				
				// MFD plots
				try {
					writeMFDPlots(invSol, dir, prefix);
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					writePaleoPlots(paleoRateConstraints, invSol, dir, prefix);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			FileWriter fw = new FileWriter(new File(dir, prefix+"_metadata.txt"));
			fw.write(info);
			fw.close();
			
			System.out.println("Deleting RupSet (norelativePaleoRateWt longer needed)");
			rupSetFile.delete();
		} catch (MissingOptionException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("DONE");
		System.exit(0);
	}
	
	public static void writeMFDPlots(InversionFaultSystemSolution invSol, File dir, String prefix) throws IOException {
		UCERF2_MFD_ConstraintFetcher ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();
		
		List<MFD_InversionConstraint> origMFDConstraints = invSol.getPlotOriginalMFDConstraints(ucerf2Fetch);
		
		int cnt = 0;
		for (MFD_InversionConstraint constraint : origMFDConstraints) {
			cnt++;
			HeadlessGraphPanel gp = invSol.getHeadlessMFDPlot(constraint, ucerf2Fetch);
			Region reg = constraint.getRegion();
			String regName = reg.getName();
			if (regName == null || regName.isEmpty())
				regName = "Uknown"+cnt;
			regName = regName.replaceAll(" ", "_");
			File file = new File(dir, prefix+"_MFD_"+regName);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
		}
	}
	
	public static ArrayList<PaleoRateConstraint> getPaleoConstraints(FaultModels fm, FaultSystemRupSet rupSet) throws IOException {
		if (fm == FaultModels.FM2_1)
			return UCERF2_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
		return UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
	}
	
	public static void writePaleoPlots(ArrayList<PaleoRateConstraint> paleoRateConstraints, FaultSystemSolution sol,
			File dir, String prefix)
			throws IOException {
		writePaleoPlots(paleoRateConstraints, Lists.newArrayList(sol), dir, prefix);
	}
	
	public static void writePaleoPlots(ArrayList<PaleoRateConstraint> paleoRateConstraints, ArrayList<FaultSystemSolution> sols,
			File dir, String prefix)
			throws IOException {
		HeadlessGraphPanel gp = UCERF3_PaleoRateConstraintFetcher.getHeadlessSegRateComparison(
				paleoRateConstraints, sols, true);
		
		File file = new File(dir, prefix+"_paleo_fit");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
	}
	
	private static ArrayList<ParentMomentRecord> getSectionMoments(FaultSystemSolution sol) {
		HashMap<Integer, ParentMomentRecord> map = Maps.newHashMap();
		
		for (int sectIndex=0; sectIndex<sol.getNumSections(); sectIndex++) {
			FaultSectionPrefData sect = sol.getFaultSectionData(sectIndex);
			int parent = sect.getParentSectionId();
			if (!map.containsKey(parent)) {
				String name = sect.getName();
				if (name.contains(", Subsection"))
					name = name.substring(0, name.indexOf(", Subsection"));
				map.put(parent, new ParentMomentRecord(parent, name, 0, 0));
			}
			ParentMomentRecord rec = map.get(parent);
			double targetMo = sol.getReducedMomentRate(sectIndex);
			double solSlip = sol.calcSlipRateForSect(sectIndex);
			double solMo = FaultMomentCalc.getMoment(sol.getAreaForSection(sectIndex), solSlip);
			if (!Double.isNaN(targetMo))
				rec.targetMoment += targetMo;
			if (!Double.isNaN(solMo))
				rec.solutionMoment += solMo;
		}
		
		ArrayList<ParentMomentRecord> recs =
				new ArrayList<CommandLineInversionRunner.ParentMomentRecord>(map.values());
		Collections.sort(recs);
		Collections.reverse(recs);
		return recs;
	}
	
	private static class ParentMomentRecord implements Comparable<ParentMomentRecord> {
		int parentID;
		String name;
		double targetMoment;
		double solutionMoment;
		public ParentMomentRecord(int parentID, String name,
				double targetMoment, double solutionMoment) {
			super();
			this.parentID = parentID;
			this.name = name;
			this.targetMoment = targetMoment;
			this.solutionMoment = solutionMoment;
		}
		public double getDiff() {
			return targetMoment - solutionMoment;
		}
		@Override
		public int compareTo(ParentMomentRecord o) {
			return Double.compare(Math.abs(getDiff()), Math.abs(o.getDiff()));
		}
	}

}
