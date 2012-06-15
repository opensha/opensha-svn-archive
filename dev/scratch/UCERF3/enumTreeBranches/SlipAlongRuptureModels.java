package scratch.UCERF3.enumTreeBranches;

public enum SlipAlongRuptureModels implements LogicTreeBranchNode<SlipAlongRuptureModels> {
	// DO NOT RENAME THESE - they are used in rupture set files
	
	CHAR(		"Characteristic",	"Char",	0d),	// "Characteristic (Dsr=Ds)"
	UNIFORM(	"Uniform",			"Uni",	1d),	// "Uniform/Boxcar (Dsr=Dr)"
	WG02(		"WGCEP-2002",		"WG02",	0d),	// "WGCEP-2002 model (Dsr prop to Vs)"
	TAPERED(	"Tapered Ends",		"Tap",	1d);	// "Tapered Ends ([Sin(x)]^0.5)"
	
	private String name, shortName;
	private double weight;
	
	private SlipAlongRuptureModels(String name, String shortName, double weight) {
		this.name = name;
		this.shortName = shortName;
		this.weight = weight;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public double getRelativeWeight() {
		return weight;
	}

	@Override
	public String encodeChoiceString() {
		return "Dsr"+getShortName();
	}
}