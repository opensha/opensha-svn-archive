package scratch.UCERF3.enumTreeBranches;

import scratch.UCERF3.logicTree.LogicTreeBranchNode;

public enum MaxMagOffFault implements LogicTreeBranchNode<MaxMagOffFault> {
	
	MAG_7p3(7.2, 0.3d, 0.1d),
	MAG_7p6(7.6, 0.6d, 0.8d),
	MAG_7p9(8.0, 0.1d, 0.1d),
	
	
	// old mags kept for compatibility (for now)
	// TODO: remove
	@Deprecated
	MAG_7p2(7.2, 0.3d, 0.0d),
	@Deprecated
	MAG_8p0(8.0, 0.1d, 0.0d);

	private double mmax;
	private double charWeight, grWeight;

	private MaxMagOffFault(double mmax, double charWeight, double grWeight) {
		this.mmax = mmax;
		this.charWeight = charWeight;
		this.grWeight = grWeight;
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
	public double getRelativeWeight(InversionModels im) {
		if (im.isCharacteristic())
			return charWeight;
		else
			return grWeight;
	}

	public double getMaxMagOffFault() {
		return mmax;
	}

	@Override
	public String encodeChoiceString() {
		return "MMaxOff"+getShortName();
	}
}
