package scratch.UCERF3.logicTree;

public interface BranchWeightProvider {
	
	/**
	 * Returns the weight for the given branch. Will not necessarily be normalized.
	 * @param branch
	 * @return
	 */
	public double getWeight(LogicTreeBranch branch);

}
