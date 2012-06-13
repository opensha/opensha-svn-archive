package scratch.UCERF3.enumTreeBranches;

public enum RelaxMFDConstraint implements LogicTreeBranchNode<RelaxMFDConstraint> {
	
	TRUE("Weak (relaxed) MFD Constraint Weights",	"WeakMFD",	true,	0d),
	FALSE("Strong (default) MFD Constraint Weights",	"StrongMFD",	false,	0d);
	
	private String name, shortName;
	private boolean value;
	private double weight;
	
	private RelaxMFDConstraint(String name, String shortName, boolean value, double weight) {
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
