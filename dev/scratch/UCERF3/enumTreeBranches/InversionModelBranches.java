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
	
	public static InversionModelBranches getTypeForName(String name) {
		if (name == null) throw new NullPointerException();
		for (InversionModelBranches inv:InversionModelBranches.values()) {
			if (inv.name.equals(name) || inv.name().equals(name)) return inv;
		}
		throw new IllegalArgumentException("InversionModelBranches name does not exist");
	}
}
