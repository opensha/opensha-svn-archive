package scratch.UCERF3.enumTreeBranches;

public enum RelaxMFDConstraint implements LogicTreeBranchNode<RelaxMFDConstraint> {
	
	TRUE("Relax MFD Constraint Weights",	"RelaxMFD",	true,	0d),
	FALSE("Default MFD Constraint Weights",	"NormalMFD",	false,	0d);
	
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
