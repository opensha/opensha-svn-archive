package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.dom4j.DocumentException;
import org.opensha.commons.hpc.JavaShellScriptWriter;
import org.opensha.commons.hpc.pbs.BatchScriptWriter;
import org.opensha.commons.hpc.pbs.USC_HPCC_ScriptWriter;

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

	public static File RUN_DIR = new File("/home/scec-02/kmilner/ucerf3/inversions");

	public static ArrayList<File> getClasspath() {
		ArrayList<File> jars = new ArrayList<File>();
		jars.add(new File(RUN_DIR, "OpenSHA_complete.jar"));
		jars.add(new File(RUN_DIR, "parallelcolt-0.9.4.jar"));
		jars.add(new File(RUN_DIR, "commons-cli-1.2.jar"));
		jars.add(new File(RUN_DIR, "csparsej.jar"));
		return jars;
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void main(String[] args) throws IOException, DocumentException {
		String runName = "weekend-converg-test";
		if (args.length > 1)
			runName = args[1];
		runName = df.format(new Date())+"-"+runName;
		runName = "2012_03_02-weekend-converg-test";
		boolean buildRupSets = true;

		int numRuns = 1;

		FaultModels[] faultModels = { FaultModels.FM3_1, FaultModels.FM3_2 };

		// if null, all that are applicable to each fault model will be used
		DeformationModels[] defModels = null;
//		DeformationModels[] defModels = { DeformationModels.GEOLOGIC_PLUS_ABM };

		InversionModels[] inversionModels = InversionModels.values();
//		InversionModels[] inversionModels =  { InversionModels.CHAR, InversionModels.UNCONSTRAINED };

		MagAreaRelationships[] magAreas = MagAreaRelationships.values();
//		MagAreaRelationships[] magAreas = { MagAreaRelationships.AVE_UCERF2 };

		//		SlipAlongRuptureModels[] slipAlongs = SlipAlongRuptureModels.values();
				SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED,
						SlipAlongRuptureModels.UNIFORM, SlipAlongRuptureModels.WG02 };
//		SlipAlongRuptureModels[] slipAlongs = { SlipAlongRuptureModels.TAPERED,
//				SlipAlongRuptureModels.UNIFORM };

//		AveSlipForRupModels[] aveSlipModels = { AveSlipForRupModels.AVE_UCERF2 };
		AveSlipForRupModels[] aveSlipModels = AveSlipForRupModels.values();
		
		// do all branch choices relative to these:
//		Branch defaultBranch = null;
		int maxAway = 1;
		LogicTreeBranch[] defaultBranches = { new LogicTreeBranch(null, null, MagAreaRelationships.AVE_UCERF2,
								AveSlipForRupModels.AVE_UCERF2, null, InversionModels.CHAR),
							 new LogicTreeBranch(null, null, MagAreaRelationships.AVE_UCERF2,
								AveSlipForRupModels.AVE_UCERF2, null, InversionModels.UNCONSTRAINED)};
		if (defaultBranches != null) {
			// make sure all default branch choices are valid!
			for (LogicTreeBranch defaultBranch : defaultBranches) {
				if (defaultBranch.getFaultModel() != null &&
						!Arrays.asList(faultModels).contains(defaultBranch.getFaultModel()))
					defaultBranch.setFaultModel(null);
				if (defaultBranch.getDefModel() != null &&
						!Arrays.asList(defModels).contains(defaultBranch.getDefModel()))
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

		File writeDir;
		if (args.length > 0)
			writeDir = new File(new File(args[0]), runName);
		else
			writeDir = new File(new File("/home/kevin/OpenSHA/UCERF3/inversions"), runName);
		if (!writeDir.exists())
			writeDir.mkdir();

		File runSubDir = new File(RUN_DIR, runName);

		//		String queue = "nbns";
		String queue = null;
		//		BatchScriptWriter batch = new USC_HPCC_ScriptWriter("pe1950");
		//		BatchScriptWriter batch = new USC_HPCC_ScriptWriter("quadcore");
		File javaBin = USC_HPCC_ScriptWriter.JAVA_BIN;
		String threads = "95%"; // max for 8 core nodes, 23/24 for dodecacore
		CoolingScheduleType cool = CoolingScheduleType.FAST_SA;
		CompletionCriteria subCompletion = TimeCompletionCriteria.getInSeconds(1);
		JavaShellScriptWriter javaWriter = new JavaShellScriptWriter(javaBin, -1, getClasspath());
		javaWriter.setHeadless(true);
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
							String baseName = fm.getShortName()+"_"+dm.getShortName()
									+"_Ma"+ma.getShortName()+"_Dsr"+sal.getShortName()+"_Dr"+as.getShortName();

							File localRupSetFile = new File(writeDir, baseName+"_rupSet.zip");
							File remoteRupSetFile = new File(runSubDir, baseName+"_rupSet.zip");

							for (InversionModels im : inversionModels) {
								if (defaultBranches != null && defaultBranches.length > 0) {
									LogicTreeBranch branch = new LogicTreeBranch(fm, dm, ma, as, sal, im);
									int closest = Integer.MAX_VALUE;
									for (LogicTreeBranch defaultBranch : defaultBranches) {
										int away = defaultBranch.getNumAwayFrom(branch);
										if (away < closest)
											closest = away;
									}
									if (closest > maxAway)
										continue;
								}
								
								
								if (buildRupSets && !localRupSetFile.exists()) {
									SimpleFaultSystemRupSet rupSet = new SimpleFaultSystemRupSet(
											InversionFaultSystemRupSetFactory.forBranch(fm, dm, ma, as, sal));
									rupSet.toZipFile(localRupSetFile);
									System.gc();
								}
								
								int mins;
								NonnegativityConstraintType nonNeg;
								int ppn; // minimum number of cpus
								int heapSizeMB;
								BatchScriptWriter batch;
								TimeCompletionCriteria checkPointCritera;
								if (im == InversionModels.GR) {
									mins = 500;
									nonNeg = NonnegativityConstraintType.PREVENT_ZERO_RATES;
									ppn = 24;
									heapSizeMB = 40000;
									batch = new USC_HPCC_ScriptWriter("dodecacore");
									checkPointCritera = TimeCompletionCriteria.getInHours(2);
								} else if (im == InversionModels.CHAR) {
									mins = 500; // TODO ?
									nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
									ppn = 8;
									heapSizeMB = 10000;
									batch = new USC_HPCC_ScriptWriter("quadcore");
									checkPointCritera = TimeCompletionCriteria.getInHours(2);
								} else { // UNCONSTRAINED
									mins = 60;
									nonNeg = NonnegativityConstraintType.LIMIT_ZERO_RATES;
									ppn = 8;
									heapSizeMB = 10000;
									batch = new USC_HPCC_ScriptWriter("quadcore");
									checkPointCritera = TimeCompletionCriteria.getInMinutes(15);
								}
								CompletionCriteria criteria = TimeCompletionCriteria.getInMinutes(mins);
								tsa_create.setCriteria(criteria);
								javaWriter.setHeapSizeMB(heapSizeMB);
								tsa_create.setNonNeg(nonNeg);
								tsa_create.setCheckPointCriteria(checkPointCritera);
								
								String name = baseName+"_"+im.getShortName();

								for (int r=0; r<numRuns; r++) {
									String jobName = name;
									if (numRuns > 1)
										jobName += "_run"+r;

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
									argss.add(remoteRupSetFile.getAbsolutePath()+" "+im.name()
											+" "+remoteInputs.getAbsolutePath());
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
		System.out.println("Wrote "+cnt+" jobs");
		System.out.println("Node hours: "+(float)nodeHours + " (/60: "+((float)nodeHours/60f)+")");
		//		DeformationModels.forFaultModel(null).toArray(new DeformationModels[0])
		System.exit(0);
	}

}
