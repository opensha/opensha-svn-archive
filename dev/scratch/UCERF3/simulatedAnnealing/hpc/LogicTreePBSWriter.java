package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.dom4j.DocumentException;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.EpicenterScriptWriter;
import org.opensha.commons.hpc.pbs.RangerScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemRupSet;
import scratch.UCERF3.enumTreeBranches.AveSlipForRupModels;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.enumTreeBranches.InversionModels;
import scratch.UCERF3.enumTreeBranches.LogicTreeBranch;
import scratch.UCERF3.enumTreeBranches.MagAreaRelationships;
import scratch.UCERF3.enumTreeBranches.SlipAlongRuptureModels;
import scratch.UCERF3.inversion.InversionConfiguration;
import scratch.UCERF3.inversion.InversionFaultSystemRupSetFactory;
import scratch.UCERF3.inversion.InversionInputGenerator;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.TimeCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;
import scratch.UCERF3.utils.paleoRateConstraints.UCERF3_PaleoRateConstraintFetcher;

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
			public int getHeapSizeMB(LogicTreeBranch branch) {
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
			public int getHeapSizeMB(LogicTreeBranch branch) {
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
			public int getHeapSizeMB(LogicTreeBranch branch) {
				return 30000;
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
		public abstract int getHeapSizeMB(LogicTreeBranch branch);
		public abstract int getPPN(LogicTreeBranch branch);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		String runName = "rup-set-test";
		if (args.length > 1)
			runName = args[1];
		runName = df.format(new Date())+"-"+runName;
		boolean buildRupSets = true;
//		runName = "2012_03_02-weekend-converg-test";
		
//		RunSites site = RunSites.RANGER;
//		RunSites site = RunSites.EPICENTER;
		RunSites site = RunSites.HPCC;

		int numRuns = 1;

		FaultModels[] faultModels = { FaultModels.FM3_1, FaultModels.FM3_2 };

		// if null, all that are applicable to each fault model will be used
		DeformationModels[] defModels = null;
//		DeformationModels[] defModels = { DeformationModels.GEOLOGIC_PLUS_ABM };

		InversionModels[] inversionModels = InversionModels.values();
//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.UNCONSTRAINED };
//		InversionModels[] inversionModels =  { InversionModels.UNCONSTRAINED };
//		InversionModels[] inversionModels =  { InversionModels.CHAR };
//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.GR };
//		InversionModels[] inversionModels =  { InversionModels.GR };

//		MagAreaRelationships[] magAreas = MagAreaRelationships.values();
		MagAreaRelationships[] magAreas = { MagAreaRelationships.ELL_B };

		//		SlipAlongRuptureModels[] slipAlongs = SlipAlongRuptureModels.values();
//				SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED,
//						SlipAlongRuptureModels.UNIFORM };
		SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED };

		AveSlipForRupModels[] aveSlipModels = { AveSlipForRupModels.ELLSWORTH_B };
//		AveSlipForRupModels[] aveSlipModels = AveSlipForRupModels.values();
		
		// this is a somewhat kludgy way of passing in a special variation to the input generator
//		String[] variations = { "MomRed_100", "MomRed_090", "MomRed_080", "MomRed_070",
//				"MomRed_060", "MomRed_050", "MomRed_040", "MomRed_030", "MomRed_020", "MomRed_010" };
		String[] variations = null;
		
		// do all branch choices relative to these:
//		Branch defaultBranch = null;
		HashMap<InversionModels, Integer> maxAway = Maps.newHashMap();
		maxAway.put(InversionModels.CHAR, 1);
		maxAway.put(InversionModels.GR, 1);
		maxAway.put(InversionModels.UNCONSTRAINED, 1);
		LogicTreeBranch[] defaultBranches = {
				new LogicTreeBranch(null, null, MagAreaRelationships.ELL_B,
								AveSlipForRupModels.ELLSWORTH_B, null, null),
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
		
		if (variations == null || variations.length == 0) {
			variations = new String[1];
			variations[0] = null;
		}

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
		CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
		CompletionCriteria subCompletion = TimeCompletionCriteria.getInSeconds(1);
		JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, -1, getClasspath(site));
		javaWriter.setHeadless(true);
		if (site.FM_STORE != null) {
			javaWriter.setProperty(FaultModels.FAULT_MODEL_STORE_PROPERTY_NAME, site.FM_STORE);
			buildRupSets = false;
		}
		ThreadedScriptCreator tsa_create = new ThreadedScriptCreator(javaWriter, threads, null, null, subCompletion);
		tsa_create.setPlots(true);
		tsa_create.setCool(cool);

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
				for (MagAreaRelationships ma : magAreas) {
					for (SlipAlongRuptureModels sal : slipAlongs) {
						for (AveSlipForRupModels as : aveSlipModels) {

							for (String variation : variations) {
								for (InversionModels im : inversionModels) {
									LogicTreeBranch branch = new LogicTreeBranch(fm, dm, ma, as, sal, im);
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
									
									String name = fm.getShortName()+"_"+dm.getShortName()
											+"_Ma"+ma.getShortName()+"_Dsr"+sal.getShortName()+"_Dr"+as.getShortName()
											+"_"+im.getShortName();
									
									if (variation != null)
										name += "_Var"+variation;
									
									int mins;
									NonnegativityConstraintType nonNeg;
									
									BatchScriptWriter batch = site.forBranch(branch);
									TimeCompletionCriteria checkPointCritera;
									if (im == InversionModels.GR) {
										mins = 500;
										nonNeg = NonnegativityConstraintType.PREVENT_ZERO_RATES;
										batch = site.forBranch(branch);
//										checkPointCritera = TimeCompletionCriteria.getInHours(2);
										checkPointCritera = null;
									} else if (im == InversionModels.CHAR) {
										mins = 500; // TODO ?
										nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
//										checkPointCritera = TimeCompletionCriteria.getInHours(2);
										checkPointCritera = null;
									} else { // UNCONSTRAINED
										mins = 60;
										nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
										checkPointCritera = null;
									}
									int ppn = site.getPPN(branch); // minimum number of cpus
									int heapSizeMB = site.getHeapSizeMB(branch);
									CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins);
									tsa_create.setCriteria(criteria);
									javaWriter.setMaxHeapSizeMB(heapSizeMB);
									tsa_create.setNonNeg(nonNeg);
									tsa_create.setCheckPointCriteria(checkPointCritera);

									for (int r=0; r<numRuns; r++) {
										String jobName = name;
										if (numRuns > 1)
											jobName += "_run"+r;
										
										File localRupSetFile = new File(writeDir, name+"_rupSet.zip");
										File remoteRupSetFile = new File(runSubDir, name+"_rupSet.zip");
										
										if (buildRupSets && !localRupSetFile.exists()) {
											SimpleFaultSystemRupSet rupSet = new SimpleFaultSystemRupSet(
													InversionFaultSystemRupSetFactory.forBranch(fm, dm, ma, as, sal, im));
											rupSet.toZipFile(localRupSetFile);
											System.gc();
										}

										tsa_create.setProgFile(new File(runSubDir, jobName+".csv"));
										tsa_create.setSolFile(new File(runSubDir, jobName+".bin"));

										File pbs = new File(writeDir, jobName+".pbs");
										System.out.println("Writing: "+pbs.getName());
										
										ArrayList<String> classNames = new ArrayList<String>();
										ArrayList<String> argss = new ArrayList<String>();
										
										int jobMins = mins+30;
										
										// input gen
										String inputFileName = jobName+"_inputs.zip";
										File remoteInputs = new File(runSubDir, inputFileName);
										
										classNames.add(CommandLineInputGenerator.class.getName());
										String inputGenArgs;
										if (variation == null)
											inputGenArgs = "";
										else
											inputGenArgs = "--var "+variation+" ";
										inputGenArgs += remoteRupSetFile.getAbsolutePath()+" "+im.name()
												+" "+remoteInputs.getAbsolutePath();
										argss.add(inputGenArgs);
										jobMins += 30;
										tsa_create.setZipFile(remoteInputs);
										
										classNames.add(tsa_create.getClassName());
										argss.add(tsa_create.getArgs());

										batch.writeScript(pbs, javaWriter.buildScript(classNames, argss),
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
