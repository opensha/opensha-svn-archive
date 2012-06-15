package scratch.UCERF3.enumTreeBranches;

public enum TotalMag5Rate implements LogicTreeBranchNode<TotalMag5Rate> {
	// TODO finalize values from Karen
	// TODO set weights
	RATE_7p2(7.2,	1d),
	RATE_8p8(8.8,	1d),
	RATE_10p6(10.6,	1d);
	
	private double rate;
	private double weight;
	
	private TotalMag5Rate(double rate, double weight) {
		this.rate = rate;
		this.weight = weight;
	}

	@Override
	public String getName() {
		String name = (float)rate+"";
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
	
	public double getRateMag5() {
		return rate;
	}

	@Override
	public String encodeChoiceString() {
		return "M5Rate"+getShortName();
	}

}
