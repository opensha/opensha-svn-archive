package scratch.UCERF3.enumTreeBranches;

import scratch.UCERF3.logicTree.LogicTreeBranchNode;

public enum MaxMagOffFault implements LogicTreeBranchNode<MaxMagOffFault> {
	
	// TODO set weights different for GR?!?!?! :(
	MAG_7p2(7.2, 0.3d), // TODO: 0.2 for GR
	MAG_7p6(7.6, 0.6d), // (same for GR)
	MAG_8p0(8.0, 0.1d); // TODO: 0.2 for GR

	private double mmax;
	private double weight;

	private MaxMagOffFault(double mmax, double weight) {
		this.mmax = mmax;
		this.weight = weight;
	}

	@Override
	public String getName() {
		String name = (float)mmax+"";
		if (!name.contains("."))
			name += ".0";
		return name;
	}

	@Override
	public String getShortName() {
		return getName();
	}

	@Override
	public double getRelativeWeight() {
		return weight;
	}

	public double getMaxMagOffFault() {
		return mmax;
	}

	@Override
	public String encodeChoiceString() {
		return "MMaxOff"+getShortName();
	}
}
