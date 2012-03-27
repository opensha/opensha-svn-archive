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
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.inversion.CommandLineInversionRunner;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.CommandLineInversionRunner.InversionOptions;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.PaleoProbabilityModel;

import com.google.common.base.Preconditions;
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
		"/home/scec-02/kmilner/ucerf3/inversions/fm_store") {
			@Override
			public BatchScriptWriter forBranch(LogicTreeBranch branch) {
				InversionModels im = branch.getInvModel();
				Preconditions.checkState(im != InversionModels.GR,
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
		"/home/scec-02/kmilner/ucerf3/inversions/fm_store") {
			@Override
			public BatchScriptWriter forBranch(LogicTreeBranch branch) {
				if (branch.getInvModel() == InversionModels.GR)
					return new USC_HPCC_ScriptWriter("dodecacore");
				return new USC_HPCC_ScriptWriter("quadcore");
			}

			@Override
			public int getMaxHeapSizeMB(LogicTreeBranch branch) {
				if (branch.getInvModel() == InversionModels.GR)
					return 40000;
				return 10000;
			}

			@Override
			public int getPPN(LogicTreeBranch branch) {
				if (branch.getInvModel() == InversionModels.GR)
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
		public VariableLogicTreeBranch(FaultModels fm,
				DeformationModels dm,
				MagAreaRelationships ma,
				AveSlipForRupModels as,
				SlipAlongRuptureModels sal,
				InversionModels im,
				CustomArg[] args) {
			super(fm, dm, ma, as, sal, im);
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

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		String runName = "production-supplement";
		if (args.length > 1)
			runName = args[1];
		runName = df.format(new Date())+"-"+runName;
		boolean buildRupSets = true;
		//		runName = "2012_03_02-weekend-converg-test";

		//		RunSites site = RunSites.RANGER;
//		RunSites site = RunSites.EPICENTER;
		RunSites site = RunSites.HPCC;

		int numRuns = 1;
		
		boolean lightweight = numRuns > 10;

		FaultModels[] faultModels = { FaultModels.FM3_1 };
//		FaultModels[] faultModels = { FaultModels.FM3_1, FaultModels.FM3_2 };
//		FaultModels[] faultModels = { FaultModels.FM2_1 };

		// if null, all that are applicable to each fault model will be used
//		DeformationModels[] defModels = null;
		DeformationModels[] defModels = { DeformationModels.GEOLOGIC_PLUS_ABM };
//		DeformationModels[] defModels = { DeformationModels.UCERF2_ALL };

//		InversionModels[] inversionModels = InversionModels.values();
//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.UNCONSTRAINED };
//		InversionModels[] inversionModels =  { InversionModels.UNCONSTRAINED };
		InversionModels[] inversionModels =  { InversionModels.CHAR };
//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.GR };
//		InversionModels[] inversionModels =  { InversionModels.GR };

		//		MagAreaRelationships[] magAreas = MagAreaRelationships.values();
		MagAreaRelationships[] magAreas = { MagAreaRelationships.ELL_B };
//		MagAreaRelationships[] magAreas = { MagAreaRelationships.AVE_UCERF2 };
//		MagAreaRelationships[] magAreas = { MagAreaRelationships.ELL_B, MagAreaRelationships.HB_08,
//				MagAreaRelationships.SHAW_09 };

		//		SlipAlongRuptureModels[] slipAlongs = SlipAlongRuptureModels.values();
		SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED,
								SlipAlongRuptureModels.UNIFORM };
//		SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED };

		AveSlipForRupModels[] aveSlipModels = { AveSlipForRupModels.ELLSWORTH_B };
//		AveSlipForRupModels[] aveSlipModels = { AveSlipForRupModels.AVE_UCERF2 };
//		AveSlipForRupModels[] aveSlipModels = { AveSlipForRupModels.ELLSWORTH_B,
//				AveSlipForRupModels.SHAW12_SQRT_LENGTH, AveSlipForRupModels.SHAW_12_CONST_STRESS_DROP,
//				AveSlipForRupModels.SHAW_2009_MOD };
		//		AveSlipForRupModels[] aveSlipModels = AveSlipForRupModels.values();

		// this is a somewhat kludgy way of passing in a special variation to the input generator
		ArrayList<CustomArg[]> variationBranches = null;
		List<CustomArg[]> variations = null;
		
		variationBranches = new ArrayList<LogicTreePBSWriter.CustomArg[]>();
		InversionOptions[] ops = { InversionOptions.DEFAULT_ASEISMICITY, InversionOptions.OFF_FUALT_ASEIS,
				InversionOptions.MFD_MODIFICATION, InversionOptions.MFD_CONSTRAINT_RELAX };
//		variationBranches.add(buildVariationBranch(ops, toArray("0", "0", "1", null)));
		variationBranches.add(buildVariationBranch(ops, toArray("0.1", "0.5", "1", null)));
//		variationBranches.add(buildVariationBranch(ops, toArray("0", "0", "1.3", null)));
//		variationBranches.add(buildVariationBranch(ops, toArray("0", "0", "1", TAG_OPTION_ON)));
		
//		variationBranches.add(buildVariationBranch(ops, toArray("0", "0", "1.35", null)));
//		variationBranches.add(buildVariationBranch(ops, toArray("0", "0", "1.4", null)));
		
//		List<CustomArg[]> variations = new ArrayList<CustomArg[]>();
//		variations.add(forOptions(InversionOptions.OFF_FUALT_ASEIS, "0", "0.5"));
//		variations.add(forOptions(InversionOptions.MFD_MODIFICATION, "1", "1.3"));
//		variations.add(forOptions(InversionOptions.DEFAULT_ASEISMICITY, "0", "0.2"));
//		CustomArg[] relaxOps = { new CustomArg(InversionOptions.MFD_CONSTRAINT_RELAX, null), null };
//		variations.add(relaxOps);
		
		// do all branch choices relative to these:
		//		Branch defaultBranch = null;
		HashMap<InversionModels, Integer> maxAway = Maps.newHashMap();
		maxAway.put(InversionModels.CHAR, 0);
		maxAway.put(InversionModels.GR, 0);
		maxAway.put(InversionModels.UNCONSTRAINED, 0);
		VariableLogicTreeBranch[] defaultBranches = {
//				new VariableLogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
//						AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, null,
//						buildVariationBranch(ops, toArray("0.2", "0.5", "1", null))),
				new VariableLogicTreeBranch(null, null, MagAreaRelationships.ELL_B,
						AveSlipForRupModels.ELLSWORTH_B, SlipAlongRuptureModels.TAPERED, null,
						null),
						//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC, MagAreaRelationships.ELL_B,
						//								AveSlipForRupModels.ELLSWORTH_B, null, null),
						//				new LogicTreeBranch(null, DeformationModels.GEOLOGIC_PLUS_ABM, MagAreaRelationships.ELL_B,
						//								AveSlipForRupModels.ELLSWORTH_B, null, null)
		};
		if (defaultBranches != null) {
			// make sure all default branch choices are valid!
			for (LogicTreeBranch defaultBranch : defaultBranches) {
				if (defaultBranch.getFaultModel() != null &&
						!Arrays.asList(faultModels).contains(defaultBranch.getFaultModel()))
					defaultBranch.setFaultModel(null);
				if (defaultBranch.getDefModel() != null &&
						defModels != null && !Arrays.asList(defModels).contains(defaultBranch.getDefModel()))
					defaultBranch.setDefModel(null);
				if (defaultBranch.getAveSlip() != null &&
						!Arrays.asList(aveSlipModels).contains(defaultBranch.getAveSlip()))
					defaultBranch.setAveSlip(null);
				if (defaultBranch.getSlipAlong() != null &&
						!Arrays.asList(slipAlongs).contains(defaultBranch.getSlipAlong()))
					defaultBranch.setSlipAlong(null);
				if (defaultBranch.getMagArea() != null &&
						!Arrays.asList(magAreas).contains(defaultBranch.getMagArea()))
					defaultBranch.setMagArea(null);
				if (defaultBranch.getInvModel() != null &&
						!Arrays.asList(inversionModels).contains(defaultBranch.getInvModel()))
					defaultBranch.setInvModel(null);
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
		CoolingScheduleType cool = CoolingScheduleType.FAST_SA; // TODO
		CompletionCriteria subCompletion = TimeCompletionCriteria.getInSeconds(1);
		JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, -1, getClasspath(site));
		javaWriter.setHeadless(true);
		if (site.FM_STORE != null) {
			javaWriter.setProperty(FaultModels.FAULT_MODEL_STORE_PROPERTY_NAME, site.FM_STORE);
			buildRupSets = false;
		}
		
		int runDigits = new String((numRuns-1)+"").length();

		double nodeHours = 0;
		int cnt = 0;

		PaleoProbabilityModel paleoProbabilityModel = PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();

		for (FaultModels fm : faultModels) {
			DeformationModels[] defModelsForFM;
			if (defModels == null)
				defModelsForFM = DeformationModels.forFaultModel(fm).toArray(new DeformationModels[0]);
			else
				defModelsForFM = defModels;
			for (DeformationModels dm : defModelsForFM) {
				if (!dm.isApplicableTo(fm))
					continue;
				for (MagAreaRelationships ma : magAreas) {
					for (SlipAlongRuptureModels sal : slipAlongs) {
						for (AveSlipForRupModels as : aveSlipModels) {
							for (CustomArg[] variationBranch : variationBranches) {
								for (InversionModels im : inversionModels) {
									VariableLogicTreeBranch branch = new VariableLogicTreeBranch(fm, dm, ma, as, sal, im, variationBranch);
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

									int mins;
									NonnegativityConstraintType nonNeg;

									BatchScriptWriter batch = site.forBranch(branch);
									TimeCompletionCriteria checkPointCriteria;
									if (im == InversionModels.GR) {
										mins = 500;
										nonNeg = NonnegativityConstraintType.PREVENT_ZERO_RATES;
										batch = site.forBranch(branch);
										//											checkPointCritera = TimeCompletionCriteria.getInHours(2);
										checkPointCriteria = null;
									} else if (im == InversionModels.CHAR) {
										mins = 500; // TODO ?
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

									for (int r=0; r<numRuns; r++) {
										String jobName = name;
										if (numRuns > 1) {
											String rStr = r+"";
											while (rStr.length() < runDigits)
												rStr = "0"+rStr;
											jobName += "_run"+rStr;
										}

										File localRupSetFile = new File(writeDir, name+"_rupSet.zip");
										File remoteRupSetFile = new File(runSubDir, name+"_rupSet.zip");

										if (buildRupSets && !localRupSetFile.exists()) {
											SimpleFaultSystemRupSet rupSet = new SimpleFaultSystemRupSet(
													InversionFaultSystemRupSetFactory.forBranch(fm, dm, ma, as, sal, im));
											rupSet.toZipFile(localRupSetFile);
											System.gc();
										}

										File pbs = new File(writeDir, jobName+".pbs");
										System.out.println("Writing: "+pbs.getName());

										int jobMins = mins+30;
										
										String className = CommandLineInversionRunner.class.getName();
										String classArgs = ThreadedSimulatedAnnealing.completionCriteriaToArgument(criteria);
										classArgs += " "+ThreadedSimulatedAnnealing.subCompletionCriteriaToArgument(subCompletion);
										classArgs += " --cool "+cool.name();
										classArgs += " --nonneg "+nonNeg.name();
										classArgs += " --num-threads "+threads;
										if (checkPointCriteria != null)
											classArgs += " --checkpoint "+checkPointCriteria.getTimeStr();
										classArgs += " --branch-prefix "+jobName;
										classArgs += " --directory "+runSubDir.getAbsolutePath();
										if (lightweight && r > 0)
											classArgs += " --lightweight";
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
						}
					}
				}
			}
		}
		System.out.println("Wrote "+cnt+" jobs");
		System.out.println("Node hours: "+(float)nodeHours + " (/60: "+((float)nodeHours/60f)+") (/14: "+((float)nodeHours/14f)+")");
		//		DeformationModels.forFaultModel(null).toArray(new DeformationModels[0])
		System.exit(0);
	}

}
