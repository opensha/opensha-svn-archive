package scratch.UCERF3.enumTreeBranches;

public enum MomentRateFixes implements LogicTreeBranchNode<MomentRateFixes> {
	// TODO set weights
	APPLY_IMPLIED_CC(		"Apply Implied Coupling Coefficient",		"ApplyCC",	1d),
	RELAX_MFD(				"Relaxed (weak) MFD Constraint Weights",	"RelaxMFD",	1d),
	APPLY_CC_AND_RELAX_MFD(	"Apply Implied CC and Relax MFD",			"ApplyCC",	0d),
	NONE(					"No Moment Rate Fixes",						"NoFix",	1d);
	
	private String name, shortName;
	private double weight;

	private MomentRateFixes(String name, String shortName, double weight) {
		this.name = name;
		this.shortName = shortName;
		this.weight = weight;
	}
	
	public boolean isRelaxMFD() {
		return this == RELAX_MFD || this == APPLY_CC_AND_RELAX_MFD;
	}
	
	public boolean isApplyCC() {
		return this == APPLY_IMPLIED_CC || this == APPLY_CC_AND_RELAX_MFD;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getName() {
		return name;
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
