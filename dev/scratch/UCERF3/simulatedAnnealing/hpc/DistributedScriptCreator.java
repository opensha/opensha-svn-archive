package scratch.UCERF3.simulatedAnnealing.hpc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.hpc.mpj.MPJShellScriptWriter;

import com.google.common.base.Preconditions;

import scratch.UCERF3.simulatedAnnealing.DistributedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;


public class DistributedScriptCreator extends ThreadedScriptCreator {
	
	private CompletionCriteria distSubCompletion = null;

	public DistributedScriptCreator(MPJShellScriptWriter mpj, File aMat, File dMat, File initial,
			int numThreads, File solFile, CompletionCriteria criteria, CompletionCriteria subCompletion,
			File mpjHome, boolean useMxdev) {
		super(mpj, aMat, dMat, initial,
				numThreads, solFile, criteria, subCompletion);
		Preconditions.checkNotNull(mpjHome, "MPJ_HOME cannot be null!");
	}

	public void setDistSubCompletion(CompletionCriteria distSubCompletion) {
		this.distSubCompletion = distSubCompletion;
	}

	@Override
	String getArgs() {
		String args = super.getArgs();
		
		if (distSubCompletion != null) {
			args += " "+ThreadedSimulatedAnnealing.subCompletionCriteriaToArgument(
					"dist-sub-completion", distSubCompletion);
		}
		
		return args;
	}

	@Override
	String getClassName() {
		return DistributedSimulatedAnnealing.class.getName();
	}

}
