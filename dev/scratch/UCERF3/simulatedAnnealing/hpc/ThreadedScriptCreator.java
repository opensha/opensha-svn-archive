package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.GenerationFunctionType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;

public class ThreadedScriptCreator {
	
	// required -- java
	private File javaBin;
	private List<File> jars;
	private int heapSizeMB;
	
	// required -- args
	private File aMat;
	private File dMat;
	private File initial;
	private int subIterations;
	private int numThreads;
	private File solFile;
	private CompletionCriteria criteria;
	
	// optional -- args
	private File progFile;
	private CoolingScheduleType cool;
	private GenerationFunctionType perturb;
	private NonnegativityConstraintType nonNeg;
	private boolean setSubIterationsZero = false;
	
	public ThreadedScriptCreator(File javaBin, List<File> jars, int heapSizeMB,
			File aMat, File dMat, File initial, int subIterations, int numThreads,
			File solFile, CompletionCriteria criteria) {
		this.javaBin = javaBin;
		this.jars = jars;
		this.heapSizeMB = heapSizeMB;
		
		this.aMat = aMat;
		this.dMat = dMat;
		this.initial = initial;
		this.subIterations = subIterations;
		this.numThreads = numThreads;
		this.solFile = solFile;
		this.criteria = criteria;
	}
	
	String getJavaCommand() {
		return getJavaCommand(ThreadedSimulatedAnnealing.class.getName());
	}
	
	String getJavaArgs(String className) {
		String cp = "";
		if (jars != null && !jars.isEmpty()) {
			cp = " -cp ";
			for (int i=0; i<jars.size(); i++) {
				if (i>0)
					cp += ":";
				cp += jars.get(i).getAbsolutePath();
			}
		}
		
		String heap = "";
		if (heapSizeMB > 0)
			heap = " -Xmx"+heapSizeMB+"M";
		
		return heap+cp+" "+className;
	}
	
	String getJavaCommand(String className) {
		
		
		return javaBin.getAbsolutePath()+getJavaArgs(className);
	}
	
	String getArgs() {
		Preconditions.checkNotNull(aMat, "A matrix file is required!");
		Preconditions.checkNotNull(dMat, "d matrix file is required!");
		Preconditions.checkNotNull(initial, "initial file is required!");
		Preconditions.checkState(subIterations > 0, "subIterations must be > 0");
		Preconditions.checkState(numThreads > 0, "numThreads must be > 0");
		Preconditions.checkNotNull(solFile, "solution file is required!");
		String args =	  "--a-matrix-file "+aMat.getAbsolutePath()
						+" --d-matrix-file "+dMat.getAbsolutePath()
						+" --initial-state-file "+initial.getAbsolutePath()
						+" --sub-iterations "+subIterations
						+" --num-threads "+numThreads
						+" --solution-file "+solFile.getAbsolutePath();
		
		Preconditions.checkNotNull(criteria, "Criteria cannot be null!");
		args += " "+ThreadedSimulatedAnnealing.completionCriteriaToArgument(criteria);
		
		if (progFile != null)
			args +=		 " --progress-file "+progFile.getAbsolutePath();
		if (setSubIterationsZero)
			args +=		 " --start-sub-iters-zero";
		if (cool != null)
			args +=		 " --cool "+cool.name();
		if (perturb != null)
			args +=		 " --perturb "+perturb.name();
		if (nonNeg != null)
			args +=		 " --nonneg "+nonNeg.name();
		
		return args;
	}
	
	String buildCommand() {
		return getJavaCommand()+" "+getArgs();
	}
	
	public List<String> buildPBSScript(int mins, int nodes, int ppn, String queue) {
		ArrayList<String> pbs = new ArrayList<String>();
		
		if (queue != null && !queue.isEmpty())
			pbs.add("#PBS -q "+queue);
		pbs.add("#PBS -l walltime=00:"+mins+":00,nodes="+nodes+":ppn="+ppn);
		pbs.add("#PBS -V");
		pbs.add("");
		
		List<String> script = buildScript();
		
		script.addAll(2, pbs);
		
		return script;
	}
	
	public List<String> buildScript() {
		ArrayList<String> script = new ArrayList<String>();
		
		script.add("#!/bin/bash");
		script.add("");
		script.add(buildCommand());
		script.add("exit $?");
		
		return script;
	}
	
	public void writeScript(File file, List<String> script) throws IOException {
		FileWriter fw = new FileWriter(file);
		
		for (String line: script)
			fw.write(line + "\n");
		
		fw.close();
	}

	public void setaMat(File aMat) {
		this.aMat = aMat;
	}

	public void setdMat(File dMat) {
		this.dMat = dMat;
	}

	public void setInitial(File initial) {
		this.initial = initial;
	}

	public void setSubIterations(int subIterations) {
		this.subIterations = subIterations;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public void setSolFile(File solFile) {
		this.solFile = solFile;
	}

	public void setProgFile(File progFile) {
		this.progFile = progFile;
	}

	public void setCriteria(CompletionCriteria criteria) {
		this.criteria = criteria;
	}

	public void setCool(CoolingScheduleType cool) {
		this.cool = cool;
	}

	public void setPerturb(GenerationFunctionType perturb) {
		this.perturb = perturb;
	}

	public void setNonNeg(NonnegativityConstraintType nonNeg) {
		this.nonNeg = nonNeg;
	}
	
	public void setSubIterationsZero(boolean setSubIterationsZero) {
		this.setSubIterationsZero = setSubIterationsZero;
	}

}
