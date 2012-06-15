package scratch.UCERF3.enumTreeBranches;

public enum TotalMag5Rate implements LogicTreeBranchNode<TotalMag5Rate> {
	// TODO finalize values from Karen
	// TODO set weights
	RATE_7p1(7.1,	0.2d),
	RATE_8p7(8.7,	0.6d),
	RATE_10p6(10.6,	0.2d);
	
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
