package scratch.UCERF3.logicTree;

import java.io.Serializable;

public interface BranchWeightProvider extends Serializable {
	
	/**
	 * Returns the weight for the given branch. Will not necessarily be normalized.
	 * @param branch
	 * @return
	 */
	public double getWeight(LogicTreeBranch branch);

}
