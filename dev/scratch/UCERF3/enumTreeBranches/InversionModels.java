package scratch.UCERF3.enumTreeBranches;

public enum InversionModels {
	
	CHAR("Characteristic"),
	GR("Gutenberg-Richter"),
	UNCONSTRAINED("Unconstrained");
	
	private String name;
	
	private InversionModels(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public static InversionModels getTypeForName(String name) {
		if (name == null) throw new NullPointerException();
		for (InversionModels inv:InversionModels.values()) {
			if (inv.name.equals(name) || inv.name().equals(name)) return inv;
		}
		throw new IllegalArgumentException("InversionModelBranches name does not exist");
	}
}
