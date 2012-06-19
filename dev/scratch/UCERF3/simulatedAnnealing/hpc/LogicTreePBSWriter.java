package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dom4j.DocumentException;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.EpicenterScriptWriter;
import org.opensha.commons.hpc.pbs.RangerScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.MaxMagOffFault;
import scratch.UCERF3.enumTreeBranches.MomentRateFixes;
import scratch.UCERF3.enumTreeBranches.ScalingRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.enumTreeBranches.SpatialSeisPDF;
import scratch.UCERF3.enumTreeBranches.TotalMag5Rate;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.CommandLineInversionRunner.InversionOptions;
import scratch.UCERF3.logicTree.DiscreteListTreeTrimmer;
import scratch.UCERF3.logicTree.ListBasedTreeTrimmer;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.logicTree.LogicTreeBranchIterator;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.logicTree.TreeTrimmer;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.VariableSubTimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.PaleoProbabilityModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LogicTreePBSWriter {

	private static DateFormat df = new SimpleDateFormat("yyyy_MM_dd");

	public static ArrayList<File> getClasspath(RunSites runSite) {
		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(runSite.RUN_DIR, "OpenSHA_complete.jar"));
		jars.add(new File(runSite.RUN_DIR, "parallelcolt-0.9.4.jar"));
		jars.add(new File(runSite.RUN_DIR, "commons-cli-1.2.jar"));
		jars.add(new File(runSite.RUN_DIR, "csparsej.jar"));
		return jars;
	}

	public enum RunSites {
		EPICENTER("/home/epicenter/kmilner/inversions", EpicenterScriptWriter.JAVA_BIN,
//				"/home/scec-02/kmilner/ucerf3/inversions/fm_store") {
				null) {
			@Override
			public BatchScriptWriter forBranch(LogicTreeBranch branch) {
				InversionModels im = branch.getValue(InversionModels.class);
				Preconditions.checkState(im != InversionModels.GR_CONSTRAINED,
						"are you kidding me? we can't run GR on epicenter!");
				return new EpicenterScriptWriter();
			}

			@Override
			public int getMaxHeapSizeMB(LogicTreeBranch branch) {
				return 7000;
			}

			@Override
			public int getPPN(LogicTreeBranch branch) {
				return 8;
			}
		},
		HPCC("/home/scec-02/kmilner/ucerf3/inversions", USC_HPCC_ScriptWriter.JAVA_BIN,
//				"/home/scec-02/kmilner/ucerf3/inversions/fm_store") {
				null) {
			@Override
			public BatchScriptWriter forBranch(LogicTreeBranch branch) {
				if (branch.getValue(InversionModels.class) == InversionModels.GR_CONSTRAINED)
					return new USC_HPCC_ScriptWriter("dodecacore");
				return new USC_HPCC_ScriptWriter("quadcore");
			}

			@Override
			public int getMaxHeapSizeMB(LogicTreeBranch branch) {
				if (branch.getValue(InversionModels.class) == InversionModels.GR_CONSTRAINED)
					return 40000;
				return 10000;
			}

			@Override
			public int getPPN(LogicTreeBranch branch) {
				if (branch.getValue(InversionModels.class) == InversionModels.GR_CONSTRAINED)
					return 24;
				return 8;
			}
		},
		RANGER("/work/00950/kevinm/ucerf3/inversion", RangerScriptWriter.JAVA_BIN, null) { // TODO!!!!
			@Override
			public BatchScriptWriter forBranch(LogicTreeBranch branch) {
				return new RangerScriptWriter();
			}

			@Override
			public int getMaxHeapSizeMB(LogicTreeBranch branch) {
				return 28000;
			}

			@Override
			public int getPPN(LogicTreeBranch branch) {
				return 16;
			}
		};

		private File RUN_DIR;
		private File JAVA_BIN;
		private String FM_STORE;

		private RunSites(String path, File javaBin, String fmStore) {
			RUN_DIR = new File(path);
			JAVA_BIN = javaBin;
			FM_STORE = fmStore;
		}

		public abstract BatchScriptWriter forBranch(LogicTreeBranch branch);
		public abstract int getMaxHeapSizeMB(LogicTreeBranch branch);
		public int getInitialHeapSizeMB(LogicTreeBranch branch) {
			return getMaxHeapSizeMB(branch);
		}
		public abstract int getPPN(LogicTreeBranch branch);
	}

	private static ArrayList<CustomArg[]> buildVariationBranches(List<CustomArg[]> variations, CustomArg[] curVariation) {
		if (curVariation == null)
			curVariation = new CustomArg[variations.size()];
		int ind = curVariation.length - variations.size();
		List<CustomArg[]> nextVars;
		if (variations.size() > 1)
			nextVars = variations.subList(1, variations.size());
		else
			nextVars = null;
		ArrayList<CustomArg[]> retVal = new ArrayList<CustomArg[]>();

		for (CustomArg var : variations.get(0)) {
			CustomArg[] branch = Arrays.copyOf(curVariation, curVariation.length);
			branch[ind] = var;
			if (nextVars == null)
				retVal.add(branch);
			else
				retVal.addAll(buildVariationBranches(nextVars, branch));
		}

		return retVal;
	}

	private static class CustomArg {
		private InversionOptions op;
		private String arg;

		public CustomArg(InversionOptions op, String arg) {
			this.op = op;
			if (op.hasOption())
				Preconditions.checkState(arg != null && !arg.isEmpty());
			else
				Preconditions.checkState(arg == null || arg.isEmpty());
			this.arg = arg;
		}
	}

	private static CustomArg[] forOptions(InversionOptions op, String... args) {
		CustomArg[] ops = new CustomArg[args.length];
		for (int i=0; i<args.length; i++)
			ops[i] = new CustomArg(op, args[i]);
		return ops;
	}

	private static <E> E[] toArray(E... vals) {
		return vals;
	}

	private static List<LogicTreeBranchNode<?>> getNonZeroChoices(Class<? extends LogicTreeBranchNode<?>> clazz) {
		List<LogicTreeBranchNode<?>> nonZeros = Lists.newArrayList();
		for (LogicTreeBranchNode<?> val : clazz.getEnumConstants())
			if (val.getRelativeWeight() > 0)
				nonZeros.add(val);
		return nonZeros;
	}

	private static List<LogicTreeBranchNode<?>> allOf(Class<? extends LogicTreeBranchNode<?>> clazz) {
		LogicTreeBranchNode<?>[] vals = clazz.getEnumConstants();

		return Arrays.asList(vals);
	}

	private static List<LogicTreeBranchNode<?>> toList(LogicTreeBranchNode<?>... vals) {
		return Arrays.asList(vals);
	}

	private static CustomArg[] buildVariationBranch(InversionOptions[] ops, String[] vals) {
		Preconditions.checkArgument(ops.length == vals.length);
		CustomArg[] args = new CustomArg[ops.length];
		for (int i=0; i<args.length; i++) {
			if (!ops[i].hasOption()) {
				if (vals[i] != null && vals[i].equals(TAG_OPTION_ON))
					args[i] = new CustomArg(ops[i], null);
			} else {
				args[i] = new CustomArg(ops[i], vals[i]);
			}
		}
		return args;
	}

	private static final String TAG_OPTION_ON = "Option On";

	private static class VariableLogicTreeBranch extends LogicTreeBranch {
		CustomArg[] args;
		public VariableLogicTreeBranch(CustomArg[] args, boolean setNullToDefault, LogicTreeBranchNode<?>... branchChoices) {
			this(args, LogicTreeBranch.fromValues(setNullToDefault, branchChoices));
		}

		public VariableLogicTreeBranch(CustomArg[] args, LogicTreeBranch branch) {
			super(branch);
			this.args = args;
		}
		@Override
		public int getNumAwayFrom(LogicTreeBranch branch) {
			int num = super.getNumAwayFrom(branch);

			if (!(branch instanceof VariableLogicTreeBranch))
				return num;

			VariableLogicTreeBranch variableBranch = (VariableLogicTreeBranch)branch;

			if (args != null) {
				for (int i=0; i<args.length; i++) {
					CustomArg myArg = args[i];
					if (myArg == null)
						continue;

					if (variableBranch.args == null || variableBranch.args.length <= i) {
						num++;
						break;
					}
					CustomArg theirArg = variableBranch.args[i];
					if (theirArg.op != myArg.op) {
						num++;
						break;
					}

					if (myArg.arg == null && theirArg.arg != null) {
						num++;
						break;
					}

					if (!myArg.arg.equals(theirArg.arg)) {
						num++;
						break;
					}
				}
			}

			return num;
		}
	}
	
	private static TreeTrimmer getUCERF2Trimmer() {
		return DiscreteListTreeTrimmer.getUCERF2_IngredientsTrimmer();
	}
	
	private static TreeTrimmer getAllDM_IM_Trimmer(boolean bothFMs) {
		List<List<LogicTreeBranchNode<?>>> limitations = Lists.newArrayList();
		
		List<LogicTreeBranchNode<?>> faultModels = Lists.newArrayList();
		faultModels.add(FaultModels.FM3_1);
		if (bothFMs)
			faultModels.add(FaultModels.FM3_2);
		limitations.add(faultModels);
		
		List<LogicTreeBranchNode<?>> defModels = getNonZeroChoices(DeformationModels.class);
		limitations.add(defModels);
		
		List<LogicTreeBranchNode<?>> invModels = getNonZeroChoices(InversionModels.class);
		limitations.add(invModels);
		
		return ListBasedTreeTrimmer.getDefaultPlusSpecifiedTrimmer(limitations);
	}
	
	private static TreeTrimmer getCustomTrimmer(boolean bothFMs) {
		List<List<LogicTreeBranchNode<?>>> limitations = Lists.newArrayList();

//		List<LogicTreeBranchNode<?>> faultModels = toList(FaultModels.FM3_1);
		List<LogicTreeBranchNode<?>> faultModels = toList(FaultModels.FM3_1, FaultModels.FM3_2);
		limitations.add(faultModels);

		// if null, all that are applicable to each fault model will be used
//		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.GEOLOGIC);
		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.GEOLOGIC, DeformationModels.ABM, DeformationModels.NEOKINEMA, DeformationModels.ZENG);
		limitations.add(defModels);

		List<LogicTreeBranchNode<?>> inversionModels = allOf(InversionModels.class);
		limitations.add(inversionModels);
		//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.UNCONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.UNCONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.CHAR_CONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.GR };
		//		InversionModels[] inversionModels =  { InversionModels.GR };

//		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.ELLSWORTH_B);
		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.SHAW_2009_MOD, ScalingRelationships.SHAW_CONST_STRESS_DROP,
					ScalingRelationships.ELLSWORTH_B, ScalingRelationships.ELLB_SQRT_LENGTH, ScalingRelationships.HANKS_BAKUN_08);
		limitations.add(scaling);

		List<LogicTreeBranchNode<?>> slipAlongs = getNonZeroChoices(SlipAlongRuptureModels.class);
		//		List<SlipAlongRuptureModels> slipAlongs = Lists.newArrayList(SlipAlongRuptureModels.TAPERED);
		limitations.add(slipAlongs);

		List<LogicTreeBranchNode<?>> mag5s = getNonZeroChoices(TotalMag5Rate.class);
		limitations.add(mag5s);

		List<LogicTreeBranchNode<?>> maxMags = getNonZeroChoices(MaxMagOffFault.class);
		limitations.add(maxMags);

		List<LogicTreeBranchNode<?>> momentFixes = getNonZeroChoices(MomentRateFixes.class);
		limitations.add(momentFixes);

		List<LogicTreeBranchNode<?>> spatialSeis = getNonZeroChoices(SpatialSeisPDF.class);
		limitations.add(spatialSeis);
		
		return new ListBasedTreeTrimmer(limitations);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		String runName = "quick-test";
		if (args.length > 1)
			runName = args[1];
		runName = df.format(new Date())+"-"+runName;
		//		runName = "2012_03_02-weekend-converg-test";

		//		RunSites site = RunSites.RANGER;
		//		RunSites site = RunSites.EPICENTER;
		RunSites site = RunSites.HPCC;

		//		String nameAdd = "VarSub5_0.3";
		String nameAdd = null;

		int numRuns = 1;
		int runStart = 0;

		boolean lightweight = numRuns > 10;

		TreeTrimmer trimmer = getAllDM_IM_Trimmer(false);

		// this is a somewhat kludgy way of passing in a special variation to the input generator
		ArrayList<CustomArg[]> variationBranches = null;
		List<CustomArg[]> variations = null;

		//		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
		//		InversionOptions[] ops = { InversionOptions.A_PRIORI_CONST_FOR_ZERO_RATES, InversionOptions.A_PRIORI_CONST_WT,
		//				InversionOptions.WATER_LEVEL_FRACT };
		////		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, "100", "0")));
		//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, "1000", "0")));
		//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, "10000", "0")));
		////		variationBranches.add(buildVariationBranch(ops, toArray(null, "1000")));
		////		variationBranches.add(buildVariationBranch(ops, toArray(null, "100")));

		// do all branch choices relative to these:
		//		Branch defaultBranch = null;
		HashMap<InversionModels, Integer> maxAway = Maps.newHashMap();
		maxAway.put(InversionModels.CHAR_CONSTRAINED, 1);
		maxAway.put(InversionModels.CHAR_UNCONSTRAINED, 2);
		maxAway.put(InversionModels.GR_CONSTRAINED, 1);
		maxAway.put(InversionModels.GR_UNCONSTRAINED, 2);
		VariableLogicTreeBranch[] defaultBranches = {
				//				new VariableLogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
				//						AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, null,
				//						buildVariationBranch(ops, toArray("0.2", "0.5", "1", null))),
				new VariableLogicTreeBranch(null, false, FaultModels.FM3_1, TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6,
						MomentRateFixes.NONE, ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, SpatialSeisPDF.UCERF3)
				//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC, MagAreaRelationships.ELL_B,
				//								AveSlipForRupModels.ELLSWORTH_B, null, null),
				//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
				//								AveSlipForRupModels.ELLSWORTH_B, null, null)
		};
		if (defaultBranches != null) { // TODO
			// make sure all default branch choices are valid!
			for (LogicTreeBranch defaultBranch : defaultBranches) {
				for (LogicTreeBranchNode<?> node : defaultBranch) {
					if (node == null)
						continue;

				}


				//				if (defaultBranch.getValue(FaultModels.class) != null &&
				//						!Arrays.asList(faultModels).contains(defaultBranch.getValue(FaultModels.class)))
				//					defaultBranch.clearValue(FaultModels.class);
				//					defaultBranch.setFaultModel(null);
				//				if (defaultBranch.getDefModel() != null &&
				//						defModels != null && !Arrays.asList(defModels).contains(defaultBranch.getDefModel()))
				//					defaultBranch.setDefModel(null);
				//				if (defaultBranch.getAveSlip() != null &&
				//						!Arrays.asList(aveSlipModels).contains(defaultBranch.getAveSlip()))
				//					defaultBranch.setAveSlip(null);
				//				if (defaultBranch.getSlipAlong() != null &&
				//						!Arrays.asList(slipAlongs).contains(defaultBranch.getSlipAlong()))
				//					defaultBranch.setSlipAlong(null);
				//				if (defaultBranch.getMagArea() != null &&
				//						!Arrays.asList(magAreas).contains(defaultBranch.getMagArea()))
				//					defaultBranch.setMagArea(null);
				//				if (defaultBranch.getValue(InversionModels.class) != null &&
				//						!Arrays.asList(inversionModels).contains(defaultBranch.getValue(InversionModels.class)))
				//					defaultBranch.setInvModel(null);
			}
		}

		if (variationBranches == null && (variations == null || variations.size() == 0)) {
			variationBranches = new ArrayList<CustomArg[]>();
			variationBranches.add(new CustomArg[0]);
		} else if (variationBranches == null) {
			// loop over each variation value building a logic tree
			variationBranches = buildVariationBranches(variations, null);
		}
		//		for (int i=variationBranches.size(); --i >= 0 && variationBranches.size() > 1;) {
		//			int numExtremes = 0;
		//			String[] branch = variationBranches.get(i);
		//			for (int j=0; j<branch.length; j++) {
		//				String[] choices = variations.get(j);
		//				if (branch[j].equals(choices[choices.length-1]))
		//					numExtremes++;
		//			}
		//			if (numExtremes >= 2)
		//				variationBranches.remove(i);
		//		}

		File writeDir;
		if (args.length > 0)
			writeDir = new File(new File(args[0]), runName);
		else
			writeDir = new File(new File("/home/kevin/OpenSHA/UCERF3/inversions"), runName);
		if (!writeDir.exists())
			writeDir.mkdir();

		File runSubDir = new File(site.RUN_DIR, runName);

		//		String queue = "nbns";
		String queue = null;
		//		BatchScriptWriter batch = new USC_HPCC_ScriptWriter("pe1950");
		//		BatchScriptWriter batch = new USC_HPCC_ScriptWriter("quadcore");
		File javaBin = site.JAVA_BIN;
		String threads = "95%"; // max for 8 core nodes, 23/24 for dodecacore
		//		String threads = "1";
		CoolingScheduleType cool = CoolingScheduleType.FAST_SA; // TODO
		CompletionCriteria subCompletion = TimeCompletionCriteria.getInSeconds(1);
		//		CompletionCriteria subCompletion = VariableSubTimeCompletionCriteria.instance("5s", "300");
		boolean keepCurrentAsBest = false;
		System.out.println("SUB: "+subCompletion);
		JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, -1, getClasspath(site));
		javaWriter.setHeadless(true);
		if (site.FM_STORE != null) {
			javaWriter.setProperty(FaultModels.FAULT_MODEL_STORE_PROPERTY_NAME, site.FM_STORE);
		}

		int runDigits = new String((numRuns-1)+"").length();

		double nodeHours = 0;
		int cnt = 0;

		LogicTreeBranchIterator it = new LogicTreeBranchIterator(trimmer);

		for (LogicTreeBranch br : it) {
			for (CustomArg[] variationBranch : variationBranches) {
				VariableLogicTreeBranch branch = new VariableLogicTreeBranch(variationBranch, br);

				InversionModels im = branch.getValue(InversionModels.class);

				if (defaultBranches != null && defaultBranches.length > 0) {
					int closest = Integer.MAX_VALUE;
					for (LogicTreeBranch defaultBranch : defaultBranches) {
						int away = defaultBranch.getNumAwayFrom(branch);
						if (away < closest)
							closest = away;
					}
					if (closest > maxAway.get(im))
						continue;
				}
				String name = branch.buildFileName();
				for (CustomArg variation : variationBranch) {
					if (variation == null)
						// this is the "off" state for a flag option
						name += "_VarNone";
					else
						name += "_Var"+variation.op.getFileName(variation.arg);
				}

				if (nameAdd != null && !nameAdd.isEmpty()) {
					if (!nameAdd.startsWith("_"))
						nameAdd = "_"+nameAdd;
					name += nameAdd;
				}

				int mins;
				NonnegativityConstraintType nonNeg;

				BatchScriptWriter batch = site.forBranch(branch);
				TimeCompletionCriteria checkPointCriteria;
				if (im == InversionModels.GR_CONSTRAINED) {
					mins = 250;
					nonNeg = NonnegativityConstraintType.PREVENT_ZERO_RATES;
					batch = site.forBranch(branch);
					//											checkPointCritera = TimeCompletionCriteria.getInHours(2);
					checkPointCriteria = null;
				} else if (im == InversionModels.CHAR_CONSTRAINED) {
					mins = 250; // TODO ?
					nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
					//											checkPointCritera = TimeCompletionCriteria.getInHours(2);
					checkPointCriteria = null;
				} else { // UNCONSTRAINED
					mins = 60;
					nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
					checkPointCriteria = null;
				}
				int ppn = site.getPPN(branch); // minimum number of cpus
				CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins);
				javaWriter.setMaxHeapSizeMB(site.getMaxHeapSizeMB(branch));
				javaWriter.setInitialHeapSizeMB(site.getInitialHeapSizeMB(branch));

				for (int r=runStart; r<numRuns; r++) {
					String jobName = name;
					if (numRuns > 1) {
						String rStr = r+"";
						while (rStr.length() < runDigits)
							rStr = "0"+rStr;
						jobName += "_run"+rStr;
					}

					File pbs = new File(writeDir, jobName+".pbs");
					System.out.println("Writing: "+pbs.getName());

					int jobMins = mins+30;

					String className = CommandLineInversionRunner.class.getName();
					String classArgs = ThreadedSimulatedAnnealing.completionCriteriaToArgument(criteria);
					classArgs += " "+ThreadedSimulatedAnnealing.subCompletionCriteriaToArgument(subCompletion);
					if (keepCurrentAsBest)
						classArgs += " --cur-as-best";
					classArgs += " --cool "+cool.name();
					classArgs += " --nonneg "+nonNeg.name();
					classArgs += " --num-threads "+threads;
					if (checkPointCriteria != null)
						classArgs += " --checkpoint "+checkPointCriteria.getTimeStr();
					classArgs += " --branch-prefix "+jobName;
					classArgs += " --directory "+runSubDir.getAbsolutePath();
					if (lightweight && r > 0)
						classArgs += " --lightweight";
					//										classArgs += " --slower-cooling 1000";
					for (CustomArg variation : variationBranch) {
						if (variation != null)
							// this is the "off" state for a flag option
							classArgs += " "+variation.op.getCommandLineArgs(variation.arg);
					}

					batch.writeScript(pbs, javaWriter.buildScript(className, classArgs),
							jobMins, 1, ppn, queue);

					nodeHours += (double)mins / 60d;

					cnt++;
				}
			}

		}

		System.out.println("Wrote "+cnt+" jobs");
		System.out.println("Node hours: "+(float)nodeHours + " (/60: "+((float)nodeHours/60f)+") (/14: "+((float)nodeHours/14f)+")");
		//		DeformationModels.forFaultModel(null).toArray(new DeformationModels[0])
		System.exit(0);
	}

}
