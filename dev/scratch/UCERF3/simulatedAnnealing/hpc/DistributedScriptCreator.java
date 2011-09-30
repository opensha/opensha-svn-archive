package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;

import com.google.common.base.Preconditions;

import scratch.UCERF3.simulatedAnnealing.DistributedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;


public class DistributedScriptCreator extends ThreadedScriptCreator {
	
	private int numDistSubIterations = -1;

	public DistributedScriptCreator(MPJShellScriptWriter mpj, File aMat, File dMat, File initial,
			int subIterations, int numThreads, File solFile, CompletionCriteria criteria,
			File mpjHome, boolean useMxdev) {
		super(mpj, aMat, dMat, initial, subIterations,
				numThreads, solFile, criteria);
		Preconditions.checkNotNull(mpjHome, "MPJ_HOME cannot be null!");
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
	String getClassName() {
		return DistributedSimulatedAnnealing.class.getName();
	}

}
