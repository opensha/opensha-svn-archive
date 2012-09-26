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
import scratch.UCERF3.logicTree.LogicalAndTrimmer;
import scratch.UCERF3.logicTree.LogicalNotTreeTrimmer;
import scratch.UCERF3.logicTree.LogicalOrTrimmer;
import scratch.UCERF3.logicTree.SingleValsTreeTrimmer;
import scratch.UCERF3.logicTree.TreeTrimmer;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.VariableSubTimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoProbabilityModel;

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

	private static List<LogicTreeBranchNode<?>> getNonZeroChoices(Class<? extends LogicTreeBranchNode<?>> clazz, InversionModels im) {
		List<LogicTreeBranchNode<?>> nonZeros = Lists.newArrayList();
		for (LogicTreeBranchNode<?> val : clazz.getEnumConstants())
			if (val.getRelativeWeight(im) > 0)
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
	private static final String TAG_OPTION_OFF = null;

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
	
	private static LogicTreeBranch getUCERF2_noIM() {
		LogicTreeBranch UCERF2_noIM = (LogicTreeBranch) LogicTreeBranch.UCERF2.clone();
		UCERF2_noIM.clearValue(InversionModels.class);
//		UCERF2_noIM.clearValue(MomentRateFixes.class);
		return UCERF2_noIM;
	}
	
	private static TreeTrimmer getUCERF2Trimmer() {
		return new ListBasedTreeTrimmer(getUCERF2_noIM(), false);
	}
	
	private static TreeTrimmer getAllDM_IM_Trimmer(boolean bothFMs) {
		List<List<LogicTreeBranchNode<?>>> limitations = Lists.newArrayList();
		
		List<LogicTreeBranchNode<?>> faultModels = Lists.newArrayList();
		faultModels.add(FaultModels.FM3_1);
		if (bothFMs)
			faultModels.add(FaultModels.FM3_2);
		limitations.add(faultModels);
		
		List<LogicTreeBranchNode<?>> defModels = getNonZeroChoices(DeformationModels.class, null);
		limitations.add(defModels);
		
		List<LogicTreeBranchNode<?>> invModels = getNonZeroChoices(InversionModels.class, null);
		limitations.add(invModels);
		
		return ListBasedTreeTrimmer.getDefaultPlusSpecifiedTrimmer(limitations);
	}
	
	private static TreeTrimmer getUCERF3RefBranches() {
		List<LogicTreeBranch> branches = Lists.newArrayList();
		
		List<LogicTreeBranchNode<?>> dms = getNonZeroChoices(DeformationModels.class, null);
		List<LogicTreeBranchNode<?>> ims = getNonZeroChoices(InversionModels.class, null);
		
		// UCERF3
		for (LogicTreeBranchNode<?> dm : dms) {
			for (LogicTreeBranchNode<?> im : ims) {
				boolean isChar = ((InversionModels)im).isCharacteristic();
				MomentRateFixes momFix;
//				if (isChar)
					momFix = MomentRateFixes.NONE;
//				else
//					momFix = MomentRateFixes.APPLY_IMPLIED_CC;
				branches.add(LogicTreeBranch.fromValues(false, FaultModels.FM3_1, dm, im,
						ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.UNIFORM, TotalMag5Rate.RATE_8p7,
						MaxMagOffFault.MAG_7p6, momFix, SpatialSeisPDF.UCERF3));
			}
		}
		
		// UCERF2
		for (LogicTreeBranchNode<?> im : ims) {
			boolean isChar = ((InversionModels)im).isCharacteristic();
			MomentRateFixes momFix;
//			if (isChar)
				momFix = MomentRateFixes.NONE;
//			else
//				momFix = MomentRateFixes.APPLY_IMPLIED_CC;
			branches.add(LogicTreeBranch.fromValues(false, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, im,
					ScalingRelationships.AVE_UCERF2, SlipAlongRuptureModels.TAPERED, TotalMag5Rate.RATE_8p7,
					MaxMagOffFault.MAG_7p6, momFix, SpatialSeisPDF.UCERF2));
		}
		
		return new DiscreteListTreeTrimmer(branches);
	}
	
	public static TreeTrimmer getNonZeroOrUCERF2Trimmer() {
		final TreeTrimmer nonZero = ListBasedTreeTrimmer.getNonZeroWeightsTrimmer();
		final TreeTrimmer ucerf2Trim = getUCERF2Trimmer();
		
		return new TreeTrimmer() {
			
			@Override
			public boolean isTreeValid(LogicTreeBranch branch) {
				return nonZero.isTreeValid(branch) || ucerf2Trim.isTreeValid(branch);
			}
		};
	}
	
	public static TreeTrimmer getNoUCERF2Trimmer() {
		return new TreeTrimmer() {
			
			@Override
			public boolean isTreeValid(LogicTreeBranch branch) {
				return !branch.getValue(FaultModels.class).equals(FaultModels.FM2_1);
			}
		};
	}
	
	public static TreeTrimmer getNeokinemaOnlyTrimmer() {
		return new TreeTrimmer() {
			
			@Override
			public boolean isTreeValid(LogicTreeBranch branch) {
				return branch.getValue(DeformationModels.class).equals(DeformationModels.NEOKINEMA);
			}
		};
	}
	
	public static TreeTrimmer getZengOnlyTrimmer() {
		return new TreeTrimmer() {
			
			@Override
			public boolean isTreeValid(LogicTreeBranch branch) {
				return branch.getValue(DeformationModels.class).equals(DeformationModels.ZENG);
			}
		};
	}
	
	private static TreeTrimmer getDiscreteCustomTrimmer() {
		List<LogicTreeBranch> branches = Lists.newArrayList();
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.ELLB_SQRT_LENGTH));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.ELLSWORTH_B));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.HANKS_BAKUN_08));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.SHAW_2009_MOD));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.SHAW_CONST_STRESS_DROP));
		
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.AVE_UCERF2, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.ELLB_SQRT_LENGTH, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM3_1, DeformationModels.ZENG, ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF3));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.HANKS_BAKUN_08, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.SHAW_2009_MOD, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
//		branches.add(LogicTreeBranch.fromValues(true, FaultModels.FM2_1, DeformationModels.UCERF2_ALL, ScalingRelationships.SHAW_CONST_STRESS_DROP, SlipAlongRuptureModels.TAPERED, InversionModels.CHAR_CONSTRAINED, TotalMag5Rate.RATE_8p7,
//				MaxMagOffFault.MAG_7p6, MomentRateFixes.NONE, SpatialSeisPDF.UCERF2));
		
		return new DiscreteListTreeTrimmer(branches);
	}
	
	private static TreeTrimmer getCustomTrimmer() {
		List<List<LogicTreeBranchNode<?>>> limitations = Lists.newArrayList();

		List<LogicTreeBranchNode<?>> faultModels = toList(FaultModels.FM3_1);
//		List<LogicTreeBranchNode<?>> faultModels = toList(FaultModels.FM3_1, FaultModels.FM3_2);
		limitations.add(faultModels);

		// if null, all that are applicable to each fault model will be used
		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.GEOLOGIC);
//		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.ABM);
//		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.NEOKINEMA);
//		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.ZENG);
//		List<LogicTreeBranchNode<?>> defModels = toList(DeformationModels.GEOLOGIC, DeformationModels.ABM, DeformationModels.NEOKINEMA, DeformationModels.ZENG);
		limitations.add(defModels);

//		List<LogicTreeBranchNode<?>> inversionModels = allOf(InversionModels.class);
		List<LogicTreeBranchNode<?>> inversionModels = toList(InversionModels.GR_CONSTRAINED);
		limitations.add(inversionModels);
		//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.UNCONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.UNCONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.CHAR_CONSTRAINED };
		//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.GR };
//				InversionModels[] inversionModels =  { InversionModels.GR_CONSTRAINED };

//		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.ELLSWORTH_B);
//		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.ELLSWORTH_B, ScalingRelationships.HANKS_BAKUN_08);
		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.HANKS_BAKUN_08);
//		List<LogicTreeBranchNode<?>> scaling = toList(ScalingRelationships.SHAW_2009_MOD, ScalingRelationships.SHAW_CONST_STRESS_DROP,
//					ScalingRelationships.ELLSWORTH_B, ScalingRelationships.ELLB_SQRT_LENGTH, ScalingRelationships.HANKS_BAKUN_08);
		limitations.add(scaling);

//		List<LogicTreeBranchNode<?>> slipAlongs = getNonZeroChoices(SlipAlongRuptureModels.class);
		List<LogicTreeBranchNode<?>> slipAlongs = toList(SlipAlongRuptureModels.UNIFORM);
		limitations.add(slipAlongs);

//		List<LogicTreeBranchNode<?>> mag5s = getNonZeroChoices(TotalMag5Rate.class);
		List<LogicTreeBranchNode<?>> mag5s = toList(TotalMag5Rate.RATE_10p6);
//		List<LogicTreeBranchNode<?>> mag5s = toList(TotalMag5Rate.RATE_10p6, TotalMag5Rate.RATE_8p7);
		limitations.add(mag5s);

//		List<LogicTreeBranchNode<?>> maxMags = getNonZeroChoices(MaxMagOffFault.class);
//		List<LogicTreeBranchNode<?>> maxMags = toList(MaxMagOffFault.MAG_7p6, MaxMagOffFault.MAG_8p0);
		List<LogicTreeBranchNode<?>> maxMags = toList(MaxMagOffFault.MAG_7p6);
		limitations.add(maxMags);

//		List<LogicTreeBranchNode<?>> momentFixes = getNonZeroChoices(MomentRateFixes.class);
//		List<LogicTreeBranchNode<?>> momentFixes = toList(MomentRateFixes.NONE, MomentRateFixes.APPLY_IMPLIED_CC);
		List<LogicTreeBranchNode<?>> momentFixes = toList(MomentRateFixes.NONE);
		limitations.add(momentFixes);

//		List<LogicTreeBranchNode<?>> spatialSeis = getNonZeroChoices(SpatialSeisPDF.class);
		List<LogicTreeBranchNode<?>> spatialSeis = toList(SpatialSeisPDF.UCERF3);
		limitations.add(spatialSeis);
		
		return new ListBasedTreeTrimmer(limitations);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		String runName = "ucerf2-ref-initial-normal";
		if (args.length > 1)
			runName = args[1];
//		int constrained_run_mins = 60;
//		int constrained_run_mins = 250;
//		int constrained_run_mins = 360;
		int constrained_run_mins = 500;
//		int constrained_run_mins = 10;
		runName = df.format(new Date())+"-"+runName;
		//		runName = "2012_03_02-weekend-converg-test";

		//		RunSites site = RunSites.RANGER;
		//		RunSites site = RunSites.EPICENTER;
		RunSites site = RunSites.HPCC;

		//		String nameAdd = "VarSub5_0.3";
		String nameAdd = null;

		int numRuns = 100;
		int runStart = 0;

		boolean lightweight = numRuns > 10;

//		TreeTrimmer trimmer = getCustomTrimmer();
//		TreeTrimmer trimmer = getNonZeroOrUCERF2Trimmer();
//		TreeTrimmer trimmer = getUCERF2Trimmer();
		TreeTrimmer trimmer = getDiscreteCustomTrimmer();
		
		TreeTrimmer charOnly = new SingleValsTreeTrimmer(InversionModels.CHAR_CONSTRAINED);
		TreeTrimmer charUnconstOnly = new SingleValsTreeTrimmer(InversionModels.CHAR_UNCONSTRAINED);
		TreeTrimmer grOnly = new SingleValsTreeTrimmer(InversionModels.GR_CONSTRAINED);
		TreeTrimmer grUnconstOnly = new SingleValsTreeTrimmer(InversionModels.GR_UNCONSTRAINED);
		TreeTrimmer charOrGR = new LogicalOrTrimmer(charOnly, grOnly);
//		TreeTrimmer neoKOnly = new SingleValsTreeTrimmer(DeformationModels.NEOKINEMA);
		TreeTrimmer noRefBranches = new LogicalNotTreeTrimmer(getUCERF3RefBranches());
		TreeTrimmer noUCERF2 = getNoUCERF2Trimmer();
//		trimmer = new LogicalAndTrimmer(trimmer, charOrGR);
//		trimmer = new LogicalAndTrimmer(trimmer, charOrGR, noUCERF2);
//		trimmer = new LogicalAndTrimmer(trimmer, charOrGR, noUCERF2);
//		trimmer = new LogicalAndTrimmer(trimmer, charUnconstOnly, noUCERF2);
//		trimmer = new LogicalAndTrimmer(trimmer, grUnconstOnly, noUCERF2);
		trimmer = new LogicalAndTrimmer(trimmer, charOnly);
//		trimmer = new LogicalAndTrimmer(trimmer, charOnly, noUCERF2);
//		trimmer = new LogicalAndTrimmer(trimmer, grOnly);
//		trimmer = new LogicalAndTrimmer(trimmer, grOnly, noUCERF2);
//		trimmer = new LogicalAndTrimmer(trimmer, grOnly, noRefBranches, noUCERF2);
		
//		trimmer = new LogicalAndTrimmer(trimmer, new SingleValsTreeTrimmer(ScalingRelationships.ELLSWORTH_B));
		
		
//		TreeTrimmer defaultBranchesTrimmer = getUCERF3RefBranches();
//		defaultBranchesTrimmer = new LogicalAndTrimmer(defaultBranchesTrimmer, getZengOnlyTrimmer());
//		TreeTrimmer defaultBranchesTrimmer = getCustomTrimmer(false);
		TreeTrimmer defaultBranchesTrimmer = null;

		// this is a somewhat kludgy way of passing in a special variation to the input generator
		ArrayList<CustomArg[]> variationBranches = null;
		List<CustomArg[]> variations = null;
		
		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
//		InversionOptions[] ops = { InversionOptions.PALEO_WT, InversionOptions.EVENT_SMOOTH_WT };
//		InversionOptions[] ops = { InversionOptions.PALEO_WT, InversionOptions.EVENT_SMOOTH_WT, InversionOptions.A_PRIORI_CONST_WT };
		InversionOptions[] ops = { InversionOptions.PALEO_WT, InversionOptions.AVE_SLIP_WT,
				InversionOptions.MFD_SMOOTHNESS_WT, InversionOptions.SECTION_NUCLEATION_MFD_WT,
				InversionOptions.INITIAL_ZERO};
		List<String[]> argVals = Lists.newArrayList();
		// paleo
		argVals.add(toArray("0.1"));
		// ave slip
		argVals.add(toArray("0.1"));
		// mfd smoothness
		argVals.add(toArray("1000"));
		// section mfd
		argVals.add(toArray("0.1"));
		// initial zero
		argVals.add(toArray(TAG_OPTION_OFF));
		
		for (String val1 : argVals.get(0))
			for (String val2 : argVals.get(1))
				for (String val3 : argVals.get(2))
					for (String val4 : argVals.get(3))
						for (String val5 : argVals.get(4))
							variationBranches.add(buildVariationBranch(ops, toArray(val1, val2, val3, val4, val5)));
		
//		variationBranches.add(buildVariationBranch(ops, toArray("0")));
//		String[] mfdTrans = { "7.85" };
//		String[] aPrioriWts = { "0" };
//		String[] nuclWts = { "0.001", "0.01", "0.1" };
//		String[] paleoWts = { "0.1", "1", "10" };
//		String[] mfdWts = { "100", "1000", "10000" };
//		String[] eventSmoothWts = { "0", "100", "1000", "10000" };
//		for (String aPrioriWt : aPrioriWts)
//			for (String nuclWt : nuclWts)
//				for (String paleoWt : paleoWts)
//					for (String mfdWt : mfdWts)
//						variationBranches.add(buildVariationBranch(ops, toArray(nuclWt, paleoWt, mfdWt)));
//		String[] eventSmoothWts = { "0", "1000", "10000", "100000" };
//		String[] paleoWts = { "100", "1000" };
//		for (String paleoWt : paleoWts)
//			for (String eventSmoothWt : eventSmoothWts)
//				variationBranches.add(buildVariationBranch(ops, toArray(paleoWt, eventSmoothWt, "0")));
//		variationBranches.add(buildVariationBranch(ops, toArray("0.1", "0")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, "100")));
		
//		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
//		InversionOptions[] ops = { InversionOptions.INITIAL_ZERO };
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF)));
		
//		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
//		InversionOptions[] ops = { InversionOptions.NO_SUBSEIS_RED,
//				InversionOptions.A_PRIORI_CONST_FOR_ZERO_RATES, InversionOptions.MFD_WT };
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_OFF, "0")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_ON, "0")));
		
//		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
//		InversionOptions[] ops = { InversionOptions.NO_SUBSEIS_RED,
//				InversionOptions.A_PRIORI_CONST_FOR_ZERO_RATES, InversionOptions.A_PRIORI_CONST_WT };
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_OFF, "100")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_OFF, "1000")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_ON, "100")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_OFF, TAG_OPTION_ON, "1000")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, TAG_OPTION_OFF, "100")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, TAG_OPTION_OFF, "1000")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, TAG_OPTION_ON, "100")));
//		variationBranches.add(buildVariationBranch(ops, toArray(TAG_OPTION_ON, TAG_OPTION_ON, "1000")));

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
		maxAway.put(InversionModels.CHAR_CONSTRAINED, 0);
		maxAway.put(InversionModels.CHAR_UNCONSTRAINED, 0);
		maxAway.put(InversionModels.GR_CONSTRAINED, 0);
		maxAway.put(InversionModels.GR_UNCONSTRAINED, 0);
		VariableLogicTreeBranch[] defaultBranches = null;
		
		if (defaultBranchesTrimmer != null) {
			List<LogicTreeBranch> defBranches = Lists.newArrayList();
			for (LogicTreeBranch branch : new LogicTreeBranchIterator(defaultBranchesTrimmer))
				defBranches.add(branch);
			defaultBranches = new VariableLogicTreeBranch[defBranches.size()];
			for (int i=0; i<defBranches.size(); i++) {
				LogicTreeBranch branch = defBranches.get(i);
				defaultBranches[i] = new VariableLogicTreeBranch(null, branch);
			}
		}
		
//		VariableLogicTreeBranch[] defaultBranches = {
//				//				new VariableLogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
//				//						AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, null,
//				//						buildVariationBranch(ops, toArray("0.2", "0.5", "1", null))),
//				new VariableLogicTreeBranch(null, false, FaultModels.FM3_1, TotalMag5Rate.RATE_8p7, MaxMagOffFault.MAG_7p6,
//						MomentRateFixes.NONE, ScalingRelationships.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, SpatialSeisPDF.UCERF3)
//				//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC, MagAreaRelationships.ELL_B,
//				//								AveSlipForRupModels.ELLSWORTH_B, null, null),
//				//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
//				//								AveSlipForRupModels.ELLSWORTH_B, null, null)
//		};

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
		CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
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
					mins = constrained_run_mins;
					nonNeg = NonnegativityConstraintType.PREVENT_ZERO_RATES;
					batch = site.forBranch(branch);
					//											checkPointCritera = TimeCompletionCriteria.getInHours(2);
					checkPointCriteria = null;
				} else if (im == InversionModels.CHAR_CONSTRAINED) {
					mins = constrained_run_mins;
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

					int jobMins = mins+60;

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
