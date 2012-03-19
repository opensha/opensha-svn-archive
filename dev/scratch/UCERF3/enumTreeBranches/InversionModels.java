package scratch.UCERF3.enumTreeBranches;

import org.opensha.commons.data.ShortNamed;

public enum InversionModels implements ShortNamed {
	
	CHAR("Characteristic", "Char"),
	GR("Gutenberg-Richter", "GR"),
	UNCONSTRAINED("Unconstrained", "Unconst");
	
	private String name, shortName;
	
	private InversionModels(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
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
	
	public static InversionModels getTypeForName(String name) {
		if (name == null) throw new NullPointerException();
		for (InversionModels inv:InversionModels.values()) {
			if (inv.name.equals(name) || inv.name().equals(name) || inv.shortName.equals(name)) return inv;
		}
		throw new IllegalArgumentException("InversionModels name does not exist");
	}
}
