package scratch.UCERF3.enumTreeBranches;

import scratch.UCERF3.logicTree.LogicTreeBranchNode;

public enum MomentRateFixes implements LogicTreeBranchNode<MomentRateFixes> {
	// TODO set weights for GR
	APPLY_IMPLIED_CC(		"Apply Implied Coupling Coefficient",		"ApplyCC",	0.4d),	// TODO 0.5 for GR
	RELAX_MFD(				"Relaxed (weak) MFD Constraint Weights",	"RelaxMFD",	0.2d),	// TODO 0.0 for GR
	APPLY_CC_AND_RELAX_MFD(	"Apply Implied CC and Relax MFD",			"ApplyCC",	0d),	// (same for GR)
	NONE(					"No Moment Rate Fixes",						"NoFix",	0.4d);	// TODO 0.5 for GR
	
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
