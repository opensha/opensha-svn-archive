package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.StatUtils;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.hpc.mpj.taskDispatch.MPJTaskCalculator;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.AverageFaultSystemSolution;
import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.analysis.CompoundFSSPlots;
import scratch.UCERF3.analysis.FaultSpecificSegmentationPlotGen;
import scratch.UCERF3.analysis.FaultSystemRupSetCalc;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.ProgressTrackingCompletionCriteria;
import scratch.UCERF3.utils.IDPairing;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.RELM_RegionUtils;
import scratch.UCERF3.utils.UCERF2_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher;
import scratch.UCERF3.utils.OLD_UCERF3_MFD_ConstraintFetcher.TimeAndRegion;
import scratch.UCERF3.utils.UCERF2_Section_MFDs.UCERF2_Section_MFDsCalc;
import scratch.UCERF3.utils.aveSlip.AveSlipConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoFitPlotter;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoSiteCorrelationData;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF2_PaleoRateConstraintFetcher;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

public class CommandLineInversionRunner {

	public enum InversionOptions {
		DEFAULT_ASEISMICITY("aseis", "default-aseis", "Aseis", true,
		"Default Aseismicity Value"),
		A_PRIORI_CONST_FOR_ZERO_RATES("apz", "a-priori-zero", "APrioriZero", false,
		"Flag to apply a priori constraint to zero rate ruptures"),
		A_PRIORI_CONST_WT("apwt", "a-priori-wt", "APrioriWt", true, "A priori constraint weight"),
		WATER_LEVEL_FRACT("wtlv", "waterlevel", "Waterlevel", true, "Waterlevel fraction"),
		PARKFIELD_WT("pkfld", "parkfield-wt", "Parkfield", true, "Parkfield constraint weight"),
		PALEO_WT("paleo", "paleo-wt", "Paleo", true, "Paleoconstraint weight"),
		AVE_SLIP_WT("aveslip", "ave-slip-wt", "AveSlip", true, "Ave slip weight"),
		//		NO_SUBSEIS_RED("nosub", "no-subseismo", "NoSubseismo", false,
		//				"Flag to turn off subseimogenic reductions"),
		MFD_WT("mfd", "mfd-wt", "MFDWt", true, "MFD constraint weight"),
		INITIAL_ZERO("zeros", "initial-zeros", "Zeros", false, "Force initial state to zeros"),
		INITIAL_RANDOM("random", "initial-random", "RandStart", false, "Force initial state to random distribution"),
		EVENT_SMOOTH_WT("eventsm", "event-smooth-wt", "EventSmoothWt", true, "Relative Event Rate Smoothness weight"),
		SECTION_NUCLEATION_MFD_WT("nuclwt", "sect-nucl-mfd-wt", "SectNuclMFDWt", true,
				"Relative section nucleation MFD constraint weight"),
		MFD_TRANSITION_MAG("mfdtrans", "mfd-trans-mag", "MFDTrans", true, "MFD transition magnitude"),
		MFD_SMOOTHNESS_WT("mfdsmooth", "mfd-smooth-wt", "Smooth", true, "MFD smoothness constraint weight"),
		PALEO_SECT_MFD_SMOOTH("paleomfdsmooth", "paleo-sect-mfd-smooth", "SmoothPaleoSect", true,
				"MFD smoothness constraint weight for peleo parent sects"),
		REMOVE_OUTLIER_FAULTS("removefaults", "remove-faults", "RemoveFaults", false, "Remove some outlier high slip faults."),
		SLIP_WT("slipwt", "slip-wt", "SlipWt", true, "Slip rate constraint wt"),
		SERIAL("serial", "force-serial", "Serial", false, "Force serial annealing"),
		SYNTHETIC("syn", "synthetic", "Synthetic", false, "Synthetic data from solution rates named syn.bin.");

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
		
		Option noPlotsOp = new Option("noplots", "no-plots", false,
				"Flag to disable any plots (but still write solution zip file)");
		noPlotsOp.setRequired(false);
		ops.addOption(noPlotsOp);

		return ops;
	}

	public static void printHelp(Options options, boolean mpj) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				ClassUtils.getClassNameWithoutPackage(CommandLineInversionRunner.class),
				options, true );
		if (mpj)
			MPJTaskCalculator.abortAndExit(2);
		else
			System.exit(2);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		run(args, false);
		System.out.println("DONE");
		System.exit(0);
	}
	
	public static void run(String[] args, boolean mpj) {
		Options options = createOptions();

		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);

			boolean lightweight = cmd.hasOption("lightweight");
			boolean noPlots = cmd.hasOption("no-plots");

			// get the directory/logic tree branch
			File dir = new File(cmd.getOptionValue("directory"));
			if (!dir.exists())
				dir.mkdir();
			String prefix = cmd.getOptionValue("branch-prefix");
			LogicTreeBranch branch = LogicTreeBranch.fromFileName(prefix);
			Preconditions.checkState(branch.isFullySpecified(),
					"Branch is not fully fleshed out! Prefix: "+prefix+", branch: "+branch);

			File subDir = new File(dir, prefix);
			if (!subDir.exists())
				subDir.mkdir();

			LaughTestFilter laughTest = LaughTestFilter.getDefault();
			String aseisArg = InversionOptions.DEFAULT_ASEISMICITY.argName;
			double defaultAseis = InversionFaultSystemRupSetFactory.DEFAULT_ASEIS_VALUE;
			if (cmd.hasOption(aseisArg)) {
				String aseisVal = cmd.getOptionValue(aseisArg);
				defaultAseis = Double.parseDouble(aseisVal);
			}

			// flag for disabling sub seismogenic moment reductions
			//			InversionFaultSystemRupSet.applySubSeismoMomentReduction = !cmd.hasOption(InversionOptions.NO_SUBSEIS_RED.argName);


			// first build the rupture set
			System.out.println("Building RupSet");
			if (cmd.hasOption("remove-faults")) {
				HashSet<Integer> sectionsToIgnore = new HashSet<Integer>();
				sectionsToIgnore.add(13); // mendocino
				sectionsToIgnore.add(97); // imperial
				sectionsToIgnore.add(172); // cerro prieto
				sectionsToIgnore.add(104); // laguna salada
				laughTest.setParentSectsToIgnore(sectionsToIgnore);
			}
			InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					laughTest, defaultAseis, branch);
			System.out.println("Num rups: "+rupSet.getNumRuptures());

			// store distances for jump plot later
			Map<IDPairing, Double> distsMap = rupSet.getSubSectionDistances();

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
					rupSet, mfdEqualityConstraintWt, mfdInequalityConstraintWt, cmd);

			ArrayList<PaleoRateConstraint> paleoRateConstraints = getPaleoConstraints(branch.getValue(FaultModels.class), rupSet);

			PaleoProbabilityModel paleoProbabilityModel =
				InversionInputGenerator.loadDefaultPaleoProbabilityModel();

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
			info += "\n\n"+getPreInversionInfo(rupSet);
			

			File rupSetFile = new File(subDir, prefix+"_rupSet.zip");
			new SimpleFaultSystemRupSet(rupSet).toZipFile(rupSetFile);
			// now clear it out of memory
			rupSet = null;
			gen.setRupSet(null);
			System.gc();

			System.out.println("Column Compressing");
			gen.columnCompress();

			DoubleMatrix2D A = gen.getA();
			double[] d = gen.getD();
			double[] initialState = gen.getInitial();
			if (cmd.hasOption(InversionOptions.INITIAL_ZERO.argName))
				initialState = new double[initialState.length];
			if (cmd.hasOption(InversionOptions.INITIAL_RANDOM.argName)) {
				initialState = new double[initialState.length];
				// random rate from to^-10 => 10^2
				double minExp = -6;
				double maxExp = -10;
				
				double deltaExp = maxExp - minExp;
				
				for (int r=0; r<initialState.length; r++)
					initialState[r] = Math.pow(10d, Math.random() * deltaExp + minExp);
			}
			DoubleMatrix2D A_ineq = gen.getA_ineq();
			double[] d_ineq = gen.getD_ineq();
			double[] minimumRuptureRates = gen.getMinimumRuptureRates();
			List<Integer> rangeEndRows = gen.getRangeEndRows();
			List<String> rangeNames = gen.getRangeNames();
			
			if (cmd.hasOption(InversionOptions.SYNTHETIC.argName)) {
				double[] synrates = MatrixIO.doubleArrayFromFile(new File(dir, "syn.bin"));
				Preconditions.checkState(synrates.length == initialState.length,
						"synthetic starting solution has different num rups!");
				// subtract min rates
				synrates = gen.adjustSolutionForMinimumRates(synrates);
				
				DoubleMatrix1D synMatrix = new DenseDoubleMatrix1D(synrates);
				
				DenseDoubleMatrix1D syn = new DenseDoubleMatrix1D(A.rows());
				A.zMult(synMatrix, syn);
				
				double[] d_syn = syn.elements();
				
				Preconditions.checkState(d.length == d_syn.length,
						"D and D_syn lengths tdon't match!");
				
				List<int[]> rangesToCopy = Lists.newArrayList();
				
				for (int i=0; i<rangeNames.size(); i++) {
					String name = rangeNames.get(i);
					boolean keep = false;
					if (name.equals("Slip Rate"))
						keep = true;
					else if (name.equals("Paleo Event Rates"))
						keep = true;
					else if (name.equals("Paleo Slips"))
						keep = true;
					else if (name.equals("MFD Equality"))
						keep = true;
					else if (name.equals("MFD Nucleation"))
						keep = true;
					else if (name.equals("Parkfield"))
						keep = true;
					
					if (keep) {
						int prevRow;
						if (i == 0)
							prevRow = 0;
						else
							prevRow = rangeEndRows.get(i-1) + 1;
						int[] range = { prevRow, rangeEndRows.get(i) };
						
						rangesToCopy.add(range);
					}
				}
				
				for (int[] range : rangesToCopy) {
					System.out.println("Copying range "+range[0]+" => "+range[1]+" from syn to D");
					for (int i=range[0]; i<=range[1]; i++)
						d[i] = d_syn[i];
				}
				
				// copy over slip rate
			}

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
			if (cmd.hasOption(InversionOptions.SERIAL.argName)) {
				// this forces serial annealing by setting the sub completion criteria to the
				// general completion criteria
				((ProgressTrackingCompletionCriteria)criteria).setIterationModulus(10000l);
				tsa.setSubCompletionCriteria(criteria);
				tsa.setNumThreads(1);
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
			int numAboveWaterlevel =  0;
			for (int i=0; i<numRups; i++) {
				if ((float)solution_no_min_rates[i] != (float)initialState[i])
					rupsPerturbed++;
				if (solution_no_min_rates[i] > 0)
					numAboveWaterlevel++;
			}
			info += "\nNum rups actually perturbed: "+rupsPerturbed+"/"+numRups+" ("
			+(float)(100d*((double)rupsPerturbed/(double)numRups))+" %)";
			info += "\nAvg Perturbs Per Perturbed Rup: "+numPerturbs+"/"+rupsPerturbed+" = "
			+((double)numPerturbs/(double)rupsPerturbed);
			info += "\nNum rups above waterlevel: "+numAboveWaterlevel+"/"+numRups+" ("
			+(float)(100d*((double)numAboveWaterlevel/(double)numRups))+" %)";
			info += "\n******************************************";
			System.out.println("Writing solution bin files");
			tsa.writeBestSolution(new File(subDir, prefix+".bin"));

			if (!lightweight) {
				System.out.println("Loading RupSet");
				FaultSystemRupSet loadedRupSet = SimpleFaultSystemRupSet.fromZipFile(rupSetFile);
				loadedRupSet.setInfoString(info);
				double[] rupRateSolution = tsa.getBestSolution();
				rupRateSolution = InversionInputGenerator.adjustSolutionForMinimumRates(
						rupRateSolution, minimumRuptureRates);
				SimpleFaultSystemSolution sol = new SimpleFaultSystemSolution(loadedRupSet, rupRateSolution);

				File solutionFile = new File(subDir, prefix+"_sol.zip");

				// add moments to info string
				info += "\n\n****** Moment and Rupture Rate Metatdata ******";
				info += "\nOriginal File Name: "+solutionFile.getName()
				+"\nNum Ruptures: "+loadedRupSet.getNumRuptures();
				int numNonZeros = 0;
				for (double rate : sol.getRateForAllRups())
					if (rate != 0)
						numNonZeros++;
				float percent = (float)numNonZeros / loadedRupSet.getNumRuptures() * 100f;
				info += "\nNum Non-Zero Rups: "+numNonZeros+"/"+loadedRupSet.getNumRuptures()+" ("+percent+" %)";
				info += "\nOrig (creep reduced) Fault Moment Rate: "+loadedRupSet.getTotalOrigMomentRate();
				double momRed = loadedRupSet.getTotalMomentRateReduction();
				info += "\nMoment Reduction (subseismogenic & coupling coefficient): "+momRed;
				info += "\nMoment Reduction Fraction: "+loadedRupSet.getTotalMomentRateReductionFraction();
				info += "\nFault Moment Rate: "
					+loadedRupSet.getTotalReducedMomentRate();
				double totalSolutionMoment = sol.getTotalFaultSolutionMomentRate();
				info += "\nFault Solution Moment Rate: "+totalSolutionMoment;

				InversionFaultSystemSolution invSol;
				try {
					invSol = new InversionFaultSystemSolution(sol);

					//					double totalOffFaultMomentRate = invSol.getTotalOffFaultSeisMomentRate(); // TODO replace - what is off fault moment rate now?
					//					info += "\nTotal Off Fault Seis Moment Rate (excluding subseismogenic): "
					//							+(totalOffFaultMomentRate-momRed);
					//					info += "\nTotal Off Fault Seis Moment Rate (inluding subseismogenic): "
					//							+totalOffFaultMomentRate;
					info += "\nTotal Moment Rate From Off Fault MFD: "+invSol.getImpliedTotalGriddedSeisMFD().getTotalMomentRate();
					//					info += "\nTotal Model Seis Moment Rate: "
					//							+(totalOffFaultMomentRate+totalSolutionMoment);
				} catch (Exception e1) {
					e1.printStackTrace();
					invSol = null;
					System.out.println("WARNING: InversionFaultSystemSolution could not be instantiated!");
				}

				double totalMultiplyNamedM7Rate = FaultSystemRupSetCalc.calcTotRateMultiplyNamedFaults(sol, 7d, null);
				double totalMultiplyNamedPaleoVisibleRate = FaultSystemRupSetCalc.calcTotRateMultiplyNamedFaults(sol, 0d, paleoProbabilityModel);

				double totalM7Rate = FaultSystemRupSetCalc.calcTotRateAboveMag(sol, 7d, null);
				double totalPaleoVisibleRate = FaultSystemRupSetCalc.calcTotRateAboveMag(sol, 0d, paleoProbabilityModel);

				info += "\n\nTotal rupture rate (M7+): "+totalM7Rate;
				info += "\nTotal multiply named rupture rate (M7+): "+totalMultiplyNamedM7Rate;
				info += "\n% of M7+ rate that are multiply named: "
					+(100d * totalMultiplyNamedM7Rate / totalM7Rate)+" %";
				info += "\nTotal paleo visible rupture rate: "+totalPaleoVisibleRate;
				info += "\nTotal multiply named paleo visible rupture rate: "+totalMultiplyNamedPaleoVisibleRate;
				info += "\n% of paleo visible rate that are multiply named: "
					+(100d * totalMultiplyNamedPaleoVisibleRate / totalPaleoVisibleRate)+" %";
				info += "\n***********************************************";

				// parent fault moment rates
				ArrayList<ParentMomentRecord> parentMoRates = getSectionMoments(sol);
				info += "\n\n****** Larges Moment Rate Discrepancies ******";
				for (int i=0; i<10 && i<parentMoRates.size(); i++) {
					ParentMomentRecord p = parentMoRates.get(i);
					info += "\n"+p.parentID+". "+p.name+"\ttarget: "+p.targetMoment
					+"\tsolution: "+p.solutionMoment+"\tdiff: "+p.getDiff();
				}
				info += "\n**********************************************";

				sol.setInfoString(info);

				System.out.println("Writing solution");
				sol.toZipFile(solutionFile);
				
				if (!noPlots) {
					CSVFile<String> moRateCSV = new CSVFile<String>(true);
					moRateCSV.addLine(Lists.newArrayList("ID", "Name", "Target", "Solution", "Diff"));
					for (ParentMomentRecord p : parentMoRates)
						moRateCSV.addLine(Lists.newArrayList(p.parentID+"", p.name, p.targetMoment+"",
								p.solutionMoment+"", p.getDiff()+""));
					moRateCSV.writeToFile(new File(subDir, prefix+"_sect_mo_rates.csv"));
					
					System.out.println("Writing Plots");
					tsa.writePlots(criteria, new File(subDir, prefix));

					// 1 km jump plot
					try {
						writeJumpPlots(sol, distsMap, subDir, prefix);
					} catch (Exception e) {
						e.printStackTrace();
					}

					// MFD plots
					try {
						writeMFDPlots(invSol, subDir, prefix);
					} catch (Exception e) {
						e.printStackTrace();
					}
					List<AveSlipConstraint> aveSlipConstraints = null;
					try {
						if (config.getPaleoSlipConstraintWt() > 0d)
							aveSlipConstraints = AveSlipConstraint.load(sol.getFaultSectionDataList());
						else
							aveSlipConstraints = null;
						writePaleoPlots(paleoRateConstraints, aveSlipConstraints, sol, subDir, prefix);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						writeSAFSegPlots(sol, subDir, prefix);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						writeParentSectionMFDPlots(sol, new File(subDir, PARENT_SECT_MFD_DIR_NAME));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						writePaleoCorrelationPlots(
								sol, new File(subDir, PALEO_CORRELATION_DIR_NAME), paleoProbabilityModel);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						writePaleoFaultPlots(
								paleoRateConstraints, aveSlipConstraints, sol, new File(subDir,
										PALEO_FAULT_BASED_DIR_NAME));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			FileWriter fw = new FileWriter(new File(subDir, prefix+"_metadata.txt"));
			fw.write(info);
			fw.close();

			System.out.println("Deleting RupSet (no longer needed)");
			rupSetFile.delete();
		} catch (MissingOptionException e) {
			System.err.println(e.getMessage());
			printHelp(options, mpj);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			printHelp(options, mpj);
		} catch (Exception e) {
			if (mpj) {
				MPJTaskCalculator.abortAndExit(e);
			} else {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private static String getPreInversionInfo(InversionFaultSystemRupSet rupSet) {
		// 2 lines, tab delimeted
		String data = rupSet.getPreInversionAnalysisData(true);
		String[] dataLines = data.split("\n");
		String header = dataLines[0];
		data = dataLines[1];
		String[] headerVals = header.trim().split("\t");
		String[] dataVals = data.trim().split("\t");
		Preconditions.checkState(headerVals.length == dataVals.length);
		
		String info = "****** Pre Inversion Analysis ******";
		for (int i=0; i<headerVals.length; i++)
			info += "\n"+headerVals[i]+": "+dataVals[i];
		info += "\n***********************************************";
		
		return info;
	}
	
	public static final String PALEO_FAULT_BASED_DIR_NAME = "paleo_fault_based";
	public static final String PALEO_CORRELATION_DIR_NAME = "paleo_correlation";
	public static final String PARENT_SECT_MFD_DIR_NAME = "parent_sect_mfds";

	public static void writeJumpPlots(FaultSystemSolution sol, Map<IDPairing, Double> distsMap, File dir, String prefix) throws IOException {
		// use UCERF2 here because it doesn't depend on distance along
		PaleoProbabilityModel paleoProbModel = new UCERF2_PaleoProbabilityModel();
		writeJumpPlot(sol, distsMap, dir, prefix, 1d, 7d, null);
		writeJumpPlot(sol, distsMap, dir, prefix, 1d, 0d, paleoProbModel);
	}
	
	public static EvenlyDiscretizedFunc[] getJumpFuncs(FaultSystemSolution sol,
			Map<IDPairing, Double> distsMap, double jumpDist, double minMag,
			PaleoProbabilityModel paleoProbModel) {
		EvenlyDiscretizedFunc solFunc = new EvenlyDiscretizedFunc(0d, 4, 1d);
		EvenlyDiscretizedFunc rupSetFunc = new EvenlyDiscretizedFunc(0d, 4, 1d);
		int maxX = solFunc.getNum()-1;

		for (int r=0; r<sol.getNumRuptures(); r++) {
			double mag = sol.getMagForRup(r);

			if (mag < minMag)
				continue;

			List<Integer> sects = sol.getSectionsIndicesForRup(r);
			
			int jumpsOverDist = 0;
			for (int i=1; i<sects.size(); i++) {
				int sect1 = sects.get(i-1);
				int sect2 = sects.get(i);

				int parent1 = sol.getFaultSectionData(sect1).getParentSectionId();
				int parent2 = sol.getFaultSectionData(sect2).getParentSectionId();

				if (parent1 != parent2) {
					double dist = distsMap.get(new IDPairing(sect1, sect2));
					if (dist > jumpDist)
						jumpsOverDist++;
				}
			}

			double rate = sol.getRateForRup(r);

			if (paleoProbModel != null)
				rate *= paleoProbModel.getProbPaleoVisible(mag, 0.5); // TODO 0.5?

						// indexes are fine to use here since it starts at zero with a delta of one 
			if (jumpsOverDist <= maxX) {
				solFunc.set(jumpsOverDist, solFunc.getY(jumpsOverDist) + rate);
				rupSetFunc.set(jumpsOverDist, rupSetFunc.getY(jumpsOverDist) + 1d);
			}
		}

		// now normalize rupSetFunc so that the sum of it's y values equals the sum of solFunc's y values
		double totY = solFunc.calcSumOfY_Vals();
		double origRupSetTotY = rupSetFunc.calcSumOfY_Vals();
		for (int i=0; i<rupSetFunc.getNum(); i++) {
			double y = rupSetFunc.getY(i);
			double fract = y / origRupSetTotY;
			double newY = totY * fract;
			rupSetFunc.set(i, newY);
		}
		
		EvenlyDiscretizedFunc[] ret = { solFunc, rupSetFunc };
		return ret;
	}

	public static void writeJumpPlot(FaultSystemSolution sol, Map<IDPairing, Double> distsMap, File dir, String prefix,
			double jumpDist, double minMag, PaleoProbabilityModel paleoProbModel) throws IOException {
		EvenlyDiscretizedFunc[] funcsArray = getJumpFuncs(sol, distsMap, jumpDist, minMag, paleoProbModel);
		writeJumpPlot(dir, prefix, funcsArray, jumpDist, minMag, paleoProbModel != null);
	}
	
	public static void writeJumpPlot(File dir, String prefix,
			DiscretizedFunc[] funcsArray, double jumpDist, double minMag, boolean paleoProb) throws IOException {
		DiscretizedFunc[] solFuncs = { funcsArray[0] };
		DiscretizedFunc[] rupSetFuncs = { funcsArray[1] };
		
		writeJumpPlot(dir, prefix, solFuncs, rupSetFuncs, jumpDist, minMag, paleoProb);
	}
	
	public static void writeJumpPlot(File dir, String prefix,
			DiscretizedFunc[] solFuncs, DiscretizedFunc[] rupSetFuncs, double jumpDist, double minMag, boolean paleoProb) throws IOException {
		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		for (int i=0; i<solFuncs.length-1; i++) {
			funcs.add(solFuncs[i]);
			funcs.add(rupSetFuncs[i]);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
			chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, PlotSymbol.CIRCLE, 3f, Color.RED));
		}

		funcs.add(solFuncs[solFuncs.length-1]);
		funcs.add(rupSetFuncs[rupSetFuncs.length-1]);
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, PlotSymbol.CIRCLE, 3f, Color.RED));

		String title = "Inversion Fault Jumps";

		prefix = getJumpFilePrefix(prefix, minMag, paleoProb);

		if (minMag > 0)
			title += " Mag "+(float)minMag+"+";

		if (paleoProb)
			title += " (Convolved w/ ProbPaleoVisible)";


		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		setFontSizes(gp);
		gp.drawGraphPanel("Number of Jumps > "+(float)jumpDist+" km", "Rate", funcs, chars, false, title);

		File file = new File(dir, prefix);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}

	private static String getJumpFilePrefix(String prefix, double minMag, boolean probPaleoVisible) {
		prefix += "_jumps";
		if (minMag > 0)
			prefix += "_m"+(float)minMag+"+";
		if (probPaleoVisible)
			prefix += "_prob_paleo";
		return prefix;
	}

	public static boolean doJumpPlotsExist(File dir, String prefix) {
		return doesJumpPlotExist(dir, prefix, 0d, true);
	}

	private static boolean doesJumpPlotExist(File dir, String prefix,
			double minMag, boolean probPaleoVisible) {
		return new File(dir, getJumpFilePrefix(prefix, minMag, probPaleoVisible)+".png").exists();
	}

	public static void writeMFDPlots(InversionFaultSystemSolution invSol, File dir, String prefix) throws IOException {
		UCERF2_MFD_ConstraintFetcher ucerf2Fetch = new UCERF2_MFD_ConstraintFetcher();

		// statewide
		writeMFDPlot(invSol, dir, prefix, invSol.getInversionMFDs().getTotalTargetGR(), invSol.getInversionMFDs().getTargetOnFaultSupraSeisMFD(),
				RELM_RegionUtils.getGriddedRegionInstance(), ucerf2Fetch);

		// no cal
		writeMFDPlot(invSol, dir, prefix,invSol.getInversionMFDs().getTotalTargetGR_NoCal(), invSol.getInversionMFDs().noCalTargetSupraMFD,
				RELM_RegionUtils.getNoCalGriddedRegionInstance(), ucerf2Fetch);

		// so cal
		writeMFDPlot(invSol, dir, prefix,invSol.getInversionMFDs().getTotalTargetGR_SoCal(), invSol.getInversionMFDs().soCalTargetSupraMFD,
				RELM_RegionUtils.getSoCalGriddedRegionInstance(), ucerf2Fetch);
	}

	public static void writeMFDPlot(InversionFaultSystemSolution invSol, File dir, String prefix, IncrementalMagFreqDist totalMFD,
			IncrementalMagFreqDist targetMFD, Region region, UCERF2_MFD_ConstraintFetcher ucerf2Fetch) throws IOException {
		HeadlessGraphPanel gp = invSol.getHeadlessMFDPlot(totalMFD, targetMFD, region, ucerf2Fetch);
		File file = new File(dir, getMFDPrefix(prefix, region));
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}

	private static String getMFDPrefix(String prefix, Region region) {
		String regName = region.getName();
		if (regName == null || regName.isEmpty())
			regName = "Uknown";
		regName = regName.replaceAll(" ", "_");
		return prefix+"_MFD_"+regName;
	}

	public static boolean doMFDPlotsExist(File dir, String prefix) {
		return new File(dir, getMFDPrefix(prefix, RELM_RegionUtils.getGriddedRegionInstance())+".png").exists();
	}

	public static ArrayList<PaleoRateConstraint> getPaleoConstraints(FaultModels fm, FaultSystemRupSet rupSet) throws IOException {
		if (fm == FaultModels.FM2_1)
			return UCERF2_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
		return UCERF3_PaleoRateConstraintFetcher.getConstraints(rupSet.getFaultSectionDataList());
	}

	public static void writePaleoPlots(ArrayList<PaleoRateConstraint> paleoRateConstraints,
			List<AveSlipConstraint> aveSlipConstraints, FaultSystemSolution sol,
			File dir, String prefix)
	throws IOException {
		HeadlessGraphPanel gp = PaleoFitPlotter.getHeadlessSegRateComparison(
				paleoRateConstraints, aveSlipConstraints, sol, true);

		File file = new File(dir, prefix+"_paleo_fit");
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}

	public static boolean doPaleoPlotsExist(File dir, String prefix) {
		return new File(dir, prefix+"_paleo_fit.png").exists();
	}

	public static void writeSAFSegPlots(FaultSystemSolution sol, File dir, String prefix) throws IOException {
		List<Integer> parentSects = FaultSpecificSegmentationPlotGen.getSAFParents(sol.getFaultModel());

		writeSAFSegPlot(sol, dir, prefix, parentSects, 0, false);
		writeSAFSegPlot(sol, dir, prefix, parentSects, 7, false);
		writeSAFSegPlot(sol, dir, prefix, parentSects, 7.5, false);

	}

	public static void writeSAFSegPlot(FaultSystemSolution sol, File dir, String prefix,
			List<Integer> parentSects, double minMag, boolean endsOnly) throws IOException {
		HeadlessGraphPanel gp = FaultSpecificSegmentationPlotGen.getSegmentationHeadlessGP(parentSects, sol, minMag, endsOnly);
		
		prefix = getSAFSegPrefix(prefix, minMag, endsOnly);

		File file = new File(dir, prefix);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
	}

	private static String getSAFSegPrefix(String prefix, double minMag, boolean endsOnly) {
		prefix += "_saf_seg";

		if (minMag > 5)
			prefix += (float)minMag+"+";

		return prefix;
	}

	public static boolean doSAFSegPlotsExist(File dir, String prefix) {
		return new File(dir, getSAFSegPrefix(prefix, 7.5, false)+".png").exists();
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

	public static void writeParentSectionMFDPlots(FaultSystemSolution sol, File dir) throws IOException {
		Map<Integer, String> parentSects = Maps.newHashMap();
		
		if (!dir.exists())
			dir.mkdir();

		for (FaultSectionPrefData sect : sol.getFaultSectionDataList())
			if (!parentSects.containsKey(sect.getParentSectionId()))
				parentSects.put(sect.getParentSectionId(), sect.getParentSectionName());

		double minMag = 5.05;
		double maxMag = 9.05;
		int numMag = (int)((maxMag - minMag) / 0.1d) + 1;
		
		CSVFile<String> sdomOverMeansCSV = null;
		
		boolean isAVG = sol instanceof AverageFaultSystemSolution;

		for (int parentSectionID : parentSects.keySet()) {
			String parentSectName = parentSects.get(parentSectionID);

			List<IncrementalMagFreqDist> nuclMFDs = Lists.newArrayList();
			List<IncrementalMagFreqDist> partMFDs = Lists.newArrayList();
			
			SummedMagFreqDist nuclMFD = sol.calcNucleationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			nuclMFDs.add(nuclMFD);
			IncrementalMagFreqDist partMFD = sol.calcParticipationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			partMFDs.add(partMFD);
			
			List<EvenlyDiscretizedFunc> nuclCmlMFDs = Lists.newArrayList();
			nuclCmlMFDs.add(nuclMFD.getCumRateDist());
			List<EvenlyDiscretizedFunc> partCmlMFDs = Lists.newArrayList();
			partCmlMFDs.add(partMFD.getCumRateDist());
			
			if (isAVG) {
				AverageFaultSystemSolution avgSol = (AverageFaultSystemSolution)sol;
				double[] sdom_over_means = calcAveSolMFDs(avgSol, true, partMFDs, parentSectionID, minMag, maxMag, numMag);
				calcAveSolMFDs(avgSol, false, nuclMFDs, parentSectionID, minMag, maxMag, numMag);
				
				if (sdomOverMeansCSV == null) {
					sdomOverMeansCSV = new CSVFile<String>(true);
					
					List<String> header = Lists.newArrayList("Parent ID", "Parent Name");
					for (int i=0; i<numMag; i++)
						header.add((float)nuclMFD.getX(i)+"");
					sdomOverMeansCSV.addLine(header);
				}
				
				List<String> line = Lists.newArrayList();
				line.add(parentSectionID+"");
				line.add(parentSectName);
				
				for (int i=0; i<numMag; i++) {
					line.add(sdom_over_means[i]+"");
				}
				sdomOverMeansCSV.addLine(line);
			}
			
			ArrayList<IncrementalMagFreqDist> ucerf2NuclMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, false, false);
			ArrayList<IncrementalMagFreqDist> ucerf2NuclCmlMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, false, true);
			ArrayList<IncrementalMagFreqDist> ucerf2PartMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, true, false);
			ArrayList<IncrementalMagFreqDist> ucerf2PartCmlMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, true, true);

			writeParentSectMFDPlot(dir, nuclMFDs, nuclCmlMFDs, isAVG, ucerf2NuclMFDs, ucerf2NuclCmlMFDs, parentSectionID, parentSectName, true);
			writeParentSectMFDPlot(dir, partMFDs, partCmlMFDs, isAVG, ucerf2PartMFDs, ucerf2PartCmlMFDs, parentSectionID, parentSectName, false);
		}
		
		if (sdomOverMeansCSV != null) {
			sdomOverMeansCSV.sort(1, 1, new Comparator<String>() {
				
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			});
			
			sdomOverMeansCSV.writeToFile(new File(dir, "participation_sdom_over_means.csv"));
		}
	}
	
	/**
	 * Calculates MFDs for average solutions, adding them to the MFD list. Also returns a list of SDOM/mean values
	 * for each mag bin.
	 * 
	 * @param avgSol
	 * @param participation
	 * @param mfds
	 * @param parentSectionID
	 * @param minMag
	 * @param maxMag
	 * @param numMag
	 * @returnist of SDOM/mean values for each mag bin.
	 */
	private static double[] calcAveSolMFDs(AverageFaultSystemSolution avgSol, boolean participation,
			List<IncrementalMagFreqDist> mfds, int parentSectionID, double minMag, double maxMag, int numMag) {
		IncrementalMagFreqDist meanMFD = mfds.get(0);
		double[] means = new double[numMag];
		for (int i=0; i<numMag; i++)
			means[i] = meanMFD.getY(i);
		
		double[] sdom_over_means = new double[numMag];
		
		int numSols = avgSol.getNumSolutions();
		double mfdVals[][] = new double[numMag][numSols];
		int cnt = 0;
		for (FaultSystemSolution sol : avgSol) {
			IncrementalMagFreqDist mfd;
			if (participation)
				mfd = sol.calcParticipationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			else
				mfd = sol.calcNucleationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			for (int i=0; i<numMag; i++) {
				mfdVals[i][cnt] = mfd.getY(i);
			}
			cnt++;
		}
		
		IncrementalMagFreqDist meanPlusSDOM = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		IncrementalMagFreqDist meanMinusSDOM = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		IncrementalMagFreqDist meanPlusStdDev = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		IncrementalMagFreqDist meanMinusStdDev = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		IncrementalMagFreqDist minFunc = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		IncrementalMagFreqDist maxFunc = new IncrementalMagFreqDist(minMag, maxMag, numMag);
		for (int i=0; i<numMag; i++) {
			double mean = means[i];
			if (mean == 0)
				continue;
			double stdDev = Math.sqrt(StatUtils.variance(mfdVals[i], mean));
			double sdom = stdDev / Math.sqrt(numSols);
			double min = StatUtils.min(mfdVals[i]);
			double max = StatUtils.max(mfdVals[i]);
			
			meanPlusSDOM.set(i, mean + sdom);
			meanMinusSDOM.set(i, mean - sdom);
			meanPlusStdDev.set(i, mean + stdDev);
			meanMinusStdDev.set(i, mean - stdDev);
			minFunc.set(i, min);
			maxFunc.set(i, max);
			
			sdom_over_means[i] = sdom / mean;
		}
		
		mfds.add(meanPlusSDOM);
		mfds.add(meanMinusSDOM);
		mfds.add(meanPlusStdDev);
		mfds.add(meanMinusStdDev);
		mfds.add(minFunc);
		mfds.add(maxFunc);
		
		return sdom_over_means;
	}
	
	private static DiscretizedFunc getRIFunc(EvenlyDiscretizedFunc cmlFunc, String name) {
		ArbitrarilyDiscretizedFunc riCmlFunc = new ArbitrarilyDiscretizedFunc();
		riCmlFunc.setName(name);
		String info = cmlFunc.getInfo();
		String newInfo = " ";
		if (info != null && info.length()>1) {
			newInfo = null;
			for (String line : Splitter.on("\n").split(info)) {
				if (line.contains("RI")) {
					if (newInfo == null)
						newInfo = "";
					else
						newInfo += "\n";
					newInfo += line;
				}
			}
			if (newInfo == null)
				newInfo = " ";
		}
		riCmlFunc.setInfo(newInfo);
		for (int i=0; i<cmlFunc.getNum(); i++) {
			double y = cmlFunc.getY(i);
			if (y > 0)
				riCmlFunc.set(cmlFunc.getX(i), 1d/y);
		}
		if (riCmlFunc.getNum() == 0) {
			for (int i=0; i<cmlFunc.getNum(); i++) {
				riCmlFunc.set(cmlFunc.getX(i), 0d);
			}
		}
		return riCmlFunc;
	}

	public static void writeParentSectMFDPlot(File dir, List<IncrementalMagFreqDist> mfds,
			List<EvenlyDiscretizedFunc> cmlMFDs,
			boolean avgColoring, List<IncrementalMagFreqDist> ucerf2MFDs,
			List<IncrementalMagFreqDist> ucerf2CmlMFDs,
			int id, String name, boolean nucleation) throws IOException {
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		setFontSizes(gp);
		gp.setYLog(true);
		gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);

		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		ArrayList<DiscretizedFunc> riFuncs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> riChars = Lists.newArrayList();
		
		IncrementalMagFreqDist mfd;
		if (mfds.size() ==  1 || avgColoring) {
			mfd = mfds.get(0);
			mfd.setName("Incremental MFD");
			funcs.add(mfd);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
			EvenlyDiscretizedFunc cmlFunc = cmlMFDs.get(0);
			cmlFunc.setName("Cumulative MFD");
			funcs.add(cmlFunc);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
			
			if (!nucleation) {
				riFuncs.add(getRIFunc(cmlFunc, "Recurrence Interval (RI) for "+name));
				riChars.add(chars.get(chars.size()-1));
			}
			
			if (avgColoring) {
				// this is an average fault system solution
				
				// mean +/- SDOM
				funcs.add(mfds.get(1));
				funcs.add(mfds.get(2));
				PlotCurveCharacterstics pchar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.BLUE);
				chars.add(pchar);
				chars.add(pchar);
				
				// mean +/- Std Dev
				funcs.add(mfds.get(3));
				funcs.add(mfds.get(4));
				pchar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GREEN);
				chars.add(pchar);
				chars.add(pchar);
				
				// min/max
				funcs.add(mfds.get(5));
				funcs.add(mfds.get(6));
				pchar = new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.GRAY);
				chars.add(pchar);
				chars.add(pchar);
			}
		} else {
			int numFractils = cmlMFDs.size()-3;
			funcs.addAll(cmlMFDs);
			chars.addAll(CompoundFSSPlots.getFractileChars(Color.BLUE, numFractils));
			if (!nucleation) {
				riFuncs.add(getRIFunc(cmlMFDs.get(cmlMFDs.size()-3),
						"Recurrence Interval (RI) for "+name));
				riChars.add(chars.get(chars.size()-3));
				riFuncs.add(getRIFunc(cmlMFDs.get(cmlMFDs.size()-1),
						"Recurrence Interval (RI) for "+name+" (minimum)"));
				riChars.add(chars.get(chars.size()-1));
				riFuncs.add(getRIFunc(cmlMFDs.get(cmlMFDs.size()-2),
						"Recurrence Interval (RI) for "+name+" (maximum)"));
				riChars.add(chars.get(chars.size()-2));
			}
			numFractils = mfds.size()-3;
			mfd = mfds.get(mfds.size()-3);
			funcs.addAll(mfds);
			chars.addAll(CompoundFSSPlots.getFractileChars(new Color(0, 126, 255), numFractils));
			// little hack to remove min/max from incremental plots
			funcs.remove(funcs.size()-1);
			funcs.remove(funcs.size()-1);
			chars.remove(chars.size()-1);
			chars.remove(chars.size()-1);
		}
		
		if (ucerf2MFDs != null) {
			Color lightRed = new Color (255, 128, 128);
			
			for (IncrementalMagFreqDist ucerf2MFD : ucerf2MFDs)
				ucerf2MFD.setName("UCERF2 "+ucerf2MFD.getName());
			IncrementalMagFreqDist meanMFD = ucerf2MFDs.get(0);
			funcs.add(meanMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, lightRed));
//			IncrementalMagFreqDist minMFD = ucerf2MFDs.get(1);
//			funcs.add(minMFD);
//			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, lightRed));
//			IncrementalMagFreqDist maxMFD = ucerf2MFDs.get(2);
//			funcs.add(maxMFD);
//			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, lightRed));
			if (ucerf2CmlMFDs != null) {
				EvenlyDiscretizedFunc cmlMeanMFD = ucerf2CmlMFDs.get(0);
				for (IncrementalMagFreqDist ucerf2MFD : ucerf2CmlMFDs)
					ucerf2MFD.setName("UCERF2 "+ucerf2MFD.getName());
				funcs.add(cmlMeanMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.RED));
				IncrementalMagFreqDist cmlMinMFD = ucerf2CmlMFDs.get(1);
				funcs.add(cmlMinMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
				IncrementalMagFreqDist cmlMaxMFD = ucerf2CmlMFDs.get(2);
				funcs.add(cmlMaxMFD);
				chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
				if (!nucleation) {
					riFuncs.add(getRIFunc(ucerf2CmlMFDs.get(0),
							"UCERF2 Recurrence Interval (RI) for "+name));
					riChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, Color.RED));
					riFuncs.add(getRIFunc(ucerf2CmlMFDs.get(2),
							"UCERF2 Recurrence Interval (RI) for "+name+" (minimum)"));
					riChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
					riFuncs.add(getRIFunc(ucerf2CmlMFDs.get(1),
							"UCERF2 Recurrence Interval (RI) for "+name+" (maximum)"));
					riChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
				}
			}
			
		}

		double minX = mfd.getMinX();
		if (minX < 5)
			minX = 5;
		gp.setUserBounds(minX, mfd.getMaxX(),
				1e-10, 1e-1);
		String title;
		String yAxisLabel;
		
		String fname = name.replaceAll("\\W+", "_");
		
		if (nucleation) {
			title = "Nucleation MFD";
			yAxisLabel = "Nucleation Rate";
			fname += "_nucleation";
		} else {
			title = "Participation MFD";
			yAxisLabel = "Participation Rate";
			fname += "_participation";
		}
		title += " for "+name+" ("+id+")";
		
		gp.setRenderingOrder(DatasetRenderingOrder.REVERSE);
		gp.drawGraphPanel("Magnitude", yAxisLabel, funcs, chars, true, title);
		
		File file = new File(dir, fname);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
		gp.saveAsTXT(file.getAbsolutePath()+".txt");
		
		if (!nucleation) {
			gp.setUserBounds(5d, 9d, 10, 1e9);
			
			title = "Participation Recurrence Interval for "+name+" ("+id+")";
			gp.drawGraphPanel("Magnitude", "Recurrence Interval", riFuncs, riChars, true, title);
			
			file = new File(dir, name.replaceAll("\\W+", "_")+"_cmlRI_participation");
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
			gp.saveAsTXT(file.getAbsolutePath()+".txt");
		}
	}

	public static void writePaleoCorrelationPlots(
			FaultSystemSolution sol, File dir, PaleoProbabilityModel paleoProb) throws IOException {
		Map<String, Table<String, String, PaleoSiteCorrelationData>> tables =
				PaleoSiteCorrelationData.loadPaleoCorrelationData(sol);
		
		Map<String, PlotSpec> specMap = Maps.newHashMap();
		
		for (String faultName : tables.keySet())
			specMap.put(faultName, PaleoSiteCorrelationData.getCorrelationPlotSpec(
					faultName, tables.get(faultName), sol, paleoProb));
		
		writePaleoCorrelationPlots(dir, specMap);
	}
	
	public static void writePaleoCorrelationPlots(
			File dir, Map<String, PlotSpec> specMap) throws IOException {
		
		
		if (!dir.exists())
			dir.mkdir();
		
		for (String faultName : specMap.keySet()) {
			String fname = faultName.replaceAll("\\W+", "_");
			
			PlotSpec spec = specMap.get(faultName);
			
			double maxX = 0;
			for (DiscretizedFunc func : spec.getFuncs()) {
				double myMaxX = func.getMaxX();
				if (myMaxX > maxX)
					maxX = myMaxX;
			}
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			setFontSizes(gp);
			gp.setUserBounds(0d, maxX, -0.05d, 1.05d);
			
			gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
					spec.getFuncs(), spec.getChars(), true, spec.getTitle());
			
			File file = new File(dir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
			gp.saveAsTXT(file.getAbsolutePath()+".txt");
		}
	}

	public static void writePaleoFaultPlots(
			List<PaleoRateConstraint> paleoRateConstraints,
			List<AveSlipConstraint> aveSlipConstraints, FaultSystemSolution sol, File dir)
					throws IOException {
		Map<String, PlotSpec[]> specs = PaleoFitPlotter.getFaultSpecificPaleoPlotSpec(
				paleoRateConstraints, aveSlipConstraints, sol);
		
		writePaleoFaultPlots(specs, null, dir);
	}
	
	public static void writePaleoFaultPlots(
			Map<String, PlotSpec[]> specs, String prefix, File dir)
					throws IOException {
		
		String[] fname_adds = { "paleo", "slips", "combined" };
		
		if (!dir.exists())
			dir.mkdir();
		
		for (String faultName : specs.keySet()) {
			String fname = faultName.replaceAll("\\W+", "_");
			
			if (prefix != null && !prefix.isEmpty())
				fname = prefix+"_"+fname;
			
			PlotSpec[] specArray = specs.get(faultName);
			
			double xMin = Double.POSITIVE_INFINITY;
			double xMax = Double.NEGATIVE_INFINITY;
			for (DiscretizedFunc func : specArray[2].getFuncs()) {
				double myXMin = func.getMinX();
				double myXMax = func.getMaxX();
				if (myXMin < xMin)
					xMin = myXMin;
				if (myXMax > xMax)
					xMax = myXMax;
			}
			
			for (int i=0; i<specArray.length; i++) {
				String fname_add = fname_adds[i];
				PlotSpec spec = specArray[i];
				HeadlessGraphPanel gp = new HeadlessGraphPanel();
				setFontSizes(gp);
				gp.setYLog(true);
				if (xMax > 0)
					// only when latitudeX, this is a kludgy way of detecting this for CA
					gp.setxAxisInverted(true);
				System.out.println("X Range: "+xMin+"=>"+xMax);
				if (i == 0)
					// just paleo
					gp.setUserBounds(xMin, xMax, 1e-5, 1e-1);
				if (i == 1)
					// just slip
					gp.setUserBounds(xMin, xMax, 1e-1, 5e1);
				else
					// combined
					gp.setUserBounds(xMin, xMax, 1e-5, 1e0);
				
				gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
						spec.getFuncs(), spec.getChars(), true, spec.getTitle());
				
				File file = new File(dir, fname+"_"+fname_add);
				gp.getCartPanel().setSize(1000, 800);
				gp.saveAsPDF(file.getAbsolutePath()+".pdf");
				gp.saveAsPNG(file.getAbsolutePath()+".png");
				gp.saveAsTXT(file.getAbsolutePath()+".txt");
			}
		}
	}
	
	public static void setFontSizes(HeadlessGraphPanel gp) {
//		gp.setTickLabelFontSize(16);
//		gp.setAxisLabelFontSize(18);
//		gp.setPlotLabelFontSize(20);
		gp.setTickLabelFontSize(18);
		gp.setAxisLabelFontSize(20);
		gp.setPlotLabelFontSize(21);
	}
}
