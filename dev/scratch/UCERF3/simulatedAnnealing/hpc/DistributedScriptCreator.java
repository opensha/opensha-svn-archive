package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import scratch.UCERF3.simulatedAnnealing.DistributedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;


public class DistributedScriptCreator extends ThreadedScriptCreator {
	
	private File mpjHome;
	private boolean useMxdev;
	private int numDistSubIterations = -1;

	public DistributedScriptCreator(File javaBin, List<File> jars,
			int heapSizeMB, File aMat, File dMat, File initial,
			int subIterations, int numThreads, File solFile, CompletionCriteria criteria,
			File mpjHome, boolean useMxdev) {
		super(javaBin, jars, heapSizeMB, aMat, dMat, initial, subIterations,
				numThreads, solFile, criteria);
		Preconditions.checkNotNull(mpjHome, "MPJ_HOME cannot be null!");
		this.mpjHome = mpjHome;
		this.useMxdev = useMxdev;
	}

	public void setMpjHome(File mpjHome) {
		this.mpjHome = mpjHome;
	}

	public void setUseMxdev(boolean useMxdev) {
		this.useMxdev = useMxdev;
	}

	public void setNumDistSubIterations(int numDistSubIterations) {
		this.numDistSubIterations = numDistSubIterations;
	}

	@Override
	String getArgs() {
		String args = super.getArgs();
		
		if (numDistSubIterations > 0) {
			args += " --dist-sub-iterations "+numDistSubIterations;
		}
		
		return args;
	}

	@Override
	String getJavaCommand() {
		return getJavaCommand(DistributedSimulatedAnnealing.class.getName());
	}

	@Override
	public List<String> buildScript() {
		ArrayList<String> script = new ArrayList<String>();
		
		script.add("#!/bin/bash");
		script.add("");
		script.add("export MPJ_HOME="+mpjHome.getAbsolutePath());
		script.add("export PATH=$PATH:$MPJ_HOME/bin");
		script.add("");
		script.add("if [[ -e $PBS_NODEFILE ]]; then");
		script.add("  #count the number of processors assigned by PBS");
		script.add("  NP=`wc -l < $PBS_NODEFILE`");
		script.add("  echo \"Running on $NP processors: \"`cat $PBS_NODEFILE`");
		script.add("else");
		script.add("  echo \"This script must be submitted to PBS with 'qsub -l nodes=X'\"");
		script.add("  exit 1");
		script.add("fi");
		script.add("");
		script.add("if [[ $NP -le 0 ]]; then");
		script.add("  echo \"invalid NP: $NP\"");
		script.add("  exit 1");
		script.add("fi");
		script.add("");
		script.add("date");
		script.add("echo \"STARTING MPJ\"");
		script.add("mpjboot $PBS_NODEFILE");
		script.add("");
		script.add("date");
		script.add("echo \"RUNNING MPJ\"");
		
		String dev;
		if (useMxdev)
			dev = "mxdev";
		else
			dev = "niodev";
		String command = "mpjrun.sh -machinesfile $PBS_NODEFILE -np $NP -dev "+dev+" -Djava.library.path=$MPJ_HOME/lib";
		command += getJavaArgs(DistributedSimulatedAnnealing.class.getName())+" "+getArgs();
		
		script.add(command);
		script.add("ret=$?");
		script.add("");
		script.add("date");
		script.add("echo \"HALTING MPJ\"");
		script.add("mpjhalt $PBS_NODEFILE");
		script.add("");
		script.add("exit $ret");
		
		return script;
	}

}
