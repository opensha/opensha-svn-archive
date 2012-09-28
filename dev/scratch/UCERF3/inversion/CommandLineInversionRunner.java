package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.opensha.commons.calc.FaultMomentCalc;
import org.opensha.commons.data.CSVFile;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.commons.util.ClassUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.HeadlessGraphPanel;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.gui.infoTools.PlotSpec;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
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

import cern.colt.matrix.tdouble.DoubleMatrix2D;

import com.google.common.base.Preconditions;
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
		EVENT_SMOOTH_WT("eventsm", "event-smooth-wt", "EventSmoothWt", true, "Relative Event Rate Smoothness weight"),
		SECTION_NUCLEATION_MFD_WT("nuclwt", "sect-nucl-mfd-wt", "SectNuclMFDWt", true,
				"Relative section nucleation MFD constraint weight"),
		MFD_TRANSITION_MAG("mfdtrans", "mfd-trans-mag", "MFDTrans", true, "MFD transition magnitude"),
		MFD_SMOOTHNESS_WT("mfdsmooth", "mfd-smooth-wt", "MFDSmooth", true, "MFD smoothness constraint weight");

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
			InversionFaultSystemRupSet rupSet = InversionFaultSystemRupSetFactory.forBranch(
					laughTest, defaultAseis, branch);

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
			info += "\nNum rups above waterlevel: "+numAboveWaterlevel;
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
					info += "\nTotal Moment Rate From Off Fault MFD: "+invSol.getImpliedOffFaultStatewideMFD().getTotalMomentRate();
					//					info += "\nTotal Model Seis Moment Rate: "
					//							+(totalOffFaultMomentRate+totalSolutionMoment);
				} catch (Exception e1) {
					e1.printStackTrace();
					invSol = null;
					System.out.println("WARNING: InversionFaultSystemSolution could not be instantiated!");
				}

				PaleoProbabilityModel ucerf2PaleoProb = new UCERF2_PaleoProbabilityModel();

				double totalMultiplyNamedM7Rate = FaultSystemRupSetCalc.calcTotRateMultiplyNamedFaults(sol, 7d, null);
				double totalMultiplyNamedPaleoVisibleRate = FaultSystemRupSetCalc.calcTotRateMultiplyNamedFaults(sol, 0d, ucerf2PaleoProb);

				double totalM7Rate = FaultSystemRupSetCalc.calcTotRateAboveMag(sol, 7d, null);
				double totalPaleoVisibleRate = FaultSystemRupSetCalc.calcTotRateAboveMag(sol, 0d, ucerf2PaleoProb);

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
				CSVFile<String> moRateCSV = new CSVFile<String>(true);
				moRateCSV.addLine(Lists.newArrayList("ID", "Name", "Target", "Solution", "Diff"));
				for (ParentMomentRecord p : parentMoRates)
					moRateCSV.addLine(Lists.newArrayList(p.parentID+"", p.name, p.targetMoment+"",
							p.solutionMoment+"", p.getDiff()+""));
				moRateCSV.writeToFile(new File(subDir, prefix+"_sect_mo_rates.csv"));

				sol.setInfoString(info);

				System.out.println("Writing solution");
				sol.toZipFile(solutionFile);

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
				try {
					List<AveSlipConstraint> aveSlipConstraints;
					if (config.getRelativePaleoSlipWt() > 0d)
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
					writeParentSectionMFDPlots(sol, new File(subDir, "parent_sect_mfds"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					writePaleoCorrelationPlots(
							sol, new File(subDir, "paleo_correlation"), paleoProbabilityModel);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			FileWriter fw = new FileWriter(new File(subDir, prefix+"_metadata.txt"));
			fw.write(info);
			fw.close();

			System.out.println("Deleting RupSet (no longer needed)");
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

	public static void writeJumpPlots(FaultSystemSolution sol, Map<IDPairing, Double> distsMap, File dir, String prefix) throws IOException {
		// use UCERF2 here because it doesn't depend on distance along
		PaleoProbabilityModel paleoProbModel = new UCERF2_PaleoProbabilityModel();
		writeJumpPlot(sol, distsMap, dir, prefix, 1d, 7d, null);
		writeJumpPlot(sol, distsMap, dir, prefix, 1d, 0d, paleoProbModel);
	}

	public static void writeJumpPlot(FaultSystemSolution sol, Map<IDPairing, Double> distsMap, File dir, String prefix,
			double jumpDist, double minMag, PaleoProbabilityModel paleoProbModel) throws IOException {
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

		ArrayList<EvenlyDiscretizedFunc> funcs = Lists.newArrayList();
		funcs.add(solFunc);
		funcs.add(rupSetFunc);

		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, PlotSymbol.CIRCLE, 5f, Color.BLACK));
		chars.add(new PlotCurveCharacterstics(PlotLineType.DASHED, 1f, PlotSymbol.CIRCLE, 3f, Color.RED));

		String title = "Inversion Fault Jumps";

		prefix = getJumpFilePrefix(prefix, minMag, paleoProbModel != null);

		if (minMag > 0)
			title += " Mag "+(float)minMag+"+";

		if (paleoProbModel != null)
			title += " (Convolved w/ ProbPaleoVisible)";


		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.drawGraphPanel("Number of Jumps > "+(float)jumpDist+" km", "Rate", funcs, chars, false, title);

		File file = new File(dir, prefix);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
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

		for (int parentSectionID : parentSects.keySet()) {
			String parentSectName = parentSects.get(parentSectionID);

			SummedMagFreqDist nuclMFD = sol.calcNucleationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			IncrementalMagFreqDist partMFD = sol.calcParticipationMFD_forParentSect(parentSectionID, minMag, maxMag, numMag);
			
			ArrayList<IncrementalMagFreqDist> ucerf2NuclMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, false, false);
			ArrayList<IncrementalMagFreqDist> ucerf2PArtMFDs = UCERF2_Section_MFDsCalc.getMeanMinAndMaxMFD(parentSectionID, true, false);

			writeParentSectMFDPlot(dir, nuclMFD, ucerf2NuclMFDs, parentSectionID, parentSectName, true);
			writeParentSectMFDPlot(dir, partMFD, ucerf2PArtMFDs, parentSectionID, parentSectName, false);
		}
	}

	private static void writeParentSectMFDPlot(File dir, IncrementalMagFreqDist mfd, List<IncrementalMagFreqDist> ucerf2MFDs,
			int id, String name, boolean nucleation) throws IOException {
		HeadlessGraphPanel gp = new HeadlessGraphPanel();
		gp.setTickLabelFontSize(14);
		gp.setAxisLabelFontSize(16);
		gp.setPlotLabelFontSize(18);
		gp.setYLog(true);
		gp.setRenderingOrder(DatasetRenderingOrder.FORWARD);

		ArrayList<DiscretizedFunc> funcs = Lists.newArrayList();
		ArrayList<PlotCurveCharacterstics> chars = Lists.newArrayList();
		
		if (ucerf2MFDs != null) {
			for (IncrementalMagFreqDist ucerf2MFD : ucerf2MFDs)
				ucerf2MFD.setName("UCERF2 "+ucerf2MFD.getName());
			IncrementalMagFreqDist meanMFD = ucerf2MFDs.get(0);
			funcs.add(meanMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, Color.RED));
			Color lightRed = new Color (255, 100, 100);
			IncrementalMagFreqDist minMFD = ucerf2MFDs.get(1);
			funcs.add(minMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, lightRed));
			IncrementalMagFreqDist maxMFD = ucerf2MFDs.get(2);
			funcs.add(maxMFD);
			chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 1f, lightRed));
		}
		mfd.setName("Incremental MFD");
		funcs.add(mfd);
		EvenlyDiscretizedFunc cmlFunc = mfd.getCumRateDist();
		cmlFunc.setName("Cumulative MFD");
		funcs.add(cmlFunc);

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
		
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLUE));
		chars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.BLACK));
		gp.drawGraphPanel("Magnitude", yAxisLabel, funcs, chars, true, title);
		
		File file = new File(dir, fname);
		gp.getCartPanel().setSize(1000, 800);
		gp.saveAsPDF(file.getAbsolutePath()+".pdf");
		gp.saveAsPNG(file.getAbsolutePath()+".png");
	}

	public static void writePaleoCorrelationPlots(
			FaultSystemSolution sol, File dir, PaleoProbabilityModel paleoProb) throws IOException {
		Map<String, Table<String, String, PaleoSiteCorrelationData>> tables =
			PaleoSiteCorrelationData.loadPaleoCorrelationData(sol);
		
		if (!dir.exists())
			dir.mkdir();
		
		for (String faultName : tables.keySet()) {
			String fname = faultName.replaceAll("\\W+", "_");
			
			PlotSpec spec = PaleoSiteCorrelationData.getCorrelationPlotSpec(
					faultName, tables.get(faultName), sol, paleoProb);
			
			double maxX = 0;
			for (DiscretizedFunc func : spec.getFuncs()) {
				double myMaxX = func.getMaxX();
				if (myMaxX > maxX)
					maxX = myMaxX;
			}
			
			HeadlessGraphPanel gp = new HeadlessGraphPanel();
			gp.setTickLabelFontSize(14);
			gp.setAxisLabelFontSize(16);
			gp.setPlotLabelFontSize(18);
			gp.setUserBounds(0d, maxX, 0d, 1d);
			
			gp.drawGraphPanel(spec.getxAxisLabel(), spec.getyAxisLabel(),
					spec.getFuncs(), spec.getChars(), true, spec.getTitle());
			
			File file = new File(dir, fname);
			gp.getCartPanel().setSize(1000, 800);
			gp.saveAsPDF(file.getAbsolutePath()+".pdf");
			gp.saveAsPNG(file.getAbsolutePath()+".png");
		}
	}
}
