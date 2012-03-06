package scratch.UCERF3.enumTreeBranches;

import org.opensha.commons.data.ShortNamed;

public enum SlipAlongRuptureModels implements ShortNamed {
	// DO NOT RENAME THESE - they are used in rupture set files
	CHAR("Characteristic", "Char"),	// "Characteristic (Dsr=Ds)"
	UNIFORM("Uniform", "Uni"),	// "Uniform/Boxcar (Dsr=Dr)"
	WG02("WGCEP-2002", "WG02"),	// "WGCEP-2002 model (Dsr prop to Vs)"
	TAPERED("Tapered Ends", "Tap");	// "Tapered Ends ([Sin(x)]^0.5)"
	
	private String name, shortName;
	
	private SlipAlongRuptureModels(String name, String shortName) {
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
}