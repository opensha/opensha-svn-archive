package scratch.UCERF3.enumTreeBranches;

public enum InversionModelBranches {
	
	CHAR("Characteristic"),
	GR("Gutenberg-Richter"),
	UNCONSTRAINED("Unconstrained");
	
	private String name;
	
	private InversionModelBranches(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
