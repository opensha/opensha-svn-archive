package scratch.UCERF3.enumTreeBranches;

public enum ApplyImpliedCouplingCoeff implements
		LogicTreeBranchNode<ApplyImpliedCouplingCoeff> {
	TRUE("Apply Implied Coupling Coefficient",		"ApplyCC",	true,	0d),
	FALSE("Ignore Implied Coupling Coefficient",	"IgnoreCC",	false,	0d);
	
	private String name, shortName;
	private boolean value;
	private double weight;
	
	private ApplyImpliedCouplingCoeff(String name, String shortName, boolean value, double weight) {
		this.name = name;
		this.shortName = shortName;
		this.value = value;
		this.weight = weight;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public boolean getValue() {
		return value;
	}

	@Override
	public double getRelativeWeight() {
		return weight;
	}

	@Override
	public String encodeChoiceString() {
		return getShortName();
	}

}
